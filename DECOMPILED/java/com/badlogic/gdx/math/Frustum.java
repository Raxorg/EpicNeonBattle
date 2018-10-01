package com.badlogic.gdx.math;

import com.badlogic.gdx.math.Plane.PlaneSide;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Frustum {
    protected static final Vector3[] clipSpacePlanePoints = new Vector3[]{new Vector3(-1.0f, -1.0f, -1.0f), new Vector3(1.0f, -1.0f, -1.0f), new Vector3(1.0f, 1.0f, -1.0f), new Vector3(-1.0f, 1.0f, -1.0f), new Vector3(-1.0f, -1.0f, 1.0f), new Vector3(1.0f, -1.0f, 1.0f), new Vector3(1.0f, 1.0f, 1.0f), new Vector3(-1.0f, 1.0f, 1.0f)};
    protected static final float[] clipSpacePlanePointsArray = new float[24];
    private static final Vector3 tmpV = new Vector3();
    public final Vector3[] planePoints = new Vector3[]{new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3()};
    protected final float[] planePointsArray = new float[24];
    public final Plane[] planes = new Plane[6];

    static {
        int i = 0;
        Vector3[] vector3Arr = clipSpacePlanePoints;
        int length = vector3Arr.length;
        int j = 0;
        while (i < length) {
            Vector3 v = vector3Arr[i];
            int i2 = j + 1;
            clipSpacePlanePointsArray[j] = v.x;
            j = i2 + 1;
            clipSpacePlanePointsArray[i2] = v.y;
            i2 = j + 1;
            clipSpacePlanePointsArray[j] = v.z;
            i++;
            j = i2;
        }
    }

    public Frustum() {
        for (int i = 0; i < 6; i++) {
            this.planes[i] = new Plane(new Vector3(), 0.0f);
        }
    }

    public void update(Matrix4 inverseProjectionView) {
        System.arraycopy(clipSpacePlanePointsArray, 0, this.planePointsArray, 0, clipSpacePlanePointsArray.length);
        Matrix4.prj(inverseProjectionView.val, this.planePointsArray, 0, 8, 3);
        int i = 0;
        int j = 0;
        while (i < 8) {
            Vector3 v = this.planePoints[i];
            int j2 = j + 1;
            v.x = this.planePointsArray[j];
            j = j2 + 1;
            v.y = this.planePointsArray[j2];
            j2 = j + 1;
            v.z = this.planePointsArray[j];
            i++;
            j = j2;
        }
        this.planes[0].set(this.planePoints[1], this.planePoints[0], this.planePoints[2]);
        this.planes[1].set(this.planePoints[4], this.planePoints[5], this.planePoints[7]);
        this.planes[2].set(this.planePoints[0], this.planePoints[4], this.planePoints[3]);
        this.planes[3].set(this.planePoints[5], this.planePoints[1], this.planePoints[6]);
        this.planes[4].set(this.planePoints[2], this.planePoints[3], this.planePoints[6]);
        this.planes[5].set(this.planePoints[4], this.planePoints[0], this.planePoints[1]);
    }

    public boolean pointInFrustum(Vector3 point) {
        for (Plane testPoint : this.planes) {
            if (testPoint.testPoint(point) == PlaneSide.Back) {
                return false;
            }
        }
        return true;
    }

    public boolean pointInFrustum(float x, float y, float z) {
        for (Plane testPoint : this.planes) {
            if (testPoint.testPoint(x, y, z) == PlaneSide.Back) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustum(Vector3 center, float radius) {
        for (int i = 0; i < 6; i++) {
            if (((this.planes[i].normal.x * center.x) + (this.planes[i].normal.y * center.y)) + (this.planes[i].normal.z * center.z) < (-radius) - this.planes[i].d) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustum(float x, float y, float z, float radius) {
        for (int i = 0; i < 6; i++) {
            if (((this.planes[i].normal.x * x) + (this.planes[i].normal.y * y)) + (this.planes[i].normal.z * z) < (-radius) - this.planes[i].d) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustumWithoutNearFar(Vector3 center, float radius) {
        for (int i = 2; i < 6; i++) {
            if (((this.planes[i].normal.x * center.x) + (this.planes[i].normal.y * center.y)) + (this.planes[i].normal.z * center.z) < (-radius) - this.planes[i].d) {
                return false;
            }
        }
        return true;
    }

    public boolean sphereInFrustumWithoutNearFar(float x, float y, float z, float radius) {
        for (int i = 2; i < 6; i++) {
            if (((this.planes[i].normal.x * x) + (this.planes[i].normal.y * y)) + (this.planes[i].normal.z * z) < (-radius) - this.planes[i].d) {
                return false;
            }
        }
        return true;
    }

    public boolean boundsInFrustum(BoundingBox bounds) {
        int i = 0;
        int len2 = this.planes.length;
        while (i < len2) {
            if (this.planes[i].testPoint(bounds.getCorner000(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner001(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner010(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner011(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner100(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner101(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner110(tmpV)) == PlaneSide.Back && this.planes[i].testPoint(bounds.getCorner111(tmpV)) == PlaneSide.Back) {
                return false;
            }
            i++;
        }
        return true;
    }

    public boolean boundsInFrustum(Vector3 center, Vector3 dimensions) {
        return boundsInFrustum(center.x, center.y, center.z, dimensions.x / 2.0f, dimensions.y / 2.0f, dimensions.z / 2.0f);
    }

    public boolean boundsInFrustum(float x, float y, float z, float halfWidth, float halfHeight, float halfDepth) {
        int i = 0;
        int len2 = this.planes.length;
        while (i < len2) {
            if (this.planes[i].testPoint(x + halfWidth, y + halfHeight, z + halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x + halfWidth, y + halfHeight, z - halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x + halfWidth, y - halfHeight, z + halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x + halfWidth, y - halfHeight, z - halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x - halfWidth, y + halfHeight, z + halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x - halfWidth, y + halfHeight, z - halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x - halfWidth, y - halfHeight, z + halfDepth) == PlaneSide.Back && this.planes[i].testPoint(x - halfWidth, y - halfHeight, z - halfDepth) == PlaneSide.Back) {
                return false;
            }
            i++;
        }
        return true;
    }
}
