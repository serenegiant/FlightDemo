package com.serenegiant.widget.gl;

import com.serenegiant.glutils.GLHelper;

import javax.microedition.khronos.opengles.GL10;

public class GLAmbientLight extends GLLight {
	
	public GLAmbientLight() {
		super();
	}

	public GLAmbientLight(float r, float g, float b, float a) {
		super();
		setAmbient(r, g, b, a);
	}
	
	@Override
	public void enable(GL10 gl, int lightID) {
		enable(gl);		
	}
	
	public void enable(GL10 gl) {
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, ambient, 0);
		GLHelper.checkGlError(gl, "GLAmbientLight#glLightModelfv");
	}
	
	@Override
	public void disable(GL10 gl) {
		
	}
	
	public float[] getAmbient() {
		return ambient;
	}
}
