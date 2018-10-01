package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class IndexBufferObjectSubData implements IndexData {
    ShortBuffer buffer;
    int bufferHandle;
    ByteBuffer byteBuffer;
    boolean isBound = false;
    final boolean isDirect;
    boolean isDirty = true;
    final int usage;

    public IndexBufferObjectSubData(boolean isStatic, int maxIndices) {
        this.byteBuffer = BufferUtils.newByteBuffer(maxIndices * 2);
        this.isDirect = true;
        this.usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
        this.buffer = this.byteBuffer.asShortBuffer();
        this.buffer.flip();
        this.byteBuffer.flip();
        this.bufferHandle = createBufferObject();
    }

    public IndexBufferObjectSubData(int maxIndices) {
        this.byteBuffer = BufferUtils.newByteBuffer(maxIndices * 2);
        this.isDirect = true;
        this.usage = GL20.GL_STATIC_DRAW;
        this.buffer = this.byteBuffer.asShortBuffer();
        this.buffer.flip();
        this.byteBuffer.flip();
        this.bufferHandle = createBufferObject();
    }

    private int createBufferObject() {
        int result = Gdx.gl20.glGenBuffer();
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, result);
        Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, this.byteBuffer.capacity(), null, this.usage);
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        return result;
    }

    public int getNumIndices() {
        return this.buffer.limit();
    }

    public int getNumMaxIndices() {
        return this.buffer.capacity();
    }

    public void setIndices(short[] indices, int offset, int count) {
        this.isDirty = true;
        this.buffer.clear();
        this.buffer.put(indices, offset, count);
        this.buffer.flip();
        this.byteBuffer.position(0);
        this.byteBuffer.limit(count << 1);
        if (this.isBound) {
            Gdx.gl20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, this.byteBuffer.limit(), this.byteBuffer);
            this.isDirty = false;
        }
    }

    public void setIndices(ShortBuffer indices) {
        int pos = indices.position();
        this.isDirty = true;
        this.buffer.clear();
        this.buffer.put(indices);
        this.buffer.flip();
        indices.position(pos);
        this.byteBuffer.position(0);
        this.byteBuffer.limit(this.buffer.limit() << 1);
        if (this.isBound) {
            Gdx.gl20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, this.byteBuffer.limit(), this.byteBuffer);
            this.isDirty = false;
        }
    }

    public ShortBuffer getBuffer() {
        this.isDirty = true;
        return this.buffer;
    }

    public void bind() {
        if (this.bufferHandle == 0) {
            throw new GdxRuntimeException("buuh");
        }
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, this.bufferHandle);
        if (this.isDirty) {
            this.byteBuffer.limit(this.buffer.limit() * 2);
            Gdx.gl20.glBufferSubData(GL20.GL_ELEMENT_ARRAY_BUFFER, 0, this.byteBuffer.limit(), this.byteBuffer);
            this.isDirty = false;
        }
        this.isBound = true;
    }

    public void unbind() {
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        this.isBound = false;
    }

    public void invalidate() {
        this.bufferHandle = createBufferObject();
        this.isDirty = true;
    }

    public void dispose() {
        GL20 gl = Gdx.gl20;
        gl.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(this.bufferHandle);
        this.bufferHandle = 0;
    }
}
