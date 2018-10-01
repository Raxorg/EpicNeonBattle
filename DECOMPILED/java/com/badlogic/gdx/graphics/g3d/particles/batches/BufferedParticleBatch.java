package com.badlogic.gdx.graphics.g3d.particles.batches;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSorter.Distance;
import com.badlogic.gdx.graphics.g3d.particles.renderers.ParticleControllerRenderData;
import com.badlogic.gdx.utils.Array;

public abstract class BufferedParticleBatch<T extends ParticleControllerRenderData> implements ParticleBatch<T> {
    protected int bufferedParticlesCount;
    protected Camera camera;
    protected int currentCapacity = 0;
    protected Array<T> renderData;
    protected ParticleSorter sorter = new Distance();

    protected abstract void allocParticlesData(int i);

    protected abstract void flush(int[] iArr);

    protected BufferedParticleBatch(Class<T> type) {
        this.renderData = new Array(false, 10, type);
    }

    public void begin() {
        this.renderData.clear();
        this.bufferedParticlesCount = 0;
    }

    public void draw(T data) {
        if (data.controller.particles.size > 0) {
            this.renderData.add(data);
            this.bufferedParticlesCount += data.controller.particles.size;
        }
    }

    public void end() {
        if (this.bufferedParticlesCount > 0) {
            ensureCapacity(this.bufferedParticlesCount);
            flush(this.sorter.sort(this.renderData));
        }
    }

    public void ensureCapacity(int capacity) {
        if (this.currentCapacity < capacity) {
            this.sorter.ensureCapacity(capacity);
            allocParticlesData(capacity);
            this.currentCapacity = capacity;
        }
    }

    public void resetCapacity() {
        this.bufferedParticlesCount = 0;
        this.currentCapacity = 0;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
        this.sorter.setCamera(camera);
    }

    public ParticleSorter getSorter() {
        return this.sorter;
    }

    public void setSorter(ParticleSorter sorter) {
        this.sorter = sorter;
        sorter.setCamera(this.camera);
        sorter.ensureCapacity(this.currentCapacity);
    }

    public int getBufferedCount() {
        return this.bufferedParticlesCount;
    }
}
