package com.serenegiant.widget.gl;

import com.serenegiant.math.Vector;

public class GLLoadableModel extends GLPolygonModel {

	public GLLoadableModel(GLGraphics glGraphics) {
		super(glGraphics, Vector.zeroVector);
	}

	public GLLoadableModel(GLGraphics glGraphics, Vector offset) {
		super(glGraphics, offset);
	}

	public GLLoadableModel(GLGraphics glGraphics, Vector offset, float scale) {
		super(glGraphics, offset, scale);
	}

	public void loadModel(IModelView modelView, String fileName) {
		mVertex = GLObjLoader.load(modelView,  fileName);
	}
	
	public void loadModel(GLObjLoader objLoader) {
		if (objLoader != null)
			mVertex = objLoader.getVertex();
	}
}
