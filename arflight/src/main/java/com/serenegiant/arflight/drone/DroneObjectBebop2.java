package com.serenegiant.arflight.drone;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
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
