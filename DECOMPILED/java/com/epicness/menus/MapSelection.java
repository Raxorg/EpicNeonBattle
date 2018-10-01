package com.epicness.menus;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.epicness.assets.AssetManager;
import com.epicness.components.Button;
import com.epicness.map.Map;
import com.epicness.map.MapData;

public class MapSelection extends Menu {
    private static Map currentMap;
    private int mapIndex;
    private Map[] maps = new Map[2];
    private Menu multiplayer;

    public MapSelection(Menu previous) {
        this.multiplayer = previous;
        this.maps[0] = new Map(20.0f, MapData.defaultMap);
        this.maps[1] = new Map(20.0f, MapData.squareMap);
        currentMap = this.maps[0];
    }

    public static Map getCurrentMap() {
        if (currentMap == null) {
            currentMap = new Map(20.0f, MapData.defaultMap);
        }
        return currentMap;
    }

    public static void resetMap() {
        currentMap = new Map(20.0f, currentMap.getMapData());
    }

    protected void makeButtons() {
        this.buttons = new Button[3];
        this.buttons[0] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, screenHeight - buttonHeight, buttonHeight, buttonHeight, Color.ORANGE) {
            public void onTouchUp() {
                MapSelection.this.scheduleTransition(MapSelection.this.multiplayer);
            }
        };
        this.buttons[1] = new Button(AssetManager.menus.backNormal, AssetManager.menus.backGlow, 0.0f, (screenHeight / 2.0f) - (buttonHeight / 2.0f), buttonHeight, buttonHeight, Color.CYAN) {
            public void onTouchUp() {
                MapSelection.this.mapIndex = MapSelection.this.mapIndex - 1;
                if (MapSelection.this.mapIndex < 0) {
                    MapSelection.this.mapIndex = MapSelection.this.maps.length - 1;
                }
                MapSelection.currentMap = MapSelection.this.maps[MapSelection.this.mapIndex];
                PlayerSelection.defaultPlayers();
            }
        };
        this.buttons[2] = new Button(AssetManager.menus.nextNormal, AssetManager.menus.nextGlow, screenWidth - buttonHeight, (screenHeight / 2.0f) - (buttonHeight / 2.0f), buttonHeight, buttonHeight, Color.CYAN) {
            public void onTouchUp() {
                MapSelection.this.mapIndex = MapSelection.this.mapIndex + 1;
                if (MapSelection.this.mapIndex > MapSelection.this.maps.length - 1) {
                    MapSelection.this.mapIndex = 0;
                }
                MapSelection.currentMap = MapSelection.this.maps[MapSelection.this.mapIndex];
                PlayerSelection.defaultPlayers();
            }
        };
    }

    protected void appear() {
        AssetManager.fonts.normal.getData().setScale(((float) (Gdx.graphics.getHeight() / 10)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 10)) / 512.0f);
        AssetManager.fonts.glow.getData().setScale(((float) (Gdx.graphics.getHeight() / 10)) / 512.0f, ((float) (Gdx.graphics.getHeight() / 10)) / 512.0f);
        AssetManager.fonts.normal.setColor(1.0f, 1.0f, 1.0f, getMenusAlpha());
        AssetManager.fonts.glow.setColor(0.0f, 0.0f, 1.0f, getMenusAlpha());
    }

    protected void step() {
        staticBatch.begin();
        staticBatch.setColor(1.0f, 1.0f, 1.0f, getMenusAlpha());
        staticBatch.draw(currentMap.getRegionNormal(), ((float) (Gdx.graphics.getWidth() / 2)) - (((float) Gdx.graphics.getHeight()) * 0.4f), ((float) (Gdx.graphics.getHeight() / 2)) - (((float) Gdx.graphics.getHeight()) * 0.4f), ((float) Gdx.graphics.getHeight()) * 0.8f, ((float) Gdx.graphics.getHeight()) * 0.8f);
        staticBatch.setColor(0.0f, 0.0f, 1.0f, getMenusAlpha());
        staticBatch.draw(currentMap.getRegionGlow(), ((float) (Gdx.graphics.getWidth() / 2)) - (((float) Gdx.graphics.getHeight()) * 0.4f), ((float) (Gdx.graphics.getHeight() / 2)) - (((float) Gdx.graphics.getHeight()) * 0.4f), ((float) Gdx.graphics.getHeight()) * 0.8f, ((float) Gdx.graphics.getHeight()) * 0.8f);
        AssetManager.fonts.normal.draw(staticBatch, currentMap.toString(), (float) ((Gdx.graphics.getWidth() / 2) - ((Gdx.graphics.getHeight() / 20) * currentMap.toString().length())), (float) Gdx.graphics.getHeight());
        AssetManager.fonts.glow.draw(staticBatch, currentMap.toString(), (float) ((Gdx.graphics.getWidth() / 2) - ((Gdx.graphics.getHeight() / 20) * currentMap.toString().length())), (float) Gdx.graphics.getHeight());
        staticBatch.end();
    }
}
