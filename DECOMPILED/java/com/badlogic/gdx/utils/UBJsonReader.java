package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class UBJsonReader implements BaseJsonReader {
    public boolean oldFormat = true;

    public JsonValue parse(InputStream input) {
        Throwable ex;
        Throwable th;
        DataInputStream din = null;
        try {
            DataInputStream din2 = new DataInputStream(input);
            try {
                JsonValue parse = parse(din2);
                StreamUtils.closeQuietly(din2);
                return parse;
            } catch (IOException e) {
                ex = e;
                din = din2;
                try {
                    throw new SerializationException(ex);
                } catch (Throwable th2) {
                    th = th2;
                    StreamUtils.closeQuietly(din);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                din = din2;
                StreamUtils.closeQuietly(din);
                throw th;
            }
        } catch (IOException e2) {
            ex = e2;
            throw new SerializationException(ex);
        }
    }

    public JsonValue parse(FileHandle file) {
        try {
            return parse(file.read(8192));
        } catch (Exception ex) {
            throw new SerializationException("Error parsing file: " + file, ex);
        }
    }

    public JsonValue parse(DataInputStream din) throws IOException {
        try {
            JsonValue parse = parse(din, din.readByte());
            return parse;
        } finally {
            StreamUtils.closeQuietly(din);
        }
    }

    protected JsonValue parse(DataInputStream din, byte type) throws IOException {
        if (type == (byte) 91) {
            return parseArray(din);
        }
        if (type == (byte) 123) {
            return parseObject(din);
        }
        if (type == (byte) 90) {
            return new JsonValue(ValueType.nullValue);
        }
        if (type == (byte) 84) {
            return new JsonValue(true);
        }
        if (type == (byte) 70) {
            return new JsonValue(false);
        }
        if (type == (byte) 66) {
            return new JsonValue((long) readUChar(din));
        }
        if (type == (byte) 85) {
            return new JsonValue((long) readUChar(din));
        }
        if (type == (byte) 105) {
            return new JsonValue(this.oldFormat ? (long) din.readShort() : (long) din.readByte());
        } else if (type == (byte) 73) {
            return new JsonValue(this.oldFormat ? (long) din.readInt() : (long) din.readShort());
        } else if (type == (byte) 108) {
            return new JsonValue((long) din.readInt());
        } else {
            if (type == (byte) 76) {
                return new JsonValue(din.readLong());
            }
            if (type == (byte) 100) {
                return new JsonValue((double) din.readFloat());
            }
            if (type == (byte) 68) {
                return new JsonValue(din.readDouble());
            }
            if (type == (byte) 115 || type == (byte) 83) {
                return new JsonValue(parseString(din, type));
            }
            if (type == (byte) 97 || type == (byte) 65) {
                return parseData(din, type);
            }
            throw new GdxRuntimeException("Unrecognized data type");
        }
    }

    protected JsonValue parseArray(DataInputStream din) throws IOException {
        JsonValue result = new JsonValue(ValueType.array);
        byte type = din.readByte();
        byte valueType = (byte) 0;
        if (type == (byte) 36) {
            valueType = din.readByte();
            type = din.readByte();
        }
        long size = -1;
        if (type == (byte) 35) {
            size = parseSize(din, false, -1);
            if (size < 0) {
                throw new GdxRuntimeException("Unrecognized data type");
            }
            if (size != 0) {
                if (valueType == (byte) 0) {
                    type = din.readByte();
                } else {
                    type = valueType;
                }
            }
            return result;
        }
        JsonValue prev = null;
        long c = 0;
        while (din.available() > 0 && type != (byte) 93) {
            JsonValue val = parse(din, type);
            if (prev != null) {
                val.prev = prev;
                prev.next = val;
                result.size++;
            } else {
                result.child = val;
                result.size = 1;
            }
            prev = val;
            if (size > 0) {
                c++;
                if (c >= size) {
                    break;
                }
            }
            if (valueType == (byte) 0) {
                type = din.readByte();
            } else {
                type = valueType;
            }
        }
        return result;
    }

    protected JsonValue parseObject(DataInputStream din) throws IOException {
        JsonValue result = new JsonValue(ValueType.object);
        byte type = din.readByte();
        byte valueType = (byte) 0;
        if (type == (byte) 36) {
            valueType = din.readByte();
            type = din.readByte();
        }
        long size = -1;
        if (type == (byte) 35) {
            size = parseSize(din, false, -1);
            if (size < 0) {
                throw new GdxRuntimeException("Unrecognized data type");
            }
            if (size != 0) {
                type = din.readByte();
            }
            return result;
        }
        JsonValue prev = null;
        long c = 0;
        while (din.available() > 0 && type != (byte) 125) {
            byte readByte;
            String key = parseString(din, true, type);
            if (valueType == (byte) 0) {
                readByte = din.readByte();
            } else {
                readByte = valueType;
            }
            JsonValue child = parse(din, readByte);
            child.setName(key);
            if (prev != null) {
                child.prev = prev;
                prev.next = child;
                result.size++;
            } else {
                result.child = child;
                result.size = 1;
            }
            prev = child;
            if (size > 0) {
                c++;
                if (c >= size) {
                    break;
                }
            }
            type = din.readByte();
        }
        return result;
    }

    protected JsonValue parseData(DataInputStream din, byte blockType) throws IOException {
        byte dataType = din.readByte();
        long size = blockType == (byte) 65 ? readUInt(din) : (long) readUChar(din);
        JsonValue result = new JsonValue(ValueType.array);
        JsonValue prev = null;
        for (long i = 0; i < size; i++) {
            JsonValue val = parse(din, dataType);
            if (prev != null) {
                prev.next = val;
                result.size++;
            } else {
                result.child = val;
                result.size = 1;
            }
            prev = val;
        }
        return result;
    }

    protected String parseString(DataInputStream din, byte type) throws IOException {
        return parseString(din, false, type);
    }

    protected String parseString(DataInputStream din, boolean sOptional, byte type) throws IOException {
        long size = -1;
        if (type == (byte) 83) {
            size = parseSize(din, true, -1);
        } else if (type == (byte) 115) {
            size = (long) readUChar(din);
        } else if (sOptional) {
            size = parseSize(din, type, false, -1);
        }
        if (size >= 0) {
            return size > 0 ? readString(din, size) : BuildConfig.VERSION_NAME;
        } else {
            throw new GdxRuntimeException("Unrecognized data type, string expected");
        }
    }

    protected long parseSize(DataInputStream din, boolean useIntOnError, long defaultValue) throws IOException {
        return parseSize(din, din.readByte(), useIntOnError, defaultValue);
    }

    protected long parseSize(DataInputStream din, byte type, boolean useIntOnError, long defaultValue) throws IOException {
        if (type == (byte) 105) {
            return (long) readUChar(din);
        }
        if (type == (byte) 73) {
            return (long) readUShort(din);
        }
        if (type == (byte) 108) {
            return readUInt(din);
        }
        if (type == (byte) 76) {
            return din.readLong();
        }
        if (useIntOnError) {
            return (((((long) (((short) type) & Keys.F12)) << 24) | (((long) (((short) din.readByte()) & Keys.F12)) << 16)) | (((long) (((short) din.readByte()) & Keys.F12)) << 8)) | ((long) (((short) din.readByte()) & Keys.F12));
        }
        return defaultValue;
    }

    protected short readUChar(DataInputStream din) throws IOException {
        return (short) (((short) din.readByte()) & Keys.F12);
    }

    protected int readUShort(DataInputStream din) throws IOException {
        return din.readShort() & 65535;
    }

    protected long readUInt(DataInputStream din) throws IOException {
        return ((long) din.readInt()) & -1;
    }

    protected String readString(DataInputStream din, long size) throws IOException {
        byte[] data = new byte[((int) size)];
        din.readFully(data);
        return new String(data, "UTF-8");
    }
}
