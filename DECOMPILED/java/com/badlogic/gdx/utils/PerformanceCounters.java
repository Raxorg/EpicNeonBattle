package com.badlogic.gdx.utils;

public class PerformanceCounters {
    private static final float nano2seconds = 1.0E-9f;
    public final Array<PerformanceCounter> counters = new Array();
    private long lastTick = 0;

    public PerformanceCounter add(String name, int windowSize) {
        PerformanceCounter result = new PerformanceCounter(name, windowSize);
        this.counters.add(result);
        return result;
    }

    public PerformanceCounter add(String name) {
        PerformanceCounter result = new PerformanceCounter(name);
        this.counters.add(result);
        return result;
    }

    public void tick() {
        long t = TimeUtils.nanoTime();
        if (this.lastTick > 0) {
            tick(((float) (t - this.lastTick)) * nano2seconds);
        }
        this.lastTick = t;
    }

    public void tick(float deltaTime) {
        for (int i = 0; i < this.counters.size; i++) {
            ((PerformanceCounter) this.counters.get(i)).tick(deltaTime);
        }
    }

    public StringBuilder toString(StringBuilder sb) {
        sb.setLength(0);
        for (int i = 0; i < this.counters.size; i++) {
            if (i != 0) {
                sb.append("; ");
            }
            ((PerformanceCounter) this.counters.get(i)).toString(sb);
        }
        return sb;
    }
}
