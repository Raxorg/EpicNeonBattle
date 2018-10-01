package com.badlogic.gdx.backends.android;

import android.view.View.OnSystemUiVisibilityChangeListener;

public class AndroidVisibilityListener {
    public void createListener(final AndroidApplicationBase application) {
        try {
            application.getApplicationWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
                public void onSystemUiVisibilityChange(int arg0) {
                    application.getHandler().post(new Runnable() {
                        public void run() {
                            application.useImmersiveMode(true);
                        }
                    });
                }
            });
        } catch (Throwable t) {
            application.log("AndroidApplication", "Can't create OnSystemUiVisibilityChangeListener, unable to use immersive mode.", t);
        }
    }
}
