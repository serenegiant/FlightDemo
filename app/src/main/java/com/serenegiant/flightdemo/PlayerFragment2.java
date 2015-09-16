package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * VideoView+MediaControllerを使って動画再生するためのFragment
 */
public class PlayerFragment2 extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = PlayerFragment2.class.getSimpleName();

	private static final String KEY_FILE_NAME = "PlayerFragment2_KEY_FILE_NAME";

	private VideoView mVideoView;
	private String mFileName;

	public static PlayerFragment2 newInstance(final String file_name) {
		PlayerFragment2 fragment = new PlayerFragment2();
		final Bundle args = new Bundle();
		args.putString(KEY_FILE_NAME, TextUtils.isEmpty(file_name) ? "" : file_name);
		fragment.setArguments(args);
		return fragment;
	}

	public PlayerFragment2() {
		super();
		// デフォルトコンストラクタが必要
		setRetainInstance(true);
	}

/*	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
	} */

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final View rootView = inflater.inflate(R.layout.fragment_player2, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mVideoView != null) {
			mVideoView.resume();
			if (!mVideoView.isPlaying()) {
				mVideoView.start();
			}
		}
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mVideoView != null) {
			mVideoView.suspend();
		}
		super.onPause();
	}

	@Override
	protected void loadArguments(final Bundle savedInstanceState) {
		super.loadArguments(savedInstanceState);
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mFileName = args.getString(KEY_FILE_NAME);
	}

	private void initView(final View rootView) {
		mVideoView = (VideoView)rootView.findViewById(R.id.videoView);
		mVideoView.setMediaController(new MediaController(getActivity()));
		mVideoView.setVideoPath(mFileName);
		mVideoView.setOnCompletionListener(mOnCompletionListener);
	}

	private final MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(final MediaPlayer mp) {
			// 再生が終了したら一覧画面に戻る
			popBackStack();
		}
	};
}
