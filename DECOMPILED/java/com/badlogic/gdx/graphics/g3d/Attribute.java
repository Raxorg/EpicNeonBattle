package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.utils.Array;

public abstract class Attribute implements Comparable<Attribute> {
    private static final Array<String> types = new Array();
    public final long type;
    private final int typeBit;

    public abstract Attribute copy();

    public static final long getAttributeType(String alias) {
        for (int i = 0; i < types.size; i++) {
            if (((String) types.get(i)).compareTo(alias) == 0) {
                return 1 << i;
            }
        }
        return 0;
    }

    public static final String getAttributeAlias(long type) {
        int idx = -1;
        while (type != 0) {
            idx++;
            if (idx < 63) {
                if (((type >> idx) & 1) != 0) {
                    break;
                }
            }
            break;
        }
        return (idx < 0 || idx >= types.size) ? null : (String) types.get(idx);
    }

    protected static final long register(String alias) {
        long result = getAttributeType(alias);
        if (result > 0) {
            return result;
        }
        types.add(alias);
        return 1 << (types.size - 1);
    }

    protected Attribute(long type) {
        this.type = type;
        this.typeBit = Long.numberOfTrailingZeros(type);
    }

    protected boolean equals(Attribute other) {
        return other.hashCode() == hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Attribute)) {
            return false;
        }
        Attribute other = (Attribute) obj;
        if (this.type == other.type) {
            return equals(other);
        }
        return false;
    }

    public String toString() {
        return getAttributeAlias(this.type);
    }

    public int hashCode() {
        return this.typeBit * 7489;
    }
}
