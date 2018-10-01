package com.badlogic.gdx.backends.android.surfaceview;

import android.content.Context;
import android.opengl.GLDebugHelper;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.opengl.GLSurfaceView.Renderer;
import android.os.Build.VERSION;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import com.badlogic.gdx.graphics.GL20;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceViewAPI18 extends SurfaceView implements Callback {
    public static final int DEBUG_CHECK_GL_ERROR = 1;
    public static final int DEBUG_LOG_GL_CALLS = 2;
    private static final boolean LOG_ATTACH_DETACH = false;
    private static final boolean LOG_EGL = false;
    private static final boolean LOG_PAUSE_RESUME = false;
    private static final boolean LOG_RENDERER = false;
    private static final boolean LOG_RENDERER_DRAW_FRAME = false;
    private static final boolean LOG_SURFACE = false;
    private static final boolean LOG_THREADS = false;
    public static final int RENDERMODE_CONTINUOUSLY = 1;
    public static final int RENDERMODE_WHEN_DIRTY = 0;
    private static final String TAG = "GLSurfaceViewAPI18";
    private static final GLThreadManager sGLThreadManager = new GLThreadManager();
    private int mDebugFlags;
    private boolean mDetached;
    private EGLConfigChooser mEGLConfigChooser;
    private int mEGLContextClientVersion;
    private EGLContextFactory mEGLContextFactory;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private GLThread mGLThread;
    private GLWrapper mGLWrapper;
    private boolean mPreserveEGLContextOnPause;
    private Renderer mRenderer;
    private final WeakReference<GLSurfaceViewAPI18> mThisWeakRef = new WeakReference(this);

    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig);

        void destroyContext(EGL10 egl10, EGLDisplay eGLDisplay, EGLContext eGLContext);
    }

    private abstract class BaseConfigChooser implements EGLConfigChooser {
        protected int[] mConfigSpec;

        abstract EGLConfig chooseConfig(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig[] eGLConfigArr);

        public BaseConfigChooser(int[] configSpec) {
            this.mConfigSpec = filterConfigSpec(configSpec);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY];
            if (egl.eglChooseConfig(display, this.mConfigSpec, null, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, num_config)) {
                int numConfigs = num_config[GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY];
                if (numConfigs <= 0) {
                    throw new IllegalArgumentException("No configs match configSpec");
                }
                EGLConfig[] configs = new EGLConfig[numConfigs];
                if (egl.eglChooseConfig(display, this.mConfigSpec, configs, numConfigs, num_config)) {
                    EGLConfig config = chooseConfig(egl, display, configs);
                    if (config != null) {
                        return config;
                    }
                    throw new IllegalArgumentException("No config chosen");
                }
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            throw new IllegalArgumentException("eglChooseConfig failed");
        }

        private int[] filterConfigSpec(int[] configSpec) {
            if (GLSurfaceViewAPI18.this.mEGLContextClientVersion != GLSurfaceViewAPI18.DEBUG_LOG_GL_CALLS) {
                return configSpec;
            }
            int len = configSpec.length;
            int[] newConfigSpec = new int[(len + GLSurfaceViewAPI18.DEBUG_LOG_GL_CALLS)];
            System.arraycopy(configSpec, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, newConfigSpec, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, len - 1);
            newConfigSpec[len - 1] = 12352;
            newConfigSpec[len] = 4;
            newConfigSpec[len + GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY] = 12344;
            return newConfigSpec;
        }
    }

    private class ComponentSizeChooser extends BaseConfigChooser {
        protected int mAlphaSize;
        protected int mBlueSize;
        protected int mDepthSize;
        protected int mGreenSize;
        protected int mRedSize;
        protected int mStencilSize;
        private int[] mValue = new int[GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY];

        public ComponentSizeChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
            super(new int[]{12324, redSize, 12323, greenSize, 12322, blueSize, 12321, alphaSize, 12325, depthSize, 12326, stencilSize, 12344});
            this.mRedSize = redSize;
            this.mGreenSize = greenSize;
            this.mBlueSize = blueSize;
            this.mAlphaSize = alphaSize;
            this.mDepthSize = depthSize;
            this.mStencilSize = stencilSize;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display, EGLConfig[] configs) {
            int length = configs.length;
            for (int i = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY; i < length; i += GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY) {
                EGLConfig config = configs[i];
                int d = findConfigAttrib(egl, display, config, 12325, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                int s = findConfigAttrib(egl, display, config, 12326, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                if (d >= this.mDepthSize && s >= this.mStencilSize) {
                    int r = findConfigAttrib(egl, display, config, 12324, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                    int g = findConfigAttrib(egl, display, config, 12323, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                    int b = findConfigAttrib(egl, display, config, 12322, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                    int a = findConfigAttrib(egl, display, config, 12321, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
                    if (r == this.mRedSize && g == this.mGreenSize && b == this.mBlueSize && a == this.mAlphaSize) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
            if (egl.eglGetConfigAttrib(display, config, attribute, this.mValue)) {
                return this.mValue[GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY];
            }
            return defaultValue;
        }
    }

    private class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION;

        private DefaultContextFactory() {
            this.EGL_CONTEXT_CLIENT_VERSION = 12440;
        }

        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config) {
            int[] attrib_list = new int[]{this.EGL_CONTEXT_CLIENT_VERSION, GLSurfaceViewAPI18.this.mEGLContextClientVersion, 12344};
            EGLContext eGLContext = EGL10.EGL_NO_CONTEXT;
            if (GLSurfaceViewAPI18.this.mEGLContextClientVersion == 0) {
                attrib_list = null;
            }
            return egl.eglCreateContext(display, config, eGLContext, attrib_list);
        }

        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                EglHelper.throwEglException("eglDestroyContex", egl.eglGetError());
            }
        }
    }

    public interface EGLWindowSurfaceFactory {
        EGLSurface createWindowSurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLConfig eGLConfig, Object obj);

        void destroySurface(EGL10 egl10, EGLDisplay eGLDisplay, EGLSurface eGLSurface);
    }

    private static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {
        private DefaultWindowSurfaceFactory() {
        }

        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, null);
            } catch (IllegalArgumentException e) {
                Log.e(GLSurfaceViewAPI18.TAG, "eglCreateWindowSurface", e);
            }
            return result;
        }

        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    private static class EglHelper {
        EGL10 mEgl;
        EGLConfig mEglConfig;
        EGLContext mEglContext;
        EGLDisplay mEglDisplay;
        EGLSurface mEglSurface;
        private WeakReference<GLSurfaceViewAPI18> mGLSurfaceViewWeakRef;

        public EglHelper(WeakReference<GLSurfaceViewAPI18> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        public void start() {
            this.mEgl = (EGL10) EGLContext.getEGL();
            this.mEglDisplay = this.mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
            if (this.mEglDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new RuntimeException("eglGetDisplay failed");
            }
            if (this.mEgl.eglInitialize(this.mEglDisplay, new int[GLSurfaceViewAPI18.DEBUG_LOG_GL_CALLS])) {
                GLSurfaceViewAPI18 view = (GLSurfaceViewAPI18) this.mGLSurfaceViewWeakRef.get();
                if (view == null) {
                    this.mEglConfig = null;
                    this.mEglContext = null;
                } else {
                    this.mEglConfig = view.mEGLConfigChooser.chooseConfig(this.mEgl, this.mEglDisplay);
                    this.mEglContext = view.mEGLContextFactory.createContext(this.mEgl, this.mEglDisplay, this.mEglConfig);
                }
                if (this.mEglContext == null || this.mEglContext == EGL10.EGL_NO_CONTEXT) {
                    this.mEglContext = null;
                    throwEglException("createContext");
                }
                this.mEglSurface = null;
                return;
            }
            throw new RuntimeException("eglInitialize failed");
        }

        public boolean createSurface() {
            if (this.mEgl == null) {
                throw new RuntimeException("egl not initialized");
            } else if (this.mEglDisplay == null) {
                throw new RuntimeException("eglDisplay not initialized");
            } else if (this.mEglConfig == null) {
                throw new RuntimeException("mEglConfig not initialized");
            } else {
                destroySurfaceImp();
                GLSurfaceViewAPI18 view = (GLSurfaceViewAPI18) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    this.mEglSurface = view.mEGLWindowSurfaceFactory.createWindowSurface(this.mEgl, this.mEglDisplay, this.mEglConfig, view.getHolder());
                } else {
                    this.mEglSurface = null;
                }
                if (this.mEglSurface == null || this.mEglSurface == EGL10.EGL_NO_SURFACE) {
                    if (this.mEgl.eglGetError() != 12299) {
                        return GLSurfaceViewAPI18.LOG_THREADS;
                    }
                    Log.e("EglHelper", "createWindowSurface returned EGL_BAD_NATIVE_WINDOW.");
                    return GLSurfaceViewAPI18.LOG_THREADS;
                } else if (this.mEgl.eglMakeCurrent(this.mEglDisplay, this.mEglSurface, this.mEglSurface, this.mEglContext)) {
                    return true;
                } else {
                    logEglErrorAsWarning("EGLHelper", "eglMakeCurrent", this.mEgl.eglGetError());
                    return GLSurfaceViewAPI18.LOG_THREADS;
                }
            }
        }

        GL createGL() {
            GL gl = this.mEglContext.getGL();
            GLSurfaceViewAPI18 view = (GLSurfaceViewAPI18) this.mGLSurfaceViewWeakRef.get();
            if (view == null) {
                return gl;
            }
            if (view.mGLWrapper != null) {
                gl = view.mGLWrapper.wrap(gl);
            }
            if ((view.mDebugFlags & 3) == 0) {
                return gl;
            }
            int configFlags = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY;
            Writer log = null;
            if ((view.mDebugFlags & GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY) != 0) {
                configFlags = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY | GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY;
            }
            if ((view.mDebugFlags & GLSurfaceViewAPI18.DEBUG_LOG_GL_CALLS) != 0) {
                log = new LogWriter();
            }
            return GLDebugHelper.wrap(gl, configFlags, log);
        }

        public int swap() {
            if (this.mEgl.eglSwapBuffers(this.mEglDisplay, this.mEglSurface)) {
                return 12288;
            }
            return this.mEgl.eglGetError();
        }

        public void destroySurface() {
            destroySurfaceImp();
        }

        private void destroySurfaceImp() {
            if (this.mEglSurface != null && this.mEglSurface != EGL10.EGL_NO_SURFACE) {
                this.mEgl.eglMakeCurrent(this.mEglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
                GLSurfaceViewAPI18 view = (GLSurfaceViewAPI18) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLWindowSurfaceFactory.destroySurface(this.mEgl, this.mEglDisplay, this.mEglSurface);
                }
                this.mEglSurface = null;
            }
        }

        public void finish() {
            if (this.mEglContext != null) {
                GLSurfaceViewAPI18 view = (GLSurfaceViewAPI18) this.mGLSurfaceViewWeakRef.get();
                if (view != null) {
                    view.mEGLContextFactory.destroyContext(this.mEgl, this.mEglDisplay, this.mEglContext);
                }
                this.mEglContext = null;
            }
            if (this.mEglDisplay != null) {
                this.mEgl.eglTerminate(this.mEglDisplay);
                this.mEglDisplay = null;
            }
        }

        private void throwEglException(String function) {
            throwEglException(function, this.mEgl.eglGetError());
        }

        public static void throwEglException(String function, int error) {
            throw new RuntimeException(formatEglError(function, error));
        }

        public static void logEglErrorAsWarning(String tag, String function, int error) {
            Log.w(tag, formatEglError(function, error));
        }

        private static String getErrorString(int error) {
            switch (error) {
                case 12288:
                    return "EGL_SUCCESS";
                case 12289:
                    return "EGL_NOT_INITIALIZED";
                case 12290:
                    return "EGL_BAD_ACCESS";
                case 12291:
                    return "EGL_BAD_ALLOC";
                case 12292:
                    return "EGL_BAD_ATTRIBUTE";
                case 12293:
                    return "EGL_BAD_CONFIG";
                case 12294:
                    return "EGL_BAD_CONTEXT";
                case 12295:
                    return "EGL_BAD_CURRENT_SURFACE";
                case 12296:
                    return "EGL_BAD_DISPLAY";
                case 12297:
                    return "EGL_BAD_MATCH";
                case 12298:
                    return "EGL_BAD_NATIVE_PIXMAP";
                case 12299:
                    return "EGL_BAD_NATIVE_WINDOW";
                case 12300:
                    return "EGL_BAD_PARAMETER";
                case 12301:
                    return "EGL_BAD_SURFACE";
                case 12302:
                    return "EGL_CONTEXT_LOST";
                default:
                    return "0x" + Integer.toHexString(error);
            }
        }

        public static String formatEglError(String function, int error) {
            return function + " failed: " + getErrorString(error);
        }
    }

    static class GLThread extends Thread {
        private EglHelper mEglHelper;
        private ArrayList<Runnable> mEventQueue = new ArrayList();
        private boolean mExited;
        private boolean mFinishedCreatingEglSurface;
        private WeakReference<GLSurfaceViewAPI18> mGLSurfaceViewWeakRef;
        private boolean mHasSurface;
        private boolean mHaveEglContext;
        private boolean mHaveEglSurface;
        private int mHeight = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY;
        private boolean mPaused;
        private boolean mRenderComplete;
        private int mRenderMode = GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY;
        private boolean mRequestPaused;
        private boolean mRequestRender = true;
        private boolean mShouldExit;
        private boolean mShouldReleaseEglContext;
        private boolean mSizeChanged = true;
        private boolean mSurfaceIsBad;
        private boolean mWaitingForSurface;
        private int mWidth = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY;

        GLThread(WeakReference<GLSurfaceViewAPI18> glSurfaceViewWeakRef) {
            this.mGLSurfaceViewWeakRef = glSurfaceViewWeakRef;
        }

        public void run() {
            setName("GLThread " + getId());
            try {
                guardedRun();
            } catch (InterruptedException e) {
            } finally {
                GLSurfaceViewAPI18.sGLThreadManager.threadExiting(this);
            }
        }

        private void stopEglSurfaceLocked() {
            if (this.mHaveEglSurface) {
                this.mHaveEglSurface = GLSurfaceViewAPI18.LOG_THREADS;
                this.mEglHelper.destroySurface();
            }
        }

        private void stopEglContextLocked() {
            if (this.mHaveEglContext) {
                this.mEglHelper.finish();
                this.mHaveEglContext = GLSurfaceViewAPI18.LOG_THREADS;
                GLSurfaceViewAPI18.sGLThreadManager.releaseEglContextLocked(this);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void guardedRun() throws java.lang.InterruptedException {
            /*
            r22 = this;
            r19 = new com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18$EglHelper;
            r0 = r22;
            r0 = r0.mGLSurfaceViewWeakRef;
            r20 = r0;
            r19.<init>(r20);
            r0 = r19;
            r1 = r22;
            r1.mEglHelper = r0;
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mHaveEglContext = r0;
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mHaveEglSurface = r0;
            r8 = 0;
            r3 = 0;
            r4 = 0;
            r5 = 0;
            r10 = 0;
            r13 = 0;
            r18 = 0;
            r6 = 0;
            r2 = 0;
            r17 = 0;
            r9 = 0;
            r7 = 0;
        L_0x002f:
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d5 }
            monitor-enter(r20);	 Catch:{ all -> 0x01d5 }
        L_0x0034:
            r0 = r22;
            r0 = r0.mShouldExit;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x004d;
        L_0x003c:
            monitor-exit(r20);	 Catch:{ all -> 0x01d2 }
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;
            monitor-enter(r20);
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x004a }
            r22.stopEglContextLocked();	 Catch:{ all -> 0x004a }
            monitor-exit(r20);	 Catch:{ all -> 0x004a }
            return;
        L_0x004a:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x004a }
            throw r19;
        L_0x004d:
            r0 = r22;
            r0 = r0.mEventQueue;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r19 = r19.isEmpty();	 Catch:{ all -> 0x01d2 }
            if (r19 != 0) goto L_0x0076;
        L_0x0059:
            r0 = r22;
            r0 = r0.mEventQueue;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r21 = 0;
            r0 = r19;
            r1 = r21;
            r19 = r0.remove(r1);	 Catch:{ all -> 0x01d2 }
            r0 = r19;
            r0 = (java.lang.Runnable) r0;	 Catch:{ all -> 0x01d2 }
            r7 = r0;
        L_0x006e:
            monitor-exit(r20);	 Catch:{ all -> 0x01d2 }
            if (r7 == 0) goto L_0x0228;
        L_0x0071:
            r7.run();	 Catch:{ all -> 0x01d5 }
            r7 = 0;
            goto L_0x002f;
        L_0x0076:
            r11 = 0;
            r0 = r22;
            r0 = r0.mPaused;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r0 = r22;
            r0 = r0.mRequestPaused;	 Catch:{ all -> 0x01d2 }
            r21 = r0;
            r0 = r19;
            r1 = r21;
            if (r0 == r1) goto L_0x00a0;
        L_0x0089:
            r0 = r22;
            r11 = r0.mRequestPaused;	 Catch:{ all -> 0x01d2 }
            r0 = r22;
            r0 = r0.mRequestPaused;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r0 = r19;
            r1 = r22;
            r1.mPaused = r0;	 Catch:{ all -> 0x01d2 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
        L_0x00a0:
            r0 = r22;
            r0 = r0.mShouldReleaseEglContext;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x00b7;
        L_0x00a8:
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x01d2 }
            r22.stopEglContextLocked();	 Catch:{ all -> 0x01d2 }
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mShouldReleaseEglContext = r0;	 Catch:{ all -> 0x01d2 }
            r2 = 1;
        L_0x00b7:
            if (r10 == 0) goto L_0x00c0;
        L_0x00b9:
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x01d2 }
            r22.stopEglContextLocked();	 Catch:{ all -> 0x01d2 }
            r10 = 0;
        L_0x00c0:
            if (r11 == 0) goto L_0x00cd;
        L_0x00c2:
            r0 = r22;
            r0 = r0.mHaveEglSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x00cd;
        L_0x00ca:
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x01d2 }
        L_0x00cd:
            if (r11 == 0) goto L_0x00f5;
        L_0x00cf:
            r0 = r22;
            r0 = r0.mHaveEglContext;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x00f5;
        L_0x00d7:
            r0 = r22;
            r0 = r0.mGLSurfaceViewWeakRef;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r16 = r19.get();	 Catch:{ all -> 0x01d2 }
            r16 = (com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18) r16;	 Catch:{ all -> 0x01d2 }
            if (r16 != 0) goto L_0x01e3;
        L_0x00e5:
            r12 = 0;
        L_0x00e6:
            if (r12 == 0) goto L_0x00f2;
        L_0x00e8:
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19 = r19.shouldReleaseEGLContextWhenPausing();	 Catch:{ all -> 0x01d2 }
            if (r19 == 0) goto L_0x00f5;
        L_0x00f2:
            r22.stopEglContextLocked();	 Catch:{ all -> 0x01d2 }
        L_0x00f5:
            if (r11 == 0) goto L_0x010a;
        L_0x00f7:
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19 = r19.shouldTerminateEGLWhenPausing();	 Catch:{ all -> 0x01d2 }
            if (r19 == 0) goto L_0x010a;
        L_0x0101:
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            r19.finish();	 Catch:{ all -> 0x01d2 }
        L_0x010a:
            r0 = r22;
            r0 = r0.mHasSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 != 0) goto L_0x013c;
        L_0x0112:
            r0 = r22;
            r0 = r0.mWaitingForSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 != 0) goto L_0x013c;
        L_0x011a:
            r0 = r22;
            r0 = r0.mHaveEglSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x0125;
        L_0x0122:
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x01d2 }
        L_0x0125:
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mWaitingForSurface = r0;	 Catch:{ all -> 0x01d2 }
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mSurfaceIsBad = r0;	 Catch:{ all -> 0x01d2 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
        L_0x013c:
            r0 = r22;
            r0 = r0.mHasSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x015b;
        L_0x0144:
            r0 = r22;
            r0 = r0.mWaitingForSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x015b;
        L_0x014c:
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mWaitingForSurface = r0;	 Catch:{ all -> 0x01d2 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
        L_0x015b:
            if (r6 == 0) goto L_0x016f;
        L_0x015d:
            r18 = 0;
            r6 = 0;
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mRenderComplete = r0;	 Catch:{ all -> 0x01d2 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
        L_0x016f:
            r19 = r22.readyToDraw();	 Catch:{ all -> 0x01d2 }
            if (r19 == 0) goto L_0x021f;
        L_0x0175:
            r0 = r22;
            r0 = r0.mHaveEglContext;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 != 0) goto L_0x0180;
        L_0x017d:
            if (r2 == 0) goto L_0x01e9;
        L_0x017f:
            r2 = 0;
        L_0x0180:
            r0 = r22;
            r0 = r0.mHaveEglContext;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x019b;
        L_0x0188:
            r0 = r22;
            r0 = r0.mHaveEglSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 != 0) goto L_0x019b;
        L_0x0190:
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mHaveEglSurface = r0;	 Catch:{ all -> 0x01d2 }
            r4 = 1;
            r5 = 1;
            r13 = 1;
        L_0x019b:
            r0 = r22;
            r0 = r0.mHaveEglSurface;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x021f;
        L_0x01a3:
            r0 = r22;
            r0 = r0.mSizeChanged;	 Catch:{ all -> 0x01d2 }
            r19 = r0;
            if (r19 == 0) goto L_0x01c1;
        L_0x01ab:
            r13 = 1;
            r0 = r22;
            r0 = r0.mWidth;	 Catch:{ all -> 0x01d2 }
            r17 = r0;
            r0 = r22;
            r9 = r0.mHeight;	 Catch:{ all -> 0x01d2 }
            r18 = 1;
            r4 = 1;
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mSizeChanged = r0;	 Catch:{ all -> 0x01d2 }
        L_0x01c1:
            r19 = 0;
            r0 = r19;
            r1 = r22;
            r1.mRequestRender = r0;	 Catch:{ all -> 0x01d2 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
            goto L_0x006e;
        L_0x01d2:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x01d2 }
            throw r19;	 Catch:{ all -> 0x01d5 }
        L_0x01d5:
            r19 = move-exception;
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;
            monitor-enter(r20);
            r22.stopEglSurfaceLocked();	 Catch:{ all -> 0x031e }
            r22.stopEglContextLocked();	 Catch:{ all -> 0x031e }
            monitor-exit(r20);	 Catch:{ all -> 0x031e }
            throw r19;
        L_0x01e3:
            r12 = r16.mPreserveEGLContextOnPause;	 Catch:{ all -> 0x01d2 }
            goto L_0x00e6;
        L_0x01e9:
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r0 = r19;
            r1 = r22;
            r19 = r0.tryAcquireEglContextLocked(r1);	 Catch:{ all -> 0x01d2 }
            if (r19 == 0) goto L_0x0180;
        L_0x01f7:
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ RuntimeException -> 0x0212 }
            r19 = r0;
            r19.start();	 Catch:{ RuntimeException -> 0x0212 }
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mHaveEglContext = r0;	 Catch:{ all -> 0x01d2 }
            r3 = 1;
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.notifyAll();	 Catch:{ all -> 0x01d2 }
            goto L_0x0180;
        L_0x0212:
            r15 = move-exception;
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r0 = r19;
            r1 = r22;
            r0.releaseEglContextLocked(r1);	 Catch:{ all -> 0x01d2 }
            throw r15;	 Catch:{ all -> 0x01d2 }
        L_0x021f:
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d2 }
            r19.wait();	 Catch:{ all -> 0x01d2 }
            goto L_0x0034;
        L_0x0228:
            if (r4 == 0) goto L_0x024c;
        L_0x022a:
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r19 = r19.createSurface();	 Catch:{ all -> 0x01d5 }
            if (r19 == 0) goto L_0x02f7;
        L_0x0236:
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d5 }
            monitor-enter(r20);	 Catch:{ all -> 0x01d5 }
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mFinishedCreatingEglSurface = r0;	 Catch:{ all -> 0x02f4 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x02f4 }
            r19.notifyAll();	 Catch:{ all -> 0x02f4 }
            monitor-exit(r20);	 Catch:{ all -> 0x02f4 }
            r4 = 0;
        L_0x024c:
            if (r5 == 0) goto L_0x0267;
        L_0x024e:
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r19 = r19.createGL();	 Catch:{ all -> 0x01d5 }
            r0 = r19;
            r0 = (javax.microedition.khronos.opengles.GL10) r0;	 Catch:{ all -> 0x01d5 }
            r8 = r0;
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d5 }
            r0 = r19;
            r0.checkGLDriver(r8);	 Catch:{ all -> 0x01d5 }
            r5 = 0;
        L_0x0267:
            if (r3 == 0) goto L_0x028f;
        L_0x0269:
            r0 = r22;
            r0 = r0.mGLSurfaceViewWeakRef;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r16 = r19.get();	 Catch:{ all -> 0x01d5 }
            r16 = (com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18) r16;	 Catch:{ all -> 0x01d5 }
            if (r16 == 0) goto L_0x028e;
        L_0x0277:
            r19 = r16.mRenderer;	 Catch:{ all -> 0x01d5 }
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ all -> 0x01d5 }
            r20 = r0;
            r0 = r20;
            r0 = r0.mEglConfig;	 Catch:{ all -> 0x01d5 }
            r20 = r0;
            r0 = r19;
            r1 = r20;
            r0.onSurfaceCreated(r8, r1);	 Catch:{ all -> 0x01d5 }
        L_0x028e:
            r3 = 0;
        L_0x028f:
            if (r13 == 0) goto L_0x02ab;
        L_0x0291:
            r0 = r22;
            r0 = r0.mGLSurfaceViewWeakRef;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r16 = r19.get();	 Catch:{ all -> 0x01d5 }
            r16 = (com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18) r16;	 Catch:{ all -> 0x01d5 }
            if (r16 == 0) goto L_0x02aa;
        L_0x029f:
            r19 = r16.mRenderer;	 Catch:{ all -> 0x01d5 }
            r0 = r19;
            r1 = r17;
            r0.onSurfaceChanged(r8, r1, r9);	 Catch:{ all -> 0x01d5 }
        L_0x02aa:
            r13 = 0;
        L_0x02ab:
            r0 = r22;
            r0 = r0.mGLSurfaceViewWeakRef;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r16 = r19.get();	 Catch:{ all -> 0x01d5 }
            r16 = (com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18) r16;	 Catch:{ all -> 0x01d5 }
            if (r16 == 0) goto L_0x02c2;
        L_0x02b9:
            r19 = r16.mRenderer;	 Catch:{ all -> 0x01d5 }
            r0 = r19;
            r0.onDrawFrame(r8);	 Catch:{ all -> 0x01d5 }
        L_0x02c2:
            r0 = r22;
            r0 = r0.mEglHelper;	 Catch:{ all -> 0x01d5 }
            r19 = r0;
            r14 = r19.swap();	 Catch:{ all -> 0x01d5 }
            switch(r14) {
                case 12288: goto L_0x02ef;
                case 12302: goto L_0x0319;
                default: goto L_0x02cf;
            };	 Catch:{ all -> 0x01d5 }
        L_0x02cf:
            r19 = "GLThread";
            r20 = "eglSwapBuffers";
            r0 = r19;
            r1 = r20;
            com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.EglHelper.logEglErrorAsWarning(r0, r1, r14);	 Catch:{ all -> 0x01d5 }
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d5 }
            monitor-enter(r20);	 Catch:{ all -> 0x01d5 }
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mSurfaceIsBad = r0;	 Catch:{ all -> 0x031b }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x031b }
            r19.notifyAll();	 Catch:{ all -> 0x031b }
            monitor-exit(r20);	 Catch:{ all -> 0x031b }
        L_0x02ef:
            if (r18 == 0) goto L_0x002f;
        L_0x02f1:
            r6 = 1;
            goto L_0x002f;
        L_0x02f4:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x02f4 }
            throw r19;	 Catch:{ all -> 0x01d5 }
        L_0x02f7:
            r20 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x01d5 }
            monitor-enter(r20);	 Catch:{ all -> 0x01d5 }
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mFinishedCreatingEglSurface = r0;	 Catch:{ all -> 0x0316 }
            r19 = 1;
            r0 = r19;
            r1 = r22;
            r1.mSurfaceIsBad = r0;	 Catch:{ all -> 0x0316 }
            r19 = com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.sGLThreadManager;	 Catch:{ all -> 0x0316 }
            r19.notifyAll();	 Catch:{ all -> 0x0316 }
            monitor-exit(r20);	 Catch:{ all -> 0x0316 }
            goto L_0x002f;
        L_0x0316:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x0316 }
            throw r19;	 Catch:{ all -> 0x01d5 }
        L_0x0319:
            r10 = 1;
            goto L_0x02ef;
        L_0x031b:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x031b }
            throw r19;	 Catch:{ all -> 0x01d5 }
        L_0x031e:
            r19 = move-exception;
            monitor-exit(r20);	 Catch:{ all -> 0x031e }
            throw r19;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18.GLThread.guardedRun():void");
        }

        public boolean ableToDraw() {
            return (this.mHaveEglContext && this.mHaveEglSurface && readyToDraw()) ? true : GLSurfaceViewAPI18.LOG_THREADS;
        }

        private boolean readyToDraw() {
            return (this.mPaused || !this.mHasSurface || this.mSurfaceIsBad || this.mWidth <= 0 || this.mHeight <= 0 || !(this.mRequestRender || this.mRenderMode == GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY)) ? GLSurfaceViewAPI18.LOG_THREADS : true;
        }

        public void setRenderMode(int renderMode) {
            if (renderMode < 0 || renderMode > GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY) {
                throw new IllegalArgumentException("renderMode");
            }
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mRenderMode = renderMode;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
            }
        }

        public int getRenderMode() {
            int i;
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                i = this.mRenderMode;
            }
            return i;
        }

        public void requestRender() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mRequestRender = true;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
            }
        }

        public void surfaceCreated() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mHasSurface = true;
                this.mFinishedCreatingEglSurface = GLSurfaceViewAPI18.LOG_THREADS;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (this.mWaitingForSurface && !this.mFinishedCreatingEglSurface && !this.mExited) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void surfaceDestroyed() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mHasSurface = GLSurfaceViewAPI18.LOG_THREADS;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (!this.mWaitingForSurface && !this.mExited) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onPause() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mRequestPaused = true;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onResume() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mRequestPaused = GLSurfaceViewAPI18.LOG_THREADS;
                this.mRequestRender = true;
                this.mRenderComplete = GLSurfaceViewAPI18.LOG_THREADS;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (!this.mExited && this.mPaused && !this.mRenderComplete) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void onWindowResize(int w, int h) {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mWidth = w;
                this.mHeight = h;
                this.mSizeChanged = true;
                this.mRequestRender = true;
                this.mRenderComplete = GLSurfaceViewAPI18.LOG_THREADS;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (!this.mExited && !this.mPaused && !this.mRenderComplete && ableToDraw()) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestExitAndWait() {
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mShouldExit = true;
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
                while (!this.mExited) {
                    try {
                        GLSurfaceViewAPI18.sGLThreadManager.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }

        public void requestReleaseEglContextLocked() {
            this.mShouldReleaseEglContext = true;
            GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
        }

        public void queueEvent(Runnable r) {
            if (r == null) {
                throw new IllegalArgumentException("r must not be null");
            }
            synchronized (GLSurfaceViewAPI18.sGLThreadManager) {
                this.mEventQueue.add(r);
                GLSurfaceViewAPI18.sGLThreadManager.notifyAll();
            }
        }
    }

    private static class GLThreadManager {
        private static String TAG = "GLThreadManager";
        private static final int kGLES_20 = 131072;
        private static final String kMSM7K_RENDERER_PREFIX = "Q3Dimension MSM7500 ";
        private GLThread mEglOwner;
        private boolean mGLESDriverCheckComplete;
        private int mGLESVersion;
        private boolean mGLESVersionCheckComplete;
        private boolean mLimitedGLESContexts;
        private boolean mMultipleGLESContextsAllowed;

        private GLThreadManager() {
        }

        public synchronized void threadExiting(GLThread thread) {
            thread.mExited = true;
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread || this.mEglOwner == null) {
                this.mEglOwner = thread;
                notifyAll();
                return true;
            }
            checkGLESVersion();
            if (this.mMultipleGLESContextsAllowed) {
                return true;
            }
            if (this.mEglOwner != null) {
                this.mEglOwner.requestReleaseEglContextLocked();
            }
            return GLSurfaceViewAPI18.LOG_THREADS;
        }

        public void releaseEglContextLocked(GLThread thread) {
            if (this.mEglOwner == thread) {
                this.mEglOwner = null;
            }
            notifyAll();
        }

        public synchronized boolean shouldReleaseEGLContextWhenPausing() {
            return this.mLimitedGLESContexts;
        }

        public synchronized boolean shouldTerminateEGLWhenPausing() {
            checkGLESVersion();
            return !this.mMultipleGLESContextsAllowed ? true : GLSurfaceViewAPI18.LOG_THREADS;
        }

        public synchronized void checkGLDriver(GL10 gl) {
            boolean z = true;
            synchronized (this) {
                if (!this.mGLESDriverCheckComplete) {
                    checkGLESVersion();
                    String renderer = gl.glGetString(GL20.GL_RENDERER);
                    if (this.mGLESVersion < kGLES_20) {
                        this.mMultipleGLESContextsAllowed = !renderer.startsWith(kMSM7K_RENDERER_PREFIX) ? true : GLSurfaceViewAPI18.LOG_THREADS;
                        notifyAll();
                    }
                    if (this.mMultipleGLESContextsAllowed) {
                        z = GLSurfaceViewAPI18.LOG_THREADS;
                    }
                    this.mLimitedGLESContexts = z;
                    this.mGLESDriverCheckComplete = true;
                }
            }
        }

        private void checkGLESVersion() {
            if (!this.mGLESVersionCheckComplete) {
                this.mGLESVersion = kGLES_20;
                if (this.mGLESVersion >= kGLES_20) {
                    this.mMultipleGLESContextsAllowed = true;
                }
                this.mGLESVersionCheckComplete = true;
            }
        }
    }

    public interface GLWrapper {
        GL wrap(GL gl);
    }

    static class LogWriter extends Writer {
        private StringBuilder mBuilder = new StringBuilder();

        LogWriter() {
        }

        public void close() {
            flushBuilder();
        }

        public void flush() {
            flushBuilder();
        }

        public void write(char[] buf, int offset, int count) {
            for (int i = GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY; i < count; i += GLSurfaceViewAPI18.RENDERMODE_CONTINUOUSLY) {
                char c = buf[offset + i];
                if (c == '\n') {
                    flushBuilder();
                } else {
                    this.mBuilder.append(c);
                }
            }
        }

        private void flushBuilder() {
            if (this.mBuilder.length() > 0) {
                Log.v("GLSurfaceView", this.mBuilder.toString());
                this.mBuilder.delete(GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, this.mBuilder.length());
            }
        }
    }

    private class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer) {
            super(8, 8, 8, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, withDepthBuffer ? 16 : GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY, GLSurfaceViewAPI18.RENDERMODE_WHEN_DIRTY);
        }
    }

    public GLSurfaceViewAPI18(Context context) {
        super(context);
        init();
    }

    public GLSurfaceViewAPI18(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mGLThread != null) {
                this.mGLThread.requestExitAndWait();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        if (VERSION.SDK_INT <= 8) {
            holder.setFormat(4);
        }
    }

    public void setGLWrapper(GLWrapper glWrapper) {
        this.mGLWrapper = glWrapper;
    }

    public void setDebugFlags(int debugFlags) {
        this.mDebugFlags = debugFlags;
    }

    public int getDebugFlags() {
        return this.mDebugFlags;
    }

    public void setPreserveEGLContextOnPause(boolean preserveOnPause) {
        this.mPreserveEGLContextOnPause = preserveOnPause;
    }

    public boolean getPreserveEGLContextOnPause() {
        return this.mPreserveEGLContextOnPause;
    }

    public void setRenderer(Renderer renderer) {
        checkRenderThreadState();
        if (this.mEGLConfigChooser == null) {
            this.mEGLConfigChooser = new SimpleEGLConfigChooser(true);
        }
        if (this.mEGLContextFactory == null) {
            this.mEGLContextFactory = new DefaultContextFactory();
        }
        if (this.mEGLWindowSurfaceFactory == null) {
            this.mEGLWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
        }
        this.mRenderer = renderer;
        this.mGLThread = new GLThread(this.mThisWeakRef);
        this.mGLThread.start();
    }

    public void setEGLContextFactory(EGLContextFactory factory) {
        checkRenderThreadState();
        this.mEGLContextFactory = factory;
    }

    public void setEGLWindowSurfaceFactory(EGLWindowSurfaceFactory factory) {
        checkRenderThreadState();
        this.mEGLWindowSurfaceFactory = factory;
    }

    public void setEGLConfigChooser(EGLConfigChooser configChooser) {
        checkRenderThreadState();
        this.mEGLConfigChooser = configChooser;
    }

    public void setEGLConfigChooser(boolean needDepth) {
        setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth));
    }

    public void setEGLConfigChooser(int redSize, int greenSize, int blueSize, int alphaSize, int depthSize, int stencilSize) {
        setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize, blueSize, alphaSize, depthSize, stencilSize));
    }

    public void setEGLContextClientVersion(int version) {
        checkRenderThreadState();
        this.mEGLContextClientVersion = version;
    }

    public void setRenderMode(int renderMode) {
        this.mGLThread.setRenderMode(renderMode);
    }

    public int getRenderMode() {
        return this.mGLThread.getRenderMode();
    }

    public void requestRender() {
        this.mGLThread.requestRender();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        this.mGLThread.surfaceCreated();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.mGLThread.surfaceDestroyed();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        this.mGLThread.onWindowResize(w, h);
    }

    public void onPause() {
        this.mGLThread.onPause();
    }

    public void onResume() {
        this.mGLThread.onResume();
    }

    public void queueEvent(Runnable r) {
        this.mGLThread.queueEvent(r);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this.mDetached && this.mRenderer != null) {
            int renderMode = RENDERMODE_CONTINUOUSLY;
            if (this.mGLThread != null) {
                renderMode = this.mGLThread.getRenderMode();
            }
            this.mGLThread = new GLThread(this.mThisWeakRef);
            if (renderMode != RENDERMODE_CONTINUOUSLY) {
                this.mGLThread.setRenderMode(renderMode);
            }
            this.mGLThread.start();
        }
        this.mDetached = LOG_THREADS;
    }

    protected void onDetachedFromWindow() {
        if (this.mGLThread != null) {
            this.mGLThread.requestExitAndWait();
        }
        this.mDetached = true;
        super.onDetachedFromWindow();
    }

    private void checkRenderThreadState() {
        if (this.mGLThread != null) {
            throw new IllegalStateException("setRenderer has already been called for this instance.");
        }
    }
}
