package com.serenegiant.drone;

import android.util.Log;

import com.serenegiant.math.Vector;

public class SkyControllerObjectRandom extends SkyControllerObject {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = SkyControllerObjectRandom.class.getSimpleName();

	public SkyControllerObjectRandom(final Vector position, final float scale) {
		super(position, scale);
	}

	public SkyControllerObjectRandom(final float x, final float y, final float z, final float scale) {
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
