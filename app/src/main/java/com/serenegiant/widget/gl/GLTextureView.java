package com.serenegiant.widget.gl;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import com.serenegiant.glutils.EGLBase;
import com.serenegiant.glutils.EglTask;

import java.lang.ref.WeakReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLTextureView extends TextureView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = GLTextureView.class.getSimpleName();

	/**
	 * The renderer only renders
	 * when the surface is created, or when {@link #requestRender} is called.
	 *
	 * @see #getRenderMode()
	 * @see #setRenderMode(int)
	 * @see #requestRender()
	 */
	public final static int RENDERMODE_WHEN_DIRTY = 0;
	/**
	 * The renderer is called
	 * continuously to re-render the scene.
	 *
	 * @see #getRenderMode()
	 * @see #setRenderMode(int)
	 */
	public final static int RENDERMODE_CONTINUOUSLY = 1;

	public interface Renderer {
		/**
		 * created EGLSurface.
		 * {@link #onSurfaceChanged(GL10, int, int)}
		 */
		public void onSurfaceCreated(GL10 gl, EGLConfig config);

		/**
		 * remake EGLSurface.
		 */
		public void onSurfaceChanged(GL10 gl, int width, int height);

		/**
		 * rendering.
		 */
		public void onDrawFrame(GL10 gl);

		/**
		 * destroyed
		 * @param gl
		 */
		public void onSurfaceDestroyed(GL10 gl);
	}

	private final Object mSync = new Object();
	private RendererTask mRendererTask;
	private Renderer mRenderer;
	private volatile boolean mIsActive;
	private int mRenderMode = RENDERMODE_CONTINUOUSLY;

	public GLTextureView(final Context context) {
		this(context, null, 0);
	}

	public GLTextureView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GLTextureView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setSurfaceTextureListener(mSurfaceTextureListener);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (DEBUG) Log.v(TAG, "onAttachedToWindow:");
	}

	@Override
	protected void onDetachedFromWindow() {
		if (DEBUG) Log.v(TAG, "onDetachedFromWindow:");
		super.onDetachedFromWindow();
	}

	public void setRenderer(final Renderer renderer) {
		if (DEBUG) Log.v(TAG, "setRenderer:");
		synchronized (mSync) {
			mRenderer = renderer;
		}
	}

	public Renderer getRenderer() {
		synchronized (mSync) {
			return mRenderer;
		}
	}

	public void setRenderMode(final int render_mode) {
		mRenderMode = render_mode;
	}

	public int getRenderMode() {
		return mRenderMode;
	}

	public void onResume() {
		if (DEBUG) Log.v(TAG, "onResume:");
		mIsActive = true;
		if (mRendererTask == null) {
			mRendererTask = new RendererTask(this);
			new Thread(mRendererTask, "RendererThread").start();
			mRendererTask.waitReady();
		}
		mRendererTask.offer(CMD_DRAW);
	}

	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		mIsActive = false;
		if (mRendererTask != null) {
			mRendererTask.release();
			mRendererTask = null;
		}
	}

	public void requestRender() {
		if (mRendererTask != null) {
			mRendererTask.offer(CMD_DRAW);
		}
	}

	public void queueEvent(final Runnable task) {
		if ((mRendererTask != null) && (task != null)) {
			mRendererTask.queueEvent(task);
		}
	}

	private void callOnSurfaceCreated(final GL10 gl, final EGLConfig config) {
		synchronized (mSync) {
			if (mRenderer != null) {
				try {
					mRenderer.onSurfaceCreated(gl, config);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private void callOnSurfaceChanged(final GL10 gl, final int width, final int height) {
		synchronized (mSync) {
			if (mRenderer != null) {
				try {
					mRenderer.onSurfaceChanged(gl, width, height);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private void callOnDrawFrame(final GL10 gl) {
		synchronized (mSync) {
			if (mRenderer != null) {
				try {
					mRenderer.onDrawFrame(gl);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private void callOnSurfaceDestroyed(final GL10 gl) {
		synchronized (mSync) {
			if (mRenderer != null) {
				try {
					mRenderer.onSurfaceDestroyed(gl);
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:" + mRendererTask);
			if (mRendererTask != null) {
				mRendererTask.offer(CMD_AVAILABLE, width, height, surface);
				if (DEBUG) Log.v(TAG, "offer CMD_AVAILABLE");
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:" + mRendererTask);
			// XXX ここに来るときには既にmRendererTaskは開放されているかも
			if (mRendererTask != null) {
				mRendererTask.offer(CMD_SIZE_CHANGED, width, height, surface);
				if (DEBUG) Log.v(TAG, "offer CMD_SIZE_CHANGED");
			}
		}

		@Override
		public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
			if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:" + mRendererTask);
			if (mRendererTask != null) {
				mRendererTask.offerFirst(CMD_DESTROYED, 0, 0, surface);
				if (DEBUG) Log.v(TAG, "offer CMD_DESTROYED");
			}
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
//			if (mRendererTask != null) {
//				mRendererTask.offerFirst(CMD_UPDATED, 0, 0, surface);
//			}
		}
	};

	private static final int CMD_AVAILABLE = 1;
	private static final int CMD_SIZE_CHANGED = 2;
	private static final int CMD_DESTROYED = 3;
	private static final int CMD_UPDATED = 4;
	private static final int CMD_DRAW = 5;
	private static final int CMD_RUN = 6;
	private static class RendererTask extends EglTask {
		private final WeakReference<GLTextureView> mWeakParent;
		private EGLBase.EglSurface mRenderSurface;	// TextureViewへOpenGL|ESを使って描画するためのeglSurface
		private GL10 mGl;
		public RendererTask(final GLTextureView parent) {
			super(1, null, EglTask.EGL_FLAG_DEPTH_BUFFER);	// GLES1で初期化
			if (DEBUG) Log.v(TAG, "RendererTask:コンストラクタ");
			mWeakParent = new WeakReference<GLTextureView>(parent);
		}

		@Override
		protected void onInit(final int request, final int flags, final int max_version, final Object shared_context) {
			if (DEBUG) Log.v(TAG, "RendererTask#onInit:");
			super.onInit(request, flags, max_version, shared_context);
			if (DEBUG) Log.v(TAG, "RendererTask#onInit:終了");
		}

		@Override
		protected void onStart() {
			if (DEBUG) Log.v(TAG, "RendererTask#onStart:");
		}

		@Override
		protected void onStop() {
			if (DEBUG) Log.v(TAG, "RendererTask#onStop:");
			// FIXME ここで#callOnSurfaceDestroyedを呼び出すようにした方がいいかも
			if (mRenderSurface != null) {
				mRenderSurface.release();
				mRenderSurface = null;
			}
			mGl = null;
		}

		@Override
		protected boolean processRequest(final int request, final int arg1, final int arg2, final Object obj) {
			final GLTextureView parent = mWeakParent.get();
			switch (request) {
			case CMD_AVAILABLE:
				if (DEBUG) Log.v(TAG, "CMD_AVAILABLE:");
				mRenderSurface = getEgl().createFromSurface(obj);
				mGl = (GL10)getGl();
				if (parent != null) {
					parent.callOnSurfaceCreated(mGl, getEgl().getConfig());
					parent.callOnSurfaceChanged(mGl, arg1, arg2); // GLSurfaceViewに合わせるため
				}
				offer(CMD_DRAW);
				break;
			case CMD_SIZE_CHANGED:
				if (DEBUG) Log.v(TAG, "CMD_SIZE_CHANGED:");
				if (parent != null) {
					parent.callOnSurfaceChanged(mGl, arg1, arg2);
				}
				break;
			case CMD_DESTROYED:
				if (DEBUG) Log.v(TAG, "CMD_DESTROYED:");
				// FIXME ここには来ないかも
				if (parent != null) {
					parent.callOnSurfaceDestroyed(mGl);
				}
				mGl = null;
				mRenderSurface.release();
				mRenderSurface = null;
				makeCurrent();
				break;
			case CMD_UPDATED:
				if (DEBUG) Log.v(TAG, "CMD_UPDATED:");
				// ここは来ない
				break;
			case CMD_DRAW:
				if (mRenderSurface != null) {
					if (parent != null) {
//						if (DEBUG) Log.v(TAG, "CMD_DRAW:");
						mRenderSurface.makeCurrent();
//						mGl.glClearColor(0, 0, 0, 0);
//						mGl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
						parent.callOnDrawFrame(mGl);
						mRenderSurface.swap();
						if (parent.mIsActive && (parent.mRenderMode == RENDERMODE_CONTINUOUSLY)) {
							try {
								Thread.sleep(10);
							} catch (final InterruptedException e) {
							}
							offer(CMD_DRAW);
						}
					}
				}
				break;
			}
			return false;
		}
	}
}
