package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.epicness.neonbattle.android.BuildConfig;

public class ImmediateModeRenderer20 implements ImmediateModeRenderer {
    private final int colorOffset;
    private final int maxVertices;
    private final Mesh mesh;
    private final int normalOffset;
    private int numSetTexCoords;
    private final int numTexCoords;
    private int numVertices;
    private boolean ownsShader;
    private int primitiveType;
    private final Matrix4 projModelView;
    private ShaderProgram shader;
    private final String[] shaderUniformNames;
    private final int texCoordOffset;
    private int vertexIdx;
    private final int vertexSize;
    private final float[] vertices;

    public ImmediateModeRenderer20(boolean hasNormals, boolean hasColors, int numTexCoords) {
        this(5000, hasNormals, hasColors, numTexCoords, createDefaultShader(hasNormals, hasColors, numTexCoords));
        this.ownsShader = true;
    }

    public ImmediateModeRenderer20(int maxVertices, boolean hasNormals, boolean hasColors, int numTexCoords) {
        this(maxVertices, hasNormals, hasColors, numTexCoords, createDefaultShader(hasNormals, hasColors, numTexCoords));
        this.ownsShader = true;
    }

    public ImmediateModeRenderer20(int maxVertices, boolean hasNormals, boolean hasColors, int numTexCoords, ShaderProgram shader) {
        int i;
        int i2 = 0;
        this.projModelView = new Matrix4();
        this.maxVertices = maxVertices;
        this.numTexCoords = numTexCoords;
        this.shader = shader;
        this.mesh = new Mesh(false, maxVertices, 0, buildVertexAttributes(hasNormals, hasColors, numTexCoords));
        this.vertices = new float[((this.mesh.getVertexAttributes().vertexSize / 4) * maxVertices)];
        this.vertexSize = this.mesh.getVertexAttributes().vertexSize / 4;
        if (this.mesh.getVertexAttribute(8) != null) {
            i = this.mesh.getVertexAttribute(8).offset / 4;
        } else {
            i = 0;
        }
        this.normalOffset = i;
        if (this.mesh.getVertexAttribute(4) != null) {
            i = this.mesh.getVertexAttribute(4).offset / 4;
        } else {
            i = 0;
        }
        this.colorOffset = i;
        if (this.mesh.getVertexAttribute(16) != null) {
            i2 = this.mesh.getVertexAttribute(16).offset / 4;
        }
        this.texCoordOffset = i2;
        this.shaderUniformNames = new String[numTexCoords];
        for (int i3 = 0; i3 < numTexCoords; i3++) {
            this.shaderUniformNames[i3] = "u_sampler" + i3;
        }
    }

    private VertexAttribute[] buildVertexAttributes(boolean hasNormals, boolean hasColor, int numTexCoords) {
        int i;
        Array<VertexAttribute> attribs = new Array();
        attribs.add(new VertexAttribute(1, 3, ShaderProgram.POSITION_ATTRIBUTE));
        if (hasNormals) {
            attribs.add(new VertexAttribute(8, 3, ShaderProgram.NORMAL_ATTRIBUTE));
        }
        if (hasColor) {
            attribs.add(new VertexAttribute(4, 4, ShaderProgram.COLOR_ATTRIBUTE));
        }
        for (i = 0; i < numTexCoords; i++) {
            attribs.add(new VertexAttribute(16, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + i));
        }
        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (i = 0; i < attribs.size; i++) {
            array[i] = (VertexAttribute) attribs.get(i);
        }
        return array;
    }

    public void setShader(ShaderProgram shader) {
        if (this.ownsShader) {
            this.shader.dispose();
        }
        this.shader = shader;
        this.ownsShader = false;
    }

    public void begin(Matrix4 projModelView, int primitiveType) {
        this.projModelView.set(projModelView);
        this.primitiveType = primitiveType;
    }

    public void color(Color color) {
        this.vertices[this.vertexIdx + this.colorOffset] = color.toFloatBits();
    }

    public void color(float r, float g, float b, float a) {
        this.vertices[this.vertexIdx + this.colorOffset] = Color.toFloatBits(r, g, b, a);
    }

    public void color(float colorBits) {
        this.vertices[this.vertexIdx + this.colorOffset] = colorBits;
    }

    public void texCoord(float u, float v) {
        int idx = this.vertexIdx + this.texCoordOffset;
        this.vertices[this.numSetTexCoords + idx] = u;
        this.vertices[(this.numSetTexCoords + idx) + 1] = v;
        this.numSetTexCoords += 2;
    }

    public void normal(float x, float y, float z) {
        int idx = this.vertexIdx + this.normalOffset;
        this.vertices[idx] = x;
        this.vertices[idx + 1] = y;
        this.vertices[idx + 2] = z;
    }

    public void vertex(float x, float y, float z) {
        int idx = this.vertexIdx;
        this.vertices[idx] = x;
        this.vertices[idx + 1] = y;
        this.vertices[idx + 2] = z;
        this.numSetTexCoords = 0;
        this.vertexIdx += this.vertexSize;
        this.numVertices++;
    }

    public void flush() {
        if (this.numVertices != 0) {
            this.shader.begin();
            this.shader.setUniformMatrix("u_projModelView", this.projModelView);
            for (int i = 0; i < this.numTexCoords; i++) {
                this.shader.setUniformi(this.shaderUniformNames[i], i);
            }
            this.mesh.setVertices(this.vertices, 0, this.vertexIdx);
            this.mesh.render(this.shader, this.primitiveType);
            this.shader.end();
            this.numSetTexCoords = 0;
            this.vertexIdx = 0;
            this.numVertices = 0;
        }
    }

    public void end() {
        flush();
    }

    public int getNumVertices() {
        return this.numVertices;
    }

    public int getMaxVertices() {
        return this.maxVertices;
    }

    public void dispose() {
        if (this.ownsShader && this.shader != null) {
            this.shader.dispose();
        }
        this.mesh.dispose();
    }

    private static String createVertexShader(boolean hasNormals, boolean hasColors, int numTexCoords) {
        int i;
        String shader = "attribute vec4 a_position;\n" + (hasNormals ? "attribute vec3 a_normal;\n" : BuildConfig.VERSION_NAME) + (hasColors ? "attribute vec4 a_color;\n" : BuildConfig.VERSION_NAME);
        for (i = 0; i < numTexCoords; i++) {
            shader = shader + "attribute vec2 a_texCoord" + i + ";\n";
        }
        shader = (shader + "uniform mat4 u_projModelView;\n") + (hasColors ? "varying vec4 v_col;\n" : BuildConfig.VERSION_NAME);
        for (i = 0; i < numTexCoords; i++) {
            shader = shader + "varying vec2 v_tex" + i + ";\n";
        }
        shader = shader + "void main() {\n   gl_Position = u_projModelView * a_position;\n" + (hasColors ? "   v_col = a_color;\n" : BuildConfig.VERSION_NAME);
        for (i = 0; i < numTexCoords; i++) {
            shader = shader + "   v_tex" + i + " = " + ShaderProgram.TEXCOORD_ATTRIBUTE + i + ";\n";
        }
        return (shader + "   gl_PointSize = 1.0;\n") + "}\n";
    }

    private static String createFragmentShader(boolean hasNormals, boolean hasColors, int numTexCoords) {
        int i;
        String shader = "#ifdef GL_ES\nprecision mediump float;\n#endif\n";
        if (hasColors) {
            shader = shader + "varying vec4 v_col;\n";
        }
        for (i = 0; i < numTexCoords; i++) {
            shader = (shader + "varying vec2 v_tex" + i + ";\n") + "uniform sampler2D u_sampler" + i + ";\n";
        }
        shader = shader + "void main() {\n   gl_FragColor = " + (hasColors ? "v_col" : "vec4(1, 1, 1, 1)");
        if (numTexCoords > 0) {
            shader = shader + " * ";
        }
        for (i = 0; i < numTexCoords; i++) {
            if (i == numTexCoords - 1) {
                shader = shader + " texture2D(u_sampler" + i + ",  v_tex" + i + ")";
            } else {
                shader = shader + " texture2D(u_sampler" + i + ",  v_tex" + i + ") *";
            }
        }
        return shader + ";\n}";
    }

    public static ShaderProgram createDefaultShader(boolean hasNormals, boolean hasColors, int numTexCoords) {
        return new ShaderProgram(createVertexShader(hasNormals, hasColors, numTexCoords), createFragmentShader(hasNormals, hasColors, numTexCoords));
    }
}
