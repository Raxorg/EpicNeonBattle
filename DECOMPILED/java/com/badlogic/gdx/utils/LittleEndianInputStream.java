package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LittleEndianInputStream extends FilterInputStream implements DataInput {
    private DataInputStream din;

    public LittleEndianInputStream(InputStream in) {
        super(in);
        this.din = new DataInputStream(in);
    }

    public void readFully(byte[] b) throws IOException {
        this.din.readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        this.din.readFully(b, off, len);
    }

    public int skipBytes(int n) throws IOException {
        return this.din.skipBytes(n);
    }

    public boolean readBoolean() throws IOException {
        return this.din.readBoolean();
    }

    public byte readByte() throws IOException {
        return this.din.readByte();
    }

    public int readUnsignedByte() throws IOException {
        return this.din.readUnsignedByte();
    }

    public short readShort() throws IOException {
        return (short) ((this.din.read() << 8) | (this.din.read() & Keys.F12));
    }

    public int readUnsignedShort() throws IOException {
        return ((this.din.read() & Keys.F12) << 8) | (this.din.read() & Keys.F12);
    }

    public char readChar() throws IOException {
        return this.din.readChar();
    }

    public int readInt() throws IOException {
        int[] res = new int[4];
        for (int i = 3; i >= 0; i--) {
            res[i] = this.din.read();
        }
        return ((((res[0] & Keys.F12) << 24) | ((res[1] & Keys.F12) << 16)) | ((res[2] & Keys.F12) << 8)) | (res[3] & Keys.F12);
    }

    public long readLong() throws IOException {
        int[] res = new int[8];
        for (int i = 7; i >= 0; i--) {
            res[i] = this.din.read();
        }
        return (((((((((long) (res[0] & Keys.F12)) << 56) | (((long) (res[1] & Keys.F12)) << 48)) | (((long) (res[2] & Keys.F12)) << 40)) | (((long) (res[3] & Keys.F12)) << 32)) | (((long) (res[4] & Keys.F12)) << 24)) | (((long) (res[5] & Keys.F12)) << 16)) | (((long) (res[6] & Keys.F12)) << 8)) | ((long) (res[7] & Keys.F12));
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public final String readLine() throws IOException {
        return this.din.readLine();
    }

    public String readUTF() throws IOException {
        return this.din.readUTF();
    }
}
