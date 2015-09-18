package com.serenegiant.flightdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.gl.AttitudeScreenBase;
import com.serenegiant.gl.IModelView;

public class CalibrationFragment extends ControlBaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = CalibrationFragment.class.getSimpleName();

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

	private IModelView mModelView;
	private ImageView mCalOpView;
	private int mState = STATE_STOPPED;

	public CalibrationFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

/*	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	} */

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_calibration, container, false);
		mModelView = (IModelView)rootView.findViewById(R.id.drone_view);
		mModelView.setModel(IModelView.MODEL_BEBOP, AttitudeScreenBase.CTRL_CALIBRATION);
		mCalOpView = (ImageView)rootView.findViewById(R.id.calibration_imageview);

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
		mModelView.onResume();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mController.sendCalibration(true);
			}
		});
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mState != STATE_STOPPED) {
			mController.sendCalibration(false);
		}
		mModelView.onPause();
		super.onPause();
	}

	/**
	 * キャリブレーションを開始した
	 */
	@Override
	protected void onStartCalibration() {
		if (mState != STATE_STOPPED) {
			Log.w(TAG, "onStartCalibration:ステートがおかしい:" + mState);
		}
		mState = STATE_START;
	}

	/**
	 * キャリブレーションが終了した
	 */
	@Override
	protected void onStopCalibration() {
		mState = STATE_STOPPED;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				popBackStack();
			}
		});
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, 2:z
	 */
	@Override
	protected void updateCalibrationAxisChanged(final int axis) {
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
			Log.w(TAG, "updateCalibrationAxisChanged:ステートがおかしい:" + mState + ",axis=" + axis);
		}
	}

}
