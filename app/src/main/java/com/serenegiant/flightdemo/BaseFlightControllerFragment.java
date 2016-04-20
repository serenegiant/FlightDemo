package com.serenegiant.flightdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.FlightControllerListener;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.SkyControllerListener;

public abstract class BaseFlightControllerFragment extends BaseControllerFragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private final String TAG = "BaseFlightControllerFragment:" + getClass().getSimpleName();

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
	}

	@Override
	protected void onBeforeCreateView() {
		if (mController instanceof IFlightController) {
			mFlightController = (IFlightController)mController;
		} else {
			mFlightController = null;
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
	protected synchronized void releaseDeviceController(final boolean disconnected) {
		if (DEBUG) Log.v(TAG, "releaseDeviceController:");
		mFlightController = null;
		super.releaseDeviceController(disconnected);
	}

	protected void startVideoStreaming() {
		if (DEBUG) Log.v(TAG, "startVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(true);
		}
	}

	protected void stopVideoStreaming() {
		if (DEBUG) Log.v(TAG, "stopVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).enableVideoStreaming(false);
		}
	}

	/**
	 * 移動停止
	 */
	protected void stopMove() {
		if (DEBUG) Log.v(TAG, "stopMove:");
		if (mController instanceof IFlightController) {
			((IFlightController)mController).setMove(0, 0, 0, 0, 0);
		}
	}

	/**
	 * 非常停止指示
	 */
	protected void emergencyStop() {
		stopMove();
		if (mController instanceof IFlightController) {
			((IFlightController)mController).requestEmergencyStop();
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onConnect:");
		stopMove();
		startVideoStreaming();
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		if (DEBUG) Log.v(TAG, "onDisconnect:");
		stopMove();
		stopVideoStreaming();
		super.onDisconnect(controller);
	}

	/**
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	protected abstract void updateFlyingState(final IDeviceController controller, final int state);

	/**
	 * キャリブレーションが必要かどうかが変化した時のコールバック
	 * @param need_calibration
	 */
	protected void updateCalibrationRequired(final IDeviceController controller, final boolean need_calibration) {
	}

	/**
	 * キャリブレーションを開始した
	 */
	protected void onStartCalibration(final IDeviceController controller) {
	}

	/**
	 * キャリブレーションが終了した
	 */
	protected void onStopCalibration(final IDeviceController controller) {
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param axis
	 */
	protected void updateCalibrationAxis(final IDeviceController controller, final int axis) {
	}

	/**
	 * キャリブレーションが必要かどうかが変化した時のコールバック
	 * @param need_calibration
	 */
	protected void updateSkyControllerCalibrationRequired(final IDeviceController controller, final boolean need_calibration) {
	}

	/**
	 * キャリブレーションを開始した
	 */
	protected void onStartSkyControllerCalibration(final IDeviceController controller) {
	}

	/**
	 * キャリブレーションが終了した
	 */
	protected void onStopSkyControllerCalibration(final IDeviceController controller) {
	}

	/**
	 * キャリブレーション中の軸が変更された
	 * @param controller
	 * @param axis
	 */
	protected void updateSkyControllerCalibrationAxis(final IDeviceController controller, final int axis) {
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param controller
	 * @param picture_state DroneStatus#MEDIA_XXX
	 */
	protected void updatePictureCaptureState(final IDeviceController controller, final int picture_state) {
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバック
	 * @param controller
	 * @param video_state DroneStatus#MEDIA_XXX
	 */
	protected void updateVideoRecordingState(final IDeviceController controller, final int video_state) {
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

	private final MyFlightControllerListener mFlightControllerListener = new MyFlightControllerListener();

	private final class MyFlightControllerListener implements FlightControllerListener, SkyControllerListener {
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
			updateBattery(controller, percent);
		}

		@Override
		public void onUpdateWiFiSignal(final IDeviceController controller, final int rssi) {
			updateWiFiSignal(controller, rssi);
		}

		@Override
		public void onAlarmStateChangedUpdate(final IDeviceController controller, int alarm_state) {
			if (DEBUG) Log.v(TAG, "mFlightControllerListener#onAlarmStateChangedUpdate:state=" + alarm_state);
			updateAlarmState(controller, alarm_state);
		}

		@Override
		public void onFlyingStateChangedUpdate(final IDeviceController controller, final int state) {
			updateFlyingState(controller, state);
		}

		@Override
		public void onFlatTrimChanged(final IDeviceController controller) {
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
		public void onCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
			updateCalibrationRequired(controller, need_calibration);
		}

		@Override
		public void onCalibrationStartStop(final IDeviceController controller, final boolean isStart) {
			if (isStart) {
				onStartCalibration(controller);
			} else {
				onStopCalibration(controller);
			}
		}

		@Override
		public void onCalibrationAxisChanged(final IDeviceController controller, final int axis) {
			updateCalibrationAxis(controller, axis);
		}

		@Override
		public void onStillCaptureStateChanged(final IDeviceController controller, final int state) {
			updatePictureCaptureState(controller, state);
		}

		@Override
		public void onVideoRecordingStateChanged(final IDeviceController controller, final int state) {
			updateVideoRecordingState(controller, state);
		}

		@Override
		public void onUpdateStorageState(final IDeviceController controller, final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
			updateStorageState(mass_storage_id, size, used_size, plugged, full, internal);
		}

		@Override
		public void onSkyControllerConnect(final IDeviceController controller) {
			BaseFlightControllerFragment.this.onSkyControllerConnect(controller);
		}

		@Override
		public void onSkyControllerDisconnect(final IDeviceController controller) {
			BaseFlightControllerFragment.this.onSkyControllerDisconnect(controller);
		}

		@Override
		public void onSkyControllerUpdateBattery(final IDeviceController controller, final int percent) {
			updateSkyControllerBattery(controller, percent);
		}

		@Override
		public void onSkyControllerAlarmStateChangedUpdate(final IDeviceController controller, final int alarm_state) {
			updateSkyControllerAlarmState(controller, alarm_state);
		}

		@Override
		public void onSkyControllerCalibrationRequiredChanged(final IDeviceController controller, final boolean need_calibration) {
			updateSkyControllerCalibrationRequired(controller, need_calibration);
		}

		@Override
		public void onSkyControllerCalibrationStartStop(final IDeviceController controller, final boolean isStart) {
			if (isStart) {
				onStartSkyControllerCalibration(controller);
			} else {
				onStopSkyControllerCalibration(controller);
			}
		}

		@Override
		public void onSkyControllerCalibrationAxisChanged(final IDeviceController controller, final int axis) {
			updateSkyControllerCalibrationAxis(controller, axis);
		}
	}

}
