package com.badlogic.gdx.graphics.g3d.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

public class DirectionalShadowLight extends DirectionalLight implements ShadowMap, Disposable {
    protected Camera cam;
    protected FrameBuffer fbo;
    protected float halfDepth;
    protected float halfHeight;
    protected final TextureDescriptor textureDesc;
    protected final Vector3 tmpV = new Vector3();

    public DirectionalShadowLight(int shadowMapWidth, int shadowMapHeight, float shadowViewportWidth, float shadowViewportHeight, float shadowNear, float shadowFar) {
        this.fbo = new FrameBuffer(Format.RGBA8888, shadowMapWidth, shadowMapHeight, true);
        this.cam = new OrthographicCamera(shadowViewportWidth, shadowViewportHeight);
        this.cam.near = shadowNear;
        this.cam.far = shadowFar;
        this.halfHeight = shadowViewportHeight * 0.5f;
        this.halfDepth = ((shadowFar - shadowNear) * 0.5f) + shadowNear;
        this.textureDesc = new TextureDescriptor();
        TextureDescriptor textureDescriptor = this.textureDesc;
        TextureDescriptor textureDescriptor2 = this.textureDesc;
        TextureFilter textureFilter = TextureFilter.Nearest;
        textureDescriptor2.magFilter = textureFilter;
        textureDescriptor.minFilter = textureFilter;
        textureDescriptor = this.textureDesc;
        textureDescriptor2 = this.textureDesc;
        TextureWrap textureWrap = TextureWrap.ClampToEdge;
        textureDescriptor2.vWrap = textureWrap;
        textureDescriptor.uWrap = textureWrap;
    }

    public void update(Camera camera) {
        update(this.tmpV.set(camera.direction).scl(this.halfHeight), camera.direction);
    }

    public void update(Vector3 center, Vector3 forward) {
        this.cam.position.set(this.direction).scl(-this.halfDepth).add(center);
        this.cam.direction.set(this.direction).nor();
        this.cam.normalizeUp();
        this.cam.update();
    }

    public void begin(Camera camera) {
        update(camera);
        begin();
    }

    public void begin(Vector3 center, Vector3 forward) {
        update(center, forward);
        begin();
    }

    public void begin() {
        int w = this.fbo.getWidth();
        int h = this.fbo.getHeight();
        this.fbo.begin();
        Gdx.gl.glViewport(0, 0, w, h);
        Gdx.gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        Gdx.gl.glClear(16640);
        Gdx.gl.glEnable(GL20.GL_SCISSOR_TEST);
        Gdx.gl.glScissor(1, 1, w - 2, h - 2);
    }

    public void end() {
        Gdx.gl.glDisable(GL20.GL_SCISSOR_TEST);
        this.fbo.end();
    }

    public FrameBuffer getFrameBuffer() {
        return this.fbo;
    }

    public Camera getCamera() {
        return this.cam;
    }

    public Matrix4 getProjViewTrans() {
        return this.cam.combined;
    }

    public TextureDescriptor getDepthMap() {
        this.textureDesc.texture = this.fbo.getColorBufferTexture();
        return this.textureDesc;
    }

    public void dispose() {
        if (this.fbo != null) {
            this.fbo.dispose();
        }
        this.fbo = null;
    }
}
