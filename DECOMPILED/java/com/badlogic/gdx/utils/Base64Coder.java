package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.utils.compression.lzma.Base;

public class Base64Coder {
    public static final CharMap regularMap = new CharMap('+', '/');
    private static final String systemLineSeparator = "\n";
    public static final CharMap urlsafeMap = new CharMap('-', '_');

    public static class CharMap {
        protected final byte[] decodingMap = new byte[Base.kNumFullDistances];
        protected final char[] encodingMap = new char[64];

        public CharMap(char char63, char char64) {
            int i;
            char c = 'A';
            int i2 = 0;
            while (c <= 'Z') {
                i = i2 + 1;
                this.encodingMap[i2] = c;
                c = (char) (c + 1);
                i2 = i;
            }
            c = 'a';
            while (c <= 'z') {
                i = i2 + 1;
                this.encodingMap[i2] = c;
                c = (char) (c + 1);
                i2 = i;
            }
            c = '0';
            while (c <= '9') {
                i = i2 + 1;
                this.encodingMap[i2] = c;
                c = (char) (c + 1);
                i2 = i;
            }
            i = i2 + 1;
            this.encodingMap[i2] = char63;
            i2 = i + 1;
            this.encodingMap[i] = char64;
            for (i = 0; i < this.decodingMap.length; i++) {
                this.decodingMap[i] = (byte) -1;
            }
            for (i = 0; i < 64; i++) {
                this.decodingMap[this.encodingMap[i]] = (byte) i;
            }
        }

        public byte[] getDecodingMap() {
            return this.decodingMap;
        }

        public char[] getEncodingMap() {
            return this.encodingMap;
        }
    }

    public static String encodeString(String s) {
        return encodeString(s, false);
    }

    public static String encodeString(String s, boolean useUrlsafeEncoding) {
        return new String(encode(s.getBytes(), useUrlsafeEncoding ? urlsafeMap.encodingMap : regularMap.encodingMap));
    }

    public static String encodeLines(byte[] in) {
        return encodeLines(in, 0, in.length, 76, systemLineSeparator, regularMap.encodingMap);
    }

    public static String encodeLines(byte[] in, int iOff, int iLen, int lineLen, String lineSeparator, CharMap charMap) {
        return encodeLines(in, iOff, iLen, lineLen, lineSeparator, charMap.encodingMap);
    }

    public static String encodeLines(byte[] in, int iOff, int iLen, int lineLen, String lineSeparator, char[] charMap) {
        int blockLen = (lineLen * 3) / 4;
        if (blockLen <= 0) {
            throw new IllegalArgumentException();
        }
        StringBuilder buf = new StringBuilder((((iLen + 2) / 3) * 4) + (lineSeparator.length() * (((iLen + blockLen) - 1) / blockLen)));
        int ip = 0;
        while (ip < iLen) {
            int l = Math.min(iLen - ip, blockLen);
            buf.append(encode(in, iOff + ip, l, charMap));
            buf.append(lineSeparator);
            ip += l;
        }
        return buf.toString();
    }

    public static char[] encode(byte[] in) {
        return encode(in, regularMap.encodingMap);
    }

    public static char[] encode(byte[] in, CharMap charMap) {
        return encode(in, 0, in.length, charMap);
    }

    public static char[] encode(byte[] in, char[] charMap) {
        return encode(in, 0, in.length, charMap);
    }

    public static char[] encode(byte[] in, int iLen) {
        return encode(in, 0, iLen, regularMap.encodingMap);
    }

    public static char[] encode(byte[] in, int iOff, int iLen, CharMap charMap) {
        return encode(in, iOff, iLen, charMap.encodingMap);
    }

    public static char[] encode(byte[] in, int iOff, int iLen, char[] charMap) {
        int oDataLen = ((iLen * 4) + 2) / 3;
        char[] out = new char[(((iLen + 2) / 3) * 4)];
        int iEnd = iOff + iLen;
        int op = 0;
        int ip = iOff;
        while (ip < iEnd) {
            int i1;
            int i2;
            int ip2 = ip + 1;
            int i0 = in[ip] & Keys.F12;
            if (ip2 < iEnd) {
                ip = ip2 + 1;
                i1 = in[ip2] & Keys.F12;
            } else {
                i1 = 0;
                ip = ip2;
            }
            if (ip < iEnd) {
                ip2 = ip + 1;
                i2 = in[ip] & Keys.F12;
            } else {
                i2 = 0;
                ip2 = ip;
            }
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 15) << 2) | (i2 >>> 6);
            int o3 = i2 & 63;
            int i = op + 1;
            out[op] = charMap[i0 >>> 2];
            op = i + 1;
            out[i] = charMap[o1];
            out[op] = op < oDataLen ? charMap[o2] : '=';
            i = op + 1;
            out[i] = i < oDataLen ? charMap[o3] : '=';
            op = i + 1;
            ip = ip2;
        }
        return out;
    }

    public static String decodeString(String s) {
        return decodeString(s, false);
    }

    public static String decodeString(String s, boolean useUrlSafeEncoding) {
        return new String(decode(s.toCharArray(), useUrlSafeEncoding ? urlsafeMap.decodingMap : regularMap.decodingMap));
    }

    public static byte[] decodeLines(String s) {
        return decodeLines(s, regularMap.decodingMap);
    }

    public static byte[] decodeLines(String s, CharMap inverseCharMap) {
        return decodeLines(s, inverseCharMap.decodingMap);
    }

    public static byte[] decodeLines(String s, byte[] inverseCharMap) {
        char[] buf = new char[s.length()];
        int p = 0;
        for (int ip = 0; ip < s.length(); ip++) {
            char c = s.charAt(ip);
            if (!(c == ' ' || c == '\r' || c == '\n' || c == '\t')) {
                int p2 = p + 1;
                buf[p] = c;
                p = p2;
            }
        }
        return decode(buf, 0, p, inverseCharMap);
    }

    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    public static byte[] decode(String s, CharMap inverseCharMap) {
        return decode(s.toCharArray(), inverseCharMap);
    }

    public static byte[] decode(char[] in, byte[] inverseCharMap) {
        return decode(in, 0, in.length, inverseCharMap);
    }

    public static byte[] decode(char[] in, CharMap inverseCharMap) {
        return decode(in, 0, in.length, inverseCharMap);
    }

    public static byte[] decode(char[] in) {
        return decode(in, 0, in.length, regularMap.decodingMap);
    }

    public static byte[] decode(char[] in, int iOff, int iLen, CharMap inverseCharMap) {
        return decode(in, iOff, iLen, inverseCharMap.decodingMap);
    }

    public static byte[] decode(char[] in, int iOff, int iLen, byte[] inverseCharMap) {
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[(iOff + iLen) - 1] == '=') {
            iLen--;
        }
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int iEnd = iOff + iLen;
        int op = 0;
        int ip;
        for (int ip2 = iOff; ip2 < iEnd; ip2 = ip) {
            int i2;
            int i3;
            ip = ip2 + 1;
            int i0 = in[ip2];
            ip2 = ip + 1;
            int i1 = in[ip];
            if (ip2 < iEnd) {
                ip = ip2 + 1;
                i2 = in[ip2];
                ip2 = ip;
            } else {
                i2 = 65;
            }
            if (ip2 < iEnd) {
                ip = ip2 + 1;
                i3 = in[ip2];
            } else {
                i3 = 65;
                ip = ip2;
            }
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int b0 = inverseCharMap[i0];
            int b1 = inverseCharMap[i1];
            int b2 = inverseCharMap[i2];
            int b3 = inverseCharMap[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int o1 = ((b1 & 15) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            int op2 = op + 1;
            out[op] = (byte) ((b0 << 2) | (b1 >>> 4));
            if (op2 < oLen) {
                op = op2 + 1;
                out[op2] = (byte) o1;
            } else {
                op = op2;
            }
            if (op < oLen) {
                op2 = op + 1;
                out[op] = (byte) o2;
            } else {
                op2 = op;
            }
            op = op2;
        }
        return out;
    }

    private Base64Coder() {
    }
}
