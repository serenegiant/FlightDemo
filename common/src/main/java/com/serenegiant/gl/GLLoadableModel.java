package com.serenegiant.gl;

import com.serenegiant.math.Vector;

public class GLLoadableModel extends GLPolygonModel {

	public GLLoadableModel(final GLGraphics glGraphics) {
		super(glGraphics, Vector.zeroVector);
	}

	public GLLoadableModel(final GLGraphics glGraphics, final Vector offset) {
		super(glGraphics, offset);
	}

	public GLLoadableModel(final GLGraphics glGraphics, final Vector offset, final float scale) {
		super(glGraphics, offset, scale);
	}

	/**
	 * コピーコンストラクタ
	 * @param other
	 */
	public GLLoadableModel(final GLLoadableModel other) {
		super(other);
	}

	public void loadModel(final IModelView modelView, final String fileName) {
		mVertex = GLObjLoader.load(modelView,  fileName);
	}
	
	public void loadModel(final GLObjLoader objLoader) {
		if (objLoader != null)
			mVertex = objLoader.getVertex();
	}
}
