package com.badlogic.gdx.graphics.g3d.shaders;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.TextureDescriptor;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;

public abstract class BaseShader implements Shader {
    private final IntIntMap attributes = new IntIntMap();
    public Camera camera;
    private Attributes combinedAttributes = new Attributes();
    public RenderContext context;
    private Mesh currentMesh;
    private final IntArray globalUniforms = new IntArray();
    private final IntArray localUniforms = new IntArray();
    private int[] locations;
    public ShaderProgram program;
    private final Array<Setter> setters = new Array();
    private final IntArray tempArray = new IntArray();
    private final Array<String> uniforms = new Array();
    private final Array<Validator> validators = new Array();

    public interface Setter {
        boolean isGlobal(BaseShader baseShader, int i);

        void set(BaseShader baseShader, int i, Renderable renderable, Attributes attributes);
    }

    public static abstract class GlobalSetter implements Setter {
        public boolean isGlobal(BaseShader shader, int inputID) {
            return true;
        }
    }

    public static abstract class LocalSetter implements Setter {
        public boolean isGlobal(BaseShader shader, int inputID) {
            return false;
        }
    }

    public interface Validator {
        boolean validate(BaseShader baseShader, int i, Renderable renderable);
    }

    public static class Uniform implements Validator {
        public final String alias;
        public final long environmentMask;
        public final long materialMask;
        public final long overallMask;

        public Uniform(String alias, long materialMask, long environmentMask, long overallMask) {
            this.alias = alias;
            this.materialMask = materialMask;
            this.environmentMask = environmentMask;
            this.overallMask = overallMask;
        }

        public Uniform(String alias, long materialMask, long environmentMask) {
            this(alias, materialMask, environmentMask, 0);
        }

        public Uniform(String alias, long overallMask) {
            this(alias, 0, 0, overallMask);
        }

        public Uniform(String alias) {
            this(alias, 0, 0);
        }

        public boolean validate(BaseShader shader, int inputID, Renderable renderable) {
            long matFlags;
            long envFlags = 0;
            if (renderable == null || renderable.material == null) {
                matFlags = 0;
            } else {
                matFlags = renderable.material.getMask();
            }
            if (!(renderable == null || renderable.environment == null)) {
                envFlags = renderable.environment.getMask();
            }
            return (this.materialMask & matFlags) == this.materialMask && (this.environmentMask & envFlags) == this.environmentMask && ((matFlags | envFlags) & this.overallMask) == this.overallMask;
        }
    }

    public int register(String alias, Validator validator, Setter setter) {
        if (this.locations != null) {
            throw new GdxRuntimeException("Cannot register an uniform after initialization");
        }
        int existing = getUniformID(alias);
        if (existing >= 0) {
            this.validators.set(existing, validator);
            this.setters.set(existing, setter);
            return existing;
        }
        this.uniforms.add(alias);
        this.validators.add(validator);
        this.setters.add(setter);
        return this.uniforms.size - 1;
    }

    public int register(String alias, Validator validator) {
        return register(alias, validator, null);
    }

    public int register(String alias, Setter setter) {
        return register(alias, null, setter);
    }

    public int register(String alias) {
        return register(alias, null, null);
    }

    public int register(Uniform uniform, Setter setter) {
        return register(uniform.alias, uniform, setter);
    }

    public int register(Uniform uniform) {
        return register(uniform, null);
    }

    public int getUniformID(String alias) {
        int n = this.uniforms.size;
        for (int i = 0; i < n; i++) {
            if (((String) this.uniforms.get(i)).equals(alias)) {
                return i;
            }
        }
        return -1;
    }

    public String getUniformAlias(int id) {
        return (String) this.uniforms.get(id);
    }

    public void init(ShaderProgram program, Renderable renderable) {
        if (this.locations != null) {
            throw new GdxRuntimeException("Already initialized");
        } else if (program.isCompiled()) {
            this.program = program;
            int n = this.uniforms.size;
            this.locations = new int[n];
            int i = 0;
            while (i < n) {
                String input = (String) this.uniforms.get(i);
                Validator validator = (Validator) this.validators.get(i);
                Setter setter = (Setter) this.setters.get(i);
                if (validator == null || validator.validate(this, i, renderable)) {
                    this.locations[i] = program.fetchUniformLocation(input, false);
                    if (this.locations[i] >= 0 && setter != null) {
                        if (setter.isGlobal(this, i)) {
                            this.globalUniforms.add(i);
                        } else {
                            this.localUniforms.add(i);
                        }
                    }
                } else {
                    this.locations[i] = -1;
                }
                if (this.locations[i] < 0) {
                    this.validators.set(i, null);
                    this.setters.set(i, null);
                }
                i++;
            }
            if (renderable != null) {
                VertexAttributes attrs = renderable.mesh.getVertexAttributes();
                int c = attrs.size();
                for (i = 0; i < c; i++) {
                    VertexAttribute attr = attrs.get(i);
                    int location = program.getAttributeLocation(attr.alias);
                    if (location >= 0) {
                        this.attributes.put(attr.getKey(), location);
                    }
                }
            }
        } else {
            throw new GdxRuntimeException(program.getLog());
        }
    }

    public void begin(Camera camera, RenderContext context) {
        this.camera = camera;
        this.context = context;
        this.program.begin();
        this.currentMesh = null;
        for (int i = 0; i < this.globalUniforms.size; i++) {
            Array array = this.setters;
            int u = this.globalUniforms.get(i);
            if (array.get(u) != null) {
                ((Setter) this.setters.get(u)).set(this, u, null, null);
            }
        }
    }

    private final int[] getAttributeLocations(VertexAttributes attrs) {
        this.tempArray.clear();
        int n = attrs.size();
        for (int i = 0; i < n; i++) {
            this.tempArray.add(this.attributes.get(attrs.get(i).getKey(), -1));
        }
        return this.tempArray.items;
    }

    public void render(Renderable renderable) {
        if (renderable.worldTransform.det3x3() != 0.0f) {
            this.combinedAttributes.clear();
            if (renderable.environment != null) {
                this.combinedAttributes.set(renderable.environment);
            }
            if (renderable.material != null) {
                this.combinedAttributes.set(renderable.material);
            }
            render(renderable, this.combinedAttributes);
        }
    }

    public void render(Renderable renderable, Attributes combinedAttributes) {
        for (int i = 0; i < this.localUniforms.size; i++) {
            Array array = this.setters;
            int u = this.localUniforms.get(i);
            if (array.get(u) != null) {
                ((Setter) this.setters.get(u)).set(this, u, renderable, combinedAttributes);
            }
        }
        if (this.currentMesh != renderable.mesh) {
            if (this.currentMesh != null) {
                this.currentMesh.unbind(this.program, this.tempArray.items);
            }
            this.currentMesh = renderable.mesh;
            this.currentMesh.bind(this.program, getAttributeLocations(renderable.mesh.getVertexAttributes()));
        }
        renderable.mesh.render(this.program, renderable.primitiveType, renderable.meshPartOffset, renderable.meshPartSize, false);
    }

    public void end() {
        if (this.currentMesh != null) {
            this.currentMesh.unbind(this.program, this.tempArray.items);
            this.currentMesh = null;
        }
        this.program.end();
    }

    public void dispose() {
        this.program = null;
        this.uniforms.clear();
        this.validators.clear();
        this.setters.clear();
        this.localUniforms.clear();
        this.globalUniforms.clear();
        this.locations = null;
    }

    public final boolean has(int inputID) {
        return inputID >= 0 && inputID < this.locations.length && this.locations[inputID] >= 0;
    }

    public final int loc(int inputID) {
        return (inputID < 0 || inputID >= this.locations.length) ? -1 : this.locations[inputID];
    }

    public final boolean set(int uniform, Matrix4 value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformMatrix(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, Matrix3 value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformMatrix(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, Vector3 value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, Vector2 value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, Color value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, float value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, float v1, float v2) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], v1, v2);
        return true;
    }

    public final boolean set(int uniform, float v1, float v2, float v3) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], v1, v2, v3);
        return true;
    }

    public final boolean set(int uniform, float v1, float v2, float v3, float v4) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformf(this.locations[uniform], v1, v2, v3, v4);
        return true;
    }

    public final boolean set(int uniform, int value) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], value);
        return true;
    }

    public final boolean set(int uniform, int v1, int v2) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], v1, v2);
        return true;
    }

    public final boolean set(int uniform, int v1, int v2, int v3) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], v1, v2, v3);
        return true;
    }

    public final boolean set(int uniform, int v1, int v2, int v3, int v4) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], v1, v2, v3, v4);
        return true;
    }

    public final boolean set(int uniform, TextureDescriptor textureDesc) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], this.context.textureBinder.bind(textureDesc));
        return true;
    }

    public final boolean set(int uniform, GLTexture texture) {
        if (this.locations[uniform] < 0) {
            return false;
        }
        this.program.setUniformi(this.locations[uniform], this.context.textureBinder.bind(texture));
        return true;
    }
}
