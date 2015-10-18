package com.serenegiant.gamepaddiag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class GamepadView extends View {
	private static final boolean DEBUG = true;	// FIXME 実同時はfalseにすること
	private static final String TAG = GamepadView.class.getSimpleName();

	private Drawable mGamepadDrawable;
	private Drawable mKeypadDrawable;
	private int mImageWidth, mImageHeight;
	private float mScale;
	private float mCenterViewX, mCenterViewY;
	private float mCenterImageX, mCenterImageY;

	public GamepadView(final Context context) {
		this(context, null, 0);
	}

	public GamepadView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GamepadView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		TypedArray attribs = context.obtainStyledAttributes(attrs, R.styleable.GamepadView, defStyleAttr, 0);
		mGamepadDrawable = attribs.getDrawable(R.styleable.GamepadView_gamepadview_gamepad_drawable);
		mKeypadDrawable = attribs.getDrawable(R.styleable.GamepadView_gamepadview_keypad_drawable);
		attribs.recycle();
		attribs = null;

		if (mGamepadDrawable instanceof BitmapDrawable) {
			final Bitmap bitmap = ((BitmapDrawable)mGamepadDrawable).getBitmap();
			mImageWidth = bitmap.getWidth();
			mImageHeight = bitmap.getHeight();
		} else {
			mImageWidth = mGamepadDrawable.getIntrinsicWidth();
			mImageHeight = mGamepadDrawable.getIntrinsicHeight();
		}
		final Rect bounds = new Rect();
		bounds.set(mGamepadDrawable.getBounds());
		if (DEBUG) Log.v(TAG, String.format("ImageSize(%d,%d),bounds=", mImageWidth, mImageHeight) + bounds);
		mGamepadDrawable.setBounds(0, 0, mImageWidth, mImageHeight);
		bounds.set(mGamepadDrawable.getBounds());
		if (DEBUG) Log.v(TAG, String.format("ImageSize(%d,%d),bounds=", mImageWidth, mImageHeight) + bounds);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed && (mImageWidth * mImageHeight != 0)) {
			// このビューのサイズ
			final int view_width = right - left;
			final int view_height = bottom - top;
			// ゲームパッド画像をビューにフィットさせるときの縦横の拡大率を計算
			final float scale_x = view_width / (float)mImageWidth;
			final float scale_y = view_height / (float)mImageHeight;
			// アスペクトを保ったままはみ出さずに表示できる最大の拡大率=縦横の拡大率の小さい方を選択
			// クロップセンターにするなら大きい方を選択する
//			final float scale = Math.max(scale_x, scale_y);	// SCALE_MODE_CROP
			final float scale = Math.min(scale_x, scale_y);	// SCALE_MODE_KEEP_ASPECT

			if (DEBUG) Log.v(TAG, String.format("view(%d,%d),size(%d,%d),scale(%f,%f,%f)",
				view_width, view_height, mImageWidth, mImageHeight, scale_x, scale_y, scale));

			mScale = scale;
			mCenterViewX = view_width / 2.0f;
			mCenterViewY = view_height / 2.0f;
			mCenterImageX = mImageWidth / 2.0f;
			mCenterImageY = mImageHeight / 2.0f;
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		// FIXME キーパッドの描画処理
		final int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
		try {
			// Canvas#setMatrixはうまく働かない
			// 元のイメージの中心点を原点に移動→拡大縮小→Viewの中心座標まで原点を移動
			canvas.translate(mCenterViewX, mCenterViewY);
			canvas.scale(mScale, mScale);
			canvas.translate(-mCenterImageX, -mCenterImageY);
			if (mGamepadDrawable != null) {
				mGamepadDrawable.draw(canvas);
			}
		} finally {
			canvas.restoreToCount(saveCount);
		}
	}
}
