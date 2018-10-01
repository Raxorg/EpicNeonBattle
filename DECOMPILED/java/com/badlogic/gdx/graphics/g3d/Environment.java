package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.graphics.g3d.attributes.DirectionalLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.SpotLightsAttribute;
import com.badlogic.gdx.graphics.g3d.environment.BaseLight;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.ShadowMap;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import java.util.Iterator;

public class Environment extends Attributes {
    public ShadowMap shadowMap;

    public Environment add(BaseLight... lights) {
        for (BaseLight light : lights) {
            add(light);
        }
        return this;
    }

    public Environment add(Array<BaseLight> lights) {
        Iterator it = lights.iterator();
        while (it.hasNext()) {
            add((BaseLight) it.next());
        }
        return this;
    }

    public Environment add(BaseLight light) {
        if (light instanceof DirectionalLight) {
            add((DirectionalLight) light);
        } else if (light instanceof PointLight) {
            add((PointLight) light);
        } else if (light instanceof SpotLight) {
            add((SpotLight) light);
        } else {
            throw new GdxRuntimeException("Unknown light type");
        }
        return this;
    }

    public Environment add(DirectionalLight light) {
        DirectionalLightsAttribute dirLights = (DirectionalLightsAttribute) get(DirectionalLightsAttribute.Type);
        if (dirLights == null) {
            dirLights = new DirectionalLightsAttribute();
            set((Attribute) dirLights);
        }
        dirLights.lights.add(light);
        return this;
    }

    public Environment add(PointLight light) {
        PointLightsAttribute pointLights = (PointLightsAttribute) get(PointLightsAttribute.Type);
        if (pointLights == null) {
            pointLights = new PointLightsAttribute();
            set((Attribute) pointLights);
        }
        pointLights.lights.add(light);
        return this;
    }

    public Environment add(SpotLight light) {
        SpotLightsAttribute spotLights = (SpotLightsAttribute) get(SpotLightsAttribute.Type);
        if (spotLights == null) {
            spotLights = new SpotLightsAttribute();
            set((Attribute) spotLights);
        }
        spotLights.lights.add(light);
        return this;
    }

    public Environment remove(BaseLight... lights) {
        for (BaseLight light : lights) {
            remove(light);
        }
        return this;
    }

    public Environment remove(Array<BaseLight> lights) {
        Iterator it = lights.iterator();
        while (it.hasNext()) {
            remove((BaseLight) it.next());
        }
        return this;
    }

    public Environment remove(BaseLight light) {
        if (light instanceof DirectionalLight) {
            remove((DirectionalLight) light);
        } else if (light instanceof PointLight) {
            remove((PointLight) light);
        } else if (light instanceof SpotLight) {
            remove((SpotLight) light);
        } else {
            throw new GdxRuntimeException("Unknown light type");
        }
        return this;
    }

    public Environment remove(DirectionalLight light) {
        if (has(DirectionalLightsAttribute.Type)) {
            DirectionalLightsAttribute dirLights = (DirectionalLightsAttribute) get(DirectionalLightsAttribute.Type);
            dirLights.lights.removeValue(light, false);
            if (dirLights.lights.size == 0) {
                remove(DirectionalLightsAttribute.Type);
            }
        }
        return this;
    }

    public Environment remove(PointLight light) {
        if (has(PointLightsAttribute.Type)) {
            PointLightsAttribute pointLights = (PointLightsAttribute) get(PointLightsAttribute.Type);
            pointLights.lights.removeValue(light, false);
            if (pointLights.lights.size == 0) {
                remove(PointLightsAttribute.Type);
            }
        }
        return this;
    }

    public Environment remove(SpotLight light) {
        if (has(SpotLightsAttribute.Type)) {
            SpotLightsAttribute spotLights = (SpotLightsAttribute) get(SpotLightsAttribute.Type);
            spotLights.lights.removeValue(light, false);
            if (spotLights.lights.size == 0) {
                remove(SpotLightsAttribute.Type);
            }
        }
        return this;
    }
}
