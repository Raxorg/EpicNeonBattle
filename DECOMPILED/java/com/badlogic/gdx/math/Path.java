package com.badlogic.gdx.math;

public interface Path<T> {
    float approxLength(int i);

    float approximate(T t);

    T derivativeAt(T t, float f);

    float locate(T t);

    T valueAt(T t, float f);
}
