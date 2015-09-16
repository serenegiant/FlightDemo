package com.serenegiant.gl;

import javax.microedition.khronos.opengles.GL10;

public abstract class GLLight {
	protected final float[] ambient = { 0.2f, 0.2f, 0.2f, 1.0f };

	public abstract void enable(final GL10 gl, final int lightID);
	public abstract void disable(final GL10 gl);
	
	public void setAmbient(final float r, final float g, final float b, final float a) {
		setFloat4(ambient, r, g, b, a);
	}
	
	protected void setFloat4(final float[] c, final float r, final float g, final float b, final float a) {
		c[0] = r; 
		c[1] = g;
		c[2] = b;
		c[3] = a;
	}
}
