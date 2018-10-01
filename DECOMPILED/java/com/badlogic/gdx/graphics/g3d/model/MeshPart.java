package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.graphics.Mesh;

public class MeshPart {
    public String id;
    public int indexOffset;
    public Mesh mesh;
    public int numVertices;
    public int primitiveType;

    public MeshPart(String id, Mesh mesh, int offset, int size, int type) {
        this.id = id;
        this.mesh = mesh;
        this.indexOffset = offset;
        this.numVertices = size;
        this.primitiveType = type;
    }

    public MeshPart(MeshPart copyFrom) {
        this(copyFrom.id, copyFrom.mesh, copyFrom.indexOffset, copyFrom.numVertices, copyFrom.primitiveType);
    }

    public boolean equals(MeshPart other) {
        return other == this || (other != null && other.mesh == this.mesh && other.primitiveType == this.primitiveType && other.indexOffset == this.indexOffset && other.numVertices == this.numVertices);
    }

    public boolean equals(Object arg0) {
        if (arg0 == null) {
            return false;
        }
        if (arg0 == this) {
            return true;
        }
        if (arg0 instanceof MeshPart) {
            return equals((MeshPart) arg0);
        }
        return false;
    }
}
