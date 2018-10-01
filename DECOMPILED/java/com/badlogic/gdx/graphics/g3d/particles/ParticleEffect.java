package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.Configurable;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Iterator;

public class ParticleEffect implements Configurable, Disposable {
    private BoundingBox bounds;
    private Array<ParticleController> controllers;

    public ParticleEffect() {
        this.controllers = new Array(true, 3, ParticleController.class);
    }

    public ParticleEffect(ParticleEffect effect) {
        this.controllers = new Array(true, effect.controllers.size);
        int n = effect.controllers.size;
        for (int i = 0; i < n; i++) {
            this.controllers.add(((ParticleController) effect.controllers.get(i)).copy());
        }
    }

    public ParticleEffect(ParticleController... emitters) {
        this.controllers = new Array((Object[]) emitters);
    }

    public void init() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).init();
        }
    }

    public void start() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).start();
        }
    }

    public void end() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).end();
        }
    }

    public void reset() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).reset();
        }
    }

    public void update() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).update();
        }
    }

    public void draw() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).draw();
        }
    }

    public void setTransform(Matrix4 transform) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).setTransform(transform);
        }
    }

    public void rotate(Quaternion rotation) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).rotate(rotation);
        }
    }

    public void rotate(Vector3 axis, float angle) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).rotate(axis, angle);
        }
    }

    public void translate(Vector3 translation) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).translate(translation);
        }
    }

    public void scale(float scaleX, float scaleY, float scaleZ) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).scale(scaleX, scaleY, scaleZ);
        }
    }

    public void scale(Vector3 scale) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).scale(scale.x, scale.y, scale.z);
        }
    }

    public Array<ParticleController> getControllers() {
        return this.controllers;
    }

    public ParticleController findController(String name) {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ParticleController emitter = (ParticleController) this.controllers.get(i);
            if (emitter.name.equals(name)) {
                return emitter;
            }
        }
        return null;
    }

    public void dispose() {
        int n = this.controllers.size;
        for (int i = 0; i < n; i++) {
            ((ParticleController) this.controllers.get(i)).dispose();
        }
    }

    public BoundingBox getBoundingBox() {
        if (this.bounds == null) {
            this.bounds = new BoundingBox();
        }
        BoundingBox bounds = this.bounds;
        bounds.inf();
        Iterator it = this.controllers.iterator();
        while (it.hasNext()) {
            bounds.ext(((ParticleController) it.next()).getBoundingBox());
        }
        return bounds;
    }

    public void setBatch(Array<ParticleBatch<?>> batches) {
        Iterator it = this.controllers.iterator();
        while (it.hasNext()) {
            ParticleController controller = (ParticleController) it.next();
            Iterator it2 = batches.iterator();
            while (it2.hasNext()) {
                if (controller.renderer.setBatch((ParticleBatch) it2.next())) {
                    break;
                }
            }
        }
    }

    public ParticleEffect copy() {
        return new ParticleEffect(this);
    }

    public void save(AssetManager assetManager, ResourceData data) {
        Iterator it = this.controllers.iterator();
        while (it.hasNext()) {
            ((ParticleController) it.next()).save(assetManager, data);
        }
    }

    public void load(AssetManager assetManager, ResourceData data) {
        Iterator it = this.controllers.iterator();
        while (it.hasNext()) {
            ((ParticleController) it.next()).load(assetManager, data);
        }
    }
}
