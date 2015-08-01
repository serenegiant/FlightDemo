package com.serenegiant.arflight;

public interface DeviceControllerListener {
	/**
	 * 切断された時のコールバック
	 */
	public void onDisconnect();

	/**
	 * 電池残量が変化した時のコールバック
	 * @param percent
	 */
	public void onUpdateBattery(final byte percent);

	/**
	 * フラットトリムが更新された時のコールバック
	 * @param success
	 */
	public void onFlatTrimUpdate(final boolean success);

	/**
	 * 飛行状態が変化した時のコールバック
	 * @param state
	 * 0: Landed state, 1:Taking off state, 2:Hovering state, 3:Flying state
	 * 4:Landing state, 5:Emergency state, 6:Rolling state
	 */
	public void onFlyingStateChangedUpdate(final int state);

	/**
	 * 機体側からの異常通知時のコールバック
	 * @param alert_state
	 * 0: No alert, 1:User emergency alert, 2:Cut out alert, 3:Critical battery alert, 4:Low battery alert
	 */
	public void onAlertStateChangedUpdate(final int alert_state);
}
