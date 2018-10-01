package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class CheckBox extends TextButton {
    private Image image;
    private Cell imageCell;
    private CheckBoxStyle style;

    public static class CheckBoxStyle extends TextButtonStyle {
        public Drawable checkboxOff;
        public Drawable checkboxOffDisabled;
        public Drawable checkboxOn;
        public Drawable checkboxOnDisabled;
        public Drawable checkboxOver;

        public CheckBoxStyle(Drawable checkboxOff, Drawable checkboxOn, BitmapFont font, Color fontColor) {
            this.checkboxOff = checkboxOff;
            this.checkboxOn = checkboxOn;
            this.font = font;
            this.fontColor = fontColor;
        }

        public CheckBoxStyle(CheckBoxStyle style) {
            this.checkboxOff = style.checkboxOff;
            this.checkboxOn = style.checkboxOn;
            this.checkboxOver = style.checkboxOver;
            this.checkboxOffDisabled = style.checkboxOffDisabled;
            this.checkboxOnDisabled = style.checkboxOnDisabled;
            this.font = style.font;
            this.fontColor = new Color(style.fontColor);
        }
    }

    public CheckBox(String text, Skin skin) {
        this(text, (CheckBoxStyle) skin.get(CheckBoxStyle.class));
    }

    public CheckBox(String text, Skin skin, String styleName) {
        this(text, (CheckBoxStyle) skin.get(styleName, CheckBoxStyle.class));
    }

    public CheckBox(String text, CheckBoxStyle style) {
        super(text, (TextButtonStyle) style);
        clearChildren();
        Label label = getLabel();
        Actor image = new Image(style.checkboxOff);
        this.image = image;
        this.imageCell = add(image);
        add((Actor) label);
        label.setAlignment(8);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public void setStyle(ButtonStyle style) {
        if (style instanceof CheckBoxStyle) {
            super.setStyle(style);
            this.style = (CheckBoxStyle) style;
            return;
        }
        throw new IllegalArgumentException("style must be a CheckBoxStyle.");
    }

    public CheckBoxStyle getStyle() {
        return this.style;
    }

    public void draw(Batch batch, float parentAlpha) {
        Drawable checkbox = null;
        if (isDisabled()) {
            if (!this.isChecked || this.style.checkboxOnDisabled == null) {
                checkbox = this.style.checkboxOffDisabled;
            } else {
                checkbox = this.style.checkboxOnDisabled;
            }
        }
        if (checkbox == null) {
            if (this.isChecked && this.style.checkboxOn != null) {
                checkbox = this.style.checkboxOn;
            } else if (!isOver() || this.style.checkboxOver == null || isDisabled()) {
                checkbox = this.style.checkboxOff;
            } else {
                checkbox = this.style.checkboxOver;
            }
        }
        this.image.setDrawable(checkbox);
        super.draw(batch, parentAlpha);
    }

    public Image getImage() {
        return this.image;
    }

    public Cell getImageCell() {
        return this.imageCell;
    }
}
