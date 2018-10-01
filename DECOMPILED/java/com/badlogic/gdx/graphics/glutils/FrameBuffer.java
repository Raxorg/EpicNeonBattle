package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class FrameBuffer extends GLFrameBuffer<Texture> {
    public FrameBuffer(Format format, int width, int height, boolean hasDepth) {
        this(format, width, height, hasDepth, false);
    }

    public FrameBuffer(Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        super(format, width, height, hasDepth, hasStencil);
    }

    protected Texture createColorTexture() {
        Texture result = new Texture(this.width, this.height, this.format);
        result.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        result.setWrap(TextureWrap.ClampToEdge, TextureWrap.ClampToEdge);
        return result;
    }

    protected void disposeColorTexture(Texture colorTexture) {
        colorTexture.dispose();
    }

    public static void unbind() {
        GLFrameBuffer.unbind();
    }
}
