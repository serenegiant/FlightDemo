package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATE_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_SETTINGSSTATE_PRODUCTVARIANTCHANGED_VARIANT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED_STATUS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_SKYCONTROLLER_WIFISTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARFeatureSkyController;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.CommonStatus;
import com.serenegiant.arflight.DeviceConnectionListener;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.IBridgeController;
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

import static com.serenegiant.arflight.ARFlightConst.ARFLIGHT_ACTION_DEVICE_LIST_CHANGED;
import static com.serenegiant.arflight.ARFlightConst.ARFLIGHT_EXTRA_DEVICE_LIST;

public class SkyControllerNewAPI extends FlightControllerBebopNewAPI implements ISkyController, IBridgeController, IVideoStreamController, IWiFiController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = SkyControllerNewAPI.class.getSimpleName();

	/** 接続中の機体情報, FIXME 排他制御が必要 */
	private DeviceInfo mConnectDevice;
	/**
	 * 機体接続待ちのためのセマフォ
	 */
	private final Semaphore mConnectDeviceSent = new Semaphore(0);
	protected volatile boolean mRequestConnectDevice;

	protected CommonStatus mSkyControllerStatus = new CommonStatus();
	public final AttributeGPS mSkyControllerGPS = new AttributeGPS();
	private final List<SkyControllerListener> mListeners = new ArrayList<SkyControllerListener>();

	public SkyControllerNewAPI(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
	}

	/** 接続開始時の追加処理 */
	protected void internal_start() {
		// onExtensionStateChangedが呼ばれるまで待機する
		if (!mRequestConnectDevice) {
			try {
				mRequestConnectDevice = true;
				if (DEBUG) Log.v(TAG, "skyControllerConnectSent待機");
				mConnectDeviceSent.acquire();
			} catch (final InterruptedException e) {
			} finally {
				mRequestConnectDevice = false;
			}
		}
		super.internal_start();
	}

	protected void internal_cancel_start() {
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
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
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
		}
	}

	/**
	 * onExtensionStateChangedの下請け
	 * これはスカイコントローラーが機体から切断された時に呼ばれる
	 */
	protected void onExtensionDisconnect() {
		if (DEBUG) Log.d(TAG, "onExtensionDisconnect:");
		if (mRequestConnectDevice) {
			mConnectDeviceSent.release();
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
		{	// スカイコントローラーが検出したアクセスポイント一覧を取得した時
			// 検出しているアクセスポイント1つ毎に1回呼び出される
			// requestWifiListに対する応答, 自動的には来ない
			final String bssid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_BSSID);
			final String ssid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SSID);
			final boolean secured = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SECURED) != 0;
			final boolean saved = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_SAVED) != 0;
			final int rssi = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_RSSI);
			final int frequency = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_WIFILIST_FREQUENCY);

			if (DEBUG) Log.v(TAG, String.format("onWifiListUpdate:bssid=%s, ssid=%s, secured=%d, saved=%d, rssi=%d, frequency=%d",
				bssid, ssid, secured, saved, rssi, frequency));
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_WIFISTATE_CONNEXIONCHANGED:	// (121, "Key used to define the command <code>ConnexionChanged</code> of class <code>WifiState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーとWiFiアクセスポイント間の接続状態が変化した時のコールバックリスナー
			// requestAllStatesでも来る
			// requestCurrentWiFiを呼んでも来る
			// 何故か1回の接続で3回来るのと切断された時には来ないみたい
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
			// requestCurrentDeviceを呼ぶと来る
			// requestCurrentWiFiを呼んでも来る
			// AccessPointのSSIDやチャネル等を変更しても来る
			// たぶん最初に見つかった機体には勝手に接続しに行きよる
			// ARCommandSkyControllerWifiStateConnexionChangedListenerのコールバックメソッドよりも後に来る

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
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SETTINGSSTATE_RESETCHANGED:	// (128, "Key used to define the command <code>ResetChanged</code> of class <code>SettingsState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーの設定がリセットされた時
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
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED:	// (132, "Key used to define the command <code>BatteryChanged</code> of class <code>SkyControllerState</code> in project <code>SkyControllerNewAPI</code>"),
		{	// スカイコントローラーのバッテリー残量が変化した時
			final int percent = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_SKYCONTROLLERSTATE_BATTERYCHANGED_PERCENT);
			mSkyControllerStatus.setBattery(percent);
			callOnUpdateBattery(percent);
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
			final ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM type
				= ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE)
			);
			final int id = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_ID);
			final String name = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_NAME);

			if (DEBUG) Log.v(TAG, "onStateGamepadControlUpdate:type=" + type + ", id=" + id + ", name=" + name);
			switch (type) {
			case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_AXIS:	// 0, スティック
			case ARCOMMANDS_SKYCONTROLLER_GAMEPADINFOSSTATE_GAMEPADCONTROL_TYPE_BUTTON:	// 1, ボタン, スティックの押し込みを含む
				break;
			}
			// FIXME GamepadControlの追加処理
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
			final int key_id = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS_KEY_ID);
			final String mapping_uid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_CURRENTBUTTONMAPPINGS_MAPPING_UID);
			if (DEBUG) Log.v(TAG, "onCurrentButtonMappingsUpdate:key_id=" + key_id + ", mapping_uid=" + mapping_uid);
			// FIXME ボタン割り当て設定追加
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
			/** ボタンの識別コード */
			final String mapping_uid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS_MAPPING_UID);
			/** ボタン名 */
			final String name = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_BUTTONMAPPINGSSTATE_AVAILABLEBUTTONMAPPINGS_NAME);
			if (DEBUG) Log.v(TAG, "onAvailableButtonMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
			// FIXME 使用可能ボタン割り当て設定に追加
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
			final int axis_id = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS_AXIS_ID);
			final String mapping_uid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_CURRENTAXISMAPPINGS_MAPPING_UID);
			if (DEBUG) Log.v(TAG, "onCurrentAxisMappingsUpdate:axis_id=" + axis_id + ", mapping_uid=" + mapping_uid);
			// FIXME ジョイスティック割当設定を追加
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
			final String mapping_uid = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS_MAPPING_UID);
			final String name = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISMAPPINGSSTATE_AVAILABLEAXISMAPPINGS_NAME);
			if (DEBUG) Log.v(TAG, "onAvailableAxisMappingsUpdate:mapping_uid=" + mapping_uid + ", name=" + name);
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
			/** 軸番号: 0..n */
	 		final int axis_id = (Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS_AXIS_ID);
	 		/** "ARMF"ってのが来る */
			final String filter_uid_or_builder = (String)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_AXISFILTERSSTATE_CURRENTAXISFILTERS_FILTER_UID_OR_BUILDER);
			if (DEBUG) Log.v(TAG, "onCurrentAxisFiltersUpdate:axis_id=" + axis_id + ", filter_uid_or_builder=" + filter_uid_or_builder);
			// FIXME ジョイスティック入力フィルター設定追加
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
			final ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM source
				= ARCOMMANDS_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureSkyController.ARCONTROLLER_DICTIONARY_KEY_SKYCONTROLLER_COPILOTINGSTATE_PILOTINGSOURCE_SOURCE)
			);
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
// 機体との接続状態の管理
//================================================================================
	private final Map<String, DeviceInfo> mDevices = new HashMap<String, DeviceInfo>();

	private void updateConnectionState(
		final ARCOMMANDS_SKYCONTROLLER_DEVICESTATE_CONNEXIONCHANGED_STATUS_ENUM status,
		final String deviceName, final int deviceProductID) {

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
		if (DEBUG) Log.v(TAG, "callOnConnect:");
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
		if (DEBUG) Log.v(TAG, "callOnDisconnect:");
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
		if (DEBUG) Log.v(TAG, "callOnUpdateBattery:" + percent);
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
//================================================================================
// IBridgeController
//================================================================================
	@Override
	public ARNetworkConfig createBridgeNetConfig() {
		return null;
	}

	@Override
	public VideoStreamDelegater getVideoStreamDelegater() {
		return null;
	}

	@Override
	public boolean connectTo(final DeviceInfo info) {
		return connectToDevice(info.name());
	}

	@Override
	public void disconnectFrom() {

	}

	@Override
	public DeviceInfo connectDeviceInfo() {
		return mConnectDevice;
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
		final ARCONTROLLER_ERROR_ENUM result = mARDeviceController.getFeatureSkyController().sendDeviceConnectToDevice(deviceName);

		if (result == ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			// 正常に送信できた。接続待ちする
			mRequestConnectDevice = true;
			try {
				mConnectDeviceSent.acquire();
			} catch (final InterruptedException e) {
				// ignore
			}
		} else {
			Log.e(TAG, "Failed to send connectToDevice command.");
		}

		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
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

//	public ARCONTROLLER_ERROR_ENUM sendWifiRequestWifiList ()
//	public ARCONTROLLER_ERROR_ENUM sendWifiRequestCurrentWifi ()
//	public ARCONTROLLER_ERROR_ENUM sendWifiConnectToWifi (String _bssid, String _ssid, String _passphrase)
//	public ARCONTROLLER_ERROR_ENUM sendWifiForgetWifi (String _ssid)
//	public ARCONTROLLER_ERROR_ENUM sendWifiWifiAuthChannel ()
//	public ARCONTROLLER_ERROR_ENUM sendDeviceRequestDeviceList ()
//	public ARCONTROLLER_ERROR_ENUM sendDeviceRequestCurrentDevice ()
//	public ARCONTROLLER_ERROR_ENUM sendSettingsAllSettings ()
//	public ARCONTROLLER_ERROR_ENUM sendSettingsReset ()
//	public ARCONTROLLER_ERROR_ENUM sendCommonAllStates ()
//	public ARCONTROLLER_ERROR_ENUM sendAccessPointSettingsAccessPointSSID (String _ssid)
//	public ARCONTROLLER_ERROR_ENUM sendAccessPointSettingsAccessPointChannel (byte _channel)
//	public ARCONTROLLER_ERROR_ENUM sendAccessPointSettingsWifiSelection (ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_TYPE_ENUM _type, ARCOMMANDS_SKYCONTROLLER_ACCESSPOINTSETTINGS_WIFISELECTION_BAND_ENUM _band, byte _channel)
//	public ARCONTROLLER_ERROR_ENUM sendCameraResetOrientation ()
//	public ARCONTROLLER_ERROR_ENUM sendGamepadInfosGetGamepadControls ()
//	public ARCONTROLLER_ERROR_ENUM sendButtonMappingsGetCurrentButtonMappings ()
//	public ARCONTROLLER_ERROR_ENUM sendButtonMappingsGetAvailableButtonMappings ()
//	public ARCONTROLLER_ERROR_ENUM sendButtonMappingsSetButtonMapping (int _key_id, String _mapping_uid)
//	public ARCONTROLLER_ERROR_ENUM sendButtonMappingsDefaultButtonMapping ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisMappingsGetCurrentAxisMappings ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisMappingsGetAvailableAxisMappings ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisMappingsSetAxisMapping (int _axis_id, String _mapping_uid)
//	public ARCONTROLLER_ERROR_ENUM sendAxisMappingsDefaultAxisMapping ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisFiltersGetCurrentAxisFilters ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisFiltersGetPresetAxisFilters ()
//	public ARCONTROLLER_ERROR_ENUM sendAxisFiltersSetAxisFilter (int _axis_id, String _filter_uid_or_builder)
//	public ARCONTROLLER_ERROR_ENUM sendAxisFiltersDefaultAxisFilters ()
//	public ARCONTROLLER_ERROR_ENUM sendCoPilotingSetPilotingSource (ARCOMMANDS_SKYCONTROLLER_COPILOTING_SETPILOTINGSOURCE_SOURCE_ENUM _source);
//	public ARCONTROLLER_ERROR_ENUM sendCalibrationEnableMagnetoCalibrationQualityUpdates (byte _enable);

}
