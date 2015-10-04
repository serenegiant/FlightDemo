package com.serenegiant.gamepad.modules;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.HIDGamePad;
import static com.serenegiant.gamepad.GamePadConst.*;

public class DualShock3 extends HIDGamePad {

	public DualShock3(final UsbDevice device) {
		super(device);
	}

	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 16) return;
		analogLeftX =(data[6] & 0xff) - 0x80;
		if (Math.abs(analogLeftX) < 6) analogLeftX = 0;
		analogLeftY = (data[7] & 0xff) - 0x80;
		if (Math.abs(analogLeftY) < 6) analogLeftY = 0;
		analogRightX = (data[8] & 0xff) - 0x80;
		if (Math.abs(analogRightX) < 6) analogRightX = 0;
		analogRightY = (data[9] & 0xff) - 0x80;
		if (Math.abs(analogRightY) < 6) analogRightY = 0;

		final byte d2 = data[2];
		final byte d3 = data[3];
		//
		keyCount[KEY_CENTER_LEFT] = (d2 & 0x01) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_LEFT_CENTER] = (d2 & 0x02) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (d2 & 0x04) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (d2 & 0x08) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;
		if ((d2 & 0xf0) == 0) {
			// dpadが押されていない時は, 左アナログスティックからDPADデータを生成
			keyCount[KEY_LEFT_LEFT] = analogLeftX < 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = analogLeftY < 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = analogLeftY > 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = analogLeftX > 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		} else {
			keyCount[KEY_LEFT_LEFT] = (d2 & 0x80) != 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = (d2 & 0x10) != 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = (d2 & 0x40) != 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = (d2 & 0x20) != 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
		}
		//
		keyCount[KEY_LEFT_2] = (d3 & 0x01) != 0 ? keyCount[KEY_LEFT_2] + 1 : 0;
		keyCount[KEY_RIGHT_2] = (d3 & 0x02) != 0 ? keyCount[KEY_RIGHT_2] + 1 : 0;
		keyCount[KEY_LEFT_1] = (d3 & 0x04) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
		keyCount[KEY_RIGHT_1] = (d3 & 0x08) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
		keyCount[KEY_RIGHT_UP] = (d3 & 0x10) != 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
		keyCount[KEY_RIGHT_RIGHT] = (d3 & 0x20) != 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		keyCount[KEY_RIGHT_DOWN] = (d3 & 0x40) != 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
		keyCount[KEY_RIGHT_LEFT] = (d3 & 0x80) != 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
		// 右キーパッドの値は他との互換性のために専用領域にコピー
		keyCount[KEY_RIGHT_A] = keyCount[KEY_RIGHT_UP];
		keyCount[KEY_RIGHT_B] = keyCount[KEY_RIGHT_RIGHT];
		keyCount[KEY_RIGHT_C] = keyCount[KEY_RIGHT_DOWN];
		keyCount[KEY_RIGHT_D] = keyCount[KEY_RIGHT_LEFT];
		if ((d3 & 0xf0) == 0) {
			// 右キーパッドが押されていない時は、右アナログスティックからデータを生成
			keyCount[KEY_RIGHT_UP] = analogRightY < 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
			keyCount[KEY_RIGHT_RIGHT] = analogRightX > 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
			keyCount[KEY_RIGHT_DOWN] = analogRightY > 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
			keyCount[KEY_RIGHT_LEFT] = analogRightX < 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
		}
/*		//
		if (n < 26) return;
		// FIXME ボタンの押し下げ圧力の処理
		if (n < 50) return;
		// FIXME 加速度センサーの処理 */
	}
}
