package com.serenegiant.arflight.controllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

public class FlightControllerCargoDrone extends FlightControllerMiniDrone {
	public FlightControllerCargoDrone(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}
}
