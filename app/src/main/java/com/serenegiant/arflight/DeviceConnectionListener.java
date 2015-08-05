package com.serenegiant.arflight;

public interface DeviceConnectionListener {
	/**
	 * 接続した時のコールバック
	 * @param controller
	 */
	public void onConnect(IDeviceController controller);
	/**
	 * 切断された時のコールバック
	 */
	public void onDisconnect(final IDeviceController controller);
}
