package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.lzma.Base;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class BitmapFont implements Disposable {
    private static final int LOG2_PAGE_SIZE = 9;
    private static final int PAGES = 128;
    private static final int PAGE_SIZE = 512;
    private final BitmapFontCache cache;
    final BitmapFontData data;
    private boolean flipped;
    boolean integer;
    private boolean ownsTexture;
    Array<TextureRegion> regions;

    public static class BitmapFontData {
        public float ascent;
        public char[] breakChars;
        public char[] capChars;
        public float capHeight;
        public float cursorX;
        public float descent;
        public float down;
        public boolean flipped;
        public FileHandle fontFile;
        public final Glyph[][] glyphs;
        public String[] imagePaths;
        public float lineHeight;
        public boolean markupEnabled;
        public float padBottom;
        public float padLeft;
        public float padRight;
        public float padTop;
        public float scaleX;
        public float scaleY;
        public float spaceWidth;
        public char[] xChars;
        public float xHeight;

        public BitmapFontData() {
            this.capHeight = 1.0f;
            this.scaleX = 1.0f;
            this.scaleY = 1.0f;
            this.glyphs = new Glyph[BitmapFont.PAGES][];
            this.xHeight = 1.0f;
            this.xChars = new char[]{'x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z'};
            this.capChars = new char[]{'M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A', 'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
        }

        public BitmapFontData(FileHandle fontFile, boolean flip) {
            this.capHeight = 1.0f;
            this.scaleX = 1.0f;
            this.scaleY = 1.0f;
            this.glyphs = new Glyph[BitmapFont.PAGES][];
            this.xHeight = 1.0f;
            this.xChars = new char[]{'x', 'e', 'a', 'o', 'n', 's', 'r', 'c', 'u', 'm', 'v', 'w', 'z'};
            this.capChars = new char[]{'M', 'N', 'B', 'D', 'C', 'E', 'F', 'K', 'A', 'G', 'H', 'I', 'J', 'L', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            this.fontFile = fontFile;
            this.flipped = flip;
            load(fontFile, flip);
        }

        public void load(FileHandle fontFile, boolean flip) {
            if (this.imagePaths != null) {
                throw new IllegalStateException("Already loaded.");
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fontFile.read()), BitmapFont.PAGE_SIZE);
            String line = bufferedReader.readLine();
            if (line == null) {
                throw new GdxRuntimeException("File is empty.");
            }
            line = line.substring(line.indexOf("padding=") + 8);
            String[] padding = line.substring(0, line.indexOf(32)).split(",", 4);
            if (padding.length != 4) {
                throw new GdxRuntimeException("Invalid padding.");
            }
            this.padTop = (float) Integer.parseInt(padding[0]);
            this.padLeft = (float) Integer.parseInt(padding[1]);
            this.padBottom = (float) Integer.parseInt(padding[2]);
            this.padRight = (float) Integer.parseInt(padding[3]);
            float padY = this.padTop + this.padBottom;
            line = bufferedReader.readLine();
            if (line == null) {
                throw new GdxRuntimeException("Missing common header.");
            }
            String[] common = line.split(" ", 7);
            if (common.length < 3) {
                throw new GdxRuntimeException("Invalid common header.");
            } else if (common[1].startsWith("lineHeight=")) {
                this.lineHeight = (float) Integer.parseInt(common[1].substring(11));
                if (common[2].startsWith("base=")) {
                    float baseLine = (float) Integer.parseInt(common[2].substring(5));
                    int pageCount = 1;
                    if (common.length >= 6 && common[5] != null && common[5].startsWith("pages=")) {
                        try {
                            pageCount = Math.max(1, Integer.parseInt(common[5].substring(6)));
                        } catch (NumberFormatException e) {
                        }
                    }
                    String[] pageLine;
                    try {
                        Glyph glyph;
                        StringTokenizer stringTokenizer;
                        this.imagePaths = new String[pageCount];
                        int p = 0;
                        while (p < pageCount) {
                            line = bufferedReader.readLine();
                            if (line == null) {
                                throw new GdxRuntimeException("Missing additional page definitions.");
                            }
                            pageLine = line.split(" ", 4);
                            if (pageLine[2].startsWith("file=")) {
                                String fileName;
                                if (pageLine[1].startsWith("id=")) {
                                    if (Integer.parseInt(pageLine[1].substring(3)) != p) {
                                        throw new GdxRuntimeException("Page IDs must be indices starting at 0: " + pageLine[1].substring(3));
                                    }
                                }
                                if (pageLine[2].endsWith("\"")) {
                                    fileName = pageLine[2].substring(6, pageLine[2].length() - 1);
                                } else {
                                    fileName = pageLine[2].substring(5, pageLine[2].length());
                                }
                                this.imagePaths[p] = fontFile.parent().child(fileName).path().replaceAll("\\\\", "/");
                                p++;
                            } else {
                                throw new GdxRuntimeException("Missing: file");
                            }
                        }
                        this.descent = 0.0f;
                        while (true) {
                            line = bufferedReader.readLine();
                            if (!(line == null || line.startsWith("kernings "))) {
                                if (line.startsWith("char ")) {
                                    glyph = new Glyph();
                                    stringTokenizer = new StringTokenizer(line, " =");
                                    stringTokenizer.nextToken();
                                    stringTokenizer.nextToken();
                                    int ch = Integer.parseInt(stringTokenizer.nextToken());
                                    if (ch <= 65535) {
                                        setGlyph(ch, glyph);
                                        glyph.id = ch;
                                        stringTokenizer.nextToken();
                                        glyph.srcX = Integer.parseInt(stringTokenizer.nextToken());
                                        stringTokenizer.nextToken();
                                        glyph.srcY = Integer.parseInt(stringTokenizer.nextToken());
                                        stringTokenizer.nextToken();
                                        glyph.width = Integer.parseInt(stringTokenizer.nextToken());
                                        stringTokenizer.nextToken();
                                        glyph.height = Integer.parseInt(stringTokenizer.nextToken());
                                        stringTokenizer.nextToken();
                                        glyph.xoffset = Integer.parseInt(stringTokenizer.nextToken());
                                        stringTokenizer.nextToken();
                                        if (flip) {
                                            glyph.yoffset = Integer.parseInt(stringTokenizer.nextToken());
                                        } else {
                                            glyph.yoffset = -(glyph.height + Integer.parseInt(stringTokenizer.nextToken()));
                                        }
                                        stringTokenizer.nextToken();
                                        glyph.xadvance = Integer.parseInt(stringTokenizer.nextToken());
                                        if (stringTokenizer.hasMoreTokens()) {
                                            stringTokenizer.nextToken();
                                        }
                                        if (stringTokenizer.hasMoreTokens()) {
                                            try {
                                                glyph.page = Integer.parseInt(stringTokenizer.nextToken());
                                            } catch (NumberFormatException e2) {
                                            }
                                        }
                                        if (glyph.width > 0 && glyph.height > 0) {
                                            this.descent = Math.min(((float) glyph.yoffset) + baseLine, this.descent);
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                        }
                        this.descent += this.padBottom;
                        while (true) {
                            line = bufferedReader.readLine();
                            if (line != null && line.startsWith("kerning ")) {
                                stringTokenizer = new StringTokenizer(line, " =");
                                stringTokenizer.nextToken();
                                stringTokenizer.nextToken();
                                int first = Integer.parseInt(stringTokenizer.nextToken());
                                stringTokenizer.nextToken();
                                int second = Integer.parseInt(stringTokenizer.nextToken());
                                if (first >= 0 && first <= 65535 && second >= 0 && second <= 65535) {
                                    glyph = getGlyph((char) first);
                                    stringTokenizer.nextToken();
                                    int amount = Integer.parseInt(stringTokenizer.nextToken());
                                    if (glyph != null) {
                                        glyph.setKerning(second, amount);
                                    }
                                }
                            }
                        }
                        Glyph spaceGlyph = getGlyph(' ');
                        if (spaceGlyph == null) {
                            spaceGlyph = new Glyph();
                            spaceGlyph.id = 32;
                            Glyph xadvanceGlyph = getGlyph('l');
                            if (xadvanceGlyph == null) {
                                xadvanceGlyph = getFirstGlyph();
                            }
                            spaceGlyph.xadvance = xadvanceGlyph.xadvance;
                            setGlyph(32, spaceGlyph);
                        }
                        if (spaceGlyph.width == 0) {
                            spaceGlyph.width = (int) (((float) spaceGlyph.xadvance) + this.padRight);
                            spaceGlyph.xoffset = (int) (-this.padLeft);
                        }
                        this.spaceWidth = (float) spaceGlyph.width;
                        Glyph xGlyph = null;
                        for (char glyph2 : this.xChars) {
                            xGlyph = getGlyph(glyph2);
                            if (xGlyph != null) {
                                break;
                            }
                        }
                        if (xGlyph == null) {
                            xGlyph = getFirstGlyph();
                        }
                        this.xHeight = ((float) xGlyph.height) - padY;
                        Glyph capGlyph = null;
                        for (char glyph22 : this.capChars) {
                            capGlyph = getGlyph(glyph22);
                            if (capGlyph != null) {
                                break;
                            }
                        }
                        if (capGlyph == null) {
                            for (Glyph[] page : this.glyphs) {
                                if (page != null) {
                                    for (Glyph glyph3 : page) {
                                        if (!(glyph3 == null || glyph3.height == 0 || glyph3.width == 0)) {
                                            this.capHeight = Math.max(this.capHeight, (float) glyph3.height);
                                        }
                                    }
                                }
                            }
                        } else {
                            this.capHeight = (float) capGlyph.height;
                        }
                        this.capHeight -= padY;
                        this.ascent = baseLine - this.capHeight;
                        this.down = -this.lineHeight;
                        if (flip) {
                            this.ascent = -this.ascent;
                            this.down = -this.down;
                        }
                        StreamUtils.closeQuietly(bufferedReader);
                    } catch (NumberFormatException ex) {
                        throw new GdxRuntimeException("Invalid page id: " + pageLine[1].substring(3), ex);
                    } catch (Exception ex2) {
                        throw new GdxRuntimeException("Error loading font file: " + fontFile, ex2);
                    } catch (Throwable th) {
                        StreamUtils.closeQuietly(bufferedReader);
                    }
                } else {
                    throw new GdxRuntimeException("Missing: base");
                }
            } else {
                throw new GdxRuntimeException("Missing: lineHeight");
            }
        }

        public void setGlyphRegion(Glyph glyph, TextureRegion region) {
            Texture texture = region.getTexture();
            float invTexWidth = 1.0f / ((float) texture.getWidth());
            float invTexHeight = 1.0f / ((float) texture.getHeight());
            float offsetX = 0.0f;
            float offsetY = 0.0f;
            float u = region.u;
            float v = region.v;
            float regionWidth = (float) region.getRegionWidth();
            float regionHeight = (float) region.getRegionHeight();
            if (region instanceof AtlasRegion) {
                AtlasRegion atlasRegion = (AtlasRegion) region;
                offsetX = atlasRegion.offsetX;
                offsetY = ((float) (atlasRegion.originalHeight - atlasRegion.packedHeight)) - atlasRegion.offsetY;
            }
            float x = (float) glyph.srcX;
            float x2 = (float) (glyph.srcX + glyph.width);
            float y = (float) glyph.srcY;
            float y2 = (float) (glyph.srcY + glyph.height);
            if (offsetX > 0.0f) {
                x -= offsetX;
                if (x < 0.0f) {
                    glyph.width = (int) (((float) glyph.width) + x);
                    glyph.xoffset = (int) (((float) glyph.xoffset) - x);
                    x = 0.0f;
                }
                x2 -= offsetX;
                if (x2 > regionWidth) {
                    glyph.width = (int) (((float) glyph.width) - (x2 - regionWidth));
                    x2 = regionWidth;
                }
            }
            if (offsetY > 0.0f) {
                y -= offsetY;
                if (y < 0.0f) {
                    glyph.height = (int) (((float) glyph.height) + y);
                    y = 0.0f;
                }
                y2 -= offsetY;
                if (y2 > regionHeight) {
                    float amount = y2 - regionHeight;
                    glyph.height = (int) (((float) glyph.height) - amount);
                    glyph.yoffset = (int) (((float) glyph.yoffset) + amount);
                    y2 = regionHeight;
                }
            }
            glyph.u = (x * invTexWidth) + u;
            glyph.u2 = (x2 * invTexWidth) + u;
            if (this.flipped) {
                glyph.v = (y * invTexHeight) + v;
                glyph.v2 = (y2 * invTexHeight) + v;
                return;
            }
            glyph.v2 = (y * invTexHeight) + v;
            glyph.v = (y2 * invTexHeight) + v;
        }

        public void setLineHeight(float height) {
            this.lineHeight = this.scaleY * height;
            this.down = this.flipped ? this.lineHeight : -this.lineHeight;
        }

        public void setGlyph(int ch, Glyph glyph) {
            Glyph[] page = this.glyphs[ch / BitmapFont.PAGE_SIZE];
            if (page == null) {
                page = new Glyph[BitmapFont.PAGE_SIZE];
                this.glyphs[ch / BitmapFont.PAGE_SIZE] = page;
            }
            page[ch & 511] = glyph;
        }

        public Glyph getFirstGlyph() {
            for (Glyph[] page : this.glyphs) {
                if (page != null) {
                    for (Glyph glyph : page) {
                        if (glyph != null && glyph.height != 0 && glyph.width != 0) {
                            return glyph;
                        }
                    }
                    continue;
                }
            }
            throw new GdxRuntimeException("No glyphs found.");
        }

        public boolean hasGlyph(char ch) {
            return getGlyph(ch) != null;
        }

        public Glyph getGlyph(char ch) {
            Glyph[] page = this.glyphs[ch / BitmapFont.PAGE_SIZE];
            if (page != null) {
                return page[ch & 511];
            }
            return null;
        }

        public void getGlyphs(GlyphRun run, CharSequence str, int start, int end) {
            boolean markupEnabled = this.markupEnabled;
            float scaleX = this.scaleX;
            Array<Glyph> glyphs = run.glyphs;
            FloatArray xAdvances = run.xAdvances;
            Glyph lastGlyph = null;
            int start2 = start;
            while (start2 < end) {
                start = start2 + 1;
                char ch = str.charAt(start2);
                Glyph glyph = getGlyph(ch);
                if (glyph == null) {
                    start2 = start;
                } else {
                    glyphs.add(glyph);
                    if (lastGlyph == null) {
                        xAdvances.add((((float) (-glyph.xoffset)) * scaleX) - this.padLeft);
                    } else {
                        xAdvances.add(((float) (lastGlyph.xadvance + lastGlyph.getKerning(ch))) * scaleX);
                    }
                    lastGlyph = glyph;
                    if (markupEnabled && ch == '[' && start < end && str.charAt(start) == '[') {
                        start++;
                    }
                    start2 = start;
                }
            }
            if (lastGlyph != null) {
                xAdvances.add((((float) (lastGlyph.xoffset + lastGlyph.width)) * scaleX) - this.padRight);
            }
        }

        public int getWrapIndex(Array<Glyph> glyphs, int start) {
            int i = start - 1;
            while (i >= 1 && isWhitespace((char) ((Glyph) glyphs.get(i)).id)) {
                i--;
            }
            while (i >= 1) {
                char ch = (char) ((Glyph) glyphs.get(i)).id;
                if (isWhitespace(ch) || isBreakChar(ch)) {
                    return i + 1;
                }
                i--;
            }
            return 0;
        }

        public boolean isBreakChar(char c) {
            if (this.breakChars == null) {
                return false;
            }
            for (char br : this.breakChars) {
                if (c == br) {
                    return true;
                }
            }
            return false;
        }

        public boolean isWhitespace(char c) {
            switch (c) {
                case BitmapFont.LOG2_PAGE_SIZE /*9*/:
                case Base.kNumPosModels /*10*/:
                case Matrix4.M13 /*13*/:
                case DefaultTextureBinder.MAX_GLES_UNITS /*32*/:
                    return true;
                default:
                    return false;
            }
        }

        public String getImagePath(int index) {
            return this.imagePaths[index];
        }

        public String[] getImagePaths() {
            return this.imagePaths;
        }

        public FileHandle getFontFile() {
            return this.fontFile;
        }

        public void setScale(float scaleX, float scaleY) {
            if (scaleX == 0.0f) {
                throw new IllegalArgumentException("scaleX cannot be 0.");
            } else if (scaleY == 0.0f) {
                throw new IllegalArgumentException("scaleY cannot be 0.");
            } else {
                float x = scaleX / this.scaleX;
                float y = scaleY / this.scaleY;
                this.lineHeight *= y;
                this.spaceWidth *= x;
                this.xHeight *= y;
                this.capHeight *= y;
                this.ascent *= y;
                this.descent *= y;
                this.down *= y;
                this.padTop *= y;
                this.padLeft *= y;
                this.padBottom *= y;
                this.padRight *= y;
                this.scaleX = scaleX;
                this.scaleY = scaleY;
            }
        }

        public void setScale(float scaleXY) {
            setScale(scaleXY, scaleXY);
        }

        public void scale(float amount) {
            setScale(this.scaleX + amount, this.scaleY + amount);
        }
    }

    public static class Glyph {
        public int height;
        public int id;
        public byte[][] kerning;
        public int page = 0;
        public int srcX;
        public int srcY;
        public float u;
        public float u2;
        public float v;
        public float v2;
        public int width;
        public int xadvance;
        public int xoffset;
        public int yoffset;

        public int getKerning(char ch) {
            if (this.kerning != null) {
                byte[] page = this.kerning[ch >>> BitmapFont.LOG2_PAGE_SIZE];
                if (page != null) {
                    return page[ch & 511];
                }
            }
            return 0;
        }

        public void setKerning(int ch, int value) {
            if (this.kerning == null) {
                this.kerning = new byte[BitmapFont.PAGES][];
            }
            byte[] page = this.kerning[ch >>> BitmapFont.LOG2_PAGE_SIZE];
            if (page == null) {
                page = new byte[BitmapFont.PAGE_SIZE];
                this.kerning[ch >>> BitmapFont.LOG2_PAGE_SIZE] = page;
            }
            page[ch & 511] = (byte) value;
        }

        public String toString() {
            return Character.toString((char) this.id);
        }
    }

    public BitmapFont() {
        this(Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png"), false, true);
    }

    public BitmapFont(boolean flip) {
        this(Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.fnt"), Gdx.files.classpath("com/badlogic/gdx/utils/arial-15.png"), flip, true);
    }

    public BitmapFont(FileHandle fontFile, TextureRegion region) {
        this(fontFile, region, false);
    }

    public BitmapFont(FileHandle fontFile, TextureRegion region, boolean flip) {
        this(new BitmapFontData(fontFile, flip), region, true);
    }

    public BitmapFont(FileHandle fontFile) {
        this(fontFile, false);
    }

    public BitmapFont(FileHandle fontFile, boolean flip) {
        this(new BitmapFontData(fontFile, flip), (TextureRegion) null, true);
    }

    public BitmapFont(FileHandle fontFile, FileHandle imageFile, boolean flip) {
        this(fontFile, imageFile, flip, true);
    }

    public BitmapFont(FileHandle fontFile, FileHandle imageFile, boolean flip, boolean integer) {
        this(new BitmapFontData(fontFile, flip), new TextureRegion(new Texture(imageFile, false)), integer);
        this.ownsTexture = true;
    }

    public BitmapFont(BitmapFontData data, TextureRegion region, boolean integer) {
        Array with;
        if (region != null) {
            with = Array.with(region);
        } else {
            with = null;
        }
        this(data, with, integer);
    }

    public BitmapFont(BitmapFontData data, Array<TextureRegion> pageRegions, boolean integer) {
        this.flipped = data.flipped;
        this.data = data;
        this.integer = integer;
        if (pageRegions == null || pageRegions.size == 0) {
            int n = data.imagePaths.length;
            this.regions = new Array(n);
            for (int i = 0; i < n; i++) {
                FileHandle file;
                if (data.fontFile == null) {
                    file = Gdx.files.internal(data.imagePaths[i]);
                } else {
                    file = Gdx.files.getFileHandle(data.imagePaths[i], data.fontFile.type());
                }
                this.regions.add(new TextureRegion(new Texture(file, false)));
            }
            this.ownsTexture = true;
        } else {
            this.regions = pageRegions;
            this.ownsTexture = false;
        }
        this.cache = newFontCache();
        load(data);
    }

    protected void load(BitmapFontData data) {
        for (Glyph[] page : data.glyphs) {
            if (page != null) {
                for (Glyph glyph : page) {
                    if (glyph != null) {
                        TextureRegion region = (TextureRegion) this.regions.get(glyph.page);
                        if (region == null) {
                            throw new IllegalArgumentException("BitmapFont texture region array cannot contain null elements.");
                        }
                        data.setGlyphRegion(glyph, region);
                    }
                }
                continue;
            }
        }
    }

    public GlyphLayout draw(Batch batch, CharSequence str, float x, float y) {
        this.cache.clear();
        GlyphLayout layout = this.cache.addText(str, x, y);
        this.cache.draw(batch);
        return layout;
    }

    public GlyphLayout draw(Batch batch, CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
        this.cache.clear();
        GlyphLayout layout = this.cache.addText(str, x, y, targetWidth, halign, wrap);
        this.cache.draw(batch);
        return layout;
    }

    public GlyphLayout draw(Batch batch, CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap) {
        this.cache.clear();
        GlyphLayout layout = this.cache.addText(str, x, y, start, end, targetWidth, halign, wrap);
        this.cache.draw(batch);
        return layout;
    }

    public void draw(Batch batch, GlyphLayout layout, float x, float y) {
        this.cache.clear();
        this.cache.addText(layout, x, y);
        this.cache.draw(batch);
    }

    public Color getColor() {
        return this.cache.getColor();
    }

    public void setColor(Color color) {
        this.cache.getColor().set(color);
    }

    public void setColor(float r, float g, float b, float a) {
        this.cache.getColor().set(r, g, b, a);
    }

    public float getScaleX() {
        return this.data.scaleX;
    }

    public float getScaleY() {
        return this.data.scaleY;
    }

    public TextureRegion getRegion() {
        return (TextureRegion) this.regions.first();
    }

    public Array<TextureRegion> getRegions() {
        return this.regions;
    }

    public TextureRegion getRegion(int index) {
        return (TextureRegion) this.regions.get(index);
    }

    public float getLineHeight() {
        return this.data.lineHeight;
    }

    public float getSpaceWidth() {
        return this.data.spaceWidth;
    }

    public float getXHeight() {
        return this.data.xHeight;
    }

    public float getCapHeight() {
        return this.data.capHeight;
    }

    public float getAscent() {
        return this.data.ascent;
    }

    public float getDescent() {
        return this.data.descent;
    }

    public boolean isFlipped() {
        return this.flipped;
    }

    public void dispose() {
        if (this.ownsTexture) {
            for (int i = 0; i < this.regions.size; i++) {
                ((TextureRegion) this.regions.get(i)).getTexture().dispose();
            }
        }
    }

    public void setFixedWidthGlyphs(CharSequence glyphs) {
        int index;
        BitmapFontData data = this.data;
        int maxAdvance = 0;
        int end = glyphs.length();
        for (index = 0; index < end; index++) {
            Glyph g = data.getGlyph(glyphs.charAt(index));
            if (g != null && g.xadvance > maxAdvance) {
                maxAdvance = g.xadvance;
            }
        }
        end = glyphs.length();
        for (index = 0; index < end; index++) {
            g = data.getGlyph(glyphs.charAt(index));
            if (g != null) {
                g.xoffset += Math.round((float) ((maxAdvance - g.xadvance) / 2));
                g.xadvance = maxAdvance;
                g.kerning = (byte[][]) null;
            }
        }
    }

    public void setUseIntegerPositions(boolean integer) {
        this.integer = integer;
        this.cache.setUseIntegerPositions(integer);
    }

    public boolean usesIntegerPositions() {
        return this.integer;
    }

    public BitmapFontCache getCache() {
        return this.cache;
    }

    public BitmapFontData getData() {
        return this.data;
    }

    public boolean ownsTexture() {
        return this.ownsTexture;
    }

    public void setOwnsTexture(boolean ownsTexture) {
        this.ownsTexture = ownsTexture;
    }

    public BitmapFontCache newFontCache() {
        return new BitmapFontCache(this, this.integer);
    }

    public String toString() {
        if (this.data.fontFile != null) {
            return this.data.fontFile.nameWithoutExtension();
        }
        return super.toString();
    }

    static int indexOf(CharSequence text, char ch, int start) {
        int n = text.length();
        while (start < n) {
            if (text.charAt(start) == ch) {
                return start;
            }
            start++;
        }
        return n;
    }
}
