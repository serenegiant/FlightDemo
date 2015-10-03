package com.serenegiant.usb;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.util.Log;

import com.serenegiant.usb.USBMonitor.UsbControlBlock;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.CRC32;

public class HIDGamepad {
	private static final boolean DEBUG = true;	// FIXME 実同時はfalseにすること
	private static final String TAG = HIDGamepad.class.getSimpleName();

	private final Object mSync = new Object();
	private UsbControlBlock mCtrlBlock;
	private volatile boolean mIsRunning;

	public static class GamepadStatus {
		public boolean left;
	}

	public interface HIDGamepadCallback {
		public void onRawdataChanged(final int n, final byte[] data);
	}

	private final HIDGamepadCallback mCallback;
	public HIDGamepad(final HIDGamepadCallback callback) throws NullPointerException {
		if (callback == null) {
			throw new NullPointerException("callback should not be a null");
		}
		mCallback = callback;
	}

	public void open(final UsbControlBlock ctrlBlock) {
		synchronized (mSync) {
			mCtrlBlock = ctrlBlock;
			// FIXME ここでインターフェースとエンドポイントを探す
			final UsbDevice device = ctrlBlock.getDevice();
			if (DEBUG) Log.v(TAG, "num_interface:" + device.getInterfaceCount());
			if (device.getInterfaceCount() != 1) {	// インターフェースの数は1個だけをサポート
				Log.e(TAG, "could not find interface");
			}
			final UsbInterface intf = device.getInterface(0);
			final int num_endpoint = intf.getEndpointCount();
			if (DEBUG) Log.v(TAG, "num_endpoint:" + num_endpoint);
			if (num_endpoint > 0) {
				UsbEndpoint ep_in = null;
				UsbEndpoint ep_out = null;
				for (int i = 0; i < num_endpoint; i++) {
					final UsbEndpoint ep = intf.getEndpoint(i);
					if (DEBUG) Log.v(TAG, "type=" + ep.getType() + ", dir=" + ep.getDirection());
					if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {	// インタラプト転送
						switch (ep.getDirection()) {
						case UsbConstants.USB_DIR_IN:
							if (ep_in == null) {
								ep_in = ep;
							}
							break;
						case UsbConstants.USB_DIR_OUT:
							if (ep_out == null) {
								ep_out = ep;
							}
							break;
						}
					}
					if ((ep_in != null) && (ep_out != null)) break;
				}
				if (ep_in != null) {
					new Thread(new GamepadInTask(intf, ep_in), "GamepadInTask").start();
					synchronized (mSync) {
						if (!mIsRunning) {
							try {
								mSync.wait();
								if (mIsRunning) {
									new Thread(mCallbackTask, "CallbackTask").start();
								}
							} catch (final InterruptedException e) {
							}
						}
					}
				} else {
					Log.e(TAG, "could not find input endpoint");
					return;
				}
				// FIXME 出力は未サポート
			} else {
				Log.e(TAG, "could not find endpoint");
			}
		}
	}

	public void close() {
		synchronized (mSync) {
			mIsRunning = false;
			mCtrlBlock = null;
			mSync.notifyAll();
		}
	}

	/**
	 * destroy UVCCamera object
	 */
	public void destroy() {
		close();
	}

	public UsbDevice getDevice() {
		return mCtrlBlock != null ? mCtrlBlock.getDevice() : null;
	}

	public String getDeviceName(){
		return mCtrlBlock != null ? mCtrlBlock.getDeviceName() : null;
	}

	public UsbControlBlock getUsbControlBlock() {
		return mCtrlBlock;
	}

	private boolean findHIDInput() {
		return false;
	}

	private byte[] mValues;

	private final Runnable mCallbackTask = new Runnable() {
		@Override
		public void run() {
			synchronized (mSync) {
				if (!mIsRunning) {
					try {
						mSync.wait();
					} catch (final InterruptedException e) {
					}
				}
			}
			int cnt = 0;
			final int n = mValues != null ? mValues.length : 0;
			final byte[] values = new byte[n];
			final byte[] prev = new byte[n];
			long prev_time = -1;
			for (; mIsRunning ;) {
				synchronized (mSync) {
					try {
						// ゲームパッドの値更新通知が来るまで待機
						mSync.wait();
						// ローカルにコピーする
						System.arraycopy(mValues, 0, values, 0, n);
						if (!mIsRunning) break;
					} catch (final InterruptedException e) {
						break;
					}
				}
				// 値が変更されているかどうかをチェック
				boolean b = false;
				for (int i = 0; i < n; i++) {
					b |= prev[i] != values[i];
					prev[i] = values[i];
				}
				if (b && (System.currentTimeMillis() - prev_time > 20)) {
					// 値が変更されていて前回のコールバック呼び出しから２０ミリ秒以上経過していたらコールバックを呼び出す
					prev_time = System.currentTimeMillis();
					try {
						mCallback.onRawdataChanged(n, values);
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				}
			}
		}
	};


	private class GamepadInTask implements Runnable {
		private final String TAG_THREAD = GamepadInTask.class.getSimpleName();

		private final UsbInterface mIntf;
		private final UsbEndpoint mEp;
		public GamepadInTask(final UsbInterface intf, final UsbEndpoint ep) {
			mIntf = intf;
			mEp = ep;
		}

		@Override
		public void run() {
			if (DEBUG) Log.v(TAG_THREAD, "#run:");
			final UsbDeviceConnection connection;
			final int intervals;
			final int max_packets;
			final int n;
			synchronized (mSync) {
				connection = mCtrlBlock.getUsbDeviceConnection();
				if (connection != null) {
					if (DEBUG) Log.v(TAG_THREAD, "claimInterface:");
					connection.claimInterface(mIntf, true);
					intervals = mEp.getInterval();
					max_packets = mEp.getMaxPacketSize();
					if (DEBUG)
						Log.v(TAG_THREAD, "intervals=" + intervals + ", max_packets=" + max_packets);
					mValues = new byte[max_packets];
					mIsRunning = true;
				} else {
					intervals = max_packets = 0;
				}
				mSync.notifyAll();
			}
			if (connection != null) {
				final ByteBuffer buffer = ByteBuffer.allocateDirect(max_packets);
				buffer.order(ByteOrder.LITTLE_ENDIAN);
				final UsbRequest request = new UsbRequest();
				request.initialize(connection, mEp);
				try {
					UsbRequest req;
					for (; mIsRunning; ) {
						buffer.clear();
						request.queue(buffer, max_packets);
						try {
							req = connection.requestWait();
						} catch (final Exception e) {
							Log.w(TAG, e);
							req = null;
						}
						if (request.equals(req)) {
//							if (DEBUG) Log.v(TAG_THREAD, "got data:" + buffer);
							buffer.clear();
							synchronized (mSync) {
								buffer.get(mValues);
								mSync.notifyAll();
							}
						}
						try {
							Thread.sleep(intervals);
						} catch (final InterruptedException e) {
						}
					}
					request.close();
				} finally {
					if (DEBUG) Log.v(TAG_THREAD, "releaseInterface:");
					connection.releaseInterface(mIntf);
				}
			}
		}
	};

}
