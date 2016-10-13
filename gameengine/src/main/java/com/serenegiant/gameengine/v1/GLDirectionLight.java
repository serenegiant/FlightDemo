package com.serenegiant.gameengine.v1;

public class GLDirectionLight extends GLPointLight {
	
	public GLDirectionLight() {
		super();
		setFloat4(position, 0, 0, -1, 0);	// direction
	}

	public GLDirectionLight(final float x, final float y, final float z) {
		super();
		setFloat4(position, -x, -y, -z, 0);	// direction
	}

	public void setPosition(final float x, final float y, final float z) {
		setFloat4(position, x, y, z, 0);	// direction
	}
	
	public void setDirection(final float x, final float y, final float z) {
		setFloat4(position, -x, -y, -z, 0);	// direction
	}

}
