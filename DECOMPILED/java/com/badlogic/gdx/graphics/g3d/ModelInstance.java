package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.MeshPart;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import java.util.Iterator;

public class ModelInstance implements RenderableProvider {
    public static boolean defaultShareKeyframes = true;
    public final Array<Animation> animations;
    public final Array<Material> materials;
    public final Model model;
    private ObjectMap<NodePart, ArrayMap<Node, Matrix4>> nodePartBones;
    public final Array<Node> nodes;
    public Matrix4 transform;
    public Object userData;

    public ModelInstance(Model model) {
        this(model, (String[]) null);
    }

    public ModelInstance(Model model, String nodeId, boolean mergeTransform) {
        this(model, null, nodeId, false, false, mergeTransform);
    }

    public ModelInstance(Model model, Matrix4 transform, String nodeId, boolean mergeTransform) {
        this(model, transform, nodeId, false, false, mergeTransform);
    }

    public ModelInstance(Model model, String nodeId, boolean parentTransform, boolean mergeTransform) {
        this(model, null, nodeId, true, parentTransform, mergeTransform);
    }

    public ModelInstance(Model model, Matrix4 transform, String nodeId, boolean parentTransform, boolean mergeTransform) {
        this(model, transform, nodeId, true, parentTransform, mergeTransform);
    }

    public ModelInstance(Model model, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
        this(model, null, nodeId, recursive, parentTransform, mergeTransform);
    }

    public ModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform) {
        this(model, transform, nodeId, recursive, parentTransform, mergeTransform, defaultShareKeyframes);
    }

    public ModelInstance(Model model, Matrix4 transform, String nodeId, boolean recursive, boolean parentTransform, boolean mergeTransform, boolean shareKeyframes) {
        this.materials = new Array();
        this.nodes = new Array();
        this.animations = new Array();
        this.nodePartBones = new ObjectMap();
        this.model = model;
        if (transform == null) {
            transform = new Matrix4();
        }
        this.transform = transform;
        this.nodePartBones.clear();
        Node node = model.getNode(nodeId, recursive);
        Array array = this.nodes;
        Node copy = copyNode(node);
        array.add(copy);
        if (mergeTransform) {
            this.transform.mul(parentTransform ? node.globalTransform : node.localTransform);
            copy.translation.set(0.0f, 0.0f, 0.0f);
            copy.rotation.idt();
            copy.scale.set(1.0f, 1.0f, 1.0f);
        } else if (parentTransform && copy.hasParent()) {
            this.transform.mul(node.getParent().globalTransform);
        }
        setBones();
        copyAnimations(model.animations, shareKeyframes);
        calculateTransforms();
    }

    public ModelInstance(Model model, String... rootNodeIds) {
        this(model, null, rootNodeIds);
    }

    public ModelInstance(Model model, Matrix4 transform, String... rootNodeIds) {
        this.materials = new Array();
        this.nodes = new Array();
        this.animations = new Array();
        this.nodePartBones = new ObjectMap();
        this.model = model;
        if (transform == null) {
            transform = new Matrix4();
        }
        this.transform = transform;
        if (rootNodeIds == null) {
            copyNodes(model.nodes);
        } else {
            copyNodes(model.nodes, rootNodeIds);
        }
        copyAnimations(model.animations, defaultShareKeyframes);
        calculateTransforms();
    }

    public ModelInstance(Model model, Array<String> rootNodeIds) {
        this(model, null, (Array) rootNodeIds);
    }

    public ModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds) {
        this(model, transform, (Array) rootNodeIds, defaultShareKeyframes);
    }

    public ModelInstance(Model model, Matrix4 transform, Array<String> rootNodeIds, boolean shareKeyframes) {
        this.materials = new Array();
        this.nodes = new Array();
        this.animations = new Array();
        this.nodePartBones = new ObjectMap();
        this.model = model;
        if (transform == null) {
            transform = new Matrix4();
        }
        this.transform = transform;
        copyNodes(model.nodes, (Array) rootNodeIds);
        copyAnimations(model.animations, shareKeyframes);
        calculateTransforms();
    }

    public ModelInstance(Model model, Vector3 position) {
        this(model);
        this.transform.setToTranslation(position);
    }

    public ModelInstance(Model model, float x, float y, float z) {
        this(model);
        this.transform.setToTranslation(x, y, z);
    }

    public ModelInstance(Model model, Matrix4 transform) {
        this(model, transform, (String[]) null);
    }

    public ModelInstance(ModelInstance copyFrom) {
        this(copyFrom, copyFrom.transform.cpy());
    }

    public ModelInstance(ModelInstance copyFrom, Matrix4 transform) {
        this(copyFrom, transform, defaultShareKeyframes);
    }

    public ModelInstance(ModelInstance copyFrom, Matrix4 transform, boolean shareKeyframes) {
        this.materials = new Array();
        this.nodes = new Array();
        this.animations = new Array();
        this.nodePartBones = new ObjectMap();
        this.model = copyFrom.model;
        if (transform == null) {
            transform = new Matrix4();
        }
        this.transform = transform;
        copyNodes(copyFrom.nodes);
        copyAnimations(copyFrom.animations, shareKeyframes);
        calculateTransforms();
    }

    public ModelInstance copy() {
        return new ModelInstance(this);
    }

    private void copyNodes(Array<Node> nodes) {
        this.nodePartBones.clear();
        int n = nodes.size;
        for (int i = 0; i < n; i++) {
            this.nodes.add(copyNode((Node) nodes.get(i)));
        }
        setBones();
    }

    private void copyNodes(Array<Node> nodes, String... nodeIds) {
        this.nodePartBones.clear();
        int n = nodes.size;
        for (int i = 0; i < n; i++) {
            Node node = (Node) nodes.get(i);
            for (String nodeId : nodeIds) {
                if (nodeId.equals(node.id)) {
                    this.nodes.add(copyNode(node));
                    break;
                }
            }
        }
        setBones();
    }

    private void copyNodes(Array<Node> nodes, Array<String> nodeIds) {
        this.nodePartBones.clear();
        int n = nodes.size;
        for (int i = 0; i < n; i++) {
            Node node = (Node) nodes.get(i);
            Iterator it = nodeIds.iterator();
            while (it.hasNext()) {
                if (((String) it.next()).equals(node.id)) {
                    this.nodes.add(copyNode(node));
                    break;
                }
            }
        }
        setBones();
    }

    private void setBones() {
        Iterator it = this.nodePartBones.entries().iterator();
        while (it.hasNext()) {
            Entry<NodePart, ArrayMap<Node, Matrix4>> e = (Entry) it.next();
            if (((NodePart) e.key).invBoneBindTransforms == null) {
                ((NodePart) e.key).invBoneBindTransforms = new ArrayMap(true, ((ArrayMap) e.value).size, Node.class, Matrix4.class);
            }
            ((NodePart) e.key).invBoneBindTransforms.clear();
            Iterator it2 = ((ArrayMap) e.value).entries().iterator();
            while (it2.hasNext()) {
                Entry<Node, Matrix4> b = (Entry) it2.next();
                ((NodePart) e.key).invBoneBindTransforms.put(getNode(((Node) b.key).id), b.value);
            }
            ((NodePart) e.key).bones = new Matrix4[((ArrayMap) e.value).size];
            for (int i = 0; i < ((NodePart) e.key).bones.length; i++) {
                ((NodePart) e.key).bones[i] = new Matrix4();
            }
        }
    }

    private Node copyNode(Node node) {
        Node copy = new Node();
        copy.id = node.id;
        copy.inheritTransform = node.inheritTransform;
        copy.translation.set(node.translation);
        copy.rotation.set(node.rotation);
        copy.scale.set(node.scale);
        copy.localTransform.set(node.localTransform);
        copy.globalTransform.set(node.globalTransform);
        Iterator it = node.parts.iterator();
        while (it.hasNext()) {
            copy.parts.add(copyNodePart((NodePart) it.next()));
        }
        for (Node child : node.getChildren()) {
            copy.addChild(copyNode(child));
        }
        return copy;
    }

    private NodePart copyNodePart(NodePart nodePart) {
        NodePart copy = new NodePart();
        copy.meshPart = new MeshPart();
        copy.meshPart.id = nodePart.meshPart.id;
        copy.meshPart.indexOffset = nodePart.meshPart.indexOffset;
        copy.meshPart.numVertices = nodePart.meshPart.numVertices;
        copy.meshPart.primitiveType = nodePart.meshPart.primitiveType;
        copy.meshPart.mesh = nodePart.meshPart.mesh;
        if (nodePart.invBoneBindTransforms != null) {
            this.nodePartBones.put(copy, nodePart.invBoneBindTransforms);
        }
        int index = this.materials.indexOf(nodePart.material, false);
        if (index < 0) {
            Array array = this.materials;
            Material copy2 = nodePart.material.copy();
            copy.material = copy2;
            array.add(copy2);
        } else {
            copy.material = (Material) this.materials.get(index);
        }
        return copy;
    }

    private void copyAnimations(Iterable<Animation> source, boolean shareKeyframes) {
        for (Animation anim : source) {
            Animation animation = new Animation();
            animation.id = anim.id;
            animation.duration = anim.duration;
            Iterator it = anim.nodeAnimations.iterator();
            while (it.hasNext()) {
                NodeAnimation nanim = (NodeAnimation) it.next();
                Node node = getNode(nanim.node.id);
                if (node != null) {
                    NodeAnimation nodeAnim = new NodeAnimation();
                    nodeAnim.node = node;
                    if (shareKeyframes) {
                        nodeAnim.translation = nanim.translation;
                        nodeAnim.rotation = nanim.rotation;
                        nodeAnim.scaling = nanim.scaling;
                    } else {
                        Iterator it2;
                        NodeKeyframe<Vector3> kf;
                        if (nanim.translation != null) {
                            nodeAnim.translation = new Array();
                            it2 = nanim.translation.iterator();
                            while (it2.hasNext()) {
                                kf = (NodeKeyframe) it2.next();
                                nodeAnim.translation.add(new NodeKeyframe(kf.keytime, kf.value));
                            }
                        }
                        if (nanim.rotation != null) {
                            nodeAnim.rotation = new Array();
                            it2 = nanim.rotation.iterator();
                            while (it2.hasNext()) {
                                NodeKeyframe<Quaternion> kf2 = (NodeKeyframe) it2.next();
                                nodeAnim.rotation.add(new NodeKeyframe(kf2.keytime, kf2.value));
                            }
                        }
                        if (nanim.scaling != null) {
                            nodeAnim.scaling = new Array();
                            it2 = nanim.scaling.iterator();
                            while (it2.hasNext()) {
                                kf = (NodeKeyframe) it2.next();
                                nodeAnim.scaling.add(new NodeKeyframe(kf.keytime, kf.value));
                            }
                        }
                    }
                    if (nodeAnim.translation != null || nodeAnim.rotation != null || nodeAnim.scaling != null) {
                        animation.nodeAnimations.add(nodeAnim);
                    }
                }
            }
            if (animation.nodeAnimations.size > 0) {
                this.animations.add(animation);
            }
        }
    }

    public void getRenderables(Array<Renderable> renderables, Pool<Renderable> pool) {
        Iterator it = this.nodes.iterator();
        while (it.hasNext()) {
            getRenderables((Node) it.next(), renderables, pool);
        }
    }

    public Renderable getRenderable(Renderable out) {
        return getRenderable(out, (Node) this.nodes.get(0));
    }

    public Renderable getRenderable(Renderable out, Node node) {
        return getRenderable(out, node, (NodePart) node.parts.get(0));
    }

    public Renderable getRenderable(Renderable out, Node node, NodePart nodePart) {
        nodePart.setRenderable(out);
        if (nodePart.bones == null && this.transform != null) {
            out.worldTransform.set(this.transform).mul(node.globalTransform);
        } else if (this.transform != null) {
            out.worldTransform.set(this.transform);
        } else {
            out.worldTransform.idt();
        }
        out.userData = this.userData;
        return out;
    }

    protected void getRenderables(Node node, Array<Renderable> renderables, Pool<Renderable> pool) {
        if (node.parts.size > 0) {
            Iterator it = node.parts.iterator();
            while (it.hasNext()) {
                NodePart nodePart = (NodePart) it.next();
                if (nodePart.enabled) {
                    renderables.add(getRenderable((Renderable) pool.obtain(), node, nodePart));
                }
            }
        }
        for (Node child : node.getChildren()) {
            getRenderables(child, renderables, pool);
        }
    }

    public void calculateTransforms() {
        int i;
        int n = this.nodes.size;
        for (i = 0; i < n; i++) {
            ((Node) this.nodes.get(i)).calculateTransforms(true);
        }
        for (i = 0; i < n; i++) {
            ((Node) this.nodes.get(i)).calculateBoneTransforms(true);
        }
    }

    public BoundingBox calculateBoundingBox(BoundingBox out) {
        out.inf();
        return extendBoundingBox(out);
    }

    public BoundingBox extendBoundingBox(BoundingBox out) {
        int n = this.nodes.size;
        for (int i = 0; i < n; i++) {
            ((Node) this.nodes.get(i)).extendBoundingBox(out);
        }
        return out;
    }

    public Animation getAnimation(String id) {
        return getAnimation(id, true);
    }

    public Animation getAnimation(String id, boolean ignoreCase) {
        int n = this.animations.size;
        int i;
        Animation animation;
        if (ignoreCase) {
            for (i = 0; i < n; i++) {
                animation = (Animation) this.animations.get(i);
                if (animation.id.equalsIgnoreCase(id)) {
                    return animation;
                }
            }
        } else {
            for (i = 0; i < n; i++) {
                animation = (Animation) this.animations.get(i);
                if (animation.id.equals(id)) {
                    return animation;
                }
            }
        }
        return null;
    }

    public Material getMaterial(String id) {
        return getMaterial(id, true);
    }

    public Material getMaterial(String id, boolean ignoreCase) {
        int n = this.materials.size;
        int i;
        Material material;
        if (ignoreCase) {
            for (i = 0; i < n; i++) {
                material = (Material) this.materials.get(i);
                if (material.id.equalsIgnoreCase(id)) {
                    return material;
                }
            }
        } else {
            for (i = 0; i < n; i++) {
                material = (Material) this.materials.get(i);
                if (material.id.equals(id)) {
                    return material;
                }
            }
        }
        return null;
    }

    public Node getNode(String id) {
        return getNode(id, true);
    }

    public Node getNode(String id, boolean recursive) {
        return getNode(id, recursive, false);
    }

    public Node getNode(String id, boolean recursive, boolean ignoreCase) {
        return Node.getNode(this.nodes, id, recursive, ignoreCase);
    }
}
