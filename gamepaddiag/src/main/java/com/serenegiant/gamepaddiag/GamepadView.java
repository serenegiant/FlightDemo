package com.serenegiant.gamepaddiag;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class GamepadView extends ImageView {
	private static final String TAG = GamepadView.class.getSimpleName();

	public interface GamepadViewListener {
		public void onLayout(final GamepadView view, final int left, final int top, final int right, final int bottom);
	}

	private GamepadViewListener mListener;
	public GamepadView(Context context) {
		this(context, null, 0);
	}

	public GamepadView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GamepadView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public void setListener(final GamepadViewListener listener) {
		mListener = listener;
	}

	public GamepadViewListener getListener() {
		return mListener;
	}

	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed && (mListener != null)) {
			try {
				mListener.onLayout(this, left, top, right, bottom);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	}
}
