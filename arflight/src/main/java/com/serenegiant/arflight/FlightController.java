package com.serenegiant.arflight;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_GENERATOR_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCommand;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStartAnimationListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStateListListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStopAllAnimationsListener;
import com.parrot.arsdk.arcommands.ARCommandCommonAnimationsStopAnimationListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCameraSettingsStateCameraSettingsChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageInfoStateListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateMassStorageStateListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonCommonStateSensorsStatesListChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonControllerStateIsPilotingChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonDebugStatsEventSendPacketListener;
import com.parrot.arsdk.arcommands.ARCommandCommonHeadlightsIntensityListener;
import com.parrot.arsdk.arcommands.ARCommandCommonHeadlightsStateIntensityChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonOverHeatStateOverHeatChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonOverHeatStateOverHeatRegulationChangedListener;
import com.parrot.arsdk.arcommands.ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener;
import com.parrot.arsdk.ardiscovery.ARDiscoveryConnection;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.math.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class FlightController extends DeviceController implements IFlightController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = FlightController.class.getSimpleName();

	private static final int DEFAULT_VIDEO_FRAGMENT_SIZE = 1000;
	private static final int DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER = 128;

	protected int videoFragmentSize = DEFAULT_VIDEO_FRAGMENT_SIZE;
	protected int videoFragmentMaximumNumber = DEFAULT_VIDEO_FRAGMENT_MAXIMUM_NUMBER;
	protected int videoMaxAckInterval;

	private LooperThread mFlightCMDThread;


	private final Object mDataSync = new Object();
	private final DataPCMD mDataPCMD = new DataPCMD();

	private final List<FlightControllerListener> mListeners = new ArrayList<FlightControllerListener>();

	protected DroneSettings mSettings;

	public FlightController(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
	}

	@Override
	protected void internal_start() {
		if (mNetConfig.hasVideo()) {
			// ビデオストリーミング用スレッドを生成&開始
			startVideoThread();
		}
		// 操縦コマンド送信スレッドを生成&開始
		startFlightCMDThread();
	}

	@Override
	protected void internal_stop() {
		sendLanding();
		// 操縦コマンド送信スレッドを終了(終了するまで戻らない)
		stopFlightCMDThread();
		// ビデオストリーミングスレッドを終了(終了するまで戻らない)
		stopVideoThread();
	}

	@Override
	protected void setAlarm(final int alarm) {
		mStatus.setAlarm(alarm);
	}

	@Override
	public int getBattery() {
		return mStatus.getBattery();
	}

	protected void setBattery(final int percent) {
		mStatus.setBattery(percent);
	}

	@Override
	protected void setCountryCode(final String code) {
		super.setCountryCode(code);
		mSettings.setCountryCode(code);
	}

	protected void setAutomaticCountry(final boolean auto) {
		super.setAutomaticCountry(auto);
		mSettings.setAutomaticCountry(auto);
	}

	@Override
	protected void onReceiveJson(final JSONObject jsonObject, final String dataRx, final String ip) throws JSONException {
		if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY)) {
			videoFragmentSize = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_SIZE_KEY);
		}
        /* Else: leave it to the default value. */
		if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY)) {
			videoFragmentMaximumNumber = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_FRAGMENT_MAXIMUM_NUMBER_KEY);
		}
		/* Else: leave it to the default value. */
		if (!jsonObject.isNull(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY)) {
			videoMaxAckInterval = jsonObject.getInt(ARDiscoveryConnection.ARDISCOVERY_CONNECTION_JSON_ARSTREAM_MAX_ACK_INTERVAL_KEY);
		}
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return super.getState() + (((DroneStatus)mStatus).getFlyingState() << 8);
		}
	}

	public boolean isFlying() {
		return ((DroneStatus)mStatus).isFlying();
	}

	@Override
	public int getAlarm() {
		return mStatus.getAlarm();
	}

	@Override
	public int getStillCaptureState() {
		return ((DroneStatus)mStatus).getStillCaptureState();
	}

	@Override
	public int getVideoRecordingState() {
		return ((DroneStatus)mStatus).getVideoRecordingState();
	}

	@Override
	public int getMassStorageId() {
		return ((DroneStatus)mStatus).massStorageId();
	}

	@Override
	public String getMassStorageName() {
		return ((DroneStatus)mStatus).massStorageName();
	}

	@Override
	public boolean needCalibration() {
		return mStatus.needCalibration();
	}

	/** 操縦コマンド送信スレッドを生成&開始 */
	private void startFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "startFlightCMDThread");
		if (mFlightCMDThread != null) {
			mFlightCMDThread.stopThread();
		}
        /* Create the looper thread */
		mFlightCMDThread = new FlightCMDThread((mNetConfig.getPCMDLoopIntervalsMs()));

        /* Start the looper thread. */
		mFlightCMDThread.start();
	}

	/** 操縦コマンド送信を終了(終了するまで戻らない) */
	private void stopFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:");
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mFlightCMDThread) {
			mFlightCMDThread.stopThread();
			try {
				mFlightCMDThread.join();
				mFlightCMDThread = null;
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:終了");
	}

	/** 映像ストリーミングデータ受信スレッドを開始(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void startVideoThread() {
	}

	/** 映像ストリーミングデータ受信スレッドを終了(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void stopVideoThread() {
	}

//================================================================================
// 機体からのデータ/状態受信時の処理関係
//================================================================================
	/**
	 * コールバックを登録
	 */
	protected void registerARCommandsListener() {
		super.registerARCommandsListener();

		ARCommand.setCommonCommonStateMassStorageStateListChangedListener(mARCommandCommonCommonStateMassStorageStateListChangedListener);
		ARCommand.setCommonCommonStateMassStorageInfoStateListChangedListener(mARCommandCommonCommonStateMassStorageInfoStateListChangedListener);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(mARCommandCommonCommonStateSensorsStatesListChangedListener);
		ARCommand.setCommonCommonStateMassStorageInfoRemainingListChangedListener(mARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener);

		ARCommand.setCommonOverHeatStateOverHeatChangedListener(mARCommandCommonOverHeatStateOverHeatChangedListener);
		ARCommand.setCommonOverHeatStateOverHeatRegulationChangedListener(mARCommandCommonOverHeatStateOverHeatRegulationChangedListener);

		ARCommand.setCommonControllerStateIsPilotingChangedListener(mARCommandCommonControllerStateIsPilotingChangedListener);

		ARCommand.setCommonWifiSettingsStateOutdoorSettingsChangedListener(mARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener);

		ARCommand.setCommonMavlinkStateMavlinkFilePlayingStateChangedListener(mARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener);

		ARCommand.setCommonCalibrationStateMagnetoCalibrationStateChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationRequiredStateListener(mARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationStartedChangedListener(mARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener);

		ARCommand.setCommonCameraSettingsStateCameraSettingsChangedListener(mARCommandCommonCameraSettingsStateCameraSettingsChangedListener);

		ARCommand.setCommonDebugStatsEventSendPacketListener(mARCommandCommonDebugStatsEventSendPacketListener);

		// LED
		ARCommand.setCommonHeadlightsIntensityListener(mARCommandCommonHeadlightsIntensityListener);
		ARCommand.setCommonHeadlightsStateIntensityChangedListener(mARCommandCommonHeadlightsStateIntensityChangedListener);
		// アニメーション
		ARCommand.setCommonAnimationsStartAnimationListener(mARCommandCommonAnimationsStartAnimationListener);
		ARCommand.setCommonAnimationsStopAnimationListener(mARCommandCommonAnimationsStopAnimationListener);
		ARCommand.setCommonAnimationsStopAllAnimationsListener(mARCommandCommonAnimationsStopAllAnimationsListener);
		ARCommand.setCommonAnimationsStateListListener(mARCommandCommonAnimationsStateListListener);
	}

	/**
	 * コールバックを登録解除
	 */
	protected void unregisterARCommandsListener() {
		ARCommand.setCommonSettingsStateAllSettingsChangedListener(null);
		ARCommand.setCommonSettingsStateResetChangedListener(null);
		ARCommand.setCommonSettingsStateProductNameChangedListener(null);
		ARCommand.setCommonSettingsStateProductVersionChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialHighChangedListener(null);
		ARCommand.setCommonSettingsStateProductSerialLowChangedListener(null);
		ARCommand.setCommonSettingsStateCountryChangedListener(null);
		ARCommand.setCommonSettingsStateAutoCountryChangedListener(null);

		ARCommand.setCommonCommonStateAllStatesChangedListener(null);
		ARCommand.setCommonCommonStateBatteryStateChangedListener(null);

		ARCommand.setCommonCommonStateMassStorageStateListChangedListener(null);
		ARCommand.setCommonCommonStateMassStorageInfoStateListChangedListener(null);
		ARCommand.setCommonCommonStateCurrentDateChangedListener(null);
		ARCommand.setCommonCommonStateCurrentTimeChangedListener(null);
		ARCommand.setCommonCommonStateMassStorageInfoRemainingListChangedListener(null);
		ARCommand.setCommonCommonStateWifiSignalChangedListener(null);
		ARCommand.setCommonCommonStateSensorsStatesListChangedListener(null);

		ARCommand.setCommonOverHeatStateOverHeatChangedListener(null);
		ARCommand.setCommonOverHeatStateOverHeatRegulationChangedListener(null);

		ARCommand.setCommonControllerStateIsPilotingChangedListener(null);

		ARCommand.setCommonWifiSettingsStateOutdoorSettingsChangedListener(null);

		ARCommand.setCommonMavlinkStateMavlinkFilePlayingStateChangedListener(null);

		ARCommand.setCommonCalibrationStateMagnetoCalibrationStateChangedListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationRequiredStateListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener(null);
		ARCommand.setCommonCalibrationStateMagnetoCalibrationStartedChangedListener(null);

		ARCommand.setCommonCameraSettingsStateCameraSettingsChangedListener(null);

		ARCommand.setCommonDebugStatsEventSendPacketListener(null);

		// LED関係
		ARCommand.setCommonHeadlightsIntensityListener(null);
		ARCommand.setCommonHeadlightsStateIntensityChangedListener(null);
		// アニメーション
		ARCommand.setCommonAnimationsStartAnimationListener(null);
		ARCommand.setCommonAnimationsStopAnimationListener(null);
		ARCommand.setCommonAnimationsStopAllAnimationsListener(null);
		ARCommand.setCommonAnimationsStateListListener(null);
		super.unregisterARCommandsListener();
	}


	/**
	 * 接続されているストレージの一覧が変更された時
	 */
	private final ARCommandCommonCommonStateMassStorageStateListChangedListener
		mARCommandCommonCommonStateMassStorageStateListChangedListener
			= new ARCommandCommonCommonStateMassStorageStateListChangedListener() {
		/**
		 * @param mass_storage_id Mass storage id (unique)
		 * @param name Mass storage name
		 */
		@Override
		public void onCommonCommonStateMassStorageStateListChangedUpdate(
			final byte mass_storage_id, final String name) {

			if (DEBUG) Log.v(TAG, String.format("onCommonCommonStateMassStorageStateListChangedUpdate:mass_storage_id=%d,name=%s", mass_storage_id, name));
			((DroneStatus)mStatus).setMassStorage(mass_storage_id, name);
		}
	};

	/**
	 * ストレージの情報が変更された時
	 */
	private final ARCommandCommonCommonStateMassStorageInfoStateListChangedListener
		mARCommandCommonCommonStateMassStorageInfoStateListChangedListener
			= new ARCommandCommonCommonStateMassStorageInfoStateListChangedListener() {
		/**
		 * @param mass_storage_id Mass storage state id (unique)
		 * @param size Mass storage size in MBytes
		 * @param used_size Mass storage used size in MBytes
		 * @param plugged Mass storage plugged (1 if mass storage is plugged, otherwise 0)
		 * @param full Mass storage full information state (1 if mass storage full, 0 otherwise).
		 * @param internal Mass storage internal type state (1 if mass storage is internal, 0 otherwise)
		 */
		@Override
		public void onCommonCommonStateMassStorageInfoStateListChangedUpdate(
			final byte mass_storage_id, final int size, final int used_size, final byte plugged, final byte full, final byte internal) {

			if (DEBUG) Log.v(TAG, String.format("onCommonCommonStateMassStorageInfoStateListChangedUpdate:mass_storage_id=%d,size=%d,used_size=%d,plugged=%d,full=%d,internal=%d", mass_storage_id, size, used_size, plugged, full, internal));
			callOnUpdateStorageState(mass_storage_id, size, used_size, plugged != 0, full != 0, internal != 0);
		}
	};

	/**
	 * ストレージの空き容量が変化した時
	 */
	private final ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener
		mARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener
		= new ARCommandCommonCommonStateMassStorageInfoRemainingListChangedListener() {
		/**
		 * @param free_space Mass storage free space in MBytes
		 * @param rec_time Mass storage record time reamining in minute
		 * @param photo_remaining Mass storage photo remaining
		 */
		@Override
		public void onCommonCommonStateMassStorageInfoRemainingListChangedUpdate(
			final int free_space, final short rec_time, final int photo_remaining) {
			if (DEBUG) Log.v(TAG, String.format("free_space=%d,rec_time=%d,photo_remaining=%d", free_space, rec_time, photo_remaining));
			// FIXME 未実装
			// XXX
		}
	};

	/**
	 * センサー状態リストが変化した時のコールバックリスナー
	 */
	private final ARCommandCommonCommonStateSensorsStatesListChangedListener
		mARCommandCommonCommonStateSensorsStatesListChangedListener
			= new ARCommandCommonCommonStateSensorsStatesListChangedListener() {
		/**
		 * @param sensor_name Sensor name
		 * @param sensorState Sensor state (1 if the sensor is OK, 0 if the sensor is NOT OK)
		 */
		@Override
		public void onCommonCommonStateSensorsStatesListChangedUpdate(
			final ARCOMMANDS_COMMON_COMMONSTATE_SENSORSSTATESLISTCHANGED_SENSORNAME_ENUM sensor_name, final byte sensorState) {

			switch (sensor_name.getValue()) {
			case SENSOR_IMU: // 0
			case SENSOR_BAROMETER:	// 1
			case SENSOR_ULTRASOUND: // 2
			case SENSOR_GPS: // 3
			case SENSOR_MAGNETOMETER: // 4
			case SENSOR_VERTICAL_CAMERA: // 5
			}
			if (DEBUG) Log.v(TAG, String.format("SensorsStatesListChangedUpdate:%d=%d", sensor_name.getValue(), sensorState));
		}
	};

	/**
	 * オーバーヒート状態が変化した時
	 */
	private final ARCommandCommonOverHeatStateOverHeatChangedListener
		mARCommandCommonOverHeatStateOverHeatChangedListener
			= new ARCommandCommonOverHeatStateOverHeatChangedListener() {
		@Override
		public void onCommonOverHeatStateOverHeatChangedUpdate() {
			// XXX
		}
	};

	/**
	 * オーバーヒート時の冷却方法設定が変更された時
	 */
	private final ARCommandCommonOverHeatStateOverHeatRegulationChangedListener
		mARCommandCommonOverHeatStateOverHeatRegulationChangedListener
			= new ARCommandCommonOverHeatStateOverHeatRegulationChangedListener() {
		/**
		 * @param regulationType Type of overheat regulation : 0 for ventilation, 1 for switch off
		 */
		@Override
		public void onCommonOverHeatStateOverHeatRegulationChangedUpdate(final byte regulationType) {
			// XXX
		}
	};

	/**
	 * アプリがHUDモードで操縦しているかどうかが変更された
	 */
	private final ARCommandCommonControllerStateIsPilotingChangedListener
		mARCommandCommonControllerStateIsPilotingChangedListener
			= new ARCommandCommonControllerStateIsPilotingChangedListener() {
		/**
		 * @param piloting 0 when the application is not in the piloting HUD, 1 when it enters the HUD.
		 */
		@Override
		public void onCommonControllerStateIsPilotingChangedUpdate(final byte piloting) {
			// XXX
		}
	};

	/**
	 * WiFiの室内/室外モードが変更された時
	 */
	private final ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener
		mARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener
			= new ARCommandCommonWifiSettingsStateOutdoorSettingsChangedListener() {
		/**
		 * @param outdoor 1 if it should use outdoor wifi settings, 0 otherwise
		 */
		@Override
		public void onCommonWifiSettingsStateOutdoorSettingsChangedUpdate(final byte outdoor) {
			// XXX
		}
	};

	/**
	 * Mavlinkファイルの再生状態が変化した時
	 */
	private final ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener
		mARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener
			= new ARCommandCommonMavlinkStateMavlinkFilePlayingStateChangedListener() {
		/**
		 * @param state State of the mavlink
		 * @param filepath flight plan file path from the mavlink ftp root
		 * @param type type of the played mavlink file
		 */
		@Override
		public void onCommonMavlinkStateMavlinkFilePlayingStateChangedUpdate(
			final ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_STATE_ENUM state,
			final String filepath, final ARCOMMANDS_COMMON_MAVLINKSTATE_MAVLINKFILEPLAYINGSTATECHANGED_TYPE_ENUM type) {
			// XXX
		}
	};

	/**
	 * キャリブレーションの状態が変わった時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationStateChangedListener() {
		/**
		 * @param xAxisCalibration State of the x axis (roll) calibration : 1 if calibration is done, 0 otherwise
		 * @param yAxisCalibration State of the y axis (pitch) calibration : 1 if calibration is done, 0 otherwise
		 * @param zAxisCalibration State of the z axis (yaw) calibration : 1 if calibration is done, 0 otherwise
		 * @param calibrationFailed 1 if calibration has failed, 0 otherwise. If this arg is 1, consider all previous arg as 0
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationStateChangedUpdate(
			final byte xAxisCalibration, final byte yAxisCalibration, final byte zAxisCalibration, final byte calibrationFailed) {

			if (DEBUG) Log.v(TAG, String.format("CalibrationStateChangedUpdate:(%d/%d/%d)%d", xAxisCalibration, yAxisCalibration, zAxisCalibration, calibrationFailed));
			mStatus.updateCalibrationState(xAxisCalibration == 1, yAxisCalibration == 1, zAxisCalibration == 1, calibrationFailed == 1);
			callOnCalibrationRequiredChanged(calibrationFailed == 1);
		}
	};

	/**
	 * キャリブレーションが必要な時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener
		mARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationRequiredStateListener() {
		/**
		 * @param required 1 if calibration is required, 0 if current calibration is still valid
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationRequiredStateUpdate(final byte required) {
			if (DEBUG) Log.v(TAG, "CalibrationRequiredStateUpdate:" + required);

			mStatus.needCalibration(required != 0);
			callOnCalibrationRequiredChanged(required != 0);
		}
	};

	/**
	 * キャリブレーション中の軸が変更された時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedListener() {
		/**
		 * @param axis The axis to calibrate, 0:x, 1:y, 2:z, 3:none
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationAxisToCalibrateChangedUpdate(
			final ARCOMMANDS_COMMON_CALIBRATIONSTATE_MAGNETOCALIBRATIONAXISTOCALIBRATECHANGED_AXIS_ENUM axis) {

			if (DEBUG) Log.v(TAG, "CalibrateAxisChanged:" + axis.getValue());
			callOnCalibrationAxisChanged(axis.getValue());
		}
	};

	/**
	 * キャリブレーションを開始/終了した時の通知
	 */
	private final ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener
		mARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener
			= new ARCommandCommonCalibrationStateMagnetoCalibrationStartedChangedListener() {
		/**
		 * @param started 1 if calibration has started, 0 otherwise
		 */
		@Override
		public void onCommonCalibrationStateMagnetoCalibrationStartedChangedUpdate(final byte started) {

			callOnCalibrationStartStop(started == 1);
		}
	};

	/**
	 * カメラ設定を受け取った時
	 */
	private final ARCommandCommonCameraSettingsStateCameraSettingsChangedListener
		mARCommandCommonCameraSettingsStateCameraSettingsChangedListener
			= new ARCommandCommonCameraSettingsStateCameraSettingsChangedListener() {
		/**
		 * @param fov Value of the camera horizontal fov (in degree)
		 * @param panMax Value of max pan (right pan) (in degree)
		 * @param panMin Value of min pan (left pan) (in degree)
		 * @param tiltMax Value of max tilt (top tilt) (in degree)
		 * @param tiltMin Value of min tilt (bottom tilt) (in degree)
		 */
		@Override
		public void onCommonCameraSettingsStateCameraSettingsChangedUpdate(
			final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
			mSettings.setCameraSettings(fov, panMax, panMin, tiltMax, tiltMin);
		}
	};

	private final ARCommandCommonDebugStatsEventSendPacketListener
		mARCommandCommonDebugStatsEventSendPacketListener
			= new ARCommandCommonDebugStatsEventSendPacketListener() {
		/**
		 * @param packet packet from drone
		 */
		@Override
		public void onCommonDebugStatsEventSendPacketUpdate(final String packet) {
			// XXX
		}
	};

	/**
	 * LED強度の変更通知?
	 */
	private final ARCommandCommonHeadlightsIntensityListener
		mARCommandCommonHeadlightsIntensityListener
			= new ARCommandCommonHeadlightsIntensityListener() {
		@Override
		public void onCommonHeadlightsIntensityUpdate(final byte left, final byte right) {
			// FIXME 未実装
			if (DEBUG) Log.v(TAG, String.format("onCommonHeadlightsIntensityUpdate(%d,%d)", left, right));
		}
	};

	/**
	 * LED強度の変更通知?
	 */
	private final ARCommandCommonHeadlightsStateIntensityChangedListener
		mARCommandCommonHeadlightsStateIntensityChangedListener
			= new ARCommandCommonHeadlightsStateIntensityChangedListener() {
		@Override
		public void onCommonHeadlightsStateIntensityChangedUpdate(final byte left, final byte right) {
			// FIXME 未実装
			if (DEBUG) Log.v(TAG, String.format("onCommonHeadlightsIntensityUpdate(%d,%d)", left, right));
		}
	};

	/**
	 * アニメーション動作開始通知
	 */
	private final ARCommandCommonAnimationsStartAnimationListener
		mARCommandCommonAnimationsStartAnimationListener
			= new ARCommandCommonAnimationsStartAnimationListener() {
		@Override
		public void onCommonAnimationsStartAnimationUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM anim) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStartAnimationUpdate:" + anim);
		}
	};

	/**
	 * アニメーション動作終了通知
	 */
	private final ARCommandCommonAnimationsStopAnimationListener
		mARCommandCommonAnimationsStopAnimationListener
			= new ARCommandCommonAnimationsStopAnimationListener() {
		@Override
		public void onCommonAnimationsStopAnimationUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM anim) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStopAnimationUpdate:" + anim);
		}
	};

	/**
	 * 全てのアニメーション動作終了したことの通知
	 */
	private final ARCommandCommonAnimationsStopAllAnimationsListener
		mARCommandCommonAnimationsStopAllAnimationsListener
			= new ARCommandCommonAnimationsStopAllAnimationsListener() {
		@Override
		public void onCommonAnimationsStopAllAnimationsUpdate() {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStopAnimationUpdate:");
		}
	};

	/**
	 * アニメーションリスト変更通知
	 */
	private final ARCommandCommonAnimationsStateListListener
		mARCommandCommonAnimationsStateListListener
			= new ARCommandCommonAnimationsStateListListener() {

		@Override
		public void onCommonAnimationsStateListUpdate(
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM anim,
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM state,
			final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM error) {

			// FIXME 未実装
			if (DEBUG) Log.v(TAG, "onCommonAnimationsStateListUpdate:anim=" + anim + ",state=" + state + ",error=" + error);
		}
	};
//================================================================================
// コールバック関係
//================================================================================
	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	@Override
	public void addListener(final DeviceConnectionListener listener) {
		super.addListener(listener);
		if (listener instanceof FlightControllerListener) {
			synchronized (mListeners) {
				mListeners.add((FlightControllerListener) listener);
				callOnUpdateBattery(getBattery());
				callOnAlarmStateChangedUpdate(mStatus.getAlarm());
				callOnFlyingStateChangedUpdate(((DroneStatus)mStatus).getFlyingState());
			}
		}
	}

	/**
	 * 指定したコールバックリスナーを取り除く
	 * @param listener
	 */
	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mListeners) {
			mListeners.remove(listener);
			if (listener instanceof FlightControllerListener) {
				mListeners.remove((FlightControllerListener) listener);
			}
		}
	}

	/**
	 * バッテリー残量変更コールバックを呼び出す
	 */
	@Override
	protected void callOnUpdateBattery(final int percent) {
		super.callOnUpdateBattery(percent);
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onUpdateBattery(percent);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 飛行ステータス変更コールバックを呼び出す
	 * @param state
	 */
	protected void callOnFlyingStateChangedUpdate(final int state) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlyingStateChangedUpdate(state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 異常状態変更コールバックを呼び出す
	 * @param state
	 */
	protected void callOnAlarmStateChangedUpdate(final int state) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onAlarmStateChangedUpdate(state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * フラットトリム実行完了時のコールバックを呼び出す
	 */
	protected void callOnFlatTrimChanged() {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlatTrimChanged();
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション状態が変更された時のコールバックを呼び出す
	 * @param need_calibration
	 */
	protected void callOnCalibrationRequiredChanged(final boolean need_calibration) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationRequiredChanged(need_calibration);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーションを開始/終了した時のコールバックを呼び出す
	 * @param is_start
	 */
	protected void callOnCalibrationStartStop(final boolean is_start) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationStartStop(is_start);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション中の軸が変更された時のコールバックを呼び出す
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	protected void callOnCalibrationAxisChanged(final int axis) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationAxisChanged(axis);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnStillCaptureStateChanged(final int state) {
		final boolean changed = ((DroneStatus)mStatus).setStillCaptureState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onStillCaptureStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnVideoRecordingStateChanged(final int state) {
		final boolean changed = ((DroneStatus)mStatus).setVideoRecordingState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onVideoRecordingStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 機体のストレージ状態が変化した時のコールバックを呼び出す
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	protected void callOnUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
		final boolean changed = ((DroneStatus)mStatus).setMassStorageInfo(mass_storage_id, size, used_size, plugged, full, internal);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onUpdateStorageState(mass_storage_id, size, used_size, plugged, full, internal);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}
//================================================================================
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
	protected abstract boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading);

	/**
	 * アプリがHUDモードで操縦しているかどうかを設定
	 * @param inHUD
	 * @return
	 */
	public boolean sendPilotingHUD(final boolean inHUD) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonControllerStateIsPilotingChanged((byte) (inHUD ? 1 : 0));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "sendPilotingHUD　Failed");
		}

		return sentStatus;
	}

	/**
	 * キャリブレーション開始/キャンセル要求
	 * @param start true:開始, false:キャンセル
	 * @return
	 */
	public boolean sendCalibration(final boolean start) {

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonCalibrationMagnetoCalibration(start ? (byte)1 : (byte)0);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "sendCalibration　Failed");
		}

		return sentStatus;
	}
//********************************************************************************
// 操縦関係
//********************************************************************************
	/**
	 * roll/pitch変更時が移動かどうか
	 * @param flag 1:移動
	 */
	@Override
	public void setFlag(final int flag) {
		synchronized (mDataSync) {
			mDataPCMD.flag = flag == 0 ? 0 : (flag != 0 ? 1 : 0);
		}
	}

	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇
	 */
	@Override
	public void setGaz(final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.gaz = gaz > 100 ? 100 : (gaz < -100 ? -100 : gaz);
		}
	}

	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する
	 * @param roll 負:左, 正:右
	 */
	@Override
	public void setRoll(final float roll) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
		}
	}

	/**
	 * 機体を左右に傾ける
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setRoll(final float roll, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する
	 * @param pitch
	 */
	@Override
	public void setPitch(final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
		}
	}

	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setPitch(final float pitch, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転
	 */
	@Override
	public void setYaw(final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.yaw = yaw > 100 ? 100 : (yaw < -100 ? -100 : yaw);
		}
	}

	/**
	 * 北磁極に対する角度を設定・・・機体側で実装されてない
	 * @param heading
	 */
	@Override
	public void setHeading(final float heading) {
		synchronized (mDataSync) {
			mDataPCMD.heading = heading;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 * @param flag roll/pitchが移動を意味する時1
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, int flag) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = flag;
		}
	}

	protected void getPCMD(final DataPCMD dest) {
		if (dest != null) {
			synchronized (mDataSync) {
				dest.set(mDataPCMD);
			}
		}
	}

	/**
	 * 最大高度設定値を返す
	 * @return
	 */
	@Override
	public AttributeFloat getMaxAltitude() {
		return mSettings.maxAltitude();
	}

	@Override
	public AttributeFloat getMaxTilt() {
		return mSettings.maxTilt();
	}

	@Override
	public AttributeFloat getMaxVerticalSpeed() {
		return mSettings.maxVerticalSpeed();
	}

	@Override
	public AttributeFloat getMaxRotationSpeed() {
		return mSettings.maxRotationSpeed();
	}

	@Override
	public Vector getAttitude(){
		return ((DroneStatus)mStatus).attitude();
	}

	public float getAltitude() {
		return (float)mStatus.altitude();
	}

	/**
	 * モーターの自動カット機能が有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isCutoffMode() {
		return mSettings.cutOffMode();
	}

	/**
	 * 自動離陸モードが有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isAutoTakeOffModeEnabled() {
		return mSettings.autoTakeOffMode();
	}

	@Override
	public boolean hasGuard() {
		return mSettings.hasGuard();
	}

	@Override
	public AttributeMotor getMotor(final int index) {
		return ((DroneStatus)mStatus).getMotor(index);
	}

	/**
	 * LEDの明るさをセット
	 * @param left [0,255], 範囲外は256の剰余を適用
	 * @param right [0,255], 範囲外は256の剰余を適用
	 * @return
	 */
	@Override
	public boolean sendHeadlightsIntensity(final int left, final int right) {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonHeadlightsIntensity((byte)(left % 256), (byte)(right % 256));
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send flip command.");
		}

		return sentStatus;
	}

	/**
	 * 指定したアニメーション動作を開始。全部動くんかな?
	 * 共通のコマンドやけどJumpingSumoでしか動かないような予感。
	 * @param animation [0,12]
	 * @return
	 */
	@Override
	public boolean sendStartAnimation(final int animation) {
		// FIXME 実行開始したアニメーション動作を保持してコールバック&stopAnimationActionで更新するようにした方がいいのかも

//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN(3, "Spin animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_TAP(4, "Tap animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_METRONOME(6, "Metronome animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ONDULATION(7, "Standing dance animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SPIRAL(10, "Spiral animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_SLALOM(11, "Slalom animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_BOOST(12, "Boost animation."),
		final ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM anim = ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM.getFromValue(animation);
		if (anim == ARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONS_STARTANIMATION_ANIM_UNKNOWN_ENUM_VALUE)
			return false;

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStartAnimation(anim);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send StartAnimation command.");
		}

		return sentStatus;
	}

	/**
	 * 指定したアニメーション動作を停止。全部動くんかな?
	 * @param animation [0,12]
	 * @return
	 */
	@Override
	public boolean sendStopAnimation(final int animation) {
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN(3, "Spin animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_TAP(4, "Tap animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_METRONOME(6, "Metronome animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ONDULATION(7, "Standing dance animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SPIRAL(10, "Spiral animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_SLALOM(11, "Slalom animation."),
//		ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_BOOST(12, "Boost animation."),

		final ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM anim = ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM.getFromValue(animation);
		if (anim == ARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONS_STOPANIMATION_ANIM_UNKNOWN_ENUM_VALUE)
			return false;
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStopAnimation(anim);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send startAnimation command.");
		}

		return sentStatus;
	}

	/**
	 * 実行中のアニメーション動作を全て停止させる
	 * @return
	 */
	@Override
	public boolean sendStopAllAnimation() {
		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStopAllAnimations();
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send stopAllAnimations command.");
		}

		return sentStatus;
	}

	/**
	 * アニメーション動作関係らしいけど何するメソッドかよくわからん
	 * @param anim
	 * @param state
	 * @param error
	 * @return
	 */
	public boolean sendAnimationStateList(final int anim, final int state, final int error) {
		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM _anim = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM.getFromValue(anim);
		if (_anim == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_FLASH(0, "Flash headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_BLINK(1, "Blink headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_HEADLIGHTS_OSCILLATION(2, "Oscillating headlights."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN(3, "Spin animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_TAP(4, "Tap animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SLOW_SHAKE(5, "Slow shake animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_METRONOME(6, "Metronome animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_ONDULATION(7, "Standing dance animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN_JUMP(8, "Spin jump animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIN_TO_POSTURE(9, "Spin that end in standing posture, or in jumper if it was standing animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SPIRAL(10, "Spiral animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_SLALOM(11, "Slalom animation."),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ANIM_BOOST(12, "Boost animation."),

		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM _state = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM.getFromValue(state);
		if (_state == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_STOPPED(0, "animation is stopped"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_STARTED(1, "animation is started"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_STATE_NOTAVAILABLE(2, "The animation is not available"),

		final ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM _error = ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM.getFromValue(error);
		if (_error == ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_ENUM.eARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_UNKNOWN_ENUM_VALUE)
			return false;
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_OK(0, "No Error"),
//			ARCOMMANDS_COMMON_ANIMATIONSSTATE_LIST_ERROR_UNKNOWN(1, "Unknown generic error"),

		boolean sentStatus = true;
		final ARCommand cmd = new ARCommand();
		final ARCOMMANDS_GENERATOR_ERROR_ENUM cmdError = cmd.setCommonAnimationsStateList(_anim, _state, _error);
		if (cmdError == ARCOMMANDS_GENERATOR_ERROR_ENUM.ARCOMMANDS_GENERATOR_OK) {
			sentStatus = sendData(mNetConfig.getC2dAckId(), cmd,
				ARNETWORK_MANAGER_CALLBACK_RETURN_ENUM.ARNETWORK_MANAGER_CALLBACK_RETURN_RETRY, null);
			cmd.dispose();
		}

		if (!sentStatus) {
			Log.e(TAG, "Failed to send AnimationsStateList command.");
		}

		return sentStatus;
	}


	/** 操縦コマンドの値保持用 */
	protected static final class DataPCMD {
		public int flag;
		public float roll;
		public float pitch;
		public float yaw;
		public float gaz;
		public float heading;

		public DataPCMD() {
			flag = 0;
			roll = pitch = yaw = gaz = heading = 0;
		}

		private void set(final DataPCMD other) {
			flag = other.flag;
			roll = other.roll;
			pitch = other.pitch;
			yaw = other.yaw;
			gaz = other.gaz;
			heading = other.heading;
		}
	}

	/**
	 * 操縦コマンド送信スレッドでのループ内の処理(sendPCMDを呼び出す)
	 * 下位クラスで定期的にコマンド送信が必要ならoverride
	 */
	protected void sendCmdInControlLoop() {
		final int flag;
		float roll, pitch, yaw, gaz, heading;
		synchronized (mDataSync) {
			flag = mDataPCMD.flag;
			roll = mDataPCMD.roll;
			pitch = mDataPCMD.pitch;
			yaw = mDataPCMD.yaw;
			gaz = mDataPCMD.gaz;
			heading = mDataPCMD.heading;
		}
		// 操縦コマンド送信
		sendPCMD(flag, (int) roll, (int) pitch, (int)yaw, (int)gaz, (int)heading);
	}

	/** 操縦コマンドを定期的に送信するためのスレッド */
	protected class FlightCMDThread extends LooperThread {
		private final long intervals_ms;
		public FlightCMDThread(final long _intervals_ms) {
			intervals_ms = _intervals_ms;
		}

		@Override
		public void onLoop() {
			final long lastTime = SystemClock.elapsedRealtime();

			final int state = FlightController.super.getState();

			if (state == STATE_STARTED) {
				sendCmdInControlLoop();
			}
			// 次の送信予定時間までの休止時間を計算[ミリ秒]
			final long sleepTime = (SystemClock.elapsedRealtime() + intervals_ms) - lastTime;

			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException e) {
				// ignore
			}
		}
	}

}
