package com.serenegiant.gamepaddiag;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

import com.serenegiant.gamepad.GamePadConst;

import java.util.ArrayList;
import java.util.List;

public class GamepadView extends View {
//	private static final boolean DEBUG = false;	// FIXME 実同時はfalseにすること
//	private static final String TAG = GamepadView.class.getSimpleName();

	private final Object mSync = new Object();
	private final List<KeyPosition> mKeyPositions = new ArrayList<KeyPosition>();
	private final SparseIntArray mKeyStates = new SparseIntArray();
	private Drawable mGamepadDrawable;
	private Drawable mKeypadDrawable;
	private int mImageWidth, mImageHeight;
	private float mScale;
	private float mCenterViewX, mCenterViewY;
	private float mCenterImageX, mCenterImageY;
	private float mOffsetX, mOffsetY;

	private final int[] mStickPos = new int[6];
	private final float[] mStickVals = new float[4];
	private float mStickScaleLeft, mStickScaleRight;

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
//		if (DEBUG) Log.v(TAG, String.format("ImageSize(%d,%d),bounds=", mImageWidth, mImageHeight) + bounds);
		mGamepadDrawable.setBounds(0, 0, mImageWidth, mImageHeight);
		bounds.set(mGamepadDrawable.getBounds());
//		if (DEBUG) Log.v(TAG, String.format("ImageSize(%d,%d),bounds=", mImageWidth, mImageHeight) + bounds);
	}

	@Override
	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
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
			final float offset_x = (view_width / scale - mImageWidth) / 2.0f;
			final float offset_y = (view_height / scale - mImageHeight) / 2.0f;

//			if (DEBUG) Log.v(TAG, String.format("view(%d,%d),size(%d,%d),scale(%f,%f,%f)",
//				view_width, view_height, mImageWidth, mImageHeight, scale_x, scale_y, scale));

			mScale = scale;
			mCenterViewX = view_width / 2.0f;
			mCenterViewY = view_height / 2.0f;
			mCenterImageX = mImageWidth / 2.0f;
			mCenterImageY = mImageHeight / 2.0f;
			mOffsetX = offset_x;
			mOffsetY = offset_y;

			mStickScaleLeft = mStickPos[2] * mScale / 256.f;
			mStickScaleRight = mStickPos[5] * mScale / 256.f;
		}
	}

	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked, android.R.attr.state_enabled };

	private final Paint mPaint = new Paint();
	private final SparseIntArray mWorkState = new SparseIntArray();
	private boolean[] mDowns = new boolean[GamePadConst.KEY_NUMS];
	private boolean[] mDownsCopy = new boolean[GamePadConst.KEY_NUMS];
	private final int[] mAnalogs = new int[4];

	@Override
	protected void onDraw(final Canvas canvas) {
		if (mKeypadDrawable != null) {
//			if (DEBUG) Log.v(TAG, "onDraw:");
			final int n = GamePadConst.KEY_NUMS;
			synchronized (mSync) {
				System.arraycopy(mDowns, 0, mDownsCopy, 0, GamePadConst.KEY_NUMS);
			}
			// キーパッドの描画処理
			mWorkState.clear();
			final int[] state = getDrawableState();
			int k = state != null ? state.length : 0;
//			if (DEBUG) Log.v(TAG, "onDraw:getDrawableState:k=" + k);
			for (int i = 0; i < k; i++) {
				mWorkState.put(state[i], state[i]);
			}
			mWorkState.delete(android.R.attr.state_checked);
			k = mWorkState.size();
			final int[] base_state = new int[k + 2];
			for (int i = 0; i < k; i++) {
				base_state[i] = mWorkState.indexOfKey(i);
			}
			base_state[k - 2] = android.R.attr.state_enabled;
			final int m = mKeyPositions.size();
			for (int i = 0; i < m; i++) {
				final KeyPosition pos = mKeyPositions.get(i);
				if (pos != null) {
					final int key = pos.key;
					final int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
					try {
						canvas.scale(mScale, mScale);
						canvas.translate(pos.center_x + mOffsetX - pos.width / 2.0f, pos.center_y + mOffsetY - pos.height / 2.0f);
						mKeypadDrawable.setBounds(0, 0, pos.width, pos.height);
						base_state[k-1] = mDownsCopy[key] ? android.R.attr.state_checked : 0;
						mKeypadDrawable.setState(mDownsCopy[key] ? CHECKED_STATE_SET : null);
						mKeypadDrawable.draw(canvas);
					} finally {
						canvas.restoreToCount(saveCount);
					}
				}
			}
		}
		// ゲームパッド画像の表示
		int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
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
		// アナログスティックの表示
		drawStickOne(canvas, mStickPos[0], mStickPos[1], mStickVals[0], mStickVals[1]);
		drawStickOne(canvas, mStickPos[3], mStickPos[4], mStickVals[2], mStickVals[3]);
	}

	private void drawStickOne(final Canvas canvas, final float cx, final float cy, final float vx, final float vy) {
		final int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
		try {
			canvas.scale(mScale, mScale);
			canvas.translate(mOffsetX, mOffsetY);
			mPaint.setColor(0xffff0000);	// 赤
			mPaint.setStrokeWidth(5.0f);
			canvas.drawLine(cx, cy, vx, vy, mPaint);
		} finally {
			canvas.restoreToCount(saveCount);
		}
	}

	public void setKeys(final List<KeyPosition> positions) {
//		if (DEBUG) Log.v(TAG, "setKeys:");
		synchronized (mSync) {
			mKeyPositions.clear();
			final int n = positions != null ? positions.size() : 0;
			for (int i = 0; i < n; i++) {
				mKeyPositions.add(positions.get(i));
			}
		}
	}

	public void setSticks(final int[] xyr) {
		synchronized (mSync) {
			System.arraycopy(xyr, 0, mStickPos, 0, 6);
			mStickVals[0] = mStickPos[0];
			mStickVals[1] = mStickPos[1];
			mStickVals[2] = mStickPos[3];
			mStickVals[3] = mStickPos[4];
		}
	}

	public void setKeyState(final boolean[] downs, final int[] analogs) {
//		if (DEBUG) Log.v(TAG, "setKeyState:");
		boolean modified = false;
		synchronized (mSync) {
			final int n = GamePadConst.KEY_NUMS;
			for (int i = 0; i < n; i++) {
				if (mDowns[i] != downs[i]) {
					mDowns[i] = downs[i];
					modified = true;
				}
			}
			// 左アナログスティック
			if (mAnalogs[0] != analogs[0]) {
				mStickVals[0] = analogs[0] * mStickScaleLeft + mStickPos[0];
				mAnalogs[0] = analogs[0];
				modified = true;
			}
			if (mAnalogs[1] != analogs[1]) {
				mStickVals[1] = analogs[1] * mStickScaleLeft + mStickPos[1];
				mAnalogs[1] = analogs[1];
				modified = true;
			}
			// 右アナログスティック
			if (mAnalogs[2] != analogs[2]) {
				mStickVals[2] = analogs[2] * mStickScaleRight + mStickPos[3];
				mAnalogs[2] = analogs[2];
				modified = true;
			}
			if (mAnalogs[3] != analogs[3]) {
				mStickVals[3] = analogs[3] * mStickScaleRight + mStickPos[4];
				mAnalogs[3] = analogs[3];
				modified = true;
			}
		}
		if (modified) {
			postInvalidate();
		}
	}

}
