package com.serenegiant.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import com.serenegiant.graphics.BrushDrawable;

import java.util.Random;

public class TouchPilotView extends View {
	private static final boolean DEBUG = true;
	private static final String TAG = "TouchPilotView";

	public interface TouchPilotListener {
		public void onDrawFinish(final TouchPilotView view);
	}

	private static final int MAX_POINTS = 2000;
	private static final int HALF_POINTS = MAX_POINTS / 2;

	private static final float TO_DEGREE = (float)(180 / Math.PI);
	private static final int FADE_ALPHA = 0x06;
	private static final int MAX_FADE_STEPS = 256 / FADE_ALPHA + 4;
	private static final int TRACKBALL_SCALE = 10;
	private static final int SPLAT_VECTORS = 40;

	public enum PaintMode {
		Draw, Splat, Erase,
	}

	private final Random mRandom = new Random();
	private final Paint mFadePaint = new Paint();
	private final Paint mPaint = new Paint();
	private final float points[] = new float[MAX_POINTS];	// 座標値
	private int pointIx;									// 描画点の現在個数

	/** タッチ位置 */
	private float mCurX, mCurY;
	//	private int mOldButtonState;
	private int mFadeSteps = MAX_FADE_STEPS;
	/** オフスクリーン描画用のBitmap */
	private Bitmap mBitmap;
	/** オフスクリーン描画用のCanvas */
	private Canvas mCanvas;
	private int mClearColor = Color.BLACK;
	private int mPaintColor = Color.RED;
	private Shader mBrushShader;
	private BrushDrawable mBrushDrawable;
	// 描画領域
	private int mMinX = Integer.MAX_VALUE;
	private int mMaxX = Integer.MIN_VALUE;
	private int mMinY = Integer.MAX_VALUE;
	private int mMaxY = Integer.MIN_VALUE;
	/**
	 * 描画モード
	 */
	private PaintMode mPaintMode = PaintMode.Draw;

	private TouchPilotListener mListener;

	public TouchPilotView(Context context) {
		this(context, null, 0);
	}

	public TouchPilotView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchPilotView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setFocusable(true);
		mPaint.setAntiAlias(true);
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

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw,
									final int oldh) {
		int curW = mBitmap != null ? mBitmap.getWidth() : 0;
		int curH = mBitmap != null ? mBitmap.getHeight() : 0;
		if (curW >= w && curH >= h) {
			return;
		}

		if (curW < w)
			curW = w;
		if (curH < h)
			curH = h;

		final Bitmap newBitmap = Bitmap.createBitmap(curW, curH, Bitmap.Config.ARGB_8888);
		final Canvas newCanvas = new Canvas();
		newCanvas.setBitmap(newBitmap);
		if (mBitmap != null) {
			newCanvas.drawBitmap(mBitmap, 0, 0, null);
		}
		mBitmap = newBitmap;
		mCanvas = newCanvas;
		mFadeSteps = MAX_FADE_STEPS;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}
	}

	@Override
	public boolean onTrackballEvent(final MotionEvent event) {
		final int action = event.getActionMasked();
		if (action == MotionEvent.ACTION_DOWN) {
			// Advance color when the trackball button is pressed.
			// advanceColor();
		}

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE) {
			final int N = event.getHistorySize();
			final float scaleX = event.getXPrecision() * TRACKBALL_SCALE;
			final float scaleY = event.getYPrecision() * TRACKBALL_SCALE;
			for (int i = 0; i < N; i++) {
				moveTrackball(event.getHistoricalX(i) * scaleX,
								 event.getHistoricalY(i) * scaleY);
			}
			moveTrackball(event.getX() * scaleX, event.getY() * scaleY);
		}
		return true;
	}

	private void moveTrackball(final float deltaX, final float deltaY) {
		final int curW = mBitmap != null ? mBitmap.getWidth() : 0;
		final int curH = mBitmap != null ? mBitmap.getHeight() : 0;

		mCurX = Math.max(Math.min(mCurX + deltaX, curW - 1), 0);
		mCurY = Math.max(Math.min(mCurY + deltaY, curH - 1), 0);
		paint(PaintMode.Draw, mCurX, mCurY);
	}

	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		final int action = event.getActionMasked();	// event.getAction();
		final float xx = event.getX();
		final float yy = event.getY();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (DEBUG) Log.v(TAG, "ACTION_DOWN");
			pointIx = 0;
			addPoint(xx, yy, event.getEventTime());
//			return true;	// trueを返さないと他のイベントが来ない
		case MotionEvent.ACTION_UP:
			if (DEBUG) Log.v(TAG, "ACTION_UP:pointIx=" + pointIx);
			pointIx = 0;
			if (mListener != null) {
				mListener.onDrawFinish(this);
			}
//			return true;
		case MotionEvent.ACTION_MOVE:
//			if (DEBUG) Log.v(TAG, "ACTION_MOVE");
			addPoint(xx, yy, event.getEventTime());
//			return true;
		case MotionEvent.ACTION_CANCEL:
			if (DEBUG) Log.v(TAG, "ACTION_CANCEL");
			pointIx = 0;
//			return true;
		}
//		final int buttonState = event.getButtonState();
//		final int pressedButtons = buttonState & ~mOldButtonState;
//		mOldButtonState = buttonState;

/*		if ((pressedButtons & MotionEvent.BUTTON_SECONDARY) != 0) {
			// Advance color when the right mouse button or first stylus button
			// is pressed.
			// advanceColor();
		} */

/*		PaintMode mode;
		if ((buttonState & MotionEvent.BUTTON_TERTIARY) != 0) {
			// Splat paint when the middle mouse button or second stylus button　is pressed.
			mode = PaintMode.Splat;
		} else if (isTouch || (buttonState & MotionEvent.BUTTON_PRIMARY) != 0) {
			// Draw paint when touching or if the primary button is pressed.
			mode = PaintMode.Draw;
		} else {
			// Otherwise, do not paint anything.
			return false;
		} */

		if (action == MotionEvent.ACTION_DOWN
				|| action == MotionEvent.ACTION_MOVE
				|| action == MotionEvent.ACTION_HOVER_MOVE) {
			final int N = event.getHistorySize();
			final int P = event.getPointerCount();
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < P; j++) {
					paint(getPaintModeForTool(event.getToolType(j), mPaintMode),
							 event.getHistoricalX(j, i),
							 event.getHistoricalY(j, i),
							 event.getHistoricalPressure(j, i),
							 event.getHistoricalTouchMajor(j, i),
							 event.getHistoricalTouchMinor(j, i),
							 event.getHistoricalOrientation(j, i),
							 event.getHistoricalAxisValue(
															 MotionEvent.AXIS_DISTANCE, j, i),
							 event.getHistoricalAxisValue(
															 MotionEvent.AXIS_TILT, j, i)
					);
				}
			}
			for (int j = 0; j < P; j++) {
				paint(getPaintModeForTool(event.getToolType(j), mPaintMode),
						 event.getX(j),
						 event.getY(j),
						 event.getPressure(j),
						 event.getTouchMajor(j),
						 event.getTouchMinor(j),
						 event.getOrientation(j),
						 event.getAxisValue(MotionEvent.AXIS_DISTANCE, j),
						 event.getAxisValue(MotionEvent.AXIS_TILT, j)
				);
			}
			mCurX = event.getX();
			mCurY = event.getY();
			mMinX = Math.min(mMinX, (int)mCurX);
			mMaxX = Math.max(mMaxX, (int)mCurX);
			mMinY = Math.min(mMinY, (int)mCurY);
			mMaxY = Math.max(mMaxY, (int)mCurY);
		}
//		return super.onTouchEvent(event);
		return true;
	}

	public void clear() {
		if (mCanvas != null) {
			mCanvas.drawColor(mClearColor, PorterDuff.Mode.CLEAR);	// Viewを透過させる
//			mPaint.setColor(mClearColor);
//			mCanvas.drawPaint(mPaint);
			invalidate();

			mFadeSteps = MAX_FADE_STEPS;
		}
		mMinX = mMinY = Integer.MAX_VALUE;
		mMaxX = mMaxY = Integer.MIN_VALUE;
	}

	private PaintMode getPaintModeForTool(final int toolType, final PaintMode defaultMode) {
		if (toolType == MotionEvent.TOOL_TYPE_ERASER) {
			return PaintMode.Erase;
		}
		return defaultMode;
	}

	private void paint(final PaintMode mode, final float x, final float y) {
		paint(mode, x, y, 1.0f, 0, 0, 0, 0, 0);
	}

	/**
	 * 描画の実体
	 * @param mode
	 * @param x
	 * @param y
	 * @param pressure
	 * @param major
	 * @param minor
	 * @param orientation
	 * @param distance
	 * @param tilt
	 */
	private void paint(final PaintMode mode, final float x, final float y,
						  final float pressure, float major, float minor,
						  final float orientation, final float distance, final float tilt) {
		if (mBitmap != null) {
			if (major <= 0 || minor <= 0) {
				// If size is not available, use a default value.
				major = minor = 16;
			}
			if (mBrushDrawable != null) {
				final int alpha_org = mBrushDrawable.getPaintAlpha();
				final float alpha = Math.min(pressure * 128, 255.0f);
				major *= (alpha / 500.0f);
				minor *= (alpha / 500.0f);
				switch (mode) {
				case Draw:
					mBrushDrawable.setPaintAlpha((int)(alpha * (alpha_org / 255.0f)));
					drawBrush(mCanvas, x, y, major, minor, orientation);
					break;
				case Erase:
					mBrushDrawable.setPaintAlpha((int)(alpha * (alpha_org / 255.0f)));
					drawBrush(mCanvas, x, y, major, minor, orientation);
					break;
				case Splat:
					mPaint.setColor(mPaintColor);
					mPaint.setAlpha(64);
					drawSplat(mCanvas, x, y, orientation, distance, tilt, mPaint);
					break;
				}
				mBrushDrawable.setPaintAlpha(alpha_org);
			} else {
				final int alpha_org = mPaint.getAlpha();
				final float alpha = Math.min(pressure * 128, 255.0f);
				major *= (alpha / 500.0f);
				minor *= (alpha / 500.0f);
				switch (mode) {
				case Draw:
					mPaint.setShader(mBrushShader);
					mPaint.setColor(mPaintColor);
					mPaint.setAlpha((int)(alpha * (alpha_org / 255.0f)));
					drawPaint(mCanvas, x, y, major, minor, orientation, mPaint);
					break;
				case Erase:
					mPaint.setColor(mClearColor);
					mPaint.setAlpha((int)(alpha * (alpha_org / 255.0f)));
					drawPaint(mCanvas, x, y, major, minor, orientation, mPaint);
					break;
				case Splat:
					mPaint.setColor(mPaintColor);
					mPaint.setAlpha(64);
					drawSplat(mCanvas, x, y, orientation, distance, tilt, mPaint);
					break;
				}
				mPaint.setAlpha(alpha_org);
			}
			mMinX = Math.min(mMinX, (int)(x - major));
			mMaxX = Math.max(mMaxX, (int)(x + major));
			mMinY = Math.min(mMinY, (int)(y - minor));
			mMaxY = Math.max(mMaxY, (int)(y + minor));
		}
		mFadeSteps = 0;
		invalidate();
	}

	/**
	 * 描画用のRectF
	 */
	private final RectF mBrushRect = new RectF();
	private final Rect mBrushBounds = new Rect();
	/**
	 * Draw an oval.
	 *
	 * When the orienation is 0 radians, orients the major axis vertically,
	 * angles less than or greater than 0 radians rotate the major axis left or
	 * right.
	 */
	private void drawBrush(final Canvas canvas, final float x, final float y,
							  final float major, final float minor, final float orientation) {

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		try {
			canvas.translate(x, y);
			canvas.rotate(orientation * TO_DEGREE);
			mBrushRect.left = -minor;
			mBrushRect.right = minor;
			mBrushRect.top = -major;
			mBrushRect.bottom = major;
			mBrushRect.round(mBrushBounds);
			mBrushDrawable.setBounds(mBrushBounds);
			mBrushDrawable.draw(canvas);
		} finally {
			canvas.restore();
		}
	}

	private void drawPaint(final Canvas canvas, final float x, final float y,
							  final float major, final float minor, final float orientation, final Paint paint) {

		canvas.save(Canvas.MATRIX_SAVE_FLAG);
		try {
			canvas.translate(x, y);
			canvas.rotate(orientation * TO_DEGREE);
			mBrushRect.left = -minor;
			mBrushRect.right = minor;
			mBrushRect.top = -major;
			mBrushRect.bottom = major;
			canvas.drawOval(mBrushRect, paint);	// 円(楕円)でプロットするとき
//			canvas.drawRect(mBrushRect, paint);	// 四角でプロットするとき
		} finally {
			canvas.restore();
		}
	}

	/**
	 * Splatter paint in an area.
	 *
	 * Chooses random vectors describing the flow of paint from a round nozzle
	 * across a range of a few degrees. Then adds this vector to the direction
	 * indicated by the orientation and tilt of the tool and throws paint at the
	 * canvas along that vector.
	 *
	 * Repeats the process until a masterpiece is born.
	 */
	private void drawSplat(final Canvas canvas, final float x, final float y,
							  final float orientation, final float distance, final float tilt,
							  final Paint paint) {
		final float z = distance * 2 + 10;

		// Calculate the center of the spray.
		final float nx = (float) (Math.sin(orientation) * Math.sin(tilt));
		final float ny = (float) (-Math.cos(orientation) * Math.sin(tilt));
		final float nz = (float) Math.cos(tilt);
		if (nz < 0.05) {
			return;
		}
		final float cd = z / nz;
		final float cx = nx * cd;
		final float cy = ny * cd;

		for (int i = 0; i < SPLAT_VECTORS; i++) {
			// Make a random 2D vector that describes the direction of a speck
			// of paint
			// ejected by the nozzle in the nozzle's plane, assuming the tool is
			// perpendicular to the surface.
			final double direction = mRandom.nextDouble() * Math.PI * 2;
			final double dispersion = mRandom.nextGaussian() * 0.2;
			double vx = Math.cos(direction) * dispersion;
			double vy = Math.sin(direction) * dispersion;
			double vz = 1;

			// Apply the nozzle tilt angle.
			double temp = vy;
			vy = temp * Math.cos(tilt) - vz * Math.sin(tilt);
			vz = temp * Math.sin(tilt) + vz * Math.cos(tilt);

			// Apply the nozzle orientation angle.
			temp = vx;
			vx = temp * Math.cos(orientation) - vy * Math.sin(orientation);
			vy = temp * Math.sin(orientation) + vy * Math.cos(orientation);

			// Determine where the paint will hit the surface.
			if (vz < 0.05) {
				continue;
			}
			final float pd = (float) (z / vz);
			final float px = (float) (vx * pd);
			final float py = (float) (vy * pd);

			// Throw some paint at this location, relative to the center of the spray.
			mCanvas.drawCircle(x + px - cx, y + py - cy, 1.0f, paint);
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
