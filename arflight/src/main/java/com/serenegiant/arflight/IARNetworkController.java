package com.serenegiant.arflight;

import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public interface IARNetworkController {
	public ARNetworkALManager getNetALManager();
	public ARNetworkManager getNetManager();
	public ARNetworkConfig getNetConfig();
}
