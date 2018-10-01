package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.ImageResolver.AssetManagerImageResolver;
import com.badlogic.gdx.maps.ImageResolver.DirectImageResolver;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;
import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;

public class TideMapLoader extends SynchronousAssetLoader<TiledMap, Parameters> {
    private Element root;
    private XmlReader xml = new XmlReader();

    public static class Parameters extends AssetLoaderParameters<TiledMap> {
    }

    public TideMapLoader() {
        super(new InternalFileHandleResolver());
    }

    public TideMapLoader(FileHandleResolver resolver) {
        super(resolver);
    }

    public TiledMap load(String fileName) {
        try {
            FileHandle tideFile = resolve(fileName);
            this.root = this.xml.parse(tideFile);
            ObjectMap<String, Texture> textures = new ObjectMap();
            Iterator it = loadTileSheets(this.root, tideFile).iterator();
            while (it.hasNext()) {
                FileHandle textureFile = (FileHandle) it.next();
                textures.put(textureFile.path(), new Texture(textureFile));
            }
            TiledMap map = loadMap(this.root, tideFile, new DirectImageResolver(textures));
            map.setOwnedResources(textures.values().toArray());
            return map;
        } catch (IOException e) {
            throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    public TiledMap load(AssetManager assetManager, String fileName, FileHandle tideFile, Parameters parameter) {
        try {
            return loadMap(this.root, tideFile, new AssetManagerImageResolver(assetManager));
        } catch (Exception e) {
            throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle tmxFile, Parameters parameter) {
        Array<AssetDescriptor> dependencies = new Array();
        try {
            this.root = this.xml.parse(tmxFile);
            Iterator it = loadTileSheets(this.root, tmxFile).iterator();
            while (it.hasNext()) {
                dependencies.add(new AssetDescriptor(((FileHandle) it.next()).path(), Texture.class));
            }
            return dependencies;
        } catch (IOException e) {
            throw new GdxRuntimeException("Couldn't load tilemap '" + fileName + "'", e);
        }
    }

    private TiledMap loadMap(Element root, FileHandle tmxFile, ImageResolver imageResolver) {
        TiledMap map = new TiledMap();
        Element properties = root.getChildByName("Properties");
        if (properties != null) {
            loadProperties(map.getProperties(), properties);
        }
        Iterator it = root.getChildByName("TileSheets").getChildrenByName("TileSheet").iterator();
        while (it.hasNext()) {
            loadTileSheet(map, (Element) it.next(), tmxFile, imageResolver);
        }
        it = root.getChildByName("Layers").getChildrenByName("Layer").iterator();
        while (it.hasNext()) {
            loadLayer(map, (Element) it.next());
        }
        return map;
    }

    private Array<FileHandle> loadTileSheets(Element root, FileHandle tideFile) throws IOException {
        Array<FileHandle> images = new Array();
        Iterator it = root.getChildByName("TileSheets").getChildrenByName("TileSheet").iterator();
        while (it.hasNext()) {
            images.add(getRelativeFileHandle(tideFile, ((Element) it.next()).getChildByName("ImageSource").getText()));
        }
        return images;
    }

    private void loadTileSheet(TiledMap map, Element element, FileHandle tideFile, ImageResolver imageResolver) {
        if (element.getName().equals("TileSheet")) {
            String id = element.getAttribute("Id");
            String description = element.getChildByName("Description").getText();
            String imageSource = element.getChildByName("ImageSource").getText();
            Element alignment = element.getChildByName("Alignment");
            String sheetSize = alignment.getAttribute("SheetSize");
            String tileSize = alignment.getAttribute("TileSize");
            String margin = alignment.getAttribute("Margin");
            String spacing = alignment.getAttribute("Spacing");
            String[] sheetSizeParts = sheetSize.split(" x ");
            int sheetSizeX = Integer.parseInt(sheetSizeParts[0]);
            int sheetSizeY = Integer.parseInt(sheetSizeParts[1]);
            String[] tileSizeParts = tileSize.split(" x ");
            int tileSizeX = Integer.parseInt(tileSizeParts[0]);
            int tileSizeY = Integer.parseInt(tileSizeParts[1]);
            String[] marginParts = margin.split(" x ");
            int marginX = Integer.parseInt(marginParts[0]);
            int marginY = Integer.parseInt(marginParts[1]);
            String[] spacingParts = margin.split(" x ");
            int spacingX = Integer.parseInt(spacingParts[0]);
            int spacingY = Integer.parseInt(spacingParts[1]);
            TextureRegion texture = imageResolver.getImage(getRelativeFileHandle(tideFile, imageSource).path());
            TiledMapTileSets tilesets = map.getTileSets();
            int firstgid = 1;
            Iterator it = tilesets.iterator();
            while (it.hasNext()) {
                firstgid += ((TiledMapTileSet) it.next()).size();
            }
            TiledMapTileSet tileset = new TiledMapTileSet();
            tileset.setName(id);
            tileset.getProperties().put("firstgid", Integer.valueOf(firstgid));
            int gid = firstgid;
            int stopWidth = texture.getRegionWidth() - tileSizeX;
            int stopHeight = texture.getRegionHeight() - tileSizeY;
            int y = marginY;
            while (y <= stopHeight) {
                int x = marginX;
                int gid2 = gid;
                while (x <= stopWidth) {
                    TiledMapTile staticTiledMapTile = new StaticTiledMapTile(new TextureRegion(texture, x, y, tileSizeX, tileSizeY));
                    staticTiledMapTile.setId(gid2);
                    gid = gid2 + 1;
                    tileset.putTile(gid2, staticTiledMapTile);
                    x += tileSizeX + spacingX;
                    gid2 = gid;
                }
                y += tileSizeY + spacingY;
                gid = gid2;
            }
            Element properties = element.getChildByName("Properties");
            if (properties != null) {
                loadProperties(tileset.getProperties(), properties);
            }
            tilesets.addTileSet(tileset);
        }
    }

    private void loadLayer(TiledMap map, Element element) {
        if (element.getName().equals("Layer")) {
            String id = element.getAttribute("Id");
            String visible = element.getAttribute("Visible");
            Element dimensions = element.getChildByName("Dimensions");
            String layerSize = dimensions.getAttribute("LayerSize");
            String tileSize = dimensions.getAttribute("TileSize");
            String[] layerSizeParts = layerSize.split(" x ");
            int layerSizeX = Integer.parseInt(layerSizeParts[0]);
            int layerSizeY = Integer.parseInt(layerSizeParts[1]);
            String[] tileSizeParts = tileSize.split(" x ");
            MapLayer tiledMapTileLayer = new TiledMapTileLayer(layerSizeX, layerSizeY, Integer.parseInt(tileSizeParts[0]), Integer.parseInt(tileSizeParts[1]));
            tiledMapTileLayer.setName(id);
            tiledMapTileLayer.setVisible(visible.equalsIgnoreCase("True"));
            Array<Element> rows = element.getChildByName("TileArray").getChildrenByName("Row");
            TiledMapTileSets tilesets = map.getTileSets();
            TiledMapTileSet currentTileSet = null;
            int firstgid = 0;
            int rowCount = rows.size;
            for (int row = 0; row < rowCount; row++) {
                Element currentRow = (Element) rows.get(row);
                int y = (rowCount - 1) - row;
                int child = 0;
                int childCount = currentRow.getChildCount();
                int x = 0;
                while (child < childCount) {
                    int x2;
                    Element currentChild = currentRow.getChild(child);
                    String name = currentChild.getName();
                    if (name.equals("TileSheet")) {
                        currentTileSet = tilesets.getTileSet(currentChild.getAttribute("Ref"));
                        firstgid = ((Integer) currentTileSet.getProperties().get("firstgid", Integer.class)).intValue();
                        x2 = x;
                    } else if (name.equals("Null")) {
                        x2 = x + currentChild.getIntAttribute("Count");
                    } else if (name.equals("Static")) {
                        cell = new Cell();
                        cell.setTile(currentTileSet.getTile(currentChild.getIntAttribute("Index") + firstgid));
                        x2 = x + 1;
                        tiledMapTileLayer.setCell(x, y, cell);
                    } else if (name.equals("Animated")) {
                        int interval = currentChild.getInt("Interval");
                        Element frames = currentChild.getChildByName("Frames");
                        Array<StaticTiledMapTile> frameTiles = new Array();
                        int frameChildCount = frames.getChildCount();
                        for (int frameChild = 0; frameChild < frameChildCount; frameChild++) {
                            Element frame = frames.getChild(frameChild);
                            String frameName = frame.getName();
                            if (frameName.equals("TileSheet")) {
                                currentTileSet = tilesets.getTileSet(frame.getAttribute("Ref"));
                                firstgid = ((Integer) currentTileSet.getProperties().get("firstgid", Integer.class)).intValue();
                            } else if (frameName.equals("Static")) {
                                frameTiles.add((StaticTiledMapTile) currentTileSet.getTile(frame.getIntAttribute("Index") + firstgid));
                            }
                        }
                        cell = new Cell();
                        cell.setTile(new AnimatedTiledMapTile(((float) interval) / 1000.0f, (Array) frameTiles));
                        x2 = x + 1;
                        tiledMapTileLayer.setCell(x, y, cell);
                    } else {
                        x2 = x;
                    }
                    child++;
                    x = x2;
                }
            }
            Element properties = element.getChildByName("Properties");
            if (properties != null) {
                loadProperties(tiledMapTileLayer.getProperties(), properties);
            }
            map.getLayers().add(tiledMapTileLayer);
        }
    }

    private void loadProperties(MapProperties properties, Element element) {
        if (element.getName().equals("Properties")) {
            Iterator it = element.getChildrenByName("Property").iterator();
            while (it.hasNext()) {
                Element property = (Element) it.next();
                String key = property.getAttribute("Key", null);
                String type = property.getAttribute("Type", null);
                String value = property.getText();
                if (type.equals("Int32")) {
                    properties.put(key, Integer.valueOf(Integer.parseInt(value)));
                } else if (type.equals("String")) {
                    properties.put(key, value);
                } else if (type.equals("Boolean")) {
                    properties.put(key, Boolean.valueOf(value.equalsIgnoreCase("true")));
                } else {
                    properties.put(key, value);
                }
            }
        }
    }

    private static FileHandle getRelativeFileHandle(FileHandle file, String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, "\\/");
        FileHandle result = file.parent();
        while (tokenizer.hasMoreElements()) {
            String token = tokenizer.nextToken();
            if (token.equals("..")) {
                result = result.parent();
            } else {
                result = result.child(token);
            }
        }
        return result;
    }
}
