package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.parrot.arsdk.arcommands.*;
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
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.utils.HandlerThreadHandler;

import java.lang.ref.WeakReference;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public abstract class DeviceControllerNewAPI implements IDeviceController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private String TAG = "DeviceControllerNewAPI:" + getClass().getSimpleName();

	private final WeakReference<Context> mWeakContext;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;
	protected ARDeviceController mARDeviceController;
	protected Handler mAsyncHandler;

	/**
	 * 接続待ちのためのセマフォ
	 */
	private final Semaphore connectSent = new Semaphore(0);
	protected volatile boolean mRequestConnect;
	/**
	 * 切断待ちのためのセマフォ
	 */
	private final Semaphore disconnectSent = new Semaphore(0);
	protected volatile boolean mRequestDisconnect;

	protected final Object mStateSync = new Object();
	private int mState = STATE_STOPPED;
	protected AttributeDevice mInfo;
	protected CommonStatus mStatus;
	protected ARCONTROLLER_DEVICE_STATE_ENUM mDeviceState = ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED;
	protected ARCONTROLLER_DEVICE_STATE_ENUM mExtensionState = ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED;

	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();

	public DeviceControllerNewAPI(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		if (DEBUG) Log.v(TAG, "コンストラクタ:");
		mWeakContext = new WeakReference<Context>(context);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(context);
		mDeviceService = service;
		mNetConfig = net_config;
		mAsyncHandler = HandlerThreadHandler.createHandler(TAG);
	}

	@Override
	public void finalize() throws Throwable {
		if (DEBUG) Log.v (TAG, "finalize:");
		release();
		super.finalize();
	}

	@Override
	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		stop();
		mLocalBroadcastManager = null;
		if (mAsyncHandler != null) {
			try {
				mAsyncHandler.getLooper().quit();
			} catch (final Exception e) {
			}
			mAsyncHandler = null;
		}
	}

	public boolean isNewAPI() {
		return true;
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
		if (DEBUG) Log.v(TAG, "addListener:" + listener);
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
		if (DEBUG) Log.v(TAG, "removeListener:" + listener);
		synchronized (mConnectionListeners) {
			mConnectionListeners.remove(listener);
		}
	}

	/**
	 * 接続時のコールバックを呼び出す
	 */
	protected void callOnConnect() {
		if (DEBUG) Log.v(TAG, "callOnConnect:");
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
		if (DEBUG) Log.v(TAG, "callOnDisconnect:");
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
		if (DEBUG) Log.v(TAG, "callOnAlarmStateChangedUpdate:" + state);
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
		if (DEBUG) Log.v(TAG, "callOnUpdateBattery:" + percent);
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
		mRequestDisconnect = false;
		setAlarm(DroneStatus.ALARM_NON);

		boolean failed = startNetwork();
		ARCONTROLLER_ERROR_ENUM error = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;

		if (!failed && (mARDeviceController != null)
			&& (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mDeviceState))) {

			mRequestConnect = true;
			try {
				if (DEBUG) Log.v(TAG, "start:ARDeviceController#start");
				error = mARDeviceController.start();
				failed = (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK);
				if (!failed) {
					onStarting();
					if (DEBUG) Log.v(TAG, "start:connectSent待機");
					connectSent.acquire();
					synchronized (mStateSync) {
						mState = STATE_STARTED;
					}
					onStarted();
					callOnConnect();
				}
			} catch (final InterruptedException e) {
				//
			} finally {
				mRequestConnect = false;
			}
		}
		if (failed) {
			Log.w(TAG, "failed to start ARController:err=" + error);
			try {
				if (mARDeviceController != null) {
					mARDeviceController.dispose();
					mARDeviceController = null;
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			synchronized (mStateSync) {
				mState = STATE_STOPPED;
			}
			setAlarm(DroneStatus.ALARM_DISCONNECTED);
		}
		if (DEBUG) Log.v(TAG, "start:終了");

		return failed;
	}

	@Override
	public void cancelStart() {
		if (DEBUG) Log.v(TAG, "cancelStart:");
		if (!mRequestDisconnect && mRequestConnect) {
			mRequestDisconnect = true;
			try {
				internal_cancel_start();
				if (mARDeviceController != null) {
					mARDeviceController.stop();
					mARDeviceController = null;
				}
				if (DEBUG) Log.v(TAG, "start:disconnectSent待機");
				disconnectSent.acquire();
			} catch (InterruptedException e) {
			} finally {
				mRequestDisconnect = false;
			}
			connectSent.release();
			//TODO see : reset the semaphores or use signals
		}
		if (DEBUG) Log.v(TAG, "cancelStart:終了");
	}

	/**
	 * デバイスへの接続開始処理
	 * @return
	 */
	protected boolean startNetwork() {
		if (DEBUG) Log.v(TAG, "startNetwork:");
		boolean failed = false;
		ARDiscoveryDevice discovery_device;
		try {
			discovery_device = new ARDiscoveryDevice();

			final Object device = mDeviceService.getDevice();
			if (device instanceof ARDiscoveryDeviceNetService) {
				if (DEBUG) Log.v(TAG, "startNetwork:ARDiscoveryDeviceNetService");
				final ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService)device;
				discovery_device.initWifi(getProductType(), netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
			} else if (device instanceof ARDiscoveryDeviceBLEService) {
				if (DEBUG) Log.v(TAG, "startNetwork:ARDiscoveryDeviceBLEService");
				final ARDiscoveryDeviceBLEService bleDeviceService = (ARDiscoveryDeviceBLEService) device;
				discovery_device.initBLE(getProductType(), getContext().getApplicationContext(), bleDeviceService.getBluetoothDevice());
			}
		} catch (final ARDiscoveryException e) {
			Log.e(TAG, "err=" + e.getError(), e);
			discovery_device = null;
			failed = true;
		}
		if (discovery_device != null) {
			if (DEBUG) Log.v(TAG, "startNetwork:ARDeviceController生成");
			ARDeviceController deviceController = null;
			try {
				deviceController = new ARDeviceController(discovery_device);
				deviceController.addListener(mDeviceControllerListener);
				mARDeviceController = deviceController;
			} catch (final ARControllerException e) {
				Log.e(TAG, "err=" + e.getError(), e);
				failed = true;
				if (deviceController != null) {
					try {
						deviceController.dispose();
					} catch (final Exception e2) {
						Log.w(TAG, e2);
					}
				}
			}
		} else {
			Log.w(TAG, "startNetwork:ARDiscoveryDeviceを初期化出来なかった");
		}
		if (DEBUG) Log.v(TAG, "startNetwork:終了" + failed);
		return failed;
	}

	/** 接続開始中の追加処理(この時点ではまだ機体との接続&ステータス取得が終わってない可能性がある, onConnectコールバックを呼ぶ前) */
	protected void onStarting() {
		if (DEBUG) Log.v(TAG, "onStarting:");
	}

	/** 接続中断の追加処理 */
	protected void internal_cancel_start() {
		if (DEBUG) Log.v(TAG, "internal_cancel_start:");
	}

	/** DeviceControllerがstartした時の処理(mARDeviceControllerは有効, onConnectを呼び出す直前) */
	protected void onStarted() {
		if (DEBUG) Log.v(TAG, "onStarted:");
		// only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
		final Date currentDate = new Date(System.currentTimeMillis());
		sendDate(currentDate);
		sendTime(currentDate);
		if (DEBUG) Log.v(TAG, "onStarted:終了");
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

		onBeforeStop();

		if (!mRequestDisconnect && (mARDeviceController != null)
			&& !ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_STOPPED.equals(mDeviceState)) {
			mRequestDisconnect = true;
			try {
				onStopping();
				if (DEBUG) Log.v(TAG, "stop:ARDeviceController#stop");
				final ARCONTROLLER_ERROR_ENUM error = mARDeviceController.stop();
				final boolean failed = (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK);
				if (failed) {
					Log.w(TAG, "failed to stop ARController:err=" + error);
				}
				if (DEBUG) Log.v(TAG, "stop:disconnectSent待機");
				disconnectSent.acquire();
			} catch (final InterruptedException e) {

			} finally {
				mRequestDisconnect = false;
			}
		}

		// ネットワーク接続をクリーンアップ
		stopNetwork();

		synchronized (mStateSync) {
			mState = STATE_STOPPED;
		}
		if (DEBUG) Log.v(TAG, "stop:終了");
	}

	/** 切断前の処理(接続中ならまだmARDeviceControllerは有効) */
	protected void onBeforeStop() {
		if (DEBUG) Log.v(TAG, "onBeforeStop:");
	}

	/** 切断中の追加処理(接続中でなければ呼び出されない, mARDeviceControllerは有効) */
	protected void onStopping() {
		if (DEBUG) Log.v(TAG, "onStopping:");
	}

	/** 切断処理(mARDeviceControllerは既にstopしているので無効) */
	protected void stopNetwork() {
		if (DEBUG) Log.v(TAG, "stopNetwork:");
		if (mARDeviceController != null) {
			mARDeviceController.dispose();
			mARDeviceController = null;
		}
	}

	@Override
	public boolean isStarted() {
		synchronized (mStateSync) {
			return (mARDeviceController != null)
				&& ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(getDeviceState())
				&& (mState == STATE_STARTED);
		}
	}

	/***
	 * 機体と接続しているかどうか
	 * 直接接続の時は#isStartedと同じ
	 * @return
	 */
	public boolean isConnected() {
		return isStarted();
	}

	protected ARCONTROLLER_DEVICE_STATE_ENUM getDeviceState() {
		synchronized (mStateSync) {
			return mDeviceState;
		}
	}

	protected ARCONTROLLER_DEVICE_STATE_ENUM getExtensionDeviceState() {
		synchronized (mStateSync) {
			return mExtensionState;
		}
	}

	private final ARDeviceControllerListener mDeviceControllerListener = new ARDeviceControllerListener() {
		@Override
		public void onStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

			try {
				DeviceControllerNewAPI.this.onStateChanged(deviceController, newState, error);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
        }

		@Override
		public void onExtensionStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState,
			final ARDISCOVERY_PRODUCT_ENUM product, final String name, final ARCONTROLLER_ERROR_ENUM error) {

			try {
				DeviceControllerNewAPI.this.onExtensionStateChanged(deviceController, newState, product, name, error);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}

		@Override
		public void onCommandReceived(final ARDeviceController deviceController,
			final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, final ARControllerDictionary elementDictionary) {

			if (elementDictionary != null) {
				final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
				if (args != null) {
					try {
						DeviceControllerNewAPI.this.onCommandReceived(deviceController, commandKey, args);
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}
		}
    };

	/**
	 * onStateChangedから呼び出される
	 * @param newState
	 */
	protected void setState(final ARCONTROLLER_DEVICE_STATE_ENUM newState) {
		synchronized (mStateSync) {
			mDeviceState = newState;
		}
	}

	/** mDeviceControllerListenerの下請け */
	protected void onStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

		if (DEBUG) Log.v(TAG, "onStateChanged:state=" + newState + ",error=" + error);
		setState(newState);
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
		if (mRequestConnect) {
			connectSent.release();
		}
	}

	/** onStateChangedの下請け */
	protected void onDisconnect() {
		if (DEBUG) Log.d(TAG, "onDisconnect:");
		setAlarm(DroneStatus.ALARM_DISCONNECTED);
		if (mRequestConnect) {
			connectSent.release();
		}
		if (mRequestDisconnect) {
			disconnectSent.release();
		}
		callOnAlarmStateChangedUpdate(DroneStatus.ALARM_DISCONNECTED);
		callOnDisconnect();
	}

	/**
	 * onExtensionStateChangedから呼び出される
	 * @param newState
	 */
	protected void setExtensionState(final ARCONTROLLER_DEVICE_STATE_ENUM newState) {
		synchronized (mStateSync) {
			mExtensionState = newState;
		}
	}

	/** mDeviceControllerListenerの下請け */
	protected void onExtensionStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState,
		final ARDISCOVERY_PRODUCT_ENUM product,
		final String name, final ARCONTROLLER_ERROR_ENUM error) {

		if (DEBUG) Log.v(TAG, "onExtensionStateChanged:state=" + newState + ",product=" + product + ",name=" + name + ",error=" + error);
		setExtensionState(newState);
		switch (newState) {
		case ARCONTROLLER_DEVICE_STATE_STOPPED: 	// (0, "device controller is stopped"),
			onExtensionDisconnect();
			break;
		case ARCONTROLLER_DEVICE_STATE_STARTING:	// (1, "device controller is starting"),
			break;
		case ARCONTROLLER_DEVICE_STATE_RUNNING:		// (2, "device controller is running"),
			onExtensionConnect();
			break;
		case ARCONTROLLER_DEVICE_STATE_PAUSED: 		// (3, "device controller is paused"),
			break;
		case ARCONTROLLER_DEVICE_STATE_STOPPING:	// (4, "device controller is stopping"),
			break;
		default:
			break;
		}
	}

	/** onExtensionStateChangedの下請け */
	protected void onExtensionConnect() {
		if (DEBUG) Log.d(TAG, "onExtensionConnect:");
	}

	/** onExtensionStateChangedの下請け */
	protected void onExtensionDisconnect() {
		if (DEBUG) Log.d(TAG, "onExtensionDisconnect:");
	}

	/** mDeviceControllerListenerの下請け */
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_COMMON:	// (157, "Key used to define the feature <code>Common</code>"),
			if (DEBUG) Log.v(TAG, "COMMON:");
			break;
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_NETWORKEVENT_DISCONNECTION:	// (158, "Key used to define the command <code>Disconnection</code> of class <code>NetworkEvent</code> in project <code>Common</code>"),
		{	// ネットワークから切断された時 FIXME 未実装
			if (DEBUG) Log.v(TAG, "NETWORKEVENT_DISCONNECTION:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_ALLSETTINGSCHANGED:	// (159, "Key used to define the command <code>AllSettingsChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// すべての設定を受信した時
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_RESETCHANGED:	// (160, "Key used to define the command <code>ResetChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 設定がリセットされた時 FIXME 未実装
			if (DEBUG) Log.v(TAG, "SETTINGSSTATE_RESETCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTNAMECHANGED:	// (161, "Key used to define the command <code>ProductNameChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 製品名を受信した時
			final String name = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTNAMECHANGED_NAME);
			mInfo.setProductName(name);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTVERSIONCHANGED:	// (162, "Key used to define the command <code>ProductVersionChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 製品バージョンを受信した時
			final String software = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTVERSIONCHANGED_SOFTWARE);
			final String hardware = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTVERSIONCHANGED_HARDWARE);
			mInfo.setProduct(software, hardware);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALHIGHCHANGED:	// (163, "Key used to define the command <code>ProductSerialHighChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// シリアル番号の上位を受信した時
			final String high = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALHIGHCHANGED_HIGH);
			mInfo.setSerialHigh(high);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALLOWCHANGED:	// (164, "Key used to define the command <code>ProductSerialLowChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// シリアル番号の下位を受信した時
			final String low = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_PRODUCTSERIALLOWCHANGED_LOW);
			mInfo.setSerialLow(low);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_COUNTRYCHANGED:	// (165, "Key used to define the command <code>CountryChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 国コードを受信した時
			final String code = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_COUNTRYCHANGED_CODE);
			setCountryCode(code);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_AUTOCOUNTRYCHANGED:	// (166, "Key used to define the command <code>AutoCountryChanged</code> of class <code>SettingsState</code> in project <code>Common</code>"),
		{	// 自動国選択設定が変更された時
			final boolean auto = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_SETTINGSSTATE_AUTOCOUNTRYCHANGED_AUTOMATIC) != 0;
			setAutomaticCountry(auto);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_ALLSTATESCHANGED:	// (167, "Key used to define the command <code>AllStatesChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 全てのステータスを受信した時
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
		{	// 機体内のストレージ一覧が変化した時
			final String name = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGESTATELISTCHANGED_NAME);
			if (!TextUtils.isEmpty(name)) {
				final int mass_storage_id = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGESTATELISTCHANGED_MASS_STORAGE_ID);
				onCommonStateMassStorageStateListChanged(mass_storage_id, name);
			} else {
				if (DEBUG) Log.v(TAG, "onCommonStateMassStorageStateListChanged:null");
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED:	// (170, "Key used to define the command <code>MassStorageInfoStateListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// ストレージの状態が変化した時
			try {
				final int mass_storage_id = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_MASS_STORAGE_ID);
				final long size = (Long)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_SIZE);
				final long used_size = (Long)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_USED_SIZE);
				final boolean plugged = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_PLUGGED) != 0;
				final boolean full = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_FULL) != 0;
				final boolean internal = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOSTATELISTCHANGED_INTERNAL) != 0;

				onCommonStateMassStorageInfoStateListChanged(mass_storage_id, size, used_size, plugged, full, internal);
			} catch (final Exception e) {
				if (DEBUG) Log.v(TAG, "onCommonStateMassStorageInfoStateListChanged:null");
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTDATECHANGED:	// (171, "Key used to define the command <code>CurrentDateChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 日付が変更された時
			final String date = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTDATECHANGED_DATE);
			if (DEBUG) Log.v(TAG, "COMMONSTATE_CURRENTDATECHANGED:" + date);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTTIMECHANGED:	// (172, "Key used to define the command <code>CurrentTimeChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 時刻が変更された時
			final String time = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_CURRENTTIMECHANGED_TIME);
			if (DEBUG) Log.v(TAG, "COMMONSTATE_CURRENTTIMECHANGED:" + time);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED:	// (173, "Key used to define the command <code>MassStorageInfoRemainingListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// ストレージの空き容量が変化した時
			final long free_space = (Long)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED_FREE_SPACE);
   			final long rec_time = (Long)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED_REC_TIME);
			final long photo = (Long)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED_PHOTO_REMAINING);
			if (DEBUG) Log.v(TAG, "COMMONSTATE_MASSSTORAGEINFOREMAININGLISTCHANGED:free_space="
				+ free_space + ",rec_time=" + rec_time + ",photo=" + photo);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_WIFISIGNALCHANGED:	// (174, "Key used to define the command <code>WifiSignalChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// WiFiの信号強度が変化した時
			final int rssi = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_WIFISIGNALCHANGED_RSSI);
			onCommonStateWifiSignalChangedUpdate(rssi);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED:	// (175, "Key used to define the command <code>SensorsStatesListChanged</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// センサー状態リストが変化した時
			try {
				final int _sensor = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME);
				final ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM sensor = ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM.getFromValue(_sensor);
				final int state = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORSTATE);

				switch (sensor.getValue()) {
				case IFlightController.SENSOR_IMU: // 0
				case IFlightController.SENSOR_BAROMETER:	// 1
				case IFlightController.SENSOR_ULTRASOUND: // 2
				case IFlightController.SENSOR_GPS: // 3
				case IFlightController.SENSOR_MAGNETOMETER: // 4
				case IFlightController.SENSOR_VERTICAL_CAMERA: // 5
				}
				if (DEBUG) Log.v(TAG, String.format("SensorsStatesListChangedUpdate:%d=%d", sensor.getValue(), state));
			} catch (final Exception e) {
				if (DEBUG) Log.v(TAG, "SensorsStatesListChangedUpdate:null");
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_PRODUCTMODEL:	// (176, "Key used to define the command <code>ProductModel</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 製品のモデルを受信した時
			final int _model = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_PRODUCTMODEL_MODEL);
			if (DEBUG) Log.v(TAG, "COMMONSTATE_PRODUCTMODEL:model=" + _model);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_COUNTRYLISTKNOWN:	// (177, "Key used to define the command <code>CountryListKnown</code> of class <code>CommonState</code> in project <code>Common</code>"),
		{	// 指定可能な国コードリストを取得した時
			final String knownCountries = (String)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_COUNTRYLISTKNOWN_COUNTRYCODES);
			if (DEBUG) Log.v(TAG, "COMMONSTATE_COUNTRYLISTKNOWN:knownCountries=" + knownCountries);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_OVERHEATSTATE_OVERHEATCHANGED:	// (178, "Key used to define the command <code>OverHeatChanged</code> of class <code>OverHeatState</code> in project <code>Common</code>"),
		{	// オーバーヒート状態が変化した時 FIXME 未実装
			if (DEBUG) Log.v(TAG, "OVERHEATSTATE_OVERHEATCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_OVERHEATSTATE_OVERHEATREGULATIONCHANGED:	// (179, "Key used to define the command <code>OverHeatRegulationChanged</code> of class <code>OverHeatState</code> in project <code>Common</code>"),
		{	//  オーバーヒート時の冷却方法設定が変更された時 FIXME 未実装
			if (DEBUG) Log.v(TAG, "OVERHEATSTATE_OVERHEATREGULATIONCHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_OVERHEATSTATE_OVERHEATREGULATIONCHANGED_REGULATIONTYPE = ""; /**< Key of the argument </code>regulationType</code> of class <code>OverHeatState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_WIFISETTINGSSTATE_OUTDOORSETTINGSCHANGED:	// (180, "Key used to define the command <code>OutdoorSettingsChanged</code> of class <code>WifiSettingsState</code> in project <code>Common</code>"),
		{	//  WiFiの室内/室外モードが変更された時
			final boolean outdoor = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_WIFISETTINGSSTATE_OUTDOORSETTINGSCHANGED_OUTDOOR) != 0;
			onOutdoorSettingChanged(outdoor);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED:	// (181, "Key used to define the command <code>MavlinkFilePlayingStateChanged</code> of class <code>MavlinkState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE = ""; /**< Key of the argument </code>state</code> of class <code>MavlinkState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_FILEPATH = ""; /**< Key of the argument </code>filepath</code> of class <code>MavlinkState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE = ""; /**< Key of the argument </code>type</code> of class <code>MavlinkState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED:	// (182, "Key used to define the command <code>MavlinkPlayErrorStateChanged</code> of class <code>MavlinkState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_MAVLINKSTATE_MAVLINKPLAYERRORSTATECHANGED_ERROR = ""; /**< Key of the argument </code>error</code> of class <code>MavlinkState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED:	// (183, "Key used to define the command <code>MagnetoCalibrationStateChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// キャリブレーションの状態が変わった時の通知
			final boolean xAxisCalibration = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_XAXISCALIBRATION) == 1;
			final boolean yAxisCalibration = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_YAXISCALIBRATION) == 1;
			final boolean zAxisCalibration = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_ZAXISCALIBRATION) == 1;
			final boolean calibrationFailed = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTATECHANGED_CALIBRATIONFAILED) == 1;
			mStatus.updateCalibrationState(xAxisCalibration, yAxisCalibration, zAxisCalibration, calibrationFailed);
			callOnCalibrationRequiredChanged(calibrationFailed);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE:	// (184, "Key used to define the command <code>MagnetoCalibrationRequiredState</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// キャリブレーションが必要な時
			final boolean required = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONREQUIREDSTATE_REQUIRED) != 0;
			callOnCalibrationRequiredChanged(required);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED:	// (185, "Key used to define the command <code>MagnetoCalibrationAxisToCalibrateChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// キャリブレーション中の軸が変更された時
			final int axis = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS);
			callOnCalibrationAxisChanged(axis);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED:	// (186, "Key used to define the command <code>MagnetoCalibrationStartedChanged</code> of class <code>CalibrationState</code> in project <code>Common</code>"),
		{	// キャリブレーションを開始/終了した時
			final boolean is_started = (Integer)args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONSTARTEDCHANGED_STARTED) != 0;
			callOnCalibrationStartStop(is_started);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED:	// (187, "Key used to define the command <code>CameraSettingsChanged</code> of class <code>CameraSettingsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_FOV = ""; /**< Key of the argument </code>fov</code> of class <code>CameraSettingsState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_PANMAX = ""; /**< Key of the argument </code>panMax</code> of class <code>CameraSettingsState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_PANMIN = ""; /**< Key of the argument </code>panMin</code> of class <code>CameraSettingsState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_TILTMAX = ""; /**< Key of the argument </code>tiltMax</code> of class <code>CameraSettingsState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_CAMERASETTINGSSTATE_CAMERASETTINGSCHANGED_TILTMIN = ""; /**< Key of the argument </code>tiltMin</code> of class <code>CameraSettingsState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED:	// (188, "Key used to define the command <code>AvailabilityStateChanged</code> of class <code>FlightPlanState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_AVAILABILITYSTATECHANGED_AVAILABILITYSTATE = ""; /**< Key of the argument </code>AvailabilityState</code> of class <code>FlightPlanState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED:	// (189, "Key used to define the command <code>ComponentStateListChanged</code> of class <code>FlightPlanState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED:");
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_COMPONENT = ""; /**< Key of the argument </code>component</code> of class <code>FlightPlanState</code> in feature <code>Common</code> */
//			public static String ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANSTATE_COMPONENTSTATELISTCHANGED_STATE = ""; /**< Key of the argument </code>State</code> of class <code>FlightPlanState</code> in feature <code>Common</code> */
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANEVENT_STARTINGERROREVENT:	// (190, "Key used to define the command <code>StartingErrorEvent</code> of class <code>FlightPlanEvent</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "FLIGHTPLANEVENT_STARTINGERROREVENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_FLIGHTPLANEVENT_SPEEDBRIDLEEVENT:	// (191, "Key used to define the command <code>SpeedBridleEvent</code> of class <code>FlightPlanEvent</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "FLIGHTPLANEVENT_SPEEDBRIDLEEVENT:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_CONTROLLERLIBARCOMMANDSVERSION:	// (192, "Key used to define the command <code>ControllerLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ARLIBSVERSIONSSTATE_CONTROLLERLIBARCOMMANDSVERSION:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_SKYCONTROLLERLIBARCOMMANDSVERSION:	// (193, "Key used to define the command <code>SkyControllerLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ARLIBSVERSIONSSTATE_SKYCONTROLLERLIBARCOMMANDSVERSION:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ARLIBSVERSIONSSTATE_DEVICELIBARCOMMANDSVERSION:	// (194, "Key used to define the command <code>DeviceLibARCommandsVersion</code> of class <code>ARLibsVersionsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ARLIBSVERSIONSSTATE_DEVICELIBARCOMMANDSVERSION:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_AUDIOSTATE_AUDIOSTREAMINGRUNNING:	// (195, "Key used to define the command <code>AudioStreamingRunning</code> of class <code>AudioState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "AUDIOSTATE_AUDIOSTREAMINGRUNNING:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_HEADLIGHTSSTATE_INTENSITYCHANGED:	// (196, "Key used to define the command <code>IntensityChanged</code> of class <code>HeadlightsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "HEADLIGHTSSTATE_INTENSITYCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ANIMATIONSSTATE_LIST:	// (197, "Key used to define the command <code>List</code> of class <code>AnimationsState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ANIMATIONSSTATE_LIST:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_SUPPORTEDACCESSORIESLISTCHANGED:	// (198, "Key used to define the command <code>SupportedAccessoriesListChanged</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSORYSTATE_SUPPORTEDACCESSORIESLISTCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_ACCESSORYCONFIGCHANGED:	// (199, "Key used to define the command <code>AccessoryConfigChanged</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSORYSTATE_ACCESSORYCONFIGCHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_ACCESSORYSTATE_ACCESSORYCONFIGMODIFICATIONENABLED:	// (200, "Key used to define the command <code>AccessoryConfigModificationEnabled</code> of class <code>AccessoryState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "ACCESSORYSTATE_ACCESSORYCONFIGMODIFICATIONENABLED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_MAXCHARGERATECHANGED:	// (201, "Key used to define the command <code>MaxChargeRateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CHARGERSTATE_MAXCHARGERATECHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_CURRENTCHARGESTATECHANGED:	// (202, "Key used to define the command <code>CurrentChargeStateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CHARGERSTATE_CURRENTCHARGESTATECHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_LASTCHARGERATECHANGED:	// (203, "Key used to define the command <code>LastChargeRateChanged</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CHARGERSTATE_LASTCHARGERATECHANGED:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_CHARGERSTATE_CHARGINGINFO:	// (204, "Key used to define the command <code>ChargingInfo</code> of class <code>ChargerState</code> in project <code>Common</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "CHARGERSTATE_CHARGINGINFO:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED:	// (205, "Key used to define the command <code>RunIdChanged</code> of class <code>RunState</code> in project <code>Common</code>"),
		{
			if (DEBUG) Log.v(TAG, "RUNSTATE_RUNIDCHANGE:");
			final String runID = (String) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED_RUNID);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG:	// (206, "Key used to define the feature <code>CommonDebug</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COMMONDEBUG:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_STATSEVENT_SENDPACKET:	// (207, "Key used to define the command <code>SendPacket</code> of class <code>StatsEvent</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COMMONDEBUG_STATSEVENT_SENDPACKET:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_DEBUGSETTINGSSTATE_INFO:	// (208, "Key used to define the command <code>Info</code> of class <code>DebugSettingsState</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COMMONDEBUG_DEBUGSETTINGSSTATE_INFO:");
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_COMMONDEBUG_DEBUGSETTINGSSTATE_LISTCHANGED:	// (209, "Key used to define the command <code>ListChanged</code> of class <code>DebugSettingsState</code> in project <code>CommonDebug</code>"),
		{	// FIXME 未実装
			if (DEBUG) Log.v(TAG, "COMMONDEBUG_DEBUGSETTINGSSTATE_LISTCHANGED:");
			break;
		}
		}
	}

	protected abstract void setCountryCode(final String code);
	protected abstract void setAutomaticCountry(final boolean auto);
	protected abstract void callOnCalibrationStartStop(final boolean is_start);
	protected abstract void callOnCalibrationRequiredChanged(final boolean failed);
	protected abstract void callOnCalibrationAxisChanged(final int axis);

	protected abstract void onOutdoorSettingChanged(final boolean outdoor);

	protected void onCommonStateMassStorageStateListChanged(
		final int mass_storage_id, final String name) {

		if (DEBUG) Log.v(TAG, String.format("onCommonStateMassStorageStateListChanged:id=%d,name=%s", mass_storage_id, name));
	}

	protected void onCommonStateMassStorageInfoStateListChanged(
		final int mass_storage_id, final long size, final long used_size,
		final boolean plugged, final boolean full, final boolean internal) {

		if (DEBUG) Log.v(TAG, String.format("onCommonStateMassStorageInfoStateListChanged:%d,size=%d,used=%d",
			mass_storage_id, size, used_size));
	}

	/**
	 * WiFiの信号強度が変化した時の処理
	 * @param rssi
	 */
	protected void onCommonStateWifiSignalChangedUpdate(final int rssi) {
		if (DEBUG) Log.v(TAG, "onCommonStateWifiSignalChangedUpdate:rssi=" + rssi);
	}

//********************************************************************************
// データ送受信関係
//********************************************************************************
	private static final SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat formattedTime = new SimpleDateFormat("'T'HHmmssZZZ", Locale.getDefault());
	@Override
	public boolean sendDate(final Date currentDate) {
		if (DEBUG) Log.v(TAG, "sendDate:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureCommon().sendCommonCurrentDate(formattedDate.format(currentDate));
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendDate failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendTime(final Date currentTime) {
		if (DEBUG) Log.v(TAG, "sendTime:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureCommon().sendCommonCurrentTime(formattedTime.format(currentTime));
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendTime failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAllSettings() {
		if (DEBUG) Log.v(TAG, "requestAllSettings:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsAllSettings();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAllSettings failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestAllStates() {
		if (DEBUG) Log.v(TAG, "requestAllStates:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
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
		if (DEBUG) Log.v(TAG, "sendNetworkDisconnect:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isConnected()) {
			result = mARDeviceController.getFeatureCommon().sendNetworkDisconnect();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setCountryCode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	protected boolean sendCountryCode(final String code) {
		if (DEBUG) Log.v(TAG, "sendCountryCode:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsCountry(code);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setCountryCode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	protected boolean sendAutomaticCountry(final boolean auto) {
		if (DEBUG) Log.v(TAG, "sendAutomaticCountry:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isStarted()) {
			result = mARDeviceController.getFeatureCommon().sendSettingsAutoCountry(auto ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setAutomaticCountry failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}
}
