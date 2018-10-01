package com.badlogic.gdx.backends.android.surfaceview;

import android.view.View.MeasureSpec;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy.MeasuredDimension;

public class FillResolutionStrategy implements ResolutionStrategy {
    public MeasuredDimension calcMeasures(int widthMeasureSpec, int heightMeasureSpec) {
        return new MeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
    }
}
