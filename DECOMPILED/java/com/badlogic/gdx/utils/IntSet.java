package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.NoSuchElementException;

public class IntSet {
    private static final int EMPTY = 0;
    private static final int PRIME1 = -1105259343;
    private static final int PRIME2 = -1262997959;
    private static final int PRIME3 = -825114047;
    int capacity;
    boolean hasZeroValue;
    private int hashShift;
    private IntSetIterator iterator1;
    private IntSetIterator iterator2;
    int[] keyTable;
    private float loadFactor;
    private int mask;
    private int pushIterations;
    public int size;
    private int stashCapacity;
    int stashSize;
    private int threshold;

    public static class IntSetIterator {
        static final int INDEX_ILLEGAL = -2;
        static final int INDEX_ZERO = -1;
        int currentIndex;
        public boolean hasNext;
        int nextIndex;
        final IntSet set;
        boolean valid = true;

        public IntSetIterator(IntSet set) {
            this.set = set;
            reset();
        }

        public void reset() {
            this.currentIndex = INDEX_ILLEGAL;
            this.nextIndex = INDEX_ZERO;
            if (this.set.hasZeroValue) {
                this.hasNext = true;
            } else {
                findNextIndex();
            }
        }

        void findNextIndex() {
            this.hasNext = false;
            int[] keyTable = this.set.keyTable;
            int n = this.set.capacity + this.set.stashSize;
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
            if (this.currentIndex == INDEX_ZERO && this.set.hasZeroValue) {
                this.set.hasZeroValue = false;
            } else if (this.currentIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            } else if (this.currentIndex >= this.set.capacity) {
                this.set.removeStashIndex(this.currentIndex);
                this.nextIndex = this.currentIndex + INDEX_ZERO;
                findNextIndex();
            } else {
                this.set.keyTable[this.currentIndex] = IntSet.EMPTY;
            }
            this.currentIndex = INDEX_ILLEGAL;
            IntSet intSet = this.set;
            intSet.size += INDEX_ZERO;
        }

        public int next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                int key = this.nextIndex == INDEX_ZERO ? IntSet.EMPTY : this.set.keyTable[this.nextIndex];
                this.currentIndex = this.nextIndex;
                findNextIndex();
                return key;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public IntArray toArray() {
            IntArray array = new IntArray(true, this.set.size);
            while (this.hasNext) {
                array.add(next());
            }
            return array;
        }
    }

    public IntSet() {
        this(32, 0.8f);
    }

    public IntSet(int initialCapacity) {
        this(initialCapacity, 0.8f);
    }

    public IntSet(int initialCapacity, float loadFactor) {
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
        }
    }

    public IntSet(IntSet map) {
        this(map.capacity, map.loadFactor);
        this.stashSize = map.stashSize;
        System.arraycopy(map.keyTable, EMPTY, this.keyTable, EMPTY, map.keyTable.length);
        this.size = map.size;
        this.hasZeroValue = map.hasZeroValue;
    }

    public boolean add(int key) {
        if (key != 0) {
            int[] keyTable = this.keyTable;
            int index1 = key & this.mask;
            int key1 = keyTable[index1];
            if (key1 == key) {
                return false;
            }
            int index2 = hash2(key);
            int key2 = keyTable[index2];
            if (key2 == key) {
                return false;
            }
            int index3 = hash3(key);
            int key3 = keyTable[index3];
            if (key3 == key) {
                return false;
            }
            int i = this.capacity;
            int n = i + this.stashSize;
            while (i < n) {
                if (keyTable[i] == key) {
                    return false;
                }
                i++;
            }
            int i2;
            if (key1 == 0) {
                keyTable[index1] = key;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                }
                return true;
            } else if (key2 == 0) {
                keyTable[index2] = key;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                }
                return true;
            } else if (key3 == 0) {
                keyTable[index3] = key;
                i2 = this.size;
                this.size = i2 + 1;
                if (i2 >= this.threshold) {
                    resize(this.capacity << 1);
                }
                return true;
            } else {
                push(key, index1, key1, index2, key2, index3, key3);
                return true;
            }
        } else if (this.hasZeroValue) {
            return false;
        } else {
            this.hasZeroValue = true;
            this.size++;
            return true;
        }
    }

    public void addAll(IntArray array) {
        addAll(array, (int) EMPTY, array.size);
    }

    public void addAll(IntArray array, int offset, int length) {
        if (offset + length > array.size) {
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
        }
        addAll(array.items, offset, length);
    }

    public void addAll(int... array) {
        addAll(array, (int) EMPTY, array.length);
    }

    public void addAll(int[] array, int offset, int length) {
        ensureCapacity(length);
        int i = offset;
        int n = i + length;
        while (i < n) {
            add(array[i]);
            i++;
        }
    }

    public void addAll(IntSet set) {
        ensureCapacity(set.size);
        IntSetIterator iterator = set.iterator();
        while (iterator.hasNext) {
            add(iterator.next());
        }
    }

    private void addResize(int key) {
        if (key == 0) {
            this.hasZeroValue = true;
            return;
        }
        int index1 = key & this.mask;
        int key1 = this.keyTable[index1];
        int i;
        if (key1 == 0) {
            this.keyTable[index1] = key;
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
            i = this.size;
            this.size = i + 1;
            if (i >= this.threshold) {
                resize(this.capacity << 1);
                return;
            }
            return;
        }
        push(key, index1, key1, index2, key2, index3, key3);
    }

    private void push(int insertKey, int index1, int key1, int index2, int key2, int index3, int key3) {
        int[] keyTable = this.keyTable;
        int mask = this.mask;
        int i = EMPTY;
        int pushIterations = this.pushIterations;
        while (true) {
            int evictedKey;
            switch (MathUtils.random(2)) {
                case EMPTY /*0*/:
                    evictedKey = key1;
                    keyTable[index1] = insertKey;
                    break;
                case BuildConfig.VERSION_CODE /*1*/:
                    evictedKey = key2;
                    keyTable[index2] = insertKey;
                    break;
                default:
                    evictedKey = key3;
                    keyTable[index3] = insertKey;
                    break;
            }
            index1 = evictedKey & mask;
            key1 = keyTable[index1];
            int i2;
            if (key1 == 0) {
                keyTable[index1] = evictedKey;
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
                addStash(evictedKey);
                return;
            }
            insertKey = evictedKey;
        }
    }

    private void addStash(int key) {
        if (this.stashSize == this.stashCapacity) {
            resize(this.capacity << 1);
            add(key);
            return;
        }
        this.keyTable[this.capacity + this.stashSize] = key;
        this.stashSize++;
        this.size++;
    }

    public boolean remove(int key) {
        if (key != 0) {
            int index = key & this.mask;
            if (this.keyTable[index] == key) {
                this.keyTable[index] = EMPTY;
                this.size--;
                return true;
            }
            index = hash2(key);
            if (this.keyTable[index] == key) {
                this.keyTable[index] = EMPTY;
                this.size--;
                return true;
            }
            index = hash3(key);
            if (this.keyTable[index] != key) {
                return removeStash(key);
            }
            this.keyTable[index] = EMPTY;
            this.size--;
            return true;
        } else if (!this.hasZeroValue) {
            return false;
        } else {
            this.hasZeroValue = false;
            this.size--;
            return true;
        }
    }

    boolean removeStash(int key) {
        int[] keyTable = this.keyTable;
        int i = this.capacity;
        int n = i + this.stashSize;
        while (i < n) {
            if (keyTable[i] == key) {
                removeStashIndex(i);
                this.size--;
                return true;
            }
            i++;
        }
        return false;
    }

    void removeStashIndex(int index) {
        this.stashSize--;
        int lastIndex = this.capacity + this.stashSize;
        if (index < lastIndex) {
            this.keyTable[index] = this.keyTable[lastIndex];
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
                    this.size = EMPTY;
                    this.stashSize = EMPTY;
                    this.hasZeroValue = false;
                    return;
                }
            }
        }
    }

    public boolean contains(int key) {
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
            if (keyTable[i] == key) {
                return true;
            }
            i++;
        }
        return false;
    }

    public int first() {
        if (this.hasZeroValue) {
            return EMPTY;
        }
        int[] keyTable = this.keyTable;
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            if (keyTable[i] != 0) {
                return keyTable[i];
            }
        }
        throw new IllegalStateException("IntSet is empty.");
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
        this.keyTable = new int[(this.stashCapacity + newSize)];
        int oldSize = this.size;
        this.size = this.hasZeroValue ? 1 : EMPTY;
        this.stashSize = EMPTY;
        if (oldSize > 0) {
            for (int i = EMPTY; i < oldEndIndex; i++) {
                int key = oldKeyTable[i];
                if (key != 0) {
                    addResize(key);
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
        int n = this.capacity + this.stashSize;
        for (int i = EMPTY; i < n; i++) {
            if (this.keyTable[i] != 0) {
                h += this.keyTable[i];
            }
        }
        return h;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IntSet)) {
            return false;
        }
        IntSet other = (IntSet) obj;
        if (other.size != this.size || other.hasZeroValue != this.hasZeroValue) {
            return false;
        }
        int i = EMPTY;
        int n = this.capacity + this.stashSize;
        while (i < n) {
            if (this.keyTable[i] != 0 && !other.contains(this.keyTable[i])) {
                return false;
            }
            i++;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.lang.String toString() {
        /*
        r6 = this;
        r5 = r6.size;
        if (r5 != 0) goto L_0x0007;
    L_0x0004:
        r5 = "[]";
    L_0x0006:
        return r5;
    L_0x0007:
        r0 = new com.badlogic.gdx.utils.StringBuilder;
        r5 = 32;
        r0.<init>(r5);
        r5 = 91;
        r0.append(r5);
        r4 = r6.keyTable;
        r1 = r4.length;
        r5 = r6.hasZeroValue;
        if (r5 == 0) goto L_0x004f;
    L_0x001a:
        r5 = "0";
        r0.append(r5);
        r2 = r1;
    L_0x0020:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x0043;
    L_0x0024:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x0039;
    L_0x0028:
        r2 = r1;
        goto L_0x0020;
    L_0x002a:
        r1 = r2 + -1;
        if (r2 <= 0) goto L_0x004d;
    L_0x002e:
        r3 = r4[r1];
        if (r3 != 0) goto L_0x0034;
    L_0x0032:
        r2 = r1;
        goto L_0x002a;
    L_0x0034:
        r0.append(r3);
        r2 = r1;
        goto L_0x0020;
    L_0x0039:
        r5 = ", ";
        r0.append(r5);
        r0.append(r3);
        r2 = r1;
        goto L_0x0020;
    L_0x0043:
        r5 = 93;
        r0.append(r5);
        r5 = r0.toString();
        goto L_0x0006;
    L_0x004d:
        r2 = r1;
        goto L_0x0020;
    L_0x004f:
        r2 = r1;
        goto L_0x002a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.utils.IntSet.toString():java.lang.String");
    }

    public IntSetIterator iterator() {
        if (this.iterator1 == null) {
            this.iterator1 = new IntSetIterator(this);
            this.iterator2 = new IntSetIterator(this);
        }
        if (this.iterator1.valid) {
            this.iterator2.reset();
            this.iterator2.valid = true;
            this.iterator1.valid = false;
            return this.iterator2;
        }
        this.iterator1.reset();
        this.iterator1.valid = true;
        this.iterator2.valid = false;
        return this.iterator1;
    }

    public static IntSet with(int... array) {
        IntSet set = new IntSet();
        set.addAll(array);
        return set;
    }
}
