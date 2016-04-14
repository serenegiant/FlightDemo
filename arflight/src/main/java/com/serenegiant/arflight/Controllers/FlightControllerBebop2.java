package com.serenegiant.arflight.controllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.ISkyController;

public class FlightControllerBebop2 extends FlightControllerBebop {
	public FlightControllerBebop2(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}

	public FlightControllerBebop2(final Context context, final ISkyController bridge) {
		super(context, bridge);
	}
}
