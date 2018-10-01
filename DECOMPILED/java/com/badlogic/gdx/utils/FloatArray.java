package com.badlogic.gdx.utils;

import com.badlogic.gdx.math.MathUtils;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Arrays;

public class FloatArray {
    public float[] items;
    public boolean ordered;
    public int size;

    public FloatArray() {
        this(true, 16);
    }

    public FloatArray(int capacity) {
        this(true, capacity);
    }

    public FloatArray(boolean ordered, int capacity) {
        this.ordered = ordered;
        this.items = new float[capacity];
    }

    public FloatArray(FloatArray array) {
        this.ordered = array.ordered;
        this.size = array.size;
        this.items = new float[this.size];
        System.arraycopy(array.items, 0, this.items, 0, this.size);
    }

    public FloatArray(float[] array) {
        this(true, array, 0, array.length);
    }

    public FloatArray(boolean ordered, float[] array, int startIndex, int count) {
        this(ordered, count);
        this.size = count;
        System.arraycopy(array, startIndex, this.items, 0, count);
    }

    public void add(float value) {
        float[] items = this.items;
        if (this.size == items.length) {
            items = resize(Math.max(8, (int) (((float) this.size) * 1.75f)));
        }
        int i = this.size;
        this.size = i + 1;
        items[i] = value;
    }

    public void addAll(FloatArray array) {
        addAll(array, 0, array.size);
    }

    public void addAll(FloatArray array, int offset, int length) {
        if (offset + length > array.size) {
            throw new IllegalArgumentException("offset + length must be <= size: " + offset + " + " + length + " <= " + array.size);
        }
        addAll(array.items, offset, length);
    }

    public void addAll(float... array) {
        addAll(array, 0, array.length);
    }

    public void addAll(float[] array, int offset, int length) {
        float[] items = this.items;
        int sizeNeeded = this.size + length;
        if (sizeNeeded > items.length) {
            items = resize(Math.max(8, (int) (((float) sizeNeeded) * 1.75f)));
        }
        System.arraycopy(array, offset, items, this.size, length);
        this.size += length;
    }

    public float get(int index) {
        if (index < this.size) {
            return this.items[index];
        }
        throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
    }

    public void set(int index, float value) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
        }
        this.items[index] = value;
    }

    public void incr(int index, float value) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
        }
        float[] fArr = this.items;
        fArr[index] = fArr[index] + value;
    }

    public void mul(int index, float value) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
        }
        float[] fArr = this.items;
        fArr[index] = fArr[index] * value;
    }

    public void insert(int index, float value) {
        if (index > this.size) {
            throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + this.size);
        }
        float[] items = this.items;
        if (this.size == items.length) {
            items = resize(Math.max(8, (int) (((float) this.size) * 1.75f)));
        }
        if (this.ordered) {
            System.arraycopy(items, index, items, index + 1, this.size - index);
        } else {
            items[this.size] = items[index];
        }
        this.size++;
        items[index] = value;
    }

    public void swap(int first, int second) {
        if (first >= this.size) {
            throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + this.size);
        } else if (second >= this.size) {
            throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + this.size);
        } else {
            float[] items = this.items;
            float firstValue = items[first];
            items[first] = items[second];
            items[second] = firstValue;
        }
    }

    public boolean contains(float value) {
        int i = this.size - 1;
        float[] items = this.items;
        int i2 = i;
        while (i2 >= 0) {
            i = i2 - 1;
            if (items[i2] == value) {
                return true;
            }
            i2 = i;
        }
        i = i2;
        return false;
    }

    public int indexOf(float value) {
        float[] items = this.items;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            if (items[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public int lastIndexOf(char value) {
        float[] items = this.items;
        for (int i = this.size - 1; i >= 0; i--) {
            if (items[i] == ((float) value)) {
                return i;
            }
        }
        return -1;
    }

    public boolean removeValue(float value) {
        float[] items = this.items;
        int n = this.size;
        for (int i = 0; i < n; i++) {
            if (items[i] == value) {
                removeIndex(i);
                return true;
            }
        }
        return false;
    }

    public float removeIndex(int index) {
        if (index >= this.size) {
            throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + this.size);
        }
        float[] items = this.items;
        float value = items[index];
        this.size--;
        if (this.ordered) {
            System.arraycopy(items, index + 1, items, index, this.size - index);
        } else {
            items[index] = items[this.size];
        }
        return value;
    }

    public void removeRange(int start, int end) {
        if (end >= this.size) {
            throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + this.size);
        } else if (start > end) {
            throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
        } else {
            float[] items = this.items;
            int count = (end - start) + 1;
            if (this.ordered) {
                System.arraycopy(items, start + count, items, start, this.size - (start + count));
            } else {
                int lastIndex = this.size - 1;
                for (int i = 0; i < count; i++) {
                    items[start + i] = items[lastIndex - i];
                }
            }
            this.size -= count;
        }
    }

    public boolean removeAll(FloatArray array) {
        int size = this.size;
        int startSize = size;
        float[] items = this.items;
        int n = array.size;
        for (int i = 0; i < n; i++) {
            float item = array.get(i);
            for (int ii = 0; ii < size; ii++) {
                if (item == items[ii]) {
                    removeIndex(ii);
                    size--;
                    break;
                }
            }
        }
        return size != startSize;
    }

    public float pop() {
        float[] fArr = this.items;
        int i = this.size - 1;
        this.size = i;
        return fArr[i];
    }

    public float peek() {
        return this.items[this.size - 1];
    }

    public float first() {
        if (this.size != 0) {
            return this.items[0];
        }
        throw new IllegalStateException("Array is empty.");
    }

    public void clear() {
        this.size = 0;
    }

    public float[] shrink() {
        if (this.items.length != this.size) {
            resize(this.size);
        }
        return this.items;
    }

    public float[] ensureCapacity(int additionalCapacity) {
        int sizeNeeded = this.size + additionalCapacity;
        if (sizeNeeded > this.items.length) {
            resize(Math.max(8, sizeNeeded));
        }
        return this.items;
    }

    protected float[] resize(int newSize) {
        float[] newItems = new float[newSize];
        System.arraycopy(this.items, 0, newItems, 0, Math.min(this.size, newItems.length));
        this.items = newItems;
        return newItems;
    }

    public void sort() {
        Arrays.sort(this.items, 0, this.size);
    }

    public void reverse() {
        float[] items = this.items;
        int lastIndex = this.size - 1;
        int n = this.size / 2;
        for (int i = 0; i < n; i++) {
            int ii = lastIndex - i;
            float temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    public void shuffle() {
        float[] items = this.items;
        for (int i = this.size - 1; i >= 0; i--) {
            int ii = MathUtils.random(i);
            float temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }
    }

    public void truncate(int newSize) {
        if (this.size > newSize) {
            this.size = newSize;
        }
    }

    public float random() {
        if (this.size == 0) {
            return 0.0f;
        }
        return this.items[MathUtils.random(0, this.size - 1)];
    }

    public float[] toArray() {
        float[] array = new float[this.size];
        System.arraycopy(this.items, 0, array, 0, this.size);
        return array;
    }

    public int hashCode() {
        if (!this.ordered) {
            return super.hashCode();
        }
        float[] items = this.items;
        int h = 1;
        for (int i = 0; i < this.size; i++) {
            h = (h * 31) + Float.floatToIntBits(items[i]);
        }
        return h;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!this.ordered) {
            return false;
        }
        if (!(object instanceof FloatArray)) {
            return false;
        }
        FloatArray array = (FloatArray) object;
        if (!array.ordered) {
            return false;
        }
        int n = this.size;
        if (n != array.size) {
            return false;
        }
        float[] items1 = this.items;
        float[] items2 = array.items;
        for (int i = 0; i < n; i++) {
            if (items1[i] != items2[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object, float epsilon) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof FloatArray)) {
            return false;
        }
        FloatArray array = (FloatArray) object;
        int n = this.size;
        if (n != array.size) {
            return false;
        }
        if (!this.ordered) {
            return false;
        }
        if (!array.ordered) {
            return false;
        }
        float[] items1 = this.items;
        float[] items2 = array.items;
        for (int i = 0; i < n; i++) {
            if (Math.abs(items1[i] - items2[i]) > epsilon) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        if (this.size == 0) {
            return "[]";
        }
        float[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('[');
        buffer.append(items[0]);
        for (int i = 1; i < this.size; i++) {
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    public String toString(String separator) {
        if (this.size == 0) {
            return BuildConfig.VERSION_NAME;
        }
        float[] items = this.items;
        StringBuilder buffer = new StringBuilder(32);
        buffer.append(items[0]);
        for (int i = 1; i < this.size; i++) {
            buffer.append(separator);
            buffer.append(items[i]);
        }
        return buffer.toString();
    }

    public static FloatArray with(float... array) {
        return new FloatArray(array);
    }
}
