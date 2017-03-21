package com.serenegiant.widget;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DirectionView extends View {
	private static final boolean DEBUG = false;	// FIXME 実同時はfalseにすること
	private static final String TAG = DirectionView.class.getSimpleName();

	public DirectionView(final Context context) {
		this(context, null, 0);
	}

	public DirectionView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DirectionView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	private final Paint mPaint = new Paint();
	private float mCenterX, mCenterY;
	private float mScale = 1.0f;
	private float directionX;
	private float directionY;
	private final Rect mWork = new Rect();
	private final RectF mFrameRect = new RectF();
	@Override
	protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			getDrawingRect(mWork);
			mFrameRect.set(mWork);
			if (DEBUG) Log.v(TAG, String.format("(%d,%d)(%d,%d),", left, top, right, bottom) + mWork);
			final int dx = (int)((directionX - mCenterX) / mScale);
			final int dy = (int)((directionY - mCenterY) / mScale);
			mCenterX = mFrameRect.centerX();
			mCenterY = mFrameRect.centerY();
			final float size = Math.min(mFrameRect.width(), mFrameRect.height());
			mScale = size / 256.0f;
			setDirection(dx, dy);
		}
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		mPaint.setColor(0x3f7f7f7f);	// 半透明の灰色
		mPaint.setStrokeWidth(1.0f);
		canvas.drawRect(mFrameRect, mPaint);
		mPaint.setColor(0xffff0000);	// 赤
		mPaint.setStrokeWidth(5.0f);
		canvas.drawLine(mCenterX, mCenterY, directionX, directionY, mPaint);
	}

	public void setDirection(final int x, final int y) {
		directionX = x * mScale + mCenterX;
		directionY = y * mScale + mCenterY;

		postInvalidate();
	}
}
