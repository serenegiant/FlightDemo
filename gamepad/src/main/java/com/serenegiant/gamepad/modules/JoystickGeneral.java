package com.serenegiant.gamepad.modules;

import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.JoystickParser;

import static com.serenegiant.gamepad.GamePadConst.*;

public class JoystickGeneral extends JoystickParser {
//	private static final boolean DEBUG = false; // FIXME 実同時はfalseにすること
//	private static final String TAG = JoystickGeneral.class.getSimpleName();

	public JoystickGeneral(InputDevice device) {
		super(device);
	}

	@Override
	protected void update() {
		// 左アナログスティックX
		if (mAxes.indexOfKey(MotionEvent.AXIS_X) >= 0) {
			analogSticks[0] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_X)] * 127);
		} else {
			analogSticks[0] = 0;
		}
		// 左アナログスティックY
		if (mAxes.indexOfKey(MotionEvent.AXIS_Y) >= 0) {
			analogSticks[1] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_Y)] * 127);
		} else {
			analogSticks[1] = 0;
		}
		// 左アナログスティック押し下げ
		if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_THUMBL) >= 0) {
			keyCount[KEY_LEFT_CENTER] = mKeys.get(KeyEvent.KEYCODE_BUTTON_THUMBL);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_9) >= 0) {
			keyCount[KEY_LEFT_CENTER] = mKeys.get(KeyEvent.KEYCODE_BUTTON_9);
		} else {
			keyCount[KEY_LEFT_CENTER] = 0;
		}
		// 右アナログスティックX
		if (mAxes.indexOfKey(MotionEvent.AXIS_RX) >= 0) {
			analogSticks[2] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_RX)] * 127);
		} else if (mAxes.indexOfKey(MotionEvent.AXIS_Z) >= 0) {
			analogSticks[2] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_Z)] * 127);
		} else {
			analogSticks[2] = 0;
		}
		// 右アナログスティックY
		if (mAxes.indexOfKey(MotionEvent.AXIS_RZ) >= 0) {
			analogSticks[3] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_RZ)] * 127);
		} else {
			analogSticks[3] = 0;
		}
		// 右アナログスティック押し下げ
		if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_THUMBR) >= 0) {
			keyCount[KEY_RIGHT_CENTER] = mKeys.get(KeyEvent.KEYCODE_BUTTON_THUMBR);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_10) >= 0) {
			keyCount[KEY_RIGHT_CENTER] = mKeys.get(KeyEvent.KEYCODE_BUTTON_10);
		} else {
			keyCount[KEY_RIGHT_CENTER] = 0;
		}
		// 上端キーL1
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_9) >= 0) {
			keyCount[KEY_LEFT_1] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_9)] * 255);
			if ((keyCount[KEY_LEFT_1] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_L1) >= 0)) {
				keyCount[KEY_LEFT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_L1);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_L1) >= 0) {
			keyCount[KEY_LEFT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_L1);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_5) >= 0) {
			keyCount[KEY_LEFT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_5);
		} else {
			keyCount[KEY_LEFT_1] = 0;
		}
		// 上端キーR1
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_10) >= 0) {
			keyCount[KEY_RIGHT_1] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_10)] * 255);
			if ((keyCount[KEY_RIGHT_1] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_R1) >= 0)) {
				keyCount[KEY_RIGHT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_R1);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_R1) >= 0) {
			keyCount[KEY_RIGHT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_R1);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_6) >= 0) {
			keyCount[KEY_RIGHT_1] = mKeys.get(KeyEvent.KEYCODE_BUTTON_6);
		} else {
			keyCount[KEY_RIGHT_1] = 0;
		}
		// 上端キーL2
		if (mAxes.indexOfKey(MotionEvent.AXIS_BRAKE) >= 0) {
			keyCount[KEY_LEFT_2] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_BRAKE)] * 255);
			if ((keyCount[KEY_LEFT_2] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_L2) >= 0)) {
				keyCount[KEY_LEFT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_L2);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_L2) >= 0) {
			keyCount[KEY_LEFT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_L2);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_7) >= 0) {
			keyCount[KEY_LEFT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_7);
		} else {
			keyCount[KEY_LEFT_2] = 0;
		}
		// 上端キーR2
		if (mAxes.indexOfKey(MotionEvent.AXIS_GAS) >= 0) {
			keyCount[KEY_RIGHT_2] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GAS)] * 255);
			if ((keyCount[KEY_RIGHT_2] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_R2) >= 0)) {
				keyCount[KEY_RIGHT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_R2);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_R2) >= 0) {
			keyCount[KEY_RIGHT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_R2);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_8) >= 0) {
			keyCount[KEY_RIGHT_2] = mKeys.get(KeyEvent.KEYCODE_BUTTON_8);
		} else {
			keyCount[KEY_RIGHT_2] = 0;
		}
		// 左キーパッド上
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_5) >= 0) {
			keyCount[KEY_LEFT_UP] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_5)] * 255);
			if ((keyCount[KEY_LEFT_UP] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_UP) >= 0)) {
				keyCount[KEY_LEFT_UP] = mKeys.get(KeyEvent.KEYCODE_DPAD_UP);
			}
		} else if (mAxes.indexOfKey(MotionEvent.AXIS_HAT_Y) >= 0) {
			keyCount[KEY_LEFT_UP] = mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_Y)] < 0 ? 255 : 0;
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_UP) >= 0) {
			keyCount[KEY_LEFT_UP] = mKeys.get(KeyEvent.KEYCODE_DPAD_UP);
		} else {
			keyCount[KEY_LEFT_UP] = 0;
		}
		// 左キーパッド右
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_6) >= 0) {
			keyCount[KEY_LEFT_RIGHT] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_6)] * 255);
			if ((keyCount[KEY_LEFT_RIGHT] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_RIGHT) >= 0)) {
				keyCount[KEY_LEFT_RIGHT] = mKeys.get(KeyEvent.KEYCODE_DPAD_RIGHT);
			}
		} else if (mAxes.indexOfKey(MotionEvent.AXIS_HAT_X) >= 0) {
			keyCount[KEY_LEFT_RIGHT] = mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_X)] > 0
				? (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_X)] * 255) : 0;
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_RIGHT) >= 0) {
			keyCount[KEY_LEFT_RIGHT] = mKeys.get(KeyEvent.KEYCODE_DPAD_RIGHT);
		} else {
			keyCount[KEY_LEFT_RIGHT] = 0;
		}
		// 左キーパッド下
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_7) >= 0) {
			keyCount[KEY_LEFT_DOWN] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_7)] * 255);
			if ((keyCount[KEY_LEFT_DOWN] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_DOWN) >= 0)) {
				keyCount[KEY_LEFT_DOWN] = mKeys.get(KeyEvent.KEYCODE_DPAD_DOWN);
			}
		} else if (mAxes.indexOfKey(MotionEvent.AXIS_HAT_Y) >= 0) {
			keyCount[KEY_LEFT_DOWN] = mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_Y)] > 0
				? (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_Y)] * 255) : 0;
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_DOWN) >= 0) {
			keyCount[KEY_LEFT_DOWN] = mKeys.get(KeyEvent.KEYCODE_DPAD_DOWN);
		} else {
			keyCount[KEY_LEFT_DOWN] = 0;
		}
		// 左キーパッド左
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_8) >= 0) {
			keyCount[KEY_LEFT_LEFT] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_8)] * 255);
			if ((keyCount[KEY_LEFT_LEFT] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_LEFT) >= 0)) {
				keyCount[KEY_LEFT_LEFT] = mKeys.get(KeyEvent.KEYCODE_DPAD_LEFT);
			}
		} else if (mAxes.indexOfKey(MotionEvent.AXIS_HAT_X) >= 0) {
			keyCount[KEY_LEFT_LEFT] = mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_X)] < 0
				? (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_HAT_X)] * 255) : 0;
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_DPAD_LEFT) >= 0) {
			keyCount[KEY_LEFT_LEFT] = mKeys.get(KeyEvent.KEYCODE_DPAD_LEFT);
		} else {
			keyCount[KEY_LEFT_LEFT] = 0;
		}
		// 右キーパッド上
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_11) >= 0) {
			keyCount[KEY_RIGHT_UP] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_11)] * 255);
			if ((keyCount[KEY_RIGHT_UP] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_Y) >= 0)) {
				keyCount[KEY_RIGHT_UP] = mKeys.get(KeyEvent.KEYCODE_BUTTON_Y);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_Y) >= 0) {
			keyCount[KEY_RIGHT_UP] = mKeys.get(KeyEvent.KEYCODE_BUTTON_Y);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_2) >= 0) {
			keyCount[KEY_RIGHT_UP] = mKeys.get(KeyEvent.KEYCODE_BUTTON_2);
		} else {
			keyCount[KEY_RIGHT_UP] = 0;
		}
		keyCount[KEY_RIGHT_A] = keyCount[KEY_RIGHT_UP];
		// 右キーパッド右
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_12) >= 0) {
			keyCount[KEY_RIGHT_RIGHT] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_12)] * 255);
			if ((keyCount[KEY_RIGHT_RIGHT] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_B) >= 0)) {
				keyCount[KEY_RIGHT_RIGHT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_B);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_B) >= 0) {
			keyCount[KEY_RIGHT_RIGHT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_B);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_4) >= 0) {
			keyCount[KEY_RIGHT_RIGHT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_4);
		} else {
			keyCount[KEY_RIGHT_RIGHT] = 0;
		}
		keyCount[KEY_RIGHT_B] = keyCount[KEY_RIGHT_RIGHT];
		// 右キーパッド下
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_13) >= 0) {
			keyCount[KEY_RIGHT_DOWN] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_13)] * 255);
			if ((keyCount[KEY_RIGHT_DOWN] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_A) >= 0)) {
				keyCount[KEY_RIGHT_DOWN] = mKeys.get(KeyEvent.KEYCODE_BUTTON_A);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_A) >= 0) {
			keyCount[KEY_RIGHT_DOWN] = mKeys.get(KeyEvent.KEYCODE_BUTTON_A);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_3) >= 0) {
			keyCount[KEY_RIGHT_DOWN] = mKeys.get(KeyEvent.KEYCODE_BUTTON_3);
		} else {
			keyCount[KEY_RIGHT_DOWN] = 0;
		}
		keyCount[KEY_RIGHT_C] = keyCount[KEY_RIGHT_DOWN];
		// 右キーパッド左
		if (mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_14) >= 0) {
			keyCount[KEY_RIGHT_LEFT] = (int)(mAxisValues[mAxes.indexOfKey(MotionEvent.AXIS_GENERIC_14)] * 255);
			if ((keyCount[KEY_RIGHT_LEFT] == 0) && (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_X) >= 0)) {
				keyCount[KEY_RIGHT_LEFT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_X);
			}
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_X) >= 0) {
			keyCount[KEY_RIGHT_LEFT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_X);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_1) >= 0) {
			keyCount[KEY_RIGHT_LEFT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_1);
		} else {
			keyCount[KEY_RIGHT_LEFT] = 0;
		}
		keyCount[KEY_RIGHT_D] = keyCount[KEY_RIGHT_LEFT];
		// 中央左
		if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_SELECT) >= 0) {
			keyCount[KEY_CENTER_LEFT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_SELECT);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_11) >= 0) {
			keyCount[KEY_CENTER_LEFT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_11);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BACK) >= 0) {
			keyCount[KEY_CENTER_LEFT] = mKeys.get(KeyEvent.KEYCODE_BACK);
		} else {
			keyCount[KEY_CENTER_LEFT] = 0;
		}
		// 中央右
		if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_START) >= 0) {
			keyCount[KEY_CENTER_RIGHT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_START);
		} else if (mKeys.indexOfKey(KeyEvent.KEYCODE_BUTTON_12) >= 0) {
			keyCount[KEY_CENTER_RIGHT] = mKeys.get(KeyEvent.KEYCODE_BUTTON_12);
		} else {
			keyCount[KEY_CENTER_RIGHT] = 0;
		}
	}


}
