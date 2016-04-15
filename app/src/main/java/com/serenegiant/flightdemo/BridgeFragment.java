package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.serenegiant.arflight.ARDeviceInfoAdapter;
import com.serenegiant.arflight.DeviceInfo;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.ISkyController;
import com.serenegiant.arflight.ManagerFragment;
import com.serenegiant.arflight.SkyControllerListener;
import com.serenegiant.widget.PlayerTextureView;


import static com.serenegiant.arflight.ARFlightConst.*;

/**
 * スカイコントローラーに接続してスカイコントローラーが
 * 検出している機体の一覧取得＆選択を行うためのFragment
 */
public class BridgeFragment extends BaseControllerFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = BridgeFragment.class.getSimpleName();

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device, true);	// NewAPIを使う
		return fragment;
	}

	private ListView mDeviceListView;
	private PlayerTextureView mVideoView;
	private ImageButton mDownloadBtn, mPilotBtn;
	private MediaPlayer mMediaPlayer;

	public BridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
		final IntentFilter filter = new IntentFilter(ARFLIGHT_ACTION_DEVICE_LIST_CHANGED);
		mLocalBroadcastManager.registerReceiver(mBroadcastReceiver, filter);
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		mLocalBroadcastManager.unregisterReceiver(mBroadcastReceiver);
//		stopDeviceController(false);
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_bridge, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
		if (mController instanceof ISkyController) {
			mController.addListener(mSkyControllerListener);
		}
		startDeviceController();
		updateButtons(false);
	}

	@Override
	public void onPause() {
		if (DEBUG) Log.d(TAG, "onPause:");

		updateButtons(false);
		if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.stop();
		}
		mController.removeListener(mSkyControllerListener);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.d(TAG, "onDestroy:");
		if (mMediaPlayer != null) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.stop();
			}
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
		super.onDestroy();
	}

	/**
	 * 接続された
	 * @param controller
	 */
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onConnect:");
		super.onConnect(controller);
		if (controller instanceof ISkyController) {
			((ISkyController)mController).requestDeviceList();	// これは動かないみたい
			((ISkyController)mController).requestCurrentDevice();
		}
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

		final ARDeviceInfoAdapter adapter = new ARDeviceInfoAdapter(getActivity(), R.layout.list_item_deviceservice);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mMediaPlayer.setOnCompletionListener(mOnCompletionListener);

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

		ImageButton button = (ImageButton)rootView.findViewById(R.id.gallery_button);
		button.setOnClickListener(mOnClickListener);
		button.setOnLongClickListener(mOnLongClickListener);

		button = (ImageButton)rootView.findViewById(R.id.script_button);
		button.setOnClickListener(mOnClickListener);
		button.setOnLongClickListener(mOnLongClickListener);

		button = (ImageButton)rootView.findViewById(R.id.config_show_btn);
		button.setOnClickListener(mOnClickListener);
		button.setOnLongClickListener(mOnLongClickListener);
	}

	private void updateButtons(final boolean visible) {
		final Activity activity = getActivity();
		if ((activity != null) && !activity.isFinishing()) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (!visible) {
						try {
							final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
							adapter.clear();
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					}
					final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
					mDownloadBtn.setVisibility(visibility);
					mPilotBtn.setVisibility(visibility);
				}
			});
		}
	}

	private final SkyControllerListener mSkyControllerListener = new SkyControllerListener() {
		@Override
		public void onSkyControllerConnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onSkyControllerConnect:controller=" + controller);
		}

		@Override
		public void onSkyControllerDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onSkyControllerDisconnect:controller=" + controller);
		}

		@Override
		public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent) {
			if (DEBUG) Log.v(TAG, "onSkyControllerUpdateBattery:controller=" + controller);
		}

		@Override
		public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			if (DEBUG) Log.v(TAG, "onSkyControllerAlarmStateChangedUpdate:controller=" + controller);
		}

		@Override
		public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
			if (DEBUG) Log.v(TAG, "onSkyControllerCalibrationRequiredChanged:controller=" + controller);
		}

		@Override
		public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart) {
			if (DEBUG) Log.v(TAG, "onSkyControllerCalibrationStartStop:controller=" + controller);
		}

		@Override
		public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis) {
			if (DEBUG) Log.v(TAG, "onSkyControllerCalibrationAxisChanged:controller=" + controller);
		}

		@Override
		public void onConnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onConnect:controller=" + controller);
			final ISkyController bridge = (ISkyController)controller;
			queueEvent(new Runnable() {
				@Override
				public void run() {
//					bridge.setCoPilotingSource(1);
//					bridge.requestWifiList();
//					bridge.requestCurrentWiFi();
//					bridge.requestDeviceList();
//					bridge.requestCurrentDevice();
//					bridge.setSkyControllerSSID("SkyController_8376");
//					bridge.setSkyControllerSSID("SkyController_saki");
//					bridge.resetSettings();
//					bridge.resetCameraOrientation();
//					bridge.requestPresetAxisFilters();
//					bridge.requestAvailableAxisMappings();
//					bridge.resetAxisMapping();
//					bridge.requestGamepadControls();
//					bridge.requestButtonEventsSettings();
				}
			}, 0);
			queueEvent(new Runnable() {
				@Override
				public void run() {
					try {
						final DeviceInfo info = bridge.connectDeviceInfo();
						if (bridge.isConnected() && (info != null)) {
							if (DEBUG) Log.v(TAG, "既に接続されていたら操縦画面へ");
							// FIXME 検出している機体が1機でそれに接続している時は操縦画面へ
							// XXX ただし今はアイコン長押しでトレース/トラキングモードに移行できるようにしているので自動では遷移しない
//							replace(PilotFragment2.newInstance(controller.getDeviceService(), info));
						}
					} catch (final Exception e) {
					}
				}
			} , 1000);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "onDisconnect:controller=" + controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
			if (DEBUG) Log.v(TAG, "onUpdateBattery:controller=" + controller + ", percent=" + percent);
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			if (DEBUG) Log.v(TAG, "onAlarmStateChangedUpdate:controller=" + controller + ", alarm_state=" + alarm_state);
		}
	};

	private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
			if (ARFLIGHT_ACTION_DEVICE_LIST_CHANGED.equals(action)) {
				final DeviceInfo[] info_array
					= intent.hasExtra(ARFLIGHT_EXTRA_DEVICE_LIST)
					? (DeviceInfo[])intent.getParcelableArrayExtra(ARFLIGHT_EXTRA_DEVICE_LIST)
					: null;
				updateDeviceList(info_array);
			}
		}
	};

	/** 検出した機体をリストに登録する, Bridge接続はBebop/Bebop2のみ対応 */
	private void updateDeviceList(final DeviceInfo[] info_array) {
		if (DEBUG) Log.v(TAG, "updateDeviceList:" + info_array);
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter) mDeviceListView.getAdapter();
		adapter.clear();
		final int n = info_array != null ? info_array.length : 0;
		for (int i = 0; i < n; i++) {
			final DeviceInfo info = info_array[i];
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
			switch (product) {
			case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
			case ARDISCOVERY_PRODUCT_BEBOP_2:	// bebop2
				adapter.add(info);
				break;
//			case ARDISCOVERY_PRODUCT_JS:		// JumpingSumo
//			case ARDISCOVERY_PRODUCT_JS_EVO_LIGHT:
//			case ARDISCOVERY_PRODUCT_JS_EVO_RACE:
//				// FIXME JumpingSumoは未実装
//				break;
//			case ARDISCOVERY_PRODUCT_MINIDRONE:	// RollingSpider
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_LIGHT:
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_BRICK:
//			case ARDISCOVERY_PRODUCT_MINIDRONE_EVO_HYDROFOIL: // ハイドロフォイルもいる?
//				adapter.add(info);
//				break;
//			case ARDISCOVERY_PRODUCT_SKYCONTROLLER:	// SkyController
//				adapter.add(info);
//				break;
//			case ARDISCOVERY_PRODUCT_NSNETSERVICE:
//				break;
			}
		}
		adapter.notifyDataSetChanged();
		mDeviceListView.setItemChecked(0, true);	// 先頭を選択
		updateButtons(n > 0);
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

	/** アイコンにタッチした時の処理 */
	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			if (DEBUG) Log.v(TAG, "onClick:");
			Fragment fragment = null;
			switch (view.getId()) {
			case R.id.pilot_button:
				fragment = getFragment(mDeviceListView.getCheckedItemPosition(), true);
				break;
			case R.id.download_button:
				fragment = getFragment(mDeviceListView.getCheckedItemPosition(), false);
				break;
			case R.id.gallery_button:
				fragment = GalleyFragment.newInstance();
				break;
			case R.id.script_button:
				fragment = ScriptFragment.newInstance();
				break;
			case R.id.config_show_btn:
				fragment = ConfigAppFragment.newInstance();
				break;
			}
			replace(fragment);
		}
	};

	/** アイコンを長押しした時の処理 */
	private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(final View view) {
			if (mPilotBtn.getVisibility() != View.VISIBLE) return false;
			if (DEBUG) Log.v(TAG, "onLongClick:");
			mVibrator.vibrate(50);
			Fragment fragment = null;
			final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
			final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
			final int position = mDeviceListView.getCheckedItemPosition();
//			final String itemValue = adapter.getItemName(position);
			final DeviceInfo info = adapter.getItem(position);
			final ARDiscoveryDeviceService device = mController.getDeviceService();
			if (device != null) {
				// 製品名を取得
				final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
				final int id = view.getId();
				switch (id) {
				case R.id.pilot_button:
				case R.id.download_button:
				case R.id.gallery_button:
				case R.id.script_button:
					switch (product) {
					case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
					case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
						switch (id) {
						case R.id.pilot_button:
							fragment = AutoPilotFragment2NewAPI.newInstance(device, info, "test011", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.download_button:
							fragment = AutoPilotFragment2NewAPI.newInstance(device, info, "test012", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.gallery_button:
							fragment = AutoPilotFragment2NewAPI.newInstance(device, info, "test013", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.script_button:
							fragment = AutoPilotFragment2NewAPI.newInstance(device, info, "test014", AutoPilotFragment2.MODE_TRACKING);
							break;
						default:
							Log.w(TAG, "未知のview idが来た。なんでやねん:" + id);
							break;
						}
						break;
					default:
						Log.w(TAG, "未知の機体が来た:" + product);
						break;
					}
					break;
				default:
					Log.w(TAG, "未知のview idが来た:" + id);
					break;
				}
			} else {
				Log.w(TAG, "機体が取得できなかった:position=" + position);
			}
			if (fragment != null) {
				replace(fragment);
				return true;
			}
			return false;
		}
	};

	/** アイコンにタッチした時の処理の下請け, 選択している機体に対応するFragmentを生成する */
	private Fragment getFragment(final int position, final boolean isPiloting) {
		if (DEBUG) Log.v(TAG, "getFragment:");
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
//		final String itemValue = adapter.getItemName(position);
		final DeviceInfo info = adapter.getItem(position);
		Fragment fragment = null;
		// 製品名を取得
		final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
		switch (product) {
		case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
		case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
			fragment = isPiloting ? PilotFragment2.newInstance(mController.getDeviceService(), info) : MediaFragment.newInstance(mController.getDeviceService(), info);
			break;
		}
		return fragment;
	}

//********************************************************************************
// 背景動画再生関連
//********************************************************************************
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

		@Override
		public void onSurfaceTextureAvailable(final SurfaceTexture surface, final int width, final int height) {
			mVideoView.reset();
			try {
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
