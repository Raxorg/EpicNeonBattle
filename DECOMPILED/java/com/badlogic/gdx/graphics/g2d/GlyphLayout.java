package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Colors;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;

public class GlyphLayout implements Poolable {
    private static final Array<Color> colorStack = new Array(4);
    public float height;
    public final Array<GlyphRun> runs = new Array();
    public float width;

    public static class GlyphRun implements Poolable {
        public final Color color = new Color();
        public final Array<Glyph> glyphs = new Array();
        public float width;
        public float x;
        public final FloatArray xAdvances = new FloatArray();
        public float y;

        public void reset() {
            this.glyphs.clear();
            this.xAdvances.clear();
            this.width = 0.0f;
        }

        public String toString() {
            StringBuilder buffer = new StringBuilder(this.glyphs.size);
            Array<Glyph> glyphs = this.glyphs;
            int n = glyphs.size;
            for (int i = 0; i < n; i++) {
                buffer.append((char) ((Glyph) glyphs.get(i)).id);
            }
            buffer.append(", #");
            buffer.append(this.color);
            buffer.append(", ");
            buffer.append(this.x);
            buffer.append(", ");
            buffer.append(this.y);
            buffer.append(", ");
            buffer.append(this.width);
            return buffer.toString();
        }
    }

    public GlyphLayout(BitmapFont font, CharSequence str) {
        setText(font, str);
    }

    public GlyphLayout(BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
        setText(font, str, color, targetWidth, halign, wrap);
    }

    public GlyphLayout(BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign, boolean wrap, String truncate) {
        setText(font, str, start, end, color, targetWidth, halign, wrap, truncate);
    }

    public void setText(BitmapFont font, CharSequence str) {
        setText(font, str, 0, str.length(), font.getColor(), 0.0f, 8, false, null);
    }

    public void setText(BitmapFont font, CharSequence str, Color color, float targetWidth, int halign, boolean wrap) {
        setText(font, str, 0, str.length(), color, targetWidth, halign, wrap, null);
    }

    public void setText(BitmapFont font, CharSequence str, int start, int end, Color color, float targetWidth, int halign, boolean wrap, String truncate) {
        if (truncate != null) {
            wrap = true;
        } else if (targetWidth <= font.data.spaceWidth) {
            wrap = false;
        }
        BitmapFontData fontData = font.data;
        boolean markupEnabled = fontData.markupEnabled;
        Pool<GlyphRun> glyphRunPool = Pools.get(GlyphRun.class);
        Array<GlyphRun> runs = this.runs;
        glyphRunPool.freeAll(runs);
        runs.clear();
        float x = 0.0f;
        float y = 0.0f;
        float width = 0.0f;
        int lines = 0;
        Array<Color> colorStack = colorStack;
        Color nextColor = color;
        colorStack.add(color);
        Pool<Color> colorPool = Pools.get(Color.class);
        int runStart = start;
        int start2 = start;
        while (true) {
            GlyphRun run;
            int i;
            int i2;
            int runEnd = -1;
            boolean newline = false;
            float[] xAdvances;
            float xAdvance;
            int wrapIndex;
            GlyphRun next;
            if (start2 != end) {
                start = start2 + 1;
                switch (str.charAt(start2)) {
                    case Base.kNumPosModels /*10*/:
                        runEnd = start - 1;
                        newline = true;
                        break;
                    case Keys.MUTE /*91*/:
                        if (markupEnabled) {
                            int length = parseColorMarkup(str, start, end, colorPool);
                            if (length >= 0) {
                                runEnd = start - 1;
                                start += length + 1;
                                nextColor = (Color) colorStack.peek();
                                break;
                            }
                        }
                        break;
                    default:
                        break;
                }
                if (runEnd == -1) {
                    if (runEnd != runStart) {
                        run = (GlyphRun) glyphRunPool.obtain();
                        runs.add(run);
                        run.color.set(color);
                        run.x = x;
                        run.y = y;
                        fontData.getGlyphs(run, str, runStart, runEnd);
                        xAdvances = run.xAdvances.items;
                        i = 0;
                        i2 = run.xAdvances.size;
                        while (i < i2) {
                            xAdvance = xAdvances[i];
                            x += xAdvance;
                            if (((((float) (((Glyph) run.glyphs.get(i - 1)).width + ((Glyph) run.glyphs.get(i - 1)).xoffset)) * fontData.scaleX) + (x - xAdvance)) - 1.0E-4f > targetWidth) {
                                if (truncate == null) {
                                    wrapIndex = fontData.getWrapIndex(run.glyphs, i);
                                    wrapIndex = i - 1;
                                    next = wrap(fontData, run, glyphRunPool, wrapIndex, i);
                                    runs.add(next);
                                    width = Math.max(width, run.x + run.width);
                                    x = 0.0f;
                                    y += fontData.down;
                                    lines++;
                                    next.x = 0.0f;
                                    next.y = y;
                                    i = -1;
                                    i2 = next.xAdvances.size;
                                    xAdvances = next.xAdvances.items;
                                    run = next;
                                    i++;
                                } else {
                                    truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
                                    x = run.x + run.width;
                                }
                            }
                            run.width += xAdvance;
                            i++;
                        }
                    }
                    if (newline) {
                        width = Math.max(width, x);
                        x = 0.0f;
                        y += fontData.down;
                        lines++;
                    }
                    runStart = start;
                    color = nextColor;
                }
                start2 = start;
            } else if (runStart == end) {
                start = start2;
            } else {
                runEnd = end;
                start = start2;
                if (runEnd == -1) {
                    if (runEnd != runStart) {
                        run = (GlyphRun) glyphRunPool.obtain();
                        runs.add(run);
                        run.color.set(color);
                        run.x = x;
                        run.y = y;
                        fontData.getGlyphs(run, str, runStart, runEnd);
                        xAdvances = run.xAdvances.items;
                        i = 0;
                        i2 = run.xAdvances.size;
                        while (i < i2) {
                            xAdvance = xAdvances[i];
                            x += xAdvance;
                            if (wrap && x > targetWidth && i > 1) {
                                if (((((float) (((Glyph) run.glyphs.get(i - 1)).width + ((Glyph) run.glyphs.get(i - 1)).xoffset)) * fontData.scaleX) + (x - xAdvance)) - 1.0E-4f > targetWidth) {
                                    if (truncate == null) {
                                        truncate(fontData, run, targetWidth, truncate, i, glyphRunPool);
                                        x = run.x + run.width;
                                    } else {
                                        wrapIndex = fontData.getWrapIndex(run.glyphs, i);
                                        if ((run.x == 0.0f && wrapIndex == 0) || wrapIndex >= run.glyphs.size) {
                                            wrapIndex = i - 1;
                                        }
                                        next = wrap(fontData, run, glyphRunPool, wrapIndex, i);
                                        runs.add(next);
                                        width = Math.max(width, run.x + run.width);
                                        x = 0.0f;
                                        y += fontData.down;
                                        lines++;
                                        next.x = 0.0f;
                                        next.y = y;
                                        i = -1;
                                        i2 = next.xAdvances.size;
                                        xAdvances = next.xAdvances.items;
                                        run = next;
                                        i++;
                                    }
                                }
                            }
                            run.width += xAdvance;
                            i++;
                        }
                    }
                    if (newline) {
                        width = Math.max(width, x);
                        x = 0.0f;
                        y += fontData.down;
                        lines++;
                    }
                    runStart = start;
                    color = nextColor;
                }
                start2 = start;
            }
            width = Math.max(width, x);
            i2 = colorStack.size;
            for (i = 1; i < i2; i++) {
                colorPool.free(colorStack.get(i));
            }
            colorStack.clear();
            if ((halign & 8) == 0) {
                float shift;
                int lineStart;
                GlyphRun glyphRun;
                boolean center = (halign & 1) != 0;
                float lineWidth = 0.0f;
                float lineY = -2.1474836E9f;
                int lineStart2 = 0;
                i2 = runs.size;
                for (i = 0; i < i2; i++) {
                    run = (GlyphRun) runs.get(i);
                    if (run.y != lineY) {
                        lineY = run.y;
                        shift = targetWidth - lineWidth;
                        if (center) {
                            shift /= 2.0f;
                            lineStart = lineStart2;
                        } else {
                            lineStart = lineStart2;
                        }
                        while (lineStart < i) {
                            lineStart2 = lineStart + 1;
                            glyphRun = (GlyphRun) runs.get(lineStart);
                            glyphRun.x += shift;
                            lineStart = lineStart2;
                        }
                        lineWidth = 0.0f;
                        lineStart2 = lineStart;
                    }
                    lineWidth += run.width;
                }
                shift = targetWidth - lineWidth;
                if (center) {
                    shift /= 2.0f;
                    lineStart = lineStart2;
                } else {
                    lineStart = lineStart2;
                }
                while (lineStart < i2) {
                    lineStart2 = lineStart + 1;
                    glyphRun = (GlyphRun) runs.get(lineStart);
                    glyphRun.x += shift;
                    lineStart = lineStart2;
                }
            }
            this.width = width;
            this.height = fontData.capHeight + (((float) lines) * fontData.lineHeight);
            return;
        }
    }

    private void truncate(BitmapFontData fontData, GlyphRun run, float targetWidth, String truncate, int widthIndex, Pool<GlyphRun> glyphRunPool) {
        GlyphRun truncateRun = (GlyphRun) glyphRunPool.obtain();
        fontData.getGlyphs(truncateRun, truncate, 0, truncate.length());
        float truncateWidth = 0.0f;
        for (int i = 1; i < truncateRun.xAdvances.size; i++) {
            truncateWidth += truncateRun.xAdvances.get(i);
        }
        targetWidth -= truncateWidth;
        int count = 0;
        float width = run.x;
        while (count < run.xAdvances.size) {
            float xAdvance = run.xAdvances.get(count);
            width += xAdvance;
            if (width > targetWidth) {
                run.width = (width - run.x) - xAdvance;
                break;
            }
            count++;
        }
        if (count > 1) {
            run.glyphs.truncate(count - 1);
            run.xAdvances.truncate(count);
            adjustLastGlyph(fontData, run);
            run.xAdvances.addAll(truncateRun.xAdvances, 1, truncateRun.xAdvances.size - 1);
        } else {
            run.glyphs.clear();
            run.xAdvances.clear();
            run.xAdvances.addAll(truncateRun.xAdvances);
            run.width += truncateRun.xAdvances.get(0);
        }
        run.glyphs.addAll(truncateRun.glyphs);
        run.width += truncateWidth;
        glyphRunPool.free(truncateRun);
    }

    private GlyphRun wrap(BitmapFontData fontData, GlyphRun first, Pool<GlyphRun> glyphRunPool, int wrapIndex, int widthIndex) {
        GlyphRun second = (GlyphRun) glyphRunPool.obtain();
        second.color.set(first.color);
        int glyphCount = first.glyphs.size;
        if (wrapIndex < glyphCount) {
            second.glyphs.addAll(first.glyphs, wrapIndex, glyphCount - wrapIndex);
            second.xAdvances.add((((float) (-((Glyph) second.glyphs.first()).xoffset)) * fontData.scaleX) - fontData.padLeft);
            second.xAdvances.addAll(first.xAdvances, wrapIndex + 1, first.xAdvances.size - (wrapIndex + 1));
        }
        int widthIndex2 = widthIndex;
        while (widthIndex2 < wrapIndex) {
            widthIndex = widthIndex2 + 1;
            first.width += first.xAdvances.get(widthIndex2);
            widthIndex2 = widthIndex;
        }
        widthIndex = widthIndex2;
        while (widthIndex > wrapIndex + 1) {
            widthIndex--;
            first.width -= first.xAdvances.get(widthIndex);
        }
        if (wrapIndex == 0) {
            glyphRunPool.free(first);
            this.runs.pop();
        } else {
            first.glyphs.truncate(wrapIndex);
            first.xAdvances.truncate(wrapIndex + 1);
            adjustLastGlyph(fontData, first);
        }
        return second;
    }

    private void adjustLastGlyph(BitmapFontData fontData, GlyphRun run) {
        Glyph last = (Glyph) run.glyphs.peek();
        if (!fontData.isWhitespace((char) last.id)) {
            float width = (((float) (last.xoffset + last.width)) * fontData.scaleX) - fontData.padRight;
            run.width += width - run.xAdvances.peek();
            run.xAdvances.set(run.xAdvances.size - 1, width);
        }
    }

    private int parseColorMarkup(CharSequence str, int start, int end, Pool<Color> colorPool) {
        if (start == end) {
            return -1;
        }
        int i;
        Color color;
        switch (str.charAt(start)) {
            case Keys.G /*35*/:
                int colorInt = 0;
                i = start + 1;
                while (i < end) {
                    char ch = str.charAt(i);
                    if (ch != ']') {
                        if (ch >= '0' && ch <= '9') {
                            colorInt = (colorInt * 16) + (ch - 48);
                        } else if (ch >= 'a' && ch <= 'f') {
                            colorInt = (colorInt * 16) + (ch - 87);
                        } else if (ch >= 'A' && ch <= 'F') {
                            colorInt = (colorInt * 16) + (ch - 55);
                        }
                        i++;
                    } else if (i >= start + 2 && i <= start + 9) {
                        if (i - start <= 7) {
                            for (int ii = 0; ii < 9 - (i - start); ii++) {
                                colorInt <<= 4;
                            }
                            colorInt |= Keys.F12;
                        }
                        color = (Color) colorPool.obtain();
                        colorStack.add(color);
                        Color.rgba8888ToColor(color, colorInt);
                        return i - start;
                    }
                    return -1;
                }
                return -1;
            case Keys.MUTE /*91*/:
                return -1;
            case Keys.PAGE_DOWN /*93*/:
                if (colorStack.size > 1) {
                    colorPool.free(colorStack.pop());
                }
                return 0;
            default:
                int colorStart = start;
                i = start + 1;
                while (i < end) {
                    if (str.charAt(i) != ']') {
                        i++;
                    } else {
                        Color namedColor = Colors.get(str.subSequence(colorStart, i).toString());
                        if (namedColor == null) {
                            return -1;
                        }
                        color = (Color) colorPool.obtain();
                        colorStack.add(color);
                        color.set(namedColor);
                        return i - start;
                    }
                }
                return -1;
        }
    }

    public void reset() {
        Pools.get(GlyphRun.class).freeAll(this.runs);
        this.runs.clear();
        this.width = 0.0f;
        this.height = 0.0f;
    }

    public String toString() {
        if (this.runs.size == 0) {
            return BuildConfig.VERSION_NAME;
        }
        StringBuilder buffer = new StringBuilder(Base.kNumFullDistances);
        buffer.append(this.width);
        buffer.append('x');
        buffer.append(this.height);
        buffer.append('\n');
        int n = this.runs.size;
        for (int i = 0; i < n; i++) {
            buffer.append(((GlyphRun) this.runs.get(i)).toString());
            buffer.append('\n');
        }
        buffer.setLength(buffer.length() - 1);
        return buffer.toString();
    }
}
