package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.NumberUtils;
import java.io.Serializable;

public class Quaternion implements Serializable {
    private static final long serialVersionUID = -7661875440774897168L;
    private static Quaternion tmp1 = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    private static Quaternion tmp2 = new Quaternion(0.0f, 0.0f, 0.0f, 0.0f);
    public float w;
    public float x;
    public float y;
    public float z;

    public Quaternion(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    public Quaternion() {
        idt();
    }

    public Quaternion(Quaternion quaternion) {
        set(quaternion);
    }

    public Quaternion(Vector3 axis, float angle) {
        set(axis, angle);
    }

    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    public Quaternion set(Quaternion quaternion) {
        return set(quaternion.x, quaternion.y, quaternion.z, quaternion.w);
    }

    public Quaternion set(Vector3 axis, float angle) {
        return setFromAxis(axis.x, axis.y, axis.z, angle);
    }

    public Quaternion cpy() {
        return new Quaternion(this);
    }

    public static final float len(float x, float y, float z, float w) {
        return (float) Math.sqrt((double) ((((x * x) + (y * y)) + (z * z)) + (w * w)));
    }

    public float len() {
        return (float) Math.sqrt((double) ((((this.x * this.x) + (this.y * this.y)) + (this.z * this.z)) + (this.w * this.w)));
    }

    public String toString() {
        return "[" + this.x + "|" + this.y + "|" + this.z + "|" + this.w + "]";
    }

    public Quaternion setEulerAngles(float yaw, float pitch, float roll) {
        return setEulerAnglesRad(yaw * MathUtils.degreesToRadians, pitch * MathUtils.degreesToRadians, MathUtils.degreesToRadians * roll);
    }

    public Quaternion setEulerAnglesRad(float yaw, float pitch, float roll) {
        float hr = roll * 0.5f;
        float shr = (float) Math.sin((double) hr);
        float chr = (float) Math.cos((double) hr);
        float hp = pitch * 0.5f;
        float shp = (float) Math.sin((double) hp);
        float chp = (float) Math.cos((double) hp);
        float hy = yaw * 0.5f;
        float shy = (float) Math.sin((double) hy);
        float chy = (float) Math.cos((double) hy);
        float chy_shp = chy * shp;
        float shy_chp = shy * chp;
        float chy_chp = chy * chp;
        float shy_shp = shy * shp;
        this.x = (chy_shp * chr) + (shy_chp * shr);
        this.y = (shy_chp * chr) - (chy_shp * shr);
        this.z = (chy_chp * shr) - (shy_shp * chr);
        this.w = (chy_chp * chr) + (shy_shp * shr);
        return this;
    }

    public int getGimbalPole() {
        float t = (this.y * this.x) + (this.z * this.w);
        if (t > 0.499f) {
            return 1;
        }
        return t < -0.499f ? -1 : 0;
    }

    public float getRollRad() {
        int pole = getGimbalPole();
        return pole == 0 ? MathUtils.atan2(((this.w * this.z) + (this.y * this.x)) * 2.0f, 1.0f - (((this.x * this.x) + (this.z * this.z)) * 2.0f)) : (((float) pole) * 2.0f) * MathUtils.atan2(this.y, this.w);
    }

    public float getRoll() {
        return getRollRad() * MathUtils.radiansToDegrees;
    }

    public float getPitchRad() {
        int pole = getGimbalPole();
        return pole == 0 ? (float) Math.asin((double) MathUtils.clamp(2.0f * ((this.w * this.x) - (this.z * this.y)), -1.0f, 1.0f)) : (((float) pole) * MathUtils.PI) * 0.5f;
    }

    public float getPitch() {
        return getPitchRad() * MathUtils.radiansToDegrees;
    }

    public float getYawRad() {
        return getGimbalPole() == 0 ? MathUtils.atan2(((this.y * this.w) + (this.x * this.z)) * 2.0f, 1.0f - (((this.y * this.y) + (this.x * this.x)) * 2.0f)) : 0.0f;
    }

    public float getYaw() {
        return getYawRad() * MathUtils.radiansToDegrees;
    }

    public static final float len2(float x, float y, float z, float w) {
        return (((x * x) + (y * y)) + (z * z)) + (w * w);
    }

    public float len2() {
        return (((this.x * this.x) + (this.y * this.y)) + (this.z * this.z)) + (this.w * this.w);
    }

    public Quaternion nor() {
        float len = len2();
        if (!(len == 0.0f || MathUtils.isEqual(len, 1.0f))) {
            len = (float) Math.sqrt((double) len);
            this.w /= len;
            this.x /= len;
            this.y /= len;
            this.z /= len;
        }
        return this;
    }

    public Quaternion conjugate() {
        this.x = -this.x;
        this.y = -this.y;
        this.z = -this.z;
        return this;
    }

    public Vector3 transform(Vector3 v) {
        tmp2.set(this);
        tmp2.conjugate();
        tmp2.mulLeft(tmp1.set(v.x, v.y, v.z, 0.0f)).mulLeft(this);
        v.x = tmp2.x;
        v.y = tmp2.y;
        v.z = tmp2.z;
        return v;
    }

    public Quaternion mul(Quaternion other) {
        float newY = (((this.w * other.y) + (this.y * other.w)) + (this.z * other.x)) - (this.x * other.z);
        float newZ = (((this.w * other.z) + (this.z * other.w)) + (this.x * other.y)) - (this.y * other.x);
        float newW = (((this.w * other.w) - (this.x * other.x)) - (this.y * other.y)) - (this.z * other.z);
        this.x = (((this.w * other.x) + (this.x * other.w)) + (this.y * other.z)) - (this.z * other.y);
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion mul(float x, float y, float z, float w) {
        float newY = (((this.w * y) + (this.y * w)) + (this.z * x)) - (this.x * z);
        float newZ = (((this.w * z) + (this.z * w)) + (this.x * y)) - (this.y * x);
        float newW = (((this.w * w) - (this.x * x)) - (this.y * y)) - (this.z * z);
        this.x = (((this.w * x) + (this.x * w)) + (this.y * z)) - (this.z * y);
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion mulLeft(Quaternion other) {
        float newY = (((other.w * this.y) + (other.y * this.w)) + (other.z * this.x)) - (other.x * this.z);
        float newZ = (((other.w * this.z) + (other.z * this.w)) + (other.x * this.y)) - (other.y * this.x);
        float newW = (((other.w * this.w) - (other.x * this.x)) - (other.y * this.y)) - (other.z * this.z);
        this.x = (((other.w * this.x) + (other.x * this.w)) + (other.y * this.z)) - (other.z * this.y);
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion mulLeft(float x, float y, float z, float w) {
        float newY = (((this.y * w) + (this.w * y)) + (this.x * z)) - (x * z);
        float newZ = (((this.z * w) + (this.w * z)) + (this.y * x)) - (y * x);
        float newW = (((this.w * w) - (this.x * x)) - (this.y * y)) - (z * z);
        this.x = (((this.x * w) + (this.w * x)) + (this.z * y)) - (z * y);
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion add(Quaternion quaternion) {
        this.x += quaternion.x;
        this.y += quaternion.y;
        this.z += quaternion.z;
        this.w += quaternion.w;
        return this;
    }

    public Quaternion add(float qx, float qy, float qz, float qw) {
        this.x += qx;
        this.y += qy;
        this.z += qz;
        this.w += qw;
        return this;
    }

    public void toMatrix(float[] matrix) {
        float xx = this.x * this.x;
        float xy = this.x * this.y;
        float xz = this.x * this.z;
        float xw = this.x * this.w;
        float yy = this.y * this.y;
        float yz = this.y * this.z;
        float yw = this.y * this.w;
        float zz = this.z * this.z;
        float zw = this.z * this.w;
        matrix[0] = 1.0f - ((yy + zz) * 2.0f);
        matrix[4] = (xy - zw) * 2.0f;
        matrix[8] = (xz + yw) * 2.0f;
        matrix[12] = 0.0f;
        matrix[1] = (xy + zw) * 2.0f;
        matrix[5] = 1.0f - ((xx + zz) * 2.0f);
        matrix[9] = (yz - xw) * 2.0f;
        matrix[13] = 0.0f;
        matrix[2] = (xz - yw) * 2.0f;
        matrix[6] = (yz + xw) * 2.0f;
        matrix[10] = 1.0f - ((xx + yy) * 2.0f);
        matrix[14] = 0.0f;
        matrix[3] = 0.0f;
        matrix[7] = 0.0f;
        matrix[11] = 0.0f;
        matrix[15] = 1.0f;
    }

    public Quaternion idt() {
        return set(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public boolean isIdentity() {
        return MathUtils.isZero(this.x) && MathUtils.isZero(this.y) && MathUtils.isZero(this.z) && MathUtils.isEqual(this.w, 1.0f);
    }

    public boolean isIdentity(float tolerance) {
        return MathUtils.isZero(this.x, tolerance) && MathUtils.isZero(this.y, tolerance) && MathUtils.isZero(this.z, tolerance) && MathUtils.isEqual(this.w, 1.0f, tolerance);
    }

    public Quaternion setFromAxis(Vector3 axis, float degrees) {
        return setFromAxis(axis.x, axis.y, axis.z, degrees);
    }

    public Quaternion setFromAxisRad(Vector3 axis, float radians) {
        return setFromAxisRad(axis.x, axis.y, axis.z, radians);
    }

    public Quaternion setFromAxis(float x, float y, float z, float degrees) {
        return setFromAxisRad(x, y, z, MathUtils.degreesToRadians * degrees);
    }

    public Quaternion setFromAxisRad(float x, float y, float z, float radians) {
        float d = Vector3.len(x, y, z);
        if (d == 0.0f) {
            return idt();
        }
        d = 1.0f / d;
        float l_ang = radians < 0.0f ? MathUtils.PI2 - ((-radians) % MathUtils.PI2) : radians % MathUtils.PI2;
        float l_sin = (float) Math.sin((double) (l_ang / 2.0f));
        return set((d * x) * l_sin, (d * y) * l_sin, (d * z) * l_sin, (float) Math.cos((double) (l_ang / 2.0f))).nor();
    }

    public Quaternion setFromMatrix(boolean normalizeAxes, Matrix4 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[0], matrix.val[4], matrix.val[8], matrix.val[1], matrix.val[5], matrix.val[9], matrix.val[2], matrix.val[6], matrix.val[10]);
    }

    public Quaternion setFromMatrix(Matrix4 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromMatrix(boolean normalizeAxes, Matrix3 matrix) {
        return setFromAxes(normalizeAxes, matrix.val[0], matrix.val[3], matrix.val[6], matrix.val[1], matrix.val[4], matrix.val[7], matrix.val[2], matrix.val[5], matrix.val[8]);
    }

    public Quaternion setFromMatrix(Matrix3 matrix) {
        return setFromMatrix(false, matrix);
    }

    public Quaternion setFromAxes(float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        return setFromAxes(false, xx, xy, xz, yx, yy, yz, zx, zy, zz);
    }

    public Quaternion setFromAxes(boolean normalizeAxes, float xx, float xy, float xz, float yx, float yy, float yz, float zx, float zy, float zz) {
        if (normalizeAxes) {
            float lx = 1.0f / Vector3.len(xx, xy, xz);
            float ly = 1.0f / Vector3.len(yx, yy, yz);
            float lz = 1.0f / Vector3.len(zx, zy, zz);
            xx *= lx;
            xy *= lx;
            xz *= lx;
            yx *= ly;
            yy *= ly;
            yz *= ly;
            zx *= lz;
            zy *= lz;
            zz *= lz;
        }
        float t = (xx + yy) + zz;
        float s;
        if (t >= 0.0f) {
            s = (float) Math.sqrt((double) (1.0f + t));
            this.w = 0.5f * s;
            s = 0.5f / s;
            this.x = (zy - yz) * s;
            this.y = (xz - zx) * s;
            this.z = (yx - xy) * s;
        } else if (xx > yy && xx > zz) {
            s = (float) Math.sqrt(((1.0d + ((double) xx)) - ((double) yy)) - ((double) zz));
            this.x = 0.5f * s;
            s = 0.5f / s;
            this.y = (yx + xy) * s;
            this.z = (xz + zx) * s;
            this.w = (zy - yz) * s;
        } else if (yy > zz) {
            s = (float) Math.sqrt(((1.0d + ((double) yy)) - ((double) xx)) - ((double) zz));
            this.y = 0.5f * s;
            s = 0.5f / s;
            this.x = (yx + xy) * s;
            this.z = (zy + yz) * s;
            this.w = (xz - zx) * s;
        } else {
            s = (float) Math.sqrt(((1.0d + ((double) zz)) - ((double) xx)) - ((double) yy));
            this.z = 0.5f * s;
            s = 0.5f / s;
            this.x = (xz + zx) * s;
            this.y = (zy + yz) * s;
            this.w = (yx - xy) * s;
        }
        return this;
    }

    public Quaternion setFromCross(Vector3 v1, Vector3 v2) {
        return setFromAxisRad((v1.y * v2.z) - (v1.z * v2.y), (v1.z * v2.x) - (v1.x * v2.z), (v1.x * v2.y) - (v1.y * v2.x), (float) Math.acos((double) MathUtils.clamp(v1.dot(v2), -1.0f, 1.0f)));
    }

    public Quaternion setFromCross(float x1, float y1, float z1, float x2, float y2, float z2) {
        return setFromAxisRad((y1 * z2) - (z1 * y2), (z1 * x2) - (x1 * z2), (x1 * y2) - (y1 * x2), (float) Math.acos((double) MathUtils.clamp(Vector3.dot(x1, y1, z1, x2, y2, z2), -1.0f, 1.0f)));
    }

    public Quaternion slerp(Quaternion end, float alpha) {
        float absDot;
        float d = (((this.x * end.x) + (this.y * end.y)) + (this.z * end.z)) + (this.w * end.w);
        if (d < 0.0f) {
            absDot = -d;
        } else {
            absDot = d;
        }
        float scale0 = 1.0f - alpha;
        float scale1 = alpha;
        if (((double) (1.0f - absDot)) > 0.1d) {
            float angle = (float) Math.acos((double) absDot);
            float invSinTheta = 1.0f / ((float) Math.sin((double) angle));
            scale0 = ((float) Math.sin((double) ((1.0f - alpha) * angle))) * invSinTheta;
            scale1 = ((float) Math.sin((double) (alpha * angle))) * invSinTheta;
        }
        if (d < 0.0f) {
            scale1 = -scale1;
        }
        this.x = (this.x * scale0) + (end.x * scale1);
        this.y = (this.y * scale0) + (end.y * scale1);
        this.z = (this.z * scale0) + (end.z * scale1);
        this.w = (this.w * scale0) + (end.w * scale1);
        return this;
    }

    public Quaternion slerp(Quaternion[] q) {
        float w = 1.0f / ((float) q.length);
        set(q[0]).exp(w);
        for (int i = 1; i < q.length; i++) {
            mul(tmp1.set(q[i]).exp(w));
        }
        nor();
        return this;
    }

    public Quaternion slerp(Quaternion[] q, float[] w) {
        set(q[0]).exp(w[0]);
        for (int i = 1; i < q.length; i++) {
            mul(tmp1.set(q[i]).exp(w[i]));
        }
        nor();
        return this;
    }

    public Quaternion exp(float alpha) {
        float coeff;
        float norm = len();
        float normExp = (float) Math.pow((double) norm, (double) alpha);
        float theta = (float) Math.acos((double) (this.w / norm));
        if (((double) Math.abs(theta)) < 0.001d) {
            coeff = (normExp * alpha) / norm;
        } else {
            coeff = (float) ((((double) normExp) * Math.sin((double) (alpha * theta))) / (((double) norm) * Math.sin((double) theta)));
        }
        this.w = (float) (((double) normExp) * Math.cos((double) (alpha * theta)));
        this.x *= coeff;
        this.y *= coeff;
        this.z *= coeff;
        nor();
        return this;
    }

    public int hashCode() {
        return ((((((NumberUtils.floatToRawIntBits(this.w) + 31) * 31) + NumberUtils.floatToRawIntBits(this.x)) * 31) + NumberUtils.floatToRawIntBits(this.y)) * 31) + NumberUtils.floatToRawIntBits(this.z);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Quaternion)) {
            return false;
        }
        Quaternion other = (Quaternion) obj;
        if (NumberUtils.floatToRawIntBits(this.w) == NumberUtils.floatToRawIntBits(other.w) && NumberUtils.floatToRawIntBits(this.x) == NumberUtils.floatToRawIntBits(other.x) && NumberUtils.floatToRawIntBits(this.y) == NumberUtils.floatToRawIntBits(other.y) && NumberUtils.floatToRawIntBits(this.z) == NumberUtils.floatToRawIntBits(other.z)) {
            return true;
        }
        return false;
    }

    public static final float dot(float x1, float y1, float z1, float w1, float x2, float y2, float z2, float w2) {
        return (((x1 * x2) + (y1 * y2)) + (z1 * z2)) + (w1 * w2);
    }

    public float dot(Quaternion other) {
        return (((this.x * other.x) + (this.y * other.y)) + (this.z * other.z)) + (this.w * other.w);
    }

    public float dot(float x, float y, float z, float w) {
        return (((this.x * x) + (this.y * y)) + (this.z * z)) + (this.w * w);
    }

    public Quaternion mul(float scalar) {
        this.x *= scalar;
        this.y *= scalar;
        this.z *= scalar;
        this.w *= scalar;
        return this;
    }

    public float getAxisAngle(Vector3 axis) {
        return getAxisAngleRad(axis) * MathUtils.radiansToDegrees;
    }

    public float getAxisAngleRad(Vector3 axis) {
        if (this.w > 1.0f) {
            nor();
        }
        float angle = (float) (2.0d * Math.acos((double) this.w));
        double s = Math.sqrt((double) (1.0f - (this.w * this.w)));
        if (s < 9.999999974752427E-7d) {
            axis.x = this.x;
            axis.y = this.y;
            axis.z = this.z;
        } else {
            axis.x = (float) (((double) this.x) / s);
            axis.y = (float) (((double) this.y) / s);
            axis.z = (float) (((double) this.z) / s);
        }
        return angle;
    }

    public float getAngleRad() {
        return (float) (Math.acos(this.w > 1.0f ? (double) (this.w / len()) : (double) this.w) * 2.0d);
    }

    public float getAngle() {
        return getAngleRad() * MathUtils.radiansToDegrees;
    }

    public void getSwingTwist(float axisX, float axisY, float axisZ, Quaternion swing, Quaternion twist) {
        float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        twist.set(axisX * d, axisY * d, axisZ * d, this.w).nor();
        swing.set(twist).conjugate().mulLeft(this);
    }

    public void getSwingTwist(Vector3 axis, Quaternion swing, Quaternion twist) {
        getSwingTwist(axis.x, axis.y, axis.z, swing, twist);
    }

    public float getAngleAroundRad(float axisX, float axisY, float axisZ) {
        float d = Vector3.dot(this.x, this.y, this.z, axisX, axisY, axisZ);
        float l2 = len2(axisX * d, axisY * d, axisZ * d, this.w);
        return MathUtils.isZero(l2) ? 0.0f : (float) (2.0d * Math.acos((double) MathUtils.clamp((float) (((double) this.w) / Math.sqrt((double) l2)), -1.0f, 1.0f)));
    }

    public float getAngleAroundRad(Vector3 axis) {
        return getAngleAroundRad(axis.x, axis.y, axis.z);
    }

    public float getAngleAround(float axisX, float axisY, float axisZ) {
        return getAngleAroundRad(axisX, axisY, axisZ) * MathUtils.radiansToDegrees;
    }

    public float getAngleAround(Vector3 axis) {
        return getAngleAround(axis.x, axis.y, axis.z);
    }
}
