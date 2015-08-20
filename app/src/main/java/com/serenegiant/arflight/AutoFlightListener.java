package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/20.
 */
public interface AutoFlightListener {
	/**
	 * 自動フライトの準備完了
	 */
	public void onPrepared();
	/**
	 * 自動フライト開始
	 */
	public void onStart();

	/**
	 * 自動フライトのコマンドコールバック
	 * @param cmd
	 * @param value
	 * @param t
	 * @return trueを返すと終了する
	 */
	public boolean onStep(final int cmd, final int value, final long t);

	/**
	 * 自動フライト停止
	 */
	public void onStop();
}
