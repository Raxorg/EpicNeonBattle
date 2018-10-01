package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.NumberUtils;

public class PolygonSpriteBatch implements Batch {
    private int blendDstFunc;
    private int blendSrcFunc;
    private boolean blendingDisabled;
    float color;
    private final Matrix4 combinedMatrix;
    private ShaderProgram customShader;
    private boolean drawing;
    private float invTexHeight;
    private float invTexWidth;
    private Texture lastTexture;
    public int maxTrianglesInBatch;
    private Mesh mesh;
    private boolean ownsShader;
    private final Matrix4 projectionMatrix;
    public int renderCalls;
    private final ShaderProgram shader;
    private Color tempColor;
    public int totalRenderCalls;
    private final Matrix4 transformMatrix;
    private int triangleIndex;
    private final short[] triangles;
    private int vertexIndex;
    private final float[] vertices;

    public PolygonSpriteBatch() {
        this(2000, null);
    }

    public PolygonSpriteBatch(int size) {
        this(size, null);
    }

    public PolygonSpriteBatch(int size, ShaderProgram defaultShader) {
        this.invTexWidth = 0.0f;
        this.invTexHeight = 0.0f;
        this.transformMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4();
        this.combinedMatrix = new Matrix4();
        this.blendSrcFunc = GL20.GL_SRC_ALPHA;
        this.blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
        this.color = Color.WHITE.toFloatBits();
        this.tempColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.renderCalls = 0;
        this.totalRenderCalls = 0;
        this.maxTrianglesInBatch = 0;
        if (size > 10920) {
            throw new IllegalArgumentException("Can't have more than 10920 triangles per batch: " + size);
        }
        VertexDataType vertexDataType = VertexDataType.VertexArray;
        if (Gdx.gl30 != null) {
            vertexDataType = VertexDataType.VertexBufferObjectWithVAO;
        }
        this.mesh = new Mesh(vertexDataType, false, size, size * 3, new VertexAttribute(1, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(4, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(16, 2, "a_texCoord0"));
        this.vertices = new float[(size * 5)];
        this.triangles = new short[(size * 3)];
        if (defaultShader == null) {
            this.shader = SpriteBatch.createDefaultShader();
            this.ownsShader = true;
        } else {
            this.shader = defaultShader;
        }
        this.projectionMatrix.setToOrtho2D(0.0f, 0.0f, (float) Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight());
    }

    public void begin() {
        if (this.drawing) {
            throw new IllegalStateException("PolygonSpriteBatch.end must be called before begin.");
        }
        this.renderCalls = 0;
        Gdx.gl.glDepthMask(false);
        if (this.customShader != null) {
            this.customShader.begin();
        } else {
            this.shader.begin();
        }
        setupMatrices();
        this.drawing = true;
    }

    public void end() {
        if (this.drawing) {
            if (this.vertexIndex > 0) {
                flush();
            }
            this.lastTexture = null;
            this.drawing = false;
            GL20 gl = Gdx.gl;
            gl.glDepthMask(true);
            if (isBlendingEnabled()) {
                gl.glDisable(GL20.GL_BLEND);
            }
            if (this.customShader != null) {
                this.customShader.end();
                return;
            } else {
                this.shader.end();
                return;
            }
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before end.");
    }

    public void setColor(Color tint) {
        this.color = tint.toFloatBits();
    }

    public void setColor(float r, float g, float b, float a) {
        this.color = NumberUtils.intToFloatColor((((((int) (255.0f * a)) << 24) | (((int) (255.0f * b)) << 16)) | (((int) (255.0f * g)) << 8)) | ((int) (255.0f * r)));
    }

    public void setColor(float color) {
        this.color = color;
    }

    public Color getColor() {
        int intBits = NumberUtils.floatToIntColor(this.color);
        Color color = this.tempColor;
        color.r = ((float) (intBits & Keys.F12)) / 255.0f;
        color.g = ((float) ((intBits >>> 8) & Keys.F12)) / 255.0f;
        color.b = ((float) ((intBits >>> 16) & Keys.F12)) / 255.0f;
        color.a = ((float) ((intBits >>> 24) & Keys.F12)) / 255.0f;
        return color;
    }

    public float getPackedColor() {
        return this.color;
    }

    public void draw(PolygonRegion region, float x, float y) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            short[] regionTriangles = region.triangles;
            int regionTrianglesLength = regionTriangles.length;
            float[] regionVertices = region.vertices;
            int regionVerticesLength = regionVertices.length;
            Texture texture = region.region.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength > this.vertices.length) {
                    flush();
                }
            }
            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            int startVertex = vertexIndex / 5;
            int i = 0;
            int triangleIndex2 = triangleIndex;
            while (i < regionTrianglesLength) {
                triangleIndex = triangleIndex2 + 1;
                triangles[triangleIndex2] = (short) (regionTriangles[i] + startVertex);
                i++;
                triangleIndex2 = triangleIndex;
            }
            this.triangleIndex = triangleIndex2;
            float[] vertices = this.vertices;
            float color = this.color;
            float[] textureCoords = region.textureCoords;
            i = 0;
            int vertexIndex2 = vertexIndex;
            while (i < regionVerticesLength) {
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = regionVertices[i] + x;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = regionVertices[i + 1] + y;
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = color;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = textureCoords[i];
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = textureCoords[i + 1];
                i += 2;
                vertexIndex2 = vertexIndex;
            }
            this.vertexIndex = vertexIndex2;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(PolygonRegion region, float x, float y, float width, float height) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            short[] regionTriangles = region.triangles;
            int regionTrianglesLength = regionTriangles.length;
            float[] regionVertices = region.vertices;
            int regionVerticesLength = regionVertices.length;
            TextureRegion textureRegion = region.region;
            Texture texture = textureRegion.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength > this.vertices.length) {
                    flush();
                }
            }
            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            int startVertex = vertexIndex / 5;
            int i = 0;
            int n = regionTriangles.length;
            int triangleIndex2 = triangleIndex;
            while (i < n) {
                triangleIndex = triangleIndex2 + 1;
                triangles[triangleIndex2] = (short) (regionTriangles[i] + startVertex);
                i++;
                triangleIndex2 = triangleIndex;
            }
            this.triangleIndex = triangleIndex2;
            float[] vertices = this.vertices;
            float color = this.color;
            float[] textureCoords = region.textureCoords;
            float sX = width / ((float) textureRegion.regionWidth);
            float sY = height / ((float) textureRegion.regionHeight);
            i = 0;
            int vertexIndex2 = vertexIndex;
            while (i < regionVerticesLength) {
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = (regionVertices[i] * sX) + x;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = (regionVertices[i + 1] * sY) + y;
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = color;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = textureCoords[i];
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = textureCoords[i + 1];
                i += 2;
                vertexIndex2 = vertexIndex;
            }
            this.vertexIndex = vertexIndex2;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(PolygonRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            short[] regionTriangles = region.triangles;
            int regionTrianglesLength = regionTriangles.length;
            float[] regionVertices = region.vertices;
            int regionVerticesLength = regionVertices.length;
            TextureRegion textureRegion = region.region;
            Texture texture = textureRegion.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + regionTrianglesLength > triangles.length || this.vertexIndex + regionVerticesLength > this.vertices.length) {
                    flush();
                }
            }
            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            int startVertex = vertexIndex / 5;
            int i = 0;
            int triangleIndex2 = triangleIndex;
            while (i < regionTrianglesLength) {
                triangleIndex = triangleIndex2 + 1;
                triangles[triangleIndex2] = (short) (regionTriangles[i] + startVertex);
                i++;
                triangleIndex2 = triangleIndex;
            }
            this.triangleIndex = triangleIndex2;
            float[] vertices = this.vertices;
            float color = this.color;
            float[] textureCoords = region.textureCoords;
            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
            float sX = width / ((float) textureRegion.regionWidth);
            float sY = height / ((float) textureRegion.regionHeight);
            float cos = MathUtils.cosDeg(rotation);
            float sin = MathUtils.sinDeg(rotation);
            i = 0;
            int vertexIndex2 = vertexIndex;
            while (i < regionVerticesLength) {
                float fx = ((regionVertices[i] * sX) - originX) * scaleX;
                float fy = ((regionVertices[i + 1] * sY) - originY) * scaleY;
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = ((cos * fx) - (sin * fy)) + worldOriginX;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = ((sin * fx) + (cos * fy)) + worldOriginY;
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = color;
                vertexIndex2 = vertexIndex + 1;
                vertices[vertexIndex] = textureCoords[i];
                vertexIndex = vertexIndex2 + 1;
                vertices[vertexIndex2] = textureCoords[i + 1];
                i += 2;
                vertexIndex2 = vertexIndex;
            }
            this.vertexIndex = vertexIndex2;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float[] polygonVertices, int verticesOffset, int verticesCount, short[] polygonTriangles, int trianglesOffset, int trianglesCount) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else if (this.triangleIndex + trianglesCount > triangles.length || this.vertexIndex + verticesCount > vertices.length) {
                flush();
            }
            int triangleIndex = this.triangleIndex;
            int vertexIndex = this.vertexIndex;
            int startVertex = vertexIndex / 5;
            int i = trianglesOffset;
            int n = i + trianglesCount;
            int triangleIndex2 = triangleIndex;
            while (i < n) {
                triangleIndex = triangleIndex2 + 1;
                triangles[triangleIndex2] = (short) (polygonTriangles[i] + startVertex);
                i++;
                triangleIndex2 = triangleIndex;
            }
            this.triangleIndex = triangleIndex2;
            System.arraycopy(polygonVertices, verticesOffset, vertices, vertexIndex, verticesCount);
            this.vertexIndex += verticesCount;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (this.drawing) {
            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            float tmp;
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
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
            float p1x = fx;
            float p1y = fy;
            float p2x = fx;
            float p2y = fy2;
            float p3x = fx2;
            float p3y = fy2;
            float p4x = fx2;
            float p4y = fy;
            if (rotation != 0.0f) {
                float cos = MathUtils.cosDeg(rotation);
                float sin = MathUtils.sinDeg(rotation);
                x1 = (cos * p1x) - (sin * p1y);
                y1 = (sin * p1x) + (cos * p1y);
                x2 = (cos * p2x) - (sin * p2y);
                y2 = (sin * p2x) + (cos * p2y);
                x3 = (cos * p3x) - (sin * p3y);
                y3 = (sin * p3x) + (cos * p3y);
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = p1x;
                y1 = p1y;
                x2 = p2x;
                y2 = p2y;
                x3 = p3x;
                y3 = p3y;
                x4 = p4x;
                y4 = p4y;
            }
            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            float u = ((float) srcX) * this.invTexWidth;
            float v = ((float) (srcY + srcHeight)) * this.invTexHeight;
            float u2 = ((float) (srcX + srcWidth)) * this.invTexWidth;
            float v2 = ((float) srcY) * this.invTexHeight;
            if (flipX) {
                tmp = u;
                u = u2;
                u2 = tmp;
            }
            if (flipY) {
                tmp = v;
                v = v2;
                v2 = tmp;
            }
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x1;
            idx = idx2 + 1;
            vertices[idx2] = y1;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x2;
            idx2 = idx + 1;
            vertices[idx] = y2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = x3;
            idx = idx2 + 1;
            vertices[idx2] = y3;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = x4;
            idx2 = idx + 1;
            vertices[idx] = y4;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        if (this.drawing) {
            float tmp;
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float u = ((float) srcX) * this.invTexWidth;
            float v = ((float) (srcY + srcHeight)) * this.invTexHeight;
            float u2 = ((float) (srcX + srcWidth)) * this.invTexWidth;
            float v2 = ((float) srcY) * this.invTexHeight;
            float fx2 = x + width;
            float fy2 = y + height;
            if (flipX) {
                tmp = u;
                u = u2;
                u2 = tmp;
            }
            if (flipY) {
                tmp = v;
                v = v2;
                v2 = tmp;
            }
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x;
            idx = idx2 + 1;
            vertices[idx2] = y;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x;
            idx2 = idx + 1;
            vertices[idx] = fy2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = fx2;
            idx = idx2 + 1;
            vertices[idx2] = fy2;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = fx2;
            idx2 = idx + 1;
            vertices[idx] = y;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                flush();
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float u = ((float) srcX) * this.invTexWidth;
            float v = ((float) (srcY + srcHeight)) * this.invTexHeight;
            float u2 = ((float) (srcX + srcWidth)) * this.invTexWidth;
            float v2 = ((float) srcY) * this.invTexHeight;
            float fx2 = x + ((float) srcWidth);
            float fy2 = y + ((float) srcHeight);
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x;
            idx = idx2 + 1;
            vertices[idx2] = y;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x;
            idx2 = idx + 1;
            vertices[idx] = fy2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = fx2;
            idx = idx2 + 1;
            vertices[idx2] = fy2;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = fx2;
            idx2 = idx + 1;
            vertices[idx] = y;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2, float v2) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                flush();
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float fx2 = x + width;
            float fy2 = y + height;
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x;
            idx = idx2 + 1;
            vertices[idx2] = y;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x;
            idx2 = idx + 1;
            vertices[idx] = fy2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = fx2;
            idx = idx2 + 1;
            vertices[idx2] = fy2;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = fx2;
            idx2 = idx + 1;
            vertices[idx] = y;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float x, float y) {
        draw(texture, x, y, (float) texture.getWidth(), (float) texture.getHeight());
    }

    public void draw(Texture texture, float x, float y, float width, float height) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                flush();
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float fx2 = x + width;
            float fy2 = y + height;
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x;
            idx = idx2 + 1;
            vertices[idx2] = y;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = 0.0f;
            idx2 = idx + 1;
            vertices[idx] = 1.0f;
            idx = idx2 + 1;
            vertices[idx2] = x;
            idx2 = idx + 1;
            vertices[idx] = fy2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = 0.0f;
            idx = idx2 + 1;
            vertices[idx2] = 0.0f;
            idx2 = idx + 1;
            vertices[idx] = fx2;
            idx = idx2 + 1;
            vertices[idx2] = fy2;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = 1.0f;
            idx2 = idx + 1;
            vertices[idx] = 0.0f;
            idx = idx2 + 1;
            vertices[idx2] = fx2;
            idx2 = idx + 1;
            vertices[idx] = y;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = 1.0f;
            idx = idx2 + 1;
            vertices[idx2] = 1.0f;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(Texture texture, float[] spriteVertices, int offset, int count) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            int triangleCount = (count / 20) * 6;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else if (this.triangleIndex + triangleCount > triangles.length || this.vertexIndex + count > vertices.length) {
                flush();
            }
            int vertexIndex = this.vertexIndex;
            int triangleIndex = this.triangleIndex;
            short vertex = (short) (vertexIndex / 5);
            int n = triangleIndex + triangleCount;
            while (triangleIndex < n) {
                triangles[triangleIndex] = vertex;
                triangles[triangleIndex + 1] = (short) (vertex + 1);
                triangles[triangleIndex + 2] = (short) (vertex + 2);
                triangles[triangleIndex + 3] = (short) (vertex + 2);
                triangles[triangleIndex + 4] = (short) (vertex + 3);
                triangles[triangleIndex + 5] = vertex;
                triangleIndex += 6;
                vertex = (short) (vertex + 4);
            }
            this.triangleIndex = triangleIndex;
            System.arraycopy(spriteVertices, offset, vertices, vertexIndex, count);
            this.vertexIndex += count;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(TextureRegion region, float x, float y) {
        draw(region, x, y, (float) region.getRegionWidth(), (float) region.getRegionHeight());
    }

    public void draw(TextureRegion region, float x, float y, float width, float height) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            Texture texture = region.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float fx2 = x + width;
            float fy2 = y + height;
            float u = region.u;
            float v = region.v2;
            float u2 = region.u2;
            float v2 = region.v;
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x;
            idx = idx2 + 1;
            vertices[idx2] = y;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x;
            idx2 = idx + 1;
            vertices[idx] = fy2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = fx2;
            idx = idx2 + 1;
            vertices[idx2] = fy2;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = fx2;
            idx2 = idx + 1;
            vertices[idx] = y;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        if (this.drawing) {
            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            Texture texture = region.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
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
            float p1x = fx;
            float p1y = fy;
            float p2x = fx;
            float p2y = fy2;
            float p3x = fx2;
            float p3y = fy2;
            float p4x = fx2;
            float p4y = fy;
            if (rotation != 0.0f) {
                float cos = MathUtils.cosDeg(rotation);
                float sin = MathUtils.sinDeg(rotation);
                x1 = (cos * p1x) - (sin * p1y);
                y1 = (sin * p1x) + (cos * p1y);
                x2 = (cos * p2x) - (sin * p2y);
                y2 = (sin * p2x) + (cos * p2y);
                x3 = (cos * p3x) - (sin * p3y);
                y3 = (sin * p3x) + (cos * p3y);
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = p1x;
                y1 = p1y;
                x2 = p2x;
                y2 = p2y;
                x3 = p3x;
                y3 = p3y;
                x4 = p4x;
                y4 = p4y;
            }
            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            float u = region.u;
            float v = region.v2;
            float u2 = region.u2;
            float v2 = region.v;
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x1;
            idx = idx2 + 1;
            vertices[idx2] = y1;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x2;
            idx2 = idx + 1;
            vertices[idx] = y2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = x3;
            idx = idx2 + 1;
            vertices[idx2] = y3;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = x4;
            idx2 = idx + 1;
            vertices[idx] = y4;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, boolean clockwise) {
        if (this.drawing) {
            float x1;
            float y1;
            float x2;
            float y2;
            float x3;
            float y3;
            float x4;
            float y4;
            float u1;
            float v1;
            float u2;
            float v2;
            float u3;
            float v3;
            float u4;
            float v4;
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            Texture texture = region.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float worldOriginX = x + originX;
            float worldOriginY = y + originY;
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
            float p1x = fx;
            float p1y = fy;
            float p2x = fx;
            float p2y = fy2;
            float p3x = fx2;
            float p3y = fy2;
            float p4x = fx2;
            float p4y = fy;
            if (rotation != 0.0f) {
                float cos = MathUtils.cosDeg(rotation);
                float sin = MathUtils.sinDeg(rotation);
                x1 = (cos * p1x) - (sin * p1y);
                y1 = (sin * p1x) + (cos * p1y);
                x2 = (cos * p2x) - (sin * p2y);
                y2 = (sin * p2x) + (cos * p2y);
                x3 = (cos * p3x) - (sin * p3y);
                y3 = (sin * p3x) + (cos * p3y);
                x4 = x1 + (x3 - x2);
                y4 = y3 - (y2 - y1);
            } else {
                x1 = p1x;
                y1 = p1y;
                x2 = p2x;
                y2 = p2y;
                x3 = p3x;
                y3 = p3y;
                x4 = p4x;
                y4 = p4y;
            }
            x1 += worldOriginX;
            y1 += worldOriginY;
            x2 += worldOriginX;
            y2 += worldOriginY;
            x3 += worldOriginX;
            y3 += worldOriginY;
            x4 += worldOriginX;
            y4 += worldOriginY;
            if (clockwise) {
                u1 = region.u2;
                v1 = region.v2;
                u2 = region.u;
                v2 = region.v2;
                u3 = region.u;
                v3 = region.v;
                u4 = region.u2;
                v4 = region.v;
            } else {
                u1 = region.u;
                v1 = region.v;
                u2 = region.u2;
                v2 = region.v;
                u3 = region.u2;
                v3 = region.v2;
                u4 = region.u;
                v4 = region.v2;
            }
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x1;
            idx = idx2 + 1;
            vertices[idx2] = y1;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u1;
            idx2 = idx + 1;
            vertices[idx] = v1;
            idx = idx2 + 1;
            vertices[idx2] = x2;
            idx2 = idx + 1;
            vertices[idx] = y2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = x3;
            idx = idx2 + 1;
            vertices[idx2] = y3;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u3;
            idx2 = idx + 1;
            vertices[idx] = v3;
            idx = idx2 + 1;
            vertices[idx2] = x4;
            idx2 = idx + 1;
            vertices[idx] = y4;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u4;
            idx = idx2 + 1;
            vertices[idx2] = v4;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void draw(TextureRegion region, float width, float height, Affine2 transform) {
        if (this.drawing) {
            short[] triangles = this.triangles;
            float[] vertices = this.vertices;
            Texture texture = region.texture;
            if (texture != this.lastTexture) {
                switchTexture(texture);
            } else {
                if (this.triangleIndex + 6 > triangles.length || this.vertexIndex + 20 > vertices.length) {
                    flush();
                }
            }
            int i = this.triangleIndex;
            int startVertex = this.vertexIndex / 5;
            int i2 = i + 1;
            triangles[i] = (short) startVertex;
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 1);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 2);
            i = i2 + 1;
            triangles[i2] = (short) (startVertex + 2);
            i2 = i + 1;
            triangles[i] = (short) (startVertex + 3);
            i = i2 + 1;
            triangles[i2] = (short) startVertex;
            this.triangleIndex = i;
            float x1 = transform.m02;
            float y1 = transform.m12;
            float x2 = (transform.m01 * height) + transform.m02;
            float y2 = (transform.m11 * height) + transform.m12;
            float x3 = ((transform.m00 * width) + (transform.m01 * height)) + transform.m02;
            float y3 = ((transform.m10 * width) + (transform.m11 * height)) + transform.m12;
            float x4 = (transform.m00 * width) + transform.m02;
            float y4 = (transform.m10 * width) + transform.m12;
            float u = region.u;
            float v = region.v2;
            float u2 = region.u2;
            float v2 = region.v;
            float color = this.color;
            int idx = this.vertexIndex;
            int idx2 = idx + 1;
            vertices[idx] = x1;
            idx = idx2 + 1;
            vertices[idx2] = y1;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u;
            idx2 = idx + 1;
            vertices[idx] = v;
            idx = idx2 + 1;
            vertices[idx2] = x2;
            idx2 = idx + 1;
            vertices[idx] = y2;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u;
            idx = idx2 + 1;
            vertices[idx2] = v2;
            idx2 = idx + 1;
            vertices[idx] = x3;
            idx = idx2 + 1;
            vertices[idx2] = y3;
            idx2 = idx + 1;
            vertices[idx] = color;
            idx = idx2 + 1;
            vertices[idx2] = u2;
            idx2 = idx + 1;
            vertices[idx] = v2;
            idx = idx2 + 1;
            vertices[idx2] = x4;
            idx2 = idx + 1;
            vertices[idx] = y4;
            idx = idx2 + 1;
            vertices[idx2] = color;
            idx2 = idx + 1;
            vertices[idx] = u2;
            idx = idx2 + 1;
            vertices[idx2] = v;
            this.vertexIndex = idx;
            return;
        }
        throw new IllegalStateException("PolygonSpriteBatch.begin must be called before draw.");
    }

    public void flush() {
        if (this.vertexIndex != 0) {
            this.renderCalls++;
            this.totalRenderCalls++;
            int trianglesInBatch = this.triangleIndex;
            if (trianglesInBatch > this.maxTrianglesInBatch) {
                this.maxTrianglesInBatch = trianglesInBatch;
            }
            this.lastTexture.bind();
            Mesh mesh = this.mesh;
            mesh.setVertices(this.vertices, 0, this.vertexIndex);
            mesh.setIndices(this.triangles, 0, this.triangleIndex);
            if (this.blendingDisabled) {
                Gdx.gl.glDisable(GL20.GL_BLEND);
            } else {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                if (this.blendSrcFunc != -1) {
                    Gdx.gl.glBlendFunc(this.blendSrcFunc, this.blendDstFunc);
                }
            }
            mesh.render(this.customShader != null ? this.customShader : this.shader, 4, 0, trianglesInBatch);
            this.vertexIndex = 0;
            this.triangleIndex = 0;
        }
    }

    public void disableBlending() {
        flush();
        this.blendingDisabled = true;
    }

    public void enableBlending() {
        flush();
        this.blendingDisabled = false;
    }

    public void setBlendFunction(int srcFunc, int dstFunc) {
        if (this.blendSrcFunc != srcFunc || this.blendDstFunc != dstFunc) {
            flush();
            this.blendSrcFunc = srcFunc;
            this.blendDstFunc = dstFunc;
        }
    }

    public int getBlendSrcFunc() {
        return this.blendSrcFunc;
    }

    public int getBlendDstFunc() {
        return this.blendDstFunc;
    }

    public void dispose() {
        this.mesh.dispose();
        if (this.ownsShader && this.shader != null) {
            this.shader.dispose();
        }
    }

    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public Matrix4 getTransformMatrix() {
        return this.transformMatrix;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (this.drawing) {
            flush();
        }
        this.projectionMatrix.set(projection);
        if (this.drawing) {
            setupMatrices();
        }
    }

    public void setTransformMatrix(Matrix4 transform) {
        if (this.drawing) {
            flush();
        }
        this.transformMatrix.set(transform);
        if (this.drawing) {
            setupMatrices();
        }
    }

    private void setupMatrices() {
        this.combinedMatrix.set(this.projectionMatrix).mul(this.transformMatrix);
        if (this.customShader != null) {
            this.customShader.setUniformMatrix("u_projTrans", this.combinedMatrix);
            this.customShader.setUniformi("u_texture", 0);
            return;
        }
        this.shader.setUniformMatrix("u_projTrans", this.combinedMatrix);
        this.shader.setUniformi("u_texture", 0);
    }

    private void switchTexture(Texture texture) {
        flush();
        this.lastTexture = texture;
        this.invTexWidth = 1.0f / ((float) texture.getWidth());
        this.invTexHeight = 1.0f / ((float) texture.getHeight());
    }

    public void setShader(ShaderProgram shader) {
        if (this.drawing) {
            flush();
            if (this.customShader != null) {
                this.customShader.end();
            } else {
                this.shader.end();
            }
        }
        this.customShader = shader;
        if (this.drawing) {
            if (this.customShader != null) {
                this.customShader.begin();
            } else {
                this.shader.begin();
            }
            setupMatrices();
        }
    }

    public ShaderProgram getShader() {
        if (this.customShader == null) {
            return this.shader;
        }
        return this.customShader;
    }

    public boolean isBlendingEnabled() {
        return !this.blendingDisabled;
    }

    public boolean isDrawing() {
        return this.drawing;
    }
}
