package com.serenegiant.arflight.drone;

import com.serenegiant.math.Vector;


public class DroneObjectCargodrone extends DroneObject {

	private static final Vector OFFSET = new Vector(0, 1.7f, 0);

	public DroneObjectCargodrone(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectCargodrone(final float x, final float y, final float z, final float scale) {
		super(OFFSET.x + x, OFFSET.y + y, OFFSET.z + z, scale);
		mGuardObject.setPosition(OFFSET.x + x + 0, OFFSET.y + y, OFFSET.z + z + 0);
		mFrontLeftRotorObj.setPosition(OFFSET.x + x + 1.8000f, OFFSET.y + y - 0.19459f, OFFSET.z + z + 1.8000f);
		mFrontRightRotorObj.setPosition(OFFSET.x + x - 1.8000f, OFFSET.y + y - 0.19459f, OFFSET.z + z + 1.8000f);
		mRearLeftRotorObj.setPosition(OFFSET.x + x + 1.8000f, OFFSET.y + y - 0.19459f, OFFSET.z + z - 1.8000f);
		mRearRightRotorObj.setPosition(OFFSET.x + x - 1.8000f, OFFSET.y + y - 0.19459f, OFFSET.z + z - 1.8000f);
	}

	@Override
	public void update(final float deltaTime) {
		mFrontLeftRotorObj.update(deltaTime);
		mFrontRightRotorObj.update(deltaTime);
		mRearLeftRotorObj.update(deltaTime);
		mRearRightRotorObj.update(deltaTime);
	}

	@Override
	protected Vector getOffset() {
		return OFFSET;
	}

}
