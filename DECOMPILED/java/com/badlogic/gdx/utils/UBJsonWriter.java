package com.badlogic.gdx.utils;

import com.badlogic.gdx.Input.Keys;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class UBJsonWriter implements Closeable {
    private JsonObject current;
    private boolean named;
    final DataOutputStream out;
    private final Array<JsonObject> stack = new Array();

    private class JsonObject {
        final boolean array;

        JsonObject(boolean array) throws IOException {
            this.array = array;
            UBJsonWriter.this.out.writeByte(array ? 91 : 123);
        }

        void close() throws IOException {
            UBJsonWriter.this.out.writeByte(this.array ? 93 : 125);
        }
    }

    public UBJsonWriter(OutputStream out) {
        if (!(out instanceof DataOutputStream)) {
            out = new DataOutputStream(out);
        }
        this.out = (DataOutputStream) out;
    }

    public UBJsonWriter object() throws IOException {
        if (!(this.current == null || this.current.array)) {
            if (this.named) {
                this.named = false;
            } else {
                throw new IllegalStateException("Name must be set.");
            }
        }
        Array array = this.stack;
        JsonObject jsonObject = new JsonObject(false);
        this.current = jsonObject;
        array.add(jsonObject);
        return this;
    }

    public UBJsonWriter object(String name) throws IOException {
        name(name).object();
        return this;
    }

    public UBJsonWriter array() throws IOException {
        if (!(this.current == null || this.current.array)) {
            if (this.named) {
                this.named = false;
            } else {
                throw new IllegalStateException("Name must be set.");
            }
        }
        Array array = this.stack;
        JsonObject jsonObject = new JsonObject(true);
        this.current = jsonObject;
        array.add(jsonObject);
        return this;
    }

    public UBJsonWriter array(String name) throws IOException {
        name(name).array();
        return this;
    }

    public UBJsonWriter name(String name) throws IOException {
        if (this.current == null || this.current.array) {
            throw new IllegalStateException("Current item must be an object.");
        }
        byte[] bytes = name.getBytes("UTF-8");
        if (bytes.length <= 127) {
            this.out.writeByte(Keys.BUTTON_R2);
            this.out.writeByte(bytes.length);
        } else if (bytes.length <= 32767) {
            this.out.writeByte(73);
            this.out.writeShort(bytes.length);
        } else {
            this.out.writeByte(Keys.BUTTON_START);
            this.out.writeInt(bytes.length);
        }
        this.out.write(bytes);
        this.named = true;
        return this;
    }

    public UBJsonWriter value(byte value) throws IOException {
        checkName();
        this.out.writeByte(Keys.BUTTON_R2);
        this.out.writeByte(value);
        return this;
    }

    public UBJsonWriter value(short value) throws IOException {
        checkName();
        this.out.writeByte(73);
        this.out.writeShort(value);
        return this;
    }

    public UBJsonWriter value(int value) throws IOException {
        checkName();
        this.out.writeByte(Keys.BUTTON_START);
        this.out.writeInt(value);
        return this;
    }

    public UBJsonWriter value(long value) throws IOException {
        checkName();
        this.out.writeByte(76);
        this.out.writeLong(value);
        return this;
    }

    public UBJsonWriter value(float value) throws IOException {
        checkName();
        this.out.writeByte(100);
        this.out.writeFloat(value);
        return this;
    }

    public UBJsonWriter value(double value) throws IOException {
        checkName();
        this.out.writeByte(68);
        this.out.writeDouble(value);
        return this;
    }

    public UBJsonWriter value(boolean value) throws IOException {
        checkName();
        this.out.writeByte(value ? 84 : 70);
        return this;
    }

    public UBJsonWriter value(char value) throws IOException {
        checkName();
        this.out.writeByte(73);
        this.out.writeChar(value);
        return this;
    }

    public UBJsonWriter value(String value) throws IOException {
        checkName();
        byte[] bytes = value.getBytes("UTF-8");
        this.out.writeByte(83);
        if (bytes.length <= 127) {
            this.out.writeByte(Keys.BUTTON_R2);
            this.out.writeByte(bytes.length);
        } else if (bytes.length <= 32767) {
            this.out.writeByte(73);
            this.out.writeShort(bytes.length);
        } else {
            this.out.writeByte(Keys.BUTTON_START);
            this.out.writeInt(bytes.length);
        }
        this.out.write(bytes);
        return this;
    }

    public UBJsonWriter value(byte[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(Keys.BUTTON_R2);
        this.out.writeByte(35);
        value(values.length);
        for (byte writeByte : values) {
            this.out.writeByte(writeByte);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(short[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(73);
        this.out.writeByte(35);
        value(values.length);
        for (short writeShort : values) {
            this.out.writeShort(writeShort);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(int[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(Keys.BUTTON_START);
        this.out.writeByte(35);
        value(values.length);
        for (int writeInt : values) {
            this.out.writeInt(writeInt);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(long[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(73);
        this.out.writeByte(35);
        value(values.length);
        for (long writeLong : values) {
            this.out.writeLong(writeLong);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(float[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(100);
        this.out.writeByte(35);
        value(values.length);
        for (float writeFloat : values) {
            this.out.writeFloat(writeFloat);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(double[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(100);
        this.out.writeByte(35);
        value(values.length);
        for (double writeDouble : values) {
            this.out.writeDouble(writeDouble);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(boolean[] values) throws IOException {
        array();
        for (boolean z : values) {
            this.out.writeByte(z ? 84 : 70);
        }
        pop();
        return this;
    }

    public UBJsonWriter value(char[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(73);
        this.out.writeByte(35);
        value(values.length);
        for (char writeChar : values) {
            this.out.writeChar(writeChar);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(String[] values) throws IOException {
        array();
        this.out.writeByte(36);
        this.out.writeByte(83);
        this.out.writeByte(35);
        value(values.length);
        for (String bytes : values) {
            byte[] bytes2 = bytes.getBytes("UTF-8");
            if (bytes2.length <= 127) {
                this.out.writeByte(Keys.BUTTON_R2);
                this.out.writeByte(bytes2.length);
            } else if (bytes2.length <= 32767) {
                this.out.writeByte(73);
                this.out.writeShort(bytes2.length);
            } else {
                this.out.writeByte(Keys.BUTTON_START);
                this.out.writeInt(bytes2.length);
            }
            this.out.write(bytes2);
        }
        pop(true);
        return this;
    }

    public UBJsonWriter value(JsonValue value) throws IOException {
        JsonValue child;
        if (value.isObject()) {
            if (value.name != null) {
                object(value.name);
            } else {
                object();
            }
            for (child = value.child; child != null; child = child.next) {
                value(child);
            }
            pop();
        } else if (value.isArray()) {
            if (value.name != null) {
                array(value.name);
            } else {
                array();
            }
            for (child = value.child; child != null; child = child.next) {
                value(child);
            }
            pop();
        } else if (value.isBoolean()) {
            if (value.name != null) {
                name(value.name);
            }
            value(value.asBoolean());
        } else if (value.isDouble()) {
            if (value.name != null) {
                name(value.name);
            }
            value(value.asDouble());
        } else if (value.isLong()) {
            if (value.name != null) {
                name(value.name);
            }
            value(value.asLong());
        } else if (value.isString()) {
            if (value.name != null) {
                name(value.name);
            }
            value(value.asString());
        } else if (value.isNull()) {
            if (value.name != null) {
                name(value.name);
            }
            value();
        } else {
            throw new IOException("Unhandled JsonValue type");
        }
        return this;
    }

    public UBJsonWriter value(Object object) throws IOException {
        if (object == null) {
            return value();
        }
        if (object instanceof Number) {
            Number number = (Number) object;
            if (object instanceof Byte) {
                return value(number.byteValue());
            }
            if (object instanceof Short) {
                return value(number.shortValue());
            }
            if (object instanceof Integer) {
                return value(number.intValue());
            }
            if (object instanceof Long) {
                return value(number.longValue());
            }
            if (object instanceof Float) {
                return value(number.floatValue());
            }
            if (object instanceof Double) {
                return value(number.doubleValue());
            }
            return this;
        } else if (object instanceof Character) {
            return value(((Character) object).charValue());
        } else {
            if (object instanceof CharSequence) {
                return value(object.toString());
            }
            throw new IOException("Unknown object type.");
        }
    }

    public UBJsonWriter value() throws IOException {
        checkName();
        this.out.writeByte(90);
        return this;
    }

    public UBJsonWriter set(String name, byte value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, short value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, int value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, long value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, float value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, double value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, boolean value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, char value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, String value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, byte[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, short[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, int[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, long[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, float[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, double[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, boolean[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, char[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name, String[] value) throws IOException {
        return name(name).value(value);
    }

    public UBJsonWriter set(String name) throws IOException {
        return name(name).value();
    }

    private void checkName() {
        if (this.current != null && !this.current.array) {
            if (this.named) {
                this.named = false;
                return;
            }
            throw new IllegalStateException("Name must be set.");
        }
    }

    public UBJsonWriter pop() throws IOException {
        return pop(false);
    }

    protected UBJsonWriter pop(boolean silent) throws IOException {
        if (this.named) {
            throw new IllegalStateException("Expected an object, array, or value since a name was set.");
        }
        if (silent) {
            this.stack.pop();
        } else {
            ((JsonObject) this.stack.pop()).close();
        }
        this.current = this.stack.size == 0 ? null : (JsonObject) this.stack.peek();
        return this;
    }

    public void flush() throws IOException {
        this.out.flush();
    }

    public void close() throws IOException {
        while (this.stack.size > 0) {
            pop();
        }
        this.out.close();
    }
}
