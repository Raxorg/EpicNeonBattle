package com.badlogic.gdx.maps.tiled.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteCache;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;

public class OrthoCachedTiledMapRenderer implements TiledMapRenderer, Disposable {
    protected static final int NUM_VERTICES = 20;
    private static final float tolerance = 1.0E-5f;
    protected boolean blending;
    protected final Rectangle cacheBounds;
    protected boolean cached;
    protected boolean canCacheMoreE;
    protected boolean canCacheMoreN;
    protected boolean canCacheMoreS;
    protected boolean canCacheMoreW;
    protected int count;
    protected final TiledMap map;
    protected float maxTileHeight;
    protected float maxTileWidth;
    protected float overCache;
    protected final SpriteCache spriteCache;
    protected float unitScale;
    protected final float[] vertices;
    protected final Rectangle viewBounds;

    public OrthoCachedTiledMapRenderer(TiledMap map) {
        this(map, 1.0f, 2000);
    }

    public OrthoCachedTiledMapRenderer(TiledMap map, float unitScale) {
        this(map, unitScale, 2000);
    }

    public OrthoCachedTiledMapRenderer(TiledMap map, float unitScale, int cacheSize) {
        this.vertices = new float[NUM_VERTICES];
        this.viewBounds = new Rectangle();
        this.cacheBounds = new Rectangle();
        this.overCache = 0.5f;
        this.map = map;
        this.unitScale = unitScale;
        this.spriteCache = new SpriteCache(cacheSize, true);
    }

    public void setView(OrthographicCamera camera) {
        this.spriteCache.setProjectionMatrix(camera.combined);
        float width = (camera.viewportWidth * camera.zoom) + ((this.maxTileWidth * 2.0f) * this.unitScale);
        float height = (camera.viewportHeight * camera.zoom) + ((this.maxTileHeight * 2.0f) * this.unitScale);
        this.viewBounds.set(camera.position.x - (width / 2.0f), camera.position.y - (height / 2.0f), width, height);
        if ((this.canCacheMoreW && this.viewBounds.x < this.cacheBounds.x - tolerance) || ((this.canCacheMoreS && this.viewBounds.y < this.cacheBounds.y - tolerance) || ((this.canCacheMoreE && this.viewBounds.x + this.viewBounds.width > (this.cacheBounds.x + this.cacheBounds.width) + tolerance) || (this.canCacheMoreN && this.viewBounds.y + this.viewBounds.height > (this.cacheBounds.y + this.cacheBounds.height) + tolerance)))) {
            this.cached = false;
        }
    }

    public void setView(Matrix4 projection, float x, float y, float width, float height) {
        this.spriteCache.setProjectionMatrix(projection);
        this.viewBounds.set(x - (this.maxTileWidth * this.unitScale), y - (this.maxTileHeight * this.unitScale), width + ((this.maxTileWidth * 2.0f) * this.unitScale), height + ((this.maxTileHeight * 2.0f) * this.unitScale));
        if ((this.canCacheMoreW && this.viewBounds.x < this.cacheBounds.x - tolerance) || ((this.canCacheMoreS && this.viewBounds.y < this.cacheBounds.y - tolerance) || ((this.canCacheMoreE && this.viewBounds.x + this.viewBounds.width > (this.cacheBounds.x + this.cacheBounds.width) + tolerance) || (this.canCacheMoreN && this.viewBounds.y + this.viewBounds.height > (this.cacheBounds.y + this.cacheBounds.height) + tolerance)))) {
            this.cached = false;
        }
    }

    public void render() {
        MapLayer layer;
        if (!this.cached) {
            this.cached = true;
            this.count = 0;
            this.spriteCache.clear();
            float extraWidth = this.viewBounds.width * this.overCache;
            float extraHeight = this.viewBounds.height * this.overCache;
            this.cacheBounds.x = this.viewBounds.x - extraWidth;
            this.cacheBounds.y = this.viewBounds.y - extraHeight;
            this.cacheBounds.width = this.viewBounds.width + (extraWidth * 2.0f);
            this.cacheBounds.height = this.viewBounds.height + (extraHeight * 2.0f);
            Iterator it = this.map.getLayers().iterator();
            while (it.hasNext()) {
                layer = (MapLayer) it.next();
                this.spriteCache.beginCache();
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer) layer);
                } else if (layer instanceof TiledMapImageLayer) {
                    renderImageLayer((TiledMapImageLayer) layer);
                }
                this.spriteCache.endCache();
            }
        }
        if (this.blending) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        this.spriteCache.begin();
        MapLayers mapLayers = this.map.getLayers();
        int j = mapLayers.getCount();
        for (int i = 0; i < j; i++) {
            layer = mapLayers.get(i);
            if (layer.isVisible()) {
                this.spriteCache.draw(i);
                renderObjects(layer);
            }
        }
        this.spriteCache.end();
        if (this.blending) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void render(int[] layers) {
        MapLayer layer;
        int i = 0;
        if (!this.cached) {
            this.cached = true;
            this.count = 0;
            this.spriteCache.clear();
            float extraWidth = this.viewBounds.width * this.overCache;
            float extraHeight = this.viewBounds.height * this.overCache;
            this.cacheBounds.x = this.viewBounds.x - extraWidth;
            this.cacheBounds.y = this.viewBounds.y - extraHeight;
            this.cacheBounds.width = this.viewBounds.width + (extraWidth * 2.0f);
            this.cacheBounds.height = this.viewBounds.height + (extraHeight * 2.0f);
            Iterator it = this.map.getLayers().iterator();
            while (it.hasNext()) {
                layer = (MapLayer) it.next();
                this.spriteCache.beginCache();
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer) layer);
                } else if (layer instanceof TiledMapImageLayer) {
                    renderImageLayer((TiledMapImageLayer) layer);
                }
                this.spriteCache.endCache();
            }
        }
        if (this.blending) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        this.spriteCache.begin();
        MapLayers mapLayers = this.map.getLayers();
        int length = layers.length;
        while (i < length) {
            int i2 = layers[i];
            layer = mapLayers.get(i2);
            if (layer.isVisible()) {
                this.spriteCache.draw(i2);
                renderObjects(layer);
            }
            i++;
        }
        this.spriteCache.end();
        if (this.blending) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void renderObjects(MapLayer layer) {
        Iterator it = layer.getObjects().iterator();
        while (it.hasNext()) {
            renderObject((MapObject) it.next());
        }
    }

    public void renderObject(MapObject object) {
    }

    public void renderTileLayer(TiledMapTileLayer layer) {
        float color = Color.toFloatBits(1.0f, 1.0f, 1.0f, layer.getOpacity());
        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();
        float layerTileWidth = layer.getTileWidth() * this.unitScale;
        float layerTileHeight = layer.getTileHeight() * this.unitScale;
        int col1 = Math.max(0, (int) (this.cacheBounds.x / layerTileWidth));
        int col2 = Math.min(layerWidth, (int) (((this.cacheBounds.x + this.cacheBounds.width) + layerTileWidth) / layerTileWidth));
        int row1 = Math.max(0, (int) (this.cacheBounds.y / layerTileHeight));
        int row2 = Math.min(layerHeight, (int) (((this.cacheBounds.y + this.cacheBounds.height) + layerTileHeight) / layerTileHeight));
        this.canCacheMoreN = row2 < layerHeight;
        this.canCacheMoreE = col2 < layerWidth;
        this.canCacheMoreW = col1 > 0;
        this.canCacheMoreS = row1 > 0;
        float[] vertices = this.vertices;
        for (int row = row2; row >= row1; row--) {
            for (int col = col1; col < col2; col++) {
                Cell cell = layer.getCell(col, row);
                if (cell != null) {
                    TiledMapTile tile = cell.getTile();
                    if (tile != null) {
                        float temp;
                        this.count++;
                        boolean flipX = cell.getFlipHorizontally();
                        boolean flipY = cell.getFlipVertically();
                        int rotations = cell.getRotation();
                        TextureRegion region = tile.getTextureRegion();
                        Texture texture = region.getTexture();
                        float x1 = (((float) col) * layerTileWidth) + (tile.getOffsetX() * this.unitScale);
                        float y1 = (((float) row) * layerTileHeight) + (tile.getOffsetY() * this.unitScale);
                        float x2 = x1 + (((float) region.getRegionWidth()) * this.unitScale);
                        float y2 = y1 + (((float) region.getRegionHeight()) * this.unitScale);
                        float adjustX = 0.5f / ((float) texture.getWidth());
                        float adjustY = 0.5f / ((float) texture.getHeight());
                        float u1 = region.getU() + adjustX;
                        float v1 = region.getV2() - adjustY;
                        float u2 = region.getU2() - adjustX;
                        float v2 = region.getV() + adjustY;
                        vertices[0] = x1;
                        vertices[1] = y1;
                        vertices[2] = color;
                        vertices[3] = u1;
                        vertices[4] = v1;
                        vertices[5] = x1;
                        vertices[6] = y2;
                        vertices[7] = color;
                        vertices[8] = u1;
                        vertices[9] = v2;
                        vertices[10] = x2;
                        vertices[11] = y2;
                        vertices[12] = color;
                        vertices[13] = u2;
                        vertices[14] = v2;
                        vertices[15] = x2;
                        vertices[16] = y1;
                        vertices[17] = color;
                        vertices[18] = u2;
                        vertices[19] = v1;
                        if (flipX) {
                            temp = vertices[3];
                            vertices[3] = vertices[13];
                            vertices[13] = temp;
                            temp = vertices[8];
                            vertices[8] = vertices[18];
                            vertices[18] = temp;
                        }
                        if (flipY) {
                            temp = vertices[4];
                            vertices[4] = vertices[14];
                            vertices[14] = temp;
                            temp = vertices[9];
                            vertices[9] = vertices[19];
                            vertices[19] = temp;
                        }
                        if (rotations != 0) {
                            float tempV;
                            float tempU;
                            switch (rotations) {
                                case BuildConfig.VERSION_CODE /*1*/:
                                    tempV = vertices[4];
                                    vertices[4] = vertices[9];
                                    vertices[9] = vertices[14];
                                    vertices[14] = vertices[19];
                                    vertices[19] = tempV;
                                    tempU = vertices[3];
                                    vertices[3] = vertices[8];
                                    vertices[8] = vertices[13];
                                    vertices[13] = vertices[18];
                                    vertices[18] = tempU;
                                    break;
                                case Base.kNumLenToPosStatesBits /*2*/:
                                    tempU = vertices[3];
                                    vertices[3] = vertices[13];
                                    vertices[13] = tempU;
                                    tempU = vertices[8];
                                    vertices[8] = vertices[18];
                                    vertices[18] = tempU;
                                    tempV = vertices[4];
                                    vertices[4] = vertices[14];
                                    vertices[14] = tempV;
                                    tempV = vertices[9];
                                    vertices[9] = vertices[19];
                                    vertices[19] = tempV;
                                    break;
                                case Base.kNumMidLenBits /*3*/:
                                    tempV = vertices[4];
                                    vertices[4] = vertices[19];
                                    vertices[19] = vertices[14];
                                    vertices[14] = vertices[9];
                                    vertices[9] = tempV;
                                    tempU = vertices[3];
                                    vertices[3] = vertices[18];
                                    vertices[18] = vertices[13];
                                    vertices[13] = vertices[8];
                                    vertices[8] = tempU;
                                    break;
                            }
                        }
                        this.spriteCache.add(texture, vertices, 0, NUM_VERTICES);
                    }
                }
            }
        }
    }

    public void renderImageLayer(TiledMapImageLayer layer) {
        float color = Color.toFloatBits(1.0f, 1.0f, 1.0f, layer.getOpacity());
        float[] vertices = this.vertices;
        TextureRegion region = layer.getTextureRegion();
        if (region != null) {
            float x = layer.getX();
            float x1 = x * this.unitScale;
            float y1 = layer.getY() * this.unitScale;
            float x2 = x1 + (((float) region.getRegionWidth()) * this.unitScale);
            float y2 = y1 + (((float) region.getRegionHeight()) * this.unitScale);
            float u1 = region.getU();
            float v1 = region.getV2();
            float u2 = region.getU2();
            float v2 = region.getV();
            vertices[0] = x1;
            vertices[1] = y1;
            vertices[2] = color;
            vertices[3] = u1;
            vertices[4] = v1;
            vertices[5] = x1;
            vertices[6] = y2;
            vertices[7] = color;
            vertices[8] = u1;
            vertices[9] = v2;
            vertices[10] = x2;
            vertices[11] = y2;
            vertices[12] = color;
            vertices[13] = u2;
            vertices[14] = v2;
            vertices[15] = x2;
            vertices[16] = y1;
            vertices[17] = color;
            vertices[18] = u2;
            vertices[19] = v1;
            this.spriteCache.add(region.getTexture(), vertices, 0, NUM_VERTICES);
        }
    }

    public void invalidateCache() {
        this.cached = false;
    }

    public boolean isCached() {
        return this.cached;
    }

    public void setOverCache(float overCache) {
        this.overCache = overCache;
    }

    public void setMaxTileSize(float maxPixelWidth, float maxPixelHeight) {
        this.maxTileWidth = maxPixelWidth;
        this.maxTileHeight = maxPixelHeight;
    }

    public void setBlending(boolean blending) {
        this.blending = blending;
    }

    public SpriteCache getSpriteCache() {
        return this.spriteCache;
    }

    public void dispose() {
        this.spriteCache.dispose();
    }
}
