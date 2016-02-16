package com.serenegiant.arflight.configs;

import com.parrot.arsdk.ardiscovery.ARDiscoveryConnection;

import org.json.JSONObject;

/**
 * ARNetworkConfigARDrone3のシノニム
 */
public class ARNetworkConfigSkyController extends ARNetworkConfigARDrone3 {
	private static final String TAG = ARNetworkConfigSkyController.class.getSimpleName();

	private static final int C2D_PORT = 35412;

	private String version = "";
	public ARNetworkConfigSkyController() {
		super();
//		c2dPort = C2D_PORT;
	}

//	@Override
//	public JSONObject onSendParams(final JSONObject json) {
//		super.onSendParams(json);
//		return json;
//	}

	@Override
	public boolean update(final JSONObject json, final String ip) {
		boolean result = super.update(json, ip);
		version = json.optString(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_SKYCONTROLLER_VERSION, version);
		return result;
	}

	public String getSkyControllerVersion() {
		return version;
	}
}
