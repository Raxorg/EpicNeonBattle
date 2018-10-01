package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CubemapAttribute extends Attribute {
    public static final long EnvironmentMap = Attribute.register(EnvironmentMapAlias);
    public static final String EnvironmentMapAlias = "environmentMapTexture";
    protected static long Mask = EnvironmentMap;
    public final TextureDescriptor<Cubemap> textureDescription;

    public static final boolean is(long mask) {
        return (Mask & mask) != EnvironmentMap;
    }

    public CubemapAttribute(long type) {
        super(type);
        if (is(type)) {
            this.textureDescription = new TextureDescriptor();
            return;
        }
        throw new GdxRuntimeException("Invalid type specified");
    }

    public <T extends Cubemap> CubemapAttribute(long type, TextureDescriptor<T> textureDescription) {
        this(type);
        this.textureDescription.set(textureDescription);
    }

    public CubemapAttribute(long type, Cubemap texture) {
        this(type);
        this.textureDescription.texture = texture;
    }

    public CubemapAttribute(CubemapAttribute copyFrom) {
        this(copyFrom.type, copyFrom.textureDescription);
    }

    public Attribute copy() {
        return new CubemapAttribute(this);
    }

    public int hashCode() {
        return (super.hashCode() * 967) + this.textureDescription.hashCode();
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        return this.textureDescription.compareTo(((CubemapAttribute) o).textureDescription);
    }
}
