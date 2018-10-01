package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.NumberUtils;
import java.nio.FloatBuffer;

public class SpriteCache implements Disposable {
    private static final float[] tempVertices = new float[30];
    private Array<Cache> caches;
    private float color;
    private final Matrix4 combinedMatrix;
    private final IntArray counts;
    private Cache currentCache;
    private ShaderProgram customShader;
    private boolean drawing;
    private final Mesh mesh;
    private final Matrix4 projectionMatrix;
    public int renderCalls;
    private final ShaderProgram shader;
    private Color tempColor;
    private final Array<Texture> textures;
    public int totalRenderCalls;
    private final Matrix4 transformMatrix;

    private static class Cache {
        int[] counts;
        final int id;
        int maxCount;
        final int offset;
        int textureCount;
        Texture[] textures;

        public Cache(int id, int offset) {
            this.id = id;
            this.offset = offset;
        }
    }

    public SpriteCache() {
        this(1000, false);
    }

    public SpriteCache(int size, boolean useIndices) {
        this(size, createDefaultShader(), useIndices);
    }

    public SpriteCache(int size, ShaderProgram shader, boolean useIndices) {
        this.transformMatrix = new Matrix4();
        this.projectionMatrix = new Matrix4();
        this.caches = new Array();
        this.combinedMatrix = new Matrix4();
        this.textures = new Array(8);
        this.counts = new IntArray(8);
        this.color = Color.WHITE.toFloatBits();
        this.tempColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
        this.customShader = null;
        this.renderCalls = 0;
        this.totalRenderCalls = 0;
        this.shader = shader;
        if (!useIndices || size <= 5460) {
            int i;
            int i2 = size * (useIndices ? 4 : 6);
            if (useIndices) {
                i = size * 6;
            } else {
                i = 0;
            }
            this.mesh = new Mesh(true, i2, i, new VertexAttribute(1, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(4, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(16, 2, "a_texCoord0"));
            this.mesh.setAutoBind(false);
            if (useIndices) {
                int length = size * 6;
                short[] indices = new short[length];
                short j = (short) 0;
                int i3 = 0;
                while (i3 < length) {
                    indices[i3 + 0] = j;
                    indices[i3 + 1] = (short) (j + 1);
                    indices[i3 + 2] = (short) (j + 2);
                    indices[i3 + 3] = (short) (j + 2);
                    indices[i3 + 4] = (short) (j + 3);
                    indices[i3 + 5] = j;
                    i3 += 6;
                    j = (short) (j + 4);
                }
                this.mesh.setIndices(indices);
            }
            this.projectionMatrix.setToOrtho2D(0.0f, 0.0f, (float) Gdx.graphics.getWidth(), (float) Gdx.graphics.getHeight());
            return;
        }
        throw new IllegalArgumentException("Can't have more than 5460 sprites per batch: " + size);
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

    public void beginCache() {
        if (this.currentCache != null) {
            throw new IllegalStateException("endCache must be called before begin.");
        }
        if (this.mesh.getNumIndices() > 0) {
        }
        this.currentCache = new Cache(this.caches.size, this.mesh.getVerticesBuffer().limit());
        this.caches.add(this.currentCache);
        this.mesh.getVerticesBuffer().compact();
    }

    public void beginCache(int cacheID) {
        if (this.currentCache != null) {
            throw new IllegalStateException("endCache must be called before begin.");
        } else if (cacheID == this.caches.size - 1) {
            this.mesh.getVerticesBuffer().limit(((Cache) this.caches.removeIndex(cacheID)).offset);
            beginCache();
        } else {
            this.currentCache = (Cache) this.caches.get(cacheID);
            this.mesh.getVerticesBuffer().position(this.currentCache.offset);
        }
    }

    public int endCache() {
        if (this.currentCache == null) {
            throw new IllegalStateException("beginCache must be called before endCache.");
        }
        Cache cache = this.currentCache;
        int cacheCount = this.mesh.getVerticesBuffer().position() - cache.offset;
        int n;
        int i;
        if (cache.textures == null) {
            cache.maxCount = cacheCount;
            cache.textureCount = this.textures.size;
            cache.textures = (Texture[]) this.textures.toArray(Texture.class);
            cache.counts = new int[cache.textureCount];
            n = this.counts.size;
            for (i = 0; i < n; i++) {
                cache.counts[i] = this.counts.get(i);
            }
            this.mesh.getVerticesBuffer().flip();
        } else if (cacheCount > cache.maxCount) {
            throw new GdxRuntimeException("If a cache is not the last created, it cannot be redefined with more entries than when it was first created: " + cacheCount + " (" + cache.maxCount + " max)");
        } else {
            cache.textureCount = this.textures.size;
            if (cache.textures.length < cache.textureCount) {
                cache.textures = new Texture[cache.textureCount];
            }
            n = cache.textureCount;
            for (i = 0; i < n; i++) {
                cache.textures[i] = (Texture) this.textures.get(i);
            }
            if (cache.counts.length < cache.textureCount) {
                cache.counts = new int[cache.textureCount];
            }
            n = cache.textureCount;
            for (i = 0; i < n; i++) {
                cache.counts[i] = this.counts.get(i);
            }
            FloatBuffer vertices = this.mesh.getVerticesBuffer();
            vertices.position(0);
            Cache lastCache = (Cache) this.caches.get(this.caches.size - 1);
            vertices.limit(lastCache.offset + lastCache.maxCount);
        }
        this.currentCache = null;
        this.textures.clear();
        this.counts.clear();
        return cache.id;
    }

    public void clear() {
        this.caches.clear();
        this.mesh.getVerticesBuffer().clear().flip();
    }

    public void add(Texture texture, float[] vertices, int offset, int length) {
        if (this.currentCache == null) {
            throw new IllegalStateException("beginCache must be called before add.");
        }
        int count = (length / ((this.mesh.getNumIndices() > 0 ? 4 : 6) * 5)) * 6;
        int lastIndex = this.textures.size - 1;
        if (lastIndex < 0 || this.textures.get(lastIndex) != texture) {
            this.textures.add(texture);
            this.counts.add(count);
        } else {
            this.counts.incr(lastIndex, count);
        }
        this.mesh.getVerticesBuffer().put(vertices, offset, length);
    }

    public void add(Texture texture, float x, float y) {
        float fx2 = x + ((float) texture.getWidth());
        float fy2 = y + ((float) texture.getHeight());
        tempVertices[0] = x;
        tempVertices[1] = y;
        tempVertices[2] = this.color;
        tempVertices[3] = 0.0f;
        tempVertices[4] = 1.0f;
        tempVertices[5] = x;
        tempVertices[6] = fy2;
        tempVertices[7] = this.color;
        tempVertices[8] = 0.0f;
        tempVertices[9] = 0.0f;
        tempVertices[10] = fx2;
        tempVertices[11] = fy2;
        tempVertices[12] = this.color;
        tempVertices[13] = 1.0f;
        tempVertices[14] = 0.0f;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = fx2;
            tempVertices[16] = y;
            tempVertices[17] = this.color;
            tempVertices[18] = 1.0f;
            tempVertices[19] = 1.0f;
            add(texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = fx2;
        tempVertices[16] = fy2;
        tempVertices[17] = this.color;
        tempVertices[18] = 1.0f;
        tempVertices[19] = 0.0f;
        tempVertices[20] = fx2;
        tempVertices[21] = y;
        tempVertices[22] = this.color;
        tempVertices[23] = 1.0f;
        tempVertices[24] = 1.0f;
        tempVertices[25] = x;
        tempVertices[26] = y;
        tempVertices[27] = this.color;
        tempVertices[28] = 0.0f;
        tempVertices[29] = 1.0f;
        add(texture, tempVertices, 0, 30);
    }

    public void add(Texture texture, float x, float y, int srcWidth, int srcHeight, float u, float v, float u2, float v2, float color) {
        float fx2 = x + ((float) srcWidth);
        float fy2 = y + ((float) srcHeight);
        tempVertices[0] = x;
        tempVertices[1] = y;
        tempVertices[2] = color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x;
        tempVertices[6] = fy2;
        tempVertices[7] = color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = fx2;
        tempVertices[11] = fy2;
        tempVertices[12] = color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = fx2;
            tempVertices[16] = y;
            tempVertices[17] = color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = fx2;
        tempVertices[16] = fy2;
        tempVertices[17] = color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = fx2;
        tempVertices[21] = y;
        tempVertices[22] = color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x;
        tempVertices[26] = y;
        tempVertices[27] = color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(texture, tempVertices, 0, 30);
    }

    public void add(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
        float invTexWidth = 1.0f / ((float) texture.getWidth());
        float invTexHeight = 1.0f / ((float) texture.getHeight());
        float u = ((float) srcX) * invTexWidth;
        float v = ((float) (srcY + srcHeight)) * invTexHeight;
        float u2 = ((float) (srcX + srcWidth)) * invTexWidth;
        float v2 = ((float) srcY) * invTexHeight;
        float fx2 = x + ((float) srcWidth);
        float fy2 = y + ((float) srcHeight);
        tempVertices[0] = x;
        tempVertices[1] = y;
        tempVertices[2] = this.color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x;
        tempVertices[6] = fy2;
        tempVertices[7] = this.color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = fx2;
        tempVertices[11] = fy2;
        tempVertices[12] = this.color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = fx2;
            tempVertices[16] = y;
            tempVertices[17] = this.color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = fx2;
        tempVertices[16] = fy2;
        tempVertices[17] = this.color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = fx2;
        tempVertices[21] = y;
        tempVertices[22] = this.color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x;
        tempVertices[26] = y;
        tempVertices[27] = this.color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(texture, tempVertices, 0, 30);
    }

    public void add(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        float invTexWidth = 1.0f / ((float) texture.getWidth());
        float invTexHeight = 1.0f / ((float) texture.getHeight());
        float u = ((float) srcX) * invTexWidth;
        float v = ((float) (srcY + srcHeight)) * invTexHeight;
        float u2 = ((float) (srcX + srcWidth)) * invTexWidth;
        float v2 = ((float) srcY) * invTexHeight;
        float fx2 = x + width;
        float fy2 = y + height;
        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }
        if (flipY) {
            tmp = v;
            v = v2;
            v2 = tmp;
        }
        tempVertices[0] = x;
        tempVertices[1] = y;
        tempVertices[2] = this.color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x;
        tempVertices[6] = fy2;
        tempVertices[7] = this.color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = fx2;
        tempVertices[11] = fy2;
        tempVertices[12] = this.color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = fx2;
            tempVertices[16] = y;
            tempVertices[17] = this.color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = fx2;
        tempVertices[16] = fy2;
        tempVertices[17] = this.color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = fx2;
        tempVertices[21] = y;
        tempVertices[22] = this.color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x;
        tempVertices[26] = y;
        tempVertices[27] = this.color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(texture, tempVertices, 0, 30);
    }

    public void add(Texture texture, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX, boolean flipY) {
        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;
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
        float invTexWidth = 1.0f / ((float) texture.getWidth());
        float invTexHeight = 1.0f / ((float) texture.getHeight());
        float u = ((float) srcX) * invTexWidth;
        float v = ((float) (srcY + srcHeight)) * invTexHeight;
        float u2 = ((float) (srcX + srcWidth)) * invTexWidth;
        float v2 = ((float) srcY) * invTexHeight;
        if (flipX) {
            float tmp = u;
            u = u2;
            u2 = tmp;
        }
        if (flipY) {
            tmp = v;
            v = v2;
            v2 = tmp;
        }
        tempVertices[0] = x1;
        tempVertices[1] = y1;
        tempVertices[2] = this.color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x2;
        tempVertices[6] = y2;
        tempVertices[7] = this.color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = x3;
        tempVertices[11] = y3;
        tempVertices[12] = this.color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = x4;
            tempVertices[16] = y4;
            tempVertices[17] = this.color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = x3;
        tempVertices[16] = y3;
        tempVertices[17] = this.color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = x4;
        tempVertices[21] = y4;
        tempVertices[22] = this.color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x1;
        tempVertices[26] = y1;
        tempVertices[27] = this.color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(texture, tempVertices, 0, 30);
    }

    public void add(TextureRegion region, float x, float y) {
        add(region, x, y, (float) region.getRegionWidth(), (float) region.getRegionHeight());
    }

    public void add(TextureRegion region, float x, float y, float width, float height) {
        float fx2 = x + width;
        float fy2 = y + height;
        float u = region.u;
        float v = region.v2;
        float u2 = region.u2;
        float v2 = region.v;
        tempVertices[0] = x;
        tempVertices[1] = y;
        tempVertices[2] = this.color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x;
        tempVertices[6] = fy2;
        tempVertices[7] = this.color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = fx2;
        tempVertices[11] = fy2;
        tempVertices[12] = this.color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = fx2;
            tempVertices[16] = y;
            tempVertices[17] = this.color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(region.texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = fx2;
        tempVertices[16] = fy2;
        tempVertices[17] = this.color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = fx2;
        tempVertices[21] = y;
        tempVertices[22] = this.color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x;
        tempVertices[26] = y;
        tempVertices[27] = this.color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(region.texture, tempVertices, 0, 30);
    }

    public void add(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
        float x1;
        float y1;
        float x2;
        float y2;
        float x3;
        float y3;
        float x4;
        float y4;
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
        tempVertices[0] = x1;
        tempVertices[1] = y1;
        tempVertices[2] = this.color;
        tempVertices[3] = u;
        tempVertices[4] = v;
        tempVertices[5] = x2;
        tempVertices[6] = y2;
        tempVertices[7] = this.color;
        tempVertices[8] = u;
        tempVertices[9] = v2;
        tempVertices[10] = x3;
        tempVertices[11] = y3;
        tempVertices[12] = this.color;
        tempVertices[13] = u2;
        tempVertices[14] = v2;
        if (this.mesh.getNumIndices() > 0) {
            tempVertices[15] = x4;
            tempVertices[16] = y4;
            tempVertices[17] = this.color;
            tempVertices[18] = u2;
            tempVertices[19] = v;
            add(region.texture, tempVertices, 0, 20);
            return;
        }
        tempVertices[15] = x3;
        tempVertices[16] = y3;
        tempVertices[17] = this.color;
        tempVertices[18] = u2;
        tempVertices[19] = v2;
        tempVertices[20] = x4;
        tempVertices[21] = y4;
        tempVertices[22] = this.color;
        tempVertices[23] = u2;
        tempVertices[24] = v;
        tempVertices[25] = x1;
        tempVertices[26] = y1;
        tempVertices[27] = this.color;
        tempVertices[28] = u;
        tempVertices[29] = v;
        add(region.texture, tempVertices, 0, 30);
    }

    public void add(Sprite sprite) {
        if (this.mesh.getNumIndices() > 0) {
            add(sprite.getTexture(), sprite.getVertices(), 0, 20);
            return;
        }
        float[] spriteVertices = sprite.getVertices();
        System.arraycopy(spriteVertices, 0, tempVertices, 0, 15);
        System.arraycopy(spriteVertices, 10, tempVertices, 15, 5);
        System.arraycopy(spriteVertices, 15, tempVertices, 20, 5);
        System.arraycopy(spriteVertices, 0, tempVertices, 25, 5);
        add(sprite.getTexture(), tempVertices, 0, 30);
    }

    public void begin() {
        if (this.drawing) {
            throw new IllegalStateException("end must be called before begin.");
        }
        this.renderCalls = 0;
        this.combinedMatrix.set(this.projectionMatrix).mul(this.transformMatrix);
        Gdx.gl20.glDepthMask(false);
        if (this.customShader != null) {
            this.customShader.begin();
            this.customShader.setUniformMatrix("u_proj", this.projectionMatrix);
            this.customShader.setUniformMatrix("u_trans", this.transformMatrix);
            this.customShader.setUniformMatrix("u_projTrans", this.combinedMatrix);
            this.customShader.setUniformi("u_texture", 0);
            this.mesh.bind(this.customShader);
        } else {
            this.shader.begin();
            this.shader.setUniformMatrix("u_projectionViewMatrix", this.combinedMatrix);
            this.shader.setUniformi("u_texture", 0);
            this.mesh.bind(this.shader);
        }
        this.drawing = true;
    }

    public void end() {
        if (this.drawing) {
            this.drawing = false;
            this.shader.end();
            Gdx.gl20.glDepthMask(true);
            if (this.customShader != null) {
                this.mesh.unbind(this.customShader);
                return;
            } else {
                this.mesh.unbind(this.shader);
                return;
            }
        }
        throw new IllegalStateException("begin must be called before end.");
    }

    public void draw(int cacheID) {
        if (this.drawing) {
            Cache cache = (Cache) this.caches.get(cacheID);
            int offset = (cache.offset / ((this.mesh.getNumIndices() > 0 ? 4 : 6) * 5)) * 6;
            Texture[] textures = cache.textures;
            int[] counts = cache.counts;
            int textureCount = cache.textureCount;
            for (int i = 0; i < textureCount; i++) {
                int count = counts[i];
                textures[i].bind();
                if (this.customShader != null) {
                    this.mesh.render(this.customShader, 4, offset, count);
                } else {
                    this.mesh.render(this.shader, 4, offset, count);
                }
                offset += count;
            }
            this.renderCalls += textureCount;
            this.totalRenderCalls += textureCount;
            return;
        }
        throw new IllegalStateException("SpriteCache.begin must be called before draw.");
    }

    public void draw(int cacheID, int offset, int length) {
        if (this.drawing) {
            Cache cache = (Cache) this.caches.get(cacheID);
            offset = (offset * 6) + cache.offset;
            length *= 6;
            Texture[] textures = cache.textures;
            int[] counts = cache.counts;
            int textureCount = cache.textureCount;
            int i = 0;
            while (i < textureCount) {
                textures[i].bind();
                int count = counts[i];
                if (count > length) {
                    i = textureCount;
                    count = length;
                } else {
                    length -= count;
                }
                if (this.customShader != null) {
                    this.mesh.render(this.customShader, 4, offset, count);
                } else {
                    this.mesh.render(this.shader, 4, offset, count);
                }
                offset += count;
                i++;
            }
            this.renderCalls += cache.textureCount;
            this.totalRenderCalls += textureCount;
            return;
        }
        throw new IllegalStateException("SpriteCache.begin must be called before draw.");
    }

    public void dispose() {
        this.mesh.dispose();
        if (this.shader != null) {
            this.shader.dispose();
        }
    }

    public Matrix4 getProjectionMatrix() {
        return this.projectionMatrix;
    }

    public void setProjectionMatrix(Matrix4 projection) {
        if (this.drawing) {
            throw new IllegalStateException("Can't set the matrix within begin/end.");
        }
        this.projectionMatrix.set(projection);
    }

    public Matrix4 getTransformMatrix() {
        return this.transformMatrix;
    }

    public void setTransformMatrix(Matrix4 transform) {
        if (this.drawing) {
            throw new IllegalStateException("Can't set the matrix within begin/end.");
        }
        this.transformMatrix.set(transform);
    }

    static ShaderProgram createDefaultShader() {
        ShaderProgram shader = new ShaderProgram("attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projectionViewMatrix;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_color = a_color;\n   v_color.a = v_color.a * (255.0/254.0);\n   v_texCoords = a_texCoord0;\n   gl_Position =  u_projectionViewMatrix * a_position;\n}\n", "#ifdef GL_ES\nprecision mediump float;\n#endif\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\n  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n}");
        if (shader.isCompiled()) {
            return shader;
        }
        throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
    }

    public void setShader(ShaderProgram shader) {
        this.customShader = shader;
    }
}
