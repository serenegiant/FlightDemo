package com.serenegiant.arflight;

import com.parrot.arsdk.arcontroller.ARFrame;

public interface VideoStreamListener {
	/**
	 * 新しい映像フレームを受信した時に呼び出される
	 * @param frame
	 */
	public void onReceiveFrame(final ARFrame frame);
	public void onFrameTimeout();
}
