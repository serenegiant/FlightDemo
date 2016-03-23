package com.serenegiant.flightdemo;

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

	public static final float SCALE_FACTOR = 250f;
	public static final int SCALE_OFFSET = 500;

// ライントレース
	public static final String KEY_PREF_NAME_AUTOPILOT = "KEY_PREF_NAME_AUTOPILOT";
	public static final String KEY_AUTO_WHITE_BLANCE = "KEY_AUTO_WHITE_BLANCE";
	public static final String KEY_EXPOSURE = "KEY_EXPOSURE";
	public static final String KEY_SATURATION = "KEY_SATURATION";
	public static final String KEY_BRIGHTNESS = "KEY_BRIGHTNESS";
	public static final String KEY_ENABLE_POSTERIZE = "KEY_ENABLE_POSTERIZE";
	public static final String KEY_POSTERIZE = "KEY_POSTERIZE";
	public static final String KEY_BINARIZE_THRESHOLD = "KEY_BINARIZE_THRESHOLD";
	public static final String KEY_TRAPEZIUM_RATE = "KEY_TRAPEZIUM_RATE";

	public static final String KEY_SMOOTH_TYPE = "KEY_SMOOTH_TYPE";
	public static final String KEY_NATIVE_SMOOTH_TYPE = "KEY_NATIVE_SMOOTH_TYPE";
	public static final String KEY_ENABLE_EDGE_DETECTION = "KEY_ENABLE_EDGE_DETECTION";
	public static final String KEY_ENABLE_NATIVE_EDGE_DETECTION = "KEY_ENABLE_NATIVE_EDGE_DETECTION";
	public static final String KEY_AREA_LIMIT_MIN = "KEY_AREA_LIMIT_MIN";
	public static final float DEFAULT_AREA_LIMIT_MIN = 1000.0f;
	public static final String KEY_ASPECT_LIMIT_MIN = "KEY_ASPECT_LIMIT_MIN";
	public static final float DEFAULT_ASPECT_LIMIT_MIN = 3.0f;
	public static final String KEY_AREA_ERR_LIMIT1 = "KEY_AREA_ERR_LIMIT1";
	public static final String KEY_AREA_ERR_LIMIT2 = "KEY_AREA_ERR_LIMIT2";
	// 色抽出設定
	public static final String KEY_ENABLE_EXTRACTION = "KEY_ENABLE_EXTRACTION";
	public static final String KEY_ENABLE_NATIVE_EXTRACTION = "KEY_ENABLE_NATIVE_EXTRACTION";
	public static final String KEY_EXTRACT_H = "KEY_ENABLE_EXTRACT_H";
	public static final String KEY_EXTRACT_S = "KEY_ENABLE_EXTRACT_S";
	public static final String KEY_EXTRACT_V = "KEY_ENABLE_EXTRACT_V";
	public static final String KEY_EXTRACT_RANGE_H = "KEY_ENABLE_EXTRACT_RANGE_H";
	public static final String KEY_EXTRACT_RANGE_S = "KEY_ENABLE_EXTRACT_RANGE_S";
	public static final String KEY_EXTRACT_RANGE_V = "KEY_ENABLE_EXTRACT_RANGE_V";


	public static final String KEY_TRACE_ATTITUDE_YAW = "KEY_TRACE_FLIGHT_ATTITUDE_YAW";
	public static final float DEFAULT_TRACE_ATTITUDE_YAW = 0.0f;
	public static final String KEY_TRACE_SPEED = "KEY_TRACE_FLIGHT_SPEED";
	public static final float DEFAULT_TRACE_SPEED = 100.0f;
	public static final String KEY_TRACE_CURVATURE = "KEY_TRACE_CURVATURE";
	public static final float DEFAULT_TRACE_CURVATURE = 0.0f;
	public static final String KEY_TRACE_DIR_REVERSE_BIAS = "KEY_TRACE_FLIGHT_DIR_REVERSE_BIAS";
	public static final float DEFAULT_TRACE_DIR_REVERSE_BIAS = 0.3f;
}
