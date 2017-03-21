package com.serenegiant.aceparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
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

import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import com.serenegiant.arflight.R;

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

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_player2, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		if (DEBUG) Log.v(TAG, "internalOnResume:");
		if (mVideoView != null) {
			mVideoView.resume();
			if (!mVideoView.isPlaying()) {
				mVideoView.start();
			}
		}
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.v(TAG, "internalOnPause:");
		if (mVideoView != null) {
			mVideoView.suspend();
		}
		super.internalOnPause();
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
