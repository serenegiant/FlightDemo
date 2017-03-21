package com.serenegiant.arflight.drone;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import com.serenegiant.gameengine.DynamicGameObject;
import com.serenegiant.gameengine.RotorObject;
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