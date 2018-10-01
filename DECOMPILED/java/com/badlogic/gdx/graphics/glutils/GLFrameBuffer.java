package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.GLTexture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public abstract class GLFrameBuffer<T extends GLTexture> implements Disposable {
    private static final Map<Application, Array<GLFrameBuffer>> buffers = new HashMap();
    private static int defaultFramebufferHandle;
    private static boolean defaultFramebufferHandleInitialized = false;
    protected T colorTexture;
    private int depthbufferHandle;
    protected final Format format;
    private int framebufferHandle;
    protected final boolean hasDepth;
    protected final boolean hasStencil;
    protected final int height;
    private int stencilbufferHandle;
    protected final int width;

    protected abstract T createColorTexture();

    protected abstract void disposeColorTexture(T t);

    public GLFrameBuffer(Format format, int width, int height, boolean hasDepth) {
        this(format, width, height, hasDepth, false);
    }

    public GLFrameBuffer(Format format, int width, int height, boolean hasDepth, boolean hasStencil) {
        this.width = width;
        this.height = height;
        this.format = format;
        this.hasDepth = hasDepth;
        this.hasStencil = hasStencil;
        build();
        addManagedFrameBuffer(Gdx.app, this);
    }

    private void build() {
        GL20 gl = Gdx.gl20;
        if (!defaultFramebufferHandleInitialized) {
            defaultFramebufferHandleInitialized = true;
            if (Gdx.app.getType() == ApplicationType.iOS) {
                IntBuffer intbuf = ByteBuffer.allocateDirect(64).order(ByteOrder.nativeOrder()).asIntBuffer();
                gl.glGetIntegerv(GL30.GL_DRAW_FRAMEBUFFER_BINDING, intbuf);
                defaultFramebufferHandle = intbuf.get(0);
            } else {
                defaultFramebufferHandle = 0;
            }
        }
        this.colorTexture = createColorTexture();
        this.framebufferHandle = gl.glGenFramebuffer();
        if (this.hasDepth) {
            this.depthbufferHandle = gl.glGenRenderbuffer();
        }
        if (this.hasStencil) {
            this.stencilbufferHandle = gl.glGenRenderbuffer();
        }
        gl.glBindTexture(GL20.GL_TEXTURE_2D, this.colorTexture.getTextureObjectHandle());
        if (this.hasDepth) {
            gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, this.depthbufferHandle);
            gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_DEPTH_COMPONENT16, this.colorTexture.getWidth(), this.colorTexture.getHeight());
        }
        if (this.hasStencil) {
            gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, this.stencilbufferHandle);
            gl.glRenderbufferStorage(GL20.GL_RENDERBUFFER, GL20.GL_STENCIL_INDEX8, this.colorTexture.getWidth(), this.colorTexture.getHeight());
        }
        gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, this.framebufferHandle);
        gl.glFramebufferTexture2D(GL20.GL_FRAMEBUFFER, GL20.GL_COLOR_ATTACHMENT0, GL20.GL_TEXTURE_2D, this.colorTexture.getTextureObjectHandle(), 0);
        if (this.hasDepth) {
            gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_DEPTH_ATTACHMENT, GL20.GL_RENDERBUFFER, this.depthbufferHandle);
        }
        if (this.hasStencil) {
            gl.glFramebufferRenderbuffer(GL20.GL_FRAMEBUFFER, GL20.GL_STENCIL_ATTACHMENT, GL20.GL_RENDERBUFFER, this.stencilbufferHandle);
        }
        gl.glBindRenderbuffer(GL20.GL_RENDERBUFFER, 0);
        gl.glBindTexture(GL20.GL_TEXTURE_2D, 0);
        int result = gl.glCheckFramebufferStatus(GL20.GL_FRAMEBUFFER);
        gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);
        if (result != GL20.GL_FRAMEBUFFER_COMPLETE) {
            disposeColorTexture(this.colorTexture);
            if (this.hasDepth) {
                gl.glDeleteRenderbuffer(this.depthbufferHandle);
            }
            if (this.hasStencil) {
                gl.glDeleteRenderbuffer(this.stencilbufferHandle);
            }
            gl.glDeleteFramebuffer(this.framebufferHandle);
            if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
                throw new IllegalStateException("frame buffer couldn't be constructed: incomplete attachment");
            } else if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS) {
                throw new IllegalStateException("frame buffer couldn't be constructed: incomplete dimensions");
            } else if (result == GL20.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
                throw new IllegalStateException("frame buffer couldn't be constructed: missing attachment");
            } else if (result == GL20.GL_FRAMEBUFFER_UNSUPPORTED) {
                throw new IllegalStateException("frame buffer couldn't be constructed: unsupported combination of formats");
            } else {
                throw new IllegalStateException("frame buffer couldn't be constructed: unknown error " + result);
            }
        }
    }

    public void dispose() {
        GL20 gl = Gdx.gl20;
        disposeColorTexture(this.colorTexture);
        if (this.hasDepth) {
            gl.glDeleteRenderbuffer(this.depthbufferHandle);
        }
        if (this.hasStencil) {
            gl.glDeleteRenderbuffer(this.stencilbufferHandle);
        }
        gl.glDeleteFramebuffer(this.framebufferHandle);
        if (buffers.get(Gdx.app) != null) {
            ((Array) buffers.get(Gdx.app)).removeValue(this, true);
        }
    }

    public void bind() {
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, this.framebufferHandle);
    }

    public static void unbind() {
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, defaultFramebufferHandle);
    }

    public void begin() {
        bind();
        setFrameBufferViewport();
    }

    protected void setFrameBufferViewport() {
        Gdx.gl20.glViewport(0, 0, this.colorTexture.getWidth(), this.colorTexture.getHeight());
    }

    public void end() {
        end(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    public void end(int x, int y, int width, int height) {
        unbind();
        Gdx.gl20.glViewport(x, y, width, height);
    }

    public T getColorBufferTexture() {
        return this.colorTexture;
    }

    public int getFramebufferHandle() {
        return this.framebufferHandle;
    }

    public int getDepthBufferHandle() {
        return this.depthbufferHandle;
    }

    public int getStencilBufferHandle() {
        return this.stencilbufferHandle;
    }

    public int getHeight() {
        return this.colorTexture.getHeight();
    }

    public int getWidth() {
        return this.colorTexture.getWidth();
    }

    public int getDepth() {
        return this.colorTexture.getDepth();
    }

    private static void addManagedFrameBuffer(Application app, GLFrameBuffer frameBuffer) {
        Array<GLFrameBuffer> managedResources = (Array) buffers.get(app);
        if (managedResources == null) {
            managedResources = new Array();
        }
        managedResources.add(frameBuffer);
        buffers.put(app, managedResources);
    }

    public static void invalidateAllFrameBuffers(Application app) {
        if (Gdx.gl20 != null) {
            Array<GLFrameBuffer> bufferArray = (Array) buffers.get(app);
            if (bufferArray != null) {
                for (int i = 0; i < bufferArray.size; i++) {
                    ((GLFrameBuffer) bufferArray.get(i)).build();
                }
            }
        }
    }

    public static void clearAllFrameBuffers(Application app) {
        buffers.remove(app);
    }

    public static StringBuilder getManagedStatus(StringBuilder builder) {
        builder.append("Managed buffers/app: { ");
        for (Application app : buffers.keySet()) {
            builder.append(((Array) buffers.get(app)).size);
            builder.append(" ");
        }
        builder.append("}");
        return builder;
    }

    public static String getManagedStatus() {
        return getManagedStatus(new StringBuilder()).toString();
    }
}
