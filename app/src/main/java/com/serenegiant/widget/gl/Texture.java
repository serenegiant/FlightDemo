package com.serenegiant.widget.gl;

import android.opengl.Matrix;

import com.serenegiant.glutils.GLHelper;

import javax.microedition.khronos.opengles.GL10;

public abstract class Texture {
	public static final int GL_TEXTURE_EXTERNAL_OES	= 0x8D65;
	public static final int GL_TEXTURE_2D           = 0x0DE1;	// = GL10.GL_TEXTURE_2D

	public int width;
	public int height;

	protected final GLGraphics glGraphics;
	protected final int mTexTarget;
	protected int textureID;
	protected int mMinFilter;
	protected int mMagFilter;
	protected int mWrapMode = GL10.GL_REPEAT;
	protected final float[] mTexMatrix = new float[16];

	public Texture(final IModelView modelView) {
		this(modelView, GL10.GL_TEXTURE_2D);
	}

	public Texture(final IModelView modelView, final int target) {
		glGraphics = modelView.getGLGraphics();
		mTexTarget = target;
		Matrix.setIdentityM(mTexMatrix, 0);
	}

	public void setFilters(final int minFilter, final int magFilter) {
		final GL10 gl = glGraphics.getGL();
		mMinFilter = minFilter;
		mMagFilter = magFilter;
		gl.glTexParameterf(mTexTarget, GL10.GL_TEXTURE_MIN_FILTER, minFilter);
		gl.glTexParameterf(mTexTarget, GL10.GL_TEXTURE_MAG_FILTER, magFilter);
		GLHelper.checkGlError(gl, "Texture#glTexParameterf");
	}

	/**
	 * テクスチャの繰り返しを指定
	 * @param wrap_mode GL_REPEAT(初期値)/GL_CLAMP_TO_EDGE/
	 */
	public void setWrap(final int wrap_mode) {
		final GL10 gl = glGraphics.getGL();
		mWrapMode = wrap_mode;
		gl.glTexParameterf(mTexTarget, GL10.GL_TEXTURE_WRAP_S, wrap_mode);
		gl.glTexParameterf(mTexTarget, GL10.GL_TEXTURE_WRAP_T, wrap_mode);
	}

	public void bind() {
		final GL10 gl = glGraphics.getGL();
		gl.glEnable(mTexTarget);
		gl.glBindTexture(mTexTarget, textureID);
	}

	public void unbind() {
		final GL10 gl = glGraphics.getGL();
		gl.glBindTexture(mTexTarget, 0);
		gl.glDisable(mTexTarget);
	}

	public float[] texMatrix() {
		return mTexMatrix;
	}

	public int target() {
		return mTexTarget;
	}

	public void release() {
		final GL10 gl = glGraphics.getGL();
		gl.glBindTexture(mTexTarget, 0);
		final int[] textureIDs = { textureID };
		gl.glDeleteTextures(1, textureIDs, 0);
	}

}
