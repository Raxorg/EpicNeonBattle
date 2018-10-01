package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class DepthTestAttribute extends Attribute {
    public static final String Alias = "depthStencil";
    protected static long Mask = Type;
    public static final long Type = Attribute.register(Alias);
    public int depthFunc;
    public boolean depthMask;
    public float depthRangeFar;
    public float depthRangeNear;

    public static final boolean is(long mask) {
        return (Mask & mask) != 0;
    }

    public DepthTestAttribute() {
        this((int) GL20.GL_LEQUAL);
    }

    public DepthTestAttribute(boolean depthMask) {
        this(GL20.GL_LEQUAL, depthMask);
    }

    public DepthTestAttribute(int depthFunc) {
        this(depthFunc, true);
    }

    public DepthTestAttribute(int depthFunc, boolean depthMask) {
        this(depthFunc, 0.0f, 1.0f, depthMask);
    }

    public DepthTestAttribute(int depthFunc, float depthRangeNear, float depthRangeFar) {
        this(depthFunc, depthRangeNear, depthRangeFar, true);
    }

    public DepthTestAttribute(int depthFunc, float depthRangeNear, float depthRangeFar, boolean depthMask) {
        this(Type, depthFunc, depthRangeNear, depthRangeFar, depthMask);
    }

    public DepthTestAttribute(long type, int depthFunc, float depthRangeNear, float depthRangeFar, boolean depthMask) {
        super(type);
        if (is(type)) {
            this.depthFunc = depthFunc;
            this.depthRangeNear = depthRangeNear;
            this.depthRangeFar = depthRangeFar;
            this.depthMask = depthMask;
            return;
        }
        throw new GdxRuntimeException("Invalid type specified");
    }

    public DepthTestAttribute(DepthTestAttribute rhs) {
        this(rhs.type, rhs.depthFunc, rhs.depthRangeNear, rhs.depthRangeFar, rhs.depthMask);
    }

    public Attribute copy() {
        return new DepthTestAttribute(this);
    }

    public int hashCode() {
        return (((((((super.hashCode() * 971) + this.depthFunc) * 971) + NumberUtils.floatToRawIntBits(this.depthRangeNear)) * 971) + NumberUtils.floatToRawIntBits(this.depthRangeFar)) * 971) + (this.depthMask ? 1 : 0);
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        DepthTestAttribute other = (DepthTestAttribute) o;
        if (this.depthFunc != other.depthFunc) {
            return this.depthFunc - other.depthFunc;
        }
        if (this.depthMask != other.depthMask) {
            if (this.depthMask) {
                return -1;
            }
            return 1;
        } else if (MathUtils.isEqual(this.depthRangeNear, other.depthRangeNear)) {
            if (MathUtils.isEqual(this.depthRangeFar, other.depthRangeFar)) {
                return 0;
            }
            if (this.depthRangeFar >= other.depthRangeFar) {
                return 1;
            }
            return -1;
        } else if (this.depthRangeNear >= other.depthRangeNear) {
            return 1;
        } else {
            return -1;
        }
    }
}
