package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/24.
 */
public interface IVideoStreamController {
	public void setVideoStream(final IVideoStream video_stream);
	public boolean isVideoStreamingEnabled();
	public void enableVideoStreaming(boolean enable);
}
