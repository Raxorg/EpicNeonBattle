package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;

public class ActorGestureListener implements EventListener {
    static final Vector2 tmpCoords = new Vector2();
    static final Vector2 tmpCoords2 = new Vector2();
    Actor actor;
    private final GestureDetector detector;
    InputEvent event;
    Actor touchDownTarget;

    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchDown.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchUp.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchDragged.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public ActorGestureListener() {
        this(20.0f, 0.4f, 1.1f, 0.15f);
    }

    public ActorGestureListener(float halfTapSquareSize, float tapCountInterval, float longPressDuration, float maxFlingDelay) {
        this.detector = new GestureDetector(halfTapSquareSize, tapCountInterval, longPressDuration, maxFlingDelay, new GestureAdapter() {
            private final Vector2 initialPointer1 = new Vector2();
            private final Vector2 initialPointer2 = new Vector2();
            private final Vector2 pointer1 = new Vector2();
            private final Vector2 pointer2 = new Vector2();

            public boolean tap(float stageX, float stageY, int count, int button) {
                ActorGestureListener.this.actor.stageToLocalCoordinates(ActorGestureListener.tmpCoords.set(stageX, stageY));
                ActorGestureListener.this.tap(ActorGestureListener.this.event, ActorGestureListener.tmpCoords.x, ActorGestureListener.tmpCoords.y, count, button);
                return true;
            }

            public boolean longPress(float stageX, float stageY) {
                ActorGestureListener.this.actor.stageToLocalCoordinates(ActorGestureListener.tmpCoords.set(stageX, stageY));
                return ActorGestureListener.this.longPress(ActorGestureListener.this.actor, ActorGestureListener.tmpCoords.x, ActorGestureListener.tmpCoords.y);
            }

            public boolean fling(float velocityX, float velocityY, int button) {
                stageToLocalAmount(ActorGestureListener.tmpCoords.set(velocityX, velocityY));
                ActorGestureListener.this.fling(ActorGestureListener.this.event, ActorGestureListener.tmpCoords.x, ActorGestureListener.tmpCoords.y, button);
                return true;
            }

            public boolean pan(float stageX, float stageY, float deltaX, float deltaY) {
                stageToLocalAmount(ActorGestureListener.tmpCoords.set(deltaX, deltaY));
                deltaX = ActorGestureListener.tmpCoords.x;
                deltaY = ActorGestureListener.tmpCoords.y;
                ActorGestureListener.this.actor.stageToLocalCoordinates(ActorGestureListener.tmpCoords.set(stageX, stageY));
                ActorGestureListener.this.pan(ActorGestureListener.this.event, ActorGestureListener.tmpCoords.x, ActorGestureListener.tmpCoords.y, deltaX, deltaY);
                return true;
            }

            public boolean zoom(float initialDistance, float distance) {
                ActorGestureListener.this.zoom(ActorGestureListener.this.event, initialDistance, distance);
                return true;
            }

            public boolean pinch(Vector2 stageInitialPointer1, Vector2 stageInitialPointer2, Vector2 stagePointer1, Vector2 stagePointer2) {
                ActorGestureListener.this.actor.stageToLocalCoordinates(this.initialPointer1.set(stageInitialPointer1));
                ActorGestureListener.this.actor.stageToLocalCoordinates(this.initialPointer2.set(stageInitialPointer2));
                ActorGestureListener.this.actor.stageToLocalCoordinates(this.pointer1.set(stagePointer1));
                ActorGestureListener.this.actor.stageToLocalCoordinates(this.pointer2.set(stagePointer2));
                ActorGestureListener.this.pinch(ActorGestureListener.this.event, this.initialPointer1, this.initialPointer2, this.pointer1, this.pointer2);
                return true;
            }

            private void stageToLocalAmount(Vector2 amount) {
                ActorGestureListener.this.actor.stageToLocalCoordinates(amount);
                amount.sub(ActorGestureListener.this.actor.stageToLocalCoordinates(ActorGestureListener.tmpCoords2.set(0.0f, 0.0f)));
            }
        });
    }

    public boolean handle(Event e) {
        if (!(e instanceof InputEvent)) {
            return false;
        }
        InputEvent event = (InputEvent) e;
        switch (AnonymousClass2.$SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[event.getType().ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                this.actor = event.getListenerActor();
                this.touchDownTarget = event.getTarget();
                this.detector.touchDown(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
                this.actor.stageToLocalCoordinates(tmpCoords.set(event.getStageX(), event.getStageY()));
                touchDown(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
                return true;
            case Base.kNumLenToPosStatesBits /*2*/:
                if (event.isTouchFocusCancel()) {
                    return false;
                }
                this.event = event;
                this.actor = event.getListenerActor();
                this.detector.touchUp(event.getStageX(), event.getStageY(), event.getPointer(), event.getButton());
                this.actor.stageToLocalCoordinates(tmpCoords.set(event.getStageX(), event.getStageY()));
                touchUp(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
                return true;
            case Base.kNumMidLenBits /*3*/:
                this.event = event;
                this.actor = event.getListenerActor();
                this.detector.touchDragged(event.getStageX(), event.getStageY(), event.getPointer());
                return true;
            default:
                return false;
        }
    }

    public void touchDown(InputEvent event, float x, float y, int pointer, int button) {
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
    }

    public void tap(InputEvent event, float x, float y, int count, int button) {
    }

    public boolean longPress(Actor actor, float x, float y) {
        return false;
    }

    public void fling(InputEvent event, float velocityX, float velocityY, int button) {
    }

    public void pan(InputEvent event, float x, float y, float deltaX, float deltaY) {
    }

    public void zoom(InputEvent event, float initialDistance, float distance) {
    }

    public void pinch(InputEvent event, Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
    }

    public GestureDetector getGestureDetector() {
        return this.detector;
    }

    public Actor getTouchDownTarget() {
        return this.touchDownTarget;
    }
}
