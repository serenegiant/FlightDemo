package com.serenegiant.gamepad;

import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

import com.serenegiant.gamepad.modules.DualShock3;
import com.serenegiant.gamepad.modules.DualShock4;
import com.serenegiant.gamepad.modules.GeneralGamepad;
import com.serenegiant.gamepad.modules.XInputF310rGamepad;
import com.serenegiant.gamepad.modules.JCU2912;
import com.serenegiant.gamepad.modules.XInputGeneral;

import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class HIDParser {
	private static final boolean DEBUG = true; // 実同時はfalseにすること
	private static final String TAG = HIDParser.class.getSimpleName();

	// DPADのキーマスクビット
	protected static final int DPAD_UP = 0x01;
	protected static final int DPAD_RIGHT = 0x02;
	protected static final int DPAD_DOWN = 0x04;
	protected static final int DPAD_LEFT = 0x08;
	protected static final int[] DPAD_DIRECTIONS = {
		DPAD_UP,				// 0:上
		DPAD_UP | DPAD_RIGHT,	// 1:右上
		DPAD_RIGHT,				// 2:右
		DPAD_RIGHT | DPAD_DOWN,	// 3:右下
		DPAD_DOWN,				// 4:下
		DPAD_DOWN | DPAD_LEFT,	// 5:左下
		DPAD_LEFT,				// 6:左
		DPAD_LEFT | DPAD_UP,	// 7:左上
		0x00,					// 8:
		0x00,					// 9:
		0x00,					//10:
		0x00,					//11:
		0x00,					//12:
		0x00,					//13:
		0x00,					//14:
		0x00,					//15=0x0f:何も押していない
	};

	/** 0:左x(左端-0x7f,右端+0x7f), 1:左y(上端-0x7f,下端+0x7f), 2:右x(左端-0x7f,右端+0x7f), 3:右y(上端-0x7f,下端+0x7f) */
	public final int[] analogSticks = new int[4];
//	public int analogLeftX;
//	public int analogLeftY;
//	public int analogRightX;
//	public int analogRightY;
	public volatile int[] keyCount = new int[KEY_NUMS];

	protected final UsbDevice mDevice;
	protected HIDParser(final UsbDevice device) {
		mDevice = device;
	}

	protected abstract void parse(final int n, final byte[] data);

	protected static HIDParser getGamepad(final UsbDevice device) {
		final int clazz = device.getDeviceClass();
		final int sub_class = device.getDeviceSubclass();
		final int protocol = device.getDeviceProtocol();
		final int vendor_id = device.getVendorId();
		final int product_id = device.getProductId();
		final String serial;

		int cs_class = 0;
		int cs_sub_class = 0;
		int cs_protocol = 0;
		final int num_interface = device.getInterfaceCount();
		if (DEBUG) Log.v(TAG, "num_interface:" + num_interface);
		for (int j = 0; j < num_interface; j++) {
			final UsbInterface intf = device.getInterface(j);
			final int num_endpoint = intf.getEndpointCount();
			if (DEBUG) Log.v(TAG, "num_endpoint:" + num_endpoint);
			if (num_endpoint > 0) {
				UsbEndpoint ep_in = null;
				UsbEndpoint ep_out = null;
				for (int i = 0; i < num_endpoint; i++) {
					final UsbEndpoint ep = intf.getEndpoint(i);
					if (DEBUG)
						Log.v(TAG, "type=" + ep.getType() + ", dir=" + ep.getDirection());
					if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {    // インタラプト転送
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
					// 入力インターフェースのエンドポイントが見つかった
					cs_class = intf.getInterfaceClass();
					cs_sub_class = intf.getInterfaceSubclass();
					cs_protocol = intf.getInterfaceProtocol();
					break;
				}
				if (ep_out != null) {
					// HID出力インターフェースのエンドポイントが見つかった
					// FIXME 出力は未サポートなので何もしない
				}
			}
		}

		if (DEBUG) Log.v(TAG, String.format("vendor_id=%d, product_id=%d", vendor_id, product_id));
		switch (vendor_id) {
		case 4607:	// ELECOM
		{
			switch (product_id) {
			case 13105:
				return new JCU2912(device);
			}
			break;
		}
		case 1356:	// SONY
		{
			switch (product_id) {
			case 616:
				return new DualShock3(device);
			case 1476:
				return new DualShock4(device);
			}
			break;
		}
		case 1133:	// Logicool/Logitech
		{
			switch (product_id) {
			case 49693:
				return new XInputF310rGamepad(device);
			}
		}
		}
		if ((clazz == 0xff) && (sub_class == 0xff) && (product_id == 0xff)	// vendor specific
			&& (cs_class == 0xff) && (cs_sub_class == 0x5d) && (cs_protocol == 0x01)) {
			// たぶんx-input
			return new XInputGeneral(device);
		} else{
			return new GeneralGamepad(device);
		}
	}

}
