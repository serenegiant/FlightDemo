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
 * スカイコントローラーだとBebop/Bebop2と同じ方法は使えない(´・ω・｀)
 * E/ARSTREAM_Reader: 21:16:05:121 | ARSTREAM_Reader_RunDataThread:369 - Error while reading stream data: Given IOBuffer identifier is unknown
 */
public class VideoStreamDelegater implements IVideoStreamController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = VideoStreamDelegater.class.getSimpleName();

	private static final boolean USE_ARSTREAM2 = false;

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
	 * ライブ映像ストリームが有効な間にnullににちゃだめ。いまはデッドロックの危険があって排他制御してない
	 * @param video_stream
	 */
	@Override
	public void setVideoStream(final IVideoStream video_stream) {
		synchronized (mStreamSync) {
			mVideoStream = video_stream;
		}
	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return mVideoThread != null && mVideoThread.isEnabled();
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
	public boolean sendVideoStreamingEnable(final boolean _enabled) {
		boolean enabled = _enabled;
		if (mVideoThread != null) {
			mVideoThread.enabled(enabled);
		} else {
			enabled = false;
		}
		if (DEBUG) Log.v(TAG, "sendVideoStreamingEnable:enabled=" + _enabled + ", enabled=" + enabled);

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setARDrone3MediaStreamingVideoEnable((byte)(enabled ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = mParent.sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MediaStreamingVideoEnable command.");
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
				if (mNetConfig.hasVideo()) {	// これではねられることはないはず
					if (mNetConfig.isSupportStream2()) {
						if (DEBUG) Log.v(TAG, "ARStream2を使う");
//						public ARStream2Manager(String serverAddress, int serverStreamPort, int serverControlPort,
//							int clientStreamPort, int clientControlPort,
//							int maxPacketSize, int maxBitrate, int maxLatency, int maxNetworkLatency)
						if (DEBUG) Log.v(TAG, String.format("ARStream2Manager生成:ip=%s,srv(stream=%d,ctrl=%d),client(stream=%d,ctrl=%d)",
							mNetConfig.getDeviceAddress(),
							mNetConfig.getServerStreamPort(),
							mNetConfig.getServerControlPort(),
							mNetConfig.getClientStreamPort(),
							mNetConfig.getClientControlPort()
						));
						if (USE_ARSTREAM2) {
							final ARStream2Manager streamManager = new ARStream2Manager(
								mNetConfig.getDeviceAddress(),
								mNetConfig.getServerStreamPort(),
								mNetConfig.getServerControlPort(),
								mNetConfig.getClientStreamPort(),
								mNetConfig.getClientControlPort(),
								mNetConfig.getMaxPacketSize(),
								mNetConfig.getMaxBitrate(),
								mNetConfig.getMaxLatency(),
								mNetConfig.getMaxNetworkLatency()
							);
							if (DEBUG) Log.v(TAG, "ARStream2Manager開始");
							streamManager.start();
							try {
								if (DEBUG) Log.v(TAG, "ARStream2Receiver生成:mVideoStream=" + mVideoStream);
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
							} finally {
								if (DEBUG) Log.v(TAG, "ARStream2Manager停止&破棄");
								streamManager.stop();
								streamManager.dispose();
							}
						} else {

						}
					} else {
						if (DEBUG) Log.v(TAG, "ARStreamを使う");
						final ARStreamManager streamManager = new ARStreamManager(mParent.getNetManager(),
							mNetConfig.getVideoDataIOBuffer(), mNetConfig.getVideoAckIOBuffer(),
							mNetConfig.getFragmentSize(), mNetConfig.getMaxAckInterval());
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
