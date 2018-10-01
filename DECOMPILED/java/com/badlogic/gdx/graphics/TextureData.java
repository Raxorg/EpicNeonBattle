package com.badlogic.gdx.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.ETC1TextureData;
import com.badlogic.gdx.graphics.glutils.FileTextureData;
import com.badlogic.gdx.graphics.glutils.KTXTextureData;

public interface TextureData {

    public static class Factory {
        public static TextureData loadFromFile(FileHandle file, boolean useMipMaps) {
            return loadFromFile(file, null, useMipMaps);
        }

        public static TextureData loadFromFile(FileHandle file, Format format, boolean useMipMaps) {
            if (file == null) {
                return null;
            }
            if (file.name().endsWith(".cim")) {
                return new FileTextureData(file, PixmapIO.readCIM(file), format, useMipMaps);
            }
            if (file.name().endsWith(".etc1")) {
                return new ETC1TextureData(file, useMipMaps);
            }
            if (file.name().endsWith(".ktx") || file.name().endsWith(".zktx")) {
                return new KTXTextureData(file, useMipMaps);
            }
            return new FileTextureData(file, new Pixmap(file), format, useMipMaps);
        }
    }

    public enum TextureDataType {
        Pixmap,
        Custom
    }

    void consumeCustomData(int i);

    Pixmap consumePixmap();

    boolean disposePixmap();

    Format getFormat();

    int getHeight();

    TextureDataType getType();

    int getWidth();

    boolean isManaged();

    boolean isPrepared();

    void prepare();

    boolean useMipMaps();
}
