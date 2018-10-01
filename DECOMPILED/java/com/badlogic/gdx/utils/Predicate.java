package com.badlogic.gdx.utils;

import java.util.Iterator;

public interface Predicate<T> {

    public static class PredicateIterable<T> implements Iterable<T> {
        public Iterable<T> iterable;
        public PredicateIterator<T> iterator = null;
        public Predicate<T> predicate;

        public PredicateIterable(Iterable<T> iterable, Predicate<T> predicate) {
            set(iterable, predicate);
        }

        public void set(Iterable<T> iterable, Predicate<T> predicate) {
            this.iterable = iterable;
            this.predicate = predicate;
        }

        public Iterator<T> iterator() {
            if (this.iterator == null) {
                this.iterator = new PredicateIterator(this.iterable.iterator(), this.predicate);
            } else {
                this.iterator.set(this.iterable.iterator(), this.predicate);
            }
            return this.iterator;
        }
    }

    public static class PredicateIterator<T> implements Iterator<T> {
        public boolean end;
        public Iterator<T> iterator;
        public T next;
        public boolean peeked;
        public Predicate<T> predicate;

        public PredicateIterator(Iterable<T> iterable, Predicate<T> predicate) {
            this(iterable.iterator(), (Predicate) predicate);
        }

        public PredicateIterator(Iterator<T> iterator, Predicate<T> predicate) {
            this.end = false;
            this.peeked = false;
            this.next = null;
            set((Iterator) iterator, (Predicate) predicate);
        }

        public void set(Iterable<T> iterable, Predicate<T> predicate) {
            set(iterable.iterator(), (Predicate) predicate);
        }

        public void set(Iterator<T> iterator, Predicate<T> predicate) {
            this.iterator = iterator;
            this.predicate = predicate;
            this.peeked = false;
            this.end = false;
            this.next = null;
        }

        public boolean hasNext() {
            if (this.end) {
                return false;
            }
            if (this.next != null) {
                return true;
            }
            this.peeked = true;
            while (this.iterator.hasNext()) {
                T n = this.iterator.next();
                if (this.predicate.evaluate(n)) {
                    this.next = n;
                    return true;
                }
            }
            this.end = true;
            return false;
        }

        public T next() {
            if (this.next == null && !hasNext()) {
                return null;
            }
            T result = this.next;
            this.next = null;
            this.peeked = false;
            return result;
        }

        public void remove() {
            if (this.peeked) {
                throw new GdxRuntimeException("Cannot remove between a call to hasNext() and next().");
            }
            this.iterator.remove();
        }
    }

    boolean evaluate(T t);
}
