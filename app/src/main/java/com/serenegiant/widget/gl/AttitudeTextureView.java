package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class AttitudeTextureView extends GLTextureModelView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeTextureView";

	public AttitudeTextureView(final Context context) {
		this(context, null);
	}

	public AttitudeTextureView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	protected IScreen createScreen() {
		if (DEBUG) Log.v(TAG, "createScreen");
		return new AttitudeScreenBebop(this, AttitudeScreenBase.CTRL_RANDOM);
	}
}
