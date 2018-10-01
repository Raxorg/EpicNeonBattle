package com.badlogic.gdx.math.collision;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;
import java.io.Serializable;

public class Sphere implements Serializable {
    private static final float PI_4_3 = 4.1887903f;
    private static final long serialVersionUID = -6487336868908521596L;
    public final Vector3 center;
    public float radius;

    public Sphere(Vector3 center, float radius) {
        this.center = new Vector3(center);
        this.radius = radius;
    }

    public boolean overlaps(Sphere sphere) {
        return this.center.dst2(sphere.center) < (this.radius + sphere.radius) * (this.radius + sphere.radius);
    }

    public int hashCode() {
        return ((this.center.hashCode() + 71) * 71) + NumberUtils.floatToRawIntBits(this.radius);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        Sphere s = (Sphere) o;
        if (this.radius == s.radius && this.center.equals(s.center)) {
            return true;
        }
        return false;
    }

    public float volume() {
        return ((PI_4_3 * this.radius) * this.radius) * this.radius;
    }

    public float surfaceArea() {
        return (12.566371f * this.radius) * this.radius;
    }
}
