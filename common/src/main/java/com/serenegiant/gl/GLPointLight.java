package com.serenegiant.gl;

import javax.microedition.khronos.opengles.GL10;

public class GLPointLight extends GLLight {
	protected final float[] diffuse = { 1f, 1f, 1f, 1f };			// 拡散光=白色
	protected final float[] specular = { 0.8f, 0.8f, 0.8f, 1f };	// 鏡面色=白色
	protected final float[] position = { 0, 0, 0, 1 };				// 最後の要素の1は点光源を意味する
	protected int lastLightID = 0;
	
	public void setAmbient(final GLAmbientLight amb) {
		final float[] c = amb.getAmbient();
		setFloat4(ambient, c[0], c[1], c[2], c[3]); 
	}

	public void setDiffuse(final float r, final float g, final float b, final float a) {
		setFloat4(diffuse, r, g, b, a); 
	}

	public void setSpecular(final float r, final float g, final float b, final float a) {
		setFloat4(specular, r, g, b, a); 
	}
	
	public void setPosition(final float x, final float y, final float z) {
		setFloat4(position, x, y, z, 1);
	}

	@Override
	public void enable(final GL10 gl, final int lightID) {
		gl.glEnable(lightID);
		gl.glLightfv(lightID, GL10.GL_AMBIENT, ambient, 0);
		gl.glLightfv(lightID, GL10.GL_DIFFUSE, diffuse, 0);
		gl.glLightfv(lightID, GL10.GL_SPECULAR, specular, 0);
		gl.glLightfv(lightID, GL10.GL_POSITION, position, 0);
		this.lastLightID = lightID;
	}
	
	@Override
	public void disable(final GL10 gl) {
		gl.glDisable(lastLightID);
	}

}
