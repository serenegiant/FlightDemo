package com.serenegiant.widget.gl;

import com.serenegiant.math.CylinderBounds;
import com.serenegiant.math.Vector;

public abstract class DroneObject extends DynamicGameObject {

	protected final RotorObject mFrontLeftRotorObj;	// ローター左前
	protected final RotorObject mFrontRightRotorObj;// ローター右前
	protected final RotorObject mRearLeftRotorObj;	// ローター左後
	protected final RotorObject mRearRightRotorObj;	// ローター右後

	public DroneObject(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObject(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
		setBounds(new CylinderBounds(x, y, z, 5, scale));	// TODO height/radiusが適当
		mFrontLeftRotorObj = new RotorObject(0, 0, 0, true);
		mFrontRightRotorObj = new RotorObject(0, 0, 0, false);
		mRearLeftRotorObj = new RotorObject(0, 0, 0, false);
		mRearRightRotorObj = new RotorObject(0, 0, 0, true);
	}

	protected abstract Vector getOffset();
}