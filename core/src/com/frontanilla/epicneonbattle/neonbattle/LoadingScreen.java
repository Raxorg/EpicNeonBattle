package com.frontanilla.epicneonbattle.neonbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;
import com.frontanilla.epicneonbattle.menus.MapSelection;
import com.frontanilla.epicneonbattle.menus.Menu;
import com.frontanilla.epicneonbattle.menus.PlayerSelection;

public class LoadingScreen extends ScreenAdapter {
    private float alpha;
    private float angle;
    private Task changeToGameScreen = new Task() {
        public void run() {
            GameMaster.getInstance().showGameScreen();
        }
    };
    private Task fadeIn = new Task() {
        public void run() {
            if (((double) LoadingScreen.this.alpha) + 0.01d <= 1.0d) {
                LoadingScreen.this.alpha = (float) (((double) LoadingScreen.this.alpha) + 0.01d);
            }
        }
    };
    private Task fadeOut = new Task() {
        public void run() {
            if (((double) LoadingScreen.this.alpha) - 0.01d >= 0.0d) {
                LoadingScreen.this.alpha = (float) (((double) LoadingScreen.this.alpha) - 0.01d);
            }
        }
    };
    private Game game;
    private Task initConfiguration = new Task() {
        public void run() {
            GameMaster.getInstance().init(MapSelection.getCurrentMap(), PlayerSelection.getPlayers(), LoadingScreen.this.game, LoadingScreen.this.previous);
            Timer.schedule(LoadingScreen.this.fadeOut, 0.0f, 0.01f, 100);
            Timer.schedule(LoadingScreen.this.changeToGameScreen, 1.0f);
        }
    };
    private SpriteBatch loadingBatch;
    private Menu previous;

    public LoadingScreen(Menu previous, Game game) {
        this.previous = previous;
        this.game = game;
        this.loadingBatch = new SpriteBatch();
    }

    public void show() {
        Timer.schedule(this.fadeIn, 0.0f, 0.01f, 100);
        Timer.schedule(this.initConfiguration, 1.0f);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.loadingBatch.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        this.loadingBatch.begin();
        this.loadingBatch.draw(AssetManager.menus.loadingCircleRegion, (float) ((Gdx.graphics.getWidth() / 2) - (Gdx.graphics.getHeight() / 4)), (float) ((Gdx.graphics.getHeight() / 2) - (Gdx.graphics.getHeight() / 4)), (float) ((Gdx.graphics.getHeight() / 2) / 2), (float) ((Gdx.graphics.getHeight() / 2) / 2), (float) (Gdx.graphics.getHeight() / 2), (float) (Gdx.graphics.getHeight() / 2), 1.0f, 1.0f, this.angle);
        this.loadingBatch.end();
        this.angle += 50.0f * delta;
    }
}
