package com.badlogic.gdx;

public interface ApplicationListener {
    void create();

    void dispose();

    void pause();

    void render();

    void resize(int i, int i2);

    void resume();
}
