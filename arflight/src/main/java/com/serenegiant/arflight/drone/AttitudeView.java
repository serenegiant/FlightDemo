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

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.serenegiant.gameengine.v1.GLModelView;
import com.serenegiant.gameengine.IScreen;

public class AttitudeView extends GLModelView {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeView";

	private int mModel = MODEL_BEBOP;
	private int mCtrlType = AttitudeScreenBase.CTRL_RANDOM;
	private boolean mHasGuard;

	public AttitudeView(final Context context) {
		this(context, null);
	}

	public AttitudeView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	public void setModel(final int model, final int type) {
		if (DEBUG) Log.v(TAG, String.format("setModel:model=%d, type=%d", model, type));
		mModel = model % MODEL_NUM;
		mCtrlType = type % AttitudeScreenBase.CTRL_NUM;
	}

	@Override
	protected IScreen createScreen() {
		if (DEBUG) Log.v(TAG, "createScreen");
		IScreen result = null;
		switch (mModel) {
		case MODEL_NON:
		case MODEL_JUMPINGSUMO:
			result = new AttitudeScreenNull(this);
			break;
		case MODEL_MINIDRONE:
			result = new AttitudeScreenMinidrone(this, mCtrlType);
			break;
		case MODEL_BEBOP2:
			result = new AttitudeScreenBebop2(this, mCtrlType);
			break;
		case MODEL_CARGO:
			result = new AttitudeScreenCargodrone(this, mCtrlType);
			break;
		case MODEL_SKYCONTROLLER:
			result = new AttitudeScreenSkyController(this, mCtrlType);
			break;
		case MODEL_MAMBO:
			result = new AttitudeScreenMambo(this, mCtrlType);
			break;
		case MODEL_BEBOP:
		default:
			result = new AttitudeScreenBebop(this, mCtrlType);
			break;
		}
		if (result instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) result).hasGuard(mHasGuard);
		}
		return result;
	}

	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[-100,100] => 今は[-30,+30][度]に対応
	 * @param pitch 前後の傾き(機種の上げ下げ)[-100,100] => 今は[-30,+30][度]に対応
	 * @param yaw 水平回転[-180,+180][度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz) {
		if (mScreen instanceof  AttitudeScreenBase) {
			((AttitudeScreenBase)mScreen).setAttitude(roll, pitch, yaw, gaz);
		}
	}

	@Override
	public void hasGuard(final boolean hasGuard) {
		mHasGuard = hasGuard;	// hasGuardが呼ばれた時点ではmScreenが未生成かもしれないのでこのくらすないでも
		if (mScreen instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) mScreen).hasGuard(hasGuard);
		}
	}

	@Override
	public void startEngine() {
		if (mScreen instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) mScreen).startEngine();
		}
	}

	@Override
	public void stopEngine() {
		if (mScreen instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) mScreen).stopEngine();
		}
	}

	@Override
	public void setRotorSpeed(final float speed) {
		if (mScreen instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) mScreen).setRotorSpeed(speed);
		}
	}

	@Override
	public void setAxis(final int axis) {
		if (mScreen instanceof AttitudeScreenBase) {
			((AttitudeScreenBase) mScreen).setAxis(axis);
		}
	}

}
