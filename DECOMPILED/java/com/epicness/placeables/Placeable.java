package com.epicness.placeables;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.epicness.gamelogic.GameMaster;
import com.epicness.gamelogic.Player;
import com.epicness.map.Map.Cell;

public abstract class Placeable {
    protected float angle;
    protected Cell cell;
    protected int cubeCost;
    protected Player owner;
    protected TextureRegion regionGlow;
    protected TextureRegion regionNormal;
    protected int techCost;

    protected Placeable(TextureRegion regionNormal, TextureRegion regionGlow, int cubeCost, int techCost) {
        this.regionNormal = regionNormal;
        this.regionGlow = regionGlow;
        this.cubeCost = cubeCost;
        this.techCost = techCost;
    }

    public TextureRegion getRegionNormal() {
        return this.regionNormal;
    }

    public TextureRegion getRegionGlow() {
        return this.regionGlow;
    }

    public float getAngle() {
        return this.angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public int getCubeCost() {
        return this.cubeCost;
    }

    public int getTechCost() {
        return this.techCost;
    }

    public boolean place(Cell cell) {
        return cell.setContent(this);
    }

    protected void destroy() {
        this.owner.removePlaceable(this);
    }

    public Cell getCell() {
        return this.cell;
    }

    public void setCell(Cell cell) {
        this.cell = cell;
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setOwner(Player player) {
        this.owner = player;
    }

    public float getXPos() {
        return ((float) this.cell.getColumn()) * GameMaster.getInstance().getMap().getCellSize();
    }

    public float getYPos() {
        return ((float) this.cell.getRow()) * GameMaster.getInstance().getMap().getCellSize();
    }
}
