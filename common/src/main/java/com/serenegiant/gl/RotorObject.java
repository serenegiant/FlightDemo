package com.serenegiant.gl;

public class RotorObject extends DynamicGameObject {
	public static final float DEFAULT_SPEED = 900;

	private final boolean mIsCW;
	private float speed;

	/**
	 * コンストラクタ
	 * @param isCW ローターの回転方向, true: CW, false: CCW
	 */
	public RotorObject(final boolean isCW) {
		super(0, 0, 0, 1f);
		mIsCW = isCW;
		speed = (isCW ? -1 : 1) * DEFAULT_SPEED;
	}

	@Override
	public synchronized void update(final float deltaTime) {
		angle.y += speed * deltaTime;
	}

	public synchronized void setSpeed(final float speed) {
		this.speed = (mIsCW ? -1 : 1) *speed;
	}
}
