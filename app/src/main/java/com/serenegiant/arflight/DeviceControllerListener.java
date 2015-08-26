package com.serenegiant.arflight;

public interface DeviceControllerListener extends DeviceConnectionListener {
	/**
	 * 電池残量が変化した時のコールバック
	 * @param percent
	 */
	public void onUpdateBattery(final int percent);

	/**
	 * 飛行状態が変化した時のコールバック
	 * @param state
	 * 0: Landed state, 1:Taking off state, 2:Hovering state, 3:Flying state
	 * 4:Landing state, 5:Emergency state, 6:Rolling state
	 */
	public void onFlyingStateChangedUpdate(final int state);

	/**
	 * 機体側からの異常通知時のコールバック
	 * @param alarm_state
	 * 0: No alert, 1:User emergency alert, 2:Cut out alert, 3:Critical battery alert, 4:Low battery alert
	 */
	public void onAlarmStateChangedUpdate(final int alarm_state);

	/**
	 * フラットトリム状態が変更された
	 */
	public void onFlatTrimChanged();
}
