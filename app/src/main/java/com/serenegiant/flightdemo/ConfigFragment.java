package com.serenegiant.flightdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

public class ConfigFragment extends ControlFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigFragment.class.getSimpleName();

	public static ConfigFragment newInstance(final ARDiscoveryDeviceService service) {
		final ConfigFragment fragment = new ConfigFragment();
		fragment.setARService(service);
		return fragment;
	}

	private ViewPager mViewPager;
	private ConfigPagerAdapter mPagerAdapter;

	public ConfigFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final View rootView = inflater.inflate(R.layout.fragment_config, container, false);
		mPagerAdapter = new ConfigPagerAdapter(inflater);
		mViewPager = (ViewPager)rootView.findViewById(R.id.config_pager);
		mViewPager.setAdapter(mPagerAdapter);
		return rootView;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		super.onPause();
	}

	private class ConfigPagerAdapter extends PagerAdapter {
		private final LayoutInflater mInflater;
		public ConfigPagerAdapter(final LayoutInflater inflater) {
			super();
			mInflater = inflater;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			if (DEBUG) Log.v(TAG, "instantiateItem:position=" + position);
			View view = null;
			switch (position) {
			case 0:
				view = mInflater.inflate(R.layout.config_minidrone_1, container, false);
				break;
			case 1:
				view = mInflater.inflate(R.layout.config_minidrone_2, container, false);
				break;
			}
			if (view != null) {
				container.addView(view, position);
			}
			return view;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			if (object instanceof View) {
				container.removeView((View)object);
			}
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view.equals(object);
		}

		@Override
		public CharSequence getPageTitle(int position) {
			CharSequence result = null;
			switch (position) {
			case 0:
				result = getString(R.string.config_minidron_1);
				break;
			case 1:
				result = getString(R.string.config_minidron_2);
				break;
			}
			return result;
		}
	}
}
