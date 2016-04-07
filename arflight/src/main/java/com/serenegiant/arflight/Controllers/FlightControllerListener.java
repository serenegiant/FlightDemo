package com.serenegiant.arflight.Controllers;

public interface FlightControllerListener extends DeviceConnectionListener {
	/**
	 * 飛行状態が変化した時のコールバック
	 * @param state
	 * 0: Landed state, 1:Taking off state, 2:Hovering state, 3:Flying state
	 * 4:Landing state, 5:Emergency state, 6:Rolling state
	 */
	public void onFlyingStateChangedUpdate(final int state);

	/**
	 * フラットトリム状態が変更された
	 */
	public void onFlatTrimChanged();

	/**
	 * キャリブレーションが必要かどうかが変更された
	 * @param need_calibration
	 */
	public void onCalibrationRequiredChanged(final boolean need_calibration);

	/**
	 * キャリブレーションを開始/終了した
	 * @param isStart
	 */
	public void onCalibrationStartStop(final boolean isStart);
	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	public void onCalibrationAxisChanged(final int axis);

	/**
	 * 静止画撮影ステータスが変更された
	 * @param state
	 */
	public void onStillCaptureStateChanged(final int state);

	/**
	 * 動画撮影ステータスが変更された
	 * @param state
	 */
	public void onVideoRecordingStateChanged(final int state);

	/**
	 * 機体のストレージ状態が変化した時にコールバック
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	public void onUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal);
}
