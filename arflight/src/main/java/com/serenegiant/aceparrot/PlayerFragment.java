package com.serenegiant.aceparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
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

import java.io.File;

import com.serenegiant.media.MediaMoviePlayer;
import com.serenegiant.media.IFrameCallback;
import com.serenegiant.arflight.R;
import com.serenegiant.widget.PlayerTextureView;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

/**
 * MediaMoviePlayerを使って動画を再生するためのFragment
 */
public class PlayerFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = PlayerFragment.class.getSimpleName();

	private static final String KEY_FILE_NAME = "PlayerFragment_KEY_FILE_NAME";

	// MediaCodecを使う場合
	private PlayerTextureView mPlayerView;	//	private PlayerGLView mPlayerView;
	private ImageButton mPlayerButton;
	private MediaMoviePlayer mPlayer;

	private String mFileName;

	public static PlayerFragment newInstance(final String file_name) {
		PlayerFragment fragment = new PlayerFragment();
		final Bundle args = new Bundle();
		args.putString(KEY_FILE_NAME, TextUtils.isEmpty(file_name) ? "" : file_name);
		fragment.setArguments(args);
		return fragment;
	}

	public PlayerFragment() {
		super();
		// デフォルトコンストラクタが必要
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_player, container, false);

		initView(rootView);
		return rootView;
	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		if (DEBUG) Log.v(TAG, "internalOnResume:");
		if (mPlayerView != null) {
			mPlayerView.onResume();
		}
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.v(TAG, "internalOnPause:");
		stopPlay();
		if (mPlayerView != null) {
			mPlayerView.onPause();
		}
		super.internalOnPause();
	}

	@Override
	protected void loadArguments(final Bundle savedInstanceState) {
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mFileName = args.getString(KEY_FILE_NAME);
	}

	private void initView(final View rootView) {
		mPlayerView = (PlayerTextureView) rootView.findViewById(R.id.player_view);
		mPlayerView.setAspectRatio(640 / 480.f);
		mPlayerView.setSurfaceTextureListener(mSurfaceTextureListener);

		mPlayerButton = (ImageButton) rootView.findViewById(R.id.play_button);
		mPlayerButton.setOnClickListener(mOnClickListener);
	}

	/**
	 * method when touch record button
	 */
	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			int i = view.getId();
			if (i == R.id.play_button) {
				if (mPlayer == null)
					startPlay(mFileName);
				else
					stopPlay();

			}
		}
	};

	/**
	 * start playing
	 */
	private void startPlay(final String file_name) {
		if (DEBUG) Log.v(TAG, "startRecording:");
		final Activity activity = getActivity();
		try {
			final File path = new File(file_name);
			mPlayerButton.setColorFilter(0x7fff0000);    // turn red
			mPlayer = new MediaMoviePlayer(mSurface, mIFrameCallback, true);
			mPlayer.prepare(path.toString());
		} catch (final Exception e) {
			Log.e(TAG, "startPlay:", e);
		}
	}

	/**
	 * request stop playing
	 */
	private void stopPlay() {
		if (DEBUG) Log.v(TAG, "stopRecording:mPlayer=" + mPlayer);
		if (mPlayerButton != null) {
			mPlayerButton.setColorFilter(0);    // return to default color
		}
		if (mPlayer != null) {
			mPlayer.release();
			mPlayer = null;
			// you should not wait here
		}
	}

	private Surface mSurface;
	private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
			if (mSurface != null)
				mSurface.release();
			mSurface = new Surface(surface);
			startPlay(mFileName);
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
			if (mSurface != null) {
				mSurface.release();
				mSurface = null;
			}
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

	/**
	 * callback methods from decoder
	 */
	private final IFrameCallback mIFrameCallback = new IFrameCallback() {
		@Override
		public void onPrepared() {
			final float aspect = mPlayer.getWidth() / (float)mPlayer.getHeight();
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPlayerView.setAspectRatio(aspect);
					}
				});
			}
			mPlayer.play();
		}

		@Override
		public void onFinished() {
			mPlayer = null;
			final Activity activity = getActivity();
			if ((activity != null) && !activity.isFinishing()) {
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPlayerButton.setColorFilter(0);    // return to default color
					}
				});
			}
			popBackStack();
		}

		@Override
		public boolean onFrameAvailable(long presentationTimeUs) {
			return false;
		}
	};

}
