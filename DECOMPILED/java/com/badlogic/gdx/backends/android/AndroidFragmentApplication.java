package com.badlogic.gdx.backends.android;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Debug;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
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
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.compression.lzma.Base;

public class AndroidFragmentApplication extends Fragment implements AndroidApplicationBase {
    private final Array<AndroidEventListener> androidEventListeners = new Array();
    protected AndroidAudio audio;
    protected Callbacks callbacks;
    AndroidClipboard clipboard;
    protected final Array<Runnable> executedRunnables = new Array();
    protected AndroidFiles files;
    protected boolean firstResume = true;
    protected AndroidGraphics graphics;
    public Handler handler;
    protected AndroidInput input;
    protected final Array<LifecycleListener> lifecycleListeners = new Array();
    protected ApplicationListener listener;
    protected int logLevel = 2;
    protected AndroidNet net;
    protected final Array<Runnable> runnables = new Array();

    public interface Callbacks {
        void exit();
    }

    static {
        GdxNativesLoader.load();
    }

    public void onAttach(Activity activity) {
        if (activity instanceof Callbacks) {
            this.callbacks = (Callbacks) activity;
        } else if (getParentFragment() instanceof Callbacks) {
            this.callbacks = (Callbacks) getParentFragment();
        } else if (getTargetFragment() instanceof Callbacks) {
            this.callbacks = (Callbacks) getTargetFragment();
        } else {
            throw new RuntimeException("Missing AndroidFragmentApplication.Callbacks. Please implement AndroidFragmentApplication.Callbacks on the parent activity, fragment or target fragment.");
        }
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
        this.callbacks = null;
    }

    protected LayoutParams createLayoutParams() {
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        layoutParams.gravity = 17;
        return layoutParams;
    }

    protected void createWakeLock(boolean use) {
        if (use) {
            getActivity().getWindow().addFlags(Base.kNumFullDistances);
        }
    }

    @TargetApi(19)
    public void useImmersiveMode(boolean use) {
        if (use && getVersion() >= 19) {
            try {
                View view = this.graphics.getView();
                View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.TYPE}).invoke(view, new Object[]{Integer.valueOf(5894)});
            } catch (Exception e) {
                log("AndroidApplication", "Failed to setup immersive mode, a throwable has occurred.", e);
            }
        }
    }

    public View initializeForView(ApplicationListener listener) {
        return initializeForView(listener, new AndroidApplicationConfiguration());
    }

    public View initializeForView(ApplicationListener listener, AndroidApplicationConfiguration config) {
        if (getVersion() < 8) {
            throw new GdxRuntimeException("LibGDX requires Android API Level 8 or later.");
        }
        this.graphics = new AndroidGraphics(this, config, config.resolutionStrategy == null ? new FillResolutionStrategy() : config.resolutionStrategy);
        this.input = AndroidInputFactory.newAndroidInput(this, getActivity(), this.graphics.view, config);
        this.audio = new AndroidAudio(getActivity(), config);
        this.files = new AndroidFiles(getResources().getAssets(), getActivity().getFilesDir().getAbsolutePath());
        this.net = new AndroidNet(this);
        this.listener = listener;
        this.handler = new Handler();
        addLifecycleListener(new LifecycleListener() {
            public void resume() {
                AndroidFragmentApplication.this.audio.resume();
            }

            public void pause() {
                AndroidFragmentApplication.this.audio.pause();
            }

            public void dispose() {
                AndroidFragmentApplication.this.audio.dispose();
            }
        });
        Gdx.app = this;
        Gdx.input = getInput();
        Gdx.audio = getAudio();
        Gdx.files = getFiles();
        Gdx.graphics = getGraphics();
        Gdx.net = getNet();
        createWakeLock(config.useWakelock);
        useImmersiveMode(config.useImmersiveMode);
        if (config.useImmersiveMode && getVersion() >= 19) {
            try {
                Class<?> vlistener = Class.forName("com.badlogic.gdx.backends.android.AndroidVisibilityListener");
                vlistener.getDeclaredMethod("createListener", new Class[]{AndroidApplicationBase.class}).invoke(vlistener.newInstance(), new Object[]{this});
            } catch (Exception e) {
                log("AndroidApplication", "Failed to create AndroidVisibilityListener", e);
            }
        }
        return this.graphics.getView();
    }

    public void onPause() {
        boolean isContinuous = this.graphics.isContinuousRendering();
        boolean isContinuousEnforced = AndroidGraphics.enforceContinuousRendering;
        AndroidGraphics.enforceContinuousRendering = true;
        this.graphics.setContinuousRendering(true);
        this.graphics.pause();
        this.input.onPause();
        if (isRemoving() || isAnyParentFragmentRemoving() || getActivity().isFinishing()) {
            this.graphics.clearManagedCaches();
            this.graphics.destroy();
        }
        AndroidGraphics.enforceContinuousRendering = isContinuousEnforced;
        this.graphics.setContinuousRendering(isContinuous);
        this.graphics.onPauseGLSurfaceView();
        super.onPause();
    }

    public void onResume() {
        Gdx.app = this;
        Gdx.input = getInput();
        Gdx.audio = getAudio();
        Gdx.files = getFiles();
        Gdx.graphics = getGraphics();
        Gdx.net = getNet();
        this.input.onResume();
        if (this.graphics != null) {
            this.graphics.onResumeGLSurfaceView();
        }
        if (this.firstResume) {
            this.firstResume = false;
        } else {
            this.graphics.resume();
        }
        super.onResume();
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
        return new AndroidPreferences(getActivity().getSharedPreferences(name, 0));
    }

    public Clipboard getClipboard() {
        if (this.clipboard == null) {
            this.clipboard = new AndroidClipboard(getActivity());
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
                AndroidFragmentApplication.this.callbacks.exit();
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
        return getActivity();
    }

    public Array<Runnable> getRunnables() {
        return this.runnables;
    }

    public Array<Runnable> getExecutedRunnables() {
        return this.executedRunnables;
    }

    public void runOnUiThread(Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }

    public Array<LifecycleListener> getLifecycleListeners() {
        return this.lifecycleListeners;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        synchronized (this.androidEventListeners) {
            for (int i = 0; i < this.androidEventListeners.size; i++) {
                ((AndroidEventListener) this.androidEventListeners.get(i)).onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    public void addAndroidEventListener(AndroidEventListener listener) {
        synchronized (this.androidEventListeners) {
            this.androidEventListeners.add(listener);
        }
    }

    public void removeAndroidEventListener(AndroidEventListener listener) {
        synchronized (this.androidEventListeners) {
            this.androidEventListeners.removeValue(listener, true);
        }
    }

    public Window getApplicationWindow() {
        return getActivity().getWindow();
    }

    public Handler getHandler() {
        return this.handler;
    }

    public WindowManager getWindowManager() {
        return (WindowManager) getContext().getSystemService("window");
    }

    private boolean isAnyParentFragmentRemoving() {
        for (Fragment fragment = getParentFragment(); fragment != null; fragment = fragment.getParentFragment()) {
            if (fragment.isRemoving()) {
                return true;
            }
        }
        return false;
    }
}
