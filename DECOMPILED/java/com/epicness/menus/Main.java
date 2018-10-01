package com.epicness.menus;

import com.badlogic.gdx.graphics.Color;
import com.epicness.assets.AssetManager;
import com.epicness.components.Button;

public class Main extends Menu {
    private Menu empty;
    private Menu modeSelection;

    public void makeButtons() {
        this.buttons = new Button[4];
        this.buttons[0] = new Button(AssetManager.menus.playNormal, AssetManager.menus.playGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), buttonHeight + (screenHeight / 2.0f), buttonWidth, buttonHeight, Color.RED) {
            public void onTouchUp() {
                if (Main.this.modeSelection == null) {
                    Main.this.modeSelection = new ModeSelection(Main.this.instance);
                }
                Main.this.scheduleTransition(Main.this.modeSelection);
            }
        };
        this.buttons[1] = new Button(AssetManager.menus.optionsNormal, AssetManager.menus.optionsGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), screenHeight / 2.0f, buttonWidth, buttonHeight, Color.YELLOW) {
            public void onTouchUp() {
                if (Main.this.empty == null) {
                    Main.this.empty = new Empty(Main.this.instance);
                }
                Main.this.scheduleTransition(Main.this.empty);
            }
        };
        this.buttons[2] = new Button(AssetManager.menus.scoresNormal, AssetManager.menus.scoresGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), (screenHeight / 2.0f) - buttonHeight, buttonWidth, buttonHeight, Color.GREEN) {
            public void onTouchUp() {
                if (Main.this.empty == null) {
                    Main.this.empty = new Empty(Main.this.instance);
                }
                Main.this.scheduleTransition(Main.this.empty);
            }
        };
        this.buttons[3] = new Button(AssetManager.menus.creditsNormal, AssetManager.menus.creditsGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), (screenHeight / 2.0f) - (buttonHeight * 2.0f), buttonWidth, buttonHeight, Color.BLUE) {
            public void onTouchUp() {
                if (Main.this.empty == null) {
                    Main.this.empty = new Empty(Main.this.instance);
                }
                Main.this.scheduleTransition(Main.this.empty);
            }
        };
    }

    public void appear() {
        if (!AssetManager.sounds.background.isPlaying()) {
            AssetManager.sounds.background.setLooping(true);
            AssetManager.sounds.background.play();
        }
    }

    public void step() {
    }

    public void hide() {
    }
}
