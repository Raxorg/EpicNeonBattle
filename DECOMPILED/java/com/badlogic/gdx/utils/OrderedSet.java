package com.badlogic.gdx.utils;

import com.badlogic.gdx.utils.ObjectSet.ObjectSetIterator;
import java.util.NoSuchElementException;

public class OrderedSet<T> extends ObjectSet<T> {
    final Array<T> items;
    OrderedSetIterator iterator1;
    OrderedSetIterator iterator2;

    public static class OrderedSetIterator<T> extends ObjectSetIterator<T> {
        private Array<T> items;

        public OrderedSetIterator(OrderedSet<T> set) {
            super(set);
            this.items = set.items;
        }

        public void reset() {
            boolean z = false;
            this.nextIndex = 0;
            if (this.set.size > 0) {
                z = true;
            }
            this.hasNext = z;
        }

        public T next() {
            if (!this.hasNext) {
                throw new NoSuchElementException();
            } else if (this.valid) {
                T key = this.items.get(this.nextIndex);
                this.nextIndex++;
                this.hasNext = this.nextIndex < this.set.size;
                return key;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public void remove() {
            if (this.nextIndex < 0) {
                throw new IllegalStateException("next must be called before remove.");
            }
            this.nextIndex--;
            this.set.remove(this.items.get(this.nextIndex));
        }
    }

    public OrderedSet() {
        this.items = new Array();
    }

    public OrderedSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
        this.items = new Array(this.capacity);
    }

    public OrderedSet(int initialCapacity) {
        super(initialCapacity);
        this.items = new Array(this.capacity);
    }

    public OrderedSet(OrderedSet set) {
        super((ObjectSet) set);
        this.items = new Array(this.capacity);
        this.items.addAll(set.items);
    }

    public boolean add(T key) {
        if (!contains(key)) {
            this.items.add(key);
        }
        return super.add(key);
    }

    public boolean remove(T key) {
        this.items.removeValue(key, false);
        return super.remove(key);
    }

    public void clear(int maximumCapacity) {
        this.items.clear();
        super.clear(maximumCapacity);
    }

    public void clear() {
        this.items.clear();
        super.clear();
    }

    public Array<T> orderedItems() {
        return this.items;
    }

    public OrderedSetIterator<T> iterator() {
        if (this.iterator1 == null) {
            this.iterator1 = new OrderedSetIterator(this);
            this.iterator2 = new OrderedSetIterator(this);
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

    public String toString() {
        if (this.size == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(32);
        buffer.append('{');
        Array<T> keys = this.items;
        int n = keys.size;
        for (int i = 0; i < n; i++) {
            Object key = keys.get(i);
            if (i > 0) {
                buffer.append(", ");
            }
            buffer.append(key);
        }
        buffer.append('}');
        return buffer.toString();
    }
}
