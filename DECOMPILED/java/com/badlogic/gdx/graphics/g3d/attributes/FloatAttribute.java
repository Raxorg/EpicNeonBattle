package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.NumberUtils;

public class FloatAttribute extends Attribute {
    public static final long AlphaTest = Attribute.register(AlphaTestAlias);
    public static final String AlphaTestAlias = "alphaTest";
    public static final long Shininess = Attribute.register(ShininessAlias);
    public static final String ShininessAlias = "shininess";
    public float value;

    public static FloatAttribute createShininess(float value) {
        return new FloatAttribute(Shininess, value);
    }

    public static FloatAttribute createAlphaTest(float value) {
        return new FloatAttribute(AlphaTest, value);
    }

    public FloatAttribute(long type) {
        super(type);
    }

    public FloatAttribute(long type, float value) {
        super(type);
        this.value = value;
    }

    public Attribute copy() {
        return new FloatAttribute(this.type, this.value);
    }

    public int hashCode() {
        return (super.hashCode() * 977) + NumberUtils.floatToRawIntBits(this.value);
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        float v = ((FloatAttribute) o).value;
        if (MathUtils.isEqual(this.value, v)) {
            return 0;
        }
        return this.value < v ? -1 : 1;
    }
}
