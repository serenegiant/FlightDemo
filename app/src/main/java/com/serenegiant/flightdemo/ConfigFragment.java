package com.serenegiant.flightdemo;

import android.app.Activity;
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
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.AttributeFloat;
import com.serenegiant.arflight.DeviceControllerMiniDrone;

public class ConfigFragment extends ControlFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigFragment.class.getSimpleName();

	public static ConfigFragment newInstance(final ARDiscoveryDeviceService device) {
		final ConfigFragment fragment = new ConfigFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private ViewPager mViewPager;
	private ConfigPagerAdapter mPagerAdapter;

	private TextView mMaxAltitudeLabel;
	private TextView mMaxTiltLabel;
	private TextView mMaxVerticalSpeedLabel;
	private TextView mMaxRotationSpeedLabel;

	private String mMaxAltitudeFormat;
	private String mMaxTiltFormat;
	private String mMaxVerticalSpeedFormat;
	private String mMaxRotationSpeedFormat;


	public ConfigFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
		mMaxAltitudeFormat = getString(R.string.max_altitude);
		mMaxTiltFormat = getString(R.string.max_tilt);
		mMaxVerticalSpeedFormat = getString(R.string.max_vertical_speed);
		mMaxRotationSpeedFormat = getString(R.string.max_rotating_speed);
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
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
		// 最大高度設定
		mMaxAltitudeLabel = (TextView)root.findViewById(R.id.max_altitude_textview);
		SeekBar seekbar = (SeekBar)root.findViewById(R.id.max_altitude_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxAltitude = deviceController.getMaxAltitude();
		seekbar.setProgress((int) ((mMaxAltitude.current - mMaxAltitude.min) / mMaxAltitude.max * 1000));
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxAltitude(mMaxAltitude.current);
		// 最大傾斜設定
		mMaxTiltLabel = (TextView)root.findViewById(R.id.max_tilt_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_tilt_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxTilt = deviceController.getMaxTilt();
		seekbar.setProgress((int) ((mMaxTilt.current - mMaxTilt.min) / mMaxTilt.max * 1000));
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxTilt(mMaxTilt.current);
		// 最大上昇/降下速度設定
		mMaxVerticalSpeedLabel = (TextView)root.findViewById(R.id.max_vertical_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_vertical_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxVerticalSpeed = deviceController.getMaxVerticalSpeed();
		seekbar.setProgress((int) ((mMaxVerticalSpeed.current - mMaxVerticalSpeed.min) / mMaxVerticalSpeed.max * 1000));
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxVerticalSpeed(mMaxVerticalSpeed.current);
		// 最大回転速度
		mMaxRotationSpeedLabel = (TextView)root.findViewById(R.id.max_rotation_speed_textview);
		seekbar = (SeekBar)root.findViewById(R.id.max_rotation_speed_seekbar);
		seekbar.setOnSeekBarChangeListener(null);
		seekbar.setMax(1000);
		mMaxRotationSpeed = deviceController.getMaxRotationSpeed();
		seekbar.setProgress((int)((mMaxRotationSpeed.current - mMaxRotationSpeed.min) / mMaxRotationSpeed.max * 1000));
		seekbar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateMaxRotationSpeed(mMaxRotationSpeed.current);
	}

	/**
	 * ドローン設定画面の準備
	 * @param root
	 */
	private void updateConfigMinidrone2(final View root) {
		// 自動カットアウトモード
		CheckBox checkbox = (CheckBox)root.findViewById(R.id.cutout_checkbox);
		checkbox.setOnCheckedChangeListener(null);
		checkbox.setChecked(((DeviceControllerMiniDrone) deviceController).isCutoffModeEnabled());
		checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		// 車輪
		checkbox = (CheckBox)root.findViewById(R.id.wheel_checkbox);
		checkbox.setOnCheckedChangeListener(null);
		checkbox.setChecked(((DeviceControllerMiniDrone)deviceController).hasWheel());
		checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
		// 自動離陸モード
		checkbox = (CheckBox)root.findViewById(R.id.auto_takeoff_checkbox);
		checkbox.setOnCheckedChangeListener(null);
		checkbox.setChecked(((DeviceControllerMiniDrone)deviceController).isAutoTakeOffModeEnabled());
		checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
	}

	/**
	 * ドローン情報画面の準備
	 * @param root
	 */
	private void updateConfigMinidrone3(final View root) {
		TextView tv = (TextView)root.findViewById(R.id.app_version_textview);
		tv.setText(BuildConfig.VERSION_NAME);
		tv = (TextView)root.findViewById(R.id.product_name_textview);
		tv.setText(deviceController.getName());
		tv = (TextView)root.findViewById(R.id.software_version_textview);
		tv.setText(deviceController.getSoftwareVersion());
		tv = (TextView)root.findViewById(R.id.hardware_version_textview);
		tv.setText(deviceController.getHardwareVersion());
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
	 * シークバーのイベント
	 */
	private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		/**
		 * タッチ処理の開始
		 * @param seekBar
		 */
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		/**
		 * シークバーの値が変更された時の処理
		 * @param seekBar
		 * @param progress
		 * @param fromUser
		 */
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (fromUser) {
				// ユーザーのタッチ処理でシークバーの値が変更された時
				switch (seekBar.getId()) {
				case R.id.max_altitude_seekbar:
					final float altitude = (int) (progress / 100f * mMaxAltitude.max) / 10f + mMaxAltitude.min;
					updateMaxAltitude(altitude);
					break;
				case R.id.max_tilt_seekbar:
					final float tilt = (int) (progress / 100f * mMaxTilt.max) / 10f + mMaxTilt.min;
					updateMaxTilt(tilt);
					break;
				case R.id.max_vertical_speed_seekbar:
					final float vertical = (int) (progress / 100f * mMaxVerticalSpeed.max) / 10f + mMaxVerticalSpeed.min;
					updateMaxVerticalSpeed(vertical);
					break;
				case R.id.max_rotation_speed_seekbar:
					final float rotation = (int) (progress / 100f * mMaxRotationSpeed.max) / 10f + mMaxRotationSpeed.min;
					updateMaxRotationSpeed(rotation);
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
		public void onStopTrackingTouch(SeekBar seekBar) {
			switch (seekBar.getId()) {
			case R.id.max_altitude_seekbar:
				final float altitude = (int)(seekBar.getProgress() / 100f * mMaxAltitude.max) / 10f + mMaxAltitude.min;
				if (altitude != mMaxAltitude.current) {
					deviceController.sendMaxAltitude(altitude);
				}
				break;
			case R.id.max_tilt_seekbar:
				final float tilt = (int)(seekBar.getProgress() / 100f * mMaxTilt.max) / 10f + mMaxTilt.min;
				if (tilt != mMaxTilt.current) {
					deviceController.sendMaxTilt(tilt);
				}
				break;
			case R.id.max_vertical_speed_seekbar:
				final float vertical = (int)(seekBar.getProgress() / 100f * mMaxVerticalSpeed.max) / 10f + mMaxVerticalSpeed.min;
				if (vertical != mMaxVerticalSpeed.current) {
					deviceController.sendMaxVerticalSpeed(vertical);
				}
				break;
			case R.id.max_rotation_speed_seekbar:
				final float rotation = (int)(seekBar.getProgress() / 100f * mMaxRotationSpeed.max) / 10f + mMaxRotationSpeed.min;
				if (rotation != mMaxRotationSpeed.current) {
					deviceController.sendMaxRotationSpeed(rotation);
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
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.cutout_checkbox:
				if (((DeviceControllerMiniDrone)deviceController).isCutoffModeEnabled() != isChecked) {
					((DeviceControllerMiniDrone) deviceController).sendCutOutMode(isChecked);
				}
				break;
			case R.id.wheel_checkbox:
				if (((DeviceControllerMiniDrone)deviceController).hasWheel() != isChecked) {
					((DeviceControllerMiniDrone) deviceController).sendWheel(isChecked);
				}
				break;
			case R.id.auto_takeoff_checkbox:
				if (((DeviceControllerMiniDrone)deviceController).isAutoTakeOffModeEnabled() != isChecked) {
					((DeviceControllerMiniDrone) deviceController).sendAutoTakeOffMode(isChecked);
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
		public Object instantiateItem(ViewGroup container, int position) {
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
				view = mInflater.inflate(R.layout.config_info, container, false);
				updateConfigMinidrone3(view);
				break;
			}
			if (view != null) {
				container.addView(view, position);
			}
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (object instanceof View) {
				container.removeView((View)object);
			}
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			CharSequence result = null;
			switch (position) {
			case 0:
				result = getString(R.string.config_1);
				break;
			case 1:
				result = getString(R.string.config_2);
				break;
			}
			return result;
		}
	}
}
