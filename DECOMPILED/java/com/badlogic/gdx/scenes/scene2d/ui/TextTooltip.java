package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;

public class TextTooltip extends Tooltip<Label> {

    public static class TextTooltipStyle {
        public Drawable background;
        public LabelStyle label;

        public TextTooltipStyle(LabelStyle label, Drawable background) {
            this.label = label;
            this.background = background;
        }

        public TextTooltipStyle(TextTooltipStyle style) {
            this.label = new LabelStyle(style.label);
            this.background = style.background;
        }
    }

    public TextTooltip(String text, Skin skin) {
        this(text, TooltipManager.getInstance(), (TextTooltipStyle) skin.get(TextTooltipStyle.class));
    }

    public TextTooltip(String text, Skin skin, String styleName) {
        this(text, TooltipManager.getInstance(), (TextTooltipStyle) skin.get(styleName, TextTooltipStyle.class));
    }

    public TextTooltip(String text, TextTooltipStyle style) {
        this(text, TooltipManager.getInstance(), style);
    }

    public TextTooltip(String text, TooltipManager manager, Skin skin) {
        this(text, manager, (TextTooltipStyle) skin.get(TextTooltipStyle.class));
    }

    public TextTooltip(String text, TooltipManager manager, Skin skin, String styleName) {
        this(text, manager, (TextTooltipStyle) skin.get(styleName, TextTooltipStyle.class));
    }

    public TextTooltip(String text, final TooltipManager manager, TextTooltipStyle style) {
        super(null, manager);
        Label label = new Label((CharSequence) text, style.label);
        label.setWrap(true);
        this.container.setActor(label);
        this.container.width(new Value() {
            public float get(Actor context) {
                return Math.min(manager.maxWidth, ((Label) TextTooltip.this.container.getActor()).getGlyphLayout().width);
            }
        });
        setStyle(style);
    }

    public void setStyle(TextTooltipStyle style) {
        if (style == null) {
            throw new NullPointerException("style cannot be null");
        } else if (style instanceof TextTooltipStyle) {
            ((Label) this.container.getActor()).setStyle(style.label);
            this.container.setBackground(style.background);
            this.container.pack();
            this.container.pack();
        } else {
            throw new IllegalArgumentException("style must be a TextTooltipStyle.");
        }
    }
}
