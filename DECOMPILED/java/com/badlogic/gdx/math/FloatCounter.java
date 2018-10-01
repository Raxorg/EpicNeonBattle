package com.badlogic.gdx.math;

public class FloatCounter {
    public float average;
    public int count;
    public float latest;
    public float max;
    public final WindowedMean mean;
    public float min;
    public float total;
    public float value;

    public FloatCounter(int windowSize) {
        this.mean = windowSize > 1 ? new WindowedMean(windowSize) : null;
        reset();
    }

    public void put(float value) {
        this.latest = value;
        this.total += value;
        this.count++;
        this.average = this.total / ((float) this.count);
        if (this.mean != null) {
            this.mean.addValue(value);
            this.value = this.mean.getMean();
        } else {
            this.value = this.latest;
        }
        if (this.mean == null || this.mean.hasEnoughData()) {
            if (this.value < this.min) {
                this.min = this.value;
            }
            if (this.value > this.max) {
                this.max = this.value;
            }
        }
    }

    public void reset() {
        this.count = 0;
        this.total = 0.0f;
        this.min = Float.MAX_VALUE;
        this.max = Float.MIN_VALUE;
        this.average = 0.0f;
        this.latest = 0.0f;
        this.value = 0.0f;
        if (this.mean != null) {
            this.mean.clear();
        }
    }
}
