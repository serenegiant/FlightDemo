package com.serenegiant.gamepad;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class Joystick extends IGamePad {
	private static final boolean DEBUG = true; // FIXME 実同時はfalseにすること
	private static final String TAG = Joystick.class.getSimpleName();

	private final Object mSync = new Object();
	private final InputManager mInputManager;
	private final SparseArray<JoystickParser> mInputDeviceStates = new SparseArray<JoystickParser>();;
	private final Handler mHandler;
	private boolean registered;
	private JoystickParser mParser;

	private static Joystick sJoystick;
	public static Joystick getInstance(final Context context) {
		if (sJoystick == null) {
			sJoystick = new Joystick(context.getApplicationContext());
		}
		return sJoystick;
	}

	private Joystick(final Context context) {
		mInputManager = (InputManager)context.getSystemService(Context.INPUT_SERVICE);
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mHandler = new Handler(thread.getLooper());
	}

	public void register() {
		if (DEBUG) Log.v(TAG, "register:");
		if (!registered) {
			registered = true;
			// Register an input device listener to watch when input devices are
			// added, removed or reconfigured.
			mInputManager.registerInputDeviceListener(mInputDeviceListener, mHandler);
			// Query all input devices.
			// We do this so that we can see them in the log as they are enumerated.
			final int[] ids = mInputManager.getInputDeviceIds();
			for (int i = ids.length - 1; i >= 0; i--) {
				getJoystick(ids[i]);
			}
		}
	}

	public void unregister() {
		if (DEBUG) Log.v(TAG, "unregister:");
		mInputManager.unregisterInputDeviceListener(mInputDeviceListener);
		registered = false;
		mParser = null;
		mInputDeviceStates.clear();
	}

	public void release() {
		if (DEBUG) Log.v(TAG, "release:");
		unregister();
		sJoystick = null;
	}

	public boolean dispatchKeyEvent(final KeyEvent event) {
//		if (DEBUG) Log.v(TAG, "dispatchKeyEvent:" + event);
		boolean result = false;
		final JoystickParser joystick = getJoystick(event.getDeviceId());
		if ((joystick != null) && joystick.isJoystick()) {
			synchronized (mSync) {
				switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN:
					result = joystick.onKeyDown(event);
					break;
				case KeyEvent.ACTION_UP:
					result = joystick.onKeyUp(event);
					break;
				}
			}
		}
		return result;
	}

	public void dispatchGenericMotionEvent(final MotionEvent event) {
//		if (DEBUG) Log.v(TAG, "dispatchGenericMotionEvent:" + event);
		// Check that the event came from a joystick since a generic motion event
		// could be almost anything.
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			final JoystickParser joystick = getJoystick(event.getDeviceId());
			if ((joystick != null) && joystick.isJoystick()) {
				synchronized (mSync) {
					joystick.onJoystickMotion(event);
				}
			}
		}
	}

	public static boolean isFromSource(final InputEvent event, int source) {
      return (event != null) && ((event.getSource() & source) == source);
  	}

	private static final int EPS = 2;
	@Override
	public void updateState(final boolean[] downs, final long[] down_times, final int[] analog_sticks, final boolean force) {
		if (mInputDeviceStates.size() > 0) {
			if (mParser == null) {
				final int n = mInputDeviceStates.size();
				for (int i = 0; i < n; i++) {
					final JoystickParser joystick = mInputDeviceStates.valueAt(i);
					if ((joystick != null) && joystick.isJoystick()) {
						// 最初に見つかったジョイスティックを使う
						mParser = joystick;
					}
				}
			}
			if (mParser != null) {
//				System.arraycopy(mParser.analogSticks, 0, analog_sticks, 0, 4);
				final int[] analogs = mParser.analogSticks;
				for (int i = 0; i < 4; i++) {
					analog_sticks[i] = Math.abs(analogs[i]) > EPS ? analogs[i] : 0;
				}
				final int[] counts = mParser.keyCount;
				final long current = System.currentTimeMillis();
				for (int i = 0; i < GamePadConst.KEY_NUMS; i++) {
					downs[i] = counts[i] != 0;
					down_times[i] = current - counts[i];
				}
			}
		}
	}

	private JoystickParser getJoystick(final int deviceId) {
		JoystickParser joystick = mInputDeviceStates.get(deviceId);
		if (joystick == null) {
			final InputDevice device = mInputManager.getInputDevice(deviceId);
			if (device == null) {
				return null;
			}
			joystick = JoystickParser.getJoystick(device);
			mInputDeviceStates.put(deviceId, joystick);
			if (DEBUG) Log.v(TAG, "Device enumerated: " + joystick.getDevice());
		}
		return joystick;
	}

	private final InputManager.InputDeviceListener mInputDeviceListener
		= new InputManager.InputDeviceListener() {

		@Override
		public void onInputDeviceAdded(final int deviceId) {
			final JoystickParser joystick = getJoystick(deviceId);
			if (DEBUG) Log.i(TAG, "Device added: " + joystick.getDevice());
		}

		@Override
		public void onInputDeviceRemoved(final int deviceId) {
			final JoystickParser state = mInputDeviceStates.get(deviceId);
			if (state != null) {
				if (DEBUG) Log.i(TAG, "Device removed: " + state.getDevice());
				mInputDeviceStates.remove(deviceId);
				mParser = null;
			}
		}

		@Override
		public void onInputDeviceChanged(final int deviceId) {
			JoystickParser state = mInputDeviceStates.get(deviceId);
			if (state != null) {
				mInputDeviceStates.remove(deviceId);
				state = getJoystick(deviceId);
				if (DEBUG) Log.i(TAG, "Device changed: " + state.getDevice());
				mParser = null;
			}
		}
	};

}
