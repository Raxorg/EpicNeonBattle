package com.frontanilla.epicneonbattle.gamelogic.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;

public class BuyMenuListener extends InputAdapter {
    private Button currentButton;

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        for (Button b : GameMaster.getInstance().getGameHUDBar().getBuyMenuButtons()) {
            if (b.isWithin(screenX, screenY)) {
                this.currentButton = b;
            }
        }
        return true;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        screenY = Gdx.graphics.getHeight() - screenY;
        for (Button b : GameMaster.getInstance().getGameHUDBar().getBuyMenuButtons()) {
            if (b.isWithin(screenX, screenY) && b == this.currentButton) {
                AssetManager.sounds.buttonTouchUp.play();
                b.onTouchUp();
            }
        }
        this.currentButton = null;
        return true;
    }
}
