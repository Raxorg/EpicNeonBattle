package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public interface TextureProvider {

    public static class AssetTextureProvider implements TextureProvider {
        public final AssetManager assetManager;

        public AssetTextureProvider(AssetManager assetManager) {
            this.assetManager = assetManager;
        }

        public Texture load(String fileName) {
            return (Texture) this.assetManager.get(fileName, Texture.class);
        }
    }

    public static class FileTextureProvider implements TextureProvider {
        public Texture load(String fileName) {
            Texture result = new Texture(Gdx.files.internal(fileName));
            result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            result.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
            return result;
        }
    }

    Texture load(String str);
}
