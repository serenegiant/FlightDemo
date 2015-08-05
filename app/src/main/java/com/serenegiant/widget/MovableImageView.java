package com.serenegiant.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 移動・サイズ変更可能なImageView
 * 枠表示の有無・移動可能かどうか・サイズ変更可能かどうかを動的に切り替え可能
 * xmlレイアウトに配置する時はFrameLayoutに入れて、android:layout_width="wrap_content"、
 * android:layout_height="wrap_content"に設定すること。</br>
 * でないとonMeasureで最大サイズを取得できないので移動もリサイズもできなくなってしまう
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class MovableImageView extends ImageView implements IMovable {

//	private static final boolean DEBUG = false;	// FIXME 実働時にはfalseにすること
//	private static final String TAG = MovableImageView.class.getSimpleName();

	private static final int RESIZE_SIZE_DP = 20;	// [dp]
	private static final int MIN_WIDTH_DP = 80;		// [dp]
	private static final int MIN_HEIGHT_DP = 80;	// [dp]

	private static final float DEFAULT_FRAMEWIDTH = 5;
	private static final float DEFAULT_WIDTH_DP = 160;	// [dp]
	private static final float DEFAULT_HEIGHT_DP = 160;	// [dp]

	private final Rect mWorkBounds = new Rect();
	private final Paint mFramePaint = new Paint();	// 選択中の外形線表示用・・・なんて言うんだっったっけ?

    private static final int TAP_TIMEOUT = ViewConfiguration.getTapTimeout();
    private static final int LONG_PRESS_TIMEOUT = ViewConfiguration.getLongPressTimeout();

	private static float[] intervals = {10f, 10f};

	private final int mResizeSize;	// リサイズ可能領域の大きさ(幅)
//	private final int mInsetSize;
	private int mFramwWidth = 1;
	private final int mMinWidth, mMinHeight;
	private int mMaxWidth = -1, mMaxHeight = -1;
	private int mParentPaddingLeft, mParentPaddingTop, mParentPaddingRight, mParentPaddingBottom;
	private int mLastWidth, mLastHeight;
	private OnBoundsChangedListener mOnBoundsChangedListener;

	private boolean mShowFrame = true;
	private boolean mMovable = true;
	private boolean mResizable = true;
	private boolean mKeepAspect = false;
	private float mAspectRatio;

	/**
	 * Viewの状態を保存・読み込むためのクラス
	 */
	public static final class SavedState extends BaseSavedState {
		private boolean mShowFrame, mMovable, mResizable, mKeepAspect;
		private float mAspectRatio;
        public SavedState(final Parcel in) {
            super(in);
            readFromParcel(in);
        }
        public SavedState(final Parcelable superState) {
            super(superState);
        }

        private void readFromParcel(final Parcel in) {
            // 保存した時と同じ順序で読み込まないとダメ
        	mShowFrame = in.readInt() != 0;
        	mMovable = in.readInt() != 0;
        	mResizable = in.readInt() != 0;
        	mKeepAspect = in.readInt() != 0;
        	mAspectRatio = in.readFloat();
        }

        @Override
        public void writeToParcel(final Parcel out, final int flags) {
            super.writeToParcel(out, flags);
            // 読み込み時と同じ順序で書き込まないとダメ
            out.writeInt(mShowFrame ? 1 : 0);
            out.writeInt(mMovable ? 1 : 0);
            out.writeInt(mResizable ? 1 : 0);
            out.writeInt(mKeepAspect ? 1 : 0);
            out.writeFloat(mAspectRatio);
        }

        public static final Creator<SavedState> CREATOR
        	= new Creator<SavedState>() {

            @Override
			public SavedState createFromParcel(final Parcel source) {
                return new SavedState(source);
            }

            @Override
			public SavedState[] newArray(final int size) {
                return new SavedState[size];
            }
        };
    }

	public MovableImageView(final Context context) {
		this(context, null, 0);
	}

	public MovableImageView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MovableImageView(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
//		if (DEBUG) Log.v(TAG, "MovableImageView:" + hashCode());
		mFramePaint.setColor(Color.RED);
		mFramePaint.setStyle(Style.STROKE);// 枠だけを描画、塗りつぶさない
		mFramePaint.setPathEffect(new DashPathEffect(intervals, 10f));	// 点線で描画
		mFramePaint.setStrokeWidth(DEFAULT_FRAMEWIDTH);
		final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mResizeSize = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, RESIZE_SIZE_DP, metrics);
//		mInsetSize = mResizeSize / 2;
		// 最小サイズをピクセル単位で取得
		mMinWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_WIDTH_DP, metrics);
		mMinHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MIN_HEIGHT_DP, metrics);
		// 標準サイズをピクセル単位で取得
		mLastWidth = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_WIDTH_DP, metrics);
		mLastHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_HEIGHT_DP, metrics);
//		if (DEBUG) Log.v(TAG, String.format("padding(%d,%d),inset=%d", getPaddingLeft(), getPaddingTop(), mInsetSize));
	}

	@Override
	protected Parcelable onSaveInstanceState() {
//		if (DEBUG) Log.w(TAG, "onSaveInstanceState:" + hashCode());
		final SavedState saveState = new SavedState(super.onSaveInstanceState());
		saveState.mShowFrame = mShowFrame;
		saveState.mMovable = mMovable;
		saveState.mResizable = mResizable;
		saveState.mKeepAspect = mKeepAspect;
		saveState.mAspectRatio = mAspectRatio;
		return saveState;
	}

	@Override
	protected void onRestoreInstanceState(final Parcelable state) {
//		if (DEBUG) Log.w(TAG, "onRestoreInstanceState:" + hashCode());
		if (state instanceof SavedState) {
			final SavedState saved = (SavedState)state;
			super.onRestoreInstanceState(saved.getSuperState());
			mShowFrame = saved.mShowFrame;
			mMovable = saved.mMovable;
			mResizable = saved.mResizable;
			mKeepAspect = saved.mKeepAspect;
			mAspectRatio = saved.mAspectRatio;
		} else {
			super.onRestoreInstanceState(state);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		if (mShowFrame && isSelected()) {
			getDrawingRect(mWorkBounds);
			mWorkBounds.inset(mFramwWidth, mFramwWidth/*mInsetSize , mInsetSize*/);	// 一回り小さく描画する
			canvas.drawRect(mWorkBounds, mFramePaint);
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		updateParentDimension();
		int width = MeasureSpec.getSize(widthMeasureSpec);
		final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		switch (widthMode) {
		case MeasureSpec.UNSPECIFIED:
			// 好きな値を設定できる時
			width = mLastWidth;
			break;
		case MeasureSpec.EXACTLY:
			// この値にしないとダメな時・・・本来は変更しないんだけどignoreExactly=tueなら上書きする
			if (ignoreExactly)
				width = mLastWidth;
			break;
		case MeasureSpec.AT_MOST:
			// 最大の大きさが指定された時
			if (width > mLastWidth) width = mLastWidth;
			break;
		}
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);

		int height = MeasureSpec.getSize(heightMeasureSpec);
		final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		switch (heightMode) {
		case MeasureSpec.UNSPECIFIED:
			// 好きな値を設定できる時
			height = mLastHeight;
			break;
		case MeasureSpec.EXACTLY:
			// この値にしないとダメな時・・・本来は変更しないんだけどignoreExactly=tueなら上書きする
			if (ignoreExactly)
				height = mLastHeight;
			break;
		case MeasureSpec.AT_MOST:
			// 最大の大きさが指定された時
			if (height > mLastHeight) height = mLastHeight;
			break;
		}
		heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
		setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		ignoreExactly = false;
		if (mKeepAspect && (mAspectRatio < 0)) {
			mAspectRatio = Math.abs((right -left) / (float)(bottom - top));
		}
	}

	private void updateParentDimension() {
		final ViewParent parent = getParent();
		if ((parent != null) && (parent instanceof ViewGroup)) {
			final ViewGroup vg = (ViewGroup)parent;
			vg.getDrawingRect(mWorkBounds);
			mParentPaddingLeft = vg.getPaddingLeft();
			mParentPaddingTop = vg.getPaddingTop();
			mParentPaddingRight = vg.getPaddingRight();
			mParentPaddingBottom = vg.getPaddingBottom();
			mMaxWidth = mWorkBounds.width() - mParentPaddingRight;
			mMaxHeight = mWorkBounds.height() - mParentPaddingBottom;
		} else
			throw new RuntimeException("view parent not found");
	}

	private long startTime;
	private int mPrevX, mPrevY;		// 前回のタッチ位置
	private int inResize;			// リサイズ中? bit0:上, bit1:右, bit2:下, bit3:左
	private boolean changed;
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(final MotionEvent event) {
		if (!mMovable && !mResizable)
			return super.onTouchEvent(event);

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
		{
			if (mMaxWidth <= 0 || mMaxHeight <= 0)
				updateParentDimension();
			startTime = event.getEventTime();
			// ビュー座標系でタッチ位置を取得
			final int x = (int)event.getX();
			final int y = (int)event.getY();
			getDrawingRect(mWorkBounds);	// ビュー座標で矩形を取得
			int sz = mResizeSize * 5 / 2;
			if ((mWorkBounds.width() < sz) || (mWorkBounds.height() < sz)) {
				sz = mWorkBounds.width() / 4;
				sz = Math.min(sz, mWorkBounds.height() / 4);
				mWorkBounds.inset(sz, sz);
			} else {
				mWorkBounds.inset(mResizeSize, mResizeSize);
			}
			inResize = 0;
			changed = false;
			if (mResizable && !mWorkBounds.contains(x, y)) {
				if (y < mWorkBounds.top) inResize |= 1;			// 上
				else if (y > mWorkBounds.bottom) inResize |= 4;	// 下
				if (x > mWorkBounds.right) inResize |= 2;		// 右
				else if (x < mWorkBounds.left) inResize |= 8;	// 左
			}
			// 画面座標系でタッチ位置を取得
			mPrevX = (int)event.getRawX();
			mPrevY = (int)event.getRawY();
			break;
		}
		case MotionEvent.ACTION_MOVE:
		{
			final long t = event.getEventTime() - startTime;
			if (t < TAP_TIMEOUT) return true;
			if (!isSelected()) {
				setSelected(true);
				invalidate();
			}

			// FIXME 移動/サイズ調整とみなす移動量のチェックが必要

			// 画面座標系でタッチ位置を取得
			final int x = (int)event.getRawX();
			final int y = (int)event.getRawY();
			getHitRect(mWorkBounds);	// 親の座標系で矩形を取得

			boolean needLayout = false;
			if (inResize != 0) {	// リサイズするとき
				final int dx = mWorkBounds.width() - mMinWidth;		// 変更できる最大幅
				final int dy = mWorkBounds.height() - mMinHeight;	// 変更できる最大高さ
				int d;
				if ((inResize & 1) != 0) {	// 上
					d = y - mPrevY;
					if ((d > dy) || (mWorkBounds.top + d < mParentPaddingTop)) {
						d = 0;
					}
					mWorkBounds.top += d;
					if (mKeepAspect && (mAspectRatio > 0)) {
						d = (int)(d * mAspectRatio);
						if ((mWorkBounds.left < d) && (mWorkBounds.left >= 0)) {
							d -= mWorkBounds.left;
							mWorkBounds.left = 0;
							mWorkBounds.right += d;
						} else {
							mWorkBounds.left -= d;
						}
					}
					needLayout = true;
				}
				if ((inResize & 2) != 0) {	// 右
					d = x - mPrevX;
					if ((d < -dx) || (mWorkBounds.right + d > mMaxWidth)) {
						d = 0;
					}
					mWorkBounds.right += d;
					if (mKeepAspect && (mAspectRatio > 0)) {
						d = (int)(d / mAspectRatio);
						if ((mWorkBounds.top < d) && (mWorkBounds.top >= 0)) {
							d -= mWorkBounds.top;
							mWorkBounds.top = 0;
							mWorkBounds.bottom += d;
						} else {
							mWorkBounds.top -= d;
						}
					}
					needLayout = true;
				}
				if ((inResize & 4) != 0) {	// 下
					d = y - mPrevY;
					if ((d < -dy) || (mWorkBounds.bottom + d > mMaxHeight)) {
						d = 0;
					}
					mWorkBounds.bottom += d;
					if (mKeepAspect && (mAspectRatio > 0)) {
						d = (int)(d * mAspectRatio);
						if (mWorkBounds.right > mMaxWidth - d) {
							d -= (mWorkBounds.right - mMaxWidth + d);
							mWorkBounds.right = mMaxWidth;
							mWorkBounds.left -= d;
						} else {
							mWorkBounds.right += d;
						}
					}
					needLayout = true;
				}
				if ((inResize & 8) != 0) {	// 左
					d = x - mPrevX;
					if ((d > dx) || (mWorkBounds.left + d < mParentPaddingLeft)) {
						d = 0;
					}
					mWorkBounds.left += d;
					if (mKeepAspect && (mAspectRatio > 0)) {
						d = (int)(d / mAspectRatio);
						if (mWorkBounds.bottom > mMaxHeight - d) {
							d -= (mWorkBounds.bottom - mMaxHeight + d);
							mWorkBounds.bottom = mMaxHeight;
							mWorkBounds.top -= d;
						} else {
							mWorkBounds.bottom += d;
						}
					}
					needLayout = true;
				}
			} else if (mMovable) {
				// 移動する時
				mWorkBounds.left += (x - mPrevX);
				mWorkBounds.top += (y - mPrevY);
				mWorkBounds.bottom = mWorkBounds.top + getHeight();
				mWorkBounds.right = mWorkBounds.left + getWidth();
				needLayout = true;
			}
			// 枠を移動・リサイズする
			if (needLayout && (mWorkBounds.top >= mParentPaddingTop) && (mWorkBounds.left >= mParentPaddingLeft)
				&& (mWorkBounds.right < mMaxWidth) && (mWorkBounds.bottom < mMaxHeight)) {

				// View自体をリサイズ, これでセットするのは親の座標系での値
				layout(mWorkBounds.left, mWorkBounds.top, mWorkBounds.right, mWorkBounds.bottom);
				changed = true;
			}
			mPrevX = x;
			mPrevY = y;
			break;
		}
		case MotionEvent.ACTION_UP:
			if (changed) {
//				getDrawingRect(mWorkBounds);
				getHitRect(mWorkBounds);	// 親の座標系で矩形を取得
				onBoundsChange(mWorkBounds);
			} else {
				final long t = event.getEventTime() - startTime;
				if (t < TAP_TIMEOUT) {
					toggleSelected();
					performClick();
					invalidate();
				} else if (t < LONG_PRESS_TIMEOUT) {
					performLongClick();
				}
			}
			break;
		}
		return true;	// trueを返さないと次のイベントが来ない(最初のACTION_DOWNだけしか来ない)
	}

	/**
	 * onMeasureでEXACPLYが来た時にもwidth/heightを上書きするためのフラグ
	 */
	private boolean ignoreExactly = false;

	protected void onBoundsChange(final Rect bounds) {
		mLastWidth = getWidth();
		mLastHeight = getHeight();

//		if (DEBUG) Log.i(TAG, String.format("x,y=%d,%d", (int)getX(), (int)getY()) + ",bounds=" + bounds);
		final ViewGroup.LayoutParams params = getLayoutParams();
		if (params instanceof FrameLayout.LayoutParams) {
			final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)params;
			lp.leftMargin = bounds.left - mParentPaddingLeft;
			lp.topMargin = bounds.top - mParentPaddingTop;
			ignoreExactly = true;	// onMeasureでEXACPLYが来た時にもwidth/heightを上書きする
			requestLayout();
		}
		if (mOnBoundsChangedListener != null) {
			try {
				mOnBoundsChangedListener.onBoundsChanged(this, bounds);
			} catch (final Exception e) {
//				if (DEBUG) Log.w(TAG, e);
			}
		}
	}

	/**
	 * 選択枠の矩形をコピーして返す
	 * @return
	 */
	@Override
	public Rect getFrameRect() {
		getDrawingRect(mWorkBounds);
		return new Rect(mWorkBounds);
	}

	/**
	 * サイズ・位置が変更された時のコールバックリスナーを設定
	 * @param listener
	 */
	@Override
	public void setOnBoundsChangedListener(final OnBoundsChangedListener listener) {
		mOnBoundsChangedListener = listener;
	}

	/**
	 * サイズ・位置が変更された時のコールバックリスナーを取得
	 * @return
	 */
	@Override
	public OnBoundsChangedListener getOnBoundsChangedListener() {
		return mOnBoundsChangedListener;
	}

	/**
	 * 枠を表示するかどうかを取得
	 * @return
	 */
	@Override
	public boolean isShowFrame() {
		return mShowFrame;
	}

	/**
	 * 枠を表示するかどうかを設定
	 * @param showFrame
	 */
	@Override
	public void setShowFrame(final boolean showFrame) {
		mShowFrame = showFrame;
	}

	/**
	 * 移動可能かどうかを取得
	 * @return
	 */
	@Override
	public boolean isMovable() {
		return mMovable;
	}

	/**
	 * 移動可能かどうかを設定
	 * @param mMovable
	 */
	@Override
	public void setMovable(final boolean movable) {
		mMovable = movable;
	}

	/**
	 * サイズ変更可能かどうかを取得
	 * @return
	 */
	@Override
	public boolean isResizable() {
		return mResizable;
	}

	/**
	 * サイズ変更かどうかをセット
	 * @param mResizable
	 */
	@Override
	public void setResizable(final boolean resizable) {
		mResizable = resizable;
	}

	/**
	 * アスペクト比固定でサイズ変更するかどうかを取得
	 */
	@Override
	public boolean keepAspect() {
		return mKeepAspect;
	}

	/**
	 * アスペクト比固定でサイズ変更するかどうかを設定
	 * setSetAspectRatioで設定した値は上書きされる
	 */
	@Override
	public void setKeepAspect(final boolean keep) {
		if (mKeepAspect != keep) {
			mKeepAspect = keep;
			mAspectRatio = -1.0f;
			requestLayout();
		}
	}

	/**
	 * アスペクト比固定でサイズ変更する場合のアスペクト比をセット
	 * @param aspect 0未満ならアスペクト比固定解除
	 */
	@Override
	public void setSetAspectRatio(final float aspect) {
		if (mAspectRatio != aspect) {
			mAspectRatio = aspect;
			mKeepAspect = (mAspectRatio > 0);
			requestLayout();
		}
	}

	/**
	 * 枠の色を取得
	 */
	@Override
	public int getFrameColor() {
		return mFramePaint.getColor();
	}
	/**
	 * 枠の色を設定
	 * @param cl
	 */
	@Override
	public void setFrameColor(final int cl) {
		mFramePaint.setColor(cl);
	}

	/**
	 * 枠のアルファ値を取得
	 * @return
	 */
	@Override
	public int getFrameAlpha() {
		return mFramePaint.getAlpha();
	}

	/**
	 * 枠のアルファ値を設定
	 * @param alpha
	 */
	@Override
	public void setFrameAlpha(final int alpha) {
		mFramePaint.setAlpha(alpha);
	}

	/**
	 * 枠の描画幅を取得
	 * @return
	 */
	@Override
	public float getFrameWidth() {
		return mFramePaint.getStrokeWidth();
	}

	/**
	 * 枠の描画幅を設定
	 * @param width
	 */
	@Override
	public void setFrameWidth(final float width) {
		mFramePaint.setStrokeWidth(width);
		mFramwWidth = width > 0 ? (int)width : 1;
	}

	/**
	 * 枠の表示効果を取得
	 * @return
	 */
	@Override
	public PathEffect getFrameEffect() {
		return mFramePaint.getPathEffect();
	}

	/**
	 * 枠の表示効果を設定
	 * @param effect
	 */
	@Override
	public void setFrameEffect(final PathEffect effect) {
		mFramePaint.setPathEffect(effect);
	}

	@Override
	public void toggleSelected() {
		setSelected(!isSelected());
	}

	@Override
	public int minWidth() {
		return mMinWidth;
	}
	@Override
	public int minHeight() {
		return mMinHeight;
	}

}
