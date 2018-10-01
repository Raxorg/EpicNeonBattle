package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Disposable;
import java.nio.FloatBuffer;

public interface VertexData extends Disposable {
    void bind(ShaderProgram shaderProgram);

    void bind(ShaderProgram shaderProgram, int[] iArr);

    void dispose();

    VertexAttributes getAttributes();

    FloatBuffer getBuffer();

    int getNumMaxVertices();

    int getNumVertices();

    void invalidate();

    void setVertices(float[] fArr, int i, int i2);

    void unbind(ShaderProgram shaderProgram);

    void unbind(ShaderProgram shaderProgram, int[] iArr);

    void updateVertices(int i, float[] fArr, int i2, int i3);
}
