package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.utils.Disposable;
import java.nio.ShortBuffer;

public interface IndexData extends Disposable {
    void bind();

    void dispose();

    ShortBuffer getBuffer();

    int getNumIndices();

    int getNumMaxIndices();

    void invalidate();

    void setIndices(ShortBuffer shortBuffer);

    void setIndices(short[] sArr, int i, int i2);

    void unbind();
}
