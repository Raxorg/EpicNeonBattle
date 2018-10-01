package com.badlogic.gdx;

public interface Screen {
    void dispose();

    void hide();

    void pause();

    void render(float f);

    void resize(int i, int i2);

    void resume();

    void show();
}
