package com.serenegiant.gamepad;

import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;

public class KeyGamePad extends IGamePad {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "KeyGamePad";

	private static final class KeyCount {
		private final int keycode;
		private boolean isDown;
		private long downTime;

		public KeyCount(final int _keycode) {
			keycode = _keycode;
			isDown = false;
			downTime = 0;
			mModified = true;
		}

		public void up() {
			if (isDown) {
				mModified = true;
			}
			isDown = false;
			downTime = 0;
		}

		public void down(final long down_time) {
			if (!isDown) {
				isDown = true;
				downTime = down_time;
				mModified = true;
			}
		}
		public boolean down() {
			return isDown;
		}

		public long count() {
			return isDown ? System.currentTimeMillis() - downTime : 0;
		}
	}

	public static boolean processKeyEvent(final KeyEvent event) {
//		if (DEBUG) Log.v(TAG, "processKeyEvent:" + event);
		boolean handled = false;
		final int keyCode = event.getKeyCode();
		final int count = event.getRepeatCount();
		switch (event.getAction()) {
		case KeyEvent.ACTION_DOWN:
			synchronized (mKeySync) {
				handled = down(keyCode);
//				dumpKeyState();
			}
//			if (DEBUG) Log.v(TAG, "DOWN:ev=" + event);
			break;
		case KeyEvent.ACTION_UP:
			synchronized (mKeySync) {
				handled = up(keyCode);
			}
//			if (DEBUG) Log.v(TAG, "UP:ev=" + event);
			break;
		case KeyEvent.ACTION_MULTIPLE:
//			if (DEBUG) Log.v(TAG, "MULTIPLE:ev=" + event);
			break;
		}
		return handled;
	}

	public static long[] downTimes() {
		updateState(mIsDowns, mDownTimes, false);
		return mDownTimes;
	}

	public static boolean[] isDowns() {
		updateState(mIsDowns, mDownTimes, false);
		return mIsDowns;
	}

	/**
	 * キーの押し下げ状態と押し下げしている時間[ミリ秒]
	 * @param downs KEY_NUMS個以上確保しておくこと
	 * @param down_Times KEY_NUMS個以上確保しておくこと
	 */
	public static final void updateState(final boolean[] downs, final long[] down_Times, final boolean force) {
		synchronized (mKeySync) {
			if (mModified || force) {
				int ix = 0;
				for (final KeyCount keycount: mKeyCounts) {
					if (keycount != null) {
						downs[ix] = keycount.isDown;
						down_Times[ix] = keycount.count();
					} else {
						downs[ix] = false;
						down_Times[ix] = 0;
					}
					ix++;
				}
				mModified = false;
			}
		}
	}

	private static final Object mKeySync = new Object();
	private static final KeyCount[] mKeyCounts = new KeyCount[KEY_NUMS];
	/** ハードウエアキーコード対押し下げ時間 */
	private static final SparseArray<Long> mHardwareKeys = new SparseArray<Long>();
	private static final long[] mDownTimes = new long[KEY_NUMS];
	private static final boolean[] mIsDowns = new boolean[KEY_NUMS];
	private static boolean mModified = true;

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
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_L1, KEY_LEFT_1);	// 左上手前
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_7, KEY_LEFT_2);		// 左上奥
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_L2, KEY_LEFT_2);	// 左上手前
		// 右上
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_6, KEY_RIGHT_1);	// 右上手前
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_R1, KEY_RIGHT_1);	// 右上手前
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_8, KEY_RIGHT_2);	// 右上奥
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_R2, KEY_RIGHT_2);	// 右上手前
		// スティック中央
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_9, KEY_LEFT_CENTER);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_10, KEY_RIGHT_CENTER);
		// 中央
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_11, KEY_CENTER_LEFT);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_12, KEY_CENTER_RIGHT);
		// A-D
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_13, KEY_RIGHT_A);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_14, KEY_RIGHT_B);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_15, KEY_RIGHT_C);
		KEY_MAP.put(KeyEvent.KEYCODE_BUTTON_16, KEY_RIGHT_D);
	}

	private static final boolean down(final int keycode) {
		// 指定されたハードウエアキーの押し下げ時間を追加する
		long down_time = System.currentTimeMillis();
		mHardwareKeys.put(keycode, down_time);
		final int app_key = KEY_MAP.get(keycode, KEY_UNKNOWN);
		if (app_key != KEY_UNKNOWN) {
			// 同じapp_keyに対応するハードウエアキーを探す
			final int n = KEY_MAP.size();
			for (int i = 0; i < n; i++) {
				final int _keycode = KEY_MAP.keyAt(i);
				final int _app_key = KEY_MAP.valueAt(i);
				if ((app_key == _app_key) && (mHardwareKeys.get(_keycode) != null)) {
					// 一番小さい値=最初に押された時刻[ミリ秒]
					down_time = Math.min(down_time, mHardwareKeys.get(_keycode));
				}
			}
			KeyCount keycount = mKeyCounts[app_key];
			if (keycount == null) {
				mKeyCounts[app_key] = keycount = new KeyCount(keycode);
			}
			keycount.down(down_time);
		}
		return app_key != KEY_UNKNOWN;
	}

	private static final boolean up(final int keycode) {
		// 指定されたハードウエアキーの押し下げ時間を削除する
		mHardwareKeys.remove(keycode);
		final int app_key = KEY_MAP.get(keycode, IGamePad.KEY_UNKNOWN);
		if (app_key != IGamePad.KEY_UNKNOWN) {
			// 同じapp_keyに対応するハードウエアキーを探す
			final int n = KEY_MAP.size();
			for (int i = 0; i < n; i++) {
				final int _keycode = KEY_MAP.keyAt(i);
				final int _app_key = KEY_MAP.valueAt(i);
				if ((app_key == _app_key) && (mHardwareKeys.get(_keycode) != null)) {
					// 同じapp_keyに対応するハードウエアキーがまだ押されている時
					return true;
				}
			}
			KeyCount keycount = mKeyCounts[app_key];
			if (keycount == null) {
				mKeyCounts[app_key] = keycount = new KeyCount(keycode);
			}
			keycount.up();
		}
		return app_key != KEY_UNKNOWN;
	}

	private static final void dumpKeyState() {
		final StringBuilder sb = new StringBuilder();
		for (final KeyCount keycount: mKeyCounts) {
			if (keycount != null) {
				sb.append("keycode=").append(keycount.keycode).append(",").append("count=").append(keycount.count()).append("\n");
			}
		}
		Log.i(TAG, "dumpKeyState:" + sb.toString());
	}
}
