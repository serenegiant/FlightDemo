package com.serenegiant.opencv;
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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.effect.EffectContext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.glutils.ShaderConst;
import com.serenegiant.mediaeffect.IEffect;
import com.serenegiant.mediaeffect.MediaEffectAutoFix;
import com.serenegiant.mediaeffect.MediaEffectBrightness;
import com.serenegiant.mediaeffect.MediaEffectDilation;
import com.serenegiant.mediaeffect.MediaEffectErosion;
import com.serenegiant.mediaeffect.MediaEffectExposure;
import com.serenegiant.mediaeffect.MediaEffectExtraction;
import com.serenegiant.mediaeffect.MediaEffectPosterize;
import com.serenegiant.mediaeffect.MediaEffectSaturate;
import com.serenegiant.mediaeffect.MediaSource;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.utils.FpsCounter;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
//	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = ImageProcessor.class.getSimpleName();

	private static final int REQUEST_DRAW = 1;
	private static final int REQUEST_UPDATE_SIZE = 2;

	/**
	 * 処理結果を通知するためのコールバックリスナー
	 */
	public interface ImageProcessorCallback {
		/**
		 * 結果映像を受け取った時の処理
		 * @param frame
		 */
		public void onFrame(final ByteBuffer frame);

		/**
		 * 処理結果データを受け取った時の処理
		 * @param type
		 * @param result
		 */
		public void onResult(final int type, final float[] result);
	}

	/** 排他制御用 */
	private final Object mSync = new Object();
	private final ImageProcessorCallback mCallback;
	private List<IEffect> mEffects = new ArrayList<IEffect>();
	private volatile boolean isProcessingRunning;	// XXX ProcessingTask#isRunningを使えばこれはいらんかも
	private ProcessingTask mProcessingTask;
	private Handler mAsyncHandler;

	private boolean mEnableAutoFix;
	private boolean mEnableExposure;
	private float mExposure;
	private boolean mEnableBrightness;
	private float mBrightness;
	private boolean mEnableSaturation;
	private float mSaturation;
	private boolean mEnablePosterize;
	private float mPosterize;	// [1,256], デフォルト10
	private boolean mEnableExtraction;
//	private int mSmoothType = 0;
	private float mBinarizeThreshold = 0.5f;
//	private boolean mEnableCanny;
	private static final int[][] COLOR_RANGES = {
		{0, 180, 0, 50, 120, 255},		// 白色
		{25, 35, 120, 130, 180, 200},	// 黄色...蛍光色はこれだとだめみたい
	};
	private int COLOR_RANGE_IX = 0;
	protected final int[] EXTRACT_COLOR_HSV_LIMIT = COLOR_RANGES[COLOR_RANGE_IX];

	private final int mSrcWidth, mSrcHeight;
	private volatile boolean requestUpdateExtractionColor;
	/** 映像処理のフレームレート計算用 */
	private final FpsCounter mResultFps = new FpsCounter();

	/** native側のインスタンスポインタ, 名前を変えたりしちゃダメ */
	private long mNativePtr;

	/**
	 * コンストラクタ
	 * @param src_width ソース映像サイズ
	 * @param src_height  ソース映像サイズ
	 * @param callback
	 * @throws NullPointerException
	 */
	public ImageProcessor(final int src_width, final int src_height, final ImageProcessorCallback callback)
		throws NullPointerException {

		if (callback == null) {
			throw new NullPointerException("callback should not be null");
		}
		mSrcWidth = src_width;
		mSrcHeight = src_height;
		mCallback = callback;
		mNativePtr = nativeCreate(new WeakReference<ImageProcessor>(this));
		final HandlerThread thread = new HandlerThread("OnFrameAvailable");
		thread.start();
		mAsyncHandler = new Handler(thread.getLooper());
	}

	/**
	 * ImageProcessorの処理スレッドを開始。処理スレッドが実際に処理を始めるまで戻らない
	 * @param width 処理画像サイズ
	 * @param height 処理画像サイズ
	 */
	public void start(final int width, final int height) {
		if (mProcessingTask == null) {
			mProcessingTask = new ProcessingTask(this, mSrcWidth, mSrcHeight, width, height);
			new Thread(mProcessingTask, "VideoStream$rendererTask").start();
			mProcessingTask.waitReady();
			synchronized (mSync) {
				for ( ; !isProcessingRunning ; ) {
					try {
						mSync.wait();
					} catch (final InterruptedException e) {
						break;
					}
				}
			}
		}
	}

	/**
	 * ImageProcessorの処理スレッドを停止
	 */
	public void stop() {
		final ProcessingTask task = mProcessingTask;
		mProcessingTask = null;
		if (task != null) {
			synchronized (mSync) {
				isProcessingRunning = false;
				mSync.notifyAll();
			}
			task.release();
		}
	}

	/**
	 * 関連するリソースをすべて破棄する
	 */
	public void release() {
		stop();
		if (mAsyncHandler != null) {
			try {
				mAsyncHandler.getLooper().quit();
			} catch (final Exception e) {
			}
			mAsyncHandler = null;
		}
		nativeRelease(mNativePtr);
	}

	/** 映像受け取り用Surfaceを取得 */
	public Surface getSurface() {
		return mProcessingTask.getSurface();
	}

	/** 映像受け取り用SurfaceTextureを取得 */
	public SurfaceTexture getSurfaceTexture() {
		return mProcessingTask.getSurfaceTexture();
	}

	/**
	 * 処理フレームレートを更新
	 */
	public void updateFps() {
		mResultFps.update();
	}

	/**
	 * 前回#getFpsを呼び出してから現在までの処理フレームレートを取得
	 * @return
	 */
	public float getFps() {
		return mResultFps.getFps();
	}

	/**
	 * 測定開始からの処理フレームレートを取得
	 * @return
	 */
	public float getTotalFps() {
		return mResultFps.getTotalFps();
	}
//================================================================================
	// 結果映像の種類定数
	/** 結果映像として元映像を返す */
	public static final int RESULT_FRAME_TYPE_SRC = 0;
	/** 結果映像として処理後映像を返す */
	public static final int RESULT_FRAME_TYPE_DST = 1;
	/** 結果映像として元映像に解析結果を書き込んで返す */
	public static final int RESULT_FRAME_TYPE_SRC_LINE = 2;
	/** 結果映像として処理後映像に解析結果を書き込んで返す */
	public static final int RESULT_FRAME_TYPE_DST_LINE = 3;

	/**
	 * 結果映像の種類を変更
	 * @param result_frame_type
	 */
	public void setResultFrameType(final int result_frame_type) {
		final int result = nativeSetResultFrameType(mNativePtr, result_frame_type);
		if (result != 0) {
			throw new IllegalStateException("nativeSetResultFrameType:result=" + result);
		}
	}

	/**
	 * 結果映像の種類を取得
	 * @return
	 * @throws IllegalStateException
	 */
	public int getResultFrameType() throws IllegalStateException {
		final int result = nativeGetResultFrameType(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetResultFrameType:result=" + result);
		}
		return result;
	}

	/**
	 * 自動補正を有効にするかどうかを設定
	 * 諸般の事情で実際には常時ON
	 * @param enable
	 */
	public void enableAutoFix(final boolean enable) {
		if (mEnableAutoFix != enable) {
			mEnableAutoFix = enable;
			final float value = enable ? 1.0f : 0.0f;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectAutoFix) {
						((MediaEffectAutoFix)effect).setParameter(value);
						effect.setEnable(true);	// FIXME 常時有効にする
					}
				}
			}
		}
	}

	/**
	 * 自動補正が有効かどうかを取得
	 * @return
	 */
	public boolean enableAutoFix() {
		return mEnableAutoFix;
	}

	/**
	 * 露出調整を有効にするかどうかを設定
	 * 機体側のカメラ設定で露出調整してそれでもダメな時のみ使うこと
	 * @param enable
	 */
	public void enableExposure(final boolean enable) {
		if (mEnableExposure != enable) {
			mEnableExposure = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectExposure) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

	/***
	 * 露出調整が有効かどうかを取得
	 * @return
	 */
	public boolean enableExposure() {
		return mEnableExposure;
	}

	/**
	 * 露出調整
	 * @param exposure -10〜+10, 0なら無調整
	 */
	public void setExposure(final float exposure) {
		final float exp = sat(exposure, -10.0f, +10.0f);
		if (mExposure != exp) {
			mExposure = exp;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectExposure) {
						((MediaEffectExposure)effect).setParameter(exp);
					}
				}
			}
		}
	}

	/**
	 * 露出調整値を取得
	 * @return
	 */
	public float getExposure() {
		return mExposure;
	}

	/**
	 * 明るさ調整の有効にするかどうかを設定
	 * 機体側のカメラ設定で明るさ調整してそれでもダメな時のみ使うこと
	 * @param enable
	 */
	public void enableBrightness(final boolean enable) {
		if (mEnableBrightness != enable) {
			mEnableBrightness = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectBrightness) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

	/**
	 * 明るさ調整が有効かどうかを取得
	 * @return
	 */
	public boolean enableBrightness() {
		return mEnableBrightness;
	}

	/**
	 * 明るさ調整
	 * @param brightness [-1.0,+1.0], 0だと無調整
	 */
	public void setBrightness(final float brightness) {
		if (mBrightness != brightness) {
			mBrightness = brightness;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectBrightness) {
						((MediaEffectBrightness)effect).setParameter(brightness);
						effect.setEnable(true);	// FIXME 無調整でも有効にする
					}
				}
			}
		}
	}

	/**
	 * 明るさ調整の設定値を取得
	 * @return
	 */
	public float getBrightness() {
		return mBrightness;
	}

	/**
	 * 彩度調整を有効にするかどうかを設定
	 * 機体側のカメラ設定で彩度調整してそれでもダメな時のみ使うこと
	 * @param enable
	 */
	public void enableSaturation(final boolean enable) {
		if (mEnableSaturation != enable) {
			mEnableSaturation = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectSaturate) {
						effect.setEnable(enable);
					}
//					if (effect instanceof MediaEffectSaturateGLES) {
//						effect.setEnable(enable);
//					}
				}
			}
		}
	}

	/**
	 * 彩度調整が有効かどうかを取得
	 * @return
	 */
	public boolean enableSaturation() {
		return mEnableSaturation;
	}

	/**
	 * 彩度調整
	 * @param saturation -1.0f〜+1.0f, -1.0ならグレースケール
	 */
	public void setSaturation(final float saturation) {
		final float sat = sat(saturation, -1.0f, +1.0f);
		if (mSaturation != sat) {
			mSaturation = sat;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectSaturate) {
						((MediaEffectSaturate)effect).setParameter(sat);
					}
//					if (effect instanceof MediaEffectSaturateGLES) {
//						((MediaEffectSaturateGLES)effect).setParameter(sat);
//					}
				}
			}
		}
	}

	/**
	 * 現在の彩度設定値
	 * @return
	 */
	public float getSaturation() {
		return mSaturation;
	}

	public void enablePosterize(final boolean enable) {
		if (mEnablePosterize != enable) {
//			if (DEBUG) Log.v(TAG, "enablePosterize:" + enable);
			mEnablePosterize = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectPosterize) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

	public boolean enablePosterize() {
		return mEnablePosterize;
	}

	/**
	 * ポスタライズ(階調化)
	 * @param posterize 1〜256, デフォルト10
	 */
	public void setPosterize(final float posterize) {
		final float post = sat((int)posterize, 1, 256);
		if (mPosterize != post) {
			mPosterize = post;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectPosterize) {
						((MediaEffectPosterize)effect).setParameter(post);
					}
				}
			}
		}
	}

	public float getPosterize() {
		return mPosterize;
	}

	/**
	 * 色抽出の有効/無効切り替え
	 * 基本的には常時有効やろな
	 * @param enable
	 */
	public void enableExtraction(final boolean enable) {
		if (mEnableExtraction != enable) {
			synchronized (mSync) {
				mEnableExtraction = enable;
			}
		}
	}

	/**
	 * 色抽出が有効かどうかを取得
	 * @return
	 */
	public boolean enableExtraction() {
		return mEnableExtraction;
	}

	/**
	 * 二値化のしきい値をセット
	 * 今は色抽出時にのみ使っている
	 * @param binarize_threshold
	 */
	public void setBinarizeThreshold(final float binarize_threshold) {
		if (mBinarizeThreshold != binarize_threshold) {
			synchronized (mSync) {
				mBinarizeThreshold = binarize_threshold;
				applyExtractionColor();
			}
		}
	}

	/**
	 * 二値化しきい値を取得
	 * @return
	 */
	public float binarizeThreshold() {
		return mBinarizeThreshold;
	}

//	public void smoothType(final int smooth_type) {
//		if (mSmoothType != smooth_type) {
//			synchronized (mSync) {
//				mSmoothType = smooth_type;
//				applySmooth(smooth_type);
//			}
//		}
//	}
//
//	private void applySmooth(final int smooth_type) {
//		if ((mProcessingTask != null) && (mProcessingTask.mSmooth != null)) {
//			switch (smooth_type) {
//			case 1:	// ガウシアン
//				mProcessingTask.mSmooth.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
//				break;
//			case 2:	// メディアン
//				// FIXME 未実装
//				break;
//			case 3:	// ブラー
//				// FIXME 未実装
//				break;
//			case 4:	// ダイレーション
//				// これはmProcessingTask.mSmoothを使わないのでここでは何もしない
//				break;
//			default:
//				mProcessingTask.mSmooth.setParameter(Texture2dProgram.KERNEL_NULL, 0.0f);
//				break;
//			}
//		}
//	}
//
//	public int smoothType() {
//		return mSmoothType;
//	}

//	/**
//	 * OpenGL|ESでのCannyエッジ検出の有効/無効を切り替え
//	 * @param enable
//	 */
//	public void enableCanny(final boolean enable) {
//		if (mEnableCanny != enable) {
//			synchronized (mSync) {
//				mEnableCanny = enable;
//			}
//		}
//	}
//
//	public boolean enableCanny() {
//		return mEnableCanny;
//	}

	/**
	 * 抽出色を映像中央部から取得して適用
	 * 色抽出が完了するまで呼び出し元をブロックするので注意
	 * @return
	 */
	public int[] requestUpdateExtractionColor() {
		final int[] temp = new int[6];
		synchronized (mSync) {
			requestUpdateExtractionColor = true;
			try {
				mSync.wait();
				System.arraycopy(EXTRACT_COLOR_HSV_LIMIT, 0, temp, 0, 6);
			} catch (final InterruptedException e) {
			}
		}
		return temp;
	}

	/**
	 * 抽出色を初期値にリセット
	 */
	public int[] resetExtractionColor() {
		final int[] temp = new int[6];
		synchronized (mSync) {
			System.arraycopy(COLOR_RANGES[COLOR_RANGE_IX], 0, EXTRACT_COLOR_HSV_LIMIT, 0, 6);
			applyExtractionColor();
			System.arraycopy(EXTRACT_COLOR_HSV_LIMIT, 0, temp, 0, 6);
		}
		return temp;
	}

	/**
	 * 抽出色の範囲をHSVで指定
	 * @param lowerH 色相(H)下限, 0-180
	 * @param upperH 色相(H)上限, 0-180
	 * @param lowerS 彩度(S)下限, 0-255
	 * @param upperS 彩度(S)上限, 0-255
	 * @param lowerV 明度(V)下限, 0-255
	 * @param upperV 明度(V)上限, 0-255
	 */
	public void setExtractionColor(
		final int lowerH, final int upperH,
		final int lowerS, final int upperS,
		final int lowerV, final int upperV) {

		synchronized (mSync) {
			EXTRACT_COLOR_HSV_LIMIT[0] = sat(lowerH, 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[1] = sat(upperH, 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[2] = sat(lowerS, 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[3] = sat(upperS, 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[4] = sat(lowerV, 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[5] = sat(upperV, 0, 255);
			applyExtractionColor();
		}
	}

	/**
	 * 抽出色を適用、mSyncをロックして呼び出すこと
	 */
	private void applyExtractionColor() {
		if ((mProcessingTask != null) && (mProcessingTask.mExtraction != null)) {
			// 指定色範囲を抽出(OpenGL|ES側)
			mProcessingTask.mExtraction.setParameter(
				EXTRACT_COLOR_HSV_LIMIT[0] / 180.0f,    // H(色相)
				EXTRACT_COLOR_HSV_LIMIT[1] / 180.0f,
				EXTRACT_COLOR_HSV_LIMIT[2] / 255.0f,    // S(彩度)
				EXTRACT_COLOR_HSV_LIMIT[3] / 255.0f,
				EXTRACT_COLOR_HSV_LIMIT[4] / 255.0f,    // V(明度)
				EXTRACT_COLOR_HSV_LIMIT[5] / 255.0f,
				0.00f, 0.00f, 0.00f,    // 抽出後加算値(HSV)
				mBinarizeThreshold);	// 2値化時のしきい値, 0なら2値化なし
		}
		// 指定色範囲を抽出(OpenCV)
		final int result = nativeSetExtractionColor(mNativePtr,
			EXTRACT_COLOR_HSV_LIMIT[0],
			EXTRACT_COLOR_HSV_LIMIT[1],
			EXTRACT_COLOR_HSV_LIMIT[2],
			EXTRACT_COLOR_HSV_LIMIT[3],
			EXTRACT_COLOR_HSV_LIMIT[4],
			EXTRACT_COLOR_HSV_LIMIT[5]);
		if (result != 0) {
			throw new IllegalStateException("nativeSetExtractionColor:result=" + result);
		}
	}

	public void enableNativeExtract(final boolean enable) {
		final int result = nativeSetEnableExtract(mNativePtr, enable ? 1 : 0);
		if (result != 0) {
			throw new IllegalStateException("nativeSetEnableExtract:result=" + result);
		}
	}

	public boolean enableNativeExtract() {
		final int result = nativeGetEnableExtract(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetEnableExtract:result=" + result);
		}
		return result != 0;
	}

	public void nativeSmoothType(final int smooth_type) {
		final int result = nativeSetSmooth(mNativePtr, smooth_type % 4);
		if (result != 0) {
			throw new IllegalStateException("nativeSetSmooth:result=" + result);
		}
	}

	public int nativeSmoothType() {
		final int result = nativeGetSmooth(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetSmooth:result=" + result);
		}
		return result;
	}

	public void enableNativeCanny(final boolean enable) {
		final int result = nativeSetEnableCanny(mNativePtr, enable ? 1 : 0);
		if (result != 0) {
			throw new IllegalStateException("nativeSetEnableCanny:result=" + result);
		}
	}

	public boolean enableNativeCanny() {
		final int result = nativeGetEnableCanny(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetEnableCanny:result=" + result);
		}
		return result != 0;
	}

	public void trapeziumRate(final float trapezium_rate) {
/*		FIXME MediaEffectTexProjectionはまだうまく動かない
		final float[] src = { 0.0f, 0.0f, 0.0f, 368.0f, 640.0f, 368.0f, 640.0f, 0.0f, };
		final float[] dst = {
			(trapezium_rate < 0 ? -trapezium_rate * 150.0f : 0.0f) + 0.0f, 0.0f,
			(trapezium_rate >= 0 ?  trapezium_rate * 150.0f : 0.0f) + 0.0f, 368.0f,
			(trapezium_rate >= 0 ?  -trapezium_rate * 150.0f : 0.0f) + 640.0f, 368.0f,
			(trapezium_rate < 0 ?  trapezium_rate * 150.0f : 0.0f) + 640.0f, 0.0f};
		synchronized (mSync) {
			for (final IEffect effect: mEffects) {
				if (effect instanceof MediaEffectTexProjection) {
					((MediaEffectTexProjection)effect).calcPerspectiveTransform(src, dst);
				}
			}
		} */
		final int result = nativeSetTrapeziumRate(mNativePtr,
			trapezium_rate < -0.01 ? trapezium_rate : (trapezium_rate > 0.01 ? trapezium_rate : 0.0));
		if (result != 0) {
			throw new IllegalStateException("nativeSetTrapeziumRate:result=" + result);
		}
	}

	public double trapeziumRate() {
		return nativeGetTrapeziumRate(mNativePtr);
	}

	public void setAreaLimit(final float min, final float max) {
		final int result = nativeSetAreaLimit(mNativePtr, min, max);
		if (result != 0) {
			throw new IllegalStateException("nativeSetAreaLimit:result=" + result);
		}
	}

	public void setAspectLimit(final float min) {
		final int result = nativeSetAspectLimit(mNativePtr, min);
		if (result != 0) {
			throw new IllegalStateException("nativeSetAspectLimit:result=" + result);
		}
	}

	public void setAreaErrLimit(final float limit1, final float limit2) {
		final int result = nativeSetAreaErrLimit(mNativePtr, limit1, limit2);
		if (result != 0) {
			throw new IllegalStateException("nativeSetAreaErrLimit:result=" + result);
		}
	}

	public int getMaxThinningLoop() {
		final int result = nativeGetMaxThinningLoop(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetMaxThinningLoop:result=" + result);
		}
		return result;
	}

	public void setMaxThinningLoop(final int max_loop) {
		final int result = nativeSetMaxThinningLoop(mNativePtr, max_loop);
		if (result != 0) {
			throw new IllegalStateException("nativeSetMaxThinningLoop:result=" + result);
		}
	}

	public boolean getFillInnerContour() {
		final int result = nativeGetFillInnerContour(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetFillInnerContour:result=" + result);
		}
		return result != 0;
	}

	public void setFillInnerContour(final boolean fill) {
		final int result = nativeSetFillInnerContour(mNativePtr, fill);
		if (result != 0) {
			throw new IllegalStateException("nativeSetFillInnerContour:result=" + result);
		}
	}
//================================================================================
	/**
	 * native側からの結果コールバック
	 * @param weakSelf
	 * @param type
	 * @param frame
	 * @param result
	 */
	private static void callFromNative(final WeakReference<ImageProcessor> weakSelf,
		final int type, final ByteBuffer frame, final float[] result) {
		final ImageProcessor self = weakSelf != null ? weakSelf.get() : null;
		if (self != null) {
			try {
				self.handleResult(type, result);
				if (frame != null) {
					self.handleOpenCVFrame(frame);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * native側からの結果コールバックの実際の処理
	 * @param result
	 */
	private void handleResult(final int type, final float[] result) {
		mResultFps.count();
		try {
			mCallback.onResult(type, result);
		} catch (final Exception e) {
		}
	}

	/**
	 * OpenCVで処理した映像を受け取った時の処理
	 * @param frame
	 */
	private void handleOpenCVFrame(final ByteBuffer frame) {
		mCallback.onFrame(frame);
	}

	private class ProcessingTask extends EglTask {
		/** 映像をテクスチャとして受け取る時のテクスチャ名(SurfaceTexture生成時/分配描画に使用) */
		private int mTexId;
		/** 映像を受け取るtsめのSurfaceTexture */
		private SurfaceTexture mSourceTexture;
		/** 映像を受け取るためのSurfaceTextureから取得したSurface */
		private Surface mSourceSurface;
		/** mSourceTextureのテクスチャ変換行列 */
		final float[] mTexMatrix = new float[16];
		/** ソース映像サイズ */
		private final int WIDTH, HEIGHT;
		/** 映像サイズ */
		private int mVideoWidth, mVideoHeight;
		// プレフィルタ処理用
		private EffectContext mEffectContext;
		private GLDrawer2D mSrcDrawer;
		private MediaEffectExtraction mExtraction;
//		private MediaEffectKernel mSmooth;
		private MediaEffectDilation mDilation;
		private MediaEffectErosion mErosion;
//		private MediaEffectGrayScale mGray;
//		private MediaEffectCanny mEdgeDetection;
		// 映像受け取り用
		private MediaSource mMediaSource;

		public ProcessingTask(final ImageProcessor parent, final int src_width, final int src_height, final int video_width, final int video_height) {
			super(null, 0);
			WIDTH = src_width;
			HEIGHT = src_height;
			mVideoWidth = video_width;
			mVideoHeight = video_height;
		}

		/** 映像受け取り用Surfaceを取得 */
		public Surface getSurface() {
			return mSourceSurface;
		}

		/** 映像受け取り用SurfaceTextureを取得 */
		public SurfaceTexture getSurfaceTexture() {
			return mSourceTexture;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onStart() {
			// ソース映像の描画用
			mSrcDrawer = new GLDrawer2D(true/*isOES*/);	// GL_TEXTURE_EXTERNAL_OESを使う
			flipMatrix(true);	// 上下入れ替え
			mTexId = GLHelper.initTex(ShaderConst.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mSourceTexture = new SurfaceTexture(mTexId);
			mSourceTexture.setDefaultBufferSize(WIDTH, HEIGHT);
			mSourceSurface = new Surface(mSourceTexture);
			if (BuildCheck.isLollipop()) {
				mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener, mAsyncHandler);
			} else {
				mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			}
//--------------------------------------------------------------------------------
			// プレフィルタの準備
			mEffectContext = EffectContext.createWithCurrentGlContext();
			synchronized (mSync) {
				// 自動調整(0〜1.0f, 0なら変化なし) これを有効にするとNewAPIで取得した映像がかなり暗くなってしまう
				final MediaEffectAutoFix autofix = new MediaEffectAutoFix(mEffectContext, mEnableAutoFix ? 1.0f : 0.0f);
				autofix.setEnable(true);	// FIXME 無効にするとGPUのドライバーがエラーを吐くので有効にして値を0にする
				mEffects.add(autofix);
				// 露出調整
				final MediaEffectExposure exposure = new MediaEffectExposure(mExposure);
//				exposure.setEnable(true);
				mEffects.add(exposure);
				// 彩度調整(-1.0f〜1.0f, -1.0fならグレースケール)
				final MediaEffectSaturate saturate = new MediaEffectSaturate(mEffectContext, mSaturation);
//				final MediaEffectSaturateGLES saturate = new MediaEffectSaturateGLES(mSaturation);
//				saturate.setEnable(mSaturation != 0.0f);
				mEffects.add(saturate);
				// 明るさ調整(0〜, 1.0fなら変化なし)
				final MediaEffectBrightness brightness = new MediaEffectBrightness(mBrightness);
				brightness.setEnable(true);
				mEffects.add(brightness);
/*				// コントラスト(0〜1.0f, 0なら変化なし)
				final MediaEffectContrast contrast = new MediaEffectContrast(mEffectContext, 1.0f);
				mEffects.add(contrast); */
/*				// ポスタライズ
				final MediaEffectPosterize posterize = new MediaEffectPosterize(mParent.mPosterize);
				posterize.setEnable(mParent.mEnablePosterize);
				mEffects.add(posterize); */
//				// エンボス
//				final MediaEffectEmboss emboss = new MediaEffectEmboss();
//				emboss.setParameter(2.0f);
//				mEffects.add(emboss);
//				// 台形補正 FIXME これはまだうまく動かない
//				final MediaEffectTexProjection proj = new MediaEffectTexProjection();
//				proj.setEnable(true);
//				mEffects.add(proj);
//--------------------------------------------------------------------------------
				// ここから下はプレフィルタじゃないよ
				// 色抽出
				mExtraction = new MediaEffectExtraction();
				applyExtractionColor();
//				// ノイズ除去(平滑化)
//				mSmooth = new MediaEffectKernel();
//				mSmooth.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
				// 膨張
				mDilation = new MediaEffectDilation(4);
				// 縮小
				mErosion = new MediaEffectErosion(1);
//				// グレースケール
//				mGray = new MediaEffectGrayScale(mEffectContext);
//				// Cannyエッジ検出フィルタ
//				mEdgeDetection = new MediaEffectCanny();
			}	// synchronized (mSync)
//--------------------------------------------------------------------------------
			handleResize(mVideoWidth, mVideoHeight);
			// native側の処理を開始
			nativeStart(mNativePtr, mVideoWidth, mVideoHeight);
			synchronized (mSync) {
				isProcessingRunning = true;
				mSync.notifyAll();
			}
			mResultFps.reset();
		}

		/**
		 * 諸般の事情で上下をひっくり返すために射影行列を計算
		 * @param verticalFlip
		 */
		private void flipMatrix(final boolean verticalFlip) {
			final float[] mat = new float[32];
			final float[] mvpMatrix = mSrcDrawer.getMvpMatrix();
			System.arraycopy(mvpMatrix, 0, mat, 16, 16);
			Matrix.setIdentityM(mat, 0);
			if (verticalFlip) {
				Matrix.scaleM(mat, 0, 1f, -1f, 1f);
			} else {
				Matrix.scaleM(mat, 0, -1f, 1f, 1f);
			}
			Matrix.multiplyMM(mvpMatrix, 0, mat, 0, mat, 16);
		}

		@Override
		protected void onStop() {
			synchronized (mSync) {
				isProcessingRunning = false;
				mSync.notifyAll();
			}
			makeCurrent();
			// native側の処理を停止させる
			nativeStop(mNativePtr);
			// 破棄処理
			mSourceSurface = null;
			if (mSourceTexture != null) {
				mSourceTexture.release();
				mSourceTexture = null;
			}
			if (mExtraction != null) {
				mExtraction.release();
				mExtraction = null;
			}
//			if (mSmooth != null) {
//				mSmooth.release();
//				mSmooth = null;
//			}
			if (mDilation != null) {
				mDilation.release();
				mDilation = null;
			}
			if (mErosion != null) {
				mErosion.release();
				mErosion = null;
			}
//			if (mGray != null) {
//				mGray.release();
//				mGray = null;
//			}
//			if (mEdgeDetection != null) {
//				mEdgeDetection.release();
//				mEdgeDetection = null;
//			}
			for (final IEffect effect: mEffects) {
				effect.release();
			}
			mEffects.clear();
			if (mMediaSource != null) {
				mMediaSource.release();
				mMediaSource = null;
			}
			if (mSrcDrawer != null) {
				mSrcDrawer.release();
				mSrcDrawer = null;
			}
		}

		@Override
		protected Object processRequest(final int request, final int arg1, final int arg2, final Object obj) {
			switch (request) {
			case REQUEST_DRAW:
				handleDraw();
				break;
			case REQUEST_UPDATE_SIZE:
				handleResize(arg1, arg2);
				break;
			}
			return null;
		}

		@Override
		protected boolean onError(final Exception e) {
			Log.w(TAG, e);
			return true;
		}

		/**
		 * 実際の描画処理(ワーカースレッド上で実行)
		 */
		private void handleDraw() {
			try {
				makeCurrent();
				mSourceTexture.updateTexImage();
				mSourceTexture.getTransformMatrix(mTexMatrix);
			} catch (final Exception e) {
				Log.e(TAG, "ProcessingTask#draw:thread id =" + Thread.currentThread().getId(), e);
				return;
			}
			// SurfaceTextureで受け取った画像をプレフィルター用にセット
			mMediaSource.setSource(mSrcDrawer, mTexId, mTexMatrix);
			// プレフィルター処理
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect.enabled()) {
						mMediaSource.apply(effect);
					}
				}
				// この時点での映像中心部の色をHSVで取得して色抽出に使えるようにする
				if (requestUpdateExtractionColor) {
					requestUpdateExtractionColor = false;
					try {
						updateExtractionColor();
					} finally {
						mSync.notifyAll();
					}
				}
				// 色抽出処理
				if (mEnableExtraction) {
					mMediaSource.apply(mExtraction);
					// 収縮処理
//					mMediaSource.apply(mErosion);
					// 膨張処理
					mMediaSource.apply(mDilation);
					// 収縮処理
					mMediaSource.apply(mErosion);
				}
//				// 平滑化処理
//				switch (mSmoothType) {
//				case 0: break;
//				case 4:
//					mMediaSource.apply(mDilation);
//					break;
//				default:
//					mMediaSource.apply(mSmooth);
//					break;
//				}
//				// エッジ検出処理
//				if (mEnableCanny) {
//					if (!mEnableExtraction || (mBinarizeThreshold == 0)) {
//						mMediaSource.apply(mGray);
//					}
//					mMediaSource.apply(mEdgeDetection);
//				}
			}
			// プレフィルター処理後の画像をNative側へ送る
			mMediaSource.getOutputTexture().bind();
			// Native側でglReadPixelsを使ってフレームバッファから画像データを取得する
			// Nexus6Pで直接glReadPixelsを使って読み込むと約5ミリ秒かかる
			// PBOのピンポンバッファを使うと約1/10の0.5ミリ秒で返ってくる
			nativeHandleFrame(mNativePtr, mVideoWidth, mVideoHeight, 0);
			mMediaSource.getOutputTexture().unbind();
			// 何も描画しないとハングアップする機種があるので塗りつぶす(と言っても1x1だから負荷は気にしなくて良い)
			makeCurrent();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
		}

		/**
		 * 映像の中心40x40ピクセルの値を読み込んでRGB→HSV変換して色抽出のパラメータを計算する
		 */
		private void updateExtractionColor() {
			final int n = 40 * 40;
			final int sz = n * 4;
			// 最終的にbyte[]に取り出すのでDirectBufferの代わりにbyte[]をwrapした方がトータル的にはいいかも
			final ByteBuffer temp = ByteBuffer.allocateDirect(sz);
			temp.order(ByteOrder.nativeOrder());
			mMediaSource.getOutputTexture().bind();
			// これは遅いのでここもPBOにした方がいいねんけど滅多に呼ばれへんからそのままでええやろ
			GLES20.glReadPixels(mVideoWidth / 2 - 20, mVideoHeight / 2 - 20, 40, 40, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, temp);
			mMediaSource.getOutputTexture().unbind();
			final byte[] rgba = new byte[sz];
			temp.clear();
			temp.get(rgba);
			// 加重平均を計算するための準備
			final float[] hsv = new float[3];
			final int[] h_cnt = new int[256];	// 0..255
			final int[] s_cnt = new int[256];	// 0..255
			final int[] v_cnt = new int[256];	// 0..255
			for (int i = 0; i < sz; i += 4) {
				Color.RGBToHSV(rgba[i] & 0xff, rgba[i + 1] & 0xff, rgba[i + 2] & 0xff, hsv);	// RGBAの順 => h[0,360], s[0,1], v[0,1]
				h_cnt[(int)(hsv[0] / 360f * 255) % 256]++;
				s_cnt[(int)(hsv[1] * 255) % 256]++;
				v_cnt[(int)(hsv[2] * 255) % 256]++;
			}
			// 荷重平均を計算
			float h = 0, s = 0, v = 0;
			for (int i = 0; i < 256; i++) {
				h += i * h_cnt[i];
				s += i * s_cnt[i];
				v += i * v_cnt[i];
			}
			h /= n;
			s /= n;
			v /= n;
			// 標準偏差を計算
			float h_sd = 0, s_sd = 0, v_sd = 0;
			for (int i = 0; i < 256; i++) {
				h_sd += (i - h) * (i - h) * h_cnt[i];
				s_sd += (i - s) * (i - s) * s_cnt[i];
				v_sd += (i - v) * (i - v) * v_cnt[i];
			}
			// 標準偏差で抽出色の範囲を設定する(H=2σ, S=3σ, V=6σ)
			// 赤付近はちょっと考えんとあかんねんけど
			h_sd = (float)Math.sqrt(h_sd / n); if (h_sd < 0.5f) h_sd= 1;	h_sd *= 2;	// 2σ
			s_sd = (float)Math.sqrt(s_sd / n); if (s_sd < 0.5f) s_sd= 1;	s_sd *= 3;	// 3σ
			v_sd = (float)Math.sqrt(v_sd / n); if (v_sd < 0.5f) v_sd= 1;	v_sd *= 6;	// 6σ
			// 抽出色をセット
			EXTRACT_COLOR_HSV_LIMIT[0] = sat((int)((h - h_sd) / 250 * 180), 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[1] = sat((int)((h + h_sd) / 250 * 180), 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[2] = sat((int)((s - s_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[3] = sat((int)((s + s_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[4] = sat((int)((v - v_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[5] = sat((int)((v + v_sd)), 0, 255);
			applyExtractionColor();
//			if (DEBUG) Log.v(TAG, String.format("AVE(%f,%f,%f),SD(%f,%f,%f)",
//				h / 250 * 180, s, v, h_sd  / 250 * 180, s_sd, v_sd));

//			if (DEBUG) Log.v(TAG, String.format("HSV(%d,%d,%d,%d,%d,%d)",
//				EXTRACT_COLOR_HSV_LIMIT[0], EXTRACT_COLOR_HSV_LIMIT[1],
//				EXTRACT_COLOR_HSV_LIMIT[2], EXTRACT_COLOR_HSV_LIMIT[3],
//				EXTRACT_COLOR_HSV_LIMIT[4], EXTRACT_COLOR_HSV_LIMIT[5]));
		}

		/**
		 * 映像サイズ変更処理(ワーカースレッド上で実行)
		 * @param width
		 * @param height
		 */
		private void handleResize(final int width, final int height) {
			mVideoWidth = width;
			mVideoHeight = height;
			// プレフィルタ用
			if (mMediaSource != null) {
				mMediaSource.resize(width, height);
			} else {
				mMediaSource = new MediaSource(width, height);
			}
			for (final IEffect effect: mEffects) {
				effect.resize(width, height);
			}
			mExtraction.resize(width, height);
//			mSmooth.resize(width, height);
//			mGray.resize(width, height);
			mDilation.resize(width, height);
			mErosion.resize(width, height);
//			mEdgeDetection.resize(width, height);
		}

		/**
		 * TextureSurfaceで映像を受け取った際のコールバックリスナー
		 */
		private final SurfaceTexture.OnFrameAvailableListener
			mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

			@Override
			public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
				// 前の映像フレームが残っていたらクリアする
				removeRequest(REQUEST_DRAW);
				// 新しく処理要求する
				offer(REQUEST_DRAW);
			}
		};
	}

	/**
	 * 飽和計算(int)
	 * @param v
	 * @param min
	 * @param max
	 * @return
	 */
	public static final int sat(final int v, final int min, final int max) {
		return v <= min ? min : (v >= max ? max : v);
	}

	/**
	 * 飽和計算(float)
	 * @param v
	 * @param min
	 * @param max
	 * @return
	 */
	public static final float sat(final float v, final float min, final float max) {
		return v <= min ? min : (v >= max ? max : v);
	}

	private static boolean isInit;
	private static native void nativeClassInit();
	static {
		if (!isInit) {
			System.loadLibrary("gnustl_shared");
			System.loadLibrary("common");
			System.loadLibrary("opencv_java3");
			System.loadLibrary("imageproc");
			nativeClassInit();
			isInit = true;
		}
	}

	private native long nativeCreate(final WeakReference<ImageProcessor> weakSelf);
	private native void nativeRelease(final long id_native);

	private static native int nativeStart(final long id_native, final int width, final int height);
	private static native int nativeStop(final long id_native);
	private static native int nativeHandleFrame(final long id_native, final int width, final int height, final int tex_name);
	private static native int nativeSetResultFrameType(final long id_native, final int showDetects);
	private static native int nativeGetResultFrameType(final long id_native);
	private static native int nativeSetExtractionColor(final long id_native,
		final int lowerH, final int upperH,
		final int lowerS, final int upperS,
		final int lowerV, final int upperV);
	private static native int nativeSetEnableExtract(final long id_native, final int enable);
	private static native int nativeGetEnableExtract(final long id_native);
	private static native int nativeSetSmooth(final long id_native, final int smooth_type);
	private static native int nativeGetSmooth(final long id_native);
	private static native int nativeSetEnableCanny(final long id_native, final int enable);
	private static native int nativeGetEnableCanny(final long id_native);
	private static native int nativeSetTrapeziumRate(final long id_native, final double trapeziumRate);
	private static native double nativeGetTrapeziumRate(final long id_native);
	private static native int nativeSetAreaLimit(final long id_native, final float min, final float max);
	private static native int nativeSetAspectLimit(final long id_native, final float min);
	private static native int nativeSetAreaErrLimit(final long id_native, final float limit1, final float limit2);
	private static native int nativeGetMaxThinningLoop(final long id_native);
	private static native int nativeSetMaxThinningLoop(final long id_native, final int max_loop);
	private static native int nativeGetFillInnerContour(final long id_native);
	private static native int nativeSetFillInnerContour(final long id_native, final boolean fill);
}
