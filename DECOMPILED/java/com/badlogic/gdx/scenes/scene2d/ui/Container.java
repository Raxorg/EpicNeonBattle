package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Value.Fixed;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class Container<T extends Actor> extends WidgetGroup {
    private T actor;
    private int align;
    private Drawable background;
    private boolean clip;
    private float fillX;
    private float fillY;
    private Value maxHeight;
    private Value maxWidth;
    private Value minHeight;
    private Value minWidth;
    private Value padBottom;
    private Value padLeft;
    private Value padRight;
    private Value padTop;
    private Value prefHeight;
    private Value prefWidth;
    private boolean round;

    public Container() {
        this.minWidth = Value.minWidth;
        this.minHeight = Value.minHeight;
        this.prefWidth = Value.prefWidth;
        this.prefHeight = Value.prefHeight;
        this.maxWidth = Value.zero;
        this.maxHeight = Value.zero;
        this.padTop = Value.zero;
        this.padLeft = Value.zero;
        this.padBottom = Value.zero;
        this.padRight = Value.zero;
        this.round = true;
        setTouchable(Touchable.childrenOnly);
        setTransform(false);
    }

    public Container(T actor) {
        this();
        setActor(actor);
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        if (isTransform()) {
            applyTransform(batch, computeTransform());
            drawBackground(batch, parentAlpha, 0.0f, 0.0f);
            if (this.clip) {
                batch.flush();
                float padLeft = this.padLeft.get(this);
                float padBottom = this.padBottom.get(this);
                if (clipBegin(padLeft, padBottom, (getWidth() - padLeft) - this.padRight.get(this), (getHeight() - padBottom) - this.padTop.get(this))) {
                    drawChildren(batch, parentAlpha);
                    batch.flush();
                    clipEnd();
                }
            } else {
                drawChildren(batch, parentAlpha);
            }
            resetTransform(batch);
            return;
        }
        drawBackground(batch, parentAlpha, getX(), getY());
        super.draw(batch, parentAlpha);
    }

    protected void drawBackground(Batch batch, float parentAlpha, float x, float y) {
        if (this.background != null) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            this.background.draw(batch, x, y, getWidth(), getHeight());
        }
    }

    public void setBackground(Drawable background) {
        setBackground(background, true);
    }

    public void setBackground(Drawable background, boolean adjustPadding) {
        if (this.background != background) {
            this.background = background;
            if (adjustPadding) {
                if (background == null) {
                    pad(Value.zero);
                } else {
                    pad(background.getTopHeight(), background.getLeftWidth(), background.getBottomHeight(), background.getRightWidth());
                }
                invalidate();
            }
        }
    }

    public Container<T> background(Drawable background) {
        setBackground(background);
        return this;
    }

    public Drawable getBackground() {
        return this.background;
    }

    public void layout() {
        if (this.actor != null) {
            float width;
            float height;
            float padLeft = this.padLeft.get(this);
            float padBottom = this.padBottom.get(this);
            float containerWidth = (getWidth() - padLeft) - this.padRight.get(this);
            float containerHeight = (getHeight() - padBottom) - this.padTop.get(this);
            float minWidth = this.minWidth.get(this.actor);
            float minHeight = this.minHeight.get(this.actor);
            float prefWidth = this.prefWidth.get(this.actor);
            float prefHeight = this.prefHeight.get(this.actor);
            float maxWidth = this.maxWidth.get(this.actor);
            float maxHeight = this.maxHeight.get(this.actor);
            if (this.fillX > 0.0f) {
                width = containerWidth * this.fillX;
            } else {
                width = Math.min(prefWidth, containerWidth);
            }
            if (width < minWidth) {
                width = minWidth;
            }
            if (maxWidth > 0.0f && width > maxWidth) {
                width = maxWidth;
            }
            if (this.fillY > 0.0f) {
                height = containerHeight * this.fillY;
            } else {
                height = Math.min(prefHeight, containerHeight);
            }
            if (height < minHeight) {
                height = minHeight;
            }
            if (maxHeight > 0.0f && height > maxHeight) {
                height = maxHeight;
            }
            float x = padLeft;
            if ((this.align & 16) != 0) {
                x += containerWidth - width;
            } else if ((this.align & 8) == 0) {
                x += (containerWidth - width) / 2.0f;
            }
            float y = padBottom;
            if ((this.align & 2) != 0) {
                y += containerHeight - height;
            } else if ((this.align & 4) == 0) {
                y += (containerHeight - height) / 2.0f;
            }
            if (this.round) {
                x = (float) Math.round(x);
                y = (float) Math.round(y);
                width = (float) Math.round(width);
                height = (float) Math.round(height);
            }
            this.actor.setBounds(x, y, width, height);
            if (this.actor instanceof Layout) {
                ((Layout) this.actor).validate();
            }
        }
    }

    public void setActor(T actor) {
        if (actor == this) {
            throw new IllegalArgumentException("actor cannot be the Container.");
        } else if (actor != this.actor) {
            if (this.actor != null) {
                super.removeActor(this.actor);
            }
            this.actor = actor;
            if (actor != null) {
                super.addActor(actor);
            }
        }
    }

    public T getActor() {
        return this.actor;
    }

    public void addActor(Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }

    public void addActorAt(int index, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }

    public void addActorBefore(Actor actorBefore, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }

    public void addActorAfter(Actor actorAfter, Actor actor) {
        throw new UnsupportedOperationException("Use Container#setActor.");
    }

    public boolean removeActor(Actor actor) {
        if (actor != this.actor) {
            return false;
        }
        setActor(null);
        return true;
    }

    public Container<T> size(Value size) {
        if (size == null) {
            throw new IllegalArgumentException("size cannot be null.");
        }
        this.minWidth = size;
        this.minHeight = size;
        this.prefWidth = size;
        this.prefHeight = size;
        this.maxWidth = size;
        this.maxHeight = size;
        return this;
    }

    public Container<T> size(Value width, Value height) {
        if (width == null) {
            throw new IllegalArgumentException("width cannot be null.");
        } else if (height == null) {
            throw new IllegalArgumentException("height cannot be null.");
        } else {
            this.minWidth = width;
            this.minHeight = height;
            this.prefWidth = width;
            this.prefHeight = height;
            this.maxWidth = width;
            this.maxHeight = height;
            return this;
        }
    }

    public Container<T> size(float size) {
        size(new Fixed(size));
        return this;
    }

    public Container<T> size(float width, float height) {
        size(new Fixed(width), new Fixed(height));
        return this;
    }

    public Container<T> width(Value width) {
        if (width == null) {
            throw new IllegalArgumentException("width cannot be null.");
        }
        this.minWidth = width;
        this.prefWidth = width;
        this.maxWidth = width;
        return this;
    }

    public Container<T> width(float width) {
        width(new Fixed(width));
        return this;
    }

    public Container<T> height(Value height) {
        if (height == null) {
            throw new IllegalArgumentException("height cannot be null.");
        }
        this.minHeight = height;
        this.prefHeight = height;
        this.maxHeight = height;
        return this;
    }

    public Container<T> height(float height) {
        height(new Fixed(height));
        return this;
    }

    public Container<T> minSize(Value size) {
        if (size == null) {
            throw new IllegalArgumentException("size cannot be null.");
        }
        this.minWidth = size;
        this.minHeight = size;
        return this;
    }

    public Container<T> minSize(Value width, Value height) {
        if (width == null) {
            throw new IllegalArgumentException("width cannot be null.");
        } else if (height == null) {
            throw new IllegalArgumentException("height cannot be null.");
        } else {
            this.minWidth = width;
            this.minHeight = height;
            return this;
        }
    }

    public Container<T> minWidth(Value minWidth) {
        if (minWidth == null) {
            throw new IllegalArgumentException("minWidth cannot be null.");
        }
        this.minWidth = minWidth;
        return this;
    }

    public Container<T> minHeight(Value minHeight) {
        if (minHeight == null) {
            throw new IllegalArgumentException("minHeight cannot be null.");
        }
        this.minHeight = minHeight;
        return this;
    }

    public Container<T> minSize(float size) {
        minSize(new Fixed(size));
        return this;
    }

    public Container<T> minSize(float width, float height) {
        minSize(new Fixed(width), new Fixed(height));
        return this;
    }

    public Container<T> minWidth(float minWidth) {
        this.minWidth = new Fixed(minWidth);
        return this;
    }

    public Container<T> minHeight(float minHeight) {
        this.minHeight = new Fixed(minHeight);
        return this;
    }

    public Container<T> prefSize(Value size) {
        if (size == null) {
            throw new IllegalArgumentException("size cannot be null.");
        }
        this.prefWidth = size;
        this.prefHeight = size;
        return this;
    }

    public Container<T> prefSize(Value width, Value height) {
        if (width == null) {
            throw new IllegalArgumentException("width cannot be null.");
        } else if (height == null) {
            throw new IllegalArgumentException("height cannot be null.");
        } else {
            this.prefWidth = width;
            this.prefHeight = height;
            return this;
        }
    }

    public Container<T> prefWidth(Value prefWidth) {
        if (prefWidth == null) {
            throw new IllegalArgumentException("prefWidth cannot be null.");
        }
        this.prefWidth = prefWidth;
        return this;
    }

    public Container<T> prefHeight(Value prefHeight) {
        if (prefHeight == null) {
            throw new IllegalArgumentException("prefHeight cannot be null.");
        }
        this.prefHeight = prefHeight;
        return this;
    }

    public Container<T> prefSize(float width, float height) {
        prefSize(new Fixed(width), new Fixed(height));
        return this;
    }

    public Container<T> prefSize(float size) {
        prefSize(new Fixed(size));
        return this;
    }

    public Container<T> prefWidth(float prefWidth) {
        this.prefWidth = new Fixed(prefWidth);
        return this;
    }

    public Container<T> prefHeight(float prefHeight) {
        this.prefHeight = new Fixed(prefHeight);
        return this;
    }

    public Container<T> maxSize(Value size) {
        if (size == null) {
            throw new IllegalArgumentException("size cannot be null.");
        }
        this.maxWidth = size;
        this.maxHeight = size;
        return this;
    }

    public Container<T> maxSize(Value width, Value height) {
        if (width == null) {
            throw new IllegalArgumentException("width cannot be null.");
        } else if (height == null) {
            throw new IllegalArgumentException("height cannot be null.");
        } else {
            this.maxWidth = width;
            this.maxHeight = height;
            return this;
        }
    }

    public Container<T> maxWidth(Value maxWidth) {
        if (maxWidth == null) {
            throw new IllegalArgumentException("maxWidth cannot be null.");
        }
        this.maxWidth = maxWidth;
        return this;
    }

    public Container<T> maxHeight(Value maxHeight) {
        if (maxHeight == null) {
            throw new IllegalArgumentException("maxHeight cannot be null.");
        }
        this.maxHeight = maxHeight;
        return this;
    }

    public Container<T> maxSize(float size) {
        maxSize(new Fixed(size));
        return this;
    }

    public Container<T> maxSize(float width, float height) {
        maxSize(new Fixed(width), new Fixed(height));
        return this;
    }

    public Container<T> maxWidth(float maxWidth) {
        this.maxWidth = new Fixed(maxWidth);
        return this;
    }

    public Container<T> maxHeight(float maxHeight) {
        this.maxHeight = new Fixed(maxHeight);
        return this;
    }

    public Container<T> pad(Value pad) {
        if (pad == null) {
            throw new IllegalArgumentException("pad cannot be null.");
        }
        this.padTop = pad;
        this.padLeft = pad;
        this.padBottom = pad;
        this.padRight = pad;
        return this;
    }

    public Container<T> pad(Value top, Value left, Value bottom, Value right) {
        if (top == null) {
            throw new IllegalArgumentException("top cannot be null.");
        } else if (left == null) {
            throw new IllegalArgumentException("left cannot be null.");
        } else if (bottom == null) {
            throw new IllegalArgumentException("bottom cannot be null.");
        } else if (right == null) {
            throw new IllegalArgumentException("right cannot be null.");
        } else {
            this.padTop = top;
            this.padLeft = left;
            this.padBottom = bottom;
            this.padRight = right;
            return this;
        }
    }

    public Container<T> padTop(Value padTop) {
        if (padTop == null) {
            throw new IllegalArgumentException("padTop cannot be null.");
        }
        this.padTop = padTop;
        return this;
    }

    public Container<T> padLeft(Value padLeft) {
        if (padLeft == null) {
            throw new IllegalArgumentException("padLeft cannot be null.");
        }
        this.padLeft = padLeft;
        return this;
    }

    public Container<T> padBottom(Value padBottom) {
        if (padBottom == null) {
            throw new IllegalArgumentException("padBottom cannot be null.");
        }
        this.padBottom = padBottom;
        return this;
    }

    public Container<T> padRight(Value padRight) {
        if (padRight == null) {
            throw new IllegalArgumentException("padRight cannot be null.");
        }
        this.padRight = padRight;
        return this;
    }

    public Container<T> pad(float pad) {
        Value value = new Fixed(pad);
        this.padTop = value;
        this.padLeft = value;
        this.padBottom = value;
        this.padRight = value;
        return this;
    }

    public Container<T> pad(float top, float left, float bottom, float right) {
        this.padTop = new Fixed(top);
        this.padLeft = new Fixed(left);
        this.padBottom = new Fixed(bottom);
        this.padRight = new Fixed(right);
        return this;
    }

    public Container<T> padTop(float padTop) {
        this.padTop = new Fixed(padTop);
        return this;
    }

    public Container<T> padLeft(float padLeft) {
        this.padLeft = new Fixed(padLeft);
        return this;
    }

    public Container<T> padBottom(float padBottom) {
        this.padBottom = new Fixed(padBottom);
        return this;
    }

    public Container<T> padRight(float padRight) {
        this.padRight = new Fixed(padRight);
        return this;
    }

    public Container<T> fill() {
        this.fillX = 1.0f;
        this.fillY = 1.0f;
        return this;
    }

    public Container<T> fillX() {
        this.fillX = 1.0f;
        return this;
    }

    public Container<T> fillY() {
        this.fillY = 1.0f;
        return this;
    }

    public Container<T> fill(float x, float y) {
        this.fillX = x;
        this.fillY = y;
        return this;
    }

    public Container<T> fill(boolean x, boolean y) {
        float f;
        float f2 = 1.0f;
        if (x) {
            f = 1.0f;
        } else {
            f = 0.0f;
        }
        this.fillX = f;
        if (!y) {
            f2 = 0.0f;
        }
        this.fillY = f2;
        return this;
    }

    public Container<T> fill(boolean fill) {
        float f;
        float f2 = 1.0f;
        if (fill) {
            f = 1.0f;
        } else {
            f = 0.0f;
        }
        this.fillX = f;
        if (!fill) {
            f2 = 0.0f;
        }
        this.fillY = f2;
        return this;
    }

    public Container<T> align(int align) {
        this.align = align;
        return this;
    }

    public Container<T> center() {
        this.align = 1;
        return this;
    }

    public Container<T> top() {
        this.align |= 2;
        this.align &= -5;
        return this;
    }

    public Container<T> left() {
        this.align |= 8;
        this.align &= -17;
        return this;
    }

    public Container<T> bottom() {
        this.align |= 4;
        this.align &= -3;
        return this;
    }

    public Container<T> right() {
        this.align |= 16;
        this.align &= -9;
        return this;
    }

    public float getMinWidth() {
        return (this.minWidth.get(this.actor) + this.padLeft.get(this)) + this.padRight.get(this);
    }

    public Value getMinHeightValue() {
        return this.minHeight;
    }

    public float getMinHeight() {
        return (this.minHeight.get(this.actor) + this.padTop.get(this)) + this.padBottom.get(this);
    }

    public Value getPrefWidthValue() {
        return this.prefWidth;
    }

    public float getPrefWidth() {
        float v = this.prefWidth.get(this.actor);
        if (this.background != null) {
            v = Math.max(v, this.background.getMinWidth());
        }
        return Math.max(getMinWidth(), (this.padLeft.get(this) + v) + this.padRight.get(this));
    }

    public Value getPrefHeightValue() {
        return this.prefHeight;
    }

    public float getPrefHeight() {
        float v = this.prefHeight.get(this.actor);
        if (this.background != null) {
            v = Math.max(v, this.background.getMinHeight());
        }
        return Math.max(getMinHeight(), (this.padTop.get(this) + v) + this.padBottom.get(this));
    }

    public Value getMaxWidthValue() {
        return this.maxWidth;
    }

    public float getMaxWidth() {
        float v = this.maxWidth.get(this.actor);
        if (v > 0.0f) {
            return v + (this.padLeft.get(this) + this.padRight.get(this));
        }
        return v;
    }

    public Value getMaxHeightValue() {
        return this.maxHeight;
    }

    public float getMaxHeight() {
        float v = this.maxHeight.get(this.actor);
        if (v > 0.0f) {
            return v + (this.padTop.get(this) + this.padBottom.get(this));
        }
        return v;
    }

    public Value getPadTopValue() {
        return this.padTop;
    }

    public float getPadTop() {
        return this.padTop.get(this);
    }

    public Value getPadLeftValue() {
        return this.padLeft;
    }

    public float getPadLeft() {
        return this.padLeft.get(this);
    }

    public Value getPadBottomValue() {
        return this.padBottom;
    }

    public float getPadBottom() {
        return this.padBottom.get(this);
    }

    public Value getPadRightValue() {
        return this.padRight;
    }

    public float getPadRight() {
        return this.padRight.get(this);
    }

    public float getPadX() {
        return this.padLeft.get(this) + this.padRight.get(this);
    }

    public float getPadY() {
        return this.padTop.get(this) + this.padBottom.get(this);
    }

    public float getFillX() {
        return this.fillX;
    }

    public float getFillY() {
        return this.fillY;
    }

    public int getAlign() {
        return this.align;
    }

    public void setRound(boolean round) {
        this.round = round;
    }

    public void setClip(boolean enabled) {
        this.clip = enabled;
        setTransform(enabled);
        invalidate();
    }

    public boolean getClip() {
        return this.clip;
    }

    public Actor hit(float x, float y, boolean touchable) {
        if (!this.clip || ((!touchable || getTouchable() != Touchable.disabled) && x >= 0.0f && x < getWidth() && y >= 0.0f && y < getHeight())) {
            return super.hit(x, y, touchable);
        }
        return null;
    }

    public void drawDebug(ShapeRenderer shapes) {
        validate();
        if (isTransform()) {
            applyTransform(shapes, computeTransform());
            if (this.clip) {
                shapes.flush();
                float padLeft = this.padLeft.get(this);
                float padBottom = this.padBottom.get(this);
                if (this.background == null ? clipBegin(0.0f, 0.0f, getWidth(), getHeight()) : clipBegin(padLeft, padBottom, (getWidth() - padLeft) - this.padRight.get(this), (getHeight() - padBottom) - this.padTop.get(this))) {
                    drawDebugChildren(shapes);
                    clipEnd();
                }
            } else {
                drawDebugChildren(shapes);
            }
            resetTransform(shapes);
            return;
        }
        super.drawDebug(shapes);
    }
}
