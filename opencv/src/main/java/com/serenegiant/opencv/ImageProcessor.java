package com.serenegiant.opencv;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.GLHelper;

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

	private final Object mSync = new Object();
	private volatile boolean isProcessingRunning;
	private ProcessingTask mProcessingTask;

	/** native側のインスタンスポインタ, 名前を変えたりしちゃダメ */
	private long mNativePtr;

	public ImageProcessor() {
		if (DEBUG) Log.v(TAG, "コンストラクタ");
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
	private static void callFromNative(final WeakReference<ImageProcessor> weakSelf, final FloatBuffer result) {
		if (DEBUG) Log.v(TAG, "callFromNative");
		final ImageProcessor self = weakSelf != null ? weakSelf.get() : null;
		if (self != null) {
			self.handleResult(result);
		}
		if (DEBUG) Log.v(TAG, "callFromNative:finished");
	}

	/**
	 * native側からの結果コールバックの実際の処理
	 * @param result これの方は未定。とりあえずFloatBufferにしてるけど#callFromNativeと合わせる
	 */
	private void handleResult(final FloatBuffer result) {
		// FIXME 解析結果の処理
		result.clear();
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
 		private GLDrawer2D drawer;

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

		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "ProcessingTask#onStart:");
			// FIXME ここで生成したSurfaceTextureに割り当てたテクスチャを直接読み込めないやろか?
			mTexId = GLHelper.initTex(GLDrawer2D.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mMasterTexture = new SurfaceTexture(mTexId);
			mMasterSurface = new Surface(mMasterTexture);
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			drawer = new GLDrawer2D(true);
//			drawer.getMvpMatrix()[5] *= -1.0f;	// 上下反転させる時
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
			if (drawer != null) {
				drawer.release();
				drawer = null;
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
			// キャプチャ用に描画する
			captureSurface.makeCurrent();
			drawer.draw(mTexId, mTexMatrix, 0);
			captureSurface.swap();
	        // ダイレクトByteBufferに読み込む
	        buf.clear();
	        GLES20.glReadPixels(0, 0, mVideoWidth, mVideoHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
			// FIXME ここでテクスチャ名をNative側へ渡してテクスチャの内容を読み取る&OpenCVで処理
			nativeHandleFrame(mNativePtr, buf, mVideoWidth, mVideoHeight);
			// FIXME 結果をどうやって受け取ろう?コールバックメソッドを呼び出す?
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
	    	captureSurface = getEgl().createOffscreen(width, height);
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
			System.loadLibrary("c++_shared");
			System.loadLibrary("common");
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
}
