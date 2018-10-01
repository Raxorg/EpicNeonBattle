package com.badlogic.gdx.graphics;

public interface CubemapData {
    void consumeCubemapData();

    int getHeight();

    int getWidth();

    boolean isManaged();

    boolean isPrepared();

    void prepare();
}
