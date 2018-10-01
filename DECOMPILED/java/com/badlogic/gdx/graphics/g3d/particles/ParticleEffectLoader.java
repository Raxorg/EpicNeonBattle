package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.particles.ResourceData.AssetData;
import com.badlogic.gdx.graphics.g3d.particles.batches.ParticleBatch;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import java.io.IOException;
import java.util.Iterator;

public class ParticleEffectLoader extends AsynchronousAssetLoader<ParticleEffect, ParticleEffectLoadParameter> {
    protected Array<Entry<String, ResourceData<ParticleEffect>>> items = new Array();

    public static class ParticleEffectLoadParameter extends AssetLoaderParameters<ParticleEffect> {
        Array<ParticleBatch<?>> batches;

        public ParticleEffectLoadParameter(Array<ParticleBatch<?>> batches) {
            this.batches = batches;
        }
    }

    public static class ParticleEffectSaveParameter extends AssetLoaderParameters<ParticleEffect> {
        Array<ParticleBatch<?>> batches;
        FileHandle file;
        AssetManager manager;

        public ParticleEffectSaveParameter(FileHandle file, AssetManager manager, Array<ParticleBatch<?>> batches) {
            this.batches = batches;
            this.file = file;
            this.manager = manager;
        }
    }

    public ParticleEffectLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {
        Array<AssetData> assets;
        ResourceData<ParticleEffect> data = (ResourceData) new Json().fromJson(ResourceData.class, file);
        synchronized (this.items) {
            Entry<String, ResourceData<ParticleEffect>> entry = new Entry();
            entry.key = fileName;
            entry.value = data;
            this.items.add(entry);
            assets = data.getAssets();
        }
        Array<AssetDescriptor> descriptors = new Array();
        Iterator it = assets.iterator();
        while (it.hasNext()) {
            AssetData<?> assetData = (AssetData) it.next();
            if (!resolve(assetData.filename).exists()) {
                assetData.filename = file.parent().child(Gdx.files.internal(assetData.filename).name()).path();
            }
            if (assetData.type == ParticleEffect.class) {
                descriptors.add(new AssetDescriptor(assetData.filename, assetData.type, (AssetLoaderParameters) parameter));
            } else {
                descriptors.add(new AssetDescriptor(assetData.filename, assetData.type));
            }
        }
        return descriptors;
    }

    public void save(ParticleEffect effect, ParticleEffectSaveParameter parameter) throws IOException {
        Object data = new ResourceData(effect);
        effect.save(parameter.manager, data);
        if (parameter.batches != null) {
            Iterator it = parameter.batches.iterator();
            while (it.hasNext()) {
                ParticleBatch<?> batch = (ParticleBatch) it.next();
                boolean save = false;
                Iterator it2 = effect.getControllers().iterator();
                while (it2.hasNext()) {
                    if (((ParticleController) it2.next()).renderer.isCompatible(batch)) {
                        save = true;
                        break;
                    }
                }
                if (save) {
                    batch.save(parameter.manager, data);
                }
            }
        }
        new Json().toJson(data, parameter.file);
    }

    public ParticleEffect loadSync(AssetManager manager, String fileName, FileHandle file, ParticleEffectLoadParameter parameter) {
        ResourceData<ParticleEffect> effectData = null;
        synchronized (this.items) {
            for (int i = 0; i < this.items.size; i++) {
                Entry<String, ResourceData<ParticleEffect>> entry = (Entry) this.items.get(i);
                if (((String) entry.key).equals(fileName)) {
                    effectData = (ResourceData) entry.value;
                    this.items.removeIndex(i);
                    break;
                }
            }
        }
        ((ParticleEffect) effectData.resource).load(manager, effectData);
        if (parameter != null) {
            if (parameter.batches != null) {
                Iterator it = parameter.batches.iterator();
                while (it.hasNext()) {
                    ((ParticleBatch) it.next()).load(manager, effectData);
                }
            }
            ((ParticleEffect) effectData.resource).setBatch(parameter.batches);
        }
        return (ParticleEffect) effectData.resource;
    }

    private <T> T find(Array<?> array, Class<T> type) {
        Iterator it = array.iterator();
        while (it.hasNext()) {
            T object = it.next();
            if (ClassReflection.isAssignableFrom(type, object.getClass())) {
                return object;
            }
        }
        return null;
    }
}
