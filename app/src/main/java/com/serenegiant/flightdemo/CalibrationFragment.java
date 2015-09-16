package com.serenegiant.flightdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.serenegiant.gl.AttitudeScreenBase;
import com.serenegiant.gl.IModelView;

public class CalibrationFragment extends ControlBaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = CalibrationFragment.class.getSimpleName();


	private IModelView mModelView;
	private ImageView mCalOpView;
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
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		mModelView.onPause();
		super.onPause();
	}

}
