package com.serenegiant.gamepad;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

/**
 * Created by saki on 15/10/13.
 */
public class Joystick extends IGamePad {
	private static final boolean DEBUG = true; // FIXME 実同時はfalseにすること
	private static final String TAG = Joystick.class.getSimpleName();

	private final Object mSync = new Object();
	private final InputManager mInputManager;
	private final SparseArray<JoystickParser> mInputDeviceStates = new SparseArray<JoystickParser>();;
	private final Handler mHandler;
	private boolean registered;

	public Joystick(final Context context) {
		mInputManager = (InputManager)context.getSystemService(Context.INPUT_SERVICE);
		HandlerThread thread = new HandlerThread(TAG);
		thread.start();
		mHandler = new Handler(thread.getLooper());
	}

	public void register() {
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
		mInputManager.unregisterInputDeviceListener(mInputDeviceListener);
		registered = false;
	}

	public void release() {
		unregister();
	}

	public void dispatchKeyEvent(final KeyEvent event) {
		// Update device state for visualization and logging.
		final JoystickParser joystick = getJoystick(event.getDeviceId());
		if (joystick != null) {
			synchronized (mSync) {
				switch (event.getAction()) {
				case KeyEvent.ACTION_DOWN:
					if (joystick.onKeyDown(event)) {
						// FIXME ここで状態変化の処理を行う
					}
					break;
				case KeyEvent.ACTION_UP:
					if (joystick.onKeyUp(event)) {
						// FIXME ここで状態変化の処理を行う
					}
					break;
				}
			}
		}
	}

	public void dispatchGenericMotionEvent(final MotionEvent event) {
		// Check that the event came from a joystick since a generic motion event
		// could be almost anything.
		if (event.isFromSource(InputDevice.SOURCE_CLASS_JOYSTICK)
			&& event.getAction() == MotionEvent.ACTION_MOVE) {
			// Update device state for visualization and logging.
			final JoystickParser joystick = getJoystick(event.getDeviceId());
			synchronized (mSync) {
				if (joystick != null && joystick.onJoystickMotion(event)) {
					// FIXME ここで状態変化の処理を行う
				}
			}
		}
	}

	@Override
	public void updateState(final boolean[] downs, final long[] down_times, final int[] analog_sticks, final boolean force) {

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
			}
		}

		@Override
		public void onInputDeviceChanged(final int deviceId) {
			JoystickParser state = mInputDeviceStates.get(deviceId);
			if (state != null) {
				mInputDeviceStates.remove(deviceId);
				state = getJoystick(deviceId);
				if (DEBUG) Log.i(TAG, "Device changed: " + state.getDevice());
			}
		}
	};

}
