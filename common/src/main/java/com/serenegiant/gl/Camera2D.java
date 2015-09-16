package com.serenegiant.gl;

import com.serenegiant.math.Vector;

public interface Camera2D {
	public void setScreenSize(float screenWidth, float screenHeight);
	public void setZoom(float zoom);
	public float getZoom();
	public void viewToCamera(Vector pos);

}
