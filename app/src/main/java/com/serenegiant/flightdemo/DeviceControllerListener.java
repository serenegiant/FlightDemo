package com.serenegiant.flightdemo;

public interface DeviceControllerListener {
	public void onDisconnect();

	public void onUpdateBattery(final byte percent);
}
