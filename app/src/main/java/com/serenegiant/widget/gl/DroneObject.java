package com.serenegiant.widget.gl;

import com.serenegiant.math.CylinderBounds;
import com.serenegiant.math.Vector;

public class DroneObject extends DynamicGameObject {

	public DroneObject(final Vector position, final float radius) {
		this(position.x, position.y, position.z, radius);
	}

	public DroneObject(final float x, final float y, final float z, final float radius) {
		super(x, y, z, radius);
		setBounds(new CylinderBounds(x, y, z, 5, radius));	// TODO height/radiusが適当
	}

	@Override
	public void update(final float deltaTime) {
	}

	public void update(final float deltaTime, final Vector force) {
		// 境界位置の更新
		bounds.position.set(position);
	}
}