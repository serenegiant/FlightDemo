package com.serenegiant.gamepaddiag;


import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.widget.CheckableImageButton;

public class MainFragment extends BaseFragment {
	private static final boolean DEBUG = true;
	private static final String TAG = MainFragment.class.getSimpleName();

	protected static class KeyPosition {
		public int center_x;
		public int center_y;
		public int width;
		public int height;

		public KeyPosition(final int center_x, final int center_y, final int width, final int height) {
			this.center_x = center_x;
			this.center_y = center_y;
			this.width = width;
			this.height = height;
		}
	}

	protected final float IMAGE_WIDTH = 548;
	protected final float IMAGE_HEIGHT = 340;
	protected static SparseArray<KeyPosition> sPositions = new SparseArray<KeyPosition>();
	static {
		sPositions.put(GamePadConst.KEY_LEFT_CENTER, new KeyPosition(193, 207, 64, 64));
		sPositions.put(GamePadConst.KEY_LEFT_UP, new KeyPosition(140, 93, 37, 37));
		sPositions.put(GamePadConst.KEY_LEFT_RIGHT, new KeyPosition(168, 122, 37, 37));
		sPositions.put(GamePadConst.KEY_LEFT_DOWN, new KeyPosition(140, 150, 37, 37));
		sPositions.put(GamePadConst.KEY_LEFT_LEFT, new KeyPosition(112, 122, 37, 37));
		//
		sPositions.put(GamePadConst.KEY_RIGHT_CENTER, new KeyPosition(360, 207, 64, 64));
		sPositions.put(GamePadConst.KEY_RIGHT_UP, new KeyPosition(421, 89, 37, 37));
		sPositions.put(GamePadConst.KEY_RIGHT_RIGHT, new KeyPosition(455, 122, 37, 37));
		sPositions.put(GamePadConst.KEY_RIGHT_DOWN, new KeyPosition(421, 157, 37, 37));
		sPositions.put(GamePadConst.KEY_RIGHT_LEFT, new KeyPosition(386, 122, 37, 37));
		//
		sPositions.put(GamePadConst.KEY_LEFT_1, new KeyPosition(126, 53, 75, 75));			// 左上前
		sPositions.put(GamePadConst.KEY_LEFT_2, new KeyPosition(116, 50, 112, 112));			// 左上後
		//
		sPositions.put(GamePadConst.KEY_CENTER_LEFT, new KeyPosition(222, 119, 32, 32));		// 中央左
		//
		sPositions.put(GamePadConst.KEY_RIGHT_1, new KeyPosition(418, 53, 75, 75));			// 右上前
		sPositions.put(GamePadConst.KEY_RIGHT_2, new KeyPosition(407, 50, 112, 112));		// 右上後
		//
		sPositions.put(GamePadConst.KEY_CENTER_RIGHT, new KeyPosition(335, 119, 32, 32));	// 中央右
		//
		sPositions.put(GamePadConst.KEY_RIGHT_A, new KeyPosition(414, 88, 37, 37));			// アナログモードの時の右キーパッド上
		sPositions.put(GamePadConst.KEY_RIGHT_B, new KeyPosition(449, 123, 37, 37));			// アナログモードの時の右キーパッド右
		sPositions.put(GamePadConst.KEY_RIGHT_C, new KeyPosition(414, 156, 37, 37));			// アナログモードの時の右キーパッド下
		sPositions.put(GamePadConst.KEY_RIGHT_D, new KeyPosition(380, 123, 37, 37));			// アナログモードの時の右キーパッド左
	}

	private SparseArray<CheckableImageButton> mButtons = new SparseArray<CheckableImageButton>();

	public MainFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		initView(rootView);
		return rootView;
	}

	private void initView(final View rootView) {
		if (DEBUG) Log.v(TAG, "initView:");
		CheckableImageButton button = (CheckableImageButton)rootView.findViewById(R.id.left_center_button);
		mButtons.put(GamePadConst.KEY_LEFT_CENTER, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_center_button);
		mButtons.put(GamePadConst.KEY_RIGHT_CENTER, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.center_left_button);
		mButtons.put(GamePadConst.KEY_CENTER_LEFT, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.center_right_button);
		mButtons.put(GamePadConst.KEY_CENTER_RIGHT, button);
		//
		button = (CheckableImageButton)rootView.findViewById(R.id.left_up_button);
		mButtons.put(GamePadConst.KEY_LEFT_UP, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.left_right_button);
		mButtons.put(GamePadConst.KEY_LEFT_RIGHT, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.left_down_button);
		mButtons.put(GamePadConst.KEY_LEFT_DOWN, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.left_left_button);
		mButtons.put(GamePadConst.KEY_LEFT_LEFT, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.left_1_button);
		mButtons.put(GamePadConst.KEY_LEFT_1, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.left_2_button);
		mButtons.put(GamePadConst.KEY_LEFT_2, button);
		//
		button = (CheckableImageButton)rootView.findViewById(R.id.right_up_button);
		mButtons.put(GamePadConst.KEY_RIGHT_UP, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_right_button);
		mButtons.put(GamePadConst.KEY_RIGHT_RIGHT, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_down_button);
		mButtons.put(GamePadConst.KEY_RIGHT_DOWN, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_left_button);
		mButtons.put(GamePadConst.KEY_RIGHT_LEFT, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_1_button);
		mButtons.put(GamePadConst.KEY_RIGHT_1, button);
		button = (CheckableImageButton)rootView.findViewById(R.id.right_2_button);
		mButtons.put(GamePadConst.KEY_RIGHT_2, button);
		//
		final ImageViewWithCallback gv = (ImageViewWithCallback)rootView.findViewById(R.id.gamepad_view);
		gv.setListener(mGamepadViewListener);
	}

	private void adjustView(final ImageViewWithCallback view, final int left, final int top, final int right, final int bottom) {
		if (DEBUG) Log.v(TAG, "adjustView:");
		if (DEBUG) Log.v(TAG, String.format("(%d,%d)-(%d,%d)", left, top, right, bottom));
		final Rect bounds = new Rect();
		view.getDrawingRect(bounds);
		if (DEBUG) Log.v(TAG, "getDrawingRect=" + bounds);
//		view.getHitRect(bounds);
//		if (DEBUG) Log.v(TAG, "getHitRect=" + bounds);
//		final Drawable drawable = view.getDrawable();
//		bounds.set(drawable.getBounds());
//		if (DEBUG) Log.v(TAG, "drawableBounds=" + bounds);
		final int view_width = right - left;
		final int view_height = bottom - top;
		final float scale_x = view_width / IMAGE_WIDTH;
		final float scale_y = view_height / IMAGE_HEIGHT;
//		final float scale = Math.max(scale_x,  scale_y);	// SCALE_MODE_CROP
		final float scale = Math.min(scale_x, scale_y);	// SCALE_MODE_KEEP_ASPECT
		final float view_offset_x = (view_width - IMAGE_WIDTH * scale) / 2.0f;
		final float view_offset_y = (view_height - IMAGE_HEIGHT * scale) / 2.0f;
		final float offset_x = (bounds.width() - IMAGE_WIDTH * scale) / 2.0f;
		final float offset_y = (bounds.height() - IMAGE_HEIGHT * scale) / 2.0f;

		if (DEBUG) Log.v(TAG, String.format("view(%d,%d),size(%f,%f),scale(%f,%f,%f),offset(%f,%f),(%f,%f)",
			view_width, view_height, IMAGE_WIDTH, IMAGE_HEIGHT, scale_x, scale_y, scale, view_offset_x, view_offset_y, offset_x, offset_y));
		final Matrix matrix = new Matrix();
		matrix.reset();
		matrix.postScale(scale / 2, scale / 2);
		matrix.postTranslate(view_offset_x, view_offset_y);
		view.setImageMatrix(matrix);
		view.setScaleType(ImageView.ScaleType.MATRIX);
		final float[] xyr = new float[4];
		final int n = GamePadConst.KEY_NUMS;
		for (int i = 0; i < n; i++) {
			final CheckableImageButton btn = mButtons.get(i);
			if (btn != null) {
				final KeyPosition pos = sPositions.get(i);
				xyr[0] = pos.center_x;
				xyr[1] = pos.center_y;
				xyr[2] = pos.width;
				xyr[3] = pos.height;
//				matrix.mapPoints(xyr);
				matrix.mapVectors(xyr);	// map w/o translation
				btn.setPosition((int)(xyr[0] * 2 - left + view_offset_x), (int)(xyr[1] * 2 - top + view_offset_y), (int)xyr[2], (int)xyr[3]);
			}
		}
	}

	private final ImageViewWithCallback.GamepadViewListener mGamepadViewListener = new ImageViewWithCallback.GamepadViewListener() {
		@Override
		public void onLayout(final ImageViewWithCallback view, final int left, final int top, final int right, final int bottom) {
		if (DEBUG) Log.v(TAG, "GamepadView#onLayout:");
			adjustView(view, left, top, right, bottom);
		}
	};

	protected void updateButtons(final boolean[] downs, final long[] counts, final int[] analog_sticks) {
		final int n = GamePadConst.KEY_NUMS;
		for (int i = 0; i < n; i++) {
			final CheckableImageButton btn = mButtons.get(i);
			if (btn != null) {
				btn.setChecked(downs[i]);
			}
		}
//		mLeftStickView.setDirection(mAnalogSticks[0], mAnalogSticks[1]);
//		mRightStickView.setDirection(mAnalogSticks[2], mAnalogSticks[3]);
	}
 }
