package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class ShapeRenderer implements Disposable {
    private boolean autoShapeType;
    private final Color color;
    private final Matrix4 combinedMatrix;
    private float defaultRectLineWidth;
    private boolean matrixDirty;
    private final Matrix4 projectionMatrix;
    private final ImmediateModeRenderer renderer;
    private ShapeType shapeType;
    private final Vector2 tmp;
    private final Matrix4 transformMatrix;

    public enum ShapeType {
        Point(0),
        Line(1),
        Filled(4);
        
        private final int glType;

        private ShapeType(int glType) {
            this.glType = glType;
        }

        public int getGlType() {
            return this.glType;
        }
    }

    public ShapeRenderer() {
        this(5000);
    }

    public ShapeRenderer(int maxVertices) {
        this(maxVertices, null);
    }

    public ShapeRenderer(int maxVertices, ShaderProgram defaultShader) {
        this.matrixDirty = false;
        this.projectionMatrix = new Matrix4();
        this.transformMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.tmp = new Vector2();
        this.color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.defaultRectLineWidth = 0.75f;
        if (defaultShader == null) {
            this.renderer = new ImmediateModeRenderer20(maxVertices, false, true, 0);
        } else {
            this.renderer = new ImmediateModeRenderer20(maxVertices, false, true, 0, defaultShader);
        }
        this.projectionMatrix.setToOrtho2D(0.0f, 0.0f, (float) Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight());
        this.matrixDirty = true;
    }

    public void setColor(Color color) {
        this.color.set(color);
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
    }

    public Color getColor() {
        return this.color;
    }

    public void updateMatrices() {
        this.matrixDirty = true;
    }

    public void setProjectionMatrix(Matrix4 matrix) {
        this.projectionMatrix.set(matrix);
        this.matrixDirty = true;
    }

    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public void setTransformMatrix(Matrix4 matrix) {
        this.transformMatrix.set(matrix);
        this.matrixDirty = true;
    }

    public Matrix4 getTransformMatrix() {
        return this.transformMatrix;
    }

    public void identity() {
        this.transformMatrix.idt();
        this.matrixDirty = true;
    }

    public void translate(float x, float y, float z) {
        this.transformMatrix.translate(x, y, z);
        this.matrixDirty = true;
    }

    public void rotate(float axisX, float axisY, float axisZ, float degrees) {
        this.transformMatrix.rotate(axisX, axisY, axisZ, degrees);
        this.matrixDirty = true;
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        this.transformMatrix.scale(scaleX, scaleY, scaleZ);
        this.matrixDirty = true;
    }

    public void setAutoShapeType(boolean autoShapeType) {
        this.autoShapeType = autoShapeType;
    }

    public void begin() {
        if (this.autoShapeType) {
            begin(ShapeType.Line);
            return;
        }
        throw new IllegalStateException("autoShapeType must be true to use this method.");
    }

    public void begin(ShapeType type) {
        if (this.shapeType != null) {
            throw new IllegalStateException("Call end() before beginning a new shape batch.");
        }
        this.shapeType = type;
        if (this.matrixDirty) {
            this.combinedMatrix.set(this.projectionMatrix);
            Matrix4.mul(this.combinedMatrix.val, this.transformMatrix.val);
            this.matrixDirty = false;
        }
        this.renderer.begin(this.combinedMatrix, this.shapeType.getGlType());
    }

    public void set(ShapeType type) {
        if (this.shapeType != type) {
            if (this.shapeType == null) {
                throw new IllegalStateException("begin must be called first.");
            } else if (this.autoShapeType) {
                end();
                begin(type);
            } else {
                throw new IllegalStateException("autoShapeType must be enabled.");
            }
        }
    }

    public void point(float x, float y, float z) {
        float size;
        if (this.shapeType == ShapeType.Line) {
            size = this.defaultRectLineWidth * 0.5f;
            line(x - size, y - size, z, x + size, y + size, z);
        } else if (this.shapeType == ShapeType.Filled) {
            size = this.defaultRectLineWidth * 0.5f;
            box(x - size, y - size, z - size, this.defaultRectLineWidth, this.defaultRectLineWidth, this.defaultRectLineWidth);
        } else {
            check(ShapeType.Point, null, 1);
            this.renderer.color(this.color);
            this.renderer.vertex(x, y, z);
        }
    }

    public final void line(float x, float y, float z, float x2, float y2, float z2) {
        line(x, y, z, x2, y2, z2, this.color, this.color);
    }

    public final void line(Vector3 v0, Vector3 v1) {
        line(v0.x, v0.y, v0.z, v1.x, v1.y, v1.z, this.color, this.color);
    }

    public final void line(float x, float y, float x2, float y2) {
        line(x, y, 0.0f, x2, y2, 0.0f, this.color, this.color);
    }

    public final void line(Vector2 v0, Vector2 v1) {
        line(v0.x, v0.y, 0.0f, v1.x, v1.y, 0.0f, this.color, this.color);
    }

    public final void line(float x, float y, float x2, float y2, Color c1, Color c2) {
        line(x, y, 0.0f, x2, y2, 0.0f, c1, c2);
    }

    public void line(float x, float y, float z, float x2, float y2, float z2, Color c1, Color c2) {
        if (this.shapeType == ShapeType.Filled) {
            rectLine(x, y, x2, y2, this.defaultRectLineWidth);
            return;
        }
        check(ShapeType.Line, null, 2);
        this.renderer.color(c1.r, c1.g, c1.b, c1.a);
        this.renderer.vertex(x, y, z);
        this.renderer.color(c2.r, c2.g, c2.b, c2.a);
        this.renderer.vertex(x2, y2, z2);
    }

    public void curve(float x1, float y1, float cx1, float cy1, float cx2, float cy2, float x2, float y2, int segments) {
        check(ShapeType.Line, null, (segments * 2) + 2);
        float colorBits = this.color.toFloatBits();
        float subdiv_step = 1.0f / ((float) segments);
        float subdiv_step2 = subdiv_step * subdiv_step;
        float subdiv_step3 = (subdiv_step * subdiv_step) * subdiv_step;
        float pre1 = 3.0f * subdiv_step;
        float pre2 = 3.0f * subdiv_step2;
        float pre4 = 6.0f * subdiv_step2;
        float pre5 = 6.0f * subdiv_step3;
        float tmp1x = (x1 - (2.0f * cx1)) + cx2;
        float tmp1y = (y1 - (2.0f * cy1)) + cy2;
        float tmp2x = (((cx1 - cx2) * 3.0f) - x1) + x2;
        float tmp2y = (((cy1 - cy2) * 3.0f) - y1) + y2;
        float fx = x1;
        float fy = y1;
        float dfx = (((cx1 - x1) * pre1) + (tmp1x * pre2)) + (tmp2x * subdiv_step3);
        float dfy = (((cy1 - y1) * pre1) + (tmp1y * pre2)) + (tmp2y * subdiv_step3);
        float ddfx = (tmp1x * pre4) + (tmp2x * pre5);
        float ddfy = (tmp1y * pre4) + (tmp2y * pre5);
        float dddfx = tmp2x * pre5;
        float dddfy = tmp2y * pre5;
        int segments2 = segments;
        while (true) {
            segments = segments2 - 1;
            if (segments2 > 0) {
                this.renderer.color(colorBits);
                this.renderer.vertex(fx, fy, 0.0f);
                fx += dfx;
                fy += dfy;
                dfx += ddfx;
                dfy += ddfy;
                ddfx += dddfx;
                ddfy += dddfy;
                this.renderer.color(colorBits);
                this.renderer.vertex(fx, fy, 0.0f);
                segments2 = segments;
            } else {
                this.renderer.color(colorBits);
                this.renderer.vertex(fx, fy, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(x2, y2, 0.0f);
                return;
            }
        }
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3) {
        check(ShapeType.Line, ShapeType.Filled, 6);
        float colorBits = this.color.toFloatBits();
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(colorBits);
            this.renderer.vertex(x1, y1, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x1, y1, 0.0f);
            return;
        }
        this.renderer.color(colorBits);
        this.renderer.vertex(x1, y1, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x2, y2, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x3, y3, 0.0f);
    }

    public void triangle(float x1, float y1, float x2, float y2, float x3, float y3, Color col1, Color col2, Color col3) {
        check(ShapeType.Line, ShapeType.Filled, 6);
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x1, y1, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x1, y1, 0.0f);
            return;
        }
        this.renderer.color(col1.r, col1.g, col1.b, col1.a);
        this.renderer.vertex(x1, y1, 0.0f);
        this.renderer.color(col2.r, col2.g, col2.b, col2.a);
        this.renderer.vertex(x2, y2, 0.0f);
        this.renderer.color(col3.r, col3.g, col3.b, col3.a);
        this.renderer.vertex(x3, y3, 0.0f);
    }

    public void rect(float x, float y, float width, float height) {
        check(ShapeType.Line, ShapeType.Filled, 8);
        float colorBits = this.color.toFloatBits();
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, 0.0f);
            return;
        }
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, 0.0f);
    }

    public void rect(float x, float y, float width, float height, Color col1, Color col2, Color col3, Color col4) {
        check(ShapeType.Line, ShapeType.Filled, 8);
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x, y, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x + width, y, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x + width, y, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x + width, y + height, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x + width, y + height, 0.0f);
            this.renderer.color(col4.r, col4.g, col4.b, col4.a);
            this.renderer.vertex(x, y + height, 0.0f);
            this.renderer.color(col4.r, col4.g, col4.b, col4.a);
            this.renderer.vertex(x, y + height, 0.0f);
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x, y, 0.0f);
            return;
        }
        this.renderer.color(col1.r, col1.g, col1.b, col1.a);
        this.renderer.vertex(x, y, 0.0f);
        this.renderer.color(col2.r, col2.g, col2.b, col2.a);
        this.renderer.vertex(x + width, y, 0.0f);
        this.renderer.color(col3.r, col3.g, col3.b, col3.a);
        this.renderer.vertex(x + width, y + height, 0.0f);
        this.renderer.color(col3.r, col3.g, col3.b, col3.a);
        this.renderer.vertex(x + width, y + height, 0.0f);
        this.renderer.color(col4.r, col4.g, col4.b, col4.a);
        this.renderer.vertex(x, y + height, 0.0f);
        this.renderer.color(col1.r, col1.g, col1.b, col1.a);
        this.renderer.vertex(x, y, 0.0f);
    }

    public void rect(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float degrees) {
        rect(x, y, originX, originY, width, height, scaleX, scaleY, degrees, this.color, this.color, this.color, this.color);
    }

    public void rect(float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float degrees, Color col1, Color col2, Color col3, Color col4) {
        check(ShapeType.Line, ShapeType.Filled, 8);
        float cos = MathUtils.cosDeg(degrees);
        float sin = MathUtils.sinDeg(degrees);
        float fx = -originX;
        float fy = -originY;
        float fx2 = width - originX;
        float fy2 = height - originY;
        if (!(scaleX == 1.0f && scaleY == 1.0f)) {
            fx *= scaleX;
            fy *= scaleY;
            fx2 *= scaleX;
            fy2 *= scaleY;
        }
        float worldOriginX = x + originX;
        float worldOriginY = y + originY;
        float x1 = ((cos * fx) - (sin * fy)) + worldOriginX;
        float y1 = ((sin * fx) + (cos * fy)) + worldOriginY;
        float x2 = ((cos * fx2) - (sin * fy)) + worldOriginX;
        float y2 = ((sin * fx2) + (cos * fy)) + worldOriginY;
        float x3 = ((cos * fx2) - (sin * fy2)) + worldOriginX;
        float y3 = ((sin * fx2) + (cos * fy2)) + worldOriginY;
        float x4 = x1 + (x3 - x2);
        float y4 = y3 - (y2 - y1);
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x1, y1, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(col2.r, col2.g, col2.b, col2.a);
            this.renderer.vertex(x2, y2, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(col3.r, col3.g, col3.b, col3.a);
            this.renderer.vertex(x3, y3, 0.0f);
            this.renderer.color(col4.r, col4.g, col4.b, col4.a);
            this.renderer.vertex(x4, y4, 0.0f);
            this.renderer.color(col4.r, col4.g, col4.b, col4.a);
            this.renderer.vertex(x4, y4, 0.0f);
            this.renderer.color(col1.r, col1.g, col1.b, col1.a);
            this.renderer.vertex(x1, y1, 0.0f);
            return;
        }
        this.renderer.color(col1.r, col1.g, col1.b, col1.a);
        this.renderer.vertex(x1, y1, 0.0f);
        this.renderer.color(col2.r, col2.g, col2.b, col2.a);
        this.renderer.vertex(x2, y2, 0.0f);
        this.renderer.color(col3.r, col3.g, col3.b, col3.a);
        this.renderer.vertex(x3, y3, 0.0f);
        this.renderer.color(col3.r, col3.g, col3.b, col3.a);
        this.renderer.vertex(x3, y3, 0.0f);
        this.renderer.color(col4.r, col4.g, col4.b, col4.a);
        this.renderer.vertex(x4, y4, 0.0f);
        this.renderer.color(col1.r, col1.g, col1.b, col1.a);
        this.renderer.vertex(x1, y1, 0.0f);
    }

    public void rectLine(float x1, float y1, float x2, float y2, float width) {
        check(ShapeType.Line, ShapeType.Filled, 8);
        float colorBits = this.color.toFloatBits();
        Vector2 t = this.tmp.set(y2 - y1, x1 - x2).nor();
        width *= 0.5f;
        float tx = t.x * width;
        float ty = t.y * width;
        if (this.shapeType == ShapeType.Line) {
            this.renderer.color(colorBits);
            this.renderer.vertex(x1 + tx, y1 + ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x1 - tx, y1 - ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2 + tx, y2 + ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2 - tx, y2 - ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2 + tx, y2 + ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x1 + tx, y1 + ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x2 - tx, y2 - ty, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x1 - tx, y1 - ty, 0.0f);
            return;
        }
        this.renderer.color(colorBits);
        this.renderer.vertex(x1 + tx, y1 + ty, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x1 - tx, y1 - ty, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x2 + tx, y2 + ty, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x2 - tx, y2 - ty, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x2 + tx, y2 + ty, 0.0f);
        this.renderer.color(colorBits);
        this.renderer.vertex(x1 - tx, y1 - ty, 0.0f);
    }

    public void rectLine(Vector2 p1, Vector2 p2, float width) {
        rectLine(p1.x, p1.y, p2.x, p2.y, width);
    }

    public void box(float x, float y, float z, float width, float height, float depth) {
        depth = -depth;
        float colorBits = this.color.toFloatBits();
        if (this.shapeType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, 24);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + width, y + height, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z + depth);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y + height, z + depth);
            return;
        }
        check(ShapeType.Line, ShapeType.Filled, 36);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y + height, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z + depth);
        this.renderer.color(colorBits);
        this.renderer.vertex(x + width, y, z);
        this.renderer.color(colorBits);
        this.renderer.vertex(x, y, z);
    }

    public void x(float x, float y, float size) {
        line(x - size, y - size, x + size, y + size);
        line(x - size, y + size, x + size, y - size);
    }

    public void x(Vector2 p, float size) {
        x(p.x, p.y, size);
    }

    public void arc(float x, float y, float radius, float start, float degrees) {
        arc(x, y, radius, start, degrees, Math.max(1, (int) ((6.0f * ((float) Math.cbrt((double) radius))) * (degrees / 360.0f))));
    }

    public void arc(float x, float y, float radius, float start, float degrees, int segments) {
        if (segments <= 0) {
            throw new IllegalArgumentException("segments must be > 0.");
        }
        float temp;
        float colorBits = this.color.toFloatBits();
        float theta = (MathUtils.PI2 * (degrees / 360.0f)) / ((float) segments);
        float cos = MathUtils.cos(theta);
        float sin = MathUtils.sin(theta);
        float cx = radius * MathUtils.cos(MathUtils.degreesToRadians * start);
        float cy = radius * MathUtils.sin(MathUtils.degreesToRadians * start);
        int i;
        if (this.shapeType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, (segments * 2) + 2);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, 0.0f);
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
                temp = cx;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, 0.0f);
        } else {
            check(ShapeType.Line, ShapeType.Filled, (segments * 3) + 3);
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x, y, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
                temp = cx;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, 0.0f);
        }
        temp = cx;
        this.renderer.color(colorBits);
        this.renderer.vertex(x + 0.0f, y + 0.0f, 0.0f);
    }

    public void circle(float x, float y, float radius) {
        circle(x, y, radius, Math.max(1, (int) (6.0f * ((float) Math.cbrt((double) radius)))));
    }

    public void circle(float x, float y, float radius, int segments) {
        if (segments <= 0) {
            throw new IllegalArgumentException("segments must be > 0.");
        }
        float temp;
        float colorBits = this.color.toFloatBits();
        float angle = MathUtils.PI2 / ((float) segments);
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float cx = radius;
        float cy = 0.0f;
        int i;
        if (this.shapeType == ShapeType.Line) {
            check(ShapeType.Line, ShapeType.Filled, (segments * 2) + 2);
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
                temp = cx;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, 0.0f);
        } else {
            check(ShapeType.Line, ShapeType.Filled, (segments * 3) + 3);
            segments--;
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x, y, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
                temp = cx;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, 0.0f);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, 0.0f);
        }
        temp = cx;
        cx = radius;
        this.renderer.color(colorBits);
        this.renderer.vertex(x + cx, y + 0.0f, 0.0f);
    }

    public void ellipse(float x, float y, float width, float height) {
        ellipse(x, y, width, height, Math.max(1, (int) (12.0f * ((float) Math.cbrt((double) Math.max(width * 0.5f, 0.5f * height))))));
    }

    public void ellipse(float x, float y, float width, float height, int segments) {
        if (segments <= 0) {
            throw new IllegalArgumentException("segments must be > 0.");
        }
        check(ShapeType.Line, ShapeType.Filled, segments * 3);
        float colorBits = this.color.toFloatBits();
        float angle = MathUtils.PI2 / ((float) segments);
        float cx = x + (width / 2.0f);
        float cy = y + (height / 2.0f);
        int i;
        if (this.shapeType == ShapeType.Line) {
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(((0.5f * width) * MathUtils.cos(((float) i) * angle)) + cx, ((0.5f * height) * MathUtils.sin(((float) i) * angle)) + cy, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(((0.5f * width) * MathUtils.cos(((float) (i + 1)) * angle)) + cx, ((0.5f * height) * MathUtils.sin(((float) (i + 1)) * angle)) + cy, 0.0f);
            }
            return;
        }
        for (i = 0; i < segments; i++) {
            this.renderer.color(colorBits);
            this.renderer.vertex(((0.5f * width) * MathUtils.cos(((float) i) * angle)) + cx, ((0.5f * height) * MathUtils.sin(((float) i) * angle)) + cy, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(cx, cy, 0.0f);
            this.renderer.color(colorBits);
            this.renderer.vertex(((0.5f * width) * MathUtils.cos(((float) (i + 1)) * angle)) + cx, ((0.5f * height) * MathUtils.sin(((float) (i + 1)) * angle)) + cy, 0.0f);
        }
    }

    public void cone(float x, float y, float z, float radius, float height) {
        cone(x, y, z, radius, height, Math.max(1, (int) (4.0f * ((float) Math.sqrt((double) radius)))));
    }

    public void cone(float x, float y, float z, float radius, float height, int segments) {
        if (segments <= 0) {
            throw new IllegalArgumentException("segments must be > 0.");
        }
        float temp;
        float temp2;
        check(ShapeType.Line, ShapeType.Filled, (segments * 4) + 2);
        float colorBits = this.color.toFloatBits();
        float angle = MathUtils.PI2 / ((float) segments);
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float cx = radius;
        float cy = 0.0f;
        int i;
        if (this.shapeType == ShapeType.Line) {
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
                this.renderer.color(colorBits);
                this.renderer.vertex(x, y, z + height);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
                temp = cx;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, z);
        } else {
            segments--;
            for (i = 0; i < segments; i++) {
                this.renderer.color(colorBits);
                this.renderer.vertex(x, y, z);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
                temp = cx;
                temp2 = cy;
                cx = (cos * cx) - (sin * cy);
                cy = (sin * temp) + (cos * cy);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + temp, y + temp2, z);
                this.renderer.color(colorBits);
                this.renderer.vertex(x + cx, y + cy, z);
                this.renderer.color(colorBits);
                this.renderer.vertex(x, y, z + height);
            }
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + cy, z);
        }
        temp = cx;
        temp2 = cy;
        cx = radius;
        this.renderer.color(colorBits);
        this.renderer.vertex(x + cx, y + 0.0f, z);
        if (this.shapeType != ShapeType.Line) {
            this.renderer.color(colorBits);
            this.renderer.vertex(x + temp, y + temp2, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x + cx, y + 0.0f, z);
            this.renderer.color(colorBits);
            this.renderer.vertex(x, y, z + height);
        }
    }

    public void polygon(float[] vertices, int offset, int count) {
        if (count < 6) {
            throw new IllegalArgumentException("Polygons must contain at least 3 points.");
        } else if (count % 2 != 0) {
            throw new IllegalArgumentException("Polygons must have an even number of vertices.");
        } else {
            check(ShapeType.Line, null, count);
            float colorBits = this.color.toFloatBits();
            float firstX = vertices[0];
            float firstY = vertices[1];
            int n = offset + count;
            for (int i = offset; i < n; i += 2) {
                float x2;
                float y2;
                float x1 = vertices[i];
                float y1 = vertices[i + 1];
                if (i + 2 >= count) {
                    x2 = firstX;
                    y2 = firstY;
                } else {
                    x2 = vertices[i + 2];
                    y2 = vertices[i + 3];
                }
                this.renderer.color(colorBits);
                this.renderer.vertex(x1, y1, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(x2, y2, 0.0f);
            }
        }
    }

    public void polygon(float[] vertices) {
        polygon(vertices, 0, vertices.length);
    }

    public void polyline(float[] vertices, int offset, int count) {
        if (count < 4) {
            throw new IllegalArgumentException("Polylines must contain at least 2 points.");
        } else if (count % 2 != 0) {
            throw new IllegalArgumentException("Polylines must have an even number of vertices.");
        } else {
            check(ShapeType.Line, null, count);
            float colorBits = this.color.toFloatBits();
            int n = (offset + count) - 2;
            for (int i = offset; i < n; i += 2) {
                float x1 = vertices[i];
                float y1 = vertices[i + 1];
                float x2 = vertices[i + 2];
                float y2 = vertices[i + 3];
                this.renderer.color(colorBits);
                this.renderer.vertex(x1, y1, 0.0f);
                this.renderer.color(colorBits);
                this.renderer.vertex(x2, y2, 0.0f);
            }
        }
    }

    public void polyline(float[] vertices) {
        polyline(vertices, 0, vertices.length);
    }

    private void check(ShapeType preferred, ShapeType other, int newVertices) {
        if (this.shapeType == null) {
            throw new IllegalStateException("begin must be called first.");
        } else if (this.shapeType == preferred || this.shapeType == other) {
            ShapeType type;
            if (this.matrixDirty) {
                type = this.shapeType;
                end();
                begin(type);
            } else if (this.renderer.getMaxVertices() - this.renderer.getNumVertices() < newVertices) {
                type = this.shapeType;
                end();
                begin(type);
            }
        } else if (this.autoShapeType) {
            end();
            begin(preferred);
        } else if (other == null) {
            throw new IllegalStateException("Must call begin(ShapeType." + preferred + ").");
        } else {
            throw new IllegalStateException("Must call begin(ShapeType." + preferred + ") or begin(ShapeType." + other + ").");
        }
    }

    public void end() {
        this.renderer.end();
        this.shapeType = null;
    }

    public void flush() {
        ShapeType type = this.shapeType;
        end();
        begin(type);
    }

    public ShapeType getCurrentType() {
        return this.shapeType;
    }

    public ImmediateModeRenderer getRenderer() {
        return this.renderer;
    }

    public boolean isDrawing() {
        return this.shapeType != null;
    }

    public void dispose() {
        this.renderer.dispose();
    }
}
