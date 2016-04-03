package com.serenegiant.arflight;

import android.graphics.SurfaceTexture;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

import com.parrot.arsdk.arstream2.ARSTREAM2_H264_FILTER_AU_SYNC_TYPE_ENUM;
import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.FullFrameRect;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.GLHelper;
import com.serenegiant.glutils.Texture2dProgram;
import com.serenegiant.utils.FpsCounter;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoStream implements IVideoStream {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static final String TAG = "VideoStream";

	private static final String VIDEO_MIME_TYPE = "video/avc";
	private static final int VIDEO_INPUT_TIMEOUT_US = 33000;
	private static final long VIDEO_OUTPUT_TIMEOUT_US = 20000;
	public static final int VIDEO_WIDTH = 640;
	public static final int VIDEO_HEIGHT = 368;

	public static final int VIDEO_WIDTH_HALF = VIDEO_WIDTH >>> 1;
	public static final int VIDEO_HEIGHT_HALF = VIDEO_HEIGHT >>> 1;

	private final Object mSync = new Object();
	private volatile boolean isRendererRunning;
	private volatile boolean isDecoderRunning;

	private final RendererTask mRendererTask;
	private final DecodeTask mDecodeTask;
	private final FpsCounter mFps = new FpsCounter();

	public VideoStream() {
		if (DEBUG) Log.v(TAG, "VideoStream:コンストラクタ");
		mDecodeTask = new DecodeTask();
		new Thread(mDecodeTask, "VideoStream#decodeTask").start();
		mRendererTask = new RendererTask(this);
		new Thread(mRendererTask, "VideoStream$rendererTask").start();
		mRendererTask.waitReady();
		synchronized (mSync) {
			for ( ; !isRendererRunning || !isDecoderRunning ; ) {
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
			isRendererRunning = isDecoderRunning = false;
			mSync.notifyAll();
		}
		mRendererTask.release();
	}

	/**
	 * 映像書き込み用Surfaceを追加する
	 * @param id
	 * @param surface
	 */
	public void addSurface(final int id, final Surface surface) {
		if (DEBUG) Log.v(TAG, "addSurface");
		mRendererTask.addSurface(id, surface);
	}

	/**
	 * 映像書き込み用Surfaceを取り除く
	 * @param id
	 */
	public void removeSurface(final int id) {
		if (DEBUG) Log.v(TAG, "removeSurface");
		mRendererTask.removeSurface(id);
	}

	@Override
	public void onReceiveFrame(final ARFrame frame) {
		// 映像フレームデータを受信した時の処理
		// デコーダーへキューイングする
		mDecodeTask.queueFrame(frame);
	}

	@Override
	public void onFrameTimeout() {
		// 一定時間内に映像フレームデータを受信できなかった時の処理
		// 今のところLogCatにメッセージを出すだけで特に何もしない
		Log.w(TAG, "onFrameTimeout");
	}

	public VideoStream updateFps() {
		mFps.update();
		return this;
	}

	public float getFps() {
		return mFps.getFps();
	}

	public float getTotalFps() {
		return mFps.getTotalFps();
	}
//--------------------------------------------------------------------------------
// ARStream2ReceiverListenerのメソッド
// 受信したフレームをDecoderTaskでMediaCodecへ投入する代わりに直接MediaCodecを操作する
//--------------------------------------------------------------------------------
	@Override
	public ByteBuffer[] onSpsPpsReady(final ByteBuffer sps, final ByteBuffer pps) {
		if (DEBUG) Log.v(TAG, "onSpsPpsReady:");
		mDecodeTask.initMediaCodec();
		mDecodeTask.configureMediaCodec(sps, pps, mRendererTask.getSurface());
		return mDecodeTask.inputBuffers;
	}

	@Override
	public int getFreeBuffer() {
		if (DEBUG) Log.v(TAG, "getFreeBuffer:");
		return mDecodeTask.mediaCodec.dequeueInputBuffer(VIDEO_INPUT_TIMEOUT_US);
	}

	@Override
	public void onBufferReady(final int bufferIdx, final long auTimestamp, final long auTimestampShifted, final ARSTREAM2_H264_FILTER_AU_SYNC_TYPE_ENUM auSyncType) {
		if (DEBUG) Log.v(TAG, "onBufferReady:");
		final int flag = (auSyncType == ARSTREAM2_H264_FILTER_AU_SYNC_TYPE_ENUM.ARSTREAM2_H264_FILTER_AU_SYNC_TYPE_IFRAME)
			? 1/*MediaCodec.BUFFER_FLAG_KEY_FRAME*/ : 0;
		final int sz = mDecodeTask.inputBuffers[bufferIdx].limit();
		mDecodeTask.mediaCodec.queueInputBuffer(bufferIdx, 0, sz, auTimestampShifted, flag);
	}
//--------------------------------------------------------------------------------

	/** 受信したh.264映像をデコードして描画タスクにキューイングするタスク */
	private final class DecodeTask implements Runnable {
		private MediaCodec mediaCodec;
		/** デコーダーが初期化出来たかどうか */
		private volatile boolean isCodecConfigured;
		/** IFrame待機中フラグ */
		private boolean waitForIFrame = true;
		private ByteBuffer [] inputBuffers;

		public DecodeTask() {
			isCodecConfigured = false;
			waitForIFrame = true;
		}

		public void queueFrame(final ARFrame frame) {
//			if (DEBUG) Log.v(TAG, "queueFrame:mediaCodec" + mediaCodec + ",isCodecConfigured=" + isCodecConfigured
//				+ ",waitForIFrame=" + waitForIFrame + ",isIFrame=" + (frame != null ? frame.isIFrame() : false));
			if ((mediaCodec != null)) {
				if (!isCodecConfigured && frame.isIFrame()) {
					final ByteBuffer csdBuffer = getCSD(frame);
					if (csdBuffer != null) {
						configureMediaCodec(csdBuffer, mRendererTask.getSurface());
					} else {
						Log.w(TAG, "CSDを取得できなかった");
					}
				}
				if (isCodecConfigured && (!waitForIFrame || frame.isIFrame())) {
					waitForIFrame = false;

					// ここに来るのはIFrameかIFrameから連続してPFrameを受信している時
					int index = -1;

					for (int i = 0; isDecoderRunning && (index < 0) && (i < 30)  ; i++) {
						try {
							index = mediaCodec.dequeueInputBuffer(VIDEO_INPUT_TIMEOUT_US);
						} catch (final IllegalStateException e) {
							Log.e(TAG, "Error while dequeue input buffer");
						}
					}
//					if (DEBUG) Log.v(TAG, "dequeueInputBuffer:index=" + index);
					if (index >= 0) {
						try {
							final ByteBuffer b = inputBuffers[index];
							final int sz = frame.getDataSize();
							b.clear();
							b.put(frame.getByteData(), 0, sz);
							int flag = 0;
							if (frame.isIFrame()) {
								flag |= MediaCodec.BUFFER_FLAG_KEY_FRAME; //  | MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
							}
							mediaCodec.queueInputBuffer(index, 0, sz, 0, flag);
						} catch (final IllegalStateException e) {
							Log.w(TAG, "Error while queue input buffer");
						}
					} else {
						if (DEBUG) Log.v(TAG, "デコーダーの準備ができてない/入力キューが満杯");
						waitForIFrame = true;
					}
				}
			} else {
				Log.w(TAG, "MediaCodecが生成されていない");
			}
		}

		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "DecodeTask#run");
			// デコーダーを初期化
			initMediaCodec();
			synchronized (mSync) {
				isDecoderRunning = true;
				mSync.notifyAll();
			}
			// デコーダーの初期化完了待ちループ
			for ( ; isDecoderRunning && !isCodecConfigured ; ) {
				try {
					Thread.sleep(VIDEO_OUTPUT_TIMEOUT_US / 1000);
				} catch (final InterruptedException e) {
					break;
				}
			}
			if (DEBUG) Log.v(TAG, "DecodeTask#run:isRendererRunning=" + isRendererRunning + ",isCodecConfigured=" + isCodecConfigured);
			if (isDecoderRunning && isCodecConfigured) {
				// 正常に初期化出来た時
				final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
				int outIndex;
				for ( ; isDecoderRunning; ) {
					// MediaCodecでデコードした映像フレームを取り出してSurfaceへ反映させるためのループ
					try {
						outIndex = mediaCodec.dequeueOutputBuffer(info, VIDEO_OUTPUT_TIMEOUT_US);
//						if (DEBUG) Log.v(TAG, "releaseOutputBuffer:" + outIndex);
						// XXX 時間調整っていらんのかな?
						if (outIndex >= 0) {
							// これを呼び出すとSurfaceへの書き込み要求が発行される
							mediaCodec.releaseOutputBuffer(outIndex, true);
						}
					} catch (final IllegalStateException e) {
						Log.e(TAG, "Error while dequeue output buffer (outIndex)");
					}
				}
			}
			synchronized (mSync) {
				isDecoderRunning = false;
				mSync.notifyAll();
			}
			// デコーダーを破棄
			releaseMediaCodec();
			if (DEBUG) Log.v(TAG, "DecodeTask#run:終了");
		}

		/**
		 * デコーダー用のMediaCodecを生成
		 */
		private void initMediaCodec() {
			try {
				mediaCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE);
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		}

		/**
		 * デコーダー用のMediaCodecを初期化
		 * @param csdBuffer
		 * @param surface
		 */
		private void configureMediaCodec(final ByteBuffer csdBuffer, final Surface surface) {
			final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
			format.setByteBuffer("csd-0", csdBuffer);

			mediaCodec.configure(format, surface, null, 0);
			mediaCodec.start();

			inputBuffers = mediaCodec.getInputBuffers();
			isCodecConfigured = true;
		}

		private void configureMediaCodec(final ByteBuffer sps, final ByteBuffer pps, final Surface surface) {
			final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
			format.setByteBuffer("csd-0", sps);
			format.setByteBuffer("csd-1", pps);

			mediaCodec.configure(format, surface, null, 0);
			mediaCodec.start();

			inputBuffers = mediaCodec.getInputBuffers();
			isCodecConfigured = true;
		}

		/**
		 * デコーダーを破棄
		 */
		private void releaseMediaCodec() {
			if ((mediaCodec != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
				try {
					if (isCodecConfigured) {
						mediaCodec.stop();
					}
				} catch (final IllegalStateException e) {
					Log.w(TAG, e);
				}
				isCodecConfigured = false;
				mediaCodec.release();
				mediaCodec = null;
			}
		}

		private ByteBuffer getCSD(final ARFrame frame) {
			if (DEBUG) Log.v(TAG, "getCSD:" + frame);
			int spsSize = -1;
			if (frame.isIFrame()) {
				final byte[] data = frame.getByteData();
				int searchIndex = 0;
				// we'll need to search the "00 00 00 01" pattern to find each header size
				// Search start at index 4 to avoid finding the SPS "00 00 00 01" tag
				for (searchIndex = 4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
					if (0 == data[searchIndex  ] &&
							0 == data[searchIndex+1] &&
							0 == data[searchIndex+2] &&
							1 == data[searchIndex+3])
					{
						break;  // SPS header found
					}
				}
				spsSize = searchIndex;

				// Search start at index 4 to avoid finding the PPS "00 00 00 01" tag
				for (searchIndex = spsSize+4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
					if (0 == data[searchIndex  ] &&
							0 == data[searchIndex+1] &&
							0 == data[searchIndex+2] &&
							1 == data[searchIndex+3]) {
						break;  // frame header found
					}
				}
				int csdSize = searchIndex;

				final ByteBuffer result = ByteBuffer.allocateDirect(csdSize);
				result.clear();
				result.put(data, 0, csdSize);	// これはちょっと遅い
//				final byte[] csdInfo = new byte[csdSize];
//				System.arraycopy(data, 0, csdInfo, 0, csdSize);
//				return ByteBuffer.wrap(csdInfo);
				return result;
			}
			return null;
		}
	};

	private static final int REQUEST_DRAW = 1;
	private static final int REQUEST_UPDATE_SIZE = 2;
	private static final int REQUEST_ADD_SURFACE = 3;
	private static final int REQUEST_REMOVE_SURFACE = 4;

	/** デコードした映像をOpenGL|ESでSurface全面に表示するためのタスク */
	private static final class RendererTask extends EglTask {
		/** 映像の分配描画先を保持&描画するためのホルダークラス */
		private static final class RendererSurfaceRec {
			private Object mSurface;
			private EGLBase.EglSurface mTargetSurface;
			final float[] mMvpMatrix = new float[16];

			public RendererSurfaceRec(final EGLBase egl, final Object surface) {
				mSurface = surface;
				mTargetSurface = egl.createFromSurface(surface);
				Matrix.setIdentityM(mMvpMatrix, 0);
			}

			public void release() {
				if (mTargetSurface != null) {
					mTargetSurface.release();
					mTargetSurface = null;
				}
				mSurface = null;
			}
		}

		private final VideoStream mParent;
		/** 受け取った映像の分配描画の排他制御用 */
		private final Object mClientSync = new Object();
		/** 分配描画先 */
		private final SparseArray<RendererSurfaceRec> mClients = new SparseArray<RendererSurfaceRec>();

//		private GLDrawer2D mDrawer;
		private FullFrameRect mDrawer;
		/** MediaCodecでデコードした映像を受け取るためのテクスチャのテクスチャ名(SurfaceTexture生成時/分配描画に使用) */
		private int mTexId;
		/** MediaCodecでデコードした映像を受け取るためのSurfaceTexture */
		private SurfaceTexture mMasterTexture;
		/** mMasterTextureのテクスチャ変換行列 */
		final float[] mTexMatrix = new float[16];
		/** MediaCodecでデコードした映像を受け取るためのSurfaceTextureから取得したSurface */
		private Surface mMasterSurface;
		/** 映像サイズ */
		private int mVideoWidth, mVideoHeight;

		/**
		 * コンストラクタ
		 * @param parent
		 */
		public RendererTask(final VideoStream parent) {
			super(null, EglTask.EGL_FLAG_RECORDABLE);
			mParent = parent;
			mVideoWidth = VIDEO_WIDTH;
			mVideoHeight = VIDEO_HEIGHT;
		}

		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "onStart:");
//			mDrawer = new GLDrawer2D(true);
			mDrawer = new FullFrameRect(new Texture2dProgram(Texture2dProgram.ProgramType.TEXTURE_EXT_FILT3x3));
			mDrawer.getProgram().setTexSize(mVideoWidth, mVideoHeight);
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_EMBOSS, 0.5f);		// エンボス
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SOBEL_H, 0.1f);		// ソーベル(エッジ検出, 1次微分)
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SOBEL2_H, 0.1f);		// ソーベル(エッジ検出, 1次微分)
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_EDGE_DETECT, 0.0f);	// エッジ検出
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SHARPNESS, 0.0f);	// シャープ
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_SMOOTH, 0.0f);		// 移動平均
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_GAUSSIAN, 0.0f);		// ガウシアン(平滑化,ノイズ除去)
//			mDrawer.getProgram().setKernel(Texture2dProgram.KERNEL_LAPLACIAN, 0.0f);	// ラプラシアン(エッジ検出, 2次微分)

			mTexId = GLHelper.initTex(GLDrawer2D.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mMasterTexture = new SurfaceTexture(mTexId);
			mMasterSurface = new Surface(mMasterTexture);
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			synchronized (mParent.mSync) {
				mParent.isRendererRunning = true;
				mParent.mSync.notifyAll();
			}
			mParent.mFps.reset();
			if (DEBUG) Log.v(TAG, "onStart:finished");
		}

		@Override
		protected void onStop() {
			if (DEBUG) Log.v(TAG, "onStop");
			synchronized (mParent.mSync) {
				mParent.isRendererRunning = false;
				mParent.mSync.notifyAll();
			}
			handleRemoveAll();
			makeCurrent();
			if (mDrawer != null) {
				mDrawer.release();
				mDrawer = null;
			}
			mMasterSurface = null;
			if (mMasterTexture != null) {
				mMasterTexture.release();
				mMasterTexture = null;
			}
			if (DEBUG) Log.v(TAG, "onStop:finished");
		}

		@Override
		protected boolean processRequest(int request, int arg1, int arg2, Object obj) {
			switch (request) {
			case REQUEST_DRAW:
				handleDraw();
				break;
			case REQUEST_UPDATE_SIZE:
				handleResize(arg1, arg2);
				break;
			case REQUEST_ADD_SURFACE:
				handleAddSurface(arg1, obj);
				break;
			case REQUEST_REMOVE_SURFACE:
				handleRemoveSurface(arg1);
				break;
			}
			return false;
		}

		/** 映像受け取り用Surfaceを取得 */
		public Surface getSurface() {
			if (DEBUG) Log.v(TAG, "getSurface:" + mMasterSurface);
			return mMasterSurface;
		}

		/** 映像受け取り用SurfaceTextureを取得 */
		public SurfaceTexture getSurfaceTexture() {
			if (DEBUG) Log.v(TAG, "getSurfaceTexture:" + mMasterTexture);
			return mMasterTexture;
		}

		/**
		 * 分配描画用のSurfaceを追加
		 * @param id
		 * @param surface Surface/SurfaceHolder/SurfaceTexture
		 */
		public void addSurface(final int id, final Object surface) {
			synchronized (mClientSync) {
				if ((surface != null) && (mClients.get(id) == null)) {
					offer(REQUEST_ADD_SURFACE, id, surface);
					try {
						mClientSync.wait();
					} catch (final InterruptedException e) {
						// ignore
					}
				}
			}
		}

		/***
		 * 分配描画用のSurfaceを削除
		 * @param id
		 */
		public void removeSurface(final int id) {
			synchronized (mClientSync) {
				if (mClients.get(id) != null) {
					offer(REQUEST_REMOVE_SURFACE, id);
					try {
						mClientSync.wait();
					} catch (final InterruptedException e) {
						// ignore
					}
				}
			}
		}

		/***
		 * 描画映像サイズを変更
		 * @param width
		 * @param height
		 */
		public void resize(final int width, final int height) {
			if ((mVideoWidth != width) || (mVideoHeight != height)) {
				offer(REQUEST_UPDATE_SIZE, width, height);
			}
		}

		/**
		 * 実際の描画処理(ワーカースレッド上で実行)
		 */
		private void handleDraw() {
//			if (DEBUG) Log.v(TAG, "handleDraw:");
			mParent.mFps.count();
			try {
				makeCurrent();
				mMasterTexture.updateTexImage();
				mMasterTexture.getTransformMatrix(mTexMatrix);
			} catch (final Exception e) {
				Log.e(TAG, "draw:thread id =" + Thread.currentThread().getId(), e);
				return;
			}
			// 各Surfaceへ描画する
			synchronized (mClientSync) {
				final int n = mClients.size();
				RendererSurfaceRec client;
				for (int i = 0; i < n; i++) {
					client = mClients.valueAt(i);
					if (client != null) {
						client.mTargetSurface.makeCurrent();
//						mDrawer.setMvpMatrix(client.mMvpMatrix, 0);
						mDrawer.draw(mTexId, mTexMatrix, 0);
						client.mTargetSurface.swap();
					}
				}
			}
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
//			if (DEBUG) Log.v(TAG, "handleDraw:終了");
		}

		/**
		 * 分配描画用Surfaceを追加(ワーカースレッド上で実行)
		 * @param id
		 * @param surface
		 */
		private void handleAddSurface(final int id, final Object surface) {
			if (DEBUG) Log.v(TAG, "handleAddSurface:id=" + id);
			checkSurface();
			synchronized (mClientSync) {
				RendererSurfaceRec client = mClients.get(id);
				if (client == null) {
					try {
						client = new RendererSurfaceRec(getEgl(), surface);
						mClients.append(id, client);
					} catch (final Exception e) {
						Log.w(TAG, "invalid surface: surface=" + surface);
					}
				} else {
					Log.w(TAG, "surface is already added: id=" + id);
				}
				mClientSync.notifyAll();
			}
		}

		/**
		 * 分配描画用Surfaceを取り除く(ワーカースレッド上で実行)
		 * @param id
		 */
		private void handleRemoveSurface(final int id) {
			if (DEBUG) Log.v(TAG, "handleRemoveSurface:id=" + id);
			synchronized (mClientSync) {
				final RendererSurfaceRec client = mClients.get(id);
				if (client != null) {
					mClients.remove(id);
					client.release();
				}
				checkSurface();
				mClientSync.notifyAll();
			}
		}

		private void handleRemoveAll() {
			if (DEBUG) Log.v(TAG, "handleRemoveAll:");
			synchronized (mClientSync) {
				final int n = mClients.size();
				RendererSurfaceRec client;
				for (int i = 0; i < n; i++) {
					client = mClients.valueAt(i);
					if (client != null) {
						makeCurrent();
						client.release();
					}
				}
				mClients.clear();
			}
			if (DEBUG) Log.v(TAG, "handleRemoveAll:finished");
		}

		private void checkSurface() {
			if (DEBUG) Log.v(TAG, "checkSurface");
			synchronized (mClientSync) {
				final int n = mClients.size();
				for (int i = 0; i < n; i++) {
					final RendererSurfaceRec client = mClients.valueAt(i);
					if (client != null && client.mSurface instanceof Surface) {
						if (!((Surface)client.mSurface).isValid()) {
							final int id = mClients.keyAt(i);
							if (DEBUG) Log.i(TAG, "checkSurface:found invalid surface:id=" + id);
							mClients.valueAt(i).release();
							mClients.remove(id);
						}
					}
				}
			}
			if (DEBUG) Log.v(TAG, "checkSurface:finished");
		}

		/**
		 * 製造サイズ変更処理(ワーカースレッド上で実行)
		 * @param width
		 * @param height
		 */
		private void handleResize(final int width, final int height) {
			if (DEBUG) Log.v(TAG, String.format("handleResize:(%d,%d)", width, height));
			mVideoWidth = width;
			mVideoHeight = height;
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
		}

		/**
		 * TextureSurfaceで映像を受け取った際のコールバックリスナー
		 */
		private final SurfaceTexture.OnFrameAvailableListener
			mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {

			@Override
			public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
				offer(REQUEST_DRAW);
			}
		};
	};
}
