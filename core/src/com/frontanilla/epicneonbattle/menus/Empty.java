package com.frontanilla.epicneonbattle.menus;

import com.badlogic.gdx.graphics.Color;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.components.Button;

public class Empty extends Menu {
    private Menu previous;

    public Empty(Menu previous) {
        this.previous = previous;
    }

    protected void makeButtons() {
        this.buttons = new Button[1];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                Empty.this.scheduleTransition(Empty.this.previous);
            }
        };
    }

    protected void appear() {
    }

    protected void step() {
    }
}
