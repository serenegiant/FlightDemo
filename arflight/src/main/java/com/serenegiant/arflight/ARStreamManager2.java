package com.serenegiant.arflight;

import android.util.Log;

import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arsal.ARNativeData;
import com.parrot.arsdk.arstream.ARSTREAM_READER_CAUSE_ENUM;
import com.parrot.arsdk.arstream.ARStreamReader;
import com.parrot.arsdk.arstream.ARStreamReader2;
import com.parrot.arsdk.arstream.ARStreamReader2Listener;
import com.parrot.arsdk.arstream.ARStreamReaderListener;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ARStreamManager2 {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = "ARStreamManager";

	/** フレームプール初期化時のフレーム数(未使用) */
	private static final int FRAME_POOL_SZ = 40;

	private final Object mPoolSync = new Object();
	private final List<ARFrame> mFramePool = new ArrayList<ARFrame>();
	private final LinkedBlockingQueue<ARFrame> mFrameQueue = new LinkedBlockingQueue<ARFrame>();


	/** 実行中フラグ */
	private volatile boolean mIsRunning;
	private ARStreamReader2 mARStreamReader;
	private final ARNetworkConfig mNetConfig;
	private final int naluBufferSize;
	public ARStreamManager2(final ARNetworkConfig config, final int naluBufferSize) {
		mNetConfig = config;
		this.naluBufferSize = naluBufferSize;
	}

	/**
	 * 機体からの映像取得用オブジェクト・スレッドを生成＆開始する
	 */
	public void start() {
		if (DEBUG) Log.v(TAG, "start:" + mIsRunning);
		if (!mIsRunning) {
			mIsRunning = true;
//			ARStreamReader2(String serverAddress, int serverStreamPort, int serverControlPort, int clientStreamPort, int clientControlPort,
//				int maxPacketSize, int maxBitrate, int maxLatency, int maxNetworkLatency, int naluBufferSize, ARStreamReader2Listener listener)
			mARStreamReader = new ARStreamReader2(
				mNetConfig.getDeviceAddress(),
				mNetConfig.getServerStreamPort(),
				mNetConfig.getServerControlPort(),
				mNetConfig.getClientStreamPort(),
				mNetConfig.getClientControlPort(),
				mNetConfig.getMaxPacketSize(),
				mNetConfig.getMaxBitrate(),
				mNetConfig.getMaxLatency(),
				mNetConfig.getMaxNetworkLatency(),
				naluBufferSize,
				mARStreamReader2Listener);
			mARStreamReader.runReaderControl();
			mARStreamReader.runReaderStream();
		}
	}

	/**
	 * 機体からの映像取得用オブジェクト・スレッドを破棄する
	 */
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:" + mIsRunning);
		if (mIsRunning) {
			mIsRunning = false;
			if (mARStreamReader != null) {
				mARStreamReader.stop();
			}
/*			if (mDataThread != null) {
				try {
					if (DEBUG) Log.v(TAG, "stop:wait data thread");
					mDataThread.join();
				} catch (final InterruptedException e) {
				}
				mDataThread = null;
			}
			if (mAckThread != null) {
				try {
					if (DEBUG) Log.v(TAG, "stop:wait ack thread");
					mAckThread.join();
				} catch (final InterruptedException e) {
				}
				mAckThread = null;
			} */
			if (DEBUG) Log.v(TAG, "stop:release ARStreamReader");
			if (mARStreamReader != null) {
				mARStreamReader.dispose();
				mARStreamReader = null;
			}
		}
		if (DEBUG) Log.v(TAG, "stop:終了");
	}

	/**
	 * 関連するリソースを破棄する。
	 */
	public void release() {
		stop();
		synchronized (mPoolSync) {
			for (final ARFrame frame: mFramePool) {
				if (frame != null) {
					frame.dispose();
				}
			}
			mFramePool.clear();
		}
		ARFrame frame = mFrameQueue.peek();
		for (; frame != null; ) {
			mFrameQueue.remove(frame);
			frame.dispose();
			frame = mFrameQueue.peek();
		}
		mFrameQueue.clear();
	}

	/**
	 * フレームキューから先頭フレームを取得する
	 * フレームキューが空なら指定した時間待機し、それでも空ならnullを返す
	 * @param receive_timeout_ms
	 * @return
	 */
	public ARFrame getFrame(final long receive_timeout_ms) {
		ARFrame result = null;
		try {
			result = mFrameQueue.poll(receive_timeout_ms, TimeUnit.MILLISECONDS);
		} catch (final InterruptedException e) {
			// ignore
		}
		return result;
	}

	/**
	 * フレームキューから先頭フレームを取得する
	 * フレームキューが空ならnullを返す
	 * @return
	 */
	public ARFrame getFrame() {
		return mFrameQueue.poll();
	}

	/**
	 * プールから空きフレームを取得する, なければ新規生成する
	 * @return
	 */
	private ARFrame obtainFrame() {
		ARFrame result;
		synchronized (mPoolSync) {
			result = mFramePool.size() > 0 ? mFramePool.get(0) : null;
		}
		if (result == null) {
			// FIXME 最大フレーム数を制限した方がいいかも
			result = new ARFrame();
		}
		return result;
	}

	/**
	 * フレームをプールに返却する
	 * @param frame
	 */
	public void recycle(final ARFrame frame) {
		if (frame != null) {
			synchronized (mPoolSync) {
				mFramePool.add(frame);
			}
		}
	}

	/** ARStreamReader2Listenerからのコールバックリスナー */
	private final ARStreamReader2Listener mARStreamReader2Listener = new ARStreamReader2Listener() {
		@Override
		public void onNaluReceived(final ARNativeData naluBuffer,
			final boolean isFirstNaluInAu, final boolean sLastNaluInAu,
			final long auTimestamp, final int missingPacketsBefore) {

		}
	};

//	/** ARStreamReaderListenerからのコールバックリスナー */
//	private final ARStreamReaderListener mARStreamReaderListener = new ARStreamReaderListener() {
//		@Override
//		public ARNativeData didUpdateFrameStatus(
//			final ARSTREAM_READER_CAUSE_ENUM cause, final ARNativeData currentFrame,
//			final boolean isFlushFrame, final int nbSkippedFrames, final int newBufferCapacity) {
//
//			ARNativeData next_frame = null;
//			switch (cause) {
//			case ARSTREAM_READER_CAUSE_FRAME_COMPLETE:
//				if (DEBUG) Log.v(TAG, "ARSTREAM_READER_CAUSE_FRAME_COMPLETE"); // Frame is complete (no error)
//			case ARSTREAM_READER_CAUSE_COPY_COMPLETE:
//				// Copy of previous frame buffer is complete (called only after ARSTREAM_READER_CAUSE_FRAME_TOO_SMALL)
//				if (currentFrame instanceof ARFrame) {
//					final ARFrame _frame = (ARFrame)currentFrame;
//					_frame.isIFrame(isFlushFrame);
//					_frame.setMissed(nbSkippedFrames);
//					// キューに追加する
//					mFrameQueue.offer(_frame);
//					// 次のフレーム用にフレームプールから取得して返す
//					next_frame = obtainFrame();
//				}
//				break;
//			case ARSTREAM_READER_CAUSE_FRAME_TOO_SMALL:
//				// Frame buffer is too small for the frame on the network
//				// フレームのバッファサイズが小さい時はサイズ調整してから同じのを返す
//				currentFrame.ensureCapacityIsAtLeast(newBufferCapacity);
//				next_frame = currentFrame;
//				break;
//			case ARSTREAM_READER_CAUSE_CANCEL:
//				// StreamReaderが終了中なのでバッファーは必要ない
//				if (currentFrame instanceof ARFrame) {
//					recycle((ARFrame) currentFrame);
//				}
//				// FIXME この時に次のフレームデータとしてnullを返していいかどうか要確認
//				// だめならダミーでcurrentFrameを返す
//				break;
//			}
//
//			return next_frame;
//		}
//	};

}
