package com.serenegiant.gameengine.v1;

public interface IModelView extends IGLGameView {
	public static final int MODEL_NON = 0;
	public static final int MODEL_BEBOP = 1;
	public static final int MODEL_MINIDRONE = 2;
	public static final int MODEL_JUMPINGSUMO = 3;
	public static final int MODEL_BEBOP2 = 4;
	public static final int MODEL_CARGO = 5;
	public static final int MODEL_SKYCONTROLLER = 6;
	public static final int MODEL_NUM = 7;

	public void setModel(final int model, final int type);
	public void hasGuard(final boolean hasGuard);
	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[度]
	 * @param pitch 前後の傾き(機種の上げ下げ)[度]
	 * @param yaw 水平回転[度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz);

	public void startEngine();
	public void stopEngine();
	public void setRotorSpeed(final float speed);
	public void setAxis(final int axis);
}
