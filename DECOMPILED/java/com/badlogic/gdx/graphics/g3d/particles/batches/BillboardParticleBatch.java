package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.AlignMode;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.BillboardControllerRenderData;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public class BillboardParticleBatch extends BufferedParticleBatch<BillboardControllerRenderData> {
    private static final VertexAttributes CPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(16, 2, "a_texCoord0"), new VertexAttribute(2, 4, ShaderProgram.COLOR_ATTRIBUTE));
    private static final int CPU_COLOR_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(2).offset / 4));
    private static final int CPU_POSITION_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(1).offset / 4));
    private static final int CPU_UV_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(16).offset / 4));
    private static final int CPU_VERTEX_SIZE = (CPU_ATTRIBUTES.vertexSize / 4);
    private static final VertexAttributes GPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(16, 2, "a_texCoord0"), new VertexAttribute(2, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(sizeAndRotationUsage, 4, "a_sizeAndRotation"));
    private static final int GPU_COLOR_OFFSET = ((short) (GPU_ATTRIBUTES.findByUsage(2).offset / 4));
    private static final int GPU_POSITION_OFFSET = ((short) (GPU_ATTRIBUTES.findByUsage(1).offset / 4));
    private static final int GPU_SIZE_ROTATION_OFFSET = ((short) (GPU_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset / 4));
    private static final int GPU_UV_OFFSET = ((short) (GPU_ATTRIBUTES.findByUsage(16).offset / 4));
    private static final int GPU_VERTEX_SIZE = (GPU_ATTRIBUTES.vertexSize / 4);
    private static final int MAX_PARTICLES_PER_MESH = 8191;
    private static final int MAX_VERTICES_PER_MESH = 32764;
    protected static final Matrix3 TMP_M3 = new Matrix3();
    protected static final Vector3 TMP_V1 = new Vector3();
    protected static final Vector3 TMP_V2 = new Vector3();
    protected static final Vector3 TMP_V3 = new Vector3();
    protected static final Vector3 TMP_V4 = new Vector3();
    protected static final Vector3 TMP_V5 = new Vector3();
    protected static final Vector3 TMP_V6 = new Vector3();
    protected static final int directionUsage = 1024;
    protected static final int sizeAndRotationUsage = 512;
    protected BlendingAttribute blendingAttribute;
    private VertexAttributes currentAttributes;
    private int currentVertexSize;
    protected DepthTestAttribute depthTestAttribute;
    private short[] indices;
    protected AlignMode mode;
    private RenderablePool renderablePool;
    private Array<Renderable> renderables;
    Shader shader;
    protected Texture texture;
    protected boolean useGPU;
    private float[] vertices;

    public static class Config {
        AlignMode mode;
        boolean useGPU;

        public Config(boolean useGPU, AlignMode mode) {
            this.useGPU = useGPU;
            this.mode = mode;
        }
    }

    private class RenderablePool extends Pool<Renderable> {
        public Renderable newObject() {
            return BillboardParticleBatch.this.allocRenderable();
        }
    }

    public BillboardParticleBatch(AlignMode mode, boolean useGPU, int capacity, BlendingAttribute blendingAttribute, DepthTestAttribute depthTestAttribute) {
        super(BillboardControllerRenderData.class);
        this.currentVertexSize = GPU_VERTEX_SIZE;
        this.useGPU = false;
        this.mode = AlignMode.Screen;
        this.renderables = new Array();
        this.renderablePool = new RenderablePool();
        this.blendingAttribute = blendingAttribute;
        this.depthTestAttribute = depthTestAttribute;
        if (this.blendingAttribute == null) {
            this.blendingAttribute = new BlendingAttribute(1, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f);
        }
        if (this.depthTestAttribute == null) {
            this.depthTestAttribute = new DepthTestAttribute(GL20.GL_LEQUAL, false);
        }
        allocIndices();
        initRenderData();
        ensureCapacity(capacity);
        setUseGpu(useGPU);
        setAlignMode(mode);
    }

    public BillboardParticleBatch(AlignMode mode, boolean useGPU, int capacity) {
        this(mode, useGPU, capacity, null, null);
    }

    public BillboardParticleBatch() {
        this(AlignMode.Screen, false, 100);
    }

    public BillboardParticleBatch(int capacity) {
        this(AlignMode.Screen, false, capacity);
    }

    public void allocParticlesData(int capacity) {
        this.vertices = new float[((this.currentVertexSize * 4) * capacity)];
        allocRenderables(capacity);
    }

    protected Renderable allocRenderable() {
        Renderable renderable = new Renderable();
        renderable.primitiveType = 4;
        renderable.meshPartOffset = GPU_VERTEX_SIZE;
        renderable.material = new Material(this.blendingAttribute, this.depthTestAttribute, TextureAttribute.createDiffuse(this.texture));
        renderable.mesh = new Mesh(false, (int) MAX_VERTICES_PER_MESH, 49146, this.currentAttributes);
        renderable.mesh.setIndices(this.indices);
        renderable.shader = this.shader;
        return renderable;
    }

    private void allocIndices() {
        this.indices = new short[49146];
        int i = GPU_VERTEX_SIZE;
        int vertex = GPU_VERTEX_SIZE;
        while (i < 49146) {
            this.indices[i] = (short) vertex;
            this.indices[i + 1] = (short) (vertex + 1);
            this.indices[i + 2] = (short) (vertex + 2);
            this.indices[i + 3] = (short) (vertex + 2);
            this.indices[i + 4] = (short) (vertex + 3);
            this.indices[i + 5] = (short) vertex;
            i += 6;
            vertex += 4;
        }
    }

    private void allocRenderables(int capacity) {
        int meshCount = MathUtils.ceil((float) (capacity / MAX_PARTICLES_PER_MESH));
        int free = this.renderablePool.getFree();
        if (free < meshCount) {
            int left = meshCount - free;
            for (int i = GPU_VERTEX_SIZE; i < left; i++) {
                this.renderablePool.free(this.renderablePool.newObject());
            }
        }
    }

    private Shader getShader(Renderable renderable) {
        Shader shader = this.useGPU ? new ParticleShader(renderable, new com.badlogic.gdx.graphics.g3d.particles.ParticleShader.Config(this.mode)) : new DefaultShader(renderable);
        shader.init();
        return shader;
    }

    private void allocShader() {
        Renderable newRenderable = allocRenderable();
        Shader shader = getShader(newRenderable);
        newRenderable.shader = shader;
        this.shader = shader;
        this.renderablePool.free(newRenderable);
    }

    private void clearRenderablesPool() {
        this.renderablePool.freeAll(this.renderables);
        int free = this.renderablePool.getFree();
        for (int i = GPU_VERTEX_SIZE; i < free; i++) {
            ((Renderable) this.renderablePool.obtain()).mesh.dispose();
        }
        this.renderables.clear();
    }

    public void setVertexData() {
        if (this.useGPU) {
            this.currentAttributes = GPU_ATTRIBUTES;
            this.currentVertexSize = GPU_VERTEX_SIZE;
            return;
        }
        this.currentAttributes = CPU_ATTRIBUTES;
        this.currentVertexSize = CPU_VERTEX_SIZE;
    }

    private void initRenderData() {
        setVertexData();
        clearRenderablesPool();
        allocShader();
        resetCapacity();
    }

    public void setAlignMode(AlignMode mode) {
        if (mode != this.mode) {
            this.mode = mode;
            if (this.useGPU) {
                initRenderData();
                allocRenderables(this.bufferedParticlesCount);
            }
        }
    }

    public AlignMode getAlignMode() {
        return this.mode;
    }

    public void setUseGpu(boolean useGPU) {
        if (this.useGPU != useGPU) {
            this.useGPU = useGPU;
            initRenderData();
            allocRenderables(this.bufferedParticlesCount);
        }
    }

    public boolean isUseGPU() {
        return this.useGPU;
    }

    public void setTexture(Texture texture) {
        this.renderablePool.freeAll(this.renderables);
        this.renderables.clear();
        int free = this.renderablePool.getFree();
        for (int i = GPU_VERTEX_SIZE; i < free; i++) {
            ((TextureAttribute) ((Renderable) this.renderablePool.obtain()).material.get(TextureAttribute.Diffuse)).textureDescription.texture = texture;
        }
        this.texture = texture;
    }

    public Texture getTexture() {
        return this.texture;
    }

    public void begin() {
        super.begin();
        this.renderablePool.freeAll(this.renderables);
        this.renderables.clear();
    }

    private static void putVertex(float[] vertices, int offset, float x, float y, float z, float u, float v, float scaleX, float scaleY, float cosRotation, float sinRotation, float r, float g, float b, float a) {
        vertices[GPU_POSITION_OFFSET + offset] = x;
        vertices[(GPU_POSITION_OFFSET + offset) + 1] = y;
        vertices[(GPU_POSITION_OFFSET + offset) + 2] = z;
        vertices[GPU_UV_OFFSET + offset] = u;
        vertices[(GPU_UV_OFFSET + offset) + 1] = v;
        vertices[GPU_SIZE_ROTATION_OFFSET + offset] = scaleX;
        vertices[(GPU_SIZE_ROTATION_OFFSET + offset) + 1] = scaleY;
        vertices[(GPU_SIZE_ROTATION_OFFSET + offset) + 2] = cosRotation;
        vertices[(GPU_SIZE_ROTATION_OFFSET + offset) + 3] = sinRotation;
        vertices[GPU_COLOR_OFFSET + offset] = r;
        vertices[(GPU_COLOR_OFFSET + offset) + 1] = g;
        vertices[(GPU_COLOR_OFFSET + offset) + 2] = b;
        vertices[(GPU_COLOR_OFFSET + offset) + 3] = a;
    }

    private static void putVertex(float[] vertices, int offset, Vector3 p, float u, float v, float r, float g, float b, float a) {
        vertices[CPU_POSITION_OFFSET + offset] = p.x;
        vertices[(CPU_POSITION_OFFSET + offset) + 1] = p.y;
        vertices[(CPU_POSITION_OFFSET + offset) + 2] = p.z;
        vertices[CPU_UV_OFFSET + offset] = u;
        vertices[(CPU_UV_OFFSET + offset) + 1] = v;
        vertices[CPU_COLOR_OFFSET + offset] = r;
        vertices[(CPU_COLOR_OFFSET + offset) + 1] = g;
        vertices[(CPU_COLOR_OFFSET + offset) + 2] = b;
        vertices[(CPU_COLOR_OFFSET + offset) + 3] = a;
    }

    private void fillVerticesGPU(int[] particlesOffset) {
        int tp = GPU_VERTEX_SIZE;
        Iterator it = this.renderData.iterator();
        while (it.hasNext()) {
            BillboardControllerRenderData data = (BillboardControllerRenderData) it.next();
            FloatChannel scaleChannel = data.scaleChannel;
            FloatChannel regionChannel = data.regionChannel;
            FloatChannel positionChannel = data.positionChannel;
            FloatChannel colorChannel = data.colorChannel;
            FloatChannel rotationChannel = data.rotationChannel;
            int p = GPU_VERTEX_SIZE;
            int c = data.controller.particles.size;
            while (p < c) {
                int baseOffset = (particlesOffset[tp] * this.currentVertexSize) * 4;
                float scale = scaleChannel.data[scaleChannel.strideSize * p];
                int regionOffset = p * regionChannel.strideSize;
                int positionOffset = p * positionChannel.strideSize;
                int colorOffset = p * colorChannel.strideSize;
                int rotationOffset = p * rotationChannel.strideSize;
                float px = positionChannel.data[positionOffset + GPU_VERTEX_SIZE];
                float py = positionChannel.data[positionOffset + 1];
                float pz = positionChannel.data[positionOffset + 2];
                float u = regionChannel.data[regionOffset + GPU_VERTEX_SIZE];
                float v = regionChannel.data[regionOffset + 1];
                float u2 = regionChannel.data[regionOffset + 2];
                float v2 = regionChannel.data[regionOffset + 3];
                float sx = regionChannel.data[regionOffset + 4] * scale;
                float sy = regionChannel.data[regionOffset + 5] * scale;
                float r = colorChannel.data[colorOffset + GPU_VERTEX_SIZE];
                float g = colorChannel.data[colorOffset + 1];
                float b = colorChannel.data[colorOffset + 2];
                float a = colorChannel.data[colorOffset + 3];
                float cosRotation = rotationChannel.data[rotationOffset + GPU_VERTEX_SIZE];
                float sinRotation = rotationChannel.data[rotationOffset + 1];
                putVertex(this.vertices, baseOffset, px, py, pz, u, v2, -sx, -sy, cosRotation, sinRotation, r, g, b, a);
                baseOffset += this.currentVertexSize;
                putVertex(this.vertices, baseOffset, px, py, pz, u2, v2, sx, -sy, cosRotation, sinRotation, r, g, b, a);
                baseOffset += this.currentVertexSize;
                putVertex(this.vertices, baseOffset, px, py, pz, u2, v, sx, sy, cosRotation, sinRotation, r, g, b, a);
                baseOffset += this.currentVertexSize;
                putVertex(this.vertices, baseOffset, px, py, pz, u, v, -sx, sy, cosRotation, sinRotation, r, g, b, a);
                int i = this.currentVertexSize + baseOffset;
                p++;
                tp++;
            }
        }
    }

    private void fillVerticesToViewPointCPU(int[] particlesOffset) {
        int tp = GPU_VERTEX_SIZE;
        Iterator it = this.renderData.iterator();
        while (it.hasNext()) {
            BillboardControllerRenderData data = (BillboardControllerRenderData) it.next();
            FloatChannel scaleChannel = data.scaleChannel;
            FloatChannel regionChannel = data.regionChannel;
            FloatChannel positionChannel = data.positionChannel;
            FloatChannel colorChannel = data.colorChannel;
            FloatChannel rotationChannel = data.rotationChannel;
            int p = GPU_VERTEX_SIZE;
            int c = data.controller.particles.size;
            while (p < c) {
                int baseOffset = (particlesOffset[tp] * this.currentVertexSize) * 4;
                float scale = scaleChannel.data[scaleChannel.strideSize * p];
                int regionOffset = p * regionChannel.strideSize;
                int positionOffset = p * positionChannel.strideSize;
                int colorOffset = p * colorChannel.strideSize;
                int rotationOffset = p * rotationChannel.strideSize;
                float px = positionChannel.data[positionOffset + GPU_VERTEX_SIZE];
                float py = positionChannel.data[positionOffset + 1];
                float pz = positionChannel.data[positionOffset + 2];
                float u = regionChannel.data[regionOffset + GPU_VERTEX_SIZE];
                float v = regionChannel.data[regionOffset + 1];
                float u2 = regionChannel.data[regionOffset + 2];
                float v2 = regionChannel.data[regionOffset + 3];
                float sx = regionChannel.data[regionOffset + 4] * scale;
                float sy = regionChannel.data[regionOffset + 5] * scale;
                float r = colorChannel.data[colorOffset + GPU_VERTEX_SIZE];
                float g = colorChannel.data[colorOffset + 1];
                float b = colorChannel.data[colorOffset + 2];
                float a = colorChannel.data[colorOffset + 3];
                float cosRotation = rotationChannel.data[rotationOffset + GPU_VERTEX_SIZE];
                float sinRotation = rotationChannel.data[rotationOffset + 1];
                Vector3 look = TMP_V3.set(this.camera.position).sub(px, py, pz).nor();
                Vector3 right = TMP_V1.set(this.camera.up).crs(look).nor();
                Vector3 up = TMP_V2.set(look).crs(right);
                right.scl(sx);
                up.scl(sy);
                if (cosRotation != 1.0f) {
                    TMP_M3.setToRotation(look, cosRotation, sinRotation);
                    putVertex(this.vertices, baseOffset, TMP_V6.set((-TMP_V1.x) - TMP_V2.x, (-TMP_V1.y) - TMP_V2.y, (-TMP_V1.z) - TMP_V2.z).mul(TMP_M3).add(px, py, pz), u, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set(TMP_V1.x - TMP_V2.x, TMP_V1.y - TMP_V2.y, TMP_V1.z - TMP_V2.z).mul(TMP_M3).add(px, py, pz), u2, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set(TMP_V1.x + TMP_V2.x, TMP_V1.y + TMP_V2.y, TMP_V1.z + TMP_V2.z).mul(TMP_M3).add(px, py, pz), u2, v, r, g, b, a);
                    putVertex(this.vertices, baseOffset + this.currentVertexSize, TMP_V6.set((-TMP_V1.x) + TMP_V2.x, (-TMP_V1.y) + TMP_V2.y, (-TMP_V1.z) + TMP_V2.z).mul(TMP_M3).add(px, py, pz), u, v, r, g, b, a);
                } else {
                    putVertex(this.vertices, baseOffset, TMP_V6.set(((-TMP_V1.x) - TMP_V2.x) + px, ((-TMP_V1.y) - TMP_V2.y) + py, ((-TMP_V1.z) - TMP_V2.z) + pz), u, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set((TMP_V1.x - TMP_V2.x) + px, (TMP_V1.y - TMP_V2.y) + py, (TMP_V1.z - TMP_V2.z) + pz), u2, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set((TMP_V1.x + TMP_V2.x) + px, (TMP_V1.y + TMP_V2.y) + py, (TMP_V1.z + TMP_V2.z) + pz), u2, v, r, g, b, a);
                    putVertex(this.vertices, baseOffset + this.currentVertexSize, TMP_V6.set(((-TMP_V1.x) + TMP_V2.x) + px, ((-TMP_V1.y) + TMP_V2.y) + py, ((-TMP_V1.z) + TMP_V2.z) + pz), u, v, r, g, b, a);
                }
                p++;
                tp++;
            }
        }
    }

    private void fillVerticesToScreenCPU(int[] particlesOffset) {
        Vector3 look = TMP_V3.set(this.camera.direction).scl(-1.0f);
        Vector3 right = TMP_V4.set(this.camera.up).crs(look).nor();
        Vector3 up = this.camera.up;
        int tp = GPU_VERTEX_SIZE;
        Iterator it = this.renderData.iterator();
        while (it.hasNext()) {
            BillboardControllerRenderData data = (BillboardControllerRenderData) it.next();
            FloatChannel scaleChannel = data.scaleChannel;
            FloatChannel regionChannel = data.regionChannel;
            FloatChannel positionChannel = data.positionChannel;
            FloatChannel colorChannel = data.colorChannel;
            FloatChannel rotationChannel = data.rotationChannel;
            int p = GPU_VERTEX_SIZE;
            int c = data.controller.particles.size;
            while (p < c) {
                int baseOffset = (particlesOffset[tp] * this.currentVertexSize) * 4;
                float scale = scaleChannel.data[scaleChannel.strideSize * p];
                int regionOffset = p * regionChannel.strideSize;
                int positionOffset = p * positionChannel.strideSize;
                int colorOffset = p * colorChannel.strideSize;
                int rotationOffset = p * rotationChannel.strideSize;
                float px = positionChannel.data[positionOffset + GPU_VERTEX_SIZE];
                float py = positionChannel.data[positionOffset + 1];
                float pz = positionChannel.data[positionOffset + 2];
                float u = regionChannel.data[regionOffset + GPU_VERTEX_SIZE];
                float v = regionChannel.data[regionOffset + 1];
                float u2 = regionChannel.data[regionOffset + 2];
                float v2 = regionChannel.data[regionOffset + 3];
                float sx = regionChannel.data[regionOffset + 4] * scale;
                float sy = regionChannel.data[regionOffset + 5] * scale;
                float r = colorChannel.data[colorOffset + GPU_VERTEX_SIZE];
                float g = colorChannel.data[colorOffset + 1];
                float b = colorChannel.data[colorOffset + 2];
                float a = colorChannel.data[colorOffset + 3];
                float cosRotation = rotationChannel.data[rotationOffset + GPU_VERTEX_SIZE];
                float sinRotation = rotationChannel.data[rotationOffset + 1];
                TMP_V1.set(right).scl(sx);
                TMP_V2.set(up).scl(sy);
                if (cosRotation != 1.0f) {
                    TMP_M3.setToRotation(look, cosRotation, sinRotation);
                    putVertex(this.vertices, baseOffset, TMP_V6.set((-TMP_V1.x) - TMP_V2.x, (-TMP_V1.y) - TMP_V2.y, (-TMP_V1.z) - TMP_V2.z).mul(TMP_M3).add(px, py, pz), u, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set(TMP_V1.x - TMP_V2.x, TMP_V1.y - TMP_V2.y, TMP_V1.z - TMP_V2.z).mul(TMP_M3).add(px, py, pz), u2, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set(TMP_V1.x + TMP_V2.x, TMP_V1.y + TMP_V2.y, TMP_V1.z + TMP_V2.z).mul(TMP_M3).add(px, py, pz), u2, v, r, g, b, a);
                    putVertex(this.vertices, baseOffset + this.currentVertexSize, TMP_V6.set((-TMP_V1.x) + TMP_V2.x, (-TMP_V1.y) + TMP_V2.y, (-TMP_V1.z) + TMP_V2.z).mul(TMP_M3).add(px, py, pz), u, v, r, g, b, a);
                } else {
                    putVertex(this.vertices, baseOffset, TMP_V6.set(((-TMP_V1.x) - TMP_V2.x) + px, ((-TMP_V1.y) - TMP_V2.y) + py, ((-TMP_V1.z) - TMP_V2.z) + pz), u, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set((TMP_V1.x - TMP_V2.x) + px, (TMP_V1.y - TMP_V2.y) + py, (TMP_V1.z - TMP_V2.z) + pz), u2, v2, r, g, b, a);
                    baseOffset += this.currentVertexSize;
                    putVertex(this.vertices, baseOffset, TMP_V6.set((TMP_V1.x + TMP_V2.x) + px, (TMP_V1.y + TMP_V2.y) + py, (TMP_V1.z + TMP_V2.z) + pz), u2, v, r, g, b, a);
                    putVertex(this.vertices, baseOffset + this.currentVertexSize, TMP_V6.set(((-TMP_V1.x) + TMP_V2.x) + px, ((-TMP_V1.y) + TMP_V2.y) + py, ((-TMP_V1.z) + TMP_V2.z) + pz), u, v, r, g, b, a);
                }
                p++;
                tp++;
            }
        }
    }

    protected void flush(int[] offsets) {
        if (this.useGPU) {
            fillVerticesGPU(offsets);
        } else if (this.mode == AlignMode.Screen) {
            fillVerticesToScreenCPU(offsets);
        } else if (this.mode == AlignMode.ViewPoint) {
            fillVerticesToViewPointCPU(offsets);
        }
        int vCount = this.bufferedParticlesCount * 4;
        int v = GPU_VERTEX_SIZE;
        while (v < vCount) {
            int addedVertexCount = Math.min(vCount - v, MAX_VERTICES_PER_MESH);
            Renderable renderable = (Renderable) this.renderablePool.obtain();
            renderable.meshPartSize = (addedVertexCount / 4) * 6;
            renderable.mesh.setVertices(this.vertices, this.currentVertexSize * v, this.currentVertexSize * addedVertexCount);
            this.renderables.add(renderable);
            v += addedVertexCount;
        }
    }

    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Iterator it = this.renderables.iterator();
        while (it.hasNext()) {
            renderables.add(((Renderable) pool.obtain()).set((Renderable) it.next()));
        }
    }

    public void save(AssetManager manager, ResourceData resources) {
        SaveData data = resources.createSaveData("billboardBatch");
        data.save("cfg", new Config(this.useGPU, this.mode));
        data.saveAsset(manager.getAssetFileName(this.texture), Texture.class);
    }

    public void load(AssetManager manager, ResourceData resources) {
        SaveData data = resources.getSaveData("billboardBatch");
        if (data != null) {
            setTexture((Texture) manager.get(data.loadAsset()));
            Config cfg = (Config) data.load("cfg");
            setUseGpu(cfg.useGPU);
            setAlignMode(cfg.mode);
        }
    }
}
