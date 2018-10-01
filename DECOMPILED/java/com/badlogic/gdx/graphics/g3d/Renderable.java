package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Matrix4;

public class Renderable {
    public Matrix4[] bones;
    public Environment environment;
    public Material material;
    public Mesh mesh;
    public int meshPartOffset;
    public int meshPartSize;
    public int primitiveType;
    public Shader shader;
    public Object userData;
    public final Matrix4 worldTransform = new Matrix4();

    public Renderable set(Renderable renderable) {
        this.worldTransform.set(renderable.worldTransform);
        this.material = renderable.material;
        this.mesh = renderable.mesh;
        this.meshPartOffset = renderable.meshPartOffset;
        this.meshPartSize = renderable.meshPartSize;
        this.primitiveType = renderable.primitiveType;
        this.bones = renderable.bones;
        this.environment = renderable.environment;
        this.shader = renderable.shader;
        this.userData = renderable.userData;
        return this;
    }
}
