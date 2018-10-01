package com.badlogic.gdx.assets;

public class AssetLoaderParameters<T> {
    public LoadedCallback loadedCallback;

    public interface LoadedCallback {
        void finishedLoading(AssetManager assetManager, String str, Class cls);
    }
}
