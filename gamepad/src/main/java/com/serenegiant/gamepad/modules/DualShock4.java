package com.serenegiant.gamepad.modules;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.HIDGamePad;

public class DualShock4 extends HIDGamePad {

	public DualShock4(final UsbDevice device) {
		super(device);
	}

	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 16) return;
		analogLeftX =(data[1] & 0xff) - 0x80;
		if (Math.abs(analogLeftX) < 6) analogLeftX = 0;
		analogLeftY = (data[2] & 0xff) - 0x80;
		if (Math.abs(analogLeftY) < 6) analogLeftY = 0;
		analogRightX = (data[3] & 0xff) - 0x80;
		if (Math.abs(analogRightX) < 6) analogRightX = 0;
		analogRightY = (data[4] & 0xff) - 0x80;
		if (Math.abs(analogRightY) < 6) analogRightY = 0;

		final byte d5 = data[5];
		final byte d6 = data[6];

		// DPAD(左キーパッド)
		final int dpad = (d5 & 0x0f);
		if (dpad == 0x0f) {
			// dpadが押されていない時は, 左アナログスティックからDPADデータを生成
			keyCount[KEY_LEFT_LEFT] = analogLeftX < 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = analogLeftY < 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = analogLeftY > 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = analogLeftX > 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		} else {
			final int dir = DPAD_DIRECTIONS[dpad];
			keyCount[KEY_LEFT_LEFT] = (dir & DPAD_LEFT) != 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = (dir & DPAD_UP) != 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = (dir & DPAD_DOWN) != 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = (dir & DPAD_RIGHT) != 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		}

		//
		keyCount[KEY_RIGHT_LEFT] = (d5 & 0x10) != 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
		keyCount[KEY_RIGHT_DOWN] = (d5 & 0x20) != 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
		keyCount[KEY_RIGHT_RIGHT] = (d5 & 0x40) != 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		keyCount[KEY_RIGHT_UP] = (d5 & 0x80) != 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
		// 右キーパッドの値は他との互換性のために専用領域にコピー
		keyCount[KEY_RIGHT_A] = keyCount[KEY_RIGHT_UP];
		keyCount[KEY_RIGHT_B] = keyCount[KEY_RIGHT_RIGHT];
		keyCount[KEY_RIGHT_C] = keyCount[KEY_RIGHT_DOWN];
		keyCount[KEY_RIGHT_D] = keyCount[KEY_RIGHT_LEFT];
		if ((d5 & 0xf0) == 0) {
			// 右キーパッドが押されていない時は、右アナログスティックからデータを生成
			keyCount[KEY_RIGHT_UP] = analogRightY < 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_RIGHT] = analogRightX > 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = analogRightY > 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
			keyCount[KEY_RIGHT_LEFT] = analogRightX < 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
		}

		//
		keyCount[KEY_LEFT_1] = (d6 & 0x01) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
		keyCount[KEY_RIGHT_1] = (d6 & 0x02) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
		keyCount[KEY_LEFT_2] = (d6 & 0x04) != 0 ? keyCount[KEY_LEFT_2] + 1 : 0;
		keyCount[KEY_RIGHT_2] = (d6 & 0x08) != 0 ? keyCount[KEY_RIGHT_2] + 1 : 0;
		keyCount[KEY_CENTER_LEFT] = (d6 & 0x10) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (d6 & 0x20) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;
		keyCount[KEY_LEFT_CENTER] = (d6 & 0x40) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (d6 & 0x80) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;

	}
}
