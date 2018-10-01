package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class TiledMapTileSets implements Iterable<TiledMapTileSet> {
    private Array<TiledMapTileSet> tilesets = new Array();

    public TiledMapTileSet getTileSet(int index) {
        return (TiledMapTileSet) this.tilesets.get(index);
    }

    public TiledMapTileSet getTileSet(String name) {
        Iterator it = this.tilesets.iterator();
        while (it.hasNext()) {
            TiledMapTileSet tileset = (TiledMapTileSet) it.next();
            if (name.equals(tileset.getName())) {
                return tileset;
            }
        }
        return null;
    }

    public void addTileSet(TiledMapTileSet tileset) {
        this.tilesets.add(tileset);
    }

    public void removeTileSet(int index) {
        this.tilesets.removeIndex(index);
    }

    public void removeTileSet(TiledMapTileSet tileset) {
        this.tilesets.removeValue(tileset, true);
    }

    public TiledMapTile getTile(int id) {
        for (int i = this.tilesets.size - 1; i >= 0; i--) {
            TiledMapTile tile = ((TiledMapTileSet) this.tilesets.get(i)).getTile(id);
            if (tile != null) {
                return tile;
            }
        }
        return null;
    }

    public Iterator<TiledMapTileSet> iterator() {
        return this.tilesets.iterator();
    }
}
