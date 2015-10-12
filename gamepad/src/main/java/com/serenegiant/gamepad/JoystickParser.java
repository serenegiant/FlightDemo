package com.serenegiant.gamepad;

import android.util.Log;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.List;

/**
 * Created by saki on 15/10/13.
 */
public abstract class JoystickParser {
	private static final boolean DEBUG = true; // FIXME 実同時はfalseにすること
	private static final String TAG = JoystickParser.class.getSimpleName();

	public static JoystickParser getJoystick(final InputDevice device) {
		JoystickParser result = null;
		return result;
	}

	private final InputDevice mDevice;
	private final int[] mAxes;
	private final float[] mAxisValues;
	private final SparseIntArray mKeys = new SparseIntArray();

	public JoystickParser(final InputDevice device) {
		mDevice = device;
		int numAxes = 0;
		final List<InputDevice.MotionRange> ranges = device.getMotionRanges();
		for (final InputDevice.MotionRange range : ranges) {
			if (isFromSource(range, InputDevice.SOURCE_CLASS_JOYSTICK)) {
				numAxes += 1;
			}
		}
		mAxes = new int[numAxes];
		mAxisValues = new float[numAxes];
		int i = 0;
		for (final InputDevice.MotionRange range : ranges) {
			if (isFromSource(range, InputDevice.SOURCE_CLASS_JOYSTICK)) {
				mAxes[i++] = range.getAxis();
			}
		}
	}

	protected static boolean isFromSource(final InputDevice.MotionRange range, final int source) {
		return (range != null) && ((range.getSource() & source) == source);
	}

	public InputDevice getDevice() {
		return mDevice;
	}

	public int getAxisCount() {
		return mAxes.length;
	}

	public int getAxis(final int axisIndex) {
		return mAxes[axisIndex];
	}

	public float getAxisValue(final int axisIndex) {
		return mAxisValues[axisIndex];
	}

	public int getKeyCount() {
		return mKeys.size();
	}

	public int getKeyCode(final int keyIndex) {
		return mKeys.keyAt(keyIndex);
	}

	public boolean isKeyPressed(final int keyIndex) {
		return mKeys.valueAt(keyIndex) != 0;
	}

	public boolean onKeyDown(final KeyEvent event) {
		final int keyCode = event.getKeyCode();
		if (isGameKey(keyCode)) {
/*			if (event.getRepeatCount() == 0) {
				final String symbolicName = KeyEvent.keyCodeToString(keyCode);
				mKeys.put(keyCode, 1);
				Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
			} */
			mKeys.put(keyCode, event.getRepeatCount());
			return true;
		}
		return false;
	}

	public boolean onKeyUp(final KeyEvent event) {
		final int keyCode = event.getKeyCode();
		if (isGameKey(keyCode)) {
			mKeys.delete(keyCode);
/*			int index = mKeys.indexOfKey(keyCode);
			if (index >= 0) {
				final String symbolicName = KeyEvent.keyCodeToString(keyCode);
				mKeys.put(keyCode, 0);
				Log.i(TAG, mDevice.getName() + " - Key Up: " + symbolicName);
			} */
			return true;
		}
		return false;
	}

	public boolean onJoystickMotion(final MotionEvent event) {
		final int historySize = event.getHistorySize();
		for (int i = mAxes.length - 1; i >= 0; i--) {
			final int axis = mAxes[i];
			final float value = event.getAxisValue(axis);
			mAxisValues[i] = value;
		}
		return true;
	}

	// Check whether this is a key we care about.
	// In a real game, we would probably let the user configure which keys to use
	// instead of hardcoding the keys like this.
	private static boolean isGameKey(final int keyCode) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_SPACE:
			return true;
		default:
			return KeyEvent.isGamepadButton(keyCode);
		}
	}
}
