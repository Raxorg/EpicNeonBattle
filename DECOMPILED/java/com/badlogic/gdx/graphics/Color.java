package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.NumberUtils;

public class Color {
    public static final Color BLACK = new Color(0.0f, 0.0f, 0.0f, 1.0f);
    public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f, 1.0f);
    public static final Color BROWN = new Color(-1958407169);
    public static final Color CHARTREUSE = new Color(2147418367);
    public static final Color CLEAR = new Color(0.0f, 0.0f, 0.0f, 0.0f);
    public static final Color CORAL = new Color(-8433409);
    public static final Color CYAN = new Color(0.0f, 1.0f, 1.0f, 1.0f);
    public static final Color DARK_GRAY = new Color(1061109759);
    public static final Color FIREBRICK = new Color(-1306385665);
    public static final Color FOREST = new Color(579543807);
    public static final Color GOLD = new Color(-2686721);
    public static final Color GOLDENROD = new Color(-626712321);
    public static final Color GRAY = new Color(2139062271);
    public static final Color GREEN = new Color(16711935);
    public static final Color LIGHT_GRAY = new Color(-1077952513);
    public static final Color LIME = new Color(852308735);
    public static final Color MAGENTA = new Color(1.0f, 0.0f, 1.0f, 1.0f);
    public static final Color MAROON = new Color(-1339006721);
    public static final Color NAVY = new Color(0.0f, 0.0f, 0.5f, 1.0f);
    public static final Color OLIVE = new Color(1804477439);
    public static final Color ORANGE = new Color(-5963521);
    public static final Color PINK = new Color(-9849601);
    public static final Color PURPLE = new Color(-1608453889);
    public static final Color RED = new Color(-16776961);
    public static final Color ROYAL = new Color(1097458175);
    public static final Color SALMON = new Color(-92245249);
    public static final Color SKY = new Color(-2016482305);
    public static final Color SLATE = new Color(1887473919);
    public static final Color TAN = new Color(-759919361);
    public static final Color TEAL = new Color(0.0f, 0.5f, 0.5f, 1.0f);
    public static final Color VIOLET = new Color(-293409025);
    public static final Color WHITE = new Color(-1);
    public static final Color YELLOW = new Color(-65281);
    public float a;
    public float b;
    public float g;
    public float r;

    public Color(int rgba8888) {
        rgba8888ToColor(this, rgba8888);
    }

    public Color(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        clamp();
    }

    public Color(Color color) {
        set(color);
    }

    public Color set(Color color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
        return this;
    }

    public Color mul(Color color) {
        this.r *= color.r;
        this.g *= color.g;
        this.b *= color.b;
        this.a *= color.a;
        return clamp();
    }

    public Color mul(float value) {
        this.r *= value;
        this.g *= value;
        this.b *= value;
        this.a *= value;
        return clamp();
    }

    public Color add(Color color) {
        this.r += color.r;
        this.g += color.g;
        this.b += color.b;
        this.a += color.a;
        return clamp();
    }

    public Color sub(Color color) {
        this.r -= color.r;
        this.g -= color.g;
        this.b -= color.b;
        this.a -= color.a;
        return clamp();
    }

    public Color clamp() {
        if (this.r < 0.0f) {
            this.r = 0.0f;
        } else if (this.r > 1.0f) {
            this.r = 1.0f;
        }
        if (this.g < 0.0f) {
            this.g = 0.0f;
        } else if (this.g > 1.0f) {
            this.g = 1.0f;
        }
        if (this.b < 0.0f) {
            this.b = 0.0f;
        } else if (this.b > 1.0f) {
            this.b = 1.0f;
        }
        if (this.a < 0.0f) {
            this.a = 0.0f;
        } else if (this.a > 1.0f) {
            this.a = 1.0f;
        }
        return this;
    }

    public Color set(float r, float g, float b, float a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
        return clamp();
    }

    public Color set(int rgba) {
        rgba8888ToColor(this, rgba);
        return this;
    }

    public Color add(float r, float g, float b, float a) {
        this.r += r;
        this.g += g;
        this.b += b;
        this.a += a;
        return clamp();
    }

    public Color sub(float r, float g, float b, float a) {
        this.r -= r;
        this.g -= g;
        this.b -= b;
        this.a -= a;
        return clamp();
    }

    public Color mul(float r, float g, float b, float a) {
        this.r *= r;
        this.g *= g;
        this.b *= b;
        this.a *= a;
        return clamp();
    }

    public Color lerp(Color target, float t) {
        this.r += (target.r - this.r) * t;
        this.g += (target.g - this.g) * t;
        this.b += (target.b - this.b) * t;
        this.a += (target.a - this.a) * t;
        return clamp();
    }

    public Color lerp(float r, float g, float b, float a, float t) {
        this.r += (r - this.r) * t;
        this.g += (g - this.g) * t;
        this.b += (b - this.b) * t;
        this.a += (a - this.a) * t;
        return clamp();
    }

    public Color premultiplyAlpha() {
        this.r *= this.a;
        this.g *= this.a;
        this.b *= this.a;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (toIntBits() != ((Color) o).toIntBits()) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result;
        int floatToIntBits;
        int i = 0;
        if (this.r != 0.0f) {
            result = NumberUtils.floatToIntBits(this.r);
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.g != 0.0f) {
            floatToIntBits = NumberUtils.floatToIntBits(this.g);
        } else {
            floatToIntBits = 0;
        }
        i2 = (i2 + floatToIntBits) * 31;
        if (this.b != 0.0f) {
            floatToIntBits = NumberUtils.floatToIntBits(this.b);
        } else {
            floatToIntBits = 0;
        }
        floatToIntBits = (i2 + floatToIntBits) * 31;
        if (this.a != 0.0f) {
            i = NumberUtils.floatToIntBits(this.a);
        }
        return floatToIntBits + i;
    }

    public float toFloatBits() {
        return NumberUtils.intToFloatColor((((((int) (this.a * 255.0f)) << 24) | (((int) (this.b * 255.0f)) << 16)) | (((int) (this.g * 255.0f)) << 8)) | ((int) (this.r * 255.0f)));
    }

    public int toIntBits() {
        return (((((int) (this.a * 255.0f)) << 24) | (((int) (this.b * 255.0f)) << 16)) | (((int) (this.g * 255.0f)) << 8)) | ((int) (this.r * 255.0f));
    }

    public String toString() {
        String value = Integer.toHexString((((((int) (this.r * 255.0f)) << 24) | (((int) (this.g * 255.0f)) << 16)) | (((int) (this.b * 255.0f)) << 8)) | ((int) (this.a * 255.0f)));
        while (value.length() < 8) {
            value = "0" + value;
        }
        return value;
    }

    public static Color valueOf(String hex) {
        return new Color(((float) Integer.valueOf(hex.substring(0, 2), 16).intValue()) / 255.0f, ((float) Integer.valueOf(hex.substring(2, 4), 16).intValue()) / 255.0f, ((float) Integer.valueOf(hex.substring(4, 6), 16).intValue()) / 255.0f, ((float) (hex.length() != 8 ? Keys.F12 : Integer.valueOf(hex.substring(6, 8), 16).intValue())) / 255.0f);
    }

    public static float toFloatBits(int r, int g, int b, int a) {
        return NumberUtils.intToFloatColor((((a << 24) | (b << 16)) | (g << 8)) | r);
    }

    public static float toFloatBits(float r, float g, float b, float a) {
        return NumberUtils.intToFloatColor((((((int) (255.0f * a)) << 24) | (((int) (255.0f * b)) << 16)) | (((int) (255.0f * g)) << 8)) | ((int) (255.0f * r)));
    }

    public static int toIntBits(int r, int g, int b, int a) {
        return (((a << 24) | (b << 16)) | (g << 8)) | r;
    }

    public static int alpha(float alpha) {
        return (int) (255.0f * alpha);
    }

    public static int luminanceAlpha(float luminance, float alpha) {
        return (((int) (luminance * 255.0f)) << 8) | ((int) (255.0f * alpha));
    }

    public static int rgb565(float r, float g, float b) {
        return ((((int) (r * 31.0f)) << 11) | (((int) (63.0f * g)) << 5)) | ((int) (b * 31.0f));
    }

    public static int rgba4444(float r, float g, float b, float a) {
        return (((((int) (r * 15.0f)) << 12) | (((int) (g * 15.0f)) << 8)) | (((int) (b * 15.0f)) << 4)) | ((int) (a * 15.0f));
    }

    public static int rgb888(float r, float g, float b) {
        return ((((int) (r * 255.0f)) << 16) | (((int) (g * 255.0f)) << 8)) | ((int) (b * 255.0f));
    }

    public static int rgba8888(float r, float g, float b, float a) {
        return (((((int) (r * 255.0f)) << 24) | (((int) (g * 255.0f)) << 16)) | (((int) (b * 255.0f)) << 8)) | ((int) (a * 255.0f));
    }

    public static int argb8888(float a, float r, float g, float b) {
        return (((((int) (a * 255.0f)) << 24) | (((int) (r * 255.0f)) << 16)) | (((int) (g * 255.0f)) << 8)) | ((int) (b * 255.0f));
    }

    public static int rgb565(Color color) {
        return ((((int) (color.r * 31.0f)) << 11) | (((int) (color.g * 63.0f)) << 5)) | ((int) (color.b * 31.0f));
    }

    public static int rgba4444(Color color) {
        return (((((int) (color.r * 15.0f)) << 12) | (((int) (color.g * 15.0f)) << 8)) | (((int) (color.b * 15.0f)) << 4)) | ((int) (color.a * 15.0f));
    }

    public static int rgb888(Color color) {
        return ((((int) (color.r * 255.0f)) << 16) | (((int) (color.g * 255.0f)) << 8)) | ((int) (color.b * 255.0f));
    }

    public static int rgba8888(Color color) {
        return (((((int) (color.r * 255.0f)) << 24) | (((int) (color.g * 255.0f)) << 16)) | (((int) (color.b * 255.0f)) << 8)) | ((int) (color.a * 255.0f));
    }

    public static int argb8888(Color color) {
        return (((((int) (color.a * 255.0f)) << 24) | (((int) (color.r * 255.0f)) << 16)) | (((int) (color.g * 255.0f)) << 8)) | ((int) (color.b * 255.0f));
    }

    public static void rgb565ToColor(Color color, int value) {
        color.r = ((float) ((63488 & value) >>> 11)) / 31.0f;
        color.g = ((float) ((value & 2016) >>> 5)) / 63.0f;
        color.b = ((float) ((value & 31) >>> 0)) / 31.0f;
    }

    public static void rgba4444ToColor(Color color, int value) {
        color.r = ((float) ((61440 & value) >>> 12)) / 15.0f;
        color.g = ((float) ((value & 3840) >>> 8)) / 15.0f;
        color.b = ((float) ((value & 240) >>> 4)) / 15.0f;
        color.a = ((float) (value & 15)) / 15.0f;
    }

    public static void rgb888ToColor(Color color, int value) {
        color.r = ((float) ((16711680 & value) >>> 16)) / 255.0f;
        color.g = ((float) ((65280 & value) >>> 8)) / 255.0f;
        color.b = ((float) (value & Keys.F12)) / 255.0f;
    }

    public static void rgba8888ToColor(Color color, int value) {
        color.r = ((float) ((-16777216 & value) >>> 24)) / 255.0f;
        color.g = ((float) ((16711680 & value) >>> 16)) / 255.0f;
        color.b = ((float) ((65280 & value) >>> 8)) / 255.0f;
        color.a = ((float) (value & Keys.F12)) / 255.0f;
    }

    public static void argb8888ToColor(Color color, int value) {
        color.a = ((float) ((-16777216 & value) >>> 24)) / 255.0f;
        color.r = ((float) ((16711680 & value) >>> 16)) / 255.0f;
        color.g = ((float) ((65280 & value) >>> 8)) / 255.0f;
        color.b = ((float) (value & Keys.F12)) / 255.0f;
    }

    public Color cpy() {
        return new Color(this);
    }
}
