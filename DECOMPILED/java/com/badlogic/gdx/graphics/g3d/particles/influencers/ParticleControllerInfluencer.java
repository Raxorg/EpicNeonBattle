package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ParticleController;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public abstract class ParticleControllerInfluencer extends Influencer {
    ObjectChannel<ParticleController> particleControllerChannel;
    public Array<ParticleController> templates;

    public static class Random extends ParticleControllerInfluencer {
        ParticleControllerPool pool = new ParticleControllerPool();

        private class ParticleControllerPool extends Pool<ParticleController> {
            public ParticleController newObject() {
                ParticleController controller = ((ParticleController) Random.this.templates.random()).copy();
                controller.init();
                return controller;
            }

            public void clear() {
                int free = Random.this.pool.getFree();
                for (int i = 0; i < free; i++) {
                    ((ParticleController) Random.this.pool.obtain()).dispose();
                }
                super.clear();
            }
        }

        public Random(ParticleController... templates) {
            super(templates);
        }

        public Random(Random particleControllerRandom) {
            super((ParticleControllerInfluencer) particleControllerRandom);
        }

        public void init() {
            this.pool.clear();
            for (int i = 0; i < this.controller.emitter.maxParticleCount; i++) {
                this.pool.free(this.pool.newObject());
            }
        }

        public void dispose() {
            this.pool.clear();
            super.dispose();
        }

        public void activateParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                ParticleController controller = (ParticleController) this.pool.obtain();
                controller.start();
                ((ParticleController[]) this.particleControllerChannel.data)[i] = controller;
            }
        }

        public void killParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                ParticleController controller = ((ParticleController[]) this.particleControllerChannel.data)[i];
                controller.end();
                this.pool.free(controller);
                ((ParticleController[]) this.particleControllerChannel.data)[i] = null;
            }
        }

        public Random copy() {
            return new Random(this);
        }
    }

    public static class Single extends ParticleControllerInfluencer {
        public Single(ParticleController... templates) {
            super(templates);
        }

        public Single(Single particleControllerSingle) {
            super((ParticleControllerInfluencer) particleControllerSingle);
        }

        public void init() {
            ParticleController first = (ParticleController) this.templates.first();
            int c = this.controller.particles.capacity;
            for (int i = 0; i < c; i++) {
                ParticleController copy = first.copy();
                copy.init();
                ((ParticleController[]) this.particleControllerChannel.data)[i] = copy;
            }
        }

        public void activateParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                ((ParticleController[]) this.particleControllerChannel.data)[i].start();
            }
        }

        public void killParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                ((ParticleController[]) this.particleControllerChannel.data)[i].end();
            }
        }

        public Single copy() {
            return new Single(this);
        }
    }

    public ParticleControllerInfluencer() {
        this.templates = new Array(true, 1, ParticleController.class);
    }

    public ParticleControllerInfluencer(ParticleController... templates) {
        this.templates = new Array((Object[]) templates);
    }

    public ParticleControllerInfluencer(ParticleControllerInfluencer influencer) {
        this((ParticleController[]) influencer.templates.items);
    }

    public void allocateChannels() {
        this.particleControllerChannel = (ObjectChannel) this.controller.particles.addChannel(ParticleChannels.ParticleController);
    }

    public void end() {
        for (int i = 0; i < this.controller.particles.size; i++) {
            ((ParticleController[]) this.particleControllerChannel.data)[i].end();
        }
    }

    public void dispose() {
        if (this.controller != null) {
            for (int i = 0; i < this.controller.particles.size; i++) {
                ParticleController controller = ((ParticleController[]) this.particleControllerChannel.data)[i];
                if (controller != null) {
                    controller.dispose();
                    ((ParticleController[]) this.particleControllerChannel.data)[i] = null;
                }
            }
        }
    }

    public void save(AssetManager manager, ResourceData resources) {
        SaveData data = resources.createSaveData();
        Array<ParticleEffect> effects = manager.getAll(ParticleEffect.class, new Array());
        Array<ParticleController> controllers = new Array(this.templates);
        Array<IntArray> effectsIndices = new Array();
        for (int i = 0; i < effects.size && controllers.size > 0; i++) {
            ParticleEffect effect = (ParticleEffect) effects.get(i);
            Array<ParticleController> effectControllers = effect.getControllers();
            Iterator<ParticleController> iterator = controllers.iterator();
            IntArray indices = null;
            while (iterator.hasNext()) {
                int index = effectControllers.indexOf((ParticleController) iterator.next(), true);
                if (index > -1) {
                    if (indices == null) {
                        indices = new IntArray();
                    }
                    iterator.remove();
                    indices.add(index);
                }
            }
            if (indices != null) {
                data.saveAsset(manager.getAssetFileName(effect), ParticleEffect.class);
                effectsIndices.add(indices);
            }
        }
        data.save("indices", effectsIndices);
    }

    public void load(AssetManager manager, ResourceData resources) {
        SaveData data = resources.getSaveData();
        Iterator<IntArray> iterator = ((Array) data.load("indices")).iterator();
        while (true) {
            AssetDescriptor descriptor = data.loadAsset();
            if (descriptor != null) {
                ParticleEffect effect = (ParticleEffect) manager.get(descriptor);
                if (effect == null) {
                    throw new RuntimeException("Template is null");
                }
                Array<ParticleController> effectControllers = effect.getControllers();
                IntArray effectIndices = (IntArray) iterator.next();
                int n = effectIndices.size;
                for (int i = 0; i < n; i++) {
                    this.templates.add(effectControllers.get(effectIndices.get(i)));
                }
            } else {
                return;
            }
        }
    }
}
