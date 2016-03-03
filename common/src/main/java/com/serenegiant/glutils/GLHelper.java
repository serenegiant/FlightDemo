package com.serenegiant.glutils;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: GLHelper.java
 *
*/

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import android.util.Log;

import com.serenegiant.utils.BuildCheck;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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
	 * テクスチャ名を生成
	 * @param texTarget
	 * @param filter_param テクスチャの補完方法を指定, min/mag共に同じ値, GL_LINEARとかGL_NEAREST
	 * @return
	 */
	public static int initTex(final int texTarget, final int filter_param) {
		return initTex(texTarget, filter_param, filter_param, GLES20.GL_CLAMP_TO_EDGE);
	}

	/**
	 * テクスチャ名を生成(GL_TEXTURE0のみ)
	 * @param texTarget
	 * @param min_filter テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 * @param mag_filter テクスチャの補間方法を指定 GL_LINEARとかGL_NEAREST
	 * @return
	 */
	public static int initTex(final int texTarget, final int min_filter, final int mag_filter, final int wrap) {
//		if (DEBUG) Log.v(TAG, "initTex:target=" + texTarget);
		final int[] tex = new int[1];
		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glGenTextures(1, tex, 0);
		GLES20.glBindTexture(texTarget, tex[0]);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MIN_FILTER, min_filter);
		GLES20.glTexParameteri(texTarget, GLES20.GL_TEXTURE_MAG_FILTER, mag_filter);
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

	public static int loadTextureFromResource(final Context context, final int resId) {
		// Create an empty, mutable bitmap
		final Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		// get a canvas to paint over the bitmap
		final Canvas canvas = new Canvas(bitmap);
		canvas.drawARGB(0,0,255,0);

		// get a background image from resources
		// note the image format must match the bitmap format
		final Drawable background = context.getResources().getDrawable(resId);
		background.setBounds(0, 0, 256, 256);
		background.draw(canvas); // draw the background to our bitmap

		final int[] textures = new int[1];

		//Generate one texture pointer...
		GLES20.glGenTextures(1, textures, 0);
		//...and bind it to our array
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

		//Create Nearest Filtered Texture
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

		//Different possible texture parameters, e.g. GLES20.GL_CLAMP_TO_EDGE
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

		//Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		//Clean up
		bitmap.recycle();

		return textures[0];
	}

	public static int createTextureWithTextContent (final String text) {
		// Create an empty, mutable bitmap
		final Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
		// get a canvas to paint over the bitmap
		final Canvas canvas = new Canvas(bitmap);
		canvas.drawARGB(0,0,255,0);

		// Draw the text
		final Paint textPaint = new Paint();
		textPaint.setTextSize(32);
		textPaint.setAntiAlias(true);
		textPaint.setARGB(0xff, 0xff, 0xff, 0xff);
		// draw the text centered
		canvas.drawText(text, 16, 112, textPaint);

		final int texture = initTex(GLES20.GL_TEXTURE_2D, GLES20.GL_NEAREST, GLES20.GL_LINEAR, GLES20.GL_REPEAT);

		// Alpha blending
		// GLES20.glEnable(GLES20.GL_BLEND);
		// GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

		// Use the Android GLUtils to specify a two-dimensional texture image from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
		// Clean up
		bitmap.recycle();

		return texture;
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
		final int vs = loadShader(GLES20.GL_VERTEX_SHADER, vss);
		if (vs == 0) {
			return 0;
		}
		// フラグメントシェーダーをコンパイル
		int fs = loadShader(GLES20.GL_FRAGMENT_SHADER, fss);
		if (fs == 0) {
			return 0;
		}
		// リンク
		final int program = GLES20.glCreateProgram();
		checkGlError("glCreateProgram");
		if (program == 0) {
			Log.e(TAG, "Could not create program");
		}
		GLES20.glAttachShader(program, vs);
		checkGlError("glAttachShader");
		GLES20.glAttachShader(program, fs);
		checkGlError("glAttachShader");
		GLES20.glLinkProgram(program);
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
		if (linkStatus[0] != GLES20.GL_TRUE) {
			Log.e(TAG, "Could not link program: ");
			Log.e(TAG, GLES20.glGetProgramInfoLog(program));
			GLES20.glDeleteProgram(program);
			return 0;
		}
		return program;
	}

	/**
	  * Compiles the provided shader source.
	  *
	  * @return A handle to the shader, or 0 on failure.
	  */
	public static int loadShader(final int shaderType, final String source) {
		int shader = GLES20.glCreateShader(shaderType);
		checkGlError("glCreateShader type=" + shaderType);
		GLES20.glShaderSource(shader, source);
		GLES20.glCompileShader(shader);
		final int[] compiled = new int[1];
		GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
		if (compiled[0] == 0) {
			Log.e(TAG, "Could not compile shader " + shaderType + ":");
			Log.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
			GLES20.glDeleteShader(shader);
			shader = 0;
		}
		return shader;
	 }

	private static final int SIZEOF_FLOAT = 4;
	/**
	 * Allocates a direct float buffer, and populates it with the float array data.
	 */
	public static FloatBuffer createFloatBuffer(final float[] coords) {
		// Allocate a direct ByteBuffer, using 4 bytes per float, and copy coords into it.
		final ByteBuffer bb = ByteBuffer.allocateDirect(coords.length * SIZEOF_FLOAT);
		bb.order(ByteOrder.nativeOrder());
		final FloatBuffer fb = bb.asFloatBuffer();
		fb.put(coords);
		fb.position(0);
		return fb;
	}

	/**
	 * Checks to see if the location we obtained is valid.  GLES returns -1 if a label
	 * could not be found, but does not set the GL error.
	 * <p>
	 * Throws a RuntimeException if the location is invalid.
	 */
	public static void checkLocation(final int location, final String label) {
		if (location < 0) {
			throw new RuntimeException("Unable to locate '" + label + "' in program");
		}
	}

	/**
	 * Writes GL version info to the log.
	 */
	public static void logVersionInfo() {
		Log.i(TAG, "vendor  : " + GLES20.glGetString(GLES20.GL_VENDOR));
		Log.i(TAG, "renderer: " + GLES20.glGetString(GLES20.GL_RENDERER));
		Log.i(TAG, "version : " + GLES20.glGetString(GLES20.GL_VERSION));

		if (BuildCheck.isAndroid4_3()) {
			final int[] values = new int[1];
			GLES30.glGetIntegerv(GLES30.GL_MAJOR_VERSION, values, 0);
			final int majorVersion = values[0];
			GLES30.glGetIntegerv(GLES30.GL_MINOR_VERSION, values, 0);
			final int minorVersion = values[0];
			if (GLES30.glGetError() == GLES30.GL_NO_ERROR) {
				Log.i(TAG, "iversion: " + majorVersion + "." + minorVersion);
			}
		}
	}
}
