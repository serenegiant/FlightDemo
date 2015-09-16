package com.serenegiant.arflight;

public class AttributeCalibration {
	private final Object mSync = new Object();

	private boolean mNeedCalibration;
	private boolean xAxisCalibration;
	private boolean yAxisCalibration;
	private boolean zAxisCalibration;
	private boolean mCalibrationError;

	/**
	 * キャリブレーション状態を更新
	 * @param x
	 * @param y
	 * @param z
	 * @param failed
	 */
	public void update(final boolean x, final boolean y, final boolean z, final boolean failed) {
		mCalibrationError = failed;
		if (failed) {
			mNeedCalibration = true;
			xAxisCalibration = yAxisCalibration = zAxisCalibration = false;
		} else {
			xAxisCalibration = x;
			yAxisCalibration = y;
			zAxisCalibration = z;
		}
	}

	/**
	 * キャリブレーションが必要かどうかを設定
	 * @param need_calibration
	 */
	public void needCalibration(final boolean need_calibration) {
		synchronized (mSync) {
			mNeedCalibration = need_calibration;
		}
	}

	/**
	 * キャリブレーションが必要かどうかを取得
	 * @return
	 */
	public boolean needCalibration() {
		synchronized (mSync) {
			return mNeedCalibration;
		}
	}
}
