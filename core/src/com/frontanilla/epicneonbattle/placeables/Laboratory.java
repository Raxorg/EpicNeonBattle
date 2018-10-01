package com.frontanilla.epicneonbattle.placeables;

import com.frontanilla.epicneonbattle.assets.AssetManager;

public class Laboratory extends Structure {
    public Laboratory() {
        super(AssetManager.game.laboratoryNormal2, AssetManager.game.laboratoryGlow2, 5, 3, 2);
    }

    public void bulletCollision(Bullet bullet) {
        super.bulletCollision(bullet);
        if (this.health == 1) {
            this.regionNormal = AssetManager.game.laboratoryNormal1;
            this.regionGlow = AssetManager.game.laboratoryGlow1;
        }
    }
}
