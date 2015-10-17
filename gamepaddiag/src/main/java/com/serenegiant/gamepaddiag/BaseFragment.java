package com.serenegiant.gamepaddiag;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;
import com.serenegiant.widget.CheckableImageButton;

public abstract class BaseFragment extends Fragment {
	private static final boolean DEBUG = true;
	private static final String TAG = BaseFragment.class.getSimpleName();

	protected static class KeyPositions {
		public int width;
		public int height;
		public int[] x = new int[GamePadConst.KEY_NUMS];
		public int[] y = new int[GamePadConst.KEY_NUMS];
		public int[] w = new int[GamePadConst.KEY_NUMS];
		public int[] h = new int[GamePadConst.KEY_NUMS];

		public KeyPositions(final int _width, final int _height, final int[] xy) {
			if ((xy == null) || (xy.length < GamePadConst.KEY_NUMS * 4) || (_width * _height == 0)) {
				throw new IllegalArgumentException("座標データがおかしい");
			}
			width = _width;
			height = _height;
			for (int i = 0; i < GamePadConst.KEY_NUMS; i++) {
				x[i] = xy[i * 4];
				y[i] = xy[i * 4 + 1];
				w[i] = xy[i * 4 + 2];
				h[i] = xy[i * 4 + 3];
			}
		}
	}
	protected static KeyPositions sPosition;


	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final Object mSync = new Object();
	private SparseArray<CheckableImageButton> mButtons = new SparseArray<CheckableImageButton>();
	private Joystick mJoystick;

	public BaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		mUIHandler.post(mKeyUpdateTask);
	}

	@Override
	public void onPause() {
		mUIHandler.removeCallbacks(mKeyUpdateTask);
		mJoystick = null;
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		super.onDetach();
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
		final GamepadView gv = (GamepadView)rootView.findViewById(R.id.gamepad_view);
		gv.setListener(mGamepadViewListener);

		mJoystick = Joystick.getInstance(getActivity());
	}

	private void adjustView(final GamepadView view, final int left, final int top, final int right, final int bottom) {
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
		final float scale_x = view_width / (float)sPosition.width;
		final float scale_y = view_height / (float)sPosition.height;
//		final float scale = Math.max(scale_x,  scale_y);	// SCALE_MODE_CROP
		final float scale = Math.min(scale_x, scale_y);	// SCALE_MODE_KEEP_ASPECT
		final float view_offset_x = (view_width - sPosition.width * scale) / 2.0f;
		final float view_offset_y = (view_height - sPosition.height * scale) / 2.0f;
		final float offset_x = (bounds.width() - sPosition.width * scale) / 2.0f;
		final float offset_y = (bounds.height() - sPosition.height * scale) / 2.0f;

		if (DEBUG) Log.v(TAG, String.format("view(%d,%d),size(%d,%d),scale(%f,%f,%f),offset(%f,%f),(%f,%f)",
			view_width, view_height, sPosition.width, sPosition.height, scale_x, scale_y, scale, view_offset_x, view_offset_y, offset_x, offset_y));
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
				xyr[0] = sPosition.x[i];
				xyr[1] = sPosition.y[i];
				xyr[2] = xyr[3] = sPosition.w[i];
//				matrix.mapPoints(xyr);
				matrix.mapVectors(xyr);	// map w/o translation
				btn.setPosition((int)(xyr[0] * 2 - left + view_offset_x), (int)(xyr[1] * 2 - top + view_offset_y), (int)xyr[2], (int)xyr[3]);
			}
		}
	}

	private final GamepadView.GamepadViewListener mGamepadViewListener = new GamepadView.GamepadViewListener() {
		@Override
		public void onLayout(final GamepadView view, final int left, final int top, final int right, final int bottom) {
		if (DEBUG) Log.v(TAG, "GamepadView#onLayout:");
			adjustView(view, left, top, right, bottom);
		}
	};


	private final boolean[] mDowns = new boolean[GamePadConst.KEY_NUMS];
	private final long[] mCounts = new long[GamePadConst.KEY_NUMS];
	private final int[] mAnalogSticks = new int[4];

	private final Runnable mKeyUpdateTask = new Runnable() {
		@Override
		public void run() {
			mUIHandler.removeCallbacks(this);
			synchronized (mSync) {
				final long current = System.currentTimeMillis();
				if (mJoystick != null) {
					mJoystick.updateState(mDowns, mCounts, mAnalogSticks, false);
				}
				final int n = GamePadConst.KEY_NUMS;
				for (int i = 0; i < n; i++) {
					final CheckableImageButton btn = mButtons.get(i);
					if (btn != null) {
						btn.setChecked(mDowns[i]);
					}
				}
			}
//			mLeftStickView.setDirection(mAnalogSticks[0], mAnalogSticks[1]);
//			mRightStickView.setDirection(mAnalogSticks[2], mAnalogSticks[3]);
			mUIHandler.postDelayed(this, 50);
		}
	};

}
