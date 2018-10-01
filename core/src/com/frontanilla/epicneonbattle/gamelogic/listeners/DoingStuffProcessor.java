package com.frontanilla.epicneonbattle.gamelogic.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.frontanilla.epicneonbattle.gamelogic.ActionObserver;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;
import com.frontanilla.epicneonbattle.map.Map;
import com.frontanilla.epicneonbattle.placeables.Tank;

public class DoingStuffProcessor extends InputAdapter {
    private Map.Cell currentCell;
    private boolean draggingStuff;
    private int lastX;
    private int lastY;
    private float xCenter;
    private float yCenter;

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (!GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.OBSERVING)) {
            Map.Cell touchedCell = calculateCellTouched(screenX, screenY);
            if (touchedCell != null) {
                this.currentCell = touchedCell;
            }
        }
        if (this.currentCell != null && !this.currentCell.isEmpty() && (this.currentCell.getContent() instanceof Tank) && this.currentCell.getContent().getOwner() == GameMaster.getInstance().getCurrentPlayer()) {
            this.draggingStuff = true;
            this.xCenter = (float) (((this.currentCell.getColumn() * Gdx.graphics.getHeight()) / 6) + (Gdx.graphics.getHeight() / 12));
            this.yCenter = (float) (((this.currentCell.getRow() * Gdx.graphics.getHeight()) / 6) + (Gdx.graphics.getHeight() / 12));
            this.lastX = screenX;
            this.lastY = screenY;
        }
        return true;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.BUILDING) && calculateCellTouched(screenX, screenY) == this.currentCell && this.currentCell != null) {
            GameMaster.getInstance().getActionObserver().attemptToBuy(this.currentCell);
        }
        this.currentCell = null;
        this.draggingStuff = false;
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (this.draggingStuff) {
            screenX = (int) (((float) screenX) + (((float) (Gdx.graphics.getHeight() / ((Gdx.graphics.getWidth() / Gdx.graphics.getHeight()) * 120))) * (GameMaster.getInstance().getMapViewer().getCamera().position.x - (GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f))));
            screenY = (int) (((float) screenY) + (((float) (Gdx.graphics.getHeight() / 120)) * (GameMaster.getInstance().getMapViewer().getCamera().position.y - (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f))));
            this.currentCell.getContent().setAngle(new Vector2(((float) this.lastX) - this.xCenter, ((float) this.lastY) - this.yCenter).angle() - 90.0f);
            this.lastX = screenX;
            this.lastY = screenY;
        }
        return false;
    }

    private Map.Cell calculateCellTouched(int screenX, int screenY) {
        int r = screenY;
        int c = screenX;
        if (r > Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)) {
            return null;
        }
        r = ((int) (((float) r) + (((float) (Gdx.graphics.getHeight() / 120)) * (GameMaster.getInstance().getMapViewer().getCamera().position.y - (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f))))) / 120;
        c = ((int) (((float) c) + (((float) (Gdx.graphics.getHeight() / ((Gdx.graphics.getWidth() / Gdx.graphics.getHeight()) * 120))) * (GameMaster.getInstance().getMapViewer().getCamera().position.x - (GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f))))) / 120;
        if (r >= GameMaster.getInstance().getMap().getRows() || c >= GameMaster.getInstance().getMap().getColumns()) {
            return null;
        }
        return GameMaster.getInstance().getMap().getCell(r, c);
    }
}
