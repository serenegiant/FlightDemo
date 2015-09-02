package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class AttitudeView extends GLModelView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeView";

	public AttitudeView(final Context context) {
		this(context, null);
	}

	public AttitudeView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	protected IScreen getScreen() {
		if (DEBUG) Log.v(TAG, "createScreen");
		return new AttitudeScreenBebop(this, AttitudeScreenBase.CTRL_RANDOM);
	}
}
