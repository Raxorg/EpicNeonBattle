package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class VertexArray implements VertexData {
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    final ByteBuffer byteBuffer;
    boolean isBound;

    public VertexArray(int numVertices, VertexAttribute... attributes) {
        this(numVertices, new VertexAttributes(attributes));
    }

    public VertexArray(int numVertices, VertexAttributes attributes) {
        this.isBound = false;
        this.attributes = attributes;
        this.byteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
        this.buffer = this.byteBuffer.asFloatBuffer();
        this.buffer.flip();
        this.byteBuffer.flip();
    }

    public void dispose() {
        BufferUtils.disposeUnsafeByteBuffer(this.byteBuffer);
    }

    public FloatBuffer getBuffer() {
        return this.buffer;
    }

    public int getNumVertices() {
        return (this.buffer.limit() * 4) / this.attributes.vertexSize;
    }

    public int getNumMaxVertices() {
        return this.byteBuffer.capacity() / this.attributes.vertexSize;
    }

    public void setVertices(float[] vertices, int offset, int count) {
        BufferUtils.copy(vertices, this.byteBuffer, count, offset);
        this.buffer.position(0);
        this.buffer.limit(count);
    }

    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
        int pos = this.byteBuffer.position();
        this.byteBuffer.position(targetOffset * 4);
        BufferUtils.copy(vertices, sourceOffset, count, this.byteBuffer);
        this.byteBuffer.position(pos);
    }

    public void bind(ShaderProgram shader) {
        bind(shader, null);
    }

    public void bind(ShaderProgram shader, int[] locations) {
        int numAttributes = this.attributes.size();
        this.byteBuffer.limit(this.buffer.limit() * 4);
        int i;
        VertexAttribute attribute;
        int location;
        if (locations == null) {
            for (i = 0; i < numAttributes; i++) {
                attribute = this.attributes.get(i);
                location = shader.getAttributeLocation(attribute.alias);
                if (location >= 0) {
                    shader.enableVertexAttribute(location);
                    if (attribute.type == GL20.GL_FLOAT) {
                        this.buffer.position(attribute.offset / 4);
                        shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, this.buffer);
                    } else {
                        this.byteBuffer.position(attribute.offset);
                        shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, this.byteBuffer);
                    }
                }
            }
        } else {
            for (i = 0; i < numAttributes; i++) {
                attribute = this.attributes.get(i);
                location = locations[i];
                if (location >= 0) {
                    shader.enableVertexAttribute(location);
                    if (attribute.type == GL20.GL_FLOAT) {
                        this.buffer.position(attribute.offset / 4);
                        shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, this.buffer);
                    } else {
                        this.byteBuffer.position(attribute.offset);
                        shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, this.byteBuffer);
                    }
                }
            }
        }
        this.isBound = true;
    }

    public void unbind(ShaderProgram shader) {
        unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        int numAttributes = this.attributes.size();
        int i;
        if (locations == null) {
            for (i = 0; i < numAttributes; i++) {
                shader.disableVertexAttribute(this.attributes.get(i).alias);
            }
        } else {
            for (i = 0; i < numAttributes; i++) {
                int location = locations[i];
                if (location >= 0) {
                    shader.disableVertexAttribute(location);
                }
            }
        }
        this.isBound = false;
    }

    public VertexAttributes getAttributes() {
        return this.attributes;
    }

    public void invalidate() {
    }
}
