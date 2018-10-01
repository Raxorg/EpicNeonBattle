package com.badlogic.gdx.utils;

import com.badlogic.gdx.graphics.GL20;
import com.epicness.neonbattle.android.BuildConfig;

public class SerializationException extends RuntimeException {
    private StringBuffer trace;

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(Throwable cause) {
        super(BuildConfig.VERSION_NAME, cause);
    }

    public boolean causedBy(Class type) {
        return causedBy(this, type);
    }

    private boolean causedBy(Throwable ex, Class type) {
        Throwable cause = ex.getCause();
        if (cause == null || cause == ex) {
            return false;
        }
        if (type.isAssignableFrom(cause.getClass())) {
            return true;
        }
        return causedBy(cause, type);
    }

    public String getMessage() {
        if (this.trace == null) {
            return super.getMessage();
        }
        StringBuffer buffer = new StringBuffer(GL20.GL_NEVER);
        buffer.append(super.getMessage());
        if (buffer.length() > 0) {
            buffer.append('\n');
        }
        buffer.append("Serialization trace:");
        buffer.append(this.trace);
        return buffer.toString();
    }

    public void addTrace(String info) {
        if (info == null) {
            throw new IllegalArgumentException("info cannot be null.");
        }
        if (this.trace == null) {
            this.trace = new StringBuffer(GL20.GL_NEVER);
        }
        this.trace.append('\n');
        this.trace.append(info);
    }
}
