package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.StackView;

import com.serenegiant.media.MediaStoreHelper;
import com.serenegiant.media.MediaStoreAdapter;

public class PhotoFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = PhotoFragment.class.getSimpleName();

	private static final String KEY_FILE_ID = "PhotoFragment_KEY_FILE_ID";

	private StackView mStackView;
	private MediaStoreAdapter mAdapter;
	private long mId;

	public static PhotoFragment newInstance(final long id) {
		PhotoFragment fragment = new PhotoFragment();
		final Bundle args = new Bundle();
		args.putLong(KEY_FILE_ID, id);
		fragment.setArguments(args);
		return fragment;
	}

	public PhotoFragment() {
		// デフォルトコンストラクタが必要
	}

/*	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
	} */

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		loadArguments(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
		initView(rootView);
		return rootView;
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

	private void loadArguments(final Bundle savedInstanceState) {
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mId = args.getLong(KEY_FILE_ID);
	}

	private void initView(final View rootView) {
		mAdapter = new MediaStoreAdapter(getActivity(), R.layout.grid_item_media);
		mAdapter.setMediaType(MediaStoreHelper.MEDIA_IMAGE);
		mStackView = (StackView)rootView.findViewById(R.id.stackView);
		mStackView.setAdapter(mAdapter);
		mStackView.setSelection(mAdapter.getPositionFromId(mId));
		mStackView.setKeepScreenOn(true);
	}
}
