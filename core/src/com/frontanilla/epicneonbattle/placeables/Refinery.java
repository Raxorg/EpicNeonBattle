package com.frontanilla.epicneonbattle.placeables;

import com.frontanilla.epicneonbattle.assets.AssetManager;

public class Refinery extends Structure {
    public Refinery() {
        super(AssetManager.game.refineryNormal2, AssetManager.game.refineryGlow2, 5, 3, 2);
    }

    public void bulletCollision(Bullet bullet) {
        super.bulletCollision(bullet);
        if (this.health == 1) {
            this.regionNormal = AssetManager.game.refineryNormal1;
            this.regionGlow = AssetManager.game.refineryGlow1;
        }
    }
}
