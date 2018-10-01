package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.StringBuilder;
import com.epicness.neonbattle.android.BuildConfig;

public class Label extends Widget {
    private static final GlyphLayout prefSizeLayout = new GlyphLayout();
    private static final Color tempColor = new Color();
    private BitmapFontCache cache;
    private String ellipsis;
    private float fontScaleX;
    private float fontScaleY;
    private int labelAlign;
    private float lastPrefHeight;
    private final GlyphLayout layout;
    private int lineAlign;
    private final Vector2 prefSize;
    private boolean prefSizeInvalid;
    private LabelStyle style;
    private final StringBuilder text;
    private boolean wrap;

    public static class LabelStyle {
        public Drawable background;
        public BitmapFont font;
        public Color fontColor;

        public LabelStyle(BitmapFont font, Color fontColor) {
            this.font = font;
            this.fontColor = fontColor;
        }

        public LabelStyle(LabelStyle style) {
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
            this.background = style.background;
        }
    }

    public Label(CharSequence text, Skin skin) {
        this(text, (LabelStyle) skin.get(LabelStyle.class));
    }

    public Label(CharSequence text, Skin skin, String styleName) {
        this(text, (LabelStyle) skin.get(styleName, LabelStyle.class));
    }

    public Label(CharSequence text, Skin skin, String fontName, Color color) {
        this(text, new LabelStyle(skin.getFont(fontName), color));
    }

    public Label(CharSequence text, Skin skin, String fontName, String colorName) {
        this(text, new LabelStyle(skin.getFont(fontName), skin.getColor(colorName)));
    }

    public Label(CharSequence text, LabelStyle style) {
        this.layout = new GlyphLayout();
        this.prefSize = new Vector2();
        this.text = new StringBuilder();
        this.labelAlign = 8;
        this.lineAlign = 8;
        this.prefSizeInvalid = true;
        this.fontScaleX = 1.0f;
        this.fontScaleY = 1.0f;
        if (text != null) {
            this.text.append(text);
        }
        setStyle(style);
        if (text != null && text.length() > 0) {
            setSize(getPrefWidth(), getPrefHeight());
        }
    }

    public void setStyle(LabelStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        } else if (style.font == null) {
            throw new IllegalArgumentException("Missing LabelStyle font.");
        } else {
            this.style = style;
            this.cache = style.font.newFontCache();
            invalidateHierarchy();
        }
    }

    public LabelStyle getStyle() {
        return this.style;
    }

    public void setText(CharSequence newText) {
        if (newText == null) {
            newText = BuildConfig.VERSION_NAME;
        }
        if (newText instanceof StringBuilder) {
            if (!this.text.equals(newText)) {
                this.text.setLength(0);
                this.text.append((StringBuilder) newText);
            } else {
                return;
            }
        } else if (!textEquals(newText)) {
            this.text.setLength(0);
            this.text.append(newText);
        } else {
            return;
        }
        invalidateHierarchy();
    }

    public boolean textEquals(CharSequence other) {
        int length = this.text.length;
        char[] chars = this.text.chars;
        if (length != other.length()) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (chars[i] != other.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public StringBuilder getText() {
        return this.text;
    }

    public void invalidate() {
        super.invalidate();
        this.prefSizeInvalid = true;
    }

    private void scaleAndComputePrefSize() {
        BitmapFont font = this.cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (!(this.fontScaleX == 1.0f && this.fontScaleY == 1.0f)) {
            font.getData().setScale(this.fontScaleX, this.fontScaleY);
        }
        computePrefSize();
        if (this.fontScaleX != 1.0f || this.fontScaleY != 1.0f) {
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }

    private void computePrefSize() {
        this.prefSizeInvalid = false;
        GlyphLayout prefSizeLayout = prefSizeLayout;
        if (this.wrap && this.ellipsis == null) {
            float width = getWidth();
            if (this.style.background != null) {
                width -= this.style.background.getLeftWidth() + this.style.background.getRightWidth();
            }
            prefSizeLayout.setText(this.cache.getFont(), this.text, Color.WHITE, width, 8, true);
        } else {
            prefSizeLayout.setText(this.cache.getFont(), this.text);
        }
        this.prefSize.set(prefSizeLayout.width, prefSizeLayout.height);
    }

    public void layout() {
        float textWidth;
        float textHeight;
        BitmapFont font = this.cache.getFont();
        float oldScaleX = font.getScaleX();
        float oldScaleY = font.getScaleY();
        if (!(this.fontScaleX == 1.0f && this.fontScaleY == 1.0f)) {
            font.getData().setScale(this.fontScaleX, this.fontScaleY);
        }
        boolean wrap = this.wrap && this.ellipsis == null;
        if (wrap) {
            float prefHeight = getPrefHeight();
            if (prefHeight != this.lastPrefHeight) {
                this.lastPrefHeight = prefHeight;
                invalidateHierarchy();
            }
        }
        float width = getWidth();
        float height = getHeight();
        Drawable background = this.style.background;
        float x = 0.0f;
        float y = 0.0f;
        if (background != null) {
            x = background.getLeftWidth();
            y = background.getBottomHeight();
            width -= background.getLeftWidth() + background.getRightWidth();
            height -= background.getBottomHeight() + background.getTopHeight();
        }
        GlyphLayout layout = this.layout;
        if (wrap || this.text.indexOf("\n") != -1) {
            layout.setText(font, this.text, 0, this.text.length, Color.WHITE, width, this.lineAlign, wrap, this.ellipsis);
            textWidth = layout.width;
            textHeight = layout.height;
            if ((this.labelAlign & 8) == 0) {
                if ((this.labelAlign & 16) != 0) {
                    x += width - textWidth;
                } else {
                    x += (width - textWidth) / 2.0f;
                }
            }
        } else {
            textWidth = width;
            textHeight = font.getData().capHeight;
        }
        if ((this.labelAlign & 2) != 0) {
            y = (y + (this.cache.getFont().isFlipped() ? 0.0f : height - textHeight)) + this.style.font.getDescent();
        } else if ((this.labelAlign & 4) != 0) {
            y = (y + (this.cache.getFont().isFlipped() ? height - textHeight : 0.0f)) - this.style.font.getDescent();
        } else {
            y += (height - textHeight) / 2.0f;
        }
        if (!this.cache.getFont().isFlipped()) {
            y += textHeight;
        }
        layout.setText(font, this.text, 0, this.text.length, Color.WHITE, textWidth, this.lineAlign, wrap, this.ellipsis);
        this.cache.setText(layout, x, y);
        if (this.fontScaleX != 1.0f || this.fontScaleY != 1.0f) {
            font.getData().setScale(oldScaleX, oldScaleY);
        }
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        Color color = tempColor.set(getColor());
        color.a *= parentAlpha;
        if (this.style.background != null) {
            batch.setColor(color.r, color.g, color.b, color.a);
            this.style.background.draw(batch, getX(), getY(), getWidth(), getHeight());
        }
        if (this.style.fontColor != null) {
            color.mul(this.style.fontColor);
        }
        this.cache.tint(color);
        this.cache.setPosition(getX(), getY());
        this.cache.draw(batch);
    }

    public float getPrefWidth() {
        if (this.wrap) {
            return 0.0f;
        }
        if (this.prefSizeInvalid) {
            scaleAndComputePrefSize();
        }
        float width = this.prefSize.x;
        Drawable background = this.style.background;
        if (background != null) {
            return width + (background.getLeftWidth() + background.getRightWidth());
        }
        return width;
    }

    public float getPrefHeight() {
        if (this.prefSizeInvalid) {
            scaleAndComputePrefSize();
        }
        float height = this.prefSize.y - ((this.style.font.getDescent() * this.fontScaleY) * 2.0f);
        Drawable background = this.style.background;
        if (background != null) {
            return height + (background.getTopHeight() + background.getBottomHeight());
        }
        return height;
    }

    public GlyphLayout getGlyphLayout() {
        return this.layout;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
        invalidateHierarchy();
    }

    public int getLabelAlign() {
        return this.labelAlign;
    }

    public int getLineAlign() {
        return this.lineAlign;
    }

    public void setAlignment(int alignment) {
        setAlignment(alignment, alignment);
    }

    public void setAlignment(int labelAlign, int lineAlign) {
        this.labelAlign = labelAlign;
        if ((lineAlign & 8) != 0) {
            this.lineAlign = 8;
        } else if ((lineAlign & 16) != 0) {
            this.lineAlign = 16;
        } else {
            this.lineAlign = 1;
        }
        invalidate();
    }

    public void setFontScale(float fontScale) {
        this.fontScaleX = fontScale;
        this.fontScaleY = fontScale;
        invalidateHierarchy();
    }

    public void setFontScale(float fontScaleX, float fontScaleY) {
        this.fontScaleX = fontScaleX;
        this.fontScaleY = fontScaleY;
        invalidateHierarchy();
    }

    public float getFontScaleX() {
        return this.fontScaleX;
    }

    public void setFontScaleX(float fontScaleX) {
        this.fontScaleX = fontScaleX;
        invalidateHierarchy();
    }

    public float getFontScaleY() {
        return this.fontScaleY;
    }

    public void setFontScaleY(float fontScaleY) {
        this.fontScaleY = fontScaleY;
        invalidateHierarchy();
    }

    public void setEllipsis(String ellipsis) {
        this.ellipsis = ellipsis;
    }

    public void setEllipsis(boolean ellipsis) {
        if (ellipsis) {
            this.ellipsis = "...";
        } else {
            this.ellipsis = null;
        }
    }

    protected BitmapFontCache getBitmapFontCache() {
        return this.cache;
    }

    public String toString() {
        return super.toString() + ": " + this.text;
    }
}
