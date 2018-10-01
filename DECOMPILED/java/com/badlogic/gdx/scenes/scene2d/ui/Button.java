package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public class Button extends Table implements Disableable {
    ButtonGroup buttonGroup;
    private ClickListener clickListener;
    boolean isChecked;
    boolean isDisabled;
    private boolean programmaticChangeEvents;
    private ButtonStyle style;

    public static class ButtonStyle {
        public Drawable checked;
        public Drawable checkedOver;
        public Drawable disabled;
        public Drawable down;
        public Drawable over;
        public float pressedOffsetX;
        public float pressedOffsetY;
        public float unpressedOffsetX;
        public float unpressedOffsetY;
        public Drawable up;

        public ButtonStyle(Drawable up, Drawable down, Drawable checked) {
            this.up = up;
            this.down = down;
            this.checked = checked;
        }

        public ButtonStyle(ButtonStyle style) {
            this.up = style.up;
            this.down = style.down;
            this.over = style.over;
            this.checked = style.checked;
            this.checkedOver = style.checkedOver;
            this.disabled = style.disabled;
            this.pressedOffsetX = style.pressedOffsetX;
            this.pressedOffsetY = style.pressedOffsetY;
            this.unpressedOffsetX = style.unpressedOffsetX;
            this.unpressedOffsetY = style.unpressedOffsetY;
        }
    }

    public Button(Skin skin) {
        super(skin);
        this.programmaticChangeEvents = true;
        initialize();
        setStyle((ButtonStyle) skin.get(ButtonStyle.class));
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button(Skin skin, String styleName) {
        super(skin);
        this.programmaticChangeEvents = true;
        initialize();
        setStyle((ButtonStyle) skin.get(styleName, ButtonStyle.class));
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button(Actor child, Skin skin, String styleName) {
        this(child, (ButtonStyle) skin.get(styleName, ButtonStyle.class));
    }

    public Button(Actor child, ButtonStyle style) {
        this.programmaticChangeEvents = true;
        initialize();
        add(child);
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button(ButtonStyle style) {
        this.programmaticChangeEvents = true;
        initialize();
        setStyle(style);
        setSize(getPrefWidth(), getPrefHeight());
    }

    public Button() {
        this.programmaticChangeEvents = true;
        initialize();
    }

    private void initialize() {
        setTouchable(Touchable.enabled);
        EventListener anonymousClass1 = new ClickListener() {
            public void clicked(InputEvent event, float x, float y) {
                if (!Button.this.isDisabled()) {
                    Button.this.setChecked(!Button.this.isChecked, true);
                }
            }
        };
        this.clickListener = anonymousClass1;
        addListener(anonymousClass1);
    }

    public Button(Drawable up) {
        this(new ButtonStyle(up, null, null));
    }

    public Button(Drawable up, Drawable down) {
        this(new ButtonStyle(up, down, null));
    }

    public Button(Drawable up, Drawable down, Drawable checked) {
        this(new ButtonStyle(up, down, checked));
    }

    public Button(Actor child, Skin skin) {
        this(child, (ButtonStyle) skin.get(ButtonStyle.class));
    }

    public void setChecked(boolean isChecked) {
        setChecked(isChecked, this.programmaticChangeEvents);
    }

    void setChecked(boolean isChecked, boolean fireEvent) {
        if (this.isChecked != isChecked) {
            if (this.buttonGroup == null || this.buttonGroup.canCheck(this, isChecked)) {
                this.isChecked = isChecked;
                if (fireEvent) {
                    ChangeEvent changeEvent = (ChangeEvent) Pools.obtain(ChangeEvent.class);
                    if (fire(changeEvent)) {
                        this.isChecked = !isChecked;
                    }
                    Pools.free(changeEvent);
                }
            }
        }
    }

    public void toggle() {
        setChecked(!this.isChecked);
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public boolean isPressed() {
        return this.clickListener.isVisualPressed();
    }

    public boolean isOver() {
        return this.clickListener.isOver();
    }

    public ClickListener getClickListener() {
        return this.clickListener;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public void setProgrammaticChangeEvents(boolean programmaticChangeEvents) {
        this.programmaticChangeEvents = programmaticChangeEvents;
    }

    public void setStyle(ButtonStyle style) {
        if (style == null) {
            throw new IllegalArgumentException("style cannot be null.");
        }
        Drawable background;
        this.style = style;
        if (!isPressed() || isDisabled()) {
            if (isDisabled() && style.disabled != null) {
                background = style.disabled;
            } else if (this.isChecked && style.checked != null) {
                background = (!isOver() || style.checkedOver == null) ? style.checked : style.checkedOver;
            } else if (!isOver() || style.over == null) {
                background = style.up;
            } else {
                background = style.over;
            }
        } else if (style.down == null) {
            background = style.up;
        } else {
            background = style.down;
        }
        setBackground(background);
    }

    public ButtonStyle getStyle() {
        return this.style;
    }

    public ButtonGroup getButtonGroup() {
        return this.buttonGroup;
    }

    public void draw(Batch batch, float parentAlpha) {
        float offsetX;
        float offsetY;
        int i;
        validate();
        boolean isPressed = isPressed();
        boolean isDisabled = isDisabled();
        Drawable background = null;
        if (isDisabled && this.style.disabled != null) {
            background = this.style.disabled;
        } else if (isPressed && this.style.down != null) {
            background = this.style.down;
        } else if (this.isChecked && this.style.checked != null) {
            background = (this.style.checkedOver == null || !isOver()) ? this.style.checked : this.style.checkedOver;
        } else if (isOver() && this.style.over != null) {
            background = this.style.over;
        } else if (this.style.up != null) {
            background = this.style.up;
        }
        setBackground(background);
        if (!isPressed || isDisabled) {
            offsetX = this.style.unpressedOffsetX;
            offsetY = this.style.unpressedOffsetY;
        } else {
            offsetX = this.style.pressedOffsetX;
            offsetY = this.style.pressedOffsetY;
        }
        Array<Actor> children = getChildren();
        for (i = 0; i < children.size; i++) {
            ((Actor) children.get(i)).moveBy(offsetX, offsetY);
        }
        super.draw(batch, parentAlpha);
        for (i = 0; i < children.size; i++) {
            ((Actor) children.get(i)).moveBy(-offsetX, -offsetY);
        }
        Stage stage = getStage();
        if (stage != null && stage.getActionsRequestRendering() && isPressed != this.clickListener.isPressed()) {
            Gdx.graphics.requestRendering();
        }
    }

    public float getPrefWidth() {
        float width = super.getPrefWidth();
        if (this.style.up != null) {
            width = Math.max(width, this.style.up.getMinWidth());
        }
        if (this.style.down != null) {
            width = Math.max(width, this.style.down.getMinWidth());
        }
        if (this.style.checked != null) {
            return Math.max(width, this.style.checked.getMinWidth());
        }
        return width;
    }

    public float getPrefHeight() {
        float height = super.getPrefHeight();
        if (this.style.up != null) {
            height = Math.max(height, this.style.up.getMinHeight());
        }
        if (this.style.down != null) {
            height = Math.max(height, this.style.down.getMinHeight());
        }
        if (this.style.checked != null) {
            return Math.max(height, this.style.checked.getMinHeight());
        }
        return height;
    }

    public float getMinWidth() {
        return getPrefWidth();
    }

    public float getMinHeight() {
        return getPrefHeight();
    }
}
