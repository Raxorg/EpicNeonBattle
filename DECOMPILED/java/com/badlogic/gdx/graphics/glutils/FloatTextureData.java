package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.FloatBuffer;

public class FloatTextureData implements TextureData {
    FloatBuffer buffer;
    int height = 0;
    boolean isPrepared = false;
    int width = 0;

    public FloatTextureData(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public TextureDataType getType() {
        return TextureDataType.Custom;
    }

    public boolean isPrepared() {
        return this.isPrepared;
    }

    public void prepare() {
        if (this.isPrepared) {
            throw new GdxRuntimeException("Already prepared");
        }
        this.buffer = BufferUtils.newFloatBuffer((this.width * this.height) * 4);
        this.isPrepared = true;
    }

    public void consumeCustomData(int target) {
        if (!Gdx.graphics.supportsExtension("texture_float")) {
            throw new GdxRuntimeException("Extension OES_TEXTURE_FLOAT not supported!");
        } else if (Gdx.app.getType() == ApplicationType.Android || Gdx.app.getType() == ApplicationType.iOS || Gdx.app.getType() == ApplicationType.WebGL) {
            Gdx.gl.glTexImage2D(target, 0, GL20.GL_RGBA, this.width, this.height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, this.buffer);
        } else {
            Gdx.gl.glTexImage2D(target, 0, GL30.GL_RGBA32F, this.width, this.height, 0, GL20.GL_RGBA, GL20.GL_FLOAT, this.buffer);
        }
    }

    public Pixmap consumePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    public boolean disposePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public Format getFormat() {
        return Format.RGBA8888;
    }

    public boolean useMipMaps() {
        return false;
    }

    public boolean isManaged() {
        return true;
    }
}
