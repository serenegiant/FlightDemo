package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/24.
 */
public interface IVideoStreamController {
	public void setVideoStreamListener(final VideoStreamListener listener);
	public boolean isVideoStreamingEnabled();
	public void enableVideoStreaming(boolean enable);
}
