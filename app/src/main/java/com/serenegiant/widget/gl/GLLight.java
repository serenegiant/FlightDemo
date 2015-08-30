package com.serenegiant.widget.gl;

import javax.microedition.khronos.opengles.GL10;

public abstract class GLLight {
	protected float[] ambient = { 0.2f, 0.2f, 0.2f, 1.0f };

	public abstract void enable(GL10 gl, int lightID);
	public abstract void disable(GL10 gl);
	
	public void setAmbient(float r, float g, float b, float a) {
		setFloat4(ambient, r, g, b, a);
	}
	
	protected void setFloat4(float[] c, float r, float g, float b, float a) {
		c[0] = r; 
		c[1] = g;
		c[2] = b;
		c[3] = a;
	}
}
