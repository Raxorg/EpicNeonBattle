package com.frontanilla.epicneonbattle.gamelogic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.frontanilla.epicneonbattle.gamelogic.listeners.DoingStuffProcessor;
import com.frontanilla.epicneonbattle.gamelogic.listeners.GameBarListener;
import com.frontanilla.epicneonbattle.gamelogic.listeners.ObservingMapProcessor;

public class GameInputListener extends InputAdapter {
    private DoingStuffProcessor doingStuffProcessor;
    private GameBarListener gameBarListener;
    private ObservingMapProcessor observingMapProcessor;

    public void init() {
        this.gameBarListener = new GameBarListener();
        this.doingStuffProcessor = new DoingStuffProcessor();
        this.observingMapProcessor = new ObservingMapProcessor();
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer == 0) {
            screenY = Gdx.graphics.getHeight() - screenY;
            this.gameBarListener.touchDown(screenX, screenY, pointer, button);
            this.doingStuffProcessor.touchDown(screenX, screenY, pointer, button);
            this.observingMapProcessor.touchDown(screenX, screenY, pointer, button);
        }
        return false;
    }

    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer != 0) {
            return false;
        }
        screenY = Gdx.graphics.getHeight() - screenY;
        this.gameBarListener.touchUp(screenX, screenY, pointer, button);
        this.doingStuffProcessor.touchUp(screenX, screenY, pointer, button);
        this.observingMapProcessor.touchUp(screenX, screenY, pointer, button);
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer == 0) {
            screenY = Gdx.graphics.getHeight() - screenY;
            this.observingMapProcessor.touchDragged(screenX, screenY, pointer);
            this.doingStuffProcessor.touchDragged(screenX, screenY, pointer);
        }
        return false;
    }
}
