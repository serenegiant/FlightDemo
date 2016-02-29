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
	private static final String TAG = "ARStreamManager2";

	/** フレームプール初期化時のフレーム数(未使用) */
	private static final int FRAME_POOL_SZ = 40;
	private static final int DEFAULT_NALU_BUFFER_SZ = 4096;

	private final Object mPoolSync = new Object();
	private final List<ARFrame> mFramePool = new ArrayList<ARFrame>();
	private final LinkedBlockingQueue<ARFrame> mFrameQueue = new LinkedBlockingQueue<ARFrame>();


	/** 実行中フラグ */
	private volatile boolean mIsRunning;
	private ARStreamReader2 mARStreamReader;
	private final ARNetworkConfig mNetConfig;
	private final int naluBufferSize;
	private Thread mControlThread;
	private Thread mStreamThread;

	public ARStreamManager2(final ARNetworkConfig config) {
		this(config, DEFAULT_NALU_BUFFER_SZ);
	}

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
			mARStreamReader = new ARStreamReader2(
				mNetConfig.getDeviceAddress(),
				mNetConfig.getServerStreamPort(),
				mNetConfig.getServerControlPort(),
				ARNetworkConfig.ARSTREAM2_CLIENT_STREAM_PORT,
				ARNetworkConfig.ARSTREAM2_CLIENT_CONTROL_PORT,
				mNetConfig.getMaxPacketSize(),
				mNetConfig.getMaxBitrate(),
				mNetConfig.getMaxLatency(),
				mNetConfig.getMaxNetworkLatency(),
				naluBufferSize,
				mARStreamReader2Listener);
			mControlThread = new Thread(new Runnable() {
				@Override
				public void run() {
					mARStreamReader.runReaderControl();
				}
			}, "ARStreamReader2_ctrl");
			mStreamThread = new Thread(new Runnable() {
				@Override
				public void run() {
					mARStreamReader.runReaderStream();
				}
			}, "ARStreamReader2_stream");
			mControlThread.start();
			mStreamThread.start();
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
			if (mControlThread != null) {
				try {
					if (DEBUG) Log.v(TAG, "stop:wait control thread");
					mControlThread.join();
				} catch (final InterruptedException e) {
				}
				mControlThread = null;
			}
			if (mStreamThread != null) {
				try {
					if (DEBUG) Log.v(TAG, "stop:wait stream thread");
					mStreamThread.join();
				} catch (final InterruptedException e) {
				}
				mStreamThread = null;
			}
			if (DEBUG) Log.v(TAG, "stop:release ARStreamReader");
			if (mARStreamReader != null) {
				// XXX アホフランス人はARStreamReader2が内部で生成したARNativeDataを破棄するのを忘れてる
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

			final ARFrame frame = obtainFrame();
			// FIXME これは上手く動かないかも。動いても効率悪い
			frame.copyByteData(naluBuffer.getByteData(), naluBuffer.getDataSize());
			frame.setMissed(missingPacketsBefore);
			mFrameQueue.offer(frame);
		}
	};

}
