package com.badlogic.gdx.scenes.scene2d.actions;

public abstract class RelativeTemporalAction extends TemporalAction {
    private float lastPercent;

    protected abstract void updateRelative(float f);

    protected void begin() {
        this.lastPercent = 0.0f;
    }

    protected void update(float percent) {
        updateRelative(percent - this.lastPercent);
        this.lastPercent = percent;
    }
}
