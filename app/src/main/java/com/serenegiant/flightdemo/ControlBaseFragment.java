package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.FlightControllerListener;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.ManagerFragment;

public abstract class ControlBaseFragment extends BaseFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = "ControlFragment";

	protected static String EXTRA_DEVICE_SERVICE = "piloting.extra.device.service";
	/** フラグメントに戻るまでの遅延時間[ミリ秒] */
	protected static final long POP_BACK_STACK_DELAY = 2000;

	private ARDiscoveryDeviceService mDevice;
	protected IDeviceController mController;
	protected IFlightController mFlightController;

	public ControlBaseFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

/*	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach:");
	} */

/*	@Override
	public void onDetach() {
		if (DEBUG) Log.v(TAG, "onDetach:");
		super.onDetach();
	} */

	@Override
	public synchronized void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.v(TAG, "onCreate:" + savedInstanceState);
		if (savedInstanceState == null)
			savedInstanceState = getArguments();
		if (savedInstanceState != null) {
			mDevice = savedInstanceState.getParcelable(EXTRA_DEVICE_SERVICE);
			mController = ManagerFragment.getController(getActivity(), mDevice);
			if (mController instanceof IFlightController) {
				mFlightController = (IFlightController)mController;
			}
		}
		if (DEBUG) Log.v(TAG, "onCreate:savedInstanceState=" + savedInstanceState + ",mController=" + mController);
	}

/*	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
	} */

/*	@Override
	public void onDestroy() {
		if (DEBUG) Log.v(TAG, "onDestroy:");
		super.onDestroy();
	} */

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mController != null) {
			mController.addListener(mFlightControllerListener);
		}
	}

	@Override
	public synchronized void onPause() {
		if (DEBUG) Log.v(TAG, "onPause:");
		if (mController != null) {
			mController.removeListener(mFlightControllerListener);
		}
		super.onPause();
	}

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
		return mController instanceof IFlightController ? ((IFlightController)mController).getAlarm() : DroneStatus.ALARM_DISCONNECTED;
	}

	protected boolean isFlying() {
		return mController instanceof IFlightController && ((IFlightController)mController).isFlying();
	}

	protected int getStillCaptureState() {
		return mController instanceof IFlightController ? ((IFlightController)mController).getStillCaptureState() : DroneStatus.MEDIA_UNAVAILABLE;
	}

	protected int getVideoRecordingState() {
		return mController instanceof IFlightController ? ((IFlightController)mController).getVideoRecordingState() : DroneStatus.MEDIA_UNAVAILABLE;
	}

	protected synchronized boolean startDeviceController() {
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		boolean result = false;
		if (mController == null) {
			mController = ManagerFragment.getController(getActivity(), mDevice);
			if (mController instanceof IFlightController) {
				mFlightController = (IFlightController)mController;
			}
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
//				mController.sendAllSettings();
//				mController.sendAllStates();
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
		mFlightController = null;
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

	protected void startVideoStreaming() {
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(true);
		}
	}

	protected void stopVideoStreaming() {
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(false);
		}
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	protected void updateBattery() {
	}

	/**
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	protected void updateFlyingState(final int state) {
	}

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param alert_state
	 */
	protected void updateAlarmState(final int alert_state) {
	}

	/**
	 * キャリブレーションが必要かどうかが変化した時のコールバック
	 * @param need_calibration
	 */
	protected void updateCalibrationRequired(final boolean need_calibration) {
	}

	/**
	 * キャリブレーションを開始した
	 */
	protected void onStartCalibration() {
	}

	/**
	 * キャリブレーションが終了した
	 */
	protected void onStopCalibration() {
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis
	 */
	protected void updateCalibrationAxisChanged(final int axis) {
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param picture_state DroneStatus#MEDIA_XXX
	 */
	protected void updatePictureCaptureState(final int picture_state) {
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバック
	 * @param video_state DroneStatus#MEDIA_XXX
	 */
	protected void updateVideoRecordingState(final int video_state) {
	}

	/**
	 * 機体のストレージ状態が変化した時のコールバック
	 * @param mass_storage_id
	 * @param size [MB]
	 * @param used_size [MB]
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	protected void updateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
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

	private final FlightControllerListener mFlightControllerListener
		= new FlightControllerListener() {
		@Override
		public void onConnect(final IDeviceController controller) {
			ControlBaseFragment.this.onConnect(controller);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "mFlightControllerListener#onDisconnect");
			ControlBaseFragment.this.onDisconnect(controller);
		}

		@Override
		public void onUpdateBattery(final int percent) {
			updateBattery();
		}

		@Override
		public void onFlyingStateChangedUpdate(final int state) {
			updateFlyingState(state);
		}

		@Override
		public void onAlarmStateChangedUpdate(int alarm_state) {
			if (DEBUG) Log.v(TAG, "mFlightControllerListener#onAlarmStateChangedUpdate:state=" + alarm_state);
			updateAlarmState(alarm_state);
		}

		@Override
		public void onFlatTrimChanged() {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					final Activity activity = getActivity();
					if ((activity != null) && !activity.isFinishing()) {
						Toast.makeText(activity, R.string.action_flat_trim_finished, Toast.LENGTH_SHORT).show();
					}
				}
			});
		}

		@Override
		public void onCalibrationRequiredChanged(final boolean need_calibration) {
			updateCalibrationRequired(need_calibration);
		}

		@Override
		public void onCalibrationStartStop(final boolean isStart) {
			if (isStart) {
				onStartCalibration();
			} else {
				onStopCalibration();
			}
		}

		@Override
		public void onCalibrationAxisChanged(final int axis) {
			updateCalibrationAxisChanged(axis);
		}

		@Override
		public void onStillCaptureStateChanged(final int state) {
			updatePictureCaptureState(state);
		}

		@Override
		public void onVideoRecordingStateChanged(final int state) {
			updateVideoRecordingState(state);
		}

		@Override
		public void onUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
			updateStorageState(mass_storage_id, size, used_size, plugged, full, internal);
		}

	};

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
