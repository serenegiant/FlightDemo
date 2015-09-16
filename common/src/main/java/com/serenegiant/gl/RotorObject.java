package com.serenegiant.gl;

public class RotorObject extends DynamicGameObject {
	private final float speed;

	/**
	 * コンストラクタ
	 * @param isCW ローターの回転方向, true: CW, false: CCW
	 */
	public RotorObject(final boolean isCW) {
		super(0, 0, 0, 1f);
		speed = (isCW ? -1 : 1) * 900;
	}

	@Override
	public void update(final float deltaTime) {
		angle.y += speed * deltaTime;
	}
}
