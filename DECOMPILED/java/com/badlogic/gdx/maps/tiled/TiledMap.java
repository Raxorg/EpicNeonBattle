package com.badlogic.gdx.maps.tiled;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.util.Iterator;

public class TiledMap extends Map {
    private Array<? extends Disposable> ownedResources;
    private TiledMapTileSets tilesets = new TiledMapTileSets();

    public TiledMapTileSets getTileSets() {
        return this.tilesets;
    }

    public void setOwnedResources(Array<? extends Disposable> resources) {
        this.ownedResources = resources;
    }

    public void dispose() {
        if (this.ownedResources != null) {
            Iterator it = this.ownedResources.iterator();
            while (it.hasNext()) {
                ((Disposable) it.next()).dispose();
            }
        }
    }
}
