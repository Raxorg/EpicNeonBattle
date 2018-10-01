package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import java.util.Iterator;

public class SkinLoader extends AsynchronousAssetLoader<Skin, SkinParameter> {

    public static class SkinParameter extends AssetLoaderParameters<Skin> {
        public final ObjectMap<String, Object> resources;
        public final String textureAtlasPath;

        public SkinParameter() {
            this(null, null);
        }

        public SkinParameter(ObjectMap<String, Object> resources) {
            this(null, resources);
        }

        public SkinParameter(String textureAtlasPath) {
            this(textureAtlasPath, null);
        }

        public SkinParameter(String textureAtlasPath, ObjectMap<String, Object> resources) {
            this.textureAtlasPath = textureAtlasPath;
            this.resources = resources;
        }
    }

    public SkinLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SkinParameter parameter) {
        Array<AssetDescriptor> deps = new Array();
        if (parameter == null || parameter.textureAtlasPath == null) {
            deps.add(new AssetDescriptor(file.pathWithoutExtension() + ".atlas", TextureAtlas.class));
        } else if (parameter.textureAtlasPath != null) {
            deps.add(new AssetDescriptor(parameter.textureAtlasPath, TextureAtlas.class));
        }
        return deps;
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
    }

    public Skin loadSync(AssetManager manager, String fileName, FileHandle file, SkinParameter parameter) {
        String textureAtlasPath = file.pathWithoutExtension() + ".atlas";
        ObjectMap<String, Object> resources = null;
        if (parameter != null) {
            if (parameter.textureAtlasPath != null) {
                textureAtlasPath = parameter.textureAtlasPath;
            }
            if (parameter.resources != null) {
                resources = parameter.resources;
            }
        }
        Skin skin = new Skin((TextureAtlas) manager.get(textureAtlasPath, TextureAtlas.class));
        if (resources != null) {
            Iterator it = resources.entries().iterator();
            while (it.hasNext()) {
                Entry<String, Object> entry = (Entry) it.next();
                skin.add((String) entry.key, entry.value);
            }
        }
        skin.load(file);
        return skin;
    }
}
