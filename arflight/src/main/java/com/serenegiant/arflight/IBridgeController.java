package com.serenegiant.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public interface IBridgeController {
	public ARNetworkConfig getBridgeNetConfig();
	public ARDiscoveryDeviceService getDeviceService();
	public ARNetworkALManager getALManager();
	public ARNetworkManager getNetManager();
	public VideoStreamDelegater getVideoStreamDelegater();

	public boolean connectTo(final DeviceInfo info);
	public void disconnectFrom();
	public boolean isConnected();
	public DeviceInfo connectDeviceInfo();
}
