package com.epicness.neonbattle;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.epicness.assets.AssetManager;
import com.epicness.map.MapData;
import com.epicness.menus.Main;
import com.epicness.menus.Menu;

public class SplashScreen extends ScreenAdapter {
    private float alpha;
    private SpriteBatch batch;
    private Task fadeIn = new Task() {
        public void run() {
            if (((double) SplashScreen.this.alpha) + 0.01d <= 1.0d) {
                SplashScreen.this.alpha = (float) (((double) SplashScreen.this.alpha) + 0.01d);
            }
        }
    };
    private Task fadeOut = new Task() {
        public void run() {
            if (((double) SplashScreen.this.alpha) - 0.01d >= 0.0d) {
                SplashScreen.this.alpha = (float) (((double) SplashScreen.this.alpha) - 0.01d);
            }
        }
    };
    private Game game;
    private Task loadMenus = new Task() {
        public void run() {
            AssetManager.getInstance().loadFonts();
            AssetManager.getInstance().loadMenus();
            AssetManager.getInstance().loadSounds();
            MapData.init();
            Timer.schedule(SplashScreen.this.fadeOut, 0.0f, 0.01f, 100);
            Timer.schedule(new Task() {
                public void run() {
                    Menu.init(SplashScreen.this.game);
                    SplashScreen.this.game.setScreen(new Main());
                }
            }, 1.2f);
        }
    };
    private Texture splash;

    public SplashScreen(Game game) {
        Gdx.input.setCatchBackKey(true);
        this.game = game;
    }

    public void show() {
        this.splash = new Texture("Images/splash.png");
        this.batch = new SpriteBatch();
        Timer.schedule(this.loadMenus, 1.0f);
        Timer.schedule(this.fadeIn, 0.0f, 0.01f, 100);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.batch.setColor(1.0f, 1.0f, 1.0f, this.alpha);
        this.batch.begin();
        this.batch.draw(this.splash, 0.0f, 0.0f, (float) Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight());
        this.batch.end();
    }

    public void hide() {
        this.splash.dispose();
    }
}
