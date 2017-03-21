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

import com.serenegiant.gameengine.FileIO;
import com.serenegiant.gameengine.v1.GLLoadableModel;
import com.serenegiant.gameengine.v1.IGLGameView;
import com.serenegiant.gameengine.v1.StaticTexture;

public class AttitudeScreenCargodrone extends AttitudeScreenBase {
	private static final String TAG = "AttitudeScreenCargodrone";

	public AttitudeScreenCargodrone(final IGLGameView modelView, final int ctrl_type) {
		super(modelView, ctrl_type);
	}

	@Override
	public void resume() {
		super.resume();
		droneModel.resume();
		if (guardModel != null) {
			guardModel.resume();
		}
		frontLeftRotorModel.resume(false);
		frontRightRotorModel.resume(false);
		rearLeftRotorModel.resume(false);
		rearRightRotorModel.resume(false);
	}

	@Override
	public void pause() {
		droneModel.pause();
		if (guardModel != null) {
			guardModel.pause();
		}
		frontLeftRotorModel.pause();
		frontRightRotorModel.pause();
		rearLeftRotorModel.pause();
		rearRightRotorModel.pause();
		super.pause();
	}

/*	@Override
	public void release() {
		super.release();
	} */

	@Override
	protected void initModel() {
		// 機体
		switch (mCtrlType) {
		case CTRL_PILOT:
			droneObj = new DroneObjectCargodrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_ATTITUDE:
			droneObj = new DroneObjectCargodrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_CALIBRATION:	// 多分これはこない
			droneObj = new DroneObjectCargodrone(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(0, 8, -9f);
			mShowGround = false;
			break;
		case CTRL_RANDOM:
		default:
			droneObj = new DroneObjectCargodroneRandom(0, 0, 0, 1.0f);
			lookAtCamera.setPosition(-9, 8, -9);
			mShowGround = false;
			break;
		}
		// 3Dモデルの読み込み
		final FileIO io = getView().getFileIO();
		StaticTexture droneTexture = null;
		try {
			droneTexture = new StaticTexture((IGLGameView)getView(), "cargo_drone_tex.png");
		} catch (final Exception e) {
			Log.w(TAG, e);
			droneTexture = new StaticTexture((IGLGameView)getView(), "model/cargo_drone_tex_blue.png");
		}
		droneModel = loadModel(io, "model/cargo_drone_body.obj");
		droneModel.setTexture(droneTexture);
		// ガード(ハル)
		guardModel = loadModel(io, "model/cargo_drone_bumper.obj");
		guardModel.setTexture(droneTexture);
		// 左前ローター
		frontLeftRotorModel = loadModel(io, "model/cargo_drone_rotor_cw.obj");
		frontLeftRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 右前ローター
		frontRightRotorModel = loadModel(io, "model/cargo_drone_rotor_ccw.obj");
		frontRightRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 左後ローター
		rearLeftRotorModel = new GLLoadableModel(frontRightRotorModel);	// コピーコンストラクタ
		rearLeftRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
		// 右後ローター
		rearRightRotorModel = new GLLoadableModel(frontLeftRotorModel);	// コピーコンストラクタ
		rearRightRotorModel.setTexture(droneTexture);	// テクスチャは今は共通
	}

/*	@Override
	protected void drawBackground(final GL10 gl) {
		super.drawBackground(gl);
	} */
}
