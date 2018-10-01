package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;

public class AddAction extends Action {
    private Action action;

    public boolean act(float delta) {
        this.target.addAction(this.action);
        return true;
    }

    public Action getAction() {
        return this.action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void restart() {
        if (this.action != null) {
            this.action.restart();
        }
    }

    public void reset() {
        super.reset();
        this.action = null;
    }
}
