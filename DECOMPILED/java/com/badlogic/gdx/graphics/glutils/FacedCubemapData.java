package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Blending;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.Factory;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FacedCubemapData implements CubemapData {
    protected final TextureData[] data;

    public FacedCubemapData() {
        this((TextureData) null, (TextureData) null, (TextureData) null, (TextureData) null, (TextureData) null, (TextureData) null);
    }

    public FacedCubemapData(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ) {
        this(Factory.loadFromFile(positiveX, false), Factory.loadFromFile(negativeX, false), Factory.loadFromFile(positiveY, false), Factory.loadFromFile(negativeY, false), Factory.loadFromFile(positiveZ, false), Factory.loadFromFile(negativeZ, false));
    }

    public FacedCubemapData(FileHandle positiveX, FileHandle negativeX, FileHandle positiveY, FileHandle negativeY, FileHandle positiveZ, FileHandle negativeZ, boolean useMipMaps) {
        this(Factory.loadFromFile(positiveX, useMipMaps), Factory.loadFromFile(negativeX, useMipMaps), Factory.loadFromFile(positiveY, useMipMaps), Factory.loadFromFile(negativeY, useMipMaps), Factory.loadFromFile(positiveZ, useMipMaps), Factory.loadFromFile(negativeZ, useMipMaps));
    }

    public FacedCubemapData(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ) {
        this(positiveX, negativeX, positiveY, negativeY, positiveZ, negativeZ, false);
    }

    public FacedCubemapData(Pixmap positiveX, Pixmap negativeX, Pixmap positiveY, Pixmap negativeY, Pixmap positiveZ, Pixmap negativeZ, boolean useMipMaps) {
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

    public FacedCubemapData(int width, int height, int depth, Format format) {
        this(new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(depth, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, depth, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true), new PixmapTextureData(new Pixmap(width, height, format), null, false, true));
    }

    public FacedCubemapData(TextureData positiveX, TextureData negativeX, TextureData positiveY, TextureData negativeY, TextureData positiveZ, TextureData negativeZ) {
        this.data = new TextureData[6];
        this.data[0] = positiveX;
        this.data[1] = negativeX;
        this.data[2] = positiveY;
        this.data[3] = negativeY;
        this.data[4] = positiveZ;
        this.data[5] = negativeZ;
    }

    public boolean isManaged() {
        for (TextureData data : this.data) {
            if (!data.isManaged()) {
                return false;
            }
        }
        return true;
    }

    public void load(CubemapSide side, FileHandle file) {
        this.data[side.index] = Factory.loadFromFile(file, false);
    }

    public void load(CubemapSide side, Pixmap pixmap) {
        Format format = null;
        TextureData[] textureDataArr = this.data;
        int i = side.index;
        if (pixmap != null) {
            Object pixmapTextureData = new PixmapTextureData(pixmap, null, false, false);
        }
        textureDataArr[i] = format;
    }

    public boolean isComplete() {
        for (TextureData textureData : this.data) {
            if (textureData == null) {
                return false;
            }
        }
        return true;
    }

    public TextureData getTextureData(CubemapSide side) {
        return this.data[side.index];
    }

    public int getWidth() {
        int tmp;
        int width = 0;
        if (this.data[CubemapSide.PositiveZ.index] != null) {
            tmp = this.data[CubemapSide.PositiveZ.index].getWidth();
            if (tmp > 0) {
                width = tmp;
            }
        }
        if (this.data[CubemapSide.NegativeZ.index] != null) {
            tmp = this.data[CubemapSide.NegativeZ.index].getWidth();
            if (tmp > width) {
                width = tmp;
            }
        }
        if (this.data[CubemapSide.PositiveY.index] != null) {
            tmp = this.data[CubemapSide.PositiveY.index].getWidth();
            if (tmp > width) {
                width = tmp;
            }
        }
        if (this.data[CubemapSide.NegativeY.index] == null) {
            return width;
        }
        tmp = this.data[CubemapSide.NegativeY.index].getWidth();
        if (tmp > width) {
            return tmp;
        }
        return width;
    }

    public int getHeight() {
        int tmp;
        int height = 0;
        if (this.data[CubemapSide.PositiveZ.index] != null) {
            tmp = this.data[CubemapSide.PositiveZ.index].getHeight();
            if (tmp > 0) {
                height = tmp;
            }
        }
        if (this.data[CubemapSide.NegativeZ.index] != null) {
            tmp = this.data[CubemapSide.NegativeZ.index].getHeight();
            if (tmp > height) {
                height = tmp;
            }
        }
        if (this.data[CubemapSide.PositiveX.index] != null) {
            tmp = this.data[CubemapSide.PositiveX.index].getHeight();
            if (tmp > height) {
                height = tmp;
            }
        }
        if (this.data[CubemapSide.NegativeX.index] == null) {
            return height;
        }
        tmp = this.data[CubemapSide.NegativeX.index].getHeight();
        if (tmp > height) {
            return tmp;
        }
        return height;
    }

    public boolean isPrepared() {
        return false;
    }

    public void prepare() {
        if (isComplete()) {
            for (int i = 0; i < this.data.length; i++) {
                if (!this.data[i].isPrepared()) {
                    this.data[i].prepare();
                }
            }
            return;
        }
        throw new GdxRuntimeException("You need to complete your cubemap data before using it");
    }

    public void consumeCubemapData() {
        for (int i = 0; i < this.data.length; i++) {
            if (this.data[i].getType() == TextureDataType.Custom) {
                this.data[i].consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i);
            } else {
                Pixmap pixmap = this.data[i].consumePixmap();
                boolean disposePixmap = this.data[i].disposePixmap();
                if (this.data[i].getFormat() != pixmap.getFormat()) {
                    Pixmap tmp = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), this.data[i].getFormat());
                    Blending blend = Pixmap.getBlending();
                    Pixmap.setBlending(Blending.None);
                    tmp.drawPixmap(pixmap, 0, 0, 0, 0, pixmap.getWidth(), pixmap.getHeight());
                    Pixmap.setBlending(blend);
                    if (this.data[i].disposePixmap()) {
                        pixmap.dispose();
                    }
                    pixmap = tmp;
                }
                Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 1);
                Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
            }
        }
    }
}
