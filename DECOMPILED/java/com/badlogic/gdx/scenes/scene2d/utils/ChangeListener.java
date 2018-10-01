package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

public abstract class ChangeListener implements EventListener {

    public static class ChangeEvent extends Event {
    }

    public abstract void changed(ChangeEvent changeEvent, Actor actor);

    public boolean handle(Event event) {
        if (event instanceof ChangeEvent) {
            changed((ChangeEvent) event, event.getTarget());
        }
        return false;
    }
}
