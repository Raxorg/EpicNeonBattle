package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.NumberUtils;

public class TextureAttribute extends Attribute {
    public static final long Ambient = Attribute.register(AmbientAlias);
    public static final String AmbientAlias = "ambientTexture";
    public static final long Bump = Attribute.register(BumpAlias);
    public static final String BumpAlias = "bumpTexture";
    public static final long Diffuse = Attribute.register(DiffuseAlias);
    public static final String DiffuseAlias = "diffuseTexture";
    public static final long Emissive = Attribute.register(EmissiveAlias);
    public static final String EmissiveAlias = "emissiveTexture";
    protected static long Mask = ((((((Diffuse | Specular) | Bump) | Normal) | Ambient) | Emissive) | Reflection);
    public static final long Normal = Attribute.register(NormalAlias);
    public static final String NormalAlias = "normalTexture";
    public static final long Reflection = Attribute.register(ReflectionAlias);
    public static final String ReflectionAlias = "reflectionTexture";
    public static final long Specular = Attribute.register(SpecularAlias);
    public static final String SpecularAlias = "specularTexture";
    public float offsetU;
    public float offsetV;
    public float scaleU;
    public float scaleV;
    public final TextureDescriptor<Texture> textureDescription;
    public int uvIndex;

    public static final boolean is(long mask) {
        return (Mask & mask) != Specular;
    }

    public static TextureAttribute createDiffuse(Texture texture) {
        return new TextureAttribute(Diffuse, texture);
    }

    public static TextureAttribute createDiffuse(TextureRegion region) {
        return new TextureAttribute(Diffuse, region);
    }

    public static TextureAttribute createSpecular(Texture texture) {
        return new TextureAttribute(Specular, texture);
    }

    public static TextureAttribute createSpecular(TextureRegion region) {
        return new TextureAttribute(Specular, region);
    }

    public static TextureAttribute createNormal(Texture texture) {
        return new TextureAttribute(Normal, texture);
    }

    public static TextureAttribute createNormal(TextureRegion region) {
        return new TextureAttribute(Normal, region);
    }

    public static TextureAttribute createBump(Texture texture) {
        return new TextureAttribute(Bump, texture);
    }

    public static TextureAttribute createBump(TextureRegion region) {
        return new TextureAttribute(Bump, region);
    }

    public static TextureAttribute createAmbient(Texture texture) {
        return new TextureAttribute(Ambient, texture);
    }

    public static TextureAttribute createAmbient(TextureRegion region) {
        return new TextureAttribute(Ambient, region);
    }

    public static TextureAttribute createEmissive(Texture texture) {
        return new TextureAttribute(Emissive, texture);
    }

    public static TextureAttribute createEmissive(TextureRegion region) {
        return new TextureAttribute(Emissive, region);
    }

    public static TextureAttribute createReflection(Texture texture) {
        return new TextureAttribute(Reflection, texture);
    }

    public static TextureAttribute createReflection(TextureRegion region) {
        return new TextureAttribute(Reflection, region);
    }

    public TextureAttribute(long type) {
        super(type);
        this.offsetU = 0.0f;
        this.offsetV = 0.0f;
        this.scaleU = 1.0f;
        this.scaleV = 1.0f;
        this.uvIndex = 0;
        if (is(type)) {
            this.textureDescription = new TextureDescriptor();
            return;
        }
        throw new GdxRuntimeException("Invalid type specified");
    }

    public <T extends Texture> TextureAttribute(long type, TextureDescriptor<T> textureDescription) {
        this(type);
        this.textureDescription.set(textureDescription);
    }

    public <T extends Texture> TextureAttribute(long type, TextureDescriptor<T> textureDescription, float offsetU, float offsetV, float scaleU, float scaleV, int uvIndex) {
        this(type, (TextureDescriptor) textureDescription);
        this.offsetU = offsetU;
        this.offsetV = offsetV;
        this.scaleU = scaleU;
        this.scaleV = scaleV;
        this.uvIndex = uvIndex;
    }

    public <T extends Texture> TextureAttribute(long type, TextureDescriptor<T> textureDescription, float offsetU, float offsetV, float scaleU, float scaleV) {
        this(type, textureDescription, offsetU, offsetV, scaleU, scaleV, 0);
    }

    public TextureAttribute(long type, Texture texture) {
        this(type);
        this.textureDescription.texture = texture;
    }

    public TextureAttribute(long type, TextureRegion region) {
        this(type);
        set(region);
    }

    public TextureAttribute(TextureAttribute copyFrom) {
        this(copyFrom.type, copyFrom.textureDescription, copyFrom.offsetU, copyFrom.offsetV, copyFrom.scaleU, copyFrom.scaleV, copyFrom.uvIndex);
    }

    public void set(TextureRegion region) {
        this.textureDescription.texture = region.getTexture();
        this.offsetU = region.getU();
        this.offsetV = region.getV();
        this.scaleU = region.getU2() - this.offsetU;
        this.scaleV = region.getV2() - this.offsetV;
    }

    public Attribute copy() {
        return new TextureAttribute(this);
    }

    public int hashCode() {
        return (((((((((((super.hashCode() * 991) + this.textureDescription.hashCode()) * 991) + NumberUtils.floatToRawIntBits(this.offsetU)) * 991) + NumberUtils.floatToRawIntBits(this.offsetV)) * 991) + NumberUtils.floatToRawIntBits(this.scaleU)) * 991) + NumberUtils.floatToRawIntBits(this.scaleV)) * 991) + this.uvIndex;
    }

    public int compareTo(Attribute o) {
        int i = -1;
        if (this.type != o.type) {
            if (this.type >= o.type) {
                i = 1;
            }
            return i;
        }
        TextureAttribute other = (TextureAttribute) o;
        int c = this.textureDescription.compareTo(other.textureDescription);
        if (c != 0) {
            return c;
        }
        if (this.uvIndex != other.uvIndex) {
            return this.uvIndex - other.uvIndex;
        }
        if (MathUtils.isEqual(this.offsetU, other.offsetU)) {
            if (MathUtils.isEqual(this.offsetV, other.offsetV)) {
                if (MathUtils.isEqual(this.scaleU, other.scaleU)) {
                    if (MathUtils.isEqual(this.scaleV, other.scaleV)) {
                        return 0;
                    }
                    if (this.scaleV >= other.scaleV) {
                        return -1;
                    }
                    return 1;
                } else if (this.scaleU >= other.scaleU) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (this.offsetV >= other.offsetV) {
                return -1;
            } else {
                return 1;
            }
        } else if (this.offsetU >= other.offsetU) {
            return -1;
        } else {
            return 1;
        }
    }
}
