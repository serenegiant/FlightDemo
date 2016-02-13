package com.serenegiant.arflight;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
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
import com.parrot.arsdk.arcommands.ARCommandSkyControllerCoPilotingStatePilotingSourceListener;
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
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.arflight.configs.ARNetworkConfigSkyController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.serenegiant.arflight.ARFlightConst.*;

public class SkyController extends DeviceController implements IBridgeController, IWiFiController {
	private static final boolean DEBUG = true;				// FIXME 実働時はfalseにすること
	private static final String TAG = SkyController.class.getSimpleName();

	private final List<SkyControllerListener> mListeners = new ArrayList<SkyControllerListener>();

	/** 接続中の機体情報, FIXME 排他制御が必要 */
	private DeviceInfo mConnectDevice;
	private VideoStreamDelegater mVideoStreamDelegater;

	public SkyController(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigSkyController());
		mInfo = new AttributeDevice();
		mStatus = new CommonStatus();
	}

	@Override
	public ARNetworkConfig getNetConfig() {
		return mNetConfig;
	}

	@Override
	public ARNetworkALManager getALManager() {
		return mARManager;
	}

	@Override
	public ARNetworkManager getNetManager() {
		return mARNetManager;
	}

	@Override
	protected void internal_stop() {
		mVideoStreamDelegater = null;
		super.internal_stop();
	}

	@Override
	protected void internal_start() {
		mVideoStreamDelegater = new VideoStreamDelegater(this);
		super.internal_start();
	}

	@Override
	public boolean connectTo(final DeviceInfo info) {
		return connectToDevice(info.name());
	}

	@Override
	public void disconnectFrom() {
	}

	@Override
	public boolean isConnected() {
		return super.isConnected() && (mConnectDevice != null);
	}

	@Override
	public DeviceInfo connectDeviceInfo() {
		return mConnectDevice;
	}

	@Override
	public VideoStreamDelegater getVideoStreamDelegater() {
		return mVideoStreamDelegater;
	}

//================================================================================
// コールバック関係
//================================================================================
	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	@Override
	public void addListener(final DeviceConnectionListener listener) {
		super.addListener(listener);
		if (listener instanceof SkyControllerListener) {
			synchronized (mListeners) {
				mListeners.add((SkyControllerListener)listener);
			}
		}
	}

	/**
	 * 指定したコールバックリスナーを取り除く
	 * @param listener
	 */
	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		if (listener instanceof SkyControllerListener) {
			synchronized (mListeners) {
				mListeners.remove((SkyControllerListener)listener);
			}
		}
		super.removeListener(listener);
	}

// FIXME スカイコントローラー専用のコールバックは未実装

//********************************************************************************
// ARSDK3からのコールバックリスナー関係
//********************************************************************************
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
		ARCommand.setSkyControllerCoPilotingStatePilotingSourceListener(mARCommandSkyControllerCoPilotingStatePilotingSourceListener);
		ARCommand.setSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener(mARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationStateListener(mARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener(mARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener);
		ARCommand.setSkyControllerButtonEventsSettingsListener(mARCommandSkyControllerButtonEventsSettingsListener);
		ARCommand.setSkyControllerDebugDebugTest1Listener(mARCommandSkyControllerDebugDebugTest1Listener);
		// スカイコントローラー自体のアプリで使用するコールバックリスナーみたい
//		ARCommand.setSkyControllerCoPilotingSetPilotingSourceListener(mARCommandSkyControllerCoPilotingSetPilotingSourceListener);
//		ARCommand.setSkyControllerCommonAllStatesListener(mARCommandSkyControllerCommonAllStatesListener);
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
		ARCommand.setSkyControllerCoPilotingStatePilotingSourceListener(null);
		ARCommand.setSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener(null);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationStateListener(null);
		ARCommand.setSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener(null);
		ARCommand.setSkyControllerButtonEventsSettingsListener(null);
		ARCommand.setSkyControllerDebugDebugTest1Listener(null);
		// スカイコントローラー自体のアプリで使用するコールバックリスナーみたい
//		ARCommand.setSkyControllerCoPilotingSetPilotingSourceListener(null);
//		ARCommand.setSkyControllerCommonAllStatesListener(null);
		super.unregisterARCommandsListener();
	}

//================================================================================
//================================================================================
	/**
	 * スカイコントローラーが検出したアクセスポイント一覧を取得した時のコールバックリスナー
	 * 検出しているアクセスポイント1つ毎に1回呼び出される
	 * requestWifiListに対する応答, 自動的には来ない
	 * */
	private ARCommandSkyControllerWifiStateWifiListListener
		mARCommandSkyControllerWifiStateWifiListListener
			= new ARCommandSkyControllerWifiStateWifiListListener() {
		/**
		 * @param bssid
		 * @param ssid
		 * @param secured
		 * @param saved
		 * @param rssi
		 * @param frequency
		 */
		@Override
		public void onSkyControllerWifiStateWifiListUpdate(
			final String bssid, final String ssid, final byte secured, final byte saved, final int rssi, final int frequency) {

			if (DEBUG) Log.v(TAG, String.format("onWifiListUpdate:bssid=%s, ssid=%s, secured=%d, saved=%d, rssi=%d, frequency=%d",
				bssid, ssid, secured, saved, rssi, frequency));
		}
	};

	/**
	 * スカイコントローラーとWiFiアクセスポイント間の接続状態が変化した時のコールバックリスナー
	 * requestAllStatesでも来る
	 * requestCurrentWiFiを呼んでも来る
	 * 何故か1回の接続で3回来るのと切断された時には来ないみたい
	 * XXX これは普通は使わんで良さそう
	 */
	private final ARCommandSkyControllerWifiStateConnexionChangedListener
		mARCommandSkyControllerWifiStateConnexionChangedListener
			= new ARCommandSkyControllerWifiStateConnexionChangedListener() {
		/**
		 * @param ssid 通常は機体名と一緒, Bebop2-K007717とか, ssidを変更できるのかどうかは未確認
		 * @param status 0:Connected, 1:Error, 2:Disconnected
		 */
		@Override
		public void onSkyControllerWifiStateConnexionChangedUpdate(
			final String ssid,
			final ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM status) {

			if (DEBUG) Log.v(TAG, "onWiFiConnexionChangedUpdate:ssid=" + ssid + ", status=" + status);
			switch (status) {
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_CONNECTED:		// 0
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ERROR:			// 1
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_DISCONNECTED:	// 2
				break;
			}
		}
	};

	/**
	 * スカイコントローラーと機体の接続状態が変化した時のコールバックリスナー
	 * requestAllStatesでも来る
	 * requestCurrentDeviceを呼ぶと来る
	 * requestCurrentWiFiを呼んでも来る
	 * AccessPointのSSIDやチャネル等を変更しても来る
	 * たぶん最初に見つかった機体には勝手に接続しに行きよる
	 * ARCommandSkyControllerWifiStateConnexionChangedListenerのコールバックメソッドよりも後に来る
	 * TODO 複数の機体を同時に検出した時はどうなるんやろ? currentDeviceの接続状態なんかな?
	 */
	private final ARCommandSkyControllerDeviceStateConnexionChangedListener
		mARCommandSkyControllerDeviceStateConnexionChangedListener
			= new ARCommandSkyControllerDeviceStateConnexionChangedListener() {
		/**
		 * @param status 機体との接続状態
		 * @param deviceName 機体名, Bebop2-K007717とか, 接続してなければ空文字列
		 * @param deviceProductID 接続している機体のプロダクトID, 接続していなければ0
		 */
		@Override
		public void onSkyControllerDeviceStateConnexionChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status,
			final String deviceName, final short deviceProductID) {

			if (DEBUG) Log.v(TAG, "onDeviceConnexionChangedUpdate:status=" + status + ", deviceName=" + deviceName + ", deviceProductID=" + deviceProductID);
			updateConnectionState(status, deviceName, deviceProductID);
		}
	};

	private final Map<String, DeviceInfo> mDevices = new HashMap<String, DeviceInfo>();

	private void updateConnectionState(
		final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status,
		final String deviceName, final short deviceProductID) {

		if (DEBUG) Log.v(TAG, "updateConnectionState:");
		DeviceInfo[] info_array = null;
		synchronized (mDevices) {
			DeviceInfo info = mDevices.containsKey(deviceName) ? mDevices.get(deviceName) : null;
			switch (status) {
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_NOTCONNECTED:        // 0
				removeDevice(deviceName);
				break;
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTING:		// 1
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTED:		// 2
				if (info == null) {
					if (DEBUG) Log.v(TAG, "found new device:name=" + deviceName);
					info = new DeviceInfo(deviceName, deviceProductID);
					mDevices.put(deviceName, info);
				}
				info.connectionState(status.getValue());
				mConnectDevice = info;
				break;
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_DISCONNECTING:    // 3
				removeDevice(deviceName);
				break;
			}
			if (mDevices.size() > 0) {
				info_array = new DeviceInfo[mDevices.size()];
				int i = 0;
				for (final DeviceInfo inf: mDevices.values()) {
					info_array[i] = inf;
				}
			}
		}
		if (mLocalBroadcastManager != null) {
			final Intent intent = new Intent(ARFLIGHT_ACTION_DEVICE_LIST_CHANGED);
			if (info_array != null) {
				intent.putExtra(ARFLIGHT_EXTRA_DEVICE_LIST, info_array);
			}
			mLocalBroadcastManager.sendBroadcast(intent);
		}
	}

	private void removeDevice(final String deviceName) {
		mDevices.remove(deviceName);
		if ((mConnectDevice != null) && mConnectDevice.name().equals(deviceName)) {
			mConnectDevice = null;
		}
	}

//--------------------------------------------------------------------------------
	/**
	 * WiFi一覧?
	 * 自動では来ない
	 */
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

	/**
	 * WiFi一覧?の終端
	 */
	private final ARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener
		mARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener
			= new ARCommandSkyControllerWifiStateAllWifiAuthChannelChangedListener() {
		@Override
		public void onSkyControllerWifiStateAllWifiAuthChannelChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllWifiAuthChannelChangedUpdate:");
		}
	};

//--------------------------------------------------------------------------------
	/**
	 * WiFiの信号強度が変化した時のコールバックリスナー
	 * requestAllStatesを呼ぶと来る
	 * FIXME ...ってどの信号強度なんだろ?
	 * FIXME スカイコントローラーが受信しているタブレット/スマホからの電波の信号強度なんかな?
	 * FIXME 機体と接続した時に送られてくるので機体からスカイコントローラーに届く信号強度かも
	 */
	private final ARCommandSkyControllerWifiStateWifiSignalChangedListener
		mARCommandSkyControllerWifiStateWifiSignalChangedListener
			= new ARCommandSkyControllerWifiStateWifiSignalChangedListener() {
		@Override
		public void onSkyControllerWifiStateWifiSignalChangedUpdate(final byte level) {

			if (DEBUG) Log.v(TAG, "onWifiSignalChangedUpdate:level=" + level);
		}
	};

	private final ARCommandSkyControllerWifiRequestWifiListListener
		mARCommandSkyControllerWifiRequestWifiListListener
			= new ARCommandSkyControllerWifiRequestWifiListListener() {
		@Override
		public void onSkyControllerWifiRequestWifiListUpdate() {

			if (DEBUG) Log.e(TAG, "onWifiListUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerWifiRequestCurrentWifiListener
		mARCommandSkyControllerWifiRequestCurrentWifiListener
			= new ARCommandSkyControllerWifiRequestCurrentWifiListener() {
		@Override
		public void onSkyControllerWifiRequestCurrentWifiUpdate() {

			if (DEBUG) Log.e(TAG, "ontCurrentWifiUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
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

			if (DEBUG) Log.e(TAG, "onForgetWifiUpdate:ssid=" + ssid);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerWifiWifiAuthChannelListener
		mARCommandSkyControllerWifiWifiAuthChannelListener
			= new ARCommandSkyControllerWifiWifiAuthChannelListener() {
		@Override
		public void onSkyControllerWifiWifiAuthChannelUpdate() {

			if (DEBUG) Log.e(TAG, "onWifiAuthChannelUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerDeviceRequestDeviceListListener
		mARCommandSkyControllerDeviceRequestDeviceListListener
		 = new ARCommandSkyControllerDeviceRequestDeviceListListener() {
		@Override
		public void onSkyControllerDeviceRequestDeviceListUpdate() {

			if (DEBUG) Log.e(TAG, "onDeviceListUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerDeviceRequestCurrentDeviceListener
		mARCommandSkyControllerDeviceRequestCurrentDeviceListener
			= new ARCommandSkyControllerDeviceRequestCurrentDeviceListener() {
		@Override
		public void onSkyControllerDeviceRequestCurrentDeviceUpdate() {

			if (DEBUG) Log.e(TAG, "onCurrentDeviceUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerDeviceConnectToDeviceListener
		mARCommandSkyControllerDeviceConnectToDeviceListener
			= new ARCommandSkyControllerDeviceConnectToDeviceListener() {
		@Override
		public void onSkyControllerDeviceConnectToDeviceUpdate(final String deviceName) {

			if (DEBUG) Log.e(TAG, "onConnectToDeviceUpdate:deviceName=" + deviceName);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerDeviceStateDeviceListListener
		mARCommandSkyControllerDeviceStateDeviceListListener
			= new ARCommandSkyControllerDeviceStateDeviceListListener() {
		@Override
		public void onSkyControllerDeviceStateDeviceListUpdate(final String name) {

			if (DEBUG) Log.e(TAG, "onDeviceListUpdate:deviceName=" + name);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerSettingsAllSettingsListener
		mARCommandSkyControllerSettingsAllSettingsListener
			= new ARCommandSkyControllerSettingsAllSettingsListener() {
		@Override
		public void onSkyControllerSettingsAllSettingsUpdate() {

			if (DEBUG) Log.e(TAG, "onAllSettingsUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerSettingsResetListener
		mARCommandSkyControllerSettingsResetListener
			= new ARCommandSkyControllerSettingsResetListener() {
		@Override
		public void onSkyControllerSettingsResetUpdate() {

			if (DEBUG) Log.e(TAG, "onResetUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	/**
	 * スカイコントローラーからすべての設定を受信した時のコールバックリスナー
	 * requestAllSettingsを呼んだ時に設定が全て送られてきた最後に呼ばれる
	 */
	private final ARCommandSkyControllerSettingsStateAllSettingsChangedListener
		mARCommandSkyControllerSettingsStateAllSettingsChangedListener
			= new ARCommandSkyControllerSettingsStateAllSettingsChangedListener() {
		@Override
		public void onSkyControllerSettingsStateAllSettingsChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllSettingsChangedUpdate:");
			if (isWaitingAllSettings) {
				cmdGetAllSettingsSent.release();
			}
		}
	};

	private final ARCommandSkyControllerSettingsStateResetChangedListener
		mARCommandSkyControllerSettingsStateResetChangedListener
			= new ARCommandSkyControllerSettingsStateResetChangedListener() {
		@Override
		public void onSkyControllerSettingsStateResetChangedUpdate() {

			if (DEBUG) Log.e(TAG, "onResetChangedUpdate:");
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	/**
	 * スカイコントローラーのシリアル番号受信時のコールバックリスナー
	 * requestAllStatesを呼んだら来る
	 */
	private final ARCommandSkyControllerSettingsStateProductSerialChangedListener
		mARCommandSkyControllerSettingsStateProductSerialChangedListener
			= new ARCommandSkyControllerSettingsStateProductSerialChangedListener() {
		/**
		 * @param serialNumber
		 */
		@Override
		public void onSkyControllerSettingsStateProductSerialChangedUpdate(final String serialNumber) {

//			if (DEBUG) Log.v(TAG, "onProductSerialChangedUpdate:serialNumber=" + serialNumber);
			mInfo.setSerialHigh(serialNumber);
		}
	};

	/**
	 * スカイコントローラーの種類情報受信時のコールバックリスナー
	 * 	 * requestAllStatesを呼んだら来る
	 */
	private final ARCommandSkyControllerSettingsStateProductVariantChangedListener
		mARCommandSkyControllerSettingsStateProductVariantChangedListener
			= new ARCommandSkyControllerSettingsStateProductVariantChangedListener() {
		/**
		 * @param variant Bebop世代かBebop2世代か
		 */
		@Override
		public void onSkyControllerSettingsStateProductVariantChangedUpdate(
			final ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM variant) {

//			if (DEBUG) Log.v(TAG, "onProductVariantChangedUpdate:variant=" + variant);
			mInfo.setSerialLow(":" + variant.toString());

		}
	};

//	/** これはスカイコントローラー自体のアプリ用コールバックリスナーみたい */
//	private final ARCommandSkyControllerCommonAllStatesListener
//		mARCommandSkyControllerCommonAllStatesListener
//			= new ARCommandSkyControllerCommonAllStatesListener() {
//		@Override
//		public void onSkyControllerCommonAllStatesUpdate() {
//
//			if (DEBUG) Log.v(TAG, "onSkyControllerCommonAllStatesUpdate:");
//		}
//	};

	/**
	 * スカイコントローラーから全てのステータスを受信した時のコールバックリスナー
	 * requestAllStatesを呼んで全てのステータスを受信した最後に呼び出される
	 */
	private final ARCommandSkyControllerCommonStateAllStatesChangedListener
		mARCommandSkyControllerCommonStateAllStatesChangedListener
			= new ARCommandSkyControllerCommonStateAllStatesChangedListener() {
		@Override
		public void onSkyControllerCommonStateAllStatesChangedUpdate() {

			if (DEBUG) Log.v(TAG, "onAllStatesChangedUpdate:");
			if (isWaitingAllStates) {
				cmdGetAllStatesSent.release();
			}
		}
	};

	/**
	 * スカイコントローラーのバッテリー残量が変化した時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerSkyControllerStateBatteryChangedListener
		mARCommandSkyControllerSkyControllerStateBatteryChangedListener
			= new ARCommandSkyControllerSkyControllerStateBatteryChangedListener() {
		/**
		 * @param percent バッテリー残量[%]
		 */
		@Override
		public void onSkyControllerSkyControllerStateBatteryChangedUpdate(final byte percent) {

			if (DEBUG) Log.v(TAG, "onBatteryChangedUpdate:percent=" + percent);
			mStatus.setBattery(percent);
		}
	};

	/**
	 * スカイコントローラーのGPSによる位置取得状態が変化した時のコールバックリスナー
	 * FIXME スカイコントローラー自体のGPSの状態なのか接続している機体のGPSの状態なのかは不明
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerSkyControllerStateGpsFixChangedListener
		mARCommandSkyControllerSkyControllerStateGpsFixChangedListener
			= new ARCommandSkyControllerSkyControllerStateGpsFixChangedListener() {
		/**
		 * @param fixed 1: GPSによって位置を確定できた, 0:できなかった
		 */
		@Override
		public void onSkyControllerSkyControllerStateGpsFixChangedUpdate(final byte fixed) {

			if (DEBUG) Log.v(TAG, "onGpsFixChangedUpdate:fixed=" + fixed);
		}
	};

	/**
	 * GPSによる位置情報が変化した時のコールバックリスナー
	 * 機体と接続しないと来ないみたい。もしかするとGPSによる機体位置なのかも
	 */
	private final ARCommandSkyControllerSkyControllerStateGpsPositionChangedListener
		mARCommandSkyControllerSkyControllerStateGpsPositionChangedListener
			= new ARCommandSkyControllerSkyControllerStateGpsPositionChangedListener() {
		@Override
		public void onSkyControllerSkyControllerStateGpsPositionChangedUpdate(
			final double latitude, final double longitude, final double altitude, final float heading) {
//			FIXME 未実装 でも頻繁に来るのでコメントアウト
//			if (DEBUG) Log.v (TAG, String.format("onGpsPositionChangedUpdate:latitude=%f, longitude=%f, altitude=%f, heading=%f",
//				latitude, longitude, altitude, heading));
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener
		mARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener
			= new ARCommandSkyControllerAccessPointSettingsAccessPointSSIDListener() {
		@Override
		public void onSkyControllerAccessPointSettingsAccessPointSSIDUpdate(final String ssid) {

			if (DEBUG) Log.e(TAG, "onAccessPointSSIDUpdate:ssid=" + ssid);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	private final ARCommandSkyControllerAccessPointSettingsAccessPointChannelListener
		mARCommandSkyControllerAccessPointSettingsAccessPointChannelListener
			= new ARCommandSkyControllerAccessPointSettingsAccessPointChannelListener() {
		@Override
		public void onSkyControllerAccessPointSettingsAccessPointChannelUpdate(final byte channel) {

			if (DEBUG) Log.e(TAG, "onAccessPointChannelUpdate:channel=" + channel);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
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
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
		}
	};

	/**
	 * スカイコントローラーのSSID変更通知
	 * requestAllSettingsを呼んでも来る
	 * setSkyControllerSSIDでセットしても呼ばれそう
	 */
	private final ARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener
		mARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateAccessPointSSIDChangedListener() {
		/**
		 * @param ssid スカイコントローラーのSSID
		 */
		@Override
		public void onSkyControllerAccessPointSettingsStateAccessPointSSIDChangedUpdate(
			final String ssid) {

			if (DEBUG) Log.v(TAG, "onAccessPointSSIDChangedUpdate:ssid=" + ssid);
		}
	};

	/**
	 * スカイコントローラのWiFiチャネル変更通知
	 * requestAllSettingsを呼んでも来る
	 */
	private final ARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener
		mARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateAccessPointChannelChangedListener() {
		/**
		 * @param channel スカイコントローラーのWiFiチャネル
		 */
		@Override
		public void onSkyControllerAccessPointSettingsStateAccessPointChannelChangedUpdate(
			final byte channel) {

			if (DEBUG) Log.v(TAG, "onAccessPointChannelChangedUpdate:channel=" + channel);
		}
	};

	/**
	 * スカイコントローラーのWiFi選択変更通知
	 * requestAllSettingsを呼んでも来る
	 * onSkyControllerAccessPointSettingsStateAccessPointSSIDChangedUpdateと
	 * onSkyControllerAccessPointSettingsStateAccessPointChannelChangedUpdateの後に来るみたい
	 */
	private final ARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener
		mARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener
			= new ARCommandSkyControllerAccessPointSettingsStateWifiSelectionChangedListener() {
		/**
		 * @param type WiFi選択方法
		 * @param band 2.4GHz帯か5GHz帯か
		 * @param channel チャネル番号
		 */
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
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
			// FIXME setSkyControllerCameraResetOrientationを呼んでも来ない
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

//--------------------------------------------------------------------------------
	/**
	 * スカイコントローラーのボタン・スティック等の種類
	 * requestAllSettingsを呼んでも来る
	 * requestGamepadControlsを呼んでも来る
	 */
	private final ARCommandSkyControllerGamepadInfosStateGamepadControlListener
		mARCommandSkyControllerGamepadInfosStateGamepadControlListener
			= new ARCommandSkyControllerGamepadInfosStateGamepadControlListener() {
		@Override
		public void onSkyControllerGamepadInfosStateGamepadControlUpdate(
			final ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM type,
			final int id, final String name) {

			if (DEBUG) Log.v(TAG, "onStateGamepadControlUpdate:type=" + type + ", id=" + id + ", name=" + name);
			switch (type) {
			case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_AXIS:	// 0, スティック
			case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_BUTTON:	// 1, ボタン, スティックの押し込みを含む
				break;
			}
			// FIXME GamepadControlの追加処理
		}
	};

	/**
	 * スカイコントローラーのボタン・スティック等の種類の終端
	 * requestAllSettingsを呼んでも来る
	 * requestGamepadControlsを呼んでも来る
	 */
	private final ARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener
		mARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener
			= new ARCommandSkyControllerGamepadInfosStateAllGamepadControlsSentListener() {
		@Override
		public void onSkyControllerGamepadInfosStateAllGamepadControlsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onStateAllGamepadControlsSentUpdate:");
			// これが来たらスカイコントローラーのボタン・スティック等の種類リストはお終い
			// FIXME GamepadControlの追加処理終了
		}
	};
//--------------------------------------------------------------------------------
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

//--------------------------------------------------------------------------------
	/**
	 * 現在のボタン割当設定を受信した時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener
		mARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsStateCurrentButtonMappingsListener() {
		@Override
		public void onSkyControllerButtonMappingsStateCurrentButtonMappingsUpdate(
			final int key_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onCurrentButtonMappingsUpdate:key_id=" + key_id + ", mapping_uid=" + mapping_uid);
			// FIXME ボタン割り当て設定追加
		}
	};

	/**
	 * 現在のボタン割当設定の終端
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener
		mARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener
			= new ARCommandSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentListener() {
		@Override
		public void onSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentButtonMappingsSentUpdate:");
			// FIXME ボタン割り当て設定取得終了
		}
	};

//--------------------------------------------------------------------------------
	/**
	 * 使用可能なボタンの割当設定
	 * requestAllStatesを呼んでも来る
	 * 複数回来た後ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListenerが来る
	 */
	private final ARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener
		mARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener
			= new ARCommandSkyControllerButtonMappingsStateAvailableButtonMappingsListener() {
		/**
		 * @param mapping_uid　ボタンの識別コード
		 * @param name ボタンの名称
		 */
		@Override
		public void onSkyControllerButtonMappingsStateAvailableButtonMappingsUpdate(
			final String mapping_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onAvailableButtonMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
			// FIXME 使用可能ボタン割り当て設定に追加
		}
	};

	/**
	 * 使用可能なボタンの割当設定の終端
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener
		mARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener
			= new ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListener() {
		@Override
		public void onSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllAvailableButtonsMappingsSentUpdate:");
			// FIXME 使用可能ボタン割り当て設定取得終了
		}
	};
//--------------------------------------------------------------------------------

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

//--------------------------------------------------------------------------------
	/**
	 * ジョイスティック割当設定が変更された時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 * resetAxisMappingを呼んでも来る
	 * 複数回来た後ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListenerが来る
	 */
	private final ARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener
		mARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsStateCurrentAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsStateCurrentAxisMappingsUpdate(
			final int axis_id, final String mapping_uid) {

			if (DEBUG) Log.v(TAG, "onCurrentAxisMappingsUpdate:axis_id=" + axis_id + ", mapping_uid=" + mapping_uid);
			// FIXME ジョイスティック割当設定を追加
		}
	};

	/**
	 * ジョイスティック割当設定の終端
	 * requestAllStatesを呼んでも来る
	 * resetAxisMappingを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener
		mARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener
			= new ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentAxisMappingsSentUpdate:");
			// FIXME ジョイスティック割当設定追加終了
		}
	};
//--------------------------------------------------------------------------------
	/**
	 * 使用可能なジョイスティック割当を受信した時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener
		mARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener
			= new ARCommandSkyControllerAxisMappingsStateAvailableAxisMappingsListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAvailableAxisMappingsUpdate(
			final String mapping_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onAvailableAxisMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
		}
	};

	/**
	 * 使用可能なジョイスティック割当の終端
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener
		mARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener
			= new ARCommandSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentListener() {
		@Override
		public void onSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllAvailableAxisMappingsSentUpdate:");
		}
	};

//--------------------------------------------------------------------------------
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
//--------------------------------------------------------------------------------
	/**
	 * ジョイスティック入力フィルター設定が更新された時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener
		mARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersStateCurrentAxisFiltersListener() {
		/**
		 * @param axis_id 0..n
		 * @param filter_uid_or_builder "ARMF"ってのが来る
		 */
		@Override
		public void onSkyControllerAxisFiltersStateCurrentAxisFiltersUpdate(
			final int axis_id, final String filter_uid_or_builder) {

			if (DEBUG) Log.v(TAG, "onCurrentAxisFiltersUpdate:axis_id=" + axis_id + ", filter_uid_or_builder=" + filter_uid_or_builder);
			// FIXME ジョイスティック入力フィルター設定追加
		}
	};

	/**
	 * ジョイスティック入力フィルター設定の終端
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener
		mARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener
			= new ARCommandSkyControllerAxisFiltersStateAllCurrentFiltersSentListener() {
		@Override
		public void onSkyControllerAxisFiltersStateAllCurrentFiltersSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllCurrentFiltersSentUpdate:");
			// FIXME ジョイスティック入力フィルター設定追加終了
		}
	};
//--------------------------------------------------------------------------------
	/**
	 * ジョイスティック入力フィルターのプリセット値を受信した時のコールバックリスナー
	 * requestAllStatesを呼んでも来る…かもしれない。プリセット値がなければこれは来ずに
	 * onSkyControllerAxisFiltersStateAllPresetFiltersSentUpdateが来る
	 */
	private final ARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener
		mARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener
			= new ARCommandSkyControllerAxisFiltersStatePresetAxisFiltersListener() {
		@Override
		public void onSkyControllerAxisFiltersStatePresetAxisFiltersUpdate(
			final String filter_uid, final String name) {

			if (DEBUG) Log.v(TAG, "onPresetAxisFiltersUpdate:filter_uid=" + filter_uid + ", name=" + name);
		}
	};

	/**
	 * ジョイスティック入力フィルターのプリセット値の終端
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener
		mARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener
			= new ARCommandSkyControllerAxisFiltersStateAllPresetFiltersSentListener() {
		@Override
		public void onSkyControllerAxisFiltersStateAllPresetFiltersSentUpdate() {

			if (DEBUG) Log.v(TAG, "onAllPresetFiltersSentUpdate:");
		}
	};

//--------------------------------------------------------------------------------
//	/**
//	 * 操縦に使用する入力方法をセットした時のコールバックリスナー, スカイコントローラー自体のアプリで使用するコールバックリスナーかも
//	 * setCoPilotingSourceでsetSkyControllerCoPilotingSetPilotingSourceを呼ぶ時の引数が
//	 * このコールバックメソッドの引数(ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM)だけど
//	 * setCoPilotingSourceを呼んだ時にはこのコールバックではなく
//	 * ARCommandSkyControllerCoPilotingStatePilotingSourceListenerが呼ばれる(1つ下のコールバック)。
//	 * このコールバックメソッドの引数と同じ型のARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUMを使って
//	 * 呼び出すsetSkyControllerCoPilotingSetPilotingSource = setCoPilotingSourceを呼び出しても
//	 * このコールバックは呼び出されない。
//	 * なんでやねん, スカイコントローラー自体のアプリで使用するコールバックかも
//	 */
//	private final ARCommandSkyControllerCoPilotingSetPilotingSourceListener
//		mARCommandSkyControllerCoPilotingSetPilotingSourceListener
//			= new ARCommandSkyControllerCoPilotingSetPilotingSourceListener() {
//		@Override
//		public void onSkyControllerCoPilotingSetPilotingSourceUpdate(
//			final ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM source) {
//
//			if (DEBUG) Log.v(TAG, "onSetPilotingSourceUpdate:source=" + source);
//			switch (source) {
//			case ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_SKYCONTROLLER:	// 0
//				// スカイコントローラーで操縦する時
//				break;
//			case ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_CONTROLLER:		// 1
//				// タブレットまたはスマホで操縦する時
//				break;
//			}
//		}
//	};

	/**
	 * 操縦に使用する入力方法が変化した時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 * setCoPilotingSourceで値をセットすると呼ばれる。
	 * 一方setCoPilotingStateCoPilotingSourceでsetSkyControllerCoPilotingStatePilotingSourceを呼ぶ時の引数は
	 * このコールバックメソッドの引数と同じARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUMだけど
	 * どちらのコールバックメソッドも呼び出されない
	 * なんでやねん。
	 */
	private final ARCommandSkyControllerCoPilotingStatePilotingSourceListener
		mARCommandSkyControllerCoPilotingStatePilotingSourceListener
			= new ARCommandSkyControllerCoPilotingStatePilotingSourceListener() {
		@Override
		public void onSkyControllerCoPilotingStatePilotingSourceUpdate(
			final ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM source) {

			if (DEBUG) Log.v(TAG, "onCoPilotingSourceUpdate:source=" + source);
			switch (source) {
			case ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_SKYCONTROLLER:
				// スカイコントローラーで操縦する時
				break;
			case ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_CONTROLLER:
				// タブレットまたはスマホで操縦する時
				break;
			}
		}
	};

	/**
	 * 磁気センサーのキャリブレーション品質更新通知の有効/無効を切り替えた際のコールバック
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener
		mARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener
			= new ARCommandSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesListener()	{
		@Override
		public void onSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdatesUpdate(final byte enable) {

			if (DEBUG) Log.v(TAG, "onCalibrationQualityUpdatesUpdate:enable=" + enable);
		}
	};

	/**
	 * 磁気センサーのキャリブレーション精度更新通知
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener
		mARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener
			= new ARCommandSkyControllerCalibrationStateMagnetoCalibrationStateListener() {
		@Override
		public void onSkyControllerCalibrationStateMagnetoCalibrationStateUpdate(
			final ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM status,
				final byte X_Quality, final byte Y_Quality, final byte Z_Quality) {

			if (DEBUG) Log.v(TAG, "onCalibrationStateUpdate:status=" + status
				+ ", X_Quality=" + X_Quality + ", Y_Quality=" + Y_Quality + ", Z_Quality=" + Z_Quality);

			// FIXME 未実装
			switch (status) {
			case ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_UNRELIABLE:
				// 磁気センサーのキャリブレーションが必要
				break;
			case ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ASSESSING:
				// 磁気センサーのキャリブレーションはしてあるけど精度が不安
				break;
			case ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_CALIBRATED:
				// 磁気センサーのキャリブレーションに問題ない
				break;
			}
		}
	};

	/**
	 * 磁気センサーのキャリブレーション精度が更新された時に通知するかどうかの設定値が変更された時のコールバックリスナー
	 * requestAllStatesを呼んでも来る
	 */
	private final ARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener
		mARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener
			= new ARCommandSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesStateListener() {
		/**
		 * @param enabled 磁気センサーのキャリブレーション精度更新通知を有効にするかどうか
		 */
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

//********************************************************************************
// データ送受信関係
//********************************************************************************
	/** DeviceControllerのメソッドを上書き */
	@Override
	public boolean requestAllSettings() {
		if (DEBUG) Log.v(TAG, "requestAllSettings:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerSettingsAllSettings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestAllSettings command.");
		}

		return sentStatus;
	}

	/** DeviceControllerのメソッドを上書き */
	@Override
	public boolean requestAllStates() {
		if (DEBUG) Log.v(TAG, "requestAllStates:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerCommonAllStates();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestAllStates command.");
		}

		return sentStatus;
	}

//================================================================================
	/**
	 * スカイコントローラーの設定をリセット
	 * onSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * SSIDもリセットされる
	 */
	public boolean resetSettings() {
		if (DEBUG) Log.v(TAG, "resetSettings:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerSettingsReset();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetSettings command.");
		}

		// FIXME リセット完了通知がくるまでブロックする
		if (DEBUG) Log.v(TAG, "resetSettings:finished");
		return sentStatus;
	}


	// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiWifiAuthChannel()

	/**
	 * スカイコントローラーのSSIDを設定
	 * onAccessPointSSIDChangedUpdateとonSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * 次回電源投入時に有効になる
	 * @param ssid 設定するSSID 使用可能な文字数はたぶん32文字, 半角英数+α
	 * @return
	 */
	public boolean setSkyControllerSSID(final String ssid) {
		if (DEBUG) Log.v(TAG, "setSkyControllerSSID:ssid=" + ssid);
		if (TextUtils.isEmpty(ssid)) return false;

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAccessPointSettingsAccessPointSSID(ssid);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setSkyControllerSSID command.");
		}

		return sentStatus;
	}

//================================================================================
	/**
	 * スカイコントローラーが検出しているWiFiアクセスポイント一覧を要求
	 * 周囲に存在するWiFiの状態を確認するぐらいにしか役に立たない
	 */
	public boolean requestWifiList() {
		if (DEBUG) Log.v(TAG, "requestWifiList:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerWifiRequestWifiList();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestWifiList command.");
		}

		return sentStatus;
	}

	/**
	 * スカイコントローラーが現在接続しているWiFiネットワークとの接続状態を
	 * これを呼ぶとARCommandSkyControllerWifiStateConnexionChangedListenerと
	 * ARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 */
	public boolean requestCurrentWiFi() {
		if (DEBUG) Log.v(TAG, "requestCurrentWiFi:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerWifiRequestCurrentWifi();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestCurrentWiFi command.");
		}

		return sentStatus;
	}

	/**
	 * スカイコントローラーからSSIDで指定したWiFiネットワークに接続する
	 * @param bssid
	 * @param ssid
	 * @param passphrase
	 * @return
	 */
	public boolean connectToWiFi(final String bssid, final String ssid, final String passphrase) {
		if (DEBUG) Log.v(TAG, "requestForgetWiFi:");
		if (TextUtils.isEmpty(ssid)) return false;

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerWifiConnectToWifi(bssid, ssid, passphrase);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send connectToWiFi command.");
		}

		return sentStatus;
	}

	/**
	 * SSIDで指定したWiFiネットワークとのスカイコントローラー上の接続設定を消去, たぶん切断される
	 * @param ssid
	 * @return
	 */
	public boolean requestForgetWiFi(final String ssid) {
		if (DEBUG) Log.v(TAG, "requestForgetWiFi:");
		if (TextUtils.isEmpty(ssid)) return false;

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerWifiForgetWifi(ssid);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestForgetWiFi command.");
		}

		return sentStatus;
	}

//================================================================================
	/**
	 * スカイコントローラーが検出している機体一覧を要求
	 */
	public boolean requestDeviceList() {
		if (DEBUG) Log.v(TAG, "requestDeviceList:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		// FIXME 送信前に機体一覧Listをクリアする

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerDeviceRequestDeviceList();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestDeviceList command.");
		}

		return sentStatus;
	}

	/**
	 * スカイコントローラーが現在接続している機体との接続状態を要求する
	 * これを呼ぶとARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * 接続している機体がなくてもARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * (ARCommandSkyControllerWifiStateConnexionChangedListenerは来ない)
	 */
	public boolean requestCurrentDevice() {
		if (DEBUG) Log.v(TAG, "requestCurrentDevice:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerDeviceRequestCurrentDevice();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestCurrentDevice command.");
		}

		return sentStatus;
	}

	/**
	 * 指定したデバイス名を持つ機体へ接続する
	 * @param deviceName
	 * @return true 接続できなかった
	 */
	public boolean connectToDevice(final String deviceName) {
		if (DEBUG) Log.v(TAG, "connectToDevice:");
		if (TextUtils.isEmpty(deviceName)) return false;

		final DeviceInfo info = mDevices.containsKey(deviceName) ? mDevices.get(deviceName) : null;
		if ((info != null) && info.isConnected()) {
			// 既に接続されている
			return false;
		}

		mConnectDevice = null;
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerDeviceConnectToDevice(deviceName);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (sentStatus) {
			// 正常に送信できた。接続待ちする
			// FIXME 接続待ち
		} else {
			Log.e(TAG, "Failed to send connectToDevice command.");
		}

		return sentStatus;
	}

//================================================================================
	/**
	 * 操縦に使用する入力方法を選択
	 * ARCommandSkyControllerCoPilotingStatePilotingSourceListenerのコールバックメソッドが呼ばれる。なんでやねん
	 * @param _source 0: スカイコントローラーを使用する, 1: タブレット/スマホを使用する
	 */
	public boolean setCoPilotingSource(final int _source) {
		if (DEBUG) Log.v(TAG, "setCoPilotingSource:");
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM source
			= ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM.getFromValue(_source % 2);
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerCoPilotingSetPilotingSource(source);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setCoPilotingSource command.");
		}

		return sentStatus;
	}


// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAccessPointSettingsAccessPointChannel(byte channel)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAccessPointSettingsWifiSelection(ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_TYPE_ENUM type, ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_BAND_ENUM band, byte channel)

	/**
	 * カメラのpan/tiltをリセットする
	 * FIXME 機体のカメラなんかな? これを呼んでも何のコールバックもこない. スカイコントローラー自体のアプリ用なのかも
	 */
	public boolean resetCameraOrientation() {
		if (DEBUG) Log.v(TAG, "resetCameraOrientation:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerCameraResetOrientation();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetCameraOrientation command.");
		}

		return sentStatus;
	}

	/**
	 * スカイコントローラーのボタン・ジョイスティック等の一覧を要求する
	 */
	public boolean requestGamepadControls() {
		if (DEBUG) Log.v(TAG, "requestGamepadControls:");

		// FIXME これを送る前にGamepadControlListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerGamepadInfosGetGamepadControls();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestGamepadControls command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerGamepadInfosStateGamepadControlUpdateで値を追加する
		// FIXME onSkyControllerGamepadInfosStateAllGamepadControlsSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/** 現在のボタン割当設定を要求 */
	public boolean requestCurrentButtonMappings() {
		if (DEBUG) Log.v(TAG, "requestCurrentButtonMappings:");

		// FIXME これを送る前にCurrentButtonMappingListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerButtonMappingsGetCurrentButtonMappings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestCurrentButtonMappings command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerButtonMappingsStateCurrentButtonMappingsUpdateで値を追加する
		// FIXME onSkyControllerButtonMappingsStateAllCurrentButtonMappingsSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/** 使用可能なボタン割当設定を要求 */
	public boolean requestAvailableButtonMappings() {
		if (DEBUG) Log.v(TAG, "requestAvailableButtonMappings:");

		// FIXME これを送る前にAvailableButtonMappingListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerButtonMappingsGetAvailableButtonMappings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestAvailableButtonMappings command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerButtonMappingsStateAvailableButtonMappingsUpdateで値を追加する
		// FIXME onSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/**
	 * ボタンの割当設定
	 * @param key_id 物理ボタンID
	 * @param mapping_uid ボタン機能ID
	 * @return
	 */
	public boolean setButtonMapping(final int key_id, final String mapping_uid) {
		if (DEBUG) Log.v(TAG, "setButtonMapping:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerButtonMappingsSetButtonMapping(key_id, mapping_uid);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setButtonMapping command.");
		}

		return sentStatus;
	}

	/**
	 * ボタン割り付け設定をデフォルトにリセットする
	 * @return
	 */
	public boolean resetButtonMapping() {
		if (DEBUG) Log.v(TAG, "resetButtonMapping:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerButtonMappingsDefaultButtonMapping();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetButtonMapping command.");
		}

		return sentStatus;
	}


	/** 現在のジョイスティック割当設定を要求 */
	public boolean requestCurrentAxisMappings() {
		if (DEBUG) Log.v(TAG, "requestCurrentAxisMappings:");

		// FIXME これを送る前にCurrentAxisMappingListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisMappingsGetCurrentAxisMappings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestCurrentAxisMappings command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerAxisMappingsStateCurrentAxisMappingsUpdateで値を追加する
		// FIXME onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/** 使用可能なジョイスティック割当設定を要求 */
	public boolean requestAvailableAxisMappings() {
		if (DEBUG) Log.v(TAG, "connectToDevice:");

		// FIXME これを送る前にAvailableAxisMappingListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisMappingsGetAvailableAxisMappings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send connectToDevice command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerAxisMappingsStateAvailableAxisMappingsUpdateで値を追加する
		// FIXME onSkyControllerAxisMappingsStateAllAvailableAxisMappingsSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/**
	 * ジョイスティックの割当を変更する
	 * @param axis_id ジョイスティックの物理ID
	 * @param mapping_uid ジョイスティックの機能ID
	 * @return
	 */
	public boolean setAxisMapping(final int axis_id, final String mapping_uid) {
		if (DEBUG) Log.v(TAG, "setAxisMapping:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisMappingsSetAxisMapping(axis_id, mapping_uid);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetAxisMapping command.");
		}

		return sentStatus;
	}

	/**
	 * ジョイスティックの割当をデフォルトにリセットする
	 * なぜかonSkyControllerAxisMappingsStateCurrentAxisMappingsUpdateと
	 * onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdateが2ペア分送られてくる
	 * もしかすると1回目は変更前で2回目が変更後なのかも
	 * @return
	 */
	public boolean resetAxisMapping() {
		if (DEBUG) Log.v(TAG, "resetAxisMapping:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisMappingsDefaultAxisMapping();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetAxisMapping command.");
		}

		return sentStatus;
	}

	/** ジョイスティックの入力フィルター設定を要求 */
	public boolean requestCurrentAxisFilters() {
		if (DEBUG) Log.v(TAG, "requestCurrentAxisFilters:");

		// FIXME これを送る前にCurrentAxisFilterListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisFiltersGetCurrentAxisFilters();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestCurrentAxisFilters command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerAxisFiltersStateCurrentAxisFiltersUpdateで値を追加する
		// FIXME onSkyControllerAxisFiltersStateAllCurrentFiltersSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/** ジョイスティックの入力フィルターのプリセット設定を要求 */
	public boolean requestPresetAxisFilters() {
		if (DEBUG) Log.v(TAG, "requestPresetAxisFilters:");

		// FIXME これを送る前にPresetAxisFilterListをクリアする
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisFiltersGetPresetAxisFilters();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestPresetAxisFilters command.");
		}

		// FIXME 終了指示が来るか全て追加終わるかタイムアウトするかisStartedがfalseになるまで別スレッドで待機
		// FIXME onSkyControllerAxisFiltersStatePresetAxisFiltersUpdateで値を追加する
		// FIXME onSkyControllerAxisFiltersStateAllPresetFiltersSentUpdateが呼ばれたら終了
		// FIXME 変更通知(コールバックかLocalBroadcast)する

		return sentStatus;
	}

	/**
	 * ジョイスティックの入力フィルター設定
	 * @param axis_id 物理ジョイスティックID
	 * @param filter_uid_or_builder フィルターID
	 * @return
	 */
	public boolean setAxisFilter(final int axis_id, final String filter_uid_or_builder) {
		if (DEBUG) Log.v(TAG, "setAxisFilter:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisFiltersSetAxisFilter(axis_id, filter_uid_or_builder);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setAxisFilter command.");
		}

		return sentStatus;
	}

	/**
	 * ジョイスティックの入力フィルターをデフォルトにリセットする
	 * @return
	 */
	public boolean resetAxisFilter() {
		if (DEBUG) Log.v(TAG, "resetAxisFilter:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerAxisFiltersDefaultAxisFilters();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send resetAxisFilter command.");
		}

		return sentStatus;
	}

	/**
	 * 磁気センサーのキャリブレーション品質更新通知の有効/無効を切り替える
	 * @param enable
	 * @return
	 */
	public boolean setMagnetoCalibrationQualityUpdates(final boolean enable) {
		if (DEBUG) Log.v(TAG, "setMagnetoCalibrationQualityUpdates:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError
			= cmd.setSkyControllerCalibrationEnableMagnetoCalibrationQualityUpdates(enable ? (byte)1 : (byte)0);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setMagnetoCalibrationQualityUpdates command.");
		}

		return sentStatus;
	}

	/**
	 * なんじゃらほい?
	 * 何のコールバックも返ってこない
	 * FIXME スカイコントローラー自体のアプリ用なのかも
	 * @return
	 */
	public boolean requestButtonEventsSettings() {
		if (DEBUG) Log.v(TAG, "requestButtonEventsSettings:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerButtonEventsSettings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send requestButtonEventsSettings command.");
		}

		return sentStatus;
	}

	/**
	 * なんかわからんけどデバッグフラグセットするんでしょうきっと
	 * @param t1Args
	 * @return
	 */
	public boolean setDebugTest1(final byte t1Args) {
		if (DEBUG) Log.v(TAG, "setDebugTest1:");

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setSkyControllerDebugDebugTest1(t1Args);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send setDebugTest1 command.");
		}

		return sentStatus;
	}

//********************************************************************************
// スカイコントローラー自体のアプリで使用するコマンドかも, 途中にstateが入ってるメソッド
//********************************************************************************
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiStateWifiList(String bssid, String ssid, byte secured, byte saved, int rssi, int frequency)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiStateConnexionChanged(String ssid, ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM status)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiStateWifiAuthChannelListChanged(ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band, byte channel, byte in_or_out)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiStateAllWifiAuthChannelChanged()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerWifiStateWifiSignalChanged(byte level)

/**
 * 操縦に使用する入力方法を選択, でもこれで値をセットしてもどのコールバックも呼び出されない。
 * もしくはスカイコントローラー自体のアプリで使用するコマンドかも
 * @param _source 0: スカイコントローラーを使用する, 1: タブレット/スマホを使用する
 */
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerCoPilotingStatePilotingSource(ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM source)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerDeviceStateDeviceList(String name)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerDeviceStateConnexionChanged(ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status, String deviceName, short deviceProductID)

// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSettingsStateAllSettingsChanged()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSettingsStateResetChanged()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSettingsStateProductSerialChanged(String serialNumber)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSettingsStateProductVariantChanged(ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM variant)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerCommonStateAllStatesChanged()

// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSkyControllerStateBatteryChanged(byte percent)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSkyControllerStateGpsFixChanged(byte fixed)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerSkyControllerStateGpsPositionChanged(double latitude, double longitude, double altitude, float heading)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAccessPointSettingsStateAccessPointSSIDChanged(String ssid)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAccessPointSettingsStateAccessPointChannelChanged(byte channel)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAccessPointSettingsStateWifiSelectionChanged(ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type, ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band, byte channel)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerGamepadInfosStateGamepadControl(ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM type, int id, String name)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerGamepadInfosStateAllGamepadControlsSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerButtonMappingsStateCurrentButtonMappings(int key_id, String mapping_uid)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerButtonMappingsStateAllCurrentButtonMappingsSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerButtonMappingsStateAvailableButtonMappings(String mapping_uid, String name)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisMappingsStateCurrentAxisMappings(int axis_id, String mapping_uid)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisMappingsStateAllCurrentAxisMappingsSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisMappingsStateAvailableAxisMappings(String mapping_uid, String name)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisMappingsStateAllAvailableAxisMappingsSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisFiltersStateCurrentAxisFilters(int axis_id, String filter_uid_or_builder)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisFiltersStateAllCurrentFiltersSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisFiltersStatePresetAxisFilters(String filter_uid, String name)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerAxisFiltersStateAllPresetFiltersSent()
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerCalibrationStateMagnetoCalibrationState(ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM status, byte X_Quality, byte Y_Quality, byte Z_Quality)
// public ARCOMMANDS_GENERATOR_ERROR_ENUM setSkyControllerCalibrationStateMagnetoCalibrationQualityUpdatesState(byte enabled)

}
