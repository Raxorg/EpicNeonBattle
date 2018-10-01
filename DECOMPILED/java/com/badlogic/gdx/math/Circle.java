package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;
import java.io.Serializable;

public class Circle implements Shape2D, Serializable {
    public float radius;
    public float x;
    public float y;

    public Circle(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Circle(Vector2 position, float radius) {
        this.x = position.x;
        this.y = position.y;
        this.radius = radius;
    }

    public Circle(Circle circle) {
        this.x = circle.x;
        this.y = circle.y;
        this.radius = circle.radius;
    }

    public Circle(Vector2 center, Vector2 edge) {
        this.x = center.x;
        this.y = center.y;
        this.radius = Vector2.len(center.x - edge.x, center.y - edge.y);
    }

    public void set(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public void set(Vector2 position, float radius) {
        this.x = position.x;
        this.y = position.y;
        this.radius = radius;
    }

    public void set(Circle circle) {
        this.x = circle.x;
        this.y = circle.y;
        this.radius = circle.radius;
    }

    public void set(Vector2 center, Vector2 edge) {
        this.x = center.x;
        this.y = center.y;
        this.radius = Vector2.len(center.x - edge.x, center.y - edge.y);
    }

    public void setPosition(Vector2 position) {
        this.x = position.x;
        this.y = position.y;
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public boolean contains(float x, float y) {
        x = this.x - x;
        y = this.y - y;
        return (x * x) + (y * y) <= this.radius * this.radius;
    }

    public boolean contains(Vector2 point) {
        float dx = this.x - point.x;
        float dy = this.y - point.y;
        return (dx * dx) + (dy * dy) <= this.radius * this.radius;
    }

    public boolean contains(Circle c) {
        float radiusDiff = this.radius - c.radius;
        if (radiusDiff < 0.0f) {
            return false;
        }
        float dx = this.x - c.x;
        float dy = this.y - c.y;
        float dst = (dx * dx) + (dy * dy);
        float radiusSum = this.radius + c.radius;
        if (radiusDiff * radiusDiff < dst || dst >= radiusSum * radiusSum) {
            return false;
        }
        return true;
    }

    public boolean overlaps(Circle c) {
        float dx = this.x - c.x;
        float dy = this.y - c.y;
        float radiusSum = this.radius + c.radius;
        return (dx * dx) + (dy * dy) < radiusSum * radiusSum;
    }

    public String toString() {
        return this.x + "," + this.y + "," + this.radius;
    }

    public float circumference() {
        return this.radius * MathUtils.PI2;
    }

    public float area() {
        return (this.radius * this.radius) * MathUtils.PI;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null || o.getClass() != getClass()) {
            return false;
        }
        Circle c = (Circle) o;
        if (this.x == c.x && this.y == c.y && this.radius == c.radius) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((((NumberUtils.floatToRawIntBits(this.radius) + 41) * 41) + NumberUtils.floatToRawIntBits(this.x)) * 41) + NumberUtils.floatToRawIntBits(this.y);
    }
}
