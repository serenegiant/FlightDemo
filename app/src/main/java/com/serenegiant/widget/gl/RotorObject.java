package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;

public class RotorObject extends DynamicGameObject {
	private final float speed;

	/**
	 * コンストラクタ
	 * @param position
	 * @param isCW ローターの回転方向, true: CW, false: CCW
	 */
	public RotorObject(final Vector position, final boolean isCW) {
		this(position.x, position.y, position.z, isCW);
	}

	/**
	 * コンストラクタ
	 * @param x
	 * @param y
	 * @param z
	 * @param isCW
	 */
	public RotorObject(final float x, final float y, final float z, final boolean isCW) {
		super(x, y, z, 1f);
		speed = (isCW ? -1 : 1) * 900;
	}

	@Override
	public void update(final float deltaTime) {
		angle.y += speed * deltaTime;
	}
}
