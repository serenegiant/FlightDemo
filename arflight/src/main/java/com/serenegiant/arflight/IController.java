package com.serenegiant.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public interface IController {
	/** コントローラーに関連付けられているARDiscoveryDeviceServiceを取得 */
	public ARDiscoveryDeviceService getDeviceService();
	public ARNetworkALManager getNetALManager();
	public ARNetworkManager getNetManager();
	public ARNetworkConfig getNetConfig();
}
