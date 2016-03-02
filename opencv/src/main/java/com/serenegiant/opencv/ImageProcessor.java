package com.serenegiant.opencv;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.glutils.GLTextureOffscreen;
import com.serenegiant.glutils.Texture2dProgram;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

	/** native側のインスタンスポインタ, 名前を変えたりしちゃダメ */
	private long mNativePtr;

	public ImageProcessor(final ImageProcessorCallback callback) {
		if (DEBUG) Log.v(TAG, "コンストラクタ");
		if (callback == null) {
			throw new NullPointerException("callback should not be null");
		}
		mCallback = callback;
		mNativePtr = nativeCreate(new WeakReference<ImageProcessor>(this));
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

	/**
	 * 関連するリソースをすべて破棄する
	 */
	public void release() {
		if (DEBUG) Log.v(TAG, "release");
		synchronized (mSync) {
			isProcessingRunning = false;
			mSync.notifyAll();
		}
		mProcessingTask.release();
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

	/**
	 * native側からの結果コールバック
	 * @param weakSelf
	 * @param result これの方は未定。とりあえずFloatBufferにしてみる
	 */
	private static void callFromNative(final WeakReference<ImageProcessor> weakSelf, final ByteBuffer frame, final ByteBuffer result) {
//		if (DEBUG) Log.v(TAG, "callFromNative");
		final ImageProcessor self = weakSelf != null ? weakSelf.get() : null;
		if (self != null) {
			try {
				final FloatBuffer buf = result.asFloatBuffer();
				self.handleResult(buf);
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
	private void handleResult(final FloatBuffer result) {
//		if (DEBUG) Log.v(TAG, "handleResult");
		result.clear();
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
		/** 映像をテクスチャとして受け取る時のテクスチャ名(SurfaceTexture生成時/分配描画に使用) */
		private int mTexId;
		/** 映像を受け取るtsめのSurfaceTexture */
		private SurfaceTexture mMasterTexture;
		/** mMasterTextureのテクスチャ変換行列 */
		final float[] mTexMatrix = new float[16];
		/** 映像を受け取るためのSurfaceTextureから取得したSurface */
		private Surface mMasterSurface;
		/** 映像サイズ */
		private int mVideoWidth, mVideoHeight;
		/** OpenGL|ESの描画Surfaceからフレームデータを読み込むためのダイレクトByteBuffer */
		private ByteBuffer buf = null;
		/** キャプチャ用のEGLSurface */
		private EGLBase.EglSurface captureSurface;
		/** キャプチャ用EGLSurfaceへの描画用 */
// 		private GLDrawer2D mDrawer;
		private FullFrameRect mDrawer;
		/** ワーク用のGLTextureOffscreen */
 		private GLTextureOffscreen workOffscreen;
		private FullFrameRect mWorkDrawer;

		public ProcessingTask(final ImageProcessor parent) {
			super(null, 0);
			mParent = parent;
			mNativePtr = mParent.mNativePtr;
			mVideoWidth = VIDEO_WIDTH;
			mVideoHeight = VIDEO_HEIGHT;
		}

		/** 映像受け取り用Surfaceを取得 */
		public Surface getSurface() {
			if (DEBUG) Log.v(TAG, "getSurface:" + mMasterSurface);
			return mMasterSurface;
		}

		/** 映像受け取り用SurfaceTextureを取得 */
		public SurfaceTexture getSurfaceTexture() {
			if (DEBUG) Log.v(TAG, "ProcessingTask:getSurfaceTexture:" + mMasterTexture);
			return mMasterTexture;
		}

		private final float[] kernel = new float[] {
			-1f, -1f, -1f,	// エッジ検出
			-1f, 8f, -1f,
			-1f, -1f, -1f
		};
		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:");
			// FIXME ここで生成したSurfaceTextureに割り当てたテクスチャを直接読み込めないやろか?
			mTexId = GLHelper.initTex(GLDrawer2D.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mMasterTexture = new SurfaceTexture(mTexId);
			mMasterSurface = new Surface(mMasterTexture);
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			// こっちはSurfaceTextureで受け取った映像を作業用テクスチャへ描画するため(OESテクスチャ)
			mWorkDrawer = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT_NIGHT/*TEXTURE_EXT_FILT3x3*/));
			mWorkDrawer.getProgram().setTexSize(mVideoWidth, mVideoHeight);
//			mWorkDrawer.getProgram().setKernel(kernel, 0.0f);	// これを入れると畳み込みフィルタでエッジ検出
			// キャプチャ用EGLSurfaceへの描画用(TEXTURE_2D)
			mDrawer = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_FILT3x3));
			mDrawer.getProgram().setTexSize(mVideoWidth, mVideoHeight);
//			mDrawer.getProgram().setKernel(kernel, 0.0f);	// これを入れると畳み込みフィルタでエッジ検出
			// これを呼ぶと映像がどんどん重なっていってしまう, setKernelで同じカーネルを割り当てる分には大丈夫なのに
//			mDrawer.updateFilter(FullFrameRect.FILTER_EDGE_DETECT);
//			mDrawer.updateFilter(FullFrameRect.FILTER_BLACK_WHITE);
			mDrawer.flipMatrix(true);
			handleResize(mVideoWidth, mVideoHeight);
			// native側の処理を開始
			nativeStart(mNativePtr);
			synchronized (mParent.mSync) {
				mParent.isProcessingRunning = true;
				mParent.mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:finished");
		}

		@Override
		protected void onStop() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStop");
			synchronized (mParent.mSync) {
				mParent.isProcessingRunning = false;
				mParent.mSync.notifyAll();
			}
			// native側の処理を停止させる
			nativeStop(mNativePtr);
			// 破棄処理
			makeCurrent();
			mMasterSurface = null;
			if (mMasterTexture != null) {
				mMasterTexture.release();
				mMasterTexture = null;
			}
			if (captureSurface != null) {
				captureSurface.release();
				captureSurface = null;
			}
			if (workOffscreen != null) {
				workOffscreen.release();
				workOffscreen = null;
			}
			if (mDrawer != null) {
				mDrawer.release();
				mDrawer = null;
			}
			if (mWorkDrawer != null) {
				mWorkDrawer.release();
				mWorkDrawer = null;
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

		/**
		 * 実際の描画処理(ワーカースレッド上で実行)
		 */
		private void handleDraw() {
			try {
				makeCurrent();
				mMasterTexture.updateTexImage();
				mMasterTexture.getTransformMatrix(mTexMatrix);
			} catch (final Exception e) {
				Log.e(TAG, "ProcessingTask#draw:thread id =" + Thread.currentThread().getId(), e);
				return;
			}
			// FIXME 必要ならOpenGL|ESで前処理する, 2値化とかエッジ強調とか
			workOffscreen.bind();
			mWorkDrawer.draw(mTexId, mTexMatrix, 0);
			workOffscreen.unbind();
			// キャプチャ用に描画する
			captureSurface.makeCurrent();
//			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//			mDrawer.draw(mTexId, mTexMatrix, 0);
			mDrawer.draw(workOffscreen.getTexture(), workOffscreen.getTexMatrix(), 0);
			captureSurface.swap();
	        // ダイレクトByteBufferに読み込む
	        buf.clear();
	        GLES20.glReadPixels(0, 0, mVideoWidth, mVideoHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
	        // native側へ引き渡す
			nativeHandleFrame(mNativePtr, buf, mVideoWidth, mVideoHeight);
			// 何も描画しないとハングアップする機種があるので塗りつぶす(と言っても1x1だから気にしなくて良い?)
			makeCurrent();
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
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
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			// ダイレクトByteBufferを生成する
			buf = ByteBuffer.allocateDirect(width * height * 4);
	    	buf.order(ByteOrder.LITTLE_ENDIAN);
	    	// キャプチャ用のオフスクリーンSurfaceを作りなおす
	    	if (captureSurface != null) {
	    		captureSurface.release();
	    		captureSurface = null;
	    	}
	    	if (workOffscreen != null) {
				workOffscreen.release();
				workOffscreen = null;
			}
	    	captureSurface = getEgl().createOffscreen(width, height);
			workOffscreen = new GLTextureOffscreen(width, height, false);
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

	private static native int nativeStart(final long id_native);
	private static native int nativeStop(final long id_native);
	private static native int nativeHandleFrame(final long id_native, final ByteBuffer frame, final int width, final int height);
	private static native int nativeSetShowDetects(final long id_native, final boolean showDetects);
}
