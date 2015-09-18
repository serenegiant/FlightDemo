package com.serenegiant.gl;

import com.serenegiant.math.Vector;



public class DroneObjectBebop extends DroneObject {

	private static final Vector OFFSET = new Vector(0, 1.7f, 0);

	public DroneObjectBebop(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectBebop(final float x, final float y, final float z, final float scale) {
		super(OFFSET.x + x, OFFSET.y + y, OFFSET.z + z, scale);
		mGuardObject.setPosition(OFFSET.x + x + 0, OFFSET.y + y, OFFSET.z + z + 0);
		mFrontLeftRotorObj.setPosition(OFFSET.x + x + 2.28305f, OFFSET.y + y, OFFSET.z + z + 2.06648f);
		mFrontRightRotorObj.setPosition(OFFSET.x + x + -2.28305f, OFFSET.y + y, OFFSET.z + z + 2.06648f);
		mRearLeftRotorObj.setPosition(OFFSET.x + x + 2.27451f, OFFSET.y + y, OFFSET.z + z - 1.58124f);
		mRearRightRotorObj.setPosition(OFFSET.x + x + -2.27451f, OFFSET.y + y, OFFSET.z + z - 1.58124f);
	}

	@Override
	public void update(final float deltaTime) {
		mGuardObject.update(deltaTime);
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
