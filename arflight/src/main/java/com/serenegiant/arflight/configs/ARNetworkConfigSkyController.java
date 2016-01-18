package com.serenegiant.arflight.configs;

import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_FRAME_TYPE_ENUM;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.parrot.arsdk.arstream.ARStreamReader;

/**
 * Created by saki on 15/10/31.
 */
public class ARNetworkConfigSkyController extends ARNetworkConfig {
	private static final String TAG = ARNetworkConfigSkyController.class.getSimpleName();

	public ARNetworkConfigSkyController() {
		// FIXME これはまだARDRONE3のをコピーしただけ
		pcmdLoopIntervalsMs = 25;
		iobufferC2dNak = 10;
		iobufferC2dAck = 11;
		iobufferC2dEmergency = 12;
		iobufferC2dArstreamAck = 13;
		iobufferD2cNavdata = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 1;
		iobufferD2cEvents = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 2;
		iobufferD2cArstreamData = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 3;

		inboundPort = 54321;
		outboundPort = 43210;

		hasVideo = true;
		videoMaxAckInterval = ARStreamReader.DEFAULT_MAX_ACK_INTERVAL;

		bleNotificationIDs = null;

		c2dParams.clear();
		c2dParams.add (new ARNetworkIOBufferParam(iobufferC2dNak,
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
/*		c2dParams.add (new ARNetworkIOBufferParam (iobufferC2dEmergency,
					  ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
					  1,
					  100,
					  ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
					  1,
					  128,
					  false)); */

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
		};
	}
}
