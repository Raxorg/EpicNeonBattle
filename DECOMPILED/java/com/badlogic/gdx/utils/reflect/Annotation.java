package com.badlogic.gdx.utils.reflect;

public final class Annotation {
    private java.lang.annotation.Annotation annotation;

    Annotation(java.lang.annotation.Annotation annotation) {
        this.annotation = annotation;
    }

    public <T extends java.lang.annotation.Annotation> T getAnnotation(Class<T> annotationType) {
        if (this.annotation.annotationType().equals(annotationType)) {
            return this.annotation;
        }
        return null;
    }

    public Class<? extends java.lang.annotation.Annotation> getAnnotationType() {
        return this.annotation.annotationType();
    }
}
