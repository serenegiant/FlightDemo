package com.serenegiant.arflight;

/** スカイコントローラーからのコールバックリスナー */
public interface SkyControllerListener extends DeviceConnectionListener {
	/**
	 * 接続した時のコールバック
	 * @param controller
	 */
	public void onSkyControllerConnect(final IDeviceController controller);
	/**
	 * 切断された時のコールバック
	 */
	public void onSkyControllerDisconnect(final IDeviceController controller);
	/**
	 * 電池残量が変化した時のコールバック
	 * @param controller
	 * @param percent
	 */
	public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent);
	/**
	 * 機器からの異常通知時のコールバック
	 * @param controller
	 * @param alarm_state
	 * 0: No alert, 1:User emergency alert, 2:Cut out alert, 3:Critical battery alert, 4:Low battery alert
	 */
	public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state);

	/**
	 * キャリブレーションが必要かどうかが変更された
	 * @param need_calibration
	 */
	public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration);

	/**
	 * キャリブレーションを開始/終了した
	 * @param isStart
	 */
	public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart);
	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis);
}
