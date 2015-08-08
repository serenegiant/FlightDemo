package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceControllerListener;
import com.serenegiant.arflight.IDeviceController;

public abstract class ControlFragment extends Fragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ControlFragment.class.getSimpleName();

	protected static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";

	private final Handler mUIHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private ARDiscoveryDeviceService mDevice;
	protected IDeviceController deviceController;

	protected volatile int mFlyingState = 0;
	protected volatile int mAlertState = 0;
	protected volatile int mBatteryState = 0;
	protected boolean mIsFlying = false;	// FIXME mFlyingStateを参照するようにしてmIsFlyingフラグは削除する
	protected boolean mIsConnected = false;

	public ControlFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	}

	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	}

	@Override
	public synchronized void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			mDevice = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			deviceController = ManagerFragment.getController(getActivity(), mDevice);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		final Bundle args = getArguments();
		if (args != null) {
			outState.putAll(args);
		}
	}

	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (deviceController != null) {
			deviceController.addListener(mDeviceControllerListener);
		}
	}

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (deviceController != null) {
			deviceController.removeListener(mDeviceControllerListener);
		}
		super.onPause();
	}

	protected Bundle setDevice(final ARDiscoveryDeviceService device) {
		mDevice = device;
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_DEVICE_SERVICE, device);
		setArguments(args);
		return args;
	}

	protected ARDiscoveryDeviceService getDevice() {
		return mDevice;
	}

	protected void runOnUiThread(final Runnable task) {
		if (task != null) {
			try {
				if (mUIThreadId != Thread.currentThread().getId()) {
					mUIHandler.post(task);
				} else {
					task.run();
				}
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	protected synchronized void startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		if (deviceController == null) {
			deviceController = ManagerFragment.getController(getActivity(), mDevice);
		}
		if (deviceController != null) {
			if (!deviceController.isStarted()) {
				mBatteryState = -1;
				updateBattery(mBatteryState);

				final ProgressDialog dialog = new ProgressDialog(getActivity());
				dialog.setTitle(R.string.connecting);
				dialog.setIndeterminate(true);
				dialog.show();

				new Thread(new Runnable() {
					@Override
					public void run() {
						final boolean failed = deviceController.start();

						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dialog.dismiss();
							}
						});

						mIsConnected = !failed;
						if (failed) {
							try {
								getFragmentManager().popBackStack();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						}
					}
				}).start();
			} else {
//				if (DEBUG) Log.v(TAG, "設定読み込み＆ステータス要求");
//				deviceController.sendAllSettings();
//				deviceController.sendAllStates();
				// sendAllSettingsとかsendAllStatesは接続した直後に1回しか有効じゃないのかも
				updateBattery(mBatteryState);
			}
		}
	}

	protected synchronized void stopDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		mIsConnected = mIsFlying = false;
		mFlyingState = mBatteryState = -1;
		if (deviceController != null) {
			final IDeviceController controller = deviceController;
			deviceController = null;
			final Activity activity = getActivity();
			final ProgressDialog dialog;
			if (activity != null && !activity.isFinishing()) {
				dialog = new ProgressDialog(activity);
				dialog.setTitle(R.string.disconnecting);
				dialog.setIndeterminate(true);
				dialog.show();
			} else {
				dialog = null;
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					controller.stop();
					if (dialog != null) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								dialog.dismiss();
							}
						});
					}
				}
			}).start();
		}
	}

	/**
	 * 非常停止指示
	 */
	protected void emergencyStop() {
		if (deviceController != null) {
			deviceController.sendEmergency();
		}
	}

	protected void updateBattery(final int battery) {
	}

	protected void updateFlyingState(final int state) {
	}

	protected void updateAlertState(final int alert_state) {
	}

	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		if (controller.isStarted()) {
			stopDeviceController(true);
		}
	}

	protected void onAlertStateChangedUpdate(int alert_state) {
	}

	private final DeviceControllerListener mDeviceControllerListener
		= new DeviceControllerListener() {
		@Override
		public void onConnect(IDeviceController controller) {
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			ControlFragment.this.onDisconnect(controller);
		}

		@Override
		public void onUpdateBattery(final byte percent) {
			if (mBatteryState != percent) {
				mBatteryState = percent;
				updateBattery(percent);
			}
		}

		@Override
		public void onFlyingStateChangedUpdate(final int state) {
			if (mFlyingState != state) {
				mFlyingState = state;
				updateFlyingState(state);
			}
		}

		@Override
		public void onAlertStateChangedUpdate(int alert_state) {
			if (DEBUG) Log.v(TAG, "onAlertStateChangedUpdate:state=" + alert_state);
			if (mAlertState != alert_state) {
				mAlertState = alert_state;
				updateAlertState(alert_state);
			}
		}
	};

}
