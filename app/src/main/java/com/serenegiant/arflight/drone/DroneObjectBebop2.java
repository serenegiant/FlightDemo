package com.serenegiant.arflight.drone;

import com.serenegiant.math.Vector;

public class DroneObjectBebop2 extends DroneObject {

	private static final Vector OFFSET = new Vector(0, 1.7f, 0);

	public DroneObjectBebop2(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectBebop2(final float x, final float y, final float z, final float scale) {
		super(OFFSET.x + x, OFFSET.y + y, OFFSET.z + z, scale);
		mGuardObject.setPosition(OFFSET.x + x + 0, OFFSET.y + y, OFFSET.z + z + 0);
		mFrontLeftRotorObj.setPosition(OFFSET.x + x + 2.51744f, OFFSET.y + y, OFFSET.z + z + 2.06648f);
		mFrontRightRotorObj.setPosition(OFFSET.x + x + -2.51744f, OFFSET.y + y, OFFSET.z + z + 2.06648f);
		mRearLeftRotorObj.setPosition(OFFSET.x + x + 2.51744f, OFFSET.y + y, OFFSET.z + z - 1.77092f);
		mRearRightRotorObj.setPosition(OFFSET.x + x + -2.51744f, OFFSET.y + y, OFFSET.z + z - 1.77092f);
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
