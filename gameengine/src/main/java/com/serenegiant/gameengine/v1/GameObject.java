package com.serenegiant.gameengine.v1;

import com.serenegiant.math.BaseBounds;
import com.serenegiant.math.CircleBounds;
import com.serenegiant.math.Vector;

import java.io.Serializable;

public abstract class GameObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3293493048019311362L;
	public Vector position;	// 実体はbounds.position
	public BaseBounds bounds = new CircleBounds(0, 0, 0, 0);	// 境界図形のデフォルトは球
	public boolean visible;
	public boolean needUpdate;
	public int state, tag;
	/**
	 * オブジェクトのID、自動生成値は1以上、負数はユーザー定義
	 */
	public int mID;
	private static int mIDCount = 1;

	public GameObject() {
		position = bounds.position;
		needUpdate = visible = true;
		mID = nextID();
	}

	protected synchronized static int nextID() {
		return mIDCount++;
	}
	
	// x, yはオブジェクトの中心座標
	public GameObject(final float x, final float y, final float z, final float radius) {
		this();
		bounds.radius = radius;
		bounds.setPosition(x, y, z);
	}

	// x, yはオブジェクトの中心座標
	public GameObject(final float x, final float y, final float radius) {
		this(x, y, 0f, radius);
	}

	public GameObject(final Vector center, final float radius) {
		this(center.x, center.y, center.z, radius);
	}

	public void setBounds(final BaseBounds bounds) {
		this.bounds = bounds;
		position = bounds.position;
	}
	
	public void setPosition(final Vector pos) {
		bounds.setPosition(pos);
	}
	
	public void setPosition(final float x, final float y, final float z) {
		bounds.setPosition(x, y, z);
	}

	public void setPosition(final float x, final float y) {
		bounds.setPosition(x, y);
	}
	
	public void rotate(final Vector angle) {
		bounds.rotate(angle);
	}
	
	public abstract void update(final float deltaTime);
}
