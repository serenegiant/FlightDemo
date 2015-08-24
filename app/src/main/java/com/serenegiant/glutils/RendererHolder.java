package com.serenegiant.glutils;
/*
 * UVCCamera
 * library and sample to access to UVC web camera on non-rooted Android device
 *
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: RendererHolder.java
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * All files in the folder are under this Apache License, Version 2.0.
 * Files in the jni/libjpeg, jni/libusb and jin/libuvc folder may have a different license, see the respective files.
*/

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.graphics.SurfaceTexture.OnFrameAvailableListener;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

/**
 * Hold shared texture that has camera frame and draw them to registered surface if needs<br>
 * Using RenderHandler is little bit slow and it is better to draw in this class directly.
 */
public class RendererHolder {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "RendererHolder";

	private final Object mSync = new Object();
	private final RenderHolderCallback mCallback;
	private volatile boolean isRunning;
	private File mCaptureFile;

	private final RendererTask mRendererTask;

	public RendererHolder(final int width, final int height, final RenderHolderCallback callback) {
//		if (DEBUG) Log.v(TAG, "Constructor");
		mCallback = callback;
		mRendererTask = new RendererTask(this, width, height);
		new Thread(mRendererTask, TAG).start();
		mRendererTask.waitReady();
		new Thread(mCaptureTask, "CaptureTask").start();
		synchronized (mSync) {
			if (!isRunning) {
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
				}
			}
		}
//		if (DEBUG) Log.v(TAG, "Constructor:finished");
	}

	public Surface getSurface() {
		return mRendererTask.getSurface();
	}

	public SurfaceTexture getSurfaceTexture() {
		return mRendererTask.getSurfaceTexture();
	}

	public void resize(final int width, final int height) {
		mRendererTask.resize(width, height);
	}

	public void addSurface(final int id, final Surface surface, final boolean isRecordable) {
//		if (DEBUG) Log.v(TAG, "addSurface:id=" + id + ",surface=" + surface);
		mRendererTask.addSurface(id, surface);
	}

	public void removeSurface(final int id) {
//		if (DEBUG) Log.v(TAG, "removeSurface:id=" + id);
		mRendererTask.removeSurface(id);
	}

	/**
	 * 静止画を撮影する
	 * 撮影完了を待機しない
	 * @param path
	 */
	public void captureStillAsync(final String path) {
//		if (DEBUG) Log.v(TAG, "captureStill:" + path);
		final File file = new File(path);
		synchronized (mSync) {
			mCaptureFile = file;
			mSync.notifyAll();
		}
	}

	/**
	 * 静止画を撮影する
	 * 撮影完了を待機する
	 * @param path
	 */
	public void captureStill(final String path) {
//		if (DEBUG) Log.v(TAG, "captureStill:" + path);
		final File file = new File(path);
		synchronized (mSync) {
			mCaptureFile = file;
			mSync.notifyAll();
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			}
		}
	}

	public void release() {
//		if (DEBUG) Log.v(TAG, "release:");
		mRendererTask.release();
		synchronized (mSync) {
			isRunning = false;
			mSync.notifyAll();
		}
//		if (DEBUG) Log.v(TAG, "release:finished");
	}

	private static final int REQUEST_DRAW = 1;
	private static final int REQUEST_UPDATE_SIZE = 2;
	private static final int REQUEST_ADD_SURFACE = 3;
	private static final int REQUEST_REMOVE_SURFACE = 4;

	private static final class RendererTask extends EglTask {

		private final class RendererSurfaceRec {
			private Object mSurface;
			private EGLBase.EglSurface mTargetSurface;
			final float[] mMvpMatrix = new float[16];

			public RendererSurfaceRec(final EGLBase egl, final Object surface) {
				mSurface = surface;
				mTargetSurface = new EGLBase.EglSurface(egl, surface);
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

		private final Object mClientSync = new Object();
		private final SparseArray<RendererSurfaceRec> mClients = new SparseArray<RendererSurfaceRec>();
		private final RendererHolder mParent;
		private GLDrawer2D mDrawer;
		private int mTexId;
		private SurfaceTexture mMasterTexture;
		final float[] mTexMatrix = new float[16];
		private Surface mMasterSurface;
		private int mVideoWidth, mVideoHeight;

		public RendererTask(final RendererHolder parent, final int width, final int height) {
			super(null, EglTask.EGL_FLAG_RECORDABLE);
			mParent = parent;
			mVideoWidth = width;
			mVideoHeight = height;
		}

		@Override
		protected void onStart() {
//			if (DEBUG) Log.v(TAG, "onStart:");
			mDrawer = new GLDrawer2D(true);
			mTexId = GLHelper.initTex(GLDrawer2D.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_NEAREST);
			mMasterTexture = new SurfaceTexture(mTexId);
			mMasterSurface = new Surface(mMasterTexture);
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
			mMasterTexture.setOnFrameAvailableListener(mOnFrameAvailableListener);
			if (mParent.mCallback != null) {
				mParent.mCallback.onCreate(mMasterSurface);
			}
			synchronized (mParent.mSync) {
				mParent.isRunning = true;
				mParent.mSync.notifyAll();
			}
//			if (DEBUG) Log.v(TAG, "onStart:finished");
		}

		@Override
		protected void onStop() {
//			if (DEBUG) Log.v(TAG, "onStop");
			synchronized (mParent.mSync) {
				mParent.isRunning = false;
				mParent.mSync.notifyAll();
			}
			if (mParent.mCallback != null) {
				mParent.mCallback.onDestroy();
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
//			if (DEBUG) Log.v(TAG, "onStop:finished");
		}

		@Override
		protected boolean onError(final Exception e) {
//			if (DEBUG) Log.w(TAG, e);
			return false;
		};

		@Override
		protected boolean processRequest(final int request, final int arg1, final int arg2, final Object obj) {
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
//			if (DEBUG) Log.v(TAG, "getSurface:" + mMasterSurface);
			return mMasterSurface;
		}

		public SurfaceTexture getSurfaceTexture() {
//			if (DEBUG) Log.v(TAG, "getSurfaceTexture:" + mMasterTexture);
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
			try {
				makeCurrent();
				mMasterTexture.updateTexImage();
				mMasterTexture.getTransformMatrix(mTexMatrix);
			} catch (final Exception e) {
				Log.e(TAG, "draw:thread id =" + Thread.currentThread().getId(), e);
				return;
			}
			synchronized (mParent.mCaptureTask) {
				// キャプチャタスクに映像が更新されたことを通知
				mParent.mCaptureTask.notify();
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
		}

		private void handleAddSurface(final int id, final Object surface) {
//			if (DEBUG) Log.v(TAG, "handleAddSurface:id=" + id);
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
//			if (DEBUG) Log.v(TAG, "handleRemoveSurface:id=" + id);
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
//			if (DEBUG) Log.v(TAG, "handleRemoveAll:");
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
//			if (DEBUG) Log.v(TAG, "handleRemoveAll:finished");
		}

		private void checkSurface() {
//			if (DEBUG) Log.v(TAG, "checkSurface");
			synchronized (mClientSync) {
				final int n = mClients.size();
				for (int i = 0; i < n; i++) {
					final RendererSurfaceRec client = mClients.valueAt(i);
					if (client != null && client.mSurface instanceof Surface) {
						if (!((Surface)client.mSurface).isValid()) {
							final int id = mClients.keyAt(i);
//							if (DEBUG) Log.i(TAG, "checkSurface:found invalid surface:id=" + id);
							mClients.valueAt(i).release();
							mClients.remove(id);
						}
					}
				}
			}
//			if (DEBUG) Log.v(TAG, "checkSurface:finished");
		}

		private void handleResize(final int width, final int height) {
//			if (DEBUG) Log.v(TAG, String.format("handleResize:(%d,%d)", width, height));
			mVideoWidth = width;
			mVideoHeight = height;
			mMasterTexture.setDefaultBufferSize(mVideoWidth, mVideoHeight);
		}

		/**
		 * TextureSurfaceで映像を受け取った際のコールバックリスナー
		 */
		private final OnFrameAvailableListener mOnFrameAvailableListener = new OnFrameAvailableListener() {
			@Override
			public void onFrameAvailable(final SurfaceTexture surfaceTexture) {
				offer(REQUEST_DRAW);
			}
		};

	}

	/**
	 * 静止画を非同期でキャプチャするためのRunnable
	 */
	private final Runnable mCaptureTask = new Runnable() {
    	EGLBase egl;
    	EGLBase.EglSurface captureSurface;
    	GLDrawer2D drawer;

    	@Override
		public void run() {
//			if (DEBUG) Log.v(TAG, "captureTask start");
			synchronized (mSync) {
				// 描画スレッドが実行されるまで待機
				if (!isRunning) {
					try {
						mSync.wait();
					} catch (final InterruptedException e) {
					}
				}
			}
			init();
			int width = -1, height = -1;
			ByteBuffer buf = null;
			File captureFile = null;
//			if (DEBUG) Log.v(TAG, "captureTask loop");
			for (; isRunning ;) {
				synchronized (mSync) {
					if (captureFile == null) {
						if (mCaptureFile == null) {
							try {
								mSync.wait();
							} catch (final InterruptedException e) {
								break;
							}
						}
						if (mCaptureFile != null) {
							captureFile = mCaptureFile;
							mCaptureFile = null;
						}
						continue;
					}
					if (buf == null | width != mRendererTask.mVideoWidth || height != mRendererTask.mVideoHeight) {
						width = mRendererTask.mVideoWidth;
						height = mRendererTask.mVideoHeight;
						buf = ByteBuffer.allocateDirect(width * height * 4);
				    	buf.order(ByteOrder.LITTLE_ENDIAN);
				    	if (captureSurface != null) {
				    		captureSurface.release();
				    		captureSurface = null;
				    	}
				    	captureSurface = egl.createOffscreen(width, height);
					}
					if (isRunning && (captureFile != null)) {
						captureSurface.makeCurrent();
						drawer.draw(mRendererTask.mTexId, mRendererTask.mTexMatrix, 0);
						captureSurface.swap();
				        buf.clear();
				        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
//				        if (DEBUG) Log.v(TAG, "save pixels to file:" + captureFile);
				        Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.PNG;
				        if (captureFile.toString().endsWith(".jpg")) {
				        	compressFormat = Bitmap.CompressFormat.JPEG;
				        }
				        BufferedOutputStream os = null;
						try {
					        try {
					            os = new BufferedOutputStream(new FileOutputStream(captureFile));
					            final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
						        buf.clear();
					            bmp.copyPixelsFromBuffer(buf);
					            bmp.compress(compressFormat, 90, os);
					            bmp.recycle();
					            os.flush();
					        } finally {
					            if (os != null) os.close();
					        }
						} catch (final FileNotFoundException e) {
							Log.w(TAG, "failed to save file", e);
						} catch (final IOException e) {
							Log.w(TAG, "failed to save file", e);
						}
					}
					captureFile = null;
					mSync.notifyAll();
				}	// end of synchronized (mSync)
			}	// end of for (; isRunning ;)
			// release resources
//			if (DEBUG) Log.v(TAG, "captureTask finishing");
			release();
//			if (DEBUG) Log.v(TAG, "captureTask finished");
		}

		private final void init() {
	    	egl = new EGLBase(mRendererTask.getContext(), false, false);
	    	captureSurface = egl.createOffscreen(mRendererTask.mVideoWidth, mRendererTask.mVideoHeight);
	    	drawer = new GLDrawer2D(true);
	    	drawer.getMvpMatrix()[5] *= -1.0f;	// flip up-side down
		}

		private final void release() {
			if (captureSurface != null) {
				captureSurface.makeCurrent();
				if (drawer != null) {
					drawer.release();
				}
				captureSurface.release();
				captureSurface = null;
			}
			drawer = null;
			if (egl != null) {
				egl.release();
				egl = null;
			}
		}
	};

}
