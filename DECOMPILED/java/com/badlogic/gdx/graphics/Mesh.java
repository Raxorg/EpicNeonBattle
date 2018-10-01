package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.IndexArray;
import com.badlogic.gdx.graphics.glutils.IndexBufferObject;
import com.badlogic.gdx.graphics.glutils.IndexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.IndexData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.VertexArray;
import com.badlogic.gdx.graphics.glutils.VertexBufferObject;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectSubData;
import com.badlogic.gdx.graphics.glutils.VertexBufferObjectWithVAO;
import com.badlogic.gdx.graphics.glutils.VertexData;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;

public class Mesh implements Disposable {
    static final Map<Application, Array<Mesh>> meshes = new HashMap();
    boolean autoBind = true;
    final IndexData indices;
    final boolean isVertexArray;
    private final Vector3 tmpV = new Vector3();
    final VertexData vertices;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType = new int[VertexDataType.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType[VertexDataType.VertexBufferObject.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType[VertexDataType.VertexBufferObjectSubData.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType[VertexDataType.VertexBufferObjectWithVAO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType[VertexDataType.VertexArray.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public enum VertexDataType {
        VertexArray,
        VertexBufferObject,
        VertexBufferObjectSubData,
        VertexBufferObjectWithVAO
    }

    protected Mesh(VertexData vertices, IndexData indices, boolean isVertexArray) {
        this.vertices = vertices;
        this.indices = indices;
        this.isVertexArray = isVertexArray;
        addManagedMesh(Gdx.app, this);
    }

    public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        this.vertices = makeVertexBuffer(isStatic, maxVertices, new VertexAttributes(attributes));
        this.indices = new IndexBufferObject(isStatic, maxIndices);
        this.isVertexArray = false;
        addManagedMesh(Gdx.app, this);
    }

    public Mesh(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
        this.vertices = makeVertexBuffer(isStatic, maxVertices, attributes);
        this.indices = new IndexBufferObject(isStatic, maxIndices);
        this.isVertexArray = false;
        addManagedMesh(Gdx.app, this);
    }

    public Mesh(boolean staticVertices, boolean staticIndices, int maxVertices, int maxIndices, VertexAttributes attributes) {
        this.vertices = makeVertexBuffer(staticVertices, maxVertices, attributes);
        this.indices = new IndexBufferObject(staticIndices, maxIndices);
        this.isVertexArray = false;
        addManagedMesh(Gdx.app, this);
    }

    private VertexData makeVertexBuffer(boolean isStatic, int maxVertices, VertexAttributes vertexAttributes) {
        if (Gdx.gl30 != null) {
            return new VertexBufferObjectWithVAO(isStatic, maxVertices, vertexAttributes);
        }
        return new VertexBufferObject(isStatic, maxVertices, vertexAttributes);
    }

    public Mesh(VertexDataType type, boolean isStatic, int maxVertices, int maxIndices, VertexAttribute... attributes) {
        switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$graphics$Mesh$VertexDataType[type.ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                this.vertices = new VertexBufferObject(isStatic, maxVertices, attributes);
                this.indices = new IndexBufferObject(isStatic, maxIndices);
                this.isVertexArray = false;
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                this.vertices = new VertexBufferObjectSubData(isStatic, maxVertices, attributes);
                this.indices = new IndexBufferObjectSubData(isStatic, maxIndices);
                this.isVertexArray = false;
                break;
            case Base.kNumMidLenBits /*3*/:
                this.vertices = new VertexBufferObjectWithVAO(isStatic, maxVertices, attributes);
                this.indices = new IndexBufferObjectSubData(isStatic, maxIndices);
                this.isVertexArray = false;
                break;
            default:
                this.vertices = new VertexArray(maxVertices, attributes);
                this.indices = new IndexArray(maxIndices);
                this.isVertexArray = true;
                break;
        }
        addManagedMesh(Gdx.app, this);
    }

    public Mesh setVertices(float[] vertices) {
        this.vertices.setVertices(vertices, 0, vertices.length);
        return this;
    }

    public Mesh setVertices(float[] vertices, int offset, int count) {
        this.vertices.setVertices(vertices, offset, count);
        return this;
    }

    public Mesh updateVertices(int targetOffset, float[] source) {
        return updateVertices(targetOffset, source, 0, source.length);
    }

    public Mesh updateVertices(int targetOffset, float[] source, int sourceOffset, int count) {
        this.vertices.updateVertices(targetOffset, source, sourceOffset, count);
        return this;
    }

    public float[] getVertices(float[] vertices) {
        return getVertices(0, -1, vertices);
    }

    public float[] getVertices(int srcOffset, float[] vertices) {
        return getVertices(srcOffset, -1, vertices);
    }

    public float[] getVertices(int srcOffset, int count, float[] vertices) {
        return getVertices(srcOffset, count, vertices, 0);
    }

    public float[] getVertices(int srcOffset, int count, float[] vertices, int destOffset) {
        int max = (getNumVertices() * getVertexSize()) / 4;
        if (count == -1) {
            count = max - srcOffset;
            if (count > vertices.length - destOffset) {
                count = vertices.length - destOffset;
            }
        }
        if (srcOffset < 0 || count <= 0 || srcOffset + count > max || destOffset < 0 || destOffset >= vertices.length) {
            throw new IndexOutOfBoundsException();
        } else if (vertices.length - destOffset < count) {
            throw new IllegalArgumentException("not enough room in vertices array, has " + vertices.length + " floats, needs " + count);
        } else {
            int pos = getVerticesBuffer().position();
            getVerticesBuffer().position(srcOffset);
            getVerticesBuffer().get(vertices, destOffset, count);
            getVerticesBuffer().position(pos);
            return vertices;
        }
    }

    public Mesh setIndices(short[] indices) {
        this.indices.setIndices(indices, 0, indices.length);
        return this;
    }

    public Mesh setIndices(short[] indices, int offset, int count) {
        this.indices.setIndices(indices, offset, count);
        return this;
    }

    public void getIndices(short[] indices) {
        getIndices(indices, 0);
    }

    public void getIndices(short[] indices, int destOffset) {
        getIndices(0, indices, destOffset);
    }

    public void getIndices(int srcOffset, short[] indices, int destOffset) {
        getIndices(srcOffset, -1, indices, destOffset);
    }

    public void getIndices(int srcOffset, int count, short[] indices, int destOffset) {
        int max = getNumIndices();
        if (count < 0) {
            count = max - srcOffset;
        }
        if (srcOffset < 0 || srcOffset >= max || srcOffset + count > max) {
            throw new IllegalArgumentException("Invalid range specified, offset: " + srcOffset + ", count: " + count + ", max: " + max);
        } else if (indices.length - destOffset < count) {
            throw new IllegalArgumentException("not enough room in indices array, has " + indices.length + " shorts, needs " + count);
        } else {
            int pos = getIndicesBuffer().position();
            getIndicesBuffer().position(srcOffset);
            getIndicesBuffer().get(indices, destOffset, count);
            getIndicesBuffer().position(pos);
        }
    }

    public int getNumIndices() {
        return this.indices.getNumIndices();
    }

    public int getNumVertices() {
        return this.vertices.getNumVertices();
    }

    public int getMaxVertices() {
        return this.vertices.getNumMaxVertices();
    }

    public int getMaxIndices() {
        return this.indices.getNumMaxIndices();
    }

    public int getVertexSize() {
        return this.vertices.getAttributes().vertexSize;
    }

    public void setAutoBind(boolean autoBind) {
        this.autoBind = autoBind;
    }

    public void bind(ShaderProgram shader) {
        bind(shader, null);
    }

    public void bind(ShaderProgram shader, int[] locations) {
        this.vertices.bind(shader, locations);
        if (this.indices.getNumIndices() > 0) {
            this.indices.bind();
        }
    }

    public void unbind(ShaderProgram shader) {
        unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        this.vertices.unbind(shader, locations);
        if (this.indices.getNumIndices() > 0) {
            this.indices.unbind();
        }
    }

    public void render(ShaderProgram shader, int primitiveType) {
        render(shader, primitiveType, 0, this.indices.getNumMaxIndices() > 0 ? getNumIndices() : getNumVertices(), this.autoBind);
    }

    public void render(ShaderProgram shader, int primitiveType, int offset, int count) {
        render(shader, primitiveType, offset, count, this.autoBind);
    }

    public void render(ShaderProgram shader, int primitiveType, int offset, int count, boolean autoBind) {
        if (count != 0) {
            if (autoBind) {
                bind(shader);
            }
            if (this.isVertexArray) {
                if (this.indices.getNumIndices() > 0) {
                    Buffer buffer = this.indices.getBuffer();
                    int oldPosition = buffer.position();
                    int oldLimit = buffer.limit();
                    buffer.position(offset);
                    buffer.limit(offset + count);
                    Gdx.gl20.glDrawElements(primitiveType, count, (int) GL20.GL_UNSIGNED_SHORT, buffer);
                    buffer.position(oldPosition);
                    buffer.limit(oldLimit);
                } else {
                    Gdx.gl20.glDrawArrays(primitiveType, offset, count);
                }
            } else if (this.indices.getNumIndices() > 0) {
                Gdx.gl20.glDrawElements(primitiveType, count, (int) GL20.GL_UNSIGNED_SHORT, offset * 2);
            } else {
                Gdx.gl20.glDrawArrays(primitiveType, offset, count);
            }
            if (autoBind) {
                unbind(shader);
            }
        }
    }

    public void dispose() {
        if (meshes.get(Gdx.app) != null) {
            ((Array) meshes.get(Gdx.app)).removeValue(this, true);
        }
        this.vertices.dispose();
        this.indices.dispose();
    }

    public VertexAttribute getVertexAttribute(int usage) {
        VertexAttributes attributes = this.vertices.getAttributes();
        int len = attributes.size();
        for (int i = 0; i < len; i++) {
            if (attributes.get(i).usage == usage) {
                return attributes.get(i);
            }
        }
        return null;
    }

    public VertexAttributes getVertexAttributes() {
        return this.vertices.getAttributes();
    }

    public FloatBuffer getVerticesBuffer() {
        return this.vertices.getBuffer();
    }

    public BoundingBox calculateBoundingBox() {
        BoundingBox bbox = new BoundingBox();
        calculateBoundingBox(bbox);
        return bbox;
    }

    public void calculateBoundingBox(BoundingBox bbox) {
        int numVertices = getNumVertices();
        if (numVertices == 0) {
            throw new GdxRuntimeException("No vertices defined");
        }
        FloatBuffer verts = this.vertices.getBuffer();
        bbox.inf();
        VertexAttribute posAttrib = getVertexAttribute(1);
        int vertexSize = this.vertices.getAttributes().vertexSize / 4;
        int idx = posAttrib.offset / 4;
        int i;
        switch (posAttrib.numComponents) {
            case BuildConfig.VERSION_CODE /*1*/:
                for (i = 0; i < numVertices; i++) {
                    bbox.ext(verts.get(idx), 0.0f, 0.0f);
                    idx += vertexSize;
                }
                return;
            case Base.kNumLenToPosStatesBits /*2*/:
                for (i = 0; i < numVertices; i++) {
                    bbox.ext(verts.get(idx), verts.get(idx + 1), 0.0f);
                    idx += vertexSize;
                }
                return;
            case Base.kNumMidLenBits /*3*/:
                for (i = 0; i < numVertices; i++) {
                    bbox.ext(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
                    idx += vertexSize;
                }
                return;
            default:
                return;
        }
    }

    public BoundingBox calculateBoundingBox(BoundingBox out, int offset, int count) {
        return extendBoundingBox(out.inf(), offset, count);
    }

    public BoundingBox calculateBoundingBox(BoundingBox out, int offset, int count, Matrix4 transform) {
        return extendBoundingBox(out.inf(), offset, count, transform);
    }

    public BoundingBox extendBoundingBox(BoundingBox out, int offset, int count) {
        return extendBoundingBox(out, offset, count, null);
    }

    public BoundingBox extendBoundingBox(BoundingBox out, int offset, int count, Matrix4 transform) {
        int numIndices = getNumIndices();
        if (offset < 0 || count < 1 || offset + count > numIndices) {
            throw new GdxRuntimeException("Not enough indices ( offset=" + offset + ", count=" + count + ", max=" + numIndices + " )");
        }
        FloatBuffer verts = this.vertices.getBuffer();
        ShortBuffer index = this.indices.getBuffer();
        VertexAttribute posAttrib = getVertexAttribute(1);
        int posoff = posAttrib.offset / 4;
        int vertexSize = this.vertices.getAttributes().vertexSize / 4;
        int end = offset + count;
        int i;
        int idx;
        switch (posAttrib.numComponents) {
            case BuildConfig.VERSION_CODE /*1*/:
                for (i = offset; i < end; i++) {
                    this.tmpV.set(verts.get((index.get(i) * vertexSize) + posoff), 0.0f, 0.0f);
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    out.ext(this.tmpV);
                }
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                for (i = offset; i < end; i++) {
                    idx = (index.get(i) * vertexSize) + posoff;
                    this.tmpV.set(verts.get(idx), verts.get(idx + 1), 0.0f);
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    out.ext(this.tmpV);
                }
                break;
            case Base.kNumMidLenBits /*3*/:
                for (i = offset; i < end; i++) {
                    idx = (index.get(i) * vertexSize) + posoff;
                    this.tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    out.ext(this.tmpV);
                }
                break;
        }
        return out;
    }

    public float calculateRadiusSquared(float centerX, float centerY, float centerZ, int offset, int count, Matrix4 transform) {
        int numIndices = getNumIndices();
        if (offset < 0 || count < 1 || offset + count > numIndices) {
            throw new GdxRuntimeException("Not enough indices");
        }
        FloatBuffer verts = this.vertices.getBuffer();
        ShortBuffer index = this.indices.getBuffer();
        VertexAttribute posAttrib = getVertexAttribute(1);
        int posoff = posAttrib.offset / 4;
        int vertexSize = this.vertices.getAttributes().vertexSize / 4;
        int end = offset + count;
        float result = 0.0f;
        int i;
        float r;
        int idx;
        switch (posAttrib.numComponents) {
            case BuildConfig.VERSION_CODE /*1*/:
                for (i = offset; i < end; i++) {
                    this.tmpV.set(verts.get((index.get(i) * vertexSize) + posoff), 0.0f, 0.0f);
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    r = this.tmpV.sub(centerX, centerY, centerZ).len2();
                    if (r > result) {
                        result = r;
                    }
                }
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                for (i = offset; i < end; i++) {
                    idx = (index.get(i) * vertexSize) + posoff;
                    this.tmpV.set(verts.get(idx), verts.get(idx + 1), 0.0f);
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    r = this.tmpV.sub(centerX, centerY, centerZ).len2();
                    if (r > result) {
                        result = r;
                    }
                }
                break;
            case Base.kNumMidLenBits /*3*/:
                for (i = offset; i < end; i++) {
                    idx = (index.get(i) * vertexSize) + posoff;
                    this.tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
                    if (transform != null) {
                        this.tmpV.mul(transform);
                    }
                    r = this.tmpV.sub(centerX, centerY, centerZ).len2();
                    if (r > result) {
                        result = r;
                    }
                }
                break;
        }
        return result;
    }

    public float calculateRadius(float centerX, float centerY, float centerZ, int offset, int count, Matrix4 transform) {
        return (float) Math.sqrt((double) calculateRadiusSquared(centerX, centerY, centerZ, offset, count, transform));
    }

    public float calculateRadius(Vector3 center, int offset, int count, Matrix4 transform) {
        return calculateRadius(center.x, center.y, center.z, offset, count, transform);
    }

    public float calculateRadius(float centerX, float centerY, float centerZ, int offset, int count) {
        return calculateRadius(centerX, centerY, centerZ, offset, count, null);
    }

    public float calculateRadius(Vector3 center, int offset, int count) {
        return calculateRadius(center.x, center.y, center.z, offset, count, null);
    }

    public float calculateRadius(float centerX, float centerY, float centerZ) {
        return calculateRadius(centerX, centerY, centerZ, 0, getNumIndices(), null);
    }

    public float calculateRadius(Vector3 center) {
        return calculateRadius(center.x, center.y, center.z, 0, getNumIndices(), null);
    }

    public ShortBuffer getIndicesBuffer() {
        return this.indices.getBuffer();
    }

    private static void addManagedMesh(Application app, Mesh mesh) {
        Array<Mesh> managedResources = (Array) meshes.get(app);
        if (managedResources == null) {
            managedResources = new Array();
        }
        managedResources.add(mesh);
        meshes.put(app, managedResources);
    }

    public static void invalidateAllMeshes(Application app) {
        Array<Mesh> meshesArray = (Array) meshes.get(app);
        if (meshesArray != null) {
            for (int i = 0; i < meshesArray.size; i++) {
                ((Mesh) meshesArray.get(i)).vertices.invalidate();
                ((Mesh) meshesArray.get(i)).indices.invalidate();
            }
        }
    }

    public static void clearAllMeshes(Application app) {
        meshes.remove(app);
    }

    public static String getManagedStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append("Managed meshes/app: { ");
        for (Application app : meshes.keySet()) {
            builder.append(((Array) meshes.get(app)).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        VertexAttribute posAttr = getVertexAttribute(1);
        int offset = posAttr.offset / 4;
        int numComponents = posAttr.numComponents;
        int numVertices = getNumVertices();
        int vertexSize = getVertexSize() / 4;
        float[] vertices = new float[(numVertices * vertexSize)];
        getVertices(vertices);
        int idx = offset;
        int i;
        int i2;
        switch (numComponents) {
            case BuildConfig.VERSION_CODE /*1*/:
                for (i = 0; i < numVertices; i++) {
                    vertices[idx] = vertices[idx] * scaleX;
                    idx += vertexSize;
                }
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                for (i = 0; i < numVertices; i++) {
                    vertices[idx] = vertices[idx] * scaleX;
                    i2 = idx + 1;
                    vertices[i2] = vertices[i2] * scaleY;
                    idx += vertexSize;
                }
                break;
            case Base.kNumMidLenBits /*3*/:
                for (i = 0; i < numVertices; i++) {
                    vertices[idx] = vertices[idx] * scaleX;
                    i2 = idx + 1;
                    vertices[i2] = vertices[i2] * scaleY;
                    i2 = idx + 2;
                    vertices[i2] = vertices[i2] * scaleZ;
                    idx += vertexSize;
                }
                break;
        }
        setVertices(vertices);
    }

    public void transform(Matrix4 matrix) {
        transform(matrix, 0, getNumVertices());
    }

    public void transform(Matrix4 matrix, int start, int count) {
        VertexAttribute posAttr = getVertexAttribute(1);
        int posOffset = posAttr.offset / 4;
        int stride = getVertexSize() / 4;
        int numComponents = posAttr.numComponents;
        int numVertices = getNumVertices();
        float[] vertices = new float[(count * stride)];
        getVertices(start * stride, count * stride, vertices);
        transform(matrix, vertices, stride, posOffset, numComponents, 0, count);
        updateVertices(start * stride, vertices);
    }

    public static void transform(Matrix4 matrix, float[] vertices, int vertexSize, int offset, int dimensions, int start, int count) {
        if (offset < 0 || dimensions < 1 || offset + dimensions > vertexSize) {
            throw new IndexOutOfBoundsException();
        } else if (start < 0 || count < 1 || (start + count) * vertexSize > vertices.length) {
            throw new IndexOutOfBoundsException("start = " + start + ", count = " + count + ", vertexSize = " + vertexSize + ", length = " + vertices.length);
        } else {
            Vector3 tmp = new Vector3();
            int idx = offset + (start * vertexSize);
            int i;
            switch (dimensions) {
                case BuildConfig.VERSION_CODE /*1*/:
                    for (i = 0; i < count; i++) {
                        tmp.set(vertices[idx], 0.0f, 0.0f).mul(matrix);
                        vertices[idx] = tmp.x;
                        idx += vertexSize;
                    }
                    return;
                case Base.kNumLenToPosStatesBits /*2*/:
                    for (i = 0; i < count; i++) {
                        tmp.set(vertices[idx], vertices[idx + 1], 0.0f).mul(matrix);
                        vertices[idx] = tmp.x;
                        vertices[idx + 1] = tmp.y;
                        idx += vertexSize;
                    }
                    return;
                case Base.kNumMidLenBits /*3*/:
                    for (i = 0; i < count; i++) {
                        tmp.set(vertices[idx], vertices[idx + 1], vertices[idx + 2]).mul(matrix);
                        vertices[idx] = tmp.x;
                        vertices[idx + 1] = tmp.y;
                        vertices[idx + 2] = tmp.z;
                        idx += vertexSize;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public void transformUV(Matrix3 matrix) {
        transformUV(matrix, 0, getNumVertices());
    }

    protected void transformUV(Matrix3 matrix, int start, int count) {
        int offset = getVertexAttribute(16).offset / 4;
        int vertexSize = getVertexSize() / 4;
        float[] vertices = new float[(getNumVertices() * vertexSize)];
        getVertices(0, vertices.length, vertices);
        transformUV(matrix, vertices, vertexSize, offset, start, count);
        setVertices(vertices, 0, vertices.length);
    }

    public static void transformUV(Matrix3 matrix, float[] vertices, int vertexSize, int offset, int start, int count) {
        if (start < 0 || count < 1 || (start + count) * vertexSize > vertices.length) {
            throw new IndexOutOfBoundsException("start = " + start + ", count = " + count + ", vertexSize = " + vertexSize + ", length = " + vertices.length);
        }
        Vector2 tmp = new Vector2();
        int idx = offset + (start * vertexSize);
        for (int i = 0; i < count; i++) {
            tmp.set(vertices[idx], vertices[idx + 1]).mul(matrix);
            vertices[idx] = tmp.x;
            vertices[idx + 1] = tmp.y;
            idx += vertexSize;
        }
    }

    public Mesh copy(boolean isStatic, boolean removeDuplicates, int[] usage) {
        int size;
        int i;
        int idx;
        int j;
        short vertexSize = getVertexSize() / 4;
        int numVertices = getNumVertices();
        float[] vertices = new float[(numVertices * vertexSize)];
        getVertices(0, vertices.length, vertices);
        short[] checks = null;
        VertexAttribute[] attrs = null;
        short newVertexSize = (short) 0;
        if (usage != null) {
            size = 0;
            int as = 0;
            for (i = 0; i < usage.length; i++) {
                if (getVertexAttribute(usage[i]) != null) {
                    size += getVertexAttribute(usage[i]).numComponents;
                    as++;
                }
            }
            if (size > 0) {
                attrs = new VertexAttribute[as];
                checks = new short[size];
                idx = -1;
                int ai = -1;
                for (int vertexAttribute : usage) {
                    VertexAttribute a = getVertexAttribute(vertexAttribute);
                    if (a != null) {
                        for (j = 0; j < a.numComponents; j++) {
                            idx++;
                            checks[idx] = (short) (a.offset + j);
                        }
                        ai++;
                        attrs[ai] = new VertexAttribute(a.usage, a.numComponents, a.alias);
                        newVertexSize += a.numComponents;
                    }
                }
            }
        }
        if (checks == null) {
            checks = new short[vertexSize];
            for (short i2 = (short) 0; i2 < vertexSize; i2 = (short) (i2 + 1)) {
                checks[i2] = i2;
            }
            newVertexSize = vertexSize;
        }
        int numIndices = getNumIndices();
        short[] indices = null;
        if (numIndices > 0) {
            indices = new short[numIndices];
            getIndices(indices);
            if (removeDuplicates || newVertexSize != vertexSize) {
                float[] tmp = new float[vertices.length];
                size = 0;
                for (i = 0; i < numIndices; i++) {
                    int idx1 = indices[i] * vertexSize;
                    short newIndex = (short) -1;
                    if (removeDuplicates) {
                        for (short j2 = (short) 0; j2 < size && newIndex < (short) 0; j2 = (short) (j2 + 1)) {
                            int idx2 = j2 * newVertexSize;
                            boolean found = true;
                            for (int k = 0; k < checks.length && found; k++) {
                                if (tmp[idx2 + k] != vertices[checks[k] + idx1]) {
                                    found = false;
                                }
                            }
                            if (found) {
                                newIndex = j2;
                            }
                        }
                    }
                    if (newIndex > (short) 0) {
                        indices[i] = newIndex;
                    } else {
                        idx = size * newVertexSize;
                        for (j = 0; j < checks.length; j++) {
                            tmp[idx + j] = vertices[checks[j] + idx1];
                        }
                        indices[i] = (short) size;
                        size++;
                    }
                }
                vertices = tmp;
                numVertices = size;
            }
        }
        Mesh mesh;
        if (attrs == null) {
            mesh = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, getVertexAttributes());
        } else {
            mesh = new Mesh(isStatic, numVertices, indices == null ? 0 : indices.length, attrs);
        }
        result.setVertices(vertices, 0, numVertices * newVertexSize);
        result.setIndices(indices);
        return result;
    }

    public Mesh copy(boolean isStatic) {
        return copy(isStatic, false, null);
    }
}
