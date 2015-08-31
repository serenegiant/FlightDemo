package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;


public class DroneObjectBebopRandom extends DroneObjectBebop {

	public DroneObjectBebopRandom(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectBebopRandom(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
	}

	@Override
	public void update(float deltaTime) {
		super.update(deltaTime);
		angle.x += 200 * deltaTime;
		angle.y += 187 * deltaTime;
		angle.z += 89 * deltaTime;
	}

}
