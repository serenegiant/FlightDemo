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

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;
import com.serenegiant.glutils.GLDrawer2D;
import com.serenegiant.glutils.GLHelper;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoStream implements IVideoStream {
	private static final boolean DEBUG = true; // FIXME 実働時はfalseにすること
	private static final String TAG = "VideoStream";

	private static final String VIDEO_MIME_TYPE = "video/avc";
	private static final int VIDEO_INPUT_TIMEOUT_US = 33000;
	private static final long VIDEO_OUTPUT_TIMEOUT_US = 20000;
	private static final int VIDEO_WIDTH = 640;
	private static final int VIDEO_HEIGHT = 368;

	private final Object mSync = new Object();
	private volatile boolean isRendererRunning;
	private volatile boolean isDecoderRunning;

	private final RendererTask mRendererTask;
	private final DecodeTask mDecodeTask;

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

	public void release() {
		if (DEBUG) Log.v(TAG, "release");
		synchronized (mSync) {
			isRendererRunning = isDecoderRunning = false;
			mSync.notifyAll();
		}
		mRendererTask.release();
	}

	public void addSurface(final int id, final Surface surface) {
		if (DEBUG) Log.v(TAG, "addSurface");
		mRendererTask.addSurface(id, surface);
	}

	public void removeSurface(final int id) {
		if (DEBUG) Log.v(TAG, "removeSurface");
		mRendererTask.removeSurface(id);
	}

	@Override
	public void onReceiveFrame(final ARFrame frame) {
		mDecodeTask.queueFrame(frame);
	}

	@Override
	public void onFrameTimeout() {
		Log.w(TAG, "onFrameTimeout");
	}

	private final class DecodeTask implements Runnable {
		private MediaCodec mediaCodec;
		private volatile boolean isCodecConfigured;
		private ByteBuffer csdBuffer;
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
					csdBuffer = getCSD(frame);
					if (csdBuffer != null) {
						configureMediaCodec(mRendererTask.getSurface());
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
						final ByteBuffer b = inputBuffers[index];
						final int sz = frame.getDataSize();
						b.clear();
						b.put(frame.getByteData(), 0, sz);
						int flag = 0;
						if (frame.isIFrame()) {
							flag |= MediaCodec.BUFFER_FLAG_KEY_FRAME; //  | MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
						}
						try {
							mediaCodec.queueInputBuffer(index, 0, sz, 0, flag);
						} catch (final IllegalStateException e) {
							Log.w(TAG, "Error while queue input buffer");
						}

					} else {
						if (DEBUG) Log.v(TAG, "デコーダーの準備ができてない/入力キューが満杯");
						waitForIFrame = true;
					}
				} else {
//					if (DEBUG) Log.v(TAG, "queueFrame:フレームをドロップ:isCodecConfigured=" + isCodecConfigured
//						+ ",waitForIFrame=" + waitForIFrame + ",isIFrame=" + (frame != null ? frame.isIFrame() : false));
				}
			} else {
				Log.w(TAG, "MediaCodecが生成されていない");
			}
		}

		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "DecodeTask#run");
			initMediaCodec();
			synchronized (mSync) {
				isDecoderRunning = true;
				mSync.notifyAll();
			}
			for ( ; isDecoderRunning && !isCodecConfigured ; ) {
				try {
					Thread.sleep(VIDEO_OUTPUT_TIMEOUT_US / 1000);
				} catch (final InterruptedException e) {
					break;
				}
			}
			if (DEBUG) Log.v(TAG, "DecodeTask#run:isRendererRunning=" + isRendererRunning + ",isCodecConfigured=" + isCodecConfigured);
			if (isCodecConfigured) {
				final MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
				int outIndex;
				for ( ; isDecoderRunning; ) {
					try {
						outIndex = mediaCodec.dequeueOutputBuffer(info, VIDEO_OUTPUT_TIMEOUT_US);
//						if (DEBUG) Log.v(TAG, "releaseOutputBuffer:" + outIndex);
						// XXX 時間調整っていらんのかな?
						if (outIndex >= 0) {
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
			releaseMediaCodec();
			if (DEBUG) Log.v(TAG, "DecodeTask#run:終了");
		}

		private void initMediaCodec() {
			try {
				mediaCodec = MediaCodec.createDecoderByType(VIDEO_MIME_TYPE);
			} catch (final IOException e) {
				Log.w(TAG, e);
			}
		}

		private void configureMediaCodec(final Surface surface) {
			final MediaFormat format = MediaFormat.createVideoFormat(VIDEO_MIME_TYPE, VIDEO_WIDTH, VIDEO_HEIGHT);
			format.setByteBuffer("csd-0", csdBuffer);

			mediaCodec.configure(format, surface, null, 0);
			mediaCodec.start();

			inputBuffers = mediaCodec.getInputBuffers();
			isCodecConfigured = true;
		}

		private void releaseMediaCodec() {
			if ((mediaCodec != null) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)) {
				if (isCodecConfigured) {
					mediaCodec.stop();
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
						break;  // PPS header found
					}
				}
				spsSize = searchIndex;

				// Search start at index 4 to avoid finding the PSS "00 00 00 01" tag
				for (searchIndex = spsSize+4; searchIndex <= frame.getDataSize() - 4; searchIndex ++) {
					if (0 == data[searchIndex  ] &&
							0 == data[searchIndex+1] &&
							0 == data[searchIndex+2] &&
							1 == data[searchIndex+3]) {
						break;  // frame header found
					}
				}
				int csdSize = searchIndex;

				final byte[] csdInfo = new byte[csdSize];
				System.arraycopy(data, 0, csdInfo, 0, csdSize);
				return ByteBuffer.wrap(csdInfo);
			}
			return null;
		}
	};

	private static final int REQUEST_DRAW = 1;
	private static final int REQUEST_UPDATE_SIZE = 2;
	private static final int REQUEST_ADD_SURFACE = 3;
	private static final int REQUEST_REMOVE_SURFACE = 4;
	private static final class RendererTask extends EglTask {
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
		private final Object mClientSync = new Object();
		private final SparseArray<RendererSurfaceRec> mClients = new SparseArray<RendererSurfaceRec>();

		private GLDrawer2D mDrawer;
		private int mTexId;
		private SurfaceTexture mMasterTexture;
		final float[] mTexMatrix = new float[16];
		private Surface mMasterSurface;
		private int mVideoWidth, mVideoHeight;

		public RendererTask(final VideoStream parent) {
			super(null, EglTask.EGL_FLAG_RECORDABLE);
			mParent = parent;
			mVideoWidth = VIDEO_WIDTH;
			mVideoHeight = VIDEO_HEIGHT;
		}

		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "onStart:");
			mDrawer = new GLDrawer2D(true);
			mTexId = GLHelper.initTex(GLDrawer2D.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mMasterTexture = new SurfaceTexture(mTexId);
			mMasterSurface = new Surface(mMasterTexture);
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			synchronized (mParent.mSync) {
				mParent.isRendererRunning = true;
				mParent.mSync.notifyAll();
			}
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

		public Surface getSurface() {
			if (DEBUG) Log.v(TAG, "getSurface:" + mMasterSurface);
			return mMasterSurface;
		}

		public SurfaceTexture getSurfaceTexture() {
			if (DEBUG) Log.v(TAG, "getSurfaceTexture:" + mMasterTexture);
			return mMasterTexture;
		}

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

		public void resize(final int width, final int height) {
			if ((mVideoWidth != width) || (mVideoHeight != height)) {
				offer(REQUEST_UPDATE_SIZE, width, height);
			}
		}

		/**
		 * 実際の描画処理
		 */
		private void handleDraw() {
//			if (DEBUG) Log.v(TAG, "handleDraw:");
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
						mDrawer.setMvpMatrix(client.mMvpMatrix, 0);
						mDrawer.draw(mTexId, mTexMatrix, 0);
						client.mTargetSurface.swap();
					}
				}
			}
			GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
			GLES20.glFlush();
//			if (DEBUG) Log.v(TAG, "handleDraw:終了");
		}

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

		private void handleResize(final int width, final int height) {
			if (DEBUG) Log.v(TAG, String.format("handleResize:(%d,%d)", width, height));
			mVideoWidth = width;
			mVideoHeight = height;
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
		}

		/**
		 * TextureSurfaceで映像を受け取った際のコールバックリスナー
		 */
		private final SurfaceTexture.OnFrameAvailableListener mOnFrameAvailableListener = new SurfaceTexture.OnFrameAvailableListener() {
			@Override
			public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
				offer(REQUEST_DRAW);
			}
		};
	};
}
