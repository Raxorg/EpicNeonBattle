package com.badlogic.gdx.graphics.g3d.particles;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Attributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.DepthTestAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.IntAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Setter;
import com.badlogic.gdx.graphics.g3d.shaders.BaseShader.Uniform;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;

public class ParticleShader extends BaseShader {
    static final Vector3 TMP_VECTOR3 = new Vector3();
    private static String defaultFragmentShader = null;
    private static String defaultVertexShader = null;
    protected static long implementedFlags = (BlendingAttribute.Type | TextureAttribute.Diffuse);
    private static final long optionalAttributes = (IntAttribute.CullFace | DepthTestAttribute.Type);
    protected final Config config;
    Material currentMaterial;
    private long materialMask;
    private Renderable renderable;
    private long vertexMask;

    public enum AlignMode {
        Screen,
        ViewPoint
    }

    public static class Config {
        public AlignMode align = AlignMode.Screen;
        public int defaultCullFace = -1;
        public int defaultDepthFunc = -1;
        public String fragmentShader = null;
        public boolean ignoreUnimplemented = true;
        public ParticleType type = ParticleType.Billboard;
        public String vertexShader = null;

        public Config(AlignMode align, ParticleType type) {
            this.align = align;
            this.type = type;
        }

        public Config(AlignMode align) {
            this.align = align;
        }

        public Config(ParticleType type) {
            this.type = type;
        }

        public Config(String vertexShader, String fragmentShader) {
            this.vertexShader = vertexShader;
            this.fragmentShader = fragmentShader;
        }
    }

    public static class Inputs {
        public static final Uniform cameraInvDirection = new Uniform("u_cameraInvDirection");
        public static final Uniform cameraRight = new Uniform("u_cameraRight");
        public static final Uniform regionSize = new Uniform("u_regionSize");
        public static final Uniform screenWidth = new Uniform("u_screenWidth");
    }

    public enum ParticleType {
        Billboard,
        Point
    }

    public static class Setters {
        public static final Setter cameraInvDirection = new Setter() {
            public boolean isGlobal(BaseShader shader, int inputID) {
                return true;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ParticleShader.TMP_VECTOR3.set(-shader.camera.direction.x, -shader.camera.direction.y, -shader.camera.direction.z).nor());
            }
        };
        public static final Setter cameraPosition = new Setter() {
            public boolean isGlobal(BaseShader shader, int inputID) {
                return true;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, shader.camera.position);
            }
        };
        public static final Setter cameraRight = new Setter() {
            public boolean isGlobal(BaseShader shader, int inputID) {
                return true;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ParticleShader.TMP_VECTOR3.set(shader.camera.direction).crs(shader.camera.up).nor());
            }
        };
        public static final Setter cameraUp = new Setter() {
            public boolean isGlobal(BaseShader shader, int inputID) {
                return true;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, ParticleShader.TMP_VECTOR3.set(shader.camera.up).nor());
            }
        };
        public static final Setter screenWidth = new Setter() {
            public boolean isGlobal(BaseShader shader, int inputID) {
                return true;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, (float) Gdx.graphics.getWidth());
            }
        };
        public static final Setter worldViewTrans = new Setter() {
            final Matrix4 temp = new Matrix4();

            public boolean isGlobal(BaseShader shader, int inputID) {
                return false;
            }

            public void set(BaseShader shader, int inputID, Renderable renderable, Attributes combinedAttributes) {
                shader.set(inputID, this.temp.set(shader.camera.view).mul(renderable.worldTransform));
            }
        };
    }

    public static String getDefaultVertexShader() {
        if (defaultVertexShader == null) {
            defaultVertexShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/particles/particles.vertex.glsl").readString();
        }
        return defaultVertexShader;
    }

    public static String getDefaultFragmentShader() {
        if (defaultFragmentShader == null) {
            defaultFragmentShader = Gdx.files.classpath("com/badlogic/gdx/graphics/g3d/particles/particles.fragment.glsl").readString();
        }
        return defaultFragmentShader;
    }

    public ParticleShader(Renderable renderable) {
        this(renderable, new Config());
    }

    public ParticleShader(Renderable renderable, Config config) {
        this(renderable, config, createPrefix(renderable, config));
    }

    public ParticleShader(Renderable renderable, Config config, String prefix) {
        String str;
        String str2;
        if (config.vertexShader != null) {
            str = config.vertexShader;
        } else {
            str = getDefaultVertexShader();
        }
        if (config.fragmentShader != null) {
            str2 = config.fragmentShader;
        } else {
            str2 = getDefaultFragmentShader();
        }
        this(renderable, config, prefix, str, str2);
    }

    public ParticleShader(Renderable renderable, Config config, String prefix, String vertexShader, String fragmentShader) {
        this(renderable, config, new ShaderProgram(prefix + vertexShader, prefix + fragmentShader));
    }

    public ParticleShader(Renderable renderable, Config config, ShaderProgram shaderProgram) {
        this.config = config;
        this.program = shaderProgram;
        this.renderable = renderable;
        this.materialMask = renderable.material.getMask() | optionalAttributes;
        this.vertexMask = renderable.mesh.getVertexAttributes().getMask();
        if (config.ignoreUnimplemented || (implementedFlags & this.materialMask) == this.materialMask) {
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.viewTrans, com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters.viewTrans);
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.projViewTrans, com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters.projViewTrans);
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.projTrans, com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters.projTrans);
            register(Inputs.screenWidth, Setters.screenWidth);
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.cameraUp, Setters.cameraUp);
            register(Inputs.cameraRight, Setters.cameraRight);
            register(Inputs.cameraInvDirection, Setters.cameraInvDirection);
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.cameraPosition, Setters.cameraPosition);
            register(com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Inputs.diffuseTexture, com.badlogic.gdx.graphics.g3d.shaders.DefaultShader.Setters.diffuseTexture);
            return;
        }
        throw new GdxRuntimeException("Some attributes not implemented yet (" + this.materialMask + ")");
    }

    public void init() {
        ShaderProgram program = this.program;
        this.program = null;
        init(program, this.renderable);
        this.renderable = null;
    }

    public static String createPrefix(Renderable renderable, Config config) {
        String prefix = BuildConfig.VERSION_NAME;
        if (Gdx.app.getType() == ApplicationType.Desktop) {
            prefix = prefix + "#version 120\n";
        } else {
            prefix = prefix + "#version 100\n";
        }
        if (config.type != ParticleType.Billboard) {
            return prefix;
        }
        prefix = prefix + "#define billboard\n";
        if (config.align == AlignMode.Screen) {
            return prefix + "#define screenFacing\n";
        }
        if (config.align == AlignMode.ViewPoint) {
            return prefix + "#define viewPointFacing\n";
        }
        return prefix;
    }

    public boolean canRender(Renderable renderable) {
        return this.materialMask == (renderable.material.getMask() | optionalAttributes) && this.vertexMask == renderable.mesh.getVertexAttributes().getMask();
    }

    public int compareTo(Shader other) {
        if (other == null) {
            return -1;
        }
        return other == this ? 0 : 0;
    }

    public boolean equals(Object obj) {
        return obj instanceof ParticleShader ? equals((ParticleShader) obj) : false;
    }

    public boolean equals(ParticleShader obj) {
        return obj == this;
    }

    public void begin(Camera camera, RenderContext context) {
        super.begin(camera, context);
    }

    public void render(Renderable renderable) {
        if (!renderable.material.has(BlendingAttribute.Type)) {
            this.context.setBlending(false, GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
        bindMaterial(renderable);
        super.render(renderable);
    }

    public void end() {
        this.currentMaterial = null;
        super.end();
    }

    protected void bindMaterial(Renderable renderable) {
        if (this.currentMaterial != renderable.material) {
            int depthFunc;
            int cullFace = this.config.defaultCullFace == -1 ? GL20.GL_BACK : this.config.defaultCullFace;
            if (this.config.defaultDepthFunc == -1) {
                depthFunc = GL20.GL_LEQUAL;
            } else {
                depthFunc = this.config.defaultDepthFunc;
            }
            float depthRangeNear = 0.0f;
            float depthRangeFar = 1.0f;
            boolean depthMask = true;
            this.currentMaterial = renderable.material;
            Iterator it = this.currentMaterial.iterator();
            while (it.hasNext()) {
                Attribute attr = (Attribute) it.next();
                long t = attr.type;
                if (BlendingAttribute.is(t)) {
                    this.context.setBlending(true, ((BlendingAttribute) attr).sourceFunction, ((BlendingAttribute) attr).destFunction);
                } else if ((DepthTestAttribute.Type & t) == DepthTestAttribute.Type) {
                    DepthTestAttribute dta = (DepthTestAttribute) attr;
                    depthFunc = dta.depthFunc;
                    depthRangeNear = dta.depthRangeNear;
                    depthRangeFar = dta.depthRangeFar;
                    depthMask = dta.depthMask;
                } else if (!this.config.ignoreUnimplemented) {
                    throw new GdxRuntimeException("Unknown material attribute: " + attr.toString());
                }
            }
            this.context.setCullFace(cullFace);
            this.context.setDepthTest(depthFunc, depthRangeNear, depthRangeFar);
            this.context.setDepthMask(depthMask);
        }
    }

    public void dispose() {
        this.program.dispose();
        super.dispose();
    }

    public int getDefaultCullFace() {
        return this.config.defaultCullFace == -1 ? GL20.GL_BACK : this.config.defaultCullFace;
    }

    public void setDefaultCullFace(int cullFace) {
        this.config.defaultCullFace = cullFace;
    }

    public int getDefaultDepthFunc() {
        return this.config.defaultDepthFunc == -1 ? GL20.GL_LEQUAL : this.config.defaultDepthFunc;
    }

    public void setDefaultDepthFunc(int depthFunc) {
        this.config.defaultDepthFunc = depthFunc;
    }
}
