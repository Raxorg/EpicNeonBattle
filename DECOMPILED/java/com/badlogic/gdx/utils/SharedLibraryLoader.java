package com.badlogic.gdx.utils;

import com.epicness.neonbattle.android.BuildConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class SharedLibraryLoader {
    public static String abi = (System.getProperty("sun.arch.abi") != null ? System.getProperty("sun.arch.abi") : BuildConfig.VERSION_NAME);
    public static boolean is64Bit;
    public static boolean isARM = System.getProperty("os.arch").startsWith("arm");
    public static boolean isAndroid;
    public static boolean isIos;
    public static boolean isLinux;
    public static boolean isMac;
    public static boolean isWindows;
    private static final HashSet<String> loadedLibraries = new HashSet();
    private String nativesJar;

    static {
        boolean z;
        isWindows = System.getProperty("os.name").contains("Windows");
        isLinux = System.getProperty("os.name").contains("Linux");
        isMac = System.getProperty("os.name").contains("Mac");
        isIos = false;
        isAndroid = false;
        if (System.getProperty("os.arch").equals("amd64") || System.getProperty("os.arch").equals("x86_64")) {
            z = true;
        } else {
            z = false;
        }
        is64Bit = z;
        String vm = System.getProperty("java.runtime.name");
        if (vm != null && vm.contains("Android Runtime")) {
            isAndroid = true;
            isWindows = false;
            isLinux = false;
            isMac = false;
            is64Bit = false;
        }
        if (!(isAndroid || isWindows || isLinux || isMac)) {
            isIos = true;
            is64Bit = false;
        }
    }

    public SharedLibraryLoader(String nativesJar) {
        this.nativesJar = nativesJar;
    }

    public String crc(InputStream input) {
        if (input == null) {
            throw new IllegalArgumentException("input cannot be null.");
        }
        CRC32 crc = new CRC32();
        byte[] buffer = new byte[StreamUtils.DEFAULT_BUFFER_SIZE];
        while (true) {
            try {
                int length = input.read(buffer);
                if (length == -1) {
                    break;
                }
                crc.update(buffer, 0, length);
            } catch (Exception e) {
                StreamUtils.closeQuietly(input);
            }
        }
        return Long.toString(crc.getValue(), 16);
    }

    public String mapLibraryName(String libraryName) {
        if (isWindows) {
            return libraryName + (is64Bit ? "64.dll" : ".dll");
        } else if (isLinux) {
            return "lib" + libraryName + (isARM ? "arm" + abi : BuildConfig.VERSION_NAME) + (is64Bit ? "64.so" : ".so");
        } else if (!isMac) {
            return libraryName;
        } else {
            return "lib" + libraryName + (is64Bit ? "64.dylib" : ".dylib");
        }
    }

    public synchronized void load(String libraryName) {
        if (!isIos) {
            libraryName = mapLibraryName(libraryName);
            if (!loadedLibraries.contains(libraryName)) {
                try {
                    if (isAndroid) {
                        System.loadLibrary(libraryName);
                    } else {
                        loadFile(libraryName);
                    }
                    loadedLibraries.add(libraryName);
                } catch (Throwable ex) {
                    GdxRuntimeException gdxRuntimeException = new GdxRuntimeException("Couldn't load shared library '" + libraryName + "' for target: " + System.getProperty("os.name") + (is64Bit ? ", 64-bit" : ", 32-bit"), ex);
                }
            }
        }
    }

    private InputStream readFile(String path) {
        InputStream resourceAsStream;
        if (this.nativesJar == null) {
            resourceAsStream = SharedLibraryLoader.class.getResourceAsStream("/" + path);
            if (resourceAsStream == null) {
                throw new GdxRuntimeException("Unable to read file for extraction: " + path);
            }
        }
        try {
            ZipFile file = new ZipFile(this.nativesJar);
            ZipEntry entry = file.getEntry(path);
            if (entry == null) {
                throw new GdxRuntimeException("Couldn't find '" + path + "' in JAR: " + this.nativesJar);
            }
            resourceAsStream = file.getInputStream(entry);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error reading '" + path + "' in JAR: " + this.nativesJar, ex);
        }
        return resourceAsStream;
    }

    public File extractFile(String sourcePath, String dirName) throws IOException {
        File extractFile;
        try {
            String sourceCrc = crc(readFile(sourcePath));
            if (dirName == null) {
                dirName = sourceCrc;
            }
            extractFile = extractFile(sourcePath, sourceCrc, getExtractedFile(dirName, new File(sourcePath).getName()));
        } catch (RuntimeException ex) {
            extractFile = new File(System.getProperty("java.library.path"), sourcePath);
            if (!extractFile.exists()) {
                throw ex;
            }
        }
        return extractFile;
    }

    private File getExtractedFile(String dirName, String fileName) {
        File idealFile = new File(System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/" + dirName, fileName);
        if (canWrite(idealFile)) {
            return idealFile;
        }
        File file;
        try {
            file = File.createTempFile(dirName, null);
            if (file.delete()) {
                File file2 = new File(file, fileName);
                if (canWrite(file2)) {
                    return file2;
                }
            }
        } catch (IOException e) {
        }
        file = new File(System.getProperty("user.home") + "/.libgdx/" + dirName, fileName);
        if (canWrite(file)) {
            return file;
        }
        file = new File(".temp/" + dirName, fileName);
        if (canWrite(file)) {
            return file;
        }
        return idealFile;
    }

    private boolean canWrite(File file) {
        File testFile;
        File parent = file.getParentFile();
        if (!file.exists()) {
            parent.mkdirs();
            if (!parent.isDirectory()) {
                return false;
            }
            testFile = file;
        } else if (!file.canWrite() || !canExecute(file)) {
            return false;
        } else {
            testFile = new File(parent, UUID.randomUUID().toString());
        }
        try {
            new FileOutputStream(testFile).close();
            if (!canExecute(testFile)) {
                return false;
            }
            testFile.delete();
            return true;
        } catch (Throwable th) {
            return false;
        } finally {
            testFile.delete();
        }
    }

    private boolean canExecute(File file) {
        try {
            Method canExecute = File.class.getMethod("canExecute", new Class[0]);
            if (((Boolean) canExecute.invoke(file, new Object[0])).booleanValue()) {
                return true;
            }
            File.class.getMethod("setExecutable", new Class[]{Boolean.TYPE, Boolean.TYPE}).invoke(file, new Object[]{Boolean.valueOf(true), Boolean.valueOf(false)});
            return ((Boolean) canExecute.invoke(file, new Object[0])).booleanValue();
        } catch (Exception e) {
            return false;
        }
    }

    private File extractFile(String sourcePath, String sourceCrc, File extractedFile) throws IOException {
        String extractedCrc = null;
        if (extractedFile.exists()) {
            try {
                extractedCrc = crc(new FileInputStream(extractedFile));
            } catch (FileNotFoundException e) {
            }
        }
        if (extractedCrc == null || !extractedCrc.equals(sourceCrc)) {
            try {
                InputStream input = readFile(sourcePath);
                extractedFile.getParentFile().mkdirs();
                FileOutputStream output = new FileOutputStream(extractedFile);
                byte[] buffer = new byte[StreamUtils.DEFAULT_BUFFER_SIZE];
                while (true) {
                    int length = input.read(buffer);
                    if (length == -1) {
                        break;
                    }
                    output.write(buffer, 0, length);
                }
                input.close();
                output.close();
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error extracting file: " + sourcePath + "\nTo: " + extractedFile.getAbsolutePath(), ex);
            }
        }
        return extractedFile;
    }

    private void loadFile(String sourcePath) {
        String sourceCrc = crc(readFile(sourcePath));
        String fileName = new File(sourcePath).getName();
        Throwable ex = loadFile(sourcePath, sourceCrc, new File(System.getProperty("java.io.tmpdir") + "/libgdx" + System.getProperty("user.name") + "/" + sourceCrc, fileName));
        if (ex != null) {
            File file;
            try {
                file = File.createTempFile(sourceCrc, null);
                if (file.delete() && loadFile(sourcePath, sourceCrc, file) == null) {
                    return;
                }
            } catch (Throwable th) {
            }
            if (loadFile(sourcePath, sourceCrc, new File(System.getProperty("user.home") + "/.libgdx/" + sourceCrc, fileName)) != null && loadFile(sourcePath, sourceCrc, new File(".temp/" + sourceCrc, fileName)) != null) {
                file = new File(System.getProperty("java.library.path"), sourcePath);
                if (file.exists()) {
                    System.load(file.getAbsolutePath());
                    return;
                }
                throw new GdxRuntimeException(ex);
            }
        }
    }

    private Throwable loadFile(String sourcePath, String sourceCrc, File extractedFile) {
        try {
            System.load(extractFile(sourcePath, sourceCrc, extractedFile).getAbsolutePath());
            return null;
        } catch (Throwable ex) {
            ex.printStackTrace();
            return ex;
        }
    }
}
