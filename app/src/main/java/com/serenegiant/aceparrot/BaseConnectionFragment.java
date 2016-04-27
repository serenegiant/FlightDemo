package com.serenegiant.aceparrot;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ListView;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.ARDeviceServiceAdapter;
import com.serenegiant.arflight.ManagerFragment;
import com.serenegiant.widget.PlayerTextureView;

import java.util.List;

import static com.serenegiant.arflight.ARFlightConst.*;

public abstract class BaseConnectionFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = BaseConnectionFragment.class.getSimpleName();

	private boolean mIsNewAPI;
	private PlayerTextureView mVideoView;
	protected ImageButton mDownloadBtn, mPilotBtn, mGalleyBrn, mScriptBtn;
	private MediaPlayer mMediaPlayer;
	protected ListView mDeviceListView;

	public BaseConnectionFragment() {
		super();
		// Required empty public constructor
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		loadArguments(savedInstanceState);
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_connection, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		manager.startDiscovery();
		manager.addCallback(mManagerCallback);
		updateButtons(false);
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause:");

		updateButtons(false);
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		if (manager != null) {
			manager.removeCallback(mManagerCallback);
			manager.stopDiscovery();
		}
		super.onPause();
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

	protected void loadArguments(final Bundle _args) {
		super.loadArguments(_args);
		Bundle args = _args;
		if (args == null) {
			args = getArguments();
		}
		if (args != null) {
			mIsNewAPI = args.getBoolean(ARFLIGHT_EXTRA_NEWAPI, false);
		}
	}

	protected Bundle setNewAPI(final boolean newAPI) {
		mIsNewAPI = newAPI;
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putBoolean(ARFLIGHT_EXTRA_NEWAPI, newAPI);
		setArguments(args);
		return args;
	}

	public boolean isNewAPI() {
		return mIsNewAPI;
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

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
		mDownloadBtn.setOnClickListener(mOnClickListener);
		mDownloadBtn.setOnLongClickListener(mOnLongClickListener);

		mPilotBtn = (ImageButton)rootView.findViewById(R.id.pilot_button);
		mPilotBtn.setOnClickListener(mOnClickListener);
		mPilotBtn.setOnLongClickListener(mOnLongClickListener);

		mGalleyBrn = (ImageButton)rootView.findViewById(R.id.gallery_button);
		mGalleyBrn.setOnClickListener(mOnClickListener);
		mGalleyBrn.setOnLongClickListener(mOnLongClickListener);

		mScriptBtn = (ImageButton)rootView.findViewById(R.id.script_button);
		mScriptBtn.setOnClickListener(mOnClickListener);
		mScriptBtn.setOnLongClickListener(mOnLongClickListener);

		ImageButton button = (ImageButton)rootView.findViewById(R.id.config_show_btn);
		button.setOnClickListener(mOnClickListener);
		button.setOnLongClickListener(mOnLongClickListener);
	}

	private void updateButtons(final boolean visible) {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					updateButtonsOnUiThread(visible);
				}
			});
		}
	}

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
		mDownloadBtn.setVisibility(visibility);
		mPilotBtn.setVisibility(visibility);
	}

	/**
	 * 検出したデバイスのリストが更新された時のコールバック
	 */
	private ManagerFragment.ManagerCallback mManagerCallback = new ManagerFragment.ManagerCallback() {
		@Override
		public void onServicesDevicesListUpdated(final List<ARDiscoveryDeviceService> devices) {
			if (DEBUG) Log.v(TAG, "onServicesDevicesListUpdated:");
			final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter) mDeviceListView.getAdapter();
			adapter.clear();
			for (final ARDiscoveryDeviceService service : devices) {
				if (DEBUG) Log.d(TAG, "service :  " + service);
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(service.getProductID());
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// bebop2
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_JS:		// JumpingSumo
				case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
				case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
					// FIXME JumpingSumoは未実装
					break;
				case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
				case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
	//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
					adapter.add(service);
					break;
				case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyControllerNewAPI
					if (isNewAPI() && BuildConfig.USE_SKYCONTROLLER) {
						adapter.add(service);
					}
					break;
				case ARDISCOVERY_PRODUCT_NSNETSERVICE:
					break;
				}
/*				// ブルートゥース接続の時だけ追加する
				if (service.getDevice() instanceof ARDiscoveryDeviceBLEService) {
					adapter.add(service.getName());
				} */
			}
			adapter.notifyDataSetChanged();
			mDeviceListView.setItemChecked(0, true);	// 先頭を選択
			updateButtons(adapter.getCount() > 0);
		}
	};

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
			BaseConnectionFragment.this.onClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected abstract void onClick(final View view, final int position);

	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			if (mPilotBtn.getVisibility() != View.VISIBLE) return false;
			return BaseConnectionFragment.this.onLongClick(view, mDeviceListView.getCheckedItemPosition());
		}
	};

	protected abstract boolean onLongClick(final View view, final int position);

	protected Fragment getFragment(final int position, final boolean isPiloting) {
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
		final String itemValue = adapter.getItemName(position);
		final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
		Fragment fragment = null;
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());

			switch (product) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
				fragment = isPiloting ? PilotFragment2.newInstance(device, null, isNewAPI()) : MediaFragment.newInstance(device, null, isNewAPI());
				break;
			case ARDISCOVERY_PRODUCT_JS:        // JumpingSumo
				//FIXME JumpingSumoは未実装
				break;
			case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
				// XXX MinidroneをNewAPIで動かすと機体が滑らかに飛行しないので常に従来APIにする
				fragment = isPiloting ? PilotFragment2.newInstance(device, null, false) : MediaFragment.newInstance(device, null, false);
				break;
			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyControllerNewAPI
				if (BuildConfig.USE_SKYCONTROLLER) {
					fragment = newBridgetFragment(device, true);	// NewAPIを使う
				}
				break;
			}
		}
		return fragment;
	}

	protected abstract BaseBridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device, final boolean newAPI);

//********************************************************************************
// 背景動画再生関連
//********************************************************************************
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
					mMediaPlayer.setDataSource(getActivity(), Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.into_the_sky));
					mMediaPlayer.prepareAsync();
				};
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
