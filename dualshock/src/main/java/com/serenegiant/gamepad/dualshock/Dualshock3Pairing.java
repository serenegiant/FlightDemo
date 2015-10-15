package com.serenegiant.gamepad.dualshock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.text.TextUtils;
import android.util.Log;

import com.serenegiant.usb.USBMonitor;
import com.serenegiant.utils.BuildCheck;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Dualshock3Pairing {
	private static final boolean DEBUG = true;	// FIXME 実同時はfalseにすること
	private static final String TAG = Dualshock3Pairing.class.getSimpleName();

	private static final int VENDOR_SONY = 0x054c;
	private static final int PRODUCT_SIXAXIS_DS3 = 0x0268;

	private static final int USB_DIR_OUT = 0;
	private static final int USB_DIR_IN = 0x80;
	private static final int USB_TYPE_MASK = (0x03 << 5);
	private static final int USB_TYPE_STANDARD = (0x00 << 5);
	private static final int USB_TYPE_CLASS = (0x01 << 5);
	private static final int USB_TYPE_VENDOR = (0x02 << 5);
	private static final int USB_TYPE_RESERVED = (0x03 << 5);
	private static final int USB_RECIP_MASK = 0x1f;
	private static final int USB_RECIP_DEVICE = 0x00;
	private static final int USB_RECIP_INTERFACE = 0x01;
	private static final int USB_RECIP_ENDPOINT = 0x02;
	private static final int USB_RECIP_OTHER = 0x03;
	private static final int USB_RECIP_PORT = 0x04;
	private static final int USB_RECIP_RPIPE = 0x05;
	private static final int USB_REQ_GET_STATUS = 0x00;
	private static final int USB_REQ_CLEAR_FEATURE = 0x01;
	private static final int USB_REQ_SET_FEATURE = 0x03;
	private static final int USB_REQ_SET_ADDRESS = 0x05;
	private static final int USB_REQ_GET_DESCRIPTOR = 0x06;
	private static final int USB_REQ_SET_DESCRIPTOR = 0x07;
	private static final int USB_REQ_GET_CONFIGURATION = 0x08;
	private static final int USB_REQ_SET_CONFIGURATION = 0x09;
	private static final int USB_REQ_GET_INTERFACE = 0x0A;
	private static final int USB_REQ_SET_INTERFACE = 0x0B;
	private static final int USB_REQ_SYNCH_FRAME = 0x0C;
	private static final int USB_REQ_SET_SEL = 0x30;
	private static final int USB_REQ_SET_ISOCH_DELAY = 0x31;
	private static final int USB_REQ_SET_ENCRYPTION = 0x0D;
	private static final int USB_REQ_GET_ENCRYPTION = 0x0E;
	private static final int USB_REQ_RPIPE_ABORT = 0x0E;
	private static final int USB_REQ_SET_HANDSHAKE = 0x0F;
	private static final int USB_REQ_RPIPE_RESET = 0x0F;
	private static final int USB_REQ_GET_HANDSHAKE = 0x10;
	private static final int USB_REQ_SET_CONNECTION = 0x11;
	private static final int USB_REQ_SET_SECURITY_DATA = 0x12;
	private static final int USB_REQ_GET_SECURITY_DATA = 0x13;
	private static final int USB_REQ_SET_WUSB_DATA = 0x14;
	private static final int USB_REQ_LOOPBACK_DATA_WRITE = 0x15;
	private static final int USB_REQ_LOOPBACK_DATA_READ = 0x16;
	private static final int USB_REQ_SET_INTERFACE_DS = 0x17;

	private static final int USB_GET_REPORT = 0x01;
	private static final int USB_SET_REPORT = 0x09;


	public static boolean isDualshock3(final UsbDevice device) {
		final int vid = device != null ? device.getVendorId() : 0;
		final int pid = device != null ? device.getProductId() : 0;
		return (vid == VENDOR_SONY) && (pid == PRODUCT_SIXAXIS_DS3);
	}

	private final USBMonitor.UsbControlBlock mCtrlBlock;
	private String mPairingAddr;
	public Dualshock3Pairing(final USBMonitor.UsbControlBlock ctrl_block) {
		if (DEBUG) Log.v(TAG, "コンストラクタ");
		mCtrlBlock = ctrl_block;
		final UsbDevice device = ctrl_block != null ? ctrl_block.getDevice() : null;
		final int vid = device != null ? device.getVendorId() : 0;
		final int pid = device != null ? device.getProductId() : 0;
		if ((vid != VENDOR_SONY) || (pid != PRODUCT_SIXAXIS_DS3)) {
			throw new IllegalArgumentException("invalid USB device");
		}
		findPairingAddress();
	}

	@SuppressLint("NewApi")
	private void findPairingAddress() {
		if (DEBUG) Log.v(TAG, "findPairingAddress");
		final UsbDevice device = mCtrlBlock.getDevice();
			UsbInterface found_intf = null;
		if (BuildCheck.isAndroid5()) {
			final int num_configs = device.getConfigurationCount();
			if (DEBUG) Log.v(TAG, "num_configs:" + num_configs);
LOOP:		for (int i = 0; i < num_configs; i++) {
				final UsbConfiguration config = device.getConfiguration(i);
				final int num_interface = config.getInterfaceCount();
				if (DEBUG) Log.v(TAG, "num_interface:" + num_interface);
				for (int j = 0; j < num_interface; j++) {
					final UsbInterface intf = config.getInterface(j);
					if (intf.getInterfaceClass() == 3) {
						if (DEBUG) Log.v(TAG, "見つかった");
						found_intf = intf;
						break LOOP;
					}
				}
			}
		} else {
			final int num_interface = device.getInterfaceCount();
			for (int i = 0; i < num_interface; i++) {
				final UsbInterface intf = device.getInterface(i);
				if (intf.getInterfaceClass() == 3) {
					found_intf = intf;
					break;
				}
			}
		}
		if (found_intf != null) {
			if (DEBUG) Log.v(TAG, "ペアリングアドレスの読み込みを試みる");
			final UsbDeviceConnection connection = mCtrlBlock.getUsbDeviceConnection();
			connection.claimInterface(found_intf, true);	// disconnect kernel driver
			final byte[] data = new byte[8];
			int bytes = connection.controlTransfer(
				USB_DIR_IN | USB_TYPE_CLASS | USB_RECIP_INTERFACE,
				USB_GET_REPORT, 0x03f5, found_intf.getId(), data, 8, 5000);
			if (bytes == 8) {
				mPairingAddr = String.format("%02X:%02X:%02X:%02X:%02X:%02X",
					data[2], data[3], data[4], data[5], data[6], data[7]);
				if (DEBUG) Log.v(TAG, "mPairingAddr=" + mPairingAddr);
				if (DEBUG) Log.v(TAG, "ペアリングアドレスの書き込みを試みる");
				data[0] = 0x01;
				data[1] = 0x00;
				bytes = connection.controlTransfer(
					USB_DIR_OUT | USB_TYPE_CLASS | USB_RECIP_INTERFACE,
					USB_SET_REPORT, 0x03f5, found_intf.getId(), data, 8, 5000);
				if (bytes == 8) {
				if (DEBUG) Log.v(TAG, "ペアリングアドレスの書き込み成功");
				} else {
					Log.w(TAG, "failed to write:result=" + bytes);
				}
			} else {
				Log.w(TAG, "failed to read:result=" + bytes);
			}
			connection.releaseInterface(found_intf);
		}
	}

	@SuppressLint("NewApi")
	public void requestPairing(final Activity context) {
		if (!TextUtils.isEmpty(mPairingAddr)) {

			if (BluetoothAdapter.checkBluetoothAddress(mPairingAddr)) {
				final BluetoothAdapter adapter;
				if (BuildCheck.isAndroid4_3()) {
					final BluetoothManager manager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
					adapter = manager.getAdapter();
				} else {
					adapter = BluetoothAdapter.getDefaultAdapter();
				}
				if (adapter != null) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							if (DEBUG) Log.i(TAG, "ペアリング開始");
							if (adapter.isEnabled()) {
								final BluetoothDevice device = adapter.getRemoteDevice(mPairingAddr);
								synchronized (mSync) {
									final BTBondStateChangeReceiver receiver = register(context);
									if (device.createBond()) {
										// PINコードをUTF8に変換
										try {
											Method convertPinToBytes = BluetoothDevice.class.getMethod("convertPinToBytes", new Class[] { String.class });
											byte[] pinCodes = (byte[])convertPinToBytes.invoke(BluetoothDevice.class, "0000");
											// PINコード登録
											if (!device.setPin(pinCodes)) {
												Log.w(TAG, "失敗かな？");
											}
										} catch (final NoSuchMethodException e) {
											Log.w(TAG, e);
										} catch (final InvocationTargetException e) {
											Log.w(TAG, e);
										} catch (final IllegalAccessException e) {
											Log.w(TAG, e);
										}
									} else {
										Log.w(TAG, "ペアリングを開始できなかった");
									}
									try {
										mSync.wait(60000);
										Log.w(TAG, "ペアリングを完了できた?");
									} catch (final InterruptedException e) {
									}
									context.unregisterReceiver(receiver);
								}
							} else {
								Log.w(TAG, "Bluetoothがonじゃないよ");
								Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
								context.startActivityForResult(enableBtIntent, 123);
							}
						}
					}).start();
				}
			} else {
				Log.w(TAG, "ペアリングアドレスのフォーマットがおかしい");
			}
		} else {
			throw new RuntimeException("ペアリングアドレスが取得できていない");
		}
	}

	private final Object mSync = new Object();
	private BTBondStateChangeReceiver register(final Context context) {
		final BTBondStateChangeReceiver result = new BTBondStateChangeReceiver();
		final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		context.registerReceiver(result, filter);
		return result;
	}

	@SuppressLint("NewApi")
	private final BluetoothGattCallback mBluetoothGattCallback  = new BluetoothGattCallback () {
		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			super.onConnectionStateChange(gatt, status, newState);
			if (DEBUG) Log.v(TAG, "onConnectionStateChange:newState=" + newState);
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			super.onServicesDiscovered(gatt, status);
			if (DEBUG) Log.v(TAG, "onServicesDiscovered:status=" + status);
		}

		@Override
		public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			super.onCharacteristicRead(gatt, characteristic, status);
			if (DEBUG) Log.v(TAG, "onCharacteristicRead:characteristic=" + characteristic + ", status=" + status);
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, final int status) {
			super.onCharacteristicWrite(gatt, characteristic, status);
			if (DEBUG) Log.v(TAG, "onCharacteristicWrite:characteristic=" + characteristic + ", status=" + status);
		}

		@Override
		public void onCharacteristicChanged(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			super.onCharacteristicChanged(gatt, characteristic);
			if (DEBUG) Log.v(TAG, "onCharacteristicChanged:characteristic=" + characteristic);
		}

		@Override
		public void onDescriptorRead(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor, final int status) {
			super.onDescriptorRead(gatt, descriptor, status);
			if (DEBUG) Log.v(TAG, "onDescriptorRead:descriptor=" + descriptor + ", status=" + status);
		}

		@Override
		public void onDescriptorWrite(final BluetoothGatt gatt, final BluetoothGattDescriptor descriptor,final  int status) {
			super.onDescriptorWrite(gatt, descriptor, status);
			if (DEBUG) Log.v(TAG, "onDescriptorWrite:descriptor=" + descriptor + ", status=" + status);
		}

		@Override
		public void onReliableWriteCompleted(final BluetoothGatt gatt, final int status) {
			super.onReliableWriteCompleted(gatt, status);
			if (DEBUG) Log.v(TAG, "onReliableWriteCompleted:status=" + status);
		}

		@Override
		public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
			super.onReadRemoteRssi(gatt, rssi, status);
			if (DEBUG) Log.v(TAG, "onReadRemoteRssi:rssi=" + rssi + ", status=" + status);
		}

		@Override
		public void onMtuChanged(final BluetoothGatt gatt, final int mtu, final int status) {
			super.onMtuChanged(gatt, mtu, status);
			if (DEBUG) Log.v(TAG, "onMtuChanged:mtu=" + mtu + ", status=" + status);
		}
	};

	private class BTBondStateChangeReceiver extends BroadcastReceiver {
		public BTBondStateChangeReceiver() {
			super();
		}

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (DEBUG) Log.v(TAG, "onReceive:intent=" + intent);
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if (device != null) {
				if (DEBUG) Log.v(TAG, "BluetoothDevice=" + device);
				switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					if (DEBUG) Log.v(TAG, "接続中");
					break;
				case BluetoothDevice.BOND_BONDED:
					if (DEBUG) Log.v(TAG, "接続履歴あり");
					device.connectGatt(context, true, mBluetoothGattCallback);
					synchronized (mSync) {
//						mSync.notifyAll();
					}
					break;
				case BluetoothDevice.BOND_NONE:
					if (DEBUG) Log.v(TAG, "接続履歴なし");
					device.connectGatt(context, true, mBluetoothGattCallback);
					synchronized (mSync) {
//						mSync.notifyAll();
					}
						break;
				}
			}
		}
	}

}
