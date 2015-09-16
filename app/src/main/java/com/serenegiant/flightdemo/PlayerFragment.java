package com.serenegiant.flightdemo;

import java.io.File;

import com.serenegiant.media.MediaMoviePlayer;
import com.serenegiant.media.IFrameCallback;
import com.serenegiant.widget.PlayerTextureView;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.MediaController;
import android.widget.VideoView;

/**
 * MediaMoviePlayerを使って動画を再生するためのFragment
 */
public class PlayerFragment extends BaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
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

/*	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadArguments(savedInstanceState);
	} */

	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {
		super.onViewStateRestored(savedInstanceState);
		loadArguments(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		loadArguments(savedInstanceState);

		final View rootView = inflater.inflate(R.layout.fragment_player, container, false);

		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mPlayerView != null) {
			mPlayerView.onResume();
		}
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		stopPlay();
		if (mPlayerView != null) {
			mPlayerView.onPause();
		}
		super.onPause();
	}

	private void loadArguments(final Bundle savedInstanceState) {
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
			switch (view.getId()) {
			case R.id.play_button:
				if (mPlayer == null)
					startPlay(mFileName);
				else
					stopPlay();
				break;
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
