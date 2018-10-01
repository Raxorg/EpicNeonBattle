package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.GdxRuntimeException;
import java.io.Serializable;

public class Matrix3 implements Serializable {
    public static final int M00 = 0;
    public static final int M01 = 3;
    public static final int M02 = 6;
    public static final int M10 = 1;
    public static final int M11 = 4;
    public static final int M12 = 7;
    public static final int M20 = 2;
    public static final int M21 = 5;
    public static final int M22 = 8;
    private static final long serialVersionUID = 7907569533774959788L;
    private float[] tmp;
    public float[] val;

    public Matrix3() {
        this.val = new float[9];
        this.tmp = new float[9];
        idt();
    }

    public Matrix3(Matrix3 matrix) {
        this.val = new float[9];
        this.tmp = new float[9];
        set(matrix);
    }

    public Matrix3(float[] values) {
        this.val = new float[9];
        this.tmp = new float[9];
        set(values);
    }

    public Matrix3 idt() {
        float[] val = this.val;
        val[M00] = 1.0f;
        val[M10] = 0.0f;
        val[M20] = 0.0f;
        val[M01] = 0.0f;
        val[M11] = 1.0f;
        val[M21] = 0.0f;
        val[M02] = 0.0f;
        val[M12] = 0.0f;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 mul(Matrix3 m) {
        float[] val = this.val;
        float v01 = ((val[M00] * m.val[M01]) + (val[M01] * m.val[M11])) + (val[M02] * m.val[M21]);
        float v02 = ((val[M00] * m.val[M02]) + (val[M01] * m.val[M12])) + (val[M02] * m.val[M22]);
        float v10 = ((val[M10] * m.val[M00]) + (val[M11] * m.val[M10])) + (val[M12] * m.val[M20]);
        float v11 = ((val[M10] * m.val[M01]) + (val[M11] * m.val[M11])) + (val[M12] * m.val[M21]);
        float v12 = ((val[M10] * m.val[M02]) + (val[M11] * m.val[M12])) + (val[M12] * m.val[M22]);
        float v20 = ((val[M20] * m.val[M00]) + (val[M21] * m.val[M10])) + (val[M22] * m.val[M20]);
        float v21 = ((val[M20] * m.val[M01]) + (val[M21] * m.val[M11])) + (val[M22] * m.val[M21]);
        float v22 = ((val[M20] * m.val[M02]) + (val[M21] * m.val[M12])) + (val[M22] * m.val[M22]);
        val[M00] = ((val[M00] * m.val[M00]) + (val[M01] * m.val[M10])) + (val[M02] * m.val[M20]);
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;
        return this;
    }

    public Matrix3 mulLeft(Matrix3 m) {
        float[] val = this.val;
        float v01 = ((m.val[M00] * val[M01]) + (m.val[M01] * val[M11])) + (m.val[M02] * val[M21]);
        float v02 = ((m.val[M00] * val[M02]) + (m.val[M01] * val[M12])) + (m.val[M02] * val[M22]);
        float v10 = ((m.val[M10] * val[M00]) + (m.val[M11] * val[M10])) + (m.val[M12] * val[M20]);
        float v11 = ((m.val[M10] * val[M01]) + (m.val[M11] * val[M11])) + (m.val[M12] * val[M21]);
        float v12 = ((m.val[M10] * val[M02]) + (m.val[M11] * val[M12])) + (m.val[M12] * val[M22]);
        float v20 = ((m.val[M20] * val[M00]) + (m.val[M21] * val[M10])) + (m.val[M22] * val[M20]);
        float v21 = ((m.val[M20] * val[M01]) + (m.val[M21] * val[M11])) + (m.val[M22] * val[M21]);
        float v22 = ((m.val[M20] * val[M02]) + (m.val[M21] * val[M12])) + (m.val[M22] * val[M22]);
        val[M00] = ((m.val[M00] * val[M00]) + (m.val[M01] * val[M10])) + (m.val[M02] * val[M20]);
        val[M10] = v10;
        val[M20] = v20;
        val[M01] = v01;
        val[M11] = v11;
        val[M21] = v21;
        val[M02] = v02;
        val[M12] = v12;
        val[M22] = v22;
        return this;
    }

    public Matrix3 setToRotation(float degrees) {
        return setToRotationRad(MathUtils.degreesToRadians * degrees);
    }

    public Matrix3 setToRotationRad(float radians) {
        float cos = (float) Math.cos((double) radians);
        float sin = (float) Math.sin((double) radians);
        float[] val = this.val;
        val[M00] = cos;
        val[M10] = sin;
        val[M20] = 0.0f;
        val[M01] = -sin;
        val[M11] = cos;
        val[M21] = 0.0f;
        val[M02] = 0.0f;
        val[M12] = 0.0f;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 setToRotation(Vector3 axis, float degrees) {
        return setToRotation(axis, MathUtils.cosDeg(degrees), MathUtils.sinDeg(degrees));
    }

    public Matrix3 setToRotation(Vector3 axis, float cos, float sin) {
        float[] val = this.val;
        float oc = 1.0f - cos;
        val[M00] = ((axis.x * oc) * axis.x) + cos;
        val[M10] = ((axis.x * oc) * axis.y) - (axis.z * sin);
        val[M20] = ((axis.z * oc) * axis.x) + (axis.y * sin);
        val[M01] = ((axis.x * oc) * axis.y) + (axis.z * sin);
        val[M11] = ((axis.y * oc) * axis.y) + cos;
        val[M21] = ((axis.y * oc) * axis.z) - (axis.x * sin);
        val[M02] = ((axis.z * oc) * axis.x) - (axis.y * sin);
        val[M12] = ((axis.y * oc) * axis.z) + (axis.x * sin);
        val[M22] = ((axis.z * oc) * axis.z) + cos;
        return this;
    }

    public Matrix3 setToTranslation(float x, float y) {
        float[] val = this.val;
        val[M00] = 1.0f;
        val[M10] = 0.0f;
        val[M20] = 0.0f;
        val[M01] = 0.0f;
        val[M11] = 1.0f;
        val[M21] = 0.0f;
        val[M02] = x;
        val[M12] = y;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 setToTranslation(Vector2 translation) {
        float[] val = this.val;
        val[M00] = 1.0f;
        val[M10] = 0.0f;
        val[M20] = 0.0f;
        val[M01] = 0.0f;
        val[M11] = 1.0f;
        val[M21] = 0.0f;
        val[M02] = translation.x;
        val[M12] = translation.y;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 setToScaling(float scaleX, float scaleY) {
        float[] val = this.val;
        val[M00] = scaleX;
        val[M10] = 0.0f;
        val[M20] = 0.0f;
        val[M01] = 0.0f;
        val[M11] = scaleY;
        val[M21] = 0.0f;
        val[M02] = 0.0f;
        val[M12] = 0.0f;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 setToScaling(Vector2 scale) {
        float[] val = this.val;
        val[M00] = scale.x;
        val[M10] = 0.0f;
        val[M20] = 0.0f;
        val[M01] = 0.0f;
        val[M11] = scale.y;
        val[M21] = 0.0f;
        val[M02] = 0.0f;
        val[M12] = 0.0f;
        val[M22] = 1.0f;
        return this;
    }

    public String toString() {
        float[] val = this.val;
        return "[" + val[M00] + "|" + val[M01] + "|" + val[M02] + "]\n" + "[" + val[M10] + "|" + val[M11] + "|" + val[M12] + "]\n" + "[" + val[M20] + "|" + val[M21] + "|" + val[M22] + "]";
    }

    public float det() {
        float[] val = this.val;
        return ((((((val[M00] * val[M11]) * val[M22]) + ((val[M01] * val[M12]) * val[M20])) + ((val[M02] * val[M10]) * val[M21])) - ((val[M00] * val[M12]) * val[M21])) - ((val[M01] * val[M10]) * val[M22])) - ((val[M02] * val[M11]) * val[M20]);
    }

    public Matrix3 inv() {
        float det = det();
        if (det == 0.0f) {
            throw new GdxRuntimeException("Can't invert a singular matrix");
        }
        float inv_det = 1.0f / det;
        float[] tmp = this.tmp;
        float[] val = this.val;
        tmp[M00] = (val[M11] * val[M22]) - (val[M21] * val[M12]);
        tmp[M10] = (val[M20] * val[M12]) - (val[M10] * val[M22]);
        tmp[M20] = (val[M10] * val[M21]) - (val[M20] * val[M11]);
        tmp[M01] = (val[M21] * val[M02]) - (val[M01] * val[M22]);
        tmp[M11] = (val[M00] * val[M22]) - (val[M20] * val[M02]);
        tmp[M21] = (val[M20] * val[M01]) - (val[M00] * val[M21]);
        tmp[M02] = (val[M01] * val[M12]) - (val[M11] * val[M02]);
        tmp[M12] = (val[M10] * val[M02]) - (val[M00] * val[M12]);
        tmp[M22] = (val[M00] * val[M11]) - (val[M10] * val[M01]);
        val[M00] = tmp[M00] * inv_det;
        val[M10] = tmp[M10] * inv_det;
        val[M20] = tmp[M20] * inv_det;
        val[M01] = tmp[M01] * inv_det;
        val[M11] = tmp[M11] * inv_det;
        val[M21] = tmp[M21] * inv_det;
        val[M02] = tmp[M02] * inv_det;
        val[M12] = tmp[M12] * inv_det;
        val[M22] = tmp[M22] * inv_det;
        return this;
    }

    public Matrix3 set(Matrix3 mat) {
        System.arraycopy(mat.val, M00, this.val, M00, this.val.length);
        return this;
    }

    public Matrix3 set(Affine2 affine) {
        float[] val = this.val;
        val[M00] = affine.m00;
        val[M10] = affine.m10;
        val[M20] = 0.0f;
        val[M01] = affine.m01;
        val[M11] = affine.m11;
        val[M21] = 0.0f;
        val[M02] = affine.m02;
        val[M12] = affine.m12;
        val[M22] = 1.0f;
        return this;
    }

    public Matrix3 set(Matrix4 mat) {
        float[] val = this.val;
        val[M00] = mat.val[M00];
        val[M10] = mat.val[M10];
        val[M20] = mat.val[M20];
        val[M01] = mat.val[M11];
        val[M11] = mat.val[M21];
        val[M21] = mat.val[M02];
        val[M02] = mat.val[M22];
        val[M12] = mat.val[9];
        val[M22] = mat.val[10];
        return this;
    }

    public Matrix3 set(float[] values) {
        System.arraycopy(values, M00, this.val, M00, this.val.length);
        return this;
    }

    public Matrix3 trn(Vector2 vector) {
        float[] fArr = this.val;
        fArr[M02] = fArr[M02] + vector.x;
        fArr = this.val;
        fArr[M12] = fArr[M12] + vector.y;
        return this;
    }

    public Matrix3 trn(float x, float y) {
        float[] fArr = this.val;
        fArr[M02] = fArr[M02] + x;
        fArr = this.val;
        fArr[M12] = fArr[M12] + y;
        return this;
    }

    public Matrix3 trn(Vector3 vector) {
        float[] fArr = this.val;
        fArr[M02] = fArr[M02] + vector.x;
        fArr = this.val;
        fArr[M12] = fArr[M12] + vector.y;
        return this;
    }

    public Matrix3 translate(float x, float y) {
        float[] val = this.val;
        this.tmp[M00] = 1.0f;
        this.tmp[M10] = 0.0f;
        this.tmp[M20] = 0.0f;
        this.tmp[M01] = 0.0f;
        this.tmp[M11] = 1.0f;
        this.tmp[M21] = 0.0f;
        this.tmp[M02] = x;
        this.tmp[M12] = y;
        this.tmp[M22] = 1.0f;
        mul(val, this.tmp);
        return this;
    }

    public Matrix3 translate(Vector2 translation) {
        float[] val = this.val;
        this.tmp[M00] = 1.0f;
        this.tmp[M10] = 0.0f;
        this.tmp[M20] = 0.0f;
        this.tmp[M01] = 0.0f;
        this.tmp[M11] = 1.0f;
        this.tmp[M21] = 0.0f;
        this.tmp[M02] = translation.x;
        this.tmp[M12] = translation.y;
        this.tmp[M22] = 1.0f;
        mul(val, this.tmp);
        return this;
    }

    public Matrix3 rotate(float degrees) {
        return rotateRad(MathUtils.degreesToRadians * degrees);
    }

    public Matrix3 rotateRad(float radians) {
        if (radians != 0.0f) {
            float cos = (float) Math.cos((double) radians);
            float sin = (float) Math.sin((double) radians);
            float[] tmp = this.tmp;
            tmp[M00] = cos;
            tmp[M10] = sin;
            tmp[M20] = 0.0f;
            tmp[M01] = -sin;
            tmp[M11] = cos;
            tmp[M21] = 0.0f;
            tmp[M02] = 0.0f;
            tmp[M12] = 0.0f;
            tmp[M22] = 1.0f;
            mul(this.val, tmp);
        }
        return this;
    }

    public Matrix3 scale(float scaleX, float scaleY) {
        float[] tmp = this.tmp;
        tmp[M00] = scaleX;
        tmp[M10] = 0.0f;
        tmp[M20] = 0.0f;
        tmp[M01] = 0.0f;
        tmp[M11] = scaleY;
        tmp[M21] = 0.0f;
        tmp[M02] = 0.0f;
        tmp[M12] = 0.0f;
        tmp[M22] = 1.0f;
        mul(this.val, tmp);
        return this;
    }

    public Matrix3 scale(Vector2 scale) {
        float[] tmp = this.tmp;
        tmp[M00] = scale.x;
        tmp[M10] = 0.0f;
        tmp[M20] = 0.0f;
        tmp[M01] = 0.0f;
        tmp[M11] = scale.y;
        tmp[M21] = 0.0f;
        tmp[M02] = 0.0f;
        tmp[M12] = 0.0f;
        tmp[M22] = 1.0f;
        mul(this.val, tmp);
        return this;
    }

    public float[] getValues() {
        return this.val;
    }

    public Vector2 getTranslation(Vector2 position) {
        position.x = this.val[M02];
        position.y = this.val[M12];
        return position;
    }

    public Vector2 getScale(Vector2 scale) {
        float[] val = this.val;
        scale.x = (float) Math.sqrt((double) ((val[M00] * val[M00]) + (val[M01] * val[M01])));
        scale.y = (float) Math.sqrt((double) ((val[M10] * val[M10]) + (val[M11] * val[M11])));
        return scale;
    }

    public float getRotation() {
        return MathUtils.radiansToDegrees * ((float) Math.atan2((double) this.val[M10], (double) this.val[M00]));
    }

    public float getRotationRad() {
        return (float) Math.atan2((double) this.val[M10], (double) this.val[M00]);
    }

    public Matrix3 scl(float scale) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * scale;
        fArr = this.val;
        fArr[M11] = fArr[M11] * scale;
        return this;
    }

    public Matrix3 scl(Vector2 scale) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * scale.x;
        fArr = this.val;
        fArr[M11] = fArr[M11] * scale.y;
        return this;
    }

    public Matrix3 scl(Vector3 scale) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * scale.x;
        fArr = this.val;
        fArr[M11] = fArr[M11] * scale.y;
        return this;
    }

    public Matrix3 transpose() {
        float[] val = this.val;
        float v01 = val[M10];
        float v02 = val[M20];
        float v10 = val[M01];
        float v12 = val[M21];
        float v20 = val[M02];
        float v21 = val[M12];
        val[M01] = v01;
        val[M02] = v02;
        val[M10] = v10;
        val[M12] = v12;
        val[M20] = v20;
        val[M21] = v21;
        return this;
    }

    private static void mul(float[] mata, float[] matb) {
        float v01 = ((mata[M00] * matb[M01]) + (mata[M01] * matb[M11])) + (mata[M02] * matb[M21]);
        float v02 = ((mata[M00] * matb[M02]) + (mata[M01] * matb[M12])) + (mata[M02] * matb[M22]);
        float v10 = ((mata[M10] * matb[M00]) + (mata[M11] * matb[M10])) + (mata[M12] * matb[M20]);
        float v11 = ((mata[M10] * matb[M01]) + (mata[M11] * matb[M11])) + (mata[M12] * matb[M21]);
        float v12 = ((mata[M10] * matb[M02]) + (mata[M11] * matb[M12])) + (mata[M12] * matb[M22]);
        float v20 = ((mata[M20] * matb[M00]) + (mata[M21] * matb[M10])) + (mata[M22] * matb[M20]);
        float v21 = ((mata[M20] * matb[M01]) + (mata[M21] * matb[M11])) + (mata[M22] * matb[M21]);
        float v22 = ((mata[M20] * matb[M02]) + (mata[M21] * matb[M12])) + (mata[M22] * matb[M22]);
        mata[M00] = ((mata[M00] * matb[M00]) + (mata[M01] * matb[M10])) + (mata[M02] * matb[M20]);
        mata[M10] = v10;
        mata[M20] = v20;
        mata[M01] = v01;
        mata[M11] = v11;
        mata[M21] = v21;
        mata[M02] = v02;
        mata[M12] = v12;
        mata[M22] = v22;
    }
}
