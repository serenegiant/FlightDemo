package com.serenegiant.arflight;


import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneFloodControlStateFloodControlChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneMediaRecordEventPictureEventChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneMediaRecordStatePictureStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingSettingsStateMaxAltitudeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingSettingsStateMaxTiltChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingStateAlertStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingStateAutoTakeOffModeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingStateFlatTrimChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDronePilotingStateFlyingStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSettingsStateCutOutModeChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSettingsStateProductInertialVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSettingsStateProductMotorsVersionChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSpeedSettingsStateMaxRotationSpeedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneSpeedSettingsStateWheelsChangedListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;


public class DeviceControllerMiniDrone extends DeviceController {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static String TAG = "DeviceControllerMiniDrone";


	public DeviceControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigMiniDrone());
	}

//================================================================================
// 機体からの状態・データコールバック関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		super.registerARCommandsListener();
		ARCommand.setMiniDronePilotingStateFlatTrimChangedListener(mPilotingStateFlatTrimChangedListener);
		ARCommand.setMiniDronePilotingStateFlyingStateChangedListener(mPilotingStateFlyingStateChangedListener);
		ARCommand.setMiniDronePilotingStateAlertStateChangedListener(mPilotingStateAlertStateChangedListener);
		ARCommand.setMiniDronePilotingStateAutoTakeOffModeChangedListener(mPilotingStateAutoTakeOffModeChangedListener);
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedListener(mARCommandMiniDroneMediaRecordStatePictureStateChangedListener);
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedV2Listener(mARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener);
		ARCommand.setMiniDroneMediaRecordEventPictureEventChangedListener(mARCommandMiniDroneMediaRecordEventPictureEventChangedListener);
		ARCommand.setMiniDronePilotingSettingsStateMaxAltitudeChangedListener(mPilotingSettingsStateMaxAltitudeChangedListener);
		ARCommand.setMiniDronePilotingSettingsStateMaxTiltChangedListener(mPilotingSettingsStateMaxTiltChangedListener);
		ARCommand.setMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedListener(mSettingsStateMaxVerticalSpeedChangedListener);
		ARCommand.setMiniDroneSpeedSettingsStateMaxRotationSpeedChangedListener(mSpeedSettingsStateMaxRotationSpeedChangedListener);
		ARCommand.setMiniDroneSpeedSettingsStateWheelsChangedListener(mSpeedSettingsStateWheelsChangedListener);
		ARCommand.setMiniDroneSettingsStateProductMotorsVersionChangedListener(mSettingsStateProductMotorsVersionChangedListener);
		ARCommand.setMiniDroneSettingsStateProductInertialVersionChangedListener(mSettingsStateProductInertialVersionChangedListener);
		ARCommand.setMiniDroneSettingsStateCutOutModeChangedListener(mSettingsStateCutOutModeChangedListener);
		ARCommand.setMiniDroneFloodControlStateFloodControlChangedListener(mFloodControlStateFloodControlChangedListener);

	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setMiniDronePilotingStateFlatTrimChangedListener(null);
		ARCommand.setMiniDronePilotingStateFlyingStateChangedListener(null);
		ARCommand.setMiniDronePilotingStateAlertStateChangedListener(null);
		ARCommand.setMiniDronePilotingStateAutoTakeOffModeChangedListener(null);
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedListener(null);
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedV2Listener(null);
		ARCommand.setMiniDroneMediaRecordEventPictureEventChangedListener(null);
		ARCommand.setMiniDronePilotingSettingsStateMaxAltitudeChangedListener(null);
		ARCommand.setMiniDronePilotingSettingsStateMaxTiltChangedListener(null);
		ARCommand.setMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedListener(null);
		ARCommand.setMiniDroneSpeedSettingsStateMaxRotationSpeedChangedListener(null);
		ARCommand.setMiniDroneSpeedSettingsStateWheelsChangedListener(null);
		ARCommand.setMiniDroneSettingsStateProductMotorsVersionChangedListener(null);
		ARCommand.setMiniDroneSettingsStateProductInertialVersionChangedListener(null);
		ARCommand.setMiniDroneSettingsStateCutOutModeChangedListener(null);
		ARCommand.setMiniDroneFloodControlStateFloodControlChangedListener(null);
		super.unregisterARCommandsListener();
	}

	/**
	 * フラットトリムが変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateFlatTrimChangedListener
		mPilotingStateFlatTrimChangedListener
			= new ARCommandMiniDronePilotingStateFlatTrimChangedListener() {
		@Override
		public void onMiniDronePilotingStateFlatTrimChangedUpdate() {
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateFlatTrimChangedUpdate:");
		}
	};

	/**
	 * 飛行状態が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateFlyingStateChangedListener
		mPilotingStateFlyingStateChangedListener
		= new ARCommandMiniDronePilotingStateFlyingStateChangedListener() {
		@Override
		public void onMiniDronePilotingStateFlyingStateChangedUpdate(
			ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateFlyingStateChangedUpdate:");
			setFlyingState(state.getValue());
			callOnFlyingStateChangedUpdate(getState());
		}
	};

	/**
	 * 自動離陸モードが変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateAutoTakeOffModeChangedListener
		mPilotingStateAutoTakeOffModeChangedListener
			= new ARCommandMiniDronePilotingStateAutoTakeOffModeChangedListener() {
		/**
		 * @param state State of automatic take off mode
		 */
		@Override
		public void onMiniDronePilotingStateAutoTakeOffModeChangedUpdate(final byte state) {
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateAutoTakeOffModeChangedUpdate:");
			if (mAutoTakeOffMode != state) {
				mAutoTakeOffMode = state;
			}
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordStatePictureStateChangedListener
		mARCommandMiniDroneMediaRecordStatePictureStateChangedListener
			= new ARCommandMiniDroneMediaRecordStatePictureStateChangedListener() {
		/**
		 * @param state 1 if picture has been taken, 0 otherwise
		 * @param mass_storage_id Mass storage id to record
		 */
		@Override
		public void onMiniDroneMediaRecordStatePictureStateChangedUpdate(
			final byte state, final byte mass_storage_id) {
			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordStatePictureStateChangedUpdate:");
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener
		mARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener
			= new ARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener() {
		@Override
		public void onMiniDroneMediaRecordStatePictureStateChangedV2Update(
			final ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state,
			final ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordStatePictureStateChangedV2Update:state=" + state + ",error=" + error);
		}
	};

	/**
	 * 写真撮影状態が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordEventPictureEventChangedListener
		mARCommandMiniDroneMediaRecordEventPictureEventChangedListener
			= new ARCommandMiniDroneMediaRecordEventPictureEventChangedListener() {
		@Override
		public void onMiniDroneMediaRecordEventPictureEventChangedUpdate(
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event,
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordEventPictureEventChangedUpdate:event=" + event + ",error=" + error);
		}
	};

	/**
	 * 最大高度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingSettingsStateMaxAltitudeChangedListener
		mPilotingSettingsStateMaxAltitudeChangedListener
			= new ARCommandMiniDronePilotingSettingsStateMaxAltitudeChangedListener() {
		/**
		 * @param current Current altitude max
		 * @param min Range min of altitude
		 * @param max Range max of altitude
		 */
		@Override
		public void onMiniDronePilotingSettingsStateMaxAltitudeChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxAltitude.current != current)
				|| (mMaxAltitude.min != min)
				|| (mMaxAltitude.max != max)) {

				mMaxAltitude.current = current;
				mMaxAltitude.min = min;
				mMaxAltitude.max = max;
			}
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxAltitudeChangedUpdate:" + mMaxAltitude);
		}
	};

	/**
	 * 最大傾斜設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingSettingsStateMaxTiltChangedListener
		mPilotingSettingsStateMaxTiltChangedListener
			= new ARCommandMiniDronePilotingSettingsStateMaxTiltChangedListener() {
		/**
		 * @param current Current max tilt
		 * @param min Range min of tilt
		 * @param max Range max of tilt
		 */
		@Override
		public void onMiniDronePilotingSettingsStateMaxTiltChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxTilt.current != current)
				|| (mMaxTilt.min != min)
				|| (mMaxTilt.max != max)) {

				mMaxTilt.current = current;
				mMaxTilt.min = min;
				mMaxTilt.max = max;
			}
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxTiltChangedUpdate:" + mMaxTilt);
		}
	};

	/**
	 * 上昇/降下速度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedListener
		mSettingsStateMaxVerticalSpeedChangedListener
			= new ARCommandMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedListener() {
		/**
		 * @param current Current max vertical speed in m/s
		 * @param min Range min of vertical speed
		 * @param max Range max of vertical speed
		 */
		@Override
		public void onMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxVerticalSpeed.current != current)
				|| (mMaxVerticalSpeed.min != min)
				|| (mMaxVerticalSpeed.max != max)) {

				mMaxVerticalSpeed.current = current;
				mMaxVerticalSpeed.min = min;
				mMaxVerticalSpeed.max = max;
			}
			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedUpdate:" + mMaxVerticalSpeed);
		}
	};

	/**
	 * 最大回転速度設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneSpeedSettingsStateMaxRotationSpeedChangedListener
		mSpeedSettingsStateMaxRotationSpeedChangedListener
			= new ARCommandMiniDroneSpeedSettingsStateMaxRotationSpeedChangedListener() {
		/**
		 * @param current Current max rotation speed in degree/s
		 * @param min Range min of rotation speed
		 * @param max Range max of rotation speed
		 */
		@Override
		public void onMiniDroneSpeedSettingsStateMaxRotationSpeedChangedUpdate(
			final float current, final float min, final float max) {
			if ((mMaxRotationSpeed.current != current)
				|| (mMaxRotationSpeed.min != min)
				|| (mMaxRotationSpeed.max != max)) {
				mMaxRotationSpeed.current = current;
				mMaxRotationSpeed.min = min;
				mMaxRotationSpeed.max = max;
			}
			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxRotationSpeedChangedUpdate:" + mMaxRotationSpeed);
		}
	};

	/**
	 * ホイールの有無設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneSpeedSettingsStateWheelsChangedListener
		mSpeedSettingsStateWheelsChangedListener
			= new ARCommandMiniDroneSpeedSettingsStateWheelsChangedListener() {
		/**
		 * @param present 1 if present, 0 if not present
		 */
		@Override
		public void onMiniDroneSpeedSettingsStateWheelsChangedUpdate(final byte present) {
			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateWheelsChangedUpdate:");
			if (mHasWheel != present) {
				mHasWheel = present;
			}
		}
	};

	private static final int MOTOR_NUMS = 4;
	private final AttributeMotor[] mMotors = new AttributeMotor[MOTOR_NUMS];
	/**
	 * モーターバージョンが変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneSettingsStateProductMotorsVersionChangedListener
		mSettingsStateProductMotorsVersionChangedListener
			= new ARCommandMiniDroneSettingsStateProductMotorsVersionChangedListener() {
		/**
		 * @param motor Product Motor number [1 - 4]
		 * @param type Product Motor type
		 * @param software Product Motors software version
		 * @param hardware Product Motors hardware version
		 */
		@Override
		public void onMiniDroneSettingsStateProductMotorsVersionChangedUpdate(
			final byte motor, final String type, final String software, final String hardware) {
			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateProductMotorsVersionChangedUpdate:");
			if (mMotors[0] == null) {
				for (int i = 0; i < MOTOR_NUMS; i++) {
					mMotors[i] = new AttributeMotor();
				}
			}
			try {
				final int ix = (motor - 1) % MOTOR_NUMS;
				mMotors[ix].type = type;
				mMotors[ix].software = software;
				mMotors[ix].hardware = hardware;
			} catch (Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	public final AttributeIMU mIMU = new AttributeIMU();
	/**
	 * フライトコントローラのバージョン
	 */
	private final ARCommandMiniDroneSettingsStateProductInertialVersionChangedListener
		mSettingsStateProductInertialVersionChangedListener
			= new ARCommandMiniDroneSettingsStateProductInertialVersionChangedListener() {
		/**
		 * @param software Product Inertial software version
		 * @param hardware Product Inertial hardware version
		 */
		@Override
		public void onMiniDroneSettingsStateProductInertialVersionChangedUpdate(
			final String software, final String hardware) {

			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateProductInertialVersionChangedUpdate:");
			mIMU.software = software;
			mIMU.hardware = hardware;
		}
	};

	/**
	 * カットオフモード設定が変更された時のコールバックリスナー
	 */
	private final ARCommandMiniDroneSettingsStateCutOutModeChangedListener
		mSettingsStateCutOutModeChangedListener
			= new ARCommandMiniDroneSettingsStateCutOutModeChangedListener() {
		/**
		 * @param enable State of cut out mode (1 if is activate, 0 otherwise)
		 */
		@Override
		public void onMiniDroneSettingsStateCutOutModeChangedUpdate(final byte enable) {
			if (DEBUG) Log.v(TAG, "onMiniDroneSettingsStateCutOutModeChangedUpdate:");
			if (mCutOffMode != enable) {
				mCutOffMode = enable;
				// XXX
			}
		}
	};

	/**
	 * FloodControl設定が変更された時のコールバックリスナー
	 * 操縦コマンド(PCMD)を連続して送る時の最短間隔・・・これ以下の間隔で送った時に無視するってことかな?
	 */
	private final ARCommandMiniDroneFloodControlStateFloodControlChangedListener
		mFloodControlStateFloodControlChangedListener
			= new ARCommandMiniDroneFloodControlStateFloodControlChangedListener() {
		/**
		 * @param delay Delay (in ms) between two PCMD
		 */
		@Override
		public void onMiniDroneFloodControlStateFloodControlChangedUpdate(final short delay) {
			if (DEBUG) Log.v(TAG, "onMiniDroneFloodControlStateFloodControlChangedUpdate:");
		}
	};

	/**
	 * 機体からの異常通知時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateAlertStateChangedListener
		mPilotingStateAlertStateChangedListener
			= new ARCommandMiniDronePilotingStateAlertStateChangedListener() {
		@Override
		public void onMiniDronePilotingStateAlertStateChangedUpdate(
			ARCOMMANDS_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM state) {

			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateAlertStateChangedUpdate:");
			callOnAlarmStateChangedUpdate(state.getValue());
		}
	};

//================================================================================
// コマンド送信関係
//================================================================================
	/**
	 * 操縦コマンドを送信
	 * @return
	 */
	@Override
	protected boolean sendPCMD(final byte flag, final byte roll, final byte pitch, final byte yaw, final byte gaz, final int psi) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;
		synchronized (mDataSync) {
			cmdError = cmd.setMiniDronePilotingPCMD(flag, roll, pitch, yaw, gaz, psi);
		}
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dNackId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send PCMD command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendTakeoff() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingTakeOff();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send TakeOff command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendLanding() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingLanding();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Landing command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendEmergency() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dEmergencyId(),
				cmd, ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendFlatTrim() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingFlatTrim();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flattrim command.");
		}

		return sentStatus;
	}

	/**
	 * 最大高度を設定
	 * @param altitude [m]
	 * @return
	 */
	@Override
	public boolean sendMaxAltitude(final float altitude) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingSettingsMaxAltitude(altitude);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(),
				cmd, ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxAltitude command.");
		}

		return sentStatus;
	}

	/**
	 * 最大傾斜設定
	 * @param tilt
	 * @return
	 */
	@Override
	public boolean sendMaxTilt(final float tilt) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingSettingsMaxTilt(tilt);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxTilt command.");
		}

		return sentStatus;
	}

	/**
	 * 最大上昇/下降速度を設定
	 * @param speed
	 * @return
	 */
	@Override
	public boolean sendMaxVerticalSpeed(final float speed) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSpeedSettingsMaxVerticalSpeed(speed);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxVerticalSpeed command.");
		}

		return sentStatus;
	}

	/**
	 * 最大回転速度
	 * @param speed [度/秒]
	 * @return
	 */
	@Override
	public boolean sendMaxRotationSpeed(final float speed) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSpeedSettingsMaxRotationSpeed(speed);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send MaxVerticalSpeed command.");
		}

		return sentStatus;
	}

	/**
	 * モーターの個数を返す
	 * @return
	 */
	@Override
	public int getMotorNums() {
		return 4;
	}

	private int mCutOffMode = -1;

	/**
	 * モーターの自動カット機能が有効かどうかを取得する
	 * @return
	 */
	public boolean isCutoffModeEnabled() {
		return mCutOffMode == 1;
	}

	/**
	 * モーターの自動カット機能のon/off
	 * @param enabled
	 * @return
	 */
	public boolean sendCutOutMode(final boolean enabled) {
		return sendCutOutMode((byte)(enabled ? 1: 0));
	}

	/**
	 * モーターの自動カット機能のon/off
	 * @param enabled
	 * @return
	 */
	public boolean sendCutOutMode(final byte enabled) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSettingsCutOutMode(enabled);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send CutOutMode command.");
		}

		return sentStatus;
	}

	private int mAutoTakeOffMode = -1;

	/**
	 * 自動離陸モードが有効かどうかを取得する
	 * @return
	 */
	public boolean isAutoTakeOffModeEnabled() {
		return mAutoTakeOffMode == 1;
	}

	public boolean sendAutoTakeOffMode(final boolean enable) {
		return sendAutoTakeOffMode((byte)(enable ? 1: 0));
	}
	/**
	 * 自動離陸モードの有効/無効を設定
	 * @param enable
	 * @return
	 */
	public boolean sendAutoTakeOffMode(final byte enable) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingAutoTakeOffMode(enable);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AutoTakeOffMode command.");
		}

		return sentStatus;
	}

	private int mHasWheel = -1;
	public boolean hasWheel() {
		return mHasWheel == 1;
	}

	public boolean sendWheel(final boolean has_wheel) {
		return sendWheel((byte)(has_wheel ? 1 : 0));
	}

	public boolean sendWheel(final byte has_wheel) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSpeedSettingsWheels(has_wheel);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Wheels command.");
		}

		return sentStatus;
	}

	/**
	 * 指定した方向にフリップ実行
	 * @param direction
	 * @return
	 */
	public boolean sendAnimationsFlip(final int direction) {

		ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM _dir;
		switch (direction) {
		case FLIP_FRONT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_FRONT;
			break;
		case FLIP_BACK:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_BACK;
			break;
		case FLIP_RIGHT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_RIGHT;
			break;
		case FLIP_LEFT:
			_dir = ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_LEFT;
			break;
		default:
			return false;
		}

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneAnimationsFlip(_dir);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}

	/**
	 * 自動で指定した角度回転させる
	 * @param degree -360〜360度
	 * @return
	 */
	public boolean sendAnimationsCap(final int degree) {

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneAnimationsCap(d);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}

}
