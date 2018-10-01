package com.badlogic.gdx.graphics.g3d.decals;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool;
import java.util.Comparator;
import java.util.Iterator;

public class CameraGroupStrategy implements GroupStrategy, Disposable {
    private static final int GROUP_BLEND = 1;
    private static final int GROUP_OPAQUE = 0;
    Pool<Array<Decal>> arrayPool;
    Camera camera;
    private final Comparator<Decal> cameraSorter;
    ObjectMap<DecalMaterial, Array<Decal>> materialGroups;
    ShaderProgram shader;
    Array<Array<Decal>> usedArrays;

    class AnonymousClass2 implements Comparator<Decal> {
        final /* synthetic */ Camera val$camera;

        AnonymousClass2(Camera camera) {
            this.val$camera = camera;
        }

        public int compare(Decal o1, Decal o2) {
            return (int) Math.signum(this.val$camera.position.dst(o2.position) - this.val$camera.position.dst(o1.position));
        }
    }

    public CameraGroupStrategy(Camera camera) {
        this(camera, new AnonymousClass2(camera));
    }

    public CameraGroupStrategy(Camera camera, Comparator<Decal> sorter) {
        this.arrayPool = new Pool<Array<Decal>>(16) {
            protected Array<Decal> newObject() {
                return new Array();
            }
        };
        this.usedArrays = new Array();
        this.materialGroups = new ObjectMap();
        this.camera = camera;
        this.cameraSorter = sorter;
        createDefaultShader();
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Camera getCamera() {
        return this.camera;
    }

    public int decideGroup(Decal decal) {
        return decal.getMaterial().isOpaque() ? 0 : GROUP_BLEND;
    }

    public void beforeGroup(int group, Array<Decal> contents) {
        if (group == GROUP_BLEND) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            contents.sort(this.cameraSorter);
            return;
        }
        int n = contents.size;
        for (int i = 0; i < n; i += GROUP_BLEND) {
            Decal decal = (Decal) contents.get(i);
            Array<Decal> materialGroup = (Array) this.materialGroups.get(decal.material);
            if (materialGroup == null) {
                materialGroup = (Array) this.arrayPool.obtain();
                materialGroup.clear();
                this.usedArrays.add(materialGroup);
                this.materialGroups.put(decal.material, materialGroup);
            }
            materialGroup.add(decal);
        }
        contents.clear();
        Iterator it = this.materialGroups.values().iterator();
        while (it.hasNext()) {
            contents.addAll((Array) it.next());
        }
        this.materialGroups.clear();
        this.arrayPool.freeAll(this.usedArrays);
        this.usedArrays.clear();
    }

    public void afterGroup(int group) {
        if (group == GROUP_BLEND) {
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    public void beforeGroups() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        this.shader.begin();
        this.shader.setUniformMatrix("u_projectionViewMatrix", this.camera.combined);
        this.shader.setUniformi("u_texture", 0);
    }

    public void afterGroups() {
        this.shader.end();
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
    }

    private void createDefaultShader() {
        this.shader = new ShaderProgram("attribute vec4 a_position;\nattribute vec4 a_color;\nattribute vec2 a_texCoord0;\nuniform mat4 u_projectionViewMatrix;\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\n\nvoid main()\n{\n   v_color = a_color;\n   v_texCoords = a_texCoord0;\n   gl_Position =  u_projectionViewMatrix * a_position;\n}\n", "#ifdef GL_ES\nprecision mediump float;\n#endif\nvarying vec4 v_color;\nvarying vec2 v_texCoords;\nuniform sampler2D u_texture;\nvoid main()\n{\n  gl_FragColor = v_color * texture2D(u_texture, v_texCoords);\n}");
        if (!this.shader.isCompiled()) {
            throw new IllegalArgumentException("couldn't compile shader: " + this.shader.getLog());
        }
    }

    public ShaderProgram getGroupShader(int group) {
        return this.shader;
    }

    public void dispose() {
        if (this.shader != null) {
            this.shader.dispose();
        }
    }
}
