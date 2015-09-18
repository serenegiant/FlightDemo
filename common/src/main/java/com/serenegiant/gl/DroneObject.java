package com.serenegiant.gl;

import com.serenegiant.math.CylinderBounds;
import com.serenegiant.math.Vector;

public abstract class DroneObject extends DynamicGameObject {

	protected final DynamicGameObject mGuardObject;
	protected final RotorObject mFrontLeftRotorObj;	// ローター左前
	protected final RotorObject mFrontRightRotorObj;// ローター右前
	protected final RotorObject mRearLeftRotorObj;	// ローター左後
	protected final RotorObject mRearRightRotorObj;	// ローター右後
	protected float mRotorSpeed = RotorObject.DEFAULT_SPEED;

	public DroneObject(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObject(final float x, final float y, final float z, final float scale) {
		super(x, y, z, scale);
		setBounds(new CylinderBounds(x, y, z, 5, scale));	// TODO height/radiusが適当
		mGuardObject = new DynamicGameObject(1, 1, 1, scale) {
			@Override
			public void update(float deltaTime) {
				// 機体と同じ動きをするので特に何もする必要なし
			}
		};
		mFrontLeftRotorObj = new RotorObject(true);
		mFrontRightRotorObj = new RotorObject(false);
		mRearLeftRotorObj = new RotorObject(false);
		mRearRightRotorObj = new RotorObject(true);
	}

	public void startEngine() {
		setRotorSpeed(RotorObject.DEFAULT_SPEED);
	}

	public void stopEngine() {
		setRotorSpeed(0);
	}

	public void setRotorSpeed(final float speed) {
		if (mRotorSpeed != speed) {
			mRotorSpeed = speed;
			mFrontLeftRotorObj.setSpeed(speed);
			mFrontRightRotorObj.setSpeed(speed);
			mRearLeftRotorObj.setSpeed(speed);
			mRearRightRotorObj.setSpeed(speed);
		}
	}

	protected abstract Vector getOffset();
}