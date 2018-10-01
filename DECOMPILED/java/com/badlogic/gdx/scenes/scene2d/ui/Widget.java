package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;

public class Widget extends Actor implements Layout {
    private boolean fillParent;
    private boolean layoutEnabled = true;
    private boolean needsLayout = true;

    public float getMinWidth() {
        return getPrefWidth();
    }

    public float getMinHeight() {
        return getPrefHeight();
    }

    public float getPrefWidth() {
        return 0.0f;
    }

    public float getPrefHeight() {
        return 0.0f;
    }

    public float getMaxWidth() {
        return 0.0f;
    }

    public float getMaxHeight() {
        return 0.0f;
    }

    public void setLayoutEnabled(boolean enabled) {
        this.layoutEnabled = enabled;
        if (enabled) {
            invalidateHierarchy();
        }
    }

    public void validate() {
        if (this.layoutEnabled) {
            Group parent = getParent();
            if (this.fillParent && parent != null) {
                float parentWidth;
                float parentHeight;
                Stage stage = getStage();
                if (stage == null || parent != stage.getRoot()) {
                    parentWidth = parent.getWidth();
                    parentHeight = parent.getHeight();
                } else {
                    parentWidth = stage.getWidth();
                    parentHeight = stage.getHeight();
                }
                setSize(parentWidth, parentHeight);
            }
            if (this.needsLayout) {
                this.needsLayout = false;
                layout();
            }
        }
    }

    public boolean needsLayout() {
        return this.needsLayout;
    }

    public void invalidate() {
        this.needsLayout = true;
    }

    public void invalidateHierarchy() {
        if (this.layoutEnabled) {
            invalidate();
            Group parent = getParent();
            if (parent instanceof Layout) {
                ((Layout) parent).invalidateHierarchy();
            }
        }
    }

    protected void sizeChanged() {
        invalidate();
    }

    public void pack() {
        setSize(getPrefWidth(), getPrefHeight());
        validate();
    }

    public void setFillParent(boolean fillParent) {
        this.fillParent = fillParent;
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
    }

    public void layout() {
    }
}
