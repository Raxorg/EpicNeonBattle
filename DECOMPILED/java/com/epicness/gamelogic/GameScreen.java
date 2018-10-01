package com.epicness.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.epicness.assets.AssetManager;

public class GameScreen extends ScreenAdapter {
    private SpriteBatch dynamicBatch;
    private SpriteBatch staticBatch;

    public void load() {
        this.dynamicBatch = new SpriteBatch();
        this.staticBatch = new SpriteBatch();
    }

    public void show() {
        AssetManager.fonts.normal.getData().setScale(((float) (Gdx.graphics.getHeight() / 8)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 8)) / 512.0f);
        AssetManager.fonts.glow.getData().setScale(((float) (Gdx.graphics.getHeight() / 8)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 8)) / 512.0f);
        GameMaster.getInstance().fadeIn();
        GameMaster.getInstance().activateInput(0.75f);
    }

    public void render(float delta) {
        Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        GameMaster.getInstance().getMapViewer().render(this.dynamicBatch, GameMaster.getInstance().getAlpha());
        GameMaster.getInstance().getWeaponManager().render(this.dynamicBatch, GameMaster.getInstance().getAlpha());
        GameMaster.getInstance().getGameHUDBar().render(this.staticBatch, GameMaster.getInstance().getAlpha());
    }
}
