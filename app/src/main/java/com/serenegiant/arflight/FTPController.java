package com.serenegiant.arflight;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.parrot.arsdk.ardatatransfer.ARDataTransferException;
import com.parrot.arsdk.ardatatransfer.ARDataTransferManager;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloader;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.arsal.ARSALBLEManager;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.parrot.arsdk.arutils.ARUTILS_ERROR_ENUM;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.io.File;
import java.lang.ref.WeakReference;

public abstract class FTPController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "FTPController:";

	protected final WeakReference<Context>mWeakContext;
	protected final WeakReference<IDeviceController>mWeakController;
	protected final FTPHandler mFTPHandler;
	protected final String mExternalDirectory;
	protected String mRemotePath;

	protected ARDataTransferManager mDataTransferManager;
	protected ARUtilsManager mFTPListManager;
	protected ARUtilsManager mFTPQueueManager;
	protected ARDataTransferMediasDownloader mDownLoader;
	protected volatile boolean mRequestCancel;

	public static FTPController newInstance(final Context context, final IDeviceController controller) {
		if (controller instanceof DeviceControllerBebop) {
			return new FTPControllerWiFi(context, controller);
		} else if (controller instanceof DeviceControllerMiniDrone) {
			return new FTPControllerBLE(context, controller);
		}
		return null;
	}

	/**
	 * コンストラクタ
	 * @param context
	 */
	protected FTPController(final Context context, final IDeviceController controller) {
		mWeakContext = new WeakReference<Context>(context);
		mWeakController = new WeakReference<IDeviceController>(controller);
		HandlerThread thread = new HandlerThread("FTPThread");
		thread.start();
		mFTPHandler = new FTPHandler(thread.getLooper());
		mFTPListManager = createARUtilsManager();
		mFTPQueueManager = createARUtilsManager();
		mDataTransferManager = createARDataTransferManager();
		final String productName = controller.getProductName().replace(" ", "_");
		final File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
			"FlightDemo/" + productName);
		path.mkdirs();
		mExternalDirectory = path.getAbsolutePath();
	}

	public void connect() {
		try {
			mFTPHandler.sendEmptyMessage(CMD_CONNECT);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * 関連するリソースを破棄する。必ず呼び出すこと。再利用は出来ない。
	 */
	public void release() {
		cancel();
		try {
			mFTPHandler.sendEmptyMessage(CMD_RELEASE);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * 実行中・実行待ちの処理を中断要求する
	 */
	public void cancel() {
		try {
			// FIXME ここで未実行のCMD_RELEASE以外のコマンドを取り除く
			mFTPHandler.removeMessages(CMD_CANCEL);
			// キャンセル要求
			mRequestCancel = true;
			mFTPHandler.sendEmptyMessage(CMD_CANCEL);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	private ARUtilsManager createARUtilsManager() {
		ARUtilsManager result = null;
		try {
			result = new ARUtilsManager();
		} catch (final ARUtilsException e) {
			Log.w(TAG, e);
		}
		return result;
	}

	private ARDataTransferManager createARDataTransferManager() {
		ARDataTransferManager result = null;
		try {
			result = new ARDataTransferManager();
		} catch (final ARDataTransferException e) {
			Log.w(TAG, e);
		}
		return result;
	}

	/**
	 * FTP接続を初期化
	 * @param controller
	 */
	protected void handleInit(final IDeviceController controller) {
	}

	protected void handleConnect() {
		if (DEBUG) Log.v(TAG, String.format("handleConnect:mExternalDirectory=%s,mRemotePath=%s", mExternalDirectory, mRemotePath));
		final ARSAL_PRINT_LEVEL_ENUM level = ARSALPrint.getMinimumLogLevel();
		try {
			ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_VERBOSE);
			try {
				mDownLoader = mDataTransferManager.getARDataTransferMediasDownloader();
				mDownLoader.createMediasDownloader(mFTPListManager, mFTPQueueManager, mRemotePath, mExternalDirectory);
			} catch (final ARDataTransferException e) {
				Log.w(TAG, e);
			}
		} finally {
			ARSALPrint.setMinimumLogLevel(level);
		}
	}

	/**
	 * FTP接続を終了
	 */
	protected void handleRelease() {
		if (mFTPListManager != null) {
			mFTPListManager.dispose();
			mFTPListManager = null;
		}
		if (mFTPQueueManager != null) {
			mFTPQueueManager.dispose();
			mFTPQueueManager = null;
		}
		if (mDataTransferManager != null) {
			mDataTransferManager.dispose();
			mDataTransferManager = null;
		}
	}

	/**
	 * エラー発生時のコールバックを呼び出す FIXME 未実装
	 * @param e
	 */
	protected void callOnError(final Exception e) {
	}

	private static final int CMD_INIT = 1;
	private static final int CMD_CONNECT = 2;
	private static final int CMD_CANCEL = 3;
	private static final int CMD_RELEASE = 9;

	/**
	 * FTP関係の処理を非同期で実行するためのHandler
	 * HandlerThreadからLooperを取得して生成すること
	 */
	private final class FTPHandler extends Handler {
		public FTPHandler(final Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(final Message msg) {
			switch (msg.what) {
			case CMD_INIT:
				try {
					handleInit((IDeviceController) msg.obj);
				} catch (final Exception e) {
					callOnError(e);
				}
				break;
			case CMD_CONNECT:
				final IDeviceController controller = mWeakController.get();
				mRemotePath = String.format("%s_%03d", controller.getMassStorageName(), controller.getMassStorageId());
				handleConnect();
				break;
			case CMD_CANCEL:
				if (mDownLoader != null) {
					mDownLoader.cancelGetAvailableMedias();
					mDownLoader.cancelQueueThread();
				}
				break;
			case CMD_RELEASE:
				if (mDownLoader != null) {
					mDownLoader.dispose();
					mDownLoader = null;
				}
				try {
					handleRelease();
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				getLooper().quit();
				break;
			}
			super.handleMessage(msg);
		}
	}


	private static final String USER_NAME = "anonymous";
	private static final String PASSWORD = "";

	/**
	 * WiFi接続用のFTP処理クラス
	 */
	public static class FTPControllerWiFi extends FTPController {
		public FTPControllerWiFi(final Context context, final IDeviceController controller) {
			super(context, controller);
			mFTPHandler.sendMessage(mFTPHandler.obtainMessage(CMD_INIT, controller));
		}

		@Override
		protected void handleInit(final IDeviceController controller) {
			final Object device = controller.getDeviceService().getDevice();
			final String hostAddr;
			final int hostPort;
			if (device instanceof ARDiscoveryDeviceNetService) {
				hostAddr = ((ARDiscoveryDeviceNetService)device).getIp();
				hostPort = ((ARDiscoveryDeviceNetService)device).getPort();
			} else {
				throw new IllegalArgumentException("ARDiscoveryDeviceNetServiceじゃない");
			}
			ARUTILS_ERROR_ENUM result = mFTPListManager.initWifiFtp(hostAddr, hostPort, USER_NAME, PASSWORD);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				throw new IllegalArgumentException("initWifiFtpが失敗:err=" + result);
			}
			result = mFTPQueueManager.initWifiFtp(hostAddr, hostPort, USER_NAME, PASSWORD);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				mFTPListManager.closeWifiFtp();
				mFTPListManager.dispose();
				throw new IllegalArgumentException("initWifiFtpが失敗:err=" + result);
			}
			super.handleInit(controller);
		}

		@Override
		protected void handleRelease() {
			if ((mFTPListManager != null) && mFTPListManager.isCorrectlyInitialized()) {
				mFTPListManager.closeWifiFtp();
			}
			if ((mFTPQueueManager != null) && mFTPQueueManager.isCorrectlyInitialized()) {
				mFTPQueueManager.closeWifiFtp();
			}
		}

	}

	/**
	 * Bluetooth接続用のFTP処理クラス
	 */
	public static class FTPControllerBLE extends FTPController {
		public FTPControllerBLE(final Context context, final IDeviceController controller) {
			super(context, controller);
			mFTPHandler.sendMessage(mFTPHandler.obtainMessage(CMD_INIT, controller));
		}

		@Override
		protected void handleInit(final IDeviceController controller) {
			final Context context = mWeakContext.get();
			final Object device = controller.getDeviceService().getDevice();
			final int hostPort;
			if (device instanceof ARDiscoveryDeviceBLEService) {
				hostPort = 0x15;	// XXX これ合ってるかなぁ
			} else {
				throw new IllegalArgumentException("ARDiscoveryDeviceBLEServiceじゃない");
			}
			final ARSALBLEManager arSALBLEManager = ARSALBLEManager.getInstance(context);
			final BluetoothGatt gatt = arSALBLEManager.getGatt();
			ARUTILS_ERROR_ENUM result = mFTPListManager.initBLEFtp(context, gatt, hostPort);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				throw new IllegalArgumentException("initBLEFtpが失敗:err=" + result);
			}
			result = mFTPQueueManager.initBLEFtp(context, gatt, hostPort);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				mFTPListManager.closeBLEFtp(context);
				mFTPListManager.dispose();
				throw new IllegalArgumentException("initBLEFtpが失敗:err=" + result);
			}
			super.handleInit(controller);
		}

		@Override
		protected void handleRelease() {
			final Context context = mWeakContext.get();
			if ((mFTPListManager != null) && mFTPListManager.isCorrectlyInitialized()) {
				mFTPListManager.closeBLEFtp(context);
			}
			if ((mFTPQueueManager != null) && mFTPQueueManager.isCorrectlyInitialized()) {
				mFTPQueueManager.closeBLEFtp(context);
			}
		}

	}

}
