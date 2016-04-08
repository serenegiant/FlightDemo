package com.serenegiant.arflight.NewControllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public class FlightControllerBebop2 extends FlightControllerBebop {
	public FlightControllerBebop2(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
	}
}
