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
		c2dPort = C2D_PORT;
	}

	@Override
	public JSONObject onSendParams(final JSONObject json) {
		super.onSendParams(json);
//		try {
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_C2DPORT_KEY, 35412);
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY, getFragmentSize());
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY, getMaxFragmentNum());
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY, -1);
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_SKYCONTROLLER_VERSION, "");	// FIXME
//			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_STATUS_KEY, "");	// FIXME
//			if (true/*isSupportStream2()*/) {	// FIXME これは違うかも
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_SERVER_STREAM_PORT_KEY, 5004);
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_SERVER_CONTROL_PORT_KEY, 5005);
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_PACKET_SIZE_KEY, getMaxPacketSize());
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_LATENCY_KEY, getMaxLatency());
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_NETWORK_LATENCY_KEY, getMaxNetworkLatency());
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_BITRATE_KEY, getMaxBitrate());
//				json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_PARAMETER_SETS_KEY, getParamSets());
//			}
//		} catch (JSONException e) {
//			Log.w(TAG, e);
//		}
		return json;
	}

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
