package com.serenegiant.arflight;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.parrot.arsdk.ardatatransfer.ARDATATRANSFER_ERROR_ENUM;
import com.parrot.arsdk.ardatatransfer.ARDataTransferException;
import com.parrot.arsdk.ardatatransfer.ARDataTransferManager;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMedia;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloader;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloaderCompletionListener;
import com.parrot.arsdk.ardatatransfer.ARDataTransferMediasDownloaderProgressListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.armedia.ARMediaObject;
import com.parrot.arsdk.arsal.ARSALBLEManager;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.parrot.arsdk.arutils.ARUTILS_ERROR_ENUM;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class FTPController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "FTPController:";

	public static final int CALLBACK_CMD_GET_AVAILABLE_MEDIAS = 0;
	public static final int CALLBACK_CMD_GET_MEDIA_THUMBNAILS = 1;
	public static final int CALLBACK_CMD_MEDIA_TRANSFER = 2;
	public static final int CALLBACK_CMD_MEDIA_DELETE = 3;

	public interface FTPControllerCallback {
		/**
		 * エラー発生時のコールバック
		 * @param e
		 * @return
		 */
		public boolean onError(final Exception e);

		/**
		 * メディアリスト更新時のコールバック
		 * @param medias
		 */
		public void onMediaListUpdated(final List<ARMediaObject>medias);

		/**
		 * 進捗状況更新時のコールバック
		 * @param cmd
		 * @param progress
		 * @param current
		 * @param total
		 * @param media
		 */
		public void onProgress(final int cmd, final float progress, final int current, final int total, final ARMediaObject media);

		/**
		 * 非同期処理終了時のコールバック
		 * @param cmd
		 * @param error
		 * @param current
		 * @param total
		 * @param media
		 */
		public void onFinished(final int cmd, final int error, final int current, final int total, final ARMediaObject media);
	}

    // for thread pool
    private static final int CORE_POOL_SIZE = 0;		// initial/minimum threads
    private static final int MAX_POOL_SIZE = 8;			// maximum threads
    private static final int KEEP_ALIVE_TIME = 10;		// time periods while keep the idle thread
    protected static final ThreadPoolExecutor EXECUTER
		= new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME,
			TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

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
	protected volatile boolean mConnected;

	private final Object mCallbackSync = new Object();
	private FTPControllerCallback mCallback;

	public static FTPController newInstance(final Context context, final IDeviceController controller) {
		if (controller instanceof DeviceControllerBebop) {
			return new FTPControllerWiFi(context, controller);
		} else if (controller instanceof DeviceControllerMiniDrone) {
			return new FTPControllerBLE(context, controller);
		}
		controller.sendVideoRecording(false);
		if (controller instanceof IVideoStreamController) {
			((IVideoStreamController)controller).enableVideoStreaming(false);
		}
		return null;
	}

	/** デバッグレベル保持用 */
	private final ARSAL_PRINT_LEVEL_ENUM mOrgLevel;
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

		mOrgLevel = ARSALPrint.getMinimumLogLevel();
		if (DEBUG) {
//			ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_VERBOSE);
		}

		mFTPListManager = createARUtilsManager();
		mFTPQueueManager = createARUtilsManager();
		mDataTransferManager = createARDataTransferManager();
		// 場所は気に入らないけど純正アプリと同じ場所に保存する
		final String productName = controller.getProductName(); // .replace(" ", "_");
		final File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), productName);
		path.mkdirs();	// バカだからここで作っといてあげないとだめ
		mExternalDirectory = path.getAbsolutePath();
	}

	public void connect() {
		try {
			mConnected = true;
			mFTPHandler.sendEmptyMessage(REQ_CONNECT);
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
			mConnected = false;
			mFTPHandler.sendEmptyMessage(REQ_RELEASE);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
		ARSALPrint.setMinimumLogLevel(mOrgLevel);
	}

	/**
	 * 実行中・実行待ちの処理を中断要求する
	 */
	public void cancel() {
		try {
			// FIXME ここで未実行のCMD_RELEASE以外のコマンドを取り除く
			mFTPHandler.removeMessages(REQ_CANCEL);
			mFTPHandler.removeMessages(REQ_DELETE);
			mFTPHandler.removeMessages(REQ_TRANSFER);
			mFTPHandler.removeMessages(REQ_TRANSFER_AND_DELETE);
			// キャンセル要求
			mRequestCancel = true;
			mFTPHandler.sendEmptyMessage(REQ_CANCEL);
		} catch (final Exception e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * コールバックを設定
	 * @param callback
	 */
	public void setCallback(final FTPControllerCallback callback) {
		synchronized (mCallbackSync) {
			mCallback = callback;
		}
	}

	/**
	 * コールバックを取得
	 * @return
	 */
	public FTPControllerCallback getCallback() {
		synchronized (mCallbackSync) {
			return mCallback;
		}
	}

	/**
	 * メディアファイル一覧更新要求
	 */
	public void updateMedia() {
		if (DEBUG) Log.v(TAG, "updateMedia:" + mConnected);
		if (mConnected) {
			mRequestCancel = false;
			mFTPHandler.removeMessages(REQ_LIST_MEDIAS);	// 未処理は無かったことにする
			mFTPHandler.sendEmptyMessage(REQ_LIST_MEDIAS);
		}
	}

	/**
	 * メディアファイルを取得要求
	 * @param medias
	 * @param deleteAfterFetch true:取得後に削除する
	 */
	public void transfer(final ARMediaObject[] medias, final boolean deleteAfterFetch) {
		if (DEBUG) Log.v(TAG, "transfer:" + mConnected);
		if (mConnected) {
			mRequestCancel = false;
			if (deleteAfterFetch) {
				mFTPHandler.sendMessage(mFTPHandler.obtainMessage(REQ_TRANSFER_AND_DELETE, medias));
			} else {
				mFTPHandler.sendMessage(mFTPHandler.obtainMessage(REQ_TRANSFER, medias));
			}
		}
	}

	/**
	 * 指定したメディアファイルを削除要求
	 * @param medias
	 */
	public void delete(final ARMediaObject[] medias) {
		if (DEBUG) Log.v(TAG, "delete:" + mConnected);
		if (mConnected) {
			mRequestCancel = false;
			mFTPHandler.sendMessage(mFTPHandler.obtainMessage(REQ_DELETE, medias));
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
	protected abstract void handleInit(final IDeviceController controller);

	protected void handleConnect() {
		if (DEBUG) Log.v(TAG, String.format("handleConnect:mExternalDirectory=%s,mRemotePath=%s", mExternalDirectory, mRemotePath));
		try {
			mDownLoader = mDataTransferManager.getARDataTransferMediasDownloader();
			mDownLoader.createMediasDownloader(mFTPListManager, mFTPQueueManager, mRemotePath, mExternalDirectory);
		} catch (final ARDataTransferException e) {
			mConnected = false;
			Log.w(TAG, e);
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
	 * メディア一覧を更新
	 */
	protected void handleUpdateMediaList() {
		final List<ARMediaObject> medias = getMediaThumbnails(getAvailableMedias());
		callOnMediaListUpdated(medias);
	}

	private static class TransferRec {
		public final ARMediaObject mediaObject;
		public final boolean needDelete;
		public TransferRec(final ARMediaObject media_obj, final boolean need_delete) {
			mediaObject = media_obj;
			needDelete = need_delete;
		}
	}

	private final Object mHandlingSync = new Object();
	private int mTotalMediaNum = 0;
	private int mFinishedNum = 0;
	/**
	 * 複数のメディアファイルを読み込み(既に読み込まれていれば何もしない)
	 * @param medias
	 * @param needDelete
	 */
	protected void handleTransfer(final ARMediaObject[] medias, final boolean needDelete) {
		if (DEBUG) Log.v(TAG, "handleTransfer:" + medias);
		final int n = medias != null ? medias.length : 0;
		if (n > 0) {
			synchronized (mHandlingSync) {
				mTotalMediaNum = n;
				mFinishedNum = 0;
			}
			if (DEBUG) Log.v(TAG, "request transfer");
			for (final ARMediaObject mediaObject : medias) {
				handleTransferOne(mediaObject, needDelete);
			}
			synchronized (mHandlingSync) {
				if (mConnected && !mRequestCancel && (mFinishedNum < mTotalMediaNum)) {
					if (DEBUG) Log.v(TAG, "run downloaderQueueRunnable");
					final Runnable downloaderQueueRunnable = mDownLoader.getDownloaderQueueRunnable();
					downloaderQueueRunnable.run();
					for (; mConnected && !mRequestCancel && (mFinishedNum < mTotalMediaNum); ) {
						try {
							if (DEBUG) Log.v(TAG, String.format("wait for finishing transfer:%d/%d", mFinishedNum, mTotalMediaNum));
							mHandlingSync.wait(1000);
						} catch (final InterruptedException e) {
							break;
						}
					}
				}
			}
		}
		if (DEBUG) Log.v(TAG, "handleTransfer:finished");
	}

	/**
	 * 複数のメディアファイルを削除
	 * @param medias
	 */
	protected void handleDelete(final ARMediaObject[] medias) {
		if (DEBUG) Log.v(TAG, "handleDelete:" + medias);
		final int n = medias != null ? medias.length : 0;
		if (n > 0) {
			int i = 0;
			for (final ARMediaObject mediaObject: medias) {
				final boolean result = handleDeleteOne(mediaObject);
				callOnFinished(CALLBACK_CMD_MEDIA_DELETE, result ? 1 : 0, ++i, n, mediaObject);
			}
		}
		if (DEBUG) Log.v(TAG, "handleDelete:finished");
	}

	/**
	 * メディアファイルを１つ読み込む(既に読み込まれていれば何もしない)
	 * @param mediaObject
	 * @return
	 */
	protected boolean handleTransferOne(final ARMediaObject mediaObject, final boolean needDelete) {
		boolean result = false;
		if (mediaObject != null) {
			final File file = new File(mediaObject.getFilePath());
			try {
				final TransferRec rec = new TransferRec(mediaObject, needDelete);
				if (!file.exists() || (file.length() != mediaObject.getSize())) {
					// 端末内にファイルが存在しないかサイズが異なる時
					if (DEBUG) Log.v(TAG, "addMediaToQueue:");
					mDownLoader.addMediaToQueue(mediaObject.media,
						mTransferProcessListener, rec,        // progressArg
						mTransferCompletionListener, rec);	// completionArg
					result = true;
				} else {
					if (DEBUG) Log.w(TAG, "既に読み込み済");
					// 転送完了したことにする
					finishTransferOne(0, rec);
				}
			} catch (final Exception e) {
				if (DEBUG) Log.w(TAG, "例外発生", e);
				// 転送失敗したことにする
				finishTransferOne(-1, null);
			}
		}
		return result;
	}

	/**
	 * メディアファイル転送時の進捗状況のコールバック
	 */
	private final ARDataTransferMediasDownloaderProgressListener mTransferProcessListener
		= new ARDataTransferMediasDownloaderProgressListener() {
		@Override
		public void didMediaProgress(final Object progressArg, final ARDataTransferMedia media, final float progress) {
//			if (DEBUG) Log.v(TAG, "didMediaProgress:arg=" + progressArg +",progress=" + progress);
			// progressArgはTransferRec
			callOnProgress(CALLBACK_CMD_MEDIA_TRANSFER, progress, mFinishedNum, mTotalMediaNum, ((TransferRec)progressArg).mediaObject);
		}
	};

	/**
	 * メディアファイル転送完了時のコールバック
	 */
	private final ARDataTransferMediasDownloaderCompletionListener mTransferCompletionListener
		= new ARDataTransferMediasDownloaderCompletionListener() {
		@Override
		public void didMediaComplete(final Object completionArg, final ARDataTransferMedia media,
			final ARDATATRANSFER_ERROR_ENUM error) {

//			if (DEBUG) Log.v(TAG, "didMediaProgress:arg=" + completionArg +",error=" + error);
			// completionArgはTransferRec
			finishTransferOne(error.getValue(), (TransferRec)completionArg);
		}
	};

	/**
	 * ファイル転送完了時の処理
	 * @param error
	 * @param completionArg
	 */
	private void finishTransferOne(final int error, final TransferRec completionArg) {
		synchronized (mHandlingSync) {
			mFinishedNum++;
			mHandlingSync.notifyAll();
		}
		final ARMediaObject mediaObject = completionArg != null ? completionArg.mediaObject : null;
		if (completionArg != null) {
			if (completionArg.needDelete) {
				// 削除要求
				handleDeleteOne(mediaObject);
			}
		}
		callOnFinished(CALLBACK_CMD_MEDIA_TRANSFER, error, mFinishedNum, mTotalMediaNum, mediaObject);
	}

	/**
	 * メディアファイルを１つ削除
	 * @param mediaObject
	 * @return
	 */
 	protected boolean handleDeleteOne(final ARMediaObject mediaObject) {
		boolean result = false;
		if (mediaObject != null) {
			final ARDATATRANSFER_ERROR_ENUM err = mDownLoader.deleteMedia(mediaObject.media);
			if (err != ARDATATRANSFER_ERROR_ENUM.ARDATATRANSFER_OK) {
				Log.w(TAG, "failed to delete media: err=" + err);
				result = true;
			}
		}
		return result;
	}


	private void dumpMediaObject(final ARMediaObject mediaObject) {
		Log.d(TAG, "dumpMediaObject:getName=" + mediaObject.getName());
		Log.d(TAG, "dumpMediaObject:getFilePath=" + mediaObject.getFilePath());
		Log.d(TAG, "dumpMediaObject:getProductId=" + mediaObject.getProductId());
		Log.d(TAG, "dumpMediaObject:getProduct=" + mediaObject.getProduct());
		Log.d(TAG, "dumpMediaObject:getDate=" + mediaObject.getDate());
		Log.d(TAG, "dumpMediaObject:getRunDate=" + mediaObject.getRunDate());
		Log.d(TAG, "dumpMediaObject:getUUID=" + mediaObject.getUUID());
		Log.d(TAG, "dumpMediaObject:getMediaType=" + mediaObject.getMediaType());
		Log.d(TAG, "dumpMediaObject:getThumbnail=" + mediaObject.getThumbnail());
		// getName=Bebop_Drone_2015-09-02T171319+0000_0A88CE.mp4
		// getFilePath=/storage/emulated/0/DCIM/FlightDemo/Bebop_Drone/Bebop_Drone_2015-09-02T171319+0000_0A88CE.mp4
		// getProductId=0901
		// getProduct=AR DRONE product
		// getDate=2015-09-02T171319+0000
		// getRunDate=null
		// getUUID=0A88CE
		// getMediaType=MEDIA_TYPE_VIDEO
		// getThumbnail=android.graphics.drawable.BitmapDrawable@1bd23e32
	}
	/**
	 * エラー発生時のコールバックを呼び出す
	 * @param e
	 */
	protected void callOnError(final Exception e) {
		boolean result = true;
		synchronized (mCallbackSync) {
			if (mCallback != null) {
				try {
					result = mCallback.onError(e);
				} catch (final Exception e1) {
					Log.w(TAG, e1);
				}
			}
		}
		if (result) {
			Log.w(TAG, e);
			cancel();
		}
	}

	/**
	 * 進捗状況更新のコールバックを呼び出す
	 * @param cmd
	 * @param progress	 [0,1]
	 */
	protected void callOnProgress(final int cmd, final float progress, final int current, final int total, final ARMediaObject media) {
		synchronized (mCallbackSync) {
			if (mCallback != null) {
				try {
					mCallback.onProgress(cmd, progress, current, total, media);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	protected void callOnFinished(final int cmd, final int error, final int current, final int total, final ARMediaObject media) {
		synchronized (mCallbackSync) {
			if (mCallback != null) {
				try {
					mCallback.onFinished(cmd, error, current, total, media);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	/**
	 * メディア一覧を更新した時のコールバックを呼び出す
	 * @param medias
	 */
	protected void callOnMediaListUpdated(final List<ARMediaObject> medias) {
		synchronized (mCallbackSync) {
			if (mCallback != null) {
				try {
					mCallback.onMediaListUpdated(medias);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private List<ARMediaObject> getAvailableMedias() {
		if (DEBUG) Log.v(TAG, "getAvailableMedias:");
		int num = -1;
		final List<ARMediaObject> medias = new ArrayList<ARMediaObject>();
		try {
			// int ARDataTransferMediasDownloader#getAvailableMediasSync(boolean withThumbnail)
			num = mDownLoader.getAvailableMediasSync(false);
			if (num > 0) {
				final Resources res = mWeakContext.get().getResources();
				for (int i = 0; i < num; i++) {
					final ARDataTransferMedia media = mDownLoader.getAvailableMediaAtIndex(i);
					final ARMediaObject mediaObject = new ARMediaObject();
					callOnProgress(CALLBACK_CMD_GET_AVAILABLE_MEDIAS, i / (float) num, 1, 1, mediaObject);
					mediaObject.updateDataTransferMedia(res, media);
					medias.add(mediaObject);
				}
				callOnFinished(CALLBACK_CMD_GET_AVAILABLE_MEDIAS, 0, 1, 1, null);
			}
		} catch (final ARDataTransferException e) {
			callOnFinished(CALLBACK_CMD_GET_AVAILABLE_MEDIAS, 1, 1, 1, null);
			callOnError(e);
		}
		return medias;
	}

	private List<ARMediaObject> getMediaThumbnails(final List<ARMediaObject> medias) {
		if (DEBUG) Log.v(TAG, "getMediaThumbnails:");
		final int num = medias.size();
		final Resources res = mWeakContext.get().getResources();
		int foundMediasThumbnail = -1;
		int i = -1;
		for (final ARMediaObject mediaObject: medias) {
			callOnProgress(CALLBACK_CMD_GET_MEDIA_THUMBNAILS, ++i / (float)num, 1, 1, mediaObject);
			final byte[] thumbnail = mDownLoader.getMediaThumbnail(mediaObject.media);
			if (thumbnail != null) {
				foundMediasThumbnail++;
				mediaObject.updateThumbnailWithDataTransferMedia(res, mediaObject.media);
			}
			callOnFinished(CALLBACK_CMD_GET_MEDIA_THUMBNAILS, 0, 1, 1, null);
		}
		return medias;
	}

	private static final int REQ_INIT = 1;
	private static final int REQ_CONNECT = 2;
	private static final int REQ_LIST_MEDIAS = 3;
	private static final int REQ_TRANSFER = 4;
	private static final int REQ_TRANSFER_AND_DELETE = 5;
	private static final int REQ_DELETE = 6;
	private static final int REQ_CANCEL = 8;
	private static final int REQ_RELEASE = 9;

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
			case REQ_INIT:
				try {
					handleInit((IDeviceController) msg.obj);
				} catch (final Exception e) {
					callOnError(e);
				}
				break;
			case REQ_CONNECT:
				final IDeviceController controller = mWeakController.get();
				mRemotePath = String.format("%s_%03d", controller.getMassStorageName(), controller.getMassStorageId());
				handleConnect();
				mFTPHandler.sendEmptyMessage(REQ_LIST_MEDIAS);
				break;
			case REQ_LIST_MEDIAS:
				handleUpdateMediaList();
				break;
			case REQ_TRANSFER:
			case REQ_TRANSFER_AND_DELETE:
				handleTransfer((ARMediaObject[]) msg.obj, msg.what == REQ_TRANSFER_AND_DELETE);
				updateMedia();
				break;
			case REQ_DELETE:
				handleDelete((ARMediaObject[]) msg.obj);
				updateMedia();
				break;
			case REQ_CANCEL:
				if (mDownLoader != null) {
					mDownLoader.cancelGetAvailableMedias();
					mDownLoader.cancelQueueThread();
				}
				break;
			case REQ_RELEASE:
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
	private static final int HOST_PORT = 21;
	/**
	 * WiFi接続用のFTP処理クラス
	 */
	public static class FTPControllerWiFi extends FTPController {
		public FTPControllerWiFi(final Context context, final IDeviceController controller) {
			super(context, controller);
			mFTPHandler.sendMessage(mFTPHandler.obtainMessage(REQ_INIT, controller));
		}

		@Override
		protected void handleInit(final IDeviceController controller) {
			final Object device = controller.getDeviceService().getDevice();
			final String hostAddr;
			if (device instanceof ARDiscoveryDeviceNetService) {
				hostAddr = ((ARDiscoveryDeviceNetService)device).getIp();
//				hostPort = ((ARDiscoveryDeviceNetService)device).getPort();	// これはFTP接続用じゃない
			} else {
				throw new IllegalArgumentException("ARDiscoveryDeviceNetServiceじゃない");
			}
			ARUTILS_ERROR_ENUM result = mFTPListManager.initWifiFtp(hostAddr, HOST_PORT, USER_NAME, PASSWORD);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				throw new IllegalArgumentException("initWifiFtpが失敗:err=" + result);
			}
			result = mFTPQueueManager.initWifiFtp(hostAddr, HOST_PORT, USER_NAME, PASSWORD);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				mFTPListManager.closeWifiFtp();
				mFTPListManager.dispose();
				throw new IllegalArgumentException("initWifiFtpが失敗:err=" + result);
			}
		}

		@Override
		protected void handleRelease() {
			if ((mFTPListManager != null) && mFTPListManager.isCorrectlyInitialized()) {
				mFTPListManager.closeWifiFtp();
			}
			if ((mFTPQueueManager != null) && mFTPQueueManager.isCorrectlyInitialized()) {
				mFTPQueueManager.closeWifiFtp();
			}
			super.handleRelease();
		}

	}

	/**
	 * Bluetooth接続用のFTP処理クラス
	 */
	public static class FTPControllerBLE extends FTPController {
		public FTPControllerBLE(final Context context, final IDeviceController controller) {
			super(context, controller);
			mFTPHandler.sendMessage(mFTPHandler.obtainMessage(REQ_INIT, controller));
		}

		@Override
		protected void handleInit(final IDeviceController controller) {
			final Context context = mWeakContext.get();
			final Object device = controller.getDeviceService().getDevice();
			if (!(device instanceof ARDiscoveryDeviceBLEService)) {
				throw new IllegalArgumentException("ARDiscoveryDeviceBLEServiceじゃない");
			}
			final ARSALBLEManager arSALBLEManager = ARSALBLEManager.getInstance(context);
			final BluetoothGatt gatt = arSALBLEManager.getGatt();
			ARUTILS_ERROR_ENUM result = mFTPListManager.initBLEFtp(context, gatt, HOST_PORT);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				throw new IllegalArgumentException("initBLEFtpが失敗:err=" + result);
			}
			result = mFTPQueueManager.initBLEFtp(context, gatt, HOST_PORT);
			if (result != ARUTILS_ERROR_ENUM.ARUTILS_OK) {
				mFTPListManager.closeBLEFtp(context);
				mFTPListManager.dispose();
				throw new IllegalArgumentException("initBLEFtpが失敗:err=" + result);
			}
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
			super.handleRelease();
		}

	}

}
