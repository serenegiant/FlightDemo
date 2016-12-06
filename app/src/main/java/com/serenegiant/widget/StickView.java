package com.serenegiant.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.serenegiant.aceparrot.R;

public class StickView extends FrameLayout {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "StickView";

	private static final int DISABLE_COLOR = 0xcf777777;

	public interface OnStickMoveListener {
		/**
		 * スティックを動かした時のコールバック
		 * @param dx [-1,1]
		 * @param dy [-1,1]
		 */
		public void onStickMove(final View view, final float dx, final float dy);
	}

	private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
	private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();
	private static final float PI = (float)Math.PI;

	private int mMaxMove2;			// 移動可能距離の２乗
	private float mRadius;			// 移動可能距離
	private long startTime;			// 最初のタッチ時刻
	private int mStartX, mStartY;	// 最初のタッチ位置
	private int mPrevX, mPrevY;		// 前回のタッチ位置
	private boolean changed;

	private int mDisableColor = DISABLE_COLOR;
	private final View mStickStartView;	// 最初にタッチした位置に表示する薄い色のView
	private final View mStickView;		// タッチに追随して表示する
	private final Rect mWorkBounds = new Rect();
	private int mMaxWidth = -1, mMaxHeight = -1;
	private int mParentPaddingLeft, mParentPaddingTop, mParentPaddingRight, mParentPaddingBottom;

	private OnStickMoveListener mOnStickMoveListener;

	public StickView(Context context) {
		this(context, null, 0);
	}

	public StickView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public StickView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.StickView, defStyleAttr, 0);
		final int stick_layout_id = attributesArray.getResourceId(R.styleable.StickView_stick_image_layout, 0);
		int stick_width = attributesArray.getDimensionPixelSize(R.styleable.StickView_stick_width, 0);
		if (stick_width == 0) {
			stick_width = LayoutParams.WRAP_CONTENT;
		}
		int stick_height = attributesArray.getDimensionPixelSize(R.styleable.StickView_stick_height, 0);
		if (stick_width == 0) {
			stick_height = LayoutParams.WRAP_CONTENT;
		}
		mDisableColor = attributesArray.getColor(R.styleable.StickView_disable_color, DISABLE_COLOR);
		attributesArray.recycle();
		attributesArray = null;

		if (stick_layout_id != 0) {
			final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mStickStartView = inflater.inflate(stick_layout_id, this, false);
			mStickStartView.setAlpha(0.3f);
			mStickView = inflater.inflate(stick_layout_id, this, false);
			final LayoutParams params = new LayoutParams(stick_width, stick_height);
			addView(mStickStartView, params);
			addView(mStickView, params);
		} else {
			mStickStartView = mStickView = null;
		}
	}

	private boolean mOnTouchEvent;
	@Override
	public synchronized boolean onTouchEvent(final MotionEvent event) {
		if (mStickView != null && isEnabled()) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				if (DEBUG) Log.v(TAG, "ACTION_DOWN:");
				if (mMaxWidth <= 0 || mMaxHeight <= 0) {
					updateParentDimension();
				}
				startTime = event.getEventTime();
//				mStickView.getHitRect(mWorkBounds);	// 親の座標系で矩形を取得
				// 画面座標系でタッチ位置を取得
				mStartX = mPrevX = (int) event.getX();
				mStartY = mPrevY = (int) event.getY();
				move(mStartX, mStartY);
				callOnStickMove(0, 0);
				changed = true;
				mOnTouchEvent = true;
				break;
			}
			case MotionEvent.ACTION_MOVE: {
				final long t = event.getEventTime() - startTime;
				// 画面座標系でタッチ位置を取得
				int x = (int)event.getX();
				int y = (int)event.getY();
				int dx = x - mStartX;
				int dy = y - mStartY;
				final int dd = dx * dx + dy * dy;
				if (dd > mMaxMove2) {
					// 最初のタッチ位置から離れすぎた時の補正
					final float angle = (float) Math.atan2(dy, dx);
					// [-PI ... PI]を[0....1]に変換する
					float unit = angle / (2 * PI);
					if (unit < 0) {
						unit += 1;
					}
//					final float r = (float)Math.sqrt(dd);
					final float d = (360.0f - unit * 360.0f) / 180.0f * PI;
					x = mStartX + (int)(mRadius * (float) Math.cos(d));
					y = mStartY - (int)(mRadius * (float) Math.sin(d));
					dx = x - mStartX;
					dy = y - mStartY;
				}
				mStickView.getHitRect(mWorkBounds);	// 親の座標系で矩形を取得
				mWorkBounds.left += (x - mPrevX);
				mWorkBounds.top += (y - mPrevY);
				mWorkBounds.bottom = mWorkBounds.top + mStickView.getHeight();
				mWorkBounds.right = mWorkBounds.left + mStickView.getWidth();
				if ( (y >= mParentPaddingTop) && (x >= mParentPaddingLeft)
					&& (x < mMaxWidth) && (y < mMaxHeight) ) {

					// View自体を移動/リサイズ, これでセットするのは親の座標系での値
					mStickView.layout(mWorkBounds.left, mWorkBounds.top, mWorkBounds.right, mWorkBounds.bottom);
					changed = true;
				}
				mPrevX = x;
				mPrevY = y;
				callOnStickMove(dx, dy);
				break;
			}
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (DEBUG) Log.v(TAG, "ACTION_UP");
				if (changed) {
					// 中央に戻す
					if ((mMaxWidth != 0) && (mMaxHeight != 0)) {
						move(mMaxWidth >>> 1, mMaxHeight >>> 1);
					}
					callOnStickMove(0, 0);
				}
				if (!changed) {
					final long t = event.getEventTime() - startTime;
					if (t < TAP_TIMEOUT) {
						performClick();
						invalidate();
					} else if (t < LONG_PRESS_TIMEOUT) {
						performLongClick();
					}
				}
				mOnTouchEvent = false;
				break;
			}
			return true;
		} else {
			return super.onTouchEvent(event);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent event) {
		if (mStickView != null && isEnabled()) {
			return true;	// スティックViewが設定されていれば常に横取りする
		} else {
			return super.onInterceptTouchEvent(event);
		}
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if ((changed || (mMaxWidth == 0) || (mMaxHeight == 0)) && !mOnTouchEvent) {
			if (DEBUG) Log.v(TAG, "onLayout:changed=" + changed + "l=" + left + ",t=" + top +",r=" + right + ",b=" + bottom);
			updateParentDimension();
			// 中央に移動
		}
		if ((mMaxWidth != 0) && (mMaxHeight != 0) && !mOnTouchEvent) {
			move(mMaxWidth >>> 1, mMaxHeight >>> 1);
		}
	}

	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		setChildColorFilter(this, enabled);
	}

	/**
	 * 再帰的に子ViewのsetColorFilterを呼び出す
	 * @param root
	 * @param enabled
	 */
	private void setChildColorFilter(final ViewGroup root, final boolean enabled) {
		final int n = root.getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = root.getChildAt(i);
			if (child instanceof ImageView) {
				((ImageView)child).setColorFilter(enabled ? 0: mDisableColor);
			} else if (child instanceof ViewGroup) {
				setChildColorFilter((ViewGroup)child, enabled);
			}
		}
	}

	/**
	 * スティックを動かした時のコールバックリスナーを設定する
	 * @param listener
	 */
	public void setOnStickMoveListener(final OnStickMoveListener listener) {
		mOnStickMoveListener = listener;
	}

	/**
	 * スティックを動かした時のコールバックリスナーを取得する
	 * @return
	 */
	public OnStickMoveListener getOnStickMoveListener() {
		return mOnStickMoveListener;
	}

	/**
	 * スティックを動かした時のコールバックを呼び出す
	 * @param dx
	 * @param dy
	 */
	private void callOnStickMove(final float dx, final float dy) {
		if (mOnStickMoveListener != null) {
			try {
				// (dx,dy)を([-1,1],[-1,1])に変換してコールバックを呼び出す
				mOnStickMoveListener.onStickMove(this, dx / mRadius, dy / mRadius);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	/**
	 * Viewの大きさ・移動可能距離等の更新
	 */
	private synchronized void updateParentDimension() {
		getDrawingRect(mWorkBounds);
		mParentPaddingLeft = getPaddingLeft();
		mParentPaddingTop = getPaddingTop();
		mParentPaddingRight = getPaddingRight();
		mParentPaddingBottom = getPaddingBottom();
		mMaxWidth = mWorkBounds.width() - mParentPaddingRight;
		mMaxHeight = mWorkBounds.height() - mParentPaddingBottom;
		if (mStickView != null) {
			mStickView.getDrawingRect(mWorkBounds);    // ビュー座標で矩形を取得
			final int w = (mWorkBounds.width() >>> 1);
			final int h = (mWorkBounds.height() >>> 1);
			mMaxMove2 = (w * w + h * h) >>> 2;
			mRadius = (float)Math.sqrt(mMaxMove2);
		}
		if (DEBUG) Log.v(TAG, String.format("padding(L=%d,T=%d,R=%d,B=%d),max(%d,%d),r=%f",
			mParentPaddingLeft, mParentPaddingTop, mParentPaddingRight, mParentPaddingBottom, mMaxWidth, mMaxHeight, mRadius));
	}

	/**
	 * 指定した座標がスティックの中央になるようにスティック用のViewを移動する
	 * @param x
	 * @param y
	 */
	private synchronized void move(final int x, final int y) {
		mStickView.getHitRect(mWorkBounds);	// 親の座標系で矩形を取得
		if (DEBUG) Log.v(TAG, "move:" + mWorkBounds);
		final int w = mWorkBounds.width() / 2;
		final int h = mWorkBounds.height() / 2;
		mWorkBounds.set(x - w, y - h, x + w, y + h);
//		if (DEBUG) Log.v(TAG, "move:" + mWorkBounds);
		mStickStartView.layout(mWorkBounds.left, mWorkBounds.top, mWorkBounds.right, mWorkBounds.bottom);
		mStickView.layout(mWorkBounds.left, mWorkBounds.top, mWorkBounds.right, mWorkBounds.bottom);

	}
}
