package com.serenegiant.arflight.controllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

public class FlightControllerCargoDroneNewAPI extends FlightControllerMiniDroneNewAPI {
	public FlightControllerCargoDroneNewAPI(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}
}
