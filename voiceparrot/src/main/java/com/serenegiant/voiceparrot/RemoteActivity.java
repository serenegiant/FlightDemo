package com.serenegiant.voiceparrot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.net.UdpBeacon;
import com.serenegiant.remotegamepad.RemoteJoystickSrv;
import com.serenegiant.utils.BuildCheck;

import java.util.UUID;

public class RemoteActivity extends Activity {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = RemoteActivity.class.getSimpleName();

	private UdpBeacon mUdpBeacon;
	private RemoteJoystickSrv mRemoteJoystickSrv;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);
		NetworkChangedReceiver.enable(getApplicationContext());
		mUdpBeacon = new UdpBeacon(mUdpBeaconCallback);
		try {
			mRemoteJoystickSrv = new RemoteJoystickSrv(this, 9876);
		} catch (final Exception e) {
			mRemoteJoystickSrv = null;
			Log.w(TAG, e);
		}
	}

	@Override
	protected final void onStart() {
		super.onStart();
		if (BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		if (!BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onPause() {
		if (!BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onPause();
	}

	@Override
	protected final void onStop() {
		if (BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		releaseJoystick();
		NetworkChangedReceiver.disable(getApplicationContext());
		if (mUdpBeacon != null) {
			mUdpBeacon.release();
			mUdpBeacon = null;
		}
		super.onDestroy();
	}

	protected void internalOnResume() {
		if (mUdpBeacon != null) {
			mUdpBeacon.start();
		}
	}

	protected void internalOnPause() {
		if (mUdpBeacon != null) {
			mUdpBeacon.stop();
		}
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (mRemoteJoystickSrv != null) {
			if (mRemoteJoystickSrv.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		if (mRemoteJoystickSrv != null) {
			mRemoteJoystickSrv.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
	}

	private void releaseJoystick() {
		if (mRemoteJoystickSrv != null) {
			mRemoteJoystickSrv.release();
			mRemoteJoystickSrv = null;
		}
	}

	private final UdpBeacon.UdpBeaconCallback
		mUdpBeaconCallback = new UdpBeacon.UdpBeaconCallback() {
		@Override
		public void onReceiveBeacon(final UUID uuid, final String s, final int i) {

		}

		@Override
		public void onError(final Exception e) {

		}
	};
}
