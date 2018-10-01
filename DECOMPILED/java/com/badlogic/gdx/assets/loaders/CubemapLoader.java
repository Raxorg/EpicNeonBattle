package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;
import com.badlogic.gdx.utils.Array;

public class CubemapLoader extends AsynchronousAssetLoader<Cubemap, CubemapParameter> {
    CubemapLoaderInfo info = new CubemapLoaderInfo();

    public static class CubemapLoaderInfo {
        Cubemap cubemap;
        CubemapData data;
        String filename;
    }

    public static class CubemapParameter extends AssetLoaderParameters<Cubemap> {
        public Cubemap cubemap = null;
        public CubemapData cubemapData = null;
        public Format format = null;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureFilter minFilter = TextureFilter.Nearest;
        public TextureWrap wrapU = TextureWrap.ClampToEdge;
        public TextureWrap wrapV = TextureWrap.ClampToEdge;
    }

    public CubemapLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, CubemapParameter parameter) {
        this.info.filename = fileName;
        if (parameter == null || parameter.cubemapData == null) {
            this.info.cubemap = null;
            if (parameter != null) {
                Format format = parameter.format;
                this.info.cubemap = parameter.cubemap;
            }
            if (fileName.contains(".ktx") || fileName.contains(".zktx")) {
                this.info.data = new KTXTextureData(file, false);
            }
        } else {
            this.info.data = parameter.cubemapData;
            this.info.cubemap = parameter.cubemap;
        }
        if (!this.info.data.isPrepared()) {
            this.info.data.prepare();
        }
    }

    public Cubemap loadSync(AssetManager manager, String fileName, FileHandle file, CubemapParameter parameter) {
        if (this.info == null) {
            return null;
        }
        Cubemap cubemap = this.info.cubemap;
        if (cubemap != null) {
            cubemap.load(this.info.data);
        } else {
            cubemap = new Cubemap(this.info.data);
        }
        if (parameter == null) {
            return cubemap;
        }
        cubemap.setFilter(parameter.minFilter, parameter.magFilter);
        cubemap.setWrap(parameter.wrapU, parameter.wrapV);
        return cubemap;
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, CubemapParameter parameter) {
        return null;
    }
}
