package com.badlogic.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class ArraySelection<T> extends Selection<T> {
    private Array<T> array;
    private boolean rangeSelect = true;

    public ArraySelection(Array<T> array) {
        this.array = array;
    }

    public void choose(T item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null.");
        } else if (!this.isDisabled) {
            if (this.selected.size > 0 && this.rangeSelect && this.multiple && (Gdx.input.isKeyPressed(59) || Gdx.input.isKeyPressed(60))) {
                int low = this.array.indexOf(getLastSelected(), false);
                int high = this.array.indexOf(item, false);
                if (low > high) {
                    int temp = low;
                    low = high;
                    high = temp;
                }
                snapshot();
                if (!UIUtils.ctrl()) {
                    this.selected.clear();
                }
                while (low <= high) {
                    this.selected.add(this.array.get(low));
                    low++;
                }
                if (fireChangeEvent()) {
                    revert();
                }
                cleanup();
                return;
            }
            super.choose(item);
        }
    }

    public boolean getRangeSelect() {
        return this.rangeSelect;
    }

    public void setRangeSelect(boolean rangeSelect) {
        this.rangeSelect = rangeSelect;
    }

    public void validate() {
        Array<T> array = this.array;
        if (array.size == 0) {
            clear();
            return;
        }
        Iterator<T> iter = items().iterator();
        while (iter.hasNext()) {
            if (!array.contains(iter.next(), false)) {
                iter.remove();
            }
        }
        if (this.required && this.selected.size == 0) {
            set(array.first());
        }
    }
}
