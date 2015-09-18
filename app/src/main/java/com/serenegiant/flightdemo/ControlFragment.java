package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceControllerListener;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IVideoStreamController;

public abstract class ControlFragment extends ControlBaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = "ControlFragment";

	public ControlFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

/*	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	} */

/*	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	} */

/*	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
	} */

/*	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
	} */

/*	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	} */

/*	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
	} */

/*	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		super.onPause();
	} */

	/**
	 * 移動停止
	 */
	protected void stopMove() {
		if (DEBUG) Log.v(TAG, "stopMove:");
		if (mController != null) {
			mController.setMove(0, 0, 0, 0, 0);
		}
	}

	/**
	 * 非常停止指示
	 */
	protected void emergencyStop() {
		stopMove();
		if (mController != null) {
			mController.sendEmergency();
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		super.onConnect(controller);
		stopMove();
		startVideoStreaming();
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		stopMove();
		stopVideoStreaming();
		stopDeviceController(true);
		super.onDisconnect(controller);
	}

}
