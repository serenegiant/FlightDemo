package com.serenegiant.aceparrot;

import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.serenegiant.media.MediaStoreImageAdapter;

public class PhotoFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = PhotoFragment.class.getSimpleName();

	private static final String KEY_FILE_ID = "PhotoFragment_KEY_FILE_ID";

	private ViewPager mViewPager;
	private MediaStoreImageAdapter mAdapter;
	private long mId;

	public static PhotoFragment newInstance(final long id) {
		PhotoFragment fragment = new PhotoFragment();
		final Bundle args = new Bundle();
		args.putLong(KEY_FILE_ID, id);
		fragment.setArguments(args);
		return fragment;
	}

	public PhotoFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

/*	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
	} */

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_photo, container, false);
		initView(rootView);
		return rootView;
	}

/*	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
	} */

/*	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		super.onPause();
	} */

	@Override
	protected void loadArguments(final Bundle savedInstanceState) {
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mId = args.getLong(KEY_FILE_ID);
	}

	private void initView(final View rootView) {
		mViewPager = (ViewPager) rootView.findViewById(R.id.viewpager);
		mViewPager.setKeepScreenOn(true);
		mAdapter = new MediaStoreImageAdapter(getActivity(), R.layout.grid_item_media, false);
		// MediaStoreImageAdapterのCursorクエリーは非同期で実行されるので
		// 生成直後はアイテム数が0なのでクエリー完了時にViewPager#setAdapterを実行する
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				super.onChanged();
				mViewPager.setAdapter(mAdapter);
				mViewPager.setCurrentItem(mAdapter.getItemPositionFromID(mId));
				mAdapter.unregisterDataSetObserver(this);	// 初回だけでOKなので登録解除する
			}
			@Override
			public void onInvalidated() {
				super.onInvalidated();
			}
		});
		mAdapter.startQuery();	// 非同期クエリー開始
	}
}
