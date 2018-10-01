package com.epicness.gamelogic.listeners;

import com.badlogic.gdx.InputAdapter;
import com.epicness.assets.AssetManager;
import com.epicness.components.Button;
import com.epicness.gamelogic.GameMaster;

public class GameBarListener extends InputAdapter {
    private Button currentButton;

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        for (Button b : GameMaster.getInstance().getGameHUDBar().getGameHUDButtons()) {
            if (b.isWithin(screenX, screenY)) {
                this.currentButton = b;
            }
        }
        return true;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        for (Button b : GameMaster.getInstance().getGameHUDBar().getGameHUDButtons()) {
            if (b.isWithin(screenX, screenY) && b == this.currentButton) {
                AssetManager.sounds.buttonTouchUp.play();
                b.onTouchUp();
            }
        }
        this.currentButton = null;
        return true;
    }
}
