package com.serenegiant.usb.gamepadmodules;

import com.serenegiant.usb.IGamePad;

public class JCU2912 extends IGamePad {

	public JCU2912() {
		super();
	}

	@Override
	protected void parse(final int n, final byte[] data) {
		if (n != 8) return;
		analogLeftX = ((int)data[0]) & 0xff - 0x80;
		analogLeftY = ((int)data[1]) & 0xff - 0x80;
		//
		keyCount[KEY_RIGHT_LEFT] = (data[5] & 0x10) != 0 ? ++keyCount[KEY_RIGHT_LEFT] : 0;
		keyCount[KEY_RIGHT_UP] = (data[5] & 0x20) != 0 ? ++keyCount[KEY_RIGHT_UP] : 0;
		keyCount[KEY_RIGHT_DOWN] = (data[5] & 0x40) != 0 ? ++keyCount[KEY_RIGHT_DOWN] : 0;
		keyCount[KEY_RIGHT_RIGHT] = (data[5] & 0x80) != 0 ? ++keyCount[KEY_RIGHT_RIGHT] : 0;
		//
		keyCount[KEY_LEFT_1] = (data[6] & 0x01) != 0 ? ++keyCount[KEY_LEFT_1] : 0;
		keyCount[KEY_RIGHT_1] = (data[6] & 0x02) != 0 ? ++keyCount[KEY_RIGHT_1] : 0;
		keyCount[KEY_LEFT_2] = (data[6] & 0x04) != 0 ? ++keyCount[KEY_LEFT_2] : 0;
		keyCount[KEY_RIGHT_2] = (data[6] & 0x08) != 0 ? ++keyCount[KEY_RIGHT_2] : 0;

		keyCount[KEY_LEFT_CENTER] = (data[6] & 0x10) != 0 ? ++keyCount[KEY_LEFT_CENTER] : 0;
		keyCount[KEY_RIGHT_CENTER] = (data[6] & 0x20) != 0 ? ++keyCount[KEY_RIGHT_CENTER] : 0;
		keyCount[KEY_CENTER_LEFT] = (data[6] & 0x40) != 0 ? ++keyCount[KEY_CENTER_LEFT] : 0;
		keyCount[KEY_CENTER_RIGHT] = (data[6] & 0x80) != 0 ? ++keyCount[KEY_CENTER_RIGHT] : 0;

		if ((data[7] & 0x80) != 0) {
			// デジタルモード
			analogRightX = keyCount[KEY_RIGHT_LEFT] != 0 ? -0x7f : (keyCount[KEY_RIGHT_RIGHT] != 0 ? 0x7f : 0);
			analogRightX = keyCount[KEY_RIGHT_UP] != 0 ? -0x7f : (keyCount[KEY_RIGHT_DOWN] != 0 ? 0x7f : 0);
		} else {
			// アナログモード
			analogRightX = ((int)data[3]) & 0xff - 0x80;
			analogRightY = ((int)data[4]) & 0xff - 0x80;
			// DPAD(左キーパッド)
			final int dir = DPAD_DIRECTIONS[(data[5] & 0x0f)];
			keyCount[KEY_LEFT_LEFT] = (dir & DPAD_LEFT) != 0 ? ++keyCount[KEY_LEFT_LEFT] : 0;
			keyCount[KEY_LEFT_UP] = (dir & DPAD_UP) != 0 ? ++keyCount[KEY_LEFT_UP] : 0;
			keyCount[KEY_LEFT_DOWN] = (dir & DPAD_DOWN) != 0 ? ++keyCount[KEY_LEFT_DOWN] : 0;
			keyCount[KEY_LEFT_RIGHT] = (dir & DPAD_RIGHT) != 0 ? ++keyCount[KEY_LEFT_RIGHT] : 0;
		}
	}
}
