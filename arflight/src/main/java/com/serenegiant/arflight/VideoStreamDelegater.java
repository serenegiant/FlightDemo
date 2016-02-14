package com.serenegiant.arflight;

import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.serenegiant.arflight.configs.ARNetworkConfig;

/**
 * ライブ映像ストリームを取得するためのクラス
 * スカイコントローラーだとBebop/Bebop2と同じ方法は使えない(´・ω・｀)
 * E/ARSTREAM_Reader: 21:16:05:121 | ARSTREAM_Reader_RunDataThread:369 - Error while reading stream data: Given IOBuffer identifier is unknown
 */
public class VideoStreamDelegater implements IVideoStreamController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = VideoStreamDelegater.class.getSimpleName();

	private final DeviceController mParent;
	private final Object mStreamSync = new Object();
	private IVideoStream mVideoStream;
	private VideoThread mVideoThread;
	protected final ARNetworkConfig mNetConfig;
	protected final int videoFragmentSize;
	protected final int videoFragmentMaximumNumber;
	protected final int videoMaxAckInterval;


	public VideoStreamDelegater(final DeviceController parent, ARNetworkConfig config) {
		mParent = parent;
		mNetConfig = config;
		videoFragmentSize = parent.videoFragmentSize;
		videoFragmentMaximumNumber = parent.videoFragmentMaximumNumber;
		videoMaxAckInterval = parent.videoMaxAckInterval;
	}

	@Override
	public void setVideoStream(final IVideoStream video_stream) {
		synchronized (mStreamSync) {
			mVideoStream = video_stream;
		}
	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return true;
	}

	@Override
	public void enableVideoStreaming(boolean enable) {
		sendVideoStreamingEnable(enable);
	}

	/**
	 * ビデオストリーミング設定
	 * @param _enabled true: ビデオストリーミング開始, false:ビデオストリーミング停止
	 * @return
	 */
	@Override
	public boolean sendVideoStreamingEnable(final boolean _enabled) {
		boolean sentStatus = true;

		boolean enabled = _enabled;
		if (mVideoThread != null) {
			mVideoThread.enabled(enabled);
		} else {
			enabled = false;
		}

		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaStreamingVideoEnable((byte)(enabled ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = mParent.sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Exposure command.");
		}
		if (mVideoThread != null) {
			mVideoThread.enabled(enabled && sentStatus);
		}

		return sentStatus;
	}

	private class VideoThread extends Thread {
		private final Object mSync = new Object();
		private volatile boolean mIsRunning;
		private volatile boolean mEnabled;

		public VideoThread () {
			mIsRunning = true;
			mEnabled = false;
		}

		public void stopThread() {
			synchronized (mSync) {
				mIsRunning = false;
				mSync.notifyAll();
			}
		}

		public boolean isRunning() {
			return mIsRunning;
		}

		public boolean isEnabled() {
			return mIsRunning && mEnabled;
		}

		public void enabled(final boolean enabled) {
			synchronized (mSync) {
				mEnabled = enabled;
				mSync.notifyAll();
			}
		}

		@Override
		public void run() {
			for (; mIsRunning ;) {
				if (!mEnabled) {
					synchronized (mSync) {
						try {
							mSync.wait(500);
						} catch (final InterruptedException e) {
							break;
						}
						continue;
					}
				}
				final ARStreamManager streamManager = new ARStreamManager(mParent.getNetManager(),
					mNetConfig.getVideoDataIOBuffer(), mNetConfig.getVideoAckIOBuffer(),
					videoFragmentSize, videoMaxAckInterval);
				streamManager.start();
				try {
					for (; mIsRunning && mEnabled ;) {
						final ARFrame frame = streamManager.getFrame(VIDEO_RECEIVE_TIMEOUT_MS);
						if (frame != null) {
							try {
								synchronized (mStreamSync) {
									if (mVideoStream != null) {
										mVideoStream.onReceiveFrame(frame);
									}
								}
							} finally {
								streamManager.recycle(frame);
							}
						}
					}
				} finally {
					streamManager.stop();
					streamManager.release();
				}
			}
		}
	}

	/**
	 * ビデオストリーミングデータ受信スレッドを開始
	 */
	public void startVideoThread() {
		if (DEBUG) Log.v(TAG, "startVideoThread");
		// 既に動いていれば一旦停止させる
		stopVideoThread();
		mVideoThread = new VideoThread();
		mVideoThread.start();
		if (DEBUG) Log.v(TAG, "startVideoThread:終了");
	}

	/**
	 * ストリーミングデータ受信スレッドを終了(終了するまで戻らない)
	 */
	public void stopVideoThread() {
		if (DEBUG) Log.v(TAG, "stopVideoThread:");
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mVideoThread) {
			mVideoThread.stopThread();
			try {
				mVideoThread.join();
				mVideoThread = null;
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG) Log.v(TAG, "stopVideoThread:終了");
	}
}
