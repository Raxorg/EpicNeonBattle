package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.SnapshotArray;

public class Stack extends WidgetGroup {
    private float maxHeight;
    private float maxWidth;
    private float minHeight;
    private float minWidth;
    private float prefHeight;
    private float prefWidth;
    private boolean sizeInvalid = true;

    public Stack() {
        setTransform(false);
        setWidth(150.0f);
        setHeight(150.0f);
        setTouchable(Touchable.childrenOnly);
    }

    public void invalidate() {
        super.invalidate();
        this.sizeInvalid = true;
    }

    private void computeSize() {
        this.sizeInvalid = false;
        this.prefWidth = 0.0f;
        this.prefHeight = 0.0f;
        this.minWidth = 0.0f;
        this.minHeight = 0.0f;
        this.maxWidth = 0.0f;
        this.maxHeight = 0.0f;
        SnapshotArray<Actor> children = getChildren();
        int n = children.size;
        for (int i = 0; i < n; i++) {
            float childMaxWidth;
            float childMaxHeight;
            Actor child = (Actor) children.get(i);
            if (child instanceof Layout) {
                Layout layout = (Layout) child;
                this.prefWidth = Math.max(this.prefWidth, layout.getPrefWidth());
                this.prefHeight = Math.max(this.prefHeight, layout.getPrefHeight());
                this.minWidth = Math.max(this.minWidth, layout.getMinWidth());
                this.minHeight = Math.max(this.minHeight, layout.getMinHeight());
                childMaxWidth = layout.getMaxWidth();
                childMaxHeight = layout.getMaxHeight();
            } else {
                this.prefWidth = Math.max(this.prefWidth, child.getWidth());
                this.prefHeight = Math.max(this.prefHeight, child.getHeight());
                this.minWidth = Math.max(this.minWidth, child.getWidth());
                this.minHeight = Math.max(this.minHeight, child.getHeight());
                childMaxWidth = 0.0f;
                childMaxHeight = 0.0f;
            }
            if (childMaxWidth > 0.0f) {
                if (this.maxWidth != 0.0f) {
                    childMaxWidth = Math.min(this.maxWidth, childMaxWidth);
                }
                this.maxWidth = childMaxWidth;
            }
            if (childMaxHeight > 0.0f) {
                if (this.maxHeight != 0.0f) {
                    childMaxHeight = Math.min(this.maxHeight, childMaxHeight);
                }
                this.maxHeight = childMaxHeight;
            }
        }
    }

    public void add(Actor actor) {
        addActor(actor);
    }

    public void layout() {
        if (this.sizeInvalid) {
            computeSize();
        }
        float width = getWidth();
        float height = getHeight();
        Array<Actor> children = getChildren();
        int n = children.size;
        for (int i = 0; i < n; i++) {
            Actor child = (Actor) children.get(i);
            child.setBounds(0.0f, 0.0f, width, height);
            if (child instanceof Layout) {
                ((Layout) child).validate();
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

    public float getMinWidth() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.minWidth;
    }

    public float getMinHeight() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.minHeight;
    }

    public float getMaxWidth() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.maxWidth;
    }

    public float getMaxHeight() {
        if (this.sizeInvalid) {
            computeSize();
        }
        return this.maxHeight;
    }
}
