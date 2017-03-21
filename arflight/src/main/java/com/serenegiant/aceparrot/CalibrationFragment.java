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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.drone.AttitudeScreenBase;
import com.serenegiant.gameengine.v1.IModelView;
import com.serenegiant.arflight.R;

import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IFlightController;

import static com.serenegiant.aceparrot.AppConst.*;

public class CalibrationFragment extends BaseFlightControllerFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = CalibrationFragment.class.getSimpleName();

	public static CalibrationFragment newInstance(final ARDiscoveryDeviceService device) {
		final CalibrationFragment fragment = new CalibrationFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private static final int STATE_STOPPED = 0;
	private static final int STATE_START = 1;
	private static final int STATE_AXIS_X = 2;
	private static final int STATE_AXIS_Y = 3;
	private static final int STATE_AXIS_Z = 4;
	private static final int STATE_AXIS_NONE = 5;
	private static final int STATE_SUCCESS = 6;
	private static final int STATE_FAILED = 7;

	private static final long POP_BACK_STACK_DELAY_NO_CONTROLLER = 300;

	private IModelView mModelView;
	private TextView mMessageTextView;
	private int mState = STATE_STOPPED;

	public CalibrationFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

/*	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	} */

/*	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	} */

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		onBeforeCreateView();
		final int model;
		switch (getProduct()) {
		case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
			model = IModelView.MODEL_BEBOP2;
			break;
		case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
			model = IModelView.MODEL_BEBOP;
			break;
//		case ARDISCOVERY_PRODUCT_BLESERVICE:			// BlueTooth products category
		case ARDISCOVERY_PRODUCT_MINIDRONE:				// DELOS product
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:	// Delos EVO Light product
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:	// Delos EVO Brick product
		case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL:// Delos EVO Hydrofoil product
		case ARDISCOVERY_PRODUCT_MINIDRONE_DELOS3:		// Delos3 product
		case ARDISCOVERY_PRODUCT_MINIDRONE_WINGX:		// WingX product
			model = IModelView.MODEL_CARGO;
			break;
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// Sky controller product
		case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// Sky controller 2 product
			model = IModelView.MODEL_SKYCONTROLLER;
			break;
		default:
			model = IModelView.MODEL_BEBOP;
			break;
		}
		final SharedPreferences pref = getActivity().getPreferences(0);
		final int color = pref.getInt(KEY_COLOR, getResources().getColor(R.color.RED));
		TextureHelper.genTexture(getActivity(), model, color);

		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_calibration, container, false);

		mModelView = (IModelView)rootView.findViewById(R.id.drone_view);
		mModelView.setModel(model, AttitudeScreenBase.CTRL_CALIBRATION);
		mMessageTextView = (TextView)rootView.findViewById(R.id.cal_msg_textview);
		mMessageTextView.setText(R.string.calibration_title);

		return rootView;
	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		if (DEBUG) Log.v(TAG, "internalOnResume:");
		mModelView.onResume();
		if (mFlightController != null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mFlightController.startCalibration(true);
					queueEvent(mUpdateStateTask, 300);
				}
			});
		} else {
			requestPopBackStack(POP_BACK_STACK_DELAY_NO_CONTROLLER);
		}
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.v(TAG, "internalOnPause:");
		if ((mState != STATE_STOPPED) && (mController instanceof IFlightController)) {
			((IFlightController)mController).startCalibration(false);
		}
		removeEvent(mUpdateStateTask);
		mModelView.onPause();
		super.internalOnPause();
	}

	@Override
	protected boolean canReleaseController() {
		return false;
	}

	@Override
	protected void updateBattery(final IDeviceController controller, final int percent) {

	}

	@Override
	protected void updateWiFiSignal(final IDeviceController controller, final int rssi) {

	}

	@Override
	protected void updateAlarmState(final IDeviceController controller, final int alert_state) {

	}

	@Override
	protected void updateFlyingState(final IDeviceController controller, final int state) {

	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onDisconnect");
		requestPopBackStack(POP_BACK_STACK_DELAY);
		super.onDisconnect(controller);
	}

	/**
	 * キャリブレーションを開始した
	 */
	@Override
	protected void onStartCalibration(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onStartCalibration:");
		if (mState != STATE_STOPPED) {
			Log.w(TAG, "onStartCalibration:ステートがおかしい:" + mState);
		}
		mState = STATE_START;
	}

	/**
	 * キャリブレーションが終了した
	 */
	@Override
	protected void onStopCalibration(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onStopCalibration:");
		mState = STATE_STOPPED;
		// FIXME ここで終了のメッセージを表示する
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mMessageTextView.setText(
					mController instanceof IFlightController && ((IFlightController)mController).needCalibration()
					? R.string.calibration_failed
					: R.string.calibration_success);

				requestPopBackStack(POP_BACK_STACK_DELAY);
			}
		});
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, 2:z
	 */
	@Override
	protected void updateCalibrationAxis(final IDeviceController controller, final int axis) {
		if (DEBUG) Log.v(TAG, "updateCalibrationAxis:axis=" + axis);
		mState = STATE_AXIS_X + axis;
		switch (mState) {
		case STATE_START:
			break;
		case STATE_AXIS_X:
		case STATE_AXIS_Y:
		case STATE_AXIS_Z:
			// 表示中の機体モデルの回転方向を変える
			mModelView.setAxis(axis);
			break;
		case STATE_AXIS_NONE:
			mState = STATE_START;
			break;
		default:
			Log.w(TAG, "updateCalibrationAxis:ステートがおかしい:" + mState + ",axis=" + axis);
		}
	}

	private final Runnable mUpdateStateTask = new Runnable() {
		private int prevState = -1;
		@Override
		public void run() {
			if (prevState != mState) {
				final int state = prevState = mState;
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (state) {
						case STATE_AXIS_X:
							mMessageTextView.setText(R.string.calibration_axis_x);
							break;
						case STATE_AXIS_Y:
							mMessageTextView.setText(R.string.calibration_axis_y);
							break;
						case STATE_AXIS_Z:
							mMessageTextView.setText(R.string.calibration_axis_z);
							break;
						}
					}
				});
			}
			mModelView.setAxis(mState - STATE_AXIS_X);
			queueEvent(this, 200);
		}
	};
}
