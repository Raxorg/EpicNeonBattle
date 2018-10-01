package com.frontanilla.epicneonbattle.gamelogic.listeners;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.frontanilla.epicneonbattle.gamelogic.ActionObserver;
import com.frontanilla.epicneonbattle.gamelogic.GameMaster;

public class ObservingMapProcessor extends InputAdapter {
    private static boolean canDown;
    private static boolean canLeft;
    private static boolean canRight;
    private static boolean canUp;
    private static boolean draggingMap;
    private static int lastX;
    private static int lastY;

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        draggingMap = false;
        return true;
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (screenY >= Gdx.graphics.getHeight() - (Gdx.graphics.getHeight() / 8)) {
            return false;
        }
        if (!GameMaster.getInstance().getActionObserver().getAction().equals(ActionObserver.Action.OBSERVING)) {
            return true;
        }
        lastX = screenX;
        lastY = screenY;
        draggingMap = true;
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (draggingMap) {
            if ((lastY - screenY > 0 && canUp) || (lastY - screenY < 0 && canDown)) {
                GameMaster.getInstance().getMapViewer().getCamera().translate(0.0f, (float) ((lastY - screenY) / 5), 0.0f);
            }
            if ((lastX - screenX > 0 && canRight) || (lastX - screenX < 0 && canLeft)) {
                GameMaster.getInstance().getMapViewer().getCamera().translate((float) ((lastX - screenX) / 5), 0.0f, 0.0f);
            }
            lastX = screenX;
            lastY = screenY;
            if (GameMaster.getInstance().getMapViewer().getCamera().position.x - (GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f) > 0.0f) {
                canLeft = true;
            } else {
                GameMaster.getInstance().getMapViewer().getCamera().position.set(GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f, GameMaster.getInstance().getMapViewer().getCamera().position.y, GameMaster.getInstance().getMapViewer().getCamera().position.z);
                canLeft = false;
            }
            if (GameMaster.getInstance().getMapViewer().getCamera().position.x + (GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f) < GameMaster.getInstance().getMap().getWidth()) {
                canRight = true;
            } else {
                GameMaster.getInstance().getMapViewer().getCamera().position.set(GameMaster.getInstance().getMap().getWidth() - (GameMaster.getInstance().getMapViewer().getCamera().viewportWidth / 2.0f), GameMaster.getInstance().getMapViewer().getCamera().position.y, GameMaster.getInstance().getMapViewer().getCamera().position.z);
                canRight = false;
            }
            if (GameMaster.getInstance().getMapViewer().getCamera().position.y - (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f) > 0.0f) {
                canDown = true;
            } else {
                GameMaster.getInstance().getMapViewer().getCamera().position.set(GameMaster.getInstance().getMapViewer().getCamera().position.x, GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f, GameMaster.getInstance().getMapViewer().getCamera().position.z);
                canDown = false;
            }
            if (GameMaster.getInstance().getMapViewer().getCamera().position.y + (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f) < GameMaster.getInstance().getMap().getHeight() + (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 8.0f)) {
                canUp = true;
            } else {
                GameMaster.getInstance().getMapViewer().getCamera().position.set(GameMaster.getInstance().getMapViewer().getCamera().position.x, (GameMaster.getInstance().getMap().getHeight() - (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 2.0f)) + (GameMaster.getInstance().getMapViewer().getCamera().viewportHeight / 8.0f), GameMaster.getInstance().getMapViewer().getCamera().position.z);
                canUp = false;
            }
        }
        return true;
    }
}
