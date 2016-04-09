package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
import com.parrot.arsdk.arcontroller.ARControllerException;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerListener;
import com.parrot.arsdk.arcontroller.ARFeatureCommon;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDevice;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryException;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.CommonStatus;
import com.serenegiant.arflight.DeviceConnectionListener;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Semaphore;

public abstract class DeviceControllerNewAPI implements IDeviceController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = DeviceControllerNewAPI.class.getSimpleName();

	private final WeakReference<Context> mWeakContext;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;
	protected ARDeviceController mARDeviceController;

	/**
	 * 切断待ちのためのセマフォ
	 */
	private final Semaphore disconnectSent = new Semaphore(0);
	protected volatile boolean mRequestCancel;
	/**
	 * 全ての設定取得待ちのためのセマフォ
	 * 初期値は0なのでonCommonSettingsStateAllSettingsChangedUpdateかcancelStart内でreleaseするまでは先に進まない
	 */
	protected final Semaphore cmdGetAllSettingsSent = new Semaphore(0);
	protected boolean isWaitingAllSettings;

	/**
	 * 全てのステータス取得待ちのためのセマフォ
	 * 初期値が0なのでonCommonCommonStateAllStatesChangedUpdateかcancelStart内でreleaseするまでは先に進まない
	 */
	protected final Semaphore cmdGetAllStatesSent = new Semaphore(0);
	protected boolean isWaitingAllStates;

	protected final Object mStateSync = new Object();
	private int mState = STATE_STOPPED;
	protected AttributeDevice mInfo;
	protected CommonStatus mStatus;
	private ARCONTROLLER_DEVICE_STATE_ENUM mDeviceState = ARCONTROLLER_DEVICE_STATE_ENUM.eARCONTROLLER_DEVICE_STATE_UNKNOWN_ENUM_VALUE;

	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();

	public DeviceControllerNewAPI(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		mWeakContext = new WeakReference<Context>(context);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
		mDeviceService = service;
		mNetConfig = net_config;
	}

	@Override
	public void release() {
		stop();
		mLocalBroadcastManager = null;
	}

	public Context getContext() {
		return mWeakContext.get();
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
		synchronized (mConnectionListeners) {
			mConnectionListeners.add(listener);
			callOnUpdateBattery(getBattery());
			callOnAlarmStateChangedUpdate(mStatus.getAlarm());
		}
	}

	/**
	 * 指定したコールバックリスナーを取り除く
	 * @param listener
	 */
	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mConnectionListeners) {
			mConnectionListeners.remove(listener);
		}
	}

	/**
	 * 接続時のコールバックを呼び出す
	 */
	protected void callOnConnect() {
		synchronized (mConnectionListeners) {
			for (final DeviceConnectionListener listener: mConnectionListeners) {
				if (listener != null) {
					try {
						listener.onConnect(this);
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
	protected void callOnDisconnect() {
		synchronized (mConnectionListeners) {
			for (final DeviceConnectionListener listener: mConnectionListeners) {
				if (listener != null) {
					try {
						listener.onDisconnect(this);
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
	protected void callOnAlarmStateChangedUpdate(final int state) {
		synchronized (mConnectionListeners) {
			for (final DeviceConnectionListener listener: mConnectionListeners) {
				if (listener != null) {
					try {
						listener.onAlarmStateChangedUpdate(this, state);
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
	protected void callOnUpdateBattery(final int percent) {
		synchronized (mConnectionListeners) {
			for (final DeviceConnectionListener listener: mConnectionListeners) {
				if (listener != null) {
					try {
						listener.onUpdateBattery(this, percent);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}
//================================================================================
	@Override
	public String getName() {
		final ARDiscoveryDeviceService device_service = getDeviceService();
		return device_service != null ? device_service.getName() : null;
	}

	@Override
	public String getProductName() {
		final ARDiscoveryDeviceService device_service = getDeviceService();
		return device_service != null ? ARDiscoveryService.getProductName(ARDiscoveryService.getProductFromProductID(device_service.getProductID())) : null;
	}

	@Override
	public int getProductId() {
		final ARDiscoveryDeviceService device_service = getDeviceService();
		return device_service != null ? device_service.getProductID() : 0;
	}

	public ARDISCOVERY_PRODUCT_ENUM getProductType() {
		return ARDiscoveryService.getProductFromProductID(getProductId());
	}

	@Override
	public ARDiscoveryDeviceService getDeviceService() {
		return mDeviceService;
	}

	@Override
	public ARNetworkALManager getNetALManager() {
		return null;
	}

	@Override
	public ARNetworkManager getNetManager() {
		return null;
	}

	@Override
	public ARNetworkConfig getNetConfig() {
		return mNetConfig;
	}

	@Override
	public String getSoftwareVersion() {
		return mInfo.productSoftware();
	}

	@Override
	public String getHardwareVersion() {
		return mInfo.productHardware();
	}

	@Override
	public String getSerial() {
		return mInfo.getSerial();
	}

	protected void setAlarm(final int alarm) {
		mStatus.setAlarm(alarm);
	}

	public int getAlarm() {
		return mStatus.getAlarm();
	}

	@Override
	public int getBattery() {
		return mStatus.getBattery();
	}

	protected void setBattery(final int percent) {
		mStatus.setBattery(percent);
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return mState;
		}
	}

	@Override
	public boolean start() {
		if (DEBUG) Log.v(TAG, "start:");

		synchronized (mStateSync) {
			if (mState != STATE_STOPPED) return false;
			mState = STATE_STARTING;
		}
		mRequestCancel = false;
		setAlarm(DroneStatus.ALARM_NON);

		boolean failed = startNetwork();

		if (!failed && (mARDeviceController != null)
			&& (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mState))) {

			final ARCONTROLLER_ERROR_ENUM error = mARDeviceController.start();
			failed = (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK);
			if (!failed) {
				internal_start();

				synchronized (mStateSync) {
					mState = STATE_STARTED;
				}
				onStarted();
			} else {
				if (failed) {
					Log.w(TAG, "failed to start ARController:err=" + error);
				}
			}
		}
		if (DEBUG) Log.v(TAG, "start:finished");

		return failed;
	}

	@Override
	public void cancelStart() {
		if (DEBUG) Log.v(TAG, "cancelStart:");
		if (!mRequestCancel) {
			mRequestCancel = true;
			internal_cancel_start();
			if (mARDeviceController != null) {
				mARDeviceController.stop();
				mARDeviceController = null;
			}
			cmdGetAllSettingsSent.release();
			cmdGetAllStatesSent.release();
			//TODO see : reset the semaphores or use signals
		}
		if (DEBUG) Log.v(TAG, "cancelStart:finished");
	}

	protected boolean startNetwork() {
		boolean failed = false;
		ARDiscoveryDevice discovery_device;
		try {
			discovery_device = new ARDiscoveryDevice();

			final Object device = mDeviceService.getDevice();
			if (device instanceof ARDiscoveryDeviceNetService) {
				final ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService)device;
				discovery_device.initWifi(getProductType(), netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
			} else if (device instanceof ARDiscoveryDeviceBLEService) {
				final ARDiscoveryDeviceBLEService bleDeviceService = (ARDiscoveryDeviceBLEService) device;
				discovery_device.initBLE(getProductType(), getContext().getApplicationContext(), bleDeviceService.getBluetoothDevice());
			}
		} catch (final ARDiscoveryException e) {
			Log.e(TAG, "Exception", e);
			Log.e(TAG, "Error: " + e.getError());
			discovery_device = null;
			failed = true;
		}
		if (discovery_device != null) {
			 try {
				final ARDeviceController deviceController = new ARDeviceController(discovery_device);
				deviceController.addListener(mDeviceControllerListener);
				mARDeviceController = deviceController;
			} catch (final ARControllerException e) {
				Log.e(TAG, "Exception", e);
				failed = true;
			}
		}
		return failed;
	}

	/** 接続開始時の追加処理 */
	protected void internal_start() {
	}

	/** 接続中断の追加処理 */
	protected void internal_cancel_start() {
	}

	/**
	 * DeviceControllerがstartした時の処理
	 */
	protected void onStarted() {
		if (DEBUG) Log.v(TAG, "onStarted:");
		// only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
		final Date currentDate = new Date(System.currentTimeMillis());
		sendDate(currentDate);
		sendTime(currentDate);
		isWaitingAllSettings = true;
		try {
			if (DEBUG) Log.v(TAG, "onStarted:requestAllSettings");
			if (requestAllSettings()) {
				try {
					if (DEBUG) Log.v(TAG, "onStarted:requestAllSettings:wait");
					//successful = cmdGetAllSettingsSent.tryAcquire (INITIAL_TIMEOUT_RETRIEVAL_MS, TimeUnit.MILLISECONDS);
					cmdGetAllSettingsSent.acquire();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		} finally {
			if (DEBUG) Log.v(TAG, "onStarted:requestAllSettings:finished");
			isWaitingAllSettings = false;
		}
		isWaitingAllStates = true;
		try {
			if (DEBUG) Log.v(TAG, "onStarted:requestAllStates");
			if (requestAllStates()) {
				try {
					if (DEBUG) Log.v(TAG, "onStarted:requestAllStates:wait");
					//successful = cmdGetAllStatesSent.tryAcquire (INITIAL_TIMEOUT_RETRIEVAL_MS, TimeUnit.MILLISECONDS);
					cmdGetAllStatesSent.acquire();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		} finally {
			if (DEBUG) Log.v(TAG, "onStarted:requestAllStates:finished");
			isWaitingAllStates = false;
		}
		callOnConnect();
		if (DEBUG) Log.v(TAG, "onStarted:finished");
	}

	/**
	 * 切断処理。子クラスで追加処理が必要であれば#internal_stopをOverrideすること
	 */
	@Override
	public final void stop() {
		if (DEBUG) Log.v(TAG, "stop:");

		synchronized (mStateSync) {
			if ((mState == STATE_STOPPED) || (mState != STATE_STARTED)) return;
			mState = STATE_STOPPING;
		}

		onStop();
		internal_stop();

		// ネットワーク接続をクリーンアップ
		stopNetwork();

		synchronized (mStateSync) {
			mState = STATE_STOPPED;
		}
		if (DEBUG) Log.v(TAG, "stop:終了");
	}

	protected void onStop() {
	}

	/** 切断の追加処理 */
	protected void internal_stop() {
	}

	protected void stopNetwork() {
		if (isActive()) {
			final ARCONTROLLER_ERROR_ENUM error = mARDeviceController.stop();
			final boolean failed = (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK);
			if (failed) {
				Log.w(TAG, "failed to stop ARController:err=" + error);
			}
			mARDeviceController.dispose();
			mARDeviceController = null;
		}
	}

	@Override
	public boolean isStarted() {
		synchronized (mStateSync) {
			return mState == STATE_STARTED;
		}
	}

	@Override
	public boolean isConnected() {
		synchronized (mStateSync) {
			return isActive() && (mState == STATE_STARTED) && (getAlarm() != DroneStatus.ALARM_DISCONNECTED);
		}
	}

	protected ARCONTROLLER_DEVICE_STATE_ENUM getDeviceState() {
		synchronized (mStateSync) {
			return mDeviceState;
		}
	}

	protected boolean isActive() {
		return (mARDeviceController != null)
			&& ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(getDeviceState());
	}

	private final ARDeviceControllerListener mDeviceControllerListener = new ARDeviceControllerListener() {
		@Override
		public void onStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

			DeviceControllerNewAPI.this.onStateChanged(deviceController, newState, error);
        }

		@Override
		public void onExtensionStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState,
			final ARDISCOVERY_PRODUCT_ENUM product, final String name, final ARCONTROLLER_ERROR_ENUM error) {

			DeviceControllerNewAPI.this.onExtensionStateChanged(deviceController, newState, product, name, error);
		}

		@Override
		public void onCommandReceived(final ARDeviceController deviceController,
			final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, final ARControllerDictionary elementDictionary) {

			if (elementDictionary != null) {
				final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
				if (args != null) {
					DeviceControllerNewAPI.this.onCommandReceived(deviceController, commandKey, args);
				}
			}
		}
    };

	/** mDeviceControllerListenerの下請け */
	protected void onStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

		if (DEBUG) Log.v(TAG, "onStateChanged:" + newState + ",error=" + error);
		synchronized (mStateSync) {
			mDeviceState = newState;
		}
		switch (newState) {
		case ARCONTROLLER_DEVICE_STATE_STOPPED: 	// (0, "device controller is stopped"),
			onDisconnect();
			break;
		case ARCONTROLLER_DEVICE_STATE_STARTING:	// (1, "device controller is starting"),
			break;
		case ARCONTROLLER_DEVICE_STATE_RUNNING:		// (2, "device controller is running"),
			onConnect();
			break;
		case ARCONTROLLER_DEVICE_STATE_PAUSED: 		// (3, "device controller is paused"),
			break;
		case ARCONTROLLER_DEVICE_STATE_STOPPING:	// (4, "device controller is stopping"),
			break;
		default:
			break;
		}
	}

	/** onStateChangedの下請け */
	protected void onConnect() {
		if (DEBUG) Log.d(TAG, "onConnect:");
		mARDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable((byte) 1);
	}

	/** onStateChangedの下請け */
	protected void onDisconnect() {
		if (DEBUG) Log.d(TAG, "onDisconnect:");
		setAlarm(DroneStatus.ALARM_DISCONNECTED);
		callOnAlarmStateChangedUpdate(DroneStatus.ALARM_DISCONNECTED);
		callOnDisconnect();
	}

	/** mDeviceControllerListenerの下請け */
	protected void onExtensionStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState,
		final ARDISCOVERY_PRODUCT_ENUM product,
		final String name, final ARCONTROLLER_ERROR_ENUM error) {

		if (DEBUG) Log.v(TAG, "onExtensionStateChanged:product=" + product + ",name=" + name + ",error" + error);
	}

	private void dumpArgs(final ARControllerArgumentDictionary<Object> args) {
		for (final String key: args.keySet()) {
			final Object obj = args.get(key);
			if (obj instanceof Integer) {
				Log.v(TAG, "dumpArgs:Integer arg" + obj);
			} else if (obj instanceof Long) {
				Log.v(TAG, "dumpArgs:Long arg" + obj);
			} else if (obj instanceof Double) {
				Log.v(TAG, "dumpArgs:Double arg" + obj);
			} else if (obj instanceof String) {
				Log.v(TAG, "dumpArgs:String arg" + obj);
			} else {
				Log.v(TAG, "dumpArgs:unknown arg" + obj);
			}
		}
	}

	/** mDeviceControllerListenerの下請け */
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		if (DEBUG) dumpArgs(args);
		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_COMMON:	// (157, "Key used to define the feature <code>Common</code>"),
			break;
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_NETWORKEVENT_DISCONNECTION:	// (158, "Key used to define the command <code>Disconnection</code> of class <code>NetworkEvent</code> in project <code>Common</code>"),
		{	// ネットワークから切断された時 FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_ALLSETTINGSCHANGED:	// (159, "Key used to define the command <code>AllSettingsChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// すべての設定を受信した時
			if (isWaitingAllSettings) {
				cmdGetAllSettingsSent.release();
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_RESETCHANGED:	// (160, "Key used to define the command <code>ResetChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 設定がリセットされた時 FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTNAMECHANGED:	// (161, "Key used to define the command <code>ProductNameChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 製品名を受信した時 FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTVERSIONCHANGED:	// (162, "Key used to define the command <code>ProductVersionChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALHIGHCHANGED:	// (163, "Key used to define the command <code>ProductSerialHighChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALLOWCHANGED:	// (164, "Key used to define the command <code>ProductSerialLowChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_COUNTRYCHANGED:	// (165, "Key used to define the command <code>CountryChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_AUTOCOUNTRYCHANGED:	// (166, "Key used to define the command <code>AutoCountryChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_ALLSTATESCHANGED:	// (167, "Key used to define the command <code>AllStatesChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 全てのステータスを受信した時
			if (isWaitingAllStates) {
				cmdGetAllStatesSent.release();
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED:	// (168, "Key used to define the command <code>BatteryStateChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// バッテリー残量が変化した時
			final int percent = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
			setBattery(percent);
			callOnUpdateBattery(percent);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGESTATELISTCHANGED:	// (169, "Key used to define the command <code>MassStorageStateListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED:	// (170, "Key used to define the command <code>MassStorageInfoStateListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTDATECHANGED:	// (171, "Key used to define the command <code>CurrentDateChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTTIMECHANGED:	// (172, "Key used to define the command <code>CurrentTimeChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED:	// (173, "Key used to define the command <code>MassStorageInfoRemainingListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_WIFISIGNALCHANGED:	// (174, "Key used to define the command <code>WifiSignalChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED:	// (175, "Key used to define the command <code>SensorsStatesListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_PRODUCTMODEL:	// (176, "Key used to define the command <code>ProductModel</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_COUNTRYLISTKNOWN:	// (177, "Key used to define the command <code>CountryListKnown</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_OVERHEATSTATE_OVERHEATCHANGED:	// (178, "Key used to define the command <code>OverHeatChanged</code> of class <code>OverHeatState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_OVERHEATSTATE_OVERHEATREGULATIONCHANGED:	// (179, "Key used to define the command <code>OverHeatRegulationChanged</code> of class <code>OverHeatState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_WIFISETTINGSSTATE_OUTDOORSETTINGSCHANGED:	// (180, "Key used to define the command <code>OutdoorSettingsChanged</code> of class <code>WifiSettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED:	// (181, "Key used to define the command <code>MavlinkFilePlayingStateChanged</code> of class <code>MavlinkState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED:	// (182, "Key used to define the command <code>MavlinkPlayErrorStateChanged</code> of class <code>MavlinkState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED:	// (183, "Key used to define the command <code>MagnetoCalibrationStateChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE:	// (184, "Key used to define the command <code>MagnetoCalibrationRequiredState</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED:	// (185, "Key used to define the command <code>MagnetoCalibrationAxisToCalibrateChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED:	// (186, "Key used to define the command <code>MagnetoCalibrationStartedChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED:	// (187, "Key used to define the command <code>CameraSettingsChanged</code> of class <code>CameraSettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED:	// (188, "Key used to define the command <code>AvailabilityStateChanged</code> of class <code>FlightPlanState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED:	// (189, "Key used to define the command <code>ComponentStateListChanged</code> of class <code>FlightPlanState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANEVENT_STARTINGERROREVENT:	// (190, "Key used to define the command <code>StartingErrorEvent</code> of class <code>FlightPlanEvent</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANEVENT_SPEEDBRIDLEEVENT:	// (191, "Key used to define the command <code>SpeedBridleEvent</code> of class <code>FlightPlanEvent</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_CONTROLLERLIBARCOMMANDSVERSION:	// (192, "Key used to define the command <code>ControllerLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_SKYCONTROLLERLIBARCOMMANDSVERSION:	// (193, "Key used to define the command <code>SkyControllerLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_DEVICELIBARCOMMANDSVERSION:	// (194, "Key used to define the command <code>DeviceLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_AUDIOSTATE_AUDIOSTREAMINGRUNNING:	// (195, "Key used to define the command <code>AudioStreamingRunning</code> of class <code>AudioState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_HEADLIGHTSSTATE_INTENSITYCHANGED:	// (196, "Key used to define the command <code>IntensityChanged</code> of class <code>HeadlightsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ANIMATIONSSTATE_LIST:	// (197, "Key used to define the command <code>List</code> of class <code>AnimationsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_SUPPORTEDACCESSORIESLISTCHANGED:	// (198, "Key used to define the command <code>SupportedAccessoriesListChanged</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_ACCESSORYCONFIGCHANGED:	// (199, "Key used to define the command <code>AccessoryConfigChanged</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_ACCESSORYCONFIGMODIFICATIONENABLED:	// (200, "Key used to define the command <code>AccessoryConfigModificationEnabled</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_MAXCHARGERATECHANGED:	// (201, "Key used to define the command <code>MaxChargeRateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_CURRENTCHARGESTATECHANGED:	// (202, "Key used to define the command <code>CurrentChargeStateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_LASTCHARGERATECHANGED:	// (203, "Key used to define the command <code>LastChargeRateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_CHARGINGINFO:	// (204, "Key used to define the command <code>ChargingInfo</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED:	// (205, "Key used to define the command <code>RunIdChanged</code> of class <code>RunState</code> in project <code>Common</code>"),
		{
			// if event received is the run id
			final String runID = (String) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED_RUNID);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG:	// (206, "Key used to define the feature <code>CommonDebug</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_STATSEVENT_SENDPACKET:	// (207, "Key used to define the command <code>SendPacket</code> of class <code>StatsEvent</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_DEBUGSETTINGSSTATE_INFO:	// (208, "Key used to define the command <code>Info</code> of class <code>DebugSettingsState</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_DEBUGSETTINGSSTATE_LISTCHANGED:	// (209, "Key used to define the command <code>ListChanged</code> of class <code>DebugSettingsState</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			break;
		}
		}
	}

//********************************************************************************
// データ送受信関係
//********************************************************************************
	private static final SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat formattedTime = new SimpleDateFormat("'T'HHmmssZZZ", Locale.getDefault());
	@Override
	public boolean sendDate(final Date currentDate) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendCommonCurrentDate(formattedDate.format(currentDate));
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendDate failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendTime(final Date currentTime) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendCommonCurrentTime(formattedTime.format(currentTime));
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendTime failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAllSettings() {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsAllSettings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAllSettings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAllStates() {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendCommonAllStates();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAllStates failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * ネットワーク切断要求
	 * @return
	 */
	public boolean sendNetworkDisconnect() {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendNetworkDisconnect();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setCountryCode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	protected boolean setCountryCode(final String code) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsCountry(code);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setCountryCode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	protected boolean setAutomaticCountry(final boolean auto) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsAutoCountry(auto ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setAutomaticCountry failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}
}
