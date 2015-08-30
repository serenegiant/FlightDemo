package com.serenegiant.widget.gl;

import android.content.Context;
import android.util.AttributeSet;

public class AttitudeView extends GLModelView {

	public AttitudeView(Context context) {
		super(context);
	}

	public AttitudeView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected Screen getScreen() {
		return null;
	}
}
