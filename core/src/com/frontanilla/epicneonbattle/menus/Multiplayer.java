package com.frontanilla.epicneonbattle.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.neonbattle.LoadingScreen;

public class Multiplayer extends Menu {
    private Screen loadingScreen;
    private Menu mapSelection;
    private Menu modeSelection;
    private Menu playerSelection;

    public Multiplayer(Menu previous) {
        this.modeSelection = previous;
    }

    protected void makeButtons() {
        this.buttons = new Button[4];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                Multiplayer.this.scheduleTransition(Multiplayer.this.modeSelection);
            }
        };
        this.buttons[1] = new Button(AssetManager.menus.mapNormal, AssetManager.menus.mapGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), screenHeight - buttonHeight, buttonWidth, buttonHeight, Color.GREEN) {
            public void onTouchUp() {
                if (Multiplayer.this.mapSelection == null) {
                    Multiplayer.this.mapSelection = new MapSelection(Multiplayer.this.instance);
                }
                Multiplayer.this.scheduleTransition(Multiplayer.this.mapSelection);
            }
        };
        this.buttons[2] = new Button(AssetManager.menus.playersNormal, AssetManager.menus.playersGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), screenHeight - (3.0f * buttonHeight), buttonWidth, buttonHeight, Color.CYAN) {
            public void onTouchUp() {
                if (Multiplayer.this.playerSelection == null) {
                    Multiplayer.this.playerSelection = new PlayerSelection(Multiplayer.this.instance);
                }
                Multiplayer.this.scheduleTransition(Multiplayer.this.playerSelection);
            }
        };
        this.buttons[3] = new Button(AssetManager.menus.beginNormal, AssetManager.menus.beginGlow, (screenWidth / 2.0f) - (buttonWidth / 2.0f), 0.0f, buttonWidth, buttonHeight, Color.RED) {
            public void onTouchUp() {
                Gdx.input.setInputProcessor(null);
                if (Multiplayer.this.loadingScreen == null) {
                    Multiplayer.this.loadingScreen = new LoadingScreen(Multiplayer.this.instance, Menu.game);
                }
                Timer.schedule(Multiplayer.this.fadeOut, 0.0f, 0.005f, 100);
                Timer.schedule(new Task() {
                    public void run() {
                        Menu.game.setScreen(Multiplayer.this.loadingScreen);
                    }
                }, 0.5f);
            }
        };
    }

    protected void appear() {
        Gdx.input.setInputProcessor(getMenuButtonListener());
    }

    protected void step() {
    }
}
