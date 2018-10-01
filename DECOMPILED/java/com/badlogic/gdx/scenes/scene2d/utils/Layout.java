package com.badlogic.gdx.scenes.scene2d.utils;

public interface Layout {
    float getMaxHeight();

    float getMaxWidth();

    float getMinHeight();

    float getMinWidth();

    float getPrefHeight();

    float getPrefWidth();

    void invalidate();

    void invalidateHierarchy();

    void layout();

    void pack();

    void setFillParent(boolean z);

    void setLayoutEnabled(boolean z);

    void validate();
}
