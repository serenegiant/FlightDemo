package com.serenegiant.widget;
/*
 * AndroGamepad
 * library and sample to access to various gamepad with common interface
 *
 * Copyright (c) 2015-2016 saki t_saki@serenegiant.com
 *
 * File name: ImageViewWithCallback.java
 *
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
*/

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

public class ImageViewWithCallback extends ImageView {
	private static final String TAG = ImageViewWithCallback.class.getSimpleName();

	public interface GamepadViewListener {
		public void onLayout(final ImageViewWithCallback view, final int left, final int top, final int right, final int bottom);
	}

	private GamepadViewListener mListener;
	public ImageViewWithCallback(Context context) {
		this(context, null, 0);
	}

	public ImageViewWithCallback(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageViewWithCallback(Context context, AttributeSet attrs, int defStyleAttr) {
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
