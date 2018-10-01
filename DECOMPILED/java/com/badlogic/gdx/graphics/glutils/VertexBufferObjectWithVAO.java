package com.badlogic.gdx.graphics.glutils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.BufferUtils;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexBufferObjectWithVAO implements VertexData {
    static final IntBuffer tmpHandle = BufferUtils.newIntBuffer(1);
    final VertexAttributes attributes;
    final FloatBuffer buffer;
    int bufferHandle;
    final ByteBuffer byteBuffer;
    boolean isBound;
    boolean isDirty;
    final boolean isStatic;
    final int usage;
    boolean vaoDirty;
    int vaoHandle;

    public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttribute... attributes) {
        this(isStatic, numVertices, new VertexAttributes(attributes));
    }

    public VertexBufferObjectWithVAO(boolean isStatic, int numVertices, VertexAttributes attributes) {
        this.isDirty = false;
        this.isBound = false;
        this.vaoDirty = true;
        this.vaoHandle = -1;
        this.isStatic = isStatic;
        this.attributes = attributes;
        this.byteBuffer = BufferUtils.newUnsafeByteBuffer(this.attributes.vertexSize * numVertices);
        this.buffer = this.byteBuffer.asFloatBuffer();
        this.buffer.flip();
        this.byteBuffer.flip();
        this.bufferHandle = Gdx.gl20.glGenBuffer();
        this.usage = isStatic ? GL20.GL_STATIC_DRAW : GL20.GL_DYNAMIC_DRAW;
    }

    public VertexAttributes getAttributes() {
        return this.attributes;
    }

    public int getNumVertices() {
        return (this.buffer.limit() * 4) / this.attributes.vertexSize;
    }

    public int getNumMaxVertices() {
        return this.byteBuffer.capacity() / this.attributes.vertexSize;
    }

    public FloatBuffer getBuffer() {
        this.isDirty = true;
        return this.buffer;
    }

    private void bufferChanged() {
        if (this.isBound) {
            Gdx.gl20.glBufferData(GL20.GL_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
    }

    public void setVertices(float[] vertices, int offset, int count) {
        this.isDirty = true;
        BufferUtils.copy(vertices, this.byteBuffer, count, offset);
        this.buffer.position(0);
        this.buffer.limit(count);
        bufferChanged();
    }

    public void updateVertices(int targetOffset, float[] vertices, int sourceOffset, int count) {
        this.isDirty = true;
        int pos = this.byteBuffer.position();
        this.byteBuffer.position(targetOffset * 4);
        BufferUtils.copy(vertices, sourceOffset, count, this.byteBuffer);
        this.byteBuffer.position(pos);
        this.buffer.position(0);
        bufferChanged();
    }

    public void bind(ShaderProgram shader) {
        bind(shader, null);
    }

    public void bind(ShaderProgram shader, int[] locations) {
        GL30 gl = Gdx.gl30;
        if (this.vaoDirty || !gl.glIsVertexArray(this.vaoHandle)) {
            tmpHandle.clear();
            gl.glGenVertexArrays(1, tmpHandle);
            this.vaoHandle = tmpHandle.get(0);
            gl.glBindVertexArray(this.vaoHandle);
            this.vaoDirty = false;
        } else {
            gl.glBindVertexArray(this.vaoHandle);
        }
        bindAttributes(shader, locations);
        bindData(gl);
        this.isBound = true;
    }

    private void bindAttributes(ShaderProgram shader, int[] locations) {
        Gdx.gl20.glBindBuffer(GL20.GL_ARRAY_BUFFER, this.bufferHandle);
        int numAttributes = this.attributes.size();
        int i;
        VertexAttribute attribute;
        int location;
        if (locations == null) {
            for (i = 0; i < numAttributes; i++) {
                attribute = this.attributes.get(i);
                location = shader.getAttributeLocation(attribute.alias);
                if (location >= 0) {
                    shader.enableVertexAttribute(location);
                    shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, attribute.offset);
                }
            }
            return;
        }
        for (i = 0; i < numAttributes; i++) {
            attribute = this.attributes.get(i);
            location = locations[i];
            if (location >= 0) {
                shader.enableVertexAttribute(location);
                shader.setVertexAttribute(location, attribute.numComponents, attribute.type, attribute.normalized, this.attributes.vertexSize, attribute.offset);
            }
        }
    }

    private void bindData(GL20 gl) {
        if (this.isDirty) {
            gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, this.bufferHandle);
            this.byteBuffer.limit(this.buffer.limit() * 4);
            gl.glBufferData(GL20.GL_ARRAY_BUFFER, this.byteBuffer.limit(), this.byteBuffer, this.usage);
            this.isDirty = false;
        }
    }

    public void unbind(ShaderProgram shader) {
        unbind(shader, null);
    }

    public void unbind(ShaderProgram shader, int[] locations) {
        Gdx.gl30.glBindVertexArray(0);
        this.isBound = false;
    }

    public void invalidate() {
        this.bufferHandle = Gdx.gl20.glGenBuffer();
        this.isDirty = true;
        this.vaoDirty = true;
    }

    public void dispose() {
        GL30 gl = Gdx.gl30;
        gl.glBindBuffer(GL20.GL_ARRAY_BUFFER, 0);
        gl.glDeleteBuffer(this.bufferHandle);
        this.bufferHandle = 0;
        BufferUtils.disposeUnsafeByteBuffer(this.byteBuffer);
        if (gl.glIsVertexArray(this.vaoHandle)) {
            tmpHandle.clear();
            tmpHandle.put(this.vaoHandle);
            tmpHandle.flip();
            gl.glDeleteVertexArrays(1, tmpHandle);
        }
    }
}
