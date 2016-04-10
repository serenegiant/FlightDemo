package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureSkyController;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.CommonStatus;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.IBridgeController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.IWiFiController;
import com.serenegiant.arflight.VideoStreamDelegater;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.util.concurrent.Semaphore;

public class SkyControllerNewAPI extends FlightControllerBebopNewAPI implements /*IBridgeController,*/ IVideoStreamController, IWiFiController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = SkyControllerNewAPI.class.getSimpleName();

	/**
	 * 接続待ちのためのセマフォ
	 */
	private final Semaphore skyControllerConnectSent = new Semaphore(0);
	protected volatile boolean mRequestSkyControllerConnect;

	protected CommonStatus mSkyControllerStatus = new CommonStatus();

	public SkyControllerNewAPI(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
	}

	/** 接続開始時の追加処理 */
	protected void internal_start() {
		// onExtensionStateChangedが呼ばれるまで待機する
		if (!mRequestSkyControllerConnect) {
			try {
				mRequestSkyControllerConnect = true;
				skyControllerConnectSent.acquire();
			} catch (final InterruptedException e) {
			} finally {
				mRequestSkyControllerConnect = false;
			}
		}
		super.internal_start();
	}

	protected void internal_cancel_start() {
		if (mRequestSkyControllerConnect) {
			skyControllerConnectSent.release();
		}
		super.internal_cancel_start();
	}

	/**
	 * onExtensionStateChangedの下請け
	 * これはスカイコントローラーが機体に接続した時に呼ばれる
	 */
	protected void onExtensionConnect() {
		if (DEBUG) Log.d(TAG, "onExtensionConnect:");
		super.onExtensionConnect();
		if (mRequestSkyControllerConnect) {
			skyControllerConnectSent.release();
		}
	}

	/**
	 * onExtensionStateChangedの下請け
	 * これはスカイコントローラーが機体から切断された時に呼ばれる
	 */
	protected void onExtensionDisconnect() {
		if (DEBUG) Log.d(TAG, "onExtensionDisconnect:");
		if (mRequestSkyControllerConnect) {
			skyControllerConnectSent.release();
		}
		super.onExtensionDisconnect();
	}

	@Override
	protected boolean isActive() {
		synchronized (mStateSync) {
			return super.isActive()
				&& ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mExtensionState);
		}
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		super.onCommandReceived(deviceController, commandKey, args);

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER:	// (119, "Key used to define the feature <code>SkyControllerNewAPI</code>"),
			break;
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST:	// (120, "Key used to define the command <code>WifiList</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "WIFISTATE_WIFILIST:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED:	// (121, "Key used to define the command <code>ConnexionChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "WIFISTATE_CONNEXIONCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED:	// (122, "Key used to define the command <code>WifiAuthChannelListChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "WIFISTATE_WIFIAUTHCHANNELLISTCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_ALLWIFIAUTHCHANNELCHANGED:	// (123, "Key used to define the command <code>AllWifiAuthChannelChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "WIFISTATE_ALLWIFIAUTHCHANNELCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFISIGNALCHANGED:	// (124, "Key used to define the command <code>WifiSignalChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "WIFISTATE_WIFISIGNALCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_DEVICELIST:	// (125, "Key used to define the command <code>DeviceList</code> of class <code>DeviceState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "DEVICESTATE_DEVICELIST:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED:	// (126, "Key used to define the command <code>ConnexionChanged</code> of class <code>DeviceState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "DEVICESTATE_CONNEXIONCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_ALLSETTINGSCHANGED:	// (127, "Key used to define the command <code>AllSettingsChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SETTINGSSTATE_ALLSETTINGSCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_RESETCHANGED:	// (128, "Key used to define the command <code>ResetChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SETTINGSSTATE_RESETCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTSERIALCHANGED:	// (129, "Key used to define the command <code>ProductSerialChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SETTINGSSTATE_PRODUCTSERIALCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED:	// (130, "Key used to define the command <code>ProductVariantChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SETTINGSSTATE_PRODUCTVARIANTCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COMMONSTATE_ALLSTATESCHANGED:	// (131, "Key used to define the command <code>AllStatesChanged</code> of class <code>CommonState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COMMONSTATE_ALLSTATESCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED:	// (132, "Key used to define the command <code>BatteryChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// バッテリー残量が変化した時
			final int percent = (Integer) args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED_PERCENT);
			mSkyControllerStatus.setBattery(percent);
			callOnUpdateBattery(percent);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSFIXCHANGED:	// (133, "Key used to define the command <code>GpsFixChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SKYCONTROLLERSTATE_GPSFIXCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED:	// (134, "Key used to define the command <code>GpsPositionChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SKYCONTROLLERSTATE_GPSPOSITIONCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTSSIDCHANGED:	// (135, "Key used to define the command <code>AccessPointSSIDChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSPOINTSETTINGSSTATE_ACCESSPOINTSSIDCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTCHANNELCHANGED:	// (136, "Key used to define the command <code>AccessPointChannelChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSPOINTSETTINGSSTATE_ACCESSPOINTCHANNELCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED:	// (137, "Key used to define the command <code>WifiSelectionChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL:	// (138, "Key used to define the command <code>GamepadControl</code> of class <code>GamepadInfosState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "GAMEPADINFOSSTATE_GAMEPADCONTROL:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_ALLGAMEPADCONTROLSSENT:	// (139, "Key used to define the command <code>AllGamepadControlsSent</code> of class <code>GamepadInfosState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "GAMEPADINFOSSTATE_ALLGAMEPADCONTROLSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS:	// (140, "Key used to define the command <code>CurrentButtonMappings</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_ALLCURRENTBUTTONMAPPINGSSENT:	// (141, "Key used to define the command <code>AllCurrentButtonMappingsSent</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "BUTTONMAPPINGSSTATE_ALLCURRENTBUTTONMAPPINGSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS:	// (142, "Key used to define the command <code>AvailableButtonMappings</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_ALLAVAILABLEBUTTONSMAPPINGSSENT:	// (143, "Key used to define the command <code>AllAvailableButtonsMappingsSent</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "BUTTONMAPPINGSSTATE_ALLAVAILABLEBUTTONSMAPPINGSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS:	// (144, "Key used to define the command <code>CurrentAxisMappings</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_ALLCURRENTAXISMAPPINGSSENT:	// (145, "Key used to define the command <code>AllCurrentAxisMappingsSent</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISMAPPINGSSTATE_ALLCURRENTAXISMAPPINGSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS:	// (146, "Key used to define the command <code>AvailableAxisMappings</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_ALLAVAILABLEAXISMAPPINGSSENT:	// (147, "Key used to define the command <code>AllAvailableAxisMappingsSent</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISMAPPINGSSTATE_ALLAVAILABLEAXISMAPPINGSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS:	// (148, "Key used to define the command <code>CurrentAxisFilters</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISFILTERSSTATE_CURRENTAXISFILTERS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_ALLCURRENTFILTERSSENT:	// (149, "Key used to define the command <code>AllCurrentFiltersSent</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISFILTERSSTATE_ALLCURRENTFILTERSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_PRESETAXISFILTERS:	// (150, "Key used to define the command <code>PresetAxisFilters</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISFILTERSSTATE_PRESETAXISFILTERS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_ALLPRESETFILTERSSENT:	// (151, "Key used to define the command <code>AllPresetFiltersSent</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AXISFILTERSSTATE_ALLPRESETFILTERSSENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE:	// (152, "Key used to define the command <code>PilotingSource</code> of class <code>CoPilotingState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COPILOTINGSTATE_PILOTINGSOURCE:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE:	// (153, "Key used to define the command <code>MagnetoCalibrationState</code> of class <code>CalibrationState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONQUALITYUPDATESSTATE:	// (154, "Key used to define the command <code>MagnetoCalibrationQualityUpdatesState</code> of class <code>CalibrationState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CALIBRATIONSTATE_MAGNETOCALIBRATIONQUALITYUPDATESSTATE:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONEVENTS_SETTINGS:	// (155, "Key used to define the command <code>Settings</code> of class <code>ButtonEvents</code> in project <code>SkyControllerNewAPI</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "BUTTONEVENTS_SETTINGS:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLERDEBUG:	// (156, "Key used to define the feature <code>SkyControllerDebug</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "SKYCONTROLLERDEBUG:");
			break;
		}
		default:
			break;
		}
	}
//================================================================================
// IBridgeController
//================================================================================
//	@Override
//	public ARNetworkConfig createBridgeNetConfig() {
//		return null;
//	}
//
//	@Override
//	public VideoStreamDelegater getVideoStreamDelegater() {
//		return null;
//	}
//
//	@Override
//	public boolean connectTo(final DeviceInfo info) {
//		return false;
//	}
//
//	@Override
//	public void disconnectFrom() {
//
//	}
//
//	@Override
//	public DeviceInfo connectDeviceInfo() {
//		return null;
//	}
}
