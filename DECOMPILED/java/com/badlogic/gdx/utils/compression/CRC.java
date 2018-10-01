package com.badlogic.gdx.utils.compression;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;

public class CRC {
    public static int[] Table = new int[Usage.BiNormal];
    int _value = -1;

    static {
        for (int i = 0; i < Usage.BiNormal; i++) {
            int r = i;
            for (int j = 0; j < 8; j++) {
                if ((r & 1) != 0) {
                    r = (r >>> 1) ^ -306674912;
                } else {
                    r >>>= 1;
                }
            }
            Table[i] = r;
        }
    }

    public void Init() {
        this._value = -1;
    }

    public void Update(byte[] data, int offset, int size) {
        for (int i = 0; i < size; i++) {
            this._value = Table[(this._value ^ data[offset + i]) & Keys.F12] ^ (this._value >>> 8);
        }
    }

    public void Update(byte[] data) {
        for (byte b : data) {
            this._value = Table[(this._value ^ b) & Keys.F12] ^ (this._value >>> 8);
        }
    }

    public void UpdateByte(int b) {
        this._value = Table[(this._value ^ b) & Keys.F12] ^ (this._value >>> 8);
    }

    public int GetDigest() {
        return this._value ^ -1;
    }
}
