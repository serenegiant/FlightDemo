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
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.attribute.AttributeIMU;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.configs.ARNetworkConfigMiniDrone;

public class FlightControllerMiniDrone extends FlightController {
	private static final boolean DEBUG = false; // FIXME 実働時はfalseにすること
	private static String TAG = "FlightControllerMiniDrone";


	public FlightControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigMiniDrone());
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);
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
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedListener(mMediaRecordStatePictureStateChangedListener);
		ARCommand.setMiniDroneMediaRecordStatePictureStateChangedV2Listener(mMediaRecordStatePictureStateChangedV2Listener);
		ARCommand.setMiniDroneMediaRecordEventPictureEventChangedListener(mMediaRecordEventPictureEventChangedListener);
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
			callOnFlatTrimChanged();
		}
	};

	/**
	 * 飛行状態を受信した時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateFlyingStateChangedListener
		mPilotingStateFlyingStateChangedListener
		= new ARCommandMiniDronePilotingStateFlyingStateChangedListener() {
		@Override
		public void onMiniDronePilotingStateFlyingStateChangedUpdate(
			ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state) {
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateFlyingStateChangedUpdate:");
			mStatus.setFlyingState(state.getValue());
			callOnFlyingStateChangedUpdate(getState());
		}
	};

	/**
	 * 機体からの異常通知を受信した時のコールバックリスナー
	 */
	private final ARCommandMiniDronePilotingStateAlertStateChangedListener
		mPilotingStateAlertStateChangedListener
		= new ARCommandMiniDronePilotingStateAlertStateChangedListener() {
		@Override
		public void onMiniDronePilotingStateAlertStateChangedUpdate(
			ARCOMMANDS_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM state) {

			if (DEBUG) Log.v(TAG, "onMiniDronePilotingStateAlertStateChangedUpdate:");
			mStatus.setAlarm(state.getValue());
			callOnAlarmStateChangedUpdate(getAlarm());
		}
	};

	/**
	 * 自動離陸モードを受信した時のコールバックリスナー
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
			mSettings.setAutoTakeOffMode(state != 0);
		}
	};

	/**
	 * 写真撮影状態を受信した時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordStatePictureStateChangedListener
		mMediaRecordStatePictureStateChangedListener
			= new ARCommandMiniDroneMediaRecordStatePictureStateChangedListener() {
		/**
		 * @param state 1 if picture has been taken, 0 otherwise
		 * @param mass_storage_id Mass storage id to record
		 */
		@Override
		public void onMiniDroneMediaRecordStatePictureStateChangedUpdate(
			final byte state, final byte mass_storage_id) {

			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordStatePictureStateChangedUpdate:state=" + state + ",mass_storage_id=" + mass_storage_id);
//			callOnStillCaptureStateChanged(state == 1 ? MEDIA_SUCCESS : MEDIA_ERROR);
		}
	};

	/**
	 * 写真撮影状態を受信した時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener
		mMediaRecordStatePictureStateChangedV2Listener
			= new ARCommandMiniDroneMediaRecordStatePictureStateChangedV2Listener() {
		@Override
		public void onMiniDroneMediaRecordStatePictureStateChangedV2Update(
			final ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state,
			final ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordStatePictureStateChangedV2Update:state=" + state + ",error=" + error);

			int _state;
			switch (state) {
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_READY:			// 撮影可能
				_state = DroneStatus.MEDIA_READY;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_BUSY:			// 撮影中
				_state = DroneStatus.MEDIA_BUSY;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_NOTAVAILABLE:	// 撮影不可
			default:
				_state = DroneStatus.MEDIA_UNAVAILABLE;
				break;
			}
			callOnStillCaptureStateChanged(_state);
		}
	};

	/**
	 * 写真撮影イベントを受信した時のコールバックリスナー
	 */
	private final ARCommandMiniDroneMediaRecordEventPictureEventChangedListener
		mMediaRecordEventPictureEventChangedListener
			= new ARCommandMiniDroneMediaRecordEventPictureEventChangedListener() {
		@Override
		public void onMiniDroneMediaRecordEventPictureEventChangedUpdate(
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event,
			final ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error) {

			if (DEBUG) Log.v(TAG, "onMiniDroneMediaRecordEventPictureEventChangedUpdate:event=" + event + ",error=" + error);
			int _state;
			switch (event) {
			case ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_TAKEN:		// 撮影成功
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_FAILED:	// 撮影失敗
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnStillCaptureStateChanged(_state);
		}
	};

	/**
	 * 最大高度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxAltitude(current, min, max);
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxAltitudeChangedUpdate:");
		}
	};

	/**
	 * 最大傾斜設定を受信した時のコールバックリスナー
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
			mSettings.setMaxTilt(current, min, max);
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxTiltChangedUpdate:");
		}
	};

	/**
	 * 上昇/降下速度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxVerticalSpeed(current, min, max);
			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedUpdate:");
		}
	};

	/**
	 * 最大回転速度設定を受信した時のコールバックリスナー
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
			mSettings.setMaxRotationSpeed(current, min, max);
			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxRotationSpeedChangedUpdate:");
		}
	};

	/**
	 * ホイールの有無設定を受信した時のコールバックリスナー
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
			mSettings.setHasGuard(present != 0);
		}
	};

	/**
	 * モーターバージョンを受信した時のコールバックリスナー
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
			try {
				final int ix = (motor - 1) % getMotorNums();
				final AttributeMotor _motor = mStatus.getMotor(ix);
				if (_motor != null) {
					_motor.set(type, software, hardware);
				} else {
					Log.w(TAG, "モーターNo.が予期したのと違う:" + motor);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	public final AttributeIMU mIMU = new AttributeIMU();
	/**
	 * フライトコントローラのバージョンを受信した時
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
			mIMU.set(software, hardware);
		}
	};

	/**
	 * カットオフモード設定を受信した時のコールバックリスナー
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
			mSettings.setCutOffMode(enable != 0);
		}
	};

	/**
	 * FloodControl設定を受信した時のコールバックリスナー
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

//================================================================================
// コマンド送信関係
//================================================================================
	/**
	 * 操縦コマンドを送信
	 * @param flag flag to activate roll/pitch movement
	 * @param roll [-100,100]
	 * @param pitch [-100,100]
	 * @param yaw [-100,100]
	 * @param gaz [-100,100]
	 * @param heading [-180,180] (無効みたい)
	 * @return
	 */
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingPCMD((byte)flag, (byte)roll, (byte)pitch, (byte)yaw, (byte)gaz, heading);
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

	/**
	 * 非常停止指示
	 * @return
	 */
	@Override
	public boolean sendEmergency() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dEmergencyId(),
				cmd, ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	/**
	 * フラットトリム指示
	 * @return
	 */
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
	 * 離陸指示
	 * @return
	 */
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

	/**
	 * 着陸指示
	 * @return
	 */
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

	@Override
	public boolean canGetAttitude() {
		return true;
	}

	/**
	 * モーターの個数を返す
	 * @return
	 */
	@Override
	public int getMotorNums() {
		return 4;
	}
	/**
	 * モーターの自動カット機能のon/off
	 * @param enabled
	 * @return
	 */
	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSettingsCutOutMode((byte) (enabled ? 1 : 0));
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

	/**
	 * 自動離陸モードの有効/無効を設定
	 * @param enable
	 * @return
	 */
	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingAutoTakeOffMode((byte) (enable ? 1 : 0));
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

	@Override
	public boolean sendHasGuard(final boolean has_wheel) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneSpeedSettingsWheels((byte) (has_wheel ? 1 : 0));
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

	public boolean sendCalibration(final boolean start) {
		// Minidroneではとりあえずキャリブレーションを無効にしておく
		return false;
	}

	@Override
	public void setGaz(float gaz) {
		super.setGaz(gaz);
		updateAttitude();
	}

	@Override
	public void setHeading(float heading) {
		super.setHeading(heading);
		updateAttitude();
	}

	@Override
	public void setMove(float roll, float pitch) {
		super.setMove(roll, pitch);
		updateAttitude();
	}

	@Override
	public void setMove(float roll, float pitch, float gaz) {
		super.setMove(roll, pitch, gaz);
		updateAttitude();
	}

	@Override
	public void setMove(float roll, float pitch, float gaz, float yaw) {
		super.setMove(roll, pitch, gaz, yaw);
		updateAttitude();
	}

	@Override
	public void setMove(float roll, float pitch, float gaz, float yaw, int flag) {
		super.setMove(roll, pitch, gaz, yaw, flag);
		updateAttitude();
	}

	@Override
	public void setPitch(float pitch) {
		super.setPitch(pitch);
		updateAttitude();
	}

	@Override
	public void setPitch(float pitch, boolean move) {
		super.setPitch(pitch, move);
		updateAttitude();
	}

	@Override
	public void setRoll(float roll) {
		super.setRoll(roll);
		updateAttitude();
	}

	@Override
	public void setRoll(float roll, boolean move) {
		super.setRoll(roll, move);
		updateAttitude();
	}

	@Override
	public void setYaw(float yaw) {
		super.setYaw(yaw);
		updateAttitude();
	}

	private static final float TO_RADIAN = (float)(Math.PI / 180.0f);
	private final DataPCMD mAttitudePCMD = new DataPCMD();
	/**
	 * 機体姿勢を更新
	 */
	private void updateAttitude() {
		getPCMD(mAttitudePCMD);	// 現在の操縦コマンドを取得
		final float tilt_rate = mSettings.maxTilt().current() * 2;	// 傾き[度/100]
		final float roll = mAttitudePCMD.flag == 0 ? 0 : tilt_rate * mAttitudePCMD.roll / 100f * TO_RADIAN;
		final float pitch = mAttitudePCMD.flag == 0 ? 0 : -tilt_rate * mAttitudePCMD.pitch / 100f * TO_RADIAN;
		final float rot_rate = mSettings.maxRotationSpeed().current() / 5;	// 回転速度
		final float yaw = rot_rate * mAttitudePCMD.yaw / 100 * TO_RADIAN;
		mStatus.setAttitude(roll, pitch, yaw);
		// FIXME yaw軸は機体の向きを変えるだけじゃなくて実際に回転の計算をした方がいいかも
	}

	/**
	 * 静止画撮影要求
	 * @param mass_storage_id
	 * @return
	 */
	@Override
	public boolean sendTakePicture(final int mass_storage_id) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneMediaRecordPicture((byte) mass_storage_id);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send picture command.");
		}

		return sentStatus;
	}

	/**
	 * 静止画撮影要求
	 * @return
	 */
	@Override
	public boolean sendTakePicture() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDroneMediaRecordPictureV2();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_DATA_POP, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send pictureV2 command.");
		}

		return sentStatus;
	}

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @param mass_storage_id
	 * @return MiniDroneでは録画をサポートしていないので常にfalseを返す
	 */
	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		return false;
	}

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @return MiniDroneでは録画をサポートしていないので常にfalseを返す
	 */
	@Override
	public boolean sendVideoRecording(final boolean start) {
		return false;
	}

	/**
	 * 指定した方向にフリップ実行
	 * @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * @return
	 */
	@Override
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
	 * @param degree -180〜180度
	 * @return
	 */
	@Override
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
