package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.CubemapData;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.TextureData.TextureDataType;
import com.badlogic.gdx.graphics.glutils.ETC1.ETC1Data;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.zip.GZIPInputStream;

public class KTXTextureData implements CubemapData, TextureData {
    private static final int GL_TEXTURE_1D = 4660;
    private static final int GL_TEXTURE_1D_ARRAY_EXT = 4660;
    private static final int GL_TEXTURE_2D_ARRAY_EXT = 4660;
    private static final int GL_TEXTURE_3D = 4660;
    private ByteBuffer compressedData;
    private FileHandle file;
    private int glBaseInternalFormat;
    private int glFormat;
    private int glInternalFormat;
    private int glType;
    private int glTypeSize;
    private int imagePos;
    private int numberOfArrayElements;
    private int numberOfFaces;
    private int numberOfMipmapLevels;
    private int pixelDepth = -1;
    private int pixelHeight = -1;
    private int pixelWidth = -1;
    private boolean useMipMaps;

    public KTXTextureData(FileHandle file, boolean genMipMaps) {
        this.file = file;
        this.useMipMaps = genMipMaps;
    }

    public TextureDataType getType() {
        return TextureDataType.Custom;
    }

    public boolean isPrepared() {
        return this.compressedData != null;
    }

    public void prepare() {
        Exception e;
        Throwable th;
        if (this.compressedData != null) {
            throw new GdxRuntimeException("Already prepared");
        } else if (this.file == null) {
            throw new GdxRuntimeException("Need a file to load from");
        } else {
            if (this.file.name().endsWith(".zktx")) {
                byte[] buffer = new byte[GL20.GL_TEXTURE_MAG_FILTER];
                DataInputStream dataInputStream = null;
                try {
                    DataInputStream in = new DataInputStream(new BufferedInputStream(new GZIPInputStream(this.file.read())));
                    try {
                        this.compressedData = BufferUtils.newUnsafeByteBuffer(in.readInt());
                        while (true) {
                            int readBytes = in.read(buffer);
                            if (readBytes == -1) {
                                break;
                            }
                            this.compressedData.put(buffer, 0, readBytes);
                        }
                        this.compressedData.position(0);
                        this.compressedData.limit(this.compressedData.capacity());
                        StreamUtils.closeQuietly(in);
                    } catch (Exception e2) {
                        e = e2;
                        dataInputStream = in;
                        try {
                            throw new GdxRuntimeException("Couldn't load zktx file '" + this.file + "'", e);
                        } catch (Throwable th2) {
                            th = th2;
                            StreamUtils.closeQuietly(dataInputStream);
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        dataInputStream = in;
                        StreamUtils.closeQuietly(dataInputStream);
                        throw th;
                    }
                } catch (Exception e3) {
                    e = e3;
                    throw new GdxRuntimeException("Couldn't load zktx file '" + this.file + "'", e);
                }
            }
            this.compressedData = ByteBuffer.wrap(this.file.readBytes());
            if (this.compressedData.get() != (byte) -85) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 75) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 84) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 88) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 32) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 49) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 49) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) -69) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 13) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 10) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 26) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else if (this.compressedData.get() != (byte) 10) {
                throw new GdxRuntimeException("Invalid KTX Header");
            } else {
                int endianTag = this.compressedData.getInt();
                if (endianTag == 67305985 || endianTag == 16909060) {
                    if (endianTag != 67305985) {
                        this.compressedData.order(this.compressedData.order() == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
                    }
                    this.glType = this.compressedData.getInt();
                    this.glTypeSize = this.compressedData.getInt();
                    this.glFormat = this.compressedData.getInt();
                    this.glInternalFormat = this.compressedData.getInt();
                    this.glBaseInternalFormat = this.compressedData.getInt();
                    this.pixelWidth = this.compressedData.getInt();
                    this.pixelHeight = this.compressedData.getInt();
                    this.pixelDepth = this.compressedData.getInt();
                    this.numberOfArrayElements = this.compressedData.getInt();
                    this.numberOfFaces = this.compressedData.getInt();
                    this.numberOfMipmapLevels = this.compressedData.getInt();
                    if (this.numberOfMipmapLevels == 0) {
                        this.numberOfMipmapLevels = 1;
                        this.useMipMaps = true;
                    }
                    this.imagePos = this.compressedData.position() + this.compressedData.getInt();
                    if (!this.compressedData.isDirect()) {
                        int pos = this.imagePos;
                        for (int level = 0; level < this.numberOfMipmapLevels; level++) {
                            pos += (this.numberOfFaces * ((this.compressedData.getInt(pos) + 3) & -4)) + 4;
                        }
                        this.compressedData.limit(pos);
                        this.compressedData.position(0);
                        ByteBuffer directBuffer = BufferUtils.newUnsafeByteBuffer(pos);
                        directBuffer.order(this.compressedData.order());
                        directBuffer.put(this.compressedData);
                        this.compressedData = directBuffer;
                        return;
                    }
                    return;
                }
                throw new GdxRuntimeException("Invalid KTX Header");
            }
        }
    }

    public void consumeCubemapData() {
        consumeCustomData(GL20.GL_TEXTURE_CUBE_MAP);
    }

    public void consumeCustomData(int target) {
        if (this.compressedData == null) {
            throw new GdxRuntimeException("Call prepare() before calling consumeCompressedData()");
        }
        IntBuffer buffer = BufferUtils.newIntBuffer(16);
        boolean compressed = false;
        if (this.glType == 0 || this.glFormat == 0) {
            if (this.glType + this.glFormat != 0) {
                throw new GdxRuntimeException("either both or none of glType, glFormat must be zero");
            }
            compressed = true;
        }
        int textureDimensions = 1;
        int glTarget = GL_TEXTURE_3D;
        if (this.pixelHeight > 0) {
            textureDimensions = 2;
            glTarget = GL20.GL_TEXTURE_2D;
        }
        if (this.pixelDepth > 0) {
            textureDimensions = 3;
            glTarget = GL_TEXTURE_3D;
        }
        if (this.numberOfFaces == 6) {
            if (textureDimensions == 2) {
                glTarget = GL20.GL_TEXTURE_CUBE_MAP;
            } else {
                throw new GdxRuntimeException("cube map needs 2D faces");
            }
        } else if (this.numberOfFaces != 1) {
            throw new GdxRuntimeException("numberOfFaces must be either 1 or 6");
        }
        if (this.numberOfArrayElements > 0) {
            if (glTarget == GL_TEXTURE_3D) {
                glTarget = GL_TEXTURE_3D;
            } else if (glTarget == 3553) {
                glTarget = GL_TEXTURE_3D;
            } else {
                throw new GdxRuntimeException("No API for 3D and cube arrays yet");
            }
            textureDimensions++;
        }
        if (glTarget == GL_TEXTURE_3D) {
            throw new GdxRuntimeException("Unsupported texture format (only 2D texture are supported in LibGdx for the time being)");
        }
        int singleFace = -1;
        if (this.numberOfFaces != 6 || target == 34067) {
            if (this.numberOfFaces == 6 && target == 34067) {
                target = GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
            } else if (target != glTarget && (34069 > target || target > 34074 || target != 3553)) {
                throw new GdxRuntimeException("Invalid target requested : 0x" + Integer.toHexString(target) + ", expecting : 0x" + Integer.toHexString(glTarget));
            }
        } else if (34069 > target || target > 34074) {
            throw new GdxRuntimeException("You must specify either GL_TEXTURE_CUBE_MAP to bind all 6 faces of the cube or the requested face GL_TEXTURE_CUBE_MAP_POSITIVE_X and followings.");
        } else {
            singleFace = target - GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
            target = GL20.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
        }
        Gdx.gl.glGetIntegerv(GL20.GL_UNPACK_ALIGNMENT, buffer);
        int previousUnpackAlignment = buffer.get(0);
        if (previousUnpackAlignment != 4) {
            Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, 4);
        }
        int glInternalFormat = this.glInternalFormat;
        int glFormat = this.glFormat;
        int pos = this.imagePos;
        for (int level = 0; level < this.numberOfMipmapLevels; level++) {
            int pixelWidth = Math.max(1, this.pixelWidth >> level);
            int pixelHeight = Math.max(1, this.pixelHeight >> level);
            int pixelDepth = Math.max(1, this.pixelDepth >> level);
            this.compressedData.position(pos);
            int faceLodSize = this.compressedData.getInt();
            int faceLodSizeRounded = (faceLodSize + 3) & -4;
            pos += 4;
            int face = 0;
            while (face < this.numberOfFaces) {
                this.compressedData.position(pos);
                pos += faceLodSizeRounded;
                if (singleFace == -1 || singleFace == face) {
                    Buffer data = this.compressedData.slice();
                    data.limit(faceLodSizeRounded);
                    if (textureDimensions != 1) {
                        if (textureDimensions == 2) {
                            if (this.numberOfArrayElements > 0) {
                                pixelHeight = this.numberOfArrayElements;
                            }
                            if (!compressed) {
                                Gdx.gl.glTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0, glFormat, this.glType, data);
                            } else if (glInternalFormat != ETC1.ETC1_RGB8_OES) {
                                Gdx.gl.glCompressedTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0, faceLodSize, data);
                            } else if (Gdx.graphics.supportsExtension("GL_OES_compressed_ETC1_RGB8_texture")) {
                                Gdx.gl.glCompressedTexImage2D(target + face, level, glInternalFormat, pixelWidth, pixelHeight, 0, faceLodSize, data);
                            } else {
                                Pixmap pixmap = ETC1.decodeImage(new ETC1Data(pixelWidth, pixelHeight, data, 0), Format.RGB888);
                                Gdx.gl.glTexImage2D(target + face, level, pixmap.getGLInternalFormat(), pixmap.getWidth(), pixmap.getHeight(), 0, pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
                                pixmap.dispose();
                            }
                        } else if (textureDimensions == 3 && this.numberOfArrayElements > 0) {
                            pixelDepth = this.numberOfArrayElements;
                        }
                    }
                }
                face++;
            }
        }
        if (previousUnpackAlignment != 4) {
            Gdx.gl.glPixelStorei(GL20.GL_UNPACK_ALIGNMENT, previousUnpackAlignment);
        }
        if (useMipMaps()) {
            Gdx.gl.glGenerateMipmap(target);
        }
        disposePreparedData();
    }

    public void disposePreparedData() {
        if (this.compressedData != null) {
            BufferUtils.disposeUnsafeByteBuffer(this.compressedData);
        }
        this.compressedData = null;
    }

    public Pixmap consumePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    public boolean disposePixmap() {
        throw new GdxRuntimeException("This TextureData implementation does not return a Pixmap");
    }

    public int getWidth() {
        return this.pixelWidth;
    }

    public int getHeight() {
        return this.pixelHeight;
    }

    public int getNumberOfMipMapLevels() {
        return this.numberOfMipmapLevels;
    }

    public int getNumberOfFaces() {
        return this.numberOfFaces;
    }

    public int getGlInternalFormat() {
        return this.glInternalFormat;
    }

    public ByteBuffer getData(int requestedLevel, int requestedFace) {
        int pos = this.imagePos;
        for (int level = 0; level < this.numberOfMipmapLevels; level++) {
            int faceLodSizeRounded = (this.compressedData.getInt(pos) + 3) & -4;
            pos += 4;
            if (level == requestedLevel) {
                for (int face = 0; face < this.numberOfFaces; face++) {
                    if (face == requestedFace) {
                        this.compressedData.position(pos);
                        ByteBuffer data = this.compressedData.slice();
                        data.limit(faceLodSizeRounded);
                        return data;
                    }
                    pos += faceLodSizeRounded;
                }
                continue;
            } else {
                pos += this.numberOfFaces * faceLodSizeRounded;
            }
        }
        return null;
    }

    public Format getFormat() {
        throw new GdxRuntimeException("This TextureData implementation directly handles texture formats.");
    }

    public boolean useMipMaps() {
        return this.useMipMaps;
    }

    public boolean isManaged() {
        return true;
    }
}
