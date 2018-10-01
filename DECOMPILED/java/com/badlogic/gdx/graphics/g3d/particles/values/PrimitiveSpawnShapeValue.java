package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public abstract class PrimitiveSpawnShapeValue extends SpawnShapeValue {
    protected static final Vector3 TMP_V1 = new Vector3();
    boolean edges = false;
    protected float spawnDepth;
    protected float spawnDepthDiff;
    public ScaledNumericValue spawnDepthValue = new ScaledNumericValue();
    protected float spawnHeight;
    protected float spawnHeightDiff;
    public ScaledNumericValue spawnHeightValue = new ScaledNumericValue();
    protected float spawnWidth;
    protected float spawnWidthDiff;
    public ScaledNumericValue spawnWidthValue = new ScaledNumericValue();

    public enum SpawnSide {
        both,
        top,
        bottom
    }

    public PrimitiveSpawnShapeValue(PrimitiveSpawnShapeValue value) {
        super(value);
    }

    public void setActive(boolean active) {
        super.setActive(active);
        this.spawnWidthValue.setActive(true);
        this.spawnHeightValue.setActive(true);
        this.spawnDepthValue.setActive(true);
    }

    public boolean isEdges() {
        return this.edges;
    }

    public void setEdges(boolean edges) {
        this.edges = edges;
    }

    public ScaledNumericValue getSpawnWidth() {
        return this.spawnWidthValue;
    }

    public ScaledNumericValue getSpawnHeight() {
        return this.spawnHeightValue;
    }

    public ScaledNumericValue getSpawnDepth() {
        return this.spawnDepthValue;
    }

    public void setDimensions(float width, float height, float depth) {
        this.spawnWidthValue.setHigh(width);
        this.spawnHeightValue.setHigh(height);
        this.spawnDepthValue.setHigh(depth);
    }

    public void start() {
        this.spawnWidth = this.spawnWidthValue.newLowValue();
        this.spawnWidthDiff = this.spawnWidthValue.newHighValue();
        if (!this.spawnWidthValue.isRelative()) {
            this.spawnWidthDiff -= this.spawnWidth;
        }
        this.spawnHeight = this.spawnHeightValue.newLowValue();
        this.spawnHeightDiff = this.spawnHeightValue.newHighValue();
        if (!this.spawnHeightValue.isRelative()) {
            this.spawnHeightDiff -= this.spawnHeight;
        }
        this.spawnDepth = this.spawnDepthValue.newLowValue();
        this.spawnDepthDiff = this.spawnDepthValue.newHighValue();
        if (!this.spawnDepthValue.isRelative()) {
            this.spawnDepthDiff -= this.spawnDepth;
        }
    }

    public void load(ParticleValue value) {
        super.load(value);
        PrimitiveSpawnShapeValue shape = (PrimitiveSpawnShapeValue) value;
        this.edges = shape.edges;
        this.spawnWidthValue.load(shape.spawnWidthValue);
        this.spawnHeightValue.load(shape.spawnHeightValue);
        this.spawnDepthValue.load(shape.spawnDepthValue);
    }

    public void write(Json json) {
        super.write(json);
        json.writeValue("spawnWidthValue", this.spawnWidthValue);
        json.writeValue("spawnHeightValue", this.spawnHeightValue);
        json.writeValue("spawnDepthValue", this.spawnDepthValue);
        json.writeValue("edges", Boolean.valueOf(this.edges));
    }

    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.spawnWidthValue = (ScaledNumericValue) json.readValue("spawnWidthValue", ScaledNumericValue.class, jsonData);
        this.spawnHeightValue = (ScaledNumericValue) json.readValue("spawnHeightValue", ScaledNumericValue.class, jsonData);
        this.spawnDepthValue = (ScaledNumericValue) json.readValue("spawnDepthValue", ScaledNumericValue.class, jsonData);
        this.edges = ((Boolean) json.readValue("edges", Boolean.TYPE, jsonData)).booleanValue();
    }
}
