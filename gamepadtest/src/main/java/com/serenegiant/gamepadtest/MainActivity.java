package com.serenegiant.gamepadtest;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.HIDGamepad;
import com.serenegiant.usb.IGamePad;
import com.serenegiant.usb.USBMonitor;

import java.util.List;

public class MainActivity extends AppCompatActivity {
	private static final boolean DEBUG = true;
	private static final String TAG = "MainActivity";

	private static final class KeyCount {
		private int keycode;
		private boolean isDown;
		private long downTime;

		public KeyCount(final int _keycode) {
			keycode = _keycode;
			isDown = false;
			downTime = 0;
		}

		public void up() {
			isDown = false;
			downTime = 0;
		}

		public void down(final long down_time) {
			if (!isDown) {
				isDown = true;
				downTime = down_time;
			}
		}
		public boolean down() {
			return isDown;
		}

		public long count() {
			return isDown ? System.currentTimeMillis() - downTime : 0;
		}
	}

	private final Object mSync = new Object();
	private final SparseArray<TextView> mTextViews = new SparseArray<TextView>();

	private final Object mUsbSync = new Object();
	private USBMonitor mUSBMonitor;
	private HIDGamepad mGamepad;
	private TextView mGamepadTv;

	private final KeyCount[] mKeyCounts = new KeyCount[16];

	private static final int KEY_UNKNOWN		= -1;
	private static final int KEY_LEFT_CENTER	= 0;
	private static final int KEY_LEFT_UP		= 1;
	private static final int KEY_LEFT_RIGHT		= 2;
	private static final int KEY_LEFT_DOWN		= 3;
	private static final int KEY_LEFT_LEFT		= 4;
	private static final int KEY_RIGHT_CENTER	= 5;
	private static final int KEY_RIGHT_UP		= 6;
	private static final int KEY_RIGHT_RIGHT	= 7;
	private static final int KEY_RIGHT_DOWN		= 8;
	private static final int KEY_RIGHT_LEFT		= 9;
	private static final int KEY_LEFT_1			= 10;	// 左上前
	private static final int KEY_LEFT_2			= 11;	// 左上後
	private static final int KEY_LEFT_3			= 12;	// 中央左
	private static final int KEY_RIGHT_1		= 13;	// 右上前
	private static final int KEY_RIGHT_2		= 14;	// 右上後
	private static final int KEY_RIGHT_3		= 15;	// 中央右

	/** ゲームパッドのハードウエアキーコードからアプリ内キーコードに変換するためのテーブル */
	private static final SparseIntArray KEY_MAP = new SparseIntArray();
	static {
		// 左側アナログスティック/十字キー
		KEY_MAP.put(KeyEvent.KEYCODE_DPAD_UP, KEY_LEFT_UP);
		KEY_MAP.put(KeyEvent.KEYCODE_DPAD_RIGHT, KEY_LEFT_RIGHT);
		KEY_MAP.put(KeyEvent.KEYCODE_DPAD_DOWN, KEY_LEFT_DOWN);
		KEY_MAP.put(KeyEvent.KEYCODE_DPAD_LEFT, KEY_LEFT_LEFT);
		// 右側アナログスティック
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_1, KEY_RIGHT_UP);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_2, KEY_RIGHT_RIGHT);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_3, KEY_RIGHT_DOWN);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_4, KEY_RIGHT_LEFT);
		// 左上
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_5, KEY_LEFT_1);		// 左上手前
//		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_L1, KEY_LEFT_1);	// 左上手前
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_7, KEY_LEFT_2);		// 左上奥
//		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_L2, KEY_LEFT_2);	// 左上手前
		// 右上
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_6, KEY_RIGHT_1);	// 右上手前
//		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_R1, KEY_RIGHT_1);	// 右上手前
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_8, KEY_RIGHT_2);	// 右上奥
//		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_R2, KEY_RIGHT_2);	// 右上手前
		// スティック中央
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_9, KEY_LEFT_CENTER);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_10, KEY_RIGHT_CENTER);
		// 中央
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_11, KEY_LEFT_3);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_12, KEY_RIGHT_3);
	}

	private TextView mKeyTextView;
	private TextView mSensorAccelTextView;
	private TextView mSensorMagnetTextView;
	private TextView mSensorGyroTextView;
	private TextView mSensorGravityTextView;
	private TextView mSensorOrientationTextView;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final View frame = findViewById(R.id.layout_frame);
		frame.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.i(TAG, "onKey:keyCode=" + keyCode);
				return false;
			}
		});
		frame.requestFocus();

		mKeyTextView = (TextView)findViewById(R.id.key_textview);
		// 左側
		TextView tv = (TextView) findViewById(R.id.left_center_textview);
//		mTextViews.append(KeyEvent.KEYCODE_DPAD_CENTER, tv);
		tv = (TextView) findViewById(R.id.left_textview);
		mTextViews.append(KeyEvent.KEYCODE_DPAD_LEFT, tv);
		tv = (TextView) findViewById(R.id.right_textview);
		mTextViews.append(KeyEvent.KEYCODE_DPAD_RIGHT, tv);
		tv = (TextView) findViewById(R.id.up_textview);
		mTextViews.append(KeyEvent.KEYCODE_DPAD_UP, tv);
		tv = (TextView) findViewById(R.id.down_textview);
		mTextViews.append(KeyEvent.KEYCODE_DPAD_DOWN, tv);
		// 右側
		tv = (TextView) findViewById(R.id.btn1_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_1, tv);
		tv = (TextView) findViewById(R.id.btn2_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_2, tv);
		tv = (TextView) findViewById(R.id.btn3_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_3, tv);
		tv = (TextView) findViewById(R.id.btn4_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_4, tv);
		// 左上
		tv = (TextView) findViewById(R.id.btn5_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_5, tv);
		tv = (TextView) findViewById(R.id.btn7_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_7, tv);
		// 右上
		tv = (TextView) findViewById(R.id.btn6_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_6, tv);
		tv = (TextView) findViewById(R.id.btn8_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_8, tv);
		// スティック中央
		tv = (TextView) findViewById(R.id.left_center_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_9, tv);
		tv = (TextView) findViewById(R.id.right_center_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_10, tv);
		// 中央
		tv = (TextView) findViewById(R.id.btn11_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_11, tv);
		tv = (TextView) findViewById(R.id.btn12_textview);
		mTextViews.append(KeyEvent.KEYCODE_BUTTON_12, tv);

		final int n = KEY_MAP.size();
		for (int i = 0; i < n; i++) {
			final int keycode = KEY_MAP.keyAt(i);
			up(keycode);
		}

		mSensorAccelTextView = (TextView)findViewById(R.id.sensor_accel_textview);
		mSensorMagnetTextView = (TextView)findViewById(R.id.sensor_magnet_textview);
		mSensorGyroTextView = (TextView)findViewById(R.id.sensor_gyro_textview);
		mSensorGravityTextView = (TextView)findViewById(R.id.sensor_gravity_textview);
		mSensorOrientationTextView = (TextView)findViewById(R.id.sensor_orientation_textview);

		final CheckBox checkbox = (CheckBox)findViewById(R.id.use_driver_checkbox);
		checkbox.setOnCheckedChangeListener(mOnCheckedChangeListener);
	}

	@Override
	protected void onResume() {
		super.onResume();
		startSensor();
		mUIHandler.post(mKeyUpdateTask);
		synchronized (mUsbSync) {
			if (mUSBMonitor != null) {
				mUSBMonitor.register();
			}
		}
	}

	@Override
	protected void onPause() {
		synchronized (mUsbSync) {
			releaseGamepad();
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
			}
		}
		stopSensor();
		mUIHandler.removeCallbacks(mKeyUpdateTask);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		releaseUsbDriver();
		super.onDestroy();
	}

	private void down(final int keycode) {
		final int app_key = KEY_MAP.get(keycode, KEY_UNKNOWN);
		if (app_key != KEY_UNKNOWN) {
			KeyCount keycount = mKeyCounts[app_key];
			if (keycount == null) {
				mKeyCounts[app_key] = keycount = new KeyCount(keycode);
			}
			keycount.down(System.currentTimeMillis());
		}
	}

	private void up(final int keycode) {
		final int app_key = KEY_MAP.get(keycode, KEY_UNKNOWN);
		if (app_key != KEY_UNKNOWN) {
			KeyCount keycount = mKeyCounts[app_key];
			if (keycount == null) {
				mKeyCounts[app_key] = keycount = new KeyCount(keycode);
			}
			keycount.up();
		}
	}

	private int key_cnt = 0;
	@Override
	public boolean dispatchKeyEvent(final KeyEvent ev) {
		final int keycode = ev.getKeyCode();
		final int count = ev.getRepeatCount();
		switch (ev.getAction()) {
			case KeyEvent.ACTION_DOWN:
				synchronized (mSync) {
					down(keycode);
				}
				// キーコード表示
				if (keycode != KeyEvent.KEYCODE_DPAD_CENTER)
				try {
					mKeyTextView.setText(getKeyCodeName(keycode));
				} catch (final Exception e) {
				}
//				if (DEBUG) Log.v(TAG, "DOWN:ev=" + ev);
				break;
			case KeyEvent.ACTION_UP:
				synchronized (mSync) {
					up(keycode);
				}
				// キーコード表示
				if (keycode != KeyEvent.KEYCODE_DPAD_CENTER)
				try {
					mKeyTextView.setText(getKeyCodeName(keycode));
				} catch (final Exception e) {
				}
//				if (DEBUG) Log.v(TAG, "UP:ev=" + ev);
				break;
			case KeyEvent.ACTION_MULTIPLE:
//				if (DEBUG) Log.v(TAG, "MULTIPLE:ev=" + ev);
				break;
		}
		return super.dispatchKeyEvent(ev);
	}

	private final Runnable mKeyUpdateTask = new Runnable() {
		@Override
		public void run() {
			mUIHandler.removeCallbacks(this);
			synchronized (mSync) {
				final int n = mKeyCounts.length;
				for (int i = 0; i < n; i++) {
					final KeyCount keycount = mKeyCounts[i];
					if (keycount != null) {
						final TextView tv = mTextViews.get(keycount.keycode);
						if (tv != null) {
							tv.setText(String.format("%d", keycount.count()));
						}
					}
				}
			}
			mUIHandler.postDelayed(this, 50);
		}
	};

	private final String getSourceName(final int src) {
		String ret = null;
		switch (src) {
		case InputDevice.SOURCE_ANY:
			ret = "SOURCE_ANY";
			break;
		case InputDevice.SOURCE_CLASS_BUTTON:
			ret = "SOURCE_CLASS_BUTTON";
			break;
		case InputDevice.SOURCE_CLASS_JOYSTICK:
			ret = "SOURCE_CLASS_JOYSTICK";
			break;
		case InputDevice.SOURCE_CLASS_MASK:
			ret = "SOURCE_CLASS_MASK";
			break;
		case InputDevice.SOURCE_CLASS_POINTER:
			ret = "SOURCE_CLASS_POINTER";
			break;
		case InputDevice.SOURCE_CLASS_POSITION:
			ret = "SOURCE_CLASS_POSITION";
			break;
		case InputDevice.SOURCE_CLASS_TRACKBALL:
			ret = "SOURCE_CLASS_TRACKBALL";
			break;
		case InputDevice.SOURCE_DPAD:
			ret = "SOURCE_DPAD";
			break;
		case InputDevice.SOURCE_GAMEPAD:
			ret = "SOURCE_GAMEPAD";
			break;
		case InputDevice.SOURCE_JOYSTICK:
			ret = "SOURCE_JOYSTICK";
			break;
		case InputDevice.SOURCE_KEYBOARD:
			ret = "SOURCE_KEYBOARD";
			break;
		case InputDevice.SOURCE_MOUSE:
			ret = "SOURCE_MOUSE";
			break;
		case InputDevice.SOURCE_TOUCHPAD:
			ret = "SOURCE_TOUCHPAD";
			break;
		case InputDevice.SOURCE_TOUCHSCREEN:
			ret = "SOURCE_TOUCHSCREEN";
			break;
		case InputDevice.SOURCE_TRACKBALL:
			ret = "SOURCE_TRACKBALL";
			break;
		case InputDevice.SOURCE_UNKNOWN:
			ret = "SOURCE_UNKNOWN";
			break;
		default:
			ret = "UNKOWN_VAL";
		}
		return ret;
	}

	private static final String getKeyCodeName(final int keycode) {
		try {
			return KEYCODE_NAME[keycode];
		} catch (final Exception e) {
			return String.format("UNKNOWN_KEY_%d", keycode);
		}
	}

	private static final String KEYCODE_NAME[] = {
	   "KEYCODE_UNKNOWN",
	   "KEYCODE_SOFT_LEFT",
	   "KEYCODE_SOFT_RIGHT",
	   "KEYCODE_HOME",
	   "KEYCODE_BACK",
	   "KEYCODE_CALL",
	   "KEYCODE_ENDCALL",
	   "KEYCODE_0",
	   "KEYCODE_1",
	   "KEYCODE_2",
	   "KEYCODE_3",
	   "KEYCODE_4",
	   "KEYCODE_5",
	   "KEYCODE_6",
	   "KEYCODE_7",
	   "KEYCODE_8",
	   "KEYCODE_9",
	   "KEYCODE_STAR",
	   "KEYCODE_POUND",
	   "KEYCODE_DPAD_UP",
	   "KEYCODE_DPAD_DOWN",
	   "KEYCODE_DPAD_LEFT",
	   "KEYCODE_DPAD_RIGHT",
	   "KEYCODE_DPAD_CENTER",
	   "KEYCODE_VOLUME_UP",
	   "KEYCODE_VOLUME_DOWN",
	   "KEYCODE_POWER",
	   "KEYCODE_CAMERA",
	   "KEYCODE_CLEAR",
	   "KEYCODE_A",
	   "KEYCODE_B",
	   "KEYCODE_C",
	   "KEYCODE_D",
	   "KEYCODE_E",
	   "KEYCODE_F",
	   "KEYCODE_G",
	   "KEYCODE_H",
	   "KEYCODE_I",
	   "KEYCODE_J",
	   "KEYCODE_K",
	   "KEYCODE_L",
	   "KEYCODE_M",
	   "KEYCODE_N",
	   "KEYCODE_O",
	   "KEYCODE_P",
	   "KEYCODE_Q",
	   "KEYCODE_R",
	   "KEYCODE_S",
	   "KEYCODE_T",
	   "KEYCODE_U",
	   "KEYCODE_V",
	   "KEYCODE_W",
	   "KEYCODE_X",
	   "KEYCODE_Y",
	   "KEYCODE_Z",
	   "KEYCODE_COMMA",
	   "KEYCODE_PERIOD",
	   "KEYCODE_ALT_LEFT",
	   "KEYCODE_ALT_RIGHT",
	   "KEYCODE_SHIFT_LEFT",
	   "KEYCODE_SHIFT_RIGHT",
	   "KEYCODE_TAB",
	   "KEYCODE_SPACE",
	   "KEYCODE_SYM",
	   "KEYCODE_EXPLORER",
	   "KEYCODE_ENVELOPE",
	   "KEYCODE_ENTER",
	   "KEYCODE_DEL",
	   "KEYCODE_GRAVE",
	   "KEYCODE_MINUS",
	   "KEYCODE_EQUALS",
	   "KEYCODE_LEFT_BRACKET",
	   "KEYCODE_RIGHT_BRACKET",
	   "KEYCODE_BACKSLASH",
	   "KEYCODE_SEMICOLON",
	   "KEYCODE_APOSTROPHE",
	   "KEYCODE_SLASH",
	   "KEYCODE_AT",
	   "KEYCODE_NUM",
	   "KEYCODE_HEADSETHOOK",
	   "KEYCODE_FOCUS",
	   "KEYCODE_PLUS",
	   "KEYCODE_MENU",
	   "KEYCODE_NOTIFICATION",
	   "KEYCODE_SEARCH",
	   "KEYCODE_MEDIA_PLAY_PAUSE",
	   "KEYCODE_MEDIA_STOP",
	   "KEYCODE_MEDIA_NEXT",
	   "KEYCODE_MEDIA_PREVIOUS",
	   "KEYCODE_MEDIA_REWIND",
	   "KEYCODE_MEDIA_FAST_FORWARD",
	   "KEYCODE_MUTE",
	   "KEYCODE_PAGE_UP",
	   "KEYCODE_PAGE_DOWN",
	   "KEYCODE_PICTSYMBOLS",
	   "KEYCODE_SWITCH_CHARSET",
	   "KEYCODE_BUTTON_A",
	   "KEYCODE_BUTTON_B",
	   "KEYCODE_BUTTON_C",
	   "KEYCODE_BUTTON_X",
	   "KEYCODE_BUTTON_Y",
	   "KEYCODE_BUTTON_Z",
	   "KEYCODE_BUTTON_L1",
	   "KEYCODE_BUTTON_R1",
	   "KEYCODE_BUTTON_L2",
	   "KEYCODE_BUTTON_R2",
	   "KEYCODE_BUTTON_THUMBL",
	   "KEYCODE_BUTTON_THUMBR",
	   "KEYCODE_BUTTON_START",
	   "KEYCODE_BUTTON_SELECT",
	   "KEYCODE_BUTTON_MODE",
	   "KEYCODE_ESCAPE",
	   "KEYCODE_FORWARD_DEL",
	   "KEYCODE_CTRL_LEFT",
	   "KEYCODE_CTRL_RIGHT",
	   "KEYCODE_CAPS_LOCK",
	   "KEYCODE_SCROLL_LOCK",
	   "KEYCODE_META_LEFT",
	   "KEYCODE_META_RIGHT",
	   "KEYCODE_FUNCTION",
	   "KEYCODE_SYSRQ",
	   "KEYCODE_BREAK",
	   "KEYCODE_MOVE_HOME",
	   "KEYCODE_MOVE_END",
	   "KEYCODE_INSERT",
	   "KEYCODE_FORWARD",
	   "KEYCODE_MEDIA_PLAY",
	   "KEYCODE_MEDIA_PAUSE",
	   "KEYCODE_MEDIA_CLOSE",
	   "KEYCODE_MEDIA_EJECT",
	   "KEYCODE_MEDIA_RECORD",
	   "KEYCODE_F1",
	   "KEYCODE_F2",
	   "KEYCODE_F3",
	   "KEYCODE_F4",
	   "KEYCODE_F5",
	   "KEYCODE_F6",
	   "KEYCODE_F7",
	   "KEYCODE_F8",
	   "KEYCODE_F9",
	   "KEYCODE_F10",
	   "KEYCODE_F11",
	   "KEYCODE_F12",
	   "KEYCODE_NUM_LOCK",
	   "KEYCODE_NUMPAD_0",
	   "KEYCODE_NUMPAD_1",
	   "KEYCODE_NUMPAD_2",
	   "KEYCODE_NUMPAD_3",
	   "KEYCODE_NUMPAD_4",
	   "KEYCODE_NUMPAD_5",
	   "KEYCODE_NUMPAD_6",
	   "KEYCODE_NUMPAD_7",
	   "KEYCODE_NUMPAD_8",
	   "KEYCODE_NUMPAD_9",
	   "KEYCODE_NUMPAD_DIVIDE",
	   "KEYCODE_NUMPAD_MULTIPLY",
	   "KEYCODE_NUMPAD_SUBTRACT",
	   "KEYCODE_NUMPAD_ADD",
	   "KEYCODE_NUMPAD_DOT",
	   "KEYCODE_NUMPAD_COMMA",
	   "KEYCODE_NUMPAD_ENTER",
	   "KEYCODE_NUMPAD_EQUALS",
	   "KEYCODE_NUMPAD_LEFT_PAREN",
	   "KEYCODE_NUMPAD_RIGHT_PAREN",
	   "KEYCODE_VOLUME_MUTE",
	   "KEYCODE_INFO",
	   "KEYCODE_CHANNEL_UP",
	   "KEYCODE_CHANNEL_DOWN",
	   "KEYCODE_ZOOM_IN",
	   "KEYCODE_ZOOM_OUT",
	   "KEYCODE_TV",
	   "KEYCODE_WINDOW",
	   "KEYCODE_GUIDE",
	   "KEYCODE_DVR",
	   "KEYCODE_BOOKMARK",
	   "KEYCODE_CAPTIONS",
	   "KEYCODE_SETTINGS",
	   "KEYCODE_TV_POWER",
	   "KEYCODE_TV_INPUT",
	   "KEYCODE_STB_POWER",
	   "KEYCODE_STB_INPUT",
	   "KEYCODE_AVR_POWER",
	   "KEYCODE_AVR_INPUT",
	   "KEYCODE_PROG_RED",
	   "KEYCODE_PROG_GREEN",
	   "KEYCODE_PROG_YELLOW",
	   "KEYCODE_PROG_BLUE",
	   "KEYCODE_APP_SWITCH",
	   "KEYCODE_BUTTON_1",
	   "KEYCODE_BUTTON_2",
	   "KEYCODE_BUTTON_3",
	   "KEYCODE_BUTTON_4",
	   "KEYCODE_BUTTON_5",
	   "KEYCODE_BUTTON_6",
	   "KEYCODE_BUTTON_7",
	   "KEYCODE_BUTTON_8",
	   "KEYCODE_BUTTON_9",
	   "KEYCODE_BUTTON_10",
	   "KEYCODE_BUTTON_11",
	   "KEYCODE_BUTTON_12",
	   "KEYCODE_BUTTON_13",
	   "KEYCODE_BUTTON_14",
	   "KEYCODE_BUTTON_15",
	   "KEYCODE_BUTTON_16",
	};

	private static final int[] SENSOR_TYPES = {
		Sensor.TYPE_MAGNETIC_FIELD,
		Sensor.TYPE_ACCELEROMETER,
		Sensor.TYPE_GRAVITY,
		Sensor.TYPE_GYROSCOPE,
//		Sensor.TYPE_ROTATION_VECTOR,
	};
	private SensorManager mSensorManager;
	private final Handler mUIHandler = new Handler();
	private final Object mSensorSync = new Object();
	private final float[] mMagnetValues = new float[3];
	private final float[] mAccelValues = new float[3];
	private final float[] mGravityValues = new float[3];
	private final float[] mGyroValues = new float[3];
	private final float[] mOrientationValues = new float[3];
	/**
	 * 磁気センサー・加速度センサー等を読み取り開始
	 */
	private void startSensor() {
		if (mSensorManager == null) {
			mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
			for (final int sensor_type : SENSOR_TYPES) {
				final List<Sensor> sensors = mSensorManager.getSensorList(sensor_type);
				if ((sensors != null) && (sensors.size() > 0)) {
					mSensorManager.registerListener(mSensorEventListener, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
				}
			}
			mUIHandler.postDelayed(mSensorUpdateTask, 50);
		}
	}

	/**
	 * 磁気センサー・加速度センサー等からの読み取り終了
	 */
	private void stopSensor() {
		mUIHandler.removeCallbacks(mSensorUpdateTask);
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorEventListener);
			mSensorManager = null;
		}
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		/**
		 * センサーの値が変化した時のコールバック
		 * @param event
		 */
		@Override
		public void onSensorChanged(final SensorEvent event) {
			final float[] values = event.values;
			final int type = event.sensor.getType();
			switch (type) {
			case Sensor.TYPE_MAGNETIC_FIELD:	// 磁気センサー
				synchronized (mSensorSync) {
					// ハイパスフィルターを通して取得
					// alpha=t/(t+dt), dt≒20msec@SENSOR_DELAY_GAME, tはローパスフィルタの時定数(t=80)
					highPassFilter(mMagnetValues, values, 0.8f);
//					System.arraycopy(values, 0, mMagnetValues, 0, 3);
				}
				break;
			case Sensor.TYPE_ACCELEROMETER:		// 加速度センサー
				synchronized (mSensorSync) {
					System.arraycopy(values, 0, mAccelValues, 0, 3);
				}
				break;
			case Sensor.TYPE_GRAVITY:			// 重力センサー
				synchronized (mSensorSync) {
					System.arraycopy(values, 0, mGravityValues, 0, 3);
				}
				break;
			case Sensor.TYPE_GYROSCOPE:			// ジャイロセンサー
				synchronized (mSensorSync) {
					System.arraycopy(values, 0, mGyroValues, 0, 3);
				}
				break;
			default:
//				if (DEBUG) Log.v(TAG, "onSensorChanged:" + String.format("その他%d(%f,%f,%f)", type, values[0], values[1], values[2]));
				break;
			}
		}

		/**
		 * センサーの精度が変更された時のコールバック
		 * @param sensor
		 * @param accuracy
		 */
		@Override
		public void onAccuracyChanged(final Sensor sensor, int accuracy) {
		}
	};

	private static final float TO_DEGREE = (float)(180 / Math.PI);
	private final float[] mRotateMatrix = new float[16];			// 回転行列
	private final float[] mInclinationMatrix = new float[16];    	// 傾斜行列
	private final Runnable mSensorUpdateTask = new Runnable() {
		@Override
		public void run() {
			synchronized (mSensorSync) {
				mSensorAccelTextView.setText(String.format("加速度(%f,%f,%f)", mAccelValues[0], mAccelValues[1], mAccelValues[2]));
				mSensorGravityTextView.setText(String.format("重力(%f,%f,%f)", mGravityValues[0], mGravityValues[1], mGravityValues[2]));
				mSensorGyroTextView.setText(String.format("ジャイロ(%f,%f,%f)", mGyroValues[0], mGyroValues[1], mGyroValues[2]));
				mSensorMagnetTextView.setText(String.format("磁気(%f,%f,%f)", mMagnetValues[0], mMagnetValues[1], mMagnetValues[2]));

				SensorManager.getRotationMatrix(mRotateMatrix, mInclinationMatrix, mAccelValues, mMagnetValues);
				getOrientation(mRotateMatrix, mOrientationValues);
				mOrientationValues[0] *= TO_DEGREE;
				mOrientationValues[1] *= TO_DEGREE;
				mOrientationValues[2] *= TO_DEGREE;
				mSensorOrientationTextView.setText(String.format("方位(%f,%f,%f)", mOrientationValues[0], mOrientationValues[1], mOrientationValues[2]));
			}
			mUIHandler.postDelayed(this, 50);
		}
	};

	/**
	 * ハイパスフィルターを通して値をセットする
	 * @param values 値を保持するためのfloat配列
	 * @param new_values 新しい値を渡すためのfloat配列
	 * @param alpha フィルター定数(alpha=t/(t+dt)
	 */
	private void highPassFilter(final float[] values, final float[] new_values, final float alpha) {
		values[0] = alpha * values[0] + (1 - alpha) * new_values[0];
		values[1] = alpha * values[1] + (1 - alpha) * new_values[1];
		values[2] = alpha * values[2] + (1 - alpha) * new_values[2];
	}

	private final float[] outR = new float[16];
	private final float[] outR2 = new float[16];
	private void getOrientation(final float[] rotateMatrix, final float[] result) {
		final Display disp = this.getWindowManager().getDefaultDisplay();
		final int dir = disp.getRotation();

		switch (dir) {
		case Surface.ROTATION_0:
			SensorManager.getOrientation(rotateMatrix, result);
			return;
		case Surface.ROTATION_90:
			SensorManager.remapCoordinateSystem(
				rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
			break;
		case Surface.ROTATION_180:
			SensorManager.remapCoordinateSystem(
				rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR2);
			SensorManager.remapCoordinateSystem(
				outR2, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
			break;
		case Surface.ROTATION_270:
			SensorManager.remapCoordinateSystem(
				outR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, outR);
			break;
		}
		SensorManager.getOrientation(outR, result);
	}


	private void startUsbDriver() {
//		final SharedPreferences pref = getPreferences(0);
//		final boolean use_usb_driver = pref.getBoolean("USE_GAMEPAD_DRIVER", false);
		synchronized (mUsbSync) {
			if (true/*use_usb_driver*/ && (mUSBMonitor == null)) {
				mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
				final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
				mUSBMonitor.setDeviceFilter(filter.get(0));
			}
		}
		mGamepadTv = (TextView)findViewById(R.id.debug_gamepad_testview);
		if (mGamepadTv != null) {
			mGamepadTv.setVisibility(View.INVISIBLE);
		}
	}

	private void releaseUsbDriver() {
		synchronized (mUsbSync) {
			releaseGamepad();
			if (mUSBMonitor != null) {
				mUSBMonitor.unregister();
				mUSBMonitor.destroy();
				mUSBMonitor = null;
			}
		}
	}

	private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
		= new CompoundButton.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.use_driver_checkbox:
				if (isChecked) {
					if (mUSBMonitor == null) {
						startUsbDriver();
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								synchronized (mUsbSync) {
									if (mUSBMonitor != null) {
										mUSBMonitor.register();
									}
								}
							}
						});
					}
				} else {
					releaseUsbDriver();
				}
			}
		}
	};

	private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
			synchronized (mUsbSync) {
				if (mUSBMonitor != null) {
					UsbDevice _device = device;
					if ((_device == null) && (mUSBMonitor.getDeviceCount() > 0)) {
						_device = mUSBMonitor.getDeviceList().get(0);
					}
					if (mGamepad == null) {
						mUSBMonitor.requestPermission(_device);
					}
				}
			}
		}

		@Override
		public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
			synchronized (mUsbSync) {
				if (mGamepadTv != null) {
					mGamepadTv.setVisibility(View.VISIBLE);
				}
				if (mGamepad == null) {
					mGamepad = new HIDGamepad(mHIDGamepadCallback);
					mGamepad.open(ctrlBlock);
				}
			}
		}

		@Override
		public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
			synchronized (mUsbSync) {
				if (mGamepad != null) {
					mGamepad.close();
				}
			}
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
			releaseGamepad();
		}

		@Override
		public void onCancel() {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
			releaseGamepad();
		}
	};

	private void releaseGamepad() {
		synchronized (mUsbSync) {
			if (mGamepad != null) {
				mGamepad.release();
				mGamepad = null;
			}
		}
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mGamepadTv != null) {
					mGamepadTv.setVisibility(View.INVISIBLE);
				}
			}
		});
	}
	/**
	 * ゲームパッドの状態をチェックするためコールバック
	 */
	private final HIDGamepad.HIDGamepadCallback mHIDGamepadCallback = new HIDGamepad.HIDGamepadCallback() {
		private final StringBuilder sb = new StringBuilder();

		@Override
		public boolean onRawdataChanged(final int n, final byte[] data) {
			if (mGamepadTv != null) {
				sb.setLength(0);
				final int m = n / 8 + 1;
				int ix = 0;
LOOP:			for (int j = 0; j < m; j++) {
					if (ix >= n) break LOOP;
					if (j != 0) {
						sb.append("\n");
					}
					for (int i = 0; i < 8; i++) {
						if (ix >= n) break LOOP;
						sb.append(String.format("%02x:", data[ix++]));
					}
				}
				final String text = sb.toString();
				mGamepadTv.post(new Runnable() {
					@Override
					public void run() {
						mGamepadTv.setText(text);
					}
				});
//				Log.v(TAG, text);
			}
			return false;
		}

		@Override
		public void onEvent(final HIDGamepad gamepad, final IGamePad data) {
			// データ受信時の処理
			final int[] counts = data.keyCount;
			for (int i = 0; i < 16; i++) {
				mKeyCounts[i].downTime = counts[i];
				mKeyCounts[i].isDown =  counts[i] != 0;
			}
		}
	};
}