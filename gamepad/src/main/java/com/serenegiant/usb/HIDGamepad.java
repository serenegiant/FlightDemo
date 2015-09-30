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

public class HIDGamepad {
	private static final boolean DEBUG = true;	// FIXME 実同時はfalseにすること
	private static final String TAG = HIDGamepad.class.getSimpleName();

	private final Object mSync = new Object();
	private UsbControlBlock mCtrlBlock;
	private volatile boolean mIsRunning;

	public HIDGamepad() {
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
			mIsRunning = true;
			final UsbDeviceConnection connection;
			synchronized (mSync) {
				connection = mCtrlBlock.getUsbDeviceConnection();
			}
			if (connection != null) {
				if (DEBUG) Log.v(TAG_THREAD, "claimInterface:");
				connection.claimInterface(mIntf, true);
				try {
					final int intervals = mEp.getInterval();
					final int max_packets = mEp.getMaxPacketSize();
					if (DEBUG) Log.v(TAG_THREAD, "intervals=" + intervals + ", max_packets=" + max_packets);
					final ByteBuffer buffer = ByteBuffer.allocate(max_packets);
					final byte[] codes = new byte[max_packets];
					final UsbRequest request = new UsbRequest();
					request.initialize(connection, mEp);
					long v, prev = -1;
					for ( ; mIsRunning ; ) {
						buffer.clear();
						request.queue (buffer, max_packets);
						if (connection.requestWait() == request) {
//							if (DEBUG) Log.v(TAG_THREAD, "got data:" + buffer);
							buffer.clear();
							buffer.get(codes);
							v = 0;
							for (int i = max_packets - 1; i >= 0; i--) {
								v = (v << 8) + codes[i];
							}
							if (v != prev) {
								prev = v;
								if (DEBUG) Log.v(TAG_THREAD, String.format("%016x", v));
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
