package com.serenegiant.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class CheckableRelativeLayout extends RelativeLayout implements ICheckableLayout {

	private boolean mIsChecked;
	private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

	public CheckableRelativeLayout(final Context context) {
		this(context, null);
	}

	public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}


	@Override
	public boolean isChecked() {
		return mIsChecked;
	}

	@Override
	public void setChecked(final boolean checked) {
		if (mIsChecked != checked) {
			mIsChecked = checked;
            refreshDrawableState();
        }
	}

	@Override
	public void toggle() {
		setChecked(!mIsChecked);
	}

	@Override
    protected int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }

	private float mTouchX, mTouchY;
	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		mTouchX = ev.getX();
		mTouchY = ev.getY();
		return super.onInterceptTouchEvent(ev);
	}

	@Override
	public float touchX() { return mTouchX; }
	@Override
	public float touchY() { return mTouchY; }
}
