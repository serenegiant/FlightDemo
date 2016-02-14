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

public final class ARNetworkConfigMiniDrone extends ARNetworkConfig {
    private static final String TAG = ARNetworkConfigMiniDrone.class.getSimpleName();
    
    public ARNetworkConfigMiniDrone() {
        pcmdLoopIntervalsMs = 50;
        iobufferC2dNak = 10;
        iobufferC2dAck = 11;
        iobufferC2dEmergency = 12;
        iobufferD2cNavdata = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 1;
        iobufferD2cEvents = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 2;
        
        hasVideo = false;
        videoMaxAckInterval = -1;
        
        final int ackOffset = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2);
        bleNotificationIDs = new int[] {
            iobufferD2cNavdata,
            iobufferD2cEvents,
            (iobufferC2dAck + ackOffset),
            (iobufferC2dEmergency + ackOffset)
        };

        // コントローラー => device(機体)へのパラメータ
        c2dParams.clear();
        c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dNak,			    		// ID
            ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,					// FRAME type
            20,																			// Time between send, ミリ秒単かな?
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,				// ackTimeoutMs
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,				// numberOfRetry
            1,																			// numberOfCell
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,		// copyMaxSize
            true)																		// isOverwriting
        );
        c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dAck,						// ID
            ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,			// FRAME type
            20,																			// Time between send, ミリ秒単かな?
            500,																		// ackTimeoutMs
            3,																			// numberOfRetry
            20,																			// numberOfCell
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,		// copyMaxSize
            false)																		// isOverwriting
        );
        c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dEmergency,					// ID
            ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,			// FRAME type
            1,																			// Time between send, ミリ秒単かな?
            100,																		// ackTimeoutMs
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,				// numberOfRetry
            1,																			// numberOfCell
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,		// copyMaxSize
            false)																		// isOverwriting
        );

        // device(機体) => コントローラーへのパラメータ
        d2cParams.clear();
        d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cNavdata,					// ID
            ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,					// FRAME type
            20,																			// Time between send, ミリ秒単かな?
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,				// ackTimeoutMs
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,				// numberOfRetry
            20,																			// numberOfCell
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,		// copyMaxSize
            false)																		// isOverwriting
        );
        d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cEvents,						// ID
            ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,			// FRAME type
            20,																			// Time between send, ミリ秒単かな?
            500,																		// ackTimeoutMs
            3,																			// numberOfRetry
            20,																			// numberOfCell
            ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,		// copyMaxSize
            false)																		// isOverwriting
        );
        
        commandsBuffers = new int[] {
            iobufferD2cNavdata,
            iobufferD2cEvents,
        };
    }
}
