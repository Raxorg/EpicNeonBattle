package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.utils.MeshPartBuilder.VertexInfo;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.NumberUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ShortArray;
import java.util.Iterator;

public class MeshBuilder implements MeshPartBuilder {
    private static IntIntMap indicesMap = null;
    private static final Array<Matrix4> matrices4Array = new Array();
    private static final Pool<Matrix4> matrices4Pool = new Pool<Matrix4>() {
        protected Matrix4 newObject() {
            return new Matrix4();
        }
    };
    private static final ShortArray tmpIndices = new ShortArray();
    private static final FloatArray tmpVertices = new FloatArray();
    private static final Vector3 vTmp = new Vector3();
    private static final Array<Vector3> vectorArray = new Array();
    private static final Pool<Vector3> vectorPool = new Pool<Vector3>() {
        protected Vector3 newObject() {
            return new Vector3();
        }
    };
    private VertexAttributes attributes;
    private int colOffset;
    private int colSize;
    private final Color color = new Color(Color.WHITE);
    private int cpOffset;
    private boolean hasColor = false;
    private boolean hasUVTransform = false;
    private ShortArray indices = new ShortArray();
    private int istart;
    private short lastIndex = (short) -1;
    private final Matrix4 matTmp1 = new Matrix4();
    private int norOffset;
    private final Matrix3 normalTransform = new Matrix3();
    private MeshPart part;
    private Array<MeshPart> parts = new Array();
    private int posOffset;
    private int posSize;
    private final Matrix4 positionTransform = new Matrix4();
    private int primitiveType;
    private int stride;
    private final Color tempC1 = new Color();
    private final Vector3 tempV1 = new Vector3();
    private final Vector3 tempV2 = new Vector3();
    private final Vector3 tempV3 = new Vector3();
    private final Vector3 tempV4 = new Vector3();
    private final Vector3 tempV5 = new Vector3();
    private final Vector3 tempV6 = new Vector3();
    private final Vector3 tempV7 = new Vector3();
    private final Vector3 tempV8 = new Vector3();
    private final Vector3 tmpNormal = new Vector3();
    private float uOffset = 0.0f;
    private float uScale = 1.0f;
    private int uvOffset;
    private float vOffset = 0.0f;
    private float vScale = 1.0f;
    private final VertexInfo vertTmp1 = new VertexInfo();
    private final VertexInfo vertTmp2 = new VertexInfo();
    private final VertexInfo vertTmp3 = new VertexInfo();
    private final VertexInfo vertTmp4 = new VertexInfo();
    private final VertexInfo vertTmp5 = new VertexInfo();
    private final VertexInfo vertTmp6 = new VertexInfo();
    private final VertexInfo vertTmp7 = new VertexInfo();
    private final VertexInfo vertTmp8 = new VertexInfo();
    private float[] vertex;
    private boolean vertexTransformationEnabled = false;
    private FloatArray vertices = new FloatArray();
    private short vindex;

    public static VertexAttributes createAttributes(long usage) {
        Array<VertexAttribute> attrs = new Array();
        if ((usage & 1) == 1) {
            attrs.add(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE));
        }
        if ((usage & 2) == 2) {
            attrs.add(new VertexAttribute(2, 4, ShaderProgram.COLOR_ATTRIBUTE));
        }
        if ((4 & usage) == 4) {
            attrs.add(new VertexAttribute(4, 4, ShaderProgram.COLOR_ATTRIBUTE));
        }
        if ((8 & usage) == 8) {
            attrs.add(new VertexAttribute(8, 3, ShaderProgram.NORMAL_ATTRIBUTE));
        }
        if ((16 & usage) == 16) {
            attrs.add(new VertexAttribute(16, 2, "a_texCoord0"));
        }
        VertexAttribute[] attributes = new VertexAttribute[attrs.size];
        for (int i = 0; i < attributes.length; i++) {
            attributes[i] = (VertexAttribute) attrs.get(i);
        }
        return new VertexAttributes(attributes);
    }

    public void begin(long attributes) {
        begin(createAttributes(attributes), -1);
    }

    public void begin(VertexAttributes attributes) {
        begin(attributes, -1);
    }

    public void begin(long attributes, int primitiveType) {
        begin(createAttributes(attributes), primitiveType);
    }

    public void begin(VertexAttributes attributes, int primitiveType) {
        int i = -1;
        if (this.attributes != null) {
            throw new RuntimeException("Call end() first");
        }
        this.attributes = attributes;
        this.vertices.clear();
        this.indices.clear();
        this.parts.clear();
        this.vindex = (short) 0;
        this.istart = 0;
        this.part = null;
        this.stride = attributes.vertexSize / 4;
        if (this.vertex == null || this.vertex.length < this.stride) {
            this.vertex = new float[this.stride];
        }
        VertexAttribute a = attributes.findByUsage(1);
        if (a == null) {
            throw new GdxRuntimeException("Cannot build mesh without position attribute");
        }
        this.posOffset = a.offset / 4;
        this.posSize = a.numComponents;
        a = attributes.findByUsage(8);
        this.norOffset = a == null ? -1 : a.offset / 4;
        a = attributes.findByUsage(2);
        this.colOffset = a == null ? -1 : a.offset / 4;
        this.colSize = a == null ? 0 : a.numComponents;
        a = attributes.findByUsage(4);
        this.cpOffset = a == null ? -1 : a.offset / 4;
        a = attributes.findByUsage(16);
        if (a != null) {
            i = a.offset / 4;
        }
        this.uvOffset = i;
        setColor(null);
        setVertexTransform(null);
        setUVRange(null);
        this.primitiveType = primitiveType;
    }

    private void endpart() {
        if (this.part != null) {
            this.part.indexOffset = this.istart;
            this.part.numVertices = this.indices.size - this.istart;
            this.istart = this.indices.size;
            this.part = null;
        }
    }

    public MeshPart part(String id, int primitiveType) {
        return part(id, primitiveType, new MeshPart());
    }

    public MeshPart part(String id, int primitiveType, MeshPart meshPart) {
        if (this.attributes == null) {
            throw new RuntimeException("Call begin() first");
        }
        endpart();
        this.part = meshPart;
        this.part.id = id;
        this.part.primitiveType = primitiveType;
        this.primitiveType = primitiveType;
        this.parts.add(this.part);
        setColor(null);
        setVertexTransform(null);
        setUVRange(null);
        return this.part;
    }

    public Mesh end(Mesh mesh) {
        endpart();
        if (this.attributes == null) {
            throw new GdxRuntimeException("Call begin() first");
        } else if (!this.attributes.equals(mesh.getVertexAttributes())) {
            throw new GdxRuntimeException("Mesh attributes don't match");
        } else if (mesh.getMaxVertices() * this.stride < this.vertices.size) {
            throw new GdxRuntimeException("Mesh can't hold enough vertices: " + mesh.getMaxVertices() + " * " + this.stride + " < " + this.vertices.size);
        } else if (mesh.getMaxIndices() < this.indices.size) {
            throw new GdxRuntimeException("Mesh can't hold enough indices: " + mesh.getMaxIndices() + " < " + this.indices.size);
        } else {
            mesh.setVertices(this.vertices.items, 0, this.vertices.size);
            mesh.setIndices(this.indices.items, 0, this.indices.size);
            Iterator it = this.parts.iterator();
            while (it.hasNext()) {
                ((MeshPart) it.next()).mesh = mesh;
            }
            this.parts.clear();
            this.attributes = null;
            this.vertices.clear();
            this.indices.clear();
            return mesh;
        }
    }

    public Mesh end() {
        return end(new Mesh(true, this.vertices.size / this.stride, this.indices.size, this.attributes));
    }

    public void clear() {
        this.vertices.clear();
        this.indices.clear();
        this.parts.clear();
        this.vindex = (short) 0;
        this.istart = 0;
        this.part = null;
    }

    public int getFloatsPerVertex() {
        return this.stride;
    }

    public int getNumVertices() {
        return this.vertices.size / this.stride;
    }

    public void getVertices(float[] out, int destOffset) {
        if (this.attributes == null) {
            throw new GdxRuntimeException("Must be called in between #begin and #end");
        } else if (destOffset < 0 || destOffset > out.length - this.vertices.size) {
            throw new GdxRuntimeException("Array to small or offset out of range");
        } else {
            System.arraycopy(this.vertices.items, 0, out, destOffset, this.vertices.size);
        }
    }

    protected float[] getVertices() {
        return this.vertices.items;
    }

    public int getNumIndices() {
        return this.indices.size;
    }

    public void getIndices(short[] out, int destOffset) {
        if (this.attributes == null) {
            throw new GdxRuntimeException("Must be called in between #begin and #end");
        } else if (destOffset < 0 || destOffset > out.length - this.indices.size) {
            throw new GdxRuntimeException("Array to small or offset out of range");
        } else {
            System.arraycopy(this.indices.items, 0, out, destOffset, this.indices.size);
        }
    }

    protected short[] getIndices() {
        return this.indices.items;
    }

    public VertexAttributes getAttributes() {
        return this.attributes;
    }

    public MeshPart getMeshPart() {
        return this.part;
    }

    private Vector3 tmp(float x, float y, float z) {
        Vector3 result = ((Vector3) vectorPool.obtain()).set(x, y, z);
        vectorArray.add(result);
        return result;
    }

    private Vector3 tmp(Vector3 copyFrom) {
        return tmp(copyFrom.x, copyFrom.y, copyFrom.z);
    }

    private Matrix4 tmp() {
        Matrix4 result = ((Matrix4) matrices4Pool.obtain()).idt();
        matrices4Array.add(result);
        return result;
    }

    private Matrix4 tmp(Matrix4 copyFrom) {
        return tmp().set(copyFrom);
    }

    private void cleanup() {
        vectorPool.freeAll(vectorArray);
        vectorArray.clear();
        matrices4Pool.freeAll(matrices4Array);
        matrices4Array.clear();
    }

    public void setColor(float r, float g, float b, float a) {
        this.color.set(r, g, b, a);
        this.hasColor = !this.color.equals(Color.WHITE);
    }

    public void setColor(Color color) {
        Color color2 = this.color;
        boolean z = color != null;
        this.hasColor = z;
        if (!z) {
            color = Color.WHITE;
        }
        color2.set(color);
    }

    public void setUVRange(float u1, float v1, float u2, float v2) {
        this.uOffset = u1;
        this.vOffset = v1;
        this.uScale = u2 - u1;
        this.vScale = v2 - v1;
        boolean z = (MathUtils.isZero(u1) && MathUtils.isZero(v1) && MathUtils.isEqual(u2, 1.0f) && MathUtils.isEqual(v2, 1.0f)) ? false : true;
        this.hasUVTransform = z;
    }

    public void setUVRange(TextureRegion region) {
        boolean z = region != null;
        this.hasUVTransform = z;
        if (z) {
            setUVRange(region.getU(), region.getV(), region.getU2(), region.getV2());
            return;
        }
        this.vOffset = 0.0f;
        this.uOffset = 0.0f;
        this.vScale = 1.0f;
        this.uScale = 1.0f;
    }

    public Matrix4 getVertexTransform(Matrix4 out) {
        return out.set(this.positionTransform);
    }

    public void setVertexTransform(Matrix4 transform) {
        boolean z = transform != null;
        this.vertexTransformationEnabled = z;
        if (z) {
            this.positionTransform.set(transform);
            this.normalTransform.set(transform).inv().transpose();
            return;
        }
        this.positionTransform.idt();
        this.normalTransform.idt();
    }

    public boolean isVertexTransformationEnabled() {
        return this.vertexTransformationEnabled;
    }

    public void setVertexTransformationEnabled(boolean enabled) {
        this.vertexTransformationEnabled = enabled;
    }

    public void ensureVertices(int numVertices) {
        this.vertices.ensureCapacity(this.stride * numVertices);
    }

    public void ensureIndices(int numIndices) {
        this.indices.ensureCapacity(numIndices);
    }

    public void ensureCapacity(int numVertices, int numIndices) {
        ensureVertices(numVertices);
        ensureIndices(numIndices);
    }

    public void ensureTriangleIndices(int numTriangles) {
        if (this.primitiveType == 1) {
            ensureIndices(numTriangles * 6);
        } else if (this.primitiveType == 4 || this.primitiveType == 0) {
            ensureIndices(numTriangles * 3);
        } else {
            throw new GdxRuntimeException("Incorrect primtive type");
        }
    }

    public void ensureTriangles(int numVertices, int numTriangles) {
        ensureVertices(numVertices);
        ensureTriangleIndices(numTriangles);
    }

    public void ensureTriangles(int numTriangles) {
        ensureTriangles(numTriangles * 3, numTriangles);
    }

    public void ensureRectangleIndices(int numRectangles) {
        if (this.primitiveType == 0) {
            ensureIndices(numRectangles * 4);
        } else if (this.primitiveType == 1) {
            ensureIndices(numRectangles * 8);
        } else {
            ensureIndices(numRectangles * 6);
        }
    }

    public void ensureRectangles(int numVertices, int numRectangles) {
        ensureVertices(numVertices);
        ensureRectangleIndices(numRectangles);
    }

    public void ensureRectangles(int numRectangles) {
        ensureRectangles(numRectangles * 4, numRectangles);
    }

    public short lastIndex() {
        return this.lastIndex;
    }

    private static final void transformPosition(float[] values, int offset, int size, Matrix4 transform) {
        if (size > 2) {
            vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform);
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
            values[offset + 2] = vTmp.z;
        } else if (size > 1) {
            vTmp.set(values[offset], values[offset + 1], 0.0f).mul(transform);
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
        } else {
            values[offset] = vTmp.set(values[offset], 0.0f, 0.0f).mul(transform).x;
        }
    }

    private static final void transformNormal(float[] values, int offset, int size, Matrix3 transform) {
        if (size > 2) {
            vTmp.set(values[offset], values[offset + 1], values[offset + 2]).mul(transform).nor();
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
            values[offset + 2] = vTmp.z;
        } else if (size > 1) {
            vTmp.set(values[offset], values[offset + 1], 0.0f).mul(transform).nor();
            values[offset] = vTmp.x;
            values[offset + 1] = vTmp.y;
        } else {
            values[offset] = vTmp.set(values[offset], 0.0f, 0.0f).mul(transform).nor().x;
        }
    }

    private final void addVertex(float[] values, int offset) {
        int o = this.vertices.size;
        this.vertices.addAll(values, offset, this.stride);
        short s = this.vindex;
        this.vindex = (short) (s + 1);
        this.lastIndex = s;
        if (this.vertexTransformationEnabled) {
            transformPosition(this.vertices.items, this.posOffset + o, this.posSize, this.positionTransform);
            if (this.norOffset >= 0) {
                transformNormal(this.vertices.items, this.norOffset + o, 3, this.normalTransform);
            }
        }
        if (this.hasColor) {
            if (this.colOffset >= 0) {
                float[] fArr = this.vertices.items;
                int i = this.colOffset + o;
                fArr[i] = fArr[i] * this.color.r;
                fArr = this.vertices.items;
                i = (this.colOffset + o) + 1;
                fArr[i] = fArr[i] * this.color.g;
                fArr = this.vertices.items;
                i = (this.colOffset + o) + 2;
                fArr[i] = fArr[i] * this.color.b;
                if (this.colSize > 3) {
                    fArr = this.vertices.items;
                    i = (this.colOffset + o) + 3;
                    fArr[i] = fArr[i] * this.color.a;
                }
            } else if (this.cpOffset >= 0) {
                this.vertices.items[this.cpOffset + o] = this.tempC1.set(NumberUtils.floatToIntColor(this.vertices.items[this.cpOffset + o])).mul(this.color).toFloatBits();
            }
        }
        if (this.hasUVTransform && this.uvOffset >= 0) {
            this.vertices.items[this.uvOffset + o] = this.uOffset + (this.uScale * this.vertices.items[this.uvOffset + o]);
            this.vertices.items[(this.uvOffset + o) + 1] = this.vOffset + (this.vScale * this.vertices.items[(this.uvOffset + o) + 1]);
        }
    }

    public short vertex(Vector3 pos, Vector3 nor, Color col, Vector2 uv) {
        if (this.vindex >= Short.MAX_VALUE) {
            throw new GdxRuntimeException("Too many vertices used");
        }
        this.vertex[this.posOffset] = pos.x;
        if (this.posSize > 1) {
            this.vertex[this.posOffset + 1] = pos.y;
        }
        if (this.posSize > 2) {
            this.vertex[this.posOffset + 2] = pos.z;
        }
        if (this.norOffset >= 0) {
            if (nor == null) {
                nor = this.tmpNormal.set(pos).nor();
            }
            this.vertex[this.norOffset] = nor.x;
            this.vertex[this.norOffset + 1] = nor.y;
            this.vertex[this.norOffset + 2] = nor.z;
        }
        if (this.colOffset >= 0) {
            if (col == null) {
                col = Color.WHITE;
            }
            this.vertex[this.colOffset] = col.r;
            this.vertex[this.colOffset + 1] = col.g;
            this.vertex[this.colOffset + 2] = col.b;
            if (this.colSize > 3) {
                this.vertex[this.colOffset + 3] = col.a;
            }
        } else if (this.cpOffset > 0) {
            if (col == null) {
                col = Color.WHITE;
            }
            this.vertex[this.cpOffset] = col.toFloatBits();
        }
        if (uv != null && this.uvOffset >= 0) {
            this.vertex[this.uvOffset] = uv.x;
            this.vertex[this.uvOffset + 1] = uv.y;
        }
        addVertex(this.vertex, 0);
        return this.lastIndex;
    }

    public short vertex(float... values) {
        int n = values.length - this.stride;
        int i = 0;
        while (i <= n) {
            addVertex(values, i);
            i += this.stride;
        }
        return this.lastIndex;
    }

    public short vertex(VertexInfo info) {
        Vector2 vector2 = null;
        Vector3 vector3 = info.hasPosition ? info.position : null;
        Vector3 vector32 = info.hasNormal ? info.normal : null;
        Color color = info.hasColor ? info.color : null;
        if (info.hasUV) {
            vector2 = info.uv;
        }
        return vertex(vector3, vector32, color, vector2);
    }

    public void index(short value) {
        this.indices.add(value);
    }

    public void index(short value1, short value2) {
        ensureIndices(2);
        this.indices.add(value1);
        this.indices.add(value2);
    }

    public void index(short value1, short value2, short value3) {
        ensureIndices(3);
        this.indices.add(value1);
        this.indices.add(value2);
        this.indices.add(value3);
    }

    public void index(short value1, short value2, short value3, short value4) {
        ensureIndices(4);
        this.indices.add(value1);
        this.indices.add(value2);
        this.indices.add(value3);
        this.indices.add(value4);
    }

    public void index(short value1, short value2, short value3, short value4, short value5, short value6) {
        ensureIndices(6);
        this.indices.add(value1);
        this.indices.add(value2);
        this.indices.add(value3);
        this.indices.add(value4);
        this.indices.add(value5);
        this.indices.add(value6);
    }

    public void index(short value1, short value2, short value3, short value4, short value5, short value6, short value7, short value8) {
        ensureIndices(8);
        this.indices.add(value1);
        this.indices.add(value2);
        this.indices.add(value3);
        this.indices.add(value4);
        this.indices.add(value5);
        this.indices.add(value6);
        this.indices.add(value7);
        this.indices.add(value8);
    }

    public void line(short index1, short index2) {
        if (this.primitiveType != 1) {
            throw new GdxRuntimeException("Incorrect primitive type");
        }
        index(index1, index2);
    }

    public void line(VertexInfo p1, VertexInfo p2) {
        ensureVertices(2);
        line(vertex(p1), vertex(p2));
    }

    public void line(Vector3 p1, Vector3 p2) {
        line(this.vertTmp1.set(p1, null, null, null), this.vertTmp2.set(p2, null, null, null));
    }

    public void line(float x1, float y1, float z1, float x2, float y2, float z2) {
        line(this.vertTmp1.set(null, null, null, null).setPos(x1, y1, z1), this.vertTmp2.set(null, null, null, null).setPos(x2, y2, z2));
    }

    public void line(Vector3 p1, Color c1, Vector3 p2, Color c2) {
        line(this.vertTmp1.set(p1, null, c1, null), this.vertTmp2.set(p2, null, c2, null));
    }

    public void triangle(short index1, short index2, short index3) {
        if (this.primitiveType == 4 || this.primitiveType == 0) {
            index(index1, index2, index3);
        } else if (this.primitiveType == 1) {
            index(index1, index2, index2, index3, index3, index1);
        } else {
            throw new GdxRuntimeException("Incorrect primitive type");
        }
    }

    public void triangle(VertexInfo p1, VertexInfo p2, VertexInfo p3) {
        ensureVertices(3);
        triangle(vertex(p1), vertex(p2), vertex(p3));
    }

    public void triangle(Vector3 p1, Vector3 p2, Vector3 p3) {
        triangle(this.vertTmp1.set(p1, null, null, null), this.vertTmp2.set(p2, null, null, null), this.vertTmp3.set(p3, null, null, null));
    }

    public void triangle(Vector3 p1, Color c1, Vector3 p2, Color c2, Vector3 p3, Color c3) {
        triangle(this.vertTmp1.set(p1, null, c1, null), this.vertTmp2.set(p2, null, c2, null), this.vertTmp3.set(p3, null, c3, null));
    }

    public void rect(short corner00, short corner10, short corner11, short corner01) {
        if (this.primitiveType == 4) {
            index(corner00, corner10, corner11, corner11, corner01, corner00);
        } else if (this.primitiveType == 1) {
            index(corner00, corner10, corner10, corner11, corner11, corner01, corner01, corner00);
        } else if (this.primitiveType == 0) {
            index(corner00, corner10, corner11, corner01);
        } else {
            throw new GdxRuntimeException("Incorrect primitive type");
        }
    }

    public void rect(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01) {
        ensureVertices(4);
        rect(vertex(corner00), vertex(corner10), vertex(corner11), vertex(corner01));
    }

    public void rect(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal) {
        rect(this.vertTmp1.set(corner00, normal, null, null).setUV(0.0f, 1.0f), this.vertTmp2.set(corner10, normal, null, null).setUV(1.0f, 1.0f), this.vertTmp3.set(corner11, normal, null, null).setUV(1.0f, 0.0f), this.vertTmp4.set(corner01, normal, null, null).setUV(0.0f, 0.0f));
    }

    public void rect(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ) {
        rect(this.vertTmp1.set(null, null, null, null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(0.0f, 1.0f), this.vertTmp2.set(null, null, null, null).setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(1.0f, 1.0f), this.vertTmp3.set(null, null, null, null).setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(1.0f, 0.0f), this.vertTmp4.set(null, null, null, null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(0.0f, 0.0f));
    }

    public void patch(VertexInfo corner00, VertexInfo corner10, VertexInfo corner11, VertexInfo corner01, int divisionsU, int divisionsV) {
        if (divisionsU < 1 || divisionsV < 1) {
            throw new GdxRuntimeException("divisionsU and divisionV must be > 0, u,v: " + divisionsU + ", " + divisionsV);
        }
        ensureRectangles((divisionsV + 1) * (divisionsU + 1), divisionsV * divisionsU);
        for (int u = 0; u <= divisionsU; u++) {
            float alphaU = ((float) u) / ((float) divisionsU);
            this.vertTmp5.set(corner00).lerp(corner10, alphaU);
            this.vertTmp6.set(corner01).lerp(corner11, alphaU);
            int v = 0;
            while (v <= divisionsV) {
                short idx = vertex(this.vertTmp7.set(this.vertTmp5).lerp(this.vertTmp6, ((float) v) / ((float) divisionsV)));
                if (u > 0 && v > 0) {
                    rect((short) ((idx - divisionsV) - 2), (short) (idx - 1), idx, (short) ((idx - divisionsV) - 1));
                }
                v++;
            }
        }
    }

    public void patch(Vector3 corner00, Vector3 corner10, Vector3 corner11, Vector3 corner01, Vector3 normal, int divisionsU, int divisionsV) {
        patch(this.vertTmp1.set(corner00, normal, null, null).setUV(0.0f, 1.0f), this.vertTmp2.set(corner10, normal, null, null).setUV(1.0f, 1.0f), this.vertTmp3.set(corner11, normal, null, null).setUV(1.0f, 0.0f), this.vertTmp4.set(corner01, normal, null, null).setUV(0.0f, 0.0f), divisionsU, divisionsV);
    }

    public void patch(float x00, float y00, float z00, float x10, float y10, float z10, float x11, float y11, float z11, float x01, float y01, float z01, float normalX, float normalY, float normalZ, int divisionsU, int divisionsV) {
        patch(this.vertTmp1.set(null).setPos(x00, y00, z00).setNor(normalX, normalY, normalZ).setUV(0.0f, 1.0f), this.vertTmp2.set(null).setPos(x10, y10, z10).setNor(normalX, normalY, normalZ).setUV(1.0f, 1.0f), this.vertTmp3.set(null).setPos(x11, y11, z11).setNor(normalX, normalY, normalZ).setUV(1.0f, 0.0f), this.vertTmp4.set(null).setPos(x01, y01, z01).setNor(normalX, normalY, normalZ).setUV(0.0f, 0.0f), divisionsU, divisionsV);
    }

    public void box(VertexInfo corner000, VertexInfo corner010, VertexInfo corner100, VertexInfo corner110, VertexInfo corner001, VertexInfo corner011, VertexInfo corner101, VertexInfo corner111) {
        ensureVertices(8);
        short i000 = vertex(corner000);
        short i100 = vertex(corner100);
        short i110 = vertex(corner110);
        short i010 = vertex(corner010);
        short i001 = vertex(corner001);
        short i101 = vertex(corner101);
        short i111 = vertex(corner111);
        short i011 = vertex(corner011);
        if (this.primitiveType == 1) {
            ensureIndices(24);
            rect(i000, i100, i110, i010);
            rect(i101, i001, i011, i111);
            index(i000, i001, i010, i011, i110, i111, i100, i101);
        } else if (this.primitiveType == 0) {
            ensureRectangleIndices(2);
            rect(i000, i100, i110, i010);
            rect(i101, i001, i011, i111);
        } else {
            ensureRectangleIndices(6);
            rect(i000, i100, i110, i010);
            rect(i101, i001, i011, i111);
            rect(i000, i010, i011, i001);
            rect(i101, i111, i110, i100);
            rect(i101, i100, i000, i001);
            rect(i110, i111, i011, i010);
        }
    }

    public void box(Vector3 corner000, Vector3 corner010, Vector3 corner100, Vector3 corner110, Vector3 corner001, Vector3 corner011, Vector3 corner101, Vector3 corner111) {
        if (this.norOffset >= 0 || this.uvOffset >= 0) {
            ensureRectangles(6);
            Vector3 nor = this.tempV1.set(corner000).lerp(corner110, 0.5f).sub(this.tempV2.set(corner001).lerp(corner111, 0.5f)).nor();
            rect(corner000, corner010, corner110, corner100, nor);
            rect(corner011, corner001, corner101, corner111, nor.scl(-1.0f));
            nor = this.tempV1.set(corner000).lerp(corner101, 0.5f).sub(this.tempV2.set(corner010).lerp(corner111, 0.5f)).nor();
            rect(corner001, corner000, corner100, corner101, nor);
            rect(corner010, corner011, corner111, corner110, nor.scl(-1.0f));
            nor = this.tempV1.set(corner000).lerp(corner011, 0.5f).sub(this.tempV2.set(corner100).lerp(corner111, 0.5f)).nor();
            rect(corner001, corner011, corner010, corner000, nor);
            rect(corner100, corner110, corner111, corner101, nor.scl(-1.0f));
            return;
        }
        box(this.vertTmp1.set(corner000, null, null, null), this.vertTmp2.set(corner010, null, null, null), this.vertTmp3.set(corner100, null, null, null), this.vertTmp4.set(corner110, null, null, null), this.vertTmp5.set(corner001, null, null, null), this.vertTmp6.set(corner011, null, null, null), this.vertTmp7.set(corner101, null, null, null), this.vertTmp8.set(corner111, null, null, null));
    }

    public void box(Matrix4 transform) {
        box(tmp(-0.5f, -0.5f, -0.5f).mul(transform), tmp(-0.5f, 0.5f, -0.5f).mul(transform), tmp(0.5f, -0.5f, -0.5f).mul(transform), tmp(0.5f, 0.5f, -0.5f).mul(transform), tmp(-0.5f, -0.5f, 0.5f).mul(transform), tmp(-0.5f, 0.5f, 0.5f).mul(transform), tmp(0.5f, -0.5f, 0.5f).mul(transform), tmp(0.5f, 0.5f, 0.5f).mul(transform));
        cleanup();
    }

    public void box(float width, float height, float depth) {
        box(this.matTmp1.setToScaling(width, height, depth));
    }

    public void box(float x, float y, float z, float width, float height, float depth) {
        box(this.matTmp1.setToScaling(width, height, depth).trn(x, y, z));
    }

    public void circle(float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ) {
        circle(radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0.0f, 360.0f);
    }

    public void circle(float radius, int divisions, Vector3 center, Vector3 normal) {
        circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
    }

    public void circle(float radius, int divisions, Vector3 center, Vector3 normal, Vector3 tangent, Vector3 binormal) {
        circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z);
    }

    public void circle(float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ) {
        circle(radius, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, 0.0f, 360.0f);
    }

    public void circle(float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
        ellipse(radius * 2.0f, radius * 2.0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
    }

    public void circle(float radius, int divisions, Vector3 center, Vector3 normal, float angleFrom, float angleTo) {
        circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
    }

    public void circle(float radius, int divisions, Vector3 center, Vector3 normal, Vector3 tangent, Vector3 binormal, float angleFrom, float angleTo) {
        circle(radius, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
    }

    public void circle(float radius, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
        ellipse(radius * 2.0f, radius * 2.0f, 0.0f, 0.0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ) {
        ellipse(width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0.0f, 360.0f);
    }

    public void ellipse(float width, float height, int divisions, Vector3 center, Vector3 normal) {
        ellipse(width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z);
    }

    public void ellipse(float width, float height, int divisions, Vector3 center, Vector3 normal, Vector3 tangent, Vector3 binormal) {
        ellipse(width, height, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z);
    }

    public void ellipse(float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ) {
        ellipse(width, height, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, 0.0f, 360.0f);
    }

    public void ellipse(float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
        ellipse(width, height, 0.0f, 0.0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, int divisions, Vector3 center, Vector3 normal, float angleFrom, float angleTo) {
        ellipse(width, height, 0.0f, 0.0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, int divisions, Vector3 center, Vector3 normal, Vector3 tangent, Vector3 binormal, float angleFrom, float angleTo) {
        ellipse(width, height, 0.0f, 0.0f, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, tangent.x, tangent.y, tangent.z, binormal.x, binormal.y, binormal.z, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
        ellipse(width, height, 0.0f, 0.0f, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, tangentX, tangentY, tangentZ, binormalX, binormalY, binormalZ, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, float innerWidth, float innerHeight, int divisions, Vector3 center, Vector3 normal) {
        ellipse(width, height, innerWidth, innerHeight, divisions, center.x, center.y, center.z, normal.x, normal.y, normal.z, 0.0f, 360.0f);
    }

    public void ellipse(float width, float height, float innerWidth, float innerHeight, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ) {
        ellipse(width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, 0.0f, 360.0f);
    }

    public void ellipse(float width, float height, float innerWidth, float innerHeight, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float angleFrom, float angleTo) {
        this.tempV1.set(normalX, normalY, normalZ).crs(0.0f, 0.0f, 1.0f);
        this.tempV2.set(normalX, normalY, normalZ).crs(0.0f, 1.0f, 0.0f);
        if (this.tempV2.len2() > this.tempV1.len2()) {
            this.tempV1.set(this.tempV2);
        }
        this.tempV2.set(this.tempV1.nor()).crs(normalX, normalY, normalZ).nor();
        ellipse(width, height, innerWidth, innerHeight, divisions, centerX, centerY, centerZ, normalX, normalY, normalZ, this.tempV1.x, this.tempV1.y, this.tempV1.z, this.tempV2.x, this.tempV2.y, this.tempV2.z, angleFrom, angleTo);
    }

    public void ellipse(float width, float height, float innerWidth, float innerHeight, int divisions, float centerX, float centerY, float centerZ, float normalX, float normalY, float normalZ, float tangentX, float tangentY, float tangentZ, float binormalX, float binormalY, float binormalZ, float angleFrom, float angleTo) {
        if (innerWidth <= 0.0f || innerHeight <= 0.0f) {
            ensureTriangles(divisions + 2, divisions);
        } else if (innerWidth == width && innerHeight == height) {
            ensureVertices(divisions + 1);
            ensureIndices(divisions + 1);
            if (this.primitiveType != 1) {
                throw new GdxRuntimeException("Incorrect primitive type : expect GL_LINES because innerWidth == width && innerHeight == height");
            }
        } else {
            ensureRectangles((divisions + 1) * 2, divisions + 1);
        }
        float ao = MathUtils.degreesToRadians * angleFrom;
        float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / ((float) divisions);
        Vector3 sxEx = this.tempV1.set(tangentX, tangentY, tangentZ).scl(0.5f * width);
        Vector3 syEx = this.tempV2.set(binormalX, binormalY, binormalZ).scl(0.5f * height);
        Vector3 sxIn = this.tempV3.set(tangentX, tangentY, tangentZ).scl(0.5f * innerWidth);
        Vector3 syIn = this.tempV4.set(binormalX, binormalY, binormalZ).scl(0.5f * innerHeight);
        VertexInfo currIn = this.vertTmp3.set(null, null, null, null);
        currIn.hasNormal = true;
        currIn.hasPosition = true;
        currIn.hasUV = true;
        currIn.uv.set(0.5f, 0.5f);
        currIn.position.set(centerX, centerY, centerZ);
        currIn.normal.set(normalX, normalY, normalZ);
        VertexInfo currEx = this.vertTmp4.set(null, null, null, null);
        currEx.hasNormal = true;
        currEx.hasPosition = true;
        currEx.hasUV = true;
        currEx.uv.set(0.5f, 0.5f);
        currEx.position.set(centerX, centerY, centerZ);
        currEx.normal.set(normalX, normalY, normalZ);
        short center = vertex(currEx);
        float us = 0.5f * (innerWidth / width);
        float vs = 0.5f * (innerHeight / height);
        short i2 = (short) 0;
        short i3 = (short) 0;
        short i4 = (short) 0;
        for (int i = 0; i <= divisions; i++) {
            float angle = ao + (((float) i) * step);
            float x = MathUtils.cos(angle);
            float y = MathUtils.sin(angle);
            currEx.position.set(centerX, centerY, centerZ).add((sxEx.x * x) + (syEx.x * y), (sxEx.y * x) + (syEx.y * y), (sxEx.z * x) + (syEx.z * y));
            currEx.uv.set(0.5f + (0.5f * x), 0.5f + (0.5f * y));
            short i1 = vertex(currEx);
            if (innerWidth <= 0.0f || innerHeight <= 0.0f) {
                if (i != 0) {
                    triangle(i1, i2, center);
                }
                i2 = i1;
            } else if (innerWidth == width && innerHeight == height) {
                if (i != 0) {
                    line(i1, i2);
                }
                i2 = i1;
            } else {
                currIn.position.set(centerX, centerY, centerZ).add((sxIn.x * x) + (syIn.x * y), (sxIn.y * x) + (syIn.y * y), (sxIn.z * x) + (syIn.z * y));
                currIn.uv.set(0.5f + (us * x), 0.5f + (vs * y));
                i2 = i1;
                i1 = vertex(currIn);
                if (i != 0) {
                    rect(i1, i2, i4, i3);
                }
                i4 = i2;
                i3 = i1;
            }
        }
    }

    public void cylinder(float width, float height, float depth, int divisions) {
        cylinder(width, height, depth, divisions, 0.0f, 360.0f);
    }

    public void cylinder(float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
        cylinder(width, height, depth, divisions, angleFrom, angleTo, true);
    }

    public void cylinder(float width, float height, float depth, int divisions, float angleFrom, float angleTo, boolean close) {
        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float hd = depth * 0.5f;
        float ao = MathUtils.degreesToRadians * angleFrom;
        float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / ((float) divisions);
        float us = 1.0f / ((float) divisions);
        VertexInfo curr1 = this.vertTmp3.set(null, null, null, null);
        curr1.hasNormal = true;
        curr1.hasPosition = true;
        curr1.hasUV = true;
        VertexInfo curr2 = this.vertTmp4.set(null, null, null, null);
        curr2.hasNormal = true;
        curr2.hasPosition = true;
        curr2.hasUV = true;
        short i3 = (short) 0;
        short i4 = (short) 0;
        ensureRectangles((divisions + 1) * 2, divisions);
        for (int i = 0; i <= divisions; i++) {
            float angle = ao + (((float) i) * step);
            float u = 1.0f - (((float) i) * us);
            curr1.position.set(MathUtils.cos(angle) * hw, 0.0f, MathUtils.sin(angle) * hd);
            curr1.normal.set(curr1.position).nor();
            curr1.position.y = -hh;
            curr1.uv.set(u, 1.0f);
            curr2.position.set(curr1.position);
            curr2.normal.set(curr1.normal);
            curr2.position.y = hh;
            curr2.uv.set(u, 0.0f);
            short i2 = vertex(curr1);
            short i1 = vertex(curr2);
            if (i != 0) {
                rect(i3, i1, i2, i4);
            }
            i4 = i2;
            i3 = i1;
        }
        if (close) {
            ellipse(width, depth, 0.0f, 0.0f, divisions, 0.0f, hh, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, angleFrom, angleTo);
            ellipse(width, depth, 0.0f, 0.0f, divisions, 0.0f, -hh, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 180.0f - angleTo, 180.0f - angleFrom);
        }
    }

    public void cone(float width, float height, float depth, int divisions) {
        cone(width, height, depth, divisions, 0.0f, 360.0f);
    }

    public void cone(float width, float height, float depth, int divisions, float angleFrom, float angleTo) {
        ensureTriangles(divisions + 2, divisions);
        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float hd = depth * 0.5f;
        float ao = MathUtils.degreesToRadians * angleFrom;
        float step = (MathUtils.degreesToRadians * (angleTo - angleFrom)) / ((float) divisions);
        float us = 1.0f / ((float) divisions);
        VertexInfo curr1 = this.vertTmp3.set(null, null, null, null);
        curr1.hasNormal = true;
        curr1.hasPosition = true;
        curr1.hasUV = true;
        short base = vertex(this.vertTmp4.set(null, null, null, null).setPos(0.0f, hh, 0.0f).setNor(0.0f, 1.0f, 0.0f).setUV(0.5f, 0.0f));
        short i2 = (short) 0;
        for (int i = 0; i <= divisions; i++) {
            float angle = ao + (((float) i) * step);
            float u = 1.0f - (((float) i) * us);
            curr1.position.set(MathUtils.cos(angle) * hw, 0.0f, MathUtils.sin(angle) * hd);
            curr1.normal.set(curr1.position).nor();
            curr1.position.y = -hh;
            curr1.uv.set(u, 1.0f);
            short i1 = vertex(curr1);
            if (i != 0) {
                triangle(base, i1, i2);
            }
            i2 = i1;
        }
        ellipse(width, depth, 0.0f, 0.0f, divisions, 0.0f, -hh, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 180.0f - angleTo, 180.0f - angleFrom);
    }

    public void sphere(float width, float height, float depth, int divisionsU, int divisionsV) {
        sphere(width, height, depth, divisionsU, divisionsV, 0.0f, 360.0f, 0.0f, 180.0f);
    }

    public void sphere(Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV) {
        sphere(transform, width, height, depth, divisionsU, divisionsV, 0.0f, 360.0f, 0.0f, 180.0f);
    }

    public void sphere(float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
        sphere(this.matTmp1.idt(), width, height, depth, divisionsU, divisionsV, angleUFrom, angleUTo, angleVFrom, angleVTo);
    }

    public void sphere(Matrix4 transform, float width, float height, float depth, int divisionsU, int divisionsV, float angleUFrom, float angleUTo, float angleVFrom, float angleVTo) {
        float hw = width * 0.5f;
        float hh = height * 0.5f;
        float hd = depth * 0.5f;
        float auo = MathUtils.degreesToRadians * angleUFrom;
        float stepU = (MathUtils.degreesToRadians * (angleUTo - angleUFrom)) / ((float) divisionsU);
        float avo = MathUtils.degreesToRadians * angleVFrom;
        float stepV = (MathUtils.degreesToRadians * (angleVTo - angleVFrom)) / ((float) divisionsV);
        float us = 1.0f / ((float) divisionsU);
        float vs = 1.0f / ((float) divisionsV);
        VertexInfo curr1 = this.vertTmp3.set(null, null, null, null);
        curr1.hasNormal = true;
        curr1.hasPosition = true;
        curr1.hasUV = true;
        int s = divisionsU + 3;
        tmpIndices.clear();
        tmpIndices.ensureCapacity(divisionsU * 2);
        tmpIndices.size = s;
        int tempOffset = 0;
        ensureRectangles((divisionsV + 1) * (divisionsU + 1), divisionsV * divisionsU);
        for (int iv = 0; iv <= divisionsV; iv++) {
            float angleV = avo + (((float) iv) * stepV);
            float v = vs * ((float) iv);
            float t = MathUtils.sin(angleV);
            float h = MathUtils.cos(angleV) * hh;
            int iu = 0;
            while (iu <= divisionsU) {
                float angleU = auo + (((float) iu) * stepU);
                float u = 1.0f - (((float) iu) * us);
                curr1.position.set((MathUtils.cos(angleU) * hw) * t, h, (MathUtils.sin(angleU) * hd) * t).mul(transform);
                curr1.normal.set(curr1.position).nor();
                curr1.uv.set(u, v);
                tmpIndices.set(tempOffset, vertex(curr1));
                int o = tempOffset + s;
                if (iv > 0 && iu > 0) {
                    rect(tmpIndices.get(tempOffset), tmpIndices.get((o - 1) % s), tmpIndices.get((o - (divisionsU + 2)) % s), tmpIndices.get((o - (divisionsU + 1)) % s));
                }
                tempOffset = (tempOffset + 1) % tmpIndices.size;
                iu++;
            }
        }
    }

    public void capsule(float radius, float height, int divisions) {
        if (height < 2.0f * radius) {
            throw new GdxRuntimeException("Height must be at least twice the radius");
        }
        float d = 2.0f * radius;
        cylinder(d, height - d, d, divisions, 0.0f, 360.0f, false);
        sphere(this.matTmp1.setToTranslation(0.0f, 0.5f * (height - d), 0.0f), d, d, d, divisions, divisions, 0.0f, 360.0f, 0.0f, 90.0f);
        sphere(this.matTmp1.setToTranslation(0.0f, -0.5f * (height - d), 0.0f), d, d, d, divisions, divisions, 0.0f, 360.0f, 90.0f, 180.0f);
    }

    public void arrow(float x1, float y1, float z1, float x2, float y2, float z2, float capLength, float stemThickness, int divisions) {
        Vector3 begin = tmp(x1, y1, z1);
        Vector3 end = tmp(x2, y2, z2);
        float length = begin.dst(end);
        float coneHeight = length * capLength;
        float coneDiameter = 2.0f * ((float) (((double) coneHeight) * Math.sqrt(0.3333333432674408d)));
        float stemLength = length - coneHeight;
        float stemDiameter = coneDiameter * stemThickness;
        Vector3 up = tmp(end).sub(begin).nor();
        Vector3 forward = tmp(up).crs(Vector3.Z);
        if (forward.isZero()) {
            forward.set(Vector3.X);
        }
        forward.crs(up).nor();
        Vector3 left = tmp(up).crs(forward).nor();
        Vector3 direction = tmp(end).sub(begin).nor();
        Matrix4 userTransform = getVertexTransform(tmp());
        Matrix4 transform = tmp();
        float[] val = transform.val;
        val[0] = left.x;
        val[4] = up.x;
        val[8] = forward.x;
        val[1] = left.y;
        val[5] = up.y;
        val[9] = forward.y;
        val[2] = left.z;
        val[6] = up.z;
        val[10] = forward.z;
        Matrix4 temp = tmp();
        transform.setTranslation(tmp(direction).scl(stemLength / 2.0f).add(x1, y1, z1));
        setVertexTransform(temp.set(transform).mul(userTransform));
        cylinder(stemDiameter, stemLength, stemDiameter, divisions);
        transform.setTranslation(tmp(direction).scl(stemLength).add(x1, y1, z1));
        setVertexTransform(temp.set(transform).mul(userTransform));
        cone(coneDiameter, coneHeight, coneDiameter, divisions);
        setVertexTransform(userTransform);
        cleanup();
    }

    public void addMesh(Mesh mesh) {
        addMesh(mesh, 0, mesh.getNumIndices());
    }

    public void addMesh(MeshPart meshpart) {
        if (meshpart.primitiveType != this.primitiveType) {
            throw new GdxRuntimeException("Primitive type doesn't match");
        }
        addMesh(meshpart.mesh, meshpart.indexOffset, meshpart.numVertices);
    }

    public void addMesh(Mesh mesh, int indexOffset, int numIndices) {
        if (!this.attributes.equals(mesh.getVertexAttributes())) {
            throw new GdxRuntimeException("Vertex attributes do not match");
        } else if (numIndices > 0) {
            int numFloats = mesh.getNumVertices() * this.stride;
            tmpVertices.clear();
            tmpVertices.ensureCapacity(numFloats);
            tmpVertices.size = numFloats;
            mesh.getVertices(tmpVertices.items);
            tmpIndices.clear();
            tmpIndices.ensureCapacity(numIndices);
            tmpIndices.size = numIndices;
            mesh.getIndices(indexOffset, numIndices, tmpIndices.items, 0);
            addMesh(tmpVertices.items, tmpIndices.items, 0, numIndices);
        }
    }

    private void addMesh(float[] vertices, short[] indices, int indexOffset, int numIndices) {
        int length;
        if (indicesMap == null) {
            indicesMap = new IntIntMap(numIndices);
        } else {
            indicesMap.clear();
            indicesMap.ensureCapacity(numIndices);
        }
        ensureIndices(numIndices);
        if (vertices.length < numIndices) {
            length = vertices.length;
        } else {
            length = numIndices;
        }
        ensureVertices(length);
        for (int i = 0; i < numIndices; i++) {
            int sidx = indices[i];
            int didx = indicesMap.get(sidx, -1);
            if (didx < 0) {
                addVertex(vertices, this.stride * sidx);
                IntIntMap intIntMap = indicesMap;
                didx = this.lastIndex;
                intIntMap.put(sidx, didx);
            }
            index((short) didx);
        }
    }
}
