package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pools;

public class BitmapFontCache {
    private static final Color tempColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    private static final float whiteTint = Color.WHITE.toFloatBits();
    private final Color color;
    private float currentTint;
    private final BitmapFont font;
    private int glyphCount;
    private int[] idx;
    private boolean integer;
    private final Array<GlyphLayout> layouts;
    private IntArray[] pageGlyphIndices;
    private float[][] pageVertices;
    private final Array<GlyphLayout> pooledLayouts;
    private int[] tempGlyphCount;
    private float x;
    private float y;

    public BitmapFontCache(BitmapFont font) {
        this(font, font.usesIntegerPositions());
    }

    public BitmapFontCache(BitmapFont font, boolean integer) {
        this.layouts = new Array();
        this.pooledLayouts = new Array();
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.font = font;
        this.integer = integer;
        int pageCount = font.regions.size;
        if (pageCount == 0) {
            throw new IllegalArgumentException("The specified font must contain at least one texture page.");
        }
        this.pageVertices = new float[pageCount][];
        this.idx = new int[pageCount];
        if (pageCount > 1) {
            this.pageGlyphIndices = new IntArray[pageCount];
            int n = this.pageGlyphIndices.length;
            for (int i = 0; i < n; i++) {
                this.pageGlyphIndices[i] = new IntArray();
            }
        }
        this.tempGlyphCount = new int[pageCount];
    }

    public void setPosition(float x, float y) {
        translate(x - this.x, y - this.y);
    }

    public void translate(float xAmount, float yAmount) {
        if (xAmount != 0.0f || yAmount != 0.0f) {
            if (this.integer) {
                xAmount = (float) Math.round(xAmount);
                yAmount = (float) Math.round(yAmount);
            }
            this.x += xAmount;
            this.y += yAmount;
            int length = this.pageVertices.length;
            for (int j = 0; j < length; j++) {
                float[] vertices = this.pageVertices[j];
                int n = this.idx[j];
                for (int i = 0; i < n; i += 5) {
                    vertices[i] = vertices[i] + xAmount;
                    int i2 = i + 1;
                    vertices[i2] = vertices[i2] + yAmount;
                }
            }
        }
    }

    public void tint(Color tint) {
        float newTint = tint.toFloatBits();
        if (this.currentTint != newTint) {
            int i;
            this.currentTint = newTint;
            int[] tempGlyphCount = this.tempGlyphCount;
            int n = tempGlyphCount.length;
            for (i = 0; i < n; i++) {
                tempGlyphCount[i] = 0;
            }
            n = this.layouts.size;
            for (i = 0; i < n; i++) {
                GlyphLayout layout = (GlyphLayout) this.layouts.get(i);
                int nn = layout.runs.size;
                for (int ii = 0; ii < nn; ii++) {
                    GlyphRun run = (GlyphRun) layout.runs.get(ii);
                    Array<Glyph> glyphs = run.glyphs;
                    float colorFloat = tempColor.set(run.color).mul(tint).toFloatBits();
                    int nnn = glyphs.size;
                    for (int iii = 0; iii < nnn; iii++) {
                        int page = ((Glyph) glyphs.get(iii)).page;
                        int offset = (tempGlyphCount[page] * 20) + 2;
                        tempGlyphCount[page] = tempGlyphCount[page] + 1;
                        float[] vertices = this.pageVertices[page];
                        for (int v = 0; v < 20; v += 5) {
                            vertices[offset + v] = colorFloat;
                        }
                    }
                }
            }
        }
    }

    public void setAlphas(float alpha) {
        int alphaBits = ((int) (254.0f * alpha)) << 24;
        float prev = 0.0f;
        float newColor = 0.0f;
        int length = this.pageVertices.length;
        for (int j = 0; j < length; j++) {
            float[] vertices = this.pageVertices[j];
            int i = 2;
            int n = this.idx[j];
            while (i < n) {
                float c = vertices[i];
                if (c != prev || i == 2) {
                    prev = c;
                    newColor = NumberUtils.intToFloatColor((16777215 & NumberUtils.floatToIntColor(c)) | alphaBits);
                    vertices[i] = newColor;
                } else {
                    vertices[i] = newColor;
                }
                i += 5;
            }
        }
    }

    public void setColors(float color) {
        int length = this.pageVertices.length;
        for (int j = 0; j < length; j++) {
            float[] vertices = this.pageVertices[j];
            int n = this.idx[j];
            for (int i = 2; i < n; i += 5) {
                vertices[i] = color;
            }
        }
    }

    public void setColors(Color tint) {
        setColors(tint.toFloatBits());
    }

    public void setColors(float r, float g, float b, float a) {
        setColors(NumberUtils.intToFloatColor((((((int) (255.0f * a)) << 24) | (((int) (255.0f * b)) << 16)) | (((int) (255.0f * g)) << 8)) | ((int) (255.0f * r))));
    }

    public void setColors(Color tint, int start, int end) {
        setColors(tint.toFloatBits(), start, end);
    }

    public void setColors(float color, int start, int end) {
        float[] vertices;
        int n;
        int i;
        if (this.pageVertices.length == 1) {
            vertices = this.pageVertices[0];
            n = end * 20;
            for (i = (start * 20) + 2; i < n; i += 5) {
                vertices[i] = color;
            }
            return;
        }
        int pageCount = this.pageVertices.length;
        for (i = 0; i < pageCount; i++) {
            vertices = this.pageVertices[i];
            IntArray glyphIndices = this.pageGlyphIndices[i];
            n = glyphIndices.size;
            for (int j = 0; j < n; j++) {
                int glyphIndex = glyphIndices.items[j];
                if (glyphIndex >= end) {
                    break;
                }
                if (glyphIndex >= start) {
                    for (int off = 0; off < 20; off += 5) {
                        vertices[((j * 20) + 2) + off] = color;
                    }
                }
            }
        }
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public void draw(Batch spriteBatch) {
        Array<TextureRegion> regions = this.font.getRegions();
        int n = this.pageVertices.length;
        for (int j = 0; j < n; j++) {
            if (this.idx[j] > 0) {
                spriteBatch.draw(((TextureRegion) regions.get(j)).getTexture(), this.pageVertices[j], 0, this.idx[j]);
            }
        }
    }

    public void draw(Batch spriteBatch, int start, int end) {
        if (this.pageVertices.length == 1) {
            spriteBatch.draw(this.font.getRegion().getTexture(), this.pageVertices[0], start * 20, (end - start) * 20);
            return;
        }
        Array<TextureRegion> regions = this.font.getRegions();
        int pageCount = this.pageVertices.length;
        for (int i = 0; i < pageCount; i++) {
            int offset = -1;
            int count = 0;
            IntArray glyphIndices = this.pageGlyphIndices[i];
            int n = glyphIndices.size;
            for (int ii = 0; ii < n; ii++) {
                int glyphIndex = glyphIndices.get(ii);
                if (glyphIndex >= end) {
                    break;
                }
                if (offset == -1 && glyphIndex >= start) {
                    offset = ii;
                }
                if (glyphIndex >= start) {
                    count++;
                }
            }
            if (!(offset == -1 || count == 0)) {
                spriteBatch.draw(((TextureRegion) regions.get(i)).getTexture(), this.pageVertices[i], offset * 20, count * 20);
            }
        }
    }

    public void draw(Batch spriteBatch, float alphaModulation) {
        if (alphaModulation == 1.0f) {
            draw(spriteBatch);
            return;
        }
        Color color = getColor();
        float oldAlpha = color.a;
        color.a *= alphaModulation;
        setColors(color);
        draw(spriteBatch);
        color.a = oldAlpha;
        setColors(color);
    }

    public void clear() {
        this.x = 0.0f;
        this.y = 0.0f;
        Pools.freeAll(this.pooledLayouts, true);
        this.pooledLayouts.clear();
        this.layouts.clear();
        int n = this.idx.length;
        for (int i = 0; i < n; i++) {
            if (this.pageGlyphIndices != null) {
                this.pageGlyphIndices[i].clear();
            }
            this.idx[i] = 0;
        }
    }

    private void requireGlyphs(GlyphLayout layout) {
        int i;
        if (this.pageVertices.length == 1) {
            int newGlyphCount = 0;
            for (i = 0; i < layout.runs.size; i++) {
                newGlyphCount += ((GlyphRun) layout.runs.get(i)).glyphs.size;
            }
            requirePageGlyphs(0, newGlyphCount);
            return;
        }
        int[] tempGlyphCount = this.tempGlyphCount;
        int n = tempGlyphCount.length;
        for (i = 0; i < n; i++) {
            tempGlyphCount[i] = 0;
        }
        n = layout.runs.size;
        for (i = 0; i < n; i++) {
            Array<Glyph> glyphs = ((GlyphRun) layout.runs.get(i)).glyphs;
            int nn = glyphs.size;
            for (int ii = 0; ii < nn; ii++) {
                int i2 = ((Glyph) glyphs.get(ii)).page;
                tempGlyphCount[i2] = tempGlyphCount[i2] + 1;
            }
        }
        n = tempGlyphCount.length;
        for (i = 0; i < n; i++) {
            requirePageGlyphs(i, tempGlyphCount[i]);
        }
    }

    private void requirePageGlyphs(int page, int glyphCount) {
        if (this.pageGlyphIndices != null && glyphCount > this.pageGlyphIndices[page].items.length) {
            this.pageGlyphIndices[page].ensureCapacity(glyphCount - this.pageGlyphIndices[page].items.length);
        }
        int vertexCount = this.idx[page] + (glyphCount * 20);
        float[] vertices = this.pageVertices[page];
        if (vertices == null) {
            this.pageVertices[page] = new float[vertexCount];
        } else if (vertices.length < vertexCount) {
            float[] newVertices = new float[vertexCount];
            System.arraycopy(vertices, 0, newVertices, 0, this.idx[page]);
            this.pageVertices[page] = newVertices;
        }
    }

    private void addToCache(GlyphLayout layout, float x, float y) {
        int i;
        int pageCount = this.font.regions.size;
        if (this.pageVertices.length < pageCount) {
            float[][] newPageVertices = new float[pageCount][];
            System.arraycopy(this.pageVertices, 0, newPageVertices, 0, this.pageVertices.length);
            this.pageVertices = newPageVertices;
            int[] newIdx = new int[pageCount];
            System.arraycopy(this.idx, 0, newIdx, 0, this.idx.length);
            this.idx = newIdx;
            IntArray[] newPageGlyphIndices = new IntArray[pageCount];
            int pageGlyphIndicesLength = 0;
            if (this.pageGlyphIndices != null) {
                pageGlyphIndicesLength = this.pageGlyphIndices.length;
                System.arraycopy(this.pageGlyphIndices, 0, newPageGlyphIndices, 0, this.pageGlyphIndices.length);
            }
            for (i = pageGlyphIndicesLength; i < pageCount; i++) {
                newPageGlyphIndices[i] = new IntArray();
            }
            this.pageGlyphIndices = newPageGlyphIndices;
            this.tempGlyphCount = new int[pageCount];
        }
        this.layouts.add(layout);
        requireGlyphs(layout);
        int n = layout.runs.size;
        for (i = 0; i < n; i++) {
            GlyphRun run = (GlyphRun) layout.runs.get(i);
            Array<Glyph> glyphs = run.glyphs;
            FloatArray xAdvances = run.xAdvances;
            float color = run.color.toFloatBits();
            float gx = x + run.x;
            float gy = y + run.y;
            int nn = glyphs.size;
            for (int ii = 0; ii < nn; ii++) {
                gx += xAdvances.get(ii);
                addGlyph((Glyph) glyphs.get(ii), gx, gy, color);
            }
        }
        this.currentTint = whiteTint;
    }

    private void addGlyph(Glyph glyph, float x, float y, float color) {
        float scaleX = this.font.data.scaleX;
        float scaleY = this.font.data.scaleY;
        x += ((float) glyph.xoffset) * scaleX;
        y += ((float) glyph.yoffset) * scaleY;
        float width = ((float) glyph.width) * scaleX;
        float height = ((float) glyph.height) * scaleY;
        float u = glyph.u;
        float u2 = glyph.u2;
        float v = glyph.v;
        float v2 = glyph.v2;
        if (this.integer) {
            x = (float) Math.round(x);
            y = (float) Math.round(y);
            width = (float) Math.round(width);
            height = (float) Math.round(height);
        }
        float x2 = x + width;
        float y2 = y + height;
        int page = glyph.page;
        int idx = this.idx[page];
        int[] iArr = this.idx;
        iArr[page] = iArr[page] + 20;
        if (this.pageGlyphIndices != null) {
            IntArray intArray = this.pageGlyphIndices[page];
            int i = this.glyphCount;
            this.glyphCount = i + 1;
            intArray.add(i);
        }
        float[] vertices = this.pageVertices[page];
        int idx2 = idx + 1;
        vertices[idx] = x;
        idx = idx2 + 1;
        vertices[idx2] = y;
        idx2 = idx + 1;
        vertices[idx] = color;
        idx = idx2 + 1;
        vertices[idx2] = u;
        idx2 = idx + 1;
        vertices[idx] = v;
        idx = idx2 + 1;
        vertices[idx2] = x;
        idx2 = idx + 1;
        vertices[idx] = y2;
        idx = idx2 + 1;
        vertices[idx2] = color;
        idx2 = idx + 1;
        vertices[idx] = u;
        idx = idx2 + 1;
        vertices[idx2] = v2;
        idx2 = idx + 1;
        vertices[idx] = x2;
        idx = idx2 + 1;
        vertices[idx2] = y2;
        idx2 = idx + 1;
        vertices[idx] = color;
        idx = idx2 + 1;
        vertices[idx2] = u2;
        idx2 = idx + 1;
        vertices[idx] = v2;
        idx = idx2 + 1;
        vertices[idx2] = x2;
        idx2 = idx + 1;
        vertices[idx] = y;
        idx = idx2 + 1;
        vertices[idx2] = color;
        idx2 = idx + 1;
        vertices[idx] = u2;
        vertices[idx2] = v;
    }

    public GlyphLayout setText(CharSequence str, float x, float y) {
        clear();
        return addText(str, x, y, 0, str.length(), 0.0f, 8, false);
    }

    public GlyphLayout setText(CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
        clear();
        return addText(str, x, y, 0, str.length(), targetWidth, halign, wrap);
    }

    public GlyphLayout setText(CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap) {
        clear();
        return addText(str, x, y, start, end, targetWidth, halign, wrap);
    }

    public void setText(GlyphLayout layout, float x, float y) {
        clear();
        addText(layout, x, y);
    }

    public GlyphLayout addText(CharSequence str, float x, float y) {
        return addText(str, x, y, 0, str.length(), 0.0f, 8, false);
    }

    public GlyphLayout addText(CharSequence str, float x, float y, float targetWidth, int halign, boolean wrap) {
        return addText(str, x, y, 0, str.length(), targetWidth, halign, wrap);
    }

    public GlyphLayout addText(CharSequence str, float x, float y, int start, int end, float targetWidth, int halign, boolean wrap) {
        GlyphLayout layout = (GlyphLayout) Pools.obtain(GlyphLayout.class);
        this.pooledLayouts.add(layout);
        layout.setText(this.font, str, start, end, this.color, targetWidth, halign, wrap, null);
        addText(layout, x, y);
        return layout;
    }

    public void addText(GlyphLayout layout, float x, float y) {
        addToCache(layout, x, this.font.data.ascent + y);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public BitmapFont getFont() {
        return this.font;
    }

    public void setUseIntegerPositions(boolean use) {
        this.integer = use;
    }

    public boolean usesIntegerPositions() {
        return this.integer;
    }

    public float[] getVertices() {
        return getVertices(0);
    }

    public float[] getVertices(int page) {
        return this.pageVertices[page];
    }

    public Array<GlyphLayout> getLayouts() {
        return this.layouts;
    }
}
