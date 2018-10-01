package com.badlogic.gdx.graphics.profiling;

import com.badlogic.gdx.graphics.GL20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GL20Profiler extends GLProfiler implements GL20 {
    public final GL20 gl20;

    protected GL20Profiler(GL20 gl20) {
        this.gl20 = gl20;
    }

    private void check() {
        int error = this.gl20.glGetError();
        while (error != 0) {
            listener.onError(error);
            error = this.gl20.glGetError();
        }
    }

    public void glActiveTexture(int texture) {
        calls++;
        this.gl20.glActiveTexture(texture);
        check();
    }

    public void glBindTexture(int target, int texture) {
        textureBindings++;
        calls++;
        this.gl20.glBindTexture(target, texture);
        check();
    }

    public void glBlendFunc(int sfactor, int dfactor) {
        calls++;
        this.gl20.glBlendFunc(sfactor, dfactor);
        check();
    }

    public void glClear(int mask) {
        calls++;
        this.gl20.glClear(mask);
        check();
    }

    public void glClearColor(float red, float green, float blue, float alpha) {
        calls++;
        this.gl20.glClearColor(red, green, blue, alpha);
        check();
    }

    public void glClearDepthf(float depth) {
        calls++;
        this.gl20.glClearDepthf(depth);
        check();
    }

    public void glClearStencil(int s) {
        calls++;
        this.gl20.glClearStencil(s);
        check();
    }

    public void glColorMask(boolean red, boolean green, boolean blue, boolean alpha) {
        calls++;
        this.gl20.glColorMask(red, green, blue, alpha);
        check();
    }

    public void glCompressedTexImage2D(int target, int level, int internalformat, int width, int height, int border, int imageSize, Buffer data) {
        calls++;
        this.gl20.glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
        check();
    }

    public void glCompressedTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int imageSize, Buffer data) {
        calls++;
        this.gl20.glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
        check();
    }

    public void glCopyTexImage2D(int target, int level, int internalformat, int x, int y, int width, int height, int border) {
        calls++;
        this.gl20.glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
        check();
    }

    public void glCopyTexSubImage2D(int target, int level, int xoffset, int yoffset, int x, int y, int width, int height) {
        calls++;
        this.gl20.glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
        check();
    }

    public void glCullFace(int mode) {
        calls++;
        this.gl20.glCullFace(mode);
        check();
    }

    public void glDeleteTextures(int n, IntBuffer textures) {
        calls++;
        this.gl20.glDeleteTextures(n, textures);
        check();
    }

    public void glDeleteTexture(int texture) {
        calls++;
        this.gl20.glDeleteTexture(texture);
        check();
    }

    public void glDepthFunc(int func) {
        calls++;
        this.gl20.glDepthFunc(func);
        check();
    }

    public void glDepthMask(boolean flag) {
        calls++;
        this.gl20.glDepthMask(flag);
        check();
    }

    public void glDepthRangef(float zNear, float zFar) {
        calls++;
        this.gl20.glDepthRangef(zNear, zFar);
        check();
    }

    public void glDisable(int cap) {
        calls++;
        this.gl20.glDisable(cap);
        check();
    }

    public void glDrawArrays(int mode, int first, int count) {
        vertexCount.put((float) count);
        drawCalls++;
        calls++;
        this.gl20.glDrawArrays(mode, first, count);
        check();
    }

    public void glDrawElements(int mode, int count, int type, Buffer indices) {
        vertexCount.put((float) count);
        drawCalls++;
        calls++;
        this.gl20.glDrawElements(mode, count, type, indices);
        check();
    }

    public void glEnable(int cap) {
        calls++;
        this.gl20.glEnable(cap);
        check();
    }

    public void glFinish() {
        calls++;
        this.gl20.glFinish();
        check();
    }

    public void glFlush() {
        calls++;
        this.gl20.glFlush();
        check();
    }

    public void glFrontFace(int mode) {
        calls++;
        this.gl20.glFrontFace(mode);
        check();
    }

    public void glGenTextures(int n, IntBuffer textures) {
        calls++;
        this.gl20.glGenTextures(n, textures);
        check();
    }

    public int glGenTexture() {
        calls++;
        int result = this.gl20.glGenTexture();
        check();
        return result;
    }

    public int glGetError() {
        calls++;
        return this.gl20.glGetError();
    }

    public void glGetIntegerv(int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetIntegerv(pname, params);
        check();
    }

    public String glGetString(int name) {
        calls++;
        String result = this.gl20.glGetString(name);
        check();
        return result;
    }

    public void glHint(int target, int mode) {
        calls++;
        this.gl20.glHint(target, mode);
        check();
    }

    public void glLineWidth(float width) {
        calls++;
        this.gl20.glLineWidth(width);
        check();
    }

    public void glPixelStorei(int pname, int param) {
        calls++;
        this.gl20.glPixelStorei(pname, param);
        check();
    }

    public void glPolygonOffset(float factor, float units) {
        calls++;
        this.gl20.glPolygonOffset(factor, units);
        check();
    }

    public void glReadPixels(int x, int y, int width, int height, int format, int type, Buffer pixels) {
        calls++;
        this.gl20.glReadPixels(x, y, width, height, format, type, pixels);
        check();
    }

    public void glScissor(int x, int y, int width, int height) {
        calls++;
        this.gl20.glScissor(x, y, width, height);
        check();
    }

    public void glStencilFunc(int func, int ref, int mask) {
        calls++;
        this.gl20.glStencilFunc(func, ref, mask);
        check();
    }

    public void glStencilMask(int mask) {
        calls++;
        this.gl20.glStencilMask(mask);
        check();
    }

    public void glStencilOp(int fail, int zfail, int zpass) {
        calls++;
        this.gl20.glStencilOp(fail, zfail, zpass);
        check();
    }

    public void glTexImage2D(int target, int level, int internalformat, int width, int height, int border, int format, int type, Buffer pixels) {
        calls++;
        this.gl20.glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
        check();
    }

    public void glTexParameterf(int target, int pname, float param) {
        calls++;
        this.gl20.glTexParameterf(target, pname, param);
        check();
    }

    public void glTexSubImage2D(int target, int level, int xoffset, int yoffset, int width, int height, int format, int type, Buffer pixels) {
        calls++;
        this.gl20.glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
        check();
    }

    public void glViewport(int x, int y, int width, int height) {
        calls++;
        this.gl20.glViewport(x, y, width, height);
        check();
    }

    public void glAttachShader(int program, int shader) {
        calls++;
        this.gl20.glAttachShader(program, shader);
        check();
    }

    public void glBindAttribLocation(int program, int index, String name) {
        calls++;
        this.gl20.glBindAttribLocation(program, index, name);
        check();
    }

    public void glBindBuffer(int target, int buffer) {
        calls++;
        this.gl20.glBindBuffer(target, buffer);
        check();
    }

    public void glBindFramebuffer(int target, int framebuffer) {
        calls++;
        this.gl20.glBindFramebuffer(target, framebuffer);
        check();
    }

    public void glBindRenderbuffer(int target, int renderbuffer) {
        calls++;
        this.gl20.glBindRenderbuffer(target, renderbuffer);
        check();
    }

    public void glBlendColor(float red, float green, float blue, float alpha) {
        calls++;
        this.gl20.glBlendColor(red, green, blue, alpha);
        check();
    }

    public void glBlendEquation(int mode) {
        calls++;
        this.gl20.glBlendEquation(mode);
        check();
    }

    public void glBlendEquationSeparate(int modeRGB, int modeAlpha) {
        calls++;
        this.gl20.glBlendEquationSeparate(modeRGB, modeAlpha);
        check();
    }

    public void glBlendFuncSeparate(int srcRGB, int dstRGB, int srcAlpha, int dstAlpha) {
        calls++;
        this.gl20.glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
        check();
    }

    public void glBufferData(int target, int size, Buffer data, int usage) {
        calls++;
        this.gl20.glBufferData(target, size, data, usage);
        check();
    }

    public void glBufferSubData(int target, int offset, int size, Buffer data) {
        calls++;
        this.gl20.glBufferSubData(target, offset, size, data);
        check();
    }

    public int glCheckFramebufferStatus(int target) {
        calls++;
        int result = this.gl20.glCheckFramebufferStatus(target);
        check();
        return result;
    }

    public void glCompileShader(int shader) {
        calls++;
        this.gl20.glCompileShader(shader);
        check();
    }

    public int glCreateProgram() {
        calls++;
        int result = this.gl20.glCreateProgram();
        check();
        return result;
    }

    public int glCreateShader(int type) {
        calls++;
        int result = this.gl20.glCreateShader(type);
        check();
        return result;
    }

    public void glDeleteBuffer(int buffer) {
        calls++;
        this.gl20.glDeleteBuffer(buffer);
        check();
    }

    public void glDeleteBuffers(int n, IntBuffer buffers) {
        calls++;
        this.gl20.glDeleteBuffers(n, buffers);
        check();
    }

    public void glDeleteFramebuffer(int framebuffer) {
        calls++;
        this.gl20.glDeleteFramebuffer(framebuffer);
        check();
    }

    public void glDeleteFramebuffers(int n, IntBuffer framebuffers) {
        calls++;
        this.gl20.glDeleteFramebuffers(n, framebuffers);
        check();
    }

    public void glDeleteProgram(int program) {
        calls++;
        this.gl20.glDeleteProgram(program);
        check();
    }

    public void glDeleteRenderbuffer(int renderbuffer) {
        calls++;
        this.gl20.glDeleteRenderbuffer(renderbuffer);
        check();
    }

    public void glDeleteRenderbuffers(int n, IntBuffer renderbuffers) {
        calls++;
        this.gl20.glDeleteRenderbuffers(n, renderbuffers);
        check();
    }

    public void glDeleteShader(int shader) {
        calls++;
        this.gl20.glDeleteShader(shader);
        check();
    }

    public void glDetachShader(int program, int shader) {
        calls++;
        this.gl20.glDetachShader(program, shader);
        check();
    }

    public void glDisableVertexAttribArray(int index) {
        calls++;
        this.gl20.glDisableVertexAttribArray(index);
        check();
    }

    public void glDrawElements(int mode, int count, int type, int indices) {
        vertexCount.put((float) count);
        drawCalls++;
        calls++;
        this.gl20.glDrawElements(mode, count, type, indices);
        check();
    }

    public void glEnableVertexAttribArray(int index) {
        calls++;
        this.gl20.glEnableVertexAttribArray(index);
        check();
    }

    public void glFramebufferRenderbuffer(int target, int attachment, int renderbuffertarget, int renderbuffer) {
        calls++;
        this.gl20.glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
        check();
    }

    public void glFramebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
        calls++;
        this.gl20.glFramebufferTexture2D(target, attachment, textarget, texture, level);
        check();
    }

    public int glGenBuffer() {
        calls++;
        int result = this.gl20.glGenBuffer();
        check();
        return result;
    }

    public void glGenBuffers(int n, IntBuffer buffers) {
        calls++;
        this.gl20.glGenBuffers(n, buffers);
        check();
    }

    public void glGenerateMipmap(int target) {
        calls++;
        this.gl20.glGenerateMipmap(target);
        check();
    }

    public int glGenFramebuffer() {
        calls++;
        int result = this.gl20.glGenFramebuffer();
        check();
        return result;
    }

    public void glGenFramebuffers(int n, IntBuffer framebuffers) {
        calls++;
        this.gl20.glGenFramebuffers(n, framebuffers);
        check();
    }

    public int glGenRenderbuffer() {
        calls++;
        int result = this.gl20.glGenRenderbuffer();
        check();
        return result;
    }

    public void glGenRenderbuffers(int n, IntBuffer renderbuffers) {
        calls++;
        this.gl20.glGenRenderbuffers(n, renderbuffers);
        check();
    }

    public String glGetActiveAttrib(int program, int index, IntBuffer size, Buffer type) {
        calls++;
        String result = this.gl20.glGetActiveAttrib(program, index, size, type);
        check();
        return result;
    }

    public String glGetActiveUniform(int program, int index, IntBuffer size, Buffer type) {
        calls++;
        String result = this.gl20.glGetActiveUniform(program, index, size, type);
        check();
        return result;
    }

    public void glGetAttachedShaders(int program, int maxcount, Buffer count, IntBuffer shaders) {
        calls++;
        this.gl20.glGetAttachedShaders(program, maxcount, count, shaders);
        check();
    }

    public int glGetAttribLocation(int program, String name) {
        calls++;
        int result = this.gl20.glGetAttribLocation(program, name);
        check();
        return result;
    }

    public void glGetBooleanv(int pname, Buffer params) {
        calls++;
        this.gl20.glGetBooleanv(pname, params);
        check();
    }

    public void glGetBufferParameteriv(int target, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetBufferParameteriv(target, pname, params);
        check();
    }

    public void glGetFloatv(int pname, FloatBuffer params) {
        calls++;
        this.gl20.glGetFloatv(pname, params);
        check();
    }

    public void glGetFramebufferAttachmentParameteriv(int target, int attachment, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
        check();
    }

    public void glGetProgramiv(int program, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetProgramiv(program, pname, params);
        check();
    }

    public String glGetProgramInfoLog(int program) {
        calls++;
        String result = this.gl20.glGetProgramInfoLog(program);
        check();
        return result;
    }

    public void glGetRenderbufferParameteriv(int target, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetRenderbufferParameteriv(target, pname, params);
        check();
    }

    public void glGetShaderiv(int shader, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetShaderiv(shader, pname, params);
        check();
    }

    public String glGetShaderInfoLog(int shader) {
        calls++;
        String result = this.gl20.glGetShaderInfoLog(shader);
        check();
        return result;
    }

    public void glGetShaderPrecisionFormat(int shadertype, int precisiontype, IntBuffer range, IntBuffer precision) {
        calls++;
        this.gl20.glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
        check();
    }

    public void glGetTexParameterfv(int target, int pname, FloatBuffer params) {
        calls++;
        this.gl20.glGetTexParameterfv(target, pname, params);
        check();
    }

    public void glGetTexParameteriv(int target, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetTexParameteriv(target, pname, params);
        check();
    }

    public void glGetUniformfv(int program, int location, FloatBuffer params) {
        calls++;
        this.gl20.glGetUniformfv(program, location, params);
        check();
    }

    public void glGetUniformiv(int program, int location, IntBuffer params) {
        calls++;
        this.gl20.glGetUniformiv(program, location, params);
        check();
    }

    public int glGetUniformLocation(int program, String name) {
        calls++;
        int result = this.gl20.glGetUniformLocation(program, name);
        check();
        return result;
    }

    public void glGetVertexAttribfv(int index, int pname, FloatBuffer params) {
        calls++;
        this.gl20.glGetVertexAttribfv(index, pname, params);
        check();
    }

    public void glGetVertexAttribiv(int index, int pname, IntBuffer params) {
        calls++;
        this.gl20.glGetVertexAttribiv(index, pname, params);
        check();
    }

    public void glGetVertexAttribPointerv(int index, int pname, Buffer pointer) {
        calls++;
        this.gl20.glGetVertexAttribPointerv(index, pname, pointer);
        check();
    }

    public boolean glIsBuffer(int buffer) {
        calls++;
        boolean result = this.gl20.glIsBuffer(buffer);
        check();
        return result;
    }

    public boolean glIsEnabled(int cap) {
        calls++;
        boolean result = this.gl20.glIsEnabled(cap);
        check();
        return result;
    }

    public boolean glIsFramebuffer(int framebuffer) {
        calls++;
        boolean result = this.gl20.glIsFramebuffer(framebuffer);
        check();
        return result;
    }

    public boolean glIsProgram(int program) {
        calls++;
        boolean result = this.gl20.glIsProgram(program);
        check();
        return result;
    }

    public boolean glIsRenderbuffer(int renderbuffer) {
        calls++;
        boolean result = this.gl20.glIsRenderbuffer(renderbuffer);
        check();
        return result;
    }

    public boolean glIsShader(int shader) {
        calls++;
        boolean result = this.gl20.glIsShader(shader);
        check();
        return result;
    }

    public boolean glIsTexture(int texture) {
        calls++;
        boolean result = this.gl20.glIsTexture(texture);
        check();
        return result;
    }

    public void glLinkProgram(int program) {
        calls++;
        this.gl20.glLinkProgram(program);
        check();
    }

    public void glReleaseShaderCompiler() {
        calls++;
        this.gl20.glReleaseShaderCompiler();
        check();
    }

    public void glRenderbufferStorage(int target, int internalformat, int width, int height) {
        calls++;
        this.gl20.glRenderbufferStorage(target, internalformat, width, height);
        check();
    }

    public void glSampleCoverage(float value, boolean invert) {
        calls++;
        this.gl20.glSampleCoverage(value, invert);
        check();
    }

    public void glShaderBinary(int n, IntBuffer shaders, int binaryformat, Buffer binary, int length) {
        calls++;
        this.gl20.glShaderBinary(n, shaders, binaryformat, binary, length);
        check();
    }

    public void glShaderSource(int shader, String string) {
        calls++;
        this.gl20.glShaderSource(shader, string);
        check();
    }

    public void glStencilFuncSeparate(int face, int func, int ref, int mask) {
        calls++;
        this.gl20.glStencilFuncSeparate(face, func, ref, mask);
        check();
    }

    public void glStencilMaskSeparate(int face, int mask) {
        calls++;
        this.gl20.glStencilMaskSeparate(face, mask);
        check();
    }

    public void glStencilOpSeparate(int face, int fail, int zfail, int zpass) {
        calls++;
        this.gl20.glStencilOpSeparate(face, fail, zfail, zpass);
        check();
    }

    public void glTexParameterfv(int target, int pname, FloatBuffer params) {
        calls++;
        this.gl20.glTexParameterfv(target, pname, params);
        check();
    }

    public void glTexParameteri(int target, int pname, int param) {
        calls++;
        this.gl20.glTexParameteri(target, pname, param);
        check();
    }

    public void glTexParameteriv(int target, int pname, IntBuffer params) {
        calls++;
        this.gl20.glTexParameteriv(target, pname, params);
        check();
    }

    public void glUniform1f(int location, float x) {
        calls++;
        this.gl20.glUniform1f(location, x);
        check();
    }

    public void glUniform1fv(int location, int count, FloatBuffer v) {
        calls++;
        this.gl20.glUniform1fv(location, count, v);
        check();
    }

    public void glUniform1fv(int location, int count, float[] v, int offset) {
        calls++;
        this.gl20.glUniform1fv(location, count, v, offset);
        check();
    }

    public void glUniform1i(int location, int x) {
        calls++;
        this.gl20.glUniform1i(location, x);
        check();
    }

    public void glUniform1iv(int location, int count, IntBuffer v) {
        calls++;
        this.gl20.glUniform1iv(location, count, v);
        check();
    }

    public void glUniform1iv(int location, int count, int[] v, int offset) {
        calls++;
        this.gl20.glUniform1iv(location, count, v, offset);
        check();
    }

    public void glUniform2f(int location, float x, float y) {
        calls++;
        this.gl20.glUniform2f(location, x, y);
        check();
    }

    public void glUniform2fv(int location, int count, FloatBuffer v) {
        calls++;
        this.gl20.glUniform2fv(location, count, v);
        check();
    }

    public void glUniform2fv(int location, int count, float[] v, int offset) {
        calls++;
        this.gl20.glUniform2fv(location, count, v, offset);
        check();
    }

    public void glUniform2i(int location, int x, int y) {
        calls++;
        this.gl20.glUniform2i(location, x, y);
        check();
    }

    public void glUniform2iv(int location, int count, IntBuffer v) {
        calls++;
        this.gl20.glUniform2iv(location, count, v);
        check();
    }

    public void glUniform2iv(int location, int count, int[] v, int offset) {
        calls++;
        this.gl20.glUniform2iv(location, count, v, offset);
        check();
    }

    public void glUniform3f(int location, float x, float y, float z) {
        calls++;
        this.gl20.glUniform3f(location, x, y, z);
        check();
    }

    public void glUniform3fv(int location, int count, FloatBuffer v) {
        calls++;
        this.gl20.glUniform3fv(location, count, v);
        check();
    }

    public void glUniform3fv(int location, int count, float[] v, int offset) {
        calls++;
        this.gl20.glUniform3fv(location, count, v, offset);
        check();
    }

    public void glUniform3i(int location, int x, int y, int z) {
        calls++;
        this.gl20.glUniform3i(location, x, y, z);
        check();
    }

    public void glUniform3iv(int location, int count, IntBuffer v) {
        calls++;
        this.gl20.glUniform3iv(location, count, v);
        check();
    }

    public void glUniform3iv(int location, int count, int[] v, int offset) {
        calls++;
        this.gl20.glUniform3iv(location, count, v, offset);
        check();
    }

    public void glUniform4f(int location, float x, float y, float z, float w) {
        calls++;
        this.gl20.glUniform4f(location, x, y, z, w);
        check();
    }

    public void glUniform4fv(int location, int count, FloatBuffer v) {
        calls++;
        this.gl20.glUniform4fv(location, count, v);
        check();
    }

    public void glUniform4fv(int location, int count, float[] v, int offset) {
        calls++;
        this.gl20.glUniform4fv(location, count, v, offset);
        check();
    }

    public void glUniform4i(int location, int x, int y, int z, int w) {
        calls++;
        this.gl20.glUniform4i(location, x, y, z, w);
        check();
    }

    public void glUniform4iv(int location, int count, IntBuffer v) {
        calls++;
        this.gl20.glUniform4iv(location, count, v);
        check();
    }

    public void glUniform4iv(int location, int count, int[] v, int offset) {
        calls++;
        this.gl20.glUniform4iv(location, count, v, offset);
        check();
    }

    public void glUniformMatrix2fv(int location, int count, boolean transpose, FloatBuffer value) {
        calls++;
        this.gl20.glUniformMatrix2fv(location, count, transpose, value);
        check();
    }

    public void glUniformMatrix2fv(int location, int count, boolean transpose, float[] value, int offset) {
        calls++;
        this.gl20.glUniformMatrix2fv(location, count, transpose, value, offset);
        check();
    }

    public void glUniformMatrix3fv(int location, int count, boolean transpose, FloatBuffer value) {
        calls++;
        this.gl20.glUniformMatrix3fv(location, count, transpose, value);
        check();
    }

    public void glUniformMatrix3fv(int location, int count, boolean transpose, float[] value, int offset) {
        calls++;
        this.gl20.glUniformMatrix3fv(location, count, transpose, value, offset);
        check();
    }

    public void glUniformMatrix4fv(int location, int count, boolean transpose, FloatBuffer value) {
        calls++;
        this.gl20.glUniformMatrix4fv(location, count, transpose, value);
        check();
    }

    public void glUniformMatrix4fv(int location, int count, boolean transpose, float[] value, int offset) {
        calls++;
        this.gl20.glUniformMatrix4fv(location, count, transpose, value, offset);
        check();
    }

    public void glUseProgram(int program) {
        shaderSwitches++;
        calls++;
        this.gl20.glUseProgram(program);
        check();
    }

    public void glValidateProgram(int program) {
        calls++;
        this.gl20.glValidateProgram(program);
        check();
    }

    public void glVertexAttrib1f(int indx, float x) {
        calls++;
        this.gl20.glVertexAttrib1f(indx, x);
        check();
    }

    public void glVertexAttrib1fv(int indx, FloatBuffer values) {
        calls++;
        this.gl20.glVertexAttrib1fv(indx, values);
        check();
    }

    public void glVertexAttrib2f(int indx, float x, float y) {
        calls++;
        this.gl20.glVertexAttrib2f(indx, x, y);
        check();
    }

    public void glVertexAttrib2fv(int indx, FloatBuffer values) {
        calls++;
        this.gl20.glVertexAttrib2fv(indx, values);
        check();
    }

    public void glVertexAttrib3f(int indx, float x, float y, float z) {
        calls++;
        this.gl20.glVertexAttrib3f(indx, x, y, z);
        check();
    }

    public void glVertexAttrib3fv(int indx, FloatBuffer values) {
        calls++;
        this.gl20.glVertexAttrib3fv(indx, values);
        check();
    }

    public void glVertexAttrib4f(int indx, float x, float y, float z, float w) {
        calls++;
        this.gl20.glVertexAttrib4f(indx, x, y, z, w);
        check();
    }

    public void glVertexAttrib4fv(int indx, FloatBuffer values) {
        calls++;
        this.gl20.glVertexAttrib4fv(indx, values);
        check();
    }

    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, Buffer ptr) {
        calls++;
        this.gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
        check();
    }

    public void glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int ptr) {
        calls++;
        this.gl20.glVertexAttribPointer(indx, size, type, normalized, stride, ptr);
        check();
    }
}
