package com.serenegiant.autoparrot;

import android.os.Bundle;
import android.text.TextUtils;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.arflight.DeviceInfo;

import static com.serenegiant.aceparrot.AppConst.*;

public class AutoPilotFragment2NewAPI extends BaseAutoPilotFragment {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = AutoPilotFragment2NewAPI.class.getSimpleName();

	public static AutoPilotFragment2NewAPI newInstance(final ARDiscoveryDeviceService device, final String pref_name, final int mode) {

		final AutoPilotFragment2NewAPI fragment = new AutoPilotFragment2NewAPI();
		final Bundle args = fragment.setDevice(device, true);
		fragment.mPrefName =  TextUtils.isEmpty(pref_name) ? TAG : pref_name;
		fragment.mMode = mode;
		args.putString(KEY_PREF_NAME_AUTOPILOT, fragment.mPrefName);
		args.putInt(KEY_AUTOPILOT_MODE, fragment.mMode);
		return fragment;
	}

	public static AutoPilotFragment2NewAPI newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info, final String pref_name, final int mode) {

		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		final AutoPilotFragment2NewAPI fragment = new AutoPilotFragment2NewAPI();
		final Bundle args = fragment.setBridge(device, info, true);
		fragment.mPrefName =  TextUtils.isEmpty(pref_name) ? TAG : pref_name;
		fragment.mMode = mode;
		args.putString(KEY_PREF_NAME_AUTOPILOT, fragment.mPrefName);
		args.putInt(KEY_AUTOPILOT_MODE, fragment.mMode);
		return fragment;
	}

	public AutoPilotFragment2NewAPI() {
		super();
		// デフォルトコンストラクタが必要
	}
}
