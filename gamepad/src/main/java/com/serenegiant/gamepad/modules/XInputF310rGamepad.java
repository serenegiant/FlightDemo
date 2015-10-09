package com.serenegiant.gamepad.modules;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.HIDParser;

import static com.serenegiant.gamepad.GamePadConst.KEY_CENTER_LEFT;
import static com.serenegiant.gamepad.GamePadConst.KEY_CENTER_RIGHT;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_1;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_2;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_CENTER;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_DOWN;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_LEFT;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_RIGHT;
import static com.serenegiant.gamepad.GamePadConst.KEY_LEFT_UP;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_1;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_2;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_A;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_B;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_C;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_CENTER;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_D;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_DOWN;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_LEFT;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_RIGHT;
import static com.serenegiant.gamepad.GamePadConst.KEY_RIGHT_UP;

public class XInputF310rGamepad extends HIDParser {

	public XInputF310rGamepad(final UsbDevice device) {
		super(device);
	}

	private static final int EPS = 10;
	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 14) return;
		// 左アナログスティック。不感領域を設ける
		analogLeftX = (data[6] & 0xff) - 0x80;
		if (Math.abs(analogLeftX) < EPS) analogLeftX = 0;
		analogLeftY = 0x80 - (data[8] & 0xff);
		if (Math.abs(analogLeftY) < EPS) analogLeftY = 0;
		//
		final byte d2 = data[2];
		final byte d3 = data[3];
		final int d4 = data[4] & 0xff;
		final int d5 = data[5] & 0xff;

		keyCount[KEY_LEFT_1] = (d3 & 0x01) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
		keyCount[KEY_RIGHT_1] = (d3 & 0x02) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
		keyCount[KEY_LEFT_2] = d4 > EPS ? d4 : 0;
		keyCount[KEY_RIGHT_2] = d5 > EPS ? d5 : 0;

		keyCount[KEY_LEFT_CENTER] = (d2 & 0x40) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (d2 & 0x80) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;
		keyCount[KEY_CENTER_LEFT] = (d2 & 0x20) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (d2 & 0x10) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;

		// DPAD(左キーパッド)
		if ((d2 & 0x0f) == 0) {
			// DPADが押されていない時は左アナログスティックの値で更新する
			keyCount[KEY_LEFT_LEFT] = analogLeftX < 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = analogLeftY < 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = analogLeftY > 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = analogLeftX > 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		} else {
			keyCount[KEY_LEFT_LEFT] = (d2 & 0x04) != 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = (d2 & 0x01) != 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = (d2 & 0x02) != 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = (d2 & 0x08) != 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		}

		// 右アナログスティック。不感領域を設ける
		analogRightX = (data[10] & 0xff) - 0x80;
		if (Math.abs(analogRightX) < EPS) analogRightX = 0;
		analogRightY = 0x80 - (data[12] & 0xff) ;
		if (Math.abs(analogRightY) < EPS) analogRightY = 0;
		keyCount[KEY_RIGHT_A] = (d3 & 0x80) != 0 ? keyCount[KEY_RIGHT_A] + 1 : 0;
		keyCount[KEY_RIGHT_B] = (d3 & 0x20) != 0 ? keyCount[KEY_RIGHT_B] + 1 : 0;
		keyCount[KEY_RIGHT_C] = (d3 & 0x10) != 0 ? keyCount[KEY_RIGHT_C] + 1: 0;
		keyCount[KEY_RIGHT_D] = (d3 & 0x40) != 0 ? keyCount[KEY_RIGHT_D] + 1 : 0;
		// 右キーパッド
		if ((d3 & 0xf0) == 0) {
			// 右キーパッドが押されていない時は右アナログスティックの値で更新する
			keyCount[KEY_RIGHT_LEFT] = analogRightX < 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
			keyCount[KEY_RIGHT_UP] = analogRightY < 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = analogRightY > 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
			keyCount[KEY_RIGHT_RIGHT] = analogRightX > 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		} else {
			keyCount[KEY_RIGHT_LEFT] = (d3 & 0x40) != 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
			keyCount[KEY_RIGHT_UP] = (d3 & 0x80) != 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = (d3 & 0x10) != 0 ? keyCount[KEY_RIGHT_DOWN] + 1: 0;
			keyCount[KEY_RIGHT_RIGHT] = (d3 & 0x20) != 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		}
	}
}
