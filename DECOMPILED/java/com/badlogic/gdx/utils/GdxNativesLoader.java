package com.badlogic.gdx.utils;

public class GdxNativesLoader {
    public static boolean disableNativesLoading = false;
    private static boolean nativesLoaded;

    public static synchronized void load() {
        synchronized (GdxNativesLoader.class) {
            if (!nativesLoaded) {
                nativesLoaded = true;
                if (disableNativesLoading) {
                    System.out.println("Native loading is disabled.");
                } else {
                    new SharedLibraryLoader().load("gdx");
                }
            }
        }
    }
}
