package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;
import java.io.Serializable;

public class Rectangle implements Shape2D, Serializable {
    private static final long serialVersionUID = 5733252015138115702L;
    public static final Rectangle tmp = new Rectangle();
    public static final Rectangle tmp2 = new Rectangle();
    public float height;
    public float width;
    public float x;
    public float y;

    public Rectangle(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public Rectangle(Rectangle rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
    }

    public Rectangle set(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        return this;
    }

    public float getX() {
        return this.x;
    }

    public Rectangle setX(float x) {
        this.x = x;
        return this;
    }

    public float getY() {
        return this.y;
    }

    public Rectangle setY(float y) {
        this.y = y;
        return this;
    }

    public float getWidth() {
        return this.width;
    }

    public Rectangle setWidth(float width) {
        this.width = width;
        return this;
    }

    public float getHeight() {
        return this.height;
    }

    public Rectangle setHeight(float height) {
        this.height = height;
        return this;
    }

    public Vector2 getPosition(Vector2 position) {
        return position.set(this.x, this.y);
    }

    public Rectangle setPosition(Vector2 position) {
        this.x = position.x;
        this.y = position.y;
        return this;
    }

    public Rectangle setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Rectangle setSize(float width, float height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Rectangle setSize(float sizeXY) {
        this.width = sizeXY;
        this.height = sizeXY;
        return this;
    }

    public Vector2 getSize(Vector2 size) {
        return size.set(this.width, this.height);
    }

    public boolean contains(float x, float y) {
        return this.x <= x && this.x + this.width >= x && this.y <= y && this.y + this.height >= y;
    }

    public boolean contains(Vector2 point) {
        return contains(point.x, point.y);
    }

    public boolean contains(Rectangle rectangle) {
        float xmin = rectangle.x;
        float xmax = xmin + rectangle.width;
        float ymin = rectangle.y;
        float ymax = ymin + rectangle.height;
        return xmin > this.x && xmin < this.x + this.width && xmax > this.x && xmax < this.x + this.width && ymin > this.y && ymin < this.y + this.height && ymax > this.y && ymax < this.y + this.height;
    }

    public boolean overlaps(Rectangle r) {
        return this.x < r.x + r.width && this.x + this.width > r.x && this.y < r.y + r.height && this.y + this.height > r.y;
    }

    public Rectangle set(Rectangle rect) {
        this.x = rect.x;
        this.y = rect.y;
        this.width = rect.width;
        this.height = rect.height;
        return this;
    }

    public Rectangle merge(Rectangle rect) {
        float minX = Math.min(this.x, rect.x);
        float maxX = Math.max(this.x + this.width, rect.x + rect.width);
        this.x = minX;
        this.width = maxX - minX;
        float minY = Math.min(this.y, rect.y);
        float maxY = Math.max(this.y + this.height, rect.y + rect.height);
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public Rectangle merge(float x, float y) {
        float minX = Math.min(this.x, x);
        float maxX = Math.max(this.x + this.width, x);
        this.x = minX;
        this.width = maxX - minX;
        float minY = Math.min(this.y, y);
        float maxY = Math.max(this.y + this.height, y);
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public Rectangle merge(Vector2 vec) {
        return merge(vec.x, vec.y);
    }

    public Rectangle merge(Vector2[] vecs) {
        float minX = this.x;
        float maxX = this.x + this.width;
        float minY = this.y;
        float maxY = this.y + this.height;
        for (Vector2 v : vecs) {
            minX = Math.min(minX, v.x);
            maxX = Math.max(maxX, v.x);
            minY = Math.min(minY, v.y);
            maxY = Math.max(maxY, v.y);
        }
        this.x = minX;
        this.width = maxX - minX;
        this.y = minY;
        this.height = maxY - minY;
        return this;
    }

    public float getAspectRatio() {
        return this.height == 0.0f ? Float.NaN : this.width / this.height;
    }

    public Vector2 getCenter(Vector2 vector) {
        vector.x = this.x + (this.width / 2.0f);
        vector.y = this.y + (this.height / 2.0f);
        return vector;
    }

    public Rectangle setCenter(float x, float y) {
        setPosition(x - (this.width / 2.0f), y - (this.height / 2.0f));
        return this;
    }

    public Rectangle setCenter(Vector2 position) {
        setPosition(position.x - (this.width / 2.0f), position.y - (this.height / 2.0f));
        return this;
    }

    public Rectangle fitOutside(Rectangle rect) {
        float ratio = getAspectRatio();
        if (ratio > rect.getAspectRatio()) {
            setSize(rect.height * ratio, rect.height);
        } else {
            setSize(rect.width, rect.width / ratio);
        }
        setPosition((rect.x + (rect.width / 2.0f)) - (this.width / 2.0f), (rect.y + (rect.height / 2.0f)) - (this.height / 2.0f));
        return this;
    }

    public Rectangle fitInside(Rectangle rect) {
        float ratio = getAspectRatio();
        if (ratio < rect.getAspectRatio()) {
            setSize(rect.height * ratio, rect.height);
        } else {
            setSize(rect.width, rect.width / ratio);
        }
        setPosition((rect.x + (rect.width / 2.0f)) - (this.width / 2.0f), (rect.y + (rect.height / 2.0f)) - (this.height / 2.0f));
        return this;
    }

    public String toString() {
        return this.x + "," + this.y + "," + this.width + "," + this.height;
    }

    public float area() {
        return this.width * this.height;
    }

    public float perimeter() {
        return 2.0f * (this.width + this.height);
    }

    public int hashCode() {
        return ((((((NumberUtils.floatToRawIntBits(this.height) + 31) * 31) + NumberUtils.floatToRawIntBits(this.width)) * 31) + NumberUtils.floatToRawIntBits(this.x)) * 31) + NumberUtils.floatToRawIntBits(this.y);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Rectangle other = (Rectangle) obj;
        if (NumberUtils.floatToRawIntBits(this.height) != NumberUtils.floatToRawIntBits(other.height)) {
            return false;
        }
        if (NumberUtils.floatToRawIntBits(this.width) != NumberUtils.floatToRawIntBits(other.width)) {
            return false;
        }
        if (NumberUtils.floatToRawIntBits(this.x) != NumberUtils.floatToRawIntBits(other.x)) {
            return false;
        }
        if (NumberUtils.floatToRawIntBits(this.y) != NumberUtils.floatToRawIntBits(other.y)) {
            return false;
        }
        return true;
    }
}
