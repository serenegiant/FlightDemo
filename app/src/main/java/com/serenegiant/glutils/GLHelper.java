package com.serenegiant.glutils;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: GLHelper.java
 *
*/

import android.opengl.GLES10;
import android.opengl.GLES20;
import android.util.Log;

import javax.microedition.khronos.opengles.GL10;

/**
 * OpenGL|ES用のヘルパークラス
 */
public final class GLHelper {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "GLHelper";

	/**
	 * OepnGL|ESのエラーをチェックしてlogCatに出力する
	 * @param op
	 */
    public static void checkGlError(final String op) {
        final int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            final String msg = op + ": glError 0x" + Integer.toHexString(error);
//			Log.e(TAG, msg);
            new Throwable(msg).printStackTrace();
//         	if (DEBUG) {
//	            throw new RuntimeException(msg);
//       	}
        }
    }

	public static void checkGlError(final GL10 gl, final String op) {
		final int error = gl.glGetError();
		if (error != GL10.GL_NO_ERROR) {
			final String msg = op + ": glError 0x" + Integer.toHexString(error);
//			Log.e(TAG, msg);
			new Throwable(msg).printStackTrace();
//         	if (DEBUG) {
//	            throw new RuntimeException(msg);
//       	}
		}
	}

	/**
	 * テクスチャ名を生成(GL_TEXTURE0のみ)
	 * @param texTarget
	 * @param filter_param テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 * @return
	 */
	public static int initTex(final int texTarget, final int filter_param) {
//		if (DEBUG) Log.v(TAG, "initTex:target=" + texTarget);
		final int[] tex = new int[1];
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glGenTextures(1, tex, 0);
		GLES20.glBindTexture(texTarget, tex[0]);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MIN_FILTER, filter_param);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MAG_FILTER, filter_param);
		return tex[0];
	}

	/**
	 * テクスチャ名を生成(GL_TEXTURE0のみ)
	 * @param gl
	 * @param texTarget
	 * @param filter_param テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 * @return
	 */
	public static int initTex(final GL10 gl, final int texTarget, final int filter_param) {
//		if (DEBUG) Log.v(TAG, "initTex:target=" + texTarget);
		final int[] tex = new int[1];
		gl.glActiveTexture(GLES20.GL_TEXTURE0);
		gl.glGenTextures(1, tex, 0);
		gl.glBindTexture(texTarget, tex[0]);
		gl.glTexParameterx(texTarget, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(texTarget, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
		gl.glTexParameterx(texTarget, GL10.GL_TEXTURE_MIN_FILTER, filter_param);
		gl.glTexParameterx(texTarget, GL10.GL_TEXTURE_MAG_FILTER, filter_param);
		return tex[0];
	}

	/**
	 * delete specific texture
	 */
	public static void deleteTex(final int hTex) {
//		if (DEBUG) Log.v(TAG, "deleteTex:");
		final int[] tex = new int[] {hTex};
		GLES20.glDeleteTextures(1, tex, 0);
	}

	/**
	 * delete specific texture
	 */
	public static void deleteTex(final GL10 gl, final int hTex) {
//		if (DEBUG) Log.v(TAG, "deleteTex:");
		final int[] tex = new int[] {hTex};
		gl.glDeleteTextures(1, tex, 0);
	}

	/**
	 * load, compile and link shader
	 * @param vss source of vertex shader
	 * @param fss source of fragment shader
	 * @return
	 */
	public static int loadShader(final String vss, final String fss) {
//		if (DEBUG) Log.v(TAG, "loadShader:");
		final int[] compiled = new int[1];
		// 頂点シェーダーをコンパイル
		int vs = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
		GLES20.glShaderSource(vs, vss);
		GLES20.glCompileShader(vs);
		GLES20.glGetShaderiv(vs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(TAG, "Failed to compile vertex shader:" + GLES20.glGetShaderInfoLog(vs));
			GLES20.glDeleteShader(vs);
			vs = 0;
		}
		// フラグメントシェーダーをコンパイル
		int fs = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
		GLES20.glShaderSource(fs, fss);
		GLES20.glCompileShader(fs);
		GLES20.glGetShaderiv(fs, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(TAG, "Failed to compile fragment shader:" + GLES20.glGetShaderInfoLog(fs));
			GLES20.glDeleteShader(fs);
			fs = 0;
		}
		// リンク
		final int program = GLES20.glCreateProgram();
		GLES20.glAttachShader(program, vs);
		GLES20.glAttachShader(program, fs);
		GLES20.glLinkProgram(program);

		return program;
	}

}
