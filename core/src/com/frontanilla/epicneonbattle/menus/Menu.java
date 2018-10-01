package com.frontanilla.epicneonbattle.menus;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import com.frontanilla.epicneonbattle.components.Button;
import com.frontanilla.epicneonbattle.components.MenuButtonListener;

public abstract class Menu extends ScreenAdapter {
    private static BackgroundDevice backgroundDevice;
    protected static float buttonHeight;
    protected static float buttonWidth;
    protected static Game game;
    private static MenuButtonListener menuButtonListener;
    private static float menusAlpha;
    protected static float screenHeight;
    protected static float screenWidth;
    protected static SpriteBatch staticBatch;
    protected Button[] buttons;
    protected boolean created;
    protected Task fadeIn = new Task() {
        public void run() {
            if (((double) Menu.menusAlpha) + 0.01d <= 1.0d) {
                Menu.menusAlpha = (float) (((double) Menu.menusAlpha) + 0.01d);
            }
        }
    };
    protected Task fadeOut = new Task() {
        public void run() {
            if (((double) Menu.menusAlpha) - 0.01d >= 0.0d) {
                Menu.menusAlpha = (float) (((double) Menu.menusAlpha) - 0.01d);
            }
        }
    };
    protected Menu instance;

    private static class BackgroundDevice {
        private Texture background1;
        private Texture background2;
        private float bg1pos;
        private float bg2pos;

        private BackgroundDevice() {
            this.bg2pos = (-Menu.screenHeight) * 2.0f;
        }

        public void create() {
            this.background1 = new Texture("Images/Menus/bg1.png");
            this.background2 = new Texture("Images/Menus/bg2.png");
            Timer.schedule(new Task() {
                public void run() {
                    BackgroundDevice.this.bg1pos = BackgroundDevice.this.bg1pos + 0.5f;
                    if (BackgroundDevice.this.bg1pos >= Menu.screenWidth) {
                        BackgroundDevice.this.bg1pos = ((-Menu.screenHeight) * 2.0f) - ((Menu.screenHeight * 2.0f) - Menu.screenWidth);
                    }
                    BackgroundDevice.this.bg2pos = BackgroundDevice.this.bg2pos + 0.5f;
                    if (BackgroundDevice.this.bg2pos >= Menu.screenWidth) {
                        BackgroundDevice.this.bg2pos = ((-Menu.screenHeight) * 2.0f) - ((Menu.screenHeight * 2.0f) - Menu.screenWidth);
                    }
                }
            }, 0.0f, 0.01f);
        }

        public void render() {
            Menu.staticBatch.begin();
            Menu.staticBatch.setColor(1.0f, 1.0f, 1.0f, 0.5f);
            Menu.staticBatch.draw(this.background1, this.bg1pos, 0.0f, Menu.screenHeight * 2.0f, Menu.screenHeight);
            Menu.staticBatch.draw(this.background2, this.bg2pos, 0.0f, Menu.screenHeight * 2.0f, Menu.screenHeight);
            Menu.staticBatch.end();
        }
    }

    protected abstract void appear();

    protected abstract void makeButtons();

    protected abstract void step();

    protected void scheduleTransition(final Menu menu) {
        Gdx.input.setInputProcessor(null);
        Timer.schedule(this.fadeOut, 0.0f, 0.003f, 100);
        Timer.schedule(new Task() {
            public void run() {
                Menu.game.setScreen(menu);
                Gdx.input.setInputProcessor(Menu.menuButtonListener);
            }
        }, 0.35f);
    }

    public static void init(Game g) {
        game = g;
        staticBatch = new SpriteBatch();
        menuButtonListener = new MenuButtonListener();
        Gdx.input.setInputProcessor(menuButtonListener);
        screenWidth = (float) Gdx.graphics.getWidth();
        screenHeight = (float) Gdx.graphics.getHeight();
        buttonHeight = screenHeight / 6.0f;
        buttonWidth = buttonHeight * 3.0f;
        backgroundDevice = new BackgroundDevice();
        backgroundDevice.create();
    }

    public final void show() {
        if (!this.created) {
            makeButtons();
            this.created = true;
        }
        menuButtonListener.setButtons(this.buttons);
        this.instance = this;
        appear();
        Timer.schedule(this.fadeIn, 0.0f, 0.003f, 100);
    }

    public final void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        backgroundDevice.render();
        staticBatch.begin();
        for (Button b : this.buttons) {
            staticBatch.setColor(1.0f, 1.0f, 1.0f, menusAlpha);
            staticBatch.draw(b.getNormalRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
            staticBatch.setColor(b.getColor().r, b.getColor().g, b.getColor().b, menusAlpha);
            staticBatch.draw(b.getGlowingRegion(), b.getX(), b.getY(), b.getWidth(), b.getHeight());
        }
        staticBatch.end();
        staticBatch.setColor(1.0f, 1.0f, 1.0f, menusAlpha);
        step();
    }

    protected float getMenusAlpha() {
        return menusAlpha;
    }

    protected MenuButtonListener getMenuButtonListener() {
        return menuButtonListener;
    }
}
