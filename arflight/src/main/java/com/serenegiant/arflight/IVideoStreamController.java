package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/24.
 */
public interface IVideoStreamController {
	public static final int DEFAULT_VIDEO_FRAGMENT_SIZE = 1000;
	public static final int DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER = 128;
	public static final int VIDEO_RECEIVE_TIMEOUT_MS = 500;

	public void setVideoStream(final IVideoStream video_stream);
	public boolean isVideoStreamingEnabled();
	public void enableVideoStreaming(boolean enable);
}
