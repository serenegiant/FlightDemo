package com.serenegiant.usb;

import com.serenegiant.usb.gamepadmodules.DualShock3;
import com.serenegiant.usb.gamepadmodules.GeneralGamepad;
import com.serenegiant.usb.gamepadmodules.JCU2912;

public abstract class IGamePad {
	public static final int KEY_UNKNOWN = -1;
	public static final int KEY_LEFT_CENTER = 0;
	public static final int KEY_LEFT_UP = 1;
	public static final int KEY_LEFT_RIGHT = 2;
	public static final int KEY_LEFT_DOWN = 3;
	public static final int KEY_LEFT_LEFT = 4;
	public static final int KEY_RIGHT_CENTER = 5;
	public static final int KEY_RIGHT_UP = 6;
	public static final int KEY_RIGHT_RIGHT = 7;
	public static final int KEY_RIGHT_DOWN = 8;
	public static final int KEY_RIGHT_LEFT = 9;
	public static final int KEY_LEFT_1 = 10;    	// 左上前
	public static final int KEY_LEFT_2 = 11;    	// 左上後
	public static final int KEY_CENTER_LEFT = 12;   // 中央左
	public static final int KEY_RIGHT_1 = 13;    	// 右上前
	public static final int KEY_RIGHT_2 = 14;   	// 右上後
	public static final int KEY_CENTER_RIGHT = 15;	// 中央右

	public static final int KEY_NUMS = 16;

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

	public int analogLeftX, analogLeftY;
	public int analogRightX, analogRightY;
	public int[] keyCount = new int[KEY_NUMS];

	protected abstract void parse(final int n, final byte[] data);

	protected static IGamePad getGamepad(final int vendor_id, final int product_id, final String serial) {
		switch (vendor_id) {
		case 1137:
		{
			switch (product_id) {
			case 809:
				return new JCU2912();
			}
			break;
		}
		case 1356:
		{
			switch (product_id) {
			case 616:
				return new DualShock3();
			}
			break;
		}
		}
		return new GeneralGamepad();
	}

}
