package com.serenegiant.gamepad.modules;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.HIDGamePad;
import static com.serenegiant.gamepad.GamePadConst.*;

public class GeneralGamepad extends HIDGamePad {

	public GeneralGamepad(final UsbDevice device) {
		super(device);
	}

	private static final int EPS = 10;
	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 6) return;
		// 左アナログスティック。不感領域を設ける
		analogLeftX =(data[0] & 0xff) - 0x7f;
		if (Math.abs(analogLeftX) < EPS) analogLeftX = 0;
		analogLeftY = (data[1] & 0xff) - 0x7f;
		if (Math.abs(analogLeftY) < EPS) analogLeftY = 0;
		//
		final byte d5 = data[5];
		final byte d6 = data[6];

		keyCount[KEY_LEFT_1] = (d6 & 0x01) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
		keyCount[KEY_RIGHT_1] = (d6 & 0x02) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
		keyCount[KEY_LEFT_2] = (d6 & 0x04) != 0 ? keyCount[KEY_LEFT_2] + 1 : 0;
		keyCount[KEY_RIGHT_2] = (d6 & 0x08) != 0 ? keyCount[KEY_RIGHT_2] + 1 : 0;

		keyCount[KEY_LEFT_CENTER] = (d6 & 0x10) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (d6 & 0x20) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;
		keyCount[KEY_CENTER_LEFT] = (d6 & 0x40) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (d6 & 0x80) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;

		// DPAD(左キーパッド)
		final int dpad = (d5 & 0x0f);
		if (dpad == 0x0f) {
			// dpadが押されていない時とデジタルモードの時は, 左アナログスティックからDPADデータを生成
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

		if (n < 8) return;
		if ((data[7] & 0x80) != 0) {
			// デジタルモード
			keyCount[KEY_RIGHT_LEFT] = (d5 & 0x80) != 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
			keyCount[KEY_RIGHT_UP] = (d5 & 0x10) != 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = (d5 & 0x40) != 0 ? keyCount[KEY_RIGHT_DOWN] + 1: 0;
			keyCount[KEY_RIGHT_RIGHT] = (d5 & 0x20) != 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
			analogRightX = keyCount[KEY_RIGHT_LEFT] != 0 ? -0x7f : (keyCount[KEY_RIGHT_RIGHT] != 0 ? 0x7f : 0);
			analogRightY = keyCount[KEY_RIGHT_UP] != 0 ? -0x7f : (keyCount[KEY_RIGHT_DOWN] != 0 ? 0x7f : 0);
		} else {
			// アナログモード
			keyCount[KEY_RIGHT_A] = (d5 & 0x20) != 0 ? keyCount[KEY_RIGHT_A] + 1 : 0;
			keyCount[KEY_RIGHT_B] = (d5 & 0x80) != 0 ? keyCount[KEY_RIGHT_B] + 1 : 0;
			keyCount[KEY_RIGHT_C] = (d5 & 0x40) != 0 ? keyCount[KEY_RIGHT_C] + 1: 0;
			keyCount[KEY_RIGHT_D] = (d5 & 0x10) != 0 ? keyCount[KEY_RIGHT_D] + 1 : 0;
			//
			// 右アナログスティック。不感領域を設ける
			analogRightX = (data[3] & 0xff) - 0x80;
			if (Math.abs(analogRightX) < EPS) analogRightX = 0;
			analogRightY = (data[4] & 0xff) - 0x80;
			if (Math.abs(analogRightY) < EPS) analogRightY = 0;
			// 1-4キーが押されていない時は右アナログスティックの値で更新する
			keyCount[KEY_RIGHT_LEFT] = analogRightX < 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
			keyCount[KEY_RIGHT_UP] = analogRightY < 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = analogRightY > 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
			keyCount[KEY_RIGHT_RIGHT] = analogRightX > 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		}
	}
}
