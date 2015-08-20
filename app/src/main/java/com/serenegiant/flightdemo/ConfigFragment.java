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
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.AttributeFloat;
import com.serenegiant.arflight.DeviceControllerMiniDrone;

public class ConfigFragment extends ControlFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigFragment.class.getSimpleName();

	public static final String KEY_REVERSE_OPERATION = "REVERSE_OPERATION";
	public static final String KEY_AUTOPILOT_MAX_CONTROL_VALUE = "CONFIG_AUTOPILOT_MAX_CONTROL_VALUE";
	public static final String KEY_AUTOPILOT_SCALE_X = "CONFIG_AUTOPILOT_SCALE_X";
	public static final String KEY_AUTOPILOT_SCALE_Y = "CONFIG_AUTOPILOT_SCALE_Y";
	public static final String KEY_AUTOPILOT_SCALE_Z = "CONFIG_AUTOPILOT_SCALE_Z";


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
	private TextView mScaleXLabel;
	private TextView mScaleYLabel;
	private TextView mScaleZLabel;
	private TextView mMaxControlValueLabel;

	private String mMaxAltitudeFormat;
	private String mMaxTiltFormat;
	private String mMaxVerticalSpeedFormat;
	private String mMaxRotationSpeedFormat;
	private String mScaleXFormat;
	private String mScaleYFormat;
	private String mScaleZFormat;
	private String mMaxControlValueFormat;

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
		mScaleXFormat = getString(R.string.config_scale_x);
		mScaleYFormat = getString(R.string.config_scale_y);
		mScaleZFormat = getString(R.string.config_scale_z);
		mMaxControlValueFormat = getString(R.string.config_control_max);

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
		mViewPager = (ViewPager)rootView.findViewById(R.id.config_pager);
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
	private void updateConfigMinidrone1(final View root) {
		if (DEBUG) Log.v(TAG, "updateConfigMinidrone1:");
		// 最大高度設定
		mMaxAltitudeLabel = (TextView)root.findViewById(R.id.max_altitude_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_altitude_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxAltitude = mController.getMaxAltitude();
		try {
			seekbar.setProgress((int) ((mMaxAltitude.current - mMaxAltitude.min) / (mMaxAltitude.max - mMaxAltitude.min) * 1000));
		} catch (Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxAltitude(mMaxAltitude.current);
		// 最大傾斜設定
		mMaxTiltLabel = (TextView)root.findViewById(R.id.max_tilt_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_tilt_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxTilt = mController.getMaxTilt();
		try {
			seekbar.setProgress((int) ((mMaxTilt.current - mMaxTilt.min) / (mMaxTilt.max - mMaxTilt.min) * 1000));
		} catch (Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxTilt(mMaxTilt.current);
		// 最大上昇/降下速度設定
		mMaxVerticalSpeedLabel = (TextView)root.findViewById(R.id.max_vertical_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_vertical_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxVerticalSpeed = mController.getMaxVerticalSpeed();
		try {
			seekbar.setProgress((int) ((mMaxVerticalSpeed.current - mMaxVerticalSpeed.min) / (mMaxVerticalSpeed.max - mMaxVerticalSpeed.min) * 1000));
		} catch (Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxVerticalSpeed(mMaxVerticalSpeed.current);
		// 最大回転速度
		mMaxRotationSpeedLabel = (TextView)root.findViewById(R.id.max_rotation_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_rotation_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxRotationSpeed = mController.getMaxRotationSpeed();
		try {
			seekbar.setProgress((int) ((mMaxRotationSpeed.current - mMaxRotationSpeed.min) / (mMaxRotationSpeed.max - mMaxRotationSpeed.min) * 1000));
		} catch (Exception e) {
			seekbar.setProgress(0);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxRotationSpeed(mMaxRotationSpeed.current);
	}

	/**
	 * ドローン設定画面の準備
	 * @param root
	 */
	private void updateConfigMinidrone2(final View root) {
		if (DEBUG) Log.v(TAG, "updateConfigMinidrone2:");
		// 自動カットアウトモード
		CheckBox checkbox = (CheckBox)root.findViewById(R.id.cutout_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(((DeviceControllerMiniDrone) mController).isCutoffModeEnabled());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
		// 車輪
		checkbox = (CheckBox)root.findViewById(R.id.wheel_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(((DeviceControllerMiniDrone) mController).hasWheel());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
		// 自動離陸モード
		checkbox = (CheckBox)root.findViewById(R.id.auto_takeoff_checkbox);
		if (checkbox != null) {
			try {
				checkbox.setOnCheckedChangeListener(null);
				checkbox.setChecked(((DeviceControllerMiniDrone) mController).isAutoTakeOffModeEnabled());
				checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * 操作設定画面の準備
	 * @param root
	 */
	private void updateConfigOperation(final View root) {
		if (DEBUG) Log.v(TAG, "updateConfigOperation:");
		final Switch sw = (Switch)root.findViewById(R.id.reverse_op_switch);
		if (sw != null) {
			sw.setChecked(mPref != null ? mPref.getBoolean(KEY_REVERSE_OPERATION, false) : false);
			sw.setOnCheckedChangeListener(mOnCheckedChangeListener);
		}
	}

	private float mMaxControlValue;
	private float mScaleX;
	private float mScaleY;
	private float mScaleZ;
	/**
	 * 自動操縦設定画面の準備
	 * @param root
	 */
	private void updateConfigAutopilot(final View root) {
		// 最大制御値設定
		mMaxControlValueLabel = (TextView)root.findViewById(R.id.max_control_value_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_control_value_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxControlValue = mPref.getFloat(KEY_AUTOPILOT_MAX_CONTROL_VALUE, 100);
		try {
			seekbar.setProgress((int) (mMaxControlValue + 500));
		} catch (Exception e) {
			seekbar.setProgress(500);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxControlValue(mMaxControlValue);
		// スケールX設定
		mScaleXLabel = (TextView)root.findViewById(R.id.scale_x_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_x);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mScaleX = mPref.getFloat(KEY_AUTOPILOT_SCALE_X, 1.0f);
		try {
			seekbar.setProgress((int) (mScaleX * 100 + 500));
		} catch (Exception e) {
			seekbar.setProgress(500);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateScaleX(mScaleX);
		// スケールY設定
		mScaleYLabel = (TextView)root.findViewById(R.id.scale_y_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_y);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mScaleY = mPref.getFloat(KEY_AUTOPILOT_SCALE_Y, 1.0f);
		try {
			seekbar.setProgress((int) (mScaleY * 100 + 500));
		} catch (Exception e) {
			seekbar.setProgress(500);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateScaleY(mScaleY);
		// スケールZ設定
		mScaleZLabel = (TextView)root.findViewById(R.id.scale_z_textview);
		seekbar = (SeekBar)root.findViewById(R.id.scale_seekbar_z);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mScaleZ = mPref.getFloat(KEY_AUTOPILOT_SCALE_Z, 1.0f);
		try {
			seekbar.setProgress((int) (mScaleZ * 100 + 500));
		} catch (Exception e) {
			seekbar.setProgress(500);
		}
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateScaleZ(mScaleZ);
	}

	/**
	 * ドローン情報画面の準備
	 * @param root
	 */
	private void updateConfigInfo(final View root) {
		if (DEBUG) Log.v(TAG, "updateConfigInfo:");
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
	 * 最大制御設定値表示を更新
	 * @param max_control_value
	 */
	private void updateMaxControlValue(final float max_control_value) {
		if (mMaxControlValueLabel != null) {
			mMaxControlValueLabel.setText(String.format(mMaxControlValueFormat, max_control_value));
		}
	}

	/**
	 * スケールZ設定表示を更新
	 * @param scale_x
	 */
	private void updateScaleX(final float scale_x) {
		if (mScaleXLabel != null) {
			mScaleXLabel.setText(String.format(mScaleXFormat, scale_x));
		}
	}

	/**
	 * スケールY設定表示を更新
	 * @param scale_y
	 */
	private void updateScaleY(final float scale_y) {
		if (mScaleYLabel != null) {
			mScaleYLabel.setText(String.format(mScaleZFormat, scale_y));
		}
	}

	/**
	 * スケールZ設定表示を更新
	 * @param scale_z
	 */
	private void updateScaleZ(final float scale_z) {
		if (mScaleZLabel != null) {
			mScaleZLabel.setText(String.format(mScaleZFormat, scale_z));
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
					final float altitude = (int) (progress / 100f * (mMaxAltitude.max - mMaxAltitude.min)) / 10f + mMaxAltitude.min;
					updateMaxAltitude(altitude);
					break;
				case R.id.max_tilt_seekbar:
					final float tilt = (int) (progress / 100f * (mMaxTilt.max - mMaxTilt.min)) / 10f + mMaxTilt.min;
					updateMaxTilt(tilt);
					break;
				case R.id.max_vertical_speed_seekbar:
					final float vertical = (int) (progress / 100f * (mMaxVerticalSpeed.max - mMaxVerticalSpeed.min)) / 10f + mMaxVerticalSpeed.min;
					updateMaxVerticalSpeed(vertical);
					break;
				case R.id.max_rotation_speed_seekbar:
					final float rotation = (int) (progress / 1000f * (mMaxRotationSpeed.max - mMaxRotationSpeed.min)) + mMaxRotationSpeed.min;
					updateMaxRotationSpeed(rotation);
					break;
				case R.id.max_control_value_seekbar:	// -500〜+500
					final float max_control_value = progress - 500;
					updateMaxControlValue(max_control_value);
					break;
				case R.id.scale_seekbar_x:
					final float scale_x = (progress - 500) / 100f;
					updateScaleX(scale_x);
					break;
				case R.id.scale_seekbar_y:
					final float scale_y = (progress - 500) / 100f;
					updateScaleY(scale_y);
					break;
				case R.id.scale_seekbar_z:
					final float scale_z = (progress - 500) / 100f;
					updateScaleZ(scale_z);
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
				final float altitude = (int)(seekBar.getProgress() / 100f * (mMaxAltitude.max - mMaxAltitude.min)) / 10f + mMaxAltitude.min;
				if (altitude != mMaxAltitude.current) {
					mController.sendMaxAltitude(altitude);
				}
				break;
			case R.id.max_tilt_seekbar:
				final float tilt = (int)(seekBar.getProgress() / 100f * (mMaxTilt.max - mMaxTilt.min)) / 10f + mMaxTilt.min;
				if (tilt != mMaxTilt.current) {
					mController.sendMaxTilt(tilt);
				}
				break;
			case R.id.max_vertical_speed_seekbar:
				final float vertical = (int)(seekBar.getProgress() / 100f * (mMaxVerticalSpeed.max - mMaxVerticalSpeed.min)) / 10f + mMaxVerticalSpeed.min;
				if (vertical != mMaxVerticalSpeed.current) {
					mController.sendMaxVerticalSpeed(vertical);
				}
				break;
			case R.id.max_rotation_speed_seekbar:
				final float rotation = (int)(seekBar.getProgress() / 1000f * (mMaxRotationSpeed.max - mMaxRotationSpeed.min)) + mMaxRotationSpeed.min;
				if (rotation != mMaxRotationSpeed.current) {
					mController.sendMaxRotationSpeed(rotation);
				}
				break;
			case R.id.max_control_value_seekbar:
				final float max_control_value = seekBar.getProgress() - 500;
				if (max_control_value != mMaxControlValue) {
					mMaxControlValue = max_control_value;
					mPref.edit().putFloat(KEY_AUTOPILOT_MAX_CONTROL_VALUE, max_control_value).apply();
				}
				break;
			case R.id.scale_seekbar_x:
				final float scale_x = (seekBar.getProgress() - 500) / 100f;
				if (scale_x != mScaleX) {
					mScaleX = scale_x;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_X, scale_x).apply();
				}
				break;
			case R.id.scale_seekbar_y:
				final float scale_y = (seekBar.getProgress() - 500) / 100f;
				if (scale_y != mScaleY) {
					mScaleY = scale_y;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_Y, scale_y).apply();
				}
				break;
			case R.id.scale_seekbar_z:
				final float scale_z = (seekBar.getProgress() - 500) / 100f;
				if (scale_z != mScaleZ) {
					mScaleZ = scale_z;
					mPref.edit().putFloat(KEY_AUTOPILOT_SCALE_Z, scale_z).apply();
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
				if (((DeviceControllerMiniDrone) mController).isCutoffModeEnabled() != isChecked) {
					((DeviceControllerMiniDrone) mController).sendCutOutMode(isChecked);
				}
				break;
			case R.id.wheel_checkbox:
				if (((DeviceControllerMiniDrone) mController).hasWheel() != isChecked) {
					((DeviceControllerMiniDrone) mController).sendWheel(isChecked);
				}
				break;
			case R.id.auto_takeoff_checkbox:
				if (((DeviceControllerMiniDrone) mController).isAutoTakeOffModeEnabled() != isChecked) {
					((DeviceControllerMiniDrone) mController).sendAutoTakeOffMode(isChecked);
				}
				break;
			case R.id.reverse_op_switch:
				if (mPref != null) {
					mPref.edit().putBoolean(KEY_REVERSE_OPERATION, isChecked).apply();
				}
				break;
			}
		}
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
			switch (position) {
			case 0:
				view = mInflater.inflate(R.layout.config_minidrone_1, container, false);
				updateConfigMinidrone1(view);
				break;
			case 1:
				view = mInflater.inflate(R.layout.config_minidrone_2, container, false);
				updateConfigMinidrone2(view);
				break;
			case 2:
				view = mInflater.inflate(R.layout.config_operation, container, false);
				updateConfigOperation(view);
				break;
			case 3:
				view = mInflater.inflate(R.layout.config_autopilot, container, false);
				updateConfigAutopilot(view);
				break;
			case 4:
				view = mInflater.inflate(R.layout.config_info, container, false);
				updateConfigInfo(view);
				break;
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
			return 5;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object) {
			return view.equals(object);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			if (DEBUG) Log.v(TAG, "getPageTitle:position=" + position);
			CharSequence result = null;
			switch (position) {
			case 0:
				result = getString(R.string.config_1);
				break;
			case 1:
				result = getString(R.string.config_2);
				break;
			case 2:
				result = getString(R.string.config_3);
				break;
			case 3:
				result = getString(R.string.config_4);
				break;
			case 4:
				result = getString(R.string.config_5);
				break;
			}
			return result;
		}
	}
}
