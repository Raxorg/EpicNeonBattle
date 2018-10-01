package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.NumberUtils;

public class Sprite extends TextureRegion {
    static final int SPRITE_SIZE = 20;
    static final int VERTEX_SIZE = 5;
    private Rectangle bounds;
    private final Color color;
    private boolean dirty;
    float height;
    private float originX;
    private float originY;
    private float rotation;
    private float scaleX;
    private float scaleY;
    final float[] vertices;
    float width;
    private float x;
    private float y;

    public Sprite() {
        this.vertices = new float[SPRITE_SIZE];
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dirty = true;
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public Sprite(Texture texture) {
        this(texture, 0, 0, texture.getWidth(), texture.getHeight());
    }

    public Sprite(Texture texture, int srcWidth, int srcHeight) {
        this(texture, 0, 0, srcWidth, srcHeight);
    }

    public Sprite(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
        this.vertices = new float[SPRITE_SIZE];
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dirty = true;
        if (texture == null) {
            throw new IllegalArgumentException("texture cannot be null.");
        }
        this.texture = texture;
        setRegion(srcX, srcY, srcWidth, srcHeight);
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
        setSize((float) Math.abs(srcWidth), (float) Math.abs(srcHeight));
        setOrigin(this.width / 2.0f, this.height / 2.0f);
    }

    public Sprite(TextureRegion region) {
        this.vertices = new float[SPRITE_SIZE];
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dirty = true;
        setRegion(region);
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
        setSize((float) region.getRegionWidth(), (float) region.getRegionHeight());
        setOrigin(this.width / 2.0f, this.height / 2.0f);
    }

    public Sprite(TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
        this.vertices = new float[SPRITE_SIZE];
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dirty = true;
        setRegion(region, srcX, srcY, srcWidth, srcHeight);
        setColor(1.0f, 1.0f, 1.0f, 1.0f);
        setSize((float) Math.abs(srcWidth), (float) Math.abs(srcHeight));
        setOrigin(this.width / 2.0f, this.height / 2.0f);
    }

    public Sprite(Sprite sprite) {
        this.vertices = new float[SPRITE_SIZE];
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.scaleX = 1.0f;
        this.scaleY = 1.0f;
        this.dirty = true;
        set(sprite);
    }

    public void set(Sprite sprite) {
        if (sprite == null) {
            throw new IllegalArgumentException("sprite cannot be null.");
        }
        System.arraycopy(sprite.vertices, 0, this.vertices, 0, SPRITE_SIZE);
        this.texture = sprite.texture;
        this.u = sprite.u;
        this.v = sprite.v;
        this.u2 = sprite.u2;
        this.v2 = sprite.v2;
        this.x = sprite.x;
        this.y = sprite.y;
        this.width = sprite.width;
        this.height = sprite.height;
        this.regionWidth = sprite.regionWidth;
        this.regionHeight = sprite.regionHeight;
        this.originX = sprite.originX;
        this.originY = sprite.originY;
        this.rotation = sprite.rotation;
        this.scaleX = sprite.scaleX;
        this.scaleY = sprite.scaleY;
        this.color.set(sprite.color);
        this.dirty = sprite.dirty;
    }

    public void setBounds(float x, float y, float width, float height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        if (!this.dirty) {
            float x2 = x + width;
            float y2 = y + height;
            float[] vertices = this.vertices;
            vertices[0] = x;
            vertices[1] = y;
            vertices[VERTEX_SIZE] = x;
            vertices[6] = y2;
            vertices[10] = x2;
            vertices[11] = y2;
            vertices[15] = x2;
            vertices[16] = y;
            if (this.rotation != 0.0f || this.scaleX != 1.0f || this.scaleY != 1.0f) {
                this.dirty = true;
            }
        }
    }

    public void setSize(float width, float height) {
        this.width = width;
        this.height = height;
        if (!this.dirty) {
            float x2 = this.x + width;
            float y2 = this.y + height;
            float[] vertices = this.vertices;
            vertices[0] = this.x;
            vertices[1] = this.y;
            vertices[VERTEX_SIZE] = this.x;
            vertices[6] = y2;
            vertices[10] = x2;
            vertices[11] = y2;
            vertices[15] = x2;
            vertices[16] = this.y;
            if (this.rotation != 0.0f || this.scaleX != 1.0f || this.scaleY != 1.0f) {
                this.dirty = true;
            }
        }
    }

    public void setPosition(float x, float y) {
        translate(x - this.x, y - this.y);
    }

    public void setX(float x) {
        translateX(x - this.x);
    }

    public void setY(float y) {
        translateY(y - this.y);
    }

    public void setCenterX(float x) {
        setX(x - (this.width / 2.0f));
    }

    public void setCenterY(float y) {
        setY(y - (this.height / 2.0f));
    }

    public void setCenter(float x, float y) {
        setCenterX(x);
        setCenterY(y);
    }

    public void translateX(float xAmount) {
        this.x += xAmount;
        if (!this.dirty) {
            float[] vertices = this.vertices;
            vertices[0] = vertices[0] + xAmount;
            vertices[VERTEX_SIZE] = vertices[VERTEX_SIZE] + xAmount;
            vertices[10] = vertices[10] + xAmount;
            vertices[15] = vertices[15] + xAmount;
        }
    }

    public void translateY(float yAmount) {
        this.y += yAmount;
        if (!this.dirty) {
            float[] vertices = this.vertices;
            vertices[1] = vertices[1] + yAmount;
            vertices[6] = vertices[6] + yAmount;
            vertices[11] = vertices[11] + yAmount;
            vertices[16] = vertices[16] + yAmount;
        }
    }

    public void translate(float xAmount, float yAmount) {
        this.x += xAmount;
        this.y += yAmount;
        if (!this.dirty) {
            float[] vertices = this.vertices;
            vertices[0] = vertices[0] + xAmount;
            vertices[1] = vertices[1] + yAmount;
            vertices[VERTEX_SIZE] = vertices[VERTEX_SIZE] + xAmount;
            vertices[6] = vertices[6] + yAmount;
            vertices[10] = vertices[10] + xAmount;
            vertices[11] = vertices[11] + yAmount;
            vertices[15] = vertices[15] + xAmount;
            vertices[16] = vertices[16] + yAmount;
        }
    }

    public void setColor(Color tint) {
        float color = tint.toFloatBits();
        float[] vertices = this.vertices;
        vertices[2] = color;
        vertices[7] = color;
        vertices[12] = color;
        vertices[17] = color;
    }

    public void setAlpha(float a) {
        float color = NumberUtils.intToFloatColor((NumberUtils.floatToIntColor(this.vertices[2]) & 16777215) | (((int) (255.0f * a)) << 24));
        this.vertices[2] = color;
        this.vertices[7] = color;
        this.vertices[12] = color;
        this.vertices[17] = color;
    }

    public void setColor(float r, float g, float b, float a) {
        float color = NumberUtils.intToFloatColor((((((int) (255.0f * a)) << 24) | (((int) (255.0f * b)) << 16)) | (((int) (255.0f * g)) << 8)) | ((int) (255.0f * r)));
        float[] vertices = this.vertices;
        vertices[2] = color;
        vertices[7] = color;
        vertices[12] = color;
        vertices[17] = color;
    }

    public void setColor(float color) {
        float[] vertices = this.vertices;
        vertices[2] = color;
        vertices[7] = color;
        vertices[12] = color;
        vertices[17] = color;
    }

    public void setOrigin(float originX, float originY) {
        this.originX = originX;
        this.originY = originY;
        this.dirty = true;
    }

    public void setOriginCenter() {
        this.originX = this.width / 2.0f;
        this.originY = this.height / 2.0f;
        this.dirty = true;
    }

    public void setRotation(float degrees) {
        this.rotation = degrees;
        this.dirty = true;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void rotate(float degrees) {
        if (degrees != 0.0f) {
            this.rotation += degrees;
            this.dirty = true;
        }
    }

    public void rotate90(boolean clockwise) {
        float[] vertices = this.vertices;
        if (clockwise) {
            float temp = vertices[4];
            vertices[4] = vertices[19];
            vertices[19] = vertices[14];
            vertices[14] = vertices[9];
            vertices[9] = temp;
            temp = vertices[3];
            vertices[3] = vertices[18];
            vertices[18] = vertices[13];
            vertices[13] = vertices[8];
            vertices[8] = temp;
            return;
        }
        temp = vertices[4];
        vertices[4] = vertices[9];
        vertices[9] = vertices[14];
        vertices[14] = vertices[19];
        vertices[19] = temp;
        temp = vertices[3];
        vertices[3] = vertices[8];
        vertices[8] = vertices[13];
        vertices[13] = vertices[18];
        vertices[18] = temp;
    }

    public void setScale(float scaleXY) {
        this.scaleX = scaleXY;
        this.scaleY = scaleXY;
        this.dirty = true;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.dirty = true;
    }

    public void scale(float amount) {
        this.scaleX += amount;
        this.scaleY += amount;
        this.dirty = true;
    }

    public float[] getVertices() {
        if (this.dirty) {
            this.dirty = false;
            float[] vertices = this.vertices;
            float localX = -this.originX;
            float localY = -this.originY;
            float localX2 = localX + this.width;
            float localY2 = localY + this.height;
            float worldOriginX = this.x - localX;
            float worldOriginY = this.y - localY;
            if (!(this.scaleX == 1.0f && this.scaleY == 1.0f)) {
                localX *= this.scaleX;
                localY *= this.scaleY;
                localX2 *= this.scaleX;
                localY2 *= this.scaleY;
            }
            float x1;
            float y1;
            float x2;
            float y2;
            if (this.rotation != 0.0f) {
                float cos = MathUtils.cosDeg(this.rotation);
                float sin = MathUtils.sinDeg(this.rotation);
                float localXCos = localX * cos;
                float localXSin = localX * sin;
                float localX2Cos = localX2 * cos;
                float localX2Sin = localX2 * sin;
                float localY2Cos = localY2 * cos;
                float localY2Sin = localY2 * sin;
                x1 = (localXCos - (localY * sin)) + worldOriginX;
                y1 = ((localY * cos) + localXSin) + worldOriginY;
                vertices[0] = x1;
                vertices[1] = y1;
                x2 = (localXCos - localY2Sin) + worldOriginX;
                y2 = (localY2Cos + localXSin) + worldOriginY;
                vertices[VERTEX_SIZE] = x2;
                vertices[6] = y2;
                float x3 = (localX2Cos - localY2Sin) + worldOriginX;
                float y3 = (localY2Cos + localX2Sin) + worldOriginY;
                vertices[10] = x3;
                vertices[11] = y3;
                vertices[15] = (x3 - x2) + x1;
                vertices[16] = y3 - (y2 - y1);
            } else {
                x1 = localX + worldOriginX;
                y1 = localY + worldOriginY;
                x2 = localX2 + worldOriginX;
                y2 = localY2 + worldOriginY;
                vertices[0] = x1;
                vertices[1] = y1;
                vertices[VERTEX_SIZE] = x1;
                vertices[6] = y2;
                vertices[10] = x2;
                vertices[11] = y2;
                vertices[15] = x2;
                vertices[16] = y1;
            }
        }
        return this.vertices;
    }

    public Rectangle getBoundingRectangle() {
        float[] vertices = getVertices();
        float minx = vertices[0];
        float miny = vertices[1];
        float maxx = vertices[0];
        float maxy = vertices[1];
        if (minx > vertices[VERTEX_SIZE]) {
            minx = vertices[VERTEX_SIZE];
        }
        if (minx > vertices[10]) {
            minx = vertices[10];
        }
        if (minx > vertices[15]) {
            minx = vertices[15];
        }
        if (maxx < vertices[VERTEX_SIZE]) {
            maxx = vertices[VERTEX_SIZE];
        }
        if (maxx < vertices[10]) {
            maxx = vertices[10];
        }
        if (maxx < vertices[15]) {
            maxx = vertices[15];
        }
        if (miny > vertices[6]) {
            miny = vertices[6];
        }
        if (miny > vertices[11]) {
            miny = vertices[11];
        }
        if (miny > vertices[16]) {
            miny = vertices[16];
        }
        if (maxy < vertices[6]) {
            maxy = vertices[6];
        }
        if (maxy < vertices[11]) {
            maxy = vertices[11];
        }
        if (maxy < vertices[16]) {
            maxy = vertices[16];
        }
        if (this.bounds == null) {
            this.bounds = new Rectangle();
        }
        this.bounds.x = minx;
        this.bounds.y = miny;
        this.bounds.width = maxx - minx;
        this.bounds.height = maxy - miny;
        return this.bounds;
    }

    public void draw(Batch batch) {
        batch.draw(this.texture, getVertices(), 0, (int) SPRITE_SIZE);
    }

    public void draw(Batch batch, float alphaModulation) {
        float oldAlpha = getColor().a;
        setAlpha(oldAlpha * alphaModulation);
        draw(batch);
        setAlpha(oldAlpha);
    }

    public float getX() {
        return this.x;
    }

    public float getY() {
        return this.y;
    }

    public float getWidth() {
        return this.width;
    }

    public float getHeight() {
        return this.height;
    }

    public float getOriginX() {
        return this.originX;
    }

    public float getOriginY() {
        return this.originY;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public Color getColor() {
        int intBits = NumberUtils.floatToIntColor(this.vertices[2]);
        Color color = this.color;
        color.r = ((float) (intBits & Keys.F12)) / 255.0f;
        color.g = ((float) ((intBits >>> 8) & Keys.F12)) / 255.0f;
        color.b = ((float) ((intBits >>> 16) & Keys.F12)) / 255.0f;
        color.a = ((float) ((intBits >>> 24) & Keys.F12)) / 255.0f;
        return color;
    }

    public void setRegion(float u, float v, float u2, float v2) {
        super.setRegion(u, v, u2, v2);
        float[] vertices = this.vertices;
        vertices[3] = u;
        vertices[4] = v2;
        vertices[8] = u;
        vertices[9] = v;
        vertices[13] = u2;
        vertices[14] = v;
        vertices[18] = u2;
        vertices[19] = v2;
    }

    public void setU(float u) {
        super.setU(u);
        this.vertices[3] = u;
        this.vertices[8] = u;
    }

    public void setV(float v) {
        super.setV(v);
        this.vertices[9] = v;
        this.vertices[14] = v;
    }

    public void setU2(float u2) {
        super.setU2(u2);
        this.vertices[13] = u2;
        this.vertices[18] = u2;
    }

    public void setV2(float v2) {
        super.setV2(v2);
        this.vertices[4] = v2;
        this.vertices[19] = v2;
    }

    public void setFlip(boolean x, boolean y) {
        boolean performX = false;
        boolean performY = false;
        if (isFlipX() != x) {
            performX = true;
        }
        if (isFlipY() != y) {
            performY = true;
        }
        flip(performX, performY);
    }

    public void flip(boolean x, boolean y) {
        super.flip(x, y);
        float[] vertices = this.vertices;
        if (x) {
            float temp = vertices[3];
            vertices[3] = vertices[13];
            vertices[13] = temp;
            temp = vertices[8];
            vertices[8] = vertices[18];
            vertices[18] = temp;
        }
        if (y) {
            temp = vertices[4];
            vertices[4] = vertices[14];
            vertices[14] = temp;
            temp = vertices[9];
            vertices[9] = vertices[19];
            vertices[19] = temp;
        }
    }

    public void scroll(float xAmount, float yAmount) {
        float[] vertices = this.vertices;
        if (xAmount != 0.0f) {
            float u = (vertices[3] + xAmount) % 1.0f;
            float u2 = u + (this.width / ((float) this.texture.getWidth()));
            this.u = u;
            this.u2 = u2;
            vertices[3] = u;
            vertices[8] = u;
            vertices[13] = u2;
            vertices[18] = u2;
        }
        if (yAmount != 0.0f) {
            float v = (vertices[9] + yAmount) % 1.0f;
            float v2 = v + (this.height / ((float) this.texture.getHeight()));
            this.v = v;
            this.v2 = v2;
            vertices[4] = v2;
            vertices[9] = v;
            vertices[14] = v;
            vertices[19] = v2;
        }
    }
}
