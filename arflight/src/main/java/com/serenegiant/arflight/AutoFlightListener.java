package com.serenegiant.arflight;
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

public interface AutoFlightListener {
	/**
	 * 自動フライトの準備完了
	 */
	public void onPrepared();
	/**
	 * 自動フライト開始
	 */
	public void onStart();

	/**
	 * 自動フライトのコマンドコールバック
	 * @param cmd
	 * @param t
	 * @param values
	 * @return trueを返すと終了する
	 */
	public boolean onStep(final int cmd, final int[] values, final long t);

	/**
	 * 値を取得
	 * @param axis, 0:roll, 1:pitch, 2:yaw, 3:gaz, 4:compass, 5:max_tilt, 6:max_rotation_speed, 7:max_vertical_speed
	 * @return
	 */
	public float getValues(final int axis);
	/**
	 * 自動フライト停止
	 */
	public void onStop();

	/**
	 * 非同期実行中にエラー発生
	 * @param e
	 */
	public void onError(Exception e);
}
