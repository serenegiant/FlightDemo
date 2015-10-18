package com.serenegiant.gamepaddiag;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.Joystick;

public class MainActivity extends Activity {
	private static final boolean DEBUG = true;
	private static final String TAG = "MainActivity";

	private Joystick mJoystick;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:");
		setContentView(R.layout.activity_main);
		mJoystick = Joystick.getInstance(this);
		if (savedInstanceState == null) {
//			final Fragment fragment = new MainFragment();
			final Fragment fragment = new MainFragment2();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	@Override
	protected void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mJoystick != null) {
			mJoystick.unregister();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mJoystick != null) {
			mJoystick.release();
			mJoystick = null;
		}
		super.onDestroy();
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
//		if (DEBUG) Log.v(TAG, "dispatchKeyEvent:" + event);
		if (mJoystick != null) {
			if (mJoystick.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
//		if (DEBUG) Log.v(TAG, "dispatchGenericMotionEvent:" + event);
		if (mJoystick != null) {
			mJoystick.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
	}

}
