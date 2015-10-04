package com.serenegiant.gamepad;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.modules.DualShock3;
import com.serenegiant.gamepad.modules.DualShock4;
import com.serenegiant.gamepad.modules.GeneralGamepad;
import com.serenegiant.gamepad.modules.JCU2912;
import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class HIDGamePad {

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

	public int analogLeftX;
	public int analogLeftY;
	public int analogRightX;
	public int analogRightY;
	public volatile int[] keyCount = new int[KEY_NUMS];

	protected final UsbDevice mDevice;
	protected HIDGamePad(final UsbDevice device) {
		mDevice = device;
	}

	protected abstract void parse(final int n, final byte[] data);

	protected static HIDGamePad getGamepad(final UsbDevice device) {
		final int vendor_id = device.getVendorId();
		final int product_id = device.getProductId();
		final String serial;
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
		}
		return new GeneralGamepad(device);
	}

}
