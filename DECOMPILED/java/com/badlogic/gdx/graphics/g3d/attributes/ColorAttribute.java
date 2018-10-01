package com.badlogic.gdx.graphics.g3d.attributes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class ColorAttribute extends Attribute {
    public static final long Ambient = Attribute.register(AmbientAlias);
    public static final String AmbientAlias = "ambientColor";
    public static final long AmbientLight = Attribute.register(AmbientLightAlias);
    public static final String AmbientLightAlias = "ambientLightColor";
    public static final long Diffuse = Attribute.register(DiffuseAlias);
    public static final String DiffuseAlias = "diffuseColor";
    public static final long Emissive = Attribute.register(EmissiveAlias);
    public static final String EmissiveAlias = "emissiveColor";
    public static final long Fog = Attribute.register(FogAlias);
    public static final String FogAlias = "fogColor";
    protected static long Mask = ((((((Ambient | Diffuse) | Specular) | Emissive) | Reflection) | AmbientLight) | Fog);
    public static final long Reflection = Attribute.register(ReflectionAlias);
    public static final String ReflectionAlias = "reflectionColor";
    public static final long Specular = Attribute.register(SpecularAlias);
    public static final String SpecularAlias = "specularColor";
    public final Color color;

    public static final boolean is(long mask) {
        return (Mask & mask) != Specular;
    }

    public static final ColorAttribute createAmbient(Color color) {
        return new ColorAttribute(Ambient, color);
    }

    public static final ColorAttribute createAmbient(float r, float g, float b, float a) {
        return new ColorAttribute(Ambient, r, g, b, a);
    }

    public static final ColorAttribute createDiffuse(Color color) {
        return new ColorAttribute(Diffuse, color);
    }

    public static final ColorAttribute createDiffuse(float r, float g, float b, float a) {
        return new ColorAttribute(Diffuse, r, g, b, a);
    }

    public static final ColorAttribute createSpecular(Color color) {
        return new ColorAttribute(Specular, color);
    }

    public static final ColorAttribute createSpecular(float r, float g, float b, float a) {
        return new ColorAttribute(Specular, r, g, b, a);
    }

    public static final ColorAttribute createReflection(Color color) {
        return new ColorAttribute(Reflection, color);
    }

    public static final ColorAttribute createReflection(float r, float g, float b, float a) {
        return new ColorAttribute(Reflection, r, g, b, a);
    }

    public ColorAttribute(long type) {
        super(type);
        this.color = new Color();
        if (!is(type)) {
            throw new GdxRuntimeException("Invalid type specified");
        }
    }

    public ColorAttribute(long type, Color color) {
        this(type);
        if (color != null) {
            this.color.set(color);
        }
    }

    public ColorAttribute(long type, float r, float g, float b, float a) {
        this(type);
        this.color.set(r, g, b, a);
    }

    public ColorAttribute(ColorAttribute copyFrom) {
        this(copyFrom.type, copyFrom.color);
    }

    public Attribute copy() {
        return new ColorAttribute(this);
    }

    public int hashCode() {
        return (super.hashCode() * 953) + this.color.toIntBits();
    }

    public int compareTo(Attribute o) {
        if (this.type != o.type) {
            return (int) (this.type - o.type);
        }
        return ((ColorAttribute) o).color.toIntBits() - this.color.toIntBits();
    }
}
