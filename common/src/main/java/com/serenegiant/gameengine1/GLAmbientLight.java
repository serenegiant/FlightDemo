package com.serenegiant.gameengine1;

import com.serenegiant.glutils.GLHelper;

import javax.microedition.khronos.opengles.GL10;

public class GLAmbientLight extends GLLight {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	
	public GLAmbientLight() {
		super();
	}

	public GLAmbientLight(final float r, final float g, final float b, final float a) {
		super();
		setAmbient(r, g, b, a);
	}
	
	@Override
	public void enable(final GL10 gl, final int lightID) {
		enable(gl);		
	}
	
	public void enable(final GL10 gl) {
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, ambient, 0);
		if (DEBUG) GLHelper.checkGlError(gl, "GLAmbientLight#glLightModelfv");
	}
	
	@Override
	public void disable(final GL10 gl) {
		
	}
	
	public float[] getAmbient() {
		return ambient;
	}
}
