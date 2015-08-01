package com.serenegiant.arflight;

public interface DeviceControllerListener {
	public void onDisconnect();

	public void onUpdateBattery(final byte percent);

	public void onFlatTrimUpdate(final boolean success);

	public void onFlyingStateChangedUpdate(final int state);
}
