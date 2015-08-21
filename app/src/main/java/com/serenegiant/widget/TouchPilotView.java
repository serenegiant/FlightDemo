package com.serenegiant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class TouchPilotView extends SurfaceView {
	private static final boolean DEBUG = true;
	private static final String TAG = "CircleView";

	public interface TouchPilotListener {
		public void onDrawFinish(final TouchPilotView view);
	}

	private static final int MAX_POINTS = 2000;
	private static final int HALF_POINTS = MAX_POINTS / 2;

	private final Object mSync = new Object();				// 描画スレッドとの同期用
	private final Paint paint = new Paint();
	private final float points[] = new float[MAX_POINTS];    // 座標値
	private int pointIx;									// 描画点の現在個数

	private TouchPilotListener mListener;
	private boolean mHasSurface;							// Surfaceが存在しているかどうか
	private boolean mIsRunning;								// 描画スレッド実行中フラグ
	private boolean mRequestDraw;							// 描画要求フラグ

	public TouchPilotView(Context context) {
		this(context, null, 0);
	}

	public TouchPilotView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchPilotView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		getHolder().addCallback(mCallback);
	}

	public void resume() {
		if (DEBUG) Log.v(TAG, "resume:");
		synchronized (mSync) {
			new Thread(mDrawTask, "DrawTask").start();
			if (!mIsRunning) {
				try {
					mSync.wait();
				} catch (final InterruptedException e) {
				}
			}
		}
	}

	public void pause() {
		if (DEBUG) Log.v(TAG, "pause:");
		synchronized (mSync) {
			mIsRunning = false;
			mSync.notifyAll();
			try {
				mSync.wait();
			} catch (final InterruptedException e) {
			}
		}
	}

	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	public void setTouchPilotListener(final TouchPilotListener listener) {
		mListener = listener;
	}

	/**
	 * 設定してあるコールバックリスナーを取得
	 * @return
	 */
	public TouchPilotListener getTouchPilotListener() {
		return mListener;
	}

	private final SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
		@Override
		public void surfaceCreated(final SurfaceHolder holder) {
			if (DEBUG) Log.v(TAG, "surfaceCreated:");
			synchronized (mSync) {
				mHasSurface = true;
			}
		}
		@Override
		public void surfaceChanged(final SurfaceHolder holder, final int format, final int width, final int height) {
			if (DEBUG) Log.v(TAG, String.format("surfaceChanged:size(%d,%d)", width, height));
			if (width == 0 || height == 0) return;
			synchronized (mSync) {
				mRequestDraw = true;
				mSync.notifyAll();
			}
		}
		@Override
		public void surfaceDestroyed(final SurfaceHolder holder) {
			if (DEBUG) Log.v(TAG, "surfaceDestroyed:");
			synchronized (mSync) {
				mHasSurface = false;
				mSync.notifyAll();
			}
		}
	};

	/**
	 * 描画スレッドの実行Runnable
	 */
	private final Runnable mDrawTask = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mDrawTask:starting");
			synchronized (mSync) {
				mIsRunning = mRequestDraw = true;
				mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "mDrawTask:started");
			while (mIsRunning) {
				synchronized (mSync) {
					if (!mHasSurface) {
						try {
							if (DEBUG) Log.v(TAG, "wait for surface created");
							mSync.wait();
							if (DEBUG) Log.v(TAG, "surface created");
							continue;
						} catch (final InterruptedException e) {
							break;
						}
					}
					if (!mRequestDraw) {
						try {
							mSync.wait();
						} catch (final InterruptedException e) {
							break;
						}
					}
					if (mRequestDraw) {
						doDraw();
						mRequestDraw = false;
					}
				}
			}
			synchronized (mSync) {
				mIsRunning = false;
				mSync.notifyAll();
			}
			if (DEBUG) Log.v(TAG, "mDrawTask:finished");
		}
	};

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final int action = event.getAction();
		final float xx = event.getX();
		final float yy = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (DEBUG) Log.v(TAG, "ACTION_DOWN");
			synchronized (mSync) {
				pointIx = 0;
				addPoint(xx, yy, event.getEventTime());
				mRequestDraw = true;
				mSync.notifyAll();
			}
			return true;	// trueを返さないと他のイベントが来ない
		case MotionEvent.ACTION_UP:
			if (DEBUG) Log.v(TAG, "ACTION_UP:pointIx=" + pointIx);
			synchronized (mSync) {
				pointIx = 0;
			}
			if (mListener != null) {
				mListener.onDrawFinish(this);
			}
			return true;
		case MotionEvent.ACTION_MOVE:
//			if (DEBUG) Log.v(TAG, "ACTION_MOVE");
			synchronized (mSync) {
				addPoint(xx, yy, event.getEventTime());
				mRequestDraw = true;
				mSync.notifyAll();
			}
			return true;
		case MotionEvent.ACTION_CANCEL:
			if (DEBUG) Log.v(TAG, "ACTION_CANCEL");
			synchronized (mSync) {
				pointIx = 0;
				mRequestDraw = true;
				mSync.notifyAll();
			}
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * タッチ位置に従って線を描く
	 */
	private final void doDraw() {
		final SurfaceHolder holder = getHolder();
		final Canvas canvas = holder.lockCanvas();
		if (canvas != null)
			try {
				canvas.drawRGB(0xff, 0xff, 0xff);	// 白で塗りつぶす
				paint.setStyle(Paint.Style.STROKE);
				paint.setStrokeWidth(20);
				// 描画処理
				if (pointIx >= 4) {
					paint.setStrokeWidth(5.0f);
					paint.setColor(0xffff0000);	// 赤
					for (int i = 2; i < pointIx; i += 2) {
						canvas.drawLine(points[i-2], points[i-1], points[i], points[i+1], paint);
					}
				}
			} finally {
				holder.unlockCanvasAndPost(canvas);
			}
	}

	/**
	 * タッチ座標を追加
	 * @param xx
	 * @param yy
	 * @param tt
	 */
	private final void addPoint(final float xx, final float yy, final long tt) {
		points[pointIx] = xx;
		points[pointIx+1] = yy;
		pointIx = (pointIx + 2) % MAX_POINTS;
	}

}
