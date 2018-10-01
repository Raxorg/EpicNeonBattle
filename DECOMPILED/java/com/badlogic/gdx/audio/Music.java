package com.badlogic.gdx.audio;

import com.badlogic.gdx.utils.Disposable;

public interface Music extends Disposable {

    public interface OnCompletionListener {
        void onCompletion(Music music);
    }

    void dispose();

    float getPosition();

    float getVolume();

    boolean isLooping();

    boolean isPlaying();

    void pause();

    void play();

    void setLooping(boolean z);

    void setOnCompletionListener(OnCompletionListener onCompletionListener);

    void setPan(float f, float f2);

    void setPosition(float f);

    void setVolume(float f);

    void stop();
}
