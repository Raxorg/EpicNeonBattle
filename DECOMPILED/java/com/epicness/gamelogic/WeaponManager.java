package com.epicness.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.epicness.assets.AssetManager;
import com.epicness.map.Map.Cell;
import com.epicness.placeables.Bullet;
import com.epicness.placeables.Structure;
import com.epicness.placeables.Unit;
import java.util.ArrayList;
import java.util.Iterator;

public class WeaponManager {
    private ArrayList<Bullet> bullets = new ArrayList();

    public void addBullet(Bullet bullet) {
        Gdx.input.setInputProcessor(null);
        this.bullets.add(bullet);
    }

    public void removeBullet(Bullet bullet) {
        this.bullets.remove(bullet);
        if (this.bullets.size() == 0) {
            GameMaster.getInstance().getTurnObserver().passTurn();
            GameMaster.getInstance().activateInput(0.0f);
        }
    }

    public void render(SpriteBatch batch, float alpha) {
        batch.begin();
        Iterator it = this.bullets.iterator();
        while (it.hasNext()) {
            Bullet b = (Bullet) it.next();
            batch.setColor(1.0f, 1.0f, 1.0f, alpha);
            batch.draw(AssetManager.game.bulletNormal, b.getXPos(), b.getYPos(), GameMaster.getInstance().getMap().getCellSize() / 2.0f, GameMaster.getInstance().getMap().getCellSize() / 2.0f, GameMaster.getInstance().getMap().getCellSize(), GameMaster.getInstance().getMap().getCellSize(), 1.0f, 1.0f, b.getAngle());
            batch.setColor(GameMaster.getInstance().getColorWithAlpha(b.getOwner().getColor()));
            batch.draw(AssetManager.game.bulletGlow, b.getXPos(), b.getYPos(), GameMaster.getInstance().getMap().getCellSize() / 2.0f, GameMaster.getInstance().getMap().getCellSize() / 2.0f, GameMaster.getInstance().getMap().getCellSize(), GameMaster.getInstance().getMap().getCellSize(), 1.0f, 1.0f, b.getAngle());
        }
        batch.end();
        checkForCollision();
        moveBullets();
    }

    private void checkForCollision() {
        Iterator it = this.bullets.iterator();
        while (it.hasNext()) {
            Bullet b = (Bullet) it.next();
            for (Player p : GameMaster.getInstance().getPlayers()) {
                if (p.getTeam() != b.getTeam()) {
                    Iterator it2 = p.getStructures().iterator();
                    while (it2.hasNext()) {
                        Structure s = (Structure) it2.next();
                        if (b.getXPos() <= s.getXPos() + 9.0f && b.getXPos() + 9.0f >= s.getXPos() && b.getYPos() <= s.getYPos() + 9.0f && b.getYPos() + 9.0f >= s.getYPos()) {
                            s.bulletCollision(b);
                            return;
                        }
                    }
                    it2 = p.getUnits().iterator();
                    while (it2.hasNext()) {
                        Unit u = (Unit) it2.next();
                        if (b.getXPos() <= u.getXPos() + 9.0f && b.getXPos() + 9.0f >= u.getXPos() && b.getYPos() <= u.getYPos() + 9.0f && b.getYPos() + 9.0f >= u.getYPos()) {
                            u.bulletCollision(b);
                            return;
                        }
                    }
                    continue;
                }
            }
            Iterator it3 = GameMaster.getInstance().getMap().getBlockedCells().iterator();
            while (it3.hasNext()) {
                Cell c = (Cell) it3.next();
                if (b.getXPos() <= ((float) ((c.getColumn() * 20) + 9)) && b.getXPos() + 9.0f >= ((float) (c.getColumn() * 20)) && b.getYPos() <= ((float) ((c.getRow() * 20) + 9)) && b.getYPos() + 9.0f >= ((float) (c.getRow() * 20))) {
                    b.destroy();
                    AssetManager.sounds.explosion.play();
                    return;
                }
            }
        }
    }

    private void moveBullets() {
        Iterator it = this.bullets.iterator();
        while (it.hasNext()) {
            Bullet b = (Bullet) it.next();
            b.setXPos((float) (((double) b.getXPos()) + Math.cos(Math.toRadians((double) (b.getAngle() + 90.0f)))));
            b.setYPos((float) (((double) b.getYPos()) + Math.sin(Math.toRadians((double) (b.getAngle() + 90.0f)))));
            if (b.getXPos() >= ((float) (GameMaster.getInstance().getMap().getColumns() * 20)) || b.getYPos() >= ((float) (GameMaster.getInstance().getMap().getRows() * 20)) || b.getXPos() <= -20.0f || b.getYPos() <= -20.0f) {
                b.destroy();
                return;
            }
            if (Math.abs(b.getXPos() - b.getInitXPos()) < 65.0f) {
                if (Math.abs(b.getYPos() - b.getInitYPos()) >= 65.0f) {
                }
            }
            b.destroy();
            return;
        }
    }
}
