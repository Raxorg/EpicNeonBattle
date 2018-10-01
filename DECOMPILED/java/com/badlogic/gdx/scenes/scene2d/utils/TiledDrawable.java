package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class TiledDrawable extends TextureRegionDrawable {
    public TiledDrawable(TextureRegion region) {
        super(region);
    }

    public TiledDrawable(TextureRegionDrawable drawable) {
        super(drawable);
    }

    public void draw(Batch batch, float x, float y, float width, float height) {
        int i;
        int ii;
        float u2;
        float v;
        TextureRegion region = getRegion();
        float regionWidth = (float) region.getRegionWidth();
        float regionHeight = (float) region.getRegionHeight();
        int fullX = (int) (width / regionWidth);
        int fullY = (int) (height / regionHeight);
        float remainingX = width - (((float) fullX) * regionWidth);
        float remainingY = height - (((float) fullY) * regionHeight);
        float startX = x;
        float startY = y;
        float endX = (x + width) - remainingX;
        float endY = (y + height) - remainingY;
        for (i = 0; i < fullX; i++) {
            y = startY;
            for (ii = 0; ii < fullY; ii++) {
                batch.draw(region, x, y, regionWidth, regionHeight);
                y += regionHeight;
            }
            x += regionWidth;
        }
        Texture texture = region.getTexture();
        float u = region.getU();
        float v2 = region.getV2();
        if (remainingX > 0.0f) {
            u2 = u + (remainingX / ((float) texture.getWidth()));
            v = region.getV();
            y = startY;
            for (ii = 0; ii < fullY; ii++) {
                batch.draw(texture, x, y, remainingX, regionHeight, u, v2, u2, v);
                y += regionHeight;
            }
            if (remainingY > 0.0f) {
                batch.draw(texture, x, y, remainingX, remainingY, u, v2, u2, v2 - (remainingY / ((float) texture.getHeight())));
            }
        }
        if (remainingY > 0.0f) {
            u2 = region.getU2();
            v = v2 - (remainingY / ((float) texture.getHeight()));
            x = startX;
            for (i = 0; i < fullX; i++) {
                batch.draw(texture, x, y, regionWidth, remainingY, u, v2, u2, v);
                x += regionWidth;
            }
        }
    }
}
