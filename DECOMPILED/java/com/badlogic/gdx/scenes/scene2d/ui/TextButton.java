package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TextButton extends Button {
    private final Label label;
    private TextButtonStyle style;

    public static class TextButtonStyle extends ButtonStyle {
        public Color checkedFontColor;
        public Color checkedOverFontColor;
        public Color disabledFontColor;
        public Color downFontColor;
        public BitmapFont font;
        public Color fontColor;
        public Color overFontColor;

        public TextButtonStyle(Drawable up, Drawable down, Drawable checked, BitmapFont font) {
            super(up, down, checked);
            this.font = font;
        }

        public TextButtonStyle(TextButtonStyle style) {
            super(style);
            this.font = style.font;
            if (style.fontColor != null) {
                this.fontColor = new Color(style.fontColor);
            }
            if (style.downFontColor != null) {
                this.downFontColor = new Color(style.downFontColor);
            }
            if (style.overFontColor != null) {
                this.overFontColor = new Color(style.overFontColor);
            }
            if (style.checkedFontColor != null) {
                this.checkedFontColor = new Color(style.checkedFontColor);
            }
            if (style.checkedOverFontColor != null) {
                this.checkedFontColor = new Color(style.checkedOverFontColor);
            }
            if (style.disabledFontColor != null) {
                this.disabledFontColor = new Color(style.disabledFontColor);
            }
        }
    }

    public TextButton(String text, Skin skin) {
        this(text, (TextButtonStyle) skin.get(TextButtonStyle.class));
        setSkin(skin);
    }

    public TextButton(String text, Skin skin, String styleName) {
        this(text, (TextButtonStyle) skin.get(styleName, TextButtonStyle.class));
        setSkin(skin);
    }

    public TextButton(String text, TextButtonStyle style) {
        setStyle(style);
        this.style = style;
        this.label = new Label((CharSequence) text, new LabelStyle(style.font, style.fontColor));
        this.label.setAlignment(1);
        add(this.label).expand().fill();
        setSize(getPrefWidth(), getPrefHeight());
    }

    public void setStyle(ButtonStyle style) {
        if (style == null) {
            throw new NullPointerException("style cannot be null");
        } else if (style instanceof TextButtonStyle) {
            super.setStyle(style);
            this.style = (TextButtonStyle) style;
            if (this.label != null) {
                TextButtonStyle textButtonStyle = (TextButtonStyle) style;
                LabelStyle labelStyle = this.label.getStyle();
                labelStyle.font = textButtonStyle.font;
                labelStyle.fontColor = textButtonStyle.fontColor;
                this.label.setStyle(labelStyle);
            }
        } else {
            throw new IllegalArgumentException("style must be a TextButtonStyle.");
        }
    }

    public TextButtonStyle getStyle() {
        return this.style;
    }

    public void draw(Batch batch, float parentAlpha) {
        Color fontColor;
        if (isDisabled() && this.style.disabledFontColor != null) {
            fontColor = this.style.disabledFontColor;
        } else if (isPressed() && this.style.downFontColor != null) {
            fontColor = this.style.downFontColor;
        } else if (this.isChecked && this.style.checkedFontColor != null) {
            fontColor = (!isOver() || this.style.checkedOverFontColor == null) ? this.style.checkedFontColor : this.style.checkedOverFontColor;
        } else if (!isOver() || this.style.overFontColor == null) {
            fontColor = this.style.fontColor;
        } else {
            fontColor = this.style.overFontColor;
        }
        if (fontColor != null) {
            this.label.getStyle().fontColor = fontColor;
        }
        super.draw(batch, parentAlpha);
    }

    public Label getLabel() {
        return this.label;
    }

    public Cell getLabelCell() {
        return getCell(this.label);
    }

    public void setText(String text) {
        this.label.setText(text);
    }

    public CharSequence getText() {
        return this.label.getText();
    }
}
