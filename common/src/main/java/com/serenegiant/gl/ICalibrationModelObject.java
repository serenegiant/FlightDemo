package com.serenegiant.gl;

/**
 * Created by saki on 2015/09/17.
 */
public interface ICalibrationModelObject {
	/**
	 * 回転軸を指定
	 * @param axis -1:回転なし, 0:x, 1:y, 2:z
	 */
	public void setAxis(final int axis);
}
