package com.frontanilla.epicneonbattle.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.gamelogic.Player;

public class EndOfBattle extends Menu {
    private Menu previous;
    private Player winner;

    public EndOfBattle(Menu previous) {
        this.previous = previous;
    }

    public void setWinner(Player player) {
        this.winner = player;
    }

    protected void makeButtons() {
        this.buttons = new Button[1];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                EndOfBattle.this.scheduleTransition(EndOfBattle.this.previous);
            }
        };
    }

    protected void appear() {
        Gdx.input.setInputProcessor(getMenuButtonListener());
    }

    protected void step() {
        staticBatch.begin();
        Color color = this.winner.getColor();
        staticBatch.setColor(color.r, color.g, color.b, getMenusAlpha());
        staticBatch.draw(AssetManager.menus.victory, 0.0f, 0.0f, (float) Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight());
        staticBatch.end();
    }
}
