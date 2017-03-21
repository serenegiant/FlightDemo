package com.serenegiant.aceparrot;
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

public class AppConst {
	public static final String KEY_OPERATION_TYPE = "OPERATION_TYPE";
	public static final String KEY_OPERATION_TOUCH = "OPERATION_TOUCH";
	// ゲームパッド
	public static final String KEY_GAMEPAD_USE_DRIVER = "CONFIG_GAMEPAD_USE_DRIVER";
	public static final String KEY_GAMEPAD_SENSITIVITY = "CONFIG_GAMEPAD_SENSITIVITY";
	public static final String KEY_GAMEPAD_SCALE_X = "CONFIG_GAMEPAD_SCALE_X";
	public static final String KEY_GAMEPAD_SCALE_Y = "CONFIG_GAMEPAD_SCALE_Y";
	public static final String KEY_GAMEPAD_SCALE_Z = "CONFIG_GAMEPAD_SCALE_Z";
	public static final String KEY_GAMEPAD_SCALE_R = "CONFIG_GAMEPAD_SCALE_R";
	// 自動操縦
	public static final String KEY_AUTOPILOT_MAX_CONTROL_VALUE = "CONFIG_AUTOPILOT_MAX_CONTROL_VALUE";
	public static final float DEFAULT_AUTOPILOT_MAX_CONTROL_VALUE = 100.0f;
	public static final String KEY_AUTOPILOT_SCALE_X = "CONFIG_AUTOPILOT_SCALE_X";
	public static final float DEFAULT_AUTOPILOT_SCALE_X = 1.0f;
	public static final String KEY_AUTOPILOT_SCALE_Y = "CONFIG_AUTOPILOT_SCALE_Y";
	public static final float DEFAULT_AUTOPILOT_SCALE_Y = 1.0f;
	public static final String KEY_AUTOPILOT_SCALE_Z = "CONFIG_AUTOPILOT_SCALE_Z";
	public static final float DEFAULT_AUTOPILOT_SCALE_Z = 1.0f;
	public static final String KEY_AUTOPILOT_SCALE_R = "CONFIG_AUTOPILOT_SCALE_R";
	public static final float DEFAULT_AUTOPILOT_SCALE_R = 1.0f;
	// アイコン
	public static final String KEY_ICON_TYPE = "ICON_TYPE";
	// 機体色
	public static final String KEY_COLOR = "CONFIG_COLOR_COLOR";
	// 操縦画面のアイコンを自動的に隠すかどうか
	public static final String KEY_AUTO_HIDE = "CONFIG_AUTO_HIDE";
	// オフラインの音声認識を優先するかどうか
	public static final String KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE = "KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE";
	// 音声認識でのスクリプト飛行を有効にするかどうか
	public static final String KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT = "KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT";
	// 減衰率
	public static final String KEY_CONFIG_VOICE_RECOGNITION_DAMPING_RATE = "KEY_CONFIG_VOICE_RECOGNITION_DAMPING_RATE";
	public static final int DEFAULT_VOICE_RECOGNITION_DAMPING_RATE = 75; // = 0.75f

	public static final float SCALE_FACTOR = 250f;
	public static final int SCALE_OFFSET = 500;

	public static final String ARFLIGHT_EXTRA_DEVICE_SERVICE = "ARFLIGHT_EXTRA_DEVICE_SERVICE";
	public static final String ARFLIGHT_EXTRA_DEVICE_INFO = "ARFLIGHT_EXTRA_DEVICE_INFO";

}
