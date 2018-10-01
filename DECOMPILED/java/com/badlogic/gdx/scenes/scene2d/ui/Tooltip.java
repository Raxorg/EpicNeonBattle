package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;

public class Tooltip<T extends Actor> extends InputListener {
    static Vector2 tmp = new Vector2();
    boolean always;
    final Container<T> container;
    boolean instant;
    private final TooltipManager manager;
    Actor targetActor;

    public Tooltip(T contents) {
        this(contents, TooltipManager.getInstance());
    }

    public Tooltip(T contents, TooltipManager manager) {
        this.manager = manager;
        this.container = new Container(contents) {
            public void act(float delta) {
                super.act(delta);
                if (Tooltip.this.targetActor != null && Tooltip.this.targetActor.getStage() == null) {
                    remove();
                }
            }
        };
        this.container.setTouchable(Touchable.disabled);
        this.container.pack();
    }

    public TooltipManager getManager() {
        return this.manager;
    }

    public Container<T> getContainer() {
        return this.container;
    }

    public void setActor(T contents) {
        this.container.setActor(contents);
        this.container.pack();
    }

    public T getActor() {
        return this.container.getActor();
    }

    public void setInstant(boolean instant) {
        this.instant = instant;
    }

    public void setAlways(boolean always) {
        this.always = always;
    }

    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (this.instant) {
            this.container.toFront();
        } else {
            this.manager.touchDown(this);
        }
        return false;
    }

    public boolean mouseMoved(InputEvent event, float x, float y) {
        if (this.container.hasParent()) {
            return false;
        }
        setContainerPosition(event.getListenerActor(), x, y);
        return true;
    }

    private void setContainerPosition(Actor actor, float x, float y) {
        this.targetActor = actor;
        Stage stage = actor.getStage();
        if (stage != null) {
            float offsetX = this.manager.offsetX;
            float offsetY = this.manager.offsetY;
            float dist = this.manager.edgeDistance;
            Vector2 point = actor.localToStageCoordinates(tmp.set(x + offsetX, (y - offsetY) - this.container.getHeight()));
            if (point.y < dist) {
                point = actor.localToStageCoordinates(tmp.set(x + offsetX, y + offsetY));
            }
            if (point.x < dist) {
                point.x = dist;
            }
            if (point.x + this.container.getWidth() > stage.getWidth() - dist) {
                point.x = (stage.getWidth() - dist) - this.container.getWidth();
            }
            if (point.y + this.container.getHeight() > stage.getHeight() - dist) {
                point.y = (stage.getHeight() - dist) - this.container.getHeight();
            }
            this.container.setPosition(point.x, point.y);
            point = actor.localToStageCoordinates(tmp.set(actor.getWidth() / 2.0f, actor.getHeight() / 2.0f));
            point.sub(this.container.getX(), this.container.getY());
            this.container.setOrigin(point.x, point.y);
        }
    }

    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
        if (pointer == -1 && !Gdx.input.isTouched()) {
            Actor actor = event.getListenerActor();
            if (fromActor == null || !fromActor.isDescendantOf(actor)) {
                setContainerPosition(actor, x, y);
                this.manager.enter(this);
            }
        }
    }

    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
        if (toActor == null || !toActor.isDescendantOf(event.getListenerActor())) {
            hide();
        }
    }

    public void hide() {
        this.manager.hide(this);
    }
}
