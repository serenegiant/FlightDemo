package com.serenegiant.gamepad;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.modules.JoystickGeneral;
import com.serenegiant.utils.BuildCheck;

import java.util.List;

import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class JoystickParser {
//	private static final boolean DEBUG = false; // FIXME 実同時はfalseにすること
//	private static final String TAG = JoystickParser.class.getSimpleName();

	@SuppressLint("NewApi")
	public static JoystickParser getJoystick(final InputDevice device) {
		final int vid = BuildCheck.isKitKat() ? device.getVendorId() : 0;
		final int pid = BuildCheck.isKitKat() ? device.getProductId() : 0;
		JoystickParser result = new JoystickGeneral(device);
		return result;
	}

	protected final InputDevice mDevice;
//	protected final int[] mAxes;
	protected final float[] mAxisValues;
	/** key=AXIS_XXX, value=index */
	protected final SparseIntArray mAxes = new SparseIntArray();
	/** key=KEYCODE_XXX,value=押し下げ状態 */
	protected final SparseIntArray mKeys = new SparseIntArray();

	public final int[] analogSticks = new int[4];
	public volatile int[] keyCount = new int[KEY_NUMS];

	public JoystickParser(final InputDevice device) {
		mDevice = device;
		int numAxes = 0;
		final List<InputDevice.MotionRange> ranges = device.getMotionRanges();
		for (final InputDevice.MotionRange range : ranges) {
			if (isFromSource(range, InputDevice.SOURCE_CLASS_JOYSTICK)) {
				numAxes++;
			}
		}
		mAxisValues = new float[numAxes];
		int i = 0;
		for (final InputDevice.MotionRange range : ranges) {
			if (isFromSource(range, InputDevice.SOURCE_CLASS_JOYSTICK)) {
				mAxes.put(range.getAxis(), i);
				i++;
			}
		}
	}

	public static boolean isFromSource(final InputDevice.MotionRange range, final int source) {
		return (range != null) && ((range.getSource() & source) == source);
	}

	public InputDevice getDevice() {
		return mDevice;
	}

	public String getName() {
		return mDevice != null ? mDevice.getName() : null;
	}

	public int getAxisCount() {
		return mAxes.size();
	}

	public int getAxis(final int axisIndex) {
		return mAxes.keyAt(axisIndex);
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

	public boolean isJoystick() {
		return (mDevice.getSources() & InputDevice.SOURCE_CLASS_JOYSTICK) == InputDevice.SOURCE_CLASS_JOYSTICK;
	}

	public boolean onKeyDown(final KeyEvent event) {
		final int keyCode = event.getKeyCode();
		if (isJoystick()) {
			if (event.getRepeatCount() == 0) {
				mKeys.put(keyCode, 255);
//				final String symbolicName = KeyEvent.keyCodeToString(keyCode);
//				Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
			}
			update();
			return true;
		}
		return false;
	}

	public boolean onKeyUp(final KeyEvent event) {
		final int keyCode = event.getKeyCode();
		if (isJoystick()) {
			mKeys.delete(keyCode);
			update();
			return true;
		}
		return false;
	}

	public boolean onJoystickMotion(final MotionEvent event) {
//		final int historySize = event.getHistorySize();
		final int n = mAxes.size();
		for (int i = 0; i < n; i++) {
			final float value = event.getAxisValue(mAxes.keyAt(i));
			mAxisValues[i] = value;
		}
		update();
		return true;
	}

	protected abstract void update();

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
		case KeyEvent.KEYCODE_BACK:
			return true;
		default:
			return KeyEvent.isGamepadButton(keyCode);
		}
	}
}
