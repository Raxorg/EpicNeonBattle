package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetLoaderParameters.LoadedCallback;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.CubemapLoader.CubemapParameter;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.TextureData.Factory;
import com.badlogic.gdx.graphics.glutils.FacedCubemapData;
import com.badlogic.gdx.graphics.glutils.PixmapTextureData;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Cubemap extends GLTexture {
    private static AssetManager assetManager;
    static final Map<Application, Array<Cubemap>> managedCubemaps = new HashMap();
    protected CubemapData data;

    public enum CubemapSide {
        PositiveX(0, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f),
        NegativeX(1, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f, 0.0f),
        PositiveY(2, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 0.0f),
        NegativeY(3, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0.0f, 0.0f, -1.0f, 0.0f, -1.0f, 0.0f),
        PositiveZ(4, GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, 1.0f),
        NegativeZ(5, GL20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0.0f, -1.0f, 0.0f, 0.0f, 0.0f, -1.0f);
        
        public final Vector3 direction;
        public final int glEnum;
        public final int index;
        public final Vector3 up;

        private CubemapSide(int index, int glEnum, float upX, float upY, float upZ, float directionX, float directionY, float directionZ) {
            this.index = index;
            this.glEnum = glEnum;
            this.up = new Vector3(upX, upY, upZ);
            this.direction = new Vector3(directionX, directionY, directionZ);
        }

        public int getGLEnum() {
            return this.glEnum;
        }

        public Vector3 getUp(Vector3 out) {
            return out.set(this.up);
        }

        public Vector3 getDirection(Vector3 out) {
            return out.set(this.direction);
        }
    }

    public Cubemap(CubemapData data) {
        super(GL20.GL_TEXTURE_CUBE_MAP);
        this.data = data;
        load(data);
    }

    public Cubemap(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    public Cubemap(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
        this(Factory.loadFromFile(positiveX, useMipMaps), Factory.loadFromFile(negativeX, useMipMaps), Factory.loadFromFile(positiveY, useMipMaps), Factory.loadFromFile(negativeY, useMipMaps), Factory.loadFromFile(positiveZ, useMipMaps), Factory.loadFromFile(negativeZ, useMipMaps));
    }

    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    public Cubemap(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps) {
        TextureData textureData = null;
        TextureData pixmapTextureData = positiveX == null ? null : new PixmapTextureData(positiveX, null, useMipMaps, false);
        TextureData pixmapTextureData2 = negativeX == null ? null : new PixmapTextureData(negativeX, null, useMipMaps, false);
        TextureData pixmapTextureData3 = positiveY == null ? null : new PixmapTextureData(positiveY, null, useMipMaps, false);
        TextureData pixmapTextureData4 = negativeY == null ? null : new PixmapTextureData(negativeY, null, useMipMaps, false);
        TextureData pixmapTextureData5 = positiveZ == null ? null : new PixmapTextureData(positiveZ, null, useMipMaps, false);
        if (negativeZ != null) {
            Object pixmapTextureData6 = new PixmapTextureData(negativeZ, null, useMipMaps, false);
        }
        this(pixmapTextureData, pixmapTextureData2, pixmapTextureData3, pixmapTextureData4, pixmapTextureData5, textureData);
    }

    public Cubemap(int width, int height, int depth, Format format) {
        this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    public Cubemap(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
        super(GL20.GL_TEXTURE_CUBE_MAP);
        this.minFilter = TextureFilter.Nearest;
        this.magFilter = TextureFilter.Nearest;
        this.uWrap = TextureWrap.ClampToEdge;
        this.vWrap = TextureWrap.ClampToEdge;
        this.data = new FacedCubemapData(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ);
        load(this.data);
    }

    public void load(CubemapData data) {
        if (!data.isPrepared()) {
            data.prepare();
        }
        bind();
        unsafeSetFilter(this.minFilter, this.magFilter, true);
        unsafeSetWrap(this.uWrap, this.vWrap, true);
        data.consumeCubemapData();
        Gdx.gl.glBindTexture(this.glTarget, 0);
    }

    public CubemapData getCubemapData() {
        return this.data;
    }

    public boolean isManaged() {
        return this.data.isManaged();
    }

    protected void reload() {
        if (isManaged()) {
            this.glHandle = Gdx.gl.glGenTexture();
            load(this.data);
            return;
        }
        throw new GdxRuntimeException("Tried to reload an unmanaged Cubemap");
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

    public void dispose() {
        if (this.glHandle != 0) {
            delete();
            if (this.data.isManaged() && managedCubemaps.get(Gdx.app) != null) {
                ((Array) managedCubemaps.get(Gdx.app)).removeValue(this, true);
            }
        }
    }

    private static void addManagedCubemap(Application app, Cubemap cubemap) {
        Array<Cubemap> managedCubemapArray = (Array) managedCubemaps.get(app);
        if (managedCubemapArray == null) {
            managedCubemapArray = new Array();
        }
        managedCubemapArray.add(cubemap);
        managedCubemaps.put(app, managedCubemapArray);
    }

    public static void clearAllCubemaps(Application app) {
        managedCubemaps.remove(app);
    }

    public static void invalidateAllCubemaps(Application app) {
        Array managedCubemapArray = (Array) managedCubemaps.get(app);
        if (managedCubemapArray != null) {
            if (assetManager == null) {
                for (int i = 0; i < managedCubemapArray.size; i++) {
                    ((Cubemap) managedCubemapArray.get(i)).reload();
                }
                return;
            }
            assetManager.finishLoading();
            Array cubemaps = new Array(managedCubemapArray);
            Iterator it = cubemaps.iterator();
            while (it.hasNext()) {
                Cubemap cubemap = (Cubemap) it.next();
                String fileName = assetManager.getAssetFileName(cubemap);
                if (fileName == null) {
                    cubemap.reload();
                } else {
                    final int refCount = assetManager.getReferenceCount(fileName);
                    assetManager.setReferenceCount(fileName, 0);
                    cubemap.glHandle = 0;
                    CubemapParameter params = new CubemapParameter();
                    params.cubemapData = cubemap.getCubemapData();
                    params.minFilter = cubemap.getMinFilter();
                    params.magFilter = cubemap.getMagFilter();
                    params.wrapU = cubemap.getUWrap();
                    params.wrapV = cubemap.getVWrap();
                    params.cubemap = cubemap;
                    params.loadedCallback = new LoadedCallback() {
                        public void finishedLoading(AssetManager assetManager, String fileName, Class type) {
                            assetManager.setReferenceCount(fileName, refCount);
                        }
                    };
                    assetManager.unload(fileName);
                    cubemap.glHandle = Gdx.gl.glGenTexture();
                    assetManager.load(fileName, Cubemap.class, params);
                }
            }
            managedCubemapArray.clear();
            managedCubemapArray.addAll(cubemaps);
        }
    }

    public static void setAssetManager(AssetManager manager) {
        assetManager = manager;
    }

    public static String getManagedStatus() {
        StringBuilder builder = new StringBuilder();
        builder.append("Managed cubemap/app: { ");
        for (Application app : managedCubemaps.keySet()) {
            builder.append(((Array) managedCubemaps.get(app)).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder.toString();
    }

    public static int getNumManagedCubemaps() {
        return ((Array) managedCubemaps.get(Gdx.app)).size;
    }
}
