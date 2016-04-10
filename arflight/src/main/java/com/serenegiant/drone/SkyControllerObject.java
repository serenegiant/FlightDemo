package com.serenegiant.drone;

import com.serenegiant.math.CylinderBounds;
import com.serenegiant.math.Vector;

public class SkyControllerObject extends DroneObject {

	private static final Vector OFFSET = new Vector(0, 1.7f, 0);

	public SkyControllerObject(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	@Override
	public void update(final float deltaTime) {
	}

	public SkyControllerObject(final float x, final float y, final float z, final float scale) {
		super(OFFSET.x + x, OFFSET.y + y, OFFSET.z + z, scale);
		setBounds(new CylinderBounds(x, y, z, 5, scale));	// TODO height/radiusが適当
	}

	public void startEngine() {
	}

	public void stopEngine() {
	}

	public void setRotorSpeed(final float speed) {
	}

	protected Vector getOffset() {
		return OFFSET;
	}
}
