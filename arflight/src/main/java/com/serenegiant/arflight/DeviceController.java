package com.serenegiant.arflight;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_DECODER_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStartAnimationListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStateListListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStopAllAnimationsListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStopAnimationListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCameraSettingsStateCameraSettingsChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateAllStatesChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateBatteryStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateCurrentDateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateCurrentTimeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageInfoStateListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageStateListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateSensorsStatesListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateWifiSignalChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonControllerStateIsPilotingChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonDebugStatsEventSendPacketListener;
import com.parrot.arsdk.arcommands.ARCommandCommonHeadlightsIntensityListener;
import com.parrot.arsdk.arcommands.ARCommandCommonHeadlightsStateIntensityChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonOverHeatStateOverHeatChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonOverHeatStateOverHeatRegulationChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateAllSettingsChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateAutoCountryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateCountryChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductNameChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialHighChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialLowChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateResetChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener;
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
import com.serenegiant.arflight.attribute.AttributeDrone;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.math.Vector;

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
	private static String TAG = "DeviceController";

	private static final int DEFAULT_VIDEO_FRAGMENT_SIZE = 1000;
	private static final int DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER = 128;

	protected final Context mContext;
	protected final ARNetworkConfig mNetConfig;
	private final ARDiscoveryDeviceService mDeviceService;

	protected ARNetworkALManager mARManager;
	protected ARNetworkManager mARNetManager;
	protected boolean mMediaOpened;

	private final Semaphore disconnectSent = new Semaphore(0);
	private volatile boolean mRequestCancel;
	private final Semaphore cmdGetAllSettingsSent = new Semaphore(0);
	private boolean isWaitingAllSettings;
	private final Semaphore cmdGetAllStatesSent = new Semaphore(0);
	private boolean isWaitingAllStates;

	protected int videoFragmentSize = DEFAULT_VIDEO_FRAGMENT_SIZE;
	protected int videoFragmentMaximumNumber = DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER;
	protected int videoMaxAckInterval;

	private Thread rxThread;
	private Thread txThread;

	private final List<ReaderThread> mReaderThreads = new ArrayList<ReaderThread>();

	private LooperThread mFlightCMDThread;

	private final Object mStateSync = new Object();
	private int mState = STATE_STOPPED;

	private final Object mDataSync = new Object();
	private final DataPCMD mDataPCMD = new DataPCMD();

	private final Object mListenerSync = new Object();
	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();
	private final List<DeviceControllerListener> mListeners = new ArrayList<DeviceControllerListener>();

	protected AttributeDrone mInfo;
	protected DroneSettings mSettings;
	protected DroneStatus mStatus;

	public DeviceController(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		mContext = context;
		mDeviceService = service;
		mNetConfig = net_config;
	}

	@Override
	public String getName() {
		return mDeviceService != null ? mDeviceService.getName() : null;
	}

	@Override
	public String getProductName() {
		return mDeviceService != null ? ARDiscoveryService.getProductName(ARDiscoveryService.getProductFromProductID(mDeviceService.getProductID())) : null;
	}

	public int getProductId() {
		return mDeviceService != null ? mDeviceService.getProductID() : 0;
	}

	@Override
	public ARDiscoveryDeviceService getDeviceService() {
		return mDeviceService;
	}

	public ARNetworkConfig getNetConfig() {
		return mNetConfig;
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return mState + (mStatus.getFlyingState() << 8);
		}
	}

	public boolean isFlying() {
		return mStatus.isFlying();
	}

	@Override
	public int getAlarm() {
		return mStatus.getAlarm();
	}

	@Override
	public int getStillCaptureState() {
		return mStatus.getStillCaptureState();
	}

	@Override
	public int getVideoRecordingState() {
		return mStatus.getVideoRecordingState();
	}

	@Override
	public int getMassStorageId() {
		return mStatus.massStorageId();
	}

	@Override
	public String getMassStorageName() {
		return mStatus.massStorageName();
	}

	/**
	 * 接続開始
	 * @return
	 */
	@Override
	public boolean start() {
		if (DEBUG) Log.v(TAG, "start:");

		synchronized (mStateSync) {
			if (mState != STATE_STOPPED) return false;
			mStatus.setAlarm(DroneStatus.ALARM_NON);
			mState = STATE_STARTING;
		}
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
			if (mNetConfig.hasVideo()) {
				// ビデオストリーミング用スレッドを生成&開始
				startVideoThread();
			}
			// 操縦コマンド送信スレッドを生成&開始
			startFlightCMDThread();

			synchronized (mStateSync) {
				mState = STATE_STARTED;
			}
			onStarted();
		}
		if (DEBUG) Log.v(TAG, "start:finished");

		return failed;
	}

	/**
	 * 接続処理を中断
	 */
	@Override
	public void cancelStart() {
		if (DEBUG) Log.v(TAG, "cancelStart:");
		if (!mRequestCancel) {
			mRequestCancel = true;
			final Object device = mDeviceService.getDevice();
			if (device instanceof ARDiscoveryDeviceNetService) {
				if (discoveryData != null) {
					discoveryData.ControllerConnectionAbort();
				}
			} else if (device instanceof ARDiscoveryDeviceBLEService) {
				mARManager.cancelBLENetwork();
			} else {
				Log.e(TAG, "Unknown network media type.");
			}
			cmdGetAllSettingsSent.release();
			cmdGetAllStatesSent.release();
			//TODO see : reset the semaphores or use signals
		}
		if (DEBUG) Log.v(TAG, "cancelStart:finished");
	}

	@Override
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");

		sendLanding();

		synchronized (mStateSync) {
			if (mState != STATE_STARTED) return;
			mState = STATE_STOPPING;
		}

		// 操縦コマンド送信スレッドを終了(終了するまで戻らない)
		stopFlightCMDThread();
		// ビデオストリーミングスレッドを終了(終了するまで戻らない)
		stopVideoThread();
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

	@Override
	public boolean isStarted() {
		synchronized (mStateSync) {
			return mState == STATE_STARTED;
		}
	}

	@Override
	public boolean isConnected() {
		synchronized (mStateSync) {
			return (mState == STATE_STARTED) && mStatus.isConnected();
		}
	}

	@Override
	public boolean needCalibration() {
		return mStatus.needCalibration();
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

	protected void prepare_nextwork() {
	}

	protected boolean startNetwork() {
		if (DEBUG) Log.v(TAG, "startNetwork:");
		boolean failed = false;
		int pingDelay = 0; /* 0 means default, -1 means no ping */

        /* Create the looper ARNetworkALManager */
		mARManager = new ARNetworkALManager();


		final Object device = mDeviceService.getDevice();
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

			prepare_nextwork();
			final ARNETWORKAL_ERROR_ENUM netALError = mARManager.initWifiNetwork(discoveryIp, c2dPort, d2cPort, 1);

			if (netALError == ARNETWORKAL_ERROR_ENUM.ARNETWORKAL_OK) {
				mMediaOpened = true;
			} else {
				Log.e(TAG, "error occurred: " + netALError.toString());
				failed = true;
			}
		} else if (device instanceof ARDiscoveryDeviceBLEService) {
			// Bluetoothの時
			if (DEBUG) Log.v(TAG, "Bluetooth接続開始");
			final ARDiscoveryDeviceBLEService bleDevice = (ARDiscoveryDeviceBLEService) device;

			prepare_nextwork();
			final ARNETWORKAL_ERROR_ENUM netALError = mARManager.initBLENetwork(
				mContext, bleDevice.getBluetoothDevice(), 1, mNetConfig.getBLENotificationIDs()/*bleNotificationIDs*/);

			if (netALError == ARNETWORKAL_ERROR_ENUM.ARNETWORKAL_OK) {
				mMediaOpened = true;
				pingDelay = -1; /* Disable ping for BLE networks */
			} else {
				Log.e(TAG, "error occurred: " + netALError.toString());
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
		if (DEBUG) Log.v(TAG, "startNetwork:finished:failed=" + failed);
		return failed;
	}

	/** 機体との接続を終了 */
	private void stopNetwork() {
		if (DEBUG) Log.v(TAG, "stopNetwork:");
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

		if ((mARManager != null) && (mMediaOpened)) {
			if (mDeviceService.getDevice() instanceof ARDiscoveryDeviceNetService) {
				mARManager.closeWifiNetwork();
			} else if (mDeviceService.getDevice() instanceof ARDiscoveryDeviceBLEService) {
				mARManager.closeBLENetwork(mContext);
			}

			mMediaOpened = false;
			mARManager.dispose();
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
		final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(mDeviceService.getProductID());

		if (!ARDISCOVERY_PRODUCT_ENUM.ARDISCOVERY_PRODUCT_SKYCONTROLLER.equals(product)) {
			discoveryData = new ARDiscoveryConnection() {
				@Override
				public String onSendJson () {
                    /* send a json with the Device to controller port */
					final JSONObject jsonObject = new JSONObject();
					try {
						jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_D2CPORT_KEY, d2cPort);
					} catch (final JSONException e) {
						Log.w(TAG, e);
					}
					try {
						Log.i(TAG, "android.os.Build.MODEL: "+android.os.Build.MODEL);
						jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_CONTROLLER_NAME_KEY, android.os.Build.MODEL);
					} catch (final JSONException e) {
						Log.w(TAG, e);
					}
					try {
						Log.i(TAG, "android.os.Build.DEVICE: "+android.os.Build.DEVICE);
						jsonObject.put(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_CONTROLLER_TYPE_KEY, android.os.Build.DEVICE);
					} catch (final JSONException e) {
						Log.w(TAG, e);
					}

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
						if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY)) {
							videoFragmentSize = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY);
						}
                        /* Else: leave it to the default value. */
						if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY)) {
							videoFragmentMaximumNumber = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY);
						}
                        /* Else: leave it to the default value. */
						if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY)) {
							videoMaxAckInterval = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY);
						}
                        /* Else: leave it to the default value. */
					} catch (final JSONException e) {
						Log.w(TAG, e);
						error = ARDISCOVERY_ERROR_ENUM.ARDISCOVERY_ERROR;
					}
					return error;
				}
			};
		}

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

	/** 操縦コマンド送信スレッドを生成&開始 */
	private void startFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "startFlightCMDThread");
		if (mFlightCMDThread != null) {
			mFlightCMDThread.stopThread();
		}
        /* Create the looper thread */
		mFlightCMDThread = new FlightCMDThread((mNetConfig.getPCMDLoopIntervalsMs()));

        /* Start the looper thread. */
		mFlightCMDThread.start();
	}

	/** 操縦コマンド送信を終了(終了するまで戻らない) */
	private void stopFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:");
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mFlightCMDThread) {
			mFlightCMDThread.stopThread();
			try {
				mFlightCMDThread.join();
				mFlightCMDThread = null;
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:終了");
	}

	/** ストリーミングデータ受信スレッドを開始(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void startVideoThread() {
	}

	/** ストリーミングデータ受信スレッドを終了(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void stopVideoThread() {
	}

//================================================================================
// 機体からのデータ/状態受信時の処理関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		ARCommand.setCommonSettingsStateAllSettingsChangedListener(mARCommandCommonSettingsStateAllSettingsChangedListener);
		ARCommand.setCommonSettingsStateResetChangedListener(mARCommandCommonSettingsStateResetChangedListener);
		ARCommand.setCommonSettingsStateProductNameChangedListener(mARCommandCommonSettingsStateProductNameChangedListener);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(mARCommandCommonSettingsStateProductVersionChangedListener);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(mARCommandCommonSettingsStateProductSerialHighChangedListener);
		ARCommand.setCommonSettingsStateProductSerialLowChangedListener(mARCommandCommonSettingsStateProductSerialLowChangedListener);
		ARCommand.setCommonSettingsStateCountryChangedListener(mARCommandCommonSettingsStateCountryChangedListener);
		ARCommand.setCommonSettingsStateAutoCountryChangedListener(mARCommandCommonSettingsStateAutoCountryChangedListener);

		ARCommand.setCommonCommonStateAllStatesChangedListener(mARCommandCommonCommonStateAllStatesChangedListener);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(mCommonStateBatteryStateChangedListener);

		ARCommand.setCommonCommonStateMassStorageStateListChangedListener(mARCommandCommonCommonStateMassStorageStateListChangedListener);
		ARCommand.setCommonCommonStateMassStorageInfoStateListChangedListener(mARCommandCommonCommonStateMassStorageInfoStateListChangedListener);
		ARCommand.setCommonCommonStateCurrentDateChangedListener(mARCommandCommonCommonStateCurrentDateChangedListener);
		ARCommand.setCommonCommonStateCurrentTimeChangedListener(mARCommandCommonCommonStateCurrentTimeChangedListener);
		ARCommand.setCommonCommonStateMassStorageInfoRemainingListChangedListener(mARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener);
		ARCommand.setCommonCommonStateWifiSignalChangedListener(mARCommandCommonCommonStateWifiSignalChangedListener);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(mARCommandCommonCommonStateSensorsStatesListChangedListener);

		ARCommand.setCommonOverHeatStateOverHeatChangedListener(mARCommandCommonOverHeatStateOverHeatChangedListener);
		ARCommand.setCommonOverHeatStateOverHeatRegulationChangedListener(mARCommandCommonOverHeatStateOverHeatRegulationChangedListener);

		ARCommand.setCommonControllerStateIsPilotingChangedListener(mARCommandCommonControllerStateIsPilotingChangedListener);

		ARCommand.setCommonWifiSettingsStateOutdoorSettingsChangedListener(mARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener);

		ARCommand.setCommonMavlinkStateMavlinkFilePlayingStateChangedListener(mARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener);

		ARCommand.setCommonCalibrationStateMagnetoCalibrationStateChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationRequiredStateListener(mARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationStartedChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener);

		ARCommand.setCommonCameraSettingsStateCameraSettingsChangedListener(mARCommandCommonCameraSettingsStateCameraSettingsChangedListener);

		ARCommand.setCommonDebugStatsEventSendPacketListener(mARCommandCommonDebugStatsEventSendPacketListener);

		// LED
		ARCommand.setCommonHeadlightsIntensityListener(mARCommandCommonHeadlightsIntensityListener);
		ARCommand.setCommonHeadlightsStateIntensityChangedListener(mARCommandCommonHeadlightsStateIntensityChangedListener);
		// アニメーション
		ARCommand.setCommonAnimationsStartAnimationListener(mARCommandCommonAnimationsStartAnimationListener);
		ARCommand.setCommonAnimationsStopAnimationListener(mARCommandCommonAnimationsStopAnimationListener);
		ARCommand.setCommonAnimationsStopAllAnimationsListener(mARCommandCommonAnimationsStopAllAnimationsListener);
		ARCommand.setCommonAnimationsStateListListener(mARCommandCommonAnimationsStateListListener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setCommonSettingsStateAllSettingsChangedListener(null);
		ARCommand.setCommonSettingsStateResetChangedListener(null);
		ARCommand.setCommonSettingsStateProductNameChangedListener(null);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialLowChangedListener(null);
		ARCommand.setCommonSettingsStateCountryChangedListener(null);
		ARCommand.setCommonSettingsStateAutoCountryChangedListener(null);

		ARCommand.setCommonCommonStateAllStatesChangedListener(null);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(null);

		ARCommand.setCommonCommonStateMassStorageStateListChangedListener(null);
		ARCommand.setCommonCommonStateMassStorageInfoStateListChangedListener(null);
		ARCommand.setCommonCommonStateCurrentDateChangedListener(null);
		ARCommand.setCommonCommonStateCurrentTimeChangedListener(null);
		ARCommand.setCommonCommonStateMassStorageInfoRemainingListChangedListener(null);
		ARCommand.setCommonCommonStateWifiSignalChangedListener(null);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(null);

		ARCommand.setCommonOverHeatStateOverHeatChangedListener(null);
		ARCommand.setCommonOverHeatStateOverHeatRegulationChangedListener(null);

		ARCommand.setCommonControllerStateIsPilotingChangedListener(null);

		ARCommand.setCommonWifiSettingsStateOutdoorSettingsChangedListener(null);

		ARCommand.setCommonMavlinkStateMavlinkFilePlayingStateChangedListener(null);

		ARCommand.setCommonCalibrationStateMagnetoCalibrationStateChangedListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationRequiredStateListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationStartedChangedListener(null);

		ARCommand.setCommonCameraSettingsStateCameraSettingsChangedListener(null);

		ARCommand.setCommonDebugStatsEventSendPacketListener(null);

		// LED関係
		ARCommand.setCommonHeadlightsIntensityListener(null);
		ARCommand.setCommonHeadlightsStateIntensityChangedListener(null);
		// アニメーション
		ARCommand.setCommonAnimationsStartAnimationListener(null);
		ARCommand.setCommonAnimationsStopAnimationListener(null);
		ARCommand.setCommonAnimationsStopAllAnimationsListener(null);
		ARCommand.setCommonAnimationsStateListListener(null);

	}

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
			mSettings.setCountryCode(code);
		}
	};

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
			mSettings.setAutomaticCountry(automatic != 0);
		}
	};

	@Override
	public int getBattery() {
		return mStatus.getBattery();
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
				mStatus.setBattery(percent);
				callOnUpdateBattery(percent);
			}
		}
	};

	/**
	 * 接続されているストレージの一覧が変更された時
	 */
	private final ARCommandCommonCommonStateMassStorageStateListChangedListener
		mARCommandCommonCommonStateMassStorageStateListChangedListener
			= new ARCommandCommonCommonStateMassStorageStateListChangedListener() {
		/**
		 * @param mass_storage_id Mass storage id (unique)
		 * @param name Mass storage name
		 */
		@Override
		public void onCommonCommonStateMassStorageStateListChangedUpdate(
			final byte mass_storage_id, final String name) {

//			if (DEBUG) Log.v(TAG, String.format("onCommonCommonStateMassStorageStateListChangedUpdate:mass_storage_id=%d,name=%s", mass_storage_id, name));
			mStatus.setMassStorage(mass_storage_id, name);
		}
	};

	/**
	 * ストレージの情報が変更された時
	 */
	private final ARCommandCommonCommonStateMassStorageInfoStateListChangedListener
		mARCommandCommonCommonStateMassStorageInfoStateListChangedListener
			= new ARCommandCommonCommonStateMassStorageInfoStateListChangedListener() {
		/**
		 * @param mass_storage_id Mass storage state id (unique)
		 * @param size Mass storage size in MBytes
		 * @param used_size Mass storage used size in MBytes
		 * @param plugged Mass storage plugged (1 if mass storage is plugged, otherwise 0)
		 * @param full Mass storage full information state (1 if mass storage full, 0 otherwise).
		 * @param internal Mass storage internal type state (1 if mass storage is internal, 0 otherwise)
		 */
		@Override
		public void onCommonCommonStateMassStorageInfoStateListChangedUpdate(
			final byte mass_storage_id, final int size, final int used_size, final byte plugged, final byte full, final byte internal) {

//			if (DEBUG) Log.v(TAG, String.format("onCommonCommonStateMassStorageInfoStateListChangedUpdate:mass_storage_id=%d,size=%d,used_size=%d,plugged=%d,full=%d,internal=%d", mass_storage_id, size, used_size, plugged, full, internal));
			callOnUpdateStorageState(mass_storage_id, size, used_size, plugged != 0, full != 0, internal != 0);
		}
	};

	/**
	 * ストレージの空き容量が変化した時
	 */
	private final ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener
		mARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener
		= new ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener() {
		/**
		 * @param free_space Mass storage free space in MBytes
		 * @param rec_time Mass storage record time reamining in minute
		 * @param photo_remaining Mass storage photo remaining
		 */
		@Override
		public void onCommonCommonStateMassStorageInfoRemainingListChangedUpdate(
			final int free_space, final short rec_time, final int photo_remaining) {
			if (DEBUG) Log.v(TAG, String.format("free_space=%d,rec_time=%d,photo_remaining=%d", free_space, rec_time, photo_remaining));
			// FIXME 未実装
			// XXX
		}
	};

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
			// XXX
		}
	};

	/**
	 * センサー状態リストが変化した時のコールバックリスナー
	 */
	private final ARCommandCommonCommonStateSensorsStatesListChangedListener
		mARCommandCommonCommonStateSensorsStatesListChangedListener
			= new ARCommandCommonCommonStateSensorsStatesListChangedListener() {
		/**
		 * @param sensor_name Sensor name
		 * @param sensorState Sensor state (1 if the sensor is OK, 0 if the sensor is NOT OK)
		 */
		@Override
		public void onCommonCommonStateSensorsStatesListChangedUpdate(
			final ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM sensor_name, final byte sensorState) {

			switch (sensor_name.getValue()) {
			case SENSOR_IMU: // 0
			case SENSOR_BAROMETER:	// 1
			case SENSOR_ULTRASOUND: // 2
			case SENSOR_GPS: // 3
			case SENSOR_MAGNETOMETER: // 4
			case SENSOR_VERTICAL_CAMERA: // 5
			}
			if (DEBUG) Log.v(TAG, String.format("SensorsStatesListChangedUpdate:%d=%d", sensor_name.getValue(), sensorState));
		}
	};

	/**
	 * オーバーヒート状態が変化した時
	 */
	private final ARCommandCommonOverHeatStateOverHeatChangedListener
		mARCommandCommonOverHeatStateOverHeatChangedListener
			= new ARCommandCommonOverHeatStateOverHeatChangedListener() {
		@Override
		public void onCommonOverHeatStateOverHeatChangedUpdate() {
			// XXX
		}
	};

	/**
	 * オーバーヒート時の冷却方法設定が変更された時
	 */
	private final ARCommandCommonOverHeatStateOverHeatRegulationChangedListener
		mARCommandCommonOverHeatStateOverHeatRegulationChangedListener
			= new ARCommandCommonOverHeatStateOverHeatRegulationChangedListener() {
		/**
		 * @param regulationType Type of overheat regulation : 0 for ventilation, 1 for switch off
		 */
		@Override
		public void onCommonOverHeatStateOverHeatRegulationChangedUpdate(final byte regulationType) {
			// XXX
		}
	};

	/**
	 * アプリがHUDモードで操縦しているかどうかが変更された
	 */
	private final ARCommandCommonControllerStateIsPilotingChangedListener
		mARCommandCommonControllerStateIsPilotingChangedListener
			= new ARCommandCommonControllerStateIsPilotingChangedListener() {
		/**
		 * @param piloting 0 when the application is not in the piloting HUD, 1 when it enters the HUD.
		 */
		@Override
		public void onCommonControllerStateIsPilotingChangedUpdate(final byte piloting) {
			// XXX
		}
	};

	/**
	 * WiFiの室内/室外モードが変更された時
	 */
	private final ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener
		mARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener
			= new ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener() {
		/**
		 * @param outdoor 1 if it should use outdoor wifi settings, 0 otherwise
		 */
		@Override
		public void onCommonWifiSettingsStateOutdoorSettingsChangedUpdate(final byte outdoor) {
			// XXX
		}
	};

	/**
	 * Mavlinkファイルの再生状態が変化した時
	 */
	private final ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener
		mARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener
			= new ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener() {
		/**
		 * @param state State of the mavlink
		 * @param filepath flight plan file path from the mavlink ftp root
		 * @param type type of the played mavlink file
		 */
		@Override
		public void onCommonMavlinkStateMavlinkFilePlayingStateChangedUpdate(
			final ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM state,
			final String filepath, final ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM type) {
			// XXX
		}
	};

	/**
	 * キャリブレーションの状態が変わった時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener() {
		/**
		 * @param xAxisCalibration State of the x axis (roll) calibration : 1 if calibration is done, 0 otherwise
		 * @param yAxisCalibration State of the y axis (pitch) calibration : 1 if calibration is done, 0 otherwise
		 * @param zAxisCalibration State of the z axis (yaw) calibration : 1 if calibration is done, 0 otherwise
		 * @param calibrationFailed 1 if calibration has failed, 0 otherwise. If this arg is 1, consider all previous arg as 0
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationStateChangedUpdate(
			final byte xAxisCalibration, final byte yAxisCalibration, final byte zAxisCalibration, final byte calibrationFailed) {

			if (DEBUG) Log.v(TAG, String.format("CalibrationStateChangedUpdate:(%d/%d/%d)%d", xAxisCalibration, yAxisCalibration, zAxisCalibration, calibrationFailed));
			mStatus.updateCalibrationState(xAxisCalibration == 1, yAxisCalibration == 1, zAxisCalibration == 1, calibrationFailed == 1);
			callOnCalibrationRequiredChanged(calibrationFailed == 1);
		}
	};

	/**
	 * キャリブレーションが必要な時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener
		mARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener() {
		/**
		 * @param required 1 if calibration is required, 0 if current calibration is still valid
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationRequiredStateUpdate(final byte required) {
			if (DEBUG) Log.v(TAG, "CalibrationRequiredStateUpdate:" + required);

			mStatus.needCalibration(required != 0);
			callOnCalibrationRequiredChanged(required != 0);
		}
	};

	/**
	 * キャリブレーション中の軸が変更された時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener() {
		/**
		 * @param axis The axis to calibrate, 0:x, 1:y, 2:z, 3:none
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedUpdate(
			final ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM axis) {

			if (DEBUG) Log.v(TAG, "CalibrateAxisChanged:" + axis.getValue());
			callOnCalibrationAxisChanged(axis.getValue());
		}
	};

	/**
	 * キャリブレーションを開始/終了した時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener() {
		/**
		 * @param started 1 if calibration has started, 0 otherwise
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationStartedChangedUpdate(final byte started) {

			callOnCalibrationStartStop(started == 1);
		}
	};

	/**
	 * カメラ設定を受け取った時
	 */
	private final ARCommandCommonCameraSettingsStateCameraSettingsChangedListener
		mARCommandCommonCameraSettingsStateCameraSettingsChangedListener
			= new ARCommandCommonCameraSettingsStateCameraSettingsChangedListener() {
		/**
		 * @param fov Value of the camera horizontal fov (in degree)
		 * @param panMax Value of max pan (right pan) (in degree)
		 * @param panMin Value of min pan (left pan) (in degree)
		 * @param tiltMax Value of max tilt (top tilt) (in degree)
		 * @param tiltMin Value of min tilt (bottom tilt) (in degree)
		 */
		@Override
		public void onCommonCameraSettingsStateCameraSettingsChangedUpdate(
			final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
			mSettings.setCameraSettings(fov, panMax, panMin, tiltMax, tiltMin);
		}
	};

	private final ARCommandCommonDebugStatsEventSendPacketListener
		mARCommandCommonDebugStatsEventSendPacketListener
			= new ARCommandCommonDebugStatsEventSendPacketListener() {
		/**
		 * @param packet packet from drone
		 */
		@Override
		public void onCommonDebugStatsEventSendPacketUpdate(final String packet) {
			// XXX
		}
	};

	/**
	 * LED強度の変更通知?
	 */
	private final ARCommandCommonHeadlightsIntensityListener
		mARCommandCommonHeadlightsIntensityListener
			= new ARCommandCommonHeadlightsIntensityListener() {
		@Override
		public void onCommonHeadlightsIntensityUpdate(final byte left, final byte right) {
			// FIXME 未実装
			if (DEBUG) Log.v(TAG, String.format("onCommonHeadlightsIntensityUpdate(%d,%d)", left, right));
		}
	};

	/**
	 * LED強度の変更通知?
	 */
	private final ARCommandCommonHeadlightsStateIntensityChangedListener
		mARCommandCommonHeadlightsStateIntensityChangedListener
			= new ARCommandCommonHeadlightsStateIntensityChangedListener() {
		@Override
		public void onCommonHeadlightsStateIntensityChangedUpdate(final byte left, final byte right) {
			// FIXME 未実装
			if (DEBUG) Log.v(TAG, String.format("onCommonHeadlightsIntensityUpdate(%d,%d)", left, right));
		}
	};

	/**
	 * アニメーション動作開始通知
	 */
	private final ARCommandCommonAnimationsStartAnimationListener
		mARCommandCommonAnimationsStartAnimationListener
			= new ARCommandCommonAnimationsStartAnimationListener() {
		@Override
		public void onCommonAnimationsStartAnimationUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM anim) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStartAnimationUpdate:" + anim);
		}
	};

	/**
	 * アニメーション動作終了通知
	 */
	private final ARCommandCommonAnimationsStopAnimationListener
		mARCommandCommonAnimationsStopAnimationListener
			= new ARCommandCommonAnimationsStopAnimationListener() {
		@Override
		public void onCommonAnimationsStopAnimationUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM anim) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStopAnimationUpdate:" + anim);
		}
	};

	/**
	 * 全てのアニメーション動作終了したことの通知
	 */
	private final ARCommandCommonAnimationsStopAllAnimationsListener
		mARCommandCommonAnimationsStopAllAnimationsListener
			= new ARCommandCommonAnimationsStopAllAnimationsListener() {
		@Override
		public void onCommonAnimationsStopAllAnimationsUpdate() {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStopAnimationUpdate:");
		}
	};

	/**
	 * アニメーションリスト変更通知
	 */
	private final ARCommandCommonAnimationsStateListListener
		mARCommandCommonAnimationsStateListListener
			= new ARCommandCommonAnimationsStateListListener() {

		@Override
		public void onCommonAnimationsStateListUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM anim,
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM state,
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM error) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStateListUpdate:anim=" + anim + ",state=" + state + ",error=" + error);
		}
	};
//================================================================================
// コールバック関係
//================================================================================
	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	@Override
	public void addListener(final DeviceConnectionListener listener) {
		synchronized (mListenerSync) {
			mConnectionListeners.add(listener);
			if (listener instanceof DeviceControllerListener) {
				mListeners.add((DeviceControllerListener) listener);
				callOnUpdateBattery(getBattery());
				callOnAlarmStateChangedUpdate(mStatus.getAlarm());
				callOnFlyingStateChangedUpdate(mStatus.getFlyingState());
			}
		}
	}

	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mListenerSync) {
			mConnectionListeners.remove(listener);
			if (listener instanceof DeviceControllerListener) {
				mListeners.remove((DeviceControllerListener) listener);
			}
		}
	}

	/**
	 * 接続時のコールバックを呼び出す
	 */
	protected void callOnConnect() {
		synchronized (mListenerSync) {
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
		synchronized (mListenerSync) {
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
	 * バッテリー残量変更コールバックを呼び出す
	 */
	protected void callOnUpdateBattery(final int percent) {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onUpdateBattery(percent);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 飛行ステータス変更コールバックを呼び出す
	 * @param state
	 */
	protected void callOnFlyingStateChangedUpdate(final int state) {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlyingStateChangedUpdate(state);
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
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onAlarmStateChangedUpdate(state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * フラットトリム実行完了時のコールバックを呼び出す
	 */
	protected void callOnFlatTrimChanged() {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlatTrimChanged();
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション状態が変更された時のコールバックを呼び出す
	 * @param need_calibration
	 */
	protected void callOnCalibrationRequiredChanged(final boolean need_calibration) {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationRequiredChanged(need_calibration);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーションを開始/終了した時のコールバックを呼び出す
	 * @param is_start
	 */
	protected void callOnCalibrationStartStop(final boolean is_start) {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationStartStop(is_start);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション中の軸が変更された時のコールバックを呼び出す
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	protected void callOnCalibrationAxisChanged(final int axis) {
		synchronized (mListenerSync) {
			for (final DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationAxisChanged(axis);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnStillCaptureStateChanged(final int state) {
		final boolean changed = mStatus.setStillCaptureState(state);
		if (changed) {
			synchronized (mListenerSync) {
				for (final DeviceControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onStillCaptureStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnVideoRecordingStateChanged(final int state) {
		final boolean changed = mStatus.setVideoRecordingState(state);
		if (changed) {
			synchronized (mListenerSync) {
				for (final DeviceControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onVideoRecordingStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 機体のストレージ状態が変化した時のコールバックを呼び出す
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	protected void callOnUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
		final boolean changed = mStatus.setMassStorageInfo(mass_storage_id, size, used_size, plugged, full, internal);
		if (changed) {
			synchronized (mListenerSync) {
				for (final DeviceControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onUpdateStorageState(mass_storage_id, size, used_size, plugged, full, internal);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}
//================================================================================
//================================================================================

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
	 * 操縦コマンドを送信
	 * @param flag flag to activate roll/pitch movement
	 * @param roll [-100,100]
	 * @param pitch [-100,100]
	 * @param yaw [-100,100]
	 * @param gaz [-100,100]
	 * @param heading [-180,180] (無効みたい)
	 * @return
	 */
	protected abstract boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading);

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
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dAck
/*			final ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(mNetConfig.getC2dAckId(), cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				Log.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			} */
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
	 * アプリがHUDモードで操縦しているかどうかを設定
	 * @param inHUD
	 * @return
	 */
	public boolean sendPilotingHUD(final boolean inHUD) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonControllerStateIsPilotingChanged((byte) (inHUD ? 1 : 0));
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

	/**
	 * キャリブレーション開始/キャンセル要求
	 * @param start true:開始, false:キャンセル
	 * @return
	 */
	public boolean sendCalibration(final boolean start) {

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCalibrationMagnetoCalibration(start ? (byte)1 : (byte)0);
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
//********************************************************************************
// 操縦関係
//********************************************************************************
	/**
	 * roll/pitch変更時が移動かどうか
	 * @param flag 1:移動
	 */
	@Override
	public void setFlag(final int flag) {
		synchronized (mDataSync) {
			mDataPCMD.flag = flag == 0 ? 0 : (flag != 0 ? 1 : 0);
		}
	}

	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇
	 */
	@Override
	public void setGaz(final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.gaz = gaz > 100 ? 100 : (gaz < -100 ? -100 : gaz);
		}
	}

	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する
	 * @param roll 負:左, 正:右
	 */
	@Override
	public void setRoll(final float roll) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
		}
	}

	/**
	 * 機体を左右に傾ける
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setRoll(final float roll, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する
	 * @param pitch
	 */
	@Override
	public void setPitch(final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
		}
	}

	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setPitch(final float pitch, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転
	 */
	@Override
	public void setYaw(final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.yaw = yaw > 100 ? 100 : (yaw < -100 ? -100 : yaw);
		}
	}

	/**
	 * 北磁極に対する角度を設定・・・機体側で実装されてない
	 * @param heading
	 */
	@Override
	public void setHeading(final float heading) {
		synchronized (mDataSync) {
			mDataPCMD.heading = heading;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 * @param flag roll/pitchが移動を意味する時1
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, int flag) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = flag;
		}
	}

	protected void getPCMD(final DataPCMD dest) {
		if (dest != null) {
			synchronized (mDataSync) {
				dest.set(mDataPCMD);
			}
		}
	}

	/**
	 * 最大高度設定値を返す
	 * @return
	 */
	@Override
	public AttributeFloat getMaxAltitude() {
		return mSettings.maxAltitude();
	}

	@Override
	public AttributeFloat getMaxTilt() {
		return mSettings.maxTilt();
	}

	@Override
	public AttributeFloat getMaxVerticalSpeed() {
		return mSettings.maxVerticalSpeed();
	}

	@Override
	public AttributeFloat getMaxRotationSpeed() {
		return mSettings.maxRotationSpeed();
	}

	@Override
	public Vector getAttitude(){
		return mStatus.attitude();
	}

	public float getAltitude() {
		return (float)mStatus.altitude();
	}

	/**
	 * モーターの自動カット機能が有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isCutoffMode() {
		return mSettings.cutOffMode();
	}

	/**
	 * 自動離陸モードが有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isAutoTakeOffModeEnabled() {
		return mSettings.autoTakeOffMode();
	}

	@Override
	public boolean hasGuard() {
		return mSettings.hasGuard();
	}

	@Override
	public AttributeMotor getMotor(final int index) {
		return mStatus.getMotor(index);
	}

	/**
	 * LEDの明るさをセット
	 * @param left [0,255], 範囲外は256の剰余を適用
	 * @param right [0,255], 範囲外は256の剰余を適用
	 * @return
	 */
	@Override
	public boolean sendHeadlightsIntensity(final int left, final int right) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonHeadlightsIntensity((byte)(left % 256), (byte)(right % 256));
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

	/**
	 * 指定したアニメーション動作を開始。全部動くんかな?
	 * 共通のコマンドやけどJumpingSumoでしか動かないような予感。
	 * @param animation [0,12]
	 * @return
	 */
	@Override
	public boolean sendStartAnimation(final int animation) {
		// FIXME 実行開始したアニメーション動作を保持してコールバック&stopAnimationActionで更新するようにした方がいいのかも

//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN(3, "Spin animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_TAP(4, "Tap animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_METRONOME(6, "Metronome animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ONDULATION(7, "Standing dance animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIRAL(10, "Spiral animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SLALOM(11, "Slalom animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_BOOST(12, "Boost animation."),
		final ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM anim = ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM.getFromValue(animation);
		if (anim == ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_UNKNOWN_ENUM_VALUE)
			return false;

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStartAnimation(anim);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send StartAnimation command.");
		}

		return sentStatus;
	}

	/**
	 * 指定したアニメーション動作を停止。全部動くんかな?
	 * @param animation [0,12]
	 * @return
	 */
	@Override
	public boolean sendStopAnimation(final int animation) {
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN(3, "Spin animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_TAP(4, "Tap animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_METRONOME(6, "Metronome animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ONDULATION(7, "Standing dance animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIRAL(10, "Spiral animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SLALOM(11, "Slalom animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_BOOST(12, "Boost animation."),

		final ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM anim = ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM.getFromValue(animation);
		if (anim == ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_UNKNOWN_ENUM_VALUE)
			return false;
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStopAnimation(anim);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send startAnimation command.");
		}

		return sentStatus;
	}

	/**
	 * 実行中のアニメーション動作を全て停止させる
	 * @return
	 */
	@Override
	public boolean sendStopAllAnimation() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStopAllAnimations();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send stopAllAnimations command.");
		}

		return sentStatus;
	}

	/**
	 * アニメーション動作関係らしいけど何するメソッドかよくわからん
	 * @param anim
	 * @param state
	 * @param error
	 * @return
	 */
	public boolean sendAnimationStateList(final int anim, final int state, final int error) {
		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM _anim = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM.getFromValue(anim);
		if (_anim == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN(3, "Spin animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_TAP(4, "Tap animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_METRONOME(6, "Metronome animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ONDULATION(7, "Standing dance animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIRAL(10, "Spiral animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SLALOM(11, "Slalom animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_BOOST(12, "Boost animation."),

		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM _state = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM.getFromValue(state);
		if (_state == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_STOPPED(0, "animation is stopped"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_STARTED(1, "animation is started"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_NOTAVAILABLE(2, "The animation is not available"),

		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM _error = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM.getFromValue(error);
		if (_error == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_OK(0, "No Error"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_UNKNOWN(1, "Unknown generic error"),

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStateList(_anim, _state, _error);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AnimationsStateList command.");
		}

		return sentStatus;
	}
//********************************************************************************
// データ送受信関係
//********************************************************************************
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
				retVal = sendInfo.getTimeoutPolicy();

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
			mStatus.setAlarm(DroneStatus.ALARM_DISCONNECTED);
			callOnAlarmStateChangedUpdate(DroneStatus.ALARM_DISCONNECTED);
			callOnDisconnect();
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

	/** 操縦コマンドの値保持用 */
	protected static final class DataPCMD {
		public int flag;
		public float roll;
		public float pitch;
		public float yaw;
		public float gaz;
		public float heading;

		public DataPCMD() {
			flag = 0;
			roll = pitch = yaw = gaz = heading = 0;
		}

		private void set(final DataPCMD other) {
			flag = other.flag;
			roll = other.roll;
			pitch = other.pitch;
			yaw = other.yaw;
			gaz = other.gaz;
			heading = other.heading;
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

	/**
	 * 操縦コマンド送信スレッドでのループ内の処理(sendPCMDを呼び出す)
	 * 下位クラスで定期的にコマンド送信が必要ならoverride
	 */
	protected void sendCmdInControlLoop() {
		final int flag;
		float roll, pitch, yaw, gaz, heading;
		synchronized (mDataSync) {
			flag = mDataPCMD.flag;
			roll = mDataPCMD.roll;
			pitch = mDataPCMD.pitch;
			yaw = mDataPCMD.yaw;
			gaz = mDataPCMD.gaz;
			heading = mDataPCMD.heading;
		}
		// 操縦コマンド送信
		sendPCMD(flag, (int) roll, (int) pitch, (int)yaw, (int)gaz, (int)heading);
	}

	/** 操縦コマンドを定期的に送信するためのスレッド */
	protected class FlightCMDThread extends LooperThread {
		private final long intervals_ms;
		public FlightCMDThread(final long _intervals_ms) {
			intervals_ms = _intervals_ms;
		}

		@Override
		public void onLoop() {
			final long lastTime = SystemClock.elapsedRealtime();

			final int state;
			synchronized (mStateSync) {
				state = mState;
			}
			if (state == STATE_STARTED) {
				sendCmdInControlLoop();
			}
			// 次の送信予定時間までの休止時間を計算[ミリ秒]
			final long sleepTime = (SystemClock.elapsedRealtime() + intervals_ms) - lastTime;

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				// ignore
			}
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

		public void setDeviceController(final IDeviceController deviceController) {
			mDeviceController = deviceController;
		}

	}
}
