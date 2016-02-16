package com.serenegiant.arflight.configs;

import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_FRAME_TYPE_ENUM;

import org.json.JSONObject;

import java.util.Iterator;

public class ARNetworkConfigBridge extends ARNetworkConfig {
	private static final String TAG = ARNetworkConfigBridge.class.getSimpleName();

	private final String skyControllerVersion;
	public ARNetworkConfigBridge(final ARNetworkConfig src) {
		skyControllerVersion = src instanceof ARNetworkConfigSkyController ? ((ARNetworkConfigSkyController)src).getSkyControllerVersion() : "";
		pcmdLoopIntervalsMs = src.pcmdLoopIntervalsMs;
		iobufferC2dNak = src.iobufferC2dNak;
		iobufferC2dAck = src.iobufferC2dAck;
		iobufferC2dEmergency = src.iobufferC2dEmergency;
		iobufferC2dArstreamAck = src.iobufferC2dArstreamAck;
		iobufferD2cNavdata = src.iobufferD2cNavdata;
		iobufferD2cEvents = src.iobufferD2cEvents;
		iobufferD2cArstreamData = src.iobufferD2cArstreamData;

		connectionStatus = src.connectionStatus;
		deviceAddress = src.deviceAddress;

		d2cPort = 54321;
		c2dPort = 43210;

		hasVideo = true;
// ARStream用
		fragmentSize = src.fragmentSize;
		maxFragmentNum = src.maxFragmentNum;
		maxAckInterval = src.maxAckInterval;
// ARStream2用
		isSupportStream2 = src.isSupportStream2;
		clientStreamPort = src.clientStreamPort;
		clientControlPort = src.clientControlPort;
		serverStreamPort = src.serverStreamPort;
		serverControlPort = src.serverControlPort;
		maxPacketSize = src.maxPacketSize;
		maxLatency = src.maxLatency;
		maxNetworkLatency = src.maxNetworkLatency;
		maxBitrate = src.maxBitrate;
		paramSets = src.paramSets;

		bleNotificationIDs = null;

        // コントローラー => device(機体)へのパラメータ
		for (final Iterator<ARNetworkIOBufferParam> iter = c2dParams.iterator() ; iter.hasNext() ;) {
			final ARNetworkIOBufferParam param = iter.next();
			if (param != null) {
				param.dispose();
			}
		}
		c2dParams.clear();
		c2dParams.addAll(src.c2dParams);
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dNak,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,
			1,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			2,
			128,
			true));
		c2dParams.add (new ARNetworkIOBufferParam (iobufferC2dAck,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			128,
			false));
		c2dParams.add (new ARNetworkIOBufferParam (iobufferC2dEmergency,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			1,
			100,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			1,
			128,
			false));
//		c2dParams.add(ARStreamReader.newAckARNetworkIOBufferParam(iobufferC2dArstreamAck));

        // device(機体) => コントローラーへのパラメータ
		for (final Iterator<ARNetworkIOBufferParam> iter = d2cParams.iterator() ; iter.hasNext() ;) {
			final ARNetworkIOBufferParam param = iter.next();
			if (param != null) {
				param.dispose();
			}
		}
		d2cParams.clear();
		d2cParams.add (new ARNetworkIOBufferParam (iobufferD2cNavdata,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			20,
			128,
			false));
		d2cParams.add (new ARNetworkIOBufferParam (iobufferD2cEvents,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			128,
			false));

		commandsBuffers = new int[] {
			iobufferD2cNavdata,
			iobufferD2cEvents,
			iobufferC2dEmergency,
		};
	}

//	@Override
//	public JSONObject onSendParams(final JSONObject json) {
//		super.onSendParams(json);
//		return json;
//	}

//	@Override
//	public boolean update(final JSONObject json, final String ip) {
//		return super.update(json, ip);
//	}
}
