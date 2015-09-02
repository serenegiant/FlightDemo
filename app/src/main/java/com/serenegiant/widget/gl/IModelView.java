package com.serenegiant.widget.gl;

import android.content.Context;

public interface IModelView {
	public GLGraphics getGLGraphics();
	public FileIO getFileIO();
	public FileIO getExtFileIO();
	public FileIO getAssetIO();
	public Context getContext();
	//
	public int getNextPickId();
	public boolean isLandscape();

	public void requestRender();
	public void setFpsRequest(final float fps);

	public int getWidth();
	public int getHeight();
	public void onResume();
	public void onPause();
	public void release();
	public void setVisibility(final int visibility);
}
