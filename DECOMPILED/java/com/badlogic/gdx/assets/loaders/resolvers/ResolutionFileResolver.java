package com.badlogic.gdx.assets.loaders.resolvers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.epicness.neonbattle.android.BuildConfig;

public class ResolutionFileResolver implements FileHandleResolver {
    protected final FileHandleResolver baseResolver;
    protected final Resolution[] descriptors;

    public static class Resolution {
        public final String folder;
        public final int portraitHeight;
        public final int portraitWidth;

        public Resolution(int portraitWidth, int portraitHeight, String folder) {
            this.portraitWidth = portraitWidth;
            this.portraitHeight = portraitHeight;
            this.folder = folder;
        }
    }

    public ResolutionFileResolver(FileHandleResolver baseResolver, Resolution... descriptors) {
        if (descriptors.length == 0) {
            throw new IllegalArgumentException("At least one Resolution needs to be supplied.");
        }
        this.baseResolver = baseResolver;
        this.descriptors = descriptors;
    }

    public FileHandle resolve(String fileName) {
        Resolution bestResolution = choose(this.descriptors);
        FileHandle handle = this.baseResolver.resolve(resolve(new FileHandle(fileName), bestResolution.folder));
        if (handle.exists()) {
            return handle;
        }
        return this.baseResolver.resolve(fileName);
    }

    protected String resolve(FileHandle originalHandle, String suffix) {
        String parentString = BuildConfig.VERSION_NAME;
        FileHandle parent = originalHandle.parent();
        if (!(parent == null || parent.name().equals(BuildConfig.VERSION_NAME))) {
            parentString = parent + "/";
        }
        return parentString + suffix + "/" + originalHandle.name();
    }

    public static Resolution choose(Resolution... descriptors) {
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        Resolution best = descriptors[0];
        int n;
        int i;
        Resolution other;
        if (w < h) {
            n = descriptors.length;
            for (i = 0; i < n; i++) {
                other = descriptors[i];
                if (w >= other.portraitWidth && other.portraitWidth >= best.portraitWidth && h >= other.portraitHeight && other.portraitHeight >= best.portraitHeight) {
                    best = descriptors[i];
                }
            }
        } else {
            n = descriptors.length;
            for (i = 0; i < n; i++) {
                other = descriptors[i];
                if (w >= other.portraitHeight && other.portraitHeight >= best.portraitHeight && h >= other.portraitWidth && other.portraitWidth >= best.portraitWidth) {
                    best = descriptors[i];
                }
            }
        }
        return best;
    }
}
