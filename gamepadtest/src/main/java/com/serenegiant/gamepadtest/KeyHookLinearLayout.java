package com.serenegiant.gamepadtest;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by saki on 2015/08/25.
 */
public class KeyHookLinearLayout extends LinearLayout {
	private static final String TAG = "KeyHookLinearLayout";

	public KeyHookLinearLayout(Context context) {
		this(context, null, 0);
	}

	public KeyHookLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public KeyHookLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
		this.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				Log.w(TAG, "onKey:event=" + event);
				return false;
			}
		});
		this.setFocusable(true);
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.w(TAG, "dispatchKeyEvent:event=" + event);
		return super.dispatchKeyEvent(event);
	}

}
