package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class Material extends Attributes {
    private static int counter = 0;
    public String id;

    public Material() {
        StringBuilder append = new StringBuilder().append("mtl");
        int i = counter + 1;
        counter = i;
        this(append.append(i).toString());
    }

    public Material(String id) {
        this.id = id;
    }

    public Material(Attribute... attributes) {
        this();
        set(attributes);
    }

    public Material(String id, Attribute... attributes) {
        this(id);
        set(attributes);
    }

    public Material(Array<Attribute> attributes) {
        this();
        set((Iterable) attributes);
    }

    public Material(String id, Array<Attribute> attributes) {
        this(id);
        set((Iterable) attributes);
    }

    public Material(Material copyFrom) {
        this(copyFrom.id, copyFrom);
    }

    public Material(String id, Material copyFrom) {
        this(id);
        Iterator it = copyFrom.iterator();
        while (it.hasNext()) {
            set(((Attribute) it.next()).copy());
        }
    }

    public final Material copy() {
        return new Material(this);
    }

    public int hashCode() {
        return super.hashCode() + (this.id.hashCode() * 3);
    }

    public boolean equals(Object other) {
        return (other instanceof Material) && (other == this || (((Material) other).id.equals(this.id) && super.equals(other)));
    }
}
