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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * コールバックリスナーでタッチイベント処理を横取りするかどうかを決めることができるLinearLayout
 */
public class TouchableLinearLayout extends LinearLayout {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = TouchableLinearLayout.class.getSimpleName();

	public interface OnTouchableListener {
		/**
		 * ViewGroupのonInterceptTouchEventが呼び出された時のコールバック
		 * @param event
		 * @return trueを返すと以降のTouchEventが横取りされてonTouchEventが呼ばれる
		 */
		public boolean onInterceptTouchEvent(final MotionEvent event);

		/**
		 * ViewGroupのonTouchEventが呼び出された時のコールバック
		 * @param event
		 * @return true:処理済み, false:上位に任せる
		 */
		public boolean onTouchEvent(final MotionEvent event);
	}

	private OnTouchableListener mOnTouchableListener;

	public TouchableLinearLayout(final Context context) {
		this(context, null, 0);
	}

	public TouchableLinearLayout(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public TouchableLinearLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public synchronized void setOnTouchableListener(final OnTouchableListener listener) {
		mOnTouchableListener = listener;
	}

	@Override
	public synchronized boolean onInterceptTouchEvent(final MotionEvent event) {
		if (mOnTouchableListener != null) {
			try {
				return mOnTouchableListener.onInterceptTouchEvent(event) || super.onInterceptTouchEvent(event);
			} catch (final Exception e) {
				// ignore
			}
		}
		return super.onInterceptTouchEvent(event);
	}

	@Override
	public synchronized boolean onTouchEvent(final MotionEvent event) {
		if (mOnTouchableListener != null) {
			try {
				return mOnTouchableListener.onTouchEvent(event) || super.onTouchEvent(event);
			} catch (final Exception e) {
				// ignore
			}
		}
		return super.onTouchEvent(event);
	}

}
