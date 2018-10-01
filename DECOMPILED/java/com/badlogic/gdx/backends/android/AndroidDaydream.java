package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.service.dreams.DreamService;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
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
import com.badlogic.gdx.utils.compression.lzma.Base;
import java.lang.reflect.Method;
import java.util.Arrays;

public class AndroidDaydream extends DreamService implements AndroidApplicationBase {
    protected AndroidAudio audio;
    AndroidClipboard clipboard;
    protected final Array<Runnable> executedRunnables = new Array();
    protected AndroidFiles files;
    protected boolean firstResume = true;
    protected AndroidGraphics graphics;
    protected Handler handler;
    protected AndroidInput input;
    protected final Array<LifecycleListener> lifecycleListeners = new Array();
    protected ApplicationListener listener;
    protected int logLevel = 2;
    protected AndroidNet net;
    protected final Array<Runnable> runnables = new Array();

    static {
        GdxNativesLoader.load();
    }

    public void initialize(ApplicationListener listener) {
        initialize(listener, new AndroidApplicationConfiguration());
    }

    public void initialize(ApplicationListener listener, AndroidApplicationConfiguration config) {
        init(listener, config, false);
    }

    public View initializeForView(ApplicationListener listener) {
        return initializeForView(listener, new AndroidApplicationConfiguration());
    }

    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        init(listener, config, true);
        return this.graphics.getView();
    }

    private void init(ApplicationListener listener, AndroidApplicationConfiguration config, boolean isForView) {
        this.graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);
        this.input = AndroidInputFactory.newAndroidInput(this, this, this.graphics.view, config);
        this.audio = new AndroidAudio(this, config);
        getFilesDir();
        this.files = new AndroidFiles(getAssets(), getFilesDir().getAbsolutePath());
        this.net = new AndroidNet(this);
        this.listener = listener;
        this.handler = new Handler();
        addLifecycleListener(new LifecycleListener() {
            public void resume() {
                AndroidDaydream.this.audio.resume();
            }

            public void pause() {
                AndroidDaydream.this.audio.pause();
            }

            public void dispose() {
                AndroidDaydream.this.audio.dispose();
                AndroidDaydream.this.audio = null;
            }
        });
        Gdx.app = this;
        Gdx.input = getInput();
        Gdx.audio = getAudio();
        Gdx.files = getFiles();
        Gdx.graphics = getGraphics();
        Gdx.net = getNet();
        if (!isForView) {
            setFullscreen(true);
            setContentView(this.graphics.getView(), createLayoutParams());
        }
        createWakeLock(config.useWakelock);
        hideStatusBar(config);
    }

    protected LayoutParams createLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.gravity = 17;
        return layoutParams;
    }

    protected void createWakeLock(boolean use) {
        if (use) {
            getWindow().addFlags(Base.kNumFullDistances);
        }
    }

    protected void hideStatusBar(AndroidApplicationConfiguration config) {
        if (config.hideStatusBar && getVersion() >= 11) {
            View rootView = getWindow().getDecorView();
            try {
                Method m = View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.TYPE});
                m.invoke(rootView, new Object[]{Integer.valueOf(0)});
                m.invoke(rootView, new Object[]{Integer.valueOf(1)});
            } catch (Exception e) {
                log("AndroidApplication", "Can't hide status bar", e);
            }
        }
    }

    public void onDreamingStopped() {
        boolean isContinuous = this.graphics.isContinuousRendering();
        this.graphics.setContinuousRendering(true);
        this.graphics.pause();
        this.input.unregisterSensorListeners();
        Arrays.fill(this.input.realId, -1);
        Arrays.fill(this.input.touched, false);
        this.graphics.clearManagedCaches();
        this.graphics.destroy();
        this.graphics.setContinuousRendering(isContinuous);
        this.graphics.onPauseGLSurfaceView();
        super.onDreamingStopped();
    }

    public void onDreamingStarted() {
        Gdx.app = this;
        Gdx.input = getInput();
        Gdx.audio = getAudio();
        Gdx.files = getFiles();
        Gdx.graphics = getGraphics();
        Gdx.net = getNet();
        getInput().registerSensorListeners();
        if (this.graphics != null) {
            this.graphics.onResumeGLSurfaceView();
        }
        if (this.firstResume) {
            this.firstResume = false;
        } else {
            this.graphics.resume();
        }
        super.onDreamingStarted();
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public ApplicationListener getApplicationListener() {
        return this.listener;
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
        return new AndroidPreferences(getSharedPreferences(name, 0));
    }

    public Clipboard getClipboard() {
        if (this.clipboard == null) {
            this.clipboard = new AndroidClipboard(this);
        }
        return this.clipboard;
    }

    public void postRunnable(Runnable runnable) {
        synchronized (this.runnables) {
            this.runnables.add(runnable);
            Gdx.graphics.requestRendering();
        }
    }

    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        boolean keyboardAvailable = false;
        if (config.hardKeyboardHidden == 1) {
            keyboardAvailable = true;
        }
        this.input.keyboardAvailable = keyboardAvailable;
    }

    public void exit() {
        this.handler.post(new Runnable() {
            public void run() {
                AndroidDaydream.this.finish();
            }
        });
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
        return this;
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

    public Window getApplicationWindow() {
        return getWindow();
    }

    public Handler getHandler() {
        return this.handler;
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
