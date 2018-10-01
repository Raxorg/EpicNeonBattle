package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.ModelLoader.ModelParameters;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.model.data.ModelAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelData;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMesh;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMeshPart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNode;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeAnimation;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodeKeyframe;
import com.badlogic.gdx.graphics.g3d.model.data.ModelNodePart;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;
import com.badlogic.gdx.utils.BaseJsonReader;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.JsonValue;
import com.epicness.neonbattle.android.BuildConfig;
import java.util.Iterator;

public class G3dModelLoader extends ModelLoader<ModelParameters> {
    public static final short VERSION_HI = (short) 0;
    public static final short VERSION_LO = (short) 1;
    protected final BaseJsonReader reader;
    private final Quaternion tempQ;

    public G3dModelLoader(BaseJsonReader reader) {
        this(reader, null);
    }

    public G3dModelLoader(BaseJsonReader reader, FileHandleResolver resolver) {
        super(resolver);
        this.tempQ = new Quaternion();
        this.reader = reader;
    }

    public ModelData loadModelData(FileHandle fileHandle, ModelParameters parameters) {
        return parseModel(fileHandle);
    }

    public ModelData parseModel(FileHandle handle) {
        JsonValue json = this.reader.parse(handle);
        ModelData model = new ModelData();
        JsonValue version = json.require("version");
        model.version[0] = version.getShort(0);
        model.version[1] = version.getShort(1);
        if (model.version[0] == (short) 0 && model.version[1] == VERSION_LO) {
            model.id = json.getString("id", BuildConfig.VERSION_NAME);
            parseMeshes(model, json);
            parseMaterials(model, json, handle.parent().path());
            parseNodes(model, json);
            parseAnimations(model, json);
            return model;
        }
        throw new GdxRuntimeException("Model version not supported");
    }

    private void parseMeshes(ModelData model, JsonValue json) {
        JsonValue meshes = json.get("meshes");
        if (meshes != null) {
            model.meshes.ensureCapacity(meshes.size);
            for (JsonValue mesh = meshes.child; mesh != null; mesh = mesh.next) {
                ModelMesh jsonMesh = new ModelMesh();
                jsonMesh.id = mesh.getString("id", BuildConfig.VERSION_NAME);
                jsonMesh.attributes = parseAttributes(mesh.require("attributes"));
                jsonMesh.vertices = mesh.require("vertices").asFloatArray();
                JsonValue meshParts = mesh.require("parts");
                Array<ModelMeshPart> parts = new Array();
                for (JsonValue meshPart = meshParts.child; meshPart != null; meshPart = meshPart.next) {
                    ModelMeshPart jsonPart = new ModelMeshPart();
                    String partId = meshPart.getString("id", null);
                    if (partId == null) {
                        throw new GdxRuntimeException("Not id given for mesh part");
                    }
                    Iterator it = parts.iterator();
                    while (it.hasNext()) {
                        if (((ModelMeshPart) it.next()).id.equals(partId)) {
                            throw new GdxRuntimeException("Mesh part with id '" + partId + "' already in defined");
                        }
                    }
                    jsonPart.id = partId;
                    String type = meshPart.getString("type", null);
                    if (type == null) {
                        throw new GdxRuntimeException("No primitive type given for mesh part '" + partId + "'");
                    }
                    jsonPart.primitiveType = parseType(type);
                    jsonPart.indices = meshPart.require("indices").asShortArray();
                    parts.add(jsonPart);
                }
                jsonMesh.parts = (ModelMeshPart[]) parts.toArray(ModelMeshPart.class);
                model.meshes.add(jsonMesh);
            }
        }
    }

    private int parseType(String type) {
        if (type.equals("TRIANGLES")) {
            return 4;
        }
        if (type.equals("LINES")) {
            return 1;
        }
        if (type.equals("POINTS")) {
            return 0;
        }
        if (type.equals("TRIANGLE_STRIP")) {
            return 5;
        }
        if (type.equals("LINE_STRIP")) {
            return 3;
        }
        throw new GdxRuntimeException("Unknown primitive type '" + type + "', should be one of triangle, trianglestrip, line, linestrip, lineloop or point");
    }

    private VertexAttribute[] parseAttributes(JsonValue attributes) {
        Array<VertexAttribute> vertexAttributes = new Array();
        JsonValue value = attributes.child;
        int blendWeightCount = 0;
        int unit = 0;
        while (value != null) {
            int blendWeightCount2;
            int unit2;
            String attr = value.asString();
            if (attr.equals("POSITION")) {
                vertexAttributes.add(VertexAttribute.Position());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.equals("NORMAL")) {
                vertexAttributes.add(VertexAttribute.Normal());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.equals("COLOR")) {
                vertexAttributes.add(VertexAttribute.ColorUnpacked());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.equals("COLORPACKED")) {
                vertexAttributes.add(VertexAttribute.ColorPacked());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.equals("TANGENT")) {
                vertexAttributes.add(VertexAttribute.Tangent());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.equals("BINORMAL")) {
                vertexAttributes.add(VertexAttribute.Binormal());
                blendWeightCount2 = blendWeightCount;
                unit2 = unit;
            } else if (attr.startsWith("TEXCOORD")) {
                unit2 = unit + 1;
                vertexAttributes.add(VertexAttribute.TexCoords(unit));
                blendWeightCount2 = blendWeightCount;
            } else if (attr.startsWith("BLENDWEIGHT")) {
                blendWeightCount2 = blendWeightCount + 1;
                vertexAttributes.add(VertexAttribute.BoneWeight(blendWeightCount));
                unit2 = unit;
            } else {
                throw new GdxRuntimeException("Unknown vertex attribute '" + attr + "', should be one of position, normal, uv, tangent or binormal");
            }
            value = value.next;
            blendWeightCount = blendWeightCount2;
            unit = unit2;
        }
        return (VertexAttribute[]) vertexAttributes.toArray(VertexAttribute.class);
    }

    private void parseMaterials(ModelData model, JsonValue json, String materialDir) {
        JsonValue materials = json.get("materials");
        if (materials != null) {
            model.materials.ensureCapacity(materials.size);
            for (JsonValue material = materials.child; material != null; material = material.next) {
                ModelMaterial jsonMaterial = new ModelMaterial();
                String id = material.getString("id", null);
                if (id == null) {
                    throw new GdxRuntimeException("Material needs an id.");
                }
                jsonMaterial.id = id;
                JsonValue diffuse = material.get("diffuse");
                if (diffuse != null) {
                    jsonMaterial.diffuse = parseColor(diffuse);
                }
                JsonValue ambient = material.get("ambient");
                if (ambient != null) {
                    jsonMaterial.ambient = parseColor(ambient);
                }
                JsonValue emissive = material.get("emissive");
                if (emissive != null) {
                    jsonMaterial.emissive = parseColor(emissive);
                }
                JsonValue specular = material.get("specular");
                if (specular != null) {
                    jsonMaterial.specular = parseColor(specular);
                }
                JsonValue reflection = material.get("reflection");
                if (reflection != null) {
                    jsonMaterial.reflection = parseColor(reflection);
                }
                jsonMaterial.shininess = material.getFloat(FloatAttribute.ShininessAlias, 0.0f);
                jsonMaterial.opacity = material.getFloat("opacity", 1.0f);
                JsonValue textures = material.get("textures");
                if (textures != null) {
                    for (JsonValue texture = textures.child; texture != null; texture = texture.next) {
                        ModelTexture jsonTexture = new ModelTexture();
                        String textureId = texture.getString("id", null);
                        if (textureId == null) {
                            throw new GdxRuntimeException("Texture has no id.");
                        }
                        jsonTexture.id = textureId;
                        String fileName = texture.getString("filename", null);
                        if (fileName == null) {
                            throw new GdxRuntimeException("Texture needs filename.");
                        }
                        StringBuilder append = new StringBuilder().append(materialDir);
                        String str = (materialDir.length() == 0 || materialDir.endsWith("/")) ? BuildConfig.VERSION_NAME : "/";
                        jsonTexture.fileName = append.append(str).append(fileName).toString();
                        jsonTexture.uvTranslation = readVector2(texture.get("uvTranslation"), 0.0f, 0.0f);
                        jsonTexture.uvScaling = readVector2(texture.get("uvScaling"), 1.0f, 1.0f);
                        String textureType = texture.getString("type", null);
                        if (textureType == null) {
                            throw new GdxRuntimeException("Texture needs type.");
                        }
                        jsonTexture.usage = parseTextureUsage(textureType);
                        if (jsonMaterial.textures == null) {
                            jsonMaterial.textures = new Array();
                        }
                        jsonMaterial.textures.add(jsonTexture);
                    }
                    continue;
                }
                model.materials.add(jsonMaterial);
            }
        }
    }

    private int parseTextureUsage(String value) {
        if (value.equalsIgnoreCase("AMBIENT")) {
            return 4;
        }
        if (value.equalsIgnoreCase("BUMP")) {
            return 8;
        }
        if (value.equalsIgnoreCase("DIFFUSE")) {
            return 2;
        }
        if (value.equalsIgnoreCase("EMISSIVE")) {
            return 3;
        }
        if (value.equalsIgnoreCase("NONE")) {
            return 1;
        }
        if (value.equalsIgnoreCase("NORMAL")) {
            return 7;
        }
        if (value.equalsIgnoreCase("REFLECTION")) {
            return 10;
        }
        if (value.equalsIgnoreCase("SHININESS")) {
            return 6;
        }
        if (value.equalsIgnoreCase("SPECULAR")) {
            return 5;
        }
        if (value.equalsIgnoreCase("TRANSPARENCY")) {
            return 9;
        }
        return 0;
    }

    private Color parseColor(JsonValue colorArray) {
        if (colorArray.size >= 3) {
            return new Color(colorArray.getFloat(0), colorArray.getFloat(1), colorArray.getFloat(2), 1.0f);
        }
        throw new GdxRuntimeException("Expected Color values <> than three.");
    }

    private Vector2 readVector2(JsonValue vectorArray, float x, float y) {
        if (vectorArray == null) {
            return new Vector2(x, y);
        }
        if (vectorArray.size == 2) {
            return new Vector2(vectorArray.getFloat(0), vectorArray.getFloat(1));
        }
        throw new GdxRuntimeException("Expected Vector2 values <> than two.");
    }

    private Array<ModelNode> parseNodes(ModelData model, JsonValue json) {
        JsonValue nodes = json.get("nodes");
        if (nodes != null) {
            model.nodes.ensureCapacity(nodes.size);
            for (JsonValue node = nodes.child; node != null; node = node.next) {
                model.nodes.add(parseNodesRecursively(node));
            }
        }
        return model.nodes;
    }

    private ModelNode parseNodesRecursively(JsonValue json) {
        ModelNode jsonNode = new ModelNode();
        String id = json.getString("id", null);
        if (id == null) {
            throw new GdxRuntimeException("Node id missing.");
        }
        jsonNode.id = id;
        JsonValue translation = json.get("translation");
        if (translation == null || translation.size == 3) {
            Vector3 vector3;
            if (translation == null) {
                vector3 = null;
            } else {
                vector3 = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
            }
            jsonNode.translation = vector3;
            JsonValue rotation = json.get("rotation");
            if (rotation == null || rotation.size == 4) {
                Quaternion quaternion;
                if (rotation == null) {
                    quaternion = null;
                } else {
                    quaternion = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
                }
                jsonNode.rotation = quaternion;
                JsonValue scale = json.get("scale");
                if (scale == null || scale.size == 3) {
                    int i;
                    if (scale == null) {
                        vector3 = null;
                    } else {
                        vector3 = new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));
                    }
                    jsonNode.scale = vector3;
                    String meshId = json.getString("mesh", null);
                    if (meshId != null) {
                        jsonNode.meshId = meshId;
                    }
                    JsonValue materials = json.get("parts");
                    if (materials != null) {
                        jsonNode.parts = new ModelNodePart[materials.size];
                        i = 0;
                        JsonValue material = materials.child;
                        while (material != null) {
                            ModelNodePart nodePart = new ModelNodePart();
                            String meshPartId = material.getString("meshpartid", null);
                            String materialId = material.getString("materialid", null);
                            if (meshPartId == null || materialId == null) {
                                throw new GdxRuntimeException("Node " + id + " part is missing meshPartId or materialId");
                            }
                            nodePart.materialId = materialId;
                            nodePart.meshPartId = meshPartId;
                            JsonValue bones = material.get("bones");
                            if (bones != null) {
                                nodePart.bones = new ArrayMap(true, bones.size, String.class, Matrix4.class);
                                int j = 0;
                                JsonValue bone = bones.child;
                                while (bone != null) {
                                    String nodeId = bone.getString("node", null);
                                    if (nodeId == null) {
                                        throw new GdxRuntimeException("Bone node ID missing");
                                    }
                                    Matrix4 transform = new Matrix4();
                                    JsonValue val = bone.get("translation");
                                    if (val != null && val.size >= 3) {
                                        transform.translate(val.getFloat(0), val.getFloat(1), val.getFloat(2));
                                    }
                                    val = bone.get("rotation");
                                    if (val != null && val.size >= 4) {
                                        transform.rotate(this.tempQ.set(val.getFloat(0), val.getFloat(1), val.getFloat(2), val.getFloat(3)));
                                    }
                                    val = bone.get("scale");
                                    if (val != null && val.size >= 3) {
                                        transform.scale(val.getFloat(0), val.getFloat(1), val.getFloat(2));
                                    }
                                    nodePart.bones.put(nodeId, transform);
                                    bone = bone.next;
                                    j++;
                                }
                                continue;
                            }
                            jsonNode.parts[i] = nodePart;
                            material = material.next;
                            i++;
                        }
                    }
                    JsonValue children = json.get("children");
                    if (children != null) {
                        jsonNode.children = new ModelNode[children.size];
                        i = 0;
                        JsonValue child = children.child;
                        while (child != null) {
                            jsonNode.children[i] = parseNodesRecursively(child);
                            child = child.next;
                            i++;
                        }
                    }
                    return jsonNode;
                }
                throw new GdxRuntimeException("Node scale incomplete");
            }
            throw new GdxRuntimeException("Node rotation incomplete");
        }
        throw new GdxRuntimeException("Node translation incomplete");
    }

    private void parseAnimations(ModelData model, JsonValue json) {
        JsonValue animations = json.get("animations");
        if (animations != null) {
            model.animations.ensureCapacity(animations.size);
            for (JsonValue anim = animations.child; anim != null; anim = anim.next) {
                JsonValue nodes = anim.get("bones");
                if (nodes != null) {
                    ModelAnimation animation = new ModelAnimation();
                    model.animations.add(animation);
                    animation.nodeAnimations.ensureCapacity(nodes.size);
                    animation.id = anim.getString("id");
                    for (JsonValue node = nodes.child; node != null; node = node.next) {
                        ModelNodeAnimation nodeAnim = new ModelNodeAnimation();
                        animation.nodeAnimations.add(nodeAnim);
                        nodeAnim.nodeId = node.getString("boneId");
                        JsonValue keyframes = node.get("keyframes");
                        JsonValue keyframe;
                        JsonValue translation;
                        JsonValue rotation;
                        if (keyframes == null || !keyframes.isArray()) {
                            ModelNodeKeyframe<Vector3> kf;
                            JsonValue translationKF = node.get("translation");
                            if (translationKF != null && translationKF.isArray()) {
                                nodeAnim.translation = new Array();
                                nodeAnim.translation.ensureCapacity(translationKF.size);
                                for (keyframe = translationKF.child; keyframe != null; keyframe = keyframe.next) {
                                    kf = new ModelNodeKeyframe();
                                    nodeAnim.translation.add(kf);
                                    kf.keytime = keyframe.getFloat("keytime", 0.0f) / 1000.0f;
                                    translation = keyframe.get("value");
                                    if (translation != null && translation.size >= 3) {
                                        kf.value = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
                                    }
                                }
                            }
                            JsonValue rotationKF = node.get("rotation");
                            if (rotationKF != null && rotationKF.isArray()) {
                                nodeAnim.rotation = new Array();
                                nodeAnim.rotation.ensureCapacity(rotationKF.size);
                                for (keyframe = rotationKF.child; keyframe != null; keyframe = keyframe.next) {
                                    ModelNodeKeyframe<Quaternion> kf2 = new ModelNodeKeyframe();
                                    nodeAnim.rotation.add(kf2);
                                    kf2.keytime = keyframe.getFloat("keytime", 0.0f) / 1000.0f;
                                    rotation = keyframe.get("value");
                                    if (rotation != null && rotation.size >= 4) {
                                        kf2.value = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
                                    }
                                }
                            }
                            JsonValue scalingKF = node.get("scaling");
                            if (scalingKF != null && scalingKF.isArray()) {
                                nodeAnim.scaling = new Array();
                                nodeAnim.scaling.ensureCapacity(scalingKF.size);
                                for (keyframe = scalingKF.child; keyframe != null; keyframe = keyframe.next) {
                                    kf = new ModelNodeKeyframe();
                                    nodeAnim.scaling.add(kf);
                                    kf.keytime = keyframe.getFloat("keytime", 0.0f) / 1000.0f;
                                    JsonValue scaling = keyframe.get("value");
                                    if (scaling != null && scaling.size >= 3) {
                                        kf.value = new Vector3(scaling.getFloat(0), scaling.getFloat(1), scaling.getFloat(2));
                                    }
                                }
                            }
                        } else {
                            for (keyframe = keyframes.child; keyframe != null; keyframe = keyframe.next) {
                                float keytime = keyframe.getFloat("keytime", 0.0f) / 1000.0f;
                                translation = keyframe.get("translation");
                                if (translation != null && translation.size == 3) {
                                    if (nodeAnim.translation == null) {
                                        nodeAnim.translation = new Array();
                                    }
                                    ModelNodeKeyframe<Vector3> tkf = new ModelNodeKeyframe();
                                    tkf.keytime = keytime;
                                    tkf.value = new Vector3(translation.getFloat(0), translation.getFloat(1), translation.getFloat(2));
                                    nodeAnim.translation.add(tkf);
                                }
                                rotation = keyframe.get("rotation");
                                if (rotation != null && rotation.size == 4) {
                                    if (nodeAnim.rotation == null) {
                                        nodeAnim.rotation = new Array();
                                    }
                                    ModelNodeKeyframe<Quaternion> rkf = new ModelNodeKeyframe();
                                    rkf.keytime = keytime;
                                    rkf.value = new Quaternion(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2), rotation.getFloat(3));
                                    nodeAnim.rotation.add(rkf);
                                }
                                JsonValue scale = keyframe.get("scale");
                                if (scale != null && scale.size == 3) {
                                    if (nodeAnim.scaling == null) {
                                        nodeAnim.scaling = new Array();
                                    }
                                    ModelNodeKeyframe<Vector3> skf = new ModelNodeKeyframe();
                                    skf.keytime = keytime;
                                    skf.value = new Vector3(scale.getFloat(0), scale.getFloat(1), scale.getFloat(2));
                                    nodeAnim.scaling.add(skf);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
