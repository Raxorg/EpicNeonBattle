package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class SpotLightsAttribute extends Attribute {
    public static final String Alias = "spotLights";
    public static final long Type = Attribute.register(Alias);
    public final Array<SpotLight> lights;

    public static final boolean is(long mask) {
        return (Type & mask) == mask;
    }

    public SpotLightsAttribute() {
        super(Type);
        this.lights = new Array(1);
    }

    public SpotLightsAttribute(SpotLightsAttribute copyFrom) {
        this();
        this.lights.addAll(copyFrom.lights);
    }

    public SpotLightsAttribute copy() {
        return new SpotLightsAttribute(this);
    }

    public int hashCode() {
        int result = super.hashCode();
        Iterator it = this.lights.iterator();
        while (it.hasNext()) {
            SpotLight light = (SpotLight) it.next();
            result = (result * 1237) + (light == null ? 0 : light.hashCode());
        }
        return result;
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return this.type < o.type ? -1 : 1;
        } else {
            return 0;
        }
    }
}
