package com.serenegiant.arflight;


import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM;
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
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordEventVideoEventChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStatePictureStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStateVideoStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandARDrone3MediaRecordStateVideoStateChangedV2Listener;
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
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeGPS;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.attribute.AttributePosition;
import com.serenegiant.arflight.configs.ARNetworkConfigARDrone3;

import java.util.HashMap;
import java.util.Map;

public class FlightControllerBebop extends FlightController implements ICameraController, IWiFiController {
	private static final boolean DEBUG = true; // FIXME 実働時はfalseにすること
	private static String TAG = "FlightControllerBebop";

	private boolean videoStreamDelegaterCreated;
	private VideoStreamDelegater mVideoStreamDelegater;

	public FlightControllerBebop(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigARDrone3());
		init();
	}

	public FlightControllerBebop(final Context context, final IBridgeController bridge) {
		super(context, bridge);
		init();
	}

	/** 共通の初期化処理 */
	private void init() {
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);

		mSettings.setCutOffMode(true);
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
		ARCommand.setARDrone3MediaRecordStateVideoStateChangedV2Listener(mMediaRecordStateVideoStateChangedV2Listener);
		ARCommand.setARDrone3MediaRecordEventVideoEventChangedListener(mMediaRecordEventVideoEventChangedListener);
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
		ARCommand.setARDrone3MediaRecordEventPictureEventChangedListener(null);
		ARCommand.setARDrone3MediaRecordStateVideoStateChangedListener(null);
		ARCommand.setARDrone3MediaRecordStateVideoStateChangedV2Listener(null);
		ARCommand.setARDrone3MediaRecordEventVideoEventChangedListener(null);
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
	 * フラットトリム変更を受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateFlatTrimChangedListener
		mPilotingStateFlatTrimChangedListener
			= new ARCommandARDrone3PilotingStateFlatTrimChangedListener() {
		@Override
		public void onARDrone3PilotingStateFlatTrimChangedUpdate() {
			callOnFlatTrimChanged();
		}
	};

	/**
	 * 飛行状態を受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateFlyingStateChangedListener
		mPilotingStateFlyingStateChangedListener
		= new ARCommandARDrone3PilotingStateFlyingStateChangedListener() {
		@Override
		public void onARDrone3PilotingStateFlyingStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
			((DroneStatus)mStatus).setFlyingState(state.getValue() * 0x100);
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

			mStatus.setAlarm(state.getValue());
			callOnAlarmStateChangedUpdate(getAlarm());
		}
	};

	/**
	 * モーターのエラー状態を受信した時
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

			final int n = getMotorNums();
			if (!ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM
					 .ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_NOERROR.equals(error)) {
				final int err = 1 << (error.getValue() - 1);
				for (int i = 0; i < n; i++) {
					if ((motorIds & (1 << i)) != 0) {
						getMotor(i).setError(err);
					}
				}
			} else {
				for (int i = 0; i < n; i++) {
					if ((motorIds & (1 << i)) != 0) {
						getMotor(i).clearError();
					}
				}
			}
			// FIXME UI側へ異常を通知する
		}
	};

	/**
	 * 最後に起こったモーターエラーを受信した時
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
			// FIXME 未実装
		}
	};

	/**
	 * 自動離陸モード設定を受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3PilotingStateAutoTakeOffModeChangedListener
		mPilotingStateAutoTakeOffModeChangedListener
			= new ARCommandARDrone3PilotingStateAutoTakeOffModeChangedListener() {
		/**
		 * @param state State of automatic take off mode
		 */
		@Override
		public void onARDrone3PilotingStateAutoTakeOffModeChangedUpdate(final byte state) {
			mSettings.setAutoTakeOffMode(state != 0);
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
		 * @param rssi RSSI of the AP in dbm (negative value) 受信信号強度
		 * @param band The band : 2.4 GHz or 5 GHz
		 * @param channel Channel of the AP
		 */
		@Override
		public void onARDrone3NetworkStateWifiScanListChangedUpdate(
			final String ssid, final short rssi,
			final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM band,
			final byte channel) {
			onWifiScanListChangedUpdate(ssid, rssi, band, channel);
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
			onAllWifiScanChangedUpdate();
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
			onWifiAuthChannelListChangedUpdate(band, channel, in_or_out);
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
			onAllWifiAuthChannelChangedUpdate();
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
			onWifiSelectionChangedUpdate(type, band, channel);
		}
	};

	/**
	 * モーターバージョンを受信した時のコールバックリスナー
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
			try {
				final int ix = (motor - 1) % getMotorNums();
				final AttributeMotor _motor = ((DroneStatus)mStatus).getMotor(ix);
				if (_motor != null) {
					_motor.set(type, software, hardware);
				} else {
					Log.w(TAG, "モーターNo.が予期したのと違う:" + motor);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	/**
	 * GPSのバージョンを受信した時
	 */
	public final AttributeGPS mGPS = new AttributeGPS();
	private final ARCommandARDrone3SettingsStateProductGPSVersionChangedListener
		mSettingsStateProductGPSVersionChangedListener
			= new ARCommandARDrone3SettingsStateProductGPSVersionChangedListener() {
		@Override
		public void onARDrone3SettingsStateProductGPSVersionChangedUpdate(
			final String software, final String hardware) {

			mGPS.set(software, hardware);
		}
	};

	private String mMotorSoftwareVersion;
	/**
	 * モーターソフトウエアバージョンを受信した時
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
	 * 最大高度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxAltitude(current, min, max);
		}
	};

	/**
	 * 最大傾斜設定を受信した時のコールバックリスナー
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
			mSettings.setMaxTilt(current, min, max);
		}
	};

	/**
	 * AbsoluteControlChanged(ってなんやろ)を受信した時, 機体側で実装されてないみたい
	 */
	private final ARCommandARDrone3PilotingSettingsStateAbsolutControlChangedListener
		mPilotingSettingsStateAbsolutControlChangedListener
		= new ARCommandARDrone3PilotingSettingsStateAbsolutControlChangedListener() {
		/**
		 * @param onoff 1:on, 0:off
		 */
		@Override
		public void onARDrone3PilotingSettingsStateAbsolutControlChangedUpdate(final byte onoff) {
			// FIXME 未実装
		}
	};

	/**
	 * 上昇/降下速度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxVerticalSpeed(current, min, max);
		}
	};

	/**
	 * 最大回転速度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxRotationSpeed(current, min, max);
		}
	};

	/**
	 * ハルの有無設定を受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3SpeedSettingsStateHullProtectionChangedListener
		mSpeedSettingsStateHullProtectionChangedListener
		= new ARCommandARDrone3SpeedSettingsStateHullProtectionChangedListener() {
		/**
		 * @param present 1 if present, 0 if not present
		 */
		@Override
		public void onARDrone3SpeedSettingsStateHullProtectionChangedUpdate(final byte present) {
			mSettings.setHasGuard(present != 0);
		}
	};

	/**
	 * 室外モードか室内モードを受信した時
	 */
	private final ARCommandARDrone3SpeedSettingsStateOutdoorChangedListener
		mSpeedSettingsStateOutdoorChangedListener
		= new ARCommandARDrone3SpeedSettingsStateOutdoorChangedListener() {
		/**
		 * @param outdoor 1:室外, 0:室内
		 */
		@Override
		public void onARDrone3SpeedSettingsStateOutdoorChangedUpdate(byte outdoor) {
			mSettings.outdoorMode(outdoor != 0);
		}
	};

	/**
	 * 飛行速度を受信した時<br>
	 * GPS座標から計算しているみたいなのでGPSを受信してないと0しか返ってこない<br>
	 * 数百ミリ秒に１回程度呼び出される
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

//			if (DEBUG) Log.v(TAG, String.format("SpeedChangedUpdate(%f,%f,%f)", speedX, speedY, speedZ));
			((DroneStatus)mStatus).setSpeed(speedY, speedX, -speedZ);
		}
	};

	/**
	 * 飛行回数・飛行時間を受信した時
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

			((DroneStatus)mStatus).setFlightDuration(nbFlights, lastFlightDuration, totalFlightDuration);
		}
	};

	/**
	 * 高度を受信した時
	 */
	private final ARCommandARDrone3PilotingStateAltitudeChangedListener
		mPilotingStateAltitudeChangedListener
		= new ARCommandARDrone3PilotingStateAltitudeChangedListener() {
		/**
		 * @param altitude 高度[m]
		 */
		@Override
		public void onARDrone3PilotingStateAltitudeChangedUpdate(final double altitude) {

			if (DEBUG) Log.v(TAG, "高度:" + altitude);
			mStatus.altitude(altitude);
		}
	};

	/**
	 * 機体位置(GPS座標)を受信した時
	 */
	private final ARCommandARDrone3PilotingStatePositionChangedListener
		mPilotingStatePositionChangedListener
		= new ARCommandARDrone3PilotingStatePositionChangedListener() {
		/**
		 * @param latitude GPS緯度[度] (500.0: 不明)
		 * @param longitude GPS経度[度] (500.0: 不明)
		 * @param altitude GPS高度[m](500.0: 不明)
		 */
		@Override
		public void onARDrone3PilotingStatePositionChangedUpdate(
			final double latitude, final double longitude, final double altitude) {

			if (DEBUG) Log.v(TAG, String.format("緯度:%f,軽度:%f,高度:%f", latitude, longitude, altitude));
			mStatus.setPosition(latitude, longitude, altitude);
		}
	};

	/**
	 * 機体姿勢を受信した時
	 */
	private final ARCommandARDrone3PilotingStateAttitudeChangedListener
		mPilotingStateAttitudeChangedListener
		= new ARCommandARDrone3PilotingStateAttitudeChangedListener() {
		/**
		 * @param roll 機体の横回転(ラジアン), 右に回転すると正、左に回転すると負, [-π,+π]
		 * @param pitch 機種の上下(ラジアン), 機種を上げると正、機種を下げると負, [-π,+π]
		 * @param yaw 機体の水平回転(ラジアン), 多分北磁極が０、東向きが正, 西向きが負, [-π,+π]
		 */
		@Override
		public void onARDrone3PilotingStateAttitudeChangedUpdate(
			final float roll, final float pitch, final float yaw) {

			if (DEBUG) Log.v(TAG, String.format("roll:%f,pitch:%f,yaw:%f", roll, pitch, yaw));
			((DroneStatus)mStatus).setAttitude(roll, pitch, yaw);
		}
	};

	/**
	 * ホーム位置(GPS座標)を受信した時
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

			mStatus.setHome(latitude, longitude, altitude);
		}
	};

	/**
	 * ホーム位置(GPS座標)がリセットされた時
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

			mStatus.setHome(latitude, longitude, altitude);
		}
	};

	/**
	 * ナビゲーションホーム状態を受信した時
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
	 * GPS固定設定?を受信した時
	 * GPSで自機位置を確認できたかどうかかも
	 */
	private final ARCommandARDrone3GPSSettingsStateGPSFixStateChangedListener
		mGPSSettingsStateGPSFixStateChangedListener
		= new ARCommandARDrone3GPSSettingsStateGPSFixStateChangedListener() {
		/**
		 * @param fixed 1 if gps on drone is fixed, 0 otherwise
		 */
		@Override
		public void onARDrone3GPSSettingsStateGPSFixStateChangedUpdate(final byte fixed) {
			mGPS.setFixed(fixed != 0);
		}
	};

	/**
	 * GPSの状態を受信した時
	 */
	private final ARCommandARDrone3GPSSettingsStateGPSUpdateStateChangedListener
		mGPSSettingsStateGPSUpdateStateChangedListener
		= new ARCommandARDrone3GPSSettingsStateGPSUpdateStateChangedListener() {
		@Override
		public void onARDrone3GPSSettingsStateGPSUpdateStateChangedUpdate(
			final ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM state) {
			switch (state) {
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_UPDATED:
				// 0: Drone GPS update succeed
				break;
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_INPROGRESS:
				// 1: Drone GPS update In progress
				break;
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_FAILED:
				// 2: Drone GPS update failed
				mStatus.setPosition(AttributePosition.INVALID_VALUE, AttributePosition.INVALID_VALUE, AttributePosition.INVALID_VALUE);
				break;
			}
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
	 * 写真撮影時のフォーマットを受信した時
	 */
	private final ARCommandARDrone3PictureSettingsStatePictureFormatChangedListener
		mPictureSettingsStatePictureFormatChangedListener
		= new ARCommandARDrone3PictureSettingsStatePictureFormatChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStatePictureFormatChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED_TYPE_ENUM type) {

			if (DEBUG) Log.v(TAG, "onARDrone3PictureSettingsStatePictureFormatChangedUpdate:" + type);
			if (mPictureFormat != type.getValue()) {
				mPictureFormat = type.getValue();
				// FIXME 未実装
			}
		}
	};

	/**
	 * 写真撮影状態を受信した時のコールバックリスナー
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

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStatePictureStateChangedUpdate:state=" + state + ",mass_storage_id=" + mass_storage_id);
		}
	};

	/**
	 * 写真撮影状態を受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener
		mMediaRecordStatePictureStateChangedV2Listener
		= new ARCommandARDrone3MediaRecordStatePictureStateChangedV2Listener() {
		@Override
		public void onARDrone3MediaRecordStatePictureStateChangedV2Update(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStatePictureStateChangedV2Update:state=" + state + ",error=" + error);

			int _state;
			switch (state) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_READY:		// 撮影可能
				_state = DroneStatus.MEDIA_READY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_BUSY:			// 撮影中
				_state = DroneStatus.MEDIA_BUSY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_NOTAVAILABLE:	// 撮影不可
			default:
				_state = DroneStatus.MEDIA_UNAVAILABLE;
				break;
			}
			callOnStillCaptureStateChanged(_state);
		}
	};

	/**
	 * 写真撮影イベントを受信した時のコールバックリスナー
	 */
	private final ARCommandARDrone3MediaRecordEventPictureEventChangedListener
		mMediaRecordEventPictureEventChangedListener
		= new ARCommandARDrone3MediaRecordEventPictureEventChangedListener() {
		@Override
		public void onARDrone3MediaRecordEventPictureEventChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordEventPictureEventChangedUpdate:event=" + event + ",error=" + error);
			int _state;
			switch (event) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_TAKEN:			// 撮影成功
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_FAILED:			// 撮影失敗
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnStillCaptureStateChanged(_state);
		}
	};

	/**
	 * ビデオ撮影状態を受信した時
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

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStateVideoStateChangedUpdate:state=" + state + ",mass_storage_id=" + mass_storage_id);
		}
	};

	/**
	 * ビデオ撮影状態を受信した時
	 */
	private final ARCommandARDrone3MediaRecordStateVideoStateChangedV2Listener
		mMediaRecordStateVideoStateChangedV2Listener
			= new ARCommandARDrone3MediaRecordStateVideoStateChangedV2Listener() {

		@Override
		public void onARDrone3MediaRecordStateVideoStateChangedV2Update(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_STATE_ENUM state,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStateVideoStateChangedV2Update:state=" + state + ",error=" + error);
			int _state;
			switch (state) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_STATE_STOPPED:
				_state = DroneStatus.MEDIA_READY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_STATE_STARTED:
				_state = DroneStatus.MEDIA_BUSY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2_STATE_NOTAVAILABLE:
			default:
				_state = DroneStatus.MEDIA_UNAVAILABLE;
				break;
			}
			callOnVideoRecordingStateChanged(_state);
		}
	};

	/**
	 * 動画撮影イベントを受信した時
	 */
	private final ARCommandARDrone3MediaRecordEventVideoEventChangedListener
		mMediaRecordEventVideoEventChangedListener
			= new ARCommandARDrone3MediaRecordEventVideoEventChangedListener() {

		@Override
		public void onARDrone3MediaRecordEventVideoEventChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_ENUM event,
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordEventVideoEventChangedUpdate:state=" + event + ",error=" + error);
			int _state;
			switch (event) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_START:
				_state = DroneStatus.MEDIA_BUSY;
				// XXX ここはreturnした方がいいかも?
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_STOP:
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_FAILED:
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnVideoRecordingStateChanged(_state);
		}
	};

	/**
	 * オートホワイトバランス設定を受信した時<br>
	 * 0: 自動 Auto guess of best white balance params<br>
	 * 1: 電球色 Tungsten white balance<br>
	 * 2: 晴天 Daylight white balance<br>
	 * 3: 曇り空 Cloudy white balance<br>
	 * 4: フラシュ撮影用 White balance for a flash<br>
	 */
	private final ARCommandARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener
		mPictureSettingsStateAutoWhiteBalanceChangedListener
			= new ARCommandARDrone3PictureSettingsStateAutoWhiteBalanceChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateAutoWhiteBalanceChangedUpdate(
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED_TYPE_ENUM type) {
			mSettings.autoWhiteBalance(type.getValue());
		}
	};

	/**
	 * 露出設定を受信した時
	 */
	private final ARCommandARDrone3PictureSettingsStateExpositionChangedListener
		mPictureSettingsStateExpositionChangedListener
			= new ARCommandARDrone3PictureSettingsStateExpositionChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateExpositionChangedUpdate(
			final float current, final float min, final float max) {
			mSettings.setExposure(current, min, max);
		}
	};

	/**
	 * 彩度設定を受信した時
	 */
	private final ARCommandARDrone3PictureSettingsStateSaturationChangedListener
		mPictureSettingsStateSaturationChangedListener
			= new ARCommandARDrone3PictureSettingsStateSaturationChangedListener() {
		@Override
		public void onARDrone3PictureSettingsStateSaturationChangedUpdate(
			final float current, final float min, final float max) {
			mSettings.setSaturation(current, min, max);
		}
	};

	/**
	 * タイムラプス設定を受信した時
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
			mSettings.setTimeLapse(enabled != 0, interval, minInterval, maxInterval);
		}
	};

	/**
	 * 自動録画設定を受信した時
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

//			if (DEBUG) Log.v(TAG, "onARDrone3PictureSettingsStateVideoAutorecordChangedUpdate:enabled=" + enabled + ",mass_storage_id=" + mass_storage_id);
			mSettings.mCamera.autoRecord(enabled != 0, mass_storage_id);
		}
	};

	/**
	 * ライブビデオストリーミングの有効無効を受信した時
	 */
	private final ARCommandARDrone3MediaStreamingStateVideoEnableChangedListener
		mMediaStreamingStateVideoEnableChangedListener
		= new ARCommandARDrone3MediaStreamingStateVideoEnableChangedListener() {
		@Override
		public void onARDrone3MediaStreamingStateVideoEnableChangedUpdate(
			final ARCOMMANDS_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED_ENABLED_ENUM enabled) {

			if (DEBUG) Log.v(TAG, "onARDrone3MediaStreamingStateVideoEnableChangedUpdate:enabled=" + enabled);

			// 0: Video streaming is enabled.
			// 1: Video streaming is disabled.
			// 2: Video streaming failed to start.
			mSettings.mCamera.videoStateState(enabled.getValue());
		}
	};

	/**
	 * カメラの向きを受信した時
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

			mSettings.mCamera.pantilt(pan, tilt);
			if (mCameraControllerListener != null) {
				try {
					mCameraControllerListener.onCameraOrientationChanged(pan, tilt);
				} catch (final Exception e) {
					// ignore
				}
			}
		}
	};

	/**
	 * 使用しているバッテリーの種類設定を受信した時
	 */
	private final ARCommandARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener
		mDebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener
			= new ARCommandARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedListener() {
		/**
		 * @param drone2BatteryUsed 1 if the drone2 battery is used, 0 if the drone3 battery is used
		 */
		@Override
		public void onARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedUpdate(final byte drone2BatteryUsed) {

			if (DEBUG) Log.v(TAG, "onARDrone3DebugBatteryDebugSettingsStateUseDrone2BatteryChangedUpdate:drone2BatteryUsed=" + drone2BatteryUsed);
			// FIXME 未実装
		}
	};

	/**
	 * 捕捉しているGPS衛星の数を受信した時
	 */
	private final ARCommandARDrone3DebugGPSDebugStateNbSatelliteChangedListener
		mDebugGPSDebugStateNbSatelliteChangedListener
			= new ARCommandARDrone3DebugGPSDebugStateNbSatelliteChangedListener() {
		/**
		 * @param nbSatellite Amount of satellite used by gps location
		 */
		@Override
		public void onARDrone3DebugGPSDebugStateNbSatelliteChangedUpdate(final byte nbSatellite) {

			if (DEBUG) Log.v(TAG, "onARDrone3DebugGPSDebugStateNbSatelliteChangedUpdate:nbSatellite=" + nbSatellite);
			mGPS.numGpsSatellite = nbSatellite;
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
	 * @param heading [-180,180] (無効みたい)
	 * @return
	 */
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, heading);
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
	 * 非常停止指示
	 * @return
	 */
	@Override
	public boolean requestEmergencyStop() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dEmergencyId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	/**
	 * フラットトリム指示
	 * @return
	 */
	@Override
	public boolean requestFlatTrim() {
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
	public boolean requestTakeoff() {
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
	public boolean requestLanding() {
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
	public boolean setMaxAltitude(final float altitude) {
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
	public boolean setMaxTilt(final float tilt) {
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
	public boolean setMaxVerticalSpeed(final float speed) {
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
	public boolean setMaxRotationSpeed(final float speed) {
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

	@Override
	public boolean canGetAttitude() {
		return true;
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
	public boolean setHasGuard(final boolean has_guard) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3SpeedSettingsHullProtection((byte)(has_guard ? 1 : 0));
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


//********************************************************************************
// WiFi関係
//********************************************************************************
	/** WiFiの状態 */
	private final Map<String, WiFiStatus> mWifiStatus = new HashMap<String, WiFiStatus>();

	/**
	 * WiFiスキャンリストが変更された時
	 * @param ssid SSID of the AP
	 * @param rssi RSSI of the AP in dbm (negative value) 受信信号強度
	 * @param band The band : 2.4 GHz or 5 GHz
	 * @param channel Channel of the AP
	 */
	protected void onWifiScanListChangedUpdate(
		final String ssid, final short rssi,
		final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM band,
		final byte channel) {
		Log.d(TAG, String.format("ssid=%s,rssi=%d,band=%s,channel=%d", ssid, rssi, band.toString(), channel));
		final String key = band.toString() + Byte.toString(channel);
		WiFiStatus status = mWifiStatus.get(key);
		if (status == null) {
			status = new WiFiStatus(-66);
		}
		status.ssid = ssid;
		status.rssi = rssi;
		status.band = band.getValue();
		status.channel = channel;
	}

	/**
	 * WiFiスキャンが変化した時
	 */
	protected void onAllWifiScanChangedUpdate() {
	}

	/**
	 * WiFiチャンネルリストが変化した時
	 * @param band The band of this channel : 2.4 GHz or 5 GHz
	 * @param channel The authorized channel.
	 * @param in_or_out Bit 0 is 1 if channel is authorized outside (0 otherwise) ; Bit 1 is 1 if channel is authorized inside (0 otherwise)
	 */
	protected void onWifiAuthChannelListChangedUpdate(
		final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band,
		final byte channel,
		final byte in_or_out) {
		Log.d(TAG, String.format("band=%s, channel=%d, in_or_out=%d", band.toString(), channel, in_or_out));
		final String key = band.toString() + Byte.toString(channel);
	}

	/**
	 * WiFiチャネルの状態が変化した時
	 */
	protected void onAllWifiAuthChannelChangedUpdate() {
	}

	/**
	 * WiFiの選択状態が変化した時
	 * @param type The type of wifi selection settings
	 * @param band The actual  wifi band state
	 * @param channel The channel (depends of the band)
	 */
	protected void onWifiSelectionChangedUpdate(
		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type,
		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band,
		final byte channel) {
	}

	/**
	 * 室外モードか室内モードかを設定
	 * @param is_outdoor
	 * @return
	 */
	public boolean sendSpeedSettingsOutdoor(final boolean is_outdoor) {
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
//********************************************************************************
// 映像関係
//********************************************************************************
//--------------------------------------------------------------------------------
// ICameraControllerのメソッド
//--------------------------------------------------------------------------------
	private CameraControllerListener mCameraControllerListener;
	@Override
	public synchronized void setCameraControllerListener(final CameraControllerListener listener) {
		mCameraControllerListener = listener;
	}

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera
	 * @return
	 */
	@Override
	public boolean sendPictureFormat(final int pictureFormat) {

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
	 * カメラの方向を設定, コールバックの返り値から推測すると設定可能なのは[-100;100]<br>
	 * Tilt and pan value is saturated by the drone.<br>
	 * Saturation value is sent by the drone through CameraSettingsChanged command.
	 * @param tilt Tilt camera consign for the drone (in degree).
	 * @param pan Pan camera consign for the drone (in degree)
	 * @return
	 */
	@Override
	public boolean sendCameraOrientation(final int tilt, final int pan) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3CameraOrientation((byte)tilt, (byte)pan);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Orientation command.");
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
	@Override
	public boolean sendAutoWhiteBalance(final int auto_white_balance) {

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;

		if ((auto_white_balance >= 0) && (auto_white_balance <= 4)) {
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM.getFromValue(auto_white_balance);

			cmdError = cmd.setARDrone3PictureSettingsAutoWhiteBalanceSelection(type);
		} else {
			// FIXME 手動設定の時の設定値はどうやって設定するんやろ?
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
	@Override
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
	@Override
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
	 * タイムラプス設定(一定時間毎に自動撮影)
	 * @param enabled
	 * @param interval 撮影間隔[秒]
	 * @return
	 */
	@Override
	public boolean sendTimelapseSelection(final boolean enabled, final float interval) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsTimelapseSelection((byte)(enabled ? 1 : 0), interval);
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
	@Override
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PictureSettingsVideoAutorecordSelection((byte)(enabled ? 1 : 0), (byte)mass_storage_id);
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
	@Override
	public boolean sendWobbleCancellation(final boolean enabled) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3DebugVideoEnableWobbleCancellation((byte)(enabled ? 1 : 0));
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
	@Override
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

	@Override
	public int getPan() {
		return (int)mSettings.currentCameraPan();
	}

	@Override
	public int getTilt() {
		return (int)mSettings.currentCameraTilt();
	}

	/**
	 * 静止画撮影要求
	 * @param mass_storage_id
	 * @return
	 */
	@Override
	public boolean requestTakePicture(final int mass_storage_id) {
		boolean sentStatus = true;

		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaRecordPicture((byte)mass_storage_id);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Picture command.");
		}

		return sentStatus;
	}

	/**
	 * 静止画撮影要求
	 * @return
	 */
	public boolean requestTakePicture() {
		boolean sentStatus = true;

		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaRecordPictureV2();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send PictureV2 command.");
		}

		return sentStatus;
	}

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @param mass_storage_id
	 * @return
	 */
	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		boolean sentStatus = true;

		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM record
			= ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.getFromValue(start ? 1 : 0);

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaRecordVideo(record, (byte)mass_storage_id);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Video recording command.");
		}

		return sentStatus;
	}

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start) {
		boolean sentStatus = true;

		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM record
			= ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.getFromValue(start ? 1 : 0);

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaRecordVideoV2(record);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Video recording command.");
		}

		return sentStatus;
	}

//--------------------------------------------------------------------------------
// IVideoStreamControllerのメソッド
//--------------------------------------------------------------------------------

	/**
	 * sendVideoStreamingEnable/enableVideoStreamingにtrueをセットする前にIVideoStreamをセットしないとダメ
	 * @param video_stream
	 */
	@Override
	public void setVideoStream(final IVideoStream video_stream) {
		if (mVideoStreamDelegater != null) {
			mVideoStreamDelegater.setVideoStream(video_stream);
		}
	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return mSettings.mCamera.isVideoStreamingEnabled();
	}

	/**
	 * ビデオストリーミング設定
	 * @param enable true: ビデオストリーミング開始, false:ビデオストリーミング停止
	 */
	@Override
	public boolean enableVideoStreaming(boolean enable) {
		if (DEBUG) Log.v(TAG, "enableVideoStreaming:enable=" + enable + ", mVideoStreamDelegater=" + mVideoStreamDelegater);
		if (mVideoStreamDelegater != null) {
			return mVideoStreamDelegater.enableVideoStreaming(enable);
		} else {
			final IBridgeController bridge = getBridge();
			if (bridge != null) {
				VideoStreamDelegater.sendVideoStreamingEnable(this, mNetConfig, enable);
			}
		}
		return false;
	}

//********************************************************************************
// GPS関係
//********************************************************************************
	/**
	 * ホーム位置を設定(GPS座標)
	 * @param latitude 緯度[度]
	 * @param longitude 経度[度]
	 * @param altitude 高度[m]
	 * @return
	 */
	public boolean sendSetHome(final double latitude, final double longitude, final double altitude) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3GPSSettingsSetHome(latitude, longitude, altitude);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send SetHome command.");
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
			Log.e(TAG, "Failed to send ResetHome command.");
		}

		return sentStatus;
	}

	/**
	 * ナビゲーションホーム実行(FIXME GPS座標が無効なら無視したほうが良い?)
	 * @param start
	 * @return
	 */
	public boolean sendNavigateHome(final boolean start) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3PilotingNavigateHome((byte)(start ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send NavigateHome command.");
		}

		return sentStatus;
	}

	/**
	 * drone2バッテリーを使用するかどうか
	 * @param use_drone2Battery
	 * @return
	 */
	public boolean sendUseDrone2Battery(final boolean use_drone2Battery) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3DebugBatteryDebugSettingsUseDrone2Battery((byte)(use_drone2Battery ? 1 : 0));
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
	 * @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * @return
	 */
	@Override
	public boolean requestAnimationsFlip(final int direction) {

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
	 * 自動で指定した角度回転させる
	 * @param degree -180〜180度
	 * @return
	 */
	@Override
	public boolean requestAnimationsCap(final int degree) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;

		if (degree != 0) {

			final AttributeFloat rotation_speed = mSettings.maxRotationSpeed();    // 回転速度[度/秒]
			final float current = rotation_speed.current();
			try {
				try {
					if (current != rotation_speed.max()) {
						// 最大回転速度に変更する
						setMaxRotationSpeed(rotation_speed.max());
						Thread.sleep(5);
					}
					final long t = (long) Math.abs(degree / rotation_speed.max() * 1000);    // 回転時間[ミリ秒]を計算
					setYaw(degree > 0 ? 100 : -100);
					Thread.sleep(t + 5);
				} catch (InterruptedException e) {
				}
				setYaw(0);
			} finally {
				// 元の回転速度設定に戻す
				setMaxRotationSpeed(current);
			}
		}
		return sentStatus;
	}

	protected void prepare_network() {
		if (DEBUG) Log.v(TAG, "prepare_network:");
		// TODO :  if ardiscoveryConnect ok
		mNetConfig.addStreamReaderIOBuffer(mNetConfig.getFragmentSize(), mNetConfig.getMaxFragmentNum());
	}

	/**
	 * ビデオストリーミングデータ受信スレッドを開始
	 */
	protected void startVideoThread() {
		if (DEBUG) Log.v(TAG, "startVideoThread");
		if (mVideoStreamDelegater == null) {
//			mVideoStreamDelegater = new VideoStreamDelegater(this, mNetConfig);
			videoStreamDelegaterCreated = false;
			final IBridgeController bridge = getBridge();
			if (bridge != null) {
				mVideoStreamDelegater = bridge.getVideoStreamDelegater();
			}
			if (mVideoStreamDelegater == null) {
				mVideoStreamDelegater = new VideoStreamDelegater(this, mNetConfig);
				videoStreamDelegaterCreated = true;
			}
		}
		if (videoStreamDelegaterCreated && (mVideoStreamDelegater != null)) {
			mVideoStreamDelegater.startVideoThread();
		}
		if (DEBUG) Log.v(TAG, "startVideoThread:終了");
	}

	/**
	 * ストリーミングデータ受信スレッドを終了(終了するまで戻らない)
	 */
	protected void stopVideoThread() {
		if (DEBUG) Log.v(TAG, "stopVideoThread:");
		if (videoStreamDelegaterCreated && (mVideoStreamDelegater != null)) {
			mVideoStreamDelegater.stopVideoThread();
		}
		videoStreamDelegaterCreated = false;
		mVideoStreamDelegater = null;
		if (DEBUG) Log.v(TAG, "stopVideoThread:終了");
	}

}
