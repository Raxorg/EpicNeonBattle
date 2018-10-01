package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.FloatCounter;

public class PerformanceCounter {
    private static final float nano2seconds = 1.0E-9f;
    public float current;
    private long lastTick;
    public final FloatCounter load;
    public final String name;
    private long startTime;
    public final FloatCounter time;
    public boolean valid;

    public PerformanceCounter(String name) {
        this(name, 5);
    }

    public PerformanceCounter(String name, int windowSize) {
        this.startTime = 0;
        this.lastTick = 0;
        this.current = 0.0f;
        this.valid = false;
        this.name = name;
        this.time = new FloatCounter(windowSize);
        this.load = new FloatCounter(1);
    }

    public void tick() {
        long t = TimeUtils.nanoTime();
        if (this.lastTick > 0) {
            tick(((float) (t - this.lastTick)) * nano2seconds);
        }
        this.lastTick = t;
    }

    public void tick(float delta) {
        if (this.valid) {
            this.time.put(this.current);
            float currentLoad = delta == 0.0f ? 0.0f : this.current / delta;
            FloatCounter floatCounter = this.load;
            if (delta <= 1.0f) {
                currentLoad = (delta * currentLoad) + ((1.0f - delta) * this.load.latest);
            }
            floatCounter.put(currentLoad);
            this.current = 0.0f;
            this.valid = false;
            return;
        }
        Gdx.app.error("PerformanceCounter", "Invalid data, check if you called PerformanceCounter#stop()");
    }

    public void start() {
        this.startTime = TimeUtils.nanoTime();
        this.valid = false;
    }

    public void stop() {
        if (this.startTime > 0) {
            this.current += ((float) (TimeUtils.nanoTime() - this.startTime)) * nano2seconds;
            this.startTime = 0;
            this.valid = true;
        }
    }

    public void reset() {
        this.time.reset();
        this.load.reset();
        this.startTime = 0;
        this.lastTick = 0;
        this.current = 0.0f;
        this.valid = false;
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder sb) {
        sb.append(this.name).append(": [time: ").append(this.time.value).append(", load: ").append(this.load.value).append("]");
        return sb;
    }
}
