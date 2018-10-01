package com.frontanilla.epicneonbattle.gamelogic;

import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.frontanilla.epicneonbattle.map.Map;
import com.frontanilla.epicneonbattle.neonbattle.android.BuildConfig;
import com.frontanilla.epicneonbattle.placeables.Base;
import com.frontanilla.epicneonbattle.placeables.Laboratory;
import com.frontanilla.epicneonbattle.placeables.Placeable;
import com.frontanilla.epicneonbattle.placeables.Refinery;
import com.frontanilla.epicneonbattle.placeables.Tank;
import com.frontanilla.epicneonbattle.placeables.Wall;

public class ActionObserver {
    private Action action;
    private Buyable buyable;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable = new int[Buyable.values().length];

        static {
            try {
                $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[Buyable.BASE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[Buyable.WALL.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[Buyable.TANK.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[Buyable.REFINERY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[Buyable.LABORATORY.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public enum Action {
        OBSERVING,
        BUILDING,
        NOTHING
    }

    public enum Buyable {
        BASE,
        WALL,
        TANK,
        REFINERY,
        LABORATORY,
        NOTHING
    }

    public enum Thing {
        WALL,
        TANK
    }

    public void attemptToBuy(Map.Cell cell) {
        Placeable p;
        switch (AnonymousClass1.$SwitchMap$com$epicness$gamelogic$ActionObserver$Buyable[this.buyable.ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                p = new Base();
                break;
            case com.badlogic.gdx.utils.compression.lzma.Base.kNumLenToPosStatesBits /*2*/:
                p = new Wall();
                break;
            case com.badlogic.gdx.utils.compression.lzma.Base.kNumMidLenBits /*3*/:
                p = new Tank();
                break;
            case com.badlogic.gdx.utils.compression.lzma.Base.kStartPosModelIndex /*4*/:
                p = new Refinery();
                break;
            case Encoder.kPropSize /*5*/:
                p = new Laboratory();
                break;
            default:
                return;
        }
        if (GameMaster.getInstance().getCurrentPlayer().getCubes() >= p.getCubeCost() && GameMaster.getInstance().getCurrentPlayer().getTech() >= p.getTechCost()) {
            GameMaster.getInstance().getCurrentPlayer().buy(p, cell);
        }
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public void setBuyable(Buyable buyable) {
        this.buyable = buyable;
    }

    public Action getAction() {
        return this.action;
    }
}
