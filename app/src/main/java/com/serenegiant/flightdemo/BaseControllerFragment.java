package com.serenegiant.flightdemo;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.ManagerFragment;

public abstract class BaseControllerFragment extends BaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = BaseControllerFragment.class.getSimpleName();

	protected static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";
	/** フラグメントに戻るまでの遅延時間[ミリ秒] */
	protected static final long POP_BACK_STACK_DELAY = 2000;

	private ARDiscoveryDeviceService mDevice;
	protected IDeviceController mController;

	public BaseControllerFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

//	@Override
//	public void onAttach(final Activity activity) {
//		super.onAttach(activity);
//		if (DEBUG) Log.v(TAG, "onAttach:");
//	}

	@Override
	public synchronized void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			mDevice = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			mController = ManagerFragment.getController(getActivity(), mDevice);
		}
		if (DEBUG) Log.v(TAG, "onCreate:savedInstanceState=" + savedInstanceState + ",mController=" + mController);
	}

//	@Override
//	public void onSaveInstanceState(final Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
//	}

	@Override
	public synchronized void onStart() {
		super.onStart();
		if (mController == null) {
			mController = ManagerFragment.getController(getActivity(), mDevice);
		}
		if (DEBUG) Log.v(TAG, "onStart:");
	}

//	@Override
//	public synchronized void onResume() {
//		super.onResume();
//		if (DEBUG) Log.v(TAG, "onResume:");
//	}

//	@Override
//	public synchronized void onPause() {
//		if (DEBUG) Log.v(TAG, "onPause:");
//		super.onPause();
//	}

//	@Override
//	public void onDestroy() {
//		if (DEBUG) Log.v(TAG, "onDestroy:");
//		super.onDestroy();
//	}

//	@Override
//	public void onDetach() {
//		if (DEBUG) Log.v(TAG, "onDetach:");
//		super.onDetach();
//	}

	protected Bundle setDevice(final ARDiscoveryDeviceService device) {
		if (DEBUG) Log.v(TAG, "setDevice:" + device);
		mDevice = device;
		Bundle args = getArguments();
		if (args == null) {
			args = new Bundle();
		}
		args.putParcelable(EXTRA_DEVICE_SERVICE, device);
		setArguments(args);
		return args;
	}

	protected ARDiscoveryDeviceService getDevice() {
		return mDevice;
	}

	protected ARDISCOVERY_PRODUCT_ENUM getProduct() {
		return mDevice != null ? ARDiscoveryService.getProductFromProductID(mDevice.getProductID()) : ARDISCOVERY_PRODUCT_ENUM.eARDISCOVERY_PRODUCT_UNKNOWN_ENUM_VALUE;
	}

	protected boolean isConnected() {
		return mController != null && mController.isConnected();
	}

	protected int getState() {
		return mController != null ? mController.getState() : IFlightController.STATE_STOPPED;
	}

	protected int getAlarm() {
		return mController != null ? mController.getAlarm() : DroneStatus.ALARM_DISCONNECTED;
	}

	protected synchronized boolean startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		boolean result = false;
		if (mController == null) {
			mController = ManagerFragment.getController(getActivity(), mDevice);
		}
		if (mController != null) {
			final int state = getState();
			if ((state != IFlightController.STATE_STARTED)
				&& (state != IFlightController.STATE_STARTING)) {
				if (DEBUG) Log.v(TAG, "未接続");
				updateBattery();

				final MainActivity activity = (MainActivity)getActivity();
				if (activity != null) {
					activity.showProgress(R.string.connecting, true, mOnCancelListener);
				}

				new Thread(new Runnable() {
					@Override
					public void run() {
						if (DEBUG) Log.v(TAG, "接続開始");
						final boolean failed = mController.start();
						if (activity != null) {
							activity.hideProgress();
						}

//						mIsConnected = !failed;
						if (failed) {
							if (DEBUG) Log.w(TAG, "DeviceControllerを開始できなかった");
							try {
								popBackStack();
							} catch (final Exception e) {
								Log.w(TAG, e);
							}
						}
					}
				}).start();
			} else {
				if (DEBUG) Log.v(TAG, "既にstartしている");
//				mController.requestAllSettings();
//				mController.requestAllStates();
				// sendAllSettingsとかsendAllStatesは接続した直後に1回しか有効じゃないのかも
//				updateBattery();
				result = true;
				onConnect(mController);
			}
		} else {
			Log.e(TAG, "controllerがnull!");
		}
		return result;
	}

	protected synchronized void stopDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		final int state = getState();
		final IDeviceController controller = mController;
		mController = null;
		if ((state == IFlightController.STATE_STARTED)
			|| (state == IFlightController.STATE_STARTING)) {

			final MainActivity activity = (MainActivity)getActivity();
			if (activity != null) {
				activity.showProgress(R.string.disconnecting, false, null);
			}

			new Thread(new Runnable() {
				@Override
				public void run() {
					if (DEBUG) Log.v(TAG, "接続終了中");
					controller.stop();
					if (activity != null) {
						ManagerFragment.releaseController(activity, controller);
						activity.hideProgress();
					}
					if (DEBUG) Log.v(TAG, "接続終了");
				}
			}).start();
		}
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	protected void updateBattery() {
	}

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param alert_state
	 */
	protected void updateAlarmState(final int alert_state) {
	}

	/**
	 * 接続された
	 * @param controller
	 */
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onConnect:");
	}

	/**
	 * 切断された
	 * @param controller
	 */
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		stopDeviceController(true);
	}

	/**
	 * アラーム状態が変化した時のコールバック
	 * @param alert_state
	 */
	protected void onAlarmStateChangedUpdate(int alert_state) {
	}

	private final DialogInterface.OnCancelListener mOnCancelListener
		= new DialogInterface.OnCancelListener() {
		@Override
		public void onCancel(final DialogInterface dialog) {
			if (DEBUG) Log.w(TAG, "ユーザーキャンセル");
			if (getState() == IFlightController.STATE_STARTING) {
				mController.cancelStart();
			}
		}
	};

}
