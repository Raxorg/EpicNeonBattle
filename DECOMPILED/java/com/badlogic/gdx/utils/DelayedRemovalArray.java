package com.badlogic.gdx.utils;

import java.util.Comparator;

public class DelayedRemovalArray<T> extends Array<T> {
    private int iterating;
    private IntArray remove = new IntArray(0);

    public DelayedRemovalArray(Array array) {
        super(array);
    }

    public DelayedRemovalArray(boolean ordered, int capacity, Class arrayType) {
        super(ordered, capacity, arrayType);
    }

    public DelayedRemovalArray(boolean ordered, int capacity) {
        super(ordered, capacity);
    }

    public DelayedRemovalArray(boolean ordered, T[] array, int startIndex, int count) {
        super(ordered, array, startIndex, count);
    }

    public DelayedRemovalArray(Class arrayType) {
        super(arrayType);
    }

    public DelayedRemovalArray(int capacity) {
        super(capacity);
    }

    public DelayedRemovalArray(T[] array) {
        super((Object[]) array);
    }

    public void begin() {
        this.iterating++;
    }

    public void end() {
        if (this.iterating == 0) {
            throw new IllegalStateException("begin must be called before end.");
        }
        this.iterating--;
        if (this.iterating == 0) {
            int n = this.remove.size;
            for (int i = 0; i < n; i++) {
                removeIndex(this.remove.pop());
            }
        }
    }

    private void remove(int index) {
        int i = 0;
        int n = this.remove.size;
        while (i < n) {
            int removeIndex = this.remove.get(i);
            if (index != removeIndex) {
                if (index < removeIndex) {
                    this.remove.insert(i, index);
                    return;
                }
                i++;
            } else {
                return;
            }
        }
        this.remove.add(index);
    }

    public boolean removeValue(T value, boolean identity) {
        if (this.iterating <= 0) {
            return super.removeValue(value, identity);
        }
        int index = indexOf(value, identity);
        if (index == -1) {
            return false;
        }
        remove(index);
        return true;
    }

    public T removeIndex(int index) {
        if (this.iterating <= 0) {
            return super.removeIndex(index);
        }
        remove(index);
        return get(index);
    }

    public void removeRange(int start, int end) {
        if (this.iterating > 0) {
            for (int i = end; i >= start; i--) {
                remove(i);
            }
            return;
        }
        super.removeRange(start, end);
    }

    public void set(int index, T value) {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.set(index, value);
    }

    public void insert(int index, T value) {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.insert(index, value);
    }

    public void swap(int first, int second) {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.swap(first, second);
    }

    public T pop() {
        if (this.iterating <= 0) {
            return super.pop();
        }
        throw new IllegalStateException("Invalid between begin/end.");
    }

    public void clear() {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.clear();
    }

    public void sort() {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.sort();
    }

    public void sort(Comparator<? super T> comparator) {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.sort(comparator);
    }

    public void reverse() {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.reverse();
    }

    public void shuffle() {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.shuffle();
    }

    public void truncate(int newSize) {
        if (this.iterating > 0) {
            throw new IllegalStateException("Invalid between begin/end.");
        }
        super.truncate(newSize);
    }

    public static <T> DelayedRemovalArray<T> with(T... array) {
        return new DelayedRemovalArray((Object[]) array);
    }
}
