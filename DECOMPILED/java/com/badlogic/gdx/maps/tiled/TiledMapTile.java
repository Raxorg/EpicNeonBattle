package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;

public interface TiledMapTile {

    public enum BlendMode {
        NONE,
        ALPHA
    }

    BlendMode getBlendMode();

    int getId();

    float getOffsetX();

    float getOffsetY();

    MapProperties getProperties();

    TextureRegion getTextureRegion();

    void setBlendMode(BlendMode blendMode);

    void setId(int i);

    void setOffsetX(float f);

    void setOffsetY(float f);

    void setTextureRegion(TextureRegion textureRegion);
}
