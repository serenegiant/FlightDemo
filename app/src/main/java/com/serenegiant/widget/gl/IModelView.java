package com.serenegiant.widget.gl;

import android.content.Context;

public interface IModelView {
	public static final int MODEL_BEBOP = 0;
	public static final int MODEL_MINIDRONE = 1;
	public static final int MODEL_JUMPINGSUMO = 2;
	public static final int MODEL_NUM = 3;

	public void setModel(final int model, final int type);
	public GLGraphics getGLGraphics();
	public FileIO getFileIO();
	public FileIO getExtFileIO();
	public FileIO getAssetIO();
	public Context getContext();
	//
	public int getNextPickId();
	public boolean isLandscape();
	public IScreen getCurrentScreen();
	public void setScreen(final IScreen screen);

	public void requestRender();
	public void setFpsRequest(final float fps);

	public int getWidth();
	public int getHeight();
	public void onResume();
	public void onPause();
	public void release();
	public void setVisibility(final int visibility);
}
