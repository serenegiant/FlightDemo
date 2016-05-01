package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureSkyController;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.CommonStatus;
import com.serenegiant.arflight.DeviceConnectionListener;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.ISkyController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.IWiFiController;
import com.serenegiant.arflight.SkyControllerListener;
import com.serenegiant.arflight.VideoStreamDelegater;
import com.serenegiant.arflight.attribute.AttributeGPS;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.serenegiant.arflight.ARFlightConst.*;

public class SkyControllerNewAPI extends FlightControllerBebopNewAPI implements ISkyController, IVideoStreamController, IWiFiController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = SkyControllerNewAPI.class.getSimpleName();

	/** 接続中の機体情報 */
	private DeviceInfo mConnectDevice;
	/** 機体接続待ちのためのセマフォ */
	private final Semaphore mConnectDeviceSent = new Semaphore(0);
	protected volatile boolean mRequestConnectDevice;

	protected CommonStatus mSkyControllerStatus = new CommonStatus();
	public final AttributeGPS mSkyControllerGPS = new AttributeGPS();
	private final List<SkyControllerListener> mListeners = new ArrayList<SkyControllerListener>();

	public SkyControllerNewAPI(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
	}

	public void cancelStart() {
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
		}
		super.cancelStart();
	}

//	protected void onStarting() {
//		if (DEBUG) Log.d(TAG, "onStarting:");
//		super.onStarting();
//	}

	protected void onStopped() {
		mConnectDeviceSent.release();
		for ( ; mConnectDeviceSent.tryAcquire(); ) {}
		super.onStopped();
	}

	@Override
	protected void onConnect() {
		super.onConnect();
		callOnSkyControllerConnect();
	}

	@Override
	protected void onDisconnect() {
		callOnSkyControllerDisconnect();
		super.onDisconnect();
	}

	@Override
	protected void callOnConnect() {
		// これは機体が接続した時(onExtensionConnect)に呼び出したいので無効にする
	}

	@Override
	protected void callOnDisconnect() {
		// これは機体が切断された時(onExtensionDisconnect)に呼び出したいので無効にする
	}

	/**
	 * onExtensionStateChangedの下請け
	 * スカイコントローラーが機体に接続した時に呼ばれる
	 */
	@Override
	protected void onExtensionConnect() {
		if (DEBUG) Log.d(TAG, "onExtensionConnect:");
		super.onExtensionConnect();
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
		}
		super.callOnConnect();
	}

	/**
	 * onExtensionStateChangedの下請け
	 * スカイコントローラーが機体から切断された時に呼ばれる
	 */
	protected void onExtensionDisconnect() {
		if (DEBUG) Log.d(TAG, "onExtensionDisconnect:");
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
		}
		super.callOnDisconnect();
		super.onExtensionDisconnect();
	}

	@Override
	public boolean isConnected() {
		synchronized (mStateSync) {
			return super.isConnected()
				&& (mConnectDevice != null)
				&& ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mExtensionState);
		}
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args,
		final ARControllerDictionary elementDictionary) {

		super.onCommandReceived(deviceController, commandKey, args, elementDictionary);

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER:	// (119, "Key used to define the feature <code>SkyControllerNewAPI</code>"),
			break;
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST:	// (120, "Key used to define the command <code>WifiList</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーが検出したアクセスポイント一覧を取得した時
			// 検出しているアクセスポイント1つ毎に1回呼び出される
			// requestWifiListに対する応答, 自動的には来ない
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object bssid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_BSSID);
				final Object ssid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SSID);
				final Object secured_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SECURED);
				final Object saved_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SAVED);
				final Object rssi_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_RSSI);
				final Object frequency_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_FREQUENCY);

				if ((bssid_obj != null) && (ssid_obj != null) && (secured_obj != null)
					&& (saved_obj != null) && (rssi_obj != null) && (frequency_obj != null)) {
					final String bssid = (String)bssid_obj;
					final String ssid = (String)ssid_obj;
					final boolean secured = (Integer)secured_obj != 0;
					final boolean saved = (Integer)saved_obj != 0;
					final int rssi = (Integer)rssi_obj;
					final int frequency = (Integer)frequency_obj;

					if (DEBUG) Log.v(TAG, String.format("onWifiListUpdate:bssid=%s, ssid=%s, secured=%d, saved=%d, rssi=%d, frequency=%d",
						bssid, ssid, secured ? 1 : 0, saved ? 1 : 0, rssi, frequency));
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED:	// (121, "Key used to define the command <code>ConnexionChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーとWiFiアクセスポイント間の接続状態が変化した時のコールバックリスナー
			// requestAllStatesでも来る
			// requestCurrentWiFiを呼んでも来る...NewAPIだとこない?
			// 何故か1回の接続で3回来るのと切断された時には来ないみたい
			// ...ARCONTROLLER_DEVICE_STATE_ENUMのstop, starting, runningのタイミングで来るのかも
			// XXX これは普通は使わんで良さそう
			final String ssid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_SSID);
			final ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM status
				= ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS)
			);
			if (DEBUG) Log.v(TAG, "onWiFiConnexionChangedUpdate:ssid=" + ssid + ", status=" + status);
			switch (status) {
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_CONNECTED:		// 0
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ERROR:			// 1
			case ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_DISCONNECTED:	// 2
				break;
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED:	// (122, "Key used to define the command <code>WifiAuthChannelListChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// WiFi一覧? 自動では来ない
			if (DEBUG) Log.v(TAG, "WIFISTATE_WIFIAUTHCHANNELLISTCHANGED:");
			final ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band
				= ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND)
			);
			final int channel = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_CHANNEL);
			final boolean in_or_out = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_IN_OR_OUT) != 0;

			if (DEBUG) Log.v(TAG, "onWifiAuthChannelListChangedUpdate:band=" + band + ", channel=" + channel + ", in_or_out=" + in_or_out);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_ALLWIFIAUTHCHANNELCHANGED:	// (123, "Key used to define the command <code>AllWifiAuthChannelChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// WiFi一覧?の終端? 自動では来ない
			if (DEBUG) Log.v(TAG, "onAllWifiAuthChannelChangedUpdate:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFISIGNALCHANGED:	// (124, "Key used to define the command <code>WifiSignalChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// WiFiの信号強度が変化した時
			// requestAllStatesを呼ぶと来る
			// FIXME ...ってどの信号強度なんだろ?
			// FIXME スカイコントローラーが受信しているタブレット/スマホからの電波の信号強度なんかな?
			// FIXME 機体と接続した時に送られてくるので機体からスカイコントローラーに届く信号強度かも
			final int level = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFISIGNALCHANGED_LEVEL);

			if (DEBUG) Log.v(TAG, "onWifiSignalChangedUpdate:level=" + level);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_DEVICELIST:	// (125, "Key used to define the command <code>DeviceList</code> of class <code>DeviceState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// デバイスリスト(機体名?)を受信した時
			final String name = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_DEVICELIST_NAME);

			if (DEBUG) Log.e(TAG, "onDeviceListUpdate:name=" + name);
			// FIXME 今のところ来ない, スカイコントローラー自体のアプリ用なのかも
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED:	// (126, "Key used to define the command <code>ConnexionChanged</code> of class <code>DeviceState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーと機体の接続状態が変化した時のコールバックリスナー
			// requestAllStatesでも来る
			// requestCurrentDeviceを呼ぶと来る...NewAPIだとこない?
			// requestCurrentWiFiを呼んでも来る...NewAPIだとこない?
			// AccessPointのSSIDやチャネル等を変更しても来る
			// たぶん最初に見つかった機体には勝手に接続しに行きよる
			// ARCommandSkyControllerWifiStateConnexionChangedListenerのコールバックメソッドよりも後に来る
			// onAllSettingsUpdate/onAllStateUpdateよりも後に来る
 			final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status
				= ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS)
			);
			final String deviceName = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_DEVICENAME);
			final int deviceProductID = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_DEVICEPRODUCTID);

			if (DEBUG) Log.v(TAG, "onDeviceConnexionChangedUpdate:status=" + status + ", deviceName=" + deviceName + ", deviceProductID=" + deviceProductID);
			updateConnectionState(status, deviceName, deviceProductID);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_ALLSETTINGSCHANGED:	// (127, "Key used to define the command <code>AllSettingsChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーの設定を全て受信した時
			if (DEBUG) Log.w(TAG, "onAllSettingsUpdate:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_RESETCHANGED:	// (128, "Key used to define the command <code>ResetChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーの設定がリセットされた時
			if (DEBUG) Log.e(TAG, "onResetSettings:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTSERIALCHANGED:	// (129, "Key used to define the command <code>ProductSerialChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのシリアル番号を受信した時
			// requestAllStatesを呼んだら来る
			final String serial = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTSERIALCHANGED_SERIALNUMBER);
			mInfo.setSerialHigh(serial);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED:	// (130, "Key used to define the command <code>ProductVariantChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーの種類情報受信時
			// requestAllStatesを呼んだら来る
			final ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM variant
				= ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT)
			);
			mInfo.setSerialLow(":" + variant.toString());
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COMMONSTATE_ALLSTATESCHANGED:	// (131, "Key used to define the command <code>AllStatesChanged</code> of class <code>CommonState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのステータスを全て受信した時
			if (DEBUG) Log.w(TAG, "onAllStateUpdate:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED:	// (132, "Key used to define the command <code>BatteryChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのバッテリー残量が変化した時
			final int percent = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED_PERCENT);
			mSkyControllerStatus.setBattery(percent);
			callOnSkyControllerUpdateBattery(percent);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSFIXCHANGED:	// (133, "Key used to define the command <code>GpsFixChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// GPSで位置を確認出来たかどうかを受信した時
			final boolean fixed = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSFIXCHANGED_FIXED) != 0;
			mSkyControllerGPS.setFixed(fixed);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED:	// (134, "Key used to define the command <code>GpsPositionChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーの位置(GPS)を受信した時
			/** GPS緯度[度] (500.0: 不明) */
			final double latitude = (Double)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED_LATITUDE);
			/** GPS経度[度] (500.0: 不明) */
			final double longitude = (Double)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED_LONGITUDE);
			/** GPS高度[m](500.0: 不明) */
			final double altitude = (Double)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED_ALTITUDE);
			/** 方位角 */
			final double heading = (Double)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_GPSPOSITIONCHANGED_HEADING);
			mSkyControllerStatus.setPosition(latitude, longitude, altitude, heading);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTSSIDCHANGED:	// (135, "Key used to define the command <code>AccessPointSSIDChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのSSIDが変更された時
			// requestAllSettingsを呼んでも来る, setSkyControllerSSIDでセットしても呼ばれそう(未確認)
			final String ssid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTSSIDCHANGED_SSID);

			if (DEBUG) Log.e(TAG, "onAccessPointSSIDUpdate:ssid=" + ssid);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTCHANNELCHANGED:	// (136, "Key used to define the command <code>AccessPointChannelChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラのWiFiチャネル変更通知
			// requestAllSettingsを呼んでも来る
			final int channel = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_ACCESSPOINTCHANNELCHANGED_CHANNEL);
			if (DEBUG) Log.v(TAG, "onAccessPointChannelChangedUpdate:channel=" + channel);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED:	// (137, "Key used to define the command <code>WifiSelectionChanged</code> of class <code>AccessPointSettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのWiFi選択変更通知
			// requestAllSettingsを呼んでも来る
			// onSkyControllerAccessPointSettingsStateAccessPointSSIDChangedUpdateと
			// onSkyControllerAccessPointSettingsStateAccessPointChannelChangedUpdateの後に来るみたい
			/** WiFi選択方法 */
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type
				= ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE)
			);
			/** 2.4GHz帯か5GHz帯か */
			final ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band
				= ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND)
			);
			/** チャネル番号 */
			final int channel = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_CHANNEL);
			if (DEBUG) Log.v(TAG, "onWifiSelectionChangedUpdate:type=" + type + ", band=" + band + ", channel=" + channel);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL:	// (138, "Key used to define the command <code>GamepadControl</code> of class <code>GamepadInfosState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのボタン・スティック等の種類
			// requestAllSettingsを呼んでも来る
			// requestGamepadControlsを呼んでも来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object name_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_NAME);
				final Object type_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE);
				final Object id_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_ID);
				if ((name_obj != null) && (type_obj != null) && (id_obj != null)) {
					final String name = (String)name_obj;
					final ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM type
						= ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM.getFromValue((Integer)type_obj);
					final int id = (Integer)id_obj;
						if (DEBUG) Log.v(TAG, "onStateGamepadControlUpdate:type=" + type + ", id=" + id + ", name=" + name);
					switch (type) {
					case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_AXIS:	// 0, スティック
					case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_BUTTON:	// 1, ボタン, スティックの押し込みを含む
						break;
					}
					// FIXME GamepadControlの追加処理
				}
			}

			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_ALLGAMEPADCONTROLSSENT:	// (139, "Key used to define the command <code>AllGamepadControlsSent</code> of class <code>GamepadInfosState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのボタン・スティック等の種類の終端
			// requestAllSettingsを呼んでも来る
			// requestGamepadControlsを呼んでも来る
			if (DEBUG) Log.v(TAG, "onStateAllGamepadControlsSentUpdate:");
			// これが来たらスカイコントローラーのボタン・スティック等の種類リストはお終い
			// FIXME GamepadControlの追加処理終了
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS:	// (140, "Key used to define the command <code>CurrentButtonMappings</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 現在のボタン割当設定を受信した時
			// requestAllStatesを呼んでも来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object uid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS_MAPPING_UID);
				final Object id_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS_KEY_ID);
				if ((uid_obj != null) && (id_obj != null)) {
					final String mapping_uid = (String)uid_obj;
					final int key_id = (Integer)id_obj;
					if (DEBUG) Log.v(TAG, "onCurrentButtonMappingsUpdate:key_id=" + key_id + ", mapping_uid=" + mapping_uid);
					// FIXME ボタン割り当て設定追加
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_ALLCURRENTBUTTONMAPPINGSSENT:	// (141, "Key used to define the command <code>AllCurrentButtonMappingsSent</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 現在のボタン割当設定の終端
			// requestAllStatesを呼んでも来る
			if (DEBUG) Log.v(TAG, "onAllCurrentButtonMappingsSentUpdate:");
			// FIXME ボタン割り当て設定取得終了
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS:	// (142, "Key used to define the command <code>AvailableButtonMappings</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 使用可能なボタンの割当設定
			// requestAllStatesを呼んでも来る
			// 複数回来た後ARCommandSkyControllerButtonMappingsStateAllAvailableButtonsMappingsSentListenerが来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object uid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS_MAPPING_UID);
				final Object name_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS_NAME);
				if ((uid_obj != null) && (name_obj != null)) {
					/** ボタンの識別コード */
					final String mapping_uid = (String)uid_obj;
					/** ボタン名 */
					final String name = (String)name_obj;
					if (DEBUG) Log.v(TAG, "onAvailableButtonMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
					// FIXME 使用可能ボタン割り当て設定に追加
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_ALLAVAILABLEBUTTONSMAPPINGSSENT:	// (143, "Key used to define the command <code>AllAvailableButtonsMappingsSent</code> of class <code>ButtonMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 使用可能なボタンの割当設定の終端
			// requestAllStatesを呼んでも来る
			if (DEBUG) Log.v(TAG, "onAllAvailableButtonsMappingsSentUpdate:");
			// FIXME 使用可能ボタン割り当て設定取得終了
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS:	// (144, "Key used to define the command <code>CurrentAxisMappings</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック割当設定が変更された時のコールバックリスナー
			// requestAllStatesを呼んでも来る
			// resetAxisMappingを呼んでも来る
			// 複数回来た後ARCommandSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentListenerが来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object uid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS_MAPPING_UID);
				final Object id_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS_AXIS_ID);
				if ((uid_obj != null) && (id_obj != null)) {
					final String mapping_uid = (String)uid_obj;
					final int axis_id = (Integer)id_obj;
					if (DEBUG) Log.v(TAG, "onCurrentAxisMappingsUpdate:axis_id=" + axis_id + ", mapping_uid=" + mapping_uid);
					// FIXME ジョイスティック割当設定を追加
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_ALLCURRENTAXISMAPPINGSSENT:	// (145, "Key used to define the command <code>AllCurrentAxisMappingsSent</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック割当設定の終端
			// requestAllStatesを呼んでも来る
			// resetAxisMappingを呼んでも来る
			if (DEBUG) Log.v(TAG, "onAllCurrentAxisMappingsSentUpdate:");
			// FIXME ジョイスティック割当設定追加終了
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS:	// (146, "Key used to define the command <code>AvailableAxisMappings</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 使用可能なジョイスティック割当を受信した時
			// requestAllStatesを呼んでも来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object uid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS_MAPPING_UID);
				final Object name_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS_NAME);
				if ((uid_obj != null) && (name_obj != null)) {
					final String mapping_uid = (String)uid_obj;
					final String name = (String)name_obj;
					if (DEBUG) Log.v(TAG, "onAvailableAxisMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_ALLAVAILABLEAXISMAPPINGSSENT:	// (147, "Key used to define the command <code>AllAvailableAxisMappingsSent</code> of class <code>AxisMappingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 使用可能なジョイスティック割当の終端
			// requestAllStatesを呼んでも来る
			if (DEBUG) Log.v(TAG, "onAllAvailableAxisMappingsSentUpdate:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS:	// (148, "Key used to define the command <code>CurrentAxisFilters</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック入力フィルター設定が更新された時
			// requestAllStatesを呼んでも来る
			// "ARMF"ってのが来る
			for (final ARControllerArgumentDictionary<Object> element: elementDictionary.values()) {
				final Object uid_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS_FILTER_UID_OR_BUILDER);
				final Object id_obj = element.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS_AXIS_ID);
				if ((uid_obj != null) && (id_obj != null)) {
					final String filter_uid_or_builder = (String)uid_obj;
					/** 軸番号: 0..n */
					final int axis_id = (Integer)id_obj;
					if (DEBUG) Log.v(TAG, "onCurrentAxisFiltersUpdate:axis_id=" + axis_id + ", filter_uid_or_builder=" + filter_uid_or_builder);
					// FIXME ジョイスティック入力フィルター設定追加
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_ALLCURRENTFILTERSSENT:	// (149, "Key used to define the command <code>AllCurrentFiltersSent</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック入力フィルター設定の終端
			if (DEBUG) Log.v(TAG, "onAllCurrentFiltersSentUpdate:");
			// FIXME ジョイスティック入力フィルター設定追加終了
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_PRESETAXISFILTERS:	// (150, "Key used to define the command <code>PresetAxisFilters</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック入力フィルターのプリセット値を受信した時
			// requestAllStatesを呼んでも来る…かもしれない。プリセット値がなければこれは来ずに
			// onSkyControllerAxisFiltersStateAllPresetFiltersSentUpdateが来る
			final String filter_uid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_PRESETAXISFILTERS_FILTER_UID);
			final String name = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_PRESETAXISFILTERS_NAME);
			if (DEBUG) Log.v(TAG, "onPresetAxisFiltersUpdate:filter_uid=" + filter_uid + ", name=" + name);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_ALLPRESETFILTERSSENT:	// (151, "Key used to define the command <code>AllPresetFiltersSent</code> of class <code>AxisFiltersState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// ジョイスティック入力フィルターのプリセット値の終端
			// requestAllStatesを呼んでも来る
			if (DEBUG) Log.v(TAG, "onAllPresetFiltersSentUpdate:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE:	// (152, "Key used to define the command <code>PilotingSource</code> of class <code>CoPilotingState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 操縦に使用する入力方法が変化した時のコールバックリスナー
			// requestAllStatesを呼んでも来る
			// setCoPilotingSourceで値をセットすると呼ばれる。
			// 一方setCoPilotingStateCoPilotingSourceでsetSkyControllerCoPilotingStatePilotingSourceを呼ぶ時の引数は
			// このコールバックメソッドの引数と同じARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUMだけど
			// どちらのコールバックメソッドも呼び出されない
			// なんでやねん。
			mCoPilotingSource = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE);
			final ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM source
				= ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM.getFromValue(mCoPilotingSource);
			if (DEBUG) Log.v(TAG, "onCoPilotingSourceUpdate:source=" + source);
			switch (source) {
			case ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_SKYCONTROLLER:
				// スカイコントローラーで操縦する時
				break;
			case ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_CONTROLLER:
				// タブレットまたはスマホで操縦する時
				break;
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE:	// (153, "Key used to define the command <code>MagnetoCalibrationState</code> of class <code>CalibrationState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 磁気センサーのキャリブレーション精度更新通知
			// requestAllStatesを呼んでも来る
			final ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM status
				= ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS)
			);
			final int X_Quality = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_X_QUALITY);
			final int Y_Quality = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_Y_QUALITY);
			final int Z_Quality = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_Z_QUALITY);
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
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONQUALITYUPDATESSTATE:	// (154, "Key used to define the command <code>MagnetoCalibrationQualityUpdatesState</code> of class <code>CalibrationState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// 磁気センサーのキャリブレーション精度が更新された時に通知するかどうかの設定値が変更された時
			// requestAllStatesを呼んでも来る
			final boolean enabled = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONQUALITYUPDATESSTATE_ENABLED) != 0;
			if (DEBUG) Log.v(TAG, "onCalibrationQualityUpdatesStateUpdate:enabled=" + enabled);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONEVENTS_SETTINGS:	// (155, "Key used to define the command <code>Settings</code> of class <code>ButtonEvents</code> in project <code>SkyControllerNewAPI</code>"),
		{	// なんかわからへん
			if (DEBUG) Log.v(TAG, "onButtonEventsSettingsUpdate:");
			break;
		}
		default:
			break;
		}
	}

//================================================================================
// 機体との接続状態の管理
//================================================================================
	/** スカイコントローラーの認識している機体一覧 */
	private final Map<String, DeviceInfo> mDevices = new HashMap<String, DeviceInfo>();

	private void updateConnectionState(
		final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status,
		final String deviceName, final int deviceProductID) {

		if (DEBUG) Log.v(TAG, "updateConnectionState:");
		synchronized (mDevices) {
			DeviceInfo info = mDevices.containsKey(deviceName) ? mDevices.get(deviceName) : null;
			switch (status) {
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_NOTCONNECTED:		// 0
				removeDevice(null);
				break;
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTING:		// 1
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_CONNECTED:		// 2
				if (info == null) {
					if (DEBUG) Log.v(TAG, "found new device:name=" + deviceName);
					info = new DeviceInfo(deviceName, deviceProductID);
					mDevices.put(deviceName, info);
				}
				info.connectionState(status.getValue());
				synchronized (mStateSync) {
					mConnectDevice = info;
				}
				break;
			case ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_DISCONNECTING:    // 3
				removeDevice(deviceName);
				break;
			}
		}
		broadcastConnectedDevices();
	}

	private boolean broadcastConnectedDevices() {
		DeviceInfo[] info_array = null;
		synchronized (mDevices) {
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
		} else {
			Log.w(TAG, "mLocalBroadcastManager is null, already released?");
		}
		return info_array != null;
	}

	/**
	 * #updateConnectionStateの下請け, mDevicesがロックされた状態で呼び出される
	 * @param deviceName
	 */
	private void removeDevice(final String deviceName) {
		if (DEBUG) Log.d(TAG, "removeDevice:" + deviceName);
		if (TextUtils.isEmpty(deviceName)) {
			mDevices.clear();
		} else {
			mDevices.remove(deviceName);
			synchronized (mStateSync) {
				if ((mConnectDevice != null) && mConnectDevice.name().equals(deviceName)) {
					mConnectDevice = null;
				}
			}
		}
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
				mListeners.add((SkyControllerListener) listener);
			}
		}
	}

	/**
	 * 指定したコールバックリスナーを取り除く
	 * @param listener
	 */
	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mListeners) {
			if (listener instanceof SkyControllerListener) {
				mListeners.remove((SkyControllerListener)listener);
			}
		}
		super.removeListener(listener);
	}

	/**
	 * 接続時のコールバックを呼び出す
	 */
	protected void callOnSkyControllerConnect() {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerConnect:");
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerConnect(this);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 切断時のコールバックを呼び出す
	 */
	protected void callOnSkyControllerDisconnect() {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerDisconnect:");
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerDisconnect(this);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 異常状態変更コールバックを呼び出す
	 * @param state
	 */
	protected void callOnSkyControllerAlarmStateChangedUpdate(final int state) {
		if (DEBUG) Log.v(TAG, "callOnAlarmStateChangedUpdate:" + state);
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerAlarmStateChangedUpdate(this, state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * バッテリー残量変更コールバックを呼び出す
	 */
	protected void callOnSkyControllerUpdateBattery(final int percent) {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerUpdateBattery:" + percent);
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerUpdateBattery(this, percent);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーションが必要かどうかが変更された時のコールバックを呼び出す
	 * @param need_calibration
	 */
	protected void callOnSkyControllerCalibrationRequiredChanged(final boolean need_calibration) {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerCalibrationRequiredChanged:" + need_calibration);
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerCalibrationRequiredChanged(this, need_calibration);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーションを開始/終了した時のコールバックを呼び出す
	 * @param isStart
	 */
	protected void callOnSkyControllerCalibrationStartStop(final boolean isStart) {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerCalibrationStartStop:" + isStart);
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerCalibrationStartStop(this, isStart);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	protected void callOnSkyControllerCalibrationAxisChanged(final int axis) {
		if (DEBUG) Log.v(TAG, "callOnSkyControllerCalibrationStartStop:" + axis);
		synchronized (mListeners) {
			for (final SkyControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onSkyControllerCalibrationAxisChanged(this, axis);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}
//================================================================================
// IBridgeController
//================================================================================
	@Override
	public ARNetworkConfig createBridgeNetConfig() {
		if (DEBUG) Log.d(TAG, "createBridgeNetConfig:");
		return null;
	}

	@Override
	public VideoStreamDelegater getVideoStreamDelegater() {
		if (DEBUG) Log.d(TAG, "getVideoStreamDelegater:");
		return null;
	}

	public boolean requestAllSettings() {
		if (DEBUG) Log.d(TAG, "requestAllSettings:");
		super.requestAllSettings();
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		result = mARDeviceController.getFeatureSkyController().sendSettingsAllSettings();
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAllSettings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	public boolean requestAllStates() {
		if (DEBUG) Log.d(TAG, "requestAllStates:");
		super.requestAllStates();
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		result = mARDeviceController.getFeatureSkyController().sendCommonAllStates();
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAllStates failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean resetSettings() {
		if (DEBUG) Log.d(TAG, "resetSettings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendSettingsReset();
		} else {
			if (DEBUG) Log.v(TAG, "resetSettings:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#resetSettings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setSkyControllerSSID(final String ssid) {
		if (DEBUG) Log.d(TAG, "setSkyControllerSSID:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAccessPointSettingsAccessPointSSID(ssid);
		} else {
			if (DEBUG) Log.v(TAG, "setSkyControllerSSID:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setSkyControllerSSID failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

//	public ARCONTROLLER_ERROR_ENUM sendAccessPointSettingsAccessPointChannel (byte _channel)

	public boolean sendWifiWifiAuthChannel() {
		if (DEBUG) Log.d(TAG, "sendWifiWifiAuthChannel:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendWifiWifiAuthChannel();
		} else {
			if (DEBUG) Log.v(TAG, "sendWifiWifiAuthChannel:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendWifiWifiAuthChannel failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

//	public ARCONTROLLER_ERROR_ENUM sendAccessPointSettingsWifiSelection (ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_TYPE_ENUM _type, ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_BAND_ENUM _band, byte _channel)

	/**
	 * @return
	 */
	@Override
	public boolean requestWifiList() {
		if (DEBUG) Log.d(TAG, "requestWifiList:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendWifiRequestWifiList();
		} else {
			if (DEBUG) Log.v(TAG, "requestWifiList:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestWifiList failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestCurrentWiFi() {
		if (DEBUG) Log.d(TAG, "requestCurrentWiFi:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendWifiRequestCurrentWifi();
		} else {
			if (DEBUG) Log.v(TAG, "requestCurrentWiFi:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestCurrentWiFi failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean connectToWiFi(final String bssid, final String ssid, final String passphrase) {
		if (DEBUG) Log.d(TAG, "connectToWiFi:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendWifiConnectToWifi(bssid, ssid, passphrase);
		} else {
			if (DEBUG) Log.v(TAG, "connectToWiFi:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#connectToWiFi failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestForgetWiFi(final String ssid) {
		if (DEBUG) Log.d(TAG, "requestForgetWiFi:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendWifiForgetWifi(ssid);
		} else {
			if (DEBUG) Log.v(TAG, "requestForgetWiFi:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestForgetWiFi failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * スカイコントローラーが検出している機体一覧を要求
	 * @return
	 */
	@Override
	public boolean requestDeviceList() {
		if (DEBUG) Log.d(TAG, "requestDeviceList:");

		// FIXME 送信前に機体一覧Listをクリアする...でもARSDK3.8.3のNewAPIだと結果が来ないので検出している機体リストに値があればそれをブロードキャストする
		if (!broadcastConnectedDevices()) {
			ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
			if (isStarted()) {
				result = mARDeviceController.getFeatureSkyController().sendDeviceRequestDeviceList();
			} else {
				if (DEBUG) Log.v(TAG, "requestDeviceList:not started");
			}
			if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
				Log.e(TAG, "#requestDeviceList failed:" + result);
			}
			return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}
		return false;
	}

	@Override
	public int getDeviceNum() {
		synchronized (mDevices) {
			return mDevices.size();
		}
	}

	@Override
	public List<DeviceInfo> getDeviceList() {
		final List<DeviceInfo> result;
		synchronized (mDevices) {
			result = new ArrayList<DeviceInfo>(mDevices.values());
		}
		return result;
	}

	/**
	 * スカイコントローラーが現在接続している機体との接続状態を要求する
	 * XXX ARSDK3.8.3のNewAPIだと結果が返ってこない
	 * @return
	 */
	@Override
	public boolean requestCurrentDevice() {
		if (DEBUG) Log.d(TAG, "requestCurrentDevice:");
		if (mConnectDevice != null) {
			broadcastConnectedDevices();
			return false;
		}
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendDeviceRequestCurrentDevice();
		} else {
			if (DEBUG) Log.v(TAG, "requestCurrentDevice:not started");
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestCurrentDevice failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public DeviceInfo getCurrentDevice() {
		synchronized (mStateSync) {
			return mConnectDevice != null ? new DeviceInfo(mConnectDevice) : null;
		}
	}

	@Override
	public boolean connectToDevice(final DeviceInfo info) {
		if (DEBUG) Log.d(TAG, "connectToDevice:info=" + info);
		return connectToDevice(info.name());
	}

	/**
	 * 指定したデバイス名を持つ機体へ接続する
	 * @param deviceName
	 * @return true 接続できなかった
	 */
	public boolean connectToDevice(final String deviceName) {
		if (DEBUG) Log.v(TAG, "connectToDevice:deviceName=" + deviceName);
		if (TextUtils.isEmpty(deviceName)) return true;

		final DeviceInfo info = mDevices.containsKey(deviceName) ? mDevices.get(deviceName) : null;
		if ((info != null) && info.isConnected()) {
			if (DEBUG) Log.v(TAG, "connectToDevice:既に接続されている");
			synchronized (mStateSync) {
				mConnectDevice = info;
			}
			return false;
		}

		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;

		if (!mRequestConnectDevice) {
			mRequestConnectDevice = true;
			synchronized (mStateSync) {
				mConnectDevice = null;
			}
			try {
				result = mARDeviceController.getFeatureSkyController().sendDeviceConnectToDevice(deviceName);
				if (result == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
					// 正常に送信できた。onExtensionStateChangedが呼ばれるまで待機する
					if (DEBUG) Log.v(TAG, "connectToDevice:skyControllerConnectSent待機");
					mConnectDeviceSent.acquire();
					if (DEBUG) Log.v(TAG, "connectToDevice:" + mConnectDevice);
					synchronized (mStateSync) {
						if (mConnectDevice == null) {
							result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR_CANCELED;
						}
					}
				}
			} catch (final InterruptedException e) {
				// ignore
			} catch (final Exception e) {
				Log.w(TAG, e);
			} finally {
				mRequestConnectDevice = false;
			}
		}

		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#connectToDevice failed:" + result);
		}

		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * このクラス内で保持している接続状態をクリアするだけ。
	 * 実際の切断はしない
	 */
	@Override
	public void disconnectFrom() {
		if (DEBUG) Log.d(TAG, "disconnectFrom:");
		synchronized (mStateSync) {
			mConnectDevice = null;
		}
	}

	@Override
	public boolean setCoPilotingSource(final int _source) {
		final ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM source
			= ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM.getFromValue(_source % 2);
		if (DEBUG) Log.d(TAG, "setCoPilotingSource:source=" + source);
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendCoPilotingSetPilotingSource(source);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setCoPilotingSource failed:" + result);
		} else {
			mCoPilotingSource = _source;
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	private int mCoPilotingSource;
	@Override
	public int getCoPilotingSource() {
		return mCoPilotingSource;
	}

	@Override
	public boolean resetCameraOrientation() {
		if (DEBUG) Log.d(TAG, "resetCameraOrientation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureSkyController().sendCameraResetOrientation();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#resetCameraOrientation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestGamepadControls() {
		if (DEBUG) Log.d(TAG, "requestGamepadControls:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendGamepadInfosGetGamepadControls();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestGamepadControls failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestCurrentButtonMappings() {
		if (DEBUG) Log.d(TAG, "requestCurrentButtonMappings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendButtonMappingsGetCurrentButtonMappings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestCurrentButtonMappings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAvailableButtonMappings() {
		if (DEBUG) Log.d(TAG, "requestAvailableButtonMappings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendButtonMappingsGetAvailableButtonMappings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAvailableButtonMappings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setButtonMapping(final int key_id, final String mapping_uid) {
		if (DEBUG) Log.d(TAG, "setButtonMapping:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendButtonMappingsSetButtonMapping(key_id, mapping_uid);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setButtonMapping failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean resetButtonMapping() {
		if (DEBUG) Log.d(TAG, "resetButtonMapping:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendButtonMappingsDefaultButtonMapping();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#resetButtonMapping failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestCurrentAxisMappings() {
		if (DEBUG) Log.d(TAG, "requestCurrentAxisMappings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisMappingsGetCurrentAxisMappings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestCurrentAxisMappings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAvailableAxisMappings() {
		if (DEBUG) Log.d(TAG, "requestAvailableAxisMappings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisMappingsGetAvailableAxisMappings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAvailableAxisMappings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setAxisMapping(final int axis_id, final String mapping_uid) {
		if (DEBUG) Log.d(TAG, "setAxisMapping:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisMappingsSetAxisMapping(axis_id, mapping_uid);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setAxisMapping failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean resetAxisMapping() {
		if (DEBUG) Log.d(TAG, "resetAxisMapping:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisMappingsDefaultAxisMapping();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#resetAxisMapping failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestCurrentAxisFilters() {
		if (DEBUG) Log.d(TAG, "requestCurrentAxisFilters:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisFiltersGetCurrentAxisFilters();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestCurrentAxisFilters failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestPresetAxisFilters() {
		if (DEBUG) Log.d(TAG, "requestPresetAxisFilters:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisFiltersGetPresetAxisFilters();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestPresetAxisFilters failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setAxisFilter(final int axis_id, final String filter_uid_or_builder) {
		if (DEBUG) Log.d(TAG, "setAxisFilter:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisFiltersSetAxisFilter(axis_id, filter_uid_or_builder);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setAxisFilter failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean resetAxisFilter() {
		if (DEBUG) Log.d(TAG, "resetAxisFilter:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureSkyController().sendAxisFiltersDefaultAxisFilters();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#resetAxisFilter failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMagnetoCalibrationQualityUpdates(final boolean enable) {
		if (DEBUG) Log.d(TAG, "setMagnetoCalibrationQualityUpdates:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureSkyController().sendCalibrationEnableMagnetoCalibrationQualityUpdates(enable ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMagnetoCalibrationQualityUpdates failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestButtonEventsSettings() {
		if (DEBUG) Log.d(TAG, "requestButtonEventsSettings:");
		return false;
	}

//================================================================================
// ISkyController
//================================================================================
	@Override
	public boolean isGPSFixedSkyController() {
		return mSkyControllerGPS.fixed();
	}

	@Override
	public int getBatterySkyController() {
		return mSkyControllerStatus.getBattery();
	}

}
