package com.serenegiant.gamepad;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.KeyEvent;

import static com.serenegiant.gamepad.GamePadConst.*;

public class KeyGamePad extends IGamePad {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
//	private static final String TAG = "KeyGamePad";

	private static final Object sSync = new Object();
	private static KeyGamePad sKeyGamePad;

	/**
	 * シングルトンアクセスするためのインスタンス取得メソッド
	 * @return
	 */
	public static KeyGamePad getInstance() {
		synchronized (sSync) {
			if (sKeyGamePad == null) {
				sKeyGamePad = new KeyGamePad();
			}
			return sKeyGamePad;
		}
	}

	protected final Object mSync = new Object();
	protected boolean mModified = true;

	private KeyGamePad() {
		// 直接の生成を禁止するためコンストラクタはprivateにする
	}

	/**
	 * ActivityのdispatchKeyEventで取得したKeyEventからGamePad用キーを取得・処理する
	 * @param event
	 * @return
	 */
	public boolean processKeyEvent(final KeyEvent event) {
		boolean handled = false;
		final int keyCode = event.getKeyCode();
		final int count = event.getRepeatCount();
		switch (event.getAction()) {
		case KeyEvent.ACTION_DOWN:
			synchronized (mSync) {
				handled = down(keyCode);
			}
			break;
		case KeyEvent.ACTION_UP:
			synchronized (mSync) {
				handled = up(keyCode);
			}
			break;
		case KeyEvent.ACTION_MULTIPLE:
			break;
		}
		return handled;
	}

	/**
	 * キーの押し下げ状態と押し下げしている時間[ミリ秒]を取得
	 * @param downs KEY_NUMS個以上確保しておくこと
	 * @param down_times KEY_NUMS個以上確保しておくこと
	 */
	@Override
	public void updateState(final boolean[] downs, final long[] down_times, final boolean force) {
		synchronized (mSync) {
			if (mModified || force) {
				final long current = System.currentTimeMillis();
				for (int i = 0; i < KEY_NUMS; i++) {
					downs[i] = false;
					down_times[i] = current;
				}
				final int n = mHardwareKeys.size();
				for (int i = 0; i < n; i++) {
					final int key = mHardwareKeys.keyAt(i);
					final int app_key = KEY_MAP.get(key, KEY_UNKNOWN);
					if ((app_key != KEY_UNKNOWN) && (mHardwareKeys.valueAt(i) != null)) {
						downs[app_key] = true;
						down_times[app_key] = Math.min(down_times[app_key], mHardwareKeys.valueAt(i));
					}
				}
				mModified = false;
			}
		}
	}

	/** ハードウエアキーコード対押し下げ時間 */
	protected final SparseArray<Long> mHardwareKeys = new SparseArray<Long>();

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

	private final boolean down(final int keycode) {
		// 指定されたハードウエアキーの押し下げ時間を追加する
		mHardwareKeys.put(keycode, System.currentTimeMillis());
		final int app_key = KEY_MAP.get(keycode, KEY_UNKNOWN);
		mModified |= (app_key != KEY_UNKNOWN);
		return app_key != KEY_UNKNOWN;
	}

	private final boolean up(final int keycode) {
		// 指定されたハードウエアキーの押し下げ時間を削除する
		mHardwareKeys.put(keycode, 0L);
		mHardwareKeys.remove(keycode);
		final int app_key = KEY_MAP.get(keycode, KEY_UNKNOWN);
		mModified |= (app_key != KEY_UNKNOWN);
		return app_key != KEY_UNKNOWN;
	}
}
