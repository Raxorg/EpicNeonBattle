package com.epicness.menus;

import com.badlogic.gdx.graphics.Color;
import com.epicness.assets.AssetManager;
import com.epicness.components.Button;

public class ModeSelection extends Menu {
    private Menu empty;
    private Menu main;
    private Menu multiplayer;

    public ModeSelection(Menu previous) {
        this.main = previous;
    }

    protected void makeButtons() {
        this.buttons = new Button[4];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                ModeSelection.this.scheduleTransition(ModeSelection.this.main);
            }
        };
        this.buttons[1] = new Button(AssetManager.menus.missionsNormal, AssetManager.menus.missionsGlow, (screenWidth / 2.0f) - (buttonHeight * 1.5f), (buttonHeight / 2.0f) + (screenHeight / 2.0f), buttonWidth, buttonHeight, Color.BLUE) {
            public void onTouchUp() {
                if (ModeSelection.this.empty == null) {
                    ModeSelection.this.empty = new Empty(ModeSelection.this.instance);
                }
                ModeSelection.this.scheduleTransition(ModeSelection.this.empty);
            }
        };
        this.buttons[2] = new Button(AssetManager.menus.tutorialNormal, AssetManager.menus.tutorialGlow, (screenWidth / 2.0f) - (buttonHeight * 1.5f), (screenHeight / 2.0f) - (buttonHeight / 2.0f), buttonWidth, buttonHeight, Color.PURPLE) {
            public void onTouchUp() {
                if (ModeSelection.this.empty == null) {
                    ModeSelection.this.empty = new Empty(ModeSelection.this.instance);
                }
                ModeSelection.this.scheduleTransition(ModeSelection.this.empty);
            }
        };
        this.buttons[3] = new Button(AssetManager.menus.multiplayerNormal, AssetManager.menus.multiplayerGlow, (screenWidth / 2.0f) - (buttonHeight * 1.5f), (screenHeight / 2.0f) - (buttonHeight * 1.5f), buttonWidth, buttonHeight, Color.RED) {
            public void onTouchUp() {
                if (ModeSelection.this.multiplayer == null) {
                    ModeSelection.this.multiplayer = new Multiplayer(ModeSelection.this.instance);
                }
                ModeSelection.this.scheduleTransition(ModeSelection.this.multiplayer);
            }
        };
    }

    protected void appear() {
    }

    protected void step() {
    }
}
