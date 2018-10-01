package com.badlogic.gdx.audio;

import com.badlogic.gdx.utils.Disposable;

public interface Sound extends Disposable {
    void dispose();

    long loop();

    long loop(float f);

    long loop(float f, float f2, float f3);

    void pause();

    void pause(long j);

    long play();

    long play(float f);

    long play(float f, float f2, float f3);

    void resume();

    void resume(long j);

    void setLooping(long j, boolean z);

    void setPan(long j, float f, float f2);

    void setPitch(long j, float f);

    void setPriority(long j, int i);

    void setVolume(long j, float f);

    void stop();

    void stop(long j);
}
