package com.badlogic.gdx.utils;

public abstract class Pool<T> {
    private final Array<T> freeObjects;
    public final int max;
    public int peak;

    public interface Poolable {
        void reset();
    }

    protected abstract T newObject();

    public Pool() {
        this(16, Integer.MAX_VALUE);
    }

    public Pool(int initialCapacity) {
        this(initialCapacity, Integer.MAX_VALUE);
    }

    public Pool(int initialCapacity, int max) {
        this.freeObjects = new Array(false, initialCapacity);
        this.max = max;
    }

    public T obtain() {
        return this.freeObjects.size == 0 ? newObject() : this.freeObjects.pop();
    }

    public void free(T object) {
        if (object == null) {
            throw new IllegalArgumentException("object cannot be null.");
        }
        if (this.freeObjects.size < this.max) {
            this.freeObjects.add(object);
            this.peak = Math.max(this.peak, this.freeObjects.size);
        }
        if (object instanceof Poolable) {
            ((Poolable) object).reset();
        }
    }

    public void freeAll(Array<T> objects) {
        if (objects == null) {
            throw new IllegalArgumentException("object cannot be null.");
        }
        Array<T> freeObjects = this.freeObjects;
        int max = this.max;
        for (int i = 0; i < objects.size; i++) {
            T object = objects.get(i);
            if (object != null) {
                if (freeObjects.size < max) {
                    freeObjects.add(object);
                }
                if (object instanceof Poolable) {
                    ((Poolable) object).reset();
                }
            }
        }
        this.peak = Math.max(this.peak, freeObjects.size);
    }

    public void clear() {
        this.freeObjects.clear();
    }

    public int getFree() {
        return this.freeObjects.size;
    }
}
