package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceControllerListener;
import com.serenegiant.arflight.IDeviceController;

import java.sql.Date;

public abstract class ControlFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = ControlFragment.class.getSimpleName();

	protected static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";

	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private final long mUIThreadId = Looper.getMainLooper().getThread().getId();

	private ARDiscoveryDeviceService mService;
	protected IDeviceController deviceController;

	protected volatile int mFlyingState = 0;
	protected volatile int mAlertState = 0;
	protected volatile int mBattery = -1;
	protected boolean mIsFlying = false;	// FIXME mFlyingStateを参照するようにしてmIsFlyingフラグは削除する
	protected boolean mIsConnected = false;

	public ControlFragment() {
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			mService = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			deviceController = createDeviceController(mService);
			if (deviceController != null) {
				deviceController.setListener(mDeviceControllerListener);
			}
		}
	}

	/**
	 * IDeviceControllerインスタンスを生成する
	 * @param service
	 * @return
	 */
	protected abstract IDeviceController createDeviceController(final ARDiscoveryDeviceService service);

	protected Bundle setARService(final ARDiscoveryDeviceService service) {
		mService = service;
		final Bundle args = new Bundle();
		args.putParcelable(EXTRA_DEVICE_SERVICE, service);
		setArguments(args);
		return args;
	}

	protected void runOnUiThread(final Runnable task) {
		if (task != null) {
			try {
				if (mUIThreadId != Thread.currentThread().getId()) {
					mHandler.post(task);
				} else {
					task.run();
				}
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	}

	protected void startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		if ((deviceController != null) && !mIsConnected) {
			mBattery = -1;
			updateBattery(mBattery);

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
		}
	}

	protected void stopDeviceController() {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		if (deviceController != null) {
			final ProgressDialog dialog = new ProgressDialog(getActivity());
			dialog.setTitle(R.string.disconnecting);
			dialog.setIndeterminate(true);
			dialog.show();

			new Thread(new Runnable() {
				@Override
				public void run() {
					doStopDeviceController();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
						}
					});
				}
			}).start();
		}
	}

	protected void doStopDeviceController() {
		mIsConnected = mIsFlying = false;
		mFlyingState = mBattery = -1;
		deviceController.stop();

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

	protected void onDisconnect() {
		stopDeviceController();
	}

	protected void onAlertStateChangedUpdate(int alert_state) {
	}

	private final DeviceControllerListener mDeviceControllerListener
		= new DeviceControllerListener() {
		@Override
		public void onDisconnect() {
			ControlFragment.this.onDisconnect();
		}

		@Override
		public void onUpdateBattery(final byte percent) {
			if (mBattery != percent) {
				mBattery = percent;
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
