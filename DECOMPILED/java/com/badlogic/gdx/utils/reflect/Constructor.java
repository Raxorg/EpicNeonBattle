package com.badlogic.gdx.utils.reflect;

import java.lang.reflect.InvocationTargetException;

public final class Constructor {
    private final java.lang.reflect.Constructor constructor;

    Constructor(java.lang.reflect.Constructor constructor) {
        this.constructor = constructor;
    }

    public Class[] getParameterTypes() {
        return this.constructor.getParameterTypes();
    }

    public Class getDeclaringClass() {
        return this.constructor.getDeclaringClass();
    }

    public boolean isAccessible() {
        return this.constructor.isAccessible();
    }

    public void setAccessible(boolean accessible) {
        this.constructor.setAccessible(accessible);
    }

    public Object newInstance(Object... args) throws ReflectionException {
        try {
            return this.constructor.newInstance(args);
        } catch (IllegalArgumentException e) {
            throw new ReflectionException("Illegal argument(s) supplied to constructor for class: " + getDeclaringClass().getName(), e);
        } catch (InstantiationException e2) {
            throw new ReflectionException("Could not instantiate instance of class: " + getDeclaringClass().getName(), e2);
        } catch (IllegalAccessException e3) {
            throw new ReflectionException("Could not instantiate instance of class: " + getDeclaringClass().getName(), e3);
        } catch (InvocationTargetException e4) {
            throw new ReflectionException("Exception occurred in constructor for class: " + getDeclaringClass().getName(), e4);
        }
    }
}
