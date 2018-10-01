package com.badlogic.gdx.graphics.g3d.model;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Iterator;

public class Node {
    private final Array<Node> children = new Array(2);
    public final Matrix4 globalTransform = new Matrix4();
    public String id;
    public boolean inheritTransform = true;
    public boolean isAnimated;
    public final Matrix4 localTransform = new Matrix4();
    protected Node parent;
    public Array<NodePart> parts = new Array(2);
    public final Quaternion rotation = new Quaternion(0.0f, 0.0f, 0.0f, 1.0f);
    public final Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
    public final Vector3 translation = new Vector3();

    public Matrix4 calculateLocalTransform() {
        if (!this.isAnimated) {
            this.localTransform.set(this.translation, this.rotation, this.scale);
        }
        return this.localTransform;
    }

    public Matrix4 calculateWorldTransform() {
        if (!this.inheritTransform || this.parent == null) {
            this.globalTransform.set(this.localTransform);
        } else {
            this.globalTransform.set(this.parent.globalTransform).mul(this.localTransform);
        }
        return this.globalTransform;
    }

    public void calculateTransforms(boolean recursive) {
        calculateLocalTransform();
        calculateWorldTransform();
        if (recursive) {
            Iterator it = this.children.iterator();
            while (it.hasNext()) {
                ((Node) it.next()).calculateTransforms(true);
            }
        }
    }

    public void calculateBoneTransforms(boolean recursive) {
        Iterator it = this.parts.iterator();
        while (it.hasNext()) {
            NodePart part = (NodePart) it.next();
            if (!(part.invBoneBindTransforms == null || part.bones == null || part.invBoneBindTransforms.size != part.bones.length)) {
                int n = part.invBoneBindTransforms.size;
                for (int i = 0; i < n; i++) {
                    part.bones[i].set(((Node[]) part.invBoneBindTransforms.keys)[i].globalTransform).mul(((Matrix4[]) part.invBoneBindTransforms.values)[i]);
                }
            }
        }
        if (recursive) {
            Iterator it2 = this.children.iterator();
            while (it2.hasNext()) {
                ((Node) it2.next()).calculateBoneTransforms(true);
            }
        }
    }

    public BoundingBox calculateBoundingBox(BoundingBox out) {
        out.inf();
        return extendBoundingBox(out);
    }

    public BoundingBox calculateBoundingBox(BoundingBox out, boolean transform) {
        out.inf();
        return extendBoundingBox(out, transform);
    }

    public BoundingBox extendBoundingBox(BoundingBox out) {
        return extendBoundingBox(out, true);
    }

    public BoundingBox extendBoundingBox(BoundingBox out, boolean transform) {
        int i;
        int partCount = this.parts.size;
        for (i = 0; i < partCount; i++) {
            NodePart part = (NodePart) this.parts.get(i);
            if (part.enabled) {
                MeshPart meshPart = part.meshPart;
                if (transform) {
                    meshPart.mesh.extendBoundingBox(out, meshPart.indexOffset, meshPart.numVertices, this.globalTransform);
                } else {
                    meshPart.mesh.extendBoundingBox(out, meshPart.indexOffset, meshPart.numVertices);
                }
            }
        }
        int childCount = this.children.size;
        for (i = 0; i < childCount; i++) {
            ((Node) this.children.get(i)).extendBoundingBox(out);
        }
        return out;
    }

    public <T extends Node> void attachTo(T parent) {
        parent.addChild(this);
    }

    public void detach() {
        if (this.parent != null) {
            this.parent.removeChild(this);
            this.parent = null;
        }
    }

    public boolean hasChildren() {
        return this.children != null && this.children.size > 0;
    }

    public int getChildCount() {
        return this.children.size;
    }

    public Node getChild(int index) {
        return (Node) this.children.get(index);
    }

    public Node getChild(String id, boolean recursive, boolean ignoreCase) {
        return getNode(this.children, id, recursive, ignoreCase);
    }

    public <T extends Node> int addChild(T child) {
        return insertChild(-1, child);
    }

    public <T extends Node> int addChildren(Iterable<T> nodes) {
        return insertChildren(-1, nodes);
    }

    public <T extends Node> int insertChild(int index, T child) {
        for (T p = this; p != null; p = p.getParent()) {
            if (p == child) {
                throw new GdxRuntimeException("Cannot add a parent as a child");
            }
        }
        Node p2 = child.getParent();
        if (p2 == null || p2.removeChild(child)) {
            if (index < 0 || index >= this.children.size) {
                index = this.children.size;
                this.children.add(child);
            } else {
                this.children.insert(index, child);
            }
            child.parent = this;
            return index;
        }
        throw new GdxRuntimeException("Could not remove child from its current parent");
    }

    public <T extends Node> int insertChildren(int index, Iterable<T> nodes) {
        if (index < 0 || index > this.children.size) {
            index = this.children.size;
        }
        int i = index;
        for (T child : nodes) {
            int i2 = i + 1;
            insertChild(i, child);
            i = i2;
        }
        return index;
    }

    public <T extends Node> boolean removeChild(T child) {
        if (!this.children.removeValue(child, true)) {
            return false;
        }
        child.parent = null;
        return true;
    }

    public Iterable<Node> getChildren() {
        return this.children;
    }

    public Node getParent() {
        return this.parent;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public static Node getNode(Array<Node> nodes, String id, boolean recursive, boolean ignoreCase) {
        int i;
        Node node;
        int n = nodes.size;
        if (ignoreCase) {
            for (i = 0; i < n; i++) {
                node = (Node) nodes.get(i);
                if (node.id.equalsIgnoreCase(id)) {
                    return node;
                }
            }
        } else {
            for (i = 0; i < n; i++) {
                node = (Node) nodes.get(i);
                if (node.id.equals(id)) {
                    return node;
                }
            }
        }
        if (recursive) {
            for (i = 0; i < n; i++) {
                node = getNode(((Node) nodes.get(i)).children, id, true, ignoreCase);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }
}
