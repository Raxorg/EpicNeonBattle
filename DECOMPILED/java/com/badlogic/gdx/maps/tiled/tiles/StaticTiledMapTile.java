package com.badlogic.gdx.maps.tiled.tiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTile.BlendMode;

public class StaticTiledMapTile implements TiledMapTile {
    private BlendMode blendMode = BlendMode.ALPHA;
    private int id;
    private float offsetX;
    private float offsetY;
    private MapProperties properties;
    private TextureRegion textureRegion;

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BlendMode getBlendMode() {
        return this.blendMode;
    }

    public void setBlendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
    }

    public MapProperties getProperties() {
        if (this.properties == null) {
            this.properties = new MapProperties();
        }
        return this.properties;
    }

    public TextureRegion getTextureRegion() {
        return this.textureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public float getOffsetX() {
        return this.offsetX;
    }

    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    public float getOffsetY() {
        return this.offsetY;
    }

    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    public StaticTiledMapTile(TextureRegion textureRegion) {
        this.textureRegion = textureRegion;
    }

    public StaticTiledMapTile(StaticTiledMapTile copy) {
        if (copy.properties != null) {
            getProperties().putAll(copy.properties);
        }
        this.textureRegion = copy.textureRegion;
        this.id = copy.id;
    }
}
