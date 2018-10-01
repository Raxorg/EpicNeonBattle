package com.frontanilla.epicneonbattle.placeables;

import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;

public class Tank extends Unit {
    public Tank() {
        super(AssetManager.game.tankNormal, AssetManager.game.tankGlow, 3, 0, 1);
    }

    public void fireBullet() {
        GameMaster.getInstance().getWeaponManager().addBullet(new Bullet(this));
        AssetManager.sounds.fireBullet.play();
    }
}
