package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.NumberUtils;

public class Decal {
    public static final int C1 = 3;
    public static final int C2 = 9;
    public static final int C3 = 15;
    public static final int C4 = 21;
    public static final int SIZE = 24;
    public static final int U1 = 4;
    public static final int U2 = 10;
    public static final int U3 = 16;
    public static final int U4 = 22;
    public static final int V1 = 5;
    public static final int V2 = 11;
    public static final int V3 = 17;
    public static final int V4 = 23;
    private static final int VERTEX_SIZE = 6;
    public static final int X1 = 0;
    public static final int X2 = 6;
    public static final int X3 = 12;
    public static final int X4 = 18;
    public static final int Y1 = 1;
    public static final int Y2 = 7;
    public static final int Y3 = 13;
    public static final int Y4 = 19;
    public static final int Z1 = 2;
    public static final int Z2 = 8;
    public static final int Z3 = 14;
    public static final int Z4 = 20;
    static final Vector3 dir = new Vector3();
    protected static Quaternion rotator = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    private static Vector3 tmp = new Vector3();
    private static Vector3 tmp2 = new Vector3();
    protected Color color;
    protected Vector2 dimensions;
    protected DecalMaterial material;
    protected Vector3 position;
    protected Quaternion rotation;
    protected Vector2 scale;
    public Vector2 transformationOffset;
    protected boolean updated;
    public int value;
    protected float[] vertices;

    public Decal() {
        this.vertices = new float[SIZE];
        this.position = new Vector3();
        this.rotation = new Quaternion();
        this.scale = new Vector2(1.0f, 1.0f);
        this.color = new Color();
        this.transformationOffset = null;
        this.dimensions = new Vector2();
        this.updated = false;
        this.material = new DecalMaterial();
    }

    public Decal(DecalMaterial material) {
        this.vertices = new float[SIZE];
        this.position = new Vector3();
        this.rotation = new Quaternion();
        this.scale = new Vector2(1.0f, 1.0f);
        this.color = new Color();
        this.transformationOffset = null;
        this.dimensions = new Vector2();
        this.updated = false;
        this.material = material;
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        float color = NumberUtils.intToFloatColor((((((int) (255.0f * a)) << SIZE) | (((int) (255.0f * b)) << U3)) | (((int) (255.0f * g)) << Z2)) | ((int) (255.0f * r)));
        this.vertices[C1] = color;
        this.vertices[C2] = color;
        this.vertices[C3] = color;
        this.vertices[C4] = color;
    }

    public void setColor(Color tint) {
        this.color.set(tint);
        float color = tint.toFloatBits();
        this.vertices[C1] = color;
        this.vertices[C2] = color;
        this.vertices[C3] = color;
        this.vertices[C4] = color;
    }

    public void setColor(float color) {
        this.color.set(NumberUtils.floatToIntColor(color));
        this.vertices[C1] = color;
        this.vertices[C2] = color;
        this.vertices[C3] = color;
        this.vertices[C4] = color;
    }

    public void setRotationX(float angle) {
        this.rotation.set(Vector3.X, angle);
        this.updated = false;
    }

    public void setRotationY(float angle) {
        this.rotation.set(Vector3.Y, angle);
        this.updated = false;
    }

    public void setRotationZ(float angle) {
        this.rotation.set(Vector3.Z, angle);
        this.updated = false;
    }

    public void rotateX(float angle) {
        rotator.set(Vector3.X, angle);
        this.rotation.mul(rotator);
        this.updated = false;
    }

    public void rotateY(float angle) {
        rotator.set(Vector3.Y, angle);
        this.rotation.mul(rotator);
        this.updated = false;
    }

    public void rotateZ(float angle) {
        rotator.set(Vector3.Z, angle);
        this.rotation.mul(rotator);
        this.updated = false;
    }

    public void setRotation(float yaw, float pitch, float roll) {
        this.rotation.setEulerAngles(yaw, pitch, roll);
        this.updated = false;
    }

    public void setRotation(Vector3 dir, Vector3 up) {
        tmp.set(up).crs(dir).nor();
        tmp2.set(dir).crs(tmp).nor();
        this.rotation.setFromAxes(tmp.x, tmp2.x, dir.x, tmp.y, tmp2.y, dir.y, tmp.z, tmp2.z, dir.z);
        this.updated = false;
    }

    public void setRotation(Quaternion q) {
        this.rotation.set(q);
        this.updated = false;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public void translateX(float units) {
        Vector3 vector3 = this.position;
        vector3.x += units;
        this.updated = false;
    }

    public void setX(float x) {
        this.position.x = x;
        this.updated = false;
    }

    public float getX() {
        return this.position.x;
    }

    public void translateY(float units) {
        Vector3 vector3 = this.position;
        vector3.y += units;
        this.updated = false;
    }

    public void setY(float y) {
        this.position.y = y;
        this.updated = false;
    }

    public float getY() {
        return this.position.y;
    }

    public void translateZ(float units) {
        Vector3 vector3 = this.position;
        vector3.z += units;
        this.updated = false;
    }

    public void setZ(float z) {
        this.position.z = z;
        this.updated = false;
    }

    public float getZ() {
        return this.position.z;
    }

    public void translate(float x, float y, float z) {
        this.position.add(x, y, z);
        this.updated = false;
    }

    public void translate(Vector3 trans) {
        this.position.add(trans);
        this.updated = false;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.updated = false;
    }

    public void setPosition(Vector3 pos) {
        this.position.set(pos);
        this.updated = false;
    }

    public Color getColor() {
        return this.color;
    }

    public Vector3 getPosition() {
        return this.position;
    }

    public void setScaleX(float scale) {
        this.scale.x = scale;
        this.updated = false;
    }

    public float getScaleX() {
        return this.scale.x;
    }

    public void setScaleY(float scale) {
        this.scale.y = scale;
        this.updated = false;
    }

    public float getScaleY() {
        return this.scale.y;
    }

    public void setScale(float scaleX, float scaleY) {
        this.scale.set(scaleX, scaleY);
        this.updated = false;
    }

    public void setScale(float scale) {
        this.scale.set(scale, scale);
        this.updated = false;
    }

    public void setWidth(float width) {
        this.dimensions.x = width;
        this.updated = false;
    }

    public float getWidth() {
        return this.dimensions.x;
    }

    public void setHeight(float height) {
        this.dimensions.y = height;
        this.updated = false;
    }

    public float getHeight() {
        return this.dimensions.y;
    }

    public void setDimensions(float width, float height) {
        this.dimensions.set(width, height);
        this.updated = false;
    }

    public float[] getVertices() {
        return this.vertices;
    }

    protected void update() {
        if (!this.updated) {
            resetVertices();
            transformVertices();
        }
    }

    protected void transformVertices() {
        float tx;
        float ty;
        if (this.transformationOffset != null) {
            tx = -this.transformationOffset.x;
            ty = -this.transformationOffset.y;
        } else {
            ty = 0.0f;
            tx = 0.0f;
        }
        float x = (this.vertices[X1] + tx) * this.scale.x;
        float y = (this.vertices[Y1] + ty) * this.scale.y;
        float z = this.vertices[Z1];
        this.vertices[X1] = ((this.rotation.w * x) + (this.rotation.y * z)) - (this.rotation.z * y);
        this.vertices[Y1] = ((this.rotation.w * y) + (this.rotation.z * x)) - (this.rotation.x * z);
        this.vertices[Z1] = ((this.rotation.w * z) + (this.rotation.x * y)) - (this.rotation.y * x);
        float w = (((-this.rotation.x) * x) - (this.rotation.y * y)) - (this.rotation.z * z);
        this.rotation.conjugate();
        x = this.vertices[X1];
        y = this.vertices[Y1];
        z = this.vertices[Z1];
        this.vertices[X1] = (((this.rotation.x * w) + (this.rotation.w * x)) + (this.rotation.z * y)) - (this.rotation.y * z);
        this.vertices[Y1] = (((this.rotation.y * w) + (this.rotation.w * y)) + (this.rotation.x * z)) - (this.rotation.z * x);
        this.vertices[Z1] = (((this.rotation.z * w) + (this.rotation.w * z)) + (this.rotation.y * x)) - (this.rotation.x * y);
        this.rotation.conjugate();
        float[] fArr = this.vertices;
        fArr[X1] = fArr[X1] + (this.position.x - tx);
        fArr = this.vertices;
        fArr[Y1] = fArr[Y1] + (this.position.y - ty);
        fArr = this.vertices;
        fArr[Z1] = fArr[Z1] + this.position.z;
        x = (this.vertices[X2] + tx) * this.scale.x;
        y = (this.vertices[Y2] + ty) * this.scale.y;
        z = this.vertices[Z2];
        this.vertices[X2] = ((this.rotation.w * x) + (this.rotation.y * z)) - (this.rotation.z * y);
        this.vertices[Y2] = ((this.rotation.w * y) + (this.rotation.z * x)) - (this.rotation.x * z);
        this.vertices[Z2] = ((this.rotation.w * z) + (this.rotation.x * y)) - (this.rotation.y * x);
        w = (((-this.rotation.x) * x) - (this.rotation.y * y)) - (this.rotation.z * z);
        this.rotation.conjugate();
        x = this.vertices[X2];
        y = this.vertices[Y2];
        z = this.vertices[Z2];
        this.vertices[X2] = (((this.rotation.x * w) + (this.rotation.w * x)) + (this.rotation.z * y)) - (this.rotation.y * z);
        this.vertices[Y2] = (((this.rotation.y * w) + (this.rotation.w * y)) + (this.rotation.x * z)) - (this.rotation.z * x);
        this.vertices[Z2] = (((this.rotation.z * w) + (this.rotation.w * z)) + (this.rotation.y * x)) - (this.rotation.x * y);
        this.rotation.conjugate();
        fArr = this.vertices;
        fArr[X2] = fArr[X2] + (this.position.x - tx);
        fArr = this.vertices;
        fArr[Y2] = fArr[Y2] + (this.position.y - ty);
        fArr = this.vertices;
        fArr[Z2] = fArr[Z2] + this.position.z;
        x = (this.vertices[X3] + tx) * this.scale.x;
        y = (this.vertices[Y3] + ty) * this.scale.y;
        z = this.vertices[Z3];
        this.vertices[X3] = ((this.rotation.w * x) + (this.rotation.y * z)) - (this.rotation.z * y);
        this.vertices[Y3] = ((this.rotation.w * y) + (this.rotation.z * x)) - (this.rotation.x * z);
        this.vertices[Z3] = ((this.rotation.w * z) + (this.rotation.x * y)) - (this.rotation.y * x);
        w = (((-this.rotation.x) * x) - (this.rotation.y * y)) - (this.rotation.z * z);
        this.rotation.conjugate();
        x = this.vertices[X3];
        y = this.vertices[Y3];
        z = this.vertices[Z3];
        this.vertices[X3] = (((this.rotation.x * w) + (this.rotation.w * x)) + (this.rotation.z * y)) - (this.rotation.y * z);
        this.vertices[Y3] = (((this.rotation.y * w) + (this.rotation.w * y)) + (this.rotation.x * z)) - (this.rotation.z * x);
        this.vertices[Z3] = (((this.rotation.z * w) + (this.rotation.w * z)) + (this.rotation.y * x)) - (this.rotation.x * y);
        this.rotation.conjugate();
        fArr = this.vertices;
        fArr[X3] = fArr[X3] + (this.position.x - tx);
        fArr = this.vertices;
        fArr[Y3] = fArr[Y3] + (this.position.y - ty);
        fArr = this.vertices;
        fArr[Z3] = fArr[Z3] + this.position.z;
        x = (this.vertices[X4] + tx) * this.scale.x;
        y = (this.vertices[Y4] + ty) * this.scale.y;
        z = this.vertices[Z4];
        this.vertices[X4] = ((this.rotation.w * x) + (this.rotation.y * z)) - (this.rotation.z * y);
        this.vertices[Y4] = ((this.rotation.w * y) + (this.rotation.z * x)) - (this.rotation.x * z);
        this.vertices[Z4] = ((this.rotation.w * z) + (this.rotation.x * y)) - (this.rotation.y * x);
        w = (((-this.rotation.x) * x) - (this.rotation.y * y)) - (this.rotation.z * z);
        this.rotation.conjugate();
        x = this.vertices[X4];
        y = this.vertices[Y4];
        z = this.vertices[Z4];
        this.vertices[X4] = (((this.rotation.x * w) + (this.rotation.w * x)) + (this.rotation.z * y)) - (this.rotation.y * z);
        this.vertices[Y4] = (((this.rotation.y * w) + (this.rotation.w * y)) + (this.rotation.x * z)) - (this.rotation.z * x);
        this.vertices[Z4] = (((this.rotation.z * w) + (this.rotation.w * z)) + (this.rotation.y * x)) - (this.rotation.x * y);
        this.rotation.conjugate();
        fArr = this.vertices;
        fArr[X4] = fArr[X4] + (this.position.x - tx);
        fArr = this.vertices;
        fArr[Y4] = fArr[Y4] + (this.position.y - ty);
        fArr = this.vertices;
        fArr[Z4] = fArr[Z4] + this.position.z;
        this.updated = true;
    }

    protected void resetVertices() {
        float left = (-this.dimensions.x) / 2.0f;
        float right = left + this.dimensions.x;
        float top = this.dimensions.y / 2.0f;
        float bottom = top - this.dimensions.y;
        this.vertices[X1] = left;
        this.vertices[Y1] = top;
        this.vertices[Z1] = 0.0f;
        this.vertices[X2] = right;
        this.vertices[Y2] = top;
        this.vertices[Z2] = 0.0f;
        this.vertices[X3] = left;
        this.vertices[Y3] = bottom;
        this.vertices[Z3] = 0.0f;
        this.vertices[X4] = right;
        this.vertices[Y4] = bottom;
        this.vertices[Z4] = 0.0f;
        this.updated = false;
    }

    protected void updateUVs() {
        TextureRegion tr = this.material.textureRegion;
        this.vertices[U1] = tr.getU();
        this.vertices[V1] = tr.getV();
        this.vertices[U2] = tr.getU2();
        this.vertices[V2] = tr.getV();
        this.vertices[U3] = tr.getU();
        this.vertices[V3] = tr.getV2();
        this.vertices[U4] = tr.getU2();
        this.vertices[V4] = tr.getV2();
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        this.material.textureRegion = textureRegion;
        updateUVs();
    }

    public TextureRegion getTextureRegion() {
        return this.material.textureRegion;
    }

    public void setBlending(int srcBlendFactor, int dstBlendFactor) {
        this.material.srcBlendFactor = srcBlendFactor;
        this.material.dstBlendFactor = dstBlendFactor;
    }

    public DecalMaterial getMaterial() {
        return this.material;
    }

    public void setMaterial(DecalMaterial material) {
        this.material = material;
    }

    public void lookAt(Vector3 position, Vector3 up) {
        dir.set(position).sub(this.position).nor();
        setRotation(dir, up);
    }

    public static Decal newDecal(TextureRegion textureRegion) {
        return newDecal((float) textureRegion.getRegionWidth(), (float) textureRegion.getRegionHeight(), textureRegion, -1, -1);
    }

    public static Decal newDecal(TextureRegion textureRegion, boolean hasTransparency) {
        int i = -1;
        float regionWidth = (float) textureRegion.getRegionWidth();
        float regionHeight = (float) textureRegion.getRegionHeight();
        int i2 = hasTransparency ? GL20.GL_SRC_ALPHA : -1;
        if (hasTransparency) {
            i = GL20.GL_ONE_MINUS_SRC_ALPHA;
        }
        return newDecal(regionWidth, regionHeight, textureRegion, i2, i);
    }

    public static Decal newDecal(float width, float height, TextureRegion textureRegion) {
        return newDecal(width, height, textureRegion, -1, -1);
    }

    public static Decal newDecal(float width, float height, TextureRegion textureRegion, boolean hasTransparency) {
        int i = -1;
        int i2 = hasTransparency ? GL20.GL_SRC_ALPHA : -1;
        if (hasTransparency) {
            i = GL20.GL_ONE_MINUS_SRC_ALPHA;
        }
        return newDecal(width, height, textureRegion, i2, i);
    }

    public static Decal newDecal(float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor) {
        Decal decal = new Decal();
        decal.setTextureRegion(textureRegion);
        decal.setBlending(srcBlendFactor, dstBlendFactor);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        return decal;
    }

    public static Decal newDecal(float width, float height, TextureRegion textureRegion, int srcBlendFactor, int dstBlendFactor, DecalMaterial material) {
        Decal decal = new Decal(material);
        decal.setTextureRegion(textureRegion);
        decal.setBlending(srcBlendFactor, dstBlendFactor);
        decal.dimensions.x = width;
        decal.dimensions.y = height;
        decal.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        return decal;
    }
}
