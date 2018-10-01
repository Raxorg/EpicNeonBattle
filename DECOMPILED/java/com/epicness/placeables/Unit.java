package com.epicness.placeables;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.epicness.assets.AssetManager;

public abstract class Unit extends Placeable {
    protected int health;

    protected Unit(TextureRegion regionNormal, TextureRegion regionGlow, int cubeCost, int techCost, int health) {
        super(regionNormal, regionGlow, cubeCost, techCost);
        this.health = health;
    }

    public void bulletCollision(Bullet bullet) {
        AssetManager.sounds.explosion.play();
        this.health--;
        if (this.health == 0) {
            destroy();
        }
        bullet.destroy();
    }
}
