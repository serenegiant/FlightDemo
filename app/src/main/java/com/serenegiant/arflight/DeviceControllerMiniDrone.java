package com.serenegiant.arflight;


import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneFloodControlStateFloodControlChangedListener;
import com.parrot.arsdk.arcommands.ARCommandMiniDroneMediaRecordStatePictureStateChangedListener;
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
import com.parrot.arsdk.arnetwork.ARNETWORK_ERROR_ENUM;
import com.parrot.arsdk.arsal.ARSALPrint;


public class DeviceControllerMiniDrone extends DeviceController {
	private static final boolean DEBUG = false;
	private static String TAG = "DeviceControllerMiniDrone";


	public DeviceControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service);
	}

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
			callOnFlyingStateChangedUpdate(state.getValue());
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
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxAltitudeChangedUpdate:");
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
			if (DEBUG) Log.v(TAG, "onMiniDronePilotingSettingsStateMaxTiltChangedUpdate:");
		}
	};

	private float mVerticalSpeedCurrent = 0;
	private float mVerticalSpeedMin = 0;
	private float mVerticalSpeedMax = 0;
	/**
	 * 水平移動速度設定が変更された時のコールバックリスナー
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

			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxVerticalSpeedChangedUpdate:");
			if ((mVerticalSpeedCurrent != current)
				|| (mVerticalSpeedMin != min)
				|| (mVerticalSpeedMax != max)) {

				mVerticalSpeedCurrent = current;
				mVerticalSpeedMin = min;
				mVerticalSpeedMax = max;
				// XXX
			}
		}
	};

	private float mMaxRotationSpeedCurrent = 0;
	private float mMaxRotationSpeedMin = 0;
	private float mMaxRotationSpeedMax = 0;
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

			if (DEBUG) Log.v(TAG, "onMiniDroneSpeedSettingsStateMaxRotationSpeedChangedUpdate:");
			if ((mMaxRotationSpeedCurrent != current)
				|| (mMaxRotationSpeedMin != min)
				|| (mMaxRotationSpeedMax != max)) {
				mMaxRotationSpeedCurrent = current;
				mMaxRotationSpeedMin = min;
				mMaxRotationSpeedMax = max;
				// XXX
			}
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
		}
	};

	/**
	 * 製品・モーターバージョンが変更された時のコールバックリスナー
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
		}
	};

	/**
	 * 慣性バージョン?ってなんやろ?
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
		}
	};

	/**
	 * FloodControl設定が変更された時のコールバックリスナー
	 * 操縦コマンド(PCMD)を連続して送る時の最短間隔・・・これ以下の間隔で送った時に無視するってことかなぁ?
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
			callOnAlertStateChangedUpdate(state.getValue());
		}
	};

	/**
	 * 操縦コマンドを送信
	 * @return
	 */
	@Override
	protected boolean sendPCMD(final byte flag, final byte roll, final byte pitch, final byte yaw, final byte gaz, final float psi) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError;
		synchronized (mDataSync) {
			cmdError = cmd.setMiniDronePilotingPCMD(flag, roll, pitch, yaw, gaz, psi);
		}
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent in loop should be sent to a buffer not acknowledged ; here iobufferC2dNak
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dNak, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (!sentStatus) {
			ARSALPrint.e(TAG, "Failed to send PCMD command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendTakeoff() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingTakeOff();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (!sentStatus) {
			ARSALPrint.e(TAG, "Failed to send TakeOff command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendLanding() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingLanding();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send Landing command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendEmergency() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingEmergency();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The command emergency should be sent to its own buffer acknowledged  ; here iobufferC2dEmergency
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dEmergency, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send Emergency command.");
		}

		return sentStatus;
	}

	@Override
	public boolean sendFlatTrim() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();

		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setMiniDronePilotingFlatTrim();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flattrim command.");
		}

		return sentStatus;
	}

	/**
	 * ミニドローンをフリップ
	 * @param direction
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
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flip command.");
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
            /* Send data with ARNetwork */
			// The commands sent by event should be sent to an buffer acknowledged  ; here iobufferC2dAck
			ARNETWORK_ERROR_ENUM netError = mARNetManager.sendData(iobufferC2dAck, cmd, null, true);

			if (netError != ARNETWORK_ERROR_ENUM.ARNETWORK_OK) {
				ARSALPrint.e(TAG, "mARNetManager.sendData() failed. " + netError.toString());
				sentStatus = false;
			}

			cmd.dispose();
		}

		if (sentStatus == false) {
			ARSALPrint.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}
}
