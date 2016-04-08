package com.serenegiant.arflight;

import com.parrot.arsdk.arcontroller.ARControllerCodec;

public interface IVideoStreamNew extends IVideoStream {
	public void configureDecoder(final ARControllerCodec codec);
	public void onReceiveFrame(final com.parrot.arsdk.arcontroller.ARFrame frame);
}
