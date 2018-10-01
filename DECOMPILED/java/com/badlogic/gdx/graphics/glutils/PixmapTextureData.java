package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class PixmapTextureData implements TextureData {
    final boolean disposePixmap;
    final Format format;
    final boolean managed;
    final Pixmap pixmap;
    final boolean useMipMaps;

    public PixmapTextureData(Pixmap pixmap, Format format, boolean useMipMaps, boolean disposePixmap) {
        this(pixmap, format, useMipMaps, disposePixmap, false);
    }

    public PixmapTextureData(Pixmap pixmap, Format format, boolean useMipMaps, boolean disposePixmap, boolean managed) {
        this.pixmap = pixmap;
        if (format == null) {
            format = pixmap.getFormat();
        }
        this.format = format;
        this.useMipMaps = useMipMaps;
        this.disposePixmap = disposePixmap;
        this.managed = managed;
    }

    public boolean disposePixmap() {
        return this.disposePixmap;
    }

    public Pixmap consumePixmap() {
        return this.pixmap;
    }

    public int getWidth() {
        return this.pixmap.getWidth();
    }

    public int getHeight() {
        return this.pixmap.getHeight();
    }

    public Format getFormat() {
        return this.format;
    }

    public boolean useMipMaps() {
        return this.useMipMaps;
    }

    public boolean isManaged() {
        return this.managed;
    }

    public TextureDataType getType() {
        return TextureDataType.Pixmap;
    }

    public void consumeCustomData(int target) {
        throw new GdxRuntimeException("This TextureData implementation does not upload data itself");
    }

    public boolean isPrepared() {
        return true;
    }

    public void prepare() {
        throw new GdxRuntimeException("prepare() must not be called on a PixmapTextureData instance as it is already prepared.");
    }
}
