package com.badlogic.gdx.backends.android;

import com.badlogic.gdx.graphics.GL20;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class AndroidGL20 implements GL20 {
    private static native void init();

    public native void glActiveTexture(int i);

    public native void glAttachShader(int i, int i2);

    public native void glBindAttribLocation(int i, int i2, String str);

    public native void glBindBuffer(int i, int i2);

    public native void glBindFramebuffer(int i, int i2);

    public native void glBindRenderbuffer(int i, int i2);

    public native void glBindTexture(int i, int i2);

    public native void glBlendColor(float f, float f2, float f3, float f4);

    public native void glBlendEquation(int i);

    public native void glBlendEquationSeparate(int i, int i2);

    public native void glBlendFunc(int i, int i2);

    public native void glBlendFuncSeparate(int i, int i2, int i3, int i4);

    public native void glBufferData(int i, int i2, Buffer buffer, int i3);

    public native void glBufferSubData(int i, int i2, int i3, Buffer buffer);

    public native int glCheckFramebufferStatus(int i);

    public native void glClear(int i);

    public native void glClearColor(float f, float f2, float f3, float f4);

    public native void glClearDepthf(float f);

    public native void glClearStencil(int i);

    public native void glColorMask(boolean z, boolean z2, boolean z3, boolean z4);

    public native void glCompileShader(int i);

    public native void glCompressedTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, Buffer buffer);

    public native void glCompressedTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glCopyTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public native void glCopyTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8);

    public native int glCreateProgram();

    public native int glCreateShader(int i);

    public native void glCullFace(int i);

    public native void glDeleteBuffer(int i);

    public native void glDeleteBuffers(int i, IntBuffer intBuffer);

    public native void glDeleteFramebuffer(int i);

    public native void glDeleteFramebuffers(int i, IntBuffer intBuffer);

    public native void glDeleteProgram(int i);

    public native void glDeleteRenderbuffer(int i);

    public native void glDeleteRenderbuffers(int i, IntBuffer intBuffer);

    public native void glDeleteShader(int i);

    public native void glDeleteTexture(int i);

    public native void glDeleteTextures(int i, IntBuffer intBuffer);

    public native void glDepthFunc(int i);

    public native void glDepthMask(boolean z);

    public native void glDepthRangef(float f, float f2);

    public native void glDetachShader(int i, int i2);

    public native void glDisable(int i);

    public native void glDisableVertexAttribArray(int i);

    public native void glDrawArrays(int i, int i2, int i3);

    public native void glDrawElements(int i, int i2, int i3, int i4);

    public native void glDrawElements(int i, int i2, int i3, Buffer buffer);

    public native void glEnable(int i);

    public native void glEnableVertexAttribArray(int i);

    public native void glFinish();

    public native void glFlush();

    public native void glFramebufferRenderbuffer(int i, int i2, int i3, int i4);

    public native void glFramebufferTexture2D(int i, int i2, int i3, int i4, int i5);

    public native void glFrontFace(int i);

    public native int glGenBuffer();

    public native void glGenBuffers(int i, IntBuffer intBuffer);

    public native int glGenFramebuffer();

    public native void glGenFramebuffers(int i, IntBuffer intBuffer);

    public native int glGenRenderbuffer();

    public native void glGenRenderbuffers(int i, IntBuffer intBuffer);

    public native int glGenTexture();

    public native void glGenTextures(int i, IntBuffer intBuffer);

    public native void glGenerateMipmap(int i);

    public native String glGetActiveAttrib(int i, int i2, IntBuffer intBuffer, Buffer buffer);

    public native String glGetActiveUniform(int i, int i2, IntBuffer intBuffer, Buffer buffer);

    public native void glGetAttachedShaders(int i, int i2, Buffer buffer, IntBuffer intBuffer);

    public native int glGetAttribLocation(int i, String str);

    public native void glGetBooleanv(int i, Buffer buffer);

    public native void glGetBufferParameteriv(int i, int i2, IntBuffer intBuffer);

    public native int glGetError();

    public native void glGetFloatv(int i, FloatBuffer floatBuffer);

    public native void glGetFramebufferAttachmentParameteriv(int i, int i2, int i3, IntBuffer intBuffer);

    public native void glGetIntegerv(int i, IntBuffer intBuffer);

    public native String glGetProgramInfoLog(int i);

    public native void glGetProgramiv(int i, int i2, IntBuffer intBuffer);

    public native void glGetRenderbufferParameteriv(int i, int i2, IntBuffer intBuffer);

    public native String glGetShaderInfoLog(int i);

    public native void glGetShaderPrecisionFormat(int i, int i2, IntBuffer intBuffer, IntBuffer intBuffer2);

    public native void glGetShaderSource(int i, int i2, Buffer buffer, String str);

    public native void glGetShaderiv(int i, int i2, IntBuffer intBuffer);

    public native String glGetString(int i);

    public native void glGetTexParameterfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetTexParameteriv(int i, int i2, IntBuffer intBuffer);

    public native int glGetUniformLocation(int i, String str);

    public native void glGetUniformfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetUniformiv(int i, int i2, IntBuffer intBuffer);

    public native void glGetVertexAttribPointerv(int i, int i2, Buffer buffer);

    public native void glGetVertexAttribfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glGetVertexAttribiv(int i, int i2, IntBuffer intBuffer);

    public native void glHint(int i, int i2);

    public native boolean glIsBuffer(int i);

    public native boolean glIsEnabled(int i);

    public native boolean glIsFramebuffer(int i);

    public native boolean glIsProgram(int i);

    public native boolean glIsRenderbuffer(int i);

    public native boolean glIsShader(int i);

    public native boolean glIsTexture(int i);

    public native void glLineWidth(float f);

    public native void glLinkProgram(int i);

    public native void glPixelStorei(int i, int i2);

    public native void glPolygonOffset(float f, float f2);

    public native void glReadPixels(int i, int i2, int i3, int i4, int i5, int i6, Buffer buffer);

    public native void glReleaseShaderCompiler();

    public native void glRenderbufferStorage(int i, int i2, int i3, int i4);

    public native void glSampleCoverage(float f, boolean z);

    public native void glScissor(int i, int i2, int i3, int i4);

    public native void glShaderBinary(int i, IntBuffer intBuffer, int i2, Buffer buffer, int i3);

    public native void glShaderSource(int i, String str);

    public native void glStencilFunc(int i, int i2, int i3);

    public native void glStencilFuncSeparate(int i, int i2, int i3, int i4);

    public native void glStencilMask(int i);

    public native void glStencilMaskSeparate(int i, int i2);

    public native void glStencilOp(int i, int i2, int i3);

    public native void glStencilOpSeparate(int i, int i2, int i3, int i4);

    public native void glTexImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glTexParameterf(int i, int i2, float f);

    public native void glTexParameterfv(int i, int i2, FloatBuffer floatBuffer);

    public native void glTexParameteri(int i, int i2, int i3);

    public native void glTexParameteriv(int i, int i2, IntBuffer intBuffer);

    public native void glTexSubImage2D(int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Buffer buffer);

    public native void glUniform1f(int i, float f);

    public native void glUniform1fv(int i, int i2, FloatBuffer floatBuffer);

    public native void glUniform1fv(int i, int i2, float[] fArr, int i3);

    public native void glUniform1i(int i, int i2);

    public native void glUniform1iv(int i, int i2, IntBuffer intBuffer);

    public native void glUniform1iv(int i, int i2, int[] iArr, int i3);

    public native void glUniform2f(int i, float f, float f2);

    public native void glUniform2fv(int i, int i2, FloatBuffer floatBuffer);

    public native void glUniform2fv(int i, int i2, float[] fArr, int i3);

    public native void glUniform2i(int i, int i2, int i3);

    public native void glUniform2iv(int i, int i2, IntBuffer intBuffer);

    public native void glUniform2iv(int i, int i2, int[] iArr, int i3);

    public native void glUniform3f(int i, float f, float f2, float f3);

    public native void glUniform3fv(int i, int i2, FloatBuffer floatBuffer);

    public native void glUniform3fv(int i, int i2, float[] fArr, int i3);

    public native void glUniform3i(int i, int i2, int i3, int i4);

    public native void glUniform3iv(int i, int i2, IntBuffer intBuffer);

    public native void glUniform3iv(int i, int i2, int[] iArr, int i3);

    public native void glUniform4f(int i, float f, float f2, float f3, float f4);

    public native void glUniform4fv(int i, int i2, FloatBuffer floatBuffer);

    public native void glUniform4fv(int i, int i2, float[] fArr, int i3);

    public native void glUniform4i(int i, int i2, int i3, int i4, int i5);

    public native void glUniform4iv(int i, int i2, IntBuffer intBuffer);

    public native void glUniform4iv(int i, int i2, int[] iArr, int i3);

    public native void glUniformMatrix2fv(int i, int i2, boolean z, FloatBuffer floatBuffer);

    public native void glUniformMatrix2fv(int i, int i2, boolean z, float[] fArr, int i3);

    public native void glUniformMatrix3fv(int i, int i2, boolean z, FloatBuffer floatBuffer);

    public native void glUniformMatrix3fv(int i, int i2, boolean z, float[] fArr, int i3);

    public native void glUniformMatrix4fv(int i, int i2, boolean z, FloatBuffer floatBuffer);

    public native void glUniformMatrix4fv(int i, int i2, boolean z, float[] fArr, int i3);

    public native void glUseProgram(int i);

    public native void glValidateProgram(int i);

    public native void glVertexAttrib1f(int i, float f);

    public native void glVertexAttrib1fv(int i, FloatBuffer floatBuffer);

    public native void glVertexAttrib2f(int i, float f, float f2);

    public native void glVertexAttrib2fv(int i, FloatBuffer floatBuffer);

    public native void glVertexAttrib3f(int i, float f, float f2, float f3);

    public native void glVertexAttrib3fv(int i, FloatBuffer floatBuffer);

    public native void glVertexAttrib4f(int i, float f, float f2, float f3, float f4);

    public native void glVertexAttrib4fv(int i, FloatBuffer floatBuffer);

    public native void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, int i5);

    public native void glVertexAttribPointer(int i, int i2, int i3, boolean z, int i4, Buffer buffer);

    public native void glViewport(int i, int i2, int i3, int i4);

    static {
        System.loadLibrary("gdx");
        init();
    }
}
