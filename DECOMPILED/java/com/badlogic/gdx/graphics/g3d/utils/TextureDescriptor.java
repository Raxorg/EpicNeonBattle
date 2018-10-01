package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;

public class TextureDescriptor<T extends GLTexture> implements Comparable<TextureDescriptor<T>> {
    public TextureFilter magFilter;
    public TextureFilter minFilter;
    public T texture;
    public TextureWrap uWrap;
    public TextureWrap vWrap;

    public TextureDescriptor(T texture, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
        this.texture = null;
        set(texture, minFilter, magFilter, uWrap, vWrap);
    }

    public TextureDescriptor(T texture) {
        this(texture, null, null, null, null);
    }

    public TextureDescriptor() {
        this.texture = null;
    }

    public void set(T texture, TextureFilter minFilter, TextureFilter magFilter, TextureWrap uWrap, TextureWrap vWrap) {
        this.texture = texture;
        this.minFilter = minFilter;
        this.magFilter = magFilter;
        this.uWrap = uWrap;
        this.vWrap = vWrap;
    }

    public <V extends T> void set(TextureDescriptor<V> other) {
        this.texture = other.texture;
        this.minFilter = other.minFilter;
        this.magFilter = other.magFilter;
        this.uWrap = other.uWrap;
        this.vWrap = other.vWrap;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TextureDescriptor)) {
            return false;
        }
        TextureDescriptor<?> other = (TextureDescriptor) obj;
        if (!(other.texture == this.texture && other.minFilter == this.minFilter && other.magFilter == this.magFilter && other.uWrap == this.uWrap && other.vWrap == this.vWrap)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        long textureObjectHandle = 811 * ((811 * ((811 * ((811 * ((811 * ((long) (this.texture == null ? 0 : this.texture.glTarget))) + ((long) (this.texture == null ? 0 : this.texture.getTextureObjectHandle())))) + ((long) (this.minFilter == null ? 0 : this.minFilter.getGLEnum())))) + ((long) (this.magFilter == null ? 0 : this.magFilter.getGLEnum())))) + ((long) (this.uWrap == null ? 0 : this.uWrap.getGLEnum())));
        if (this.vWrap != null) {
            i = this.vWrap.getGLEnum();
        }
        long result = textureObjectHandle + ((long) i);
        return (int) ((result >> 32) ^ result);
    }

    public int compareTo(TextureDescriptor<T> o) {
        int i = 0;
        if (o == this) {
            return 0;
        }
        int t1 = this.texture == null ? 0 : this.texture.glTarget;
        int t2 = o.texture == null ? 0 : o.texture.glTarget;
        if (t1 != t2) {
            return t1 - t2;
        }
        int h1 = this.texture == null ? 0 : this.texture.getTextureObjectHandle();
        int h2 = o.texture == null ? 0 : o.texture.getTextureObjectHandle();
        if (h1 != h2) {
            return h1 - h2;
        }
        int gLEnum;
        if (this.minFilter != o.minFilter) {
            gLEnum = this.minFilter == null ? 0 : this.minFilter.getGLEnum();
            if (o.minFilter != null) {
                i = o.minFilter.getGLEnum();
            }
            return gLEnum - i;
        } else if (this.magFilter != o.magFilter) {
            gLEnum = this.magFilter == null ? 0 : this.magFilter.getGLEnum();
            if (o.magFilter != null) {
                i = o.magFilter.getGLEnum();
            }
            return gLEnum - i;
        } else if (this.uWrap != o.uWrap) {
            gLEnum = this.uWrap == null ? 0 : this.uWrap.getGLEnum();
            if (o.uWrap != null) {
                i = o.uWrap.getGLEnum();
            }
            return gLEnum - i;
        } else if (this.vWrap == o.vWrap) {
            return 0;
        } else {
            gLEnum = this.vWrap == null ? 0 : this.vWrap.getGLEnum();
            if (o.vWrap != null) {
                i = o.vWrap.getGLEnum();
            }
            return gLEnum - i;
        }
    }
}
