package com.serenegiant.arflight;

import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsAccessPointChannelListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAccessPointSettingsWifiSelectionListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersDefaultAxisFiltersListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersGetCurrentAxisFiltersListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersGetPresetAxisFiltersListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersSetAxisFilterListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsDefaultAxisMappingListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsGetAvailableAxisMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsGetCurrentAxisMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsSetAxisMappingListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonEventsSettingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsDefaultButtonMappingListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsGetAvailableButtonMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsGetCurrentButtonMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsSetButtonMappingListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCameraResetOrientationListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCoPilotingSetPilotingSourceListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCoPilotingStatePilotingSourceListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCommonAllStatesListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCommonStateAllStatesChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDebugDebugTest1Listener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDeviceConnectToDeviceListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDeviceRequestCurrentDeviceListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDeviceRequestDeviceListListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDeviceStateConnexionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerDeviceStateDeviceListListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerGamepadInfosGetGamepadControlsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerGamepadInfosStateGamepadControlListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsAllSettingsListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsResetListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsStateAllSettingsChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsStateProductSerialChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsStateProductVariantChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSettingsStateResetChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSkyControllerStateBatteryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSkyControllerStateGpsFixChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerSkyControllerStateGpsPositionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiConnectToWifiListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiForgetWifiListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiRequestCurrentWifiListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiRequestWifiListListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiStateConnexionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiStateWifiAuthChannelListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiStateWifiListListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiStateWifiSignalChangedListener;
import com.parrot.arsdk.arcommands.ARCommandSkyControllerWifiWifiAuthChannelListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.arflight.configs.ARNetworkConfigARDrone3;
import com.serenegiant.arflight.configs.ARNetworkConfigSkyController;

import java.sql.Date;

/**
 * Created by saki on 15/10/31.
 */
public class SkyController extends DeviceController implements IBridgeController, IWiFiController {
	private static final boolean DEBUG = true;				// FIXME 実働時はfalseにすること
	private static final String TAG = SkyController.class.getSimpleName();

	public SkyController(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigSkyController());
		mInfo = new AttributeDevice();
	}

//================================================================================
// ARSDK3からのコールバックリスナー関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		super.registerARCommandsListener();
		ARCommand.setSkyControllerWifiStateWifiListListener(mARCommandSkyControllerWifiStateWifiListListener);
		ARCommand.setSkyControllerWifiStateConnexionChangedListener(mARCommandSkyControllerWifiStateConnexionChangedListener);
		ARCommand.setSkyControllerWifiStateWifiAuthChannelListChangedListener(mARCommandSkyControllerWifiStateWifiAuthChannelListChangedListener);
		ARCommand.setSkyControllerWifiStateAllWifiAuthChannelChangedListener(mARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener);
		ARCommand.setSkyControllerWifiStateWifiSignalChangedListener(mARCommandSkyControllerWifiStateWifiSignalChangedListener);
		ARCommand.setSkyControllerWifiRequestWifiListListener(mARCommandSkyControllerWifiRequestWifiListListener);
		ARCommand.setSkyControllerWifiRequestCurrentWifiListener(mARCommandSkyControllerWifiRequestCurrentWifiListener);
		ARCommand.setSkyControllerWifiConnectToWifiListener(mARCommandSkyControllerWifiConnectToWifiListener);
		ARCommand.setSkyControllerWifiForgetWifiListener(mARCommandSkyControllerWifiForgetWifiListener);
		ARCommand.setSkyControllerWifiWifiAuthChannelListener(mARCommandSkyControllerWifiWifiAuthChannelListener);
		ARCommand.setSkyControllerDeviceRequestDeviceListListener(mARCommandSkyControllerDeviceRequestDeviceListListener);
		ARCommand.setSkyControllerDeviceRequestCurrentDeviceListener(mARCommandSkyControllerDeviceRequestCurrentDeviceListener);
		ARCommand.setSkyControllerDeviceConnectToDeviceListener(mARCommandSkyControllerDeviceConnectToDeviceListener);
		ARCommand.setSkyControllerDeviceStateDeviceListListener(mARCommandSkyControllerDeviceStateDeviceListListener);
		ARCommand.setSkyControllerDeviceStateConnexionChangedListener(mARCommandSkyControllerDeviceStateConnexionChangedListener);
		ARCommand.setSkyControllerSettingsAllSettingsListener(mARCommandSkyControllerSettingsAllSettingsListener);
		ARCommand.setSkyControllerSettingsResetListener(mARCommandSkyControllerSettingsResetListener);
		ARCommand.setSkyControllerSettingsStateAllSettingsChangedListener(mARCommandSkyControllerSettingsStateAllSettingsChangedListener);
		ARCommand.setSkyControllerSettingsStateResetChangedListener(mARCommandSkyControllerSettingsStateResetChangedListener);
		ARCommand.setSkyControllerSettingsStateProductSerialChangedListener(mARCommandSkyControllerSettingsStateProductSerialChangedListener);
		ARCommand.setSkyControllerSettingsStateProductVariantChangedListener(mARCommandSkyControllerSettingsStateProductVariantChangedListener);
		ARCommand.setSkyControllerCommonAllStatesListener(mARCommandSkyControllerCommonAllStatesListener);
		ARCommand.setSkyControllerCommonStateAllStatesChangedListener(mARCommandSkyControllerCommonStateAllStatesChangedListener);
		ARCommand.setSkyControllerSkyControllerStateBatteryChangedListener(mARCommandSkyControllerSkyControllerStateBatteryChangedListener);
		ARCommand.setSkyControllerSkyControllerStateGpsFixChangedListener(mARCommandSkyControllerSkyControllerStateGpsFixChangedListener);
		ARCommand.setSkyControllerSkyControllerStateGpsPositionChangedListener(mARCommandSkyControllerSkyControllerStateGpsPositionChangedListener);
		ARCommand.setSkyControllerAccessPointSettingsAccessPointSSIDListener(mARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener);
		ARCommand.setSkyControllerAccessPointSettingsAccessPointChannelListener(mARCommandSkyControllerAccessPointSettingsAccessPointChannelListener);
		ARCommand.setSkyControllerAccessPointSettingsWifiSelectionListener(mARCommandSkyControllerAccessPointSettingsWifiSelectionListener);
		ARCommand.setSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener(mARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener);
		ARCommand.setSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener(mARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener);
		ARCommand.setSkyControllerAccessPointSettingsStateWifiSelectionChangedListener(mARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener);
		ARCommand.setSkyControllerCameraResetOrientationListener(mARCommandSkyControllerCameraResetOrientationListener);
		ARCommand.setSkyControllerGamepadInfosGetGamepadControlsListener(mARCommandSkyControllerGamepadInfosGetGamepadControlsListener);
		ARCommand.setSkyControllerGamepadInfosStateGamepadControlListener(mARCommandSkyControllerGamepadInfosStateGamepadControlListener);
		ARCommand.setSkyControllerGamepadInfosStateAllGamepadControlsSentListener(mARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener);
		ARCommand.setSkyControllerButtonMappingsGetCurrentButtonMappingsListener(mARCommandSkyControllerButtonMappingsGetCurrentButtonMappingsListener);
		ARCommand.setSkyControllerButtonMappingsGetAvailableButtonMappingsListener(mARCommandSkyControllerButtonMappingsGetAvailableButtonMappingsListener);
		ARCommand.setSkyControllerButtonMappingsSetButtonMappingListener(mARCommandSkyControllerButtonMappingsSetButtonMappingListener);
		ARCommand.setSkyControllerButtonMappingsDefaultButtonMappingListener(mARCommandSkyControllerButtonMappingsDefaultButtonMappingListener);
		ARCommand.setSkyControllerButtonMappingsStateCurrentButtonMappingsListener(mARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener);
		ARCommand.setSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener(mARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener);
		ARCommand.setSkyControllerButtonMappingsStateAvailableButtonMappingsListener(mARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener);
		ARCommand.setSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener(mARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener);
		ARCommand.setSkyControllerAxisMappingsGetCurrentAxisMappingsListener(mARCommandSkyControllerAxisMappingsGetCurrentAxisMappingsListener);
		ARCommand.setSkyControllerAxisMappingsGetAvailableAxisMappingsListener(mARCommandSkyControllerAxisMappingsGetAvailableAxisMappingsListener);
		ARCommand.setSkyControllerAxisMappingsSetAxisMappingListener(mARCommandSkyControllerAxisMappingsSetAxisMappingListener);
		ARCommand.setSkyControllerAxisMappingsDefaultAxisMappingListener(mARCommandSkyControllerAxisMappingsDefaultAxisMappingListener);
		ARCommand.setSkyControllerAxisMappingsStateCurrentAxisMappingsListener(mARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener);
		ARCommand.setSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener(mARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener);
		ARCommand.setSkyControllerAxisMappingsStateAvailableAxisMappingsListener(mARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener);
		ARCommand.setSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener(mARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener);
		ARCommand.setSkyControllerAxisFiltersGetCurrentAxisFiltersListener(mARCommandSkyControllerAxisFiltersGetCurrentAxisFiltersListener);
		ARCommand.setSkyControllerAxisFiltersGetPresetAxisFiltersListener(mARCommandSkyControllerAxisFiltersGetPresetAxisFiltersListener);
		ARCommand.setSkyControllerAxisFiltersSetAxisFilterListener(mARCommandSkyControllerAxisFiltersSetAxisFilterListener);
		ARCommand.setSkyControllerAxisFiltersDefaultAxisFiltersListener(mARCommandSkyControllerAxisFiltersDefaultAxisFiltersListener);
		ARCommand.setSkyControllerAxisFiltersStateCurrentAxisFiltersListener(mARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener);
		ARCommand.setSkyControllerAxisFiltersStateAllCurrentFiltersSentListener(mARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener);
		ARCommand.setSkyControllerAxisFiltersStatePresetAxisFiltersListener(mARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener);
		ARCommand.setSkyControllerAxisFiltersStateAllPresetFiltersSentListener(mARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener);
		ARCommand.setSkyControllerCoPilotingSetPilotingSourceListener(mARCommandSkyControllerCoPilotingSetPilotingSourceListener);
		ARCommand.setSkyControllerCoPilotingStatePilotingSourceListener(mARCommandSkyControllerCoPilotingStatePilotingSourceListener);
		ARCommand.setSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener(mARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationStateListener(mARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener(mARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener);
		ARCommand.setSkyControllerButtonEventsSettingsListener(mARCommandSkyControllerButtonEventsSettingsListener);
		ARCommand.setSkyControllerDebugDebugTest1Listener(mARCommandSkyControllerDebugDebugTest1Listener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setSkyControllerWifiStateWifiListListener(null);
		ARCommand.setSkyControllerWifiStateConnexionChangedListener(null);
		ARCommand.setSkyControllerWifiStateWifiAuthChannelListChangedListener(null);
		ARCommand.setSkyControllerWifiStateAllWifiAuthChannelChangedListener(null);
		ARCommand.setSkyControllerWifiStateWifiSignalChangedListener(null);
		ARCommand.setSkyControllerWifiRequestWifiListListener(null);
		ARCommand.setSkyControllerWifiRequestCurrentWifiListener(null);
		ARCommand.setSkyControllerWifiConnectToWifiListener(null);
		ARCommand.setSkyControllerWifiForgetWifiListener(null);
		ARCommand.setSkyControllerWifiWifiAuthChannelListener(null);
		ARCommand.setSkyControllerDeviceRequestDeviceListListener(null);
		ARCommand.setSkyControllerDeviceRequestCurrentDeviceListener(null);
		ARCommand.setSkyControllerDeviceConnectToDeviceListener(null);
		ARCommand.setSkyControllerDeviceStateDeviceListListener(null);
		ARCommand.setSkyControllerDeviceStateConnexionChangedListener(null);
		ARCommand.setSkyControllerSettingsAllSettingsListener(null);
		ARCommand.setSkyControllerSettingsResetListener(null);
		ARCommand.setSkyControllerSettingsStateAllSettingsChangedListener(null);
		ARCommand.setSkyControllerSettingsStateResetChangedListener(null);
		ARCommand.setSkyControllerSettingsStateProductSerialChangedListener(null);
		ARCommand.setSkyControllerSettingsStateProductVariantChangedListener(null);
		ARCommand.setSkyControllerCommonAllStatesListener(null);
		ARCommand.setSkyControllerCommonStateAllStatesChangedListener(null);
		ARCommand.setSkyControllerSkyControllerStateBatteryChangedListener(null);
		ARCommand.setSkyControllerSkyControllerStateGpsFixChangedListener(null);
		ARCommand.setSkyControllerSkyControllerStateGpsPositionChangedListener(null);
		ARCommand.setSkyControllerAccessPointSettingsAccessPointSSIDListener(null);
		ARCommand.setSkyControllerAccessPointSettingsAccessPointChannelListener(null);
		ARCommand.setSkyControllerAccessPointSettingsWifiSelectionListener(null);
		ARCommand.setSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener(null);
		ARCommand.setSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener(null);
		ARCommand.setSkyControllerAccessPointSettingsStateWifiSelectionChangedListener(null);
		ARCommand.setSkyControllerCameraResetOrientationListener(null);
		ARCommand.setSkyControllerGamepadInfosGetGamepadControlsListener(null);
		ARCommand.setSkyControllerGamepadInfosStateGamepadControlListener(null);
		ARCommand.setSkyControllerGamepadInfosStateAllGamepadControlsSentListener(null);
		ARCommand.setSkyControllerButtonMappingsGetCurrentButtonMappingsListener(null);
		ARCommand.setSkyControllerButtonMappingsGetAvailableButtonMappingsListener(null);
		ARCommand.setSkyControllerButtonMappingsSetButtonMappingListener(null);
		ARCommand.setSkyControllerButtonMappingsDefaultButtonMappingListener(null);
		ARCommand.setSkyControllerButtonMappingsStateCurrentButtonMappingsListener(null);
		ARCommand.setSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener(null);
		ARCommand.setSkyControllerButtonMappingsStateAvailableButtonMappingsListener(null);
		ARCommand.setSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener(null);
		ARCommand.setSkyControllerAxisMappingsGetCurrentAxisMappingsListener(null);
		ARCommand.setSkyControllerAxisMappingsGetAvailableAxisMappingsListener(null);
		ARCommand.setSkyControllerAxisMappingsSetAxisMappingListener(null);
		ARCommand.setSkyControllerAxisMappingsDefaultAxisMappingListener(null);
		ARCommand.setSkyControllerAxisMappingsStateCurrentAxisMappingsListener(null);
		ARCommand.setSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener(null);
		ARCommand.setSkyControllerAxisMappingsStateAvailableAxisMappingsListener(null);
		ARCommand.setSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener(null);
		ARCommand.setSkyControllerAxisFiltersGetCurrentAxisFiltersListener(null);
		ARCommand.setSkyControllerAxisFiltersGetPresetAxisFiltersListener(null);
		ARCommand.setSkyControllerAxisFiltersSetAxisFilterListener(null);
		ARCommand.setSkyControllerAxisFiltersDefaultAxisFiltersListener(null);
		ARCommand.setSkyControllerAxisFiltersStateCurrentAxisFiltersListener(null);
		ARCommand.setSkyControllerAxisFiltersStateAllCurrentFiltersSentListener(null);
		ARCommand.setSkyControllerAxisFiltersStatePresetAxisFiltersListener(null);
		ARCommand.setSkyControllerAxisFiltersStateAllPresetFiltersSentListener(null);
		ARCommand.setSkyControllerCoPilotingSetPilotingSourceListener(null);
		ARCommand.setSkyControllerCoPilotingStatePilotingSourceListener(null);
		ARCommand.setSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener(null);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationStateListener(null);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener(null);
		ARCommand.setSkyControllerButtonEventsSettingsListener(null);
		ARCommand.setSkyControllerDebugDebugTest1Listener(null);
		super.unregisterARCommandsListener();
	}

//================================================================================
//================================================================================
	@Override
	public int getBattery() {
		return 0;
	}

	private ARCommandSkyControllerWifiStateWifiListListener
		mARCommandSkyControllerWifiStateWifiListListener
			= new ARCommandSkyControllerWifiStateWifiListListener() {
		@Override
		public void onSkyControllerWifiStateWifiListUpdate(
			final String bssid, final String ssid, final byte secured, final byte saved, final int rssi, final int frequency) {

			if (DEBUG) Log.v(TAG, String.format("onWifiListUpdate:bssid=%s, ssid=%s, secured=%d, saved=%d, rssi=%d, frequency=%d",
				bssid, ssid, secured, saved, rssi, frequency));
		}
	};

	private final ARCommandSkyControllerWifiStateConnexionChangedListener
		mARCommandSkyControllerWifiStateConnexionChangedListener
			= new ARCommandSkyControllerWifiStateConnexionChangedListener() {
		@Override
		public void onSkyControllerWifiStateConnexionChangedUpdate(
			final String ssid,
			final ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM status) {

			if (DEBUG) Log.v(TAG, "onConnexionChangedUpdate:ssid=" + ssid + ", status=" + status);
		}
	};

	private final ARCommandSkyControllerWifiStateWifiAuthChannelListChangedListener
		mARCommandSkyControllerWifiStateWifiAuthChannelListChangedListener
			= new ARCommandSkyControllerWifiStateWifiAuthChannelListChangedListener() {
		@Override
		public void onSkyControllerWifiStateWifiAuthChannelListChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band,
			final byte channel, final byte in_or_out) {

			if (DEBUG) Log.v(TAG, "onWifiAuthChannelListChangedUpdate:band=" + band + ", channel=" + channel + ", in_or_out=" + in_or_out);
		}
	};

	private final ARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener
		mARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener
			= new ARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener() {
		@Override
		public void onSkyControllerWifiStateAllWifiAuthChannelChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllWifiAuthChannelChangedUpdate:");
		}
	};

	private final ARCommandSkyControllerWifiStateWifiSignalChangedListener
		mARCommandSkyControllerWifiStateWifiSignalChangedListener
			= new ARCommandSkyControllerWifiStateWifiSignalChangedListener() {
		@Override
		public void onSkyControllerWifiStateWifiSignalChangedUpdate(final byte level) {

			if (DEBUG) Log.v(TAG, "onWifiSignalChangedUpdate:");
		}
	};

	private final ARCommandSkyControllerWifiRequestWifiListListener
		mARCommandSkyControllerWifiRequestWifiListListener
			= new ARCommandSkyControllerWifiRequestWifiListListener() {
		@Override
		public void onSkyControllerWifiRequestWifiListUpdate() {

			if (DEBUG) Log.v(TAG, "onWifiListUpdate:");
		}
	};

	private final ARCommandSkyControllerWifiRequestCurrentWifiListener
		mARCommandSkyControllerWifiRequestCurrentWifiListener
			= new ARCommandSkyControllerWifiRequestCurrentWifiListener() {
		@Override
		public void onSkyControllerWifiRequestCurrentWifiUpdate() {

			if (DEBUG) Log.v(TAG, "ontCurrentWifiUpdate:");
		}
	};

	private final ARCommandSkyControllerWifiConnectToWifiListener
		mARCommandSkyControllerWifiConnectToWifiListener
			= new ARCommandSkyControllerWifiConnectToWifiListener() {
		@Override
		public void onSkyControllerWifiConnectToWifiUpdate(
			final String bssid, final String ssid, final String passphrase) {

			if (DEBUG) Log.v(TAG, String.format("onConnectToWifiUpdate:bssid=%s, ssid=%s, passphrase=%s",
				bssid, ssid, passphrase));
		}
	};

	private final ARCommandSkyControllerWifiForgetWifiListener
		mARCommandSkyControllerWifiForgetWifiListener
			= new ARCommandSkyControllerWifiForgetWifiListener() {
		@Override
		public void onSkyControllerWifiForgetWifiUpdate(final String ssid) {

			if (DEBUG) Log.v(TAG, "onForgetWifiUpdate:ssid=" + ssid);
		}
	};

	private final ARCommandSkyControllerWifiWifiAuthChannelListener
		mARCommandSkyControllerWifiWifiAuthChannelListener
			= new ARCommandSkyControllerWifiWifiAuthChannelListener() {
		@Override
		public void onSkyControllerWifiWifiAuthChannelUpdate() {

			if (DEBUG) Log.v(TAG, "onWifiAuthChannelUpdate:");
		}
	};

	private final ARCommandSkyControllerDeviceRequestDeviceListListener
		mARCommandSkyControllerDeviceRequestDeviceListListener
		 = new ARCommandSkyControllerDeviceRequestDeviceListListener() {
		@Override
		public void onSkyControllerDeviceRequestDeviceListUpdate() {

			if (DEBUG) Log.v(TAG, "onDeviceListUpdate:");
		}
	};

	private final ARCommandSkyControllerDeviceRequestCurrentDeviceListener
		mARCommandSkyControllerDeviceRequestCurrentDeviceListener
			= new ARCommandSkyControllerDeviceRequestCurrentDeviceListener() {
		@Override
		public void onSkyControllerDeviceRequestCurrentDeviceUpdate() {

			if (DEBUG) Log.v(TAG, "onCurrentDeviceUpdate:");
		}
	};

	private final ARCommandSkyControllerDeviceConnectToDeviceListener
		mARCommandSkyControllerDeviceConnectToDeviceListener
			= new ARCommandSkyControllerDeviceConnectToDeviceListener() {
		@Override
		public void onSkyControllerDeviceConnectToDeviceUpdate(final String deviceName) {

			if (DEBUG) Log.v(TAG, "onConnectToDeviceUpdate:deviceName=" + deviceName);
		}
	};

	private final ARCommandSkyControllerDeviceStateDeviceListListener
		mARCommandSkyControllerDeviceStateDeviceListListener
			= new ARCommandSkyControllerDeviceStateDeviceListListener() {
		@Override
		public void onSkyControllerDeviceStateDeviceListUpdate(final String name) {

			if (DEBUG) Log.v(TAG, "onDeviceListUpdate:deviceName=" + name);
		}
	};

	private final ARCommandSkyControllerDeviceStateConnexionChangedListener
		mARCommandSkyControllerDeviceStateConnexionChangedListener
			= new ARCommandSkyControllerDeviceStateConnexionChangedListener() {
		@Override
		public void onSkyControllerDeviceStateConnexionChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status,
			final String deviceName, final short deviceProductID) {

			if (DEBUG) Log.v(TAG, "onConnexionChangedUpdate:status=" + status + ", deviceName=" + deviceName + ", deviceProductID=" + deviceProductID);
		}
	};

	private final ARCommandSkyControllerSettingsAllSettingsListener
		mARCommandSkyControllerSettingsAllSettingsListener
			= new ARCommandSkyControllerSettingsAllSettingsListener() {
		@Override
		public void onSkyControllerSettingsAllSettingsUpdate() {

			if (DEBUG) Log.v(TAG, "onAllSettingsUpdate:");
		}
	};

	private final ARCommandSkyControllerSettingsResetListener
		mARCommandSkyControllerSettingsResetListener
			= new ARCommandSkyControllerSettingsResetListener() {
		@Override
		public void onSkyControllerSettingsResetUpdate() {

			if (DEBUG) Log.v(TAG, "onResetUpdate:");
		}
	};

	private final ARCommandSkyControllerSettingsStateAllSettingsChangedListener
		mARCommandSkyControllerSettingsStateAllSettingsChangedListener
			= new ARCommandSkyControllerSettingsStateAllSettingsChangedListener() {
		@Override
		public void onSkyControllerSettingsStateAllSettingsChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllSettingsChangedUpdate:");
		}
	};

	private final ARCommandSkyControllerSettingsStateResetChangedListener
		mARCommandSkyControllerSettingsStateResetChangedListener
			= new ARCommandSkyControllerSettingsStateResetChangedListener() {
		@Override
		public void onSkyControllerSettingsStateResetChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onResetChangedUpdate:");
		}
	};

	private final ARCommandSkyControllerSettingsStateProductSerialChangedListener
		mARCommandSkyControllerSettingsStateProductSerialChangedListener
			= new ARCommandSkyControllerSettingsStateProductSerialChangedListener() {
		@Override
		public void onSkyControllerSettingsStateProductSerialChangedUpdate(final String serialNumber) {

			if (DEBUG) Log.v(TAG, "onProductSerialChangedUpdate:serialNumber=" + serialNumber);
		}
	};

	private final ARCommandSkyControllerSettingsStateProductVariantChangedListener
		mARCommandSkyControllerSettingsStateProductVariantChangedListener
			= new ARCommandSkyControllerSettingsStateProductVariantChangedListener() {
		@Override
		public void onSkyControllerSettingsStateProductVariantChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM variant) {

			if (DEBUG) Log.v(TAG, "onProductVariantChangedUpdate:variant=" + variant);
		}
	};

	private final ARCommandSkyControllerCommonAllStatesListener
		mARCommandSkyControllerCommonAllStatesListener
			= new ARCommandSkyControllerCommonAllStatesListener() {
		@Override
		public void onSkyControllerCommonAllStatesUpdate() {

			if (DEBUG) Log.v(TAG, "onAllStatesUpdate:");
		}
	};

	private final ARCommandSkyControllerCommonStateAllStatesChangedListener
		mARCommandSkyControllerCommonStateAllStatesChangedListener
			= new ARCommandSkyControllerCommonStateAllStatesChangedListener() {
		@Override
		public void onSkyControllerCommonStateAllStatesChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllStatesChangedUpdate:");
		}
	};

	/** スカイコントローラーのバッテリー残量が変化した時のコールバックリスナー */
	private final ARCommandSkyControllerSkyControllerStateBatteryChangedListener
		mARCommandSkyControllerSkyControllerStateBatteryChangedListener
			= new ARCommandSkyControllerSkyControllerStateBatteryChangedListener() {
		@Override
		public void onSkyControllerSkyControllerStateBatteryChangedUpdate(final byte percent) {

			if (DEBUG) Log.v(TAG, "onBatteryChangedUpdate:percent=" + percent);
		}
	};

	/** スカイコントローラーのGPSによる位置確定状態が変化した時のコールバックリスナー */
	private final ARCommandSkyControllerSkyControllerStateGpsFixChangedListener
		mARCommandSkyControllerSkyControllerStateGpsFixChangedListener
			= new ARCommandSkyControllerSkyControllerStateGpsFixChangedListener() {
		@Override
		public void onSkyControllerSkyControllerStateGpsFixChangedUpdate(final byte fixed) {

			if (DEBUG) Log.v(TAG, "onGpsFixChangedUpdate:fixed=" + fixed);
		}
	};

	private final ARCommandSkyControllerSkyControllerStateGpsPositionChangedListener
		mARCommandSkyControllerSkyControllerStateGpsPositionChangedListener
			= new ARCommandSkyControllerSkyControllerStateGpsPositionChangedListener() {
		@Override
		public void onSkyControllerSkyControllerStateGpsPositionChangedUpdate(
			final double latitude, final double longitude, final double altitude, final float heading) {

			if (DEBUG) Log.v (TAG, String.format("onGpsPositionChangedUpdate:latitude=%f, longitude=%f, altitude=%f, heading=%f",
				latitude, longitude, altitude, heading));
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener
		mARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener
			= new ARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener() {
		@Override
		public void onSkyControllerAccessPointSettingsAccessPointSSIDUpdate(final String ssid) {

			if (DEBUG) Log.v(TAG, "onAccessPointSSIDUpdate:ssid=" + ssid);
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsAccessPointChannelListener
		mARCommandSkyControllerAccessPointSettingsAccessPointChannelListener
			= new ARCommandSkyControllerAccessPointSettingsAccessPointChannelListener() {
		@Override
		public void onSkyControllerAccessPointSettingsAccessPointChannelUpdate(final byte channel) {

			if (DEBUG) Log.v(TAG, "onAccessPointChannelUpdate:channel=" + channel);
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsWifiSelectionListener
		mARCommandSkyControllerAccessPointSettingsWifiSelectionListener
			= new ARCommandSkyControllerAccessPointSettingsWifiSelectionListener() {
		@Override
		public void onSkyControllerAccessPointSettingsWifiSelectionUpdate(
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_TYPE_ENUM type,
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_BAND_ENUM band,
			final byte channel) {

			if (DEBUG) Log.v(TAG, "onWifiSelectionUpdate:type=" + type + ", band=" + band + ", channel=" + channel);
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener
		mARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener() {
		@Override
		public void onSkyControllerAccessPointSettingsStateAccessPointSSIDChangedUpdate(
			final String ssid) {

			if (DEBUG) Log.v(TAG, "onAccessPointSSIDChangedUpdate:ssid=" + ssid);
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener
		mARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener() {
		@Override
		public void onSkyControllerAccessPointSettingsStateAccessPointChannelChangedUpdate(
			final byte channel) {

			if (DEBUG) Log.v(TAG, "onAccessPointChannelChangedUpdate:channel=" + channel);
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener
		mARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener() {
		@Override
		public void onSkyControllerAccessPointSettingsStateWifiSelectionChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type,
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band,
			final byte channel) {

			if (DEBUG) Log.v(TAG, "onWifiSelectionChangedUpdate:type=" + type + ", band=" + band + ", channel=" + channel);
		}
	};

	private final ARCommandSkyControllerCameraResetOrientationListener
		mARCommandSkyControllerCameraResetOrientationListener
			= new ARCommandSkyControllerCameraResetOrientationListener() {
		@Override
		public void onSkyControllerCameraResetOrientationUpdate() {

			if (DEBUG) Log.v(TAG, "onCameraResetOrientationUpdate:");
		}
	};

	private final ARCommandSkyControllerGamepadInfosGetGamepadControlsListener
		mARCommandSkyControllerGamepadInfosGetGamepadControlsListener
			= new ARCommandSkyControllerGamepadInfosGetGamepadControlsListener() {
		@Override
		public void onSkyControllerGamepadInfosGetGamepadControlsUpdate() {

			if (DEBUG) Log.v(TAG, "onGetGamepadControlsUpdate:");
		}
	};

	private final ARCommandSkyControllerGamepadInfosStateGamepadControlListener
		mARCommandSkyControllerGamepadInfosStateGamepadControlListener
			= new ARCommandSkyControllerGamepadInfosStateGamepadControlListener() {
		@Override
		public void onSkyControllerGamepadInfosStateGamepadControlUpdate(
			final ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM type,
			final int id, final String name) {

			if (DEBUG) Log.v(TAG, "onStateGamepadControlUpdate:type=" + type + ", id=" + id + ", name=" + name);
		}
	};

	private final ARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener
		mARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener
			= new ARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener() {
		@Override
		public void onSkyControllerGamepadInfosStateAllGamepadControlsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onStateAllGamepadControlsSentUpdate:");
		}
	};

	private final ARCommandSkyControllerButtonMappingsGetCurrentButtonMappingsListener
		mARCommandSkyControllerButtonMappingsGetCurrentButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsGetCurrentButtonMappingsListener() {
		@Override
		public void onSkyControllerButtonMappingsGetCurrentButtonMappingsUpdate() {

			if (DEBUG) Log.v(TAG, "onGetCurrentButtonMappingsUpdate:");
		}
	};


	private final ARCommandSkyControllerButtonMappingsGetAvailableButtonMappingsListener
		mARCommandSkyControllerButtonMappingsGetAvailableButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsGetAvailableButtonMappingsListener() {
		@Override
		public void onSkyControllerButtonMappingsGetAvailableButtonMappingsUpdate() {

			if (DEBUG) Log.v(TAG, "onGetAvailableButtonMappingsUpdate:");
		}
	};

	private final ARCommandSkyControllerButtonMappingsSetButtonMappingListener
		mARCommandSkyControllerButtonMappingsSetButtonMappingListener
			= new ARCommandSkyControllerButtonMappingsSetButtonMappingListener() {
		@Override
		public void onSkyControllerButtonMappingsSetButtonMappingUpdate(
			final int key_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onSetButtonMappingUpdate:key_id=" + key_id + ", mapping_uid=" + mapping_uid);
		}
	};

	private final ARCommandSkyControllerButtonMappingsDefaultButtonMappingListener
		mARCommandSkyControllerButtonMappingsDefaultButtonMappingListener
			= new ARCommandSkyControllerButtonMappingsDefaultButtonMappingListener() {
		@Override
		public void onSkyControllerButtonMappingsDefaultButtonMappingUpdate() {

			if (DEBUG) Log.v(TAG, "onDefaultButtonMappingUpdate:");
		}
	};

	private final ARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener
		mARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener() {
		@Override
		public void onSkyControllerButtonMappingsStateCurrentButtonMappingsUpdate(
			final int key_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onCurrentButtonMappingsUpdate:key_id=" + key_id + ", mapping_uid=" + mapping_uid);
		}
	};

	private final ARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener
		mARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener
			= new ARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener() {
		@Override
		public void onSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentButtonMappingsSentUpdate:");
		}
	};

	private final ARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener
		mARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener() {
		@Override
		public void onSkyControllerButtonMappingsStateAvailableButtonMappingsUpdate(
			final String mapping_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onAvailableButtonMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
		}
	};

	private final ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener
		mARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener
			= new ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener() {
		@Override
		public void onSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllAvailableButtonsMappingsSentUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisMappingsGetCurrentAxisMappingsListener
		mARCommandSkyControllerAxisMappingsGetCurrentAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsGetCurrentAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsGetCurrentAxisMappingsUpdate() {

			if (DEBUG) Log.v(TAG, "onGetCurrentAxisMappingsUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisMappingsGetAvailableAxisMappingsListener
		mARCommandSkyControllerAxisMappingsGetAvailableAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsGetAvailableAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsGetAvailableAxisMappingsUpdate() {

			if (DEBUG) Log.v(TAG, "onGetAvailableAxisMappingsUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisMappingsSetAxisMappingListener
		mARCommandSkyControllerAxisMappingsSetAxisMappingListener
			= new ARCommandSkyControllerAxisMappingsSetAxisMappingListener() {
		@Override
		public void onSkyControllerAxisMappingsSetAxisMappingUpdate(
			final int axis_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onSetAxisMappingUpdate:axis_id=" + axis_id + ", mapping_uid=" + mapping_uid);
		}
	};

	private final ARCommandSkyControllerAxisMappingsDefaultAxisMappingListener
		mARCommandSkyControllerAxisMappingsDefaultAxisMappingListener
			= new ARCommandSkyControllerAxisMappingsDefaultAxisMappingListener() {
		@Override
		public void onSkyControllerAxisMappingsDefaultAxisMappingUpdate() {

			if (DEBUG) Log.v(TAG, "onDefaultAxisMappingUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener
		mARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsStateCurrentAxisMappingsUpdate(
			final int axis_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onCurrentAxisMappingsUpdate:axis_id=" + axis_id + ", mapping_uid=" + mapping_uid);
		}
	};

	private final ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener
		mARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener
			= new ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentAxisMappingsSentUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener
		mARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAvailableAxisMappingsUpdate(
			final String mapping_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onAvailableAxisMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
		}
	};

	private final ARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener
		mARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener
			= new ARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllAvailableAxisMappingsSentUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisFiltersGetCurrentAxisFiltersListener
		mARCommandSkyControllerAxisFiltersGetCurrentAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersGetCurrentAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersGetCurrentAxisFiltersUpdate() {

			if (DEBUG) Log.v(TAG, "onGetCurrentAxisFiltersUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisFiltersGetPresetAxisFiltersListener
		mARCommandSkyControllerAxisFiltersGetPresetAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersGetPresetAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersGetPresetAxisFiltersUpdate() {

			if (DEBUG) Log.v(TAG, "onGetPresetAxisFiltersUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisFiltersSetAxisFilterListener
		mARCommandSkyControllerAxisFiltersSetAxisFilterListener
			= new ARCommandSkyControllerAxisFiltersSetAxisFilterListener() {
		@Override
		public void onSkyControllerAxisFiltersSetAxisFilterUpdate(
			final int axis_id, final String filter_uid_or_builder) {

			if (DEBUG) Log.v(TAG, "onSetAxisFilterUpdate:axis_id=" + axis_id + ", filter_uid_or_builder=" + filter_uid_or_builder);
		}
	};

	private final ARCommandSkyControllerAxisFiltersDefaultAxisFiltersListener
		mARCommandSkyControllerAxisFiltersDefaultAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersDefaultAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersDefaultAxisFiltersUpdate() {

			if (DEBUG) Log.v(TAG, "onDefaultAxisFiltersUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener
		mARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersStateCurrentAxisFiltersUpdate(
			final int axis_id, final String filter_uid_or_builder) {

			if (DEBUG) Log.v(TAG, "onCurrentAxisFiltersUpdate:axis_id=" + axis_id + ", filter_uid_or_builder=" + filter_uid_or_builder);
		}
	};

	private final ARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener
		mARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener
			= new ARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener() {
		@Override
		public void onSkyControllerAxisFiltersStateAllCurrentFiltersSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentFiltersSentUpdate:");
		}
	};

	private final ARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener
		mARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersStatePresetAxisFiltersUpdate(
			final String filter_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onPresetAxisFiltersUpdate:filter_uid=" + filter_uid + ", name=" + name);
		}
	};

	private final ARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener
		mARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener
			= new ARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener() {
		@Override
		public void onSkyControllerAxisFiltersStateAllPresetFiltersSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllPresetFiltersSentUpdate:");
		}
	};

	private final ARCommandSkyControllerCoPilotingSetPilotingSourceListener
		mARCommandSkyControllerCoPilotingSetPilotingSourceListener
			= new ARCommandSkyControllerCoPilotingSetPilotingSourceListener() {
		@Override
		public void onSkyControllerCoPilotingSetPilotingSourceUpdate(
			final ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM source) {

			if (DEBUG) Log.v(TAG, "onSetPilotingSourceUpdate:source=" + source);
		}
	};

	private final ARCommandSkyControllerCoPilotingStatePilotingSourceListener
		mARCommandSkyControllerCoPilotingStatePilotingSourceListener
			= new ARCommandSkyControllerCoPilotingStatePilotingSourceListener() {
		@Override
		public void onSkyControllerCoPilotingStatePilotingSourceUpdate(
			final ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM source) {

			if (DEBUG) Log.v(TAG, "onPilotingSourceUpdate:source=" + source);
		}
	};

	private final ARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener
		mARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener
			= new ARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener()	{
		@Override
		public void onSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesUpdate(final byte enable) {

			if (DEBUG) Log.v(TAG, "onCalibrationQualityUpdatesUpdate:enable=" + enable);
		}
	};

	private final ARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener
		mARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener
			= new ARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener() {
		@Override
		public void onSkyControllerCalibrationStateMagnetoCalibrationStateUpdate(
			final ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM status,
				final byte X_Quality, final byte Y_Quality, final byte Z_Quality) {

			if (DEBUG) Log.v(TAG, "onCalibrationStateUpdate:status=" + status
				+ ", X_Quality=" + X_Quality + ", Y_Quality=" + Y_Quality + ", Z_Quality=" + Z_Quality);
		}
	};

	private final ARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener
		mARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener
			= new ARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener() {
		@Override
		public void onSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateUpdate(
			final byte enabled) {

			if (DEBUG) Log.v(TAG, "onCalibrationQualityUpdatesStateUpdate:enabled=" + enabled);
		}
	};

	private final ARCommandSkyControllerButtonEventsSettingsListener
		mARCommandSkyControllerButtonEventsSettingsListener
			= new ARCommandSkyControllerButtonEventsSettingsListener() {
		@Override
		public void onSkyControllerButtonEventsSettingsUpdate() {

			if (DEBUG) Log.v(TAG, "onButtonEventsSettingsUpdate:");
		}
	};

	private final ARCommandSkyControllerDebugDebugTest1Listener
		mARCommandSkyControllerDebugDebugTest1Listener
			= new ARCommandSkyControllerDebugDebugTest1Listener() {
		@Override
		public void onSkyControllerDebugDebugTest1Update(final byte t1Args) {

			if (DEBUG) Log.v(TAG, "onDebugDebugTest1Update:t1Args=" + t1Args);
		}
	};
}
