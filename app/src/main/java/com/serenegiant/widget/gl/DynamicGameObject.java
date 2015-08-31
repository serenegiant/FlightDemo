package com.serenegiant.widget.gl;

import com.serenegiant.math.BaseBounds;
import com.serenegiant.math.Vector;

public abstract class DynamicGameObject extends GameObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3375839480870725915L;
	public final Vector velocity;
	public final Vector accel;
	public Vector angle;
	
	public DynamicGameObject(float x, float y, float z, float radius) {
		super(x, y, z, radius);
		velocity = new Vector();
		accel = new Vector();
		setBounds(bounds);
	}

	public DynamicGameObject(Vector center, float radius) {
		this(center.x, center.y, center.z, radius);
	}

	@Override
	public void setBounds(BaseBounds bounds) {
		super.setBounds(bounds);
		angle = bounds.angle;
	}
	

}
