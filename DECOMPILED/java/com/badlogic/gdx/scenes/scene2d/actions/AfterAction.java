package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class AfterAction extends DelegateAction {
    private Array<Action> waitForActions = new Array(false, 4);

    public void setTarget(Actor target) {
        if (target != null) {
            this.waitForActions.addAll(target.getActions());
        }
        super.setTarget(target);
    }

    public void restart() {
        super.restart();
        this.waitForActions.clear();
    }

    protected boolean delegate(float delta) {
        Array<Action> currentActions = this.target.getActions();
        if (currentActions.size == 1) {
            this.waitForActions.clear();
        }
        for (int i = this.waitForActions.size - 1; i >= 0; i--) {
            if (currentActions.indexOf((Action) this.waitForActions.get(i), true) == -1) {
                this.waitForActions.removeIndex(i);
            }
        }
        if (this.waitForActions.size > 0) {
            return false;
        }
        return this.action.act(delta);
    }
}
