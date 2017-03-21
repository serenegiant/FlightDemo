package com.serenegiant.arflight;
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

public interface IAutoFlight {
	public static final int CMD_EMERGENCY = -1;		// 非常停止

	public static final int CMD_NON = 0;
	public static final int CMD_TAKEOFF = 1;		// 離陸
	public static final int CMD_LANDING = 2;		// 着陸

	public static final int CMD_UP_DOWN = 3;		// 上昇:gaz>0, 下降: gaz<0
	public static final int CMD_RIGHT_LEFT = 4;		// 右: roll>0,flag=1 左: roll<0,flag=1
	public static final int CMD_FORWARD_BACK = 5;	// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
	public static final int CMD_TURN = 6;			// 右回転: yaw>0, 左回転: ywa<0
	public static final int CMD_COMPASS = 7;		// 北磁極に対する角度を指定-360-360度

	public static final int CMD_MOVE5 = 8;			// roll + pitch + gaz + yaw + flag
	public static final int CMD_MOVE4 = 9;			// roll + pitch + gaz + yaw
	public static final int CMD_MOVE3 = 10;			// roll + pitch + gaz
	public static final int CMD_MOVE2 = 11;			// roll + pitch

	public static final int CMD_FLIP = 100;			// 1:前, 2:後, 3:右, 4:左
	public static final int CMD_CAP = 101;			// -180〜180度

	/**
	 * 自動フライトの準備
	 * @param args
	 * @throws RuntimeException
	 */
	public void prepare(Object... args) throws RuntimeException;
	/**
	 * 自動フライト開始
	 */
	public void play() throws IllegalStateException;

	/**
	 * 自動フライト終了
	 */
	public void stop();

	/**
	 * 自動フライトの準備ができるかどうか
	 * @return
	 */
	public boolean isPrepared();
	/**
	 * 自動フライト中かどうか
	 * @return
	 */
	public boolean isPlaying();

	/**
	 * 関係するリソースを破棄する
	 */
	public void release();
}
