package com.serenegiant.gamepaddiag;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.Joystick;

public class MainActivity extends FragmentActivity {
//	private static final boolean DEBUG = false;	// FIXME 実同時はfalseにすること
//	private static final String TAG = "MainActivity";

	private Joystick mJoystick;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		if (DEBUG) Log.v(TAG, "onCreate:");
		setContentView(R.layout.activity_main);
		final ViewPager pager = (ViewPager)findViewById(R.id.viewpager);
		final GamepadFragmentPagerAdapter adapter = new GamepadFragmentPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
		mJoystick = Joystick.getInstance(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		if (DEBUG) Log.v(TAG, "onResume:");
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	@Override
	protected void onPause() {
//		if (DEBUG) Log.v(TAG, "onPause:");
		if (mJoystick != null) {
			mJoystick.unregister();
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
//		if (DEBUG) Log.v(TAG, "onDestroy:");
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

	private static class GamepadFragmentPagerAdapter extends FragmentPagerAdapter {

		public GamepadFragmentPagerAdapter(final FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(final int position) {
			switch (position) {
			case 0:
				return new MainFragment();
			case 1:
				return new MainFragment2();
			default:
				return null;
			}
		}

		@Override
		public int getCount() {
			return 2;
		}
	}
}
