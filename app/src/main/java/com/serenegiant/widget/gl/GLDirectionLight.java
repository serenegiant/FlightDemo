package com.serenegiant.widget.gl;

public class GLDirectionLight extends GLPointLight {
	
	public GLDirectionLight() {
		super();
		setFloat4(position, 0, 0, -1, 0);	// direction
	}

	public GLDirectionLight(float x, float y, float z) {
		super();
		setFloat4(position, -x, -y, -z, 0);	// direction
	}

	public void setPosition(float x, float y, float z) {
		setFloat4(position, x, y, z, 0);	// direction
	}
	
	public void setDirection(float x, float y, float z) {
		setFloat4(position, -x, -y, -z, 0);	// direction
	}

}
