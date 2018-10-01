package com.badlogic.gdx.utils;

public class Pools {
    private static final ObjectMap<Class, Pool> typePools = new ObjectMap();

    public static <T> Pool<T> get(Class<T> type, int max) {
        Pool pool = (Pool) typePools.get(type);
        if (pool != null) {
            return pool;
        }
        pool = new ReflectionPool(type, 4, max);
        typePools.put(type, pool);
        return pool;
    }

    public static <T> Pool<T> get(Class<T> type) {
        return get(type, 100);
    }

    public static <T> void set(Class<T> type, Pool<T> pool) {
        typePools.put(type, pool);
    }

    public static <T> T obtain(Class<T> type) {
        return get(type).obtain();
    }

    public static void free(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null.");
        }
        Pool pool = (Pool) typePools.get(object.getClass());
        if (pool != null) {
            pool.free(object);
        }
    }

    public static void freeAll(Array objects) {
        freeAll(objects, false);
    }

    public static void freeAll(Array objects, boolean samePool) {
        if (objects == null) {
            throw new IllegalArgumentException("Objects cannot be null.");
        }
        Pool pool = null;
        int n = objects.size;
        for (int i = 0; i < n; i++) {
            Object object = objects.get(i);
            if (object != null) {
                if (pool == null) {
                    pool = (Pool) typePools.get(object.getClass());
                    if (pool == null) {
                    }
                }
                pool.free(object);
                if (!samePool) {
                    pool = null;
                }
            }
        }
    }

    private Pools() {
    }
}
