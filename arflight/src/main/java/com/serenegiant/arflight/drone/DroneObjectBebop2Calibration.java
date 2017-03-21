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

import android.util.Log;

import com.serenegiant.math.Vector;

public class DroneObjectBebop2Calibration extends DroneObjectBebop2 implements ICalibrationModelObject {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = DroneObjectBebop2Calibration.class.getSimpleName();

	private static final float ROTATION_SPEED = 180.0f;
	private int mAxis = -1;
	private volatile int mRequestAxis = -1;
	private float mDirection = ROTATION_SPEED;

	public DroneObjectBebop2Calibration(final Vector position, final float scale) {
		this(position.x, position.y, position.z, scale);
	}

	public DroneObjectBebop2Calibration(final float x, final float y, final float z, final float scale) {
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
