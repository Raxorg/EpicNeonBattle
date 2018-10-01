package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.BaseTmxMapLoader.Parameters;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader.Element;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.IOException;
import java.util.Iterator;

public class AtlasTmxMapLoader extends BaseTmxMapLoader<AtlasTiledMapLoaderParameters> {
    protected Array<Texture> trackedTextures = new Array();

    private interface AtlasResolver {

        public static class AssetManagerAtlasResolver implements AtlasResolver {
            private final AssetManager assetManager;

            public AssetManagerAtlasResolver(AssetManager assetManager) {
                this.assetManager = assetManager;
            }

            public TextureAtlas getAtlas(String name) {
                return (TextureAtlas) this.assetManager.get(name, TextureAtlas.class);
            }
        }

        public static class DirectAtlasResolver implements AtlasResolver {
            private final ObjectMap<String, TextureAtlas> atlases;

            public DirectAtlasResolver(ObjectMap<String, TextureAtlas> atlases) {
                this.atlases = atlases;
            }

            public TextureAtlas getAtlas(String name) {
                return (TextureAtlas) this.atlases.get(name);
            }
        }

        TextureAtlas getAtlas(String str);
    }

    public static class AtlasTiledMapLoaderParameters extends Parameters {
        public boolean forceTextureFilters = false;
    }

    public AtlasTmxMapLoader() {
        super(new InternalFileHandleResolver());
    }

    public AtlasTmxMapLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public TiledMap load(String fileName) {
        return load(fileName, new AtlasTiledMapLoaderParameters());
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle tmxFile, AtlasTiledMapLoaderParameters parameter) {
        Array<AssetDescriptor> dependencies = new Array();
        try {
            this.root = this.xml.parse(tmxFile);
            Element properties = this.root.getChildByName("properties");
            if (properties != null) {
                Iterator it = properties.getChildrenByName("property").iterator();
                while (it.hasNext()) {
                    Element property = (Element) it.next();
                    String name = property.getAttribute("name");
                    String value = property.getAttribute("value");
                    if (name.startsWith("atlas")) {
                        dependencies.add(new AssetDescriptor(BaseTmxMapLoader.getRelativeFileHandle(tmxFile, value), TextureAtlas.class));
                    }
                }
            }
            return dependencies;
        } catch (IOException e) {
            throw new GdxRuntimeException("Unable to parse .tmx file.");
        }
    }

    public TiledMap load(String fileName, AtlasTiledMapLoaderParameters parameter) {
        if (parameter != null) {
            try {
                this.convertObjectToTileSpace = parameter.convertObjectToTileSpace;
                this.flipY = parameter.flipY;
            } catch (IOException e) {
                throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
            }
        }
        this.convertObjectToTileSpace = false;
        this.flipY = true;
        FileHandle tmxFile = resolve(fileName);
        this.root = this.xml.parse(tmxFile);
        ObjectMap<String, TextureAtlas> atlases = new ObjectMap();
        FileHandle atlasFile = loadAtlas(this.root, tmxFile);
        if (atlasFile == null) {
            throw new GdxRuntimeException("Couldn't load atlas");
        }
        atlases.put(atlasFile.path(), new TextureAtlas(atlasFile));
        TiledMap map = loadMap(this.root, tmxFile, new DirectAtlasResolver(atlases));
        map.setOwnedResources(atlases.values().toArray());
        setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
        return map;
    }

    protected FileHandle loadAtlas(Element root, FileHandle tmxFile) throws IOException {
        Element e = root.getChildByName("properties");
        if (e != null) {
            Iterator it = e.getChildrenByName("property").iterator();
            while (it.hasNext()) {
                Element property = (Element) it.next();
                String name = property.getAttribute("name", null);
                String value = property.getAttribute("value", null);
                if (name.equals("atlas")) {
                    if (value == null) {
                        value = property.getText();
                    }
                    if (!(value == null || value.length() == 0)) {
                        return BaseTmxMapLoader.getRelativeFileHandle(tmxFile, value);
                    }
                }
            }
        }
        FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
        return !atlasFile.exists() ? null : atlasFile;
    }

    private void setTextureFilters(TextureFilter min, TextureFilter mag) {
        Iterator it = this.trackedTextures.iterator();
        while (it.hasNext()) {
            ((Texture) it.next()).setFilter(min, mag);
        }
        this.trackedTextures.clear();
    }

    public void loadAsync(AssetManager manager, String fileName, FileHandle tmxFile, AtlasTiledMapLoaderParameters parameter) {
        this.map = null;
        if (parameter != null) {
            this.convertObjectToTileSpace = parameter.convertObjectToTileSpace;
            this.flipY = parameter.flipY;
        } else {
            this.convertObjectToTileSpace = false;
            this.flipY = true;
        }
        try {
            this.map = loadMap(this.root, tmxFile, new AssetManagerAtlasResolver(manager));
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    public TiledMap loadSync(AssetManager manager, String fileName, FileHandle file, AtlasTiledMapLoaderParameters parameter) {
        if (parameter != null) {
            setTextureFilters(parameter.textureMinFilter, parameter.textureMagFilter);
        }
        return this.map;
    }

    protected TiledMap loadMap(Element root, FileHandle tmxFile, AtlasResolver resolver) {
        TiledMap map = new TiledMap();
        String mapOrientation = root.getAttribute("orientation", null);
        int mapWidth = root.getIntAttribute("width", 0);
        int mapHeight = root.getIntAttribute("height", 0);
        int tileWidth = root.getIntAttribute("tilewidth", 0);
        int tileHeight = root.getIntAttribute("tileheight", 0);
        String mapBackgroundColor = root.getAttribute("backgroundcolor", null);
        MapProperties mapProperties = map.getProperties();
        if (mapOrientation != null) {
            mapProperties.put("orientation", mapOrientation);
        }
        mapProperties.put("width", Integer.valueOf(mapWidth));
        mapProperties.put("height", Integer.valueOf(mapHeight));
        mapProperties.put("tilewidth", Integer.valueOf(tileWidth));
        mapProperties.put("tileheight", Integer.valueOf(tileHeight));
        if (mapBackgroundColor != null) {
            mapProperties.put("backgroundcolor", mapBackgroundColor);
        }
        this.mapTileWidth = tileWidth;
        this.mapTileHeight = tileHeight;
        this.mapWidthInPixels = mapWidth * tileWidth;
        this.mapHeightInPixels = mapHeight * tileHeight;
        if (mapOrientation != null && "staggered".equals(mapOrientation) && mapHeight > 1) {
            this.mapWidthInPixels += tileWidth / 2;
            this.mapHeightInPixels = (this.mapHeightInPixels / 2) + (tileHeight / 2);
        }
        int j = root.getChildCount();
        for (int i = 0; i < j; i++) {
            Element element = root.getChild(i);
            String elementName = element.getName();
            if (elementName.equals("properties")) {
                loadProperties(map.getProperties(), element);
            } else if (elementName.equals("tileset")) {
                loadTileset(map, element, tmxFile, resolver);
            } else if (elementName.equals("layer")) {
                loadTileLayer(map, element);
            } else if (elementName.equals("objectgroup")) {
                loadObjectGroup(map, element);
            }
        }
        return map;
    }

    protected void loadTileset(TiledMap map, Element element, FileHandle tmxFile, AtlasResolver resolver) {
        if (element.getName().equals("tileset")) {
            Element imageElement;
            String name = element.get("name", null);
            int firstgid = element.getIntAttribute("firstgid", 1);
            int tilewidth = element.getIntAttribute("tilewidth", 0);
            int tileheight = element.getIntAttribute("tileheight", 0);
            int spacing = element.getIntAttribute("spacing", 0);
            int margin = element.getIntAttribute("margin", 0);
            String source = element.getAttribute("source", null);
            int offsetX = 0;
            int offsetY = 0;
            String imageSource = BuildConfig.VERSION_NAME;
            int imageWidth = 0;
            int imageHeight = 0;
            Element offset;
            FileHandle image;
            if (source != null) {
                FileHandle tsx = BaseTmxMapLoader.getRelativeFileHandle(tmxFile, source);
                try {
                    element = this.xml.parse(tsx);
                    name = element.get("name", null);
                    tilewidth = element.getIntAttribute("tilewidth", 0);
                    tileheight = element.getIntAttribute("tileheight", 0);
                    spacing = element.getIntAttribute("spacing", 0);
                    margin = element.getIntAttribute("margin", 0);
                    offset = element.getChildByName("tileoffset");
                    if (offset != null) {
                        offsetX = offset.getIntAttribute("x", 0);
                        offsetY = offset.getIntAttribute("y", 0);
                    }
                    imageElement = element.getChildByName("image");
                    if (imageElement != null) {
                        imageSource = imageElement.getAttribute("source");
                        imageWidth = imageElement.getIntAttribute("width", 0);
                        imageHeight = imageElement.getIntAttribute("height", 0);
                        image = BaseTmxMapLoader.getRelativeFileHandle(tsx, imageSource);
                    }
                } catch (IOException e) {
                    throw new GdxRuntimeException("Error parsing external tileset.");
                }
            }
            offset = element.getChildByName("tileoffset");
            if (offset != null) {
                offsetX = offset.getIntAttribute("x", 0);
                offsetY = offset.getIntAttribute("y", 0);
            }
            imageElement = element.getChildByName("image");
            if (imageElement != null) {
                imageSource = imageElement.getAttribute("source");
                imageWidth = imageElement.getIntAttribute("width", 0);
                imageHeight = imageElement.getIntAttribute("height", 0);
                image = BaseTmxMapLoader.getRelativeFileHandle(tmxFile, imageSource);
            }
            String atlasFilePath = (String) map.getProperties().get("atlas", String.class);
            if (atlasFilePath == null) {
                FileHandle atlasFile = tmxFile.sibling(tmxFile.nameWithoutExtension() + ".atlas");
                if (atlasFile.exists()) {
                    atlasFilePath = atlasFile.name();
                }
            }
            if (atlasFilePath == null) {
                throw new GdxRuntimeException("The map is missing the 'atlas' property");
            }
            Iterator it;
            TextureRegion region;
            int tileid;
            Element tileElement;
            TiledMapTile tile;
            String terrain;
            String probability;
            Element properties;
            FileHandle atlasHandle = resolve(BaseTmxMapLoader.getRelativeFileHandle(tmxFile, atlasFilePath).path());
            TextureAtlas atlas = resolver.getAtlas(atlasHandle.path());
            String regionsName = atlasHandle.nameWithoutExtension();
            Iterator it2 = atlas.getTextures().iterator();
            while (it2.hasNext()) {
                this.trackedTextures.add((Texture) it2.next());
            }
            TiledMapTileSet tileset = new TiledMapTileSet();
            MapProperties props = tileset.getProperties();
            tileset.setName(name);
            props.put("firstgid", Integer.valueOf(firstgid));
            props.put("imagesource", imageSource);
            props.put("imagewidth", Integer.valueOf(imageWidth));
            props.put("imageheight", Integer.valueOf(imageHeight));
            props.put("tilewidth", Integer.valueOf(tilewidth));
            props.put("tileheight", Integer.valueOf(tileheight));
            props.put("margin", Integer.valueOf(margin));
            props.put("spacing", Integer.valueOf(spacing));
            if (imageSource != null && imageSource.length() > 0) {
                int lastgid = (((imageWidth / tilewidth) * (imageHeight / tileheight)) + firstgid) - 1;
                it = atlas.findRegions(regionsName).iterator();
                while (it.hasNext()) {
                    region = (AtlasRegion) it.next();
                    if (region != null) {
                        tileid = region.index + 1;
                        if (tileid >= firstgid && tileid <= lastgid) {
                            StaticTiledMapTile staticTiledMapTile = new StaticTiledMapTile(region);
                            staticTiledMapTile.setId(tileid);
                            staticTiledMapTile.setOffsetX((float) offsetX);
                            staticTiledMapTile.setOffsetY(this.flipY ? (float) (-offsetY) : (float) offsetY);
                            tileset.putTile(tileid, staticTiledMapTile);
                        }
                    }
                }
            }
            it = element.getChildrenByName("tile").iterator();
            while (it.hasNext()) {
                tileElement = (Element) it.next();
                tileid = firstgid + tileElement.getIntAttribute("id", 0);
                tile = tileset.getTile(tileid);
                if (tile == null) {
                    imageElement = tileElement.getChildByName("image");
                    if (imageElement != null) {
                        String regionName = imageElement.getAttribute("source");
                        regionName = regionName.substring(0, regionName.lastIndexOf(46));
                        region = atlas.findRegion(regionName);
                        if (region == null) {
                            throw new GdxRuntimeException("Tileset region not found: " + regionName);
                        }
                        float f;
                        TiledMapTile staticTiledMapTile2 = new StaticTiledMapTile(region);
                        staticTiledMapTile2.setId(tileid);
                        staticTiledMapTile2.setOffsetX((float) offsetX);
                        if (this.flipY) {
                            f = (float) (-offsetY);
                        } else {
                            f = (float) offsetY;
                        }
                        staticTiledMapTile2.setOffsetY(f);
                        tileset.putTile(tileid, staticTiledMapTile2);
                    }
                }
                if (tile != null) {
                    terrain = tileElement.getAttribute("terrain", null);
                    if (terrain != null) {
                        tile.getProperties().put("terrain", terrain);
                    }
                    probability = tileElement.getAttribute("probability", null);
                    if (probability != null) {
                        tile.getProperties().put("probability", probability);
                    }
                    properties = tileElement.getChildByName("properties");
                    if (properties != null) {
                        loadProperties(tile.getProperties(), properties);
                    }
                }
            }
            Array<Element> tileElements = element.getChildrenByName("tile");
            Array<AnimatedTiledMapTile> animatedTiles = new Array();
            it = tileElements.iterator();
            while (it.hasNext()) {
                tileElement = (Element) it.next();
                tile = tileset.getTile(firstgid + tileElement.getIntAttribute("id", 0));
                if (tile != null) {
                    Element animationElement = tileElement.getChildByName("animation");
                    if (animationElement != null) {
                        Array<StaticTiledMapTile> staticTiles = new Array();
                        IntArray intervals = new IntArray();
                        Iterator it3 = animationElement.getChildrenByName("frame").iterator();
                        while (it3.hasNext()) {
                            Element frameElement = (Element) it3.next();
                            staticTiles.add((StaticTiledMapTile) tileset.getTile(frameElement.getIntAttribute("tileid") + firstgid));
                            intervals.add(frameElement.getIntAttribute("duration"));
                        }
                        AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, (Array) staticTiles);
                        animatedTile.setId(tile.getId());
                        animatedTiles.add(animatedTile);
                        tile = animatedTile;
                    }
                    terrain = tileElement.getAttribute("terrain", null);
                    if (terrain != null) {
                        tile.getProperties().put("terrain", terrain);
                    }
                    probability = tileElement.getAttribute("probability", null);
                    if (probability != null) {
                        tile.getProperties().put("probability", probability);
                    }
                    properties = tileElement.getChildByName("properties");
                    if (properties != null) {
                        loadProperties(tile.getProperties(), properties);
                    }
                }
            }
            it2 = animatedTiles.iterator();
            while (it2.hasNext()) {
                AnimatedTiledMapTile tile2 = (AnimatedTiledMapTile) it2.next();
                tileset.putTile(tile2.getId(), tile2);
            }
            properties = element.getChildByName("properties");
            if (properties != null) {
                loadProperties(tileset.getProperties(), properties);
            }
            map.getTileSets().addTileSet(tileset);
        }
    }
}
