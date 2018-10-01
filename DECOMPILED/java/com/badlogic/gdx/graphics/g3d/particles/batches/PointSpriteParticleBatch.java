package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.FloatChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.Config;
import com.badlogic.gdx.graphics.g3d.particles.ParticleShader.ParticleType;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.graphics.g3d.particles.renderers.PointSpriteControllerRenderData;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public class PointSpriteParticleBatch extends BufferedParticleBatch<PointSpriteControllerRenderData> {
    protected static final VertexAttributes CPU_ATTRIBUTES = new VertexAttributes(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(2, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(16, 4, "a_region"), new VertexAttribute(sizeAndRotationUsage, 3, "a_sizeAndRotation"));
    protected static final int CPU_COLOR_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(2).offset / 4));
    protected static final int CPU_POSITION_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(1).offset / 4));
    protected static final int CPU_REGION_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(16).offset / 4));
    protected static final int CPU_SIZE_AND_ROTATION_OFFSET = ((short) (CPU_ATTRIBUTES.findByUsage(sizeAndRotationUsage).offset / 4));
    protected static final int CPU_VERTEX_SIZE = ((short) (CPU_ATTRIBUTES.vertexSize / 4));
    protected static final Vector3 TMP_V1 = new Vector3();
    private static boolean pointSpritesEnabled = false;
    protected static final int sizeAndRotationUsage = 512;
    Renderable renderable;
    private float[] vertices;

    private static void enablePointSprites() {
        Gdx.gl.glEnable(GL20.GL_VERTEX_PROGRAM_POINT_SIZE);
        if (Gdx.app.getType() == ApplicationType.Desktop) {
            Gdx.gl.glEnable(34913);
        }
        pointSpritesEnabled = true;
    }

    public PointSpriteParticleBatch() {
        this(1000);
    }

    public PointSpriteParticleBatch(int capacity) {
        super(PointSpriteControllerRenderData.class);
        if (!pointSpritesEnabled) {
            enablePointSprites();
        }
        allocRenderable();
        ensureCapacity(capacity);
        this.renderable.shader = new ParticleShader(this.renderable, new Config(ParticleType.Point));
        this.renderable.shader.init();
    }

    protected void allocParticlesData(int capacity) {
        this.vertices = new float[(CPU_VERTEX_SIZE * capacity)];
        if (this.renderable.mesh != null) {
            this.renderable.mesh.dispose();
        }
        this.renderable.mesh = new Mesh(false, capacity, (int) CPU_VERTEX_SIZE, CPU_ATTRIBUTES);
    }

    protected void allocRenderable() {
        this.renderable = new Renderable();
        this.renderable.primitiveType = CPU_VERTEX_SIZE;
        this.renderable.meshPartOffset = CPU_VERTEX_SIZE;
        this.renderable.material = new Material(new BlendingAttribute(1, GL20.GL_ONE_MINUS_SRC_ALPHA, 1.0f), new DepthTestAttribute(GL20.GL_LEQUAL, false), TextureAttribute.createDiffuse((Texture) null));
    }

    public void setTexture(Texture texture) {
        ((TextureAttribute) this.renderable.material.get(TextureAttribute.Diffuse)).textureDescription.texture = texture;
    }

    public Texture getTexture() {
        return (Texture) ((TextureAttribute) this.renderable.material.get(TextureAttribute.Diffuse)).textureDescription.texture;
    }

    protected void flush(int[] offsets) {
        int tp = CPU_VERTEX_SIZE;
        Iterator it = this.renderData.iterator();
        while (it.hasNext()) {
            PointSpriteControllerRenderData data = (PointSpriteControllerRenderData) it.next();
            FloatChannel scaleChannel = data.scaleChannel;
            FloatChannel regionChannel = data.regionChannel;
            FloatChannel positionChannel = data.positionChannel;
            FloatChannel colorChannel = data.colorChannel;
            FloatChannel rotationChannel = data.rotationChannel;
            int p = CPU_VERTEX_SIZE;
            while (p < data.controller.particles.size) {
                int offset = offsets[tp] * CPU_VERTEX_SIZE;
                int regionOffset = p * regionChannel.strideSize;
                int positionOffset = p * positionChannel.strideSize;
                int colorOffset = p * colorChannel.strideSize;
                int rotationOffset = p * rotationChannel.strideSize;
                this.vertices[CPU_POSITION_OFFSET + offset] = positionChannel.data[positionOffset + CPU_VERTEX_SIZE];
                this.vertices[(CPU_POSITION_OFFSET + offset) + 1] = positionChannel.data[positionOffset + 1];
                this.vertices[(CPU_POSITION_OFFSET + offset) + 2] = positionChannel.data[positionOffset + 2];
                this.vertices[CPU_COLOR_OFFSET + offset] = colorChannel.data[colorOffset + CPU_VERTEX_SIZE];
                this.vertices[(CPU_COLOR_OFFSET + offset) + 1] = colorChannel.data[colorOffset + 1];
                this.vertices[(CPU_COLOR_OFFSET + offset) + 2] = colorChannel.data[colorOffset + 2];
                this.vertices[(CPU_COLOR_OFFSET + offset) + 3] = colorChannel.data[colorOffset + 3];
                this.vertices[CPU_SIZE_AND_ROTATION_OFFSET + offset] = scaleChannel.data[scaleChannel.strideSize * p];
                this.vertices[(CPU_SIZE_AND_ROTATION_OFFSET + offset) + 1] = rotationChannel.data[rotationOffset + CPU_VERTEX_SIZE];
                this.vertices[(CPU_SIZE_AND_ROTATION_OFFSET + offset) + 2] = rotationChannel.data[rotationOffset + 1];
                this.vertices[CPU_REGION_OFFSET + offset] = regionChannel.data[regionOffset + CPU_VERTEX_SIZE];
                this.vertices[(CPU_REGION_OFFSET + offset) + 1] = regionChannel.data[regionOffset + 1];
                this.vertices[(CPU_REGION_OFFSET + offset) + 2] = regionChannel.data[regionOffset + 2];
                this.vertices[(CPU_REGION_OFFSET + offset) + 3] = regionChannel.data[regionOffset + 3];
                p++;
                tp++;
            }
        }
        this.renderable.meshPartSize = this.bufferedParticlesCount;
        this.renderable.mesh.setVertices(this.vertices, CPU_VERTEX_SIZE, this.bufferedParticlesCount * CPU_VERTEX_SIZE);
    }

    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        if (this.bufferedParticlesCount > 0) {
            renderables.add(((Renderable) pool.obtain()).set(this.renderable));
        }
    }

    public void save(AssetManager manager, ResourceData resources) {
        resources.createSaveData("pointSpriteBatch").saveAsset(manager.getAssetFileName(getTexture()), Texture.class);
    }

    public void load(AssetManager manager, ResourceData resources) {
        SaveData data = resources.getSaveData("pointSpriteBatch");
        if (data != null) {
            setTexture((Texture) manager.get(data.loadAsset()));
        }
    }
}
