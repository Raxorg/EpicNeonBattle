package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ArrayReflection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayMap<K, V> implements Iterable<Entry<K, V>> {
    private Entries entries1;
    private Entries entries2;
    public K[] keys;
    private Keys keysIter1;
    private Keys keysIter2;
    public boolean ordered;
    public int size;
    public V[] values;
    private Values valuesIter1;
    private Values valuesIter2;

    public static class Entries<K, V> implements Iterable<Entry<K, V>>, Iterator<Entry<K, V>> {
        Entry<K, V> entry = new Entry();
        int index;
        private final ArrayMap<K, V> map;
        boolean valid = true;

        public Entries(ArrayMap<K, V> map) {
            this.map = map;
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.index < this.map.size;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public Iterator<Entry<K, V>> iterator() {
            return this;
        }

        public Entry<K, V> next() {
            if (this.index >= this.map.size) {
                throw new NoSuchElementException(String.valueOf(this.index));
            } else if (this.valid) {
                this.entry.key = this.map.keys[this.index];
                Entry entry = this.entry;
                Object[] objArr = this.map.values;
                int i = this.index;
                this.index = i + 1;
                entry.value = objArr[i];
                return this.entry;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public void remove() {
            this.index--;
            this.map.removeIndex(this.index);
        }

        public void reset() {
            this.index = 0;
        }
    }

    public static class Keys<K> implements Iterable<K>, Iterator<K> {
        int index;
        private final ArrayMap<K, Object> map;
        boolean valid = true;

        public Keys(ArrayMap<K, Object> map) {
            this.map = map;
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.index < this.map.size;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public Iterator<K> iterator() {
            return this;
        }

        public K next() {
            if (this.index >= this.map.size) {
                throw new NoSuchElementException(String.valueOf(this.index));
            } else if (this.valid) {
                Object[] objArr = this.map.keys;
                int i = this.index;
                this.index = i + 1;
                return objArr[i];
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public void remove() {
            this.index--;
            this.map.removeIndex(this.index);
        }

        public void reset() {
            this.index = 0;
        }

        public Array<K> toArray() {
            return new Array(true, this.map.keys, this.index, this.map.size - this.index);
        }

        public Array<K> toArray(Array array) {
            array.addAll(this.map.keys, this.index, this.map.size - this.index);
            return array;
        }
    }

    public static class Values<V> implements Iterable<V>, Iterator<V> {
        int index;
        private final ArrayMap<Object, V> map;
        boolean valid = true;

        public Values(ArrayMap<Object, V> map) {
            this.map = map;
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.index < this.map.size;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public Iterator<V> iterator() {
            return this;
        }

        public V next() {
            if (this.index >= this.map.size) {
                throw new NoSuchElementException(String.valueOf(this.index));
            } else if (this.valid) {
                Object[] objArr = this.map.values;
                int i = this.index;
                this.index = i + 1;
                return objArr[i];
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public void remove() {
            this.index--;
            this.map.removeIndex(this.index);
        }

        public void reset() {
            this.index = 0;
        }

        public Array<V> toArray() {
            return new Array(true, this.map.values, this.index, this.map.size - this.index);
        }

        public Array<V> toArray(Array array) {
            array.addAll(this.map.values, this.index, this.map.size - this.index);
            return array;
        }
    }

    public ArrayMap() {
        this(true, 16);
    }

    public ArrayMap(int capacity) {
        this(true, capacity);
    }

    public ArrayMap(boolean ordered, int capacity) {
        this.ordered = ordered;
        this.keys = new Object[capacity];
        this.values = new Object[capacity];
    }

    public ArrayMap(boolean ordered, int capacity, Class keyArrayType, Class valueArrayType) {
        this.ordered = ordered;
        this.keys = (Object[]) ArrayReflection.newInstance(keyArrayType, capacity);
        this.values = (Object[]) ArrayReflection.newInstance(valueArrayType, capacity);
    }

    public ArrayMap(Class keyArrayType, Class valueArrayType) {
        this(false, 16, keyArrayType, valueArrayType);
    }

    public ArrayMap(ArrayMap array) {
        this(array.ordered, array.size, array.keys.getClass().getComponentType(), array.values.getClass().getComponentType());
        this.size = array.size;
        System.arraycopy(array.keys, 0, this.keys, 0, this.size);
        System.arraycopy(array.values, 0, this.values, 0, this.size);
    }

    public int put(K key, V value) {
        int index = indexOfKey(key);
        if (index == -1) {
            if (this.size == this.keys.length) {
                resize(Math.max(8, (int) (((float) this.size) * 1.75f)));
            }
            index = this.size;
            this.size = index + 1;
        }
        this.keys[index] = key;
        this.values[index] = value;
        return index;
    }

    public int put(K key, V value, int index) {
        int existingIndex = indexOfKey(key);
        if (existingIndex != -1) {
            removeIndex(existingIndex);
        } else if (this.size == this.keys.length) {
            resize(Math.max(8, (int) (((float) this.size) * 1.75f)));
        }
        System.arraycopy(this.keys, index, this.keys, index + 1, this.size - index);
        System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
        this.keys[index] = key;
        this.values[index] = value;
        this.size++;
        return index;
    }

    public void putAll(ArrayMap map) {
        putAll(map, 0, map.size);
    }

    public void putAll(ArrayMap map, int offset, int length) {
        if (offset + length > map.size) {
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + map.size);
        }
        int sizeNeeded = (this.size + length) - offset;
        if (sizeNeeded >= this.keys.length) {
            resize(Math.max(8, (int) (((float) sizeNeeded) * 1.75f)));
        }
        System.arraycopy(map.keys, offset, this.keys, this.size, length);
        System.arraycopy(map.values, offset, this.values, this.size, length);
        this.size += length;
    }

    public V get(K key) {
        Object[] keys = this.keys;
        int i = this.size - 1;
        if (key == null) {
            while (i >= 0) {
                if (keys[i] == key) {
                    return this.values[i];
                }
                i--;
            }
        } else {
            while (i >= 0) {
                if (key.equals(keys[i])) {
                    return this.values[i];
                }
                i--;
            }
        }
        return null;
    }

    public K getKey(V value, boolean identity) {
        Object[] values = this.values;
        int i = this.size - 1;
        if (identity || value == null) {
            while (i >= 0) {
                if (values[i] == value) {
                    return this.keys[i];
                }
                i--;
            }
        } else {
            while (i >= 0) {
                if (value.equals(values[i])) {
                    return this.keys[i];
                }
                i--;
            }
        }
        return null;
    }

    public K getKeyAt(int index) {
        if (index < this.size) {
            return this.keys[index];
        }
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    public V getValueAt(int index) {
        if (index < this.size) {
            return this.values[index];
        }
        throw new IndexOutOfBoundsException(String.valueOf(index));
    }

    public K firstKey() {
        if (this.size != 0) {
            return this.keys[0];
        }
        throw new IllegalStateException("Map is empty.");
    }

    public V firstValue() {
        if (this.size != 0) {
            return this.values[0];
        }
        throw new IllegalStateException("Map is empty.");
    }

    public void setKey(int index, K key) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        this.keys[index] = key;
    }

    public void setValue(int index, V value) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        this.values[index] = value;
    }

    public void insert(int index, K key, V value) {
        if (index > this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        if (this.size == this.keys.length) {
            resize(Math.max(8, (int) (((float) this.size) * 1.75f)));
        }
        if (this.ordered) {
            System.arraycopy(this.keys, index, this.keys, index + 1, this.size - index);
            System.arraycopy(this.values, index, this.values, index + 1, this.size - index);
        } else {
            this.keys[this.size] = this.keys[index];
            this.values[this.size] = this.values[index];
        }
        this.size++;
        this.keys[index] = key;
        this.values[index] = value;
    }

    public boolean containsKey(K key) {
        K[] keys = this.keys;
        int i = this.size - 1;
        int i2;
        if (key == null) {
            do {
                i2 = i;
                if (i2 >= 0) {
                    i = i2 - 1;
                }
            } while (keys[i2] != key);
            return true;
        }
        do {
            i2 = i;
            if (i2 >= 0) {
                i = i2 - 1;
            }
        } while (!key.equals(keys[i2]));
        return true;
        return false;
    }

    public boolean containsValue(V value, boolean identity) {
        V[] values = this.values;
        int i = this.size - 1;
        int i2;
        if (identity || value == null) {
            do {
                i2 = i;
                if (i2 >= 0) {
                    i = i2 - 1;
                }
            } while (values[i2] != value);
            return true;
        }
        do {
            i2 = i;
            if (i2 >= 0) {
                i = i2 - 1;
            }
        } while (!value.equals(values[i2]));
        return true;
        return false;
    }

    public int indexOfKey(K key) {
        Object[] keys = this.keys;
        int n;
        int i;
        if (key == null) {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (keys[i] == key) {
                    return i;
                }
            }
        } else {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (key.equals(keys[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public int indexOfValue(V value, boolean identity) {
        Object[] values = this.values;
        int n;
        int i;
        if (identity || value == null) {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (values[i] == value) {
                    return i;
                }
            }
        } else {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (value.equals(values[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    public V removeKey(K key) {
        Object[] keys = this.keys;
        int n;
        int i;
        V value;
        if (key == null) {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (keys[i] == key) {
                    value = this.values[i];
                    removeIndex(i);
                    return value;
                }
            }
        } else {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (key.equals(keys[i])) {
                    value = this.values[i];
                    removeIndex(i);
                    return value;
                }
            }
        }
        return null;
    }

    public boolean removeValue(V value, boolean identity) {
        Object[] values = this.values;
        int n;
        int i;
        if (identity || value == null) {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (values[i] == value) {
                    removeIndex(i);
                    return true;
                }
            }
        } else {
            n = this.size;
            for (i = 0; i < n; i++) {
                if (value.equals(values[i])) {
                    removeIndex(i);
                    return true;
                }
            }
        }
        return false;
    }

    public void removeIndex(int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        Object[] keys = this.keys;
        this.size--;
        if (this.ordered) {
            System.arraycopy(keys, index + 1, keys, index, this.size - index);
            System.arraycopy(this.values, index + 1, this.values, index, this.size - index);
        } else {
            keys[index] = keys[this.size];
            this.values[index] = this.values[this.size];
        }
        keys[this.size] = null;
        this.values[this.size] = null;
    }

    public K peekKey() {
        return this.keys[this.size - 1];
    }

    public V peekValue() {
        return this.values[this.size - 1];
    }

    public void clear(int maximumCapacity) {
        if (this.keys.length <= maximumCapacity) {
            clear();
            return;
        }
        this.size = 0;
        resize(maximumCapacity);
    }

    public void clear() {
        K[] keys = this.keys;
        V[] values = this.values;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            keys[i] = null;
            values[i] = null;
        }
        this.size = 0;
    }

    public void shrink() {
        if (this.keys.length != this.size) {
            resize(this.size);
        }
    }

    public void ensureCapacity(int additionalCapacity) {
        int sizeNeeded = this.size + additionalCapacity;
        if (sizeNeeded >= this.keys.length) {
            resize(Math.max(8, sizeNeeded));
        }
    }

    protected void resize(int newSize) {
        Object[] newKeys = (Object[]) ((Object[]) ArrayReflection.newInstance(this.keys.getClass().getComponentType(), newSize));
        System.arraycopy(this.keys, 0, newKeys, 0, Math.min(this.size, newKeys.length));
        this.keys = newKeys;
        Object[] newValues = (Object[]) ((Object[]) ArrayReflection.newInstance(this.values.getClass().getComponentType(), newSize));
        System.arraycopy(this.values, 0, newValues, 0, Math.min(this.size, newValues.length));
        this.values = newValues;
    }

    public void reverse() {
        int lastIndex = this.size - 1;
        int n = this.size / 2;
        for (int i = 0; i < n; i++) {
            int ii = lastIndex - i;
            K tempKey = this.keys[i];
            this.keys[i] = this.keys[ii];
            this.keys[ii] = tempKey;
            V tempValue = this.values[i];
            this.values[i] = this.values[ii];
            this.values[ii] = tempValue;
        }
    }

    public void shuffle() {
        for (int i = this.size - 1; i >= 0; i--) {
            int ii = MathUtils.random(i);
            K tempKey = this.keys[i];
            this.keys[i] = this.keys[ii];
            this.keys[ii] = tempKey;
            V tempValue = this.values[i];
            this.values[i] = this.values[ii];
            this.values[ii] = tempValue;
        }
    }

    public void truncate(int newSize) {
        if (this.size > newSize) {
            for (int i = newSize; i < this.size; i++) {
                this.keys[i] = null;
                this.values[i] = null;
            }
            this.size = newSize;
        }
    }

    public int hashCode() {
        K[] keys = this.keys;
        V[] values = this.values;
        int h = 0;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            K key = keys[i];
            V value = values[i];
            if (key != null) {
                h += key.hashCode() * 31;
            }
            if (value != null) {
                h += value.hashCode();
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ArrayMap)) {
            return false;
        }
        ArrayMap<K, V> other = (ArrayMap) obj;
        if (other.size != this.size) {
            return false;
        }
        K[] keys = this.keys;
        V[] values = this.values;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            K key = keys[i];
            V value = values[i];
            if (value == null) {
                if (!other.containsKey(key) || other.get(key) != null) {
                    return false;
                }
            } else if (!value.equals(other.get(key))) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        if (this.size == 0) {
            return "{}";
        }
        K[] keys = this.keys;
        V[] values = this.values;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        buffer.append(keys[0]);
        buffer.append('=');
        buffer.append(values[0]);
        for (int i = 1; i < this.size; i++) {
            buffer.append(", ");
            buffer.append(keys[i]);
            buffer.append('=');
            buffer.append(values[i]);
        }
        buffer.append('}');
        return buffer.toString();
    }

    public Iterator<Entry<K, V>> iterator() {
        return entries();
    }

    public Entries<K, V> entries() {
        if (this.entries1 == null) {
            this.entries1 = new Entries(this);
            this.entries2 = new Entries(this);
        }
        if (this.entries1.valid) {
            this.entries2.index = 0;
            this.entries2.valid = true;
            this.entries1.valid = false;
            return this.entries2;
        }
        this.entries1.index = 0;
        this.entries1.valid = true;
        this.entries2.valid = false;
        return this.entries1;
    }

    public Values<V> values() {
        if (this.valuesIter1 == null) {
            this.valuesIter1 = new Values(this);
            this.valuesIter2 = new Values(this);
        }
        if (this.valuesIter1.valid) {
            this.valuesIter2.index = 0;
            this.valuesIter2.valid = true;
            this.valuesIter1.valid = false;
            return this.valuesIter2;
        }
        this.valuesIter1.index = 0;
        this.valuesIter1.valid = true;
        this.valuesIter2.valid = false;
        return this.valuesIter1;
    }

    public Keys<K> keys() {
        if (this.keysIter1 == null) {
            this.keysIter1 = new Keys(this);
            this.keysIter2 = new Keys(this);
        }
        if (this.keysIter1.valid) {
            this.keysIter2.index = 0;
            this.keysIter2.valid = true;
            this.keysIter1.valid = false;
            return this.keysIter2;
        }
        this.keysIter1.index = 0;
        this.keysIter1.valid = true;
        this.keysIter2.valid = false;
        return this.keysIter1;
    }
}
