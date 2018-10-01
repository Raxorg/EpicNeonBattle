package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

public class IntAttribute extends Attribute {
    public static final long CullFace = Attribute.register(CullFaceAlias);
    public static final String CullFaceAlias = "cullface";
    public int value;

    public static IntAttribute createCullFace(int value) {
        return new IntAttribute(CullFace, value);
    }

    public IntAttribute(long type) {
        super(type);
    }

    public IntAttribute(long type, int value) {
        super(type);
        this.value = value;
    }

    public Attribute copy() {
        return new IntAttribute(this.type, this.value);
    }

    public int hashCode() {
        return (super.hashCode() * 983) + this.value;
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        return this.value - ((IntAttribute) o).value;
    }
}
