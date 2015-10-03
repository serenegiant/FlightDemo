package com.serenegiant.usb.gamepadmodules;

import com.serenegiant.usb.IGamePad;

public class JCU2912 extends IGamePad {

	public JCU2912() {
		super();
	}

	@Override
	protected void parse(final int n, final byte[] data) {
		if (n < 6) return;
		analogLeftX =(data[0] & 0xff) - 0x7f;
		if (Math.abs(analogLeftX) <= 2) analogLeftX = 0;	// オフセットがアナログモードは0x80, デジタルモードは0x7fなので少し丸める
		analogLeftY = (data[1] & 0xff) - 0x7f;
		if (Math.abs(analogLeftY) <= 2) analogLeftY = 0;	// オフセットがアナログモードは0x80, デジタルモードは0x7fなので少し丸める
		//
		keyCount[KEY_RIGHT_LEFT] = (data[5] & 0x10) != 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
		keyCount[KEY_RIGHT_UP] = (data[5] & 0x20) != 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
		keyCount[KEY_RIGHT_DOWN] = (data[5] & 0x40) != 0 ? keyCount[KEY_RIGHT_DOWN] + 1: 0;
		keyCount[KEY_RIGHT_RIGHT] = (data[5] & 0x80) != 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
		//
		keyCount[KEY_LEFT_1] = (data[6] & 0x01) != 0 ? keyCount[KEY_LEFT_1] + 1 : 0;
		keyCount[KEY_RIGHT_1] = (data[6] & 0x02) != 0 ? keyCount[KEY_RIGHT_1] + 1 : 0;
		keyCount[KEY_LEFT_2] = (data[6] & 0x04) != 0 ? keyCount[KEY_LEFT_2] + 1 : 0;
		keyCount[KEY_RIGHT_2] = (data[6] & 0x08) != 0 ? keyCount[KEY_RIGHT_2] + 1 : 0;

		keyCount[KEY_LEFT_CENTER] = (data[6] & 0x10) != 0 ? keyCount[KEY_LEFT_CENTER] + 1 : 0;
		keyCount[KEY_RIGHT_CENTER] = (data[6] & 0x20) != 0 ?+keyCount[KEY_RIGHT_CENTER] + 1 : 0;
		keyCount[KEY_CENTER_LEFT] = (data[6] & 0x40) != 0 ? keyCount[KEY_CENTER_LEFT] + 1 : 0;
		keyCount[KEY_CENTER_RIGHT] = (data[6] & 0x80) != 0 ?+keyCount[KEY_CENTER_RIGHT] + 1 : 0;

		// DPAD(左キーパッド)
		final int dpad = (data[5] & 0x0f);
		if (dpad == 0x0f) {
			// dpadが押されていない時とデジタルモードの時は, 左アナログスティックからDPADデータを生成
			keyCount[KEY_LEFT_LEFT] = analogLeftX < 0 ? keyCount[KEY_LEFT_LEFT] + 1 : 0;
			keyCount[KEY_LEFT_UP] = analogLeftY < 0 ? keyCount[KEY_LEFT_UP] + 1 : 0;
			keyCount[KEY_LEFT_DOWN] = analogLeftY > 0 ? keyCount[KEY_LEFT_DOWN] + 1 : 0;
			keyCount[KEY_LEFT_RIGHT] = analogLeftX > 0 ? keyCount[KEY_LEFT_RIGHT] + 1 : 0;
			keyCount[KEY_RIGHT_A] = keyCount[KEY_RIGHT_UP];
			keyCount[KEY_RIGHT_B] = keyCount[KEY_RIGHT_RIGHT];
			keyCount[KEY_RIGHT_C] = keyCount[KEY_RIGHT_DOWN];
			keyCount[KEY_RIGHT_D] = keyCount[KEY_RIGHT_LEFT];

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
			analogRightX = keyCount[KEY_RIGHT_LEFT] != 0 ? -0x7f : (keyCount[KEY_RIGHT_RIGHT] != 0 ? 0x7f : 0);
			analogRightY = keyCount[KEY_RIGHT_UP] != 0 ? -0x7f : (keyCount[KEY_RIGHT_DOWN] != 0 ? 0x7f : 0);
		} else {
			// アナログモード
			analogRightX = (data[3] & 0xff) - 0x80;
			analogRightY = (data[4] & 0xff) - 0x80;
			if ((data[5] & 0xf0) == 0) {
				// 1-4キーが押されていない時は右アナログスティックの値で更新する
				keyCount[KEY_RIGHT_LEFT] = analogRightX < 0 ? keyCount[KEY_RIGHT_LEFT] + 1 : 0;
				keyCount[KEY_RIGHT_UP] = analogRightY < 0 ? keyCount[KEY_RIGHT_UP] + 1 : 0;
				keyCount[KEY_RIGHT_DOWN] = analogRightY > 0 ? keyCount[KEY_RIGHT_DOWN] + 1 : 0;
				keyCount[KEY_RIGHT_RIGHT] = analogRightX > 0 ? keyCount[KEY_RIGHT_RIGHT] + 1 : 0;
			}
		}
	}
}
