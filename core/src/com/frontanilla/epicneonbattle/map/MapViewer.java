package com.frontanilla.epicneonbattle.map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.frontanilla.epicneonbattle.assets.AssetManager;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;
import com.frontanilla.epicneonbattle.gamelogic.Player;
import com.frontanilla.epicneonbattle.placeables.Structure;
import com.frontanilla.epicneonbattle.placeables.Unit;

import java.util.Iterator;

public class MapViewer {
    private static OrthographicCamera camera;
    private Map map;

    public MapViewer(Map map) {
        this.map = map;
        camera = new OrthographicCamera((((float) Gdx.graphics.getWidth()) / ((float) Gdx.graphics.getHeight())) * 120.0f, 120.0f);
        camera.position.set(camera.viewportWidth / 2.0f, camera.viewportHeight / 2.0f, 0.0f);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    public void render(SpriteBatch batch, float alpha) {
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawTheCells(batch, alpha);
        drawTheTerritories(batch);
        drawThePlaceables(batch, alpha);
        batch.end();
    }

    private void drawTheCells(SpriteBatch batch, float alpha) {
        for (int r = 0; r < this.map.getRows(); r++) {
            for (int c = 0; c < this.map.getColumns(); c++) {
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                Map.Cell cell = this.map.getCell(r, c);
                if (cell.getRegionNormal() != null) {
                    batch.draw(cell.getRegionNormal(), this.map.getCellSize() * ((float) cell.getColumn()), this.map.getCellSize() * ((float) cell.getRow()), this.map.getCellSize(), this.map.getCellSize());
                    batch.draw(cell.getRegionGlow(), this.map.getCellSize() * ((float) cell.getColumn()), this.map.getCellSize() * ((float) cell.getRow()), this.map.getCellSize(), this.map.getCellSize());
                }
            }
        }
    }

    private void drawTheTerritories(SpriteBatch batch) {
        for (Player p : GameMaster.getInstance().getPlayers()) {
            if (p.isAlive()) {
                Iterator it = p.getTerritory().iterator();
                while (it.hasNext()) {
                    Map.Cell c = (Map.Cell) it.next();
                    batch.setColor(GameMaster.getInstance().getColorWithAlpha(p.getColor()));
                    batch.draw(AssetManager.game.transparent, this.map.getCellSize() * ((float) c.getColumn()), this.map.getCellSize() * ((float) c.getRow()), this.map.getCellSize(), this.map.getCellSize());
                }
            }
        }
    }

    private void drawThePlaceables(SpriteBatch batch, float alpha) {
        for (Player p : GameMaster.getInstance().getPlayers()) {
            Iterator it = p.getUnits().iterator();
            while (it.hasNext()) {
                Unit u = (Unit) it.next();
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(u.getRegionNormal(), this.map.getCellSize() * ((float) u.getCell().getColumn()), this.map.getCellSize() * ((float) u.getCell().getRow()), this.map.getCellSize() / 2.0f, this.map.getCellSize() / 2.0f, this.map.getCellSize(), this.map.getCellSize(), 1.0f, 1.0f, u.getAngle());
                batch.setColor(GameMaster.getInstance().getColorWithAlpha(p.getColor()));
                batch.draw(u.getRegionGlow(), this.map.getCellSize() * ((float) u.getCell().getColumn()), this.map.getCellSize() * ((float) u.getCell().getRow()), this.map.getCellSize() / 2.0f, this.map.getCellSize() / 2.0f, this.map.getCellSize(), this.map.getCellSize(), 1.0f, 1.0f, u.getAngle());
            }
            it = p.getStructures().iterator();
            while (it.hasNext()) {
                Structure s = (Structure) it.next();
                batch.setColor(1.0f, 1.0f, 1.0f, alpha);
                batch.draw(s.getRegionNormal(), this.map.getCellSize() * ((float) s.getCell().getColumn()), this.map.getCellSize() * ((float) s.getCell().getRow()), this.map.getCellSize() / 2.0f, this.map.getCellSize() / 2.0f, this.map.getCellSize(), this.map.getCellSize(), 1.0f, 1.0f, s.getAngle());
                batch.setColor(GameMaster.getInstance().getColorWithAlpha(p.getColor()));
                batch.draw(s.getRegionGlow(), this.map.getCellSize() * ((float) s.getCell().getColumn()), this.map.getCellSize() * ((float) s.getCell().getRow()), this.map.getCellSize() / 2.0f, this.map.getCellSize() / 2.0f, this.map.getCellSize(), this.map.getCellSize(), 1.0f, 1.0f, s.getAngle());
            }
        }
    }
}
