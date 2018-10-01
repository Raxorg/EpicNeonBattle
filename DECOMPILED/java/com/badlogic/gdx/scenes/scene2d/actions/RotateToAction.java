package com.badlogic.gdx.scenes.scene2d.actions;

public class RotateToAction extends TemporalAction {
    private float end;
    private float start;

    protected void begin() {
        this.start = this.target.getRotation();
    }

    protected void update(float percent) {
        this.target.setRotation(this.start + ((this.end - this.start) * percent));
    }

    public float getRotation() {
        return this.end;
    }

    public void setRotation(float rotation) {
        this.end = rotation;
    }
}
