package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

public class VerticalGroup extends WidgetGroup {
    private int align;
    private float fill;
    private float padBottom;
    private float padLeft;
    private float padRight;
    private float padTop;
    private float prefHeight;
    private float prefWidth;
    private boolean reverse;
    private boolean round = true;
    private boolean sizeInvalid = true;
    private float spacing;

    public VerticalGroup() {
        setTouchable(Touchable.childrenOnly);
    }

    public void invalidate() {
        super.invalidate();
        this.sizeInvalid = true;
    }

    private void computeSize() {
        this.sizeInvalid = false;
        SnapshotArray<Actor> children = getChildren();
        int n = children.size;
        this.prefWidth = 0.0f;
        this.prefHeight = (this.padTop + this.padBottom) + (this.spacing * ((float) (n - 1)));
        for (int i = 0; i < n; i++) {
            Actor child = (Actor) children.get(i);
            if (child instanceof Layout) {
                Layout layout = (Layout) child;
                this.prefWidth = Math.max(this.prefWidth, layout.getPrefWidth());
                this.prefHeight += layout.getPrefHeight();
            } else {
                this.prefWidth = Math.max(this.prefWidth, child.getWidth());
                this.prefHeight += child.getHeight();
            }
        }
        this.prefWidth += this.padLeft + this.padRight;
        if (this.round) {
            this.prefWidth = (float) Math.round(this.prefWidth);
            this.prefHeight = (float) Math.round(this.prefHeight);
        }
    }

    public void layout() {
        float y;
        float spacing = this.spacing;
        float padLeft = this.padLeft;
        int align = this.align;
        boolean reverse = this.reverse;
        boolean round = this.round;
        float groupWidth = (getWidth() - padLeft) - this.padRight;
        if (reverse) {
            y = this.padBottom;
        } else {
            y = (getHeight() - this.padTop) + spacing;
        }
        SnapshotArray<Actor> children = getChildren();
        int n = children.size;
        for (int i = 0; i < n; i++) {
            float width;
            float height;
            Actor child = (Actor) children.get(i);
            Layout layout = null;
            if (child instanceof Layout) {
                layout = (Layout) child;
                if (this.fill > 0.0f) {
                    width = groupWidth * this.fill;
                } else {
                    width = Math.min(layout.getPrefWidth(), groupWidth);
                }
                width = Math.max(width, layout.getMinWidth());
                float maxWidth = layout.getMaxWidth();
                if (maxWidth > 0.0f && width > maxWidth) {
                    width = maxWidth;
                }
                height = layout.getPrefHeight();
            } else {
                width = child.getWidth();
                height = child.getHeight();
                if (this.fill > 0.0f) {
                    width *= this.fill;
                }
            }
            float x = padLeft;
            if ((align & 16) != 0) {
                x += groupWidth - width;
            } else if ((align & 8) == 0) {
                x += (groupWidth - width) / 2.0f;
            }
            if (!reverse) {
                y -= height + spacing;
            }
            if (round) {
                child.setBounds((float) Math.round(x), (float) Math.round(y), (float) Math.round(width), (float) Math.round(height));
            } else {
                child.setBounds(x, y, width, height);
            }
            if (reverse) {
                y += height + spacing;
            }
            if (layout != null) {
                layout.validate();
            }
        }
    }

    public float getPrefWidth() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.prefWidth;
    }

    public float getPrefHeight() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.prefHeight;
    }

    public void setRound(boolean round) {
        this.round = round;
    }

    public VerticalGroup reverse() {
        reverse(true);
        return this;
    }

    public VerticalGroup reverse(boolean reverse) {
        this.reverse = reverse;
        return this;
    }

    public boolean getReverse() {
        return this.reverse;
    }

    public VerticalGroup space(float spacing) {
        this.spacing = spacing;
        return this;
    }

    public float getSpace() {
        return this.spacing;
    }

    public VerticalGroup pad(float pad) {
        this.padTop = pad;
        this.padLeft = pad;
        this.padBottom = pad;
        this.padRight = pad;
        return this;
    }

    public VerticalGroup pad(float top, float left, float bottom, float right) {
        this.padTop = top;
        this.padLeft = left;
        this.padBottom = bottom;
        this.padRight = right;
        return this;
    }

    public VerticalGroup padTop(float padTop) {
        this.padTop = padTop;
        return this;
    }

    public VerticalGroup padLeft(float padLeft) {
        this.padLeft = padLeft;
        return this;
    }

    public VerticalGroup padBottom(float padBottom) {
        this.padBottom = padBottom;
        return this;
    }

    public VerticalGroup padRight(float padRight) {
        this.padRight = padRight;
        return this;
    }

    public float getPadTop() {
        return this.padTop;
    }

    public float getPadLeft() {
        return this.padLeft;
    }

    public float getPadBottom() {
        return this.padBottom;
    }

    public float getPadRight() {
        return this.padRight;
    }

    public VerticalGroup align(int align) {
        this.align = align;
        return this;
    }

    public VerticalGroup center() {
        this.align = 1;
        return this;
    }

    public VerticalGroup left() {
        this.align |= 8;
        this.align &= -17;
        return this;
    }

    public VerticalGroup right() {
        this.align |= 16;
        this.align &= -9;
        return this;
    }

    public int getAlign() {
        return this.align;
    }

    public VerticalGroup fill() {
        this.fill = 1.0f;
        return this;
    }

    public VerticalGroup fill(float fill) {
        this.fill = fill;
        return this;
    }

    public float getFill() {
        return this.fill;
    }
}
