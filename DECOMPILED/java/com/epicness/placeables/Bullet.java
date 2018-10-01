package com.epicness.placeables;

import com.badlogic.gdx.utils.Timer;
import com.epicness.assets.AssetManager;
import com.epicness.gamelogic.GameMaster;
import com.epicness.gamelogic.Team;

public class Bullet extends Special {
    private float initXPos = this.xPos;
    private float initYPos = this.yPos;
    private Timer timer;
    private float xPos;
    private float yPos;

    public Bullet(Tank tank) {
        super(AssetManager.game.bulletNormal, AssetManager.game.bulletGlow, 0, 0);
        this.xPos = ((float) tank.cell.getColumn()) * GameMaster.getInstance().getMap().getCellSize();
        this.yPos = ((float) tank.cell.getRow()) * GameMaster.getInstance().getMap().getCellSize();
        this.angle = tank.angle;
        this.owner = tank.owner;
        this.timer = new Timer();
    }

    public float getInitYPos() {
        return this.initYPos;
    }

    public float getInitXPos() {
        return this.initXPos;
    }

    public float getXPos() {
        return this.xPos;
    }

    public float getYPos() {
        return this.yPos;
    }

    public void setXPos(float xPos) {
        this.xPos = xPos;
    }

    public void setYPos(float yPos) {
        this.yPos = yPos;
    }

    public Team getTeam() {
        return this.owner.getTeam();
    }

    public void destroy() {
        this.timer.clear();
        GameMaster.getInstance().getWeaponManager().removeBullet(this);
    }
}
