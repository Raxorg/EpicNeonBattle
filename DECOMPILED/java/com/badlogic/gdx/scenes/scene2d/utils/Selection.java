package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.OrderedSet;
import com.badlogic.gdx.utils.Pools;
import java.util.Iterator;

public class Selection<T> implements Disableable, Iterable<T> {
    private Actor actor;
    boolean isDisabled;
    T lastSelected;
    boolean multiple;
    private final OrderedSet<T> old = new OrderedSet();
    private boolean programmaticChangeEvents = true;
    boolean required;
    final OrderedSet<T> selected = new OrderedSet();
    private boolean toggle;

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public void choose(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        } else if (!this.isDisabled) {
            snapshot();
            try {
                if ((!this.toggle && ((this.required || this.selected.size != 1) && !UIUtils.ctrl())) || !this.selected.contains(item)) {
                    boolean modified = false;
                    if (!(this.multiple && (this.toggle || UIUtils.ctrl()))) {
                        if (this.selected.size == 1 && this.selected.contains(item)) {
                            cleanup();
                            return;
                        } else {
                            modified = this.selected.size > 0;
                            this.selected.clear();
                        }
                    }
                    if (this.selected.add(item) || modified) {
                        this.lastSelected = item;
                    } else {
                        cleanup();
                        return;
                    }
                } else if (!this.required || this.selected.size != 1) {
                    this.selected.remove(item);
                    this.lastSelected = null;
                } else {
                    return;
                }
                if (fireChangeEvent()) {
                    revert();
                }
                cleanup();
            } finally {
                cleanup();
            }
        }
    }

    public boolean hasItems() {
        return this.selected.size > 0;
    }

    public boolean isEmpty() {
        return this.selected.size == 0;
    }

    public int size() {
        return this.selected.size;
    }

    public OrderedSet<T> items() {
        return this.selected;
    }

    public T first() {
        return this.selected.size == 0 ? null : this.selected.first();
    }

    void snapshot() {
        this.old.clear();
        this.old.addAll(this.selected);
    }

    void revert() {
        this.selected.clear();
        this.selected.addAll(this.old);
    }

    void cleanup() {
        this.old.clear(32);
    }

    public void set(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        } else if (this.selected.size != 1 || this.selected.first() != item) {
            snapshot();
            this.selected.clear();
            this.selected.add(item);
            if (this.programmaticChangeEvents && fireChangeEvent()) {
                revert();
            } else {
                this.lastSelected = item;
            }
            cleanup();
        }
    }

    public void setAll(Array<T> items) {
        boolean added = false;
        snapshot();
        this.selected.clear();
        int n = items.size;
        for (int i = 0; i < n; i++) {
            T item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("item cannot be null.");
            }
            if (this.selected.add(item)) {
                added = true;
            }
        }
        if (added && this.programmaticChangeEvents && fireChangeEvent()) {
            revert();
        } else {
            this.lastSelected = items.peek();
        }
        cleanup();
    }

    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        } else if (!this.selected.add(item)) {
        } else {
            if (this.programmaticChangeEvents && fireChangeEvent()) {
                this.selected.remove(item);
            } else {
                this.lastSelected = item;
            }
        }
    }

    public void addAll(Array<T> items) {
        boolean added = false;
        snapshot();
        int n = items.size;
        for (int i = 0; i < n; i++) {
            T item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("item cannot be null.");
            }
            if (this.selected.add(item)) {
                added = true;
            }
        }
        if (added && this.programmaticChangeEvents && fireChangeEvent()) {
            revert();
        } else {
            this.lastSelected = items.peek();
        }
        cleanup();
    }

    public void remove(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        } else if (!this.selected.remove(item)) {
        } else {
            if (this.programmaticChangeEvents && fireChangeEvent()) {
                this.selected.add(item);
            } else {
                this.lastSelected = null;
            }
        }
    }

    public void removeAll(Array<T> items) {
        boolean removed = false;
        snapshot();
        int n = items.size;
        for (int i = 0; i < n; i++) {
            T item = items.get(i);
            if (item == null) {
                throw new IllegalArgumentException("item cannot be null.");
            }
            if (this.selected.remove(item)) {
                removed = true;
            }
        }
        if (removed && this.programmaticChangeEvents && fireChangeEvent()) {
            revert();
        } else {
            this.lastSelected = null;
        }
        cleanup();
    }

    public void clear() {
        if (this.selected.size != 0) {
            snapshot();
            this.selected.clear();
            if (this.programmaticChangeEvents && fireChangeEvent()) {
                revert();
            } else {
                this.lastSelected = null;
            }
            cleanup();
        }
    }

    public boolean fireChangeEvent() {
        if (this.actor == null) {
            return false;
        }
        ChangeEvent changeEvent = (ChangeEvent) Pools.obtain(ChangeEvent.class);
        try {
            boolean fire = this.actor.fire(changeEvent);
            return fire;
        } finally {
            Pools.free(changeEvent);
        }
    }

    public boolean contains(T item) {
        if (item == null) {
            return false;
        }
        return this.selected.contains(item);
    }

    public T getLastSelected() {
        if (this.lastSelected != null) {
            return this.lastSelected;
        }
        if (this.selected.size > 0) {
            return this.selected.first();
        }
        return null;
    }

    public Iterator<T> iterator() {
        return this.selected.iterator();
    }

    public Array<T> toArray() {
        return this.selected.iterator().toArray();
    }

    public Array<T> toArray(Array<T> array) {
        return this.selected.iterator().toArray(array);
    }

    public void setDisabled(boolean isDisabled) {
        this.isDisabled = isDisabled;
    }

    public boolean isDisabled() {
        return this.isDisabled;
    }

    public boolean getToggle() {
        return this.toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
    }

    public boolean getMultiple() {
        return this.multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean getRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setProgrammaticChangeEvents(boolean programmaticChangeEvents) {
        this.programmaticChangeEvents = programmaticChangeEvents;
    }

    public String toString() {
        return this.selected.toString();
    }
}
