package com.badlogic.gdx.utils;

import com.badlogic.gdx.files.FileHandle;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

public class I18NBundle {
    private static final String DEFAULT_ENCODING = "UTF-8";
    private static final Locale ROOT_LOCALE = new Locale(BuildConfig.VERSION_NAME, BuildConfig.VERSION_NAME, BuildConfig.VERSION_NAME);
    private static boolean exceptionOnMissingKey = true;
    private static boolean simpleFormatter = false;
    private TextFormatter formatter;
    private Locale locale;
    private I18NBundle parent;
    private ObjectMap<String, String> properties;

    public static boolean getSimpleFormatter() {
        return simpleFormatter;
    }

    public static void setSimpleFormatter(boolean enabled) {
        simpleFormatter = enabled;
    }

    public static boolean getExceptionOnMissingKey() {
        return exceptionOnMissingKey;
    }

    public static void setExceptionOnMissingKey(boolean enabled) {
        exceptionOnMissingKey = enabled;
    }

    public static I18NBundle createBundle(FileHandle baseFileHandle) {
        return createBundleImpl(baseFileHandle, Locale.getDefault(), DEFAULT_ENCODING);
    }

    public static I18NBundle createBundle(FileHandle baseFileHandle, Locale locale) {
        return createBundleImpl(baseFileHandle, locale, DEFAULT_ENCODING);
    }

    public static I18NBundle createBundle(FileHandle baseFileHandle, String encoding) {
        return createBundleImpl(baseFileHandle, Locale.getDefault(), encoding);
    }

    public static I18NBundle createBundle(FileHandle baseFileHandle, Locale locale, String encoding) {
        return createBundleImpl(baseFileHandle, locale, encoding);
    }

    private static I18NBundle createBundleImpl(FileHandle baseFileHandle, Locale locale, String encoding) {
        if (baseFileHandle == null || locale == null || encoding == null) {
            throw new NullPointerException();
        }
        I18NBundle baseBundle = null;
        Locale targetLocale = locale;
        do {
            List<Locale> candidateLocales = getCandidateLocales(targetLocale);
            I18NBundle bundle = loadBundleChain(baseFileHandle, encoding, candidateLocales, 0, baseBundle);
            if (bundle != null) {
                Locale bundleLocale = bundle.getLocale();
                boolean isBaseBundle = bundleLocale.equals(ROOT_LOCALE);
                if (!isBaseBundle || bundleLocale.equals(locale) || (candidateLocales.size() == 1 && bundleLocale.equals(candidateLocales.get(0)))) {
                    break;
                } else if (isBaseBundle && baseBundle == null) {
                    baseBundle = bundle;
                }
            }
            targetLocale = getFallbackLocale(targetLocale);
        } while (targetLocale != null);
        if (bundle != null) {
            return bundle;
        }
        if (baseBundle != null) {
            return baseBundle;
        }
        throw new MissingResourceException("Can't find bundle for base file handle " + baseFileHandle.path() + ", locale " + locale, baseFileHandle + "_" + locale, BuildConfig.VERSION_NAME);
    }

    private static List<Locale> getCandidateLocales(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();
        String variant = locale.getVariant();
        List<Locale> locales = new ArrayList(4);
        if (variant.length() > 0) {
            locales.add(locale);
        }
        if (country.length() > 0) {
            locales.add(locales.size() == 0 ? locale : new Locale(language, country));
        }
        if (language.length() > 0) {
            if (locales.size() != 0) {
                locale = new Locale(language);
            }
            locales.add(locale);
        }
        locales.add(ROOT_LOCALE);
        return locales;
    }

    private static Locale getFallbackLocale(Locale locale) {
        Locale defaultLocale = Locale.getDefault();
        return locale.equals(defaultLocale) ? null : defaultLocale;
    }

    private static I18NBundle loadBundleChain(FileHandle baseFileHandle, String encoding, List<Locale> candidateLocales, int candidateIndex, I18NBundle baseBundle) {
        Locale targetLocale = (Locale) candidateLocales.get(candidateIndex);
        I18NBundle parent = null;
        if (candidateIndex != candidateLocales.size() - 1) {
            parent = loadBundleChain(baseFileHandle, encoding, candidateLocales, candidateIndex + 1, baseBundle);
        } else if (baseBundle != null && targetLocale.equals(ROOT_LOCALE)) {
            return baseBundle;
        }
        I18NBundle bundle = loadBundle(baseFileHandle, encoding, targetLocale);
        if (bundle == null) {
            return parent;
        }
        bundle.parent = parent;
        return bundle;
    }

    private static I18NBundle loadBundle(FileHandle baseFileHandle, String encoding, Locale targetLocale) {
        Throwable e;
        Throwable th;
        I18NBundle bundle = null;
        Reader reader = null;
        try {
            FileHandle fileHandle = toFileHandle(baseFileHandle, targetLocale);
            if (checkFileExistence(fileHandle)) {
                I18NBundle bundle2 = new I18NBundle();
                try {
                    reader = fileHandle.reader(encoding);
                    bundle2.load(reader);
                    bundle = bundle2;
                } catch (IOException e2) {
                    e = e2;
                    bundle = bundle2;
                    try {
                        throw new GdxRuntimeException(e);
                    } catch (Throwable th2) {
                        th = th2;
                        StreamUtils.closeQuietly(reader);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    bundle = bundle2;
                    StreamUtils.closeQuietly(reader);
                    throw th;
                }
            }
            StreamUtils.closeQuietly(reader);
            if (bundle != null) {
                bundle.setLocale(targetLocale);
            }
            return bundle;
        } catch (IOException e3) {
            e = e3;
            throw new GdxRuntimeException(e);
        }
    }

    private static boolean checkFileExistence(FileHandle fh) {
        try {
            fh.read().close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected void load(Reader reader) throws IOException {
        this.properties = new ObjectMap();
        PropertiesUtils.load(this.properties, reader);
    }

    private static FileHandle toFileHandle(FileHandle baseFileHandle, Locale locale) {
        StringBuilder sb = new StringBuilder(baseFileHandle.name());
        if (!locale.equals(ROOT_LOCALE)) {
            String language = locale.getLanguage();
            String country = locale.getCountry();
            String variant = locale.getVariant();
            boolean emptyLanguage = BuildConfig.VERSION_NAME.equals(language);
            boolean emptyCountry = BuildConfig.VERSION_NAME.equals(country);
            boolean emptyVariant = BuildConfig.VERSION_NAME.equals(variant);
            if (!(emptyLanguage && emptyCountry && emptyVariant)) {
                sb.append('_');
                if (!emptyVariant) {
                    sb.append(language).append('_').append(country).append('_').append(variant);
                } else if (emptyCountry) {
                    sb.append(language);
                } else {
                    sb.append(language).append('_').append(country);
                }
            }
        }
        return baseFileHandle.sibling(sb.append(".properties").toString());
    }

    public Locale getLocale() {
        return this.locale;
    }

    private void setLocale(Locale locale) {
        this.locale = locale;
        this.formatter = new TextFormatter(locale, !simpleFormatter);
    }

    public final String get(String key) {
        String result = (String) this.properties.get(key);
        if (result == null) {
            if (this.parent != null) {
                result = this.parent.get(key);
            }
            if (result == null) {
                if (!exceptionOnMissingKey) {
                    return "???" + key + "???";
                }
                throw new MissingResourceException("Can't find bundle key " + key, getClass().getName(), key);
            }
        }
        return result;
    }

    public String format(String key, Object... args) {
        return this.formatter.format(get(key), args);
    }
}
