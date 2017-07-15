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

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ListView;

import com.serenegiant.arflight.R;
import com.serenegiant.widget.PlayerTextureView;

import java.io.IOException;

import jp.co.rediscovery.arflight.ARDeviceServiceAdapter;

public abstract class AbstractConnectionFragment extends BaseConnectionFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = AbstractConnectionFragment.class.getSimpleName();

	private PlayerTextureView mVideoView;
	protected ImageButton mDownloadBtn, mPilotBtn, mVoicePilotBtn, mGalleyBrn, mScriptBtn;
	private MediaPlayer mMediaPlayer;
	protected ListView mDeviceListView;

	public AbstractConnectionFragment() {
		super();
		// Required empty public constructor
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		loadArguments(savedInstanceState);
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		return internalCreateView(local_inflater, container, savedInstanceState, R.layout.fragment_connection);
	}

	@Override
	protected View internalCreateView(final LayoutInflater inflater,
		final ViewGroup container, final Bundle savedInstanceState, @LayoutRes final int layout_id) {

		final View rootView = inflater.inflate(layout_id, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		if (DEBUG) Log.d(TAG, "internalOnResume:");
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}

	@Override
	protected void internalOnPause() {
		if (DEBUG) Log.d(TAG, "internalOnPause:");

		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		super.internalOnPause();
	}

	@Override
	public void onDestroy() {
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}

	protected ARDeviceServiceAdapter getDeviceAdapter() {
		return (ARDeviceServiceAdapter) mDeviceListView.getAdapter();
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	protected void initView(final View rootView) {

		final ARDeviceServiceAdapter adapter = new ARDeviceServiceAdapter(getActivity(), R.layout.list_item_deviceservice);

		mDeviceListView = (ListView)rootView.findViewById(R.id.list);
		final View empty_view = rootView.findViewById(R.id.empty_view);
		mDeviceListView.setEmptyView(empty_view);
		mDeviceListView.setAdapter(adapter);
		mDeviceListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		mVideoView = (PlayerTextureView)rootView.findViewById(R.id.videoView);
		mVideoView.setScaleMode(PlayerTextureView.SCALE_MODE_CROP);
		mVideoView.setSurfaceTextureListener(mSurfaceTextureListener);

		mDownloadBtn = (ImageButton)rootView.findViewById(R.id.download_button);
		if (mDownloadBtn != null) {
			mDownloadBtn.setOnClickListener(mOnClickListener);
			mDownloadBtn.setOnLongClickListener(mOnLongClickListener);
		}

		mPilotBtn = (ImageButton)rootView.findViewById(R.id.pilot_button);
		if (mPilotBtn != null) {
			mPilotBtn.setOnClickListener(mOnClickListener);
			mPilotBtn.setOnLongClickListener(mOnLongClickListener);
		}

		mVoicePilotBtn = (ImageButton)rootView.findViewById(R.id.voice_pilot_button);
		if (mVoicePilotBtn != null) {
			mVoicePilotBtn.setOnClickListener(mOnClickListener);
			mVoicePilotBtn.setOnLongClickListener(mOnLongClickListener);
		}

		mGalleyBrn = (ImageButton)rootView.findViewById(R.id.gallery_button);
		if (mGalleyBrn != null) {
			mGalleyBrn.setOnClickListener(mOnClickListener);
			mGalleyBrn.setOnLongClickListener(mOnLongClickListener);
		}

		mScriptBtn = (ImageButton)rootView.findViewById(R.id.script_button);
		if (mScriptBtn != null) {
			mScriptBtn.setOnClickListener(mOnClickListener);
			mScriptBtn.setOnLongClickListener(mOnLongClickListener);
		}

		ImageButton button = (ImageButton)rootView.findViewById(R.id.config_show_btn);
		button.setOnClickListener(mOnClickListener);
		button.setOnLongClickListener(mOnLongClickListener);
	}
	
	@Override
	protected void updateButtonsOnUiThread(final boolean visible) {
		if (!visible) {
			try {
				final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
				adapter.clear();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
		if (mDownloadBtn != null) {
			mDownloadBtn.setVisibility(visibility);
		}
		if (mPilotBtn != null) {
			mPilotBtn.setVisibility(visibility);
		}
		if (mVoicePilotBtn != null) {
			mVoicePilotBtn.setVisibility(visibility);
		}
	}


	private void clearCheck(final ViewGroup parent) {
		final int n = parent.getChildCount();
		for (int i = 0; i < n; i++) {
			final View v = parent.getChildAt(i);
			if (v instanceof Checkable) {
				((Checkable) v).setChecked(false);
			}
		}
	}

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			AbstractConnectionFragment.this.onClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected abstract void onClick(final View view, final int position);

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			if ((mPilotBtn != null) && (mPilotBtn.getVisibility() != View.VISIBLE)) return false;
			return AbstractConnectionFragment.this.onLongClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected abstract boolean onLongClick(final View view, final int position);

	@Override
	protected void onDeviceListUpdated(final ARDeviceServiceAdapter adapter) {
		mDeviceListView.setItemChecked(0, true);	// 先頭を選択
		updateButtons(adapter.getCount() > 0);
	}

//********************************************************************************
// 背景動画再生関連
//********************************************************************************
	protected abstract void setDataSource(final Context context, final MediaPlayer media_player) throws IOException;

	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
			mVideoView.reset();
			try {
				if (mMediaPlayer == null) {
					mMediaPlayer = new MediaPlayer();
					mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
					mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
				}
				if (mMediaPlayer != null) {
					setDataSource(getActivity(), mMediaPlayer);
					mMediaPlayer.prepareAsync();
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(final SurfaceTexture surface, final int width, final int height) {
			mVideoView.reset();
		}

		@Override
		public boolean onSurfaceTextureDestroyed(final SurfaceTexture surface) {
			if (mMediaPlayer != null) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
				}
				mMediaPlayer.release();
				mMediaPlayer = null;
			}
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(final SurfaceTexture surface) {
		}
	};

	private final MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(final MediaPlayer mp) {
			mVideoView.setAspectRatio(mp.getVideoWidth() / (double)mp.getVideoHeight());
			mp.setSurface(new Surface(mVideoView.getSurfaceTexture()));
			mp.setVolume(0.5f, 0.5f);
			mp.setLooping(true);
			mp.start();
		}
	};

	private final MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(final MediaPlayer mp) {
			// 再生が終了したら最初に戻って再度再生する
			// 全体を再生し直すなら#onPreparedでMediaPlayer#setLooping(true);を呼ぶ方が簡単
//			mp.seekTo(0);
//			mp.start();
		}
	};
}
