package com.badlogic.gdx.scenes.scene2d;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;

public class InputListener implements EventListener {
    private static final Vector2 tmpCoords = new Vector2();

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.keyDown.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.keyUp.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.keyTyped.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchDown.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchUp.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.touchDragged.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.mouseMoved.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.scrolled.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.enter.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[Type.exit.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
        }
    }

    public boolean handle(Event e) {
        if (!(e instanceof InputEvent)) {
            return false;
        }
        InputEvent event = (InputEvent) e;
        switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[event.getType().ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                return keyDown(event, event.getKeyCode());
            case Base.kNumLenToPosStatesBits /*2*/:
                return keyUp(event, event.getKeyCode());
            case Base.kNumMidLenBits /*3*/:
                return keyTyped(event, event.getCharacter());
            default:
                event.toCoordinates(event.getListenerActor(), tmpCoords);
                switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$scenes$scene2d$InputEvent$Type[event.getType().ordinal()]) {
                    case Base.kStartPosModelIndex /*4*/:
                        return touchDown(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
                    case Encoder.kPropSize /*5*/:
                        touchUp(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getButton());
                        return true;
                    case com.badlogic.gdx.utils.compression.rangecoder.Encoder.kNumBitPriceShiftBits /*6*/:
                        touchDragged(event, tmpCoords.x, tmpCoords.y, event.getPointer());
                        return true;
                    case Matrix4.M31 /*7*/:
                        return mouseMoved(event, tmpCoords.x, tmpCoords.y);
                    case Base.kNumMidLenSymbols /*8*/:
                        return scrolled(event, tmpCoords.x, tmpCoords.y, event.getScrollAmount());
                    case Matrix4.M12 /*9*/:
                        enter(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getRelatedActor());
                        return false;
                    case Base.kNumPosModels /*10*/:
                        exit(event, tmpCoords.x, tmpCoords.y, event.getPointer(), event.getRelatedActor());
                        return false;
                    default:
                        return false;
                }
        }
    }

    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        return false;
    }

    public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
    }

    public void touchDragged(InputEvent event, float x, float y, int pointer) {
    }

    public boolean mouseMoved(InputEvent event, float x, float y) {
        return false;
    }

    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
    }

    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
    }

    public boolean scrolled(InputEvent event, float x, float y, int amount) {
        return false;
    }

    public boolean keyDown(InputEvent event, int keycode) {
        return false;
    }

    public boolean keyUp(InputEvent event, int keycode) {
        return false;
    }

    public boolean keyTyped(InputEvent event, char character) {
        return false;
    }
}
