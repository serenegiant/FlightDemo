package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class AttitudeTextureView extends GLTextureModelView {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeTextureView";

	private int mModel = MODEL_BEBOP;
	private int mCtrlType = AttitudeScreenBase.CTRL_RANDOM;

	public AttitudeTextureView(final Context context) {
		this(context, null);
	}

	public AttitudeTextureView(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) Log.v(TAG, "コンストラクタ");
	}

	@Override
	public void setModel(final int model, final int type) {
		mModel = model % MODEL_NUM;
		mCtrlType = type % AttitudeScreenBase.CTRL_NUM;
	}

	@Override
	protected IScreen createScreen() {
		if (DEBUG) Log.v(TAG, "createScreen");
		switch (mModel) {
		case MODEL_MINIDRONE:
		case MODEL_JUMPINGSUMO:
			return new AttitudeScreenMinidrone(this, mCtrlType);
		case MODEL_BEBOP:
		default:
			return new AttitudeScreenBebop(this, mCtrlType);
		}
	}
}
