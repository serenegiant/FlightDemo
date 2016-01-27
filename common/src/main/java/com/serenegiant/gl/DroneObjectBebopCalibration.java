package com.serenegiant.gl;

import android.util.Log;

import com.serenegiant.math.Vector;


public class DroneObjectBebopCalibration extends DroneObjectBebop implements ICalibrationModelObject {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = DroneObjectBebopCalibration.class.getSimpleName();

	private static final float ROTATION_SPEED = 180.0f;
	private int mAxis = -1;
	private volatile int mRequestAxis = -1;
	private float mDirection = ROTATION_SPEED;

	public DroneObjectBebopCalibration(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectBebopCalibration(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
		stopEngine();
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
	public void setAxis(int axis) {
		if (DEBUG) Log.v(TAG, "setAxis:" + axis);
		mRequestAxis = axis;
	}
}
