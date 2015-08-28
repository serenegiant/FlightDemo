package com.serenegiant.arflight;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class TouchFlight implements IAutoFlight {
	private static final boolean DEBUG = false;				// FIXME 実働時はfalseにすること
	private static final String TAG = "TouchFlight";

	private final Object mSync = new Object();
	private final AutoFlightListener mAutoFlightListener;
	private final Handler mHandler;	// プライベートスレッドでの実行用

	private float mFactorX = 1.0f, mFactorY = 1.0f, mFactorZ = 1.0f, mFactorR = 1.0f;
	private float mMinX, mMaxX;
	private float mMinY, mMaxY;
	private float mMinZ, mMaxZ;

	private int mTouchPointNums;
	private float[] mTouchPoints;
	private volatile boolean mIsPlayback;	// 再生中

	public TouchFlight(final AutoFlightListener listener) {
		if (listener == null) {
			throw new NullPointerException("AutoFlightListenerコールバックリスナーが設定されてない");
		}
		mAutoFlightListener = listener;
		final HandlerThread thread = new HandlerThread("ScriptFlight");
		thread.start();
		mHandler = new Handler(thread.getLooper());
	}

	@Override
	public void prepare(Object... args) throws RuntimeException {
		if (DEBUG) Log.v(TAG, "prepare:");
		if (mIsPlayback) {
			throw new IllegalStateException("既に実行中");
		}
		synchronized (mSync) {
			if ((args != null) && (args.length == 9)) {
				mMinX = (float) args[0];
				mMaxX = (float) args[1];
				mMinY = (float) args[2];
				mMaxY = (float) args[3];
				mMinZ = (float) args[4];
				mMaxZ = (float) args[5];
				mTouchPointNums = (int) args[6];
				final float[] points = (float[]) args[7];

				final int n = mTouchPointNums * 4;    // 各点につき(x,y,z,t)の4つ
				if ((mTouchPoints == null) || (mTouchPoints.length < n)) {
					mTouchPoints = new float[n];
				}
				System.arraycopy(points, 0, mTouchPoints, 0, n);
			} else {
				float max_control_value = 100.0f;
				float scale_x = 1.0f, scale_y = 1.0f, scale_z = 1.0f, scale_r = 1.0f;
				if ((args != null) && (args.length == 5)) {
					if (args[0] instanceof Float) {
						max_control_value = (float) args[0];
					}
					if (args[1] instanceof Float) {
						scale_x = (float) args[1];
					}
					if (args[2] instanceof Float) {
						scale_y = (float) args[2];
					}
					if (args[3] instanceof Float) {
						scale_z = (float) args[3];
					}
					if (args[4] instanceof Float) {
						scale_r = (float) args[4];
					}
				}
				mFactorX = max_control_value * scale_x / (mMaxX != mMinX ? Math.abs(mMaxX - mMinX) : 1.0f);
				mFactorY = max_control_value * scale_y / (mMaxY != mMinY ? Math.abs(mMaxY - mMinY) : 1.0f);
				mFactorZ = max_control_value * scale_z / (mMaxZ != mMinZ ? Math.abs(mMaxZ - mMinZ) : 1.0f);
				mFactorR = max_control_value * scale_r;
				if (DEBUG) Log.v(TAG, String.format("factor(%f,%f,%f,%f)", mFactorX, mFactorY, mFactorZ, mFactorR));
				if (!isPrepared())
					throw new RuntimeException("prepareできてない");
				try {
					mAutoFlightListener.onPrepared();
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	@Override
	public void play() throws IllegalStateException {
		if (DEBUG) Log.v(TAG, "play:");
		if (mIsPlayback) {
			throw new IllegalStateException("既に実行中");
		}
		if (!isPrepared()) {
			throw new IllegalStateException("prepareが呼ばれてない");
		}
		synchronized (mSync) {
			mIsPlayback = true;
		}
		mHandler.post(mPlaybackRunnable);
	}

	@Override
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
		synchronized (mSync) {
			mIsPlayback = false;
			mSync.notifyAll();
		}
		mHandler.removeCallbacks(mPlaybackRunnable);
	}

	@Override
	public boolean isPrepared() {
		synchronized (mSync) {
			return !mIsPlayback && (mTouchPoints != null) && (mTouchPoints.length >= 4);    // 最低2点必要
		}
	}

	@Override
	public boolean isPlaying() {
		synchronized (mSync) {
			return mIsPlayback;
		}
	}

	@Override
	public void release() {
		stop();
		mHandler.getLooper().quit();
	}

	public void clear() {
		synchronized (mSync) {
			if (mIsPlayback) {
				mMinX = mMaxX = mMinY = mMaxY = mMinZ = mMaxZ = 0;
				mFactorX = mFactorY = mFactorZ = mFactorR = 1.0f;
				mTouchPointNums = 0;
				mTouchPoints = null;
			}
		}
	}

	private static final float EPS = 0.1f;
	/**
	 * コマンド再生スレッドの実行用Runnable
	 */
	private final Runnable mPlaybackRunnable = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run");
			try {
				mAutoFlightListener.onStart();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			try {
				// タッチ軌跡を操縦コマンドに変換して送信
				final int n;
				final float[] points;
				synchronized (mSync) {
					n = mTouchPointNums * 4;
					points = mTouchPoints;    // ローカルコピー
				}
				final int[] values = new int[4];
				values[3] = 0;	// yaw = 0
				final float fx = mFactorX, fy = mFactorY, fz = mFactorZ, fr = mFactorR;	// ローカルコピー
				float prev_x = points[0];
				float prev_y = points[1];
				float prev_z = 0;
				float prev_r = 0;
				final float offset_z = points[2];
				final long touch_time = (long)points[3];
				final long start_time = System.currentTimeMillis();
				long current_time;
				for (int ix = 0; mIsPlayback && (ix < n) ; ix += 4) {
					final float x = points[ix];
					final float y = points[ix+1];
					final float z = points[ix+2] - offset_z;
					final float dx = x - prev_x;
					final float dy = y - prev_y;
					final float dz = z - prev_z;
//					final float dr = r - prev_r;
					if ((Math.abs(dx) > EPS) || (Math.abs(dy) > EPS) || (Math.abs(dz) > EPS)) {
						prev_x = x;
						prev_y = y;
						prev_z = z;
//						prev_r = r;
						values[0] = (int)(dx * fx);	// roll
						values[1] = (int)(dy * fy);	// pitch
						values[2] = (int)(dz * fz);	// gaz
//						values[3] = (int)(dr * fr);	// yaw
						current_time = System.currentTimeMillis() - start_time;
						final long t = (long)points[ix+3] - touch_time;
						if (t > current_time) {
							synchronized (mSync) {
								try {
									mSync.wait(t - current_time);
								} catch (final InterruptedException e) {
								}
							}
						}
						if (!mIsPlayback) break;
						try {
							if (mAutoFlightListener.onStep(CMD_MOVE4, values, t)) {
								// trueが返ってきたので終了する
								break;
							}
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
						// ここで操縦コマンド発行
					}
				}
			} catch (final Exception e) {
				mAutoFlightListener.onError(e);
			} finally {
				stop();
			}
			try {
				mAutoFlightListener.onStop();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run:終了");
		}
	};
}
