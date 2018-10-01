package com.frontanilla.epicneonbattle.components;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Button {
    protected Color color;
    private TextureRegion glowingRegion;
    private float height;
    private TextureRegion normalRegion;
    private float width;
    private float x;
    private float y;

    public abstract void onTouchUp();

    public Button(TextureRegion normalRegion, TextureRegion glowingRegion, float x, float y, float width, float height, Color color) {
        this.normalRegion = normalRegion;
        this.glowingRegion = glowingRegion;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
    }

    public boolean isWithin(int x, int y) {
        return ((float) x) >= this.x && ((float) x) <= this.x + this.width && ((float) y) >= this.y && ((float) y) <= this.y + this.height;
    }

    public TextureRegion getNormalRegion() {
        return this.normalRegion;
    }

    public TextureRegion getGlowingRegion() {
        return this.glowingRegion;
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
