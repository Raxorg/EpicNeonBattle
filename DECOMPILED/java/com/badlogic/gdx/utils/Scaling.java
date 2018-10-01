package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;

public enum Scaling {
    fit,
    fill,
    fillX,
    fillY,
    stretch,
    stretchX,
    stretchY,
    none;
    
    private static final Vector2 temp = null;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$utils$Scaling = null;

        static {
            $SwitchMap$com$badlogic$gdx$utils$Scaling = new int[Scaling.values().length];
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.fit.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.fill.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.fillX.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.fillY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.stretch.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.stretchX.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.stretchY.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$utils$Scaling[Scaling.none.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
        }
    }

    static {
        temp = new Vector2();
    }

    public Vector2 apply(float sourceWidth, float sourceHeight, float targetWidth, float targetHeight) {
        float scale;
        switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$utils$Scaling[ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                scale = targetHeight / targetWidth > sourceHeight / sourceWidth ? targetWidth / sourceWidth : targetHeight / sourceHeight;
                temp.x = sourceWidth * scale;
                temp.y = sourceHeight * scale;
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                scale = targetHeight / targetWidth < sourceHeight / sourceWidth ? targetWidth / sourceWidth : targetHeight / sourceHeight;
                temp.x = sourceWidth * scale;
                temp.y = sourceHeight * scale;
                break;
            case Base.kNumMidLenBits /*3*/:
                scale = targetWidth / sourceWidth;
                temp.x = sourceWidth * scale;
                temp.y = sourceHeight * scale;
                break;
            case Base.kStartPosModelIndex /*4*/:
                scale = targetHeight / sourceHeight;
                temp.x = sourceWidth * scale;
                temp.y = sourceHeight * scale;
                break;
            case Encoder.kPropSize /*5*/:
                temp.x = targetWidth;
                temp.y = targetHeight;
                break;
            case com.badlogic.gdx.utils.compression.rangecoder.Encoder.kNumBitPriceShiftBits /*6*/:
                temp.x = targetWidth;
                temp.y = sourceHeight;
                break;
            case Matrix4.M31 /*7*/:
                temp.x = sourceWidth;
                temp.y = targetHeight;
                break;
            case Base.kNumMidLenSymbols /*8*/:
                temp.x = sourceWidth;
                temp.y = sourceHeight;
                break;
        }
        return temp;
    }
}
