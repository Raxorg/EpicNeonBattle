package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class RenderContext {
    private int blendDFactor;
    private int blendSFactor;
    private boolean blending;
    private int cullFace;
    private int depthFunc;
    private boolean depthMask;
    private float depthRangeFar;
    private float depthRangeNear;
    public final TextureBinder textureBinder;

    public RenderContext(TextureBinder textures) {
        this.textureBinder = textures;
    }

    public void begin() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        this.depthFunc = 0;
        Gdx.gl.glDepthMask(true);
        this.depthMask = true;
        Gdx.gl.glDisable(GL20.GL_BLEND);
        this.blending = false;
        Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        this.blendDFactor = 0;
        this.blendSFactor = 0;
        this.cullFace = 0;
        this.textureBinder.begin();
    }

    public void end() {
        if (this.depthFunc != 0) {
            Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        }
        if (!this.depthMask) {
            Gdx.gl.glDepthMask(true);
        }
        if (this.blending) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
        if (this.cullFace > 0) {
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        }
        this.textureBinder.end();
    }

    public void setDepthMask(boolean depthMask) {
        if (this.depthMask != depthMask) {
            GL20 gl20 = Gdx.gl;
            this.depthMask = depthMask;
            gl20.glDepthMask(depthMask);
        }
    }

    public void setDepthTest(int depthFunction) {
        setDepthTest(depthFunction, 0.0f, 1.0f);
    }

    public void setDepthTest(int depthFunction, float depthRangeNear, float depthRangeFar) {
        boolean wasEnabled;
        boolean enabled = true;
        if (this.depthFunc != 0) {
            wasEnabled = true;
        } else {
            wasEnabled = false;
        }
        if (depthFunction == 0) {
            enabled = false;
        }
        if (this.depthFunc != depthFunction) {
            this.depthFunc = depthFunction;
            if (enabled) {
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthFunc(depthFunction);
            } else {
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
            }
        }
        if (enabled) {
            GL20 gl20;
            if (!(wasEnabled && this.depthFunc == depthFunction)) {
                gl20 = Gdx.gl;
                this.depthFunc = depthFunction;
                gl20.glDepthFunc(depthFunction);
            }
            if (!wasEnabled || this.depthRangeNear != depthRangeNear || this.depthRangeFar != depthRangeFar) {
                gl20 = Gdx.gl;
                this.depthRangeNear = depthRangeNear;
                this.depthRangeFar = depthRangeFar;
                gl20.glDepthRangef(depthRangeNear, depthRangeFar);
            }
        }
    }

    public void setBlending(boolean enabled, int sFactor, int dFactor) {
        if (enabled != this.blending) {
            this.blending = enabled;
            if (enabled) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
            } else {
                Gdx.gl.glDisable(GL20.GL_BLEND);
            }
        }
        if (!enabled) {
            return;
        }
        if (this.blendSFactor != sFactor || this.blendDFactor != dFactor) {
            Gdx.gl.glBlendFunc(sFactor, dFactor);
            this.blendSFactor = sFactor;
            this.blendDFactor = dFactor;
        }
    }

    public void setCullFace(int face) {
        if (face != this.cullFace) {
            this.cullFace = face;
            if (face == GL20.GL_FRONT || face == GL20.GL_BACK || face == GL20.GL_FRONT_AND_BACK) {
                Gdx.gl.glEnable(GL20.GL_CULL_FACE);
                Gdx.gl.glCullFace(face);
                return;
            }
            Gdx.gl.glDisable(GL20.GL_CULL_FACE);
        }
    }
}
