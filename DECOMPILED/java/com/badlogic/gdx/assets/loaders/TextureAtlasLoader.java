package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Page;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class TextureAtlasLoader extends SynchronousAssetLoader<TextureAtlas, TextureAtlasParameter> {
    TextureAtlasData data;

    public static class TextureAtlasParameter extends AssetLoaderParameters<TextureAtlas> {
        public boolean flip = false;

        public TextureAtlasParameter(boolean flip) {
            this.flip = flip;
        }
    }

    public TextureAtlasLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public TextureAtlas load(AssetManager assetManager, String fileName, FileHandle file, TextureAtlasParameter parameter) {
        Iterator it = this.data.getPages().iterator();
        while (it.hasNext()) {
            Page page = (Page) it.next();
            page.texture = (Texture) assetManager.get(page.textureFile.path().replaceAll("\\\\", "/"), Texture.class);
        }
        return new TextureAtlas(this.data);
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle atlasFile, TextureAtlasParameter parameter) {
        FileHandle imgDir = atlasFile.parent();
        if (parameter != null) {
            this.data = new TextureAtlasData(atlasFile, imgDir, parameter.flip);
        } else {
            this.data = new TextureAtlasData(atlasFile, imgDir, false);
        }
        Array<AssetDescriptor> dependencies = new Array();
        Iterator it = this.data.getPages().iterator();
        while (it.hasNext()) {
            Page page = (Page) it.next();
            AssetLoaderParameters params = new TextureParameter();
            params.format = page.format;
            params.genMipMaps = page.useMipMaps;
            params.minFilter = page.minFilter;
            params.magFilter = page.magFilter;
            dependencies.add(new AssetDescriptor(page.textureFile, Texture.class, params));
        }
        return dependencies;
    }
}
