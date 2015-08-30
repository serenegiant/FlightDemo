package com.serenegiant.widget.gl;

import javax.microedition.khronos.opengles.GL10;

public class GLMaterial extends GLPointLight {

	public GLMaterial() {
		super();
		setFloat4(ambient, 1f, 1f, 1f, 1f); 
	}

	@Override
	public void enable(GL10 gl, int lightID) {
		enable(gl);
	}

	public void enable(GL10 gl) {
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, ambient, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, diffuse, 0);
		gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, specular, 0);
	}

	@Override
	public void disable(GL10 gl) {
	
	}
}
