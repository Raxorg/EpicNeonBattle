package com.badlogic.gdx.maps;

import com.badlogic.gdx.graphics.Color;
import com.epicness.neonbattle.android.BuildConfig;

public class MapObject {
    private Color color = Color.WHITE.cpy();
    private String name = BuildConfig.VERSION_NAME;
    private float opacity = 1.0f;
    private MapProperties properties = new MapProperties();
    private boolean visible = true;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getOpacity() {
        return this.opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public MapProperties getProperties() {
        return this.properties;
    }
}
