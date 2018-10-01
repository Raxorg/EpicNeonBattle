package com.badlogic.gdx.backends.android;

import android.content.Context;
import android.os.Build.VERSION;
import com.badlogic.gdx.Application;

public class AndroidInputFactory {
    public static AndroidInput newAndroidInput(Application activity, Context context, Object view, AndroidApplicationConfiguration config) {
        try {
            Class<?> clazz;
            if (VERSION.SDK_INT >= 12) {
                clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInputThreePlus");
            } else {
                clazz = Class.forName("com.badlogic.gdx.backends.android.AndroidInput");
            }
            return (AndroidInput) clazz.getConstructor(new Class[]{Application.class, Context.class, Object.class, AndroidApplicationConfiguration.class}).newInstance(new Object[]{activity, context, view, config});
        } catch (Exception e) {
            throw new RuntimeException("Couldn't construct AndroidInput, this should never happen", e);
        }
    }
}
