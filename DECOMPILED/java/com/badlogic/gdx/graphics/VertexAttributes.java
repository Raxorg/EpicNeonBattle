package com.badlogic.gdx.graphics;

import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class VertexAttributes implements Iterable<VertexAttribute>, Comparable<VertexAttributes> {
    private final VertexAttribute[] attributes;
    private ReadonlyIterable<VertexAttribute> iterable;
    private long mask = -1;
    public final int vertexSize;

    private static class ReadonlyIterable<T> implements Iterable<T> {
        private final T[] array;
        private ReadonlyIterator iterator1;
        private ReadonlyIterator iterator2;

        public ReadonlyIterable(T[] array) {
            this.array = array;
        }

        public Iterator<T> iterator() {
            if (this.iterator1 == null) {
                this.iterator1 = new ReadonlyIterator(this.array);
                this.iterator2 = new ReadonlyIterator(this.array);
            }
            if (this.iterator1.valid) {
                this.iterator2.index = 0;
                this.iterator2.valid = true;
                this.iterator1.valid = false;
                return this.iterator2;
            }
            this.iterator1.index = 0;
            this.iterator1.valid = true;
            this.iterator2.valid = false;
            return this.iterator1;
        }
    }

    private static class ReadonlyIterator<T> implements Iterator<T>, Iterable<T> {
        private final T[] array;
        int index;
        boolean valid = true;

        public ReadonlyIterator(T[] array) {
            this.array = array;
        }

        public boolean hasNext() {
            if (this.valid) {
                return this.index < this.array.length;
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public T next() {
            if (this.index >= this.array.length) {
                throw new NoSuchElementException(String.valueOf(this.index));
            } else if (this.valid) {
                Object[] objArr = this.array;
                int i = this.index;
                this.index = i + 1;
                return objArr[i];
            } else {
                throw new GdxRuntimeException("#iterator() cannot be used nested.");
            }
        }

        public void remove() {
            throw new GdxRuntimeException("Remove not allowed.");
        }

        public void reset() {
            this.index = 0;
        }

        public Iterator<T> iterator() {
            return this;
        }
    }

    public static final class Usage {
        public static final int BiNormal = 256;
        public static final int BoneWeight = 64;
        public static final int ColorPacked = 4;
        public static final int ColorUnpacked = 2;
        public static final int Generic = 32;
        public static final int Normal = 8;
        public static final int Position = 1;
        public static final int Tangent = 128;
        public static final int TextureCoordinates = 16;
    }

    public VertexAttributes(VertexAttribute... attributes) {
        if (attributes.length == 0) {
            throw new IllegalArgumentException("attributes must be >= 1");
        }
        VertexAttribute[] list = new VertexAttribute[attributes.length];
        for (int i = 0; i < attributes.length; i++) {
            list[i] = attributes[i];
        }
        this.attributes = list;
        this.vertexSize = calculateOffsets();
    }

    public int getOffset(int usage, int defaultIfNotFound) {
        VertexAttribute vertexAttribute = findByUsage(usage);
        return vertexAttribute == null ? defaultIfNotFound : vertexAttribute.offset / 4;
    }

    public int getOffset(int usage) {
        return getOffset(usage, 0);
    }

    public VertexAttribute findByUsage(int usage) {
        int len = size();
        for (int i = 0; i < len; i++) {
            if (get(i).usage == usage) {
                return get(i);
            }
        }
        return null;
    }

    private int calculateOffsets() {
        int count = 0;
        for (VertexAttribute attribute : this.attributes) {
            attribute.offset = count;
            if (attribute.usage == 4) {
                count += 4;
            } else {
                count += attribute.numComponents * 4;
            }
        }
        return count;
    }

    public int size() {
        return this.attributes.length;
    }

    public VertexAttribute get(int index) {
        return this.attributes[index];
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < this.attributes.length; i++) {
            builder.append("(");
            builder.append(this.attributes[i].alias);
            builder.append(", ");
            builder.append(this.attributes[i].usage);
            builder.append(", ");
            builder.append(this.attributes[i].numComponents);
            builder.append(", ");
            builder.append(this.attributes[i].offset);
            builder.append(")");
            builder.append("\n");
        }
        builder.append("]");
        return builder.toString();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VertexAttributes)) {
            return false;
        }
        VertexAttributes other = (VertexAttributes) obj;
        if (this.attributes.length != other.attributes.length) {
            return false;
        }
        for (int i = 0; i < this.attributes.length; i++) {
            if (!this.attributes[i].equals(other.attributes[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        long result = (long) (this.attributes.length * 61);
        for (VertexAttribute hashCode : this.attributes) {
            result = (61 * result) + ((long) hashCode.hashCode());
        }
        return (int) ((result >> 32) ^ result);
    }

    public long getMask() {
        if (this.mask == -1) {
            long result = 0;
            for (VertexAttribute vertexAttribute : this.attributes) {
                result |= (long) vertexAttribute.usage;
            }
            this.mask = result;
        }
        return this.mask;
    }

    public int compareTo(VertexAttributes o) {
        int i = 1;
        if (this.attributes.length != o.attributes.length) {
            return this.attributes.length - o.attributes.length;
        }
        long m1 = getMask();
        long m2 = o.getMask();
        if (m1 == m2) {
            int i2 = this.attributes.length - 1;
            while (i2 >= 0) {
                VertexAttribute va0 = this.attributes[i2];
                VertexAttribute va1 = o.attributes[i2];
                if (va0.usage != va1.usage) {
                    return va0.usage - va1.usage;
                }
                if (va0.unit != va1.unit) {
                    return va0.unit - va1.unit;
                }
                if (va0.numComponents != va1.numComponents) {
                    return va0.numComponents - va1.numComponents;
                }
                if (va0.normalized != va1.normalized) {
                    if (!va0.normalized) {
                        i = -1;
                    }
                    return i;
                } else if (va0.type != va1.type) {
                    return va0.type - va1.type;
                } else {
                    i2--;
                }
            }
            return 0;
        } else if (m1 >= m2) {
            return 1;
        } else {
            return -1;
        }
    }

    public Iterator<VertexAttribute> iterator() {
        if (this.iterable == null) {
            this.iterable = new ReadonlyIterable(this.attributes);
        }
        return this.iterable.iterator();
    }
}
