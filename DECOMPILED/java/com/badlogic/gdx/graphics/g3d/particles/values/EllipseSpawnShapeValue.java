package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.g3d.particles.values.PrimitiveSpawnShapeValue.SpawnSide;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public final class EllipseSpawnShapeValue extends PrimitiveSpawnShapeValue {
    SpawnSide side = SpawnSide.both;

    public EllipseSpawnShapeValue(EllipseSpawnShapeValue value) {
        super(value);
        load(value);
    }

    public void spawnAux(Vector3 vector, float percent) {
        float radiusX;
        float radiusY;
        float radiusZ;
        float width = this.spawnWidth + (this.spawnWidthDiff * this.spawnWidthValue.getScale(percent));
        float height = this.spawnHeight + (this.spawnHeightDiff * this.spawnHeightValue.getScale(percent));
        float depth = this.spawnDepth + (this.spawnDepthDiff * this.spawnDepthValue.getScale(percent));
        float maxT = MathUtils.PI2;
        if (this.side == SpawnSide.top) {
            maxT = MathUtils.PI;
        } else if (this.side == SpawnSide.bottom) {
            maxT = -3.1415927f;
        }
        float t = MathUtils.random(0.0f, maxT);
        if (!this.edges) {
            radiusX = MathUtils.random(width / 2.0f);
            radiusY = MathUtils.random(height / 2.0f);
            radiusZ = MathUtils.random(depth / 2.0f);
        } else if (width == 0.0f) {
            vector.set(0.0f, (height / 2.0f) * MathUtils.sin(t), (depth / 2.0f) * MathUtils.cos(t));
            return;
        } else if (height == 0.0f) {
            vector.set((width / 2.0f) * MathUtils.cos(t), 0.0f, (depth / 2.0f) * MathUtils.sin(t));
            return;
        } else if (depth == 0.0f) {
            vector.set((width / 2.0f) * MathUtils.cos(t), (height / 2.0f) * MathUtils.sin(t), 0.0f);
            return;
        } else {
            radiusX = width / 2.0f;
            radiusY = height / 2.0f;
            radiusZ = depth / 2.0f;
        }
        float z = MathUtils.random(-1.0f, 1.0f);
        float r = (float) Math.sqrt((double) (1.0f - (z * z)));
        vector.set((radiusX * r) * MathUtils.cos(t), (radiusY * r) * MathUtils.sin(t), radiusZ * z);
    }

    public SpawnSide getSide() {
        return this.side;
    }

    public void setSide(SpawnSide side) {
        this.side = side;
    }

    public void load(ParticleValue value) {
        super.load(value);
        this.side = ((EllipseSpawnShapeValue) value).side;
    }

    public SpawnShapeValue copy() {
        return new EllipseSpawnShapeValue(this);
    }

    public void write(Json json) {
        super.write(json);
        json.writeValue("side", this.side);
    }

    public void read(Json json, JsonValue jsonData) {
        super.read(json, jsonData);
        this.side = (SpawnSide) json.readValue("side", SpawnSide.class, jsonData);
    }
}
