package com.serenegiant.arflight.NewControllers;

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
import java.util.ArrayList;
import java.util.List;

public abstract class DeviceController implements IDeviceController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = DeviceController.class.getSimpleName();

	private final WeakReference<Context> mWeakContext;
	protected LocalBroadcastManager mLocalBroadcastManager;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;
	protected ARDeviceController mARDeviceController;

	protected final Object mStateSync = new Object();
	private int mState = STATE_STOPPED;
	protected AttributeDevice mInfo;
	protected CommonStatus mStatus;

	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();

	public DeviceController(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
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

	}

	protected boolean startNetwork() {
		boolean failed = false;
		ARDiscoveryDevice device = null;
		try {
			device = new ARDiscoveryDevice();

			ARDiscoveryDeviceNetService netDeviceService = (ARDiscoveryDeviceNetService) mDeviceService.getDevice();
			device.initWifi(getProductType(), netDeviceService.getName(), netDeviceService.getIp(), netDeviceService.getPort());
		} catch (final ARDiscoveryException e) {
			Log.e(TAG, "Exception", e);
			Log.e(TAG, "Error: " + e.getError());
			failed = true;
		}
		if (device != null) {
			ARDeviceController deviceController = mARDeviceController = null;
			try {
				deviceController = new ARDeviceController(device);

				deviceController.addListener(mDeviceControllerListener);
				mARDeviceController = deviceController;
			} catch (final ARControllerException e) {
				Log.e(TAG, "Exception", e);
				failed = true;
			}
		}
		return failed;
	}

	protected void internal_start() {
	}

	/**
	 * DeviceControllerがstartした時の処理
	 */
	protected void onStarted() {
		if (DEBUG) Log.v(TAG, "onStarted:");
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
		if ((mARDeviceController != null) && (ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING.equals(mState))) {
			final ARCONTROLLER_ERROR_ENUM error = mARDeviceController.stop();
			final boolean failed = (error != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK);
			if (failed) {
				Log.w(TAG, "failed to stop ARController:err=" + error);
			}
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
			return (mState == STATE_STARTED) && (getAlarm() != DroneStatus.ALARM_DISCONNECTED);
		}
	}

	@Override
	public boolean sendDate(final Date currentDate) {
		return false;
	}

	@Override
	public boolean sendTime(final Date currentDate) {
		return false;
	}

	@Override
	public boolean requestAllSettings() {
		return false;
	}

	@Override
	public boolean requestAllStates() {
		return false;
	}


	private final ARDeviceControllerListener mDeviceControllerListener = new ARDeviceControllerListener() {
		@Override
		public void onStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

			DeviceController.this.onStateChanged(deviceController, newState, error);
        }

		@Override
		public void onExtensionStateChanged(final ARDeviceController deviceController,
			final ARCONTROLLER_DEVICE_STATE_ENUM newState,
			final ARDISCOVERY_PRODUCT_ENUM product, final String name, final ARCONTROLLER_ERROR_ENUM error) {

			DeviceController.this.onExtensionStateChanged(deviceController, newState, product, name, error);
		}

		@Override
		public void onCommandReceived(final ARDeviceController deviceController,
			final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey, final ARControllerDictionary elementDictionary) {

			DeviceController.this.onCommandReceived(deviceController, commandKey, elementDictionary);
		}
    };

	/** mDeviceControllerListenerの下請け */
	protected void onStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState, final ARCONTROLLER_ERROR_ENUM error) {

		mDeviceState = newState;
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

	private ARCONTROLLER_DEVICE_STATE_ENUM mDeviceState;
	protected ARCONTROLLER_DEVICE_STATE_ENUM getDeviceState() {
		return mDeviceState;
	}

	/** onStateChangedの下請け */
	protected void onConnect() {
		mARDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable((byte) 1);
	}

	/** onStateChangedの下請け */
	protected void onDisconnect() {
		setAlarm(DroneStatus.ALARM_DISCONNECTED);
		callOnAlarmStateChangedUpdate(DroneStatus.ALARM_DISCONNECTED);
		callOnDisconnect();
	}

	/** mDeviceControllerListenerの下請け */
	protected void onExtensionStateChanged(final ARDeviceController deviceController,
		final ARCONTROLLER_DEVICE_STATE_ENUM newState,
		final ARDISCOVERY_PRODUCT_ENUM product,
		final String name, final ARCONTROLLER_ERROR_ENUM error) {
	}

	/** mDeviceControllerListenerの下請け */
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerDictionary elementDictionary) {

		if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED) && (elementDictionary != null)) {
			// if event received is the battery update
			final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
                if (args != null) {
                    final int percent = (Integer) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_COMMONSTATE_BATTERYSTATECHANGED_PERCENT);
                    // バッテリー残表が変化した時の処理
					setBattery(percent);
					callOnUpdateBattery(percent);
                }
		} else if ((commandKey == ARCONTROLLER_DICTIONARY_KEY_ENUM.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED) && (elementDictionary != null)){
			// if event received is the run id
			final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
			if (args != null) {
				final String runID = (String) args.get(ARFeatureCommon.ARCONTROLLER_DICTIONARY_KEY_COMMON_RUNSTATE_RUNIDCHANGED_RUNID);
            }
		}
	}

	protected void setCountryCode(final String code) {
	}

	protected void setAutomaticCountry(final boolean auto) {
	}
}
