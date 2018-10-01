package com.badlogic.gdx.graphics.g3d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.utils.DefaultRenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Pool;

public class ModelBatch implements Disposable {
    protected Camera camera;
    protected final RenderContext context;
    private final boolean ownContext;
    protected final Array<Renderable> renderables;
    protected final RenderablePool renderablesPool;
    protected final ShaderProvider shaderProvider;
    protected final RenderableSorter sorter;

    protected static class RenderablePool extends Pool<Renderable> {
        protected Array<Renderable> obtained = new Array();

        protected RenderablePool() {
        }

        protected Renderable newObject() {
            return new Renderable();
        }

        public Renderable obtain() {
            Renderable renderable = (Renderable) super.obtain();
            renderable.environment = null;
            renderable.material = null;
            renderable.mesh = null;
            renderable.shader = null;
            this.obtained.add(renderable);
            return renderable;
        }

        public void flush() {
            super.freeAll(this.obtained);
            this.obtained.clear();
        }
    }

    public ModelBatch(RenderContext context, ShaderProvider shaderProvider, RenderableSorter sorter) {
        this.renderablesPool = new RenderablePool();
        this.renderables = new Array();
        if (sorter == null) {
            sorter = new DefaultRenderableSorter();
        }
        this.sorter = sorter;
        this.ownContext = context == null;
        if (context == null) {
            context = new RenderContext(new DefaultTextureBinder(1, 1));
        }
        this.context = context;
        if (shaderProvider == null) {
            shaderProvider = new DefaultShaderProvider();
        }
        this.shaderProvider = shaderProvider;
    }

    public ModelBatch(RenderContext context, ShaderProvider shaderProvider) {
        this(context, shaderProvider, null);
    }

    public ModelBatch(RenderContext context, RenderableSorter sorter) {
        this(context, null, sorter);
    }

    public ModelBatch(RenderContext context) {
        this(context, null, null);
    }

    public ModelBatch(ShaderProvider shaderProvider, RenderableSorter sorter) {
        this(null, shaderProvider, sorter);
    }

    public ModelBatch(RenderableSorter sorter) {
        this(null, null, sorter);
    }

    public ModelBatch(ShaderProvider shaderProvider) {
        this(null, shaderProvider, null);
    }

    public ModelBatch(FileHandle vertexShader, FileHandle fragmentShader) {
        this(null, new DefaultShaderProvider(vertexShader, fragmentShader), null);
    }

    public ModelBatch(String vertexShader, String fragmentShader) {
        this(null, new DefaultShaderProvider(vertexShader, fragmentShader), null);
    }

    public ModelBatch() {
        this(null, null, null);
    }

    public void begin(Camera cam) {
        if (this.camera != null) {
            throw new GdxRuntimeException("Call end() first.");
        }
        this.camera = cam;
        if (this.ownContext) {
            this.context.begin();
        }
    }

    public void setCamera(Camera cam) {
        if (this.camera == null) {
            throw new GdxRuntimeException("Call begin() first.");
        }
        if (this.renderables.size > 0) {
            flush();
        }
        this.camera = cam;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public boolean ownsRenderContext() {
        return this.ownContext;
    }

    public RenderContext getRenderContext() {
        return this.context;
    }

    public ShaderProvider getShaderProvider() {
        return this.shaderProvider;
    }

    public RenderableSorter getRenderableSorter() {
        return this.sorter;
    }

    public void flush() {
        this.sorter.sort(this.camera, this.renderables);
        Shader currentShader = null;
        for (int i = 0; i < this.renderables.size; i++) {
            Renderable renderable = (Renderable) this.renderables.get(i);
            if (currentShader != renderable.shader) {
                if (currentShader != null) {
                    currentShader.end();
                }
                currentShader = renderable.shader;
                currentShader.begin(this.camera, this.context);
            }
            currentShader.render(renderable);
        }
        if (currentShader != null) {
            currentShader.end();
        }
        this.renderablesPool.flush();
        this.renderables.clear();
    }

    public void end() {
        flush();
        if (this.ownContext) {
            this.context.end();
        }
        this.camera = null;
    }

    public void render(Renderable renderable) {
        renderable.shader = this.shaderProvider.getShader(renderable);
        renderable.mesh.setAutoBind(false);
        this.renderables.add(renderable);
    }

    public void render(RenderableProvider renderableProvider) {
        int offset = this.renderables.size;
        renderableProvider.getRenderables(this.renderables, this.renderablesPool);
        for (int i = offset; i < this.renderables.size; i++) {
            Renderable renderable = (Renderable) this.renderables.get(i);
            renderable.shader = this.shaderProvider.getShader(renderable);
        }
    }

    public <T extends RenderableProvider> void render(Iterable<T> renderableProviders) {
        for (T renderableProvider : renderableProviders) {
            render((RenderableProvider) renderableProvider);
        }
    }

    public void render(RenderableProvider renderableProvider, Environment environment) {
        int offset = this.renderables.size;
        renderableProvider.getRenderables(this.renderables, this.renderablesPool);
        for (int i = offset; i < this.renderables.size; i++) {
            Renderable renderable = (Renderable) this.renderables.get(i);
            renderable.environment = environment;
            renderable.shader = this.shaderProvider.getShader(renderable);
        }
    }

    public <T extends RenderableProvider> void render(Iterable<T> renderableProviders, Environment environment) {
        for (T renderableProvider : renderableProviders) {
            render((RenderableProvider) renderableProvider, environment);
        }
    }

    public void render(RenderableProvider renderableProvider, Shader shader) {
        int offset = this.renderables.size;
        renderableProvider.getRenderables(this.renderables, this.renderablesPool);
        for (int i = offset; i < this.renderables.size; i++) {
            Renderable renderable = (Renderable) this.renderables.get(i);
            renderable.shader = shader;
            renderable.shader = this.shaderProvider.getShader(renderable);
        }
    }

    public <T extends RenderableProvider> void render(Iterable<T> renderableProviders, Shader shader) {
        for (T renderableProvider : renderableProviders) {
            render((RenderableProvider) renderableProvider, shader);
        }
    }

    public void render(RenderableProvider renderableProvider, Environment environment, Shader shader) {
        int offset = this.renderables.size;
        renderableProvider.getRenderables(this.renderables, this.renderablesPool);
        for (int i = offset; i < this.renderables.size; i++) {
            Renderable renderable = (Renderable) this.renderables.get(i);
            renderable.environment = environment;
            renderable.shader = shader;
            renderable.shader = this.shaderProvider.getShader(renderable);
        }
    }

    public <T extends RenderableProvider> void render(Iterable<T> renderableProviders, Environment environment, Shader shader) {
        for (T renderableProvider : renderableProviders) {
            render((RenderableProvider) renderableProvider, environment, shader);
        }
    }

    public void dispose() {
        this.shaderProvider.dispose();
    }
}
