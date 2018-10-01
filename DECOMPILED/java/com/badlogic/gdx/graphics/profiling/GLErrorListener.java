package com.badlogic.gdx.graphics.profiling;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

public interface GLErrorListener {
    public static final GLErrorListener LOGGING_LISTENER = new GLErrorListener() {
        public void onError(int error) {
            String place = null;
            try {
                StackTraceElement[] stack = Thread.currentThread().getStackTrace();
                for (int i = 0; i < stack.length; i++) {
                    if ("check".equals(stack[i].getMethodName())) {
                        if (i + 1 < stack.length) {
                            place = stack[i + 1].getMethodName();
                        }
                        if (place == null) {
                            Gdx.app.error("GLProfiler", "Error " + GLProfiler.resolveErrorNumber(error) + " from " + place);
                        } else {
                            Gdx.app.error("GLProfiler", "Error " + GLProfiler.resolveErrorNumber(error) + " at: ", new Exception());
                        }
                    }
                }
            } catch (Exception e) {
            }
            if (place == null) {
                Gdx.app.error("GLProfiler", "Error " + GLProfiler.resolveErrorNumber(error) + " at: ", new Exception());
            } else {
                Gdx.app.error("GLProfiler", "Error " + GLProfiler.resolveErrorNumber(error) + " from " + place);
            }
        }
    };
    public static final GLErrorListener THROWING_LISTENER = new GLErrorListener() {
        public void onError(int error) {
            throw new GdxRuntimeException("GLProfiler: Got GL error " + GLProfiler.resolveErrorNumber(error));
        }
    };

    void onError(int i);
}
