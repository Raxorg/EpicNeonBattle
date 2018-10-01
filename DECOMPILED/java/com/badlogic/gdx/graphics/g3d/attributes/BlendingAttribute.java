package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;

public class BlendingAttribute extends Attribute {
    public static final String Alias = "blended";
    public static final long Type = Attribute.register(Alias);
    public boolean blended;
    public int destFunction;
    public float opacity;
    public int sourceFunction;

    public static final boolean is(long mask) {
        return (Type & mask) == mask;
    }

    public BlendingAttribute() {
        this(null);
    }

    public BlendingAttribute(boolean blended, int sourceFunc, int destFunc, float opacity) {
        super(Type);
        this.opacity = 1.0f;
        this.blended = blended;
        this.sourceFunction = sourceFunc;
        this.destFunction = destFunc;
        this.opacity = opacity;
    }

    public BlendingAttribute(int sourceFunc, int destFunc, float opacity) {
        this(true, sourceFunc, destFunc, opacity);
    }

    public BlendingAttribute(int sourceFunc, int destFunc) {
        this(sourceFunc, destFunc, 1.0f);
    }

    public BlendingAttribute(boolean blended, float opacity) {
        this(blended, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, opacity);
    }

    public BlendingAttribute(float opacity) {
        this(true, opacity);
    }

    public BlendingAttribute(BlendingAttribute copyFrom) {
        this(copyFrom == null ? true : copyFrom.blended, copyFrom == null ? GL20.GL_SRC_ALPHA : copyFrom.sourceFunction, copyFrom == null ? GL20.GL_ONE_MINUS_SRC_ALPHA : copyFrom.destFunction, copyFrom == null ? 1.0f : copyFrom.opacity);
    }

    public BlendingAttribute copy() {
        return new BlendingAttribute(this);
    }

    public int hashCode() {
        return (((((((super.hashCode() * 947) + (this.blended ? 1 : 0)) * 947) + this.sourceFunction) * 947) + this.destFunction) * 947) + NumberUtils.floatToRawIntBits(this.opacity);
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        BlendingAttribute other = (BlendingAttribute) o;
        if (this.blended != other.blended) {
            if (this.blended) {
                return 1;
            }
            return -1;
        } else if (this.sourceFunction != other.sourceFunction) {
            return this.sourceFunction - other.sourceFunction;
        } else {
            if (this.destFunction != other.destFunction) {
                return this.destFunction - other.destFunction;
            }
            if (MathUtils.isEqual(this.opacity, other.opacity)) {
                return 0;
            }
            if (this.opacity >= other.opacity) {
                return -1;
            }
            return 1;
        }
    }
}
