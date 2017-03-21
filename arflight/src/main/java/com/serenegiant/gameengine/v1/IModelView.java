package com.serenegiant.gameengine.v1;
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

public interface IModelView extends IGLGameView {
	public static final int MODEL_NON = 0;
	public static final int MODEL_BEBOP = 1;
	public static final int MODEL_MINIDRONE = 2;
	public static final int MODEL_JUMPINGSUMO = 3;
	public static final int MODEL_BEBOP2 = 4;
	public static final int MODEL_CARGO = 5;
	public static final int MODEL_SKYCONTROLLER = 6;
	public static final int MODEL_MAMBO = 7;
	public static final int MODEL_NUM = 8;

	public void setModel(final int model, final int type);
	public void hasGuard(final boolean hasGuard);
	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[度]
	 * @param pitch 前後の傾き(機種の上げ下げ)[度]
	 * @param yaw 水平回転[度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz);

	public void startEngine();
	public void stopEngine();
	public void setRotorSpeed(final float speed);
	public void setAxis(final int axis);
}
