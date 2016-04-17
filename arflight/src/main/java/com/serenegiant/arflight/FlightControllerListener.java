package com.serenegiant.arflight;

public interface FlightControllerListener extends DeviceConnectionListener {
	/**
	 * 飛行状態が変化した時のコールバック
	 * @param controller
	 * @param state
	 * 0: Landed state, 1:Taking off state, 2:Hovering state, 3:Flying state
	 * 4:Landing state, 5:Emergency state, 6:Rolling state
	 */
	public void onFlyingStateChangedUpdate(final IDeviceController controller, final int state);

	/**
	 * フラットトリム状態が変更された
	 * @param controller
	 */
	public void onFlatTrimChanged(final IDeviceController controller);

	/**
	 * キャリブレーションが必要かどうかが変更された
	 * @param controller
	 * @param need_calibration
	 */
	public void onCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration);

	/**
	 * キャリブレーションを開始/終了した
	 * @param controller
	 * @param isStart
	 */
	public void onCalibrationStartStop(final IDeviceController controller, final boolean isStart);
	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	public void onCalibrationAxisChanged(final IDeviceController controller, final int axis);

	/**
	 * 静止画撮影ステータスが変更された
	 * @param controller
	 * @param state
	 */
	public void onStillCaptureStateChanged(final IDeviceController controller, final int state);

	/**
	 * 動画撮影ステータスが変更された
	 * @param controller
	 * @param state
	 */
	public void onVideoRecordingStateChanged(final IDeviceController controller, final int state);

	/**
	 * 機体のストレージ状態が変化した時にコールバック
	 * @param controller
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	public void onUpdateStorageState(final IDeviceController controller, final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal);
}
