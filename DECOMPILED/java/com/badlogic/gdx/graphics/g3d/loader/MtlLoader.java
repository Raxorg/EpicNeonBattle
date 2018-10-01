package com.badlogic.gdx.graphics.g3d.loader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.model.data.ModelMaterial;
import com.badlogic.gdx.graphics.g3d.model.data.ModelTexture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StreamUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;

/* compiled from: ObjLoader */
class MtlLoader {
    public Array<ModelMaterial> materials = new Array();

    MtlLoader() {
    }

    public void load(FileHandle file) {
        String curMatName = "default";
        Color difcolor = Color.WHITE;
        Color speccolor = Color.WHITE;
        float opacity = 1.0f;
        float shininess = 0.0f;
        String texFilename = null;
        if (file != null && file.exists()) {
            ModelMaterial mat;
            ModelTexture tex;
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.read()), StreamUtils.DEFAULT_BUFFER_SIZE);
            Color speccolor2 = speccolor;
            Color difcolor2 = difcolor;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.length() > 0 && line.charAt(0) == '\t') {
                    line = line.substring(1).trim();
                }
                String[] tokens = line.split("\\s+");
                if (!(tokens[0].length() == 0 || tokens[0].charAt(0) == '#')) {
                    String key = tokens[0].toLowerCase();
                    if (key.equals("newmtl")) {
                        mat = new ModelMaterial();
                        mat.id = curMatName;
                        mat.diffuse = new Color(difcolor2);
                        mat.specular = new Color(speccolor2);
                        mat.opacity = opacity;
                        mat.shininess = shininess;
                        if (texFilename != null) {
                            tex = new ModelTexture();
                            tex.usage = 2;
                            tex.fileName = new String(texFilename);
                            if (mat.textures == null) {
                                mat.textures = new Array(1);
                            }
                            mat.textures.add(tex);
                        }
                        this.materials.add(mat);
                        if (tokens.length > 1) {
                            curMatName = tokens[1].replace('.', '_');
                        } else {
                            curMatName = "default";
                        }
                        difcolor = Color.WHITE;
                        speccolor = Color.WHITE;
                        opacity = 1.0f;
                        shininess = 0.0f;
                    } else if (key.equals("kd") || key.equals("ks")) {
                        float r = Float.parseFloat(tokens[1]);
                        float g = Float.parseFloat(tokens[2]);
                        float b = Float.parseFloat(tokens[3]);
                        float a = 1.0f;
                        if (tokens.length > 4) {
                            a = Float.parseFloat(tokens[4]);
                        }
                        if (tokens[0].toLowerCase().equals("kd")) {
                            difcolor = new Color();
                            try {
                                difcolor.set(r, g, b, a);
                                speccolor = speccolor2;
                            } catch (IOException e) {
                                speccolor = speccolor2;
                            }
                        } else {
                            try {
                                speccolor = new Color();
                                try {
                                    speccolor.set(r, g, b, a);
                                    difcolor = difcolor2;
                                } catch (IOException e2) {
                                    difcolor = difcolor2;
                                }
                            } catch (IOException e3) {
                                speccolor = speccolor2;
                                difcolor = difcolor2;
                            }
                        }
                    } else if (key.equals("tr") || key.equals("d")) {
                        opacity = Float.parseFloat(tokens[1]);
                        speccolor = speccolor2;
                        difcolor = difcolor2;
                    } else if (key.equals("ns")) {
                        shininess = Float.parseFloat(tokens[1]);
                        speccolor = speccolor2;
                        difcolor = difcolor2;
                    } else if (key.equals("map_kd")) {
                        texFilename = file.parent().child(tokens[1]).path();
                        speccolor = speccolor2;
                        difcolor = difcolor2;
                    } else {
                        speccolor = speccolor2;
                        difcolor = difcolor2;
                    }
                    speccolor2 = speccolor;
                    difcolor2 = difcolor;
                }
            }
            reader.close();
            mat = new ModelMaterial();
            mat.id = curMatName;
            mat.diffuse = new Color(difcolor2);
            mat.specular = new Color(speccolor2);
            mat.opacity = opacity;
            mat.shininess = shininess;
            if (texFilename != null) {
                tex = new ModelTexture();
                tex.usage = 2;
                tex.fileName = new String(texFilename);
                if (mat.textures == null) {
                    mat.textures = new Array(1);
                }
                mat.textures.add(tex);
            }
            this.materials.add(mat);
            speccolor = speccolor2;
            difcolor = difcolor2;
        }
    }

    public ModelMaterial getMaterial(String name) {
        Iterator it = this.materials.iterator();
        while (it.hasNext()) {
            ModelMaterial m = (ModelMaterial) it.next();
            if (m.id.equals(name)) {
                return m;
            }
        }
        ModelMaterial mat = new ModelMaterial();
        mat.id = name;
        mat.diffuse = new Color(Color.WHITE);
        this.materials.add(mat);
        return mat;
    }
}
