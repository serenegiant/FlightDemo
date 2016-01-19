package com.serenegiant.gamepad;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.serenegiant.gamepad.modules.DualShock3;
import com.serenegiant.gamepad.modules.DualShock4;
import com.serenegiant.gamepad.modules.GeneralGamepad;
import com.serenegiant.gamepad.modules.JCU2912;
import com.serenegiant.gamepad.modules.JoystickBSGP1204;
import com.serenegiant.gamepad.modules.JoystickDualShock4;
import com.serenegiant.gamepad.modules.JoystickGeneral;
import com.serenegiant.gamepad.modules.JoystickJCU3312s;
import com.serenegiant.gamepad.modules.XInputF310rGamepad;
import com.serenegiant.gamepad.modules.XInputGeneral;
import com.serenegiant.utils.BuildCheck;

import java.util.List;

import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class JoystickParser {
//	private static final boolean DEBUG = false; // FIXME 実同時はfalseにすること
//	private static final String TAG = JoystickParser.class.getSimpleName();

	public static JoystickParser getJoystick(final InputDevice device, final int vid, final int pid, final UsbDevice usb_device) {

		final int vendor_id = vid != 0 ? vid : (usb_device != null ? usb_device.getVendorId() : 0);
		final int product_id = pid != 0 ? pid : (usb_device != null ? usb_device.getProductId() : 0);
		final int clazz = usb_device != null ? usb_device.getDeviceClass() : 0;
		final int sub_class = usb_device != null ? usb_device.getDeviceSubclass() : 0;

		int cs_class = 0;
		int cs_sub_class = 0;
		int cs_protocol = 0;
		if (usb_device != null) {
			final int num_interface = usb_device.getInterfaceCount();
//			if (DEBUG) Log.v(TAG, "num_interface:" + num_interface);
			for (int j = 0; j < num_interface; j++) {
				final UsbInterface intf = usb_device.getInterface(j);
				final int num_endpoint = intf.getEndpointCount();
//				if (DEBUG) Log.v(TAG, "num_endpoint:" + num_endpoint);
				if (num_endpoint > 0) {
					UsbEndpoint ep_in = null;
					for (int i = 0; i < num_endpoint; i++) {
						final UsbEndpoint ep = intf.getEndpoint(i);
//						if (DEBUG) Log.v(TAG, "type=" + ep.getType() + ", dir=" + ep.getDirection());
						if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_INT) {    // インタラプト転送
							if (ep.getDirection() == UsbConstants.USB_DIR_IN) {
								if (ep_in == null) {
									ep_in = ep;
								}
								break;
							}
						}
						if (ep_in != null) break;
					}
					if (ep_in != null) {
						// 入力インターフェースのエンドポイントが見つかった
						cs_class = intf.getInterfaceClass();
						cs_sub_class = intf.getInterfaceSubclass();
						cs_protocol = intf.getInterfaceProtocol();
						break;
					}
				}
			}
		}
//		if (DEBUG) Log.v(TAG, String.format("getJoystick:vid=%d/%d,pid=%d/%d", vid, vendor_id, pid, product_id));
		switch (vendor_id) {
		case 4607:	// ELECOM
		{
			switch (product_id) {
			case 13105:
				return new JoystickGeneral(device);
			}
			break;
		}
		case 1464: // ELECOM
		{
			switch (product_id) {
			case 4100:
				return new JoystickJCU3312s(device);
			}
			break;
		}
		case 1356:	// SONY
		{
			switch (product_id) {
			case 616:
				return new JoystickGeneral(device);
			case 1476:
				return new JoystickDualShock4(device);
			}
			break;
		}
		case 1133:	// Logicool/Logitech
		{
			switch (product_id) {
			case 49693:
				return new JoystickGeneral(device);
			}
			break;
		}
		case 7640:	// iBuffalo
		{
			switch (product_id) {
			case 15:
				return new JoystickBSGP1204(device);
			}
			break;
		}
		}
		if ((clazz == 0xff) && (sub_class == 0xff) && (product_id == 0xff)	// vendor specific
			&& (cs_class == 0xff) && (cs_sub_class == 0x5d) && (cs_protocol == 0x01)) {
			// たぶんx-input
			return new JoystickGeneral(device);
		}
		// フォールバック
		return new JoystickGeneral(device);
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
				final String symbolicName = KeyEvent.keyCodeToString(keyCode);
//				if (DEBUG) Log.i(TAG, mDevice.getName() + " - Key Down: " + symbolicName);
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
//		if (DEBUG) dumpAxisValues();
		update();
		return true;
	}

	private int cnt;
	protected void dumpAxisValues() {
		final StringBuilder sb = new StringBuilder();
		final int n = mAxes.size();
		if ((++cnt % 10) == 0) {
			for (int i = 0; i < n; i++) {
				sb.append(String.format("%12s", MotionEvent.axisToString(mAxes.keyAt(i))));
			}
//			Log.v(TAG, sb.toString());
		}
		sb.setLength(0);
		for (int i = 0; i < n; i++) {
			sb.append(String.format("%12f", mAxisValues[i]));
		}
//		Log.v(TAG, sb.toString());
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
