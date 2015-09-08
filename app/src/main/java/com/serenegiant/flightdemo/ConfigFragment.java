package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.AttributeFloat;

public class ConfigFragment extends ControlFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigFragment.class.getSimpleName();

	public static final String KEY_OPERATION_TYPE = "OPERATION_TYPE";
	// ゲームパッド
	public static final String KEY_GAMEPAD_SENSITIVITY = "CONFIG_GAMEPAD_SENSITIVITY";
	public static final String KEY_GAMEPAD_SCALE_X = "CONFIG_GAMEPAD_SCALE_X";
	public static final String KEY_GAMEPAD_SCALE_Y = "CONFIG_GAMEPAD_SCALE_Y";
	public static final String KEY_GAMEPAD_SCALE_Z = "CONFIG_GAMEPAD_SCALE_Z";
	public static final String KEY_GAMEPAD_SCALE_R = "CONFIG_GAMEPAD_SCALE_R";
	// 自動操縦
	public static final String KEY_AUTOPILOT_MAX_CONTROL_VALUE = "CONFIG_AUTOPILOT_MAX_CONTROL_VALUE";
	public static final String KEY_AUTOPILOT_SCALE_X = "CONFIG_AUTOPILOT_SCALE_X";
	public static final String KEY_AUTOPILOT_SCALE_Y = "CONFIG_AUTOPILOT_SCALE_Y";
	public static final String KEY_AUTOPILOT_SCALE_Z = "CONFIG_AUTOPILOT_SCALE_Z";
	public static final String KEY_AUTOPILOT_SCALE_R = "CONFIG_AUTOPILOT_SCALE_R";


	public static ConfigFragment newInstance(final ARDiscoveryDeviceService device) {
		final ConfigFragment fragment = new ConfigFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private SharedPreferences mPref;
	private ViewPager mViewPager;
	private ConfigPagerAdapter mPagerAdapter;

	private TextView mMaxAltitudeLabel;
	private TextView mMaxTiltLabel;
	private TextView mMaxVerticalSpeedLabel;
	private TextView mMaxRotationSpeedLabel;

	private TextView mAutopilotScaleXLabel;
	private TextView mAutopilotScaleYLabel;
	private TextView mAutopilotScaleZLabel;
	private TextView mAutopilotScaleRLabel;
	private TextView mAutopilotMaxControlValueLabel;

	private TextView mGamepadScaleXLabel;
	private TextView mGamepadScaleYLabel;
	private TextView mGamepadScaleZLabel;
	private TextView mGamepadScaleRLabel;
	private TextView mGamepadMaxControlValueLabel;

	private String mMaxAltitudeFormat;
	private String mMaxTiltFormat;
	private String mMaxVerticalSpeedFormat;
	private String mMaxRotationSpeedFormat;

	private String mGamepadScaleXFormat;
	private String mGamepadScaleYFormat;
	private String mGamepadScaleZFormat;
	private String mGamepadScaleRFormat;
	private String mGamepadSensitivityFormat;

	private String mAutopilotScaleXFormat;
	private String mAutopilotScaleYFormat;
	private String mAutopilotScaleZFormat;
	private String mAutopilotScaleRFormat;
	private String mAutopilotMaxControlValueFormat;

	public ConfigFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
		mMaxAltitudeFormat = getString(R.string.config_max_altitude);
		mMaxTiltFormat = getString(R.string.config_max_tilt);
		mMaxVerticalSpeedFormat = getString(R.string.config_max_vertical_speed);
		mMaxRotationSpeedFormat = getString(R.string.config_max_rotating_speed);

		mGamepadScaleXFormat = getString(R.string.config_scale_x);
		mGamepadScaleYFormat = getString(R.string.config_scale_y);
		mGamepadScaleZFormat = getString(R.string.config_scale_z);
		mGamepadScaleRFormat = getString(R.string.config_scale_r);
		mGamepadSensitivityFormat = getString(R.string.config_control_max);

		mAutopilotScaleXFormat = getString(R.string.config_scale_x);
		mAutopilotScaleYFormat = getString(R.string.config_scale_y);
		mAutopilotScaleZFormat = getString(R.string.config_scale_z);
		mAutopilotScaleRFormat = getString(R.string.config_scale_r);
		mAutopilotMaxControlValueFormat = getString(R.string.config_control_max);

		mPref = activity.getPreferences(0);
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		mPref = null;
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_config, container, false);
		mPagerAdapter = new ConfigPagerAdapter(inflater);
		mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		return rootView;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		super.onPause();
	}

	private AttributeFloat mMaxAltitude;
	private AttributeFloat mMaxTilt;
	private AttributeFloat mMaxVerticalSpeed;
	private AttributeFloat mMaxRotationSpeed;

	/**
	 * 飛行設定画面の準備
	 * @param root
	 */
	private void initConfigMinidrone1(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigMinidrone1:");
		// 最大高度設定
		mMaxAltitudeLabel = (TextView)root.findViewById(R.id.max_altitude_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_altitude_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxAltitude = mController.getMaxAltitude();
		try {
			seekbar.setProgress((int) ((mMaxAltitude.current() - mMaxAltitude.min()) / (mMaxAltitude.max() - mMaxAltitude.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxAltitude(mMaxAltitude.current());
		// 最大傾斜設定
		// bebopは5-30度。最大時速約50km/hrからすると13.9m/s/30度≒0.46[m/s/度]
		mMaxTiltLabel = (TextView)root.findViewById(R.id.max_tilt_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_tilt_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxTilt = mController.getMaxTilt();
		try {
			seekbar.setProgress((int) ((mMaxTilt.current() - mMaxTilt.min()) / (mMaxTilt.max() - mMaxTilt.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxTilt(mMaxTilt.current());
		// 最大上昇/降下速度設定
		mMaxVerticalSpeedLabel = (TextView)root.findViewById(R.id.max_vertical_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_vertical_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxVerticalSpeed = mController.getMaxVerticalSpeed();
		try {
			seekbar.setProgress((int) ((mMaxVerticalSpeed.current() - mMaxVerticalSpeed.min()) / (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxVerticalSpeed(mMaxVerticalSpeed.current());
		// 最大回転速度
		mMaxRotationSpeedLabel = (TextView)root.findViewById(R.id.max_rotation_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_rotation_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxRotationSpeed = mController.getMaxRotationSpeed();
		try {
			seekbar.setProgress((int) ((mMaxRotationSpeed.current() - mMaxRotationSpeed.min()) / (mMaxRotationSpeed.max() - mMaxRotationSpeed.min()) * 1000));
		} catch (final Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxRotationSpeed(mMaxRotationSpeed.current());
	}

	/**
	 * ドローン設定画面の準備
	 * @param root
	 */
	private void initConfigMinidrone2(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigMinidrone2:");
		// 自動カットアウトモード
		CheckBox checkbox = (CheckBox)root.findViewById(R.id.cutout_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mController.isCutoffMode());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 車輪
		checkbox = (CheckBox)root.findViewById(R.id.wheel_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mController.hasGuard());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		// 自動離陸モード
		checkbox = (CheckBox)root.findViewById(R.id.auto_takeoff_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(mController.isAutoTakeOffModeEnabled());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * 操作設定画面の準備
	 * @param root
	 */
	private void initConfigOperation(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigOperation:");
		final RadioGroup group = (RadioGroup)root.findViewById(R.id.operation_radiogroup);
		switch (mPref.getInt(KEY_OPERATION_TYPE, 0)) {
		case 1:		// 左右反転
			group.check(R.id.operation_reverse_radiobutton);
			break;
		case 2:		// タッチ描画で操作
			group.check(R.id.operation_touch_radiobutton);
			break;
		default:	// 通常
			group.check(R.id.operation_normal_radiobutton);
			break;
		}
		group.setOnCheckedChangeListener(mOnRadioButtonCheckedChangeListener);
	}

	private static final float SCALE_FACTOR = 250f;
	private static final int SCALE_OFFSET = 500;
	private float mGamepadMaxControlValue;
	private float mGamepadScaleX;
	private float mGamepadScaleY;
	private float mGamepadScaleZ;
	private float mGamepadScaleR;
	/**
	 * ゲームパッド設定画面の準備
	 * @param root
	 */
	private void initConfigGamepad(final View root) {
		// 最大制御値設定
		mGamepadMaxControlValueLabel = (TextView)root.findViewById(R.id.gamepad_sensitivity_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.gamepad_sensitivity_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadMaxControlValue = mPref.getFloat(KEY_GAMEPAD_SENSITIVITY, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadMaxControlValue + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadMaxControlValue(mGamepadMaxControlValue);
		// スケールX設定
		mGamepadScaleXLabel = (TextView)root.findViewById(R.id.gamepad_scale_x_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_x);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleX = mPref.getFloat(KEY_GAMEPAD_SCALE_X, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleX * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleX(mGamepadScaleX);
		// スケールY設定
		mGamepadScaleYLabel = (TextView)root.findViewById(R.id.gamepad_scale_y_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_y);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleY = mPref.getFloat(KEY_GAMEPAD_SCALE_Y, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleY * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleY(mGamepadScaleY);
		// スケールZ設定
		mGamepadScaleZLabel = (TextView)root.findViewById(R.id.gamepad_scale_z_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_z);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleZ = mPref.getFloat(KEY_GAMEPAD_SCALE_Z, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleZ * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleZ(mGamepadScaleZ);
		// スケールR設定
		mGamepadScaleRLabel = (TextView)root.findViewById(R.id.gamepad_scale_r_textview);
		seekbar = (SeekBar)root.findViewById(R.id.gamepad_scale_seekbar_r);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mGamepadScaleR = mPref.getFloat(KEY_GAMEPAD_SCALE_R, 1.0f);
		try {
			seekbar.setProgress((int) (mGamepadScaleR * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateGamepadScaleR(mGamepadScaleR);
	}

	private float mAutopilotMaxControlValue;
	private float mAutopilotScaleX;
	private float mAutopilotScaleY;
	private float mAutopilotScaleZ;
	private float mAutopilotScaleR;
	/**
	 * 自動操縦設定画面の準備
	 * @param root
	 */
	private void initConfigAutopilot(final View root) {
		// 最大制御値設定
		mAutopilotMaxControlValueLabel = (TextView)root.findViewById(R.id.max_control_value_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_control_value_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mAutopilotMaxControlValue = mPref.getFloat(KEY_AUTOPILOT_MAX_CONTROL_VALUE, 100.0f);
		try {
			seekbar.setProgress((int) (mAutopilotMaxControlValue + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateAutopilotMaxControlValue(mAutopilotMaxControlValue);
		// スケールX設定
		mAutopilotScaleXLabel = (TextView)root.findViewById(R.id.scale_x_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_x);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mAutopilotScaleX = mPref.getFloat(KEY_AUTOPILOT_SCALE_X, 1.0f);
		try {
			seekbar.setProgress((int) (mAutopilotScaleX * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateAutopilotScaleX(mAutopilotScaleX);
		// スケールY設定
		mAutopilotScaleYLabel = (TextView)root.findViewById(R.id.scale_y_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_y);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mAutopilotScaleY = mPref.getFloat(KEY_AUTOPILOT_SCALE_Y, 1.0f);
		try {
			seekbar.setProgress((int) (mAutopilotScaleY * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateAutopilotScaleY(mAutopilotScaleY);
		// スケールZ設定
		mAutopilotScaleZLabel = (TextView)root.findViewById(R.id.scale_z_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_z);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mAutopilotScaleZ = mPref.getFloat(KEY_AUTOPILOT_SCALE_Z, 1.0f);
		try {
			seekbar.setProgress((int) (mAutopilotScaleZ * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateAutopilotScaleZ(mAutopilotScaleZ);
		// スケールR設定
		mAutopilotScaleRLabel = (TextView)root.findViewById(R.id.scale_r_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_r);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mAutopilotScaleR = mPref.getFloat(KEY_AUTOPILOT_SCALE_R, 1.0f);
		try {
			seekbar.setProgress((int) (mAutopilotScaleR * SCALE_FACTOR + SCALE_OFFSET));
		} catch (final Exception e) {
			seekbar.setProgress(SCALE_OFFSET);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateAutopilotScaleR(mAutopilotScaleR);
	}

	/**
	 * ドローン情報画面の準備
	 * @param root
	 */
	private void initConfigInfo(final View root) {
		if (DEBUG) Log.v(TAG, "initConfigInfo:");
		TextView tv = (TextView)root.findViewById(R.id.app_version_textview);
		tv.setText(BuildConfig.VERSION_NAME);
		tv = (TextView)root.findViewById(R.id.product_name_textview);
		tv.setText(mController.getName());
		tv = (TextView)root.findViewById(R.id.software_version_textview);
		tv.setText(mController.getSoftwareVersion());
		tv = (TextView)root.findViewById(R.id.hardware_version_textview);
		tv.setText(mController.getHardwareVersion());
	}

	/**
	 * 最大高度設定値表示を更新
	 * @param max_altitude
	 */
	private void updateMaxAltitude(final float max_altitude) {
		if (mMaxAltitudeLabel != null) {
			mMaxAltitudeLabel.setText(String.format(mMaxAltitudeFormat, max_altitude));
		}
	}

	/**
	 * 最大傾斜設定表示を更新
	 * @param max_tilt
	 */
	private void updateMaxTilt(final float max_tilt) {
		if (mMaxTiltLabel != null) {
			mMaxTiltLabel.setText(String.format(mMaxTiltFormat, max_tilt));
		}
	}

	/**
	 * 最大上昇/降下速度設定表示を更新
	 * @param max_vertical_speed
	 */
	private void updateMaxVerticalSpeed(final float max_vertical_speed) {
		if (mMaxVerticalSpeedLabel != null) {
			mMaxVerticalSpeedLabel.setText(String.format(mMaxVerticalSpeedFormat, max_vertical_speed));
		}
	}

	/**
	 * 最大回転速度設定表示を更新
	 * @param max_rotation_speed
	 */
	private void updateMaxRotationSpeed(final float max_rotation_speed) {
		if (mMaxRotationSpeedLabel != null) {
			mMaxRotationSpeedLabel.setText(String.format(mMaxRotationSpeedFormat, max_rotation_speed));
		}
	}

	/**
	 * ゲームパッド:最大制御設定値表示を更新
	 * @param sensitivity
	 */
	private void updateGamepadMaxControlValue(final float sensitivity) {
		if (mGamepadMaxControlValueLabel != null) {
			mGamepadMaxControlValueLabel.setText(String.format(mGamepadSensitivityFormat, sensitivity));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_x
	 */
	private void updateGamepadScaleX(final float scale_x) {
		if (mGamepadScaleXLabel != null) {
			mGamepadScaleXLabel.setText(String.format(mGamepadScaleXFormat, scale_x));
		}
	}

	/**
	 * ゲームパッド:スケールY設定表示を更新
	 * @param scale_y
	 */
	private void updateGamepadScaleY(final float scale_y) {
		if (mGamepadScaleYLabel != null) {
			mGamepadScaleYLabel.setText(String.format(mGamepadScaleYFormat, scale_y));
		}
	}

	/**
	 * ゲームパッド:スケールZ設定表示を更新
	 * @param scale_z
	 */
	private void updateGamepadScaleZ(final float scale_z) {
		if (mGamepadScaleZLabel != null) {
			mGamepadScaleZLabel.setText(String.format(mGamepadScaleZFormat, scale_z));
		}
	}

	/**
	 * ゲームパッド:スケールR設定表示を更新
	 * @param scale_r
	 */
	private void updateGamepadScaleR(final float scale_r) {
		if (mGamepadScaleRLabel != null) {
			mGamepadScaleRLabel.setText(String.format(mGamepadScaleRFormat, scale_r));
		}
	}

	/**
	 * 自動操縦:最大制御設定値表示を更新
	 * @param max_control_value
	 */
	private void updateAutopilotMaxControlValue(final float max_control_value) {
		if (mAutopilotMaxControlValueLabel != null) {
			mAutopilotMaxControlValueLabel.setText(String.format(mAutopilotMaxControlValueFormat, max_control_value));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_x
	 */
	private void updateAutopilotScaleX(final float scale_x) {
		if (mAutopilotScaleXLabel != null) {
			mAutopilotScaleXLabel.setText(String.format(mAutopilotScaleXFormat, scale_x));
		}
	}

	/**
	 * 自動操縦:スケールY設定表示を更新
	 * @param scale_y
	 */
	private void updateAutopilotScaleY(final float scale_y) {
		if (mAutopilotScaleYLabel != null) {
			mAutopilotScaleYLabel.setText(String.format(mAutopilotScaleYFormat, scale_y));
		}
	}

	/**
	 * 自動操縦:スケールZ設定表示を更新
	 * @param scale_z
	 */
	private void updateAutopilotScaleZ(final float scale_z) {
		if (mAutopilotScaleZLabel != null) {
			mAutopilotScaleZLabel.setText(String.format(mAutopilotScaleZFormat, scale_z));
		}
	}

	/**
	 * 自動操縦:スケールR設定表示を更新
	 * @param scale_r
	 */
	private void updateAutopilotScaleR(final float scale_r) {
		if (mAutopilotScaleRLabel != null) {
			mAutopilotScaleRLabel.setText(String.format(mAutopilotScaleRFormat, scale_r));
		}
	}

	/**
	 * シークバーのイベント
	 */
	private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		/**
		 * タッチ処理の開始
		 * @param seekBar
		 */
		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
		}

		/**
		 * シークバーの値が変更された時の処理
		 * @param seekBar
		 * @param progress
		 * @param fromUser
		 */
		@Override
		public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
			if (fromUser) {
				// ユーザーのタッチ処理でシークバーの値が変更された時
				switch (seekBar.getId()) {
				case R.id.max_altitude_seekbar:
					final float altitude = (int) (progress / 100f * (mMaxAltitude.max() - mMaxAltitude.min())) / 10f + mMaxAltitude.min();
					updateMaxAltitude(altitude);
					break;
				case R.id.max_tilt_seekbar:
					final float tilt = (int) (progress / 100f * (mMaxTilt.max() - mMaxTilt.min())) / 10f + mMaxTilt.min();
					updateMaxTilt(tilt);
					break;
				case R.id.max_vertical_speed_seekbar:
					final float vertical = (int) (progress / 100f * (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min())) / 10f + mMaxVerticalSpeed.min();
					updateMaxVerticalSpeed(vertical);
					break;
				case R.id.max_rotation_speed_seekbar:
					final float rotation = (int) (progress / 1000f * (mMaxRotationSpeed.max() - mMaxRotationSpeed.min())) + mMaxRotationSpeed.min();
					updateMaxRotationSpeed(rotation);
					break;
				case R.id.max_control_value_seekbar:	// -500〜+500
					final float max_control_value = progress - SCALE_OFFSET;
					updateAutopilotMaxControlValue(max_control_value);
					break;
				case R.id.scale_seekbar_x:
					final float scale_x = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateAutopilotScaleX(scale_x);
					break;
				case R.id.scale_seekbar_y:
					final float scale_y = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateAutopilotScaleY(scale_y);
					break;
				case R.id.scale_seekbar_z:
					final float scale_z = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateAutopilotScaleZ(scale_z);
					break;
				case R.id.scale_seekbar_r:
					final float scale_r = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateAutopilotScaleR(scale_r);
					break;
				case R.id.gamepad_sensitivity_seekbar:	// -5.00〜+5.00
					final float sensitivity = (progress - SCALE_OFFSET) / 100f;
					updateGamepadMaxControlValue(sensitivity);
					break;
				case R.id.gamepad_scale_seekbar_x:
					final float gamepad_scale_x = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateGamepadScaleX(gamepad_scale_x);
					break;
				case R.id.gamepad_scale_seekbar_y:
					final float gamepad_scale_y = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateGamepadScaleY(gamepad_scale_y);
					break;
				case R.id.gamepad_scale_seekbar_z:
					final float gamepad_scale_z = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateGamepadScaleZ(gamepad_scale_z);
					break;
				case R.id.gamepad_scale_seekbar_r:
					final float gamepad_scale_r = (progress - SCALE_OFFSET) / SCALE_FACTOR;
					updateGamepadScaleR(gamepad_scale_r);
					break;
				}
			}
		}

		/**
		 * シークバーのタッチ処理が終了した時の処理
		 * ここで設定を適用する
		 * @param seekBar
		 */
		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			if (mController == null) {
				Log.w(TAG, "deviceControllerがnull");
				return;
			}
			switch (seekBar.getId()) {
			case R.id.max_altitude_seekbar:
				final float altitude = (int)(seekBar.getProgress() / 100f * (mMaxAltitude.max() - mMaxAltitude.min())) / 10f + mMaxAltitude.min();
				if (altitude != mMaxAltitude.current()) {
					mController.sendMaxAltitude(altitude);
				}
				break;
			case R.id.max_tilt_seekbar:
				final float tilt = (int)(seekBar.getProgress() / 100f * (mMaxTilt.max() - mMaxTilt.min())) / 10f + mMaxTilt.min();
				if (tilt != mMaxTilt.current()) {
					mController.sendMaxTilt(tilt);
				}
				break;
			case R.id.max_vertical_speed_seekbar:
				final float vertical = (int)(seekBar.getProgress() / 100f * (mMaxVerticalSpeed.max() - mMaxVerticalSpeed.min())) / 10f + mMaxVerticalSpeed.min();
				if (vertical != mMaxVerticalSpeed.current()) {
					mController.sendMaxVerticalSpeed(vertical);
				}
				break;
			case R.id.max_rotation_speed_seekbar:
				final float rotation = (int)(seekBar.getProgress() / 1000f * (mMaxRotationSpeed.max() - mMaxRotationSpeed.min())) + mMaxRotationSpeed.min();
				if (rotation != mMaxRotationSpeed.current()) {
					mController.sendMaxRotationSpeed(rotation);
				}
				break;
			// 自動操縦
			case R.id.max_control_value_seekbar:
				final float max_control_value = seekBar.getProgress() - SCALE_OFFSET;
				if (max_control_value != mAutopilotMaxControlValue) {
					mAutopilotMaxControlValue = max_control_value;
					mPref.edit().putFloat(KEY_AUTOPILOT_MAX_CONTROL_VALUE, max_control_value).apply();
				}
				break;
			case R.id.scale_seekbar_x:
				final float scale_x = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (scale_x != mAutopilotScaleX) {
					mAutopilotScaleX = scale_x;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_X, scale_x).apply();
				}
				break;
			case R.id.scale_seekbar_y:
				final float scale_y = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (scale_y != mAutopilotScaleY) {
					mAutopilotScaleY = scale_y;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_Y, scale_y).apply();
				}
				break;
			case R.id.scale_seekbar_z:
				final float scale_z = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (scale_z != mAutopilotScaleZ) {
					mAutopilotScaleZ = scale_z;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_Z, scale_z).apply();
				}
				break;
			case R.id.scale_seekbar_r:
				final float scale_r = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (scale_r != mAutopilotScaleR) {
					mAutopilotScaleR = scale_r;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_R, scale_r).apply();
				}
				break;
			// ゲームパッド
			case R.id.gamepad_sensitivity_seekbar:
				final float sensitivity = (seekBar.getProgress() - SCALE_OFFSET) / 100f;
				if (sensitivity != mGamepadMaxControlValue) {
					mGamepadMaxControlValue = sensitivity;
					mPref.edit().putFloat(KEY_GAMEPAD_SENSITIVITY, sensitivity).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_x:
				final float gamepad_scale_x = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (gamepad_scale_x != mGamepadScaleX) {
					mGamepadScaleX = gamepad_scale_x;
					mPref.edit().putFloat(KEY_GAMEPAD_SCALE_X, gamepad_scale_x).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_y:
				final float gamepad_scale_y = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (gamepad_scale_y != mGamepadScaleY) {
					mGamepadScaleY = gamepad_scale_y;
					mPref.edit().putFloat(KEY_GAMEPAD_SCALE_Y, gamepad_scale_y).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_z:
				final float gamepad_scale_z = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (gamepad_scale_z != mGamepadScaleZ) {
					mGamepadScaleZ = gamepad_scale_z;
					mPref.edit().putFloat(KEY_GAMEPAD_SCALE_Z, gamepad_scale_z).apply();
				}
				break;
			case R.id.gamepad_scale_seekbar_r:
				final float gamepad_scale_r = (seekBar.getProgress() - SCALE_OFFSET) / SCALE_FACTOR;
				if (gamepad_scale_r != mGamepadScaleR) {
					mGamepadScaleR = gamepad_scale_r;
					mPref.edit().putFloat(KEY_GAMEPAD_SCALE_R, gamepad_scale_r).apply();
				}
				break;
			}
		}
	};

	/**
	 * チェックボックスの選択状態が変更された時の処理
	 */
	private final CompoundButton.OnCheckedChangeListener
		mOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.cutout_checkbox:
				if (mController.isCutoffMode() != isChecked) {
					mController.sendCutOutMode(isChecked);
				}
				break;
			case R.id.wheel_checkbox:
				if (mController.hasGuard() != isChecked) {
					mController.sendHasGuard(isChecked);
				}
				break;
			case R.id.auto_takeoff_checkbox:
				if (mController.isAutoTakeOffModeEnabled() != isChecked) {
					mController.sendAutoTakeOffMode(isChecked);
				}
				break;
			}
		}
	};

	private final RadioGroup.OnCheckedChangeListener mOnRadioButtonCheckedChangeListener = new RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			switch (checkedId) {
			case R.id.operation_normal_radiobutton:
				mPref.edit().putInt(KEY_OPERATION_TYPE, 0).apply();
				break;
			case R.id.operation_reverse_radiobutton:
				mPref.edit().putInt(KEY_OPERATION_TYPE, 1).apply();
				break;
			case R.id.operation_touch_radiobutton:
				mPref.edit().putInt(KEY_OPERATION_TYPE, 2).apply();
				break;
			}
		}
	};

	private static interface AdapterItemHandler {
		public void initialize(final ConfigFragment parent, final View view);
	}

	private static final class PagerAdapterConfig {
		public final int title_id;
		public final int layout_id;
		public final AdapterItemHandler handler;

		public PagerAdapterConfig(final int _title_id, final int _layout_id, final AdapterItemHandler _handler) {
			title_id = _title_id;
			layout_id = _layout_id;
			handler = _handler;
		}
	}

	private static PagerAdapterConfig[] PAGER_CONFIG;
	static {
		PAGER_CONFIG = new PagerAdapterConfig[6];
		PAGER_CONFIG[0] = new PagerAdapterConfig(R.string.config_title_flight, R.layout.config_minidrone_1, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigMinidrone1(view);
			}
		});
		PAGER_CONFIG[1] = new PagerAdapterConfig(R.string.config_title_drone, R.layout.config_minidrone_2, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigMinidrone2(view);
			}
		});
		PAGER_CONFIG[2] = new PagerAdapterConfig(R.string.config_title_operation, R.layout.config_operation, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigOperation(view);
			}
		});
		PAGER_CONFIG[3] = new PagerAdapterConfig(R.string.config_title_gamepad, R.layout.config_gamepad, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigGamepad(view);
			}
		});
		PAGER_CONFIG[4] = new PagerAdapterConfig(R.string.config_title_autopilot, R.layout.config_autopilot, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigAutopilot(view);
			}
		});
		PAGER_CONFIG[5] = new PagerAdapterConfig(R.string.config_title_info, R.layout.config_info, new AdapterItemHandler() {
			@Override
			public void initialize(final ConfigFragment parent, final View view) {
				parent.initConfigInfo(view);
			}
		});
	};

	/**
	 * 設定画面の各ページ用のViewを提供するためのPagerAdapterクラス
	 */
	private class ConfigPagerAdapter extends PagerAdapter {
		private final LayoutInflater mInflater;
		public ConfigPagerAdapter(final LayoutInflater inflater) {
			super();
			mInflater = inflater;
		}

		@Override
		public synchronized Object instantiateItem(final ViewGroup container, final int position) {
			if (DEBUG) Log.v(TAG, "instantiateItem:position=" + position);
			View view = null;
			if ((position >= 0) && (position < PAGER_CONFIG.length)) {
				final PagerAdapterConfig config = PAGER_CONFIG[position];
				view = mInflater.inflate(config.layout_id, container, false);
				config.handler.initialize(ConfigFragment.this, view);
			}
			if (view != null) {
				container.addView(view);
			}
			return view;
		}

		@Override
		public synchronized void destroyItem(final ViewGroup container, final int position, final Object object) {
			if (DEBUG) Log.v(TAG, "destroyItem:position=" + position);
			if (object instanceof View) {
				container.removeView((View)object);
			}
		}

		@Override
		public int getCount() {
			return PAGER_CONFIG.length;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object) {
			return view.equals(object);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			if (DEBUG) Log.v(TAG, "getPageTitle:position=" + position);
			CharSequence result = null;
			if ((position >= 0) && (position < PAGER_CONFIG.length)) {
				result = getString(PAGER_CONFIG[position].title_id);
			}
			return result;
		}
	}
}
