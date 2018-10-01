package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue.Triangle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public final class UnweightedMeshSpawnShapeValue extends MeshSpawnShapeValue {
    private short[] indices;
    private int positionOffset;
    private int triangleCount;
    private int vertexCount;
    private int vertexSize;
    private float[] vertices;

    public UnweightedMeshSpawnShapeValue(UnweightedMeshSpawnShapeValue value) {
        super(value);
        load(value);
    }

    public void setMesh(Mesh mesh, Model model) {
        super.setMesh(mesh, model);
        this.vertexSize = mesh.getVertexSize() / 4;
        this.positionOffset = mesh.getVertexAttribute(1).offset / 4;
        int indicesCount = mesh.getNumIndices();
        if (indicesCount > 0) {
            this.indices = new short[indicesCount];
            mesh.getIndices(this.indices);
            this.triangleCount = this.indices.length / 3;
        } else {
            this.indices = null;
        }
        this.vertexCount = mesh.getNumVertices();
        this.vertices = new float[(this.vertexCount * this.vertexSize)];
        mesh.getVertices(this.vertices);
    }

    public void spawnAux(Vector3 vector, float percent) {
        if (this.indices == null) {
            int p1Offset = (MathUtils.random(this.vertexCount - 3) * this.vertexSize) + this.positionOffset;
            int p2Offset = p1Offset + this.vertexSize;
            int p3Offset = p2Offset + this.vertexSize;
            Triangle.pick(this.vertices[p1Offset], this.vertices[p1Offset + 1], this.vertices[p1Offset + 2], this.vertices[p2Offset], this.vertices[p2Offset + 1], this.vertices[p2Offset + 2], this.vertices[p3Offset], this.vertices[p3Offset + 1], this.vertices[p3Offset + 2], vector);
            return;
        }
        int triangleIndex = MathUtils.random(this.triangleCount - 1) * 3;
        p1Offset = (this.indices[triangleIndex] * this.vertexSize) + this.positionOffset;
        p2Offset = (this.indices[triangleIndex + 1] * this.vertexSize) + this.positionOffset;
        p3Offset = (this.indices[triangleIndex + 2] * this.vertexSize) + this.positionOffset;
        Triangle.pick(this.vertices[p1Offset], this.vertices[p1Offset + 1], this.vertices[p1Offset + 2], this.vertices[p2Offset], this.vertices[p2Offset + 1], this.vertices[p2Offset + 2], this.vertices[p3Offset], this.vertices[p3Offset + 1], this.vertices[p3Offset + 2], vector);
    }

    public SpawnShapeValue copy() {
        return new UnweightedMeshSpawnShapeValue(this);
    }
}
