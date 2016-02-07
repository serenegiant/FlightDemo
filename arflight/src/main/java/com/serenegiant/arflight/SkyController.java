package com.serenegiant.arflight;

import android.content.Context;

import com.parrot.arsdk.arcommands.ARCommand;
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

//================================================================================
// ARSDK3からのコールバックリスナー関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		super.registerARCommandsListener();
		// FIXME SkyController用コールバックを登録
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		// FIXME SkyController用コールバックを登録解除
		super.unregisterARCommandsListener();
	}

//================================================================================
//================================================================================
	@Override
	public int getBattery() {
		return 0;
	}

}
