package com.serenegiant.opencv;

import android.graphics.SurfaceTexture;
import android.media.effect.EffectContext;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.mediaeffect.IEffect;
import com.serenegiant.mediaeffect.MediaEffectAutoFix;
import com.serenegiant.mediaeffect.MediaEffectBrightness;
import com.serenegiant.mediaeffect.MediaEffectCanny;
import com.serenegiant.mediaeffect.MediaEffectExposure;
import com.serenegiant.mediaeffect.MediaEffectExtraction;
import com.serenegiant.mediaeffect.MediaEffectSaturate;
import com.serenegiant.mediaeffect.MediaSource;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
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
	private boolean mEnableExtraction;
	private boolean mEnableCanny;
	protected final int[] EXTRACT_COLOR_HSV_LIMIT = new int[] {0, 180, 0, 50, 120, 255};

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
	public void enableExposure(final boolean enable) {
		if (mEnableExposure != enable) {
			if (DEBUG) Log.v(TAG, "enableExposure:");
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
//				enableExposure(exp != 0);
			}
		}
	}

	public float getExposure() {
		return mExposure;
	}

	public void enableBrightness(final boolean enable) {
		if (mEnableBrightness != enable) {
			if (DEBUG) Log.v(TAG, "enableBrightness:" + enable);
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
//				enableBrightness(brightness != 0.0f);
			}
		}
	}

	public float getBrightness() {
		return mBrightness;
	}

	public void enableSaturation(final boolean enable) {
		if (mEnableSaturation != enable) {
			if (DEBUG) Log.v(TAG, "enableSaturation:" + enable);
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
//				enableSaturation(sat != 0.0f);
			}
		}
	}

	public float getSaturation() {
		return mSaturation;
	}

	/**
	 * OpenGL|ESでの色抽出の有効/無効切り替え
	 * @param enable
	 */
	public void enableExtraction(final boolean enable) {
		if (mEnableExtraction != enable) {
			if (DEBUG) Log.v(TAG, "setExtraction:" + enable);
			mEnableExtraction = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectExtraction) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

	public boolean enableExtraction() {
		return mEnableExtraction;
	}

	/**
	 * OpenGL|ESでのCannyエッジ検出の有効/無効を切り替え
	 * @param enable
	 */
	public void enableCanny(final boolean enable) {
		if (mEnableCanny != enable) {
			if (DEBUG) Log.v(TAG, "enableCanny:" + enable);
			mEnableCanny = enable;
			synchronized (mSync) {
				for (final IEffect effect: mEffects) {
					if (effect instanceof MediaEffectCanny) {
						effect.setEnable(enable);
					}
				}
			}
		}
	}

	public boolean enableCanny() {
		return mEnableCanny;
	}

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

		EXTRACT_COLOR_HSV_LIMIT[0] = sat(lowerH, 0, 180);
		EXTRACT_COLOR_HSV_LIMIT[1] = sat(upperH, 0, 180);
		EXTRACT_COLOR_HSV_LIMIT[2] = sat(lowerS, 0, 255);
		EXTRACT_COLOR_HSV_LIMIT[3] = sat(upperS, 0, 255);
		EXTRACT_COLOR_HSV_LIMIT[4] = sat(lowerV, 0, 255);
		EXTRACT_COLOR_HSV_LIMIT[5] = sat(upperV, 0, 255);
		synchronized (mSync) {
			for (final IEffect effect: mEffects) {
				if (effect instanceof MediaEffectExtraction) {
					((MediaEffectExtraction)effect).setParameter(
						EXTRACT_COLOR_HSV_LIMIT[0] / 180.0f,	// 色相H 制限なし(0-180),
						EXTRACT_COLOR_HSV_LIMIT[1] / 180.0f,
						EXTRACT_COLOR_HSV_LIMIT[2] / 255.0f,	// 彩度S 0-10,
						EXTRACT_COLOR_HSV_LIMIT[3] / 255.0f,
						EXTRACT_COLOR_HSV_LIMIT[4] / 255.0f,	// 明度V 200-255,
						EXTRACT_COLOR_HSV_LIMIT[5] / 255.0f,
						0.0f
					);
				}
			}
		}
		final int result = nativeSetExtractionColor(mNativePtr, lowerH, upperH, lowerS, upperS, lowerV, upperV);
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
				self.handleResult(result);
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
	private void handleResult(final float[] result) {
//		if (DEBUG) Log.v(TAG, "handleResult");
	}

	/**
	 * OpenCVで処理した映像を受け取った時の処理
	 * @param frame
	 */
	private void handleOpenCVFrame(final ByteBuffer frame) {
//		if (DEBUG) Log.v(TAG, "handleOpenCVFrame");
		mCallback.onFrame(frame);
	}

	private static class ProcessingTask extends EglTask {
		private final ImageProcessor mParent;
		/** コピー */
		private final long mNativePtr;
		private final Object mSync;
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
		// 映像受け取り用
		private MediaSource mMediaSource;
		// 映像効果リスト
		private final List<IEffect> mEffects;

		public ProcessingTask(final ImageProcessor parent) {
			super(null, 0);
			mParent = parent;
			mSync = parent.mSync;
			mEffects = parent.mEffects;
			mNativePtr = mParent.mNativePtr;
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
				final MediaEffectExposure exposure = new MediaEffectExposure(mParent.mExposure);
				exposure.setEnable(true);
				mEffects.add(exposure);
				// 彩度調整(-1.0f〜1.0f, -1.0fならグレースケール)
				final MediaEffectSaturate saturate = new MediaEffectSaturate(mEffectContext, mParent.mSaturation);
				saturate.setEnable(true);
				mEffects.add(saturate);
//				// 明るさ調整
//				if (mParent.mEnableEmphasis) {
//					final MediaEffectExtraction adjust = new MediaEffectExtraction();
//					adjust.setParameter(    // 抽出なし
//						0.0f, 1.0f,			// H(色相) 制限なし(0-360),
//						0.0f, 1.0f,			// S(彩度) 0-10,
//						0.0f, 1.0f,			// V(明度) 200-255,
//						0.0f, 0.0f, 0.1f,	// 抽出後加算値(HSV)
//						0.0f);				// 2値化時のしきい値, 0なら2値化なし
//					mEffects.add(adjust);
//				}
				// 明るさ調整(0〜, 1.0fなら変化なし)
				final MediaEffectBrightness brightness = new MediaEffectBrightness(mParent.mBrightness);
				brightness.setEnable(true);
				mEffects.add(brightness);
/*				// コントラスト(0〜1.0f, 0なら変化なし)
				final MediaEffectContrast contrast = new MediaEffectContrast(mEffectContext, 1.0f);
				mEffects.add(contrast); */
				// 色抽出(白色)
				final MediaEffectExtraction extraction = new MediaEffectExtraction();
				extraction.setParameter(    // 白色を抽出
					mParent.EXTRACT_COLOR_HSV_LIMIT[0] / 180.0f,	// H(色相) 制限なし(0-180),
					mParent.EXTRACT_COLOR_HSV_LIMIT[1] / 180.0f,
					mParent.EXTRACT_COLOR_HSV_LIMIT[2] / 255.0f,	// S(彩度) 0-10,
					mParent.EXTRACT_COLOR_HSV_LIMIT[3] / 255.0f,
					mParent.EXTRACT_COLOR_HSV_LIMIT[4] / 255.0f,	// V(明度) 200-255,
					mParent.EXTRACT_COLOR_HSV_LIMIT[5] / 255.0f,
					0.00f, 0.00f, 0.00f,    // 抽出後加算値(HSV)
					0.00f);					// 2値化時のしきい値, 0なら2値化なし
				extraction.setEnable(mParent.mEnableExtraction);
				mEffects.add(extraction);
/*				// ノイズ除去(平滑化)
				final MediaEffectKernel gaussian = new MediaEffectKernel();
				gaussian.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
				mEffects.add(gaussian); */
				// Cannyエッジ検出フィルタ
				final MediaEffectCanny canny = new MediaEffectCanny();
				canny.setEnable(mParent.mEnableCanny);
				mEffects.add(canny);
			}	// synchronized (mSync)
//--------------------------------------------------------------------------------
			handleResize(mVideoWidth, mVideoHeight);
			// native側の処理を開始
			nativeStart(mNativePtr, mVideoWidth, mVideoHeight);
			synchronized (mSync) {
				mParent.isProcessingRunning = true;
				mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:finished");
		}

		@Override
		protected void onStop() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStop");
			synchronized (mSync) {
				mParent.isProcessingRunning = false;
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
			}
			// プレフィルター処理後の画像をNative側へ送る
			mMediaSource.getOutputTexture().bind();
			// Native側でglReadPixelsを使ってフレームバッファから画像データを取得する
			// Nexus6Pで直接glReadPixelsで読み込むと約5ミリ秒かかる
			// PBOのピンポンバッファを使うと約1/10の0.5ミリ秒で返ってくる
			nativeHandleFrame(mNativePtr, mVideoWidth, mVideoHeight, 0);
			mMediaSource.getOutputTexture().unbind();
			// 何も描画しないとハングアップする機種があるので塗りつぶす(と言っても1x1だから気にしなくて良い?)
			makeCurrent();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
//			if (DEBUG) Log.v(TAG, "ProcessingTask#handleDraw:finished");
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
			mSourceTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mSrcDrawer.getProgram().setTexSize(mVideoWidth, mVideoHeight);
			// プレフィルタ用
			if (mMediaSource != null) {
				mMediaSource.resize(width, height);
			} else {
				mMediaSource = new MediaSource(width, height);
			}
			for (final IEffect effect: mEffects) {
				effect.resize(width, height);
			}
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
	private static native int nativeSetEnableCanny(final long id_native, final int enable);
	private static native int nativeGetEnableCanny(final long id_native);
}
