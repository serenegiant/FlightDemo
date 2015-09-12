package com.serenegiant.media;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class MediaStoreImageAdapter extends PagerAdapter {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = MediaStoreImageAdapter.class.getSimpleName();

	private final MediaStoreAdapter mAdapter;

	public MediaStoreImageAdapter(final Context context, final int id_layout) {
		mAdapter = new MediaStoreAdapter(context, id_layout);
	}

	@Override
	public int getCount() {
		return mAdapter.getCount();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		return mAdapter.getView(position, null, container);
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		super.destroyItem(container, position, object);
	}

	@Override
	public int getItemPosition(Object object) {
		return super.getItemPosition(object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return false;
	}
}
