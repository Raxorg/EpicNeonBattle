package com.badlogic.gdx.utils;

import java.util.Comparator;

public class QuickSelect<T> {
    private T[] array;
    private Comparator<? super T> comp;

    public int select(T[] items, Comparator<T> comp, int n, int size) {
        this.array = items;
        this.comp = comp;
        return recursiveSelect(0, size - 1, n);
    }

    private int partition(int left, int right, int pivot) {
        T pivotValue = this.array[pivot];
        swap(right, pivot);
        int storage = left;
        for (int i = left; i < right; i++) {
            if (this.comp.compare(this.array[i], pivotValue) < 0) {
                swap(storage, i);
                storage++;
            }
        }
        swap(right, storage);
        return storage;
    }

    private int recursiveSelect(int left, int right, int k) {
        if (left == right) {
            return left;
        }
        int result;
        int pivotNewIndex = partition(left, right, medianOfThreePivot(left, right));
        int pivotDist = (pivotNewIndex - left) + 1;
        if (pivotDist == k) {
            result = pivotNewIndex;
        } else if (k < pivotDist) {
            result = recursiveSelect(left, pivotNewIndex - 1, k);
        } else {
            result = recursiveSelect(pivotNewIndex + 1, right, k - pivotDist);
        }
        return result;
    }

    private int medianOfThreePivot(int leftIdx, int rightIdx) {
        T left = this.array[leftIdx];
        int midIdx = (leftIdx + rightIdx) / 2;
        T mid = this.array[midIdx];
        T right = this.array[rightIdx];
        if (this.comp.compare(left, mid) > 0) {
            if (this.comp.compare(mid, right) > 0) {
                return midIdx;
            }
            if (this.comp.compare(left, right) > 0) {
                return rightIdx;
            }
            return leftIdx;
        } else if (this.comp.compare(left, right) > 0) {
            return leftIdx;
        } else {
            if (this.comp.compare(mid, right) > 0) {
                return rightIdx;
            }
            return midIdx;
        }
    }

    private void swap(int left, int right) {
        T tmp = this.array[left];
        this.array[left] = this.array[right];
        this.array[right] = tmp;
    }
}
