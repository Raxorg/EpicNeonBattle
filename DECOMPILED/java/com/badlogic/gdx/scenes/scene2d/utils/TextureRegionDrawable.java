package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TextureRegionDrawable extends BaseDrawable implements TransformDrawable {
    private TextureRegion region;

    public TextureRegionDrawable(TextureRegion region) {
        setRegion(region);
    }

    public TextureRegionDrawable(TextureRegionDrawable drawable) {
        super(drawable);
        setRegion(drawable.region);
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        batch.draw(this.region, x, y, width, height);
    }

    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        batch.draw(this.region, x, y, originX, originY, width, height, scaleX, scaleY, rotation);
    }

    public void setRegion(TextureRegion region) {
        this.region = region;
        setMinWidth((float) region.getRegionWidth());
        setMinHeight((float) region.getRegionHeight());
    }

    public TextureRegion getRegion() {
        return this.region;
    }

    public SpriteDrawable tint(Color tint) {
        Sprite sprite;
        if (this.region instanceof AtlasRegion) {
            sprite = new AtlasSprite((AtlasRegion) this.region);
        } else {
            sprite = new Sprite(this.region);
        }
        sprite.setColor(tint);
        return new SpriteDrawable(sprite);
    }
}
