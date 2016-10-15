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
	 * @param t
	 * @param values
	 * @return trueを返すと終了する
	 */
	public boolean onStep(final int cmd, final int[] values, final long t);

	/**
	 * 値を取得
	 * @param axis, 0:roll, 1:pitch, 2:yaw, 3:gaz, 4:compass, 5:max_tilt, 6:max_rotation_speed, 7:max_vertical_speed
	 * @return
	 */
	public float getValues(final int axis);
	/**
	 * 自動フライト停止
	 */
	public void onStop();

	/**
	 * 非同期実行中にエラー発生
	 * @param e
	 */
	public void onError(Exception e);
}
