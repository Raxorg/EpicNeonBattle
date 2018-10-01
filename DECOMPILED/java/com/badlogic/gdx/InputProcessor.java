package com.badlogic.gdx;

public interface InputProcessor {
    boolean keyDown(int i);

    boolean keyTyped(char c);

    boolean keyUp(int i);

    boolean mouseMoved(int i, int i2);

    boolean scrolled(int i);

    boolean touchDown(int i, int i2, int i3, int i4);

    boolean touchDragged(int i, int i2, int i3);

    boolean touchUp(int i, int i2, int i3, int i4);
}
