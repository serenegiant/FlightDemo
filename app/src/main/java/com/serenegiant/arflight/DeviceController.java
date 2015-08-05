package com.serenegiant.arflight;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_DECODER_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateBatteryStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateSensorsStatesListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductNameChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialHighChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductSerialLowChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonSettingsStateProductVersionChangedListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryConnection;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNETWORK_ERROR_ENUM;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM;
import com.parrot.arsdk.arnetwork.ARNetworkIOBufferParam;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_ERROR_ENUM;
import com.parrot.arsdk.arnetworkal.ARNETWORKAL_FRAME_TYPE_ENUM;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.parrot.arsdk.arsal.ARNativeData;
import com.parrot.arsdk.arsal.ARSALPrint;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class DeviceController implements IDeviceController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = "DeviceController";

	protected static final int iobufferC2dNak = 10;
	protected static final int iobufferC2dAck = 11;
	protected static final int iobufferC2dEmergency = 12;
	protected static final int iobufferD2cNavdata = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 1;
	protected static final int iobufferD2cEvents = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 2;

	protected static final int ackOffset = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2);

	protected static final List<ARNetworkIOBufferParam> c2dParams = new ArrayList<ARNetworkIOBufferParam>();
	protected static final List<ARNetworkIOBufferParam> d2cParams = new ArrayList<ARNetworkIOBufferParam>();
	protected static int commandsBuffers[] = {};

	protected static final int bleNotificationIDs[] = new int[] {
		iobufferD2cNavdata,
		iobufferD2cEvents,
		(iobufferC2dAck + ackOffset),
		(iobufferC2dEmergency + ackOffset)
	};

	protected android.content.Context mContext;

	protected ARNetworkALManager mARManager;
	protected ARNetworkManager mARNetManager;
	protected boolean mMediaOpened;

	private int c2dPort;
	private int d2cPort;
	private Thread rxThread;
	private Thread txThread;

	private final List<ReaderThread> mReaderThreads = new ArrayList<ReaderThread>();
	private ARDiscoveryConnection mDiscoveryData;

	private LooperThread mLooperThread;

	protected final Object mDataSync = new Object();
	private final DataPCMD mDataPCMD = new DataPCMD();
	private final DataPCMD mSendingPCMD = new DataPCMD();
	private ARDiscoveryDeviceService mDeviceService;
	private volatile boolean mIsStarted;

	private final Object mListenerSync = new Object();
	private final List<DeviceConnectionListener> mConnectionListeners = new ArrayList<DeviceConnectionListener>();
	private final List<DeviceControllerListener> mListeners = new ArrayList<DeviceControllerListener>();

	static {
		// コントローラー => 機体へのパラメータ
		c2dParams.clear();
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dNak,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			1,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			true)
		);
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dAck,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false)
		);
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dEmergency,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			1,
			100,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			1,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false)
		);

		// 機体 => コントローラーへのパラメータ
		d2cParams.clear();
		d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cNavdata,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false)
		);
		d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cEvents,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false)
		);

		commandsBuffers = new int[] {
			iobufferD2cNavdata,
			iobufferD2cEvents,
		};

	}

	public DeviceController(final Context context, final ARDiscoveryDeviceService service) {
		mDeviceService = service;
		mContext = context;
	}

	@Override
	public String getName() {
		return mDeviceService != null ? mDeviceService.getName() : null;
	}

	@Override
	public ARDiscoveryDeviceService getDevice() {
		return mDeviceService;
	}

	@Override
	public boolean start() {
		if (DEBUG) Log.d(TAG, "start ...");

		boolean failed = mIsStarted;
		if (!mIsStarted) {
			registerARCommandsListener();

			failed = startNetwork();
			if (!failed) {
				/* start the reader threads */
				startReadThreads();
				/* start the looper thread */
				startLooperThread();

				onStarted();
				mIsStarted = true;
			}
		}

		return failed;
	}

	@Override
	public void stop() {
		if (DEBUG) Log.d(TAG, "stop ...");

		mIsStarted = false;
		unregisterARCommandsListener();

        /* Cancel the looper thread and block until it is stopped. */
		stopLooperThread();

        /* cancel all reader threads and block until they are all stopped. */
		stopReaderThreads();

        /* ARNetwork cleanup */
		stopNetwork();

	}

	@Override
	public boolean isStarted() {
		return mIsStarted;
	}

	protected void onStarted() {
		//only with RollingSpider in version 1.97 : date and time must be sent to permit a reconnection
		final Date currentDate = new Date(System.currentTimeMillis());
		sendDate(currentDate);
		sendTime(currentDate);
		sendAllSettings();
		sendAllStates();
	}

	private boolean startNetwork() {
		boolean failed = false;
		int pingDelay = 0; /* 0 means default, -1 means no ping */

        /* Create the looper ARNetworkALManager */
		mARManager = new ARNetworkALManager();


        /* setup ARNetworkAL for BLE */

		final ARDiscoveryDeviceBLEService bleDevice = (ARDiscoveryDeviceBLEService) mDeviceService.getDevice();

		final ARNETWORKAL_ERROR_ENUM netALError = mARManager.initBLENetwork(mContext, bleDevice.getBluetoothDevice(), 1, bleNotificationIDs);

		if (netALError == ARNETWORKAL_ERROR_ENUM.ARNETWORKAL_OK) {
			mMediaOpened = true;
			pingDelay = -1; /* Disable ping for BLE networks */
		} else {
			ARSALPrint.e(TAG, "error occured: " + netALError.toString());
			failed = true;
		}

		if (!failed) {
			/* Create the ARNetworkManager */
			mARNetManager = new ARNetworkManagerExtend(mARManager, c2dParams.toArray(new ARNetworkIOBufferParam[c2dParams.size()]), d2cParams.toArray(new ARNetworkIOBufferParam[d2cParams.size()]), pingDelay);

			if (mARNetManager.isCorrectlyInitialized() == false) {
				ARSALPrint.e(TAG, "new ARNetworkManager failed");
				failed = true;
			}
		}

		if (!failed) {
            /* Create and start Tx and Rx threads */
			rxThread = new Thread(mARNetManager.m_receivingRunnable);
			rxThread.start();

			txThread = new Thread(mARNetManager.m_sendingRunnable);
			txThread.start();
		}

		return failed;
	}

	private void startReadThreads() {
        /* Create the reader threads */
		for (final int bufferId : commandsBuffers) {
			final ReaderThread readerThread = new ReaderThread(bufferId);
			mReaderThreads.add(readerThread);
		}

        /* Mark all reader threads as started */
		for (final ReaderThread readerThread : mReaderThreads) {
			readerThread.start();
		}
	}

	private void startLooperThread() {
        /* Create the looper thread */
		mLooperThread = new ControllerLooperThread();

        /* Start the looper thread. */
		mLooperThread.start();
	}

	private void stopLooperThread() {
		if (DEBUG) Log.v(TAG, "stopLooperThread:");
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mLooperThread) {
			mLooperThread.stopThread();
			try {
				mLooperThread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (DEBUG) Log.v(TAG, "stopLooperThread:finished");
	}

	private void stopReaderThreads() {
		if (DEBUG) Log.v(TAG, "stopReaderThreads:");
		/* cancel all reader threads and block until they are all stopped. */
		for (final ReaderThread thread : mReaderThreads) {
			thread.stopThread();
		}
		for (final ReaderThread thread : mReaderThreads) {
			try {
				thread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		mReaderThreads.clear();
		if (DEBUG) Log.v(TAG, "stopReaderThreads:finished");
	}

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
				e.printStackTrace();
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
		if (DEBUG) Log.v(TAG, "stopNetwork:finished");
	}

//================================================================================
// 機体からのデータ/状態受信時の処理関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
//		ARCommand.setCommonCommonStateAllStatesChangedListener(mARCommandCommonCommonStateAllStatesChangedListener);
		ARCommand.setCommonSettingsStateProductNameChangedListener(mARCommandCommonSettingsStateProductNameChangedListener);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(mARCommandCommonSettingsStateProductVersionChangedListener);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(mARCommandCommonSettingsStateProductSerialHighChangedListener);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(mCommonStateBatteryStateChangedListener);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(mARCommandCommonCommonStateSensorsStatesListChangedListener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
//		ARCommand.setCommonCommonStateAllStatesChangedListener(null);
		ARCommand.setCommonSettingsStateProductNameChangedListener(null);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(null);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(null);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(null);
	}

	/**
	 * AllStatesChangedListener・・・値を読める訳じゃないのでコメントアウト
	 */
/*	private final ARCommandCommonCommonStateAllStatesChangedListener
		mARCommandCommonCommonStateAllStatesChangedListener
			= new ARCommandCommonCommonStateAllStatesChangedListener() {
		@Override
		public void onCommonCommonStateAllStatesChangedUpdate() {
		}
	}; */

	private String mProductName;
	private final ARCommandCommonSettingsStateProductNameChangedListener
		mARCommandCommonSettingsStateProductNameChangedListener
		= new ARCommandCommonSettingsStateProductNameChangedListener() {
		@Override
		public void onCommonSettingsStateProductNameChangedUpdate(final String s) {
			mProductName = s;
		}
	};

	@Override
	public String getSoftwareVersion() {
		return mProduct.software;
	}

	@Override
	public String getHardwareVersion() {
		return mProduct.hardware;
	}

	private final AttributeVersion mProduct = new AttributeVersion();
	private final ARCommandCommonSettingsStateProductVersionChangedListener
		mARCommandCommonSettingsStateProductVersionChangedListener
		= new ARCommandCommonSettingsStateProductVersionChangedListener() {
		@Override
		public void onCommonSettingsStateProductVersionChangedUpdate(final String software, final String hardware) {
			mProduct.software = software;
			mProduct.hardware = hardware;
		}
	};

	@Override
	public String getSerial() {
		return mSerialHigh + mSerialLow;
	}

	private String mSerialHigh, mSerialLow;
	private final ARCommandCommonSettingsStateProductSerialHighChangedListener
		mARCommandCommonSettingsStateProductSerialHighChangedListener
		= new ARCommandCommonSettingsStateProductSerialHighChangedListener() {
		@Override
		public void onCommonSettingsStateProductSerialHighChangedUpdate(final String high) {
			mSerialHigh = high;
		}
	};

	private final ARCommandCommonSettingsStateProductSerialLowChangedListener
		mARCommandCommonSettingsStateProductSerialLowChangedListener
		= new ARCommandCommonSettingsStateProductSerialLowChangedListener() {
		@Override
		public void onCommonSettingsStateProductSerialLowChangedUpdate(final String low) {
			mSerialLow = low;
		}
	};

	/**
	 * バッテリーの残量が変化した時のコールバックリスナー
	 */
	private final ARCommandCommonCommonStateBatteryStateChangedListener
		mCommonStateBatteryStateChangedListener
		= new ARCommandCommonCommonStateBatteryStateChangedListener() {
		@Override
		public void onCommonCommonStateBatteryStateChangedUpdate(final byte b) {
			callOnUpdateBattery(b);
		}
	};

	/**
	 * センサー状態リストが変化した時のコールバックリスナー
	 */
	private final ARCommandCommonCommonStateSensorsStatesListChangedListener
		mARCommandCommonCommonStateSensorsStatesListChangedListener
			= new ARCommandCommonCommonStateSensorsStatesListChangedListener() {
		@Override
		public void onCommonCommonStateSensorsStatesListChangedUpdate(
			final ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM sensor_name, final byte b) {

			switch (sensor_name.getValue()) {
			case SENSOR_IMU: // 0
			case SENSOR_BAROMETER:	// 1
			case SENSOR_ULTRASOUND: // 2
			case SENSOR_GPS: // 3
			case SENSOR_MAGNETOMETER: // 4
			case SENSOR_VERTICAL_CAMERA: // 5
			}
			if (DEBUG) Log.v(TAG, String.format("SensorsStatesListChangedUpdate:%d=%d", sensor_name.getValue(), b));
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
				mListeners.add((DeviceControllerListener)listener);
			}
		}
	}

	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mListenerSync) {
			mConnectionListeners.remove(listener);
			if (listener instanceof DeviceControllerListener) {
				mListeners.remove((DeviceControllerListener)listener);
			}
		}
	}

	protected void callOnDisconnect() {
		synchronized (mListenerSync) {
			for (DeviceConnectionListener listener: mConnectionListeners) {
				if (listener != null) {
					try {
						listener.onDisconnect(this);
					} catch (Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	protected void callOnUpdateBattery(final byte percent) {
		synchronized (mListenerSync) {
			for (DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onUpdateBattery(percent);
					} catch (Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	protected void callOnFlyingStateChangedUpdate(final int state) {
		synchronized (mListenerSync) {
			for (DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlyingStateChangedUpdate(state);
					} catch (Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	protected void callOnAlertStateChangedUpdate(final int state) {
		synchronized (mListenerSync) {
			for (DeviceControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onAlertStateChangedUpdate(state);
					} catch (Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

//================================================================================
//================================================================================
	/**
	 * 操縦コマンドを送信
	 * @return
	 */
	protected abstract boolean sendPCMD(final byte flag, final byte roll, final byte pitch, final byte yaw, final byte gaz, final float psi);

	@Override
	public boolean sendAllSettings() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonSettingsAllSettings();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send AllSettings command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendAllStates() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonAllStates();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send CommonAllStates command.");
		}

		return sentStatus;
	}

	private static final SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat formattedTime = new SimpleDateFormat("'T'HHmmssZZZ", Locale.getDefault());

	@Override
	public boolean sendDate(Date currentDate) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonCurrentDate(formattedDate.format(currentDate));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send date command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendTime(Date currentDate) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();


		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCommonCurrentTime(formattedTime.format(currentDate));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send time command.");
		}

		return sentStatus;
	}

	/**
	 * roll/pitch変更時が移動なのか機体姿勢変更なのかを指示
	 * @param flag 1:移動, 0:機体姿勢変更
	 */
	@Override
	public void setFlag(final byte flag) {
		synchronized (mDataSync) {
			mDataPCMD.flag = flag;
		}
	}

	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇
	 */
	@Override
	public void setGaz(final byte gaz) {
		synchronized (mDataSync) {
			mDataPCMD.gaz = gaz;
		}
	}

	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する, flag=0:機体姿勢変更のみ
	 * @param roll 負:左, 正:右
	 */
	@Override
	public void setRoll(final byte roll) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll;
		}
	}

	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する, flag=0:機体姿勢変更のみ
	 * @param pitch
	 */
	@Override
	public void setPitch(final byte pitch) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch;
		}
	}

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転
	 */
	@Override
	public void setYaw(final byte yaw) {
		synchronized (mDataSync) {
			mDataPCMD.yaw = yaw;
		}
	}

	/**
	 * 北磁極に対する角度を設定・・・でもローリングスパイダーでは動かない
	 * @param psi
	 */
	@Override
	public void setPsi(final float psi) {
		synchronized (mDataSync) {
			mDataPCMD.psi = psi;
		}
	}

	protected final AttributeFloat mMaxAltitude = new AttributeFloat();
	/**
	 * 最大高度設定値を返す
	 * @return
	 */
	@Override
	public AttributeFloat getMaxAltitude() {
		return mMaxAltitude;
	}

	protected final AttributeFloat mMaxTilt = new AttributeFloat();
	@Override
	public AttributeFloat getMaxTilt() {
		return mMaxTilt;
	}

	protected final AttributeFloat mMaxVerticalSpeed = new AttributeFloat();
	@Override
	public AttributeFloat getMaxVerticalSpeed() {
		return mMaxVerticalSpeed;
	}

	protected final AttributeFloat mMaxRotationSpeed = new AttributeFloat();
	@Override
	public AttributeFloat getMaxRotationSpeed() {
		return mMaxRotationSpeed;
	}

	private static final int MOTOR_NUMS = 4;
	protected final AttributeMotor[] mMotors = new AttributeMotor[MOTOR_NUMS];

	@Override
	public AttributeMotor getMotor(final int index) {
		final int n = getMotorNums();
		if ((index >= 0) && (index < n)) {
			return mMotors[index];
		}
		return null;
	}

	/**
	 * Extend of ARNetworkManager implementing the callback
	 */
	private class ARNetworkManagerExtend extends ARNetworkManager {
		public ARNetworkManagerExtend(
			final ARNetworkALManager osSpecificManager, final ARNetworkIOBufferParam[] inputParamArray,
			final ARNetworkIOBufferParam[] outputParamArray, final int timeBetweenPingsMs) {

			super(osSpecificManager, inputParamArray, outputParamArray, timeBetweenPingsMs);
		}

		private static final String TAG = "ARNetworkManagerExtend";

		@Override
		public ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM onCallback(
			final int ioBufferId, final ARNativeData data,
			final ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM status, final Object customData) {

			ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DEFAULT;

			if (status == ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM.ARNETWORK_MANAGER_CALLBACK_STATUS_TIMEOUT) {
				retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP;
			}

			return retVal;
		}

		@Override
		public void onDisconnect(final ARNetworkALManager arNetworkALManager) {
			Log.d(TAG, "onDisconnect ...");
			callOnDisconnect();
		}
	}

	private static final class DataPCMD {
		public byte flag;
		public byte roll;
		public byte pitch;
		public byte yaw;
		public byte gaz;
		public float psi;

		public DataPCMD() {
			flag = 0;
			roll = 0;
			pitch = 0;
			yaw = 0;
			gaz = 0;
			psi = 0.0f;
		}

		private void set(final DataPCMD other) {
			flag = other.flag;
			roll = other.roll;
			pitch = other.pitch;
			yaw = other.yaw;
			gaz = other.gaz;
			psi = other.psi;
		}
	}

	private static int cnt = 0;
	private abstract class LooperThread extends Thread {
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
			if (DEBUG) Log.v("LooperThread", "onStart:" + cnt++);
		}

		protected abstract void onLoop();

		protected void onStop() {
			if (DEBUG) Log.v("LooperThread", "onStop:" + --cnt);
		}

	}

	private static final int MAX_READ_TIMEOUT_MS = 1000;
	private class ReaderThread extends LooperThread {
		final int mBufferId;
		final ARCommand dataRecv = new ARCommand(128 * 1024);//TODO define

		public ReaderThread(final int bufferId) {
			mBufferId = bufferId;
		}

		@Override
		public void onLoop() {
			boolean skip = false;

            /* read data*/
			final ARNETWORK_ERROR_ENUM netError = mARNetManager.readDataWithTimeout(mBufferId, dataRecv, MAX_READ_TIMEOUT_MS);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
//				if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_ERROR_BUFFER_EMPTY) {
//                    ARSALPrint.e(TAG, "ReaderThread readDataWithTimeout() failed. " + netError + " mBufferId: " + mBufferId);
//				}
				skip = true;
			}

			if (!skip) {
				final ARCOMMANDS_DECODER_ERROR_ENUM decodeStatus = dataRecv.decode();
				if ((decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_OK)
					&& (decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_ERROR_NO_CALLBACK)
					&& (decodeStatus != ARCOMMANDS_DECODER_ERROR_ENUM.ARCOMMANDS_DECODER_ERROR_UNKNOWN_COMMAND)) {

					ARSALPrint.e(TAG, "ARCommand.decode() failed. " + decodeStatus);
				}
			}
		}

		@Override
		public void onStop() {
			dataRecv.dispose();
			super.onStop();
		}
	}

	/** sendPCMDを呼び出す間隔[ミリ秒] */
	private static final long CMD_SENDING_INTERVALS_MS = 50;

	protected class ControllerLooperThread extends LooperThread {
		public ControllerLooperThread() {
		}

		@Override
		public void onLoop() {
			final long lastTime = SystemClock.elapsedRealtime();

			final byte flag, roll, pitch, yaw, gaz;
			final float psi;
			synchronized (mDataSync) {
				flag = mDataPCMD.flag;
				roll = mDataPCMD.roll;
				pitch = mDataPCMD.pitch;
				yaw = mDataPCMD.yaw;
				gaz = mDataPCMD.gaz;
				psi = mDataPCMD.psi;
			}
			sendPCMD(flag, roll, pitch, yaw, gaz, psi);

			final long sleepTime = (SystemClock.elapsedRealtime() + CMD_SENDING_INTERVALS_MS) - lastTime;

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
