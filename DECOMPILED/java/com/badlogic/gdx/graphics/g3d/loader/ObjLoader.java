package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.StreamUtils;
import com.epicness.neonbattle.android.BuildConfig;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

public class ObjLoader extends ModelLoader<ObjLoaderParameters> {
    public static boolean logWarning = false;
    final Array<Group> groups;
    final FloatArray norms;
    final FloatArray uvs;
    final FloatArray verts;

    private class Group {
        Array<Integer> faces = new Array((int) HttpStatus.SC_OK);
        boolean hasNorms;
        boolean hasUVs;
        Material mat = new Material(BuildConfig.VERSION_NAME);
        String materialName = "default";
        final String name;
        int numFaces = 0;

        Group(String name) {
            this.name = name;
        }
    }

    public static class ObjLoaderParameters extends ModelParameters {
        public boolean flipV;

        public ObjLoaderParameters(boolean flipV) {
            this.flipV = flipV;
        }
    }

    public ObjLoader() {
        this(null);
    }

    public ObjLoader(FileHandleResolver resolver) {
        super(resolver);
        this.verts = new FloatArray((int) HttpStatus.SC_MULTIPLE_CHOICES);
        this.norms = new FloatArray((int) HttpStatus.SC_MULTIPLE_CHOICES);
        this.uvs = new FloatArray((int) HttpStatus.SC_OK);
        this.groups = new Array(10);
    }

    public Model loadModel(FileHandle fileHandle, boolean flipV) {
        return loadModel(fileHandle, new ObjLoaderParameters(flipV));
    }

    public ModelData loadModelData(FileHandle file, ObjLoaderParameters parameters) {
        return loadModelData(file, parameters == null ? false : parameters.flipV);
    }

    protected ModelData loadModelData(FileHandle file, boolean flipV) {
        int i;
        if (logWarning) {
            Gdx.app.error("ObjLoader", "Wavefront (OBJ) is not fully supported, consult the documentation for more information");
        }
        MtlLoader mtl = new MtlLoader();
        Group activeGroup = new Group("default");
        this.groups.add(activeGroup);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(file.read()), StreamUtils.DEFAULT_BUFFER_SIZE);
        int id = 0;
        while (true) {
            String line = bufferedReader.readLine();
            if (line == null) {
                break;
            }
            String[] tokens = line.split("\\s+");
            if (tokens.length < 1) {
                break;
            }
            Array<Integer> faces;
            try {
                if (tokens[0].length() != 0) {
                    char firstChar = tokens[0].toLowerCase().charAt(0);
                    if (firstChar != '#') {
                        if (firstChar == 'v') {
                            if (tokens[0].length() == 1) {
                                this.verts.add(Float.parseFloat(tokens[1]));
                                this.verts.add(Float.parseFloat(tokens[2]));
                                this.verts.add(Float.parseFloat(tokens[3]));
                            } else if (tokens[0].charAt(1) == 'n') {
                                this.norms.add(Float.parseFloat(tokens[1]));
                                this.norms.add(Float.parseFloat(tokens[2]));
                                this.norms.add(Float.parseFloat(tokens[3]));
                            } else if (tokens[0].charAt(1) == 't') {
                                this.uvs.add(Float.parseFloat(tokens[1]));
                                this.uvs.add(flipV ? 1.0f - Float.parseFloat(tokens[2]) : Float.parseFloat(tokens[2]));
                            }
                        } else if (firstChar == 'f') {
                            faces = activeGroup.faces;
                            i = 1;
                            while (i < tokens.length - 2) {
                                String[] parts = tokens[1].split("/");
                                faces.add(Integer.valueOf(getIndex(parts[0], this.verts.size)));
                                if (parts.length > 2) {
                                    if (i == 1) {
                                        activeGroup.hasNorms = true;
                                    }
                                    faces.add(Integer.valueOf(getIndex(parts[2], this.norms.size)));
                                }
                                if (parts.length > 1 && parts[1].length() > 0) {
                                    if (i == 1) {
                                        activeGroup.hasUVs = true;
                                    }
                                    faces.add(Integer.valueOf(getIndex(parts[1], this.uvs.size)));
                                }
                                i++;
                                parts = tokens[i].split("/");
                                faces.add(Integer.valueOf(getIndex(parts[0], this.verts.size)));
                                if (parts.length > 2) {
                                    faces.add(Integer.valueOf(getIndex(parts[2], this.norms.size)));
                                }
                                if (parts.length > 1 && parts[1].length() > 0) {
                                    faces.add(Integer.valueOf(getIndex(parts[1], this.uvs.size)));
                                }
                                i++;
                                parts = tokens[i].split("/");
                                faces.add(Integer.valueOf(getIndex(parts[0], this.verts.size)));
                                if (parts.length > 2) {
                                    faces.add(Integer.valueOf(getIndex(parts[2], this.norms.size)));
                                }
                                if (parts.length > 1 && parts[1].length() > 0) {
                                    faces.add(Integer.valueOf(getIndex(parts[1], this.uvs.size)));
                                }
                                activeGroup.numFaces++;
                                i--;
                            }
                        } else if (firstChar == 'o' || firstChar == 'g') {
                            if (tokens.length > 1) {
                                activeGroup = setActiveGroup(tokens[1]);
                            } else {
                                activeGroup = setActiveGroup("default");
                            }
                        } else if (tokens[0].equals("mtllib")) {
                            mtl.load(file.parent().child(tokens[1]));
                        } else if (tokens[0].equals("usemtl")) {
                            if (tokens.length == 1) {
                                activeGroup.materialName = "default";
                            } else {
                                activeGroup.materialName = tokens[1];
                            }
                        }
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }
        bufferedReader.close();
        i = 0;
        while (i < this.groups.size) {
            if (((Group) this.groups.get(i)).numFaces < 1) {
                this.groups.removeIndex(i);
                i--;
            }
            i++;
        }
        if (this.groups.size < 1) {
            return null;
        }
        int numGroups = this.groups.size;
        ModelData data = new ModelData();
        for (int g = 0; g < numGroups; g++) {
            int i2;
            Group group = (Group) this.groups.get(g);
            faces = group.faces;
            int numElements = faces.size;
            int numFaces = group.numFaces;
            boolean hasNorms = group.hasNorms;
            boolean hasUVs = group.hasUVs;
            int i3 = numFaces * 3;
            int i4 = (hasNorms ? 3 : 0) + 3;
            if (hasUVs) {
                i2 = 2;
            } else {
                i2 = 0;
            }
            float[] finalVerts = new float[((i2 + i4) * i3)];
            int vi = 0;
            int i5 = 0;
            while (i5 < numElements) {
                i = i5 + 1;
                int intValue = ((Integer) faces.get(i5)).intValue() * 3;
                int i6 = vi + 1;
                int i7 = intValue + 1;
                finalVerts[vi] = this.verts.get(intValue);
                vi = i6 + 1;
                intValue = i7 + 1;
                finalVerts[i6] = this.verts.get(i7);
                i6 = vi + 1;
                finalVerts[vi] = this.verts.get(intValue);
                if (hasNorms) {
                    i5 = i + 1;
                    int intValue2 = ((Integer) faces.get(i)).intValue() * 3;
                    vi = i6 + 1;
                    int i8 = intValue2 + 1;
                    finalVerts[i6] = this.norms.get(intValue2);
                    i6 = vi + 1;
                    intValue2 = i8 + 1;
                    finalVerts[vi] = this.norms.get(i8);
                    vi = i6 + 1;
                    finalVerts[i6] = this.norms.get(intValue2);
                } else {
                    vi = i6;
                    i5 = i;
                }
                if (hasUVs) {
                    i = i5 + 1;
                    int intValue3 = ((Integer) faces.get(i5)).intValue() * 2;
                    i6 = vi + 1;
                    int uvIndex = intValue3 + 1;
                    finalVerts[vi] = this.uvs.get(intValue3);
                    vi = i6 + 1;
                    finalVerts[i6] = this.uvs.get(uvIndex);
                    i6 = vi;
                } else {
                    i6 = vi;
                    i = i5;
                }
                vi = i6;
                i5 = i;
            }
            int numIndices = numFaces * 3 >= 32767 ? 0 : numFaces * 3;
            short[] finalIndices = new short[numIndices];
            if (numIndices > 0) {
                for (i = 0; i < numIndices; i++) {
                    finalIndices[i] = (short) i;
                }
            }
            Array<VertexAttribute> attributes = new Array();
            attributes.add(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE));
            if (hasNorms) {
                attributes.add(new VertexAttribute(8, 3, ShaderProgram.NORMAL_ATTRIBUTE));
            }
            if (hasUVs) {
                attributes.add(new VertexAttribute(16, 2, "a_texCoord0"));
            }
            id++;
            String nodeId = "node" + id;
            String meshId = "mesh" + id;
            String partId = "part" + id;
            ModelNode node = new ModelNode();
            node.id = nodeId;
            node.meshId = meshId;
            node.scale = new Vector3(1.0f, 1.0f, 1.0f);
            node.translation = new Vector3();
            node.rotation = new Quaternion();
            ModelNodePart pm = new ModelNodePart();
            pm.meshPartId = partId;
            pm.materialId = group.materialName;
            node.parts = new ModelNodePart[]{pm};
            ModelMeshPart part = new ModelMeshPart();
            part.id = partId;
            part.indices = finalIndices;
            part.primitiveType = 4;
            ModelMesh mesh = new ModelMesh();
            mesh.id = meshId;
            mesh.attributes = (VertexAttribute[]) attributes.toArray(VertexAttribute.class);
            mesh.vertices = finalVerts;
            mesh.parts = new ModelMeshPart[]{part};
            data.nodes.add(node);
            data.meshes.add(mesh);
            data.materials.add(mtl.getMaterial(group.materialName));
        }
        if (this.verts.size > 0) {
            this.verts.clear();
        }
        if (this.norms.size > 0) {
            this.norms.clear();
        }
        if (this.uvs.size > 0) {
            this.uvs.clear();
        }
        if (this.groups.size <= 0) {
            return data;
        }
        this.groups.clear();
        return data;
    }

    private Group setActiveGroup(String name) {
        Group group;
        Iterator it = this.groups.iterator();
        while (it.hasNext()) {
            group = (Group) it.next();
            if (group.name.equals(name)) {
                return group;
            }
        }
        group = new Group(name);
        this.groups.add(group);
        return group;
    }

    private int getIndex(String index, int size) {
        if (index == null || index.length() == 0) {
            return 0;
        }
        int idx = Integer.parseInt(index);
        if (idx < 0) {
            return size + idx;
        }
        return idx - 1;
    }
}
