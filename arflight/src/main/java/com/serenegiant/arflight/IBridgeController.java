package com.serenegiant.arflight;

import com.serenegiant.arflight.configs.ARNetworkConfig;

public interface IBridgeController extends IController {
	/** ブリッジ接続用のARNetworkConfigを新規に生成して返す */
	public ARNetworkConfig createBridgeNetConfig();
	public VideoStreamDelegater getVideoStreamDelegater();

	public boolean connectTo(final DeviceInfo info);
	public void disconnectFrom();
	public boolean isConnected();
	public DeviceInfo connectDeviceInfo();
}
