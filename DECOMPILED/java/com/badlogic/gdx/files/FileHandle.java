package com.badlogic.gdx.files;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

public class FileHandle {
    protected File file;
    protected FileType type;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$Files$FileType = new int[FileType.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$Files$FileType[FileType.Internal.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$Files$FileType[FileType.Classpath.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    protected FileHandle() {
    }

    public FileHandle(String fileName) {
        this.file = new File(fileName);
        this.type = FileType.Absolute;
    }

    public FileHandle(File file) {
        this.file = file;
        this.type = FileType.Absolute;
    }

    protected FileHandle(String fileName, FileType type) {
        this.type = type;
        this.file = new File(fileName);
    }

    protected FileHandle(File file, FileType type) {
        this.file = file;
        this.type = type;
    }

    public String path() {
        return this.file.getPath().replace('\\', '/');
    }

    public String name() {
        return this.file.getName();
    }

    public String extension() {
        String name = this.file.getName();
        int dotIndex = name.lastIndexOf(46);
        if (dotIndex == -1) {
            return BuildConfig.VERSION_NAME;
        }
        return name.substring(dotIndex + 1);
    }

    public String nameWithoutExtension() {
        String name = this.file.getName();
        int dotIndex = name.lastIndexOf(46);
        return dotIndex == -1 ? name : name.substring(0, dotIndex);
    }

    public String pathWithoutExtension() {
        String path = this.file.getPath().replace('\\', '/');
        int dotIndex = path.lastIndexOf(46);
        return dotIndex == -1 ? path : path.substring(0, dotIndex);
    }

    public FileType type() {
        return this.type;
    }

    public File file() {
        if (this.type == FileType.External) {
            return new File(Gdx.files.getExternalStoragePath(), this.file.getPath());
        }
        return this.file;
    }

    public InputStream read() {
        if (this.type == FileType.Classpath || ((this.type == FileType.Internal && !file().exists()) || (this.type == FileType.Local && !file().exists()))) {
            InputStream resourceAsStream = FileHandle.class.getResourceAsStream("/" + this.file.getPath().replace('\\', '/'));
            if (resourceAsStream != null) {
                return resourceAsStream;
            }
            throw new GdxRuntimeException("File not found: " + this.file + " (" + this.type + ")");
        }
        try {
            return new FileInputStream(file());
        } catch (Exception ex) {
            if (file().isDirectory()) {
                throw new GdxRuntimeException("Cannot open a stream to a directory: " + this.file + " (" + this.type + ")", ex);
            }
            throw new GdxRuntimeException("Error reading file: " + this.file + " (" + this.type + ")", ex);
        }
    }

    public BufferedInputStream read(int bufferSize) {
        return new BufferedInputStream(read(), bufferSize);
    }

    public Reader reader() {
        return new InputStreamReader(read());
    }

    public Reader reader(String charset) {
        InputStream stream = read();
        try {
            return new InputStreamReader(stream, charset);
        } catch (UnsupportedEncodingException ex) {
            StreamUtils.closeQuietly(stream);
            throw new GdxRuntimeException("Error reading file: " + this, ex);
        }
    }

    public BufferedReader reader(int bufferSize) {
        return new BufferedReader(new InputStreamReader(read()), bufferSize);
    }

    public BufferedReader reader(int bufferSize, String charset) {
        try {
            return new BufferedReader(new InputStreamReader(read(), charset), bufferSize);
        } catch (UnsupportedEncodingException ex) {
            throw new GdxRuntimeException("Error reading file: " + this, ex);
        }
    }

    public String readString() {
        return readString(null);
    }

    public String readString(String charset) {
        InputStreamReader reader;
        StringBuilder output = new StringBuilder(estimateLength());
        if (charset == null) {
            try {
                reader = new InputStreamReader(read());
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error reading layout file: " + this, ex);
            } catch (Throwable th) {
                StreamUtils.closeQuietly(null);
            }
        } else {
            reader = new InputStreamReader(read(), charset);
        }
        char[] buffer = new char[Usage.BiNormal];
        while (true) {
            int length = reader.read(buffer);
            if (length == -1) {
                StreamUtils.closeQuietly(reader);
                return output.toString();
            }
            output.append(buffer, 0, length);
        }
    }

    public byte[] readBytes() {
        InputStream input = read();
        try {
            byte[] copyStreamToByteArray = StreamUtils.copyStreamToByteArray(input, estimateLength());
            StreamUtils.closeQuietly(input);
            return copyStreamToByteArray;
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error reading file: " + this, ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(input);
        }
    }

    private int estimateLength() {
        int length = (int) length();
        return length != 0 ? length : GL20.GL_NEVER;
    }

    public int readBytes(byte[] bytes, int offset, int size) {
        InputStream input = read();
        int position = 0;
        while (true) {
            try {
                int count = input.read(bytes, offset + position, size - position);
                if (count <= 0) {
                    StreamUtils.closeQuietly(input);
                    return position - offset;
                }
                position += count;
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error reading file: " + this, ex);
            } catch (Throwable th) {
                StreamUtils.closeQuietly(input);
            }
        }
    }

    public OutputStream write(boolean append) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot write to a classpath file: " + this.file);
        } else if (this.type == FileType.Internal) {
            throw new GdxRuntimeException("Cannot write to an internal file: " + this.file);
        } else {
            parent().mkdirs();
            try {
                return new FileOutputStream(file(), append);
            } catch (Exception ex) {
                if (file().isDirectory()) {
                    throw new GdxRuntimeException("Cannot open a stream to a directory: " + this.file + " (" + this.type + ")", ex);
                }
                throw new GdxRuntimeException("Error writing file: " + this.file + " (" + this.type + ")", ex);
            }
        }
    }

    public OutputStream write(boolean append, int bufferSize) {
        return new BufferedOutputStream(write(append), bufferSize);
    }

    public void write(InputStream input, boolean append) {
        OutputStream output = null;
        try {
            output = write(append);
            StreamUtils.copyStream(input, output);
            StreamUtils.closeQuietly(input);
            StreamUtils.closeQuietly(output);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error stream writing to file: " + this.file + " (" + this.type + ")", ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(input);
            StreamUtils.closeQuietly(output);
        }
    }

    public Writer writer(boolean append) {
        return writer(append, null);
    }

    public Writer writer(boolean append, String charset) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot write to a classpath file: " + this.file);
        } else if (this.type == FileType.Internal) {
            throw new GdxRuntimeException("Cannot write to an internal file: " + this.file);
        } else {
            parent().mkdirs();
            try {
                FileOutputStream output = new FileOutputStream(file(), append);
                if (charset == null) {
                    return new OutputStreamWriter(output);
                }
                return new OutputStreamWriter(output, charset);
            } catch (IOException ex) {
                if (file().isDirectory()) {
                    throw new GdxRuntimeException("Cannot open a stream to a directory: " + this.file + " (" + this.type + ")", ex);
                }
                throw new GdxRuntimeException("Error writing file: " + this.file + " (" + this.type + ")", ex);
            }
        }
    }

    public void writeString(String string, boolean append) {
        writeString(string, append, null);
    }

    public void writeString(String string, boolean append, String charset) {
        Writer writer = null;
        try {
            writer = writer(append, charset);
            writer.write(string);
            StreamUtils.closeQuietly(writer);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error writing file: " + this.file + " (" + this.type + ")", ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(writer);
        }
    }

    public void writeBytes(byte[] bytes, boolean append) {
        OutputStream output = write(append);
        try {
            output.write(bytes);
            StreamUtils.closeQuietly(output);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing file: " + this.file + " (" + this.type + ")", ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(output);
        }
    }

    public void writeBytes(byte[] bytes, int offset, int length, boolean append) {
        OutputStream output = write(append);
        try {
            output.write(bytes, offset, length);
            StreamUtils.closeQuietly(output);
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing file: " + this.file + " (" + this.type + ")", ex);
        } catch (Throwable th) {
            StreamUtils.closeQuietly(output);
        }
    }

    public FileHandle[] list() {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot list a classpath directory: " + this.file);
        }
        String[] relativePaths = file().list();
        if (relativePaths == null) {
            return new FileHandle[0];
        }
        FileHandle[] handles = new FileHandle[relativePaths.length];
        int n = relativePaths.length;
        for (int i = 0; i < n; i++) {
            handles[i] = child(relativePaths[i]);
        }
        return handles;
    }

    public FileHandle[] list(FileFilter filter) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot list a classpath directory: " + this.file);
        }
        String[] relativePaths = file().list();
        if (relativePaths == null) {
            return new FileHandle[0];
        }
        FileHandle[] handles = new FileHandle[relativePaths.length];
        int count = 0;
        for (String path : relativePaths) {
            FileHandle child = child(path);
            if (filter.accept(child.file())) {
                handles[count] = child;
                count++;
            }
        }
        if (count >= relativePaths.length) {
            return handles;
        }
        FileHandle[] newHandles = new FileHandle[count];
        System.arraycopy(handles, 0, newHandles, 0, count);
        return newHandles;
    }

    public FileHandle[] list(FilenameFilter filter) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot list a classpath directory: " + this.file);
        }
        File file = file();
        String[] relativePaths = file.list();
        if (relativePaths == null) {
            return new FileHandle[0];
        }
        FileHandle[] handles = new FileHandle[relativePaths.length];
        int count = 0;
        for (String path : relativePaths) {
            if (filter.accept(file, path)) {
                handles[count] = child(path);
                count++;
            }
        }
        if (count >= relativePaths.length) {
            return handles;
        }
        FileHandle[] newHandles = new FileHandle[count];
        System.arraycopy(handles, 0, newHandles, 0, count);
        return newHandles;
    }

    public FileHandle[] list(String suffix) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot list a classpath directory: " + this.file);
        }
        String[] relativePaths = file().list();
        if (relativePaths == null) {
            return new FileHandle[0];
        }
        FileHandle[] handles = new FileHandle[relativePaths.length];
        int count = 0;
        for (String path : relativePaths) {
            if (path.endsWith(suffix)) {
                handles[count] = child(path);
                count++;
            }
        }
        if (count >= relativePaths.length) {
            return handles;
        }
        FileHandle[] newHandles = new FileHandle[count];
        System.arraycopy(handles, 0, newHandles, 0, count);
        return newHandles;
    }

    public boolean isDirectory() {
        if (this.type == FileType.Classpath) {
            return false;
        }
        return file().isDirectory();
    }

    public FileHandle child(String name) {
        if (this.file.getPath().length() == 0) {
            return new FileHandle(new File(name), this.type);
        }
        return new FileHandle(new File(this.file, name), this.type);
    }

    public FileHandle sibling(String name) {
        if (this.file.getPath().length() != 0) {
            return new FileHandle(new File(this.file.getParent(), name), this.type);
        }
        throw new GdxRuntimeException("Cannot get the sibling of the root.");
    }

    public FileHandle parent() {
        File parent = this.file.getParentFile();
        if (parent == null) {
            if (this.type == FileType.Absolute) {
                parent = new File("/");
            } else {
                parent = new File(BuildConfig.VERSION_NAME);
            }
        }
        return new FileHandle(parent, this.type);
    }

    public void mkdirs() {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot mkdirs with a classpath file: " + this.file);
        } else if (this.type == FileType.Internal) {
            throw new GdxRuntimeException("Cannot mkdirs with an internal file: " + this.file);
        } else {
            file().mkdirs();
        }
    }

    public boolean exists() {
        switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$Files$FileType[this.type.ordinal()]) {
            case BuildConfig.VERSION_CODE /*1*/:
                if (file().exists()) {
                    return true;
                }
                break;
            case Base.kNumLenToPosStatesBits /*2*/:
                break;
            default:
                return file().exists();
        }
        return FileHandle.class.getResource(new StringBuilder().append("/").append(this.file.getPath().replace('\\', '/')).toString()) != null;
    }

    public boolean delete() {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot delete a classpath file: " + this.file);
        } else if (this.type != FileType.Internal) {
            return file().delete();
        } else {
            throw new GdxRuntimeException("Cannot delete an internal file: " + this.file);
        }
    }

    public boolean deleteDirectory() {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot delete a classpath file: " + this.file);
        } else if (this.type != FileType.Internal) {
            return deleteDirectory(file());
        } else {
            throw new GdxRuntimeException("Cannot delete an internal file: " + this.file);
        }
    }

    public void emptyDirectory() {
        emptyDirectory(false);
    }

    public void emptyDirectory(boolean preserveTree) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot delete a classpath file: " + this.file);
        } else if (this.type == FileType.Internal) {
            throw new GdxRuntimeException("Cannot delete an internal file: " + this.file);
        } else {
            emptyDirectory(file(), preserveTree);
        }
    }

    public void copyTo(FileHandle dest) {
        boolean sourceDir = isDirectory();
        if (sourceDir) {
            if (!dest.exists()) {
                dest.mkdirs();
                if (!dest.isDirectory()) {
                    throw new GdxRuntimeException("Destination directory cannot be created: " + dest);
                }
            } else if (!dest.isDirectory()) {
                throw new GdxRuntimeException("Destination exists but is not a directory: " + dest);
            }
            if (!sourceDir) {
                dest = dest.child(name());
            }
            copyDirectory(this, dest);
            return;
        }
        if (dest.isDirectory()) {
            dest = dest.child(name());
        }
        copyFile(this, dest);
    }

    public void moveTo(FileHandle dest) {
        if (this.type == FileType.Classpath) {
            throw new GdxRuntimeException("Cannot move a classpath file: " + this.file);
        } else if (this.type == FileType.Internal) {
            throw new GdxRuntimeException("Cannot move an internal file: " + this.file);
        } else {
            copyTo(dest);
            delete();
            if (exists() && isDirectory()) {
                deleteDirectory();
            }
        }
    }

    public long length() {
        FileType fileType = this.type;
        long j = FileType.Classpath;
        if (fileType != j) {
            fileType = this.type;
            j = FileType.Internal;
            if (fileType != j || this.file.exists()) {
                return file().length();
            }
        }
        InputStream input = read();
        try {
            j = (long) input.available();
            return j;
        } catch (Exception e) {
            return 0;
        } finally {
            StreamUtils.closeQuietly(input);
        }
    }

    public long lastModified() {
        return file().lastModified();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof FileHandle)) {
            return false;
        }
        FileHandle other = (FileHandle) obj;
        if (this.type == other.type && path().equals(other.path())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((this.type.hashCode() + 37) * 67) + path().hashCode();
    }

    public String toString() {
        return this.file.getPath().replace('\\', '/');
    }

    public static FileHandle tempFile(String prefix) {
        try {
            return new FileHandle(File.createTempFile(prefix, null));
        } catch (IOException ex) {
            throw new GdxRuntimeException("Unable to create temp file.", ex);
        }
    }

    public static FileHandle tempDirectory(String prefix) {
        try {
            File file = File.createTempFile(prefix, null);
            if (!file.delete()) {
                throw new IOException("Unable to delete temp file: " + file);
            } else if (file.mkdir()) {
                return new FileHandle(file);
            } else {
                throw new IOException("Unable to create temp directory: " + file);
            }
        } catch (IOException ex) {
            throw new GdxRuntimeException("Unable to create temp file.", ex);
        }
    }

    private static void emptyDirectory(File file, boolean preserveTree) {
        if (file.exists()) {
            File[] files = file.listFiles();
            if (files != null) {
                int n = files.length;
                for (int i = 0; i < n; i++) {
                    if (!files[i].isDirectory()) {
                        files[i].delete();
                    } else if (preserveTree) {
                        emptyDirectory(files[i], true);
                    } else {
                        deleteDirectory(files[i]);
                    }
                }
            }
        }
    }

    private static boolean deleteDirectory(File file) {
        emptyDirectory(file, false);
        return file.delete();
    }

    private static void copyFile(FileHandle source, FileHandle dest) {
        try {
            dest.write(source.read(), false);
        } catch (Exception ex) {
            throw new GdxRuntimeException("Error copying source file: " + source.file + " (" + source.type + ")\n" + "To destination: " + dest.file + " (" + dest.type + ")", ex);
        }
    }

    private static void copyDirectory(FileHandle sourceDir, FileHandle destDir) {
        destDir.mkdirs();
        for (FileHandle srcFile : sourceDir.list()) {
            FileHandle destFile = destDir.child(srcFile.name());
            if (srcFile.isDirectory()) {
                copyDirectory(srcFile, destFile);
            } else {
                copyFile(srcFile, destFile);
            }
        }
    }
}
