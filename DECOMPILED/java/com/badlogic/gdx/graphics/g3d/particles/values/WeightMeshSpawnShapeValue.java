package com.badlogic.gdx.graphics.g3d.particles.values;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.particles.values.MeshSpawnShapeValue.Triangle;
import com.badlogic.gdx.math.CumulativeDistribution;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public final class WeightMeshSpawnShapeValue extends MeshSpawnShapeValue {
    private CumulativeDistribution<Triangle> distribution = new CumulativeDistribution();

    public WeightMeshSpawnShapeValue(WeightMeshSpawnShapeValue value) {
        super(value);
        load(value);
    }

    public void init() {
        calculateWeights();
    }

    public void calculateWeights() {
        this.distribution.clear();
        VertexAttributes attributes = this.mesh.getVertexAttributes();
        int indicesCount = this.mesh.getNumIndices();
        int vertexCount = this.mesh.getNumVertices();
        short vertexSize = (short) (attributes.vertexSize / 4);
        short positionOffset = (short) (attributes.findByUsage(1).offset / 4);
        float[] vertices = new float[(vertexCount * vertexSize)];
        this.mesh.getVertices(vertices);
        int i;
        int p1Offset;
        int p2Offset;
        int p3Offset;
        float x1;
        float y1;
        float z1;
        float x2;
        float y2;
        float z2;
        float x3;
        float y3;
        float z3;
        float area;
        if (indicesCount > 0) {
            short[] indices = new short[indicesCount];
            this.mesh.getIndices(indices);
            for (i = 0; i < indicesCount; i += 3) {
                p1Offset = (indices[i] * vertexSize) + positionOffset;
                p2Offset = (indices[i + 1] * vertexSize) + positionOffset;
                p3Offset = (indices[i + 2] * vertexSize) + positionOffset;
                x1 = vertices[p1Offset];
                y1 = vertices[p1Offset + 1];
                z1 = vertices[p1Offset + 2];
                x2 = vertices[p2Offset];
                y2 = vertices[p2Offset + 1];
                z2 = vertices[p2Offset + 2];
                x3 = vertices[p3Offset];
                y3 = vertices[p3Offset + 1];
                z3 = vertices[p3Offset + 2];
                area = Math.abs(((((y2 - y3) * x1) + ((y3 - y1) * x2)) + ((y1 - y2) * x3)) / 2.0f);
                this.distribution.add(new Triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3), area);
            }
        } else {
            for (i = 0; i < vertexCount; i += vertexSize) {
                p1Offset = i + positionOffset;
                p2Offset = p1Offset + vertexSize;
                p3Offset = p2Offset + vertexSize;
                x1 = vertices[p1Offset];
                y1 = vertices[p1Offset + 1];
                z1 = vertices[p1Offset + 2];
                x2 = vertices[p2Offset];
                y2 = vertices[p2Offset + 1];
                z2 = vertices[p2Offset + 2];
                x3 = vertices[p3Offset];
                y3 = vertices[p3Offset + 1];
                z3 = vertices[p3Offset + 2];
                area = Math.abs(((((y2 - y3) * x1) + ((y3 - y1) * x2)) + ((y1 - y2) * x3)) / 2.0f);
                this.distribution.add(new Triangle(x1, y1, z1, x2, y2, z2, x3, y3, z3), area);
            }
        }
        this.distribution.generateNormalized();
    }

    public void spawnAux(Vector3 vector, float percent) {
        Triangle t = (Triangle) this.distribution.value();
        float a = MathUtils.random();
        float b = MathUtils.random();
        vector.set((t.x1 + ((t.x2 - t.x1) * a)) + ((t.x3 - t.x1) * b), (t.y1 + ((t.y2 - t.y1) * a)) + ((t.y3 - t.y1) * b), (t.z1 + ((t.z2 - t.z1) * a)) + ((t.z3 - t.z1) * b));
    }

    public SpawnShapeValue copy() {
        return new WeightMeshSpawnShapeValue(this);
    }
}
