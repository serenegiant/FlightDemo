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
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = BridgeFragment.class.getSimpleName();

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device, final boolean newAPI) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device, newAPI);
		return fragment;
	}

	private ListView mDeviceListView;
	private PlayerTextureView mVideoView;
	private ImageButton mDownloadBtn, mPilotBtn;
	private MediaPlayer mMediaPlayer;
	private boolean mIsConnectToDevice;
	private boolean mNeedRequestDeviceList;

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
		super.onDetach();
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateView:");
		onBeforeCreateView();
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_bridge, container, false);
		initView(rootView);
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume:");
		mIsConnectToDevice = false;
		if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
		if (mController instanceof ISkyController) {
			mController.addListener(mSkyControllerListener);
		}
		if (DEBUG) Log.v(TAG, "onResume:isAdded=" + isAdded() + ",isDetached=" + isDetached()
			+ ",isHidden=" + isHidden() + ",isInLayout=" + isInLayout()
			+ ",isRemoving=" + isRemoving() + ",isResumed=" + isResumed()
			+ ",isVisible=" + isVisible() + ",mIsConnectToDevice=" + mIsConnectToDevice);
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
		if (mController != null) {
			mController.removeListener(mSkyControllerListener);
			if (!mIsConnectToDevice) {
				releaseDeviceController(true);
			} else {
				mNeedRequestDeviceList = true;
			}
			mController = null;
		}
		if (DEBUG) Log.v(TAG, "onPause:isAdded=" + isAdded() + ",isDetached=" + isDetached()
			+ ",isHidden=" + isHidden() + ",isInLayout=" + isInLayout()
			+ ",isRemoving=" + isRemoving() + ",isResumed=" + isResumed()
			+ ",isVisible=" + isVisible() + ",mIsConnectToDevice=" + mIsConnectToDevice);
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

	@Override
	protected void onBeforeCreateView() {
	}

//	/**
//	 * 接続された
//	 * @param controller
//	 */
//	protected void onConnect(final IDeviceController controller) {
//		if (DEBUG) Log.v(TAG, "onConnect:");
//		super.onConnect(controller);
//	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	private void initView(final View rootView) {

		final ARDeviceInfoAdapter adapter = new ARDeviceInfoAdapter(getActivity(), R.layout.list_item_deviceservice);

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

	protected synchronized boolean startDeviceController() {
		final boolean already_connected = super.startDeviceController();
		if (already_connected) {
			onSkyControllerConnect(mController);
		}
		return already_connected;
	}

	protected void releaseDeviceController(final boolean disconnected) {
		mIsConnectToDevice = mNeedRequestDeviceList = false;
		super.releaseDeviceController(disconnected);
	}

	@Override
	protected void updateBattery(final IDeviceController controller, final int percent) {

	}

	@Override
	protected void updateWiFiSignal(final IDeviceController controller, final int rssi) {

	}

	@Override
	protected void updateAlarmState(final IDeviceController controller, final int alert_state) {

	}

	@Override
	protected void onConnect(final IDeviceController controller) {

	}

	@Override
	protected void onSkyControllerConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onSkyControllerConnect:controller=" + controller);
		if (mNeedRequestDeviceList) {
			mNeedRequestDeviceList = false;
			final ISkyController ctrl = (ISkyController)controller;
//			ctrl.requestWifiList();
//			ctrl.requestDeviceList();
//			ctrl.requestCurrentDevice();
			ctrl.requestAllStates();
		}
	}

	/**
	 * スカイコントローラーのキャリブレーションの状態が変化した時
	 * @param controller
	 * @param need_calibration
	 */
	protected void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
		if (DEBUG) Log.v(TAG, "onSkyControllerCalibrationRequiredChanged:controller=" + controller + ",need_calibration=" + need_calibration);
	}

	/**
	 * スカイコントローラーのキャリブレーションを開始した
	 */
	protected void onSkyControllerStartCalibration(final IDeviceController controller) {
	}

	/**
	 * スカイコントローラーのキャリブレーションが終了した
	 */
	protected void onSkyControllerStopCalibration(final IDeviceController controller) {
	}

	/**
	 * スカイコントローラーのキャリブレーション中の軸が変更された
	 * @param controller
	 * @param axis
	 */
	protected void updateSkyControllerCalibrationAxis(final IDeviceController controller, final int axis) {
		if (DEBUG) Log.v(TAG, "updateSkyControllerCalibrationAxis:controller=" + controller + ",axis=" + axis);
	}

	private final SkyControllerListener mSkyControllerListener = new SkyControllerListener() {
		@Override
		public void onSkyControllerConnect(final IDeviceController controller) {
			BridgeFragment.this.onSkyControllerConnect(controller);
		}

		@Override
		public void onSkyControllerDisconnect(final IDeviceController controller) {
			BridgeFragment.this.onSkyControllerDisconnect(controller);
		}

		@Override
		public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent) {
			BridgeFragment.this.updateSkyControllerBattery(controller, percent);
		}

		@Override
		public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			BridgeFragment.this.updateSkyControllerAlarmState(controller, alarm_state);
		}

		@Override
		public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
			BridgeFragment.this.onSkyControllerCalibrationRequiredChanged(controller, need_calibration);
		}

		@Override
		public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart) {
			if (isStart) {
				onSkyControllerStartCalibration(controller);
			} else {
				onSkyControllerStopCalibration(controller);
			}
		}

		@Override
		public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis) {
			updateSkyControllerCalibrationAxis(controller, axis);
		}

		@Override
		public void onConnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onConnect:controller=" + controller);
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
						final DeviceInfo info = bridge.getCurrentDevice();
						final int numDevices = bridge.getDeviceNum();
						if (bridge.isConnected() && (info != null) && (numDevices == 1)) {
							if (DEBUG) Log.v(TAG, "既に1機だけ検出&接続されていたら操縦画面へ");
							// 検出している機体が1機でそれに接続している時は操縦画面へ
							// FIXME ただし今はアイコン長押しでトレース/トラッキングモードに移行できるようにしているので自動では遷移しない
//							replace(PilotFragment2.newInstance(controller.getDeviceService(), info));
						}
					} catch (final Exception e) {
					}
				}
			} , 1000);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onDisconnect:controller=" + controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onUpdateBattery:controller=" + controller + ", percent=" + percent);
		}

		@Override
		public void onUpdateWiFiSignal(final IDeviceController controller, final int rssi) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onUpdateWiFiSignal:controller=" + controller + ", rssi=" + rssi);
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			if (DEBUG) Log.v(TAG, "SkyControllerListener#onAlarmStateChangedUpdate:controller=" + controller + ", alarm_state=" + alarm_state);
		}
	};

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
			if (DEBUG) Log.v(TAG, "updateDeviceList:product=" + product);
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
			if (fragment != null) {
				replace(fragment);
			}
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
							fragment = AutoPilotFragment2.newInstance(device, info, "test001", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
							break;
						case R.id.download_button:
							fragment = AutoPilotFragment2.newInstance(device, info, "test002", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
							break;
						case R.id.gallery_button:
							fragment = AutoPilotFragment2.newInstance(device, info, "test003", AutoPilotFragment2.MODE_TRACE, mController.isNewAPI());
							break;
						case R.id.script_button:
							fragment = AutoPilotFragment2.newInstance(device, info, "test004", AutoPilotFragment2.MODE_TRACKING, mController.isNewAPI());
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
				mIsConnectToDevice = mNeedRequestDeviceList = true;
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
			mIsConnectToDevice = mNeedRequestDeviceList = true;
			fragment = isPiloting ? PilotFragment2.newInstance(mController.getDeviceService(), info, mController.isNewAPI())
				: MediaFragment.newInstance(mController.getDeviceService(), info, mController.isNewAPI());
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
