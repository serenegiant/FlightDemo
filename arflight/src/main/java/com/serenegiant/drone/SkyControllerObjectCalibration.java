package com.serenegiant.drone;

import android.util.Log;

import com.serenegiant.math.Vector;

public class SkyControllerObjectCalibration extends SkyControllerObject implements ICalibrationModelObject {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = SkyControllerObjectCalibration.class.getSimpleName();

	private static final float ROTATION_SPEED = 180.0f;
	private int mAxis = -1;
	private volatile int mRequestAxis = -1;
	private float mDirection = ROTATION_SPEED;

	public SkyControllerObjectCalibration(final Vector position, final float scale) {
		super(position, scale);
	}

	public SkyControllerObjectCalibration(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
	}

	@Override
	public void update(final float deltaTime) {
		super.update(deltaTime);
		final int requestAxis = mRequestAxis;
		if (mAxis != requestAxis) {
			mAxis = requestAxis;
			angle.clear(0);
			mDirection = ROTATION_SPEED;
		}
		switch (mAxis) {
		case 0:	// roll
			angle.z -= deltaTime * mDirection;
			break;
		case 1:	// pitch
			angle.x -= deltaTime * mDirection;
			break;
		case 2:	// yaw
			angle.y += deltaTime * mDirection;
			break;
		}
	}

	@Override
	public void setAxis(final int axis) {
		if (DEBUG) Log.v(TAG, "setAxis:" + axis);
		mRequestAxis = axis;
	}
}
