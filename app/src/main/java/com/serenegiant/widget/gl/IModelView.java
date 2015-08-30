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
}
