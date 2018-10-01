package com.badlogic.gdx.math;

import java.io.Serializable;

public class Matrix4 implements Serializable {
    public static final int M00 = 0;
    public static final int M01 = 4;
    public static final int M02 = 8;
    public static final int M03 = 12;
    public static final int M10 = 1;
    public static final int M11 = 5;
    public static final int M12 = 9;
    public static final int M13 = 13;
    public static final int M20 = 2;
    public static final int M21 = 6;
    public static final int M22 = 10;
    public static final int M23 = 14;
    public static final int M30 = 3;
    public static final int M31 = 7;
    public static final int M32 = 11;
    public static final int M33 = 15;
    static final Vector3 l_vex = new Vector3();
    static final Vector3 l_vey = new Vector3();
    static final Vector3 l_vez = new Vector3();
    static Quaternion quat = new Quaternion();
    static Quaternion quat2 = new Quaternion();
    static final Vector3 right = new Vector3();
    private static final long serialVersionUID = -2717655254359579617L;
    private static final float[] tmp = new float[16];
    static final Vector3 tmpForward = new Vector3();
    static final Matrix4 tmpMat = new Matrix4();
    static final Vector3 tmpUp = new Vector3();
    static final Vector3 tmpVec = new Vector3();
    public final float[] val;

    public static native float det(float[] fArr);

    public static native boolean inv(float[] fArr);

    public static native void mul(float[] fArr, float[] fArr2);

    public static native void mulVec(float[] fArr, float[] fArr2);

    public static native void mulVec(float[] fArr, float[] fArr2, int i, int i2, int i3);

    public static native void prj(float[] fArr, float[] fArr2);

    public static native void prj(float[] fArr, float[] fArr2, int i, int i2, int i3);

    public static native void rot(float[] fArr, float[] fArr2);

    public static native void rot(float[] fArr, float[] fArr2, int i, int i2, int i3);

    public Matrix4() {
        this.val = new float[16];
        this.val[M00] = 1.0f;
        this.val[M11] = 1.0f;
        this.val[M22] = 1.0f;
        this.val[M33] = 1.0f;
    }

    public Matrix4(Matrix4 matrix) {
        this.val = new float[16];
        set(matrix);
    }

    public Matrix4(float[] values) {
        this.val = new float[16];
        set(values);
    }

    public Matrix4(Quaternion quaternion) {
        this.val = new float[16];
        set(quaternion);
    }

    public Matrix4(Vector3 position, Quaternion rotation, Vector3 scale) {
        this.val = new float[16];
        set(position, rotation, scale);
    }

    public Matrix4 set(Matrix4 matrix) {
        return set(matrix.val);
    }

    public Matrix4 set(float[] values) {
        System.arraycopy(values, M00, this.val, M00, this.val.length);
        return this;
    }

    public Matrix4 set(Quaternion quaternion) {
        return set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public Matrix4 set(float quaternionX, float quaternionY, float quaternionZ, float quaternionW) {
        return set(0.0f, 0.0f, 0.0f, quaternionX, quaternionY, quaternionZ, quaternionW);
    }

    public Matrix4 set(Vector3 position, Quaternion orientation) {
        return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w);
    }

    public Matrix4 set(float translationX, float translationY, float translationZ, float quaternionX, float quaternionY, float quaternionZ, float quaternionW) {
        float xs = quaternionX * 2.0f;
        float ys = quaternionY * 2.0f;
        float zs = quaternionZ * 2.0f;
        float wx = quaternionW * xs;
        float wy = quaternionW * ys;
        float wz = quaternionW * zs;
        float xx = quaternionX * xs;
        float xy = quaternionX * ys;
        float xz = quaternionX * zs;
        float yy = quaternionY * ys;
        float yz = quaternionY * zs;
        float zz = quaternionZ * zs;
        this.val[M00] = 1.0f - (yy + zz);
        this.val[M01] = xy - wz;
        this.val[M02] = xz + wy;
        this.val[M03] = translationX;
        this.val[M10] = xy + wz;
        this.val[M11] = 1.0f - (xx + zz);
        this.val[M12] = yz - wx;
        this.val[M13] = translationY;
        this.val[M20] = xz - wy;
        this.val[M21] = yz + wx;
        this.val[M22] = 1.0f - (xx + yy);
        this.val[M23] = translationZ;
        this.val[M30] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M32] = 0.0f;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 set(Vector3 position, Quaternion orientation, Vector3 scale) {
        return set(position.x, position.y, position.z, orientation.x, orientation.y, orientation.z, orientation.w, scale.x, scale.y, scale.z);
    }

    public Matrix4 set(float translationX, float translationY, float translationZ, float quaternionX, float quaternionY, float quaternionZ, float quaternionW, float scaleX, float scaleY, float scaleZ) {
        float xs = quaternionX * 2.0f;
        float ys = quaternionY * 2.0f;
        float zs = quaternionZ * 2.0f;
        float wx = quaternionW * xs;
        float wy = quaternionW * ys;
        float wz = quaternionW * zs;
        float xx = quaternionX * xs;
        float xy = quaternionX * ys;
        float xz = quaternionX * zs;
        float yy = quaternionY * ys;
        float yz = quaternionY * zs;
        float zz = quaternionZ * zs;
        this.val[M00] = (1.0f - (yy + zz)) * scaleX;
        this.val[M01] = (xy - wz) * scaleY;
        this.val[M02] = (xz + wy) * scaleZ;
        this.val[M03] = translationX;
        this.val[M10] = (xy + wz) * scaleX;
        this.val[M11] = (1.0f - (xx + zz)) * scaleY;
        this.val[M12] = (yz - wx) * scaleZ;
        this.val[M13] = translationY;
        this.val[M20] = (xz - wy) * scaleX;
        this.val[M21] = (yz + wx) * scaleY;
        this.val[M22] = (1.0f - (xx + yy)) * scaleZ;
        this.val[M23] = translationZ;
        this.val[M30] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M32] = 0.0f;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 set(Vector3 xAxis, Vector3 yAxis, Vector3 zAxis, Vector3 pos) {
        this.val[M00] = xAxis.x;
        this.val[M01] = xAxis.y;
        this.val[M02] = xAxis.z;
        this.val[M10] = yAxis.x;
        this.val[M11] = yAxis.y;
        this.val[M12] = yAxis.z;
        this.val[M20] = zAxis.x;
        this.val[M21] = zAxis.y;
        this.val[M22] = zAxis.z;
        this.val[M03] = pos.x;
        this.val[M13] = pos.y;
        this.val[M23] = pos.z;
        this.val[M30] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M32] = 0.0f;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 cpy() {
        return new Matrix4(this);
    }

    public Matrix4 trn(Vector3 vector) {
        float[] fArr = this.val;
        fArr[M03] = fArr[M03] + vector.x;
        fArr = this.val;
        fArr[M13] = fArr[M13] + vector.y;
        fArr = this.val;
        fArr[M23] = fArr[M23] + vector.z;
        return this;
    }

    public Matrix4 trn(float x, float y, float z) {
        float[] fArr = this.val;
        fArr[M03] = fArr[M03] + x;
        fArr = this.val;
        fArr[M13] = fArr[M13] + y;
        fArr = this.val;
        fArr[M23] = fArr[M23] + z;
        return this;
    }

    public float[] getValues() {
        return this.val;
    }

    public Matrix4 mul(Matrix4 matrix) {
        mul(this.val, matrix.val);
        return this;
    }

    public Matrix4 mulLeft(Matrix4 matrix) {
        tmpMat.set(matrix);
        mul(tmpMat.val, this.val);
        return set(tmpMat);
    }

    public Matrix4 tra() {
        tmp[M00] = this.val[M00];
        tmp[M01] = this.val[M10];
        tmp[M02] = this.val[M20];
        tmp[M03] = this.val[M30];
        tmp[M10] = this.val[M01];
        tmp[M11] = this.val[M11];
        tmp[M12] = this.val[M21];
        tmp[M13] = this.val[M31];
        tmp[M20] = this.val[M02];
        tmp[M21] = this.val[M12];
        tmp[M22] = this.val[M22];
        tmp[M23] = this.val[M32];
        tmp[M30] = this.val[M03];
        tmp[M31] = this.val[M13];
        tmp[M32] = this.val[M23];
        tmp[M33] = this.val[M33];
        return set(tmp);
    }

    public Matrix4 idt() {
        this.val[M00] = 1.0f;
        this.val[M01] = 0.0f;
        this.val[M02] = 0.0f;
        this.val[M03] = 0.0f;
        this.val[M10] = 0.0f;
        this.val[M11] = 1.0f;
        this.val[M12] = 0.0f;
        this.val[M13] = 0.0f;
        this.val[M20] = 0.0f;
        this.val[M21] = 0.0f;
        this.val[M22] = 1.0f;
        this.val[M23] = 0.0f;
        this.val[M30] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M32] = 0.0f;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 inv() {
        float l_det = (((((((((((((((((((((((((this.val[M30] * this.val[M21]) * this.val[M12]) * this.val[M03]) - (((this.val[M20] * this.val[M31]) * this.val[M12]) * this.val[M03])) - (((this.val[M30] * this.val[M11]) * this.val[M22]) * this.val[M03])) + (((this.val[M10] * this.val[M31]) * this.val[M22]) * this.val[M03])) + (((this.val[M20] * this.val[M11]) * this.val[M32]) * this.val[M03])) - (((this.val[M10] * this.val[M21]) * this.val[M32]) * this.val[M03])) - (((this.val[M30] * this.val[M21]) * this.val[M02]) * this.val[M13])) + (((this.val[M20] * this.val[M31]) * this.val[M02]) * this.val[M13])) + (((this.val[M30] * this.val[M01]) * this.val[M22]) * this.val[M13])) - (((this.val[M00] * this.val[M31]) * this.val[M22]) * this.val[M13])) - (((this.val[M20] * this.val[M01]) * this.val[M32]) * this.val[M13])) + (((this.val[M00] * this.val[M21]) * this.val[M32]) * this.val[M13])) + (((this.val[M30] * this.val[M11]) * this.val[M02]) * this.val[M23])) - (((this.val[M10] * this.val[M31]) * this.val[M02]) * this.val[M23])) - (((this.val[M30] * this.val[M01]) * this.val[M12]) * this.val[M23])) + (((this.val[M00] * this.val[M31]) * this.val[M12]) * this.val[M23])) + (((this.val[M10] * this.val[M01]) * this.val[M32]) * this.val[M23])) - (((this.val[M00] * this.val[M11]) * this.val[M32]) * this.val[M23])) - (((this.val[M20] * this.val[M11]) * this.val[M02]) * this.val[M33])) + (((this.val[M10] * this.val[M21]) * this.val[M02]) * this.val[M33])) + (((this.val[M20] * this.val[M01]) * this.val[M12]) * this.val[M33])) - (((this.val[M00] * this.val[M21]) * this.val[M12]) * this.val[M33])) - (((this.val[M10] * this.val[M01]) * this.val[M22]) * this.val[M33])) + (((this.val[M00] * this.val[M11]) * this.val[M22]) * this.val[M33]);
        if (l_det == 0.0f) {
            throw new RuntimeException("non-invertible matrix");
        }
        float inv_det = 1.0f / l_det;
        tmp[M00] = ((((((this.val[M12] * this.val[M23]) * this.val[M31]) - ((this.val[M13] * this.val[M22]) * this.val[M31])) + ((this.val[M13] * this.val[M21]) * this.val[M32])) - ((this.val[M11] * this.val[M23]) * this.val[M32])) - ((this.val[M12] * this.val[M21]) * this.val[M33])) + ((this.val[M11] * this.val[M22]) * this.val[M33]);
        tmp[M01] = ((((((this.val[M03] * this.val[M22]) * this.val[M31]) - ((this.val[M02] * this.val[M23]) * this.val[M31])) - ((this.val[M03] * this.val[M21]) * this.val[M32])) + ((this.val[M01] * this.val[M23]) * this.val[M32])) + ((this.val[M02] * this.val[M21]) * this.val[M33])) - ((this.val[M01] * this.val[M22]) * this.val[M33]);
        tmp[M02] = ((((((this.val[M02] * this.val[M13]) * this.val[M31]) - ((this.val[M03] * this.val[M12]) * this.val[M31])) + ((this.val[M03] * this.val[M11]) * this.val[M32])) - ((this.val[M01] * this.val[M13]) * this.val[M32])) - ((this.val[M02] * this.val[M11]) * this.val[M33])) + ((this.val[M01] * this.val[M12]) * this.val[M33]);
        tmp[M03] = ((((((this.val[M03] * this.val[M12]) * this.val[M21]) - ((this.val[M02] * this.val[M13]) * this.val[M21])) - ((this.val[M03] * this.val[M11]) * this.val[M22])) + ((this.val[M01] * this.val[M13]) * this.val[M22])) + ((this.val[M02] * this.val[M11]) * this.val[M23])) - ((this.val[M01] * this.val[M12]) * this.val[M23]);
        tmp[M10] = ((((((this.val[M13] * this.val[M22]) * this.val[M30]) - ((this.val[M12] * this.val[M23]) * this.val[M30])) - ((this.val[M13] * this.val[M20]) * this.val[M32])) + ((this.val[M10] * this.val[M23]) * this.val[M32])) + ((this.val[M12] * this.val[M20]) * this.val[M33])) - ((this.val[M10] * this.val[M22]) * this.val[M33]);
        tmp[M11] = ((((((this.val[M02] * this.val[M23]) * this.val[M30]) - ((this.val[M03] * this.val[M22]) * this.val[M30])) + ((this.val[M03] * this.val[M20]) * this.val[M32])) - ((this.val[M00] * this.val[M23]) * this.val[M32])) - ((this.val[M02] * this.val[M20]) * this.val[M33])) + ((this.val[M00] * this.val[M22]) * this.val[M33]);
        tmp[M12] = ((((((this.val[M03] * this.val[M12]) * this.val[M30]) - ((this.val[M02] * this.val[M13]) * this.val[M30])) - ((this.val[M03] * this.val[M10]) * this.val[M32])) + ((this.val[M00] * this.val[M13]) * this.val[M32])) + ((this.val[M02] * this.val[M10]) * this.val[M33])) - ((this.val[M00] * this.val[M12]) * this.val[M33]);
        tmp[M13] = ((((((this.val[M02] * this.val[M13]) * this.val[M20]) - ((this.val[M03] * this.val[M12]) * this.val[M20])) + ((this.val[M03] * this.val[M10]) * this.val[M22])) - ((this.val[M00] * this.val[M13]) * this.val[M22])) - ((this.val[M02] * this.val[M10]) * this.val[M23])) + ((this.val[M00] * this.val[M12]) * this.val[M23]);
        tmp[M20] = ((((((this.val[M11] * this.val[M23]) * this.val[M30]) - ((this.val[M13] * this.val[M21]) * this.val[M30])) + ((this.val[M13] * this.val[M20]) * this.val[M31])) - ((this.val[M10] * this.val[M23]) * this.val[M31])) - ((this.val[M11] * this.val[M20]) * this.val[M33])) + ((this.val[M10] * this.val[M21]) * this.val[M33]);
        tmp[M21] = ((((((this.val[M03] * this.val[M21]) * this.val[M30]) - ((this.val[M01] * this.val[M23]) * this.val[M30])) - ((this.val[M03] * this.val[M20]) * this.val[M31])) + ((this.val[M00] * this.val[M23]) * this.val[M31])) + ((this.val[M01] * this.val[M20]) * this.val[M33])) - ((this.val[M00] * this.val[M21]) * this.val[M33]);
        tmp[M22] = ((((((this.val[M01] * this.val[M13]) * this.val[M30]) - ((this.val[M03] * this.val[M11]) * this.val[M30])) + ((this.val[M03] * this.val[M10]) * this.val[M31])) - ((this.val[M00] * this.val[M13]) * this.val[M31])) - ((this.val[M01] * this.val[M10]) * this.val[M33])) + ((this.val[M00] * this.val[M11]) * this.val[M33]);
        tmp[M23] = ((((((this.val[M03] * this.val[M11]) * this.val[M20]) - ((this.val[M01] * this.val[M13]) * this.val[M20])) - ((this.val[M03] * this.val[M10]) * this.val[M21])) + ((this.val[M00] * this.val[M13]) * this.val[M21])) + ((this.val[M01] * this.val[M10]) * this.val[M23])) - ((this.val[M00] * this.val[M11]) * this.val[M23]);
        tmp[M30] = ((((((this.val[M12] * this.val[M21]) * this.val[M30]) - ((this.val[M11] * this.val[M22]) * this.val[M30])) - ((this.val[M12] * this.val[M20]) * this.val[M31])) + ((this.val[M10] * this.val[M22]) * this.val[M31])) + ((this.val[M11] * this.val[M20]) * this.val[M32])) - ((this.val[M10] * this.val[M21]) * this.val[M32]);
        tmp[M31] = ((((((this.val[M01] * this.val[M22]) * this.val[M30]) - ((this.val[M02] * this.val[M21]) * this.val[M30])) + ((this.val[M02] * this.val[M20]) * this.val[M31])) - ((this.val[M00] * this.val[M22]) * this.val[M31])) - ((this.val[M01] * this.val[M20]) * this.val[M32])) + ((this.val[M00] * this.val[M21]) * this.val[M32]);
        tmp[M32] = ((((((this.val[M02] * this.val[M11]) * this.val[M30]) - ((this.val[M01] * this.val[M12]) * this.val[M30])) - ((this.val[M02] * this.val[M10]) * this.val[M31])) + ((this.val[M00] * this.val[M12]) * this.val[M31])) + ((this.val[M01] * this.val[M10]) * this.val[M32])) - ((this.val[M00] * this.val[M11]) * this.val[M32]);
        tmp[M33] = ((((((this.val[M01] * this.val[M12]) * this.val[M20]) - ((this.val[M02] * this.val[M11]) * this.val[M20])) + ((this.val[M02] * this.val[M10]) * this.val[M21])) - ((this.val[M00] * this.val[M12]) * this.val[M21])) - ((this.val[M01] * this.val[M10]) * this.val[M22])) + ((this.val[M00] * this.val[M11]) * this.val[M22]);
        this.val[M00] = tmp[M00] * inv_det;
        this.val[M01] = tmp[M01] * inv_det;
        this.val[M02] = tmp[M02] * inv_det;
        this.val[M03] = tmp[M03] * inv_det;
        this.val[M10] = tmp[M10] * inv_det;
        this.val[M11] = tmp[M11] * inv_det;
        this.val[M12] = tmp[M12] * inv_det;
        this.val[M13] = tmp[M13] * inv_det;
        this.val[M20] = tmp[M20] * inv_det;
        this.val[M21] = tmp[M21] * inv_det;
        this.val[M22] = tmp[M22] * inv_det;
        this.val[M23] = tmp[M23] * inv_det;
        this.val[M30] = tmp[M30] * inv_det;
        this.val[M31] = tmp[M31] * inv_det;
        this.val[M32] = tmp[M32] * inv_det;
        this.val[M33] = tmp[M33] * inv_det;
        return this;
    }

    public float det() {
        return (((((((((((((((((((((((((this.val[M30] * this.val[M21]) * this.val[M12]) * this.val[M03]) - (((this.val[M20] * this.val[M31]) * this.val[M12]) * this.val[M03])) - (((this.val[M30] * this.val[M11]) * this.val[M22]) * this.val[M03])) + (((this.val[M10] * this.val[M31]) * this.val[M22]) * this.val[M03])) + (((this.val[M20] * this.val[M11]) * this.val[M32]) * this.val[M03])) - (((this.val[M10] * this.val[M21]) * this.val[M32]) * this.val[M03])) - (((this.val[M30] * this.val[M21]) * this.val[M02]) * this.val[M13])) + (((this.val[M20] * this.val[M31]) * this.val[M02]) * this.val[M13])) + (((this.val[M30] * this.val[M01]) * this.val[M22]) * this.val[M13])) - (((this.val[M00] * this.val[M31]) * this.val[M22]) * this.val[M13])) - (((this.val[M20] * this.val[M01]) * this.val[M32]) * this.val[M13])) + (((this.val[M00] * this.val[M21]) * this.val[M32]) * this.val[M13])) + (((this.val[M30] * this.val[M11]) * this.val[M02]) * this.val[M23])) - (((this.val[M10] * this.val[M31]) * this.val[M02]) * this.val[M23])) - (((this.val[M30] * this.val[M01]) * this.val[M12]) * this.val[M23])) + (((this.val[M00] * this.val[M31]) * this.val[M12]) * this.val[M23])) + (((this.val[M10] * this.val[M01]) * this.val[M32]) * this.val[M23])) - (((this.val[M00] * this.val[M11]) * this.val[M32]) * this.val[M23])) - (((this.val[M20] * this.val[M11]) * this.val[M02]) * this.val[M33])) + (((this.val[M10] * this.val[M21]) * this.val[M02]) * this.val[M33])) + (((this.val[M20] * this.val[M01]) * this.val[M12]) * this.val[M33])) - (((this.val[M00] * this.val[M21]) * this.val[M12]) * this.val[M33])) - (((this.val[M10] * this.val[M01]) * this.val[M22]) * this.val[M33])) + (((this.val[M00] * this.val[M11]) * this.val[M22]) * this.val[M33]);
    }

    public float det3x3() {
        return ((((((this.val[M00] * this.val[M11]) * this.val[M22]) + ((this.val[M01] * this.val[M12]) * this.val[M20])) + ((this.val[M02] * this.val[M10]) * this.val[M21])) - ((this.val[M00] * this.val[M12]) * this.val[M21])) - ((this.val[M01] * this.val[M10]) * this.val[M22])) - ((this.val[M02] * this.val[M11]) * this.val[M20]);
    }

    public Matrix4 setToProjection(float near, float far, float fovy, float aspectRatio) {
        idt();
        float l_fd = (float) (1.0d / Math.tan((((double) fovy) * 0.017453292519943295d) / 2.0d));
        float l_a1 = (far + near) / (near - far);
        float l_a2 = ((2.0f * far) * near) / (near - far);
        this.val[M00] = l_fd / aspectRatio;
        this.val[M10] = 0.0f;
        this.val[M20] = 0.0f;
        this.val[M30] = 0.0f;
        this.val[M01] = 0.0f;
        this.val[M11] = l_fd;
        this.val[M21] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M02] = 0.0f;
        this.val[M12] = 0.0f;
        this.val[M22] = l_a1;
        this.val[M32] = -1.0f;
        this.val[M03] = 0.0f;
        this.val[M13] = 0.0f;
        this.val[M23] = l_a2;
        this.val[M33] = 0.0f;
        return this;
    }

    public Matrix4 setToProjection(float left, float right, float bottom, float top, float near, float far) {
        float y = (2.0f * near) / (top - bottom);
        float a = (right + left) / (right - left);
        float b = (top + bottom) / (top - bottom);
        float l_a1 = (far + near) / (near - far);
        float l_a2 = ((2.0f * far) * near) / (near - far);
        this.val[M00] = (2.0f * near) / (right - left);
        this.val[M10] = 0.0f;
        this.val[M20] = 0.0f;
        this.val[M30] = 0.0f;
        this.val[M01] = 0.0f;
        this.val[M11] = y;
        this.val[M21] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M02] = a;
        this.val[M12] = b;
        this.val[M22] = l_a1;
        this.val[M32] = -1.0f;
        this.val[M03] = 0.0f;
        this.val[M13] = 0.0f;
        this.val[M23] = l_a2;
        this.val[M33] = 0.0f;
        return this;
    }

    public Matrix4 setToOrtho2D(float x, float y, float width, float height) {
        setToOrtho(x, x + width, y, y + height, 0.0f, 1.0f);
        return this;
    }

    public Matrix4 setToOrtho2D(float x, float y, float width, float height, float near, float far) {
        setToOrtho(x, x + width, y, y + height, near, far);
        return this;
    }

    public Matrix4 setToOrtho(float left, float right, float bottom, float top, float near, float far) {
        idt();
        float y_orth = 2.0f / (top - bottom);
        float z_orth = -2.0f / (far - near);
        float tx = (-(right + left)) / (right - left);
        float ty = (-(top + bottom)) / (top - bottom);
        float tz = (-(far + near)) / (far - near);
        this.val[M00] = 2.0f / (right - left);
        this.val[M10] = 0.0f;
        this.val[M20] = 0.0f;
        this.val[M30] = 0.0f;
        this.val[M01] = 0.0f;
        this.val[M11] = y_orth;
        this.val[M21] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M02] = 0.0f;
        this.val[M12] = 0.0f;
        this.val[M22] = z_orth;
        this.val[M32] = 0.0f;
        this.val[M03] = tx;
        this.val[M13] = ty;
        this.val[M23] = tz;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 setTranslation(Vector3 vector) {
        this.val[M03] = vector.x;
        this.val[M13] = vector.y;
        this.val[M23] = vector.z;
        return this;
    }

    public Matrix4 setTranslation(float x, float y, float z) {
        this.val[M03] = x;
        this.val[M13] = y;
        this.val[M23] = z;
        return this;
    }

    public Matrix4 setToTranslation(Vector3 vector) {
        idt();
        this.val[M03] = vector.x;
        this.val[M13] = vector.y;
        this.val[M23] = vector.z;
        return this;
    }

    public Matrix4 setToTranslation(float x, float y, float z) {
        idt();
        this.val[M03] = x;
        this.val[M13] = y;
        this.val[M23] = z;
        return this;
    }

    public Matrix4 setToTranslationAndScaling(Vector3 translation, Vector3 scaling) {
        idt();
        this.val[M03] = translation.x;
        this.val[M13] = translation.y;
        this.val[M23] = translation.z;
        this.val[M00] = scaling.x;
        this.val[M11] = scaling.y;
        this.val[M22] = scaling.z;
        return this;
    }

    public Matrix4 setToTranslationAndScaling(float translationX, float translationY, float translationZ, float scalingX, float scalingY, float scalingZ) {
        idt();
        this.val[M03] = translationX;
        this.val[M13] = translationY;
        this.val[M23] = translationZ;
        this.val[M00] = scalingX;
        this.val[M11] = scalingY;
        this.val[M22] = scalingZ;
        return this;
    }

    public Matrix4 setToRotation(Vector3 axis, float degrees) {
        if (degrees != 0.0f) {
            return set(quat.set(axis, degrees));
        }
        idt();
        return this;
    }

    public Matrix4 setToRotationRad(Vector3 axis, float radians) {
        if (radians != 0.0f) {
            return set(quat.setFromAxisRad(axis, radians));
        }
        idt();
        return this;
    }

    public Matrix4 setToRotation(float axisX, float axisY, float axisZ, float degrees) {
        if (degrees != 0.0f) {
            return set(quat.setFromAxis(axisX, axisY, axisZ, degrees));
        }
        idt();
        return this;
    }

    public Matrix4 setToRotationRad(float axisX, float axisY, float axisZ, float radians) {
        if (radians != 0.0f) {
            return set(quat.setFromAxisRad(axisX, axisY, axisZ, radians));
        }
        idt();
        return this;
    }

    public Matrix4 setToRotation(Vector3 v1, Vector3 v2) {
        return set(quat.setFromCross(v1, v2));
    }

    public Matrix4 setToRotation(float x1, float y1, float z1, float x2, float y2, float z2) {
        return set(quat.setFromCross(x1, y1, z1, x2, y2, z2));
    }

    public Matrix4 setFromEulerAngles(float yaw, float pitch, float roll) {
        quat.setEulerAngles(yaw, pitch, roll);
        return set(quat);
    }

    public Matrix4 setFromEulerAnglesRad(float yaw, float pitch, float roll) {
        quat.setEulerAnglesRad(yaw, pitch, roll);
        return set(quat);
    }

    public Matrix4 setToScaling(Vector3 vector) {
        idt();
        this.val[M00] = vector.x;
        this.val[M11] = vector.y;
        this.val[M22] = vector.z;
        return this;
    }

    public Matrix4 setToScaling(float x, float y, float z) {
        idt();
        this.val[M00] = x;
        this.val[M11] = y;
        this.val[M22] = z;
        return this;
    }

    public Matrix4 setToLookAt(Vector3 direction, Vector3 up) {
        l_vez.set(direction).nor();
        l_vex.set(direction).nor();
        l_vex.crs(up).nor();
        l_vey.set(l_vex).crs(l_vez).nor();
        idt();
        this.val[M00] = l_vex.x;
        this.val[M01] = l_vex.y;
        this.val[M02] = l_vex.z;
        this.val[M10] = l_vey.x;
        this.val[M11] = l_vey.y;
        this.val[M12] = l_vey.z;
        this.val[M20] = -l_vez.x;
        this.val[M21] = -l_vez.y;
        this.val[M22] = -l_vez.z;
        return this;
    }

    public Matrix4 setToLookAt(Vector3 position, Vector3 target, Vector3 up) {
        tmpVec.set(target).sub(position);
        setToLookAt(tmpVec, up);
        mul(tmpMat.setToTranslation(-position.x, -position.y, -position.z));
        return this;
    }

    public Matrix4 setToWorld(Vector3 position, Vector3 forward, Vector3 up) {
        tmpForward.set(forward).nor();
        right.set(tmpForward).crs(up).nor();
        tmpUp.set(right).crs(tmpForward).nor();
        set(right, tmpUp, tmpForward.scl(-1.0f), position);
        return this;
    }

    public String toString() {
        return "[" + this.val[M00] + "|" + this.val[M01] + "|" + this.val[M02] + "|" + this.val[M03] + "]\n" + "[" + this.val[M10] + "|" + this.val[M11] + "|" + this.val[M12] + "|" + this.val[M13] + "]\n" + "[" + this.val[M20] + "|" + this.val[M21] + "|" + this.val[M22] + "|" + this.val[M23] + "]\n" + "[" + this.val[M30] + "|" + this.val[M31] + "|" + this.val[M32] + "|" + this.val[M33] + "]\n";
    }

    public Matrix4 lerp(Matrix4 matrix, float alpha) {
        for (int i = M00; i < 16; i += M10) {
            this.val[i] = (this.val[i] * (1.0f - alpha)) + (matrix.val[i] * alpha);
        }
        return this;
    }

    public Matrix4 avg(Matrix4 other, float w) {
        getScale(tmpVec);
        other.getScale(tmpForward);
        getRotation(quat);
        other.getRotation(quat2);
        getTranslation(tmpUp);
        other.getTranslation(right);
        setToScaling(tmpVec.scl(w).add(tmpForward.scl(1.0f - w)));
        rotate(quat.slerp(quat2, 1.0f - w));
        setTranslation(tmpUp.scl(w).add(right.scl(1.0f - w)));
        return this;
    }

    public Matrix4 avg(Matrix4[] t) {
        float w = 1.0f / ((float) t.length);
        tmpVec.set(t[M00].getScale(tmpUp).scl(w));
        quat.set(t[M00].getRotation(quat2).exp(w));
        tmpForward.set(t[M00].getTranslation(tmpUp).scl(w));
        for (int i = M10; i < t.length; i += M10) {
            tmpVec.add(t[i].getScale(tmpUp).scl(w));
            quat.mul(t[i].getRotation(quat2).exp(w));
            tmpForward.add(t[i].getTranslation(tmpUp).scl(w));
        }
        quat.nor();
        setToScaling(tmpVec);
        rotate(quat);
        setTranslation(tmpForward);
        return this;
    }

    public Matrix4 avg(Matrix4[] t, float[] w) {
        tmpVec.set(t[M00].getScale(tmpUp).scl(w[M00]));
        quat.set(t[M00].getRotation(quat2).exp(w[M00]));
        tmpForward.set(t[M00].getTranslation(tmpUp).scl(w[M00]));
        for (int i = M10; i < t.length; i += M10) {
            tmpVec.add(t[i].getScale(tmpUp).scl(w[i]));
            quat.mul(t[i].getRotation(quat2).exp(w[i]));
            tmpForward.add(t[i].getTranslation(tmpUp).scl(w[i]));
        }
        quat.nor();
        setToScaling(tmpVec);
        rotate(quat);
        setTranslation(tmpForward);
        return this;
    }

    public Matrix4 set(Matrix3 mat) {
        this.val[M00] = mat.val[M00];
        this.val[M10] = mat.val[M10];
        this.val[M20] = mat.val[M20];
        this.val[M30] = 0.0f;
        this.val[M01] = mat.val[M30];
        this.val[M11] = mat.val[M01];
        this.val[M21] = mat.val[M11];
        this.val[M31] = 0.0f;
        this.val[M02] = 0.0f;
        this.val[M12] = 0.0f;
        this.val[M22] = 1.0f;
        this.val[M32] = 0.0f;
        this.val[M03] = mat.val[M21];
        this.val[M13] = mat.val[M31];
        this.val[M23] = 0.0f;
        this.val[M33] = mat.val[M02];
        return this;
    }

    public Matrix4 set(Affine2 affine) {
        this.val[M00] = affine.m00;
        this.val[M10] = affine.m10;
        this.val[M20] = 0.0f;
        this.val[M30] = 0.0f;
        this.val[M01] = affine.m01;
        this.val[M11] = affine.m11;
        this.val[M21] = 0.0f;
        this.val[M31] = 0.0f;
        this.val[M02] = 0.0f;
        this.val[M12] = 0.0f;
        this.val[M22] = 1.0f;
        this.val[M32] = 0.0f;
        this.val[M03] = affine.m02;
        this.val[M13] = affine.m12;
        this.val[M23] = 0.0f;
        this.val[M33] = 1.0f;
        return this;
    }

    public Matrix4 setAsAffine(Affine2 affine) {
        this.val[M00] = affine.m00;
        this.val[M10] = affine.m10;
        this.val[M01] = affine.m01;
        this.val[M11] = affine.m11;
        this.val[M03] = affine.m02;
        this.val[M13] = affine.m12;
        return this;
    }

    public Matrix4 setAsAffine(Matrix4 mat) {
        this.val[M00] = mat.val[M00];
        this.val[M10] = mat.val[M10];
        this.val[M01] = mat.val[M01];
        this.val[M11] = mat.val[M11];
        this.val[M03] = mat.val[M03];
        this.val[M13] = mat.val[M13];
        return this;
    }

    public Matrix4 scl(Vector3 scale) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * scale.x;
        fArr = this.val;
        fArr[M11] = fArr[M11] * scale.y;
        fArr = this.val;
        fArr[M22] = fArr[M22] * scale.z;
        return this;
    }

    public Matrix4 scl(float x, float y, float z) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * x;
        fArr = this.val;
        fArr[M11] = fArr[M11] * y;
        fArr = this.val;
        fArr[M22] = fArr[M22] * z;
        return this;
    }

    public Matrix4 scl(float scale) {
        float[] fArr = this.val;
        fArr[M00] = fArr[M00] * scale;
        fArr = this.val;
        fArr[M11] = fArr[M11] * scale;
        fArr = this.val;
        fArr[M22] = fArr[M22] * scale;
        return this;
    }

    public Vector3 getTranslation(Vector3 position) {
        position.x = this.val[M03];
        position.y = this.val[M13];
        position.z = this.val[M23];
        return position;
    }

    public Quaternion getRotation(Quaternion rotation, boolean normalizeAxes) {
        return rotation.setFromMatrix(normalizeAxes, this);
    }

    public Quaternion getRotation(Quaternion rotation) {
        return rotation.setFromMatrix(this);
    }

    public float getScaleXSquared() {
        return ((this.val[M00] * this.val[M00]) + (this.val[M01] * this.val[M01])) + (this.val[M02] * this.val[M02]);
    }

    public float getScaleYSquared() {
        return ((this.val[M10] * this.val[M10]) + (this.val[M11] * this.val[M11])) + (this.val[M12] * this.val[M12]);
    }

    public float getScaleZSquared() {
        return ((this.val[M20] * this.val[M20]) + (this.val[M21] * this.val[M21])) + (this.val[M22] * this.val[M22]);
    }

    public float getScaleX() {
        if (MathUtils.isZero(this.val[M01]) && MathUtils.isZero(this.val[M02])) {
            return Math.abs(this.val[M00]);
        }
        return (float) Math.sqrt((double) getScaleXSquared());
    }

    public float getScaleY() {
        if (MathUtils.isZero(this.val[M10]) && MathUtils.isZero(this.val[M12])) {
            return Math.abs(this.val[M11]);
        }
        return (float) Math.sqrt((double) getScaleYSquared());
    }

    public float getScaleZ() {
        if (MathUtils.isZero(this.val[M20]) && MathUtils.isZero(this.val[M21])) {
            return Math.abs(this.val[M22]);
        }
        return (float) Math.sqrt((double) getScaleZSquared());
    }

    public Vector3 getScale(Vector3 scale) {
        return scale.set(getScaleX(), getScaleY(), getScaleZ());
    }

    public Matrix4 toNormalMatrix() {
        this.val[M03] = 0.0f;
        this.val[M13] = 0.0f;
        this.val[M23] = 0.0f;
        return inv().tra();
    }

    public Matrix4 translate(Vector3 translation) {
        return translate(translation.x, translation.y, translation.z);
    }

    public Matrix4 translate(float x, float y, float z) {
        tmp[M00] = 1.0f;
        tmp[M01] = 0.0f;
        tmp[M02] = 0.0f;
        tmp[M03] = x;
        tmp[M10] = 0.0f;
        tmp[M11] = 1.0f;
        tmp[M12] = 0.0f;
        tmp[M13] = y;
        tmp[M20] = 0.0f;
        tmp[M21] = 0.0f;
        tmp[M22] = 1.0f;
        tmp[M23] = z;
        tmp[M30] = 0.0f;
        tmp[M31] = 0.0f;
        tmp[M32] = 0.0f;
        tmp[M33] = 1.0f;
        mul(this.val, tmp);
        return this;
    }

    public Matrix4 rotate(Vector3 axis, float degrees) {
        if (degrees == 0.0f) {
            return this;
        }
        quat.set(axis, degrees);
        return rotate(quat);
    }

    public Matrix4 rotateRad(Vector3 axis, float radians) {
        if (radians == 0.0f) {
            return this;
        }
        quat.setFromAxisRad(axis, radians);
        return rotate(quat);
    }

    public Matrix4 rotate(float axisX, float axisY, float axisZ, float degrees) {
        if (degrees == 0.0f) {
            return this;
        }
        quat.setFromAxis(axisX, axisY, axisZ, degrees);
        return rotate(quat);
    }

    public Matrix4 rotateRad(float axisX, float axisY, float axisZ, float radians) {
        if (radians == 0.0f) {
            return this;
        }
        quat.setFromAxisRad(axisX, axisY, axisZ, radians);
        return rotate(quat);
    }

    public Matrix4 rotate(Quaternion rotation) {
        rotation.toMatrix(tmp);
        mul(this.val, tmp);
        return this;
    }

    public Matrix4 rotate(Vector3 v1, Vector3 v2) {
        return rotate(quat.setFromCross(v1, v2));
    }

    public Matrix4 scale(float scaleX, float scaleY, float scaleZ) {
        tmp[M00] = scaleX;
        tmp[M01] = 0.0f;
        tmp[M02] = 0.0f;
        tmp[M03] = 0.0f;
        tmp[M10] = 0.0f;
        tmp[M11] = scaleY;
        tmp[M12] = 0.0f;
        tmp[M13] = 0.0f;
        tmp[M20] = 0.0f;
        tmp[M21] = 0.0f;
        tmp[M22] = scaleZ;
        tmp[M23] = 0.0f;
        tmp[M30] = 0.0f;
        tmp[M31] = 0.0f;
        tmp[M32] = 0.0f;
        tmp[M33] = 1.0f;
        mul(this.val, tmp);
        return this;
    }

    public void extract4x3Matrix(float[] dst) {
        dst[M00] = this.val[M00];
        dst[M10] = this.val[M10];
        dst[M20] = this.val[M20];
        dst[M30] = this.val[M01];
        dst[M01] = this.val[M11];
        dst[M11] = this.val[M21];
        dst[M21] = this.val[M02];
        dst[M31] = this.val[M12];
        dst[M02] = this.val[M22];
        dst[M12] = this.val[M03];
        dst[M22] = this.val[M13];
        dst[M32] = this.val[M23];
    }
}
