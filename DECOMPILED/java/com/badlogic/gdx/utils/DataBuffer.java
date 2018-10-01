package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.StreamUtils.OptimizedByteArrayOutputStream;

public class DataBuffer extends DataOutput {
    private final OptimizedByteArrayOutputStream outStream;

    public DataBuffer() {
        this(32);
    }

    public DataBuffer(int initialSize) {
        super(new OptimizedByteArrayOutputStream(initialSize));
        this.outStream = (OptimizedByteArrayOutputStream) this.out;
    }

    public byte[] getBuffer() {
        return this.outStream.getBuffer();
    }

    public byte[] toArray() {
        return this.outStream.toByteArray();
    }
}
