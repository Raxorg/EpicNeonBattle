package com.badlogic.gdx.math;

import java.io.Serializable;

public class GridPoint2 implements Serializable {
    private static final long serialVersionUID = -4019969926331717380L;
    public int x;
    public int y;

    public GridPoint2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public GridPoint2(GridPoint2 point) {
        this.x = point.x;
        this.y = point.y;
    }

    public GridPoint2 set(GridPoint2 point) {
        this.x = point.x;
        this.y = point.y;
        return this;
    }

    public GridPoint2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        GridPoint2 g = (GridPoint2) o;
        if (this.x == g.x && this.y == g.y) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((this.x + 53) * 53) + this.y;
    }

    public String toString() {
        return "(" + this.x + ", " + this.y + ")";
    }
}
