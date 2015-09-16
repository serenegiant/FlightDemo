package com.serenegiant.gl;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

public class AttitudeView extends GLModelView {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = "AttitudeView";

	private int mModel = MODEL_BEBOP;
	private int mCtrlType = AttitudeScreenBase.CTRL_RANDOM;

	public AttitudeView(final Context context) {
		this(context, null);
	}

	public AttitudeView(final Context context, final AttributeSet attrs) {
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

	/**
	 * 機体姿勢をセット
	 * @param roll 左右の傾き[-100,100] => 今は[-30,+30][度]に対応
	 * @param pitch 前後の傾き(機種の上げ下げ)[-100,100] => 今は[-30,+30][度]に対応
	 * @param yaw 水平回転[-180,+180][度], 0は進行方向と一致
	 * @param gaz 高さ移動量 [-100,100] 単位未定
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw, final float gaz) {
		if (mScreen instanceof  AttitudeScreenBase) {
			((AttitudeScreenBase)mScreen).setAttitude(roll, pitch, yaw, gaz);
		}
	}

}
