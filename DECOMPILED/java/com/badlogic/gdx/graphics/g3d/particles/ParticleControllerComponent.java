package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.Configurable;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;

public abstract class ParticleControllerComponent implements Configurable, Disposable, Serializable {
    protected static final Matrix3 TMP_M3 = new Matrix3();
    protected static final Matrix4 TMP_M4 = new Matrix4();
    protected static final Quaternion TMP_Q = new Quaternion();
    protected static final Quaternion TMP_Q2 = new Quaternion();
    protected static final Vector3 TMP_V1 = new Vector3();
    protected static final Vector3 TMP_V2 = new Vector3();
    protected static final Vector3 TMP_V3 = new Vector3();
    protected static final Vector3 TMP_V4 = new Vector3();
    protected static final Vector3 TMP_V5 = new Vector3();
    protected static final Vector3 TMP_V6 = new Vector3();
    protected ParticleController controller;

    public abstract ParticleControllerComponent copy();

    public void activateParticles(int startIndex, int count) {
    }

    public void killParticles(int startIndex, int count) {
    }

    public void update() {
    }

    public void init() {
    }

    public void start() {
    }

    public void end() {
    }

    public void dispose() {
    }

    public void allocateChannels() {
    }

    public void set(ParticleController particleController) {
        this.controller = particleController;
    }

    public void save(AssetManager manager, ResourceData data) {
    }

    public void load(AssetManager manager, ResourceData data) {
    }

    public void write(Json json) {
    }

    public void read(Json json, JsonValue jsonData) {
    }
}
