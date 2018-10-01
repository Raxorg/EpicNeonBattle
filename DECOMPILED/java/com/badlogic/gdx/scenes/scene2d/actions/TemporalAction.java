package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.Pool;

public abstract class TemporalAction extends Action {
    private boolean began;
    private boolean complete;
    private float duration;
    private Interpolation interpolation;
    private boolean reverse;
    private float time;

    protected abstract void update(float f);

    public TemporalAction(float duration) {
        this.duration = duration;
    }

    public TemporalAction(float duration, Interpolation interpolation) {
        this.duration = duration;
        this.interpolation = interpolation;
    }

    public boolean act(float delta) {
        boolean z = true;
        if (!this.complete) {
            Pool pool = getPool();
            setPool(null);
            try {
                float percent;
                if (!this.began) {
                    begin();
                    this.began = true;
                }
                this.time += delta;
                if (this.time < this.duration) {
                    z = false;
                }
                this.complete = z;
                if (this.complete) {
                    percent = 1.0f;
                } else {
                    percent = this.time / this.duration;
                    if (this.interpolation != null) {
                        percent = this.interpolation.apply(percent);
                    }
                }
                if (this.reverse) {
                    percent = 1.0f - percent;
                }
                update(percent);
                if (this.complete) {
                    end();
                }
                z = this.complete;
            } finally {
                setPool(pool);
            }
        }
        return z;
    }

    protected void begin() {
    }

    protected void end() {
    }

    public void finish() {
        this.time = this.duration;
    }

    public void restart() {
        this.time = 0.0f;
        this.began = false;
        this.complete = false;
    }

    public void reset() {
        super.reset();
        this.reverse = false;
        this.interpolation = null;
    }

    public float getTime() {
        return this.time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public float getDuration() {
        return this.duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public Interpolation getInterpolation() {
        return this.interpolation;
    }

    public void setInterpolation(Interpolation interpolation) {
        this.interpolation = interpolation;
    }

    public boolean isReverse() {
        return this.reverse;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }
}
