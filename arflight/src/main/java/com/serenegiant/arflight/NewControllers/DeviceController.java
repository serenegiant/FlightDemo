package com.serenegiant.arflight.NewControllers;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.Controllers.DeviceConnectionListener;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.lang.ref.WeakReference;
import java.sql.Date;

public class DeviceController implements IDeviceController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = DeviceController.class.getSimpleName();

	private final WeakReference<Context> mWeakContext;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;
	public DeviceController(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		mWeakContext = new WeakReference<Context>(context);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
		mDeviceService = service;
		mNetConfig = net_config;
	}

	@Override
	public void release() {

	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getProductName() {
		return null;
	}

	@Override
	public int getProductId() {
		return 0;
	}

	@Override
	public String getSoftwareVersion() {
		return null;
	}

	@Override
	public String getHardwareVersion() {
		return null;
	}

	@Override
	public String getSerial() {
		return null;
	}

	@Override
	public int getAlarm() {
		return 0;
	}

	@Override
	public int getBattery() {
		return 0;
	}

	@Override
	public void addListener(final DeviceConnectionListener mListener) {

	}

	@Override
	public void removeListener(final DeviceConnectionListener mListener) {

	}

	@Override
	public int getState() {
		return 0;
	}

	@Override
	public boolean start() {
		return false;
	}

	@Override
	public void cancelStart() {

	}

	@Override
	public void stop() {

	}

	@Override
	public boolean isStarted() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean sendDate(final Date currentDate) {
		return false;
	}

	@Override
	public boolean sendTime(final Date currentDate) {
		return false;
	}

	@Override
	public boolean requestAllSettings() {
		return false;
	}

	@Override
	public boolean requestAllStates() {
		return false;
	}

	@Override
	public ARDiscoveryDeviceService getDeviceService() {
		return null;
	}

	@Override
	public ARNetworkALManager getNetALManager() {
		return null;
	}

	@Override
	public ARNetworkManager getNetManager() {
		return null;
	}

	@Override
	public ARNetworkConfig getNetConfig() {
		return null;
	}
}
