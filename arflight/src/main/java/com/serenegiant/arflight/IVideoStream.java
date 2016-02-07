package com.serenegiant.arflight;

public interface IVideoStream {
	public void onReceiveFrame(final ARFrame frame);
	public void onFrameTimeout();
}
