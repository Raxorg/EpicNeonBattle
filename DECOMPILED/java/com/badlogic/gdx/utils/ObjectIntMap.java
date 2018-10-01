package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.compression.lzma.Encoder;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ObjectIntMap<K> implements Iterable<Entry<K>> {
    private static final int PRIME1 = -1105259343;
    private static final int PRIME2 = -1262997959;
    private static final int PRIME3 = -825114047;
    int capacity;
    private Entries entries1;
    private Entries entries2;
    private int hashShift;
    K[] keyTable;
    private Keys keys1;
    private Keys keys2;
    private float loadFactor;
    private int mask;
    private int pushIterations;
    public int size;
    private int stashCapacity;
    int stashSize;
    private int threshold;
    int[] valueTable;
    private Values values1;
    private Values values2;

    private static class MapIterator<K> {
        int currentIndex;
        public boolean hasNext;
        final ObjectIntMap<K> map;
        int nextIndex;
        boolean valid = true;

        public MapIterator(ObjectIntMap<K> map) {
            this.map = map;
            reset();
        }

        public void reset() {
            this.currentIndex = -1;
            this.nextIndex = -1;
            findNextIndex();
        }

        void findNextIndex() {
            this.hasNext = false;
            K[] keyTable = this.map.keyTable;
            int n = this.map.capacity + this.map.stashSize;
            do {
                int i = this.nextIndex + 1;
                this.nextIndex = i;
                if (i >= n) {
                    return;
                }
            } while (keyTable[this.nextIndex] == null);
            this.hasNext = true;
        }

        public void remove() {
            if (this.currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            if (this.currentIndex >= this.map.capacity) {
                this.map.removeStashIndex(this.currentIndex);
                this.nextIndex = this.currentIndex - 1;
                findNextIndex();
            } else {
                this.map.keyTable[this.currentIndex] = null;
            }
            this.currentIndex = -1;
            ObjectIntMap objectIntMap = this.map;
            objectIntMap.size--;
        }
    }

    public static class Entries<K> extends MapIterator<K> implements Iterable<Entry<K>>, Iterator<Entry<K>> {
        private Entry<K> entry = new Entry();

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Entries(ObjectIntMap<K> map) {
            super(map);
        }

        public Entry<K> next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                K[] keyTable = this.map.keyTable;
                this.entry.key = keyTable[this.nextIndex];
                this.entry.value = this.map.valueTable[this.nextIndex];
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

        public Entries<K> iterator() {
            return this;
        }

        public void remove() {
            super.remove();
        }
    }

    public static class Entry<K> {
        public K key;
        public int value;

        public String toString() {
            return this.key + "=" + this.value;
        }
    }

    public static class Keys<K> extends MapIterator<K> implements Iterable<K>, Iterator<K> {
        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Keys(ObjectIntMap<K> map) {
            super(map);
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.hasNext;
            }
            throw new GdxRuntimeException("#iterator() cannot be used nested.");
        }

        public K next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                K key = this.map.keyTable[this.nextIndex];
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return key;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public Keys<K> iterator() {
            return this;
        }

        public Array<K> toArray() {
            Array array = new Array(true, this.map.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }

        public Array<K> toArray(Array<K> array) {
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }

        public void remove() {
            super.remove();
        }
    }

    public static class Values extends MapIterator<Object> {
        public /* bridge */ /* synthetic */ void remove() {
            super.remove();
        }

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Values(ObjectIntMap<?> map) {
            super(map);
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.hasNext;
            }
            throw new GdxRuntimeException("#iterator() cannot be used nested.");
        }

        public int next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                int value = this.map.valueTable[this.nextIndex];
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return value;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public IntArray toArray() {
            IntArray array = new IntArray(true, this.map.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }
    }

    public ObjectIntMap() {
        this(32, 0.8f);
    }

    public ObjectIntMap(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    public ObjectIntMap(int initialCapacity, float loadFactor) {
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
            this.hashShift = 31 - Integer.numberOfTrailingZeros(this.capacity);
            this.stashCapacity = Math.max(3, ((int) Math.ceil(Math.log((double) this.capacity))) * 2);
            this.pushIterations = Math.max(Math.min(this.capacity, 8), ((int) Math.sqrt((double) this.capacity)) / 8);
            this.keyTable = new Object[(this.capacity + this.stashCapacity)];
            this.valueTable = new int[this.keyTable.length];
        }
    }

    public ObjectIntMap(ObjectIntMap<? extends K> map) {
        this(map.capacity, map.loadFactor);
        this.stashSize = map.stashSize;
        System.arraycopy(map.keyTable, 0, this.keyTable, 0, map.keyTable.length);
        System.arraycopy(map.valueTable, 0, this.valueTable, 0, map.valueTable.length);
        this.size = map.size;
    }

    public void put(K key, int value) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null.");
        }
        K[] keyTable = this.keyTable;
        int hashCode = key.hashCode();
        int index1 = hashCode & this.mask;
        K key1 = keyTable[index1];
        if (key.equals(key1)) {
            this.valueTable[index1] = value;
            return;
        }
        int index2 = hash2(hashCode);
        K key2 = keyTable[index2];
        if (key.equals(key2)) {
            this.valueTable[index2] = value;
            return;
        }
        int index3 = hash3(hashCode);
        K key3 = keyTable[index3];
        if (key.equals(key3)) {
            this.valueTable[index3] = value;
            return;
        }
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key.equals(keyTable[i])) {
                this.valueTable[i] = value;
                return;
            }
            i++;
        }
        int i2;
        if (key1 == null) {
            keyTable[index1] = key;
            this.valueTable[index1] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
        } else if (key2 == null) {
            keyTable[index2] = key;
            this.valueTable[index2] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
        } else if (key3 == null) {
            keyTable[index3] = key;
            this.valueTable[index3] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
        } else {
            push(key, value, index1, key1, index2, key2, index3, key3);
        }
    }

    public void putAll(ObjectIntMap<K> map) {
        Iterator it = map.entries().iterator();
        while (it.hasNext()) {
            Entry<K> entry = (Entry) it.next();
            put(entry.key, entry.value);
        }
    }

    private void putResize(K key, int value) {
        int hashCode = key.hashCode();
        int index1 = hashCode & this.mask;
        K key1 = this.keyTable[index1];
        int i;
        if (key1 == null) {
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
        int index2 = hash2(hashCode);
        K key2 = this.keyTable[index2];
        if (key2 == null) {
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
        int index3 = hash3(hashCode);
        K key3 = this.keyTable[index3];
        if (key3 == null) {
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

    private void push(K insertKey, int insertValue, int index1, K key1, int index2, K key2, int index3, K key3) {
        K[] keyTable = this.keyTable;
        int[] valueTable = this.valueTable;
        int mask = this.mask;
        int i = 0;
        int pushIterations = this.pushIterations;
        while (true) {
            K evictedKey;
            int evictedValue;
            switch (MathUtils.random(2)) {
                case Encoder.EMatchFinderTypeBT2 /*0*/:
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
            int hashCode = evictedKey.hashCode();
            index1 = hashCode & mask;
            key1 = keyTable[index1];
            int i2;
            if (key1 == null) {
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
            index2 = hash2(hashCode);
            key2 = keyTable[index2];
            if (key2 == null) {
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
            index3 = hash3(hashCode);
            key3 = keyTable[index3];
            if (key3 == null) {
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

    private void putStash(K key, int value) {
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

    public int get(K key, int defaultValue) {
        int hashCode = key.hashCode();
        int index = hashCode & this.mask;
        if (!key.equals(this.keyTable[index])) {
            index = hash2(hashCode);
            if (!key.equals(this.keyTable[index])) {
                index = hash3(hashCode);
                if (!key.equals(this.keyTable[index])) {
                    return getStash(key, defaultValue);
                }
            }
        }
        return this.valueTable[index];
    }

    private int getStash(K key, int defaultValue) {
        K[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key.equals(keyTable[i])) {
                return this.valueTable[i];
            }
            i++;
        }
        return defaultValue;
    }

    public int getAndIncrement(K key, int defaultValue, int increment) {
        int hashCode = key.hashCode();
        int index = hashCode & this.mask;
        if (!key.equals(this.keyTable[index])) {
            index = hash2(hashCode);
            if (!key.equals(this.keyTable[index])) {
                index = hash3(hashCode);
                if (!key.equals(this.keyTable[index])) {
                    return getAndIncrementStash(key, defaultValue, increment);
                }
            }
        }
        int value = this.valueTable[index];
        this.valueTable[index] = value + increment;
        return value;
    }

    private int getAndIncrementStash(K key, int defaultValue, int increment) {
        K[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key.equals(keyTable[i])) {
                int value = this.valueTable[i];
                this.valueTable[i] = value + increment;
                return value;
            }
            i++;
        }
        put(key, defaultValue + increment);
        return defaultValue;
    }

    public int remove(K key, int defaultValue) {
        int hashCode = key.hashCode();
        int index = hashCode & this.mask;
        if (key.equals(this.keyTable[index])) {
            this.keyTable[index] = null;
            int oldValue = this.valueTable[index];
            this.size--;
            return oldValue;
        }
        index = hash2(hashCode);
        if (key.equals(this.keyTable[index])) {
            this.keyTable[index] = null;
            oldValue = this.valueTable[index];
            this.size--;
            return oldValue;
        }
        index = hash3(hashCode);
        if (!key.equals(this.keyTable[index])) {
            return removeStash(key, defaultValue);
        }
        this.keyTable[index] = null;
        oldValue = this.valueTable[index];
        this.size--;
        return oldValue;
    }

    int removeStash(K key, int defaultValue) {
        K[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key.equals(keyTable[i])) {
                int oldValue = this.valueTable[i];
                removeStashIndex(i);
                this.size--;
                return oldValue;
            }
            i++;
        }
        return defaultValue;
    }

    void removeStashIndex(int index) {
        this.stashSize--;
        int lastIndex = this.capacity + this.stashSize;
        if (index < lastIndex) {
            this.keyTable[index] = this.keyTable[lastIndex];
            this.valueTable[index] = this.valueTable[lastIndex];
        }
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
        this.size = 0;
        resize(maximumCapacity);
    }

    public void clear() {
        if (this.size != 0) {
            K[] keyTable = this.keyTable;
            int i = this.capacity + this.stashSize;
            while (true) {
                int i2 = i - 1;
                if (i > 0) {
                    keyTable[i2] = null;
                    i = i2;
                } else {
                    this.size = 0;
                    this.stashSize = 0;
                    return;
                }
            }
        }
    }

    public boolean containsValue(int value) {
        K[] keyTable = this.keyTable;
        int[] valueTable = this.valueTable;
        int i = this.capacity + this.stashSize;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return false;
            }
            if (keyTable[i2] != null && valueTable[i2] == value) {
                return true;
            }
            i = i2;
        }
    }

    public boolean containsKey(K key) {
        int hashCode = key.hashCode();
        if (!key.equals(this.keyTable[hashCode & this.mask])) {
            if (!key.equals(this.keyTable[hash2(hashCode)])) {
                if (!key.equals(this.keyTable[hash3(hashCode)])) {
                    return containsKeyStash(key);
                }
            }
        }
        return true;
    }

    private boolean containsKeyStash(K key) {
        K[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key.equals(keyTable[i])) {
                return true;
            }
            i++;
        }
        return false;
    }

    public K findKey(int value) {
        K[] keyTable = this.keyTable;
        int[] valueTable = this.valueTable;
        int i = this.capacity + this.stashSize;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return null;
            }
            if (keyTable[i2] != null && valueTable[i2] == value) {
                return keyTable[i2];
            }
            i = i2;
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
        this.hashShift = 31 - Integer.numberOfTrailingZeros(newSize);
        this.stashCapacity = Math.max(3, ((int) Math.ceil(Math.log((double) newSize))) * 2);
        this.pushIterations = Math.max(Math.min(newSize, 8), ((int) Math.sqrt((double) newSize)) / 8);
        K[] oldKeyTable = this.keyTable;
        int[] oldValueTable = this.valueTable;
        this.keyTable = new Object[(this.stashCapacity + newSize)];
        this.valueTable = new int[(this.stashCapacity + newSize)];
        int oldSize = this.size;
        this.size = 0;
        this.stashSize = 0;
        if (oldSize > 0) {
            for (int i = 0; i < oldEndIndex; i++) {
                K key = oldKeyTable[i];
                if (key != null) {
                    putResize(key, oldValueTable[i]);
                }
            }
        }
    }

    private int hash2(int h) {
        h *= PRIME2;
        return ((h >>> this.hashShift) ^ h) & this.mask;
    }

    private int hash3(int h) {
        h *= PRIME3;
        return ((h >>> this.hashShift) ^ h) & this.mask;
    }

    public int hashCode() {
        int h = 0;
        K[] keyTable = this.keyTable;
        int[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = 0; i < n; i++) {
            K key = keyTable[i];
            if (key != null) {
                h = (h + (key.hashCode() * 31)) + valueTable[i];
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ObjectIntMap)) {
            return false;
        }
        ObjectIntMap<K> other = (ObjectIntMap) obj;
        if (other.size != this.size) {
            return false;
        }
        K[] keyTable = this.keyTable;
        int[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = 0; i < n; i++) {
            K key = keyTable[i];
            if (key != null) {
                int otherValue = other.get(key, 0);
                if (otherValue == 0 && !other.containsKey(key)) {
                    return false;
                }
                if (otherValue != valueTable[i]) {
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
        r8 = this;
        r7 = 61;
        r6 = r8.size;
        if (r6 != 0) goto L_0x0009;
    L_0x0006:
        r6 = "{}";
    L_0x0008:
        return r6;
    L_0x0009:
        r0 = new com.badlogic.gdx.utils.StringBuilder;
        r6 = 32;
        r0.<init>(r6);
        r6 = 123; // 0x7b float:1.72E-43 double:6.1E-322;
        r0.append(r6);
        r4 = r8.keyTable;
        r5 = r8.valueTable;
        r1 = r4.length;
        r2 = r1;
    L_0x001b:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x0057;
    L_0x001f:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x0025;
    L_0x0023:
        r2 = r1;
        goto L_0x001b;
    L_0x0025:
        r0.append(r3);
        r0.append(r7);
        r6 = r5[r1];
        r0.append(r6);
        r2 = r1;
    L_0x0031:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x004d;
    L_0x0035:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x003b;
    L_0x0039:
        r2 = r1;
        goto L_0x0031;
    L_0x003b:
        r6 = ", ";
        r0.append(r6);
        r0.append(r3);
        r0.append(r7);
        r6 = r5[r1];
        r0.append(r6);
        r2 = r1;
        goto L_0x0031;
    L_0x004d:
        r6 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        r0.append(r6);
        r6 = r0.toString();
        goto L_0x0008;
    L_0x0057:
        r2 = r1;
        goto L_0x0031;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.ObjectIntMap.toString():java.lang.String");
    }

    public Entries<K> iterator() {
        return entries();
    }

    public Entries<K> entries() {
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

    public Values values() {
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

    public Keys<K> keys() {
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
