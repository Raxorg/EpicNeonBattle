package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;

public abstract class Action implements Poolable {
    protected Actor actor;
    private Pool pool;
    protected Actor target;

    public abstract boolean act(float f);

    public void restart() {
    }

    public void setActor(Actor actor) {
        this.actor = actor;
        if (this.target == null) {
            setTarget(actor);
        }
        if (actor == null && this.pool != null) {
            this.pool.free(this);
            this.pool = null;
        }
    }

    public Actor getActor() {
        return this.actor;
    }

    public void setTarget(Actor target) {
        this.target = target;
    }

    public Actor getTarget() {
        return this.target;
    }

    public void reset() {
        this.actor = null;
        this.target = null;
        this.pool = null;
        restart();
    }

    public Pool getPool() {
        return this.pool;
    }

    public void setPool(Pool pool) {
        this.pool = pool;
    }

    public String toString() {
        String name = getClass().getName();
        int dotIndex = name.lastIndexOf(46);
        if (dotIndex != -1) {
            name = name.substring(dotIndex + 1);
        }
        if (name.endsWith("Action")) {
            return name.substring(0, name.length() - 6);
        }
        return name;
    }
}
