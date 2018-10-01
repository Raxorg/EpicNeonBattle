package com.badlogic.gdx.graphics.g3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import java.util.Comparator;

public class DefaultRenderableSorter implements RenderableSorter, Comparator<Renderable> {
    private Camera camera;
    private final Vector3 tmpV1 = new Vector3();
    private final Vector3 tmpV2 = new Vector3();

    public void sort(Camera camera, Array<Renderable> renderables) {
        this.camera = camera;
        renderables.sort(this);
    }

    public int compare(Renderable o1, Renderable o2) {
        boolean b1;
        boolean b2;
        if (o1.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o1.material.get(BlendingAttribute.Type)).blended) {
            b1 = true;
        } else {
            b1 = false;
        }
        if (o2.material.has(BlendingAttribute.Type) && ((BlendingAttribute) o2.material.get(BlendingAttribute.Type)).blended) {
            b2 = true;
        } else {
            b2 = false;
        }
        if (b1 == b2) {
            o1.worldTransform.getTranslation(this.tmpV1);
            o2.worldTransform.getTranslation(this.tmpV2);
            float dst = (float) (((int) (this.camera.position.dst2(this.tmpV1) * 1000.0f)) - ((int) (this.camera.position.dst2(this.tmpV2) * 1000.0f)));
            int result = dst < 0.0f ? -1 : dst > 0.0f ? 1 : 0;
            if (b1) {
                result = -result;
            }
            return result;
        } else if (b1) {
            return 1;
        } else {
            return -1;
        }
    }
}
