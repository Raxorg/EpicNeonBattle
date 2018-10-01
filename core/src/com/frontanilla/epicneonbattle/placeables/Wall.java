package com.frontanilla.epicneonbattle.placeables;

import com.frontanilla.epicneonbattle.assets.AssetManager;

public class Wall extends Structure {
    public Wall() {
        super(AssetManager.game.wallNormal3, AssetManager.game.wallGlow3, 2, 0, 3);
    }

    public void bulletCollision(Bullet bullet) {
        super.bulletCollision(bullet);
        if (this.health == 2) {
            this.regionNormal = AssetManager.game.wallNormal2;
            this.regionGlow = AssetManager.game.wallGlow2;
        }
        if (this.health == 1) {
            this.regionNormal = AssetManager.game.wallNormal1;
            this.regionGlow = AssetManager.game.wallGlow1;
        }
    }
}
