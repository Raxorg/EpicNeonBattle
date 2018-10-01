package com.epicness.placeables;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public abstract class Special extends Placeable {
    protected Special(TextureRegion regionNormal, TextureRegion regionGlow, int cubeCost, int techCost) {
        super(regionNormal, regionGlow, cubeCost, techCost);
    }
}
