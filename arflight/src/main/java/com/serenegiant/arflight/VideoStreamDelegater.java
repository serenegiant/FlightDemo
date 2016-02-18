package com.serenegiant.arflight;

import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.parrot.arsdk.arstream2.ARStream2Manager;
import com.parrot.arsdk.arstream2.ARStream2Receiver;
import com.serenegiant.arflight.configs.ARNetworkConfig;

/**
 * ライブ映像ストリームを取得するためのクラス
 * スカイコントローラーだとBebop/Bebop2と同じ方法は使えないみたい(´・ω・｀)
 * E/ARSTREAM_Reader: 21:16:05:121 | ARSTREAM_Reader_RunDataThread:369 - Error while reading stream data: Given IOBuffer identifier is unknown
 */
public class VideoStreamDelegater implements IVideoStreamController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = VideoStreamDelegater.class.getSimpleName();

	private static final boolean USE_ARSTREAM2 = true;

	private final DeviceController mParent;
	private final ARNetworkConfig mNetConfig;
	private final Object mStreamSync = new Object();
	private IVideoStream mVideoStream;
	private VideoThread mVideoThread;

	public VideoStreamDelegater(final DeviceController parent, final ARNetworkConfig config) {
		mParent = parent;
		mNetConfig = config;
	}

	/**
	 * enableVideoStreaming/sendVideoStreamingEnable(true)を呼ぶ前にセットしないとダメ
	 * ライブ映像ストリームが有効な間にnullにしちゃだめ。
	 * USE_ARSTREAM2=trueの時はストリーミング中に呼び出すとブロックされる
	 * @param video_stream
	 */
	@Override
	public void setVideoStream(final IVideoStream video_stream) {
		if (DEBUG) Log.v(TAG, "setVideoStream:video_stream=" + video_stream);
		synchronized (mStreamSync) {
			mVideoStream = video_stream;
		}
	}

	@Override
	public boolean isVideoStreamingEnabled() {
		synchronized (mStreamSync) {
			return mVideoThread != null && mVideoThread.isEnabled();
		}
	}

	/**
	 * ビデオストリーミング設定
	 * @param _enable true: ビデオストリーミング開始, false:ビデオストリーミング停止
	 * @return
	 */
	@Override
	public boolean enableVideoStreaming(final boolean _enable) {
		if (DEBUG) Log.v(TAG, "enableVideoStreaming:enable=" + _enable);
		boolean enabled = _enable;
		if (mVideoThread != null) {
			mVideoThread.enabled(enabled);
		} else {
			enabled = false;
		}
		final boolean sentStatus = sendVideoStreamingEnable(mParent, mNetConfig, enabled);
		if (mVideoThread != null) {
			mVideoThread.enabled(enabled && sentStatus);
		}
		return sentStatus;
	}

	public static boolean sendVideoStreamingEnable(final DeviceController controller, final ARNetworkConfig config, final boolean enabled) {

		if (DEBUG) Log.v(TAG, "sendVideoStreamingEnable:enabled=" + enabled);

		boolean sentStatus = true;

		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaStreamingVideoEnable((byte)(enabled ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = controller.sendData(config.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MediaStreamingVideoEnable command.");
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
			if (DEBUG) Log.v(TAG, "stopThread:");
			synchronized (mSync) {
				mIsRunning = false;
				mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "stopThread:finished");
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
				if (mNetConfig.hasVideo()) {	// これではねられることはないはず
					if (mNetConfig.isSupportStream2()) {
						if (DEBUG) Log.v(TAG, String.format("ip=%s,srv(stream=%d,ctrl=%d),client(stream=%d,ctrl=%d)",
							mNetConfig.getDeviceAddress(),
							mNetConfig.getServerStreamPort(),
							mNetConfig.getServerControlPort(),
							ARNetworkConfig.ARSTREAM2_CLIENT_STREAM_PORT,
							ARNetworkConfig.ARSTREAM2_CLIENT_CONTROL_PORT
						));
						if (USE_ARSTREAM2) {
							if (DEBUG) Log.v(TAG, "USE_ARSTREAM2:ARStream2Managerを使う");
							final ARStream2Manager streamManager = new ARStream2Manager(
								mNetConfig.getDeviceAddress(),
								mNetConfig.getServerStreamPort(),
								mNetConfig.getServerControlPort(),
								ARNetworkConfig.ARSTREAM2_CLIENT_STREAM_PORT,
								ARNetworkConfig.ARSTREAM2_CLIENT_CONTROL_PORT,
								mNetConfig.getMaxPacketSize(),
								mNetConfig.getMaxBitrate(),
								mNetConfig.getMaxLatency(),
								mNetConfig.getMaxNetworkLatency()
							);
							if (DEBUG) Log.v(TAG, "ARStream2Manager開始");
							streamManager.start();
							if (DEBUG) Log.v(TAG, "streaming loop:開始");
							try {
								if (DEBUG) Log.v(TAG, "ARStream2Receiver生成:mVideoStream=" + mVideoStream);
								synchronized (mStreamSync) {
									final ARStream2Receiver mReceiver = new ARStream2Receiver(streamManager, mVideoStream);
									if (DEBUG) Log.v(TAG, "ARStream2Receiver開始");
									mReceiver.start();
									try {
										for (; mIsRunning && mEnabled ;) {
											try {
												Thread.sleep(100);
											} catch (InterruptedException e) {
												break;
											}
										}
									} finally {
										if (DEBUG) Log.v(TAG, "ARStream2Receiver停止&破棄");
										mReceiver.stop();
										mReceiver.dispose();
									}
								}	// end of synchronized (mStreamSync)
							} finally {
								if (DEBUG) Log.v(TAG, "streaming loop:終了");
								streamManager.stop();
								streamManager.dispose();
							}
						} else {
							if (DEBUG) Log.v(TAG, "ARStreamManager2を使う");
							final ARStreamManager2 streamManager = new ARStreamManager2(mNetConfig);
							streamManager.start();
							if (DEBUG) Log.v(TAG, "streaming loop:開始");
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
								if (DEBUG) Log.v(TAG, "streaming loop:終了");
								streamManager.stop();
								streamManager.release();
							}
						}
					} else {
						if (DEBUG) Log.v(TAG, "ARStreamManagerを使う");
						final ARStreamManager streamManager = new ARStreamManager(mParent.getNetManager(),
							mNetConfig.getVideoDataIOBuffer(), mNetConfig.getVideoAckIOBuffer(),
							mNetConfig.getFragmentSize(), mNetConfig.getMaxAckInterval());
						streamManager.start();
						if (DEBUG) Log.v(TAG, "streaming loop:開始");
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
							if (DEBUG) Log.v(TAG, "streaming loop:終了");
							streamManager.stop();
							streamManager.release();
						}
					}
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
