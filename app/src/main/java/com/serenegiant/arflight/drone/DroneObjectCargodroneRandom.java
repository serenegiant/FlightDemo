package com.serenegiant.arflight.drone;

import com.serenegiant.math.Vector;

public class DroneObjectCargodroneRandom extends DroneObjectCargodrone {

	public DroneObjectCargodroneRandom(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectCargodroneRandom(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		angle.x += 200 * deltaTime;
		angle.y += 187 * deltaTime;
		angle.z += 89 * deltaTime;
//		angle.x += 20 * deltaTime;
//		angle.y += 18 * deltaTime;
//		angle.z += 9 * deltaTime;
	}

}
