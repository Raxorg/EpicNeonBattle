package com.badlogic.gdx.graphics;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class PixmapIO {

    private static class CIM {
        private static final int BUFFER_SIZE = 32000;
        private static final byte[] readBuffer = new byte[BUFFER_SIZE];
        private static final byte[] writeBuffer = new byte[BUFFER_SIZE];

        private CIM() {
        }

        public static void write(FileHandle file, Pixmap pixmap) {
            Exception e;
            Throwable th;
            DataOutputStream dataOutputStream = null;
            try {
                DataOutputStream out = new DataOutputStream(new DeflaterOutputStream(file.write(false)));
                try {
                    out.writeInt(pixmap.getWidth());
                    out.writeInt(pixmap.getHeight());
                    out.writeInt(Format.toGdx2DPixmapFormat(pixmap.getFormat()));
                    ByteBuffer pixelBuf = pixmap.getPixels();
                    pixelBuf.position(0);
                    pixelBuf.limit(pixelBuf.capacity());
                    int remainingBytes = pixelBuf.capacity() % BUFFER_SIZE;
                    int iterations = pixelBuf.capacity() / BUFFER_SIZE;
                    synchronized (writeBuffer) {
                        for (int i = 0; i < iterations; i++) {
                            pixelBuf.get(writeBuffer);
                            out.write(writeBuffer);
                        }
                        pixelBuf.get(writeBuffer, 0, remainingBytes);
                        out.write(writeBuffer, 0, remainingBytes);
                    }
                    pixelBuf.position(0);
                    pixelBuf.limit(pixelBuf.capacity());
                    StreamUtils.closeQuietly(out);
                } catch (Exception e2) {
                    e = e2;
                    dataOutputStream = out;
                } catch (Throwable th2) {
                    th = th2;
                    dataOutputStream = out;
                    StreamUtils.closeQuietly(dataOutputStream);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                try {
                    throw new GdxRuntimeException("Couldn't write Pixmap to file '" + file + "'", e);
                } catch (Throwable th3) {
                    th = th3;
                    StreamUtils.closeQuietly(dataOutputStream);
                    throw th;
                }
            }
        }

        public static Pixmap read(FileHandle file) {
            Exception e;
            Throwable th;
            DataInputStream dataInputStream = null;
            try {
                DataInputStream in = new DataInputStream(new InflaterInputStream(new BufferedInputStream(file.read())));
                try {
                    Pixmap pixmap = new Pixmap(in.readInt(), in.readInt(), Format.fromGdx2DPixmapFormat(in.readInt()));
                    ByteBuffer pixelBuf = pixmap.getPixels();
                    pixelBuf.position(0);
                    pixelBuf.limit(pixelBuf.capacity());
                    synchronized (readBuffer) {
                        while (true) {
                            int readBytes = in.read(readBuffer);
                            if (readBytes > 0) {
                                pixelBuf.put(readBuffer, 0, readBytes);
                            }
                        }
                    }
                    pixelBuf.position(0);
                    pixelBuf.limit(pixelBuf.capacity());
                    StreamUtils.closeQuietly(in);
                    return pixmap;
                } catch (Exception e2) {
                    e = e2;
                    dataInputStream = in;
                } catch (Throwable th2) {
                    th = th2;
                    dataInputStream = in;
                    StreamUtils.closeQuietly(dataInputStream);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                try {
                    throw new GdxRuntimeException("Couldn't read Pixmap from file '" + file + "'", e);
                } catch (Throwable th3) {
                    th = th3;
                    StreamUtils.closeQuietly(dataInputStream);
                    throw th;
                }
            }
        }
    }

    public static class PNG implements Disposable {
        private static final byte COLOR_ARGB = (byte) 6;
        private static final byte COMPRESSION_DEFLATE = (byte) 0;
        private static final byte FILTER_NONE = (byte) 0;
        private static final int IDAT = 1229209940;
        private static final int IEND = 1229278788;
        private static final int IHDR = 1229472850;
        private static final byte INTERLACE_NONE = (byte) 0;
        private static final byte PAETH = (byte) 4;
        private static final byte[] SIGNATURE = new byte[]{(byte) -119, (byte) 80, (byte) 78, (byte) 71, (byte) 13, (byte) 10, (byte) 26, (byte) 10};
        private final ChunkBuffer buffer;
        private ByteArray curLineBytes;
        private final Deflater deflater;
        private boolean flipY;
        private int lastLineLen;
        private ByteArray lineOutBytes;
        private ByteArray prevLineBytes;

        static class ChunkBuffer extends DataOutputStream {
            final ByteArrayOutputStream buffer;
            final CRC32 crc;

            ChunkBuffer(int initialSize) {
                this(new ByteArrayOutputStream(initialSize), new CRC32());
            }

            private ChunkBuffer(ByteArrayOutputStream buffer, CRC32 crc) {
                super(new CheckedOutputStream(buffer, crc));
                this.buffer = buffer;
                this.crc = crc;
            }

            public void endChunk(DataOutputStream target) throws IOException {
                flush();
                target.writeInt(this.buffer.size() - 4);
                this.buffer.writeTo(target);
                target.writeInt((int) this.crc.getValue());
                this.buffer.reset();
                this.crc.reset();
            }
        }

        public PNG() {
            this(GL20.GL_COLOR_BUFFER_BIT);
        }

        public PNG(int initialBufferSize) {
            this.flipY = true;
            this.buffer = new ChunkBuffer(initialBufferSize);
            this.deflater = new Deflater();
        }

        public void setFlipY(boolean flipY) {
            this.flipY = flipY;
        }

        public void setCompression(int level) {
            this.deflater.setLevel(level);
        }

        public void write(FileHandle file, Pixmap pixmap) throws IOException {
            OutputStream output = file.write(false);
            try {
                write(output, pixmap);
            } finally {
                StreamUtils.closeQuietly(output);
            }
        }

        public void write(OutputStream output, Pixmap pixmap) throws IOException {
            byte[] lineOut;
            byte[] curLine;
            byte[] prevLine;
            DeflaterOutputStream deflaterOutput = new DeflaterOutputStream(this.buffer, this.deflater);
            DataOutputStream dataOutput = new DataOutputStream(output);
            dataOutput.write(SIGNATURE);
            this.buffer.writeInt(IHDR);
            this.buffer.writeInt(pixmap.getWidth());
            this.buffer.writeInt(pixmap.getHeight());
            this.buffer.writeByte(8);
            this.buffer.writeByte(6);
            this.buffer.writeByte(0);
            this.buffer.writeByte(0);
            this.buffer.writeByte(0);
            this.buffer.endChunk(dataOutput);
            this.buffer.writeInt(IDAT);
            this.deflater.reset();
            int lineLen = pixmap.getWidth() * 4;
            if (this.lineOutBytes == null) {
                ByteArray byteArray = new ByteArray(lineLen);
                this.lineOutBytes = byteArray;
                lineOut = byteArray.items;
                byteArray = new ByteArray(lineLen);
                this.curLineBytes = byteArray;
                curLine = byteArray.items;
                byteArray = new ByteArray(lineLen);
                this.prevLineBytes = byteArray;
                prevLine = byteArray.items;
            } else {
                lineOut = this.lineOutBytes.ensureCapacity(lineLen);
                curLine = this.curLineBytes.ensureCapacity(lineLen);
                prevLine = this.prevLineBytes.ensureCapacity(lineLen);
                int n = this.lastLineLen;
                for (int i = 0; i < n; i++) {
                    prevLine[i] = INTERLACE_NONE;
                }
            }
            this.lastLineLen = lineLen;
            ByteBuffer pixels = pixmap.getPixels();
            int oldPosition = pixels.position();
            boolean rgba8888 = pixmap.getFormat() == Format.RGBA8888;
            int h = pixmap.getHeight();
            for (int y = 0; y < h; y++) {
                int py;
                int i2;
                if (this.flipY) {
                    py = (h - y) - 1;
                } else {
                    py = y;
                }
                if (rgba8888) {
                    pixels.position(py * lineLen);
                    pixels.get(curLine, 0, lineLen);
                } else {
                    i2 = 0;
                    for (int px = 0; px < pixmap.getWidth(); px++) {
                        int pixel = pixmap.getPixel(px, py);
                        int i3 = i2 + 1;
                        curLine[i2] = (byte) ((pixel >> 24) & Keys.F12);
                        i2 = i3 + 1;
                        curLine[i3] = (byte) ((pixel >> 16) & Keys.F12);
                        i3 = i2 + 1;
                        curLine[i2] = (byte) ((pixel >> 8) & Keys.F12);
                        i2 = i3 + 1;
                        curLine[i3] = (byte) (pixel & Keys.F12);
                    }
                }
                lineOut[0] = (byte) (curLine[0] - prevLine[0]);
                lineOut[1] = (byte) (curLine[1] - prevLine[1]);
                lineOut[2] = (byte) (curLine[2] - prevLine[2]);
                lineOut[3] = (byte) (curLine[3] - prevLine[3]);
                for (i2 = 4; i2 < lineLen; i2++) {
                    int a = curLine[i2 - 4] & Keys.F12;
                    int b = prevLine[i2] & Keys.F12;
                    int c = prevLine[i2 - 4] & Keys.F12;
                    int p = (a + b) - c;
                    int pa = p - a;
                    if (pa < 0) {
                        pa = -pa;
                    }
                    int pb = p - b;
                    if (pb < 0) {
                        pb = -pb;
                    }
                    int pc = p - c;
                    if (pc < 0) {
                        pc = -pc;
                    }
                    if (pa <= pb && pa <= pc) {
                        c = a;
                    } else if (pb <= pc) {
                        c = b;
                    }
                    lineOut[i2] = (byte) (curLine[i2] - c);
                }
                deflaterOutput.write(4);
                deflaterOutput.write(lineOut, 0, lineLen);
                byte[] temp = curLine;
                curLine = prevLine;
                prevLine = temp;
            }
            pixels.position(oldPosition);
            deflaterOutput.finish();
            this.buffer.endChunk(dataOutput);
            this.buffer.writeInt(IEND);
            this.buffer.endChunk(dataOutput);
            output.flush();
        }

        public void dispose() {
            this.deflater.end();
        }
    }

    public static void writeCIM(FileHandle file, Pixmap pixmap) {
        CIM.write(file, pixmap);
    }

    public static Pixmap readCIM(FileHandle file) {
        return CIM.read(file);
    }

    public static void writePNG(FileHandle file, Pixmap pixmap) {
        PNG writer;
        try {
            writer = new PNG((int) (((float) (pixmap.getWidth() * pixmap.getHeight())) * 1.5f));
            writer.setFlipY(false);
            writer.write(file, pixmap);
            writer.dispose();
        } catch (IOException ex) {
            throw new GdxRuntimeException("Error writing PNG: " + file, ex);
        } catch (Throwable th) {
            writer.dispose();
        }
    }
}
