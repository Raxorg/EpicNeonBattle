package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class VertexBufferObjectSubData implements VertexData {
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    int bufferHandle;
    final ByteBuffer byteBuffer;
    boolean isBound = false;
    final boolean isDirect;
    boolean isDirty = false;
    final boolean isStatic;
    final int usage;

    public VertexBufferObjectSubData(boolean isStatic, int numVertices, VertexAttribute... attributes) {
        this.isStatic = isStatic;
        this.attributes = new VertexAttributes(attributes);
        this.byteBuffer = BufferUtils.newByteBuffer(this.attributes.vertexSize * numVertices);
        this.isDirect = true;
        this.usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
        this.buffer = this.byteBuffer.asFloatBuffer();
        this.bufferHandle = createBufferObject();
        this.buffer.flip();
        this.byteBuffer.flip();
    }

    private int createBufferObject() {
        int result = Gdx.gl20.glGenBuffer();
        Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, result);
        Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, this.byteBuffer.capacity(), null, this.usage);
        Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        return result;
    }

    public VertexAttributes getAttributes() {
        return this.attributes;
    }

    public int getNumVertices() {
        return (this.buffer.limit() * 4) / this.attributes.vertexSize;
    }

    public int getNumMaxVertices() {
        return this.byteBuffer.capacity() / this.attributes.vertexSize;
    }

    public FloatBuffer getBuffer() {
        this.isDirty = true;
        return this.buffer;
    }

    private void bufferChanged() {
        if (this.isBound) {
            Gdx.gl20.glBufferSubData(GL20.GL_ARRAY_BUFFER, 0, this.byteBuffer.limit(), this.byteBuffer);
            this.isDirty = false;
        }
    }

    public void setVertices(float[] vertices, int offset, int count) {
        this.isDirty = true;
        if (this.isDirect) {
            BufferUtils.copy(vertices, this.byteBuffer, count, offset);
            this.buffer.position(0);
            this.buffer.limit(count);
        } else {
            this.buffer.clear();
            this.buffer.put(vertices, offset, count);
            this.buffer.flip();
            this.byteBuffer.position(0);
            this.byteBuffer.limit(this.buffer.limit() << 2);
        }
        bufferChanged();
    }

    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
        this.isDirty = true;
        if (this.isDirect) {
            int pos = this.byteBuffer.position();
            this.byteBuffer.position(targetOffset * 4);
            BufferUtils.copy(vertices, sourceOffset, count, this.byteBuffer);
            this.byteBuffer.position(pos);
            bufferChanged();
            return;
        }
        throw new GdxRuntimeException("Buffer must be allocated direct.");
    }

    public void bind(ShaderProgram shader) {
        bind(shader, null);
    }

    public void bind(ShaderProgram shader, int[] locations) {
        GL20 gl = Gdx.gl20;
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, this.bufferHandle);
        if (this.isDirty) {
            this.byteBuffer.limit(this.buffer.limit() * 4);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
        int numAttributes = this.attributes.size();
        int i;
        VertexAttribute attribute;
        int location;
        if (locations == null) {
            for (i = 0; i < numAttributes; i++) {
                attribute = this.attributes.get(i);
                location = shader.getAttributeLocation(attribute.alias);
                if (location >= 0) {
                    shader.enableVertexAttribute(location);
                    shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, attribute.offset);
                }
            }
        } else {
            for (i = 0; i < numAttributes; i++) {
                attribute = this.attributes.get(i);
                location = locations[i];
                if (location >= 0) {
                    shader.enableVertexAttribute(location);
                    shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, attribute.offset);
                }
            }
        }
        this.isBound = true;
    }

    public void unbind(ShaderProgram shader) {
        unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        GL20 gl = Gdx.gl20;
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
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        this.isBound = false;
    }

    public void invalidate() {
        this.bufferHandle = createBufferObject();
        this.isDirty = true;
    }

    public void dispose() {
        GL20 gl = Gdx.gl20;
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(this.bufferHandle);
        this.bufferHandle = 0;
    }

    public int getBufferHandle() {
        return this.bufferHandle;
    }
}
