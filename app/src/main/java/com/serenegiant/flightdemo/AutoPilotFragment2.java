package com.serenegiant.flightdemo;

import android.os.*;
import android.text.TextUtils;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceInfo;

import static com.serenegiant.flightdemo.AppConst.*;

public class AutoPilotFragment2 extends BaseAutoPilotFragment {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = AutoPilotFragment2.class.getSimpleName();

	public static AutoPilotFragment2 newInstance(final ARDiscoveryDeviceService device, final String pref_name, final int mode) {

		final AutoPilotFragment2 fragment = new AutoPilotFragment2();
		final Bundle args = fragment.setDevice(device);
		fragment.mPrefName =  TextUtils.isEmpty(pref_name) ? TAG : pref_name;
		fragment.mMode = mode;
		args.putString(KEY_PREF_NAME_AUTOPILOT, fragment.mPrefName);
		args.putInt(KEY_AUTOPILOT_MODE, fragment.mMode);
		return fragment;
	}

	public static AutoPilotFragment2 newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info, final String pref_name, final int mode) {

		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		final AutoPilotFragment2 fragment = new AutoPilotFragment2();
		final Bundle args = fragment.setBridge(device, info);
		fragment.mPrefName =  TextUtils.isEmpty(pref_name) ? TAG : pref_name;
		fragment.mMode = mode;
		args.putString(KEY_PREF_NAME_AUTOPILOT, fragment.mPrefName);
		args.putInt(KEY_AUTOPILOT_MODE, fragment.mMode);
		return fragment;
	}

	public AutoPilotFragment2() {
		super();
		// デフォルトコンストラクタが必要
	}
}
