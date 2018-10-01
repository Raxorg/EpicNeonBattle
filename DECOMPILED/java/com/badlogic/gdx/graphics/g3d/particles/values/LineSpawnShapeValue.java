package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public final class LineSpawnShapeValue extends PrimitiveSpawnShapeValue {
    public LineSpawnShapeValue(LineSpawnShapeValue value) {
        super(value);
        load(value);
    }

    public void spawnAux(Vector3 vector, float percent) {
        float width = this.spawnWidth + (this.spawnWidthDiff * this.spawnWidthValue.getScale(percent));
        float height = this.spawnHeight + (this.spawnHeightDiff * this.spawnHeightValue.getScale(percent));
        float depth = this.spawnDepth + (this.spawnDepthDiff * this.spawnDepthValue.getScale(percent));
        float a = MathUtils.random();
        vector.x = a * width;
        vector.y = a * height;
        vector.z = a * depth;
    }

    public SpawnShapeValue copy() {
        return new LineSpawnShapeValue(this);
    }
}
