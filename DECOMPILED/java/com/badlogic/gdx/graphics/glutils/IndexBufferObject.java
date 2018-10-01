package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class IndexBufferObject implements IndexData {
    ShortBuffer buffer;
    int bufferHandle;
    ByteBuffer byteBuffer;
    private final boolean empty;
    boolean isBound;
    final boolean isDirect;
    boolean isDirty;
    final int usage;

    public IndexBufferObject(int maxIndices) {
        this(true, maxIndices);
    }

    public IndexBufferObject(boolean isStatic, int maxIndices) {
        boolean z = false;
        this.isDirty = true;
        this.isBound = false;
        if (maxIndices == 0) {
            z = true;
        }
        this.empty = z;
        if (this.empty) {
            maxIndices = 1;
        }
        this.byteBuffer = BufferUtils.newUnsafeByteBuffer(maxIndices * 2);
        this.isDirect = true;
        this.buffer = this.byteBuffer.asShortBuffer();
        this.buffer.flip();
        this.byteBuffer.flip();
        this.bufferHandle = Gdx.gl20.glGenBuffer();
        this.usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    public int getNumIndices() {
        return this.empty ? 0 : this.buffer.limit();
    }

    public int getNumMaxIndices() {
        return this.empty ? 0 : this.buffer.capacity();
    }

    public void setIndices(short[] indices, int offset, int count) {
        this.isDirty = true;
        this.buffer.clear();
        this.buffer.put(indices, offset, count);
        this.buffer.flip();
        this.byteBuffer.position(0);
        this.byteBuffer.limit(count << 1);
        if (this.isBound) {
            Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
    }

    public void setIndices(ShortBuffer indices) {
        this.isDirty = true;
        int pos = indices.position();
        this.buffer.clear();
        this.buffer.put(indices);
        this.buffer.flip();
        indices.position(pos);
        this.byteBuffer.position(0);
        this.byteBuffer.limit(this.buffer.limit() << 1);
        if (this.isBound) {
            Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
    }

    public ShortBuffer getBuffer() {
        this.isDirty = true;
        return this.buffer;
    }

    public void bind() {
        if (this.bufferHandle == 0) {
            throw new GdxRuntimeException("No buffer allocated!");
        }
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, this.bufferHandle);
        if (this.isDirty) {
            this.byteBuffer.limit(this.buffer.limit() * 2);
            Gdx.gl20.glBufferData(GL20.GL_ELEMENT_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
        this.isBound = true;
    }

    public void unbind() {
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        this.isBound = false;
    }

    public void invalidate() {
        this.bufferHandle = Gdx.gl20.glGenBuffer();
        this.isDirty = true;
    }

    public void dispose() {
        Gdx.gl20.glBindBuffer(GL20.GL_ELEMENT_ARRAY_BUFFER, 0);
        Gdx.gl20.glDeleteBuffer(this.bufferHandle);
        this.bufferHandle = 0;
        BufferUtils.disposeUnsafeByteBuffer(this.byteBuffer);
    }
}
