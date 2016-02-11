package com.serenegiant.arflight;

import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_DECODER_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateAllStatesChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateBatteryStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateCurrentDateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateCurrentTimeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateWifiSignalChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateAllSettingsChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateAutoCountryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateCountryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductNameChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialHighChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialLowChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateResetChangedListener;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_ERROR_ENUM;
import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryConnection;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.parrot.arsdk.arnetwork.ARNETWORK_ERROR_ENUM;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM;
import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_ERROR_ENUM;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.parrot.arsdk.arsal.ARNativeData;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public abstract class DeviceController implements IDeviceController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = DeviceController.class.getSimpleName();

	protected final Context mContext;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;
	private final SkyController mSkyController;

	protected ARNetworkALManager mARManager;
	protected ARNetworkManager mARNetManager;
	protected boolean mMediaOpened;
	private int videoFragmentSize;
	private int videoFragmentMaximumNumber;
	private int videoMaxAckInterval;

	private final Semaphore disconnectSent = new Semaphore(0);
	private volatile boolean mRequestCancel;
	protected final Semaphore cmdGetAllSettingsSent = new Semaphore(0);
	protected boolean isWaitingAllSettings;
	protected final Semaphore cmdGetAllStatesSent = new Semaphore(0);
	protected boolean isWaitingAllStates;

	protected final Object mStateSync = new Object();
	private int mState = STATE_STOPPED;

	private Thread rxThread;
	private Thread txThread;

	private final List<ReaderThread> mReaderThreads = new ArrayList<ReaderThread>();
	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();
	protected AttributeDevice mInfo;
	protected CommonStatus mStatus;

	public DeviceController(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		mContext = context;
		mDeviceService = service;
		mSkyController = null;
		mNetConfig = net_config;
	}

	public DeviceController(final Context context, final SkyController skyController) {
		mContext = context;
		mDeviceService = null;
		mSkyController = skyController;
		mNetConfig = skyController.mNetConfig;
	}

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

	public int getProductId() {
		final ARDiscoveryDeviceService device_service = getDeviceService();
		return device_service != null ? device_service.getProductID() : 0;
	}

	@Override
	public ARDiscoveryDeviceService getDeviceService() {
		// これだとSkyController経由で繋いでる時はSkyControllerのARDiscoveryDeviceServiceが返ってきてしまう
		// SkyController経由で接続しているARDiscoveryDeviceServiceを返せた方がいいのかも
		return mDeviceService != null ? mDeviceService : mSkyController.getDeviceService();
	}

	@Override
	public ARNetworkConfig getNetConfig() {
		return mNetConfig;
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return mState;
		}
	}

	/**
	 * 接続開始。子クラスで追加処理が必要であれば#internal_startをOverrideすること
	 * @return
	 */
	@Override
	public final boolean start() {
		if (DEBUG) Log.v(TAG, "start:");

		synchronized (mStateSync) {
			if (mState != STATE_STOPPED) return false;
			mState = STATE_STARTING;
		}
		setAlarm(DroneStatus.ALARM_NON);

		registerARCommandsListener();

		final boolean failed = startNetwork();

		if (!failed) {
            // ネットワークへの送受信スレッドを生成&開始
			rxThread = new Thread(mARNetManager.m_receivingRunnable);
			rxThread.start();

			txThread = new Thread(mARNetManager.m_sendingRunnable);
			txThread.start();

			// 機体データ受信スレッドを生成&開始
			startReadThreads();

			internal_start();

			synchronized (mStateSync) {
				mState = STATE_STARTED;
			}
			onStarted();
		}
		if (DEBUG) Log.v(TAG, "start:finished");

		return failed;
	}

	protected void setAlarm(final int alarm) {
	}

	protected int getAlarm() {
		return DroneStatus.ALARM_DISCONNECTED;
	}

	protected void internal_start() {
	}

	/**
	 * 接続処理を中断
	 */
	@Override
	public void cancelStart() {
		if (DEBUG) Log.v(TAG, "cancelStart:");
		if (!mRequestCancel) {
			mRequestCancel = true;
			final ARDiscoveryDeviceService device_service = getDeviceService();
			final Object device = device_service.getDevice();
			if (device instanceof ARDiscoveryDeviceNetService) {
				if (discoveryData != null) {
					discoveryData.ControllerConnectionAbort();
				}
			} else if (device instanceof ARDiscoveryDeviceBLEService) {
				mARManager.cancelBLENetwork();
			} else {
				Log.w(TAG, "Unknown network media type.");
			}
			cmdGetAllSettingsSent.release();
			cmdGetAllStatesSent.release();
			//TODO see : reset the semaphores or use signals
		}
		if (DEBUG) Log.v(TAG, "cancelStart:finished");
	}

	/**
	 * 切断処理。子クラスで追加処理が必要であれば#internal_stopをOverrideすること
	 */
	@Override
	public final void stop() {
		if (DEBUG) Log.v(TAG, "stop:");

		synchronized (mStateSync) {
			if (mState != STATE_STARTED) return;
			mState = STATE_STOPPING;
		}

		internal_stop();
		// 機体データ受信スレッドを終了(終了するまで戻らない)
		stopReaderThreads();

		// ネットワーク接続をクリーンアップ
		stopNetwork();

		unregisterARCommandsListener();

		synchronized (mStateSync) {
			mState = STATE_STOPPED;
		}
		if (DEBUG) Log.v(TAG, "stop:終了");
	}

	protected void internal_stop() {
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
			if (DEBUG) Log.v(TAG, "onStarted:sendAllSettings");
			if (sendAllSettings()) {
				try {
					if (DEBUG) Log.v(TAG, "onStarted:sendAllSettings:wait");
					//successful = cmdGetAllSettingsSent.tryAcquire (INITIAL_TIMEOUT_RETRIEVAL_MS, TimeUnit.MILLISECONDS);
					cmdGetAllSettingsSent.acquire();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		} finally {
			if (DEBUG) Log.v(TAG, "onStarted:sendAllSettings:finished");
			isWaitingAllSettings = false;
		}
		isWaitingAllStates = true;
		try {
			if (DEBUG) Log.v(TAG, "onStarted:sendAllStates");
			if (sendAllStates()) {
				try {
					if (DEBUG) Log.v(TAG, "onStarted:sendAllStates:wait");
					//successful = cmdGetAllStatesSent.tryAcquire (INITIAL_TIMEOUT_RETRIEVAL_MS, TimeUnit.MILLISECONDS);
					cmdGetAllStatesSent.acquire();
				} catch (final InterruptedException e) {
					// ignore
				}
			}
		} finally {
			if (DEBUG) Log.v(TAG, "onStarted:sendAllStates:finished");
			isWaitingAllStates = false;
		}
		callOnConnect();
		if (DEBUG) Log.v(TAG, "onStarted:finished");
	}

	private String discoveryIp;
	private int discoveryPort;

	protected void prepare_network() {
	}

	protected boolean startNetwork() {
		if (DEBUG) Log.v(TAG, "startNetwork:");
		boolean failed = false;
		int pingDelay = 0; /* 0 means default, -1 means no ping */

		// FIXME SkyControllerへブリッジさせる時の処理を追加する
		// SkyControllerからmARManagerとmARNetManagerをコピーすればいいんかな?

		if (mDeviceService != null) {
			/* Create the looper ARNetworkALManager */
			mARManager = new ARNetworkALManager();

			final ARDiscoveryDeviceService device_service = getDeviceService();
			final Object device = device_service.getDevice();
			if (DEBUG) Log.v(TAG, "device=" + device);
			if (device instanceof ARDiscoveryDeviceNetService) {
				// Wifiの時
				if (DEBUG) Log.v(TAG, "Wifi接続開始");
				final ARDiscoveryDeviceNetService netDevice = (ARDiscoveryDeviceNetService)device;
				discoveryIp = netDevice.getIp();
				discoveryPort = netDevice.getPort();

				/*  */
				if (!ardiscoveryConnect()) {
					failed = true;
				}

				prepare_network();
				final ARNETWORKAL_ERROR_ENUM netALError = mARManager.initWifiNetwork(discoveryIp, c2dPort, d2cPort, 1);

				if (netALError == ARNETWORKAL_ERROR_ENUM.ARNETWORKAL_OK) {
					mMediaOpened = true;
				} else {
					Log.w(TAG, "error occurred: " + netALError.toString());
					failed = true;
				}
			} else if (device instanceof ARDiscoveryDeviceBLEService) {
				// Bluetoothの時
				if (DEBUG) Log.v(TAG, "Bluetooth接続開始");
				final ARDiscoveryDeviceBLEService bleDevice = (ARDiscoveryDeviceBLEService) device;

				prepare_network();
				final ARNETWORKAL_ERROR_ENUM netALError = mARManager.initBLENetwork(
					mContext, bleDevice.getBluetoothDevice(), 1, mNetConfig.getBLENotificationIDs()/*bleNotificationIDs*/);

				if (netALError == ARNETWORKAL_ERROR_ENUM.ARNETWORKAL_OK) {
					mMediaOpened = true;
					pingDelay = -1; /* Disable ping for BLE networks */
				} else {
					Log.w(TAG, "error occurred: " + netALError.toString());
					failed = true;
				}
			} else {
				Log.w(TAG, "unknown AR discovery device service");
				failed = true;
			}
			if (!failed) {
				// ARNetworkManagerを生成
				if (DEBUG) Log.v(TAG, "ARNetworkManagerを生成");
				mARNetManager = new ARNetworkManagerExtend(mARManager,
					mNetConfig.getC2dParams(), mNetConfig.getD2cParams(), pingDelay);
				if (mARNetManager.isCorrectlyInitialized() == false) {
					Log.e(TAG, "new ARNetworkManager failed");
					failed = true;
				}
			}
		} else if (mSkyController != null) {
			mARManager = mSkyController.mARManager;
			mARNetManager = mSkyController.mARNetManager;
		}
		if (DEBUG) Log.v(TAG, "startNetwork:finished:failed=" + failed);
		return failed;
	}

	/** 機体との接続を終了 */
	private void stopNetwork() {
		if (DEBUG) Log.v(TAG, "stopNetwork:");
		// FIXME SkyControllerへブリッジさせる時の処理を追加する

		if (mDeviceService != null) {
			if (mARNetManager != null) {
				mARNetManager.stop();

				try {
					if (txThread != null) {
						txThread.join();
					}
					if (rxThread != null) {
						rxThread.join();
					}
				} catch (final InterruptedException e) {
					Log.w(TAG, e);
				}

				mARNetManager.dispose();
			}

			final ARDiscoveryDeviceService device_service = getDeviceService();
			if ((mARManager != null) && (mMediaOpened)) {
				if (device_service.getDevice() instanceof ARDiscoveryDeviceNetService) {
					mARManager.closeWifiNetwork();
				} else if (device_service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
					mARManager.closeBLENetwork(mContext);
				}

				mMediaOpened = false;
				mARManager.dispose();
			}
		} else if (mSkyController != null) {
			mARNetManager = null;
			mARManager = null;
		}
		if (DEBUG) Log.v(TAG, "stopNetwork:終了");
	}

	private Semaphore discoverSemaphore;
	private ARDiscoveryConnection discoveryData;
	private int c2dPort, d2cPort;

	private boolean ardiscoveryConnect() {
		boolean ok = true;
		ARDISCOVERY_ERROR_ENUM error = ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_OK;
		discoverSemaphore = new Semaphore(0);

		d2cPort = mNetConfig.getInboundPort();

		// 製品の種類を取得
		final ARDiscoveryDeviceService device_service = getDeviceService();
		final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device_service.getProductID());

		discoveryData = new ARDiscoveryConnection() {
			@Override
			public String onSendJson () {
                /* send a json with the Device to controller port */
				final JSONObject jsonObject = DeviceController.this.onSendJson(new JSONObject());
				return jsonObject.toString();
			}

			@Override
			public ARDISCOVERY_ERROR_ENUM onReceiveJson (final String dataRx, final String ip) {
				/* Receive a json with the controller to Device port */
				ARDISCOVERY_ERROR_ENUM error = ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_OK;
				try {
					/* Convert String to json */
					final JSONObject jsonObject = new JSONObject(dataRx);
					if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_C2DPORT_KEY)) {
						c2dPort = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_C2DPORT_KEY);
					}
					DeviceController.this.onReceiveJson(jsonObject, dataRx, ip);
					/* Else: leave it to the default value. */
				} catch (final JSONException e) {
					Log.w(TAG, e);
					error = ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_ERROR;
				}
				return error;
			}
		};

		if (ok) {
			// 接続監視スレッドを生成＆実行開始
			final ConnectionThread connectionThread = new ConnectionThread();
			connectionThread.start();
            /* wait the discovery of the connection data */
			try {
				discoverSemaphore.acquire();
				error = connectionThread.getError();
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}

            /* release discoveryData it not needed more */
            if (discoveryData != null) {
				discoveryData.dispose();
				discoveryData = null;
			}
		}

		return ok && (error == ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_OK);
	}

	protected JSONObject onSendJson(final JSONObject jsonObject) {
		try {
			jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_D2CPORT_KEY, d2cPort);
		} catch (final JSONException e) {
			Log.w(TAG, e);
		}
		try {
			if (DEBUG) Log.v(TAG, "android.os.Build.MODEL: "+android.os.Build.MODEL);
			jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_CONTROLLER_NAME_KEY, android.os.Build.MODEL);
		} catch (final JSONException e) {
			Log.w(TAG, e);
		}
		try {
			if (DEBUG) Log.v(TAG, "android.os.Build.DEVICE: "+android.os.Build.DEVICE);
			jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_CONTROLLER_TYPE_KEY, android.os.Build.DEVICE);
		} catch (final JSONException e) {
			Log.w(TAG, e);
		}
		return jsonObject;
	}

	protected void onReceiveJson(final JSONObject jsonObject, final String dataRx, final String ip) throws JSONException {
	}

	/** 機体からのデータ受信用スレッドを生成＆開始 */
	private void startReadThreads() {
		if (DEBUG) Log.v(TAG, "startReadThreads");
        /* Create the reader threads */
		for (final int bufferId : mNetConfig.getCommandsIOBuffers()) {
			final ReaderThread readerThread = new ReaderThread(bufferId);
			mReaderThreads.add(readerThread);
		}

        /* Mark all reader threads as started */
		for (final ReaderThread readerThread : mReaderThreads) {
			readerThread.start();
		}
	}

	/** 機体からのデータ受信用スレッドを終了(終了するまで戻らない) */
	private void stopReaderThreads() {
		if (DEBUG) Log.v(TAG, "stopReaderThreads:");
		for (final ReaderThread thread : mReaderThreads) {
			thread.stopThread();
		}
		for (final ReaderThread thread : mReaderThreads) {
			try {
				thread.join();
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		mReaderThreads.clear();
		if (DEBUG) Log.v(TAG, "stopReaderThreads:終了");
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
	}

	/**
	 * バッテリー残量変更コールバックを呼び出す
	 */
	protected void callOnUpdateBattery(final int percent) {
	}

//================================================================================
// ARSDK3からのコールバックリスナー関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		ARCommand.setCommonCommonStateAllStatesChangedListener(mARCommandCommonCommonStateAllStatesChangedListener);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(mCommonStateBatteryStateChangedListener);
		ARCommand.setCommonCommonStateCurrentDateChangedListener(mARCommandCommonCommonStateCurrentDateChangedListener);
		ARCommand.setCommonCommonStateCurrentTimeChangedListener(mARCommandCommonCommonStateCurrentTimeChangedListener);
		ARCommand.setCommonCommonStateWifiSignalChangedListener(mARCommandCommonCommonStateWifiSignalChangedListener);
		ARCommand.setCommonSettingsStateAllSettingsChangedListener(mARCommandCommonSettingsStateAllSettingsChangedListener);
		ARCommand.setCommonSettingsStateResetChangedListener(mARCommandCommonSettingsStateResetChangedListener);
		ARCommand.setCommonSettingsStateProductNameChangedListener(mARCommandCommonSettingsStateProductNameChangedListener);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(mARCommandCommonSettingsStateProductVersionChangedListener);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(mARCommandCommonSettingsStateProductSerialHighChangedListener);
		ARCommand.setCommonSettingsStateProductSerialLowChangedListener(mARCommandCommonSettingsStateProductSerialLowChangedListener);
		ARCommand.setCommonSettingsStateCountryChangedListener(mARCommandCommonSettingsStateCountryChangedListener);
		ARCommand.setCommonSettingsStateAutoCountryChangedListener(mARCommandCommonSettingsStateAutoCountryChangedListener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setCommonCommonStateAllStatesChangedListener(null);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(null);
		ARCommand.setCommonCommonStateCurrentDateChangedListener(null);
		ARCommand.setCommonCommonStateCurrentTimeChangedListener(null);
		ARCommand.setCommonCommonStateWifiSignalChangedListener(null);
		ARCommand.setCommonSettingsStateAllSettingsChangedListener(null);
		ARCommand.setCommonSettingsStateResetChangedListener(null);
		ARCommand.setCommonSettingsStateProductNameChangedListener(null);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialLowChangedListener(null);
		ARCommand.setCommonSettingsStateCountryChangedListener(null);
		ARCommand.setCommonSettingsStateAutoCountryChangedListener(null);
	}

	/**
	 * 設定がリセットされた時
	 */
	private final ARCommandCommonSettingsStateResetChangedListener
		mARCommandCommonSettingsStateResetChangedListener
		= new ARCommandCommonSettingsStateResetChangedListener() {
		@Override
		public void onCommonSettingsStateResetChangedUpdate() {
		}
	};

	/**
	 * 製品名を受信した時
	 */
	private final ARCommandCommonSettingsStateProductNameChangedListener
		mARCommandCommonSettingsStateProductNameChangedListener
			= new ARCommandCommonSettingsStateProductNameChangedListener() {
		/**
		 * @param name
		 */
		@Override
		public void onCommonSettingsStateProductNameChangedUpdate(final String name) {
			mInfo.setProductName(name);
		}
	};

	@Override
	public String getSoftwareVersion() {
		return mInfo.productSoftware();
	}

	@Override
	public String getHardwareVersion() {
		return mInfo.productHardware();
	}

	/**
	 * 製品バージョンを受信した時
	 */
	private final ARCommandCommonSettingsStateProductVersionChangedListener
		mARCommandCommonSettingsStateProductVersionChangedListener
			= new ARCommandCommonSettingsStateProductVersionChangedListener() {
		@Override
		public void onCommonSettingsStateProductVersionChangedUpdate(
			final String software, final String hardware) {
			mInfo.setProduct(software, hardware);
		}
	};

	@Override
	public String getSerial() {
		return mInfo.getSerial();
	}

	/**
	 * シリアル番号の上位を受信した時
	 */
	private final ARCommandCommonSettingsStateProductSerialHighChangedListener
		mARCommandCommonSettingsStateProductSerialHighChangedListener
			= new ARCommandCommonSettingsStateProductSerialHighChangedListener() {
		/**
		 * @param high Serial high number (hexadecimal value)
		 */
		@Override
		public void onCommonSettingsStateProductSerialHighChangedUpdate(final String high) {
			mInfo.setSerialHigh(high);
		}
	};

	/**
	 * シリアル番号の下位を受信した時
	 */
	private final ARCommandCommonSettingsStateProductSerialLowChangedListener
		mARCommandCommonSettingsStateProductSerialLowChangedListener
			= new ARCommandCommonSettingsStateProductSerialLowChangedListener() {
		/**
		 * @param low Serial low number (hexadecimal value)
		 */
		@Override
		public void onCommonSettingsStateProductSerialLowChangedUpdate(final String low) {
			mInfo.setSerialLow(low);
		}
	};

	/**
	 * 国コードを受信した時
	 */
	private final ARCommandCommonSettingsStateCountryChangedListener
		mARCommandCommonSettingsStateCountryChangedListener
			= new ARCommandCommonSettingsStateCountryChangedListener() {
		/**
		 * @param code Country code with ISO 3166 format, empty string means unknown country.
		 */
		@Override
		public void onCommonSettingsStateCountryChangedUpdate(final String code) {
			setCountryCode(code);
		}
	};

	protected void setCountryCode(final String code) {
	}

	/**
	 * 自動国選択設定が変更された時
	 */
	private final ARCommandCommonSettingsStateAutoCountryChangedListener
		mARCommandCommonSettingsStateAutoCountryChangedListener
			= new ARCommandCommonSettingsStateAutoCountryChangedListener() {
		/**
		 * @param automatic 0: Manual, 1: Auto
		 */
		@Override
		public void onCommonSettingsStateAutoCountryChangedUpdate(final byte automatic) {
			setAutomaticCountry(automatic != 0);
		}
	};

	protected void setAutomaticCountry(final boolean auto) {
	}

	/**
	 * WiFiの信号強度が変化した時
	 */
	private final ARCommandCommonCommonStateWifiSignalChangedListener
		mARCommandCommonCommonStateWifiSignalChangedListener
			= new ARCommandCommonCommonStateWifiSignalChangedListener() {
		/**
		 * @param rssi RSSI of the signal between controller and the product (in dbm)
		 */
		@Override
		public void onCommonCommonStateWifiSignalChangedUpdate(final short rssi) {
			DeviceController.this.onCommonCommonStateWifiSignalChangedUpdate(rssi);
		}
	};

	/**
	 * WiFiの信号強度が変化した時の処理
	 * @param rssi
	 */
	protected void onCommonCommonStateWifiSignalChangedUpdate(final short rssi) {
	}

	/**
	 * 日付が変更された時
	 */
	private final ARCommandCommonCommonStateCurrentDateChangedListener
		mARCommandCommonCommonStateCurrentDateChangedListener
			= new ARCommandCommonCommonStateCurrentDateChangedListener() {
		/**
		 * @param date Date with ISO-8601 format
		 */
		@Override
		public void onCommonCommonStateCurrentDateChangedUpdate(final String date) {
			// XXX
		}
	};

	/**
	 * 現在時刻が変更された時
	 */
	private final ARCommandCommonCommonStateCurrentTimeChangedListener
		mARCommandCommonCommonStateCurrentTimeChangedListener
			= new ARCommandCommonCommonStateCurrentTimeChangedListener() {
		/**
		 * @param time Time with ISO-8601 format
		 */
		@Override
		public void onCommonCommonStateCurrentTimeChangedUpdate(final String time) {
			// XXX
		}
	};

	protected void setBattery(final int percent) {
	}

	/**
	 * バッテリーの残量が変化した時
	 */
	private final ARCommandCommonCommonStateBatteryStateChangedListener
		mCommonStateBatteryStateChangedListener
		= new ARCommandCommonCommonStateBatteryStateChangedListener() {
		/**
		 * @param percent
		 */
		@Override
		public void onCommonCommonStateBatteryStateChangedUpdate(final byte percent) {
			if (getBattery() != percent) {
				// FIXME DroneStatusの#setBatteryを呼べばコールバックが呼び出されるようにしたい
				setBattery(percent);
				callOnUpdateBattery(percent);
			}
		}
	};

	/**
	 * AllSettings要求が完了した時
	 */
	private final ARCommandCommonSettingsStateAllSettingsChangedListener
		mARCommandCommonSettingsStateAllSettingsChangedListener
			= new ARCommandCommonSettingsStateAllSettingsChangedListener() {
		@Override
		public void onCommonSettingsStateAllSettingsChangedUpdate() {
			if (isWaitingAllSettings) {
				cmdGetAllSettingsSent.release();
			}
		}
	};

	/**
	 * AllStates要求が完了した時
	 */
	private final ARCommandCommonCommonStateAllStatesChangedListener
		mARCommandCommonCommonStateAllStatesChangedListener
			= new ARCommandCommonCommonStateAllStatesChangedListener() {
		@Override
		public void onCommonCommonStateAllStatesChangedUpdate() {
			if (isWaitingAllStates) {
				cmdGetAllStatesSent.release();
			}
		}
	};

//********************************************************************************
// データ送受信関係
//********************************************************************************
	@Override
	public boolean sendAllSettings() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonSettingsAllSettings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AllSettings command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendAllStates() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonAllStates();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);


			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send CommonAllStates command.");
		}

		return sentStatus;
	}

	private static final SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat formattedTime = new SimpleDateFormat("'T'HHmmssZZZ", Locale.getDefault());

	@Override
	public boolean sendDate(final Date currentDate) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonCurrentDate(formattedDate.format(currentDate));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);


			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send date command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendTime(final Date currentDate) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();


		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonCurrentTime(formattedTime.format(currentDate));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);

			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send time command.");
		}

		return sentStatus;
	}

	/**
	 * ネットワーク切断要求
	 * @return
	 */
	public boolean sendNetworkDisconnect() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonNetworkDisconnect();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}


	private final NetworkNotificationListener mNetworkNotificationListener
		= new NetworkNotificationListener() {
		@Override
		public void networkDidSendFrame(final NetworkNotificationData notificationData) {
			// コマンドを送信完了した
		}

		@Override
		public void networkDidReceiveAck(final NetworkNotificationData notificationData) {
			// Ackを受信した
			if (notificationData != null) {
				notificationData.notificationRun();
			}
		}

		@Override
		public void networkTimeoutOccurred(final NetworkNotificationData notificationData) {
			// タイムアウトが発生した
			if (DEBUG) Log.w(TAG, "networkTimeoutOccurred:");
		}

		@Override
		public void networkDidCancelFrame(final NetworkNotificationData notificationData) {
			// キャンセルされた
			if (DEBUG) Log.w(TAG, "networkDidCancelFrame:");
			if (notificationData != null) {
				notificationData.notificationRun();
			}
		}
	};

	/**
	 * 指定したコマンドを指定したバッファにキューイング
	 * @param bufferId バッファID
	 * @param cmd
	 * @param timeoutPolicy
	 * @param notificationData
	 * @return 正常にキューイングできればtrue
	 */
	protected boolean sendData(final int bufferId, final ARCommand cmd,
		final ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM timeoutPolicy,
		final NetworkNotificationData notificationData) {

		synchronized (mStateSync) {
			if (mState != STATE_STARTED) return false;
		}

		boolean result = true;

		final ARNetworkSendInfo sendInfo
			= new ARNetworkSendInfo(timeoutPolicy, mNetworkNotificationListener, notificationData, this);

		final ARNETWORK_ERROR_ENUM netError= mARNetManager.sendData(bufferId, cmd, sendInfo, true);
		if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
			Log.e(TAG, "ARNetManager#sendData failed. " + netError.toString());
			result = false;
		}

		return result;
	}

	/**
	 * コールバック呼び出し用にARNetworkManagerを拡張
	 */
	protected class ARNetworkManagerExtend extends ARNetworkManager {
		private static final String TAG = "ARNetworkManagerExtend";

		public ARNetworkManagerExtend(
			final ARNetworkALManager osSpecificManager, final ARNetworkIOBufferParam[] inputParamArray,
			final ARNetworkIOBufferParam[] outputParamArray, final int timeBetweenPingsMs) {

			super(osSpecificManager, inputParamArray, outputParamArray, timeBetweenPingsMs);
		}

		@Override
		public ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM onCallback(
			final int ioBufferId, final ARNativeData data,
			final ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM status, final Object customData) {

			ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DEFAULT;
			final ARNetworkSendInfo sendInfo = (ARNetworkSendInfo) customData;

//			if (status == ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM.ARNETWORK_MANAGER_CALLBACK_STATUS_TIMEOUT) {
//				retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP;
//			}

			final NetworkNotificationListener listener = sendInfo != null ? sendInfo.getNotificationListener() : null;
			switch (status) {
			case ARNETWORK_MANAGER_CALLBACK_STATUS_SENT:
				if (listener != null) {
					try {
						listener.networkDidSendFrame(sendInfo.getNotificationData());
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
				break;

			case ARNETWORK_MANAGER_CALLBACK_STATUS_ACK_RECEIVED:
				if (listener != null) {
					try {
						listener.networkDidReceiveAck(sendInfo.getNotificationData());
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
				break;

			case ARNETWORK_MANAGER_CALLBACK_STATUS_TIMEOUT:
				if (listener != null) {
					try {
						listener.networkTimeoutOccurred(sendInfo.getNotificationData());
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
                /* Apply sending policy. */
				if (sendInfo != null) {
					retVal = sendInfo.getTimeoutPolicy();
				}

				break;

			case ARNETWORK_MANAGER_CALLBACK_STATUS_CANCEL:
				if (listener != null) {
					try {
						listener.networkDidCancelFrame(sendInfo.getNotificationData());
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
				break;

			case ARNETWORK_MANAGER_CALLBACK_STATUS_FREE:
				if (data != null) {
					data.dispose();
				} else {
					Log.w(TAG, "no data to free");
				}

				break;

			case ARNETWORK_MANAGER_CALLBACK_STATUS_DONE:
				break;

			default:
				Log.w(TAG, "default case status:"+ status);
				break;
			}

			return retVal;
		}

		@Override
		public void onDisconnect(final ARNetworkALManager arNetworkALManager) {
			if (DEBUG) Log.d(TAG, "onDisconnect ...");
			DeviceController.this.stop();
			setAlarm(DroneStatus.ALARM_DISCONNECTED);
			callOnAlarmStateChangedUpdate(DroneStatus.ALARM_DISCONNECTED);
			callOnDisconnect();
		}
	}

	private static int thread_cnt = 0;
	protected abstract class LooperThread extends Thread {
		private volatile boolean mIsRunning;

		public LooperThread() {
			mIsRunning = true;
		}

		@Override
		public void run() {
			onStart();

			for ( ; mIsRunning ; ) {
				onLoop();
			}

			mIsRunning = false;
			onStop();
		}

		public void stopThread() {
			mIsRunning = false;
		}

		public boolean isRunning() {
			return mIsRunning;
		}

		protected void onStart() {
			if (DEBUG) Log.v("LooperThread", "onStart:" + thread_cnt++);
		}

		protected abstract void onLoop();

		protected void onStop() {
			if (DEBUG) Log.v("LooperThread", "onStop:" + --thread_cnt);
		}

	}

	/** 機体との接続処理用スレッド */
	private class ConnectionThread extends Thread {
		private ARDISCOVERY_ERROR_ENUM error;

		@Override
		public void run() {
			error = discoveryData.ControllerConnection(discoveryPort, discoveryIp);
			if (error != ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_OK) {
				Log.e(TAG, "Error while opening discovery connection : " + error);
			}

            // discoverSemaphore can be disposed
			discoverSemaphore.release();
		}

		public ARDISCOVERY_ERROR_ENUM getError() {
			return error;
		}
	}

	/** 機体からのデータ受信タイムアウト[ミリ秒] */
	private static final int MAX_READ_TIMEOUT_MS = 1000;

	/** 機体からデータを受信するためのスレッド */
	private class ReaderThread extends LooperThread {
		private final int mBufferId;
		private final ARCommand dataRecv = new ARCommand(128 * 1024);//TODO define

		public ReaderThread(final int bufferId) {
			mBufferId = bufferId;
		}

		@Override
		public void onLoop() {
			boolean skip = false;

            /* read data */
			final ARNETWORK_ERROR_ENUM netError = mARNetManager.readDataWithTimeout(mBufferId, dataRecv, MAX_READ_TIMEOUT_MS);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				// FIXME 正常終了以外の時
//				if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_ERROR_BUFFER_EMPTY) {
//                    Log.e(TAG, "ReaderThread readDataWithTimeout() failed. " + netError + " mBufferId: " + mBufferId);
//				}
				skip = true;
			}

			if (!skip) {
				// 正常終了時の処理
				final ARCOMMANDS_DECODER_ERROR_ENUM decodeStatus = dataRecv.decode();
				if ((decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_OK)
					&& (decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_ERROR_NO_CALLBACK)
					&& (decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_ERROR_UNKNOWN_COMMAND)) {
					// デコードに失敗した時の処理
					Log.e(TAG, "ARCommand.decode() failed. " + decodeStatus);
				}
			}
		}

		@Override
		public void onStop() {
			dataRecv.dispose();
			super.onStop();
		}
	}

	/** 機体との接続のコールバック用インターフェース */
	public interface NetworkNotificationListener {
		public void networkDidSendFrame (final NetworkNotificationData notificationData);
		public void networkDidReceiveAck (final NetworkNotificationData notificationData);
		public void networkTimeoutOccurred (final NetworkNotificationData notificationData);
		public void networkDidCancelFrame (final NetworkNotificationData notificationData);
	}

	public static abstract class NetworkNotificationData {
		public NetworkNotificationData() {
		}

		public void notificationRun() {
		}
	}

	/** 機体へのコマンド送信時のコールバック情報保持用クラス */
	private static class ARNetworkSendInfo {
		private ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM mTimeoutPolicy;
		private NetworkNotificationListener mNotificationListener;
		private NetworkNotificationData mNotificationData;
		private IDeviceController mDeviceController;

		public ARNetworkSendInfo(final ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM timeoutPolicy,
			final NetworkNotificationListener notificationListener,
			final NetworkNotificationData notificationData,
			final IDeviceController deviceController) {

			mTimeoutPolicy = timeoutPolicy;
			mNotificationListener = notificationListener;
			mNotificationData = notificationData;
			mDeviceController = deviceController;
		}

		public ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM getTimeoutPolicy() {
			return mTimeoutPolicy;
		}

		public NetworkNotificationListener getNotificationListener() {
			return mNotificationListener;
		}

		public NetworkNotificationData getNotificationData() {
			return mNotificationData;
		}

		public IDeviceController getDeviceController() {
			return mDeviceController;
		}

		public void setTimeoutPolicy(final ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM timeoutPolicy) {
			mTimeoutPolicy = timeoutPolicy;
		}

		public void setNotificationListener(final NetworkNotificationListener notificationListener) {
			mNotificationListener = notificationListener;
		}

		public void setUserData(final NetworkNotificationData notificationData) {
			mNotificationData = notificationData;
		}

		public void setDeviceController(final IFlightController deviceController) {
			mDeviceController = deviceController;
		}

	}

}
