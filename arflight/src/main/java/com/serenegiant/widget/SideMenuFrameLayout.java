package com.serenegiant.widget;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
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
import android.view.View;
import android.widget.FrameLayout;

public class SideMenuFrameLayout extends FrameLayout implements ISideMenuView {

	private OnSidleMenuListener mOnSidleMenuListener;

	public SideMenuFrameLayout(final Context context) {
		super(context);
	}

	public SideMenuFrameLayout(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	public SideMenuFrameLayout(final Context context, final AttributeSet attrs, final int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public void hide() {
		final int n = getChildCount();
		int m = 0;
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).hide();
				m++;
			}
		}
		if ((m == 0) && (mOnSidleMenuListener != null)) {
			// 子ViewにISideMenuViewインスタンスが無い時はここでコールバックを呼び出す
			// 子Viewが無い時orISideMenuViewインスタンスが無い時に
			// onSideMenuHideが呼び出されないのを防ぐため
			mOnSidleMenuListener.onSideMenuHide(this);
		}
	}

	@Override
	public void reset() {
		final int n = getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).reset();
			}
		}
	}

	@Override
	public void setOnSidleMenuListener(final OnSidleMenuListener listener) {
		mOnSidleMenuListener = listener;
		final int n = getChildCount();
		for (int i = 0; i < n; i++) {
			final View child = getChildAt(i);
			if (child instanceof ISideMenuView) {
				((ISideMenuView)child).setOnSidleMenuListener(listener);
			}
		}
	}

	@Override
	public OnSidleMenuListener getOnSidleMenuListener() {
		return mOnSidleMenuListener;
	}

}
