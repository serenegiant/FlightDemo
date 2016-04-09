package com.serenegiant.arflight.controllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.IBridgeController;

public class FlightControllerBebop2 extends FlightControllerBebop {
	public FlightControllerBebop2(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}

	public FlightControllerBebop2(final Context context, final IBridgeController bridge) {
		super(context, bridge);
	}
}
