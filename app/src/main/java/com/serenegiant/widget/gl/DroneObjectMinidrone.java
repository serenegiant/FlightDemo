package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;


public class DroneObjectMinidrone extends DroneObject {

	private static final Vector OFFSET = new Vector(0, 1.7f, 0);

	public DroneObjectMinidrone(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectMinidrone(final float x, final float y, final float z, final float scale) {
		super(OFFSET.x + x, OFFSET.y + y, OFFSET.z + z, scale);
		mFrontLeftRotorObj.setPosition(OFFSET.x + x + 3.33891f, OFFSET.y + y - 0.19459f, OFFSET.z + z + 3.26455f);
		mFrontRightRotorObj.setPosition(OFFSET.x + x -3.33891f, OFFSET.y + y - 0.19459f, OFFSET.z + z + 3.26455f);
		mRearLeftRotorObj.setPosition(OFFSET.x + x + 3.36173f, OFFSET.y + y - 0.19459f, OFFSET.z + z - 3.46190f);
		mRearRightRotorObj.setPosition(OFFSET.x + x -3.36173f, OFFSET.y + y - 0.19459f, OFFSET.z + z - 3.46190f);
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
