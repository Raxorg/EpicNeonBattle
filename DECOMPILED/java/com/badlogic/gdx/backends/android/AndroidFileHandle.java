package com.badlogic.gdx.backends.android;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

public class AndroidFileHandle extends FileHandle {
    final AssetManager assets;

    AndroidFileHandle(AssetManager assets, String fileName, FileType type) {
        super(fileName.replace('\\', '/'), type);
        this.assets = assets;
    }

    AndroidFileHandle(AssetManager assets, File file, FileType type) {
        super(file, type);
        this.assets = assets;
    }

    public FileHandle child(String name) {
        name = name.replace('\\', '/');
        if (this.file.getPath().length() == 0) {
            return new AndroidFileHandle(this.assets, new File(name), this.type);
        }
        return new AndroidFileHandle(this.assets, new File(this.file, name), this.type);
    }

    public FileHandle sibling(String name) {
        name = name.replace('\\', '/');
        if (this.file.getPath().length() != 0) {
            return new AndroidFileHandle(this.assets, new File(this.file.getParent(), name), this.type);
        }
        throw new GdxRuntimeException("Cannot get the sibling of the root.");
    }

    public FileHandle parent() {
        File parent = this.file.getParentFile();
        if (parent == null) {
            if (this.type == FileType.Absolute) {
                parent = new File("/");
            } else {
                parent = new File(BuildConfig.VERSION_NAME);
            }
        }
        return new AndroidFileHandle(this.assets, parent, this.type);
    }

    public InputStream read() {
        if (this.type != FileType.Internal) {
            return super.read();
        }
        try {
            return this.assets.open(this.file.getPath());
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error reading file: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public FileHandle[] list() {
        if (this.type != FileType.Internal) {
            return super.list();
        }
        try {
            String[] relativePaths = this.assets.list(this.file.getPath());
            FileHandle[] fileHandleArr = new FileHandle[relativePaths.length];
            int n = fileHandleArr.length;
            for (int i = 0; i < n; i++) {
                fileHandleArr[i] = new AndroidFileHandle(this.assets, new File(this.file, relativePaths[i]), this.type);
            }
            return fileHandleArr;
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error listing children: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public FileHandle[] list(FileFilter filter) {
        if (this.type != FileType.Internal) {
            return super.list(filter);
        }
        try {
            String[] relativePaths = this.assets.list(this.file.getPath());
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            int n = handles.length;
            for (int i = 0; i < n; i++) {
                FileHandle child = new AndroidFileHandle(this.assets, new File(this.file, relativePaths[i]), this.type);
                if (filter.accept(child.file())) {
                    handles[count] = child;
                    count++;
                }
            }
            if (count >= relativePaths.length) {
                return handles;
            }
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            return newHandles;
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error listing children: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public FileHandle[] list(FilenameFilter filter) {
        if (this.type != FileType.Internal) {
            return super.list(filter);
        }
        try {
            String[] relativePaths = this.assets.list(this.file.getPath());
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            int n = handles.length;
            for (int i = 0; i < n; i++) {
                String path = relativePaths[i];
                if (filter.accept(this.file, path)) {
                    handles[count] = new AndroidFileHandle(this.assets, new File(this.file, path), this.type);
                    count++;
                }
            }
            if (count >= relativePaths.length) {
                return handles;
            }
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            return newHandles;
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error listing children: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public FileHandle[] list(String suffix) {
        if (this.type != FileType.Internal) {
            return super.list(suffix);
        }
        try {
            String[] relativePaths = this.assets.list(this.file.getPath());
            FileHandle[] handles = new FileHandle[relativePaths.length];
            int count = 0;
            int n = handles.length;
            for (int i = 0; i < n; i++) {
                String path = relativePaths[i];
                if (path.endsWith(suffix)) {
                    handles[count] = new AndroidFileHandle(this.assets, new File(this.file, path), this.type);
                    count++;
                }
            }
            if (count >= relativePaths.length) {
                return handles;
            }
            FileHandle[] newHandles = new FileHandle[count];
            System.arraycopy(handles, 0, newHandles, 0, count);
            return newHandles;
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error listing children: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public boolean isDirectory() {
        if (this.type != FileType.Internal) {
            return super.isDirectory();
        }
        try {
            if (this.assets.list(this.file.getPath()).length > 0) {
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean exists() {
        if (this.type != FileType.Internal) {
            return super.exists();
        }
        String fileName = this.file.getPath();
        try {
            this.assets.open(fileName).close();
            return true;
        } catch (Exception e) {
            try {
                if (this.assets.list(fileName).length <= 0) {
                    return false;
                }
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    public long length() {
        if (this.type == FileType.Internal) {
            AssetFileDescriptor fileDescriptor = null;
            try {
                fileDescriptor = this.assets.openFd(this.file.getPath());
                long length = fileDescriptor.getLength();
                if (fileDescriptor == null) {
                    return length;
                }
                try {
                    fileDescriptor.close();
                    return length;
                } catch (IOException e) {
                    return length;
                }
            } catch (IOException e2) {
                if (fileDescriptor != null) {
                    try {
                        fileDescriptor.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th) {
                if (fileDescriptor != null) {
                    try {
                        fileDescriptor.close();
                    } catch (IOException e4) {
                    }
                }
            }
        }
        return super.length();
    }

    public long lastModified() {
        return super.lastModified();
    }

    public File file() {
        if (this.type == FileType.Local) {
            return new File(Gdx.files.getLocalStoragePath(), this.file.getPath());
        }
        return super.file();
    }
}
