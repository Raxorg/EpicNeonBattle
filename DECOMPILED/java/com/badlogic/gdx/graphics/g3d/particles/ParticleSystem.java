package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.RenderableProvider;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public final class ParticleSystem implements RenderableProvider {
    private static ParticleSystem instance;
    private Array<ParticleBatch<?>> batches = new Array();
    private Array<ParticleEffect> effects = new Array();

    public static ParticleSystem get() {
        if (instance == null) {
            instance = new ParticleSystem();
        }
        return instance;
    }

    private ParticleSystem() {
    }

    public void add(ParticleBatch<?> batch) {
        this.batches.add(batch);
    }

    public void add(ParticleEffect effect) {
        this.effects.add(effect);
    }

    public void remove(ParticleEffect effect) {
        this.effects.removeValue(effect, true);
    }

    public void removeAll() {
        this.effects.clear();
    }

    public void update() {
        Iterator it = this.effects.iterator();
        while (it.hasNext()) {
            ((ParticleEffect) it.next()).update();
        }
    }

    public void updateAndDraw() {
        Iterator it = this.effects.iterator();
        while (it.hasNext()) {
            ParticleEffect effect = (ParticleEffect) it.next();
            effect.update();
            effect.draw();
        }
    }

    public void begin() {
        Iterator it = this.batches.iterator();
        while (it.hasNext()) {
            ((ParticleBatch) it.next()).begin();
        }
    }

    public void draw() {
        Iterator it = this.effects.iterator();
        while (it.hasNext()) {
            ((ParticleEffect) it.next()).draw();
        }
    }

    public void end() {
        Iterator it = this.batches.iterator();
        while (it.hasNext()) {
            ((ParticleBatch) it.next()).end();
        }
    }

    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Iterator it = this.batches.iterator();
        while (it.hasNext()) {
            ((ParticleBatch) it.next()).getRenderables(renderables, pool);
        }
    }

    public Array<ParticleBatch<?>> getBatches() {
        return this.batches;
    }
}
