package com.badlogic.gdx.graphics.g3d.model.data;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

public class ModelNode {
    public int boneId = -1;
    public ModelNode[] children;
    public String id;
    public String meshId;
    public ModelNodePart[] parts;
    public Quaternion rotation;
    public Vector3 scale;
    public Vector3 translation;
}
