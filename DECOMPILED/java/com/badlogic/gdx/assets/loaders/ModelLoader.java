package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider.AssetTextureProvider;
import com.badlogic.gdx.graphics.g3d.utils.TextureProvider.FileTextureProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import java.util.Iterator;

public abstract class ModelLoader<P extends ModelParameters> extends AsynchronousAssetLoader<Model, P> {
    protected ModelParameters defaultParameters = new ModelParameters();
    protected Array<Entry<String, ModelData>> items = new Array();

    public static class ModelParameters extends AssetLoaderParameters<Model> {
        public TextureParameter textureParameter = new TextureParameter();

        public ModelParameters() {
            TextureParameter textureParameter = this.textureParameter;
            TextureParameter textureParameter2 = this.textureParameter;
            TextureFilter textureFilter = TextureFilter.Linear;
            textureParameter2.magFilter = textureFilter;
            textureParameter.minFilter = textureFilter;
            textureParameter = this.textureParameter;
            textureParameter2 = this.textureParameter;
            TextureWrap textureWrap = TextureWrap.Repeat;
            textureParameter2.wrapV = textureWrap;
            textureParameter.wrapU = textureWrap;
        }
    }

    public abstract ModelData loadModelData(FileHandle fileHandle, P p);

    public ModelLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public ModelData loadModelData(FileHandle fileHandle) {
        return loadModelData(fileHandle, null);
    }

    public Model loadModel(FileHandle fileHandle, TextureProvider textureProvider, P parameters) {
        ModelData data = loadModelData(fileHandle, parameters);
        return data == null ? null : new Model(data, textureProvider);
    }

    public Model loadModel(FileHandle fileHandle, P parameters) {
        return loadModel(fileHandle, new FileTextureProvider(), parameters);
    }

    public Model loadModel(FileHandle fileHandle, TextureProvider textureProvider) {
        return loadModel(fileHandle, textureProvider, null);
    }

    public Model loadModel(FileHandle fileHandle) {
        return loadModel(fileHandle, new FileTextureProvider(), null);
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, P parameters) {
        Array<AssetDescriptor> deps = new Array();
        ModelData data = loadModelData(file, parameters);
        if (data != null) {
            Entry<String, ModelData> item = new Entry();
            item.key = fileName;
            item.value = data;
            synchronized (this.items) {
                this.items.add(item);
            }
            AssetLoaderParameters textureParameter = parameters != null ? parameters.textureParameter : this.defaultParameters.textureParameter;
            Iterator it = data.materials.iterator();
            while (it.hasNext()) {
                ModelMaterial modelMaterial = (ModelMaterial) it.next();
                if (modelMaterial.textures != null) {
                    Iterator it2 = modelMaterial.textures.iterator();
                    while (it2.hasNext()) {
                        deps.add(new AssetDescriptor(((ModelTexture) it2.next()).fileName, Texture.class, textureParameter));
                    }
                }
            }
        }
        return deps;
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, P p) {
    }

    public Model loadSync(AssetManager manager, String fileName, FileHandle file, P p) {
        ModelData data = null;
        synchronized (this.items) {
            for (int i = 0; i < this.items.size; i++) {
                if (((String) ((Entry) this.items.get(i)).key).equals(fileName)) {
                    data = (ModelData) ((Entry) this.items.get(i)).value;
                    this.items.removeIndex(i);
                }
            }
        }
        if (data == null) {
            return null;
        }
        Model result = new Model(data, new AssetTextureProvider(manager));
        Iterator<Disposable> disposables = result.getManagedDisposables().iterator();
        while (disposables.hasNext()) {
            if (((Disposable) disposables.next()) instanceof Texture) {
                disposables.remove();
            }
        }
        return result;
    }
}
