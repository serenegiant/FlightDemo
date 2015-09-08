package com.serenegiant.arflight;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceBLEService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceNetService;
import com.parrot.arsdk.arsal.ARSALBLEManager;
import com.parrot.arsdk.arutils.ARUTILS_ERROR_ENUM;
import com.parrot.arsdk.arutils.ARUtilsException;
import com.parrot.arsdk.arutils.ARUtilsManager;

import java.lang.ref.WeakReference;

public abstract class FTPController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "FTPController:";

	protected final WeakReference<Context>mWeakContext;
	protected final ARUtilsManager mFTPListManager;
	protected final ARUtilsManager mFTPQueueManager;

	public static FTPController newInstance(final Context context, final IDeviceController controller) {
		if (controller instanceof DeviceControllerBebop) {
			return new FTPControllerWiFi(context, controller);
		} else if (controller instanceof DeviceControllerMiniDrone) {
			return new FTPControllerBLE(context, controller);
		}
		return null;
	}

	protected FTPController(final Context context) {
		mFTPListManager = createARUtilsManager();
		mFTPQueueManager = createARUtilsManager();
		mWeakContext = new WeakReference<Context>(context);
	}

	public void release() {
		mFTPListManager.dispose();
		mFTPQueueManager.dispose();
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

	private static final String USER_NAME = "anonymous";
	private static final String PASSWORD = "";
	public static class FTPControllerWiFi extends FTPController {
		public FTPControllerWiFi(final Context context, final IDeviceController controller) {
			super(context);
			final Object device = controller.getDeviceService();
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
		}

		@Override
		public void release() {
			mFTPListManager.closeWifiFtp();
			mFTPQueueManager.closeWifiFtp();
			super.release();
		}
	}

	public static class FTPControllerBLE extends FTPController {
		public FTPControllerBLE(final Context context, final IDeviceController controller) {
			super(context);
			final Object device = controller.getDeviceService();
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
		}

		@Override
		public void release() {
			mFTPListManager.closeBLEFtp(mWeakContext.get());
			mFTPQueueManager.closeBLEFtp(mWeakContext.get());
			super.release();
		}
	}

}
