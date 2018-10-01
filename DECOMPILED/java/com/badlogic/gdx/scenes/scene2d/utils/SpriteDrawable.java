package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasSprite;

public class SpriteDrawable extends BaseDrawable implements TransformDrawable {
    private static Color tmpColor = new Color();
    private Sprite sprite;

    public SpriteDrawable(Sprite sprite) {
        setSprite(sprite);
    }

    public SpriteDrawable(SpriteDrawable drawable) {
        super(drawable);
        setSprite(drawable.sprite);
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        draw(batch, x, y, width / 2.0f, height / 2.0f, width, height, 1.0f, 1.0f, 0.0f);
    }

    public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        this.sprite.setOrigin(originX, originY);
        this.sprite.setRotation(rotation);
        this.sprite.setScale(scaleX, scaleY);
        this.sprite.setBounds(x, y, width, height);
        Color color = this.sprite.getColor();
        this.sprite.setColor(tmpColor.set(color).mul(batch.getColor()));
        this.sprite.draw(batch);
        this.sprite.setColor(color);
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        setMinWidth(sprite.getWidth());
        setMinHeight(sprite.getHeight());
    }

    public Sprite getSprite() {
        return this.sprite;
    }

    public SpriteDrawable tint(Color tint) {
        SpriteDrawable drawable = new SpriteDrawable(this);
        Sprite sprite = drawable.getSprite();
        if (sprite instanceof AtlasSprite) {
            sprite = new AtlasSprite((AtlasSprite) sprite);
        } else {
            sprite = new Sprite(sprite);
        }
        sprite.setColor(tint);
        drawable.setSprite(sprite);
        return drawable;
    }
}
