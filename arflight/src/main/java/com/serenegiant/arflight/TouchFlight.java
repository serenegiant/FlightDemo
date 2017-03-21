package com.serenegiant.arflight;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

public class TouchFlight implements IAutoFlight {
	private static final boolean DEBUG = false;				// FIXME 実働時はfalseにすること
	private static final String TAG = TouchFlight.class.getSimpleName();

	private static final int VERTIAL_WIDTH = 500;		// 仮想的な操作画面の幅
	private static final long CMD_DELAY_TIME_MS = 5;	// コマンド遅延時間=5ミリ秒
	private static final long MIN_CONTROL_TIME_MS = 25;	// 最小コマンド実行間隔
	private static final float EPS = 1f;				// 最小移動間隔[]

	private final Object mSync = new Object();
	private final AutoFlightListener mAutoFlightListener;
	private final Handler mHandler;	// プライベートスレッドでの実行用

	private float mFactorX = 1.0f, mFactorY = 1.0f, mFactorZ = 1.0f, mFactorR = 1.0f;
	private int mWidth, mHeight;
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
			if ((args != null) && (args.length == 10)) {
				mWidth = (int)args[0];
				mHeight = (int)args[1];
				mMinX = (float) args[2];
				mMaxX = (float) args[3];
				mMinY = (float) args[4];
				mMaxY = (float) args[5];
				mMinZ = (float) args[6];
				mMaxZ = (float) args[7];
				mTouchPointNums = (int) args[8];
				final float[] points = (float[]) args[9];

				final int n = mTouchPointNums * 4;    // 各点につき(x,y,z,t)の4つ
				if ((mTouchPoints == null) || (mTouchPoints.length < n)) {
					mTouchPoints = new float[n];
				}
				System.arraycopy(points, 0, mTouchPoints, 0, n);
			} else {
				float max_control_value = 1.0f;
				float scale_x = 1.0f, scale_y = 1.0f, scale_z = 1.0f, scale_r = 1.0f;
				if ((args != null) && (args.length == 5)) {
					if (args[0] instanceof Float) {
						max_control_value = (float) args[0] / 100.f;
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
				final float normalized_x = VERTIAL_WIDTH;	// 仮想的な横幅
				final float normalized_y = normalized_x * mHeight / mWidth;	// アスペクト比を保った仮想的な高さを計算
				final float normalized_z = 2;		// 仮想的な振れ幅を2
				mFactorX = max_control_value * scale_x * normalized_x / mWidth;
				mFactorY = max_control_value * scale_y * normalized_y / mHeight;
				mFactorZ = max_control_value * scale_z * normalized_z / (mMaxZ != mMinZ ? Math.abs(mMaxZ - mMinZ) : 1.0f);
				mFactorR = max_control_value * scale_r;
				if (DEBUG) Log.v(TAG, String.format("max_control_value:%f,factor(%f,%f,%f,%f)", max_control_value, mFactorX, mFactorY, mFactorZ, mFactorR));
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

	private static final float SCALE = 1000f;
	/**
	 * 与えられた移動量(dx,dy,dz)と移動時間dtから移動速度を計算してmoveにセット、新しい移動時間を返す
	 * @param dx 移動量x
	 * @param dy 移動量y
	 * @param dz 移動量z
	 * @param dt 初期移動時間
	 * @param move 移動速度をセットするint[]配列
	 * @return
	 */
	private long calcMoveCmd(final float dx, final float dy, final float dz, final long dt, final int[] move) {
//		if (DEBUG) Log.v(TAG, String.format("d(%f,%f,%f,%d)", dx, dy, dz, dt));
		float dt2 = dt;
		float sx = 0, sy = 0, sz = 0;
		if (dt > 0) {
			for (int i = 0; mIsPlayback && (i < 16); i++) {
				sx = dx / dt2  * SCALE;
				sy = dy / dt2 * SCALE;
				sz = dz / dt2 * SCALE;
				final float sx2 = sx < -100 ? -100 : (sx > 100 ? 100 : sx);
				if (sx != sx2) {
					dt2 = dx / (sx2 / SCALE);
					continue;
				}
				final float sy2 = sy < -100 ? -100 : (sy > 100 ? 100 : sy);
				if (sy != sy2) {
					dt2 = dy / (sy2 / SCALE);
					continue;
				}
				final float sz2 = sz < -100 ? -100 : (sz > 100 ? 100 : sz);
				if (sz != sz2) {
					dt2 = dz / (sz2 / SCALE);
					continue;
				}
				// sx, sy, szが全て[-100,+100]に収まれば終了
				break;
			}
		}
//		if (DEBUG) Log.v(TAG, String.format("s(%f,%f,%f,%f)", sx, sy, sz, dt2));
		if (move != null && move.length >= 3) {
			move[0] = (int) sx;
			move[1] = (int) sy;
			move[2] = (int) sz;
		}
		if (sx == 0 && sy == 0 && sz == 0)
			dt2 = 0;
		return (long)dt2;
	}
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
				long min_interval = MIN_CONTROL_TIME_MS, prev_t = (long)points[3];
				for (int ix = 4; mIsPlayback && (ix < n) ; ix += 4) {
//					if (DEBUG) Log.v(TAG, String.format("%4d)%d", ix, prev_t));
					min_interval = Math.min(min_interval, (long)points[ix + 3] - prev_t);
					prev_t = (long)points[ix + 3];
					if (min_interval <= 1) break;
				}
				min_interval = Math.max(min_interval, 1);	// 1ミリ秒よりも短くならないようにする
				if (DEBUG) Log.v(TAG, "min_interval=" + min_interval);
				// 最小時間間隔がMIN_CONTROL_TIME_MSよりも短ければ最小がMIN_CONTROL_TIME_MSになるようにスケール変換
				// MIN_CONTROL_TIME_MSよりも長ければ無変換(1.0f)
				final float ft = (min_interval < MIN_CONTROL_TIME_MS ? MIN_CONTROL_TIME_MS / (float)min_interval : 1.0f);
				final int[] values = new int[4];
				values[3] = 0;	// yaw = 0
				final float fx = mFactorX, fy = mFactorY, fz = mFactorZ, fr = mFactorR;	// ローカルコピー
				float prev_x = points[0] - mMinX;
				float prev_y = points[1] - mMinY;
				float prev_z = 0;
				float prev_r = 0;
				prev_t = (long)points[3];
				final float offset_z = points[2];
				final long start_time = System.currentTimeMillis();
				long current_time;
				for (int ix = 0; mIsPlayback && (ix < n) ; ix += 4) {
					final float x = points[ix] - mMinX;
					final float y = points[ix+1] - mMinY;
					final float z = points[ix+2] - offset_z;
					final float dx = x - prev_x;
					final float dy = prev_y - y;	// 画面上が前進方向になるように符号反転
					final float dz = z - prev_z;
//					final float dr = r - prev_r;
					if ((Math.abs(dx) > EPS) || (Math.abs(dy) > EPS) || (Math.abs(dz) > EPS)) {
						final long t = (long)((points[ix+3] - prev_t) * ft);
						final long dt = calcMoveCmd(dx * fx, dy * fy, dz * fz, t, values);
						if (!mIsPlayback) break;
						if (dt > 0) {
							prev_x = x;
							prev_y = y;
							prev_z = z;
//							prev_r = r;
							prev_t = (long) points[ix + 3];
							try {
								if (mAutoFlightListener.onStep(CMD_MOVE4, values, System.currentTimeMillis() - start_time)) {
									// trueが返ってきたので終了する
									break;
								}
								synchronized (mSync) {
									try {
										mSync.wait(dt + CMD_DELAY_TIME_MS);	// 移動時間+コマンド遅延時間
									} catch (final InterruptedException e) {
									}
								}
								values[0] = values[1] = values[2] = values[3] = 0;
								if (mAutoFlightListener.onStep(CMD_MOVE4, values, System.currentTimeMillis() - start_time)) {
									// trueが返ってきたので終了する
									break;
								}
								synchronized (mSync) {
									try {
										mSync.wait(CMD_DELAY_TIME_MS);
									} catch (final InterruptedException e) {
									}
								}
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						}
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
