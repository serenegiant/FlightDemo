package com.serenegiant.aceparrot;
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

import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 設定画面の各ページ用のViewを提供するためのPagerAdapterクラス
 */
public class ConfigPagerAdapter extends PagerAdapter {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigPagerAdapter.class.getSimpleName();

	private final BaseFragment mParent;
	private final LayoutInflater mInflater;
	private final PagerAdapterConfig[] mConfigs;
	public ConfigPagerAdapter(final BaseFragment parent, final LayoutInflater inflater, final PagerAdapterConfig[] configs) {
		super();
		mParent = parent;
		mInflater = inflater;
		mConfigs = configs;
	}

	@Override
	public synchronized Object instantiateItem(final ViewGroup container, final int position) {
		if (DEBUG) Log.v(TAG, "instantiateItem:position=" + position);
		View view = null;
		final int n = mConfigs != null ? mConfigs.length : 0;
		if ((position >= 0) && (position < n)) {
			final PagerAdapterConfig config = mConfigs[position];
			view = mInflater.inflate(config.layout_id, container, false);
			config.handler.initialize(mParent, view);
		}
		if (view != null) {
			container.addView(view);
		}
		return view;
	}

	@Override
	public synchronized void destroyItem(final ViewGroup container, final int position, final Object object) {
		if (DEBUG) Log.v(TAG, "destroyItem:position=" + position);
		if (object instanceof View) {
			container.removeView((View)object);
		}
	}

	@Override
	public int getCount() {
		return mConfigs != null ? mConfigs.length : 0;
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return view.equals(object);
	}

	@Override
	public CharSequence getPageTitle(final int position) {
		if (DEBUG) Log.v(TAG, "getPageTitle:position=" + position);
		CharSequence result = null;
		final int n = mConfigs != null ? mConfigs.length : 0;
		if ((position >= 0) && (position < n)) {
			result = mParent.getString(mConfigs[position].title_id);
		}
		return result;
	}
}
