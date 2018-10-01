package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.Factory;
import com.badlogic.gdx.utils.Array;

public class TextureLoader extends AsynchronousAssetLoader<Texture, TextureParameter> {
    TextureLoaderInfo info = new TextureLoaderInfo();

    public static class TextureLoaderInfo {
        TextureData data;
        String filename;
        Texture texture;
    }

    public static class TextureParameter extends AssetLoaderParameters<Texture> {
        public Format format = null;
        public boolean genMipMaps = false;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public Texture texture = null;
        public TextureData textureData = null;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;
    }

    public TextureLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, TextureParameter parameter) {
        this.info.filename = fileName;
        if (parameter == null || parameter.textureData == null) {
            Format format = null;
            boolean genMipMaps = false;
            this.info.texture = null;
            if (parameter != null) {
                format = parameter.format;
                genMipMaps = parameter.genMipMaps;
                this.info.texture = parameter.texture;
            }
            this.info.data = Factory.loadFromFile(file, format, genMipMaps);
        } else {
            this.info.data = parameter.textureData;
            this.info.texture = parameter.texture;
        }
        if (!this.info.data.isPrepared()) {
            this.info.data.prepare();
        }
    }

    public Texture loadSync(AssetManager manager, String fileName, FileHandle file, TextureParameter parameter) {
        if (this.info == null) {
            return null;
        }
        Texture texture = this.info.texture;
        if (texture != null) {
            texture.load(this.info.data);
        } else {
            texture = new Texture(this.info.data);
        }
        if (parameter == null) {
            return texture;
        }
        texture.setFilter(parameter.minFilter, parameter.magFilter);
        texture.setWrap(parameter.wrapU, parameter.wrapV);
        return texture;
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, TextureParameter parameter) {
        return null;
    }
}
