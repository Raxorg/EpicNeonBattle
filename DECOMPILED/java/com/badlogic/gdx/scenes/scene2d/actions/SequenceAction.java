package com.badlogic.gdx.scenes.scene2d.actions;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.utils.Pool;

public class SequenceAction extends ParallelAction {
    private int index;

    public SequenceAction(Action action1) {
        addAction(action1);
    }

    public SequenceAction(Action action1, Action action2) {
        addAction(action1);
        addAction(action2);
    }

    public SequenceAction(Action action1, Action action2, Action action3) {
        addAction(action1);
        addAction(action2);
        addAction(action3);
    }

    public SequenceAction(Action action1, Action action2, Action action3, Action action4) {
        addAction(action1);
        addAction(action2);
        addAction(action3);
        addAction(action4);
    }

    public SequenceAction(Action action1, Action action2, Action action3, Action action4, Action action5) {
        addAction(action1);
        addAction(action2);
        addAction(action3);
        addAction(action4);
        addAction(action5);
    }

    public boolean act(float delta) {
        if (this.index >= this.actions.size) {
            return true;
        }
        Pool pool = getPool();
        setPool(null);
        try {
            if (((Action) this.actions.get(this.index)).act(delta)) {
                if (this.actor == null) {
                    return true;
                }
                this.index++;
                if (this.index >= this.actions.size) {
                    setPool(pool);
                    return true;
                }
            }
            setPool(pool);
            return false;
        } finally {
            setPool(pool);
        }
    }

    public void restart() {
        super.restart();
        this.index = 0;
    }
}
