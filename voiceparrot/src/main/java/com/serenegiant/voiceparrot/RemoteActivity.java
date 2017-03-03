package com.serenegiant.voiceparrot;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.net.UdpBeacon;
import com.serenegiant.utils.BuildCheck;

import java.util.UUID;

import jp.co.rediscovery.arflight.ManagerFragment;

public class RemoteActivity extends Activity {

	private UdpBeacon mUdpBeacon;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote);
		NetworkChangedReceiver.enable(getApplicationContext());
		mUdpBeacon = new UdpBeacon(mUdpBeaconCallback);
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
		hideProgress();
		NetworkChangedReceiver.disable(getApplicationContext());
		super.onDestroy();
	}

	protected void internalOnResume() {
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	protected void internalOnPause() {
		if (mJoystick != null) {
			mJoystick.unregister();
		}
		if (isFinishing()) {
			ManagerFragment.releaseAll(this);
		}
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (mJoystick != null) {
			if (mJoystick.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		if (mJoystick != null) {
			mJoystick.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
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
