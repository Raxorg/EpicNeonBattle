package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Pools;

public class Slider extends ProgressBar {
    int draggingPointer;
    boolean mouseOver;
    private Interpolation visualInterpolationInverse;

    public static class SliderStyle extends ProgressBarStyle {
        public Drawable knobDown;
        public Drawable knobOver;

        public SliderStyle(Drawable background, Drawable knob) {
            super(background, knob);
        }

        public SliderStyle(SliderStyle style) {
            super(style);
            this.knobOver = style.knobOver;
            this.knobDown = style.knobDown;
        }
    }

    public Slider(float min, float max, float stepSize, boolean vertical, Skin skin) {
        this(min, max, stepSize, vertical, (SliderStyle) skin.get("default-" + (vertical ? "vertical" : "horizontal"), SliderStyle.class));
    }

    public Slider(float min, float max, float stepSize, boolean vertical, Skin skin, String styleName) {
        this(min, max, stepSize, vertical, (SliderStyle) skin.get(styleName, SliderStyle.class));
    }

    public Slider(float min, float max, float stepSize, boolean vertical, SliderStyle style) {
        super(min, max, stepSize, vertical, (ProgressBarStyle) style);
        this.draggingPointer = -1;
        this.visualInterpolationInverse = Interpolation.linear;
        this.shiftIgnoresSnap = true;
        addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (Slider.this.disabled || Slider.this.draggingPointer != -1) {
                    return false;
                }
                Slider.this.draggingPointer = pointer;
                Slider.this.calculatePositionAndValue(x, y);
                return true;
            }

            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (pointer == Slider.this.draggingPointer) {
                    Slider.this.draggingPointer = -1;
                    if (!Slider.this.calculatePositionAndValue(x, y)) {
                        ChangeEvent changeEvent = (ChangeEvent) Pools.obtain(ChangeEvent.class);
                        Slider.this.fire(changeEvent);
                        Pools.free(changeEvent);
                    }
                }
            }

            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                Slider.this.calculatePositionAndValue(x, y);
            }

            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    Slider.this.mouseOver = true;
                }
            }

            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    Slider.this.mouseOver = false;
                }
            }
        });
    }

    public void setStyle(SliderStyle style) {
        if (style == null) {
            throw new NullPointerException("style cannot be null");
        } else if (style instanceof SliderStyle) {
            super.setStyle(style);
        } else {
            throw new IllegalArgumentException("style must be a SliderStyle.");
        }
    }

    public SliderStyle getStyle() {
        return (SliderStyle) super.getStyle();
    }

    protected Drawable getKnobDrawable() {
        SliderStyle style = getStyle();
        if (this.disabled && style.disabledKnob != null) {
            return style.disabledKnob;
        }
        if (!isDragging() || style.knobDown == null) {
            return (!this.mouseOver || style.knobOver == null) ? style.knob : style.knobOver;
        } else {
            return style.knobDown;
        }
    }

    boolean calculatePositionAndValue(float x, float y) {
        float value;
        SliderStyle style = getStyle();
        Drawable knob = getKnobDrawable();
        Drawable bg = (!this.disabled || style.disabledBackground == null) ? style.background : style.disabledBackground;
        float oldPosition = this.position;
        float min = getMinValue();
        float max = getMaxValue();
        if (this.vertical) {
            float height = (getHeight() - bg.getTopHeight()) - bg.getBottomHeight();
            float knobHeight = knob == null ? 0.0f : knob.getMinHeight();
            this.position = (y - bg.getBottomHeight()) - (0.5f * knobHeight);
            value = min + ((max - min) * this.visualInterpolationInverse.apply(this.position / (height - knobHeight)));
            this.position = Math.max(0.0f, this.position);
            this.position = Math.min(height - knobHeight, this.position);
        } else {
            float width = (getWidth() - bg.getLeftWidth()) - bg.getRightWidth();
            float knobWidth = knob == null ? 0.0f : knob.getMinWidth();
            this.position = (x - bg.getLeftWidth()) - (0.5f * knobWidth);
            value = min + ((max - min) * this.visualInterpolationInverse.apply(this.position / (width - knobWidth)));
            this.position = Math.max(0.0f, this.position);
            this.position = Math.min(width - knobWidth, this.position);
        }
        float oldValue = value;
        boolean valueSet = setValue(value);
        if (value == oldValue) {
            this.position = oldPosition;
        }
        return valueSet;
    }

    public boolean isDragging() {
        return this.draggingPointer != -1;
    }

    public void setVisualInterpolationInverse(Interpolation interpolation) {
        this.visualInterpolationInverse = interpolation;
    }
}
