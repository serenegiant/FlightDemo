/*
    Copyright (C) 2014 Parrot SA

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in
      the documentation and/or other materials provided with the 
      distribution.
    * Neither the name of Parrot nor the names
      of its contributors may be used to endorse or promote products
      derived from this software without specific prior written
      permission.

    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
    "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
    LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
    FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
    COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
    BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
    OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
    AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
    OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
    OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
    SUCH DAMAGE.
*/
package com.serenegiant.arflight.configs;

import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryConnection;
import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arstream.ARStreamReader;
import com.serenegiant.arflight.IVideoStreamController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ARNetworkConfig {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = ARNetworkConfig.class.getSimpleName();

	public static final int ARSTREAM2_CLIENT_CONTROL_PORT = 55005;
	public static final int ARSTREAM2_CLIENT_STREAM_PORT = 55004;

	protected long pcmdLoopIntervalsMs = 50;
	protected int iobufferC2dNak = -1;
	protected int iobufferC2dAck = -1;
	protected int iobufferC2dEmergency = -1;
	protected int iobufferC2dArstreamAck = -1;
	protected int iobufferD2cNavdata = -1;
	protected int iobufferD2cEvents = -1;
	protected int iobufferD2cArstreamData = -1;

	protected String deviceAddress;
	/** コントローラーが機器(機体)からのデータを受信する際のポート番号 */
	protected int d2cPort = -1;
	/** コントローラーが機器(機体)にデータを送信する際の接続先ポート番号 */
	protected int c2dPort = -1;
	protected int connectionStatus = -1;

	protected final List<ARNetworkIOBufferParam> c2dParams = new ArrayList<ARNetworkIOBufferParam>();
	protected final List<ARNetworkIOBufferParam> d2cParams = new ArrayList<ARNetworkIOBufferParam>();
	protected int commandsBuffers[] = {};

	protected boolean hasVideo = false;
// ARStream用
	protected int fragmentSize = IVideoStreamController.DEFAULT_VIDEO_FRAGMENT_SIZE;
	protected int maxFragmentNum = IVideoStreamController.DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER;
	protected int maxAckInterval = -1;
// ARStream2用
	protected boolean isSupportStream2 = false;
	protected int serverStreamPort = -1;	// 5005
	protected int serverControlPort = -1;	// 5004
	protected int maxPacketSize;
	protected int maxLatency;
	protected int maxNetworkLatency;
	protected int maxBitrate;
	protected String paramSets;

	protected int bleNotificationIDs[] = null;

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	public void release() {
		for (final Iterator<ARNetworkIOBufferParam> iter = c2dParams.iterator() ; iter.hasNext() ;) {
			final ARNetworkIOBufferParam param = iter.next();
			if (param != null) {
				param.dispose();
			}
		}
		c2dParams.clear();
		for (final Iterator<ARNetworkIOBufferParam> iter = d2cParams.iterator() ; iter.hasNext() ;) {
			final ARNetworkIOBufferParam param = iter.next();
			if (param != null) {
				param.dispose();
			}
		}
        d2cParams.clear();
	}

	public String getDeviceAddress() {
		return deviceAddress;
	}

	/**
	 * Return the port number for WiFi devices.
	 */
	public int getD2CPort() {
		return d2cPort;
	}

	/**
	 * コントローラーが機器(機体)にデータを送信する際の接続先ポート番号を取得
	 */
	public int getC2DPort() {
		return c2dPort;
	}

	public int getConnectionStatus() {
		return connectionStatus;
	}

	public long getPCMDLoopIntervalsMs() {
		return pcmdLoopIntervalsMs;
	}
	
	/**
	 * Return a boolean indicating whether the device supports video streaming.
	 */
	public boolean hasVideo() {
		return hasVideo;
	}

	/**
	 * Get the controller to device parameters.
	 *
	 * @note The data shall not be modified nor freed by the user.
	 */
	public List<ARNetworkIOBufferParam> getC2dParamsList() {
		return c2dParams;
	}

	/**
	 * Get the device to controller parameters.
	 *
	 * @note The data shall not be modified nor freed by the user.
	 */
	public List<ARNetworkIOBufferParam> getD2cParamsList() {
		return d2cParams;
	}

	/**
	 * Get the controller to device parameters.
	 *
	 * @note The data shall not be modified nor freed by the user.
	 */
	public ARNetworkIOBufferParam[] getC2dParams() {
		return c2dParams.toArray(new ARNetworkIOBufferParam[c2dParams.size()]);
	}

	/**
	 * Get the device to controller parameters.
	 *
	 * @note The data shall not be modified nor freed by the user.
	 */
	public ARNetworkIOBufferParam[] getD2cParams() {
		return d2cParams.toArray(new ARNetworkIOBufferParam[d2cParams.size()]);
	}

	public int getC2dNackId() {
		return iobufferC2dNak;
	}

	public int getC2dAckId() {
		return iobufferC2dAck;
	}

	public int getC2dEmergencyId() {
		return iobufferC2dEmergency;
	}

	/**
	 * Get an array of buffer IDs from which to read commands.
	 */
	public int[] getCommandsIOBuffers() {
		return commandsBuffers;
	}

	/**
	 * Get the buffer ID of the video stream data channel.
	 */
	public int getVideoDataIOBuffer() {
		return iobufferD2cArstreamData;
	}

	/**
	 * Get the buffer ID of the video stream acknowledgment channel.
	 */
	public int getVideoAckIOBuffer() {
		return iobufferC2dArstreamAck;
	}

	/**
	 * Get the buffer ID of the acknowledged channel on which all the common commands will be sent.
	 *
	 * @warning I insist that it MUST be the ID of an acknowledged IOBuffer. Returning an ID for an
	 * unacknowledged IOBuffer will cause the controller to wait for a notification that will never
	 * come.
	 */
	public int commonCommandsAckedIOBuffer() {
		return iobufferC2dAck;
	}

	/**
	 * specify the ID to notify
	 * Android 4.3 BLE can notify only 4 characteristics
	 */
	public int[] getBLENotificationIDs() {
		return bleNotificationIDs;
	}

	public int getDefaultVideoMaxAckInterval() {
		return maxAckInterval;
	}

	public int getFragmentSize() {
		return fragmentSize;
	}
	public int getMaxFragmentNum() {
		return maxFragmentNum;
	}

	public int getMaxAckInterval() {
		return maxAckInterval;
	}

	public boolean isSupportStream2() {
		return isSupportStream2;
	}

	public int getServerStreamPort() {
		return serverStreamPort;
	}

	public int getServerControlPort() {
		return serverControlPort;
	}

	public int getMaxPacketSize() {
		return maxPacketSize;
	}

	public int getMaxLatency() {
		return maxLatency;
	}

	public int getMaxNetworkLatency() {
		return maxNetworkLatency;
	}

	public int getMaxBitrate() {
		return maxBitrate;
	}

	public String getParamSets() {
		return paramSets;
	}

	/**
	 * Add a StreamReader IOBuffer
	 * @param maxFragmentSize     Maximum size of the fragment to send
	 * @param maxNumberOfFragment Maximum number of the fragment to send
	 */
	public void addStreamReaderIOBuffer(final int maxFragmentSize, final int maxNumberOfFragment) {
		Iterator<ARNetworkIOBufferParam> iter;
		/* add the Stream parameters for the new connection */
		if (iobufferC2dArstreamAck != -1) {
			iter = c2dParams.iterator();
			for ( ; iter.hasNext() ;) {
				final ARNetworkIOBufferParam param = iter.next();
				if (param.getId() == iobufferC2dArstreamAck) {
					c2dParams.remove(param);
					param.dispose();
					iter = c2dParams.iterator();
				}
			}
			c2dParams.add(ARStreamReader.newAckARNetworkIOBufferParam(iobufferC2dArstreamAck));
		}
		if (iobufferD2cArstreamData != -1) {
			iter = d2cParams.iterator();
			for ( ; iter.hasNext() ;) {
				final ARNetworkIOBufferParam param = iter.next();
				if (param.getId() == iobufferD2cArstreamData) {
					d2cParams.remove(param);
					param.dispose();
					iter = d2cParams.iterator();
				}
			}
			d2cParams.add(ARStreamReader.newDataARNetworkIOBufferParam(iobufferD2cArstreamData, maxFragmentSize, maxNumberOfFragment));
		}
	}

	public JSONObject onSendParams(final JSONObject json) {
		if (DEBUG) Log.v(TAG, "onSendParams:" + json);
		try {
			json.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_D2CPORT_KEY, d2cPort);
		} catch (final JSONException e) {
			Log.w(TAG, e);
		}
		return json;
	}

	/**
	 * JSONに含まれる値でパラメータを更新する
	 * @param json
	 * @return
	 */
	public boolean update(final JSONObject json, final String ip) {
		if (DEBUG) Log.v(TAG, "update:ip=" + ip + ", " + json);
		boolean result = false;
		deviceAddress = ip;
		connectionStatus = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_STATUS_KEY, -1);
		c2dPort = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_C2DPORT_KEY, c2dPort);
		maxAckInterval = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY, -1);
//		clientStreamPort = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_CLIENT_STREAM_PORT_KEY, ARSTREAM2_CLIENT_STREAM_PORT);
//		clientControlPort = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_CLIENT_CONTROL_PORT_KEY, ARSTREAM2_CLIENT_CONTROL_PORT);

		final int fragment_size = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY,
			IVideoStreamController.DEFAULT_VIDEO_FRAGMENT_SIZE);
		final int max_fragment_num = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY,
			IVideoStreamController.DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER);
		final int server_control_port = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_SERVER_CONTROL_PORT_KEY, -1);
		final int server_stream_port = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_SERVER_STREAM_PORT_KEY, -1);
		final boolean support_stream2 = ((server_control_port != -1) && (server_stream_port != -1));
		final int max_packet_size = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_PACKET_SIZE_KEY, -1);
		final int max_latency = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_LATENCY_KEY, -1);
		final int max_network_latency = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_NETWORK_LATENCY_KEY, -1);
		final int max_bitrate = json.optInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_MAX_BITRATE_KEY, -1);
		final String params = json.optString(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM2_PARAMETER_SETS_KEY, "");
		if ((fragment_size != fragmentSize) || (max_fragment_num != maxFragmentNum)
				|| (max_packet_size != maxPacketSize) || (max_latency != maxLatency) || (max_network_latency != maxNetworkLatency)
				|| (max_bitrate != maxBitrate) || (!params.equals(paramSets)) || (support_stream2 != isSupportStream2)) {
			// どれかの値が変更された時
      		fragmentSize = fragment_size;
      		maxFragmentNum = max_fragment_num;
      		isSupportStream2 = support_stream2;
      		maxPacketSize = max_packet_size;
      		maxLatency = max_latency;
      		maxNetworkLatency = max_network_latency;
      		maxBitrate = max_bitrate;
			paramSets = params;
			serverControlPort = server_control_port;
			serverStreamPort = server_stream_port;
			result = true;
		}
		return result;
  	}
}
