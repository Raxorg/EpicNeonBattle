package com.badlogic.gdx.math;

import com.badlogic.gdx.utils.BooleanArray;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import com.badlogic.gdx.utils.compression.lzma.Encoder;

public class DelaunayTriangulator {
    private static final int COMPLETE = 1;
    private static final float EPSILON = 1.0E-6f;
    private static final int INCOMPLETE = 2;
    private static final int INSIDE = 0;
    private final Vector2 centroid = new Vector2();
    private final BooleanArray complete = new BooleanArray(false, 16);
    private final IntArray edges = new IntArray();
    private final ShortArray originalIndices = new ShortArray(false, 0);
    private final IntArray quicksortStack = new IntArray();
    private float[] sortedPoints;
    private final float[] superTriangle = new float[6];
    private final ShortArray triangles = new ShortArray(false, 16);

    public ShortArray computeTriangles(FloatArray points, boolean sorted) {
        return computeTriangles(points.items, 0, points.size, sorted);
    }

    public ShortArray computeTriangles(float[] polygon, boolean sorted) {
        return computeTriangles(polygon, 0, polygon.length, sorted);
    }

    public ShortArray computeTriangles(float[] points, int offset, int count, boolean sorted) {
        ShortArray triangles = this.triangles;
        triangles.clear();
        if (count >= 6) {
            short offset2;
            int i;
            short[] trianglesArray;
            int n;
            triangles.ensureCapacity(count);
            if (!sorted) {
                if (this.sortedPoints == null || this.sortedPoints.length < count) {
                    this.sortedPoints = new float[count];
                }
                System.arraycopy(points, offset, this.sortedPoints, 0, count);
                points = this.sortedPoints;
                offset2 = (short) 0;
                sort(points, count);
            }
            short end = offset2 + count;
            float xmin = points[0];
            float ymin = points[COMPLETE];
            float xmax = xmin;
            float ymax = ymin;
            short i2 = offset2 + INCOMPLETE;
            while (i2 < end) {
                float value = points[i2];
                if (value < xmin) {
                    xmin = value;
                }
                if (value > xmax) {
                    xmax = value;
                }
                i = i2 + COMPLETE;
                value = points[i];
                if (value < ymin) {
                    ymin = value;
                }
                if (value > ymax) {
                    ymax = value;
                }
                i2 = i + COMPLETE;
            }
            float dx = xmax - xmin;
            float dy = ymax - ymin;
            if (dx <= dy) {
                dx = dy;
            }
            float dmax = dx * 20.0f;
            float xmid = (xmax + xmin) / 2.0f;
            float ymid = (ymax + ymin) / 2.0f;
            float[] superTriangle = this.superTriangle;
            superTriangle[0] = xmid - dmax;
            superTriangle[COMPLETE] = ymid - dmax;
            superTriangle[INCOMPLETE] = xmid;
            superTriangle[3] = ymid + dmax;
            superTriangle[4] = xmid + dmax;
            superTriangle[5] = ymid - dmax;
            IntArray edges = this.edges;
            edges.ensureCapacity(count / INCOMPLETE);
            BooleanArray complete = this.complete;
            complete.clear();
            complete.ensureCapacity(count);
            triangles.add((int) end);
            triangles.add(end + INCOMPLETE);
            triangles.add(end + 4);
            complete.add(false);
            for (short pointIndex = offset2; pointIndex < end; pointIndex += INCOMPLETE) {
                float x = points[pointIndex];
                float y = points[pointIndex + COMPLETE];
                trianglesArray = triangles.items;
                boolean[] completeArray = complete.items;
                for (int triangleIndex = triangles.size - 1; triangleIndex >= 0; triangleIndex -= 3) {
                    int completeIndex = triangleIndex / 3;
                    if (!completeArray[completeIndex]) {
                        float x1;
                        float y1;
                        float x2;
                        float y2;
                        float x3;
                        float y3;
                        short p1 = trianglesArray[triangleIndex - 2];
                        short p2 = trianglesArray[triangleIndex - 1];
                        short p3 = trianglesArray[triangleIndex];
                        if (p1 >= end) {
                            i = p1 - end;
                            x1 = superTriangle[i];
                            y1 = superTriangle[i + COMPLETE];
                        } else {
                            x1 = points[p1];
                            y1 = points[p1 + COMPLETE];
                        }
                        if (p2 >= end) {
                            i = p2 - end;
                            x2 = superTriangle[i];
                            y2 = superTriangle[i + COMPLETE];
                        } else {
                            x2 = points[p2];
                            y2 = points[p2 + COMPLETE];
                        }
                        if (p3 >= end) {
                            i = p3 - end;
                            x3 = superTriangle[i];
                            y3 = superTriangle[i + COMPLETE];
                        } else {
                            x3 = points[p3];
                            y3 = points[p3 + COMPLETE];
                        }
                        switch (circumCircle(x, y, x1, y1, x2, y2, x3, y3)) {
                            case Encoder.EMatchFinderTypeBT2 /*0*/:
                                edges.add(p1);
                                edges.add(p2);
                                edges.add(p2);
                                edges.add(p3);
                                edges.add(p3);
                                edges.add(p1);
                                triangles.removeIndex(triangleIndex);
                                triangles.removeIndex(triangleIndex - 1);
                                triangles.removeIndex(triangleIndex - 2);
                                complete.removeIndex(completeIndex);
                                break;
                            case COMPLETE /*1*/:
                                completeArray[completeIndex] = true;
                                break;
                            default:
                                break;
                        }
                    }
                }
                int[] edgesArray = edges.items;
                n = edges.size;
                for (i = 0; i < n; i += INCOMPLETE) {
                    int p12 = edgesArray[i];
                    if (p12 != -1) {
                        int p22 = edgesArray[i + COMPLETE];
                        boolean skip = false;
                        int ii = i + INCOMPLETE;
                        while (ii < n) {
                            if (p12 == edgesArray[ii + COMPLETE] && p22 == edgesArray[ii]) {
                                skip = true;
                                edgesArray[ii] = -1;
                            }
                            ii += INCOMPLETE;
                        }
                        if (!skip) {
                            triangles.add(p12);
                            triangles.add(edgesArray[i + COMPLETE]);
                            triangles.add((int) pointIndex);
                            complete.add(false);
                        }
                    }
                }
                edges.clear();
            }
            trianglesArray = triangles.items;
            i = triangles.size - 1;
            while (i >= 0) {
                if (trianglesArray[i] >= end || trianglesArray[i - 1] >= end || trianglesArray[i - 2] >= end) {
                    triangles.removeIndex(i);
                    triangles.removeIndex(i - 1);
                    triangles.removeIndex(i - 2);
                }
                i -= 3;
            }
            if (!sorted) {
                short[] originalIndicesArray = this.originalIndices.items;
                n = triangles.size;
                for (i = 0; i < n; i += COMPLETE) {
                    trianglesArray[i] = (short) (originalIndicesArray[trianglesArray[i] / INCOMPLETE] * INCOMPLETE);
                }
            }
            if (offset2 == (short) 0) {
                n = triangles.size;
                for (i = 0; i < n; i += COMPLETE) {
                    trianglesArray[i] = (short) (trianglesArray[i] / INCOMPLETE);
                }
            } else {
                n = triangles.size;
                for (i = 0; i < n; i += COMPLETE) {
                    trianglesArray[i] = (short) ((trianglesArray[i] - offset2) / INCOMPLETE);
                }
            }
        }
        return triangles;
    }

    private int circumCircle(float xp, float yp, float x1, float y1, float x2, float y2, float x3, float y3) {
        float xc;
        float yc;
        float y1y2 = Math.abs(y1 - y2);
        float y2y3 = Math.abs(y2 - y3);
        if (y1y2 >= EPSILON) {
            float m1 = (-(x2 - x1)) / (y2 - y1);
            float mx1 = (x1 + x2) / 2.0f;
            float my1 = (y1 + y2) / 2.0f;
            if (y2y3 < EPSILON) {
                xc = (x3 + x2) / 2.0f;
                yc = ((xc - mx1) * m1) + my1;
            } else {
                float m2 = (-(x3 - x2)) / (y3 - y2);
                xc = ((((m1 * mx1) - (m2 * ((x2 + x3) / 2.0f))) + ((y2 + y3) / 2.0f)) - my1) / (m1 - m2);
                yc = ((xc - mx1) * m1) + my1;
            }
        } else if (y2y3 < EPSILON) {
            return INCOMPLETE;
        } else {
            xc = (x2 + x1) / 2.0f;
            yc = ((xc - ((x2 + x3) / 2.0f)) * ((-(x3 - x2)) / (y3 - y2))) + ((y2 + y3) / 2.0f);
        }
        float dx = x2 - xc;
        float dy = y2 - yc;
        float rsqr = (dx * dx) + (dy * dy);
        dx = xp - xc;
        dx *= dx;
        dy = yp - yc;
        if (((dy * dy) + dx) - rsqr <= EPSILON) {
            return 0;
        }
        return (xp <= xc || dx <= rsqr) ? INCOMPLETE : COMPLETE;
    }

    private void sort(float[] values, int count) {
        short pointCount = count / INCOMPLETE;
        this.originalIndices.clear();
        this.originalIndices.ensureCapacity(pointCount);
        short[] originalIndicesArray = this.originalIndices.items;
        for (short i = (short) 0; i < pointCount; i = (short) (i + COMPLETE)) {
            originalIndicesArray[i] = i;
        }
        int upper = count - 1;
        IntArray stack = this.quicksortStack;
        stack.add(0);
        stack.add(upper - 1);
        while (stack.size > 0) {
            upper = stack.pop();
            int lower = stack.pop();
            if (upper > lower) {
                int i2 = quicksortPartition(values, lower, upper, originalIndicesArray);
                if (i2 - lower > upper - i2) {
                    stack.add(lower);
                    stack.add(i2 - 2);
                }
                stack.add(i2 + INCOMPLETE);
                stack.add(upper);
                if (upper - i2 >= i2 - lower) {
                    stack.add(lower);
                    stack.add(i2 - 2);
                }
            }
        }
    }

    private int quicksortPartition(float[] values, int lower, int upper, short[] originalIndices) {
        float tempValue;
        short tempIndex;
        float value = values[lower];
        int up = upper;
        int down = lower + INCOMPLETE;
        while (down < up) {
            while (down < up && values[down] <= value) {
                down += INCOMPLETE;
            }
            while (values[up] > value) {
                up -= 2;
            }
            if (down < up) {
                tempValue = values[down];
                values[down] = values[up];
                values[up] = tempValue;
                tempValue = values[down + COMPLETE];
                values[down + COMPLETE] = values[up + COMPLETE];
                values[up + COMPLETE] = tempValue;
                tempIndex = originalIndices[down / INCOMPLETE];
                originalIndices[down / INCOMPLETE] = originalIndices[up / INCOMPLETE];
                originalIndices[up / INCOMPLETE] = tempIndex;
            }
        }
        values[lower] = values[up];
        values[up] = value;
        tempValue = values[lower + COMPLETE];
        values[lower + COMPLETE] = values[up + COMPLETE];
        values[up + COMPLETE] = tempValue;
        tempIndex = originalIndices[lower / INCOMPLETE];
        originalIndices[lower / INCOMPLETE] = originalIndices[up / INCOMPLETE];
        originalIndices[up / INCOMPLETE] = tempIndex;
        return up;
    }

    public void trim(ShortArray triangles, float[] points, float[] hull, int offset, int count) {
        short[] trianglesArray = triangles.items;
        for (int i = triangles.size - 1; i >= 0; i -= 3) {
            int p1 = trianglesArray[i - 2] * INCOMPLETE;
            int p2 = trianglesArray[i - 1] * INCOMPLETE;
            int p3 = trianglesArray[i] * INCOMPLETE;
            GeometryUtils.triangleCentroid(points[p1], points[p1 + COMPLETE], points[p2], points[p2 + COMPLETE], points[p3], points[p3 + COMPLETE], this.centroid);
            if (!Intersector.isPointInPolygon(hull, offset, count, this.centroid.x, this.centroid.y)) {
                triangles.removeIndex(i);
                triangles.removeIndex(i - 1);
                triangles.removeIndex(i - 2);
            }
        }
    }
}
