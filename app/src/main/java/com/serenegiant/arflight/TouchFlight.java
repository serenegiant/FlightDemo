package com.serenegiant.arflight;

import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class TouchFlight implements IAutoFlight {
	private static final boolean DEBUG = true;				// FIXME 実働時はfalseにすること
	private static final String TAG = "TouchFlight";

	private final Object mSync = new Object();
	private final AutoFlightListener mAutoFlightListener;
	private final Handler mHandler;	// プライベートスレッドでの実行用

	private double mMaxControlValue = 100;
	private double mScaleX = 1.0, mScaleY = 1.0, mScaleZ = 1.0;
	private int mMinX, mMaxX;
	private int mMinY, mMaxY;
	private int mMinZ, mMaxZ;
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
			if ((args != null) && (args.length == 8)) {
				mMinX = (int) args[0];
				mMaxX = (int) args[1];
				mMinY = (int) args[2];
				mMaxY = (int) args[3];
				mMinZ = (int) args[4];
				mMaxZ = (int) args[5];
				mTouchPointNums = (int) args[6];
				final float[] points = (float[]) args[7];

				final int n = mTouchPointNums * 4;    // 各点につき(x,y,z,t)の4つ
				if ((mTouchPoints == null) || (mTouchPoints.length < n)) {
					mTouchPoints = new float[n];
				}
				System.arraycopy(points, 0, mTouchPoints, 0, n);
			} else {
				if ((args != null) && (args.length == 4)) {
					if (args[0] instanceof Double) {
						mMaxControlValue = (double) args[0];
					}
					if (args[1] instanceof Double) {
						mScaleX = (double) args[1];
					}
					if (args[2] instanceof Double) {
						mScaleY = (double) args[2];
					}
					if (args[3] instanceof Double) {
						mScaleZ = (double) args[3];
					}
				}
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
				mMaxControlValue = 100;
				mScaleX = 1.0;
				mScaleY = 1.0;
				mScaleZ = 1.0;
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
				// FIXME ここでタッチ軌跡を操縦コマンドに変換して送信
				final int n;
				final float[] points;
				synchronized (mSync) {
					n = mTouchPointNums * 4;
					points = mTouchPoints;    // ローカルコピー
				}
				final long start_time = System.currentTimeMillis();
				final int[] values = new int[4];
				final float fx = (float)(mMaxControlValue * mScaleX / (mMaxX != mMinX ? Math.abs(mMaxX - mMinX) : 1));
				final float fy = (float)(mMaxControlValue * mScaleY / (mMaxY != mMinY ? Math.abs(mMaxY - mMinX) : 1));
				final float fz = (float)(mMaxControlValue * mScaleZ / (mMaxZ != mMinZ ? Math.abs(mMaxZ - mMinZ) : 1));
				values[3] = 0;	// yaw = 0
				float prev_x = points[0];
				float prev_y = points[1];
				float prev_z = points[2];
				final long touch_time = (long)points[3];
				long current_time;
				for (int ix = 0; mIsPlayback && (ix < n) ; ix += 4) {
					final float x = points[ix];
					final float y = points[ix+1];
					final float z = points[ix+2];
					final float dx = x - prev_x;
					final float dy = y - prev_y;
					final float dz = z - prev_z;
					if ((Math.abs(dx) > EPS) || (Math.abs(dy) > EPS) || (Math.abs(dz) > EPS)) {
						prev_x = x;
						prev_y = y;
						prev_z = z;
						values[0] = (int)(dx * fx);	// roll
						values[1] = (int)(dy * fy);	// pitch
						values[2] = (int)(dz * fz);	// faz
						current_time = System.currentTimeMillis() - start_time;
						final long t = (long)points[ix+3];
						if (t > current_time) {
							synchronized (mSync) {
								try {
									mSync.wait(t - current_time);
								} catch (InterruptedException e) {
								}
							}
						}
						if (!mIsPlayback) break;
						try {
							if (mAutoFlightListener.onStep(CMD_MOVE, values, t)) {
								// trueが返ってきたので終了する
								break;
							}
						} catch (Exception e) {
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
