package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;

public abstract class FocusListener implements EventListener {

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$scenes$scene2d$utils$FocusListener$FocusEvent$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$utils$FocusListener$FocusEvent$Type[Type.keyboard.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$utils$FocusListener$FocusEvent$Type[Type.scroll.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public static class FocusEvent extends Event {
        private boolean focused;
        private Actor relatedActor;
        private Type type;

        public enum Type {
            keyboard,
            scroll
        }

        public void reset() {
            super.reset();
            this.relatedActor = null;
        }

        public boolean isFocused() {
            return this.focused;
        }

        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        public Type getType() {
            return this.type;
        }

        public void setType(Type focusType) {
            this.type = focusType;
        }

        public Actor getRelatedActor() {
            return this.relatedActor;
        }

        public void setRelatedActor(Actor relatedActor) {
            this.relatedActor = relatedActor;
        }
    }

    public boolean handle(Event event) {
        if (event instanceof FocusEvent) {
            FocusEvent focusEvent = (FocusEvent) event;
            switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$scenes$scene2d$utils$FocusListener$FocusEvent$Type[focusEvent.getType().ordinal()]) {
                case BuildConfig.VERSION_CODE /*1*/:
                    keyboardFocusChanged(focusEvent, event.getTarget(), focusEvent.isFocused());
                    break;
                case Base.kNumLenToPosStatesBits /*2*/:
                    scrollFocusChanged(focusEvent, event.getTarget(), focusEvent.isFocused());
                    break;
                default:
                    break;
            }
        }
        return false;
    }

    public void keyboardFocusChanged(FocusEvent event, Actor actor, boolean focused) {
    }

    public void scrollFocusChanged(FocusEvent event, Actor actor, boolean focused) {
    }
}
