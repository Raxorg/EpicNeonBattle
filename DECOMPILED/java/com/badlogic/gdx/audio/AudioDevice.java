package com.badlogic.gdx.audio;

import com.badlogic.gdx.utils.Disposable;

public interface AudioDevice extends Disposable {
    void dispose();

    int getLatency();

    boolean isMono();

    void setVolume(float f);

    void writeSamples(float[] fArr, int i, int i2);

    void writeSamples(short[] sArr, int i, int i2);
}
