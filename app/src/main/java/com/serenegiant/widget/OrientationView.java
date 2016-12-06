package com.serenegiant.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.serenegiant.aceparrot.R;

/**
 * カメラのPan/Tiltに対応する位置に十字線を描くためのView
 */
public class OrientationView extends View {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = OrientationView.class.getSimpleName();

	private int mPan, mTilt;
	private float mScaleX, mScaleY;
	private final Paint mPaint = new Paint();
	private final PathEffect mPathEffect;
	private final Rect mBounds = new Rect();

	public OrientationView(final Context context) {
		this(context, null, 0);
	}

	public OrientationView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OrientationView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// XXX ハードウエアアクセれレーションが有効だと点線を描画できないので無効にする
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// Dip => px変換係数を取得
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mPathEffect = new DashPathEffect(new float[] { 15 * scale, 10 * scale }, 0);	// 15dp-10dp
//		mPathEffect = createSafeDashedPathEffect(20 * scale, 5 * scale, 0, 3 * scale);
		mPaint.setColor(getResources().getColor(R.color.QUATER_CLEAR_WHITE));
		mPaint.setStrokeWidth(3 * scale);	// 3dp
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setPathEffect(mPathEffect);
	}

	public synchronized void setPanTilt(final int pan, final int tilt) {
		if ((mPan != pan) || (mTilt != tilt)) {
			mPan = pan;
			mTilt = tilt;
			postInvalidate();
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mScaleX = w / 200f;
		mScaleY = h / 200f;
	}

	@Override
	protected synchronized void onDraw(final Canvas canvas) {
//		super.onDraw(canvas);	// このメソッドはにもしないのでコメントアウト
		getDrawingRect(mBounds);
		final float px = mPan * mScaleX + mBounds.centerX();
		final float py = mTilt * mScaleY + mBounds.centerY();
		mPaint.setPathEffect(mPathEffect);
		canvas.drawLine(0, py, getWidth(), py, mPaint);
		canvas.drawLine(px, 0, px, getHeight(), mPaint);
	}

	private PathDashPathEffect createSafeDashedPathEffect(int dashLength, int spaceLength, int offset, int strokeWidth) {
		final Path p = new Path();
		p.moveTo(0, strokeWidth / 2);
		p.lineTo(dashLength, strokeWidth / 2);
		p.lineTo(dashLength, -strokeWidth / 2);
		p.lineTo(0, -strokeWidth / 2);
		return new PathDashPathEffect(p, dashLength + spaceLength, offset, PathDashPathEffect.Style.ROTATE);
	}
}
