package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.PixmapPacker.Page;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.compression.lzma.Base;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

public class PixmapPackerIO {

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$badlogic$gdx$graphics$g2d$PixmapPackerIO$ImageFormat = new int[ImageFormat.values().length];

        static {
            try {
                $SwitchMap$com$badlogic$gdx$graphics$g2d$PixmapPackerIO$ImageFormat[ImageFormat.CIM.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$badlogic$gdx$graphics$g2d$PixmapPackerIO$ImageFormat[ImageFormat.PNG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public enum ImageFormat {
        CIM(".cim"),
        PNG(".png");
        
        private final String extension;

        public String getExtension() {
            return this.extension;
        }

        private ImageFormat(String extension) {
            this.extension = extension;
        }
    }

    public static class SaveParameters {
        public ImageFormat format = ImageFormat.PNG;
        public TextureFilter magFilter = TextureFilter.Nearest;
        public TextureFilter minFilter = TextureFilter.Nearest;
    }

    public void save(FileHandle file, PixmapPacker packer) throws IOException {
        save(file, packer, new SaveParameters());
    }

    public void save(FileHandle file, PixmapPacker packer, SaveParameters parameters) throws IOException {
        Writer writer = file.writer(false);
        int index = 0;
        Iterator it = packer.pages.iterator();
        while (it.hasNext()) {
            Page page = (Page) it.next();
            if (page.rects.size > 0) {
                index++;
                FileHandle pageFile = file.sibling(file.nameWithoutExtension() + "_" + index + parameters.format.getExtension());
                switch (AnonymousClass1.$SwitchMap$com$badlogic$gdx$graphics$g2d$PixmapPackerIO$ImageFormat[parameters.format.ordinal()]) {
                    case BuildConfig.VERSION_CODE /*1*/:
                        PixmapIO.writeCIM(pageFile, page.image);
                        break;
                    case Base.kNumLenToPosStatesBits /*2*/:
                        PixmapIO.writePNG(pageFile, page.image);
                        break;
                }
                writer.write("\n");
                writer.write(pageFile.name() + "\n");
                writer.write("size: " + page.image.getWidth() + "," + page.image.getHeight() + "\n");
                writer.write("format: " + packer.pageFormat.name() + "\n");
                writer.write("filter: " + parameters.minFilter.name() + "," + parameters.magFilter.name() + "\n");
                writer.write("repeat: none\n");
                Iterator it2 = page.rects.keys().iterator();
                while (it2.hasNext()) {
                    String name = (String) it2.next();
                    writer.write(name + "\n");
                    Rectangle rect = (Rectangle) page.rects.get(name);
                    writer.write("rotate: false\n");
                    writer.write("xy: " + ((int) rect.x) + "," + ((int) rect.y) + "\n");
                    writer.write("size: " + ((int) rect.width) + "," + ((int) rect.height) + "\n");
                    writer.write("orig: " + ((int) rect.width) + "," + ((int) rect.height) + "\n");
                    writer.write("offset: 0, 0\n");
                    writer.write("index: -1\n");
                }
            }
        }
        writer.close();
    }
}
