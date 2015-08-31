package com.serenegiant.widget.gl;

import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public abstract class GLModelView extends GLSurfaceView implements IModelView {

	protected enum ModelState {
		INITIALIZE,
		RESUME0,
		RESUME1,
		RUNNING,
		PAUSE,
		IDLE,
		FINISH
	}

	protected GLGraphics glGraphics;
	protected Screen mScreen;
	/** UIスレッドからscreenを切り替える際の中継用変数 */
	protected Screen mNextScreen;
	protected ModelState mState = ModelState.INITIALIZE;
	protected final Object mStateSyncObj = new Object();
	protected long mPrevTime = System.nanoTime();
	private TimerThread timerThread = null;
	private long mGameThreadID;
	private boolean mIsLandscape;
	//
	protected LoadableInterface mLoadableInterface;
	// 描画関係
	protected final Object mRendererSyncObj = new Object();
	protected boolean mForceRender = false;			// 強制描画フラグ, 画面更新時間になっていなくてもこのフラグがtrueならdrawを呼び出す
	protected float mFpsRequested;
	protected boolean mContinueRendering = false;	// 連続描画モード?
	protected long mUpdateIntervals = 0;			// 連続描画間隔?
	protected boolean glActive = false;

	public GLModelView(Context context) {
		this(context, null);
	}

	public GLModelView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onResume() {
		super.onResume();
		startTimerThread();
	}

	@Override
	public void onPause() {
//		if (DEBUG) Log.v(TAG, "GLGameFragment#onPause:isFinishing=" + getActivity().isFinishing());
		stopTimerThread();
		glActive = false;
		super.onPause();
	}

	/**
	 * タイマースレッドが存在しなければ生成する
	 */
	private void startTimerThread() {
//		if (DEBUG) Log.v(TAG, "GLGameFragment#startTimerThread");
		if (timerThread == null) {
			timerThread = new TimerThread();
			timerThread.start();
		}
	}

	/**
	 * タイマースレッドが存在すれば停止・破棄する
	 */
	private void stopTimerThread() {
//		if (DEBUG) Log.v(TAG, "GLGameFragment#stopTimerThread");
		if (timerThread != null) {
			timerThread.pause();
			timerThread = null;
		}
	}
	/**
	 * 一定時間毎にGLSurfaceViewのレンダラースレッドを起床させるスレッド
	 * このスレッドが起床要求するかtouchEvent/accelEventが発生するとレンダラースレッドが起床される
	 *
	 */
	private class TimerThread extends Thread {
		public volatile boolean isRunning = true;

		@Override
		public void run() {
			long prevTime = System.nanoTime();
			long delta, nano_time;

			setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);	// requestRenderが呼ばれた時だけ描画する
			while (isRunning) {
				nano_time = System.nanoTime();
				delta = nano_time - prevTime;
				synchronized(mRendererSyncObj) {
					if (mForceRender || (delta > mUpdateIntervals)) {			// 起床要求の時間確認
						prevTime = nano_time;
						requestRender();				// レンダラースレッドを起床要求
						mForceRender = false;
					} else {
						try {
							mRendererSyncObj.wait(5);	// 5msecが経過するか、別スレッドからnotifyされるまで待つ。
						} catch (final InterruptedException e) {
						}
					}
				}
			}
			setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);	// 連続描画に戻す
			synchronized (mRendererSyncObj) {
				mRendererSyncObj.notifyAll();
			}
		}

		public void pause() {
//			if (DEBUG) Log.v(TAG, "GLGameFragment#timerthread#pause");
			isRunning = false;
			synchronized (mRendererSyncObj) {
				while (true) {
					try {
						mRendererSyncObj.wait();
						break;
					} catch (final InterruptedException e) {
						// リトライ
					}
				}
			}
			while (isAlive()) {
				try {
					join();
					break;
				} catch (final InterruptedException e) {
					// リトライする
				}
			}
		}
	}

	// GLSurfaceViewのレンダラー
	private final GLSurfaceView.Renderer renderer = new Renderer() {
		float deltaTime;
		@Override
		public void onDrawFrame(final GL10 gl) {
			ModelState localState = null;
			synchronized (mStateSyncObj) {
				localState = mState;
			}

			if (localState == ModelState.RUNNING) {	// 実行
				deltaTime = (System.nanoTime() - mPrevTime) / 1000000000.0f;
				mPrevTime = System.nanoTime();
				mScreen.update(deltaTime);
				mScreen.draw(deltaTime);
				// 出来るだけこのタイミングでガベージコレクションが走って欲しいんだけど、頻度が高すぎる
			}
			else if (localState == ModelState.PAUSE) {		// 中断処理
				mScreen.pause();
				if (mLoadableInterface != null) {
					mLoadableInterface.pause();
				}
				synchronized (mStateSyncObj) {
//					localState = GLGameState.IDLE;
					mState = ModelState.IDLE;
					mStateSyncObj.notifyAll();
				}
			}
			else if (localState == ModelState.FINISH) {	// 終了処理
				mScreen.pause();
				mScreen.dispose();
				if (mLoadableInterface != null) {
					mLoadableInterface.dispose();
					mLoadableInterface = null;
				}
				synchronized (mStateSyncObj) {
					mState = ModelState.IDLE;
					mStateSyncObj.notifyAll();
				}
			}
		}

		@Override
		public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
//			if (DEBUG) Log.v(TAG, "GLGameFragment#onSurfaceChanged:width=" + width + " height=" + height);
			mIsLandscape = (width > height);
			glGraphics.setGL(gl);	// 2013/11/20追加 なぜかonSurfaceCreatedで正しくセットされてない時がある
			synchronized (mStateSyncObj) {
				if (mState == ModelState.RUNNING) {
					setScreenSize(mScreen, width, height);
				} else {
					setScreen(mScreen == null ? getScreen() : mScreen);
					mState = ModelState.RUNNING;
				}
			}
			gl.glViewport(0, 0, width, height);
		}

		@Override
		public void onSurfaceCreated(final GL10 gl, final EGLConfig config) {
//			if (DEBUG) Log.v(TAG, "GLGameFragment#onSurfaceCreated");
			ModelState localState;
			glActive = true;
			glGraphics.setGL(gl);
			synchronized (mStateSyncObj) {
				mGameThreadID = Thread.currentThread().getId(); // 2013/07/22
				if (mScreen == null) {	// 2013/06/05
					mState = ModelState.INITIALIZE;
				}
				localState = mState;
			}
			if (localState == ModelState.INITIALIZE) {
				initialize();
			}
			if (mLoadableInterface != null) {
				if (localState == ModelState.INITIALIZE) {
					mLoadableInterface.load(getContext());
				} else {
					mLoadableInterface.reload(getContext());
				}
				mLoadableInterface.resume(getContext());
			}
			mPrevTime = System.nanoTime();
/*			setScreen(mScreen == null ? getStartScreen() : mScreen);
			synchronized (mStateSyncObj) {
				mState = GameState.RUNNING;
			} */
			System.gc();
		}
	};

	protected void initialize() {
	}

	/**
	 * サイズ変更時の処理
	 * onSurfaceChangedから呼び出される
	 * @param screen
	 * @param width
	 * @param height
	 */
	protected void setScreenSize(final Screen screen, final int width, final int height) {
		if (screen != null) {
			screen.setScreenSize(width, height);
			screen.onSizeChanged(width, height);
			requestRender();
		}
	}

	public void setScreen(final Screen screen) {
		// 呼び出し元のスレッドIDに応じて直接呼び出すかrunnable経由で呼び出すかを切り替える
		if (mGameThreadID == Thread.currentThread().getId()) {	// ゲームスレッド内から呼び出された時
			internalSetScreen(screen);							// 直接実行する
		} else {												// 他スレッドから呼び出された時
			mNextScreen = screen;
			queueEvent(mChangeScreenRunnable);					// runnableをゲームスレッドに渡して実行してもらう
		}
	}

	/**
	 * screen切り替え処理の実体(無限ループにならないようにrunnableからsetScreenを再び呼び出さないようにするため別メソッド化)
	 * @param screen
	 */
	protected void internalSetScreen(final Screen screen) {
//		if (DEBUG) Log.v(TAG, "BaseGameFragment#internalSetScreen");
		if (screen == null)
			throw new IllegalArgumentException("Screen must not be null");
		if ((mScreen != null) && (mScreen != screen)) {
			mScreen.pause();
			mScreen.dispose();
		}
		System.gc();	// 2013/05/24
		setScreenSize(screen, getWidth(), getHeight());
		synchronized (mStateSyncObj) {
			mScreen = screen;
		}
		screen.resume();
		requestRender();	// 2013/06/23
//		mNextScreen = null;
	}

	/**
	 * UIスレッドからscreenを切り替える際にゲームスレッドに変更を要求するためのrunnable
	 */
	protected final Runnable mChangeScreenRunnable = new Runnable() {
		@Override
		public void run() {
//			if (DEBUG) Log.v(TAG, "BaseGameFragment#mChangeScreenRunnable#run");
			internalSetScreen(mNextScreen);
		}
	};

	/**
	 * 表示画面を生成
	 * @return
	 */
	protected abstract Screen getScreen();

	@Override
	public FileIO getAssetIO() {
		final Context app = getContext().getApplicationContext();
		return (app instanceof IModelViewApplication) ? ((IModelViewApplication)app).getAssetIO() : null;
	}

	@Override
	public FileIO getExtFileIO() {
		final Context app = getContext().getApplicationContext();
		return (app instanceof IModelViewApplication) ? ((IModelViewApplication)app).getExtFileIO() : null;
	}

	@Override
	public FileIO getFileIO() {
		final Context app = getContext().getApplicationContext();
		return (app instanceof IModelViewApplication) ? ((IModelViewApplication)app).getFileIO() : null;
	}

	@Override
	public GLGraphics getGLGraphics() {
		return glGraphics;
	}

	@Override
	public int getNextPickId() {
		// FIXME 未実装
		return 0;
	}

	@Override
	public boolean isLandscape() {
		return mIsLandscape;
	}
}
