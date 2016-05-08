package com.serenegiant.aceparrot;

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
