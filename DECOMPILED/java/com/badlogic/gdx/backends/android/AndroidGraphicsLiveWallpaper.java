package com.badlogic.gdx.backends.android;

import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.EGLConfigChooser;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceView20API18;
import com.badlogic.gdx.backends.android.surfaceview.GLSurfaceViewAPI18;
import com.badlogic.gdx.backends.android.surfaceview.ResolutionStrategy;
import com.badlogic.gdx.utils.GdxRuntimeException;
import javax.microedition.khronos.opengles.GL10;

public final class AndroidGraphicsLiveWallpaper extends AndroidGraphics {
    public AndroidGraphicsLiveWallpaper(AndroidLiveWallpaper lwp, AndroidApplicationConfiguration config, ResolutionStrategy resolutionStrategy) {
        super(lwp, config, resolutionStrategy, false);
    }

    SurfaceHolder getSurfaceHolder() {
        SurfaceHolder surfaceHolder;
        synchronized (((AndroidLiveWallpaper) this.app).service.sync) {
            surfaceHolder = ((AndroidLiveWallpaper) this.app).service.getSurfaceHolder();
        }
        return surfaceHolder;
    }

    protected View createGLSurfaceView(AndroidApplicationBase application, ResolutionStrategy resolutionStrategy) {
        if (checkGL20()) {
            GLSurfaceView20API18 view;
            EGLConfigChooser configChooser = getEglConfigChooser();
            if (VERSION.SDK_INT > 10 || !this.config.useGLSurfaceView20API18) {
                view = new GLSurfaceView20(application.getContext(), resolutionStrategy) {
                    public SurfaceHolder getHolder() {
                        return AndroidGraphicsLiveWallpaper.this.getSurfaceHolder();
                    }

                    public void onDestroy() {
                        onDetachedFromWindow();
                    }
                };
                if (configChooser != null) {
                    view.setEGLConfigChooser(configChooser);
                } else {
                    view.setEGLConfigChooser(this.config.r, this.config.g, this.config.b, this.config.a, this.config.depth, this.config.stencil);
                }
                view.setRenderer(this);
            } else {
                view = new GLSurfaceView20API18(application.getContext(), resolutionStrategy) {
                    public SurfaceHolder getHolder() {
                        return AndroidGraphicsLiveWallpaper.this.getSurfaceHolder();
                    }

                    public void onDestroy() {
                        onDetachedFromWindow();
                    }
                };
                if (configChooser != null) {
                    view.setEGLConfigChooser(configChooser);
                } else {
                    view.setEGLConfigChooser(this.config.r, this.config.g, this.config.b, this.config.a, this.config.depth, this.config.stencil);
                }
                view.setRenderer(this);
            }
            return view;
        }
        throw new GdxRuntimeException("Libgdx requires OpenGL ES 2.0");
    }

    public void onDestroyGLSurfaceView() {
        if (this.view == null) {
            return;
        }
        if ((this.view instanceof GLSurfaceView) || (this.view instanceof GLSurfaceViewAPI18)) {
            try {
                this.view.getClass().getMethod("onDestroy", new Class[0]).invoke(this.view, new Object[0]);
                if (AndroidLiveWallpaperService.DEBUG) {
                    Log.d("WallpaperService", " > AndroidLiveWallpaper - onDestroy() stopped GLThread managed by GLSurfaceView");
                }
            } catch (Throwable t) {
                Log.e("WallpaperService", "failed to destroy GLSurfaceView's thread! GLSurfaceView.onDetachedFromWindow impl changed since API lvl 16!");
                t.printStackTrace();
            }
        }
    }

    void resume() {
        synchronized (this.synch) {
            this.running = true;
            this.resume = true;
            while (this.resume) {
                try {
                    this.synch.wait();
                } catch (InterruptedException e) {
                    Gdx.app.log("AndroidGraphics", "waiting for resume synchronization failed!");
                }
            }
        }
    }

    public void onDrawFrame(GL10 gl) {
        long time = System.nanoTime();
        this.deltaTime = ((float) (time - this.lastFrameTime)) / 1.0E9f;
        this.lastFrameTime = time;
        if (this.resume) {
            this.deltaTime = 0.0f;
        } else {
            this.mean.addValue(this.deltaTime);
        }
        synchronized (this.synch) {
            boolean lrunning = this.running;
            boolean lpause = this.pause;
            boolean ldestroy = this.destroy;
            boolean lresume = this.resume;
            if (this.resume) {
                this.resume = false;
                this.synch.notifyAll();
            }
            if (this.pause) {
                this.pause = false;
                this.synch.notifyAll();
            }
            if (this.destroy) {
                this.destroy = false;
                this.synch.notifyAll();
            }
        }
        if (lresume) {
            this.app.getApplicationListener().resume();
            Gdx.app.log("AndroidGraphics", "resumed");
        }
        if (lrunning) {
            synchronized (this.app.getRunnables()) {
                this.app.getExecutedRunnables().clear();
                this.app.getExecutedRunnables().addAll(this.app.getRunnables());
                this.app.getRunnables().clear();
                for (int i = 0; i < this.app.getExecutedRunnables().size; i++) {
                    try {
                        ((Runnable) this.app.getExecutedRunnables().get(i)).run();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
            this.app.getInput().processEvents();
            this.frameId++;
            this.app.getApplicationListener().render();
        }
        if (lpause) {
            this.app.getApplicationListener().pause();
            Gdx.app.log("AndroidGraphics", "paused");
        }
        if (ldestroy) {
            this.app.getApplicationListener().dispose();
            Gdx.app.log("AndroidGraphics", "destroyed");
        }
        if (time - this.frameStart > 1000000000) {
            this.fps = this.frames;
            this.frames = 0;
            this.frameStart = time;
        }
        this.frames++;
    }

    protected void logManagedCachesStatus() {
        if (AndroidLiveWallpaperService.DEBUG) {
            super.logManagedCachesStatus();
        }
    }
}
