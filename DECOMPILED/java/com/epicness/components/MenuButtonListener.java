package com.epicness.components;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.epicness.assets.AssetManager;

public class MenuButtonListener extends InputAdapter {
    private Button[] buttons;

    public void setButtons(Button[] buttons) {
        this.buttons = buttons;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        int i = 0;
        if (pointer != 0) {
            return false;
        }
        screenY = Gdx.graphics.getHeight() - screenY;
        Button[] buttonArr = this.buttons;
        int length = buttonArr.length;
        while (i < length) {
            Button b = buttonArr[i];
            if (b.isWithin(screenX, screenY)) {
                AssetManager.sounds.buttonTouchUp.play();
                b.onTouchUp();
            }
            i++;
        }
        return true;
    }
}
