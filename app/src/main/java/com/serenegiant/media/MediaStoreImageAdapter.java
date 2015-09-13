package com.serenegiant.media;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MediaStoreImageAdapter extends PagerAdapter {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = MediaStoreImageAdapter.class.getSimpleName();

	public MediaStoreImageAdapter(final Context context, final int id_layout) {
	}

	@Override
	public int getCount() {
		return 0;
	}

	@Override
	public Object instantiateItem(final ViewGroup container, final int position) {
		return null;
	}

	@Override
	public void destroyItem(final ViewGroup container, final int position, final Object object) {
		super.destroyItem(container, position, object);
	}

	@Override
	public int getItemPosition(final Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public boolean isViewFromObject(final View view, final Object object) {
		return true;
	}
}
