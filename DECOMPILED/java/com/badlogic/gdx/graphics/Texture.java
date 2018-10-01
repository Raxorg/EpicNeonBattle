package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader.TextureParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData.Factory;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Texture extends GLTexture {
    private static AssetManager assetManager;
    static final Map<Application, Array<Texture>> managedTextures = new HashMap();
    TextureData data;

    public enum TextureFilter {
        Nearest(GL20.GL_NEAREST),
        Linear(GL20.GL_LINEAR),
        MipMap(GL20.GL_LINEAR_MIPMAP_LINEAR),
        MipMapNearestNearest(GL20.GL_NEAREST_MIPMAP_NEAREST),
        MipMapLinearNearest(GL20.GL_LINEAR_MIPMAP_NEAREST),
        MipMapNearestLinear(GL20.GL_NEAREST_MIPMAP_LINEAR),
        MipMapLinearLinear(GL20.GL_LINEAR_MIPMAP_LINEAR);
        
        final int glEnum;

        private TextureFilter(int glEnum) {
            this.glEnum = glEnum;
        }

        public boolean isMipMap() {
            return (this.glEnum == GL20.GL_NEAREST || this.glEnum == GL20.GL_LINEAR) ? false : true;
        }

        public int getGLEnum() {
            return this.glEnum;
        }
    }

    public enum TextureWrap {
        MirroredRepeat(GL20.GL_MIRRORED_REPEAT),
        ClampToEdge(GL20.GL_CLAMP_TO_EDGE),
        Repeat(GL20.GL_REPEAT);
        
        final int glEnum;

        private TextureWrap(int glEnum) {
            this.glEnum = glEnum;
        }

        public int getGLEnum() {
            return this.glEnum;
        }
    }

    public Texture(String internalPath) {
        this(Gdx.files.internal(internalPath));
    }

    public Texture(FileHandle file) {
        this(file, null, false);
    }

    public Texture(FileHandle file, boolean useMipMaps) {
        this(file, null, useMipMaps);
    }

    public Texture(FileHandle file, Format format, boolean useMipMaps) {
        this(Factory.loadFromFile(file, format, useMipMaps));
    }

    public Texture(Pixmap pixmap) {
        this(new PixmapTextureData(pixmap, null, false, false));
    }

    public Texture(Pixmap pixmap, boolean useMipMaps) {
        this(new PixmapTextureData(pixmap, null, useMipMaps, false));
    }

    public Texture(Pixmap pixmap, Format format, boolean useMipMaps) {
        this(new PixmapTextureData(pixmap, format, useMipMaps, false));
    }

    public Texture(int width, int height, Format format) {
        this(new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    public Texture(TextureData data) {
        super(GL20.GL_TEXTURE_2D, Gdx.gl.glGenTexture());
        load(data);
        if (data.isManaged()) {
            addManagedTexture(Gdx.app, this);
        }
    }

    public void load(TextureData data) {
        if (this.data == null || data.isManaged() == this.data.isManaged()) {
            this.data = data;
            if (!data.isPrepared()) {
                data.prepare();
            }
            bind();
            GLTexture.uploadImageData(GL20.GL_TEXTURE_2D, data);
            setFilter(this.minFilter, this.magFilter);
            setWrap(this.uWrap, this.vWrap);
            Gdx.gl.glBindTexture(this.glTarget, 0);
            return;
        }
        throw new GdxRuntimeException("New data must have the same managed status as the old data");
    }

    protected void reload() {
        if (isManaged()) {
            this.glHandle = Gdx.gl.glGenTexture();
            load(this.data);
            return;
        }
        throw new GdxRuntimeException("Tried to reload unmanaged Texture");
    }

    public void draw(Pixmap pixmap, int x, int y) {
        if (this.data.isManaged()) {
            throw new GdxRuntimeException("can't draw to a managed texture");
        }
        bind();
        Gdx.gl.glTexSubImage2D(this.glTarget, 0, x, y, pixmap.getWidth(), pixmap.getHeight(), pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
    }

    public int getWidth() {
        return this.data.getWidth();
    }

    public int getHeight() {
        return this.data.getHeight();
    }

    public int getDepth() {
        return 0;
    }

    public TextureData getTextureData() {
        return this.data;
    }

    public boolean isManaged() {
        return this.data.isManaged();
    }

    public void dispose() {
        if (this.glHandle != 0) {
            delete();
            if (this.data.isManaged() && managedTextures.get(Gdx.app) != null) {
                ((Array) managedTextures.get(Gdx.app)).removeValue(this, true);
            }
        }
    }

    private static void addManagedTexture(Application app, Texture texture) {
        Array<Texture> managedTextureArray = (Array) managedTextures.get(app);
        if (managedTextureArray == null) {
            managedTextureArray = new Array();
        }
        managedTextureArray.add(texture);
        managedTextures.put(app, managedTextureArray);
    }

    public static void clearAllTextures(Application app) {
        managedTextures.remove(app);
    }

    public static void invalidateAllTextures(Application app) {
        Array managedTextureArray = (Array) managedTextures.get(app);
        if (managedTextureArray != null) {
            if (assetManager == null) {
                for (int i = 0; i < managedTextureArray.size; i++) {
                    ((Texture) managedTextureArray.get(i)).reload();
                }
                return;
            }
            assetManager.finishLoading();
            Array textures = new Array(managedTextureArray);
            Iterator it = textures.iterator();
            while (it.hasNext()) {
                Texture texture = (Texture) it.next();
                String fileName = assetManager.getAssetFileName(texture);
                if (fileName == null) {
                    texture.reload();
                } else {
                    final int refCount = assetManager.getReferenceCount(fileName);
                    assetManager.setReferenceCount(fileName, 0);
                    texture.glHandle = 0;
                    TextureParameter params = new TextureParameter();
                    params.textureData = texture.getTextureData();
                    params.minFilter = texture.getMinFilter();
                    params.magFilter = texture.getMagFilter();
                    params.wrapU = texture.getUWrap();
                    params.wrapV = texture.getVWrap();
                    params.genMipMaps = texture.data.useMipMaps();
                    params.texture = texture;
                    params.loadedCallback = new LoadedCallback() {
                        public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                            assetManager.setReferenceCount(fileName, refCount);
                        }
                    };
                    assetManager.unload(fileName);
                    texture.glHandle = Gdx.gl.glGenTexture();
                    assetManager.load(fileName, Texture.class, params);
                }
            }
            managedTextureArray.clear();
            managedTextureArray.addAll(textures);
        }
    }

    public static void setAssetManager(AssetManager manager) {
        assetManager = manager;
    }

    public static String getManagedStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append("Managed textures/app: { ");
        for (Application app : managedTextures.keySet()) {
            builder.append(((Array) managedTextures.get(app)).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    public static int getNumManagedTextures() {
        return ((Array) managedTextures.get(Gdx.app)).size;
    }
}
