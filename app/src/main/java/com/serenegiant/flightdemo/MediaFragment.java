package com.serenegiant.flightdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.FTPController;
import com.serenegiant.arflight.IDeviceController;

public class MediaFragment extends ControlBaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = MediaFragment.class.getSimpleName();

	public static MediaFragment newInstance(final ARDiscoveryDeviceService device) {
		final MediaFragment fragment = new MediaFragment();
		fragment.setDevice(device);
		return fragment;
	}

	private FTPController mFTPController;
	private ViewPager mViewPager;
	private MediaPagerAdapter mPagerAdapter;

	public MediaFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(final Activity activity) {
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
		final View rootView = inflater.inflate(R.layout.fragment_media, container, false);
		mPagerAdapter = new MediaPagerAdapter(inflater);
		mViewPager = (ViewPager)rootView.findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAdapter);
		return rootView;
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		if (mFTPController != null) {
			mFTPController.release();
			mFTPController = null;
		}
		stopDeviceController(false);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		startDeviceController();
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		super.onPause();
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onConnect");
		super.onConnect(controller);
		mFTPController = FTPController.newInstance(getActivity().getApplicationContext(), mController);
		post(mConnectCheckTask, 500);
	}

	/** 切断された時に前のフラグメントに戻るまでの遅延時間[ミリ秒] */
	private static final long POP_BACK_STACK_DELAY = 2000;
	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "#onDisconnect");
		requestPopBackStack(POP_BACK_STACK_DELAY);
		super.onDisconnect(controller);
	}

	private final Runnable mConnectCheckTask = new Runnable() {
		@Override
		public void run() {
			final String mass_storage_id = mController.getMassStorageName();
			if (TextUtils.isEmpty(mass_storage_id)) {
				post(this, 1000);
			} else {
				mFTPController.connect();
			}
		}
	};


	private void initMediaList(final View rootView) {
	}


	private static interface AdapterItemHandler {
		public void initialize(final MediaFragment parent, final View view);
	}

	private static final class PagerAdapterConfig {
		public final int title_id;
		public final int layout_id;
		public final AdapterItemHandler handler;

		public PagerAdapterConfig(final int _title_id, final int _layout_id, final AdapterItemHandler _handler) {
			title_id = _title_id;
			layout_id = _layout_id;
			handler = _handler;
		}
	}

	private static PagerAdapterConfig[] PAGER_MEDIA;
	static {
		PAGER_MEDIA = new PagerAdapterConfig[1];
		PAGER_MEDIA[0] = new PagerAdapterConfig(R.string.media_title_list, R.layout.media_list, new AdapterItemHandler() {
			@Override
			public void initialize(final MediaFragment parent, final View view) {
				parent.initMediaList(view);
			}
		});
	};
	/**
	 * メディア画面の各ページ用のViewを提供するためのPagerAdapterクラス
	 */
	private class MediaPagerAdapter extends PagerAdapter {
		private final LayoutInflater mInflater;
		public MediaPagerAdapter(final LayoutInflater inflater) {
			super();
			mInflater = inflater;
		}

		@Override
		public synchronized Object instantiateItem(final ViewGroup container, final int position) {
			if (DEBUG) Log.v(TAG, "instantiateItem:position=" + position);
			View view = null;
			if ((position >= 0) && (position < PAGER_MEDIA.length)) {
				final PagerAdapterConfig config = PAGER_MEDIA[position];
				view = mInflater.inflate(config.layout_id, container, false);
				config.handler.initialize(MediaFragment.this, view);
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
			return PAGER_MEDIA.length;
		}

		@Override
		public boolean isViewFromObject(final View view, final Object object) {
			return view.equals(object);
		}

		@Override
		public CharSequence getPageTitle(final int position) {
			if (DEBUG) Log.v(TAG, "getPageTitle:position=" + position);
			CharSequence result = null;
			if ((position >= 0) && (position < PAGER_MEDIA.length)) {
				result = getString(PAGER_MEDIA[position].title_id);
			}
			return result;
		}
	}
}
