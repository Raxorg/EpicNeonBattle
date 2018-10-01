package com.badlogic.gdx.backends.android;

import android.content.res.AssetManager;
import android.os.Environment;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.files.FileHandle;

public class AndroidFiles implements Files {
    protected final AssetManager assets;
    protected final String localpath;
    protected final String sdcard = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/");

    public AndroidFiles(AssetManager assets) {
        this.assets = assets;
        this.localpath = this.sdcard;
    }

    public AndroidFiles(AssetManager assets, String localpath) {
        this.assets = assets;
        if (!localpath.endsWith("/")) {
            localpath = localpath + "/";
        }
        this.localpath = localpath;
    }

    public FileHandle getFileHandle(String path, FileType type) {
        return new AndroidFileHandle(type == FileType.Internal ? this.assets : null, path, type);
    }

    public FileHandle classpath(String path) {
        return new AndroidFileHandle(null, path, FileType.Classpath);
    }

    public FileHandle internal(String path) {
        return new AndroidFileHandle(this.assets, path, FileType.Internal);
    }

    public FileHandle external(String path) {
        return new AndroidFileHandle(null, path, FileType.External);
    }

    public FileHandle absolute(String path) {
        return new AndroidFileHandle(null, path, FileType.Absolute);
    }

    public FileHandle local(String path) {
        return new AndroidFileHandle(null, path, FileType.Local);
    }

    public String getExternalStoragePath() {
        return this.sdcard;
    }

    public boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public String getLocalStoragePath() {
        return this.localpath;
    }

    public boolean isLocalStorageAvailable() {
        return true;
    }
}
