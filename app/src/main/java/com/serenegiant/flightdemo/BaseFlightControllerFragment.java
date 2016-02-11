package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.FlightControllerListener;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.ManagerFragment;
import com.serenegiant.arflight.SkyController;

public abstract class BaseFlightControllerFragment extends BaseControllerFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = BaseFlightControllerFragment.class.getSimpleName();

	protected IFlightController mFlightController;

	public BaseFlightControllerFragment() {
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
		if (mController instanceof IFlightController) {
			mFlightController = (IFlightController)mController;
		}
	}

//	@Override
//	public void onSaveInstanceState(final Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (DEBUG) Log.v(TAG, "onSaveInstanceState:" + outState);
//	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if (DEBUG) Log.v(TAG, "onResume:");
		if (mFlightController != null) {
			mFlightController.addListener(mFlightControllerListener);
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

	protected boolean isFlying() {
		return mController instanceof IFlightController && ((IFlightController)mController).isFlying();
	}

	protected int getStillCaptureState() {
		return mController instanceof IFlightController ? ((IFlightController)mController).getStillCaptureState() : DroneStatus.MEDIA_UNAVAILABLE;
	}

	protected int getVideoRecordingState() {
		return mController instanceof IFlightController ? ((IFlightController)mController).getVideoRecordingState() : DroneStatus.MEDIA_UNAVAILABLE;
	}

	@Override
	protected synchronized boolean startDeviceController() {
		final boolean result = super.startDeviceController();
		if (DEBUG) Log.v(TAG, "startDeviceController:");
		if (mController instanceof IFlightController) {
			mFlightController = (IFlightController)mController;
		}
		return result;
	}

	@Override
	protected synchronized void stopDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "stopDeviceController:");
		mFlightController = null;
		super.stopDeviceController(disconnected);
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
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	protected void updateFlyingState(final int state) {
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

	private final FlightControllerListener mFlightControllerListener
		= new FlightControllerListener() {
		@Override
		public void onConnect(final IDeviceController controller) {
			BaseFlightControllerFragment.this.onConnect(controller);
		}

		@Override
		public void onDisconnect(final IDeviceController controller) {
			if (DEBUG) Log.v(TAG, "mFlightControllerListener#onDisconnect");
			BaseFlightControllerFragment.this.onDisconnect(controller);
		}

		@Override
		public void onUpdateBattery(final IDeviceController controller, final int percent) {
			updateBattery();
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, int alarm_state) {
			if (DEBUG) Log.v(TAG, "mFlightControllerListener#onAlarmStateChangedUpdate:state=" + alarm_state);
			updateAlarmState(alarm_state);
		}

		@Override
		public void onFlyingStateChangedUpdate(final int state) {
			updateFlyingState(state);
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

}
