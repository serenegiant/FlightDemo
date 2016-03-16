package com.serenegiant.opencv;

import android.graphics.SurfaceTexture;
import android.media.effect.EffectContext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.math.Vector;
import com.serenegiant.mediaeffect.IEffect;
import com.serenegiant.mediaeffect.MediaEffectAutoFix;
import com.serenegiant.mediaeffect.MediaEffectBrightness;
import com.serenegiant.mediaeffect.MediaEffectCanny;
import com.serenegiant.mediaeffect.MediaEffectDilation;
import com.serenegiant.mediaeffect.MediaEffectExposure;
import com.serenegiant.mediaeffect.MediaEffectExtraction;
import com.serenegiant.mediaeffect.MediaEffectGrayScale;
import com.serenegiant.mediaeffect.MediaEffectKernel;
import com.serenegiant.mediaeffect.MediaEffectPosterize;
import com.serenegiant.mediaeffect.MediaEffectSaturate;
import com.serenegiant.mediaeffect.MediaSource;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ImageProcessor {
	private static final boolean DEBUG = true; // FIXME 実働時はfalseにすること
	private static final String TAG = ImageProcessor.class.getSimpleName();

	private static final int VIDEO_WIDTH = 640;
	private static final int VIDEO_HEIGHT = 368;

	private static final int REQUEST_DRAW = 1;
	private static final int REQUEST_UPDATE_SIZE = 2;

	public interface ImageProcessorCallback {
		public void onFrame(final ByteBuffer frame);
		public void onResult(final int type, final float[] result);
	}

	private final Object mSync = new Object();
	private final ImageProcessorCallback mCallback;
	private List<IEffect> mEffects = new ArrayList<IEffect>();
	private volatile boolean isProcessingRunning;
	private ProcessingTask mProcessingTask;

	private boolean mEnableExposure;
	private float mExposure;
	private boolean mEnableBrightness;
	private float mBrightness;
	private boolean mEnableSaturation;
	private float mSaturation;
	private boolean mEnablePosterize;
	private float mPosterize;	// [1,256], デフォルト10
	private boolean mEnableExtraction;
	private int mSmoothType = 0;
	private float mBinarizeThreshold = 0.5f;
	private boolean mEnableCanny;
	private static final int[][] COLOR_RANGES = {
		{0, 180, 0, 50, 120, 255},		// 白色
		{25, 35, 120, 130, 180, 200},	// 黄色...蛍光色はこれだとだめみたい
	};
	private int COLOR_RANGE_IX = 0;
	protected final int[] EXTRACT_COLOR_HSV_LIMIT = COLOR_RANGES[COLOR_RANGE_IX];


	private volatile boolean requestUpdateExtractionColor;

	/** native側のインスタンスポインタ, 名前を変えたりしちゃダメ */
	private long mNativePtr;

	public ImageProcessor(final ImageProcessorCallback callback) {
		if (DEBUG) Log.v(TAG, "コンストラクタ");
		if (callback == null) {
			throw new NullPointerException("callback should not be null");
		}
		mCallback = callback;
		mNativePtr = nativeCreate(new WeakReference<ImageProcessor>(this));
	}

	public void start() {
		if (DEBUG) Log.v(TAG, "start:");
		if (mProcessingTask == null) {
			mProcessingTask = new ProcessingTask(this);
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

	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
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
		if (DEBUG) Log.v(TAG, "release");
		stop();
		nativeRelease(mNativePtr);
		if (DEBUG) Log.v(TAG, "release:finished");
	}

	/** 映像受け取り用Surfaceを取得 */
	public Surface getSurface() {
		return mProcessingTask.getSurface();
	}

	/** 映像受け取り用SurfaceTextureを取得 */
	public SurfaceTexture getSurfaceTexture() {
		return mProcessingTask.getSurfaceTexture();
	}

//================================================================================
	public static final int RESULT_FRAME_TYPE_SRC = 0;
	public static final int RESULT_FRAME_TYPE_DST = 1;
	public static final int RESULT_FRAME_TYPE_SRC_LINE = 2;
	public static final int RESULT_FRAME_TYPE_DST_LINE = 3;

	public void setResultFrameType(final int result_frame_type) {
		final int result = nativeSetResultFrameType(mNativePtr, result_frame_type);
		if (result != 0) {
			throw new IllegalStateException("nativeSetResultFrameType:result=" + result);
		}
	}

	public int getResultFrameType() {
		final int result = nativeGetResultFrameType(mNativePtr);
		if (result < 0) {
			throw new IllegalStateException("nativeGetResultFrameType:result=" + result);
		}
		return result;
	}

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

	public float getExposure() {
		return mExposure;
	}

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
					}
				}
			}
		}
	}

	public float getBrightness() {
		return mBrightness;
	}

	public void enableSaturation(final boolean enable) {
		if (mEnableSaturation != enable) {
			mEnableSaturation = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectSaturate) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

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
				}
			}
		}
	}

	public float getSaturation() {
		return mSaturation;
	}

	public void enablePosterize(final boolean enable) {
		if (mEnablePosterize != enable) {
			if (DEBUG) Log.v(TAG, "enablePosterize:" + enable);
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
	 * OpenGL|ESでの色抽出の有効/無効切り替え
	 * @param enable
	 */
	public void enableExtraction(final boolean enable) {
		if (mEnableExtraction != enable) {
			if (DEBUG) Log.v(TAG, "setExtraction:" + enable);
			synchronized (mSync) {
				mEnableExtraction = enable;
			}
		}
	}

	public boolean enableExtraction() {
		return mEnableExtraction;
	}

	public void setBinarizeThreshold(final float binarize_threshold) {
		if (mBinarizeThreshold != binarize_threshold) {
			if (DEBUG) Log.v(TAG, "setBinarizeThreshold:" + binarize_threshold);
			synchronized (mSync) {
				mBinarizeThreshold = binarize_threshold;
				applyExtractionColor();
			}
		}
	}

	public float binarizeThreshold() {
		return mBinarizeThreshold;
	}

	public void smoothType(final int smooth_type) {
		if (mSmoothType != smooth_type) {
			synchronized (mSync) {
				mSmoothType = smooth_type;
				applySmooth(smooth_type);
			}
		}
	}

	private void applySmooth(final int smooth_type) {
		if ((mProcessingTask != null) && (mProcessingTask.mSmooth != null)) {
			switch (smooth_type) {
			case 1:	// ガウシアン
				mProcessingTask.mSmooth.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
				break;
			case 2:	// メディアン
				// FIXME 未実装
				break;
			case 3:	// ブラー
				// FIXME 未実装
				break;
			case 4:	// ダイレーション
				// これはmProcessingTask.mSmootを使わないのでここでは何もしない
				break;
			default:
				mProcessingTask.mSmooth.setParameter(Texture2dProgram.KERNEL_NULL, 0.0f);
				break;
			}
		}
	}

	public int smoothType() {
		return mSmoothType;
	}

	/**
	 * OpenGL|ESでのCannyエッジ検出の有効/無効を切り替え
	 * @param enable
	 */
	public void enableCanny(final boolean enable) {
		if (mEnableCanny != enable) {
			if (DEBUG) Log.v(TAG, "enableCanny:" + enable);
			synchronized (mSync) {
				mEnableCanny = enable;
			}
		}
	}

	public boolean enableCanny() {
		return mEnableCanny;
	}

	/**
	 * 抽出色を映像中央部から取得して適用
	 * @return
	 */
	public int[] requestUpdateExtractionColor() {
		if (DEBUG) Log.v(TAG, "requestUpdateExtractionColor:");
		final int[] temp = new int[6];
		synchronized (mSync) {
			requestUpdateExtractionColor = true;
			try {
				mSync.wait();
				System.arraycopy(EXTRACT_COLOR_HSV_LIMIT, 0, temp, 0, 6);
			} catch (InterruptedException e) {
			}
		}
		return temp;
	}

	/**
	 * 抽出色を初期値にリセット
	 */
	public void resetExtractionColor() {
		synchronized (mSync) {
			System.arraycopy(COLOR_RANGES[COLOR_RANGE_IX], 0, EXTRACT_COLOR_HSV_LIMIT, 0, 6);
			applyExtractionColor();
		}
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
				EXTRACT_COLOR_HSV_LIMIT[0] / 180.0f,    // H(色相) 制限なし(0-180),
				EXTRACT_COLOR_HSV_LIMIT[1] / 180.0f,
				EXTRACT_COLOR_HSV_LIMIT[2] / 255.0f,    // S(彩度) 0-10,
				EXTRACT_COLOR_HSV_LIMIT[3] / 255.0f,
				EXTRACT_COLOR_HSV_LIMIT[4] / 255.0f,    // V(明度) 200-255,
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

//================================================================================
	/**
	 * native側からの結果コールバック
	 * @param weakSelf
	 * @param type
	 * @param frame
	 * @param result
	 */
	private static void callFromNative(final WeakReference<ImageProcessor> weakSelf, final int type, final ByteBuffer frame, final float[] result) {
//		if (DEBUG) Log.v(TAG, "callFromNative");
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
//		if (DEBUG) Log.v(TAG, "callFromNative:finished");
	}

	/**
	 * native側からの結果コールバックの実際の処理
	 * @param result
	 */
	private void handleResult(final int type, final float[] result) {
//		if (DEBUG) Log.v(TAG, "handleResult");
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
//		if (DEBUG) Log.v(TAG, "handleOpenCVFrame");
		mCallback.onFrame(frame);
	}

	private class ProcessingTask extends EglTask {
		/** 映像をテクスチャとして受け取る時のテクスチャ名(SurfaceTexture生成時/分配描画に使用) */
		private int mTexId;
		/** 映像を受け取るtsめのSurfaceTexture */
		private SurfaceTexture mSourceTexture;
		/** mMasterTextureのテクスチャ変換行列 */
		final float[] mTexMatrix = new float[16];
		/** 映像を受け取るためのSurfaceTextureから取得したSurface */
		private Surface mSourceSurface;
		/** 映像サイズ */
		private int mVideoWidth, mVideoHeight;
		// プレフィルタ処理用
		private EffectContext mEffectContext;
		private FullFrameRect mSrcDrawer;
		private MediaEffectExtraction mExtraction;
		private MediaEffectKernel mSmooth;
		private MediaEffectDilation mDilation;
		private MediaEffectGrayScale mGray;
		private MediaEffectCanny mEdgeDetection;
		// 映像受け取り用
		private MediaSource mMediaSource;

		public ProcessingTask(final ImageProcessor parent) {
			super(null, 0);
			mVideoWidth = VIDEO_WIDTH;
			mVideoHeight = VIDEO_HEIGHT;
		}

		/** 映像受け取り用Surfaceを取得 */
		public Surface getSurface() {
			if (DEBUG) Log.v(TAG, "getSurface:" + mSourceSurface);
			return mSourceSurface;
		}

		/** 映像受け取り用SurfaceTextureを取得 */
		public SurfaceTexture getSurfaceTexture() {
			if (DEBUG) Log.v(TAG, "ProcessingTask:getSurfaceTexture:" + mSourceTexture);
			return mSourceTexture;
		}

		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:");
			// ソース映像の描画用
			mSrcDrawer = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT_FILT3x3));
			mSrcDrawer.getProgram().setTexSize(mVideoWidth, mVideoHeight);
			mSrcDrawer.flipMatrix(true);	// 上下入れ替え
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_EMBOSS, 0.5f);		// エンボス
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SOBEL_H, 0.0f);		// ソ-ベル(エッジ検出, 1次微分)
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SOBEL2_H, 0.0f);		// ソ-ベル2(エッジ検出, 1次微分)
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_EDGE_DETECT, 0.0f);	// エッジ検出
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SHARPNESS, 0.0f);		// シャープ
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SMOOTH, 0.0f);		// 移動平均(平滑化)
			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);		// ガウシアン(平滑化)
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_BRIGHTEN, 0.0f);		//
//			mSrcDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_LAPLACIAN, 0.0f);		// ラプラシアン(2次微分)
			mTexId = mSrcDrawer.createTextureObject();
			mSourceTexture = new SurfaceTexture(mTexId);
			mSourceSurface = new Surface(mSourceTexture);
			mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
//--------------------------------------------------------------------------------
			// プレフィルタの準備
			mEffectContext = EffectContext.createWithCurrentGlContext();
			synchronized (mSync) {
				// 自動調整(0〜1.0f, 0なら変化なし)
				final MediaEffectAutoFix autofix = new MediaEffectAutoFix(mEffectContext, 1.0f);
				mEffects.add(autofix);
				// 露出調整
				final MediaEffectExposure exposure = new MediaEffectExposure(mExposure);
				exposure.setEnable(true);
				mEffects.add(exposure);
				// 彩度調整(-1.0f〜1.0f, -1.0fならグレースケール)
				final MediaEffectSaturate saturate = new MediaEffectSaturate(mEffectContext, mSaturation);
				saturate.setEnable(true);
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
//--------------------------------------------------------------------------------
				// 色抽出とCannyエッジ検出はプレフィルタじゃないよ
				// 色抽出(白色)
				mExtraction = new MediaEffectExtraction();
				applyExtractionColor();
				// ノイズ除去(平滑化)
				mSmooth = new MediaEffectKernel();
				mSmooth.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
				mDilation = new MediaEffectDilation(4);
				// グレースケール
				mGray = new MediaEffectGrayScale(mEffectContext);
				// Cannyエッジ検出フィルタ
				mEdgeDetection = new MediaEffectCanny();
			}	// synchronized (mSync)
//--------------------------------------------------------------------------------
			handleResize(mVideoWidth, mVideoHeight);
			// native側の処理を開始
			nativeStart(mNativePtr, mVideoWidth, mVideoHeight);
			synchronized (mSync) {
				isProcessingRunning = true;
				mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:finished");
		}

		@Override
		protected void onStop() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStop");
			synchronized (mSync) {
				isProcessingRunning = false;
				mSync.notifyAll();
			}
			// native側の処理を停止させる
			nativeStop(mNativePtr);
			// 破棄処理
			makeCurrent();
			mSourceSurface = null;
			if (mSourceTexture != null) {
				mSourceTexture.release();
				mSourceTexture = null;
			}
			if (mExtraction != null) {
				mExtraction.release();
				mExtraction = null;
			}
			if (mSmooth != null) {
				mSmooth.release();
				mSmooth = null;
			}
			if (mDilation != null) {
				mDilation.release();
				mDilation = null;
			}
			if (mGray != null) {
				mGray.release();
				mGray = null;
			}
			if (mEdgeDetection != null) {
				mEdgeDetection.release();
				mEdgeDetection = null;
			}
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
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStop:finished");
		}

		@Override
		protected boolean processRequest(final int request, final int arg1, final int arg2, final Object obj) {
			switch (request) {
			case REQUEST_DRAW:
				handleDraw();
				break;
			case REQUEST_UPDATE_SIZE:
				handleResize(arg1, arg2);
				break;
			}
			return false;
		}

		@Override
		protected boolean onError(final Exception e) {
			if (DEBUG) Log.w(TAG, e);
			return true;
		}

		/**
		 * 実際の描画処理(ワーカースレッド上で実行)
		 */
		private void handleDraw() {
//			if (DEBUG) Log.v(TAG, "ProcessingTask#handleDraw:");
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
					updateExtractionColor();
					mSync.notifyAll();
				}
				// 色抽出処理
				if (mEnableExtraction) {
					mMediaSource.apply(mExtraction);
				}
				// 平滑化処理
				if (mSmoothType == 4) {
					mMediaSource.apply(mDilation);
				} else if (mSmoothType != 0) {
					mMediaSource.apply(mSmooth);
				}
				// エッジ検出処理
				if (mEnableCanny) {
					if (!mEnableExtraction || (mBinarizeThreshold == 0)) {
						mMediaSource.apply(mGray);
					}
					mMediaSource.apply(mEdgeDetection);
				}
			}
			// プレフィルター処理後の画像をNative側へ送る
			mMediaSource.getOutputTexture().bind();
			// Native側でglReadPixelsを使ってフレームバッファから画像データを取得する
			// Nexus6Pで直接glReadPixelsを使って読み込むと約5ミリ秒かかる
			// PBOのピンポンバッファを使うと約1/10の0.5ミリ秒で返ってくる
			nativeHandleFrame(mNativePtr, mVideoWidth, mVideoHeight, 0);
			mMediaSource.getOutputTexture().unbind();
			// 何も描画しないとハングアップする機種があるので塗りつぶす(と言っても1x1だから気にしなくて良い?)
			makeCurrent();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
//			if (DEBUG) Log.v(TAG, "ProcessingTask#handleDraw:finished");
		}

		private void updateExtractionColor() {
			final int n = 40 * 40;
			final int sz = n * 4;
			final ByteBuffer temp = ByteBuffer.allocateDirect(sz);
			temp.order(ByteOrder.nativeOrder());
			mMediaSource.getOutputTexture().bind();
			GLES20.glReadPixels(mVideoWidth / 2 - 20, mVideoHeight / 2 - 20, 40, 40, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, temp);
			mMediaSource.getOutputTexture().unbind();
			final byte[] rgba = new byte[sz];
			temp.clear();
			temp.get(rgba);
			//
			final float[] hsv = new float[3];
			final int[] h_cnt = new int[256];	// 0..255
			final int[] s_cnt = new int[256];	// 0..255
			final int[] v_cnt = new int[256];	// 0..255
			for (int i = 0; i < sz; i += 4) {
				rgb2hsv(rgba[i], rgba[i+1], rgba[i+2], hsv);	// RGBAの順 => h[0,360], s[0,1], v[0,1]
				h_cnt[(int)(hsv[0] / 360f * 255) % 256]++;
				s_cnt[(int)(hsv[1] * 255) % 256]++;
				v_cnt[(int)(hsv[2] * 255) % 256]++;
			}
			float h = 0, s = 0, v = 0;
			for (int i = 0; i < 256; i++) {
				h += i * h_cnt[i];
				s += i * s_cnt[i];
				v += i * v_cnt[i];
			}
			// 平均
			h /= n;
			s /= n;
			v /= n;
			float h_sd = 0, s_sd = 0, v_sd = 0;
			for (int i = 0; i < 256; i++) {
				h_sd += (i - h) * (i - h) * h_cnt[i];
				s_sd += (i - s) * (i - s) * s_cnt[i];
				v_sd += (i - v) * (i - v) * v_cnt[i];
			}
			// 標準偏差2σ
			h_sd = (float)Math.sqrt(h_sd / n); if (h_sd < 0.5f) h_sd= 1;	h_sd *= 2;	// 2σ
			s_sd = (float)Math.sqrt(s_sd / n); if (s_sd < 0.5f) s_sd= 1;	s_sd *= 3;	// 3σ
			v_sd = (float)Math.sqrt(v_sd / n); if (v_sd < 0.5f) v_sd= 1;	v_sd *= 6;	// 6σ

			EXTRACT_COLOR_HSV_LIMIT[0] = sat((int)((h - h_sd) / 250 * 180), 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[1] = sat((int)((h + h_sd) / 250 * 180), 0, 180);
			EXTRACT_COLOR_HSV_LIMIT[2] = sat((int)((s - s_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[3] = sat((int)((s + s_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[4] = sat((int)((v - v_sd)), 0, 255);
			EXTRACT_COLOR_HSV_LIMIT[5] = sat((int)((v + v_sd)), 0, 255);
			applyExtractionColor();
			if (DEBUG) Log.v(TAG, String.format("AVE(%f,%f,%f),SD(%f,%f,%f)",
				h / 250 * 180, s, v, h_sd  / 250 * 180, s_sd, v_sd));

			if (DEBUG) Log.v(TAG, String.format("HSV(%d,%d,%d,%d,%d,%d)",
				EXTRACT_COLOR_HSV_LIMIT[0], EXTRACT_COLOR_HSV_LIMIT[1],
				EXTRACT_COLOR_HSV_LIMIT[2], EXTRACT_COLOR_HSV_LIMIT[3],
				EXTRACT_COLOR_HSV_LIMIT[4], EXTRACT_COLOR_HSV_LIMIT[5]));
		}

		private void rgb2hsv(final byte _r, final byte _g, final byte _b, final float[] hsv) {
			final float b = sat(((_b & 0xff)) / 255.0f, 0, 1);
			final float g = sat(((_g & 0xff)) / 255.0f, 0, 1);
			final float r = sat((_r & 0xff) / 255.0f, 0, 1);
			final float rgb_min = Math.min(Math.min(r, g), b);
			final float rgb_max = Math.max(Math.max(r, g), b);
			hsv[0] = hsv[1] = hsv[2] = 0.0f;
			if (rgb_max != rgb_min) {
				// 色相Hの計算[0,360]
				if (rgb_min == b) {
					hsv[0] = (60 * (g - r) / (rgb_max - rgb_min) + 60);
				} else if (rgb_min == r) {
					hsv[0] = (60 * (b - g) / (rgb_max - rgb_min) + 180);
				} else if (rgb_min == g) {
					hsv[0] = (60 * (r - b) / (rgb_max - rgb_min) + 300);
				}
			}
			// 彩度S, 明度Vの計算[0.0f,1.0f]
			hsv[1] = rgb_max - rgb_min;
			hsv[2] = rgb_max;
		}

		/**
		 * 製造サイズ変更処理(ワーカースレッド上で実行)
		 * @param width
		 * @param height
		 */
		private void handleResize(final int width, final int height) {
			if (DEBUG) Log.v(TAG, String.format("ProcessingTask#handleResize:(%d,%d)", width, height));
			mVideoWidth = width;
			mVideoHeight = height;
			mSourceTexture.setDefaultBufferSize(width, height);
			mSrcDrawer.getProgram().setTexSize(width, height);
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
			mSmooth.resize(width, height);
			mGray.resize(width, height);
			mEdgeDetection.resize(width, height);
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

	private static final int sat(final int v, final int min, final int max) {
		return v <= min ? min : (v >= max ? max : v);
	}

	private static final float sat(final float v, final float min, final float max) {
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
}
