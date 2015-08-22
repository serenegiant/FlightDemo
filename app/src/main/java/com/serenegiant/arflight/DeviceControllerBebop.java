package com.serenegiant.arflight;


import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED_ENABLED_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_REASON_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED_MOTORERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandARDrone3CameraStateOrientationListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3DebugGPSDebugStateNbSatelliteChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3GPSSettingsStateGPSFixStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3GPSSettingsStateGPSUpdateStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3GPSSettingsStateHomeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3GPSSettingsStateResetHomeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordEventPictureEventChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStatePictureStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStateVideoStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaStreamingStateVideoEnableChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3NetworkSettingsStateWifiSelectionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3NetworkStateAllWifiAuthChannelChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3NetworkStateAllWifiScanChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3NetworkStateWifiAuthChannelListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3NetworkStateWifiScanListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStateExpositionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStatePictureFormatChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStateSaturationChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStateTimelapseChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PictureSettingsStateVideoAutorecordChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingSettingsStateAbsolutControlChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingSettingsStateMaxAltitudeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingSettingsStateMaxTiltChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateAlertStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateAltitudeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateAttitudeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateAutoTakeOffModeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateFlatTrimChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateFlyingStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateNavigateHomeStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStatePositionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3PilotingStateSpeedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateMotorErrorLastErrorChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateMotorErrorStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateMotorFlightsStatusChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateMotorSoftwareVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateProductGPSVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SettingsStateProductMotorVersionListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SpeedSettingsStateHullProtectionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SpeedSettingsStateMaxRotationSpeedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SpeedSettingsStateMaxVerticalSpeedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3SpeedSettingsStateOutdoorChangedListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;

public class DeviceControllerBebop extends DeviceController {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static String TAG = "DeviceControllerBebop";


	public DeviceControllerBebop(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigARDrone3());
		mAttributeDrone = new StatusDrone();
		mCutOffMode = true;
	}

//================================================================================
// 機体からの状態・データコールバック関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		super.registerARCommandsListener();

		ARCommand.setARDrone3MediaRecordStatePictureStateChangedListener(mMediaRecordStatePictureStateChangedListener);
		ARCommand.setARDrone3MediaRecordStatePictureStateChangedV2Listener(mMediaRecordStatePictureStateChangedV2Listener);
		ARCommand.setARDrone3MediaRecordEventPictureEventChangedListener(mMediaRecordEventPictureEventChangedListener);
		ARCommand.setARDrone3MediaRecordStateVideoStateChangedListener(mMediaRecordStateVideoStateChangedListener);
		ARCommand.setARDrone3PilotingStateFlatTrimChangedListener(mPilotingStateFlatTrimChangedListener);
		ARCommand.setARDrone3PilotingStateFlyingStateChangedListener(mPilotingStateFlyingStateChangedListener);
		ARCommand.setARDrone3PilotingStateAlertStateChangedListener(mPilotingStateAlertStateChangedListener);
		ARCommand.setARDrone3PilotingStateNavigateHomeStateChangedListener(mPilotingStateNavigateHomeStateChangedListener);
		ARCommand.setARDrone3PilotingStatePositionChangedListener(mPilotingStatePositionChangedListener);
		ARCommand.setARDrone3PilotingStateSpeedChangedListener(mPilotingStateSpeedChangedListener);
		ARCommand.setARDrone3PilotingStateAttitudeChangedListener(mPilotingStateAttitudeChangedListener);
		ARCommand.setARDrone3PilotingStateAutoTakeOffModeChangedListener (mPilotingStateAutoTakeOffModeChangedListener);
		ARCommand.setARDrone3PilotingStateAltitudeChangedListener(mPilotingStateAltitudeChangedListener);
		ARCommand.setARDrone3NetworkStateWifiScanListChangedListener(mNetworkStateWifiScanListChangedListener);
		ARCommand.setARDrone3NetworkStateAllWifiScanChangedListener(mNetworkStateAllWifiScanChangedListener);
		ARCommand.setARDrone3NetworkStateWifiAuthChannelListChangedListener(mNetworkStateWifiAuthChannelListChangedListener);
		ARCommand.setARDrone3NetworkStateAllWifiAuthChannelChangedListener(mNetworkStateAllWifiAuthChannelChangedListener);
		ARCommand.setARDrone3PilotingSettingsStateMaxAltitudeChangedListener (mPilotingSettingsStateMaxAltitudeChangedListener);
		ARCommand.setARDrone3PilotingSettingsStateMaxTiltChangedListener (mPilotingSettingsStateMaxTiltChangedListener);
		ARCommand.setARDrone3PilotingSettingsStateAbsolutControlChangedListener(mPilotingSettingsStateAbsolutControlChangedListener);
		ARCommand.setARDrone3SpeedSettingsStateMaxVerticalSpeedChangedListener (mSettingsStateMaxVerticalSpeedChangedListener);
		ARCommand.setARDrone3SpeedSettingsStateMaxRotationSpeedChangedListener (mSpeedSettingsStateMaxRotationSpeedChangedListener);
		ARCommand.setARDrone3SpeedSettingsStateHullProtectionChangedListener(mSpeedSettingsStateHullProtectionChangedListener);
		ARCommand.setARDrone3SpeedSettingsStateOutdoorChangedListener(mSpeedSettingsStateOutdoorChangedListener);
		ARCommand.setARDrone3NetworkSettingsStateWifiSelectionChangedListener(mNetworkSettingsStateWifiSelectionChangedListener);
		ARCommand.setARDrone3SettingsStateProductMotorVersionListChangedListener(mSettingsStateProductMotorVersionListChangedListener);
		ARCommand.setARDrone3SettingsStateProductGPSVersionChangedListener(mSettingsStateProductGPSVersionChangedListener);
		ARCommand.setARDrone3SettingsStateMotorErrorStateChangedListener(mSettingsStateMotorErrorStateChangedListener);
		ARCommand.setARDrone3SettingsStateMotorSoftwareVersionChangedListener(mSettingsStateMotorSoftwareVersionChangedListener);
		ARCommand.setARDrone3SettingsStateMotorFlightsStatusChangedListener(mSettingsStateMotorFlightsStatusChangedListener);
		ARCommand.setARDrone3SettingsStateMotorErrorLastErrorChangedListener(mSettingsStateMotorErrorLastErrorChangedListener);
		ARCommand.setARDrone3PictureSettingsStatePictureFormatChangedListener(mPictureSettingsStatePictureFormatChangedListener);
		ARCommand.setARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener(mPictureSettingsStateAutoWhiteBalanceChangedListener);
		ARCommand.setARDrone3PictureSettingsStateExpositionChangedListener(mPictureSettingsStateExpositionChangedListener);
		ARCommand.setARDrone3PictureSettingsStateSaturationChangedListener(mPictureSettingsStateSaturationChangedListener);
		ARCommand.setARDrone3PictureSettingsStateTimelapseChangedListener(mPictureSettingsStateTimelapseChangedListener);
		ARCommand.setARDrone3PictureSettingsStateVideoAutorecordChangedListener(mPictureSettingsStateVideoAutorecordChangedListener);
		ARCommand.setARDrone3MediaStreamingStateVideoEnableChangedListener(mMediaStreamingStateVideoEnableChangedListener);
		ARCommand.setARDrone3GPSSettingsStateHomeChangedListener(mGPSSettingsStateHomeChangedListener);
		ARCommand.setARDrone3GPSSettingsStateResetHomeChangedListener(mGPSSettingsStateResetHomeChangedListener);
		ARCommand.setARDrone3GPSSettingsStateGPSFixStateChangedListener(mGPSSettingsStateGPSFixStateChangedListener);
		ARCommand.setARDrone3GPSSettingsStateGPSUpdateStateChangedListener(mGPSSettingsStateGPSUpdateStateChangedListener);
		ARCommand.setARDrone3CameraStateOrientationListener(mCameraStateOrientationListener);
		ARCommand.setARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener(mDebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener);
		ARCommand.setARDrone3DebugGPSDebugStateNbSatelliteChangedListener(mDebugGPSDebugStateNbSatelliteChangedListener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setARDrone3MediaRecordStatePictureStateChangedListener(null);
		ARCommand.setARDrone3MediaRecordStatePictureStateChangedV2Listener(null);
		ARCommand.setARDrone3MediaRecordStateVideoStateChangedListener(null);
		ARCommand.setARDrone3PilotingStateFlatTrimChangedListener(null);
		ARCommand.setARDrone3PilotingStateFlyingStateChangedListener(null);
		ARCommand.setARDrone3PilotingStateAlertStateChangedListener(null);
		ARCommand.setARDrone3PilotingStateNavigateHomeStateChangedListener(null);
		ARCommand.setARDrone3PilotingStatePositionChangedListener(null);
		ARCommand.setARDrone3PilotingStateSpeedChangedListener(null);
		ARCommand.setARDrone3PilotingStateAttitudeChangedListener(null);
		ARCommand.setARDrone3PilotingStateAutoTakeOffModeChangedListener(null);
		ARCommand.setARDrone3PilotingStateAltitudeChangedListener(null);
		ARCommand.setARDrone3NetworkStateWifiScanListChangedListener(null);
		ARCommand.setARDrone3NetworkStateAllWifiScanChangedListener(null);
		ARCommand.setARDrone3NetworkStateWifiAuthChannelListChangedListener(null);
		ARCommand.setARDrone3NetworkStateAllWifiAuthChannelChangedListener(null);
		ARCommand.setARDrone3PilotingSettingsStateMaxAltitudeChangedListener(null);
		ARCommand.setARDrone3PilotingSettingsStateMaxTiltChangedListener(null);
		ARCommand.setARDrone3PilotingSettingsStateAbsolutControlChangedListener(null);
		ARCommand.setARDrone3SpeedSettingsStateMaxVerticalSpeedChangedListener(null);
		ARCommand.setARDrone3SpeedSettingsStateMaxRotationSpeedChangedListener(null);
		ARCommand.setARDrone3SpeedSettingsStateHullProtectionChangedListener(null);
		ARCommand.setARDrone3SpeedSettingsStateOutdoorChangedListener(null);
		ARCommand.setARDrone3NetworkSettingsStateWifiSelectionChangedListener(null);
		ARCommand.setARDrone3SettingsStateProductMotorVersionListChangedListener(null);
		ARCommand.setARDrone3SettingsStateProductGPSVersionChangedListener(null);
		ARCommand.setARDrone3SettingsStateMotorErrorStateChangedListener(null);
		ARCommand.setARDrone3SettingsStateMotorSoftwareVersionChangedListener(null);
		ARCommand.setARDrone3SettingsStateMotorFlightsStatusChangedListener(null);
		ARCommand.setARDrone3SettingsStateMotorErrorLastErrorChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStatePictureFormatChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStateExpositionChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStateSaturationChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStateTimelapseChangedListener(null);
		ARCommand.setARDrone3PictureSettingsStateVideoAutorecordChangedListener(null);
		ARCommand.setARDrone3MediaStreamingStateVideoEnableChangedListener(null);
		ARCommand.setARDrone3GPSSettingsStateHomeChangedListener(null);
		ARCommand.setARDrone3GPSSettingsStateResetHomeChangedListener(null);
		ARCommand.setARDrone3GPSSettingsStateGPSFixStateChangedListener(null);
		ARCommand.setARDrone3GPSSettingsStateGPSUpdateStateChangedListener(null);
		ARCommand.setARDrone3CameraStateOrientationListener(null);
		ARCommand.setARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener(null);
		ARCommand.setARDrone3DebugGPSDebugStateNbSatelliteChangedListener(null);

		super.unregisterARCommandsListener();
	}

	/**
	 * フラットトリムが変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateFlatTrimChangedListener
		mPilotingStateFlatTrimChangedListener
			= new ARCommandARDrone3PilotingStateFlatTrimChangedListener() {
		@Override
		public void onARDrone3PilotingStateFlatTrimChangedUpdate() {
			if (DEBUG) Log.v(TAG, "onARDrone3PilotingStateFlatTrimChangedUpdate:");
		}
	};

	/**
	 * 飛行状態が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateFlyingStateChangedListener
		mPilotingStateFlyingStateChangedListener
		= new ARCommandARDrone3PilotingStateFlyingStateChangedListener() {
		@Override
		public void onARDrone3PilotingStateFlyingStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
			if (DEBUG) Log.v(TAG, "onARDrone3PilotingStateFlyingStateChangedUpdate:");
			mAttributeDrone.setFlyingState(state.getValue());
			callOnFlyingStateChangedUpdate(getState());
		}
	};

	/**
	 * 機体からの異常通知時
	 */
	private final ARCommandARDrone3PilotingStateAlertStateChangedListener
		mPilotingStateAlertStateChangedListener
		= new ARCommandARDrone3PilotingStateAlertStateChangedListener() {
		@Override
		public void onARDrone3PilotingStateAlertStateChangedUpdate(
																	  final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM state) {

			if (DEBUG) Log.v(TAG, "onARDrone3PilotingStateAlertStateChangedUpdate:");
			mAttributeDrone.setAlarm(state.getValue());
			callOnAlarmStateChangedUpdate(getAlarm());
		}
	};

	/**
	 * モーターのエラー状態が変化した時
	 */
	private final ARCommandARDrone3SettingsStateMotorErrorStateChangedListener
		mSettingsStateMotorErrorStateChangedListener
		= new ARCommandARDrone3SettingsStateMotorErrorStateChangedListener() {
		/**
		 * @param motorIds ビットフィールド, ビット0:モーター0, ビット1:モーター1, ビット2:モーター2, ビット3: モーター3
		 * @param error
		 */
		@Override
		public void onARDrone3SettingsStateMotorErrorStateChangedUpdate(
			final byte motorIds,
			final ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM error) {

			if (!ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM
					 .ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_NOERROR.equals(error)) {
				final int err = 1 << (error.getValue() - 1);
				for (int i = 0; i < MOTOR_NUMS; i++) {
					if ((motorIds & (1 << i)) != 0) {
						mMotors[i].error |= err;
					}
				}
			} else {
				for (int i = 0; i < MOTOR_NUMS; i++) {
					if ((motorIds & (1 << i)) != 0) {
						mMotors[i].error = AttributeMotor.ERR_MOTOR_NON;
					}
				}
			}
			// FIXME
		}
	};

	/**
	 * 最後に起こったモーターエラー
	 */
	private final ARCommandARDrone3SettingsStateMotorErrorLastErrorChangedListener
		mSettingsStateMotorErrorLastErrorChangedListener
		= new ARCommandARDrone3SettingsStateMotorErrorLastErrorChangedListener() {
		/**
		 * @param error 型は違うけど値はARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUMと同じ
		 */
		@Override
		public void onARDrone3SettingsStateMotorErrorLastErrorChangedUpdate(
			final ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED_MOTORERROR_ENUM error) {
			if (DEBUG) Log.v(TAG, "onARDrone3SettingsStateMotorErrorLastErrorChangedUpdate:" + error);
		}
	};

	/**
	 * 自動離陸モードが変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateAutoTakeOffModeChangedListener
		mPilotingStateAutoTakeOffModeChangedListener
			= new ARCommandARDrone3PilotingStateAutoTakeOffModeChangedListener() {
		/**
		 * @param state State of automatic take off mode
		 */
		@Override
		public void onARDrone3PilotingStateAutoTakeOffModeChangedUpdate(final byte state) {
			if (DEBUG) Log.v(TAG, "onARDrone3PilotingStateAutoTakeOffModeChangedUpdate:");
			if (mAutoTakeOffMode != (state != 0)) {
				mAutoTakeOffMode = (state != 0);
			}
		}
	};

	/**
	 * WiFiスキャンリストが変更された時
	 */
	private final ARCommandARDrone3NetworkStateWifiScanListChangedListener
		mNetworkStateWifiScanListChangedListener
			= new ARCommandARDrone3NetworkStateWifiScanListChangedListener() {
		/**
		 * @param ssid SSID of the AP
		 * @param rssi RSSI of the AP in dbm (negative value)
		 * @param band The band : 2.4 GHz or 5 GHz
		 * @param channel Channel of the AP
		 */
		@Override
		public void onARDrone3NetworkStateWifiScanListChangedUpdate(
			final String ssid, final short rssi,
			final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM band,
			final byte channel) {
			// FIXME
		}
	};

	/**
	 * WiFiスキャンが変化した時
	 */
	private final ARCommandARDrone3NetworkStateAllWifiScanChangedListener
		mNetworkStateAllWifiScanChangedListener
			= new ARCommandARDrone3NetworkStateAllWifiScanChangedListener() {
		@Override
		public void onARDrone3NetworkStateAllWifiScanChangedUpdate() {
			// FIXME
		}
	};

	/**
	 * WiFiチャンネルリストが変化した時
	 */
	private final ARCommandARDrone3NetworkStateWifiAuthChannelListChangedListener
		mNetworkStateWifiAuthChannelListChangedListener
			= new ARCommandARDrone3NetworkStateWifiAuthChannelListChangedListener() {
		/**
		 * @param band The band of this channel : 2.4 GHz or 5 GHz
		 * @param channel The authorized channel.
		 * @param in_or_out Bit 0 is 1 if channel is authorized outside (0 otherwise) ; Bit 1 is 1 if channel is authorized inside (0 otherwise)
		 */
		@Override
		public void onARDrone3NetworkStateWifiAuthChannelListChangedUpdate(
			final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band,
			final byte channel,
			final byte in_or_out) {
			// FIXME
		}
	};

	/**
	 * WiFiチャネルの状態が変化した時
	 */
	private final ARCommandARDrone3NetworkStateAllWifiAuthChannelChangedListener
		mNetworkStateAllWifiAuthChannelChangedListener
			= new ARCommandARDrone3NetworkStateAllWifiAuthChannelChangedListener() {
		@Override
		public void onARDrone3NetworkStateAllWifiAuthChannelChangedUpdate() {
			// FIXME
		}
	};

	/**
	 * WiFiの選択状態が変化した時
	 */
	private final ARCommandARDrone3NetworkSettingsStateWifiSelectionChangedListener
		mNetworkSettingsStateWifiSelectionChangedListener
			= new ARCommandARDrone3NetworkSettingsStateWifiSelectionChangedListener() {
		/**
		 * @param type The type of wifi selection settings
		 * @param band The actual  wifi band state
		 * @param channel The channel (depends of the band)
		 */
		@Override
		public void onARDrone3NetworkSettingsStateWifiSelectionChangedUpdate(
			final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type,
			final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band,
			final byte channel) {
			// FIXME
		}
	};


	protected static final int MOTOR_NUMS = 4;
	protected final AttributeMotor[] mMotors = new AttributeMotor[MOTOR_NUMS];
	/**
	 * モーターバージョンが変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3SettingsStateProductMotorVersionListChangedListener
		mSettingsStateProductMotorVersionListChangedListener
			= new ARCommandARDrone3SettingsStateProductMotorVersionListChangedListener() {
		/**
		 * @param motor Product Motor number [1 - 4]
		 * @param type Product Motor type
		 * @param software Product Motors software version
		 * @param hardware Product Motors hardware version
		 */
		@Override
		public void onARDrone3SettingsStateProductMotorVersionListChangedUpdate(
			final byte motor, final String type, final String software, final String hardware) {
			if (DEBUG) Log.v(TAG, "onARDrone3SettingsStateProductMotorVersionListChangedUpdate:");
			if (mMotors[0] == null) {
				for (int i = 0; i < MOTOR_NUMS; i++) {
					mMotors[i] = new AttributeMotor();
				}
			}
			try {
				final int ix = (motor - 1) % MOTOR_NUMS;
				mMotors[ix].type = type;
				mMotors[ix].software = software;
				mMotors[ix].hardware = hardware;
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	/**
	 * GPSのバージョン
	 */
	public final AttributeGPS mGPS = new AttributeGPS();
	private final ARCommandARDrone3SettingsStateProductGPSVersionChangedListener
		mSettingsStateProductGPSVersionChangedListener
			= new ARCommandARDrone3SettingsStateProductGPSVersionChangedListener() {
		@Override
		public void onARDrone3SettingsStateProductGPSVersionChangedUpdate(
			final String software, final String hardware) {

			if (DEBUG) Log.v(TAG, "onARDrone3SettingsStateProductGPSVersionChangedUpdate:");
			mGPS.software = software;
			mGPS.hardware = hardware;
		}
	};

	private String mMotorSoftwareVersion;
	/**
	 * モーターソフトウエアバージョンが変更された時
	 */
	private final ARCommandARDrone3SettingsStateMotorSoftwareVersionChangedListener
		mSettingsStateMotorSoftwareVersionChangedListener
		= new ARCommandARDrone3SettingsStateMotorSoftwareVersionChangedListener() {
		/**
		 * @param version name of the version : ドット区切り
		 * 	(major version - minor version - firmware type - nb motors handled).
		 * 	Firmware types : Release, Debug, Alpha, Test-bench
		 */
		@Override
		public void onARDrone3SettingsStateMotorSoftwareVersionChangedUpdate(final String version) {
			mMotorSoftwareVersion = version;
		}
	};

	/**
	 * 最大高度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingSettingsStateMaxAltitudeChangedListener
		mPilotingSettingsStateMaxAltitudeChangedListener
		= new ARCommandARDrone3PilotingSettingsStateMaxAltitudeChangedListener() {
		/**
		 * @param current Current altitude max
		 * @param min Range min of altitude
		 * @param max Range max of altitude
		 */
		@Override
		public void onARDrone3PilotingSettingsStateMaxAltitudeChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxAltitude.current != current)
					|| (mMaxAltitude.min != min)
					|| (mMaxAltitude.max != max)) {

				mMaxAltitude.current = current;
				mMaxAltitude.min = min;
				mMaxAltitude.max = max;
			}
			if (DEBUG) Log.v(TAG, "onARDrone3PilotingSettingsStateMaxAltitudeChangedUpdate:" + mMaxAltitude);
		}
	};

	/**
	 * 最大傾斜設定が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingSettingsStateMaxTiltChangedListener
		mPilotingSettingsStateMaxTiltChangedListener
		= new ARCommandARDrone3PilotingSettingsStateMaxTiltChangedListener() {
		/**
		 * @param current Current max tilt
		 * @param min Range min of tilt
		 * @param max Range max of tilt
		 */
		@Override
		public void onARDrone3PilotingSettingsStateMaxTiltChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxTilt.current != current)
					|| (mMaxTilt.min != min)
					|| (mMaxTilt.max != max)) {

				mMaxTilt.current = current;
				mMaxTilt.min = min;
				mMaxTilt.max = max;
			}
			if (DEBUG) Log.v(TAG, "onARDrone3PilotingSettingsStateMaxTiltChangedUpdate:" + mMaxTilt);
		}
	};

	/**
	 * AbsoluteControlChanged(ってなんやろ)コマンドをデコードした時
	 */
	private final ARCommandARDrone3PilotingSettingsStateAbsolutControlChangedListener
		mPilotingSettingsStateAbsolutControlChangedListener
		= new ARCommandARDrone3PilotingSettingsStateAbsolutControlChangedListener() {
		/**
		 * @param onoff 1:on, 0:off
		 */
		@Override
		public void onARDrone3PilotingSettingsStateAbsolutControlChangedUpdate(final byte onoff) {
			// FIXME
		}
	};

	/**
	 * 上昇/降下速度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3SpeedSettingsStateMaxVerticalSpeedChangedListener
		mSettingsStateMaxVerticalSpeedChangedListener
		= new ARCommandARDrone3SpeedSettingsStateMaxVerticalSpeedChangedListener() {
		/**
		 * @param current Current max vertical speed in m/s
		 * @param min Range min of vertical speed
		 * @param max Range max of vertical speed
		 */
		@Override
		public void onARDrone3SpeedSettingsStateMaxVerticalSpeedChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxVerticalSpeed.current != current)
					|| (mMaxVerticalSpeed.min != min)
					|| (mMaxVerticalSpeed.max != max)) {

				mMaxVerticalSpeed.current = current;
				mMaxVerticalSpeed.min = min;
				mMaxVerticalSpeed.max = max;
			}
			if (DEBUG) Log.v(TAG, "onARDrone3SpeedSettingsStateMaxVerticalSpeedChangedUpdate:" + mMaxVerticalSpeed);
		}
	};

	/**
	 * 最大回転速度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3SpeedSettingsStateMaxRotationSpeedChangedListener
		mSpeedSettingsStateMaxRotationSpeedChangedListener
		= new ARCommandARDrone3SpeedSettingsStateMaxRotationSpeedChangedListener() {
		/**
		 * @param current Current max rotation speed in degree/s
		 * @param min Range min of rotation speed
		 * @param max Range max of rotation speed
		 */
		@Override
		public void onARDrone3SpeedSettingsStateMaxRotationSpeedChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxRotationSpeed.current != current)
					|| (mMaxRotationSpeed.min != min)
					|| (mMaxRotationSpeed.max != max)) {
				mMaxRotationSpeed.current = current;
				mMaxRotationSpeed.min = min;
				mMaxRotationSpeed.max = max;
			}
			if (DEBUG) Log.v(TAG, "onARDrone3SpeedSettingsStateMaxRotationSpeedChangedUpdate:" + mMaxRotationSpeed);
		}
	};

	/**
	 * ハルの有無設定が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3SpeedSettingsStateHullProtectionChangedListener
		mSpeedSettingsStateHullProtectionChangedListener
		= new ARCommandARDrone3SpeedSettingsStateHullProtectionChangedListener() {
		/**
		 * @param present 1 if present, 0 if not present
		 */
		@Override
		public void onARDrone3SpeedSettingsStateHullProtectionChangedUpdate(final byte present) {
			if (DEBUG) Log.v(TAG, "onARDrone3SpeedSettingsStateHullProtectionChangedUpdate:");
			if (mHasGuard != (present != 0)) {
				mHasGuard = (present != 0);
			}
		}
	};

	/** 室外モードか室内モードか */
	private boolean mOutdoorMode;
	/**
	 * 室外モードか室内モードかが変更された時
	 */
	private final ARCommandARDrone3SpeedSettingsStateOutdoorChangedListener
		mSpeedSettingsStateOutdoorChangedListener
		= new ARCommandARDrone3SpeedSettingsStateOutdoorChangedListener() {
		/**
		 * @param outdoor 1:室外, 0:室内
		 */
		@Override
		public void onARDrone3SpeedSettingsStateOutdoorChangedUpdate(byte outdoor) {
			mOutdoorMode = (outdoor != 0);
		}
	};

	/**
	 * 飛行回数・飛行時間が変化した時
	 */
	private final ARCommandARDrone3SettingsStateMotorFlightsStatusChangedListener
		mSettingsStateMotorFlightsStatusChangedListener
		= new ARCommandARDrone3SettingsStateMotorFlightsStatusChangedListener() {
		/**
		 * @param nbFlights 飛行回数
		 * @param lastFlightDuration 最後の飛行時間[秒]
		 * @param totalFlightDuration 合計飛行時間[秒]
		 */
		@Override
		public void onARDrone3SettingsStateMotorFlightsStatusChangedUpdate(
			final short nbFlights, final short lastFlightDuration, final int totalFlightDuration) {
			// FIXME
		}
	};

	/**
	 * 高度が変更になった時
	 */
	private final ARCommandARDrone3PilotingStateAltitudeChangedListener
		mPilotingStateAltitudeChangedListener
		= new ARCommandARDrone3PilotingStateAltitudeChangedListener() {
		/**
		 * @param altitude 高度[m]
		 */
		@Override
		public void onARDrone3PilotingStateAltitudeChangedUpdate(final double altitude) {
			if (mAttributeDrone.altitude != altitude) {
				mAttributeDrone.altitude = altitude;
				// FIXME
			}
		}
	};

	/**
	 * 機体位置が変更された時
	 */
	private final ARCommandARDrone3PilotingStatePositionChangedListener
		mPilotingStatePositionChangedListener
		= new ARCommandARDrone3PilotingStatePositionChangedListener() {
		/**
		 * @param latitude GPS緯度[度] (500.0: 不明)
		 * @param longitude GPS経度[度] (500.0: 不明)
		 * @param altitude GPS高度[m](from GPS)
		 */
		@Override
		public void onARDrone3PilotingStatePositionChangedUpdate(
			final double latitude, final double longitude, final double altitude) {
			// FIXME
		}
	};

	/**
	 * 飛行速度設定が変更された時
	 */
	private final ARCommandARDrone3PilotingStateSpeedChangedListener
		mPilotingStateSpeedChangedListener
		= new ARCommandARDrone3PilotingStateSpeedChangedListener() {
		/**
		 * @param speedX Speed on the x axis (when drone moves forward, speed is > 0) (in m/s)
		 * @param speedY Speed on the y axis (when drone moves to right, speed is > 0) (in m/s)
		 * @param speedZ Speed on the z axis (when drone moves down, speed is > 0) (in m/s)
		 */
		@Override
		public void onARDrone3PilotingStateSpeedChangedUpdate(
			final float speedX, final float speedY, final float speedZ) {
			// FIXME
		}
	};

	/**
	 * 機体姿勢が変更された時
	 */
	private final ARCommandARDrone3PilotingStateAttitudeChangedListener
		mPilotingStateAttitudeChangedListener
		= new ARCommandARDrone3PilotingStateAttitudeChangedListener() {
		/**
		 * @param roll roll value (in radian)
		 * @param pitch Pitch value (in radian)
		 * @param yaw Yaw value (in radian)
		 */
		@Override
		public void onARDrone3PilotingStateAttitudeChangedUpdate(
			final float roll, final float pitch, final float yaw) {
			// FIXME
		}
	};

	/**
	 * ホーム位置が変更された時
	 */
	private final ARCommandARDrone3GPSSettingsStateHomeChangedListener
		mGPSSettingsStateHomeChangedListener
		= new ARCommandARDrone3GPSSettingsStateHomeChangedListener() {
		/**
		 * @param latitude ホーム位置:緯度[度]
		 * @param longitude ホーム位置:経度[度]
		 * @param altitude ホーム位置:高度[m]
		 */
		@Override
		public void onARDrone3GPSSettingsStateHomeChangedUpdate(
			final double latitude, final double longitude, final double altitude) {
			// FIXME
		}
	};

	/**
	 * ホーム位置が変更された時
	 */
	private final ARCommandARDrone3GPSSettingsStateResetHomeChangedListener
		mGPSSettingsStateResetHomeChangedListener
		= new ARCommandARDrone3GPSSettingsStateResetHomeChangedListener() {
		/**
		 * @param latitude ホーム位置:緯度[度]
		 * @param longitude ホーム位置:経度[度]
		 * @param altitude ホーム位置:高度[m]
		 */
		@Override
		public void onARDrone3GPSSettingsStateResetHomeChangedUpdate(
			final double latitude, final double longitude, final double altitude) {
			// FIXME
		}
	};

	/**
	 * ナビゲーションホーム状態が変更された時
	 */
	private final ARCommandARDrone3PilotingStateNavigateHomeStateChangedListener
		mPilotingStateNavigateHomeStateChangedListener
		= new ARCommandARDrone3PilotingStateNavigateHomeStateChangedListener() {
		/**
		 * @param state State of navigate home
		 * @param reason Reason of the state
		 */
		@Override
		public void onARDrone3PilotingStateNavigateHomeStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_STATE_ENUM state,
			final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_REASON_ENUM reason) {
			// state=0(available): 利用可能, Navigate home is available
			// state=1(inProgress): 実行中, Navigate home is in progress
			// state=2(unavailable): 利用不可, Navigate home is not available
			// state=3(pending): 中断中, Navigate home has been received, but its process is pending

			// reason=0: User requested a navigate home (available->inProgress)
			// reason=1: Connection between controller and product lost (available->inProgress)
			// reason=2: Low battery occurred (available->inProgress)
			// reason=3: Navigate home is finished (inProgress->available)
			// reason=4: Navigate home has been stopped (inProgress->available)
			// reason=5: Navigate home disabled by product (inProgress->unavailable or available->unavailable)
			// reason=6: Navigate home enabled by product (unavailable->available)
			// FIXME
		}
	};

	/**
	 * GPS固定設定?が変更された時
	 */
	private final ARCommandARDrone3GPSSettingsStateGPSFixStateChangedListener
		mGPSSettingsStateGPSFixStateChangedListener
		= new ARCommandARDrone3GPSSettingsStateGPSFixStateChangedListener() {
		/**
		 * @param fixed 1 if gps on drone is fixed, 0 otherwise
		 */
		@Override
		public void onARDrone3GPSSettingsStateGPSFixStateChangedUpdate(final byte fixed) {
			// FIXME
		}
	};

	/**
	 * GPSの状態が変化した時
	 */
	private final ARCommandARDrone3GPSSettingsStateGPSUpdateStateChangedListener
		mGPSSettingsStateGPSUpdateStateChangedListener
		= new ARCommandARDrone3GPSSettingsStateGPSUpdateStateChangedListener() {
		@Override
		public void onARDrone3GPSSettingsStateGPSUpdateStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM state) {
			// state=0: Drone GPS update succeed
			// state=1: Drone GPS update In progress
			// state=2: Drone GPS update failed
			// FIXME
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3MediaRecordStatePictureStateChangedListener
		mMediaRecordStatePictureStateChangedListener
		= new ARCommandARDrone3MediaRecordStatePictureStateChangedListener() {
		/**
		 * @param state 1 if picture has been taken, 0 otherwise
		 * @param mass_storage_id Mass storage id to record
		 */
		@Override
		public void onARDrone3MediaRecordStatePictureStateChangedUpdate(
			final byte state, final byte mass_storage_id) {
			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStatePictureStateChangedUpdate:");
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener
		mMediaRecordStatePictureStateChangedV2Listener
		= new ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener() {
		@Override
		public void onARDrone3MediaRecordStatePictureStateChangedV2Update(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStatePictureStateChangedV2Update:state=" + state + ",error=" + error);
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandARDrone3MediaRecordEventPictureEventChangedListener
		mMediaRecordEventPictureEventChangedListener
		= new ARCommandARDrone3MediaRecordEventPictureEventChangedListener() {
		@Override
		public void onARDrone3MediaRecordEventPictureEventChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordEventPictureEventChangedUpdate:event=" + event + ",error=" + error);
		}
	};

	/**
	 * ビデオ撮影状態が変更された時
	 */
	private final ARCommandARDrone3MediaRecordStateVideoStateChangedListener
		mMediaRecordStateVideoStateChangedListener
		= new ARCommandARDrone3MediaRecordStateVideoStateChangedListener() {
		/**
		 * @param state State of video
		 * @param mass_storage_id Mass storage id where the video was recorded
		 */
		@Override
		public void onARDrone3MediaRecordStateVideoStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGED_STATE_ENUM state, final byte mass_storage_id) {
			// FIXME
//			0: Video was stopped
//			1: Video was started
//			2: Video was failed
//			3: Video was auto stopped
		}
	};

	/**
	 * 写真撮影のフォーマット<br>
	 * 0: Take raw image<br>
	 * 1: Take a 4:3 jpeg photo<br>
	 * 2: Take a 16:9 snapshot from camera<br>
	 */
	private int mPictureFormat;
	/**
	 * 写真撮影時のフォーマットが変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStatePictureFormatChangedListener
		mPictureSettingsStatePictureFormatChangedListener
			= new ARCommandARDrone3PictureSettingsStatePictureFormatChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStatePictureFormatChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED_TYPE_ENUM type) {
			if (mPictureFormat != type.getValue()) {
				mPictureFormat = type.getValue();
				// FIXME
			}
		}
	};

	/**
	 * オートホワイトバランス設定<br>
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 */
	private int mAutoWhiteBalanceMode;
	/**
	 * オートホワイトバランス設定が変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener
		mPictureSettingsStateAutoWhiteBalanceChangedListener
			= new ARCommandARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateAutoWhiteBalanceChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED_TYPE_ENUM type) {
			if (mAutoWhiteBalanceMode != type.getValue()) {
				mAutoWhiteBalanceMode = type.getValue();
				// FIXME
			}
		}
	};

	/**
	 * 露出設定
	 */
	private final AttributeFloat mExposure = new AttributeFloat();
	/**
	 * 露出設定が変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStateExpositionChangedListener
		mPictureSettingsStateExpositionChangedListener
			= new ARCommandARDrone3PictureSettingsStateExpositionChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateExpositionChangedUpdate(
			final float current, final float min, final float max) {
			if ((mExposure.current != current)
				|| (mExposure.min != min)
				|| (mExposure.max != max)) {
				mExposure.current = current;
				mExposure.min = min;
				mExposure.max = max;
				// FIXME
			}
		}
	};

	/**
	 * 彩度設定
	 */
	private final AttributeFloat mSaturation = new AttributeFloat();
	/**
	 * 再度設定が変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStateSaturationChangedListener
		mPictureSettingsStateSaturationChangedListener
			= new ARCommandARDrone3PictureSettingsStateSaturationChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateSaturationChangedUpdate(
			final float current, final float min, final float max) {
			if ((mSaturation.current != current)
				|| (mSaturation.min != min)
				|| (mSaturation.max != max)) {
				mSaturation.current = current;
				mSaturation.min = min;
				mSaturation.max = max;
				// FIXME
			}
		}
	};

	/** タイムラプス設定 */
	private final AttributeTimeLapse mTimeLapse = new AttributeTimeLapse();
	/**
	 * タイムラプス設定が変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStateTimelapseChangedListener
		mPictureSettingsStateTimelapseChangedListener
			= new ARCommandARDrone3PictureSettingsStateTimelapseChangedListener() {
		/**
		 * @param enabled 1 if timelapse is enabled, 0 otherwise
		 * @param interval interval in seconds for taking pictures
		 * @param minInterval Minimal interval for taking pictures
		 * @param maxInterval Maximal interval for taking pictures
		 */
		@Override
		public void onARDrone3PictureSettingsStateTimelapseChangedUpdate(
			final byte enabled, final float interval, final float minInterval, final float maxInterval) {
			if ((mTimeLapse.enabled != (enabled != 0))
				|| (mTimeLapse.interval.current != interval)
				|| (mTimeLapse.interval.min != minInterval)
				|| (mTimeLapse.interval.max != maxInterval)) {
				mTimeLapse.enabled = (enabled != 0);
				mTimeLapse.interval.current = interval;
				mTimeLapse.interval.min = minInterval;
				mTimeLapse.interval.max = maxInterval;
				// FIXME
			}
		}
	};

	/**
	 * 自動録画設定が変更された時
	 */
	private final ARCommandARDrone3PictureSettingsStateVideoAutorecordChangedListener
		mPictureSettingsStateVideoAutorecordChangedListener
			= new ARCommandARDrone3PictureSettingsStateVideoAutorecordChangedListener() {
		/**
		 * @param enabled 1 if video autorecord is enabled, 0 otherwise
		 * @param mass_storage_id Mass storage id for the taken video
		 */
		@Override
		public void onARDrone3PictureSettingsStateVideoAutorecordChangedUpdate(
			final byte enabled, final byte mass_storage_id) {
			// FIXME
		}
	};

	/**
	 * ビデオストリーミングの有効無効が変更された時
	 */
	private final ARCommandARDrone3MediaStreamingStateVideoEnableChangedListener
		mMediaStreamingStateVideoEnableChangedListener
			= new ARCommandARDrone3MediaStreamingStateVideoEnableChangedListener() {
		@Override
		public void onARDrone3MediaStreamingStateVideoEnableChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED_ENABLED_ENUM enabled) {
			// 0: Video streaming is enabled.
			// 1: Video streaming is disabled.
			// 2: Video streaming failed to start.
			// FIXME
		}
	};

	/**
	 * カメラの向きが変更された時
	 */
	private final ARCommandARDrone3CameraStateOrientationListener
		mCameraStateOrientationListener
			= new ARCommandARDrone3CameraStateOrientationListener() {
		/**
		 * @param tilt Tilt camera consign for the drone [-100;100]
		 * @param pan Pan camera consign for the drone [-100;100]
		 */
		@Override
		public void onARDrone3CameraStateOrientationUpdate(
			final byte tilt, final byte pan) {
			// FIXME
		}
	};

	/**
	 * 使用しているバッテリーの種類設定が変わった時
	 */
	private final ARCommandARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener
		mDebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener
			= new ARCommandARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener() {
		/**
		 * @param drone2BatteryUsed 1 if the drone2 battery is used, 0 if the drone3 battery is used
		 */
		@Override
		public void onARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedUpdate(final byte drone2BatteryUsed) {
			// FIXME
		}
	};

	/**
	 * 捕捉しているGPS衛星の数が変わった時
	 */
	private final ARCommandARDrone3DebugGPSDebugStateNbSatelliteChangedListener
		mDebugGPSDebugStateNbSatelliteChangedListener
			= new ARCommandARDrone3DebugGPSDebugStateNbSatelliteChangedListener() {
		/**
		 * @param nbSatellite Amount of satellite used by gps location
		 */
		@Override
		public void onARDrone3DebugGPSDebugStateNbSatelliteChangedUpdate(final byte nbSatellite) {
			// FIXME
		}
	};

//================================================================================
// コマンド送信関係
//================================================================================
	/**
	 * 操縦コマンドを送信
	 * @param flag flag to activate roll/pitch movement
	 * @param roll [-100,100]
	 * @param pitch [-100,100]
	 * @param yaw [-100,100]
	 * @param gaz [-100,100]
	 * @param heading [-180,180]
	 * @return
	 */
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;
		synchronized (mDataSync) {
			cmdError = cmd.setARDrone3PilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, heading);
		}
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dNackId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send PCMD command.");
		}

		return sentStatus;
	}

	/**
	 * 非常停止
	 * @return
	 */
	@Override
	public boolean sendEmergency() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dEmergencyId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	/**
	 * フラットトリム
	 * @return
	 */
	@Override
	public boolean sendFlatTrim() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingFlatTrim();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flattrim command.");
		}

		return sentStatus;
	}

	/**
	 * 離陸指示
	 * @return
	 */
	@Override
	public boolean sendTakeoff() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingTakeOff();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send TakeOff command.");
		}

		return sentStatus;
	}

	/**
	 * 着陸指示
	 * @return
	 */
	@Override
	public boolean sendLanding() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingLanding();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Landing command.");
		}

		return sentStatus;
	}

	/**
	 * 最大高度を設定
	 * @param altitude [m]
	 * @return
	 */
	@Override
	public boolean sendMaxAltitude(final float altitude) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingSettingsMaxAltitude(altitude);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(),
				cmd, ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxAltitude command.");
		}

		return sentStatus;
	}

	/**
	 * 最大傾斜設定
	 * @param tilt
	 * @return
	 */
	@Override
	public boolean sendMaxTilt(final float tilt) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingSettingsMaxTilt(tilt);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxTilt command.");
		}

		return sentStatus;
	}

	/**
	 * 最大上昇/下降速度を設定
	 * @param speed
	 * @return
	 */
	@Override
	public boolean sendMaxVerticalSpeed(final float speed) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3SpeedSettingsMaxVerticalSpeed(speed);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxVerticalSpeed command.");
		}

		return sentStatus;
	}

	/**
	 * 最大回転速度
	 * @param speed [度/秒]
	 * @return
	 */
	@Override
	public boolean sendMaxRotationSpeed(final float speed) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3SpeedSettingsMaxRotationSpeed(speed);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxVerticalSpeed command.");
		}

		return sentStatus;
	}

	/**
	 * AbsoluteControlの有効無効を設定
	 * @param enable
	 * @return
	 */
	public boolean sendAbsoluteControl(final boolean enable) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingSettingsAbsolutControl((byte)(enable ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxVerticalSpeed command.");
		}

		return sentStatus;
	}

	/**
	 * モーターの個数を返す
	 * @return
	 */
	@Override
	public int getMotorNums() {
		return 4;
	}

	/**
	 * モーターの自動カット機能のon/off Beebapは常にonな気がする
	 * @param enabled
	 * @return
	 */
	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		return true;
	}

	/**
	 * 自動離陸モードの有効/無効を設定
	 * @param enable
	 * @return
	 */
	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingAutoTakeOffMode((byte) (enable ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AutoTakeOffMode command.");
		}

		return sentStatus;
	}

	/**
	 * ハルの有無を送信
	 * @param has_guard
	 * @return
	 */
	public boolean sendHasGuard(final boolean has_guard) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3SpeedSettingsHullProtection((byte) (has_guard ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send hull protection command.");
		}

		return sentStatus;
	}

	/**
	 * 室外モードか室内モードかを設定
	 * @param is_outdoor
	 * @return
	 */
	public boolean SendSpeedSettingsOutdoor(final boolean is_outdoor) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3SpeedSettingsOutdoor((byte) (is_outdoor ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send hull protection command.");
		}

		return sentStatus;
	}

	/**
	 * WiFi選択設定
	 * @param is_auto_select
	 * @param selection_band 0: 2.4GHz, 1: 5GHz, 2:2.4GHz+5GHz
	 * @param channel
	 * @return
	 */
	public boolean sendWifiSelection(final boolean is_auto_select, final int selection_band, int channel) {

		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_ENUM type
			= is_auto_select ? ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_ENUM. ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_AUTO
			: ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_ENUM.ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_TYPE_MANUAL;

		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_BAND_ENUM band
			= ARCOMMANDS_ARDRONE3_NETWORKSETTINGS_WIFISELECTION_BAND_ENUM.getFromValue(selection_band);

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3NetworkSettingsWifiSelection(type, band, (byte) channel);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send WifiSelection command.");
		}

		return sentStatus;
	}

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera
	 * @return
	 */
	public boolean SendPictureFormat(final int pictureFormat) {

		final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM type
			= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM.getFromValue(pictureFormat);

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsPictureFormatSelection(type);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send PictureFormat command.");
		}

		return sentStatus;
	}

	/**
	 * オートホワイトバランス設定
	 * @param auto_white_balance<br>
	 * -1: 手動
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 * @return
	 */
	public boolean sendAutoWhiteBalance(final int auto_white_balance) {

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;

		if ((auto_white_balance >= 0) && (auto_white_balance <= 4)) {
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM.getFromValue(auto_white_balance);


			cmdError = cmd.setARDrone3PictureSettingsAutoWhiteBalanceSelection(type);
		} else {
			cmdError = cmd.setARDrone3DebugVideoManualWhiteBalance();
		}
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AutoWhiteBalance command.");
		}

		return sentStatus;
	}

	/**
	 * 露出設定
	 * @param exposure Exposure value (bounds given by ExpositionChanged arg min and max, by default [-3:3])
	 * @return
	 */
	public boolean sendExposure(final float exposure) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsExpositionSelection(exposure);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
									 ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 彩度設定
	 * @param saturation Saturation value (bounds given by SaturationChanged arg min and max, by default [-100:100])
	 * @return
	 */
	public boolean sendSaturation(final float saturation) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsSaturationSelection(saturation);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * タイムラプス設定
	 * @param enabled
	 * @param interval 撮影間隔[秒]
	 * @return
	 */
	public boolean sendTimelapseSelection(final boolean enabled, final float interval) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsTimelapseSelection((byte) (enabled ? 1 : 0), interval);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 自動録画設定
	 * @param enabled
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoAutorecord(final boolean enabled, final int mass_storage_id) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsVideoAutorecordSelection((byte) (enabled ? 1 : 0), (byte) mass_storage_id);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 動画撮影設定
	 * @param enabled
	 * @return
	 */
	public boolean sendVideoEnable(final boolean enabled) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaStreamingVideoEnable((byte) (enabled ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * ホーム位置を設定
	 * @param latitude 緯度[度]
	 * @param longitude 経度[度]
	 * @param altitude 高度[m]
	 * @return
	 */
	public boolean setHome(final double latitude, final double longitude, final double altitude) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3GPSSettingsSetHome(latitude, longitude, altitude);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * ホーム位置をリセット
	 * @return
	 */
	public boolean sendResetHome() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3GPSSettingsResetHome();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 映像のブレ補正設定
	 * @param enabled
	 * @return
	 */
	public boolean sendWobbleCancellation(final boolean enabled) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3DebugVideoEnableWobbleCancellation((byte) (enabled ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 映像のブレ補正用のジャイロ設定
	 * @param anglesDelay_s Shift by x seconds angles (video stabilization)
	 * @param gyrosDelay_s Shift by x seconds t gyros (wobble cancellation
	 * @return
	 */
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3DebugVideoSyncAnglesGyros(anglesDelay_s, gyrosDelay_s);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
									 ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * drone2バッテリーを使用するかどうか
	 * @param use_drone2Battery
	 * @return
	 */
	public boolean SendUseDrone2Battery(final boolean use_drone2Battery) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3DebugBatteryDebugSettingsUseDrone2Battery((byte) (use_drone2Battery ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}

		return sentStatus;
	}

	/**
	 * 指定した方向にフリップ実行
	 * @param direction
	 * @return
	 */
	public boolean sendAnimationsFlip(final int direction) {

		ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM _dir;
		switch (direction) {
		case FLIP_FRONT:
			_dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_FRONT;
			break;
		case FLIP_BACK:
			_dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_BACK;
			break;
		case FLIP_RIGHT:
			_dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_RIGHT;
			break;
		case FLIP_LEFT:
			_dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_LEFT;
			break;
		default:
			return false;
		}

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3AnimationsFlip(_dir);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}

	/**
	 * FIXME 自動で指定した角度回転させる
	 * @param degree -180〜180度
	 * @return
	 */
	public boolean sendAnimationsCap(final int degree) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;
		// FIXME 未実装
		// headingに対して指示量を加算したのを送信する?
		return sentStatus;
	}

}
