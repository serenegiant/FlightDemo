package com.serenegiant.gamepad.modules;

import android.hardware.usb.UsbDevice;

import com.serenegiant.gamepad.HIDParser;
import static com.serenegiant.gamepad.GamePadConst.*;

public class DualShock3 extends HIDParser {

	public DualShock3(final UsbDevice device) {
		super(device);
	}

	private static final int EPS = 10;
	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 10) return;
		analogSticks[0] = (data[6] & 0xff) - 0x80;
		if (Math.abs(analogSticks[0]) < EPS) analogSticks[0] = 0;
		analogSticks[1] = (data[7] & 0xff) - 0x80;
		if (Math.abs(analogSticks[1]) < EPS) analogSticks[1] = 0;
		analogSticks[2] = (data[8] & 0xff) - 0x80;
		if (Math.abs(analogSticks[2]) < EPS) analogSticks[2] = 0;
		analogSticks[3] = (data[9] & 0xff) - 0x80;
		if (Math.abs(analogSticks[3]) < EPS) analogSticks[3] = 0;

		final byte d2 = data[2];
		final byte d3 = data[3];
		// 中央キー
		keyCount[KEY_CENTER_LEFT] = (d2 & 0x01) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_LEFT_CENTER] = (d2 & 0x02) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (d2 & 0x04) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (d2 & 0x08) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;
		// 右キーバッド
		keyCount[KEY_RIGHT_A] = (d3 & 0x10) != 0 ? keyCount[KEY_RIGHT_A] + 1 : 0;
		keyCount[KEY_RIGHT_B] = (d3 & 0x20) != 0 ? keyCount[KEY_RIGHT_B] + 1 : 0;
		keyCount[KEY_RIGHT_C] = (d3 & 0x40) != 0 ? keyCount[KEY_RIGHT_C] + 1 : 0;
		keyCount[KEY_RIGHT_D] = (d3 & 0x80) != 0 ? keyCount[KEY_RIGHT_D] + 1 : 0;
		if (n < 26) {
			// DPAD(左キーバッド)
			keyCount[KEY_LEFT_LEFT] = (d2 & 0x80) != 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = (d2 & 0x10) != 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = (d2 & 0x40) != 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = (d2 & 0x20) != 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
			// 上端キー
			keyCount[KEY_LEFT_2] = (d3 & 0x01) != 0 ? keyCount[KEY_LEFT_2] + 1 : 0;
			keyCount[KEY_RIGHT_2] = (d3 & 0x02) != 0 ? keyCount[KEY_RIGHT_2] + 1 : 0;
			keyCount[KEY_LEFT_1] = (d3 & 0x04) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
			keyCount[KEY_RIGHT_1] = (d3 & 0x08) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
			// 右キーバッド
			keyCount[KEY_RIGHT_UP] = keyCount[KEY_RIGHT_A];
			keyCount[KEY_RIGHT_RIGHT] = keyCount[KEY_RIGHT_B];
			keyCount[KEY_RIGHT_DOWN] = keyCount[KEY_RIGHT_C];
			keyCount[KEY_RIGHT_LEFT] = keyCount[KEY_RIGHT_D];
		} else {
			// DPAD(左キーバッド)
			final int d14 = data[14] & 0xff;
			final int d15 = data[15] & 0xff;
			final int d16 = data[16] & 0xff;
			final int d17 = data[17] & 0xff;
			keyCount[KEY_LEFT_UP] = d14 > EPS ? d14 : 0;
			keyCount[KEY_LEFT_RIGHT] = d15 > EPS ? d15 : 0;
			keyCount[KEY_LEFT_DOWN] = d16 > EPS ? d16 : 0;
			keyCount[KEY_LEFT_LEFT] = d17 > EPS ? d17 : 0;
			// 上端キー
			final int d18 = data[18] & 0xff;
			final int d19 = data[19] & 0xff;
			final int d20 = data[20] & 0xff;
			final int d21 = data[21] & 0xff;
			keyCount[KEY_LEFT_2] = d18 > EPS ? d18 : 0;
			keyCount[KEY_RIGHT_2] = d19 > EPS ? d19 : 0;
			keyCount[KEY_LEFT_1] = d20 > EPS ? d20 : 0;
			keyCount[KEY_RIGHT_1] = d21 > EPS ? d21 : 0;
			// 右キーバッド
			final int d22 = data[22] & 0xff;
			final int d23 = data[23] & 0xff;
			final int d24 = data[24] & 0xff;
			final int d25 = data[25] & 0xff;
			keyCount[KEY_RIGHT_UP] = d22 > EPS ? d22 : 0;
			keyCount[KEY_RIGHT_RIGHT] = d23 > EPS ? d23 : 0;
			keyCount[KEY_RIGHT_DOWN] = d24 > EPS ? d24 : 0;
			keyCount[KEY_RIGHT_LEFT] = d25 > EPS ? d25 : 0;
		}
/*		if (n < 50) return;
		// FIXME 加速度センサーの処理 */
	}
}
