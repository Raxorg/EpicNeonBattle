package com.badlogic.gdx.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import java.nio.ByteBuffer;

public final class ScreenUtils {
    public static TextureRegion getFrameBufferTexture() {
        return getFrameBufferTexture(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public static TextureRegion getFrameBufferTexture(int x, int y, int w, int h) {
        int potW = MathUtils.nextPowerOfTwo(w);
        int potH = MathUtils.nextPowerOfTwo(h);
        Pixmap pixmap = getFrameBufferPixmap(x, y, w, h);
        Pixmap potPixmap = new Pixmap(potW, potH, Format.RGBA8888);
        potPixmap.drawPixmap(pixmap, 0, 0);
        TextureRegion textureRegion = new TextureRegion(new Texture(potPixmap), 0, h, w, -h);
        potPixmap.dispose();
        pixmap.dispose();
        return textureRegion;
    }

    public static Pixmap getFrameBufferPixmap(int x, int y, int w, int h) {
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        Pixmap pixmap = new Pixmap(w, h, Format.RGBA8888);
        Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixmap.getPixels());
        return pixmap;
    }

    public static byte[] getFrameBufferPixels(boolean flipY) {
        return getFrameBufferPixels(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), flipY);
    }

    public static byte[] getFrameBufferPixels(int x, int y, int w, int h, boolean flipY) {
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
        ByteBuffer pixels = BufferUtils.newByteBuffer((w * h) * 4);
        Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixels);
        byte[] lines = new byte[((w * h) * 4)];
        if (flipY) {
            int numBytesPerLine = w * 4;
            for (int i = 0; i < h; i++) {
                pixels.position(((h - i) - 1) * numBytesPerLine);
                pixels.get(lines, i * numBytesPerLine, numBytesPerLine);
            }
        } else {
            pixels.clear();
            pixels.get(lines);
        }
        return lines;
    }
}
