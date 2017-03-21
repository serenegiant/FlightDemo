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
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.serenegiant.arflight.R;

/**
 * カメラのPan/Tiltに対応する位置に十字線を描くためのView
 */
public class OrientationView extends View {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = OrientationView.class.getSimpleName();

	private int mPan, mTilt;
	private float mScaleX, mScaleY;
	private final Paint mPaint = new Paint();
	private final PathEffect mPathEffect;
	private final Rect mBounds = new Rect();

	public OrientationView(final Context context) {
		this(context, null, 0);
	}

	public OrientationView(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OrientationView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		// XXX ハードウエアアクセれレーションが有効だと点線を描画できないので無効にする
		setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		// Dip => px変換係数を取得
		final float scale = getContext().getResources().getDisplayMetrics().density;
		mPathEffect = new DashPathEffect(new float[] { 15 * scale, 10 * scale }, 0);	// 15dp-10dp
//		mPathEffect = createSafeDashedPathEffect(20 * scale, 5 * scale, 0, 3 * scale);
		mPaint.setColor(getResources().getColor(R.color.QUATER_CLEAR_WHITE));
		mPaint.setStrokeWidth(3 * scale);	// 3dp
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setPathEffect(mPathEffect);
	}

	public synchronized void setPanTilt(final int pan, final int tilt) {
		if ((mPan != pan) || (mTilt != tilt)) {
			mPan = pan;
			mTilt = tilt;
			postInvalidate();
		}
	}

	@Override
	protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		mScaleX = w / 200f;
		mScaleY = h / 200f;
	}

	@Override
	protected synchronized void onDraw(final Canvas canvas) {
//		super.onDraw(canvas);	// このメソッドはにもしないのでコメントアウト
		getDrawingRect(mBounds);
		final float px = mPan * mScaleX + mBounds.centerX();
		final float py = mTilt * mScaleY + mBounds.centerY();
		mPaint.setPathEffect(mPathEffect);
		canvas.drawLine(0, py, getWidth(), py, mPaint);
		canvas.drawLine(px, 0, px, getHeight(), mPaint);
	}

	private PathDashPathEffect createSafeDashedPathEffect(int dashLength, int spaceLength, int offset, int strokeWidth) {
		final Path p = new Path();
		p.moveTo(0, strokeWidth / 2);
		p.lineTo(dashLength, strokeWidth / 2);
		p.lineTo(dashLength, -strokeWidth / 2);
		p.lineTo(0, -strokeWidth / 2);
		return new PathDashPathEffect(p, dashLength + spaceLength, offset, PathDashPathEffect.Style.ROTATE);
	}
}
