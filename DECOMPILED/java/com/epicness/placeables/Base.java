package com.epicness.placeables;

import com.epicness.assets.AssetManager;

public class Base extends Structure {
    public Base() {
        super(AssetManager.game.baseNormal4, AssetManager.game.baseGlow4, 0, 0, 4);
    }

    public void bulletCollision(Bullet bullet) {
        super.bulletCollision(bullet);
        if (this.health == 3) {
            this.regionNormal = AssetManager.game.baseNormal3;
            this.regionGlow = AssetManager.game.baseGlow3;
        }
        if (this.health == 2) {
            this.regionNormal = AssetManager.game.baseNormal2;
            this.regionGlow = AssetManager.game.baseGlow2;
        }
        if (this.health == 1) {
            this.regionNormal = AssetManager.game.baseNormal1;
            this.regionGlow = AssetManager.game.baseGlow1;
        }
    }
}
