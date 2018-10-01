package com.badlogic.gdx.backends.android.surfaceview;

import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy.MeasuredDimension;

public class FixedResolutionStrategy implements ResolutionStrategy {
    private final int height;
    private final int width;

    public FixedResolutionStrategy(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public MeasuredDimension calcMeasures(int widthMeasureSpec, int heightMeasureSpec) {
        return new MeasuredDimension(this.width, this.height);
    }
}
