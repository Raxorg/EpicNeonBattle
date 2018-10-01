package com.badlogic.gdx.backends.android;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build.VERSION;
import com.badlogic.gdx.Preferences;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Map;
import java.util.Map.Entry;

public class AndroidPreferences implements Preferences {
    Editor editor;
    SharedPreferences sharedPrefs;

    public AndroidPreferences(SharedPreferences preferences) {
        this.sharedPrefs = preferences;
    }

    public Preferences putBoolean(String key, boolean val) {
        edit();
        this.editor.putBoolean(key, val);
        return this;
    }

    public Preferences putInteger(String key, int val) {
        edit();
        this.editor.putInt(key, val);
        return this;
    }

    public Preferences putLong(String key, long val) {
        edit();
        this.editor.putLong(key, val);
        return this;
    }

    public Preferences putFloat(String key, float val) {
        edit();
        this.editor.putFloat(key, val);
        return this;
    }

    public Preferences putString(String key, String val) {
        edit();
        this.editor.putString(key, val);
        return this;
    }

    public Preferences put(Map<String, ?> vals) {
        edit();
        for (Entry<String, ?> val : vals.entrySet()) {
            if (val.getValue() instanceof Boolean) {
                putBoolean((String) val.getKey(), ((Boolean) val.getValue()).booleanValue());
            }
            if (val.getValue() instanceof Integer) {
                putInteger((String) val.getKey(), ((Integer) val.getValue()).intValue());
            }
            if (val.getValue() instanceof Long) {
                putLong((String) val.getKey(), ((Long) val.getValue()).longValue());
            }
            if (val.getValue() instanceof String) {
                putString((String) val.getKey(), (String) val.getValue());
            }
            if (val.getValue() instanceof Float) {
                putFloat((String) val.getKey(), ((Float) val.getValue()).floatValue());
            }
        }
        return this;
    }

    public boolean getBoolean(String key) {
        return this.sharedPrefs.getBoolean(key, false);
    }

    public int getInteger(String key) {
        return this.sharedPrefs.getInt(key, 0);
    }

    public long getLong(String key) {
        return this.sharedPrefs.getLong(key, 0);
    }

    public float getFloat(String key) {
        return this.sharedPrefs.getFloat(key, 0.0f);
    }

    public String getString(String key) {
        return this.sharedPrefs.getString(key, BuildConfig.VERSION_NAME);
    }

    public boolean getBoolean(String key, boolean defValue) {
        return this.sharedPrefs.getBoolean(key, defValue);
    }

    public int getInteger(String key, int defValue) {
        return this.sharedPrefs.getInt(key, defValue);
    }

    public long getLong(String key, long defValue) {
        return this.sharedPrefs.getLong(key, defValue);
    }

    public float getFloat(String key, float defValue) {
        return this.sharedPrefs.getFloat(key, defValue);
    }

    public String getString(String key, String defValue) {
        return this.sharedPrefs.getString(key, defValue);
    }

    public Map<String, ?> get() {
        return this.sharedPrefs.getAll();
    }

    public boolean contains(String key) {
        return this.sharedPrefs.contains(key);
    }

    public void clear() {
        edit();
        this.editor.clear();
    }

    public void flush() {
        if (this.editor != null) {
            if (VERSION.SDK_INT >= 9) {
                this.editor.apply();
            } else {
                this.editor.commit();
            }
            this.editor = null;
        }
    }

    public void remove(String key) {
        edit();
        this.editor.remove(key);
    }

    private void edit() {
        if (this.editor == null) {
            this.editor = this.sharedPrefs.edit();
        }
    }
}
