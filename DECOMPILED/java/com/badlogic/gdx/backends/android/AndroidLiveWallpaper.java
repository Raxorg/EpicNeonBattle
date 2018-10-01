package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.backends.android.surfaceview.FillResolutionStrategy;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Clipboard;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class AndroidLiveWallpaper implements AndroidApplicationBase {
    protected AndroidAudio audio;
    AndroidClipboard clipboard;
    protected final Array<Runnable> executedRunnables = new Array();
    protected AndroidFiles files;
    protected boolean firstResume = true;
    protected AndroidGraphicsLiveWallpaper graphics;
    protected AndroidInput input;
    protected final Array<LifecycleListener> lifecycleListeners = new Array();
    protected ApplicationListener listener;
    protected int logLevel = 2;
    protected AndroidNet net;
    protected final Array<Runnable> runnables = new Array();
    protected AndroidLiveWallpaperService service;

    static {
        GdxNativesLoader.load();
    }

    public AndroidLiveWallpaper(AndroidLiveWallpaperService service) {
        this.service = service;
    }

    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
        if (getVersion() < 8) {
            throw new GdxRuntimeException("LibGDX requires Android API Level 8 or later.");
        }
        this.graphics = new AndroidGraphicsLiveWallpaper(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);
        this.input = AndroidInputFactory.newAndroidInput(this, getService(), this.graphics.view, config);
        this.audio = new AndroidAudio(getService(), config);
        getService().getFilesDir();
        this.files = new AndroidFiles(getService().getAssets(), getService().getFilesDir().getAbsolutePath());
        this.net = new AndroidNet(this);
        this.listener = listener;
        Gdx.app = this;
        Gdx.input = this.input;
        Gdx.audio = this.audio;
        Gdx.files = this.files;
        Gdx.graphics = this.graphics;
        Gdx.net = this.net;
    }

    public void onPause() {
        if (AndroidLiveWallpaperService.DEBUG) {
            Log.d("WallpaperService", " > AndroidLiveWallpaper - onPause()");
        }
        this.audio.pause();
        this.input.onPause();
        if (this.graphics != null) {
            this.graphics.onPauseGLSurfaceView();
        }
        if (AndroidLiveWallpaperService.DEBUG) {
            Log.d("WallpaperService", " > AndroidLiveWallpaper - onPause() done!");
        }
    }

    public void onResume() {
        Gdx.app = this;
        Gdx.input = this.input;
        Gdx.audio = this.audio;
        Gdx.files = this.files;
        Gdx.graphics = this.graphics;
        Gdx.net = this.net;
        this.input.onResume();
        if (this.graphics != null) {
            this.graphics.onResumeGLSurfaceView();
        }
        if (this.firstResume) {
            this.firstResume = false;
            return;
        }
        this.audio.resume();
        this.graphics.resume();
    }

    public void onDestroy() {
        if (this.graphics != null) {
            this.graphics.onDestroyGLSurfaceView();
        }
        if (this.audio != null) {
            this.audio.dispose();
        }
    }

    public WindowManager getWindowManager() {
        return this.service.getWindowManager();
    }

    public AndroidLiveWallpaperService getService() {
        return this.service;
    }

    public ApplicationListener getApplicationListener() {
        return this.listener;
    }

    public void postRunnable(Runnable runnable) {
        synchronized (this.runnables) {
            this.runnables.add(runnable);
        }
    }

    public Audio getAudio() {
        return this.audio;
    }

    public Files getFiles() {
        return this.files;
    }

    public Graphics getGraphics() {
        return this.graphics;
    }

    public AndroidInput getInput() {
        return this.input;
    }

    public Net getNet() {
        return this.net;
    }

    public ApplicationType getType() {
        return ApplicationType.Android;
    }

    public int getVersion() {
        return VERSION.SDK_INT;
    }

    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public long getNativeHeap() {
        return Debug.getNativeHeapAllocatedSize();
    }

    public Preferences getPreferences(String name) {
        return new AndroidPreferences(this.service.getSharedPreferences(name, 0));
    }

    public Clipboard getClipboard() {
        if (this.clipboard == null) {
            this.clipboard = new AndroidClipboard(this.service);
        }
        return this.clipboard;
    }

    public void debug(String tag, String message) {
        if (this.logLevel >= 3) {
            Log.d(tag, message);
        }
    }

    public void debug(String tag, String message, Throwable exception) {
        if (this.logLevel >= 3) {
            Log.d(tag, message, exception);
        }
    }

    public void log(String tag, String message) {
        if (this.logLevel >= 2) {
            Log.i(tag, message);
        }
    }

    public void log(String tag, String message, Throwable exception) {
        if (this.logLevel >= 2) {
            Log.i(tag, message, exception);
        }
    }

    public void error(String tag, String message) {
        if (this.logLevel >= 1) {
            Log.e(tag, message);
        }
    }

    public void error(String tag, String message, Throwable exception) {
        if (this.logLevel >= 1) {
            Log.e(tag, message, exception);
        }
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public int getLogLevel() {
        return this.logLevel;
    }

    public void exit() {
    }

    public void addLifecycleListener(LifecycleListener listener) {
        synchronized (this.lifecycleListeners) {
            this.lifecycleListeners.add(listener);
        }
    }

    public void removeLifecycleListener(LifecycleListener listener) {
        synchronized (this.lifecycleListeners) {
            this.lifecycleListeners.removeValue(listener, true);
        }
    }

    public Context getContext() {
        return this.service;
    }

    public Array<Runnable> getRunnables() {
        return this.runnables;
    }

    public Array<Runnable> getExecutedRunnables() {
        return this.executedRunnables;
    }

    public Array<LifecycleListener> getLifecycleListeners() {
        return this.lifecycleListeners;
    }

    public void startActivity(Intent intent) {
        this.service.startActivity(intent);
    }

    public Window getApplicationWindow() {
        throw new UnsupportedOperationException();
    }

    public Handler getHandler() {
        throw new UnsupportedOperationException();
    }

    public void runOnUiThread(Runnable runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            new Handler(Looper.getMainLooper()).post(runnable);
        } else {
            runnable.run();
        }
    }

    public void useImmersiveMode(boolean b) {
        throw new UnsupportedOperationException();
    }
}
