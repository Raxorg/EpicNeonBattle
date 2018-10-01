package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class LongMap<V> implements Iterable<Entry<V>> {
    private static final int EMPTY = 0;
    private static final int PRIME1 = -1105259343;
    private static final int PRIME2 = -1262997959;
    private static final int PRIME3 = -825114047;
    int capacity;
    private Entries entries1;
    private Entries entries2;
    boolean hasZeroValue;
    private int hashShift;
    long[] keyTable;
    private Keys keys1;
    private Keys keys2;
    private float loadFactor;
    private int mask;
    private int pushIterations;
    public int size;
    private int stashCapacity;
    int stashSize;
    private int threshold;
    V[] valueTable;
    private Values values1;
    private Values values2;
    V zeroValue;

    private static class MapIterator<V> {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;
        int currentIndex;
        public boolean hasNext;
        final LongMap<V> map;
        int nextIndex;
        boolean valid = true;

        public MapIterator(LongMap<V> map) {
            this.map = map;
            reset();
        }

        public void reset() {
            this.currentIndex = INDEX_ILLEGAL;
            this.nextIndex = INDEX_ZERO;
            if (this.map.hasZeroValue) {
                this.hasNext = true;
            } else {
                findNextIndex();
            }
        }

        void findNextIndex() {
            this.hasNext = false;
            long[] keyTable = this.map.keyTable;
            int n = this.map.capacity + this.map.stashSize;
            do {
                int i = this.nextIndex + 1;
                this.nextIndex = i;
                if (i >= n) {
                    return;
                }
            } while (keyTable[this.nextIndex] == 0);
            this.hasNext = true;
        }

        public void remove() {
            if (this.currentIndex == INDEX_ZERO && this.map.hasZeroValue) {
                this.map.zeroValue = null;
                this.map.hasZeroValue = false;
            } else if (this.currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (this.currentIndex >= this.map.capacity) {
                this.map.removeStashIndex(this.currentIndex);
                this.nextIndex = this.currentIndex + INDEX_ZERO;
                findNextIndex();
            } else {
                this.map.keyTable[this.currentIndex] = 0;
                this.map.valueTable[this.currentIndex] = null;
            }
            this.currentIndex = INDEX_ILLEGAL;
            LongMap longMap = this.map;
            longMap.size += INDEX_ZERO;
        }
    }

    public static class Entries<V> extends MapIterator<V> implements Iterable<Entry<V>>, Iterator<Entry<V>> {
        private Entry<V> entry = new Entry();

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Entries(LongMap map) {
            super(map);
        }

        public Entry<V> next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                long[] keyTable = this.map.keyTable;
                if (this.nextIndex == -1) {
                    this.entry.key = 0;
                    this.entry.value = this.map.zeroValue;
                } else {
                    this.entry.key = keyTable[this.nextIndex];
                    this.entry.value = this.map.valueTable[this.nextIndex];
                }
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return this.entry;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.hasNext;
            }
            throw new GdxRuntimeException("#iterator() cannot be used nested.");
        }

        public Iterator<Entry<V>> iterator() {
            return this;
        }

        public void remove() {
            super.remove();
        }
    }

    public static class Entry<V> {
        public long key;
        public V value;

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    public static class Keys extends MapIterator {
        public /* bridge */ /* synthetic */ void remove() {
            super.remove();
        }

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Keys(LongMap map) {
            super(map);
        }

        public long next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                long key = this.nextIndex == -1 ? 0 : this.map.keyTable[this.nextIndex];
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return key;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public LongArray toArray() {
            LongArray array = new LongArray(true, this.map.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }
    }

    public static class Values<V> extends MapIterator<V> implements Iterable<V>, Iterator<V> {
        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Values(LongMap<V> map) {
            super(map);
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.hasNext;
            }
            throw new GdxRuntimeException("#iterator() cannot be used nested.");
        }

        public V next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                V value;
                if (this.nextIndex == -1) {
                    value = this.map.zeroValue;
                } else {
                    value = this.map.valueTable[this.nextIndex];
                }
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return value;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public Iterator<V> iterator() {
            return this;
        }

        public Array<V> toArray() {
            Array array = new Array(true, this.map.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }

        public void remove() {
            super.remove();
        }
    }

    public LongMap() {
        this(32, 0.8f);
    }

    public LongMap(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    public LongMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity must be >= 0: " + initialCapacity);
        } else if (initialCapacity > 1073741824) {
            throw new IllegalArgumentException("initialCapacity is too large: " + initialCapacity);
        } else {
            this.capacity = MathUtils.nextPowerOfTwo(initialCapacity);
            if (loadFactor <= 0.0f) {
                throw new IllegalArgumentException("loadFactor must be > 0: " + loadFactor);
            }
            this.loadFactor = loadFactor;
            this.threshold = (int) (((float) this.capacity) * loadFactor);
            this.mask = this.capacity - 1;
            this.hashShift = 63 - Long.numberOfTrailingZeros((long) this.capacity);
            this.stashCapacity = Math.max(3, ((int) Math.ceil(Math.log((double) this.capacity))) * 2);
            this.pushIterations = Math.max(Math.min(this.capacity, 8), ((int) Math.sqrt((double) this.capacity)) / 8);
            this.keyTable = new long[(this.capacity + this.stashCapacity)];
            this.valueTable = new Object[this.keyTable.length];
        }
    }

    public LongMap(LongMap<? extends V> map) {
        this(map.capacity, map.loadFactor);
        this.stashSize = map.stashSize;
        System.arraycopy(map.keyTable, EMPTY, this.keyTable, EMPTY, map.keyTable.length);
        System.arraycopy(map.valueTable, EMPTY, this.valueTable, EMPTY, map.valueTable.length);
        this.size = map.size;
        this.zeroValue = map.zeroValue;
        this.hasZeroValue = map.hasZeroValue;
    }

    public V put(long key, V value) {
        V oldValue;
        if (key == 0) {
            oldValue = this.zeroValue;
            this.zeroValue = value;
            if (this.hasZeroValue) {
                return oldValue;
            }
            this.hasZeroValue = true;
            this.size++;
            return oldValue;
        }
        long[] keyTable = this.keyTable;
        int index1 = (int) (((long) this.mask) & key);
        long key1 = keyTable[index1];
        if (key1 == key) {
            oldValue = this.valueTable[index1];
            this.valueTable[index1] = value;
            return oldValue;
        }
        int index2 = hash2(key);
        long key2 = keyTable[index2];
        if (key2 == key) {
            oldValue = this.valueTable[index2];
            this.valueTable[index2] = value;
            return oldValue;
        }
        int index3 = hash3(key);
        long key3 = keyTable[index3];
        if (key3 == key) {
            oldValue = this.valueTable[index3];
            this.valueTable[index3] = value;
            return oldValue;
        }
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (keyTable[i] == key) {
                oldValue = this.valueTable[i];
                this.valueTable[i] = value;
                return oldValue;
            }
            i++;
        }
        int i2;
        if (key1 == 0) {
            keyTable[index1] = key;
            this.valueTable[index1] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
            return null;
        } else if (key2 == 0) {
            keyTable[index2] = key;
            this.valueTable[index2] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
            return null;
        } else if (key3 == 0) {
            keyTable[index3] = key;
            this.valueTable[index3] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
            return null;
        } else {
            push(key, value, index1, key1, index2, key2, index3, key3);
            return null;
        }
    }

    public void putAll(LongMap<V> map) {
        Iterator it = map.entries().iterator();
        while (it.hasNext()) {
            Entry<V> entry = (Entry) it.next();
            put(entry.key, entry.value);
        }
    }

    private void putResize(long key, V value) {
        if (key == 0) {
            this.zeroValue = value;
            this.hasZeroValue = true;
            return;
        }
        int index1 = (int) (((long) this.mask) & key);
        long key1 = this.keyTable[index1];
        int i;
        if (key1 == 0) {
            this.keyTable[index1] = key;
            this.valueTable[index1] = value;
            i = this.size;
            this.size = i + 1;
            if (i >= this.threshold) {
                resize(this.capacity << 1);
                return;
            }
            return;
        }
        int index2 = hash2(key);
        long key2 = this.keyTable[index2];
        if (key2 == 0) {
            this.keyTable[index2] = key;
            this.valueTable[index2] = value;
            i = this.size;
            this.size = i + 1;
            if (i >= this.threshold) {
                resize(this.capacity << 1);
                return;
            }
            return;
        }
        int index3 = hash3(key);
        long key3 = this.keyTable[index3];
        if (key3 == 0) {
            this.keyTable[index3] = key;
            this.valueTable[index3] = value;
            i = this.size;
            this.size = i + 1;
            if (i >= this.threshold) {
                resize(this.capacity << 1);
                return;
            }
            return;
        }
        push(key, value, index1, key1, index2, key2, index3, key3);
    }

    private void push(long insertKey, V insertValue, int index1, long key1, int index2, long key2, int index3, long key3) {
        long[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int mask = this.mask;
        int i = EMPTY;
        int pushIterations = this.pushIterations;
        while (true) {
            long evictedKey;
            V evictedValue;
            switch (MathUtils.random(2)) {
                case EMPTY /*0*/:
                    evictedKey = key1;
                    evictedValue = valueTable[index1];
                    keyTable[index1] = insertKey;
                    valueTable[index1] = insertValue;
                    break;
                case BuildConfig.VERSION_CODE /*1*/:
                    evictedKey = key2;
                    evictedValue = valueTable[index2];
                    keyTable[index2] = insertKey;
                    valueTable[index2] = insertValue;
                    break;
                default:
                    evictedKey = key3;
                    evictedValue = valueTable[index3];
                    keyTable[index3] = insertKey;
                    valueTable[index3] = insertValue;
                    break;
            }
            index1 = (int) (((long) mask) & evictedKey);
            key1 = keyTable[index1];
            int i2;
            if (key1 == 0) {
                keyTable[index1] = evictedKey;
                valueTable[index1] = evictedValue;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                    return;
                }
                return;
            }
            index2 = hash2(evictedKey);
            key2 = keyTable[index2];
            if (key2 == 0) {
                keyTable[index2] = evictedKey;
                valueTable[index2] = evictedValue;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                    return;
                }
                return;
            }
            index3 = hash3(evictedKey);
            key3 = keyTable[index3];
            if (key3 == 0) {
                keyTable[index3] = evictedKey;
                valueTable[index3] = evictedValue;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                    return;
                }
                return;
            }
            i++;
            if (i == pushIterations) {
                putStash(evictedKey, evictedValue);
                return;
            } else {
                insertKey = evictedKey;
                insertValue = evictedValue;
            }
        }
    }

    private void putStash(long key, V value) {
        if (this.stashSize == this.stashCapacity) {
            resize(this.capacity << 1);
            put(key, value);
            return;
        }
        int index = this.capacity + this.stashSize;
        this.keyTable[index] = key;
        this.valueTable[index] = value;
        this.stashSize++;
        this.size++;
    }

    public V get(long key) {
        if (key != 0) {
            int index = (int) (((long) this.mask) & key);
            if (this.keyTable[index] != key) {
                index = hash2(key);
                if (this.keyTable[index] != key) {
                    index = hash3(key);
                    if (this.keyTable[index] != key) {
                        return getStash(key, null);
                    }
                }
            }
            return this.valueTable[index];
        } else if (this.hasZeroValue) {
            return this.zeroValue;
        } else {
            return null;
        }
    }

    public V get(long key, V defaultValue) {
        if (key != 0) {
            int index = (int) (((long) this.mask) & key);
            if (this.keyTable[index] != key) {
                index = hash2(key);
                if (this.keyTable[index] != key) {
                    index = hash3(key);
                    if (this.keyTable[index] != key) {
                        return getStash(key, defaultValue);
                    }
                }
            }
            return this.valueTable[index];
        } else if (this.hasZeroValue) {
            return this.zeroValue;
        } else {
            return defaultValue;
        }
    }

    private V getStash(long key, V defaultValue) {
        long[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (keyTable[i] == key) {
                return this.valueTable[i];
            }
            i++;
        }
        return defaultValue;
    }

    public V remove(long key) {
        V v;
        if (key != 0) {
            int index = (int) (((long) this.mask) & key);
            if (this.keyTable[index] == key) {
                this.keyTable[index] = 0;
                v = this.valueTable[index];
                this.valueTable[index] = null;
                this.size--;
                return v;
            }
            index = hash2(key);
            if (this.keyTable[index] == key) {
                this.keyTable[index] = 0;
                v = this.valueTable[index];
                this.valueTable[index] = null;
                this.size--;
                return v;
            }
            index = hash3(key);
            if (this.keyTable[index] != key) {
                return removeStash(key);
            }
            this.keyTable[index] = 0;
            v = this.valueTable[index];
            this.valueTable[index] = null;
            this.size--;
            return v;
        } else if (!this.hasZeroValue) {
            return null;
        } else {
            v = this.zeroValue;
            this.zeroValue = null;
            this.hasZeroValue = false;
            this.size--;
            return v;
        }
    }

    V removeStash(long key) {
        long[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (keyTable[i] == key) {
                V oldValue = this.valueTable[i];
                removeStashIndex(i);
                this.size--;
                return oldValue;
            }
            i++;
        }
        return null;
    }

    void removeStashIndex(int index) {
        this.stashSize--;
        int lastIndex = this.capacity + this.stashSize;
        if (index < lastIndex) {
            this.keyTable[index] = this.keyTable[lastIndex];
            this.valueTable[index] = this.valueTable[lastIndex];
            this.valueTable[lastIndex] = null;
            return;
        }
        this.valueTable[index] = null;
    }

    public void shrink(int maximumCapacity) {
        if (maximumCapacity < 0) {
            throw new IllegalArgumentException("maximumCapacity must be >= 0: " + maximumCapacity);
        }
        if (this.size > maximumCapacity) {
            maximumCapacity = this.size;
        }
        if (this.capacity > maximumCapacity) {
            resize(MathUtils.nextPowerOfTwo(maximumCapacity));
        }
    }

    public void clear(int maximumCapacity) {
        if (this.capacity <= maximumCapacity) {
            clear();
            return;
        }
        this.zeroValue = null;
        this.hasZeroValue = false;
        this.size = EMPTY;
        resize(maximumCapacity);
    }

    public void clear() {
        if (this.size != 0) {
            long[] keyTable = this.keyTable;
            V[] valueTable = this.valueTable;
            int i = this.capacity + this.stashSize;
            while (true) {
                int i2 = i - 1;
                if (i > 0) {
                    keyTable[i2] = 0;
                    valueTable[i2] = null;
                    i = i2;
                } else {
                    this.size = EMPTY;
                    this.stashSize = EMPTY;
                    this.zeroValue = null;
                    this.hasZeroValue = false;
                    return;
                }
            }
        }
    }

    public boolean containsValue(Object value, boolean identity) {
        V[] valueTable = this.valueTable;
        int i;
        int i2;
        if (value == null) {
            if (!this.hasZeroValue || this.zeroValue != null) {
                long[] keyTable = this.keyTable;
                i = this.capacity + this.stashSize;
                while (true) {
                    i2 = i - 1;
                    if (i <= 0) {
                        break;
                    } else if (keyTable[i2] != 0 && valueTable[i2] == null) {
                        return true;
                    } else {
                        i = i2;
                    }
                }
            } else {
                return true;
            }
        } else if (identity) {
            if (value != this.zeroValue) {
                i = this.capacity + this.stashSize;
                while (true) {
                    i2 = i - 1;
                    if (i <= 0) {
                        break;
                    } else if (valueTable[i2] == value) {
                        return true;
                    } else {
                        i = i2;
                    }
                }
            } else {
                return true;
            }
        } else if (!this.hasZeroValue || !value.equals(this.zeroValue)) {
            i = this.capacity + this.stashSize;
            while (true) {
                i2 = i - 1;
                if (i <= 0) {
                    break;
                } else if (value.equals(valueTable[i2])) {
                    return true;
                } else {
                    i = i2;
                }
            }
        } else {
            return true;
        }
        return false;
    }

    public boolean containsKey(long key) {
        if (key == 0) {
            return this.hasZeroValue;
        }
        if (this.keyTable[(int) (((long) this.mask) & key)] != key) {
            if (this.keyTable[hash2(key)] != key) {
                if (this.keyTable[hash3(key)] != key) {
                    return containsKeyStash(key);
                }
            }
        }
        return true;
    }

    private boolean containsKeyStash(long key) {
        long[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (keyTable[i] == key) {
                return true;
            }
            i++;
        }
        return false;
    }

    public long findKey(Object value, boolean identity, long notFound) {
        V[] valueTable = this.valueTable;
        int i;
        int i2;
        if (value == null) {
            if (this.hasZeroValue && this.zeroValue == null) {
                return 0;
            }
            long[] keyTable = this.keyTable;
            i = this.capacity + this.stashSize;
            while (true) {
                i2 = i - 1;
                if (i <= 0) {
                    return notFound;
                }
                if (keyTable[i2] != 0 && valueTable[i2] == null) {
                    return keyTable[i2];
                }
                i = i2;
            }
        } else if (identity) {
            if (value == this.zeroValue) {
                return 0;
            }
            i = this.capacity + this.stashSize;
            while (true) {
                i2 = i - 1;
                if (i <= 0) {
                    return notFound;
                }
                if (valueTable[i2] == value) {
                    return this.keyTable[i2];
                }
                i = i2;
            }
        } else if (this.hasZeroValue && value.equals(this.zeroValue)) {
            return 0;
        } else {
            i = this.capacity + this.stashSize;
            while (true) {
                i2 = i - 1;
                if (i <= 0) {
                    return notFound;
                }
                if (value.equals(valueTable[i2])) {
                    return this.keyTable[i2];
                }
                i = i2;
            }
        }
    }

    public void ensureCapacity(int additionalCapacity) {
        int sizeNeeded = this.size + additionalCapacity;
        if (sizeNeeded >= this.threshold) {
            resize(MathUtils.nextPowerOfTwo((int) (((float) sizeNeeded) / this.loadFactor)));
        }
    }

    private void resize(int newSize) {
        int oldEndIndex = this.capacity + this.stashSize;
        this.capacity = newSize;
        this.threshold = (int) (((float) newSize) * this.loadFactor);
        this.mask = newSize - 1;
        this.hashShift = 63 - Long.numberOfTrailingZeros((long) newSize);
        this.stashCapacity = Math.max(3, ((int) Math.ceil(Math.log((double) newSize))) * 2);
        this.pushIterations = Math.max(Math.min(newSize, 8), ((int) Math.sqrt((double) newSize)) / 8);
        long[] oldKeyTable = this.keyTable;
        V[] oldValueTable = this.valueTable;
        this.keyTable = new long[(this.stashCapacity + newSize)];
        this.valueTable = new Object[(this.stashCapacity + newSize)];
        int oldSize = this.size;
        this.size = this.hasZeroValue ? 1 : EMPTY;
        this.stashSize = EMPTY;
        if (oldSize > 0) {
            for (int i = EMPTY; i < oldEndIndex; i++) {
                long key = oldKeyTable[i];
                if (key != 0) {
                    putResize(key, oldValueTable[i]);
                }
            }
        }
    }

    private int hash2(long h) {
        h *= -1262997959;
        return (int) (((h >>> this.hashShift) ^ h) & ((long) this.mask));
    }

    private int hash3(long h) {
        h *= -825114047;
        return (int) (((h >>> this.hashShift) ^ h) & ((long) this.mask));
    }

    public int hashCode() {
        int h = EMPTY;
        if (this.hasZeroValue && this.zeroValue != null) {
            h = EMPTY + this.zeroValue.hashCode();
        }
        long[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            long key = keyTable[i];
            if (key != 0) {
                h += ((int) ((key >>> 32) ^ key)) * 31;
                V value = valueTable[i];
                if (value != null) {
                    h += value.hashCode();
                }
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LongMap)) {
            return false;
        }
        LongMap<V> other = (LongMap) obj;
        if (other.size != this.size) {
            return false;
        }
        if (other.hasZeroValue != this.hasZeroValue) {
            return false;
        }
        if (this.hasZeroValue) {
            if (other.zeroValue == null) {
                if (this.zeroValue != null) {
                    return false;
                }
            } else if (!other.zeroValue.equals(this.zeroValue)) {
                return false;
            }
        }
        long[] keyTable = this.keyTable;
        V[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            long key = keyTable[i];
            if (key != 0) {
                V value = valueTable[i];
                if (value == null) {
                    if (!other.containsKey(key) || other.get(key) != null) {
                        return false;
                    }
                } else if (!value.equals(other.get(key))) {
                    return false;
                }
            }
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String toString() {
        /*
        r12 = this;
        r10 = 0;
        r8 = 61;
        r7 = r12.size;
        if (r7 != 0) goto L_0x000b;
    L_0x0008:
        r7 = "[]";
    L_0x000a:
        return r7;
    L_0x000b:
        r0 = new com.badlogic.gdx.utils.StringBuilder;
        r7 = 32;
        r0.<init>(r7);
        r7 = 91;
        r0.append(r7);
        r3 = r12.keyTable;
        r6 = r12.valueTable;
        r1 = r3.length;
        r2 = r1;
    L_0x001d:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x005d;
    L_0x0021:
        r4 = r3[r1];
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x0029;
    L_0x0027:
        r2 = r1;
        goto L_0x001d;
    L_0x0029:
        r0.append(r4);
        r0.append(r8);
        r7 = r6[r1];
        r0.append(r7);
        r2 = r1;
    L_0x0035:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x0053;
    L_0x0039:
        r4 = r3[r1];
        r7 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r7 != 0) goto L_0x0041;
    L_0x003f:
        r2 = r1;
        goto L_0x0035;
    L_0x0041:
        r7 = ", ";
        r0.append(r7);
        r0.append(r4);
        r0.append(r8);
        r7 = r6[r1];
        r0.append(r7);
        r2 = r1;
        goto L_0x0035;
    L_0x0053:
        r7 = 93;
        r0.append(r7);
        r7 = r0.toString();
        goto L_0x000a;
    L_0x005d:
        r2 = r1;
        goto L_0x0035;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.LongMap.toString():java.lang.String");
    }

    public Iterator<Entry<V>> iterator() {
        return entries();
    }

    public Entries<V> entries() {
        if (this.entries1 == null) {
            this.entries1 = new Entries(this);
            this.entries2 = new Entries(this);
        }
        if (this.entries1.valid) {
            this.entries2.reset();
            this.entries2.valid = true;
            this.entries1.valid = false;
            return this.entries2;
        }
        this.entries1.reset();
        this.entries1.valid = true;
        this.entries2.valid = false;
        return this.entries1;
    }

    public Values<V> values() {
        if (this.values1 == null) {
            this.values1 = new Values(this);
            this.values2 = new Values(this);
        }
        if (this.values1.valid) {
            this.values2.reset();
            this.values2.valid = true;
            this.values1.valid = false;
            return this.values2;
        }
        this.values1.reset();
        this.values1.valid = true;
        this.values2.valid = false;
        return this.values1;
    }

    public Keys keys() {
        if (this.keys1 == null) {
            this.keys1 = new Keys(this);
            this.keys2 = new Keys(this);
        }
        if (this.keys1.valid) {
            this.keys2.reset();
            this.keys2.valid = true;
            this.keys1.valid = false;
            return this.keys2;
        }
        this.keys1.reset();
        this.keys1.valid = true;
        this.keys2.valid = false;
        return this.keys1;
    }
}
