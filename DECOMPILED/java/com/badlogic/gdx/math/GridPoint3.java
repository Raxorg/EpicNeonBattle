package com.badlogic.gdx.math;

import java.io.Serializable;

public class GridPoint3 implements Serializable {
    private static final long serialVersionUID = 5922187982746752830L;
    public int x;
    public int y;
    public int z;

    public GridPoint3(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public GridPoint3(GridPoint3 point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
    }

    public GridPoint3 set(GridPoint3 point) {
        this.x = point.x;
        this.y = point.y;
        this.z = point.z;
        return this;
    }

    public GridPoint3 set(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        GridPoint3 g = (GridPoint3) o;
        if (this.x == g.x && this.y == g.y && this.z == g.z) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((((this.x + 17) * 17) + this.y) * 17) + this.z;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ", " + this.z + ")";
    }
}
