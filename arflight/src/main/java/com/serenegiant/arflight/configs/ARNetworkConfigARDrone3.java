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

import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_FRAME_TYPE_ENUM;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.parrot.arsdk.arstream.ARStreamReader;

import java.util.Iterator;

public class ARNetworkConfigARDrone3 extends ARNetworkConfig {
    private static final String TAG = ARNetworkConfigARDrone3.class.getSimpleName();

    public ARNetworkConfigARDrone3() {
        pcmdLoopIntervalsMs = 25;
        iobufferC2dNak = 10;
        iobufferC2dAck = 11;
        iobufferC2dEmergency = 12;
        iobufferC2dArstreamAck = 13;
        iobufferD2cNavdata = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 1;
        iobufferD2cEvents = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 2;
        iobufferD2cArstreamData = (ARNetworkALManager.ARNETWORKAL_MANAGER_WIFI_ID_MAX / 2) - 3;
        
        d2cPort = 54321;
        c2dPort = 43210;
        
        hasVideo = true;
        maxAckInterval = ARStreamReader.DEFAULT_MAX_ACK_INTERVAL;
        
        bleNotificationIDs = null;
        
        // コントローラー => device(機体)へのパラメータ
		for (final Iterator<ARNetworkIOBufferParam> iter = c2dParams.iterator() ; iter.hasNext() ;) {
			final ARNetworkIOBufferParam param = iter.next();
			if (param != null) {
				param.dispose();
			}
		}
        c2dParams.clear();
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
		c2dParams.add(ARStreamReader.newAckARNetworkIOBufferParam(iobufferC2dArstreamAck));

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
        };
    }
    
}
