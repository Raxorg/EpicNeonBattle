package com.badlogic.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener.FocusEvent;
import com.badlogic.gdx.utils.ObjectMap;

public class Dialog extends Window {
    Table buttonTable;
    boolean cancelHide;
    Table contentTable;
    FocusListener focusListener;
    protected InputListener ignoreTouchDown = new InputListener() {
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };
    Actor previousKeyboardFocus;
    Actor previousScrollFocus;
    private Skin skin;
    ObjectMap<Actor, Object> values = new ObjectMap();

    public Dialog(String title, Skin skin) {
        super(title, (WindowStyle) skin.get(WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public Dialog(String title, Skin skin, String windowStyleName) {
        super(title, (WindowStyle) skin.get(windowStyleName, WindowStyle.class));
        setSkin(skin);
        this.skin = skin;
        initialize();
    }

    public Dialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        initialize();
    }

    private void initialize() {
        setModal(true);
        defaults().space(6.0f);
        Actor table = new Table(this.skin);
        this.contentTable = table;
        add(table).expand().fill();
        row();
        table = new Table(this.skin);
        this.buttonTable = table;
        add(table);
        this.contentTable.defaults().space(6.0f);
        this.buttonTable.defaults().space(6.0f);
        this.buttonTable.addListener(new ChangeListener() {
            public void changed(ChangeEvent event, Actor actor) {
                if (Dialog.this.values.containsKey(actor)) {
                    while (actor.getParent() != Dialog.this.buttonTable) {
                        actor = actor.getParent();
                    }
                    Dialog.this.result(Dialog.this.values.get(actor));
                    if (!Dialog.this.cancelHide) {
                        Dialog.this.hide();
                    }
                    Dialog.this.cancelHide = false;
                }
            }
        });
        this.focusListener = new FocusListener() {
            public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusEvent event) {
                Stage stage = Dialog.this.getStage();
                if (Dialog.this.isModal && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == Dialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(Dialog.this) && !newFocusedActor.equals(Dialog.this.previousKeyboardFocus) && !newFocusedActor.equals(Dialog.this.previousScrollFocus)) {
                        event.cancel();
                    }
                }
            }
        };
    }

    protected void setStage(Stage stage) {
        if (stage == null) {
            addListener(this.focusListener);
        } else {
            removeListener(this.focusListener);
        }
        super.setStage(stage);
    }

    public Table getContentTable() {
        return this.contentTable;
    }

    public Table getButtonTable() {
        return this.buttonTable;
    }

    public Dialog text(String text) {
        if (this.skin != null) {
            return text(text, (LabelStyle) this.skin.get(LabelStyle.class));
        }
        throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
    }

    public Dialog text(String text, LabelStyle labelStyle) {
        return text(new Label((CharSequence) text, labelStyle));
    }

    public Dialog text(Label label) {
        this.contentTable.add((Actor) label);
        return this;
    }

    public Dialog button(String text) {
        return button(text, null);
    }

    public Dialog button(String text, Object object) {
        if (this.skin != null) {
            return button(text, object, (TextButtonStyle) this.skin.get(TextButtonStyle.class));
        }
        throw new IllegalStateException("This method may only be used if the dialog was constructed with a Skin.");
    }

    public Dialog button(String text, Object object, TextButtonStyle buttonStyle) {
        return button(new TextButton(text, buttonStyle), object);
    }

    public Dialog button(Button button) {
        return button(button, null);
    }

    public Dialog button(Button button, Object object) {
        this.buttonTable.add((Actor) button);
        setObject(button, object);
        return this;
    }

    public Dialog show(Stage stage, Action action) {
        clearActions();
        removeCaptureListener(this.ignoreTouchDown);
        this.previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (!(actor == null || actor.isDescendantOf(this))) {
            this.previousKeyboardFocus = actor;
        }
        this.previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (!(actor == null || actor.isDescendantOf(this))) {
            this.previousScrollFocus = actor;
        }
        pack();
        stage.addActor(this);
        stage.setKeyboardFocus(this);
        stage.setScrollFocus(this);
        if (action != null) {
            addAction(action);
        }
        return this;
    }

    public Dialog show(Stage stage) {
        show(stage, Actions.sequence(Actions.alpha(0.0f), Actions.fadeIn(0.4f, Interpolation.fade)));
        setPosition((float) Math.round((stage.getWidth() - getWidth()) / 2.0f), (float) Math.round((stage.getHeight() - getHeight()) / 2.0f));
        return this;
    }

    public void hide(Action action) {
        Stage stage = getStage();
        if (stage != null) {
            removeListener(this.focusListener);
            if (this.previousKeyboardFocus != null && this.previousKeyboardFocus.getStage() == null) {
                this.previousKeyboardFocus = null;
            }
            Actor actor = stage.getKeyboardFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setKeyboardFocus(this.previousKeyboardFocus);
            }
            if (this.previousScrollFocus != null && this.previousScrollFocus.getStage() == null) {
                this.previousScrollFocus = null;
            }
            actor = stage.getScrollFocus();
            if (actor == null || actor.isDescendantOf(this)) {
                stage.setScrollFocus(this.previousScrollFocus);
            }
        }
        if (action != null) {
            addCaptureListener(this.ignoreTouchDown);
            addAction(Actions.sequence(action, Actions.removeListener(this.ignoreTouchDown, true), Actions.removeActor()));
            return;
        }
        remove();
    }

    public void hide() {
        hide(Actions.sequence(Actions.fadeOut(0.4f, Interpolation.fade), Actions.removeListener(this.ignoreTouchDown, true), Actions.removeActor()));
    }

    public void setObject(Actor actor, Object object) {
        this.values.put(actor, object);
    }

    public Dialog key(final int keycode, final Object object) {
        addListener(new InputListener() {
            public boolean keyDown(InputEvent event, int keycode2) {
                if (keycode == keycode2) {
                    Dialog.this.result(object);
                    if (!Dialog.this.cancelHide) {
                        Dialog.this.hide();
                    }
                    Dialog.this.cancelHide = false;
                }
                return false;
            }
        });
        return this;
    }

    protected void result(Object object) {
    }

    public void cancel() {
        this.cancelHide = true;
    }
}
