package com.serenegiant.arflight;

import com.parrot.arsdk.arstream2.ARStream2ReceiverListener;

public interface IVideoStream extends ARStream2ReceiverListener {
	public void onReceiveFrame(final ARFrame frame);
	public void onFrameTimeout();
	public VideoStream updateFps();
	public float getFps();
	public float getTotalFps();
}
