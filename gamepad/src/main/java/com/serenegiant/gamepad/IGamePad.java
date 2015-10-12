package com.serenegiant.gamepad;

import static com.serenegiant.gamepad.GamePadConst.*;

public abstract class IGamePad {
	/**
	 * キーの押し下げ状態と押し下げしている時間[ミリ秒]を取得
	 * @param downs KEY_NUMS個以上確保しておくこと
	 * @param down_times KEY_NUMS個以上確保しておくこと
	 * @param analog_sticks 4個以上確保しておくこと
	 */
	public abstract void updateState(final boolean[] downs, final long[] down_times, final int[] analog_sticks, final boolean force);

}
