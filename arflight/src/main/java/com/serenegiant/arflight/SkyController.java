package com.serenegiant.arflight;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.arflight.configs.ARNetworkConfigARDrone3;
import com.serenegiant.arflight.configs.ARNetworkConfigSkyController;

import java.sql.Date;

/**
 * Created by saki on 15/10/31.
 */
public class SkyController extends DeviceController implements IBridgeController, IWiFiController {
	private static final boolean DEBUG = false;				// FIXME 実働時はfalseにすること
	private static final String TAG = SkyController.class.getSimpleName();

	public SkyController(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigSkyController());
		mInfo = new AttributeDevice();
	}

	@Override
	public int getBattery() {
		return 0;
	}

}
