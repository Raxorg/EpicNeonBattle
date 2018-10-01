package com.frontanilla.epicneonbattle.map;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.frontanilla.epicneonbattle.assets.AssetManager;

public class MapData {
    public static MapData defaultMap;
    public static MapData squareMap;
    private int[][] data;
    private TextureRegion mapGlow;
    private TextureRegion mapNormal;
    private int maxPlayers;
    private String name;

    public MapData(String name, int[][] data, int maxPlayers, TextureRegion regionNormal, TextureRegion regionGlow) {
        this.data = data;
        this.mapNormal = regionNormal;
        this.mapGlow = regionGlow;
        this.name = name;
        this.maxPlayers = maxPlayers;
    }

    public static void init() {
        defaultMap = new MapData("DEFAULT", new int[][]{new int[]{0, 0, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0}, new int[]{0, 3, 0, 1, 0, 0, 0, 0, 2, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 0, 2, 0, 0, 0, 0, 1, 0, 3, 0}, new int[]{0, 0, 0, 2, 0, 0, 0, 0, 1, 0, 0, 0}}, 2, AssetManager.menus.defaultNormal, AssetManager.menus.defaultGlow);
        squareMap = new MapData("SQUARE", new int[][]{new int[]{0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0}, new int[]{0, 3, 0, 2, 0, 0, 0, 0, 2, 0, 3, 0}, new int[]{0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0}, new int[]{2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new int[]{0, 0, 1, 0, 0, 2, 2, 0, 0, 1, 0, 0}, new int[]{0, 0, 1, 0, 0, 2, 2, 0, 0, 1, 0, 0}, new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, new int[]{2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2}, new int[]{0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0}, new int[]{0, 3, 0, 2, 0, 0, 0, 0, 2, 0, 3, 0}, new int[]{0, 0, 0, 2, 0, 0, 0, 0, 2, 0, 0, 0}}, 4, AssetManager.menus.squareNormal, AssetManager.menus.squareGlow);
    }

    public int getMaxPlayers() {
        return this.maxPlayers;
    }

    public String getName() {
        return this.name;
    }

    public int[][] getData() {
        return this.data;
    }

    public TextureRegion getRegionNormal() {
        return this.mapNormal;
    }

    public TextureRegion getRegionGlow() {
        return this.mapGlow;
    }

    public int getNumRows() {
        return this.data.length;
    }

    public int getNumColumns() {
        return this.data[0].length;
    }
}
