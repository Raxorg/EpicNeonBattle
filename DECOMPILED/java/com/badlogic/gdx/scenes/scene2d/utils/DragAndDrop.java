package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import java.util.Iterator;

public class DragAndDrop {
    static final Vector2 tmpVector = new Vector2();
    int activePointer = -1;
    private int button;
    boolean cancelTouchFocus = true;
    Actor dragActor;
    float dragActorX = 14.0f;
    float dragActorY = -20.0f;
    long dragStartTime;
    int dragTime = Keys.F7;
    boolean isValidTarget;
    boolean keepWithinStage = true;
    Payload payload;
    ObjectMap<Source, DragListener> sourceListeners = new ObjectMap();
    private float tapSquareSize = 8.0f;
    Target target;
    Array<Target> targets = new Array();
    float touchOffsetX;
    float touchOffsetY;

    public static class Payload {
        Actor dragActor;
        Actor invalidDragActor;
        Object object;
        Actor validDragActor;

        public void setDragActor(Actor dragActor) {
            this.dragActor = dragActor;
        }

        public Actor getDragActor() {
            return this.dragActor;
        }

        public void setValidDragActor(Actor validDragActor) {
            this.validDragActor = validDragActor;
        }

        public Actor getValidDragActor() {
            return this.validDragActor;
        }

        public void setInvalidDragActor(Actor invalidDragActor) {
            this.invalidDragActor = invalidDragActor;
        }

        public Actor getInvalidDragActor() {
            return this.invalidDragActor;
        }

        public Object getObject() {
            return this.object;
        }

        public void setObject(Object object) {
            this.object = object;
        }
    }

    public static abstract class Source {
        final Actor actor;

        public abstract Payload dragStart(InputEvent inputEvent, float f, float f2, int i);

        public Source(Actor actor) {
            if (actor == null) {
                throw new IllegalArgumentException("actor cannot be null.");
            }
            this.actor = actor;
        }

        public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, Target target) {
        }

        public Actor getActor() {
            return this.actor;
        }
    }

    public static abstract class Target {
        final Actor actor;

        public abstract boolean drag(Source source, Payload payload, float f, float f2, int i);

        public abstract void drop(Source source, Payload payload, float f, float f2, int i);

        public Target(Actor actor) {
            if (actor == null) {
                throw new IllegalArgumentException("actor cannot be null.");
            }
            this.actor = actor;
            Stage stage = actor.getStage();
            if (stage != null && actor == stage.getRoot()) {
                throw new IllegalArgumentException("The stage root cannot be a drag and drop target.");
            }
        }

        public void reset(Source source, Payload payload) {
        }

        public Actor getActor() {
            return this.actor;
        }
    }

    public void addSource(final Source source) {
        DragListener listener = new DragListener() {
            public void dragStart(InputEvent event, float x, float y, int pointer) {
                if (DragAndDrop.this.activePointer != -1) {
                    event.stop();
                    return;
                }
                DragAndDrop.this.activePointer = pointer;
                DragAndDrop.this.dragStartTime = System.currentTimeMillis();
                DragAndDrop.this.payload = source.dragStart(event, getTouchDownX(), getTouchDownY(), pointer);
                event.stop();
                if (DragAndDrop.this.cancelTouchFocus && DragAndDrop.this.payload != null) {
                    source.getActor().getStage().cancelTouchFocusExcept(this, source.getActor());
                }
            }

            public void drag(InputEvent event, float x, float y, int pointer) {
                if (DragAndDrop.this.payload != null && pointer == DragAndDrop.this.activePointer) {
                    Stage stage = event.getStage();
                    Touchable dragActorTouchable = null;
                    if (DragAndDrop.this.dragActor != null) {
                        dragActorTouchable = DragAndDrop.this.dragActor.getTouchable();
                        DragAndDrop.this.dragActor.setTouchable(Touchable.disabled);
                    }
                    Target newTarget = null;
                    DragAndDrop.this.isValidTarget = false;
                    float stageX = event.getStageX() + DragAndDrop.this.touchOffsetX;
                    float stageY = event.getStageY() + DragAndDrop.this.touchOffsetY;
                    Actor hit = event.getStage().hit(stageX, stageY, true);
                    if (hit == null) {
                        hit = event.getStage().hit(stageX, stageY, false);
                    }
                    if (hit != null) {
                        int n = DragAndDrop.this.targets.size;
                        for (int i = 0; i < n; i++) {
                            Target target = (Target) DragAndDrop.this.targets.get(i);
                            if (target.actor.isAscendantOf(hit)) {
                                newTarget = target;
                                target.actor.stageToLocalCoordinates(DragAndDrop.tmpVector.set(stageX, stageY));
                                break;
                            }
                        }
                    }
                    if (newTarget != DragAndDrop.this.target) {
                        if (DragAndDrop.this.target != null) {
                            DragAndDrop.this.target.reset(source, DragAndDrop.this.payload);
                        }
                        DragAndDrop.this.target = newTarget;
                    }
                    if (newTarget != null) {
                        DragAndDrop.this.isValidTarget = newTarget.drag(source, DragAndDrop.this.payload, DragAndDrop.tmpVector.x, DragAndDrop.tmpVector.y, pointer);
                    }
                    if (DragAndDrop.this.dragActor != null) {
                        DragAndDrop.this.dragActor.setTouchable(dragActorTouchable);
                    }
                    Actor actor = null;
                    if (DragAndDrop.this.target != null) {
                        actor = DragAndDrop.this.isValidTarget ? DragAndDrop.this.payload.validDragActor : DragAndDrop.this.payload.invalidDragActor;
                    }
                    if (actor == null) {
                        actor = DragAndDrop.this.payload.dragActor;
                    }
                    if (actor != null) {
                        if (DragAndDrop.this.dragActor != actor) {
                            if (DragAndDrop.this.dragActor != null) {
                                DragAndDrop.this.dragActor.remove();
                            }
                            DragAndDrop.this.dragActor = actor;
                            stage.addActor(actor);
                        }
                        float actorX = event.getStageX() + DragAndDrop.this.dragActorX;
                        float actorY = (event.getStageY() + DragAndDrop.this.dragActorY) - actor.getHeight();
                        if (DragAndDrop.this.keepWithinStage) {
                            if (actorX < 0.0f) {
                                actorX = 0.0f;
                            }
                            if (actorY < 0.0f) {
                                actorY = 0.0f;
                            }
                            if (actor.getWidth() + actorX > stage.getWidth()) {
                                actorX = stage.getWidth() - actor.getWidth();
                            }
                            if (actor.getHeight() + actorY > stage.getHeight()) {
                                actorY = stage.getHeight() - actor.getHeight();
                            }
                        }
                        actor.setPosition(actorX, actorY);
                    }
                }
            }

            public void dragStop(InputEvent event, float x, float y, int pointer) {
                if (pointer == DragAndDrop.this.activePointer) {
                    DragAndDrop.this.activePointer = -1;
                    if (DragAndDrop.this.payload != null) {
                        if (System.currentTimeMillis() - DragAndDrop.this.dragStartTime < ((long) DragAndDrop.this.dragTime)) {
                            DragAndDrop.this.isValidTarget = false;
                        }
                        if (DragAndDrop.this.dragActor != null) {
                            DragAndDrop.this.dragActor.remove();
                        }
                        if (DragAndDrop.this.isValidTarget) {
                            DragAndDrop.this.target.actor.stageToLocalCoordinates(DragAndDrop.tmpVector.set(event.getStageX() + DragAndDrop.this.touchOffsetX, event.getStageY() + DragAndDrop.this.touchOffsetY));
                            DragAndDrop.this.target.drop(source, DragAndDrop.this.payload, DragAndDrop.tmpVector.x, DragAndDrop.tmpVector.y, pointer);
                        }
                        source.dragStop(event, x, y, pointer, DragAndDrop.this.payload, DragAndDrop.this.isValidTarget ? DragAndDrop.this.target : null);
                        if (DragAndDrop.this.target != null) {
                            DragAndDrop.this.target.reset(source, DragAndDrop.this.payload);
                        }
                        DragAndDrop.this.payload = null;
                        DragAndDrop.this.target = null;
                        DragAndDrop.this.isValidTarget = false;
                        DragAndDrop.this.dragActor = null;
                    }
                }
            }
        };
        listener.setTapSquareSize(this.tapSquareSize);
        listener.setButton(this.button);
        source.actor.addCaptureListener(listener);
        this.sourceListeners.put(source, listener);
    }

    public void removeSource(Source source) {
        source.actor.removeCaptureListener((DragListener) this.sourceListeners.remove(source));
    }

    public void addTarget(Target target) {
        this.targets.add(target);
    }

    public void removeTarget(Target target) {
        this.targets.removeValue(target, true);
    }

    public void clear() {
        this.targets.clear();
        Iterator it = this.sourceListeners.entries().iterator();
        while (it.hasNext()) {
            Entry<Source, DragListener> entry = (Entry) it.next();
            ((Source) entry.key).actor.removeCaptureListener((EventListener) entry.value);
        }
        this.sourceListeners.clear();
    }

    public void setTapSquareSize(float halfTapSquareSize) {
        this.tapSquareSize = halfTapSquareSize;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public void setDragActorPosition(float dragActorX, float dragActorY) {
        this.dragActorX = dragActorX;
        this.dragActorY = dragActorY;
    }

    public void setTouchOffset(float touchOffsetX, float touchOffsetY) {
        this.touchOffsetX = touchOffsetX;
        this.touchOffsetY = touchOffsetY;
    }

    public boolean isDragging() {
        return this.payload != null;
    }

    public Actor getDragActor() {
        return this.dragActor;
    }

    public void setDragTime(int dragMillis) {
        this.dragTime = dragMillis;
    }

    public void setCancelTouchFocus(boolean cancelTouchFocus) {
        this.cancelTouchFocus = cancelTouchFocus;
    }

    public void setKeepWithinStage(boolean keepWithinStage) {
        this.keepWithinStage = keepWithinStage;
    }
}
