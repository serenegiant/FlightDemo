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
import com.serenegiant.mediaeffect.MediaEffectExtraction;
import com.serenegiant.mediaeffect.MediaEffectKernel;
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
	}

	private final Object mSync = new Object();
	private final ImageProcessorCallback mCallback;
	private volatile boolean isProcessingRunning;
	private ProcessingTask mProcessingTask;
	private volatile boolean mEnableEmphasis;
	private volatile boolean mEnabledExtraction;

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

	public void setEmphasis(final boolean emphasis) {
		if (DEBUG) Log.v(TAG, "setEmphasis:" + emphasis);
		if (mEnableEmphasis != emphasis) {
			mEnableEmphasis = emphasis;
		}
	}

	public boolean getEmphasis() {
		return mEnableEmphasis;
	}

	public void setExtraction(final boolean extraction) {
		if (DEBUG) Log.v(TAG, "setExtraction:");
		if (mEnabledExtraction != extraction) {
			mEnabledExtraction = extraction;
		}
	}

	public boolean getExtraction() {
		return mEnabledExtraction;
	}

	/**
	 * native側からの結果コールバック
	 * @param weakSelf
	 * @param result これの方は未定。とりあえずFloatBufferにしてみる
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
	 * @param result これの方は未定。とりあえずFloatBufferにしてるけど#callFromNativeと合わせる
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
		private List<IEffect> mEffects = new ArrayList<IEffect>();

		public ProcessingTask(final ImageProcessor parent) {
			super(null, 0);
			mParent = parent;
			mSync = parent.mSync;
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
			mSourceTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mSourceTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
//--------------------------------------------------------------------------------
			// プレフィルタの準備
			mEffectContext = EffectContext.createWithCurrentGlContext();
			// 自動調整(0〜1.0f, 0なら変化なし)
			final MediaEffectAutoFix autofix = new MediaEffectAutoFix(mEffectContext, 1.0f);
			mEffects.add(autofix);
			// 彩度調整(-1.0f〜1.0f, -1.0fならグレースケール)
			final MediaEffectSaturate saturate = new MediaEffectSaturate(mEffectContext, 0.1f);
			mEffects.add(saturate);
			// 明るさ調整
			if (mParent.mEnableEmphasis) {
				final MediaEffectExtraction adjust = new MediaEffectExtraction();
				adjust.setParameter(    // 抽出なし
					0.0f, 1.0f,			// H(色相) 制限なし(0-360),
					0.0f, 1.0f,			// S(彩度) 0-10,
					0.0f, 1.0f,			// V(明度) 200-255,
					0.0f, 0.0f, 0.1f,	// 抽出後加算値(HSV)
					0.0f);				// 2値化時のしきい値, 0なら2値化なし
				mEffects.add(adjust);
			}
//			final MediaEffectKernel adjust2 = new MediaEffectKernel(Texture2dProgram.KERNEL_LAPLACIAN);
//			mEffects.add(adjust2);
//			// 明るさ調整(0〜1.0f, 0なら変化なし)
//			final MediaEffectBrightness brightness = new MediaEffectBrightness(mEffectContext, 1.0f);
//			mEffects.add(brightness);
//			mEffects.add(brightness);
/*			// コントラスト(0〜1.0f, 0なら変化なし)
			final MediaEffectContrast contrast = new MediaEffectContrast(mEffectContext, 1.0f);
			mEffects.add(contrast); */
			if (mParent.mEnabledExtraction) {
				// 色抽出(白色)
				final MediaEffectExtraction extraction = new MediaEffectExtraction();
				extraction.setParameter(    // 白色を抽出
					0.00f, 1.00f,           // H(色相) 制限なし(0-360),
					0.00f, 0.20f,           // S(彩度) 0-10,
					0.45f, 1.00f,           // V(明度) 200-255,
					0.00f, 0.00f, 0.00f,    // 抽出後加算値(HSV)
					0.45f);					// 2値化時のしきい値, 0なら2値化なし
				mEffects.add(extraction);
				// ノイズ除去(平滑化)
				final MediaEffectKernel gaussian = new MediaEffectKernel();
				gaussian.setParameter(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);
				mEffects.add(gaussian);
			}
//			final MediaEffectBlackWhite bw = new MediaEffectBlackWhite(mEffectContext);
//			mEffects.add(bw);
/*			// Cannyエッジ検出フィルタ
			final MediaEffectCanny canny = new MediaEffectCanny();
			mEffects.add(canny); */
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
			for (final IEffect effect: mEffects) {
				mMediaSource.apply(effect);
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
				mMediaSource.resize(width, height);
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

	public static final int RESULT_FRAME_TYPE_SRC = 0;
	public static final int RESULT_FRAME_TYPE_DST = 1;
	public static final int RESULT_FRAME_TYPE_SRC_LINE = 2;
	public static final int RESULT_FRAME_TYPE_DST_LINE = 3;

	public void setResultFrameType(final int result_frame_type) {
		final int result = nativeSetResultFrameType(mNativePtr, result_frame_type);
		if (result != 0) {
			throw new IllegalStateException("result=" + result);
		}
	}

	public int getResultFrameType() {
		return nativeGetResultFrameType(mNativePtr);
	}

	public void setExtractionColor(
		final int lowerH, final int upperH,
		final int lowerS, final int upperS,
		final int lowerV, final int upperV) {
		final int result = nativeSetExtractionColor(mNativePtr, lowerH, upperH, lowerS, upperS, lowerV, upperV);
		if (result != 0) {
			throw new IllegalStateException("result=" + result);
		}
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
}
