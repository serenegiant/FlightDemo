package com.serenegiant.gl;

import android.graphics.SurfaceTexture;

public interface IVideoScreen {
	public void setEnableVideo(final boolean enable);
	public SurfaceTexture getVideoTexture();
}
