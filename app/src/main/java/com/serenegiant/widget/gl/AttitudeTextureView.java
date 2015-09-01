package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class AttitudeTextureView extends GLTextureModelView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeTextureView";

	public AttitudeTextureView(Context context) {
		this(context, null);
	}

	public AttitudeTextureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	protected Screen getScreen() {
		if (DEBUG) Log.v(TAG, "getScreen");
		return new AttitudeScreenBebopRandom(this);
	}
}
