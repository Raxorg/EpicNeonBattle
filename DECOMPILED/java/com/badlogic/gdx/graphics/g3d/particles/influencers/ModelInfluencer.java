package com.badlogic.gdx.graphics.g3d.particles.influencers;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParallelArray.ObjectChannel;
import com.badlogic.gdx.graphics.g3d.particles.ParticleChannels;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.SaveData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public abstract class ModelInfluencer extends Influencer {
    ObjectChannel<ModelInstance> modelChannel;
    public Array<Model> models;

    public static class Random extends ModelInfluencer {
        ModelInstancePool pool = new ModelInstancePool();

        private class ModelInstancePool extends Pool<ModelInstance> {
            public ModelInstance newObject() {
                return new ModelInstance((Model) Random.this.models.random());
            }
        }

        public Random(Random influencer) {
            super((ModelInfluencer) influencer);
        }

        public Random(Model... models) {
            super(models);
        }

        public void init() {
            this.pool.clear();
        }

        public void activateParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                ((ModelInstance[]) this.modelChannel.data)[i] = (ModelInstance) this.pool.obtain();
            }
        }

        public void killParticles(int startIndex, int count) {
            int c = startIndex + count;
            for (int i = startIndex; i < c; i++) {
                this.pool.free(((ModelInstance[]) this.modelChannel.data)[i]);
                ((ModelInstance[]) this.modelChannel.data)[i] = null;
            }
        }

        public Random copy() {
            return new Random(this);
        }
    }

    public static class Single extends ModelInfluencer {
        public Single(Single influencer) {
            super((ModelInfluencer) influencer);
        }

        public Single(Model... models) {
            super(models);
        }

        public void init() {
            Model first = (Model) this.models.first();
            int c = this.controller.emitter.maxParticleCount;
            for (int i = 0; i < c; i++) {
                ((ModelInstance[]) this.modelChannel.data)[i] = new ModelInstance(first);
            }
        }

        public Single copy() {
            return new Single(this);
        }
    }

    public ModelInfluencer() {
        this.models = new Array(true, 1, Model.class);
    }

    public ModelInfluencer(Model... models) {
        this.models = new Array((Object[]) models);
    }

    public ModelInfluencer(ModelInfluencer influencer) {
        this((Model[]) influencer.models.toArray(Model.class));
    }

    public void allocateChannels() {
        this.modelChannel = (ObjectChannel) this.controller.particles.addChannel(ParticleChannels.ModelInstance);
    }

    public void save(AssetManager manager, ResourceData resources) {
        SaveData data = resources.createSaveData();
        Iterator it = this.models.iterator();
        while (it.hasNext()) {
            data.saveAsset(manager.getAssetFileName((Model) it.next()), Model.class);
        }
    }

    public void load(AssetManager manager, ResourceData resources) {
        SaveData data = resources.getSaveData();
        while (true) {
            AssetDescriptor descriptor = data.loadAsset();
            if (descriptor != null) {
                Model model = (Model) manager.get(descriptor);
                if (model == null) {
                    throw new RuntimeException("Model is null");
                }
                this.models.add(model);
            } else {
                return;
            }
        }
    }
}
