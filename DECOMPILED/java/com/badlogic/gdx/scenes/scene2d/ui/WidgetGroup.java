package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

public class WidgetGroup extends Group implements Layout {
    private boolean fillParent;
    private boolean layoutEnabled = true;
    private boolean needsLayout = true;

    public WidgetGroup(Actor... actors) {
        for (Actor actor : actors) {
            addActor(actor);
        }
    }

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
        if (this.layoutEnabled != enabled) {
            this.layoutEnabled = enabled;
            setLayoutEnabled(this, enabled);
        }
    }

    private void setLayoutEnabled(Group parent, boolean enabled) {
        SnapshotArray<Actor> children = parent.getChildren();
        int n = children.size;
        for (int i = 0; i < n; i++) {
            Actor actor = (Actor) children.get(i);
            if (actor instanceof Layout) {
                ((Layout) actor).setLayoutEnabled(enabled);
            } else if (actor instanceof Group) {
                setLayoutEnabled((Group) actor, enabled);
            }
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
                if (!(getWidth() == parentWidth && getHeight() == parentHeight)) {
                    setWidth(parentWidth);
                    setHeight(parentHeight);
                    invalidate();
                }
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
        invalidate();
        Group parent = getParent();
        if (parent instanceof Layout) {
            ((Layout) parent).invalidateHierarchy();
        }
    }

    protected void childrenChanged() {
        invalidateHierarchy();
    }

    protected void sizeChanged() {
        invalidate();
    }

    public void pack() {
        setSize(getPrefWidth(), getPrefHeight());
        validate();
        if (this.needsLayout) {
            setSize(getPrefWidth(), getPrefHeight());
            validate();
        }
    }

    public void setFillParent(boolean fillParent) {
        this.fillParent = fillParent;
    }

    public void layout() {
    }

    public void draw(Batch batch, float parentAlpha) {
        validate();
        super.draw(batch, parentAlpha);
    }
}
