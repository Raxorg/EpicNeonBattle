package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.Cubemap.CubemapSide;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class FrameBufferCubemap extends GLFrameBuffer<Cubemap> {
    private int currentSide;

    public FrameBufferCubemap(Format format, int width, int height, boolean hasDepth) {
        this(format, width, height, hasDepth, false);
    }

    public FrameBufferCubemap(Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        super(format, width, height, hasDepth, hasStencil);
    }

    protected Cubemap createColorTexture() {
        Cubemap result = new Cubemap(this.width, this.height, this.width, this.format);
        result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        return result;
    }

    protected void disposeColorTexture(Cubemap colorTexture) {
        colorTexture.dispose();
    }

    public void bind() {
        this.currentSide = -1;
        super.bind();
    }

    public boolean nextSide() {
        if (this.currentSide > 5) {
            throw new GdxRuntimeException("No remaining sides.");
        } else if (this.currentSide == 5) {
            return false;
        } else {
            this.currentSide++;
            bindSide(getSide());
            return true;
        }
    }

    protected void bindSide(CubemapSide side) {
        Gdx.gl20.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, side.glEnum, ((Cubemap) this.colorTexture).getTextureObjectHandle(), 0);
    }

    public CubemapSide getSide() {
        return this.currentSide < 0 ? null : CubemapSide.values()[this.currentSide];
    }
}
