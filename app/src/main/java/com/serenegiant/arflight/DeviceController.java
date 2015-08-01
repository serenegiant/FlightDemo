package com.serenegiant.arflight;


import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_DECODER_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateBatteryStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingFlatTrimListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingStateFlyingStateChangedListener;
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
import java.util.concurrent.Semaphore;

public class DeviceController {
	private static String TAG = "DeviceController";

	public static final int FLIP_FRONT = 1;
	public static final int FLIP_BACK = 2;
	public static final int FLIP_RIGHT = 3;
	public static final int FLIP_LEFT = 4;

	private static final int iobufferC2dNak = 10;
	private static final int iobufferC2dAck = 11;
	private static final int iobufferC2dEmergency = 12;
	private static final int iobufferD2cNavdata = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 1;
	private static final int iobufferD2cEvents = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2) - 2;

	private static final int ackOffset = (ARNetworkALManager.ARNETWORKAL_MANAGER_BLE_ID_MAX / 2);

	protected static final List<ARNetworkIOBufferParam> c2dParams = new ArrayList<ARNetworkIOBufferParam>();
	protected static final List<ARNetworkIOBufferParam> d2cParams = new ArrayList<ARNetworkIOBufferParam>();
	protected static int commandsBuffers[] = {};

	protected static final int bleNotificationIDs[] = new int[] {
		iobufferD2cNavdata,
		iobufferD2cEvents,
		(iobufferC2dAck + ackOffset),
		(iobufferC2dEmergency + ackOffset)
	};

	private android.content.Context mContext;

	private ARNetworkALManager mARManager;
	private ARNetworkManager mARNetManager;
	private boolean mMediaOpened;

	private int c2dPort;
	private int d2cPort;
	private Thread rxThread;
	private Thread txThread;

	private List<ReaderThread> mReaderThreads;
	private Semaphore mDiscoverSemaphore;
	private ARDiscoveryConnection mDiscoveryData;

	private LooperThread mLooperThread;

	private final Object mDataSync = new Object();
	private final DataPCMD mDataPCMD = new DataPCMD();
	private ARDiscoveryDeviceService mDeviceService;

	private DeviceControllerListener mListener;

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
			true));
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dAck,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false));
		c2dParams.add(new ARNetworkIOBufferParam(iobufferC2dEmergency,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			1,
			100,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			1,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false));

		// 機体 => コントローラーへのパラメータ
		d2cParams.clear();
		d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cNavdata,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_INFINITE_NUMBER,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false));
		d2cParams.add(new ARNetworkIOBufferParam(iobufferD2cEvents,
			ARNETWORKAL_FRAME_TYPE_ENUM.ARNETWORKAL_FRAME_TYPE_DATA_WITH_ACK,
			20,
			500,
			3,
			20,
			ARNetworkIOBufferParam.ARNETWORK_IOBUFFERPARAM_DATACOPYMAXSIZE_USE_MAX,
			false));

		commandsBuffers = new int[] {
			iobufferD2cNavdata,
			iobufferD2cEvents,
		};

	}

	public DeviceController(final android.content.Context context, final ARDiscoveryDeviceService service) {
		mDeviceService = service;
		mContext = context;
		mReaderThreads = new ArrayList<ReaderThread>();
	}

	public boolean start() {
		Log.d(TAG, "start ...");

		registerARCommandsListener();

		final boolean failed = startNetwork();
		if (!failed) {
			/* start the reader threads */
			startReadThreads();
			/* start the looper thread */
			startLooperThread();
		}

		return failed;
	}

	public void stop() {
		Log.d(TAG, "stop ...");

		unregisterARCommandsListener();

        /* Cancel the looper thread and block until it is stopped. */
		stopLooperThread();

        /* cancel all reader threads and block until they are all stopped. */
		stopReaderThreads();

        /* ARNetwork cleanup */
		stopNetwork();

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
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mLooperThread) {
			mLooperThread.stopThread();
			try {
				mLooperThread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void stopReaderThreads() {
		if (mReaderThreads != null) {
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
		}
	}

	private void stopNetwork() {
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
	}


	protected void registerARCommandsListener() {
		ARCommand.setCommonCommonStateBatteryStateChangedListener(
			mARCommandCommonCommonStateBatteryStateChangedListener);
		ARCommand.setMiniDronePilotingFlatTrimListener(
			mARCommandMiniDronePilotingFlatTrimListener);
		ARCommand.setMiniDronePilotingStateFlyingStateChangedListener(
			mARCommandMiniDronePilotingStateFlyingStateChangedListener
		);
	}

	protected void unregisterARCommandsListener() {
		ARCommand.setCommonCommonStateBatteryStateChangedListener(null);
		ARCommand.setMiniDronePilotingFlatTrimListener(null);
		ARCommand.setMiniDronePilotingStateFlyingStateChangedListener(null);
	}

	private final ARCommandCommonCommonStateBatteryStateChangedListener
		mARCommandCommonCommonStateBatteryStateChangedListener
			= new ARCommandCommonCommonStateBatteryStateChangedListener() {
		@Override
		public void onCommonCommonStateBatteryStateChangedUpdate(final byte b) {
			Log.d(TAG, "onCommonCommonStateBatteryStateChangedUpdate ...");

			if (mListener != null) {
				mListener.onUpdateBattery(b);
			}
		}
	};

	private final ARCommandMiniDronePilotingFlatTrimListener
		mARCommandMiniDronePilotingFlatTrimListener
			= new ARCommandMiniDronePilotingFlatTrimListener() {
		@Override
		public void onMiniDronePilotingFlatTrimUpdate() {
			Log.d(TAG, "onMiniDronePilotingFlatTrimUpdate ...");

			if (mListener != null) {
				mListener.onFlatTrimUpdate(true);
			}
		}
	};

	private final ARCommandMiniDronePilotingStateFlyingStateChangedListener
		mARCommandMiniDronePilotingStateFlyingStateChangedListener
			= new ARCommandMiniDronePilotingStateFlyingStateChangedListener() {
		@Override
		public void onMiniDronePilotingStateFlyingStateChangedUpdate(
			ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM arcommands_minidrone_pilotingstate_flyingstatechanged_state_enum) {
			Log.d(TAG, "onMiniDronePilotingStateFlyingStateChangedUpdate ...");

			if (mListener != null) {
				mListener.onFlyingStateChangedUpdate(
					arcommands_minidrone_pilotingstate_flyingstatechanged_state_enum.getValue());
			}
		}
	};

	private boolean sendPCMD() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;
		synchronized (mDataSync) {
			cmdError = cmd.setMiniDronePilotingPCMD(mDataPCMD.flag, mDataPCMD.roll, mDataPCMD.pitch, mDataPCMD.yaw, mDataPCMD.gaz, mDataPCMD.psi);
		}
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent in loop should be sent to a buffer not acknowledged ; here iobufferC2dNak
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dNak, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (!sentStatus) {
			ARSALPrint.e(TAG, "Failed to send PCMD command.");
		}

		return sentStatus;
	}

	public boolean sendTakeoff() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingTakeOff();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (!sentStatus) {
			ARSALPrint.e(TAG, "Failed to send TakeOff command.");
		}

		return sentStatus;
	}

	public boolean sendLanding() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingLanding();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send Landing command.");
		}

		return sentStatus;
	}

	public boolean sendEmergency() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dEmergency
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dEmergency, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	private static final SimpleDateFormat formattedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
	private static final SimpleDateFormat formattedTime = new SimpleDateFormat("'T'HHmmssZZZ", Locale.getDefault());

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

	public boolean sendFlatTrim() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingFlatTrim();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flattrim command.");
			if (mListener != null) {
				mListener.onFlatTrimUpdate(false);
			}
		}

		return sentStatus;
	}

	/**
	 * ミニドローンをフリップ, でもどれを選択肢しても同じ動きしなしてない気がする
	 * @param direction
	 * @return
	 */
	public boolean sendAnimationsFlip(final int direction) {

		ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM _dir;
		switch (direction) {
		case FLIP_FRONT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_FRONT;
			break;
		case FLIP_BACK:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_BACK;
			break;
		case FLIP_RIGHT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_RIGHT;
			break;
		case FLIP_LEFT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_LEFT;
			break;
		default:
			return false;
		}

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneAnimationsFlip(_dir);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flip command.");
			if (mListener != null) {
				mListener.onFlatTrimUpdate(false);
			}
		}

		return sentStatus;
	}

	/**
	 * ミニドローンを自動で指定した角度回転させる…みたい
	 * @param degree
	 * @return
	 */
	public boolean sendAnimationsCap(final int degree) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneAnimationsCap(d);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flip command.");
			if (mListener != null) {
				mListener.onFlatTrimUpdate(false);
			}
		}

		return sentStatus;
	}

	/**
	 * rool/pitch変更時が移動なのか機体姿勢変更なのかを指示
	 * @param flag 1:移動, 0:機体姿勢変更
	 */
	public void setFlag(final byte flag) {
		synchronized (mDataSync) {
			mDataPCMD.flag = flag;
		}
	}

	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇
	 */
	public void setGaz(final byte gaz) {
		synchronized (mDataSync) {
			mDataPCMD.gaz = gaz;
		}
	}

	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する, flag=0:機体姿勢変更のみ
	 * @param roll 負:左, 正:右
	 */
	public void setRoll(final byte roll) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll;
		}
	}

	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する, flag=0:機体姿勢変更のみ
	 * @param pitch
	 */
	public void setPitch(final byte pitch) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch;
		}
	}

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転
	 */
	public void setYaw(final byte yaw) {
		synchronized (mDataSync) {
			mDataPCMD.yaw = yaw;
		}
	}

	/**
	 * 北磁極に対する角度を設定
	 * @param psi
	 */
	public void setPsi(final float psi) {
		synchronized (mDataSync) {
			mDataPCMD.psi = psi;
		}
	}

	/**
	 * コールバックリスナーを設定
	 * @param mListener
	 */
	public void setListener(final DeviceControllerListener mListener) {
		this.mListener = mListener;
	}

	/**
	 * Extend of ARNetworkManager implementing the callback
	 */
	private class ARNetworkManagerExtend extends ARNetworkManager {
		public ARNetworkManagerExtend(final ARNetworkALManager osSpecificManager, final ARNetworkIOBufferParam[] inputParamArray, final ARNetworkIOBufferParam[] outputParamArray, final int timeBetweenPingsMs) {
			super(osSpecificManager, inputParamArray, outputParamArray, timeBetweenPingsMs);
		}

		private static final String TAG = "ARNetworkManagerExtend";

		@Override
		public ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM onCallback(final int ioBufferId, final ARNativeData data, final ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM status, final Object customData) {
			ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DEFAULT;

			if (status == ARNETWORK_MANAGER_CALLBACK_STATUS_ENUM.ARNETWORK_MANAGER_CALLBACK_STATUS_TIMEOUT) {
				retVal = ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP;
			}

			return retVal;
		}

		@Override
		public void onDisconnect(final ARNetworkALManager arNetworkALManager) {
			Log.d(TAG, "onDisconnect ...");

			if (mListener != null) {
				mListener.onDisconnect();
			}
		}
	}

	private class DataPCMD {
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
	}


	private abstract class LooperThread extends Thread {
		private boolean mIsAlive;
		private boolean mIsRunning;

		public LooperThread() {
			mIsRunning = false;
			mIsAlive = true;
		}

		@Override
		public void run() {
			mIsRunning = true;

			onStart();

			while (this.mIsAlive) {
				onLoop();
			}
			onStop();

			mIsRunning = false;
		}

		public void onStart() {

		}

		public abstract void onLoop();

		public void onStop() {

		}

		public void stopThread() {
			mIsAlive = false;
		}

		public boolean ismIsRunning() {
			return mIsRunning;
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
		public void onStart() {

		}

		@Override
		public void onLoop() {
			boolean skip = false;

            /* read data*/
			final ARNETWORK_ERROR_ENUM netError = mARNetManager.readDataWithTimeout(mBufferId, dataRecv, MAX_READ_TIMEOUT_MS);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_ERROR_BUFFER_EMPTY) {
//                    ARSALPrint.e (TAG, "ReaderThread readDataWithTimeout() failed. " + netError + " mBufferId: " + mBufferId);
				}
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

			sendPCMD();

			final long sleepTime = (SystemClock.elapsedRealtime() + CMD_SENDING_INTERVALS_MS) - lastTime;

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
