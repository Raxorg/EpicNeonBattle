package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Animation;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.NodeKeyframe;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import java.util.Iterator;

public class BaseAnimationController {
    private static final Transform tmpT = new Transform();
    private static final ObjectMap<Node, Transform> transforms = new ObjectMap();
    private boolean applying = false;
    public final ModelInstance target;
    private final Pool<Transform> transformPool = new Pool<Transform>() {
        protected Transform newObject() {
            return new Transform();
        }
    };

    public static final class Transform implements Poolable {
        public final Quaternion rotation = new Quaternion();
        public final Vector3 scale = new Vector3(1.0f, 1.0f, 1.0f);
        public final Vector3 translation = new Vector3();

        public Transform idt() {
            this.translation.set(0.0f, 0.0f, 0.0f);
            this.rotation.idt();
            this.scale.set(1.0f, 1.0f, 1.0f);
            return this;
        }

        public Transform set(Vector3 t, Quaternion r, Vector3 s) {
            this.translation.set(t);
            this.rotation.set(r);
            this.scale.set(s);
            return this;
        }

        public Transform set(Transform other) {
            return set(other.translation, other.rotation, other.scale);
        }

        public Transform lerp(Transform target, float alpha) {
            return lerp(target.translation, target.rotation, target.scale, alpha);
        }

        public Transform lerp(Vector3 targetT, Quaternion targetR, Vector3 targetS, float alpha) {
            this.translation.lerp(targetT, alpha);
            this.rotation.slerp(targetR, alpha);
            this.scale.lerp(targetS, alpha);
            return this;
        }

        public Matrix4 toMatrix4(Matrix4 out) {
            return out.set(this.translation, this.rotation, this.scale);
        }

        public void reset() {
            idt();
        }

        public String toString() {
            return this.translation.toString() + " - " + this.rotation.toString() + " - " + this.scale.toString();
        }
    }

    public BaseAnimationController(ModelInstance target) {
        this.target = target;
    }

    protected void begin() {
        if (this.applying) {
            throw new GdxRuntimeException("You must call end() after each call to being()");
        }
        this.applying = true;
    }

    protected void apply(Animation animation, float time, float weight) {
        if (this.applying) {
            applyAnimation(transforms, this.transformPool, weight, animation, time);
            return;
        }
        throw new GdxRuntimeException("You must call begin() before adding an animation");
    }

    protected void end() {
        if (this.applying) {
            Iterator it = transforms.entries().iterator();
            while (it.hasNext()) {
                Entry<Node, Transform> entry = (Entry) it.next();
                ((Transform) entry.value).toMatrix4(((Node) entry.key).localTransform);
                this.transformPool.free(entry.value);
            }
            transforms.clear();
            this.target.calculateTransforms();
            this.applying = false;
            return;
        }
        throw new GdxRuntimeException("You must call begin() first");
    }

    protected void applyAnimation(Animation animation, float time) {
        if (this.applying) {
            throw new GdxRuntimeException("Call end() first");
        }
        applyAnimation(null, null, 1.0f, animation, time);
        this.target.calculateTransforms();
    }

    protected void applyAnimations(Animation anim1, float time1, Animation anim2, float time2, float weight) {
        if (anim2 == null || weight == 0.0f) {
            applyAnimation(anim1, time1);
        } else if (anim1 == null || weight == 1.0f) {
            applyAnimation(anim2, time2);
        } else if (this.applying) {
            throw new GdxRuntimeException("Call end() first");
        } else {
            begin();
            apply(anim1, time1, 1.0f);
            apply(anim2, time2, weight);
            end();
        }
    }

    private static final <T> int getFirstKeyframeIndexAtTime(Array<NodeKeyframe<T>> arr, float time) {
        int n = arr.size - 1;
        int i = 0;
        while (i < n) {
            if (time >= ((NodeKeyframe) arr.get(i)).keytime && time <= ((NodeKeyframe) arr.get(i + 1)).keytime) {
                return i;
            }
            i++;
        }
        return 0;
    }

    private static final Vector3 getTranslationAtTime(NodeAnimation nodeAnim, float time, Vector3 out) {
        if (nodeAnim.translation == null) {
            return out.set(nodeAnim.node.translation);
        }
        if (nodeAnim.translation.size == 1) {
            return out.set((Vector3) ((NodeKeyframe) nodeAnim.translation.get(0)).value);
        }
        int index = getFirstKeyframeIndexAtTime(nodeAnim.translation, time);
        NodeKeyframe firstKeyframe = (NodeKeyframe) nodeAnim.translation.get(index);
        out.set((Vector3) firstKeyframe.value);
        index++;
        if (index >= nodeAnim.translation.size) {
            return out;
        }
        NodeKeyframe<Vector3> secondKeyframe = (NodeKeyframe) nodeAnim.translation.get(index);
        out.lerp((Vector3) secondKeyframe.value, (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime));
        return out;
    }

    private static final Quaternion getRotationAtTime(NodeAnimation nodeAnim, float time, Quaternion out) {
        if (nodeAnim.rotation == null) {
            return out.set(nodeAnim.node.rotation);
        }
        if (nodeAnim.rotation.size == 1) {
            return out.set((Quaternion) ((NodeKeyframe) nodeAnim.rotation.get(0)).value);
        }
        int index = getFirstKeyframeIndexAtTime(nodeAnim.rotation, time);
        NodeKeyframe firstKeyframe = (NodeKeyframe) nodeAnim.rotation.get(index);
        out.set((Quaternion) firstKeyframe.value);
        index++;
        if (index >= nodeAnim.rotation.size) {
            return out;
        }
        NodeKeyframe<Quaternion> secondKeyframe = (NodeKeyframe) nodeAnim.rotation.get(index);
        out.slerp((Quaternion) secondKeyframe.value, (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime));
        return out;
    }

    private static final Vector3 getScalingAtTime(NodeAnimation nodeAnim, float time, Vector3 out) {
        if (nodeAnim.scaling == null) {
            return out.set(nodeAnim.node.scale);
        }
        if (nodeAnim.scaling.size == 1) {
            return out.set((Vector3) ((NodeKeyframe) nodeAnim.scaling.get(0)).value);
        }
        int index = getFirstKeyframeIndexAtTime(nodeAnim.scaling, time);
        NodeKeyframe firstKeyframe = (NodeKeyframe) nodeAnim.scaling.get(index);
        out.set((Vector3) firstKeyframe.value);
        index++;
        if (index >= nodeAnim.scaling.size) {
            return out;
        }
        NodeKeyframe<Vector3> secondKeyframe = (NodeKeyframe) nodeAnim.scaling.get(index);
        out.lerp((Vector3) secondKeyframe.value, (time - firstKeyframe.keytime) / (secondKeyframe.keytime - firstKeyframe.keytime));
        return out;
    }

    private static final Transform getNodeAnimationTransform(NodeAnimation nodeAnim, float time) {
        Transform transform = tmpT;
        getTranslationAtTime(nodeAnim, time, transform.translation);
        getRotationAtTime(nodeAnim, time, transform.rotation);
        getScalingAtTime(nodeAnim, time, transform.scale);
        return transform;
    }

    private static final void applyNodeAnimationDirectly(NodeAnimation nodeAnim, float time) {
        Node node = nodeAnim.node;
        node.isAnimated = true;
        getNodeAnimationTransform(nodeAnim, time).toMatrix4(node.localTransform);
    }

    private static final void applyNodeAnimationBlending(NodeAnimation nodeAnim, ObjectMap<Node, Transform> out, Pool<Transform> pool, float alpha, float time) {
        Node node = nodeAnim.node;
        node.isAnimated = true;
        Transform transform = getNodeAnimationTransform(nodeAnim, time);
        Transform t = (Transform) out.get(node, null);
        if (t != null) {
            if (alpha > 0.999999f) {
                t.set(transform);
            } else {
                t.lerp(transform, alpha);
            }
        } else if (alpha > 0.999999f) {
            out.put(node, ((Transform) pool.obtain()).set(transform));
        } else {
            out.put(node, ((Transform) pool.obtain()).set(node.translation, node.rotation, node.scale).lerp(transform, alpha));
        }
    }

    protected static void applyAnimation(ObjectMap<Node, Transform> out, Pool<Transform> pool, float alpha, Animation animation, float time) {
        Iterator it;
        if (out == null) {
            it = animation.nodeAnimations.iterator();
            while (it.hasNext()) {
                applyNodeAnimationDirectly((NodeAnimation) it.next(), time);
            }
            return;
        }
        it = out.keys().iterator();
        while (it.hasNext()) {
            ((Node) it.next()).isAnimated = false;
        }
        it = animation.nodeAnimations.iterator();
        while (it.hasNext()) {
            applyNodeAnimationBlending((NodeAnimation) it.next(), out, pool, alpha, time);
        }
        Iterator it2 = out.entries().iterator();
        while (it2.hasNext()) {
            Entry<Node, Transform> e = (Entry) it2.next();
            if (!((Node) e.key).isAnimated) {
                ((Node) e.key).isAnimated = true;
                ((Transform) e.value).lerp(((Node) e.key).translation, ((Node) e.key).rotation, ((Node) e.key).scale, alpha);
            }
        }
    }

    protected void removeAnimation(Animation animation) {
        Iterator it = animation.nodeAnimations.iterator();
        while (it.hasNext()) {
            ((NodeAnimation) it.next()).node.isAnimated = false;
        }
    }
}
