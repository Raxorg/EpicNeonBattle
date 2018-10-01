package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntFloatMap implements Iterable<Entry> {
    private static final int EMPTY = 0;
    private static final int PRIME1 = -1105259343;
    private static final int PRIME2 = -1262997959;
    private static final int PRIME3 = -825114047;
    int capacity;
    private Entries entries1;
    private Entries entries2;
    boolean hasZeroValue;
    private int hashShift;
    int[] keyTable;
    private Keys keys1;
    private Keys keys2;
    private float loadFactor;
    private int mask;
    private int pushIterations;
    public int size;
    private int stashCapacity;
    int stashSize;
    private int threshold;
    float[] valueTable;
    private Values values1;
    private Values values2;
    float zeroValue;

    private static class MapIterator {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;
        int currentIndex;
        public boolean hasNext;
        final IntFloatMap map;
        int nextIndex;
        boolean valid = true;

        public MapIterator(IntFloatMap map) {
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
            int[] keyTable = this.map.keyTable;
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
                this.map.hasZeroValue = false;
            } else if (this.currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (this.currentIndex >= this.map.capacity) {
                this.map.removeStashIndex(this.currentIndex);
                this.nextIndex = this.currentIndex + INDEX_ZERO;
                findNextIndex();
            } else {
                this.map.keyTable[this.currentIndex] = IntFloatMap.EMPTY;
            }
            this.currentIndex = INDEX_ILLEGAL;
            IntFloatMap intFloatMap = this.map;
            intFloatMap.size += INDEX_ZERO;
        }
    }

    public static class Entries extends MapIterator implements Iterable<Entry>, Iterator<Entry> {
        private Entry entry = new Entry();

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Entries(IntFloatMap map) {
            super(map);
        }

        public Entry next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                int[] keyTable = this.map.keyTable;
                if (this.nextIndex == -1) {
                    this.entry.key = IntFloatMap.EMPTY;
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

        public Iterator<Entry> iterator() {
            return this;
        }

        public void remove() {
            super.remove();
        }
    }

    public static class Entry {
        public int key;
        public float value;

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

        public Keys(IntFloatMap map) {
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
                int key = this.nextIndex == -1 ? IntFloatMap.EMPTY : this.map.keyTable[this.nextIndex];
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return key;
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

    public static class Values extends MapIterator {
        public /* bridge */ /* synthetic */ void remove() {
            super.remove();
        }

        public /* bridge */ /* synthetic */ void reset() {
            super.reset();
        }

        public Values(IntFloatMap map) {
            super(map);
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.hasNext;
            }
            throw new GdxRuntimeException("#iterator() cannot be used nested.");
        }

        public float next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                float value;
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

        public FloatArray toArray() {
            FloatArray array = new FloatArray(true, this.map.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }
    }

    public IntFloatMap() {
        this(32, 0.8f);
    }

    public IntFloatMap(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    public IntFloatMap(int initialCapacity, float loadFactor) {
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
            this.keyTable = new int[(this.capacity + this.stashCapacity)];
            this.valueTable = new float[this.keyTable.length];
        }
    }

    public IntFloatMap(IntFloatMap map) {
        this(map.capacity, map.loadFactor);
        this.stashSize = map.stashSize;
        System.arraycopy(map.keyTable, EMPTY, this.keyTable, EMPTY, map.keyTable.length);
        System.arraycopy(map.valueTable, EMPTY, this.valueTable, EMPTY, map.valueTable.length);
        this.size = map.size;
        this.zeroValue = map.zeroValue;
        this.hasZeroValue = map.hasZeroValue;
    }

    public void put(int key, float value) {
        if (key == 0) {
            this.zeroValue = value;
            if (!this.hasZeroValue) {
                this.hasZeroValue = true;
                this.size++;
                return;
            }
            return;
        }
        int[] keyTable = this.keyTable;
        int index1 = key & this.mask;
        int key1 = keyTable[index1];
        if (key == key1) {
            this.valueTable[index1] = value;
            return;
        }
        int index2 = hash2(key);
        int key2 = keyTable[index2];
        if (key == key2) {
            this.valueTable[index2] = value;
            return;
        }
        int index3 = hash3(key);
        int key3 = keyTable[index3];
        if (key == key3) {
            this.valueTable[index3] = value;
            return;
        }
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key == keyTable[i]) {
                this.valueTable[i] = value;
                return;
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
        } else if (key2 == 0) {
            keyTable[index2] = key;
            this.valueTable[index2] = value;
            i2 = this.size;
            this.size = i2 + 1;
            if (i2 >= this.threshold) {
                resize(this.capacity << 1);
            }
        } else if (key3 == 0) {
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

    public void putAll(IntFloatMap map) {
        Iterator it = map.entries().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            put(entry.key, entry.value);
        }
    }

    private void putResize(int key, float value) {
        if (key == 0) {
            this.zeroValue = value;
            this.hasZeroValue = true;
            return;
        }
        int index1 = key & this.mask;
        int key1 = this.keyTable[index1];
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
        int key2 = this.keyTable[index2];
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
        int key3 = this.keyTable[index3];
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

    private void push(int insertKey, float insertValue, int index1, int key1, int index2, int key2, int index3, int key3) {
        int[] keyTable = this.keyTable;
        float[] valueTable = this.valueTable;
        int mask = this.mask;
        int i = EMPTY;
        int pushIterations = this.pushIterations;
        while (true) {
            int evictedKey;
            float evictedValue;
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
            index1 = evictedKey & mask;
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

    private void putStash(int key, float value) {
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

    public float get(int key, float defaultValue) {
        if (key != 0) {
            int index = key & this.mask;
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

    private float getStash(int key, float defaultValue) {
        int[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key == keyTable[i]) {
                return this.valueTable[i];
            }
            i++;
        }
        return defaultValue;
    }

    public float getAndIncrement(int key, float defaultValue, float increment) {
        float value;
        if (key != 0) {
            int index = key & this.mask;
            if (key != this.keyTable[index]) {
                index = hash2(key);
                if (key != this.keyTable[index]) {
                    index = hash3(key);
                    if (key != this.keyTable[index]) {
                        return getAndIncrementStash(key, defaultValue, increment);
                    }
                }
            }
            value = this.valueTable[index];
            this.valueTable[index] = value + increment;
            return value;
        } else if (this.hasZeroValue) {
            value = this.zeroValue;
            this.zeroValue += increment;
            return value;
        } else {
            this.hasZeroValue = true;
            this.zeroValue = defaultValue + increment;
            this.size++;
            return defaultValue;
        }
    }

    private float getAndIncrementStash(int key, float defaultValue, float increment) {
        int[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key == keyTable[i]) {
                float value = this.valueTable[i];
                this.valueTable[i] = value + increment;
                return value;
            }
            i++;
        }
        put(key, defaultValue + increment);
        return defaultValue;
    }

    public float remove(int key, float defaultValue) {
        if (key != 0) {
            int index = key & this.mask;
            float oldValue;
            if (key == this.keyTable[index]) {
                this.keyTable[index] = EMPTY;
                oldValue = this.valueTable[index];
                this.size--;
                return oldValue;
            }
            index = hash2(key);
            if (key == this.keyTable[index]) {
                this.keyTable[index] = EMPTY;
                oldValue = this.valueTable[index];
                this.size--;
                return oldValue;
            }
            index = hash3(key);
            if (key != this.keyTable[index]) {
                return removeStash(key, defaultValue);
            }
            this.keyTable[index] = EMPTY;
            oldValue = this.valueTable[index];
            this.size--;
            return oldValue;
        } else if (!this.hasZeroValue) {
            return defaultValue;
        } else {
            this.hasZeroValue = false;
            this.size--;
            return this.zeroValue;
        }
    }

    float removeStash(int key, float defaultValue) {
        int[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key == keyTable[i]) {
                float oldValue = this.valueTable[i];
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
        this.hasZeroValue = false;
        this.size = EMPTY;
        resize(maximumCapacity);
    }

    public void clear() {
        if (this.size != 0) {
            int[] keyTable = this.keyTable;
            int i = this.capacity + this.stashSize;
            while (true) {
                int i2 = i - 1;
                if (i > 0) {
                    keyTable[i2] = EMPTY;
                    i = i2;
                } else {
                    this.hasZeroValue = false;
                    this.size = EMPTY;
                    this.stashSize = EMPTY;
                    return;
                }
            }
        }
    }

    public boolean containsValue(float value) {
        if (this.hasZeroValue && this.zeroValue == value) {
            return true;
        }
        int[] keyTable = this.keyTable;
        float[] valueTable = this.valueTable;
        int i = this.capacity + this.stashSize;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return false;
            }
            if (keyTable[i2] != 0 && valueTable[i2] == value) {
                return true;
            }
            i = i2;
        }
    }

    public boolean containsValue(float value, float epsilon) {
        if (this.hasZeroValue && Math.abs(this.zeroValue - value) <= epsilon) {
            return true;
        }
        float[] valueTable = this.valueTable;
        int i = this.capacity + this.stashSize;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return false;
            }
            if (Math.abs(valueTable[i2] - value) <= epsilon) {
                return true;
            }
            i = i2;
        }
    }

    public boolean containsKey(int key) {
        if (key == 0) {
            return this.hasZeroValue;
        }
        if (this.keyTable[key & this.mask] != key) {
            if (this.keyTable[hash2(key)] != key) {
                if (this.keyTable[hash3(key)] != key) {
                    return containsKeyStash(key);
                }
            }
        }
        return true;
    }

    private boolean containsKeyStash(int key) {
        int[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (key == keyTable[i]) {
                return true;
            }
            i++;
        }
        return false;
    }

    public int findKey(float value, int notFound) {
        if (this.hasZeroValue && this.zeroValue == value) {
            return EMPTY;
        }
        int[] keyTable = this.keyTable;
        float[] valueTable = this.valueTable;
        int i = this.capacity + this.stashSize;
        while (true) {
            int i2 = i - 1;
            if (i <= 0) {
                return notFound;
            }
            if (keyTable[i2] != 0 && valueTable[i2] == value) {
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
        int[] oldKeyTable = this.keyTable;
        float[] oldValueTable = this.valueTable;
        this.keyTable = new int[(this.stashCapacity + newSize)];
        this.valueTable = new float[(this.stashCapacity + newSize)];
        int oldSize = this.size;
        this.size = this.hasZeroValue ? 1 : EMPTY;
        this.stashSize = EMPTY;
        if (oldSize > 0) {
            for (int i = EMPTY; i < oldEndIndex; i++) {
                int key = oldKeyTable[i];
                if (key != 0) {
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
        int h = EMPTY;
        if (this.hasZeroValue) {
            h = EMPTY + Float.floatToIntBits(this.zeroValue);
        }
        int[] keyTable = this.keyTable;
        float[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            int key = keyTable[i];
            if (key != 0) {
                h = (h + (key * 31)) + Float.floatToIntBits(valueTable[i]);
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IntFloatMap)) {
            return false;
        }
        IntFloatMap other = (IntFloatMap) obj;
        if (other.size != this.size) {
            return false;
        }
        if (other.hasZeroValue != this.hasZeroValue) {
            return false;
        }
        if (this.hasZeroValue && other.zeroValue != this.zeroValue) {
            return false;
        }
        int[] keyTable = this.keyTable;
        float[] valueTable = this.valueTable;
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            int key = keyTable[i];
            if (key != 0) {
                float otherValue = other.get(key, 0.0f);
                if (otherValue == 0.0f && !other.containsKey(key)) {
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
        r6 = r8.hasZeroValue;
        if (r6 == 0) goto L_0x0068;
    L_0x001e:
        r6 = "0=";
        r0.append(r6);
        r6 = r8.zeroValue;
        r0.append(r6);
        r2 = r1;
    L_0x0029:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x005c;
    L_0x002d:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x004a;
    L_0x0031:
        r2 = r1;
        goto L_0x0029;
    L_0x0033:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x0066;
    L_0x0037:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x003d;
    L_0x003b:
        r2 = r1;
        goto L_0x0033;
    L_0x003d:
        r0.append(r3);
        r0.append(r7);
        r6 = r5[r1];
        r0.append(r6);
        r2 = r1;
        goto L_0x0029;
    L_0x004a:
        r6 = ", ";
        r0.append(r6);
        r0.append(r3);
        r0.append(r7);
        r6 = r5[r1];
        r0.append(r6);
        r2 = r1;
        goto L_0x0029;
    L_0x005c:
        r6 = 125; // 0x7d float:1.75E-43 double:6.2E-322;
        r0.append(r6);
        r6 = r0.toString();
        goto L_0x0008;
    L_0x0066:
        r2 = r1;
        goto L_0x0029;
    L_0x0068:
        r2 = r1;
        goto L_0x0033;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.IntFloatMap.toString():java.lang.String");
    }

    public Iterator<Entry> iterator() {
        return entries();
    }

    public Entries entries() {
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
