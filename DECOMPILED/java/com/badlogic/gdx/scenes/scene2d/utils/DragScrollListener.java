package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

public class DragScrollListener extends DragListener {
    Interpolation interpolation = Interpolation.exp5In;
    float maxSpeed = 75.0f;
    float minSpeed = 15.0f;
    long rampTime = 1750;
    private ScrollPane scroll;
    private Task scrollDown;
    private Task scrollUp;
    long startTime;
    float tickSecs = 0.05f;

    public DragScrollListener(final ScrollPane scroll) {
        this.scroll = scroll;
        this.scrollUp = new Task() {
            public void run() {
                scroll.setScrollY(scroll.getScrollY() - DragScrollListener.this.getScrollPixels());
            }
        };
        this.scrollDown = new Task() {
            public void run() {
                scroll.setScrollY(scroll.getScrollY() + DragScrollListener.this.getScrollPixels());
            }
        };
    }

    public void setup(float minSpeedPixels, float maxSpeedPixels, float tickSecs, float rampSecs) {
        this.minSpeed = minSpeedPixels;
        this.maxSpeed = maxSpeedPixels;
        this.tickSecs = tickSecs;
        this.rampTime = (long) (1000.0f * rampSecs);
    }

    float getScrollPixels() {
        return this.interpolation.apply(this.minSpeed, this.maxSpeed, Math.min(1.0f, ((float) (System.currentTimeMillis() - this.startTime)) / ((float) this.rampTime)));
    }

    public void drag(InputEvent event, float x, float y, int pointer) {
        if (x >= 0.0f && x < this.scroll.getWidth()) {
            if (y >= this.scroll.getHeight()) {
                this.scrollDown.cancel();
                if (!this.scrollUp.isScheduled()) {
                    this.startTime = System.currentTimeMillis();
                    Timer.schedule(this.scrollUp, this.tickSecs, this.tickSecs);
                    return;
                }
                return;
            } else if (y < 0.0f) {
                this.scrollUp.cancel();
                if (!this.scrollDown.isScheduled()) {
                    this.startTime = System.currentTimeMillis();
                    Timer.schedule(this.scrollDown, this.tickSecs, this.tickSecs);
                    return;
                }
                return;
            }
        }
        this.scrollUp.cancel();
        this.scrollDown.cancel();
    }

    public void dragStop(InputEvent event, float x, float y, int pointer) {
        this.scrollUp.cancel();
        this.scrollDown.cancel();
    }
}
