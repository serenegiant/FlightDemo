package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.serenegiant.arflight.AutoFlightListener;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.FlightRecorder;
import com.serenegiant.arflight.IAutoFlight;
import com.serenegiant.arflight.ICameraController;
import com.serenegiant.arflight.IDeviceController;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.IVideoStreamController;
import com.serenegiant.arflight.ScriptFlight;
import com.serenegiant.arflight.TouchFlight;
import com.serenegiant.arflight.VideoStream;
import com.serenegiant.arflight.controllers.FlightControllerMiniDrone;
import com.serenegiant.arflight.controllers.FlightControllerMiniDroneNewAPI;
import com.serenegiant.dialog.SelectFileDialogFragment;
import com.serenegiant.drone.IVideoScreen;
import com.serenegiant.gameengine1.IModelView;
import com.serenegiant.gameengine1.IScreen;
import com.serenegiant.gamepad.GamePadConst;
import com.serenegiant.gamepad.Joystick;
import com.serenegiant.math.Vector;
import com.serenegiant.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.serenegiant.flightdemo.AppConst.*;

public abstract class BasePilotFragment extends ControlFragment implements SelectFileDialogFragment.OnFileSelectListener {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = BasePilotFragment.class.getSimpleName();

	static {
		FileUtils.DIR_NAME = "FlightDemo";
	}

	/** 飛行記録 */
	protected final FlightRecorder mFlightRecorder;
	/** スクリプト操縦 */
	protected final ScriptFlight mScriptFlight;
	protected boolean mScriptRunning;
	/** タッチ描画操縦 */
	protected final TouchFlight mTouchFlight;
	protected boolean mTouchMoveRunning;
	/** モデル表示 */
	protected IModelView mModelView;

	protected VideoStream mVideoStream;
	protected boolean mVideoRecording;

	public BasePilotFragment() {
		super();
		// デフォルトコンストラクタが必要
		mFlightRecorder = new FlightRecorder(mAutoFlightListener);
		mScriptFlight = new ScriptFlight(mAutoFlightListener);
		mTouchFlight = new TouchFlight(mAutoFlightListener);
	}

//	@Override
//	public void onAttach(Activity activity) {
//		super.onAttach(activity);
//		if (DEBUG) Log.v(TAG, "onAttach:");
//	}

	@Override
	public void onDetach() {
//		if (DEBUG) Log.v(TAG, "onDetach:");
		mJoystick = null;
		stopDeviceController(false);
		mFlightRecorder.release();
		mScriptFlight.release();
		super.onDetach();
	}

	// 操縦用
	protected int mOperationType;				// 操縦スティックのモード
	protected boolean mOperationTouch;		// タッチ描画で操縦モードかどうか
	protected double mMaxControlValue = DEFAULT_AUTOPILOT_MAX_CONTROL_VALUE;
	protected double mScaleX, mScaleY, mScaleZ, mScaleR;
	protected float mGamepadSensitivity = 1.0f;
	protected float mGamepadScaleX, mGamepadScaleY, mGamepadScaleZ, mGamepadScaleR;
	protected boolean mAutoHide;

	@Override
	public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final SharedPreferences pref = getActivity().getPreferences(0);
		mOperationTouch = pref.getBoolean(KEY_OPERATION_TOUCH, false);
		mOperationType = pref.getInt(KEY_OPERATION_TYPE, 0);
		mMaxControlValue = pref.getFloat(KEY_AUTOPILOT_MAX_CONTROL_VALUE, DEFAULT_AUTOPILOT_MAX_CONTROL_VALUE);
		mScaleX = pref.getFloat(KEY_AUTOPILOT_SCALE_X, DEFAULT_AUTOPILOT_SCALE_X);
		mScaleY = pref.getFloat(KEY_AUTOPILOT_SCALE_Y, DEFAULT_AUTOPILOT_SCALE_Y);
		mScaleZ = pref.getFloat(KEY_AUTOPILOT_SCALE_Z, DEFAULT_AUTOPILOT_SCALE_Z);
		mScaleR = pref.getFloat(KEY_AUTOPILOT_SCALE_R, DEFAULT_AUTOPILOT_SCALE_R);
		mGamepadSensitivity = pref.getFloat(KEY_GAMEPAD_SENSITIVITY, 1.0f);
		mGamepadScaleX = pref.getFloat(KEY_GAMEPAD_SCALE_X, 1.0f);
		mGamepadScaleY = pref.getFloat(KEY_GAMEPAD_SCALE_Y, 1.0f);
		mGamepadScaleZ = pref.getFloat(KEY_GAMEPAD_SCALE_Z, 1.0f);
		mGamepadScaleR = pref.getFloat(KEY_GAMEPAD_SCALE_R, 1.0f);
		mAutoHide = pref.getBoolean(KEY_AUTO_HIDE, false);
		int layout_id;
		if (mOperationTouch) {
			layout_id = R.layout.fragment_pilot_touch;
		} else {
			switch (mOperationType) {
			case 1:
				layout_id = R.layout.fragment_pilot_reverse;
				break;
			case 2:
				layout_id = R.layout.fragment_pilot_mode1;
				break;
			case 3:
				layout_id = R.layout.fragment_pilot_mode2;
				break;
//			case 0:
			default:
				layout_id = R.layout.fragment_pilot;
			}
		}

		return internalCreateView(inflater, container, savedInstanceState, layout_id);

	}

	@Override
	public void onResume() {
		super.onResume();
//		if (DEBUG) Log.v(TAG, "onResume:");
		final Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			mJoystick = ((MainActivity)activity).mJoystick;
		}
//		mControllerFrame.setKeepScreenOn(true);
		startDeviceController();
		startSensor();
		if (mModelView != null) {
			if ((mFlightController != null) && isStarted()) {
				mModelView.hasGuard(mFlightController.hasGuard());
			}
			mModelView.onResume();
		}
	}

	@Override
	public void onPause() {
//		if (DEBUG) Log.v(TAG, "onPause:");
		mJoystick = null;
		if (mModelView != null) {
			mModelView.onPause();
		}
		if (mController instanceof ICameraController) {
			((ICameraController)mController).sendVideoRecording(false);
		}
		stopVideoStreaming();
		stopRecord();
		stopPlay();
		stopScript();
		stopTouchMove();
		stopSensor();
		remove(mGamePadTask);
		remove(mUpdateStatusTask);
//		mControllerFrame.setKeepScreenOn(false);
		super.onPause();
	}

//	@Override
//	public void onDestroy() {
//		if (DEBUG) Log.v(TAG, "onDestroy:");
//		super.onDestroy();
//	}

	@Override
	public void onFileSelect(File[] files) {
//		if (DEBUG) Log.v(TAG, "onFileSelect:");
		if ((files != null) && (files.length > 0)
			&& !mFlightRecorder.isPlaying() && !mFlightRecorder.isRecording() ) {
			mFlightRecorder.load(files[0]);
			updateButtons();
		}
	}

	protected abstract View internalCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState, final int layout_id);

	@Override
	protected void startVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "startVideoStreaming:");
		if (mController instanceof IVideoStreamController) {
			if (mVideoStream == null) {
				mVideoStream = new VideoStream();
			}
			((IVideoStreamController)mController).setVideoStream(mVideoStream);
			post(new Runnable() {
				@Override
				public void run() {
					if (mVideoStream == null) {
						Log.w(TAG, "mVideoStreamが破棄されてる");
						return;
					}
					final IScreen screen = mModelView.getCurrentScreen();
					if (DEBUG) Log.v(TAG, "startVideoStreaming:screen=" + screen);
					if (screen instanceof IVideoScreen) {
						try {
							final SurfaceTexture surface = ((IVideoScreen) screen).getVideoTexture();
							if ((surface != null) && (mSurfaceId == 0)) {
								final Surface _surface = new Surface(surface);
								mSurfaceId = _surface.hashCode();
								mVideoStream.addSurface(mSurfaceId, _surface);
								((IVideoScreen) screen).setEnableVideo(true);
							}
						} catch (final Exception e) {
							Log.w(TAG, e);
						}
					} else {
						post(this, 300);
					}
				}
			}, 100);
		} else {
			Log.w(TAG, "IVideoStreamControllerじゃない");
		}
		super.startVideoStreaming();
	}

	@Override
	protected void stopVideoStreaming() {
//		if (DEBUG) Log.v(TAG, "stopVideoStreaming:");
		super.stopVideoStreaming();
		final IScreen screen = mModelView.getCurrentScreen();
		if (screen instanceof IVideoScreen) {
			((IVideoScreen) screen).setEnableVideo(false);
		}
		mSurfaceId = 0;
		if (mController instanceof IVideoStreamController) {
			((IVideoStreamController)mController).setVideoStream(null);
		}
		if (mVideoStream != null) {
			mVideoStream.release();
			mVideoStream = null;
		}
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
//		if (DEBUG) Log.v(TAG, "onConnect:");
		super.onConnect(controller);

		mVideoRecording = false;
		if (mModelView != null) {
			mModelView.hasGuard((controller instanceof IFlightController) && ((IFlightController)controller).hasGuard());
		}
		startGamePadTask();
		startUpdateStatusTask();
		updateButtons();
		// キャリブレーションが必要ならCalibrationFragmentへ遷移させる
		if ((controller instanceof IFlightController) && ((IFlightController)controller).needCalibration()) {
			replace(CalibrationFragment.newInstance(getDevice()));
		}
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
//		if (DEBUG) Log.v(TAG, "#onDisconnect");
		mVideoRecording = false;
		stopRecord();
		stopPlay();
		stopGamePadTask();
		remove(mUpdateStatusTask);
		requestPopBackStack(POP_BACK_STACK_DELAY);
		super.onDisconnect(controller);
	}

	/**
	 * 飛行ステータスが変化した時のコールバック
	 * @param state
	 */
	@Override
	protected void updateFlyingState(final int state) {
		updateButtons();
	}

	/**
	 * 異常ステータスが変化した時のコールバック
	 * @param alert_state
	 */
	@Override
	protected void updateAlarmState(final int alert_state) {
		runOnUiThread(mUpdateAlarmMessageTask);
		updateButtons();
	}

	/**
	 * バッテリー残量が変化した時のコールバック
	 */
	@Override
	protected void updateBattery(final IDeviceController controller) {
		runOnUiThread(mUpdateBatteryTask);
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバック
	 * @param picture_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updatePictureCaptureState(final int picture_state) {
		switch (picture_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_success, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		case DroneStatus.MEDIA_ERROR:
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(getActivity(), R.string.capture_error, Toast.LENGTH_SHORT).show();
				}
			});
			break;
		}
		updateButtons();
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバック
	 * @param video_state DroneStatus#MEDIA_XXX
	 */
	@Override
	protected void updateVideoRecordingState(final int video_state) {
		switch (video_state) {
		case DroneStatus.MEDIA_UNAVAILABLE:
		case DroneStatus.MEDIA_READY:
		case DroneStatus.MEDIA_BUSY:
			break;
		case DroneStatus.MEDIA_SUCCESS:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_success, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		case DroneStatus.MEDIA_ERROR:
			if (mVideoRecording) {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast.makeText(getActivity(), R.string.video_error, Toast.LENGTH_SHORT).show();
					}
				});
			}
			mVideoRecording = false;
			break;
		}
		updateButtons();
	}

	/**
	 * 移動停止
	 */
	@Override
	protected void stopMove() {
//		if (DEBUG) Log.v(TAG, "stopMove:");
		super.stopMove();
		if (mFlightController != null) {
			mFlightRecorder.record(FlightRecorder.CMD_UP_DOWN, 0);
			mFlightRecorder.record(FlightRecorder.CMD_TURN, 0);
			mFlightRecorder.record(FlightRecorder.CMD_FORWARD_BACK, 0);
			mFlightRecorder.record(FlightRecorder.CMD_RIGHT_LEFT, 0);
		}
	}

	/**
	 * 非常停止
	 */
	@Override
	protected void emergencyStop() {
		super.emergencyStop();	// stopMoveはこの中から呼び出されるので自前では呼ばない
		stopPlay();
		stopScript();
		stopTouchMove();
		stopAnimationActionAll();
	}

	private static final int[] SENSOR_TYPES = {
		Sensor.TYPE_MAGNETIC_FIELD,
		Sensor.TYPE_GRAVITY,
		Sensor.TYPE_ACCELEROMETER,
//		Sensor.TYPE_GYROSCOPE,
	};
	private SensorManager mSensorManager;
	private int mRotation;									// 画面の向き
	private final Object mSensorSync = new Object();		// センサー値同期用
	private final float[] mMagnetValues = new float[3];		// 磁気[μT]
	private final float[] mGravityValues = new float[3];	// 重力[m/s^2]
	private final float[] mAzimuthValues = new float[3];	// 方位[-180,+180][度]
//	private final float[] mAccelValues = new float[3];		// 加速度[m/s^2]
//	private final float[] mGyroValues = new float[3];		// ジャイロ[radian/s]

	/**
	 * 磁気センサー・加速度センサー等を読み取り開始
	 * (MiniDroneの時は何もしない)
	 */
	private void startSensor() {
		for (int i = 0; i < 3; i++) {
			mMagnetValues[i] = mGravityValues[i] = mAzimuthValues[i] = 0;
//			mAccelValues[i] = mGyroValues[i] = 0;
		}
		if ((mController instanceof FlightControllerMiniDrone)
			|| (mController instanceof FlightControllerMiniDroneNewAPI)) return;
		final Display display = getActivity().getWindowManager().getDefaultDisplay();
		mRotation = display.getRotation();
		// 重力センサーがあればそれを使う。なければ加速度センサーで代用する
		boolean hasGravity = false;
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
			for (final int sensor_type : SENSOR_TYPES) {
				final List<Sensor> sensors = mSensorManager.getSensorList(sensor_type);
				if ((sensors != null) && (sensors.size() > 0)) {
					if (sensor_type == Sensor.TYPE_GRAVITY)
						hasGravity = true;
					if (!hasGravity || (sensor_type != Sensor.TYPE_ACCELEROMETER)) {
						mSensorManager.registerListener(mSensorEventListener, sensors.get(0), SensorManager.SENSOR_DELAY_GAME);
					}
				}
			}
		}
	}

	/**
	 * 磁気センサー・加速度センサー等からの読み取り終了
	 */
	private void stopSensor() {
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorEventListener);
			mSensorManager = null;
		}
	}

	private final SensorEventListener mSensorEventListener = new SensorEventListener() {
		/**
		 * ハイパスフィルターを通して値をセットする
		 * @param values 値を保持するためのfloat配列
		 * @param new_values 新しい値を渡すためのfloat配列
		 * @param alpha フィルター定数(alpha=t/(t+dt)
		 */
		private void highPassFilter(final float[] values, final float[] new_values, final float alpha) {
			values[0] = alpha * values[0] + (1 - alpha) * new_values[0];
			values[1] = alpha * values[1] + (1 - alpha) * new_values[1];
			values[2] = alpha * values[2] + (1 - alpha) * new_values[2];
		}

		private final float[] outR = new float[16];
		private final float[] outR2 = new float[16];
		private void getOrientation(final float[] rotateMatrix, final float[] result) {

			switch (mRotation) {
			case Surface.ROTATION_0:
				SensorManager.getOrientation(rotateMatrix, result);
				return;
			case Surface.ROTATION_90:
				SensorManager.remapCoordinateSystem(
					rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_180:
				SensorManager.remapCoordinateSystem(
					rotateMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR2);
				SensorManager.remapCoordinateSystem(
					outR2, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			case Surface.ROTATION_270:
				SensorManager.remapCoordinateSystem(
					outR, SensorManager.AXIS_MINUS_Y, SensorManager.AXIS_MINUS_X, outR);
				break;
			}
			SensorManager.getOrientation(outR, result);
		}

		private static final float TO_DEGREE = (float)(180 / Math.PI);
		private final float[] mRotateMatrix = new float[16];			// 回転行列
		private final float[] mInclinationMatrix = new float[16];    	// 傾斜行列

		/**
		 * センサーの値が変化した時のコールバック
		 * @param event
		 */
		@Override
		public void onSensorChanged(final SensorEvent event) {
			final float[] values = event.values;
			final int type = event.sensor.getType();
			switch (type) {
			case Sensor.TYPE_MAGNETIC_FIELD:	// 磁気センサー
				synchronized (mSensorSync) {
					// ハイパスフィルターを通して取得
					// alpha=t/(t+dt), dt≒20msec@SENSOR_DELAY_GAME, tはローパスフィルタの時定数(t=80)
					highPassFilter(mMagnetValues, values, 0.8f);
//					System.arraycopy(values, 0, mMagnetValues, 0, 3);
					// 磁気センサーの値と重力センサーの値から
					SensorManager.getRotationMatrix(mRotateMatrix, mInclinationMatrix, mGravityValues, mMagnetValues);
					getOrientation(mRotateMatrix, mAzimuthValues);
					mAzimuthValues[0] *= TO_DEGREE;
					mAzimuthValues[1] *= TO_DEGREE;
					mAzimuthValues[2] *= TO_DEGREE;
				}
				break;
			case Sensor.TYPE_GRAVITY:			// 重力センサー
				synchronized (mSensorSync) {
					System.arraycopy(values, 0, mGravityValues, 0, 3);
				}
				break;
			case Sensor.TYPE_ACCELEROMETER:		// 加速度センサー
				synchronized (mSensorSync) {
//					System.arraycopy(values, 0, mAccelValues, 0, 3);
					System.arraycopy(values, 0, mGravityValues, 0, 3);	// 重力センサーが無い時は加速度センサーで代用
				}
				break;
//			case Sensor.TYPE_GYROSCOPE:			// ジャイロセンサー
//				synchronized (mSensorSync) {
//					System.arraycopy(values, 0, mGyroValues, 0, 3);
//				}
//				break;
			default:
//				if (DEBUG) Log.v(TAG, "onSensorChanged:" + String.format("その他%d(%f,%f,%f)", type, values[0], values[1], values[2]));
				break;
			}
		}

		/**
		 * センサーの精度が変更された時のコールバック
		 * @param sensor
		 * @param accuracy
		 */
		@Override
		public void onAccuracyChanged(final Sensor sensor, int accuracy) {
		}
	};


	/**
	 * 離陸指示
	 */
	protected void takeOff() {
		// 離陸指示
		if (mFlightController != null) {
			mFlightController.requestTakeoff();
			mFlightRecorder.record(FlightRecorder.CMD_TAKEOFF);
		}
	}

	/**
	 * 着陸指示
	 */
	protected void landing() {
		// 着陸指示
		stopMove();
		if (mFlightController != null) {
			mFlightController.requestLanding();
			mFlightRecorder.record(FlightRecorder.CMD_LANDING);
		}
	}

	/**
	 * 記録開始
	 * @param needClear 既存の記録を破棄してから記録開始するかどうか
	 */
	protected void startRecord(final boolean needClear) {
//		if (DEBUG) Log.v(TAG, "startRecord:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			if (needClear) {
				mFlightRecorder.clear();
			}
			mFlightRecorder.start();
			updateTime(0);
			updateButtons();
		}
	}

	/**
	 * 記録終了
	 */
	protected void stopRecord() {
//		if (DEBUG) Log.v(TAG, "stopRecord:");
		if (mFlightRecorder.isRecording()) {
			mFlightRecorder.stop();
			// ファイルへ保存
			final String path = FileUtils.getCaptureFile(getActivity(), "Documents", ".fcr", false).getAbsolutePath();
			if (!TextUtils.isEmpty(path)) {
				mFlightRecorder.save(path);
				updateButtons();
			}
			updateTime(-1);
		}
	}

	/**
	 * 再生開始
	 */
	protected void startPlay() {
//		if (DEBUG) Log.v(TAG, "startPlay:");
		if (!mScriptRunning && !mTouchMoveRunning && mFlightRecorder.isPrepared()) {
			try {
				mFlightRecorder.prepare();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * 再生終了
	 */
	protected void stopPlay() {
//		if (DEBUG) Log.v(TAG, "stopPlay:");
		if (mFlightRecorder.isPlaying()) {
			mFlightRecorder.stop();
			updateButtons();
		}
	}

	protected final List<ScriptHelper.ScriptRec> mScripts = new ArrayList<ScriptHelper.ScriptRec>();
	protected List<String> setupScript() {
		final List<String> result = new ArrayList<String>();

		final SharedPreferences pref = getActivity().getPreferences(0);
		try {
			ScriptHelper.loadScripts(pref, mScripts);
			final int n = mScripts.size();
			for (int i = 0; i < n; i++) {
				result.add(mScripts.get(i).name);
			}
/*			for (int i = 0; i < SIDE_MENU_ITEMS.length; i++) {
				result.add(getString(SIDE_MENU_ITEMS[i]));
			} */

		} catch (final IOException e) {
			Log.w(TAG, e);
		}
		return result;
	}

	/**
	 * スクリプト実行開始
	 */
	protected void startScript(final int index) {
//		if (DEBUG) Log.v(TAG, "startScript:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isRecording() && !mFlightRecorder.isPlaying()) {
			mScriptRunning = true;
			try {
				final int n = mScripts.size();
				if ((index >= 0) && (index < n)) {
					mScriptFlight.prepare(new FileInputStream(mScripts.get(index).path), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
				} else {
					throw new IOException("スクリプトファイルが見つからない(範囲外):" + index);
				}
/*				switch (index) {
				case 0:
					mScriptFlight.prepare(getResources().getAssets().open("circle_xy2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 1:
					mScriptFlight.prepare(getResources().getAssets().open("circle_xz.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 2:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_xr.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 3:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_yr.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 4:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_xr2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				case 5:
					mScriptFlight.prepare(getResources().getAssets().open("revolution_yr2.script"), mMaxControlValue, mScaleX, mScaleY, mScaleZ, mScaleR);
					break;
				default:
					throw new IOException("スクリプトファイルが見つからない(範囲外):" + index);
				} */
			} catch (final IOException e) {
				mScriptRunning = false;
				ScriptHelper.appendLog(getActivity(), e.getMessage());
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * スクリプト実行終了
 	 */
	protected void stopScript() {
//		if (DEBUG) Log.v(TAG, "stopScript:");
		mScriptRunning = false;
		if (mScriptFlight.isPlaying()) {
			mScriptFlight.stop();
			updateButtons();
		}
	}

	/**
	 * タッチ描画での操縦開始
	 */
	protected void startTouchMove() {
//		if (DEBUG) Log.v(TAG, "startTouchMove:");
		if (!mScriptRunning && !mTouchMoveRunning && !mFlightRecorder.isPlaying()) {
			mTouchMoveRunning = true;
			try {
				mTouchFlight.prepare((float)mMaxControlValue, (float)mScaleX, (float)mScaleY, (float)mScaleZ, (float)mScaleR);
			} catch (final Exception e) {
				mTouchMoveRunning = false;
				Log.w(TAG, e);
			}
			updateButtons();
		}
	}

	/**
	 * タッチ描画での操縦終了
	 */
	protected void stopTouchMove() {
//		if (DEBUG) Log.v(TAG, "stopTouchMove:");
		mTouchMoveRunning = false;
		if (mTouchFlight.isPlaying()) {
			mTouchFlight.stop();
			updateButtons();
		}
	}

	/**
	 * アニメーション動作開始指示
	 * @param anim
	 */
	protected void startAnimationAction(final int anim) {
		if (!mFlightRecorder.isPlaying()) {
			mFlightController.startAnimation(anim);
			updateButtons();
		}
	}

	/**
	 * アニメーション動作停止指示
	 * @param anim
	 */
	protected void stopAnimationAction(final int anim) {
		if (mFlightController != null) {
			mFlightController.stopAnimation(anim);
		}
	}

	/**
	 * アニメーション動作を全て停止
	 */
	protected void stopAnimationActionAll() {
		if (mFlightController != null) {
			mFlightController.stopAllAnimation();
		}
		updateButtons();
	}

	protected static final String asString(final int[] values) {
		final int n = values != null ? values.length : 0;
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			sb.append(values[i]).append(",");
		}
		return sb.toString();
	}

	/**
	 * 自動フライト実行時のコールバックリスナー
	 */
	private final AutoFlightListener mAutoFlightListener = new AutoFlightListener() {

		@Override
		public void onPrepared() {
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onPrepared:");
			if (mScriptRunning) {
				// スクリプト操縦
				if (mScriptFlight.isPrepared()) {
					mScriptFlight.play();
				} else {
					mScriptRunning = false;
				}
			} else if (mTouchMoveRunning) {
				// タッチ描画操縦
				if (mTouchFlight.isPrepared()) {
					mTouchFlight.play();
				} else {
					mTouchMoveRunning = false;
				}
			} else if (mFlightRecorder.isPrepared()) {
				// 飛行記録を再生
				mFlightRecorder.pos(0);
				mFlightRecorder.play();
			}

			updateButtons();
		}

		@Override
		public void onStart() {
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onStart:");
			updateTime(0);
			updateButtons();
		}

		@Override
		public boolean onStep(final int cmd, final int[] values, final long t) {
//			if (DEBUG) Log.v(TAG, String.format("mAutoFlightListener#onStep:cmd=%d,t=%d,v=[%s]" , cmd, t, asString(values)));
			updateTime(t);
			if (mFlightController != null) {
				switch (cmd) {
				case IAutoFlight.CMD_EMERGENCY:			// 非常停止
					mFlightController.requestEmergencyStop();
					updateButtons();
					break;
				case IAutoFlight.CMD_TAKEOFF:			// 離陸
					mFlightController.requestTakeoff();
					updateButtons();
					break;
				case IAutoFlight.CMD_LANDING:			// 着陸
					mFlightController.requestLanding();
					updateButtons();
					break;
				case IAutoFlight.CMD_UP_DOWN:			// 上昇:gaz>0, 下降: gaz<0
					mFlightController.setGaz(values[0]);
					break;
				case IAutoFlight.CMD_RIGHT_LEFT:		// 右: roll>0,flag=1 左: roll<0,flag=1
					mFlightController.setRoll(values[0]);
					mFlightController.setFlag((values[0] != 0 ? 1 : 0));
					break;
				case IAutoFlight.CMD_FORWARD_BACK:		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
					mFlightController.setPitch(values[0]);
					break;
				case IAutoFlight.CMD_TURN:				// 右回転: yaw>0, 左回転: ywa<0
					mFlightController.setYaw(values[0]);
					break;
				case IAutoFlight.CMD_COMPASS:			// 北磁極に対する角度 -360〜360度
					mFlightController.setHeading(values[0]);	// 実際は浮動小数点だけど
					break;
				case IAutoFlight.CMD_MOVE5:
					mFlightController.setMove(values[0], values[1], values[2], values[3], values[4]);
					break;
				case IAutoFlight.CMD_MOVE4:
					mFlightController.setMove(values[0], values[1], values[2], values[3]);
					break;
				case IAutoFlight.CMD_MOVE3:
					mFlightController.setMove(values[0], values[1], values[2]);
					break;
				case IAutoFlight.CMD_MOVE2:
					mFlightController.setMove(values[0], values[1]);
					break;
				case IAutoFlight.CMD_FLIP:				// フリップ
					mFlightController.requestAnimationsFlip(values[0]);
					break;
				case IAutoFlight.CMD_CAP:				// キャップ(指定角度水平回転)
					mFlightController.requestAnimationsCap(values[0]);
					break;
				}
				if (mTouchMoveRunning && mFlightRecorder.isRecording()) {
					mFlightRecorder.record(cmd, values);
				}
				return false;
			} else {
				return true;
			}
		}

		@Override
		public float getValues(int axis) {
			if (axis >= 0 && axis < 3) {
				final Vector attitude = new Vector(mFlightController.getAttitude());
				attitude.toDegree();
				switch (axis) {
				case 0:    // roll
					return attitude.x;
				case 1:    // pitch
					return attitude.y;
				case 2:    // yaw
					return attitude.z;
				}
			} else {
				switch (axis) {
				case 3:    // gaz
					return mFlightController.getAltitude();
				case 4:    // FIXME compass
					return 0;
				case 5:		// max_tilt
					return mFlightController.getMaxTilt().current();
				case 6:		// max_rotation_speed
					return mFlightController.getMaxRotationSpeed().current();
				case 7:		// max_vertical_speed
					return mFlightController.getMaxVerticalSpeed().current();

				}
			}
			return 0;
		}

		@Override
		public void onStop() {
//			if (DEBUG) Log.v(TAG, "mAutoFlightListener#onBeforeStop:");
			stopPlay();
			stopScript();
			stopTouchMove();
			BasePilotFragment.super.stopMove();
			updateTime(-1);
			updateButtons();
		}

		@Override
		public void onError(final Exception e) {
			stopPlay();
			stopScript();
			stopTouchMove();
			updateButtons();
			Log.w(TAG, e);
			ScriptHelper.appendLog(getActivity(), e.getMessage());
		}

	};

	protected void startUpdateStatusTask() {
		post(mUpdateStatusTask, 100);
	}

	// 機体姿勢と高度
	private float mCurrentRoll = 0;
	private float mCurrentPitch = 0;
	private float mCurrentYaw = 0;
	private float mCurrentAltitude = 0;
	/** 定期的にステータスをポーリングして処理するスレッドの実行部 */
	private final Runnable mUpdateStatusTask = new Runnable() {
		private final Vector mAttitude = new Vector();
		private int prevState;
		@Override
		public void run() {
			if (mFlightController != null) {
				if (mFlightController.canGetAttitude()) {
					// 機体姿勢を取得できる時
					mAttitude.set(mFlightController.getAttitude());
					mAttitude.toDegree();	// ラジアンを度に変換
					final float altitude = mFlightController.getAltitude();
					float yaw = mAzimuthValues[0] - mAttitude.z();
					if ((mCurrentRoll != mAttitude.x())
						|| (mCurrentPitch != mAttitude.y())
						|| (mCurrentYaw != yaw)
						|| (mCurrentAltitude != altitude)) {

							mCurrentRoll = mAttitude.x();
							mCurrentPitch = mAttitude.y();
							mCurrentYaw = yaw;
							mCurrentAltitude = altitude;
							if (mModelView != null) {
								mModelView.setAttitude(mCurrentRoll, mCurrentPitch, yaw, altitude);
							}
					}
				}
			}
			if (mController instanceof ICameraController) {
				// FIXME カメラの方向を更新する
			}
			if (prevState != getState()) {
				prevState = getState();
				updateButtons();
			}
			post(this, 50);	// 50ミリ秒=1秒間に最大で約20回更新
		}
	};

	/** アラート表示の更新処理をUIスレッドで実行するためのRunnable */
	private final Runnable mUpdateAlarmMessageTask = new Runnable() {
		@Override
		public void run() {
			updateAlarmMessageOnUIThread(getAlarm());
		}
	};

	protected abstract void updateAlarmMessageOnUIThread(final int alarm);
	protected abstract void updateBatteryOnUIThread(final int battery);
	/**
	 * バッテリー残量表示の更新処理をUIスレッドでするためのRunnable
	 */
	private final Runnable mUpdateBatteryTask = new Runnable() {
		@Override
		public void run() {
			updateBatteryOnUIThread(mFlightController != null ? mFlightController.getBattery() : -1);
		}
	};

	private volatile long lastCall = -1;
	private void updateTime(final long t) {
//		if (DEBUG) Log.v(TAG, "updateTime:" + t);
		mCurrentTime = t;
		lastCall = System.currentTimeMillis();
		runOnUiThread(mUpdateTimeTask);
	}

	private volatile long mCurrentTime;	// 現在の経過時間[ミリ秒]
	private final Runnable mIntervalUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			if (mCurrentTime >= 0) {
				runOnUiThread(mUpdateTimeTask);
			}
		}
	};

	/** 自動操縦中(スクリプト/再生)の時刻表示更新 */
	protected abstract void updateTimeOnUIThread(final int minutes, final int seconds);

	private final Runnable mUpdateTimeTask = new Runnable() {
		@Override
		public void run() {
			remove(mIntervalUpdateTimeTask);
			long t = mCurrentTime;
			if (t >= 0) {
				t +=  System.currentTimeMillis() - lastCall;
				final int m = (int)(t / 60000);
				final int s = (int)(t - m * 60000) / 1000;
				updateTimeOnUIThread(m, s);
				post(mIntervalUpdateTimeTask, 500);	// プライベートスレッド上で遅延実行
			}
		}
	};

	protected void setChildVisibility(final View view, final int visibility) {
		if (view != null) {
			view.setVisibility(visibility);
			view.setTag(R.id.anim_visibility, visibility);
		}
	}

	/**
	 * ボタン表示の更新(UIスレッドで処理)
	 */
	protected abstract void updateButtons();

	protected static final int DISABLE_COLOR = 0xcf777777;

	/** ゲームパッド読み取りスレッド操作用Handler */
	private Handler mGamePadHandler;
	/** ゲームパッド読み取りスレッド開始 */
	protected void startGamePadTask() {
		if (mGamePadHandler == null) {
			final HandlerThread thread = new HandlerThread("GamePadThread");
			thread.start();
			mGamePadHandler = new Handler(thread.getLooper());
		}
		mGamePadHandler.removeCallbacks(mGamePadTask);
		mGamePadHandler.postDelayed(mGamePadTask, 100);
	}

	/** ゲームパッド読み取りスレッド終了 */
	protected void stopGamePadTask() {
		if (mGamePadHandler != null) {
			final Handler handler = mGamePadHandler;
			mGamePadHandler = null;
			handler.removeCallbacks(mGamePadTask);
			handler.getLooper().quit();
		}
	}

	private static final long YAW_LIMIT = 200;
	private Joystick mJoystick;
	private final boolean[] downs = new boolean[GamePadConst.KEY_NUMS];
	private final long[] down_times = new long[GamePadConst.KEY_NUMS];
	private final int[] analogSticks = new int[4];
	boolean moved;
	/** ゲームパッド読み取りスレッドの実行部 */
	private final Runnable mGamePadTask = new Runnable() {
		private int mCurrentPan = Integer.MAX_VALUE, mCurrentTilt = Integer.MAX_VALUE;

		@Override
		public void run() {
			final Handler handler = mGamePadHandler;
			if (handler == null) return;	// 既に終了指示が出てる

			long interval = 50;
			handler.removeCallbacks(this);
			try {
				if (mJoystick != null) {
					mJoystick.updateState(downs, down_times, analogSticks, false);
				}

				// 左右の上端ボタン(手前側)を同時押しすると非常停止
				if (((downs[GamePadConst.KEY_RIGHT_RIGHT] || downs[GamePadConst.KEY_RIGHT_1]))
					&& (downs[GamePadConst.KEY_RIGHT_LEFT] || downs[GamePadConst.KEY_LEFT_1]) ) {
					emergencyStop();
					return;
				}

				// 飛行していない時にL2/R2同時押しするとフラットトリム実行
				if ((getState() == IFlightController.STATE_STARTED)
					&& (getAlarm() == DroneStatus.ALARM_NON)
					&& downs[GamePadConst.KEY_LEFT_2] && downs[GamePadConst.KEY_RIGHT_2]) {

					mFlightController.requestFlatTrim();
					return;
				}

				// L2押しながら左アナログスティックでカメラのpan/tilt
				if (downs[GamePadConst.KEY_LEFT_2] && (mController instanceof ICameraController)) {
					if (mCurrentPan > 100) {
						mCurrentPan = ((ICameraController)mController).getPan();
						mCurrentTilt = ((ICameraController)mController).getTilt();
					}
					final int pan = mCurrentPan;
					final int tilt = mCurrentTilt;
					// 左アナログスティックの左右=左右移動
					int p = pan + (int)(analogSticks[0] / 51.2f);
					if (p < -100) {
						p = -100;
					} else if (p > 100) {
						p = 100;
					}
					// 左アナログスティックの上下=前後移動
					int t = tilt + (int)(analogSticks[1] / 51.2f);
					if (t < -100) {
						t = -100;
					} else if (t > 100) {
						t = 100;
					}
//					if (DEBUG) Log.d(TAG, String.format("(%d,%d),pan=%d/%d,tilt=%d/%d", analogSticks[0], analogSticks[1], pan, p, tilt, t));
					if ((p != pan) || (t != tilt)) {
						((ICameraController)mController).sendCameraOrientation(t, p);
						mCurrentPan = p;
						mCurrentTilt = t;
						return;
					}
					interval = 20;
				} else {
					mCurrentPan = mCurrentTilt = Integer.MAX_VALUE;
				}

				// R2押しながら左スティックでフリップ
				if (downs[GamePadConst.KEY_RIGHT_2]) {
					if (downs[GamePadConst.KEY_LEFT_LEFT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_LEFT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_RIGHT]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_RIGHT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_UP]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_FRONT);
						return;
					} if (downs[GamePadConst.KEY_LEFT_DOWN]) {
						mFlightController.requestAnimationsFlip(IFlightController.FLIP_BACK);
						return;
					}
				}

				// 中央の右側ボタン[12]=着陸
				if (downs[GamePadConst.KEY_CENTER_RIGHT]) {
					landing();
					return;
				}
				// 中央の左側ボタン[11]=離陸
				if (downs[GamePadConst.KEY_CENTER_LEFT]) {
					takeOff();
					return;
				}
				// ここまでは共通操作の処理

				// 操作モード毎の処理
				switch (mOperationType) {
				case 0:	// 通常
					gamepad_normal();
					break;
				case 1:	// 左右反転
					gamepad_reverse();
					break;
				case 2:	// mode1
					gamepad_mode1();
					break;
				case 3:	// mode2
					gamepad_mode2();
					break;
				default:
					gamepad_normal();
					break;
				}
//				KeyGamePad.KEY_LEFT_CENTER:		// = 0;
//				KeyGamePad.KEY_LEFT_UP:			// = 1;
//				KeyGamePad.KEY_LEFT_RIGHT:		// = 2;
//				KeyGamePad.KEY_LEFT_DOWN:		// = 3;
//				KeyGamePad.KEY_LEFT_LEFT:		// = 4;
//				KeyGamePad.KEY_RIGHT_CENTER:	// = 5;
//				KeyGamePad.KEY_RIGHT_UP:		// = 6;
//				KeyGamePad.KEY_RIGHT_RIGHT:		// = 7;
//				KeyGamePad.KEY_RIGHT_DOWN:		// = 8;
//				KeyGamePad.KEY_RIGHT_LEFT:		// = 9;
//				KeyGamePad.KEY_LEFT_1:			// = 10;	// 左上前
//				KeyGamePad.KEY_LEFT_2:			// = 11;	// 左上後
//				KeyGamePad.KEY_CENTER_LEFT:		// = 12;	// 中央左
//				KeyGamePad.KEY_RIGHT_1:			// = 13;	// 右上前
//				KeyGamePad.KEY_RIGHT_2:			// = 14;	// 右上後
//				KeyGamePad.KEY_CENTER_RIGHT:	// = 15;	// 中央右
			} finally {
				handler.postDelayed(this, interval);
			}
		}
	};

	private static final float DEAD_ZONE = 2.0f;
	/**
	 * ゲームパッド操作時の実際の移動コマンド発行処理
	 * @param roll
	 * @param pitch
	 * @param gaz
	 * @param yaw
	 */
	protected void gamepad_move(final float roll, final float pitch, final float gaz, final float yaw) {
		final int r = (int)(Math.abs(roll) > DEAD_ZONE ? roll : 0.0f);
		final int p = (int)(Math.abs(pitch) > DEAD_ZONE ? pitch : 0.0f);
		final int g = (int)(Math.abs(gaz) > DEAD_ZONE ? gaz : 0.0f);
		final int y = (int)(Math.abs(yaw) > DEAD_ZONE ? yaw : 0.0f);
		if ((r != 0) || (p != 0) || (g != 0) || (y != 0)) {
//			if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", roll, pitch, gaz, yaw));
			if (mFlightController != null) {
				moved = true;
				mFlightController.setMove(r, p, g, y);
				mFlightRecorder.record(FlightRecorder.CMD_MOVE4, r, p, g, y);
			}
		} else if (moved) {
			if (mFlightController != null) {
				mFlightController.setMove(0, 0, 0, 0, 0);
				mFlightRecorder.record(FlightRecorder.CMD_MOVE4, 0, 0, 0, 0);
			}
			moved = false;
//			if (DEBUG) Log.v(TAG, String.format("move(%5.1f,%5.1f,%5.1f,%5.1f", 0f, 0f, 0f, 0f));
		}
	}

	/** 通常操作モードでのゲームパッド入力処理 */
	private void gamepad_normal() {
		// 右アナログスティックの左右=左右移動
		final float roll = analogSticks[2] * mGamepadSensitivity * mGamepadScaleX;
		// 右アナログスティックの上下=前後移動
		final float pitch = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleY;
		// 左アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleZ;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** 左右反転操作モードでのゲームパッド入力処理 */
	private void gamepad_reverse() {
		// 左アナログスティックの左右=左右移動
		final float roll = analogSticks[0] * mGamepadSensitivity * mGamepadScaleX;
		// 左アナログスティックの上下=前後移動
		final float pitch = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleY;
		// 右アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleZ;
		// 右アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[2])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** モード1でのゲームパッド入力処理 */
	private void gamepad_mode1() {
		// モード1
		// 右スティック: 左右=左右移動, 上下=上昇下降
		// 左スティック: 左右=左右回転, 上下=前後移動
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=前後移動
		final float pitch = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleY;
		// 右アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleZ;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	/** モード2でのゲームパッド入力処理 */
	private void gamepad_mode2() {
		// モード2
		// 右スティック: 左右=左右移動, 上下=前後移動
		// 左スティック: 左右=左右回転, 上下=上昇下降
		// 右アナログスティックの左右=左右移動
		final float roll = mGamepadSensitivity * mGamepadScaleX * analogSticks[2];
		// 左アナログスティックの上下=上昇下降
		final float gaz = -analogSticks[1] * mGamepadSensitivity * mGamepadScaleZ;
		// 右アナログスティックの上下=前後移動
		final float pitch = -analogSticks[3] * mGamepadSensitivity * mGamepadScaleY;
		// 左アナログスティックの左右または上端ボタン(手前,R1/L1)=左右回転
		final float yaw = mGamepadSensitivity * (
			downs[GamePadConst.KEY_RIGHT_1]
			? down_times[GamePadConst.KEY_RIGHT_1]
			: (
				downs[GamePadConst.KEY_LEFT_1]
				? -down_times[GamePadConst.KEY_LEFT_1]
				: analogSticks[0])
		);
		gamepad_move(roll, pitch, gaz, yaw);
	}

	private int mSurfaceId = 0;
	private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
		@Override
		public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureAvailable:");
			if ((mVideoStream != null) && (mSurfaceId == 0)) {
				final Surface _surface = new Surface(surface);
				mSurfaceId = _surface.hashCode();
				mVideoStream.addSurface(mSurfaceId, _surface);
			}
		}

		@Override
		public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureSizeChanged:");
		}

		@Override
		public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//			if (DEBUG) Log.v(TAG, "onSurfaceTextureDestroyed:");
			if (mVideoStream != null) {
				mVideoStream.removeSurface(mSurfaceId);
			}
			mSurfaceId = 0;
			return true;
		}

		@Override
		public void onSurfaceTextureUpdated(SurfaceTexture surface) {
		}
	};

}
