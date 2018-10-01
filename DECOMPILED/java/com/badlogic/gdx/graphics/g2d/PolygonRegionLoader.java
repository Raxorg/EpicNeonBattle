package com.badlogic.gdx.graphics.g2d;

import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.SynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.BufferedReader;
import java.io.IOException;

public class PolygonRegionLoader extends SynchronousAssetLoader<PolygonRegion, PolygonRegionParameters> {
    private PolygonRegionParameters defaultParameters;
    private EarClippingTriangulator triangulator;

    public static class PolygonRegionParameters extends AssetLoaderParameters<PolygonRegion> {
        public int readerBuffer = GL20.GL_STENCIL_BUFFER_BIT;
        public String[] textureExtensions = new String[]{"png", "PNG", "jpeg", "JPEG", "jpg", "JPG", "cim", "CIM", "etc1", "ETC1", "ktx", "KTX", "zktx", "ZKTX"};
        public String texturePrefix = "i ";
    }

    public PolygonRegionLoader() {
        this(new InternalFileHandleResolver());
    }

    public PolygonRegionLoader(FileHandleResolver resolver) {
        super(resolver);
        this.defaultParameters = new PolygonRegionParameters();
        this.triangulator = new EarClippingTriangulator();
    }

    public PolygonRegion load(AssetManager manager, String fileName, FileHandle file, PolygonRegionParameters parameter) {
        return load(new TextureRegion((Texture) manager.get((String) manager.getDependencies(fileName).first())), file);
    }

    public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, PolygonRegionParameters params) {
        if (params == null) {
            params = this.defaultParameters;
        }
        String image = null;
        try {
            BufferedReader reader = file.reader(params.readerBuffer);
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                if (line.startsWith(params.texturePrefix)) {
                    image = line.substring(params.texturePrefix.length());
                    break;
                }
            }
            reader.close();
            if (image == null && params.textureExtensions != null) {
                for (String extension : params.textureExtensions) {
                    FileHandle sibling = file.sibling(file.nameWithoutExtension().concat("." + extension));
                    if (sibling.exists()) {
                        image = sibling.name();
                    }
                }
            }
            if (image == null) {
                return null;
            }
            Array<AssetDescriptor> deps = new Array(1);
            deps.add(new AssetDescriptor(file.sibling(image), Texture.class));
            return deps;
        } catch (IOException e) {
            throw new GdxRuntimeException("Error reading " + fileName, e);
        }
    }

    public PolygonRegion load(TextureRegion textureRegion, FileHandle file) {
        String line;
        BufferedReader reader = file.reader((int) Usage.BiNormal);
        do {
            try {
                line = reader.readLine();
                if (line == null) {
                    StreamUtils.closeQuietly(reader);
                    throw new GdxRuntimeException("Polygon shape not found: " + file);
                }
            } catch (IOException ex) {
                throw new GdxRuntimeException("Error reading polygon shape file: " + file, ex);
            } catch (Throwable th) {
                StreamUtils.closeQuietly(reader);
            }
        } while (!line.startsWith("s"));
        String[] polygonStrings = line.substring(1).trim().split(",");
        float[] vertices = new float[polygonStrings.length];
        int n = vertices.length;
        for (int i = 0; i < n; i++) {
            vertices[i] = Float.parseFloat(polygonStrings[i]);
        }
        PolygonRegion polygonRegion = new PolygonRegion(textureRegion, vertices, this.triangulator.computeTriangles(vertices).toArray());
        StreamUtils.closeQuietly(reader);
        return polygonRegion;
    }
}
