package com.serenegiant.gamepad;

import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class IGamePad {
	protected final Object mKeySync = new Object();
	protected final int[] mDownTimes = new int[KEY_NUMS];
	protected final boolean[] mIsDowns = new boolean[KEY_NUMS];
	protected boolean mModified = true;

	public int[] downTimes() {
		updateState(mIsDowns, mDownTimes, false);
		return mDownTimes;
	}

	public boolean[] isDowns() {
		updateState(mIsDowns, mDownTimes, false);
		return mIsDowns;
	}

	/**
	 * キーの押し下げ状態と押し下げしている時間[ミリ秒]を取得
	 * @param downs KEY_NUMS個以上確保しておくこと
	 * @param down_Times KEY_NUMS個以上確保しておくこと
	 */
	public abstract void updateState(final boolean[] downs, final int[] down_Times, final boolean force);

}
