package com.badlogic.gdx.assets.loaders;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

public class SoundLoader extends AsynchronousAssetLoader<Sound, SoundParameter> {
    private Sound sound;

    public static class SoundParameter extends AssetLoaderParameters<Sound> {
    }

    public SoundLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter) {
        this.sound = Gdx.audio.newSound(file);
    }

    public Sound loadSync(AssetManager manager, String fileName, FileHandle file, SoundParameter parameter) {
        Sound sound = this.sound;
        this.sound = null;
        return sound;
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, SoundParameter parameter) {
        return null;
    }
}
