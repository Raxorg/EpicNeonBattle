package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.epicness.neonbattle.android.BuildConfig;

public class AmbientCubemap {
    public final float[] data;

    private static final float clamp(float v) {
        if (v < 0.0f) {
            return 0.0f;
        }
        return v > 1.0f ? 1.0f : v;
    }

    public AmbientCubemap() {
        this.data = new float[18];
    }

    public AmbientCubemap(float[] copyFrom) {
        if (copyFrom.length != 18) {
            throw new GdxRuntimeException("Incorrect array size");
        }
        this.data = new float[copyFrom.length];
        System.arraycopy(copyFrom, 0, this.data, 0, this.data.length);
    }

    public AmbientCubemap(AmbientCubemap copyFrom) {
        this(copyFrom.data);
    }

    public AmbientCubemap set(float[] values) {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = values[i];
        }
        return this;
    }

    public AmbientCubemap set(AmbientCubemap other) {
        return set(other.data);
    }

    public AmbientCubemap set(Color color) {
        return set(color.r, color.g, color.b);
    }

    public AmbientCubemap set(float r, float g, float b) {
        int idx = 0;
        while (idx < this.data.length) {
            int idx2 = idx + 1;
            this.data[idx] = r;
            idx = idx2 + 1;
            this.data[idx2] = g;
            idx2 = idx + 1;
            this.data[idx] = b;
            idx = idx2;
        }
        return this;
    }

    public Color getColor(Color out, int side) {
        side *= 3;
        return out.set(this.data[side], this.data[side + 1], this.data[side + 2], 1.0f);
    }

    public AmbientCubemap clear() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = 0.0f;
        }
        return this;
    }

    public AmbientCubemap clamp() {
        for (int i = 0; i < this.data.length; i++) {
            this.data[i] = clamp(this.data[i]);
        }
        return this;
    }

    public AmbientCubemap add(float r, float g, float b) {
        int idx = 0;
        while (idx < this.data.length) {
            float[] fArr = this.data;
            int i = idx + 1;
            fArr[idx] = fArr[idx] + r;
            fArr = this.data;
            idx = i + 1;
            fArr[i] = fArr[i] + g;
            fArr = this.data;
            i = idx + 1;
            fArr[idx] = fArr[idx] + b;
            idx = i;
        }
        return this;
    }

    public AmbientCubemap add(Color color) {
        return add(color.r, color.g, color.b);
    }

    public AmbientCubemap add(float r, float g, float b, float x, float y, float z) {
        float x2 = x * x;
        float y2 = y * y;
        float z2 = z * z;
        float d = (x2 + y2) + z2;
        if (d != 0.0f) {
            d = (1.0f / d) * (1.0f + d);
            float rd = r * d;
            float gd = g * d;
            float bd = b * d;
            int idx = x > 0.0f ? 0 : 3;
            float[] fArr = this.data;
            fArr[idx] = fArr[idx] + (x2 * rd);
            fArr = this.data;
            int i = idx + 1;
            fArr[i] = fArr[i] + (x2 * gd);
            fArr = this.data;
            i = idx + 2;
            fArr[i] = fArr[i] + (x2 * bd);
            idx = y > 0.0f ? 6 : 9;
            fArr = this.data;
            fArr[idx] = fArr[idx] + (y2 * rd);
            fArr = this.data;
            i = idx + 1;
            fArr[i] = fArr[i] + (y2 * gd);
            fArr = this.data;
            i = idx + 2;
            fArr[i] = fArr[i] + (y2 * bd);
            idx = z > 0.0f ? 12 : 15;
            fArr = this.data;
            fArr[idx] = fArr[idx] + (z2 * rd);
            fArr = this.data;
            i = idx + 1;
            fArr[i] = fArr[i] + (z2 * gd);
            fArr = this.data;
            i = idx + 2;
            fArr[i] = fArr[i] + (z2 * bd);
        }
        return this;
    }

    public AmbientCubemap add(Color color, Vector3 direction) {
        return add(color.r, color.g, color.b, direction.x, direction.y, direction.z);
    }

    public AmbientCubemap add(float r, float g, float b, Vector3 direction) {
        return add(r, g, b, direction.x, direction.y, direction.z);
    }

    public AmbientCubemap add(Color color, float x, float y, float z) {
        return add(color.r, color.g, color.b, x, y, z);
    }

    public AmbientCubemap add(Color color, Vector3 point, Vector3 target) {
        return add(color.r, color.g, color.b, target.x - point.x, target.y - point.y, target.z - point.z);
    }

    public AmbientCubemap add(Color color, Vector3 point, Vector3 target, float intensity) {
        float t = intensity / (1.0f + target.dst(point));
        return add(color.r * t, color.g * t, color.b * t, target.x - point.x, target.y - point.y, target.z - point.z);
    }

    public String toString() {
        String result = BuildConfig.VERSION_NAME;
        for (int i = 0; i < this.data.length; i += 3) {
            result = result + Float.toString(this.data[i]) + ", " + Float.toString(this.data[i + 1]) + ", " + Float.toString(this.data[i + 2]) + "\n";
        }
        return result;
    }
}
