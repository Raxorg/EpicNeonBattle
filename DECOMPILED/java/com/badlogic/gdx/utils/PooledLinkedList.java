package com.badlogic.gdx.utils;

public class PooledLinkedList<T> {
    private Item<T> curr;
    private Item<T> head;
    private Item<T> iter;
    private final Pool<Item<T>> pool;
    private int size = 0;
    private Item<T> tail;

    static final class Item<T> {
        public Item<T> next;
        public T payload;
        public Item<T> prev;

        Item() {
        }
    }

    public PooledLinkedList(int maxPoolSize) {
        this.pool = new Pool<Item<T>>(16, maxPoolSize) {
            protected Item<T> newObject() {
                return new Item();
            }
        };
    }

    public void add(T object) {
        Item<T> item = (Item) this.pool.obtain();
        item.payload = object;
        item.next = null;
        item.prev = null;
        if (this.head == null) {
            this.head = item;
            this.tail = item;
            this.size++;
            return;
        }
        item.prev = this.tail;
        this.tail.next = item;
        this.tail = item;
        this.size++;
    }

    public int size() {
        return this.size;
    }

    public void iter() {
        this.iter = this.head;
    }

    public void iterReverse() {
        this.iter = this.tail;
    }

    public T next() {
        if (this.iter == null) {
            return null;
        }
        T payload = this.iter.payload;
        this.curr = this.iter;
        this.iter = this.iter.next;
        return payload;
    }

    public T previous() {
        if (this.iter == null) {
            return null;
        }
        T payload = this.iter.payload;
        this.curr = this.iter;
        this.iter = this.iter.prev;
        return payload;
    }

    public void remove() {
        if (this.curr != null) {
            this.size--;
            this.pool.free(this.curr);
            Item<T> c = this.curr;
            Item<T> n = this.curr.next;
            Item<T> p = this.curr.prev;
            this.curr = null;
            if (this.size == 0) {
                this.head = null;
                this.tail = null;
            } else if (c == this.head) {
                n.prev = null;
                this.head = n;
            } else if (c == this.tail) {
                p.next = null;
                this.tail = p;
            } else {
                p.next = n;
                n.prev = p;
            }
        }
    }

    public void clear() {
        iter();
        while (next() != null) {
            remove();
        }
    }
}
