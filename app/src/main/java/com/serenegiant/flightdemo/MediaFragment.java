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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.armedia.ARMediaObject;
import com.serenegiant.arflight.FTPController;
import com.serenegiant.arflight.IDeviceController;

import java.util.List;

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
	private MediaListAdapter mMediaListAdapter;	// 取得したメディアファイルの一覧アクセス用Adapter

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
		mMediaListAdapter = new MediaListAdapter(getActivity(), R.layout.list_item_media);
		mPagerAdapter = new MediaPagerAdapter(inflater);

		final View rootView = inflater.inflate(R.layout.fragment_media, container, false);
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
		mFTPController.setCallback(mCallback);
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

	/**
	 * 接続後機体のストレージ状態を受信するまで待機するためのRunnable
	 */
	private final Runnable mConnectCheckTask = new Runnable() {
		@Override
		public void run() {
			final String mass_storage_id = mController.getMassStorageName();
			if (TextUtils.isEmpty(mass_storage_id)) {
				post(this, 1000);	// まだ準備出来てないので1秒後に再実行
			} else {
				mFTPController.connect();
			}
		}
	};

	/**
	 * FTPControllerからのコールバック
	 */
	private final FTPController.FTPControllerCallback mCallback = new FTPController.FTPControllerCallback() {
		@Override
		public boolean onError(final Exception e) {
			Log.w(TAG, e);
			return false;
		}

		@Override
		public void onProgress(final int cmd, final float progress) {
			// FIXME 未実装
		}

		@Override
		public void onMediaListUpdated(final List<ARMediaObject> medias) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mMediaListAdapter.clear();
					mMediaListAdapter.addAll(medias);
					mMediaListAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	private ProgressBar mFreeSpaceProgressbar;
	/**
	 * メディアファイル一覧画面の準備
	 * @param rootView
	 */
	private void initMediaList(final View rootView) {
		final ListView listview = (ListView)rootView.findViewById(R.id.listView);
		if (listview != null) {
			final View empty_view = rootView.findViewById(R.id.empty_view);
			listview.setEmptyView(empty_view);
			listview.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				}
				@Override
				public void onNothingSelected(final AdapterView<?> parent) {
				}
			});
			listview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
			listview.setAdapter(mMediaListAdapter);
		}
		mFreeSpaceProgressbar = (ProgressBar)rootView.findViewById(R.id.frees_pace_progress);
	}

	@Override
	protected void updateStorageState(int mass_storage_id, int size, int used_size, boolean plugged, boolean full, boolean internal) {
		if (mFreeSpaceProgressbar != null) {
			mFreeSpaceProgressbar.setProgress((int)(used_size / (float)size * 100f));
		}
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
/*			if ((position >= 0) && (position < PAGER_MEDIA.length)) {
				result = getString(PAGER_MEDIA[position].title_id);
			} */
			return result;
		}
	}
}
