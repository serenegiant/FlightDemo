package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.util.Log;

import com.parrot.arsdk.arcommands.*;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.arcontroller.ARDeviceControllerStreamListener;
import com.parrot.arsdk.arcontroller.ARFeatureARDrone3;
import com.parrot.arsdk.arcontroller.ARFrame;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.CameraControllerListener;
import com.serenegiant.arflight.DroneSettings;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.ICameraController;
import com.serenegiant.arflight.IVideoStream;
import com.serenegiant.arflight.IVideoStreamNew;
import com.serenegiant.arflight.IWiFiController;
import com.serenegiant.arflight.WiFiStatus;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeGPS;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.attribute.AttributePosition;
import com.serenegiant.arflight.configs.ARNetworkConfigARDrone3;

import java.util.HashMap;
import java.util.Map;

public class FlightControllerBebopNewAPI extends FlightControllerNewAPI implements ICameraController, IWiFiController {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = FlightControllerBebopNewAPI.class.getSimpleName();

	private final Object mVideoSync = new Object();
	private IVideoStreamNew mVideoStream;
	/**
	 * 写真撮影のフォーマット<br>
	 * 0: Take raw image<br>
	 * 1: Take a 4:3 jpeg photo<br>
	 * 2: Take a 16:9 snapshot from camera<br>
	 */
	private int mPictureFormat;
	private String mMotorSoftwareVersion;

	public FlightControllerBebopNewAPI(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigARDrone3());
		if (DEBUG) Log.v (TAG, "コンストラクタ:");
		init();
	}

	/** 共通の初期化処理 */
	private void init() {
		if (DEBUG) Log.v (TAG, "init:");
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);

		mSettings.setCutOffMode(true);
	}

	@Override
	protected boolean startNetwork() {
		if (DEBUG) Log.v (TAG, "startNetwork:");
		final boolean failed = super.startNetwork();
		if (!failed) {
			mARDeviceController.addStreamListener(mStreamListener);
		}
		return failed;
	}

	@Override
	protected void onBeforeStop() {
		if (DEBUG) Log.v(TAG, "onBeforeStop:");
		if (mARDeviceController != null) {
			enableVideoStreaming(false);
			mARDeviceController.removeStreamListener(mStreamListener);
		}
		super.onBeforeStop();
	}

//	@Override
//	protected void onConnect() {
//		super.onConnect();
//		enableVideoStreaming(true);
//	}

//	protected void onDisconnect() {
//		enableVideoStreaming(false);
//		super.onDisconnect();
//	}

	private final ARDeviceControllerStreamListener mStreamListener = new ARDeviceControllerStreamListener() {
		@Override
		public ARCONTROLLER_ERROR_ENUM configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
			try {
				FlightControllerBebopNewAPI.this.configureDecoder(deviceController, codec);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public ARCONTROLLER_ERROR_ENUM onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
			try {
				FlightControllerBebopNewAPI.this.onFrameReceived(deviceController, frame);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public void onFrameTimeout(final ARDeviceController deviceController) {
			try {
				FlightControllerBebopNewAPI.this.onFrameTimeout(deviceController);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
	};

	protected void configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
		synchronized (mVideoSync) {
			if (mVideoStream != null) {
				mVideoStream.configureDecoder(codec);
			}
		}
	}

	protected void onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
		synchronized (mVideoSync) {
			if (mVideoStream != null) {
				mVideoStream.onReceiveFrame(frame);
			}
		}
	}

	protected void onFrameTimeout(final ARDeviceController deviceController) {
		synchronized (mVideoSync) {
			if (mVideoStream != null) {
				mVideoStream.onFrameTimeout();
			}
		}
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		super.onCommandReceived(deviceController, commandKey, args);

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3:	// (0, "Key used to define the feature <code>ARDrone3</code>"),
			break;
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGED:	// (1, "Key used to define the command <code>PictureStateChanged</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
		{	// 写真撮影状態を受信した時
			final int state = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGED_STATE);
			final int mass_storage_id = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGED_MASS_STORAGE_ID);
			if (DEBUG) Log.v(TAG, "onARDrone3MediaRecordStatePictureStateChangedUpdate:state=" + state + ",mass_storage_id=" + mass_storage_id);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGED:	// (2, "Key used to define the command <code>VideoStateChanged</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2:	// (3, "Key used to define the command <code>PictureStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
		{	// 写真撮影状態を受信した時
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM state
				= ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE)
			);
			final ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_ERROR)
			);
			int _state;
			switch (state) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_READY:		// 撮影可能
				_state = DroneStatus.MEDIA_READY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_BUSY:			// 撮影中
				_state = DroneStatus.MEDIA_BUSY;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2_STATE_NOTAVAILABLE:	// 撮影不可
			default:
				_state = DroneStatus.MEDIA_UNAVAILABLE;
				break;
			}
			callOnStillCaptureStateChanged(_state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2:	// (4, "Key used to define the command <code>VideoStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED:	// (5, "Key used to define the command <code>PictureEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>ARDrone3</code>"),
		{	// 写真撮影イベントを受信した時
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM event
				= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT)
			);
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR)
			);
			int _state;
			switch (event) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_TAKEN:			// 撮影成功
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_EVENT_FAILED:			// 撮影失敗
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnStillCaptureStateChanged(_state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED:	// (6, "Key used to define the command <code>VideoEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>ARDrone3</code>"),
		{	// 動画撮影イベントを受信した時
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_ENUM event
				= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT)
			);
			final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_ERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_ERROR_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_ERROR)
			);
			int _state;
			switch (event) {
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_START:	// 0
				_state = DroneStatus.MEDIA_BUSY;
				// XXX ここはreturnした方がいいかも?
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_STOP:	// 1
				_state = DroneStatus.MEDIA_SUCCESS;
				break;
			case ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED_EVENT_FAILED:	// 2
			default:
				_state = DroneStatus.MEDIA_ERROR;
				break;
			}
			callOnVideoRecordingStateChanged(_state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLATTRIMCHANGED:	// (7, "Key used to define the command <code>FlatTrimChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// フラットトリム変更を受信した時
			callOnFlatTrimChanged();
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED:	// (8, "Key used to define the command <code>FlyingStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 飛行状態が変化した時の処理
			final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state
				= ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));
			((DroneStatus)mStatus).setFlyingState(state.getValue() * 0x100);
			callOnFlyingStateChangedUpdate(getState());
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALERTSTATECHANGED:	// (9, "Key used to define the command <code>AlertStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 機体からの異常通知時
			final int state = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALERTSTATECHANGED_STATE);
			mStatus.setAlarm(state);
			callOnAlarmStateChangedUpdate(getAlarm());
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED:	// (10, "Key used to define the command <code>NavigateHomeStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// ナビゲーションホーム状態を受信した時
			// final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_STATE_ENUM
			final int state = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_STATE);
			// state=0(available): 利用可能, Navigate home is available
			// state=1(inProgress): 実行中, Navigate home is in progress
			// state=2(unavailable): 利用不可, Navigate home is not available
			// state=3(pending): 中断中, Navigate home has been received, but its process is pending

			// final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_REASON_ENUM
			final int reason = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED_REASON);
			// reason=0: User requested a navigate home (available->inProgress)
			// reason=1: Connection between controller and product lost (available->inProgress)
			// reason=2: Low battery occurred (available->inProgress)
			// reason=3: Navigate home is finished (inProgress->available)
			// reason=4: Navigate home has been stopped (inProgress->available)
			// reason=5: Navigate home disabled by product (inProgress->unavailable or available->unavailable)
			// reason=6: Navigate home enabled by product (unavailable->available)

			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED:	// (11, "Key used to define the command <code>PositionChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 機体位置(GPS座標)を受信した時
			/** GPS緯度[度] (500.0: 不明) */
			final double latitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LATITUDE);
			/** GPS経度[度] (500.0: 不明) */
			final double longitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_LONGITUDE);
			/** GPS高度[m](500.0: 不明) */
			final double altitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED_ALTITUDE);
			mStatus.setPosition(latitude, longitude, altitude);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED:	// (12, "Key used to define the command <code>SpeedChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 機体の速度を受信した時
			final float speedX = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDX)).floatValue();
			final float speedY = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDY)).floatValue();
			final float speedZ = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED_SPEEDZ)).floatValue();
			((DroneStatus)mStatus).setSpeed(speedY, speedX, -speedZ);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED:	// (13, "Key used to define the command <code>AttitudeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 機体姿勢を受信した時
			final float roll = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_ROLL)).floatValue();
			final float pitch = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_PITCH)).floatValue();
			final float yaw = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED_YAW)).floatValue();
			((DroneStatus)mStatus).setAttitude(roll, pitch, yaw);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED:	// (14, "Key used to define the command <code>AutoTakeOffModeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 自動離陸モード設定を受信した時
			final boolean state = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED_STATE) != 0;
			mSettings.setAutoTakeOffMode(state);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALTITUDECHANGED:	// (15, "Key used to define the command <code>AltitudeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
		{	// 高度を受信した時
			final float altitude = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALTITUDECHANGED_ALTITUDE)).floatValue();
			mStatus.altitude(altitude);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND:	// (16, "Key used to define the command <code>MoveByEnd</code> of class <code>PilotingEvent</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED:	// (17, "Key used to define the command <code>WifiScanListChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
		{	// WiFiスキャンリストが変更された時
			/** SSID of the AP */
			final String ssid  = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_SSID);
			/** RSSI of the AP in dbm (negative value) 受信信号強度 */
			final int rssi = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_RSSI);
			/** The band : 2.4 GHz or 5 GHz */
			final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM band
				= ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND)
			);
			/** Channel of the AP */
			final int channel = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_CHANNEL);
			onWifiScanListChangedUpdate(ssid, rssi, band, channel);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_ALLWIFISCANCHANGED:	// (18, "Key used to define the command <code>AllWifiScanChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
		{
			onAllWifiScanChangedUpdate();
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_ALLWIFIAUTHCHANNELCHANGED:	// (20, "Key used to define the command <code>AllWifiAuthChannelChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
		{
			onAllWifiAuthChannelChangedUpdate();
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED:	// (21, "Key used to define the command <code>MaxAltitudeChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 最大高度設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED_MAX)).floatValue();
			mSettings.setMaxAltitude(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXTILTCHANGED:	// (22, "Key used to define the command <code>MaxTiltChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 最大傾斜設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXTILTCHANGED_MAX)).floatValue();
			mSettings.setMaxTilt(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_ABSOLUTCONTROLCHANGED:	// (23, "Key used to define the command <code>AbsolutControlChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// AbsoluteControlChanged(ってなんやろ)を受信した時, 機体側で実装されてないみたい
			final boolean absoluteControl = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_ABSOLUTCONTROLCHANGED_ON) != 0;
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXDISTANCECHANGED:	// (24, "Key used to define the command <code>MaxDistanceChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 最大距離を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXDISTANCECHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXDISTANCECHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXDISTANCECHANGED_MAX)).floatValue();
			mSettings.setMaxDistance(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_NOFLYOVERMAXDISTANCECHANGED:	// (25, "Key used to define the command <code>NoFlyOverMaxDistanceChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXHORIZONTALSPEED:	// (26, "Key used to define the command <code>AutonomousFlightMaxHorizontalSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXVERTICALSPEED:	// (27, "Key used to define the command <code>AutonomousFlightMaxVerticalSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXHORIZONTALACCELERATION:	// (28, "Key used to define the command <code>AutonomousFlightMaxHorizontalAcceleration</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXVERTICALACCELERATION:	// (29, "Key used to define the command <code>AutonomousFlightMaxVerticalAcceleration</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXROTATIONSPEED:	// (30, "Key used to define the command <code>AutonomousFlightMaxRotationSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED:	// (31, "Key used to define the command <code>MaxVerticalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 上昇/降下速度設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED_MAX)).floatValue();
			mSettings.setMaxVerticalSpeed(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED:	// (32, "Key used to define the command <code>MaxRotationSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 最大回転速度設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_CURRENT)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED_MAX)).floatValue();
			mSettings.setMaxRotationSpeed(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_HULLPROTECTIONCHANGED:	// (33, "Key used to define the command <code>HullProtectionChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
		{	// ハルの有無設定を受信した時
			final boolean present = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_HULLPROTECTIONCHANGED_PRESENT) != 0;
			mSettings.setHasGuard(present);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_OUTDOORCHANGED:	// (34, "Key used to define the command <code>OutdoorChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 室外モードか室内モードを受信した時
			final boolean outdoor = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_OUTDOORCHANGED_OUTDOOR) != 0;
			mSettings.outdoorMode(outdoor);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED:	// (35, "Key used to define the command <code>WifiSelectionChanged</code> of class <code>NetworkSettingsState</code> in project <code>ARDrone3</code>"),
		{	// WiFiの選択状態が変化した時
			final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE)
			);
			final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band
				= ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND)
			);
			final int channel = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_CHANNEL);
			onWifiSelectionChangedUpdate(type, band, channel);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED:	// (36, "Key used to define the command <code>WifiSecurityChanged</code> of class <code>NetworkSettingsState</code> in project <code>ARDrone3</code>"),
		{	// WiFiのセキュリティ設定が変化した時
			final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED_TYPE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED_TYPE)
			);
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED:	// (37, "Key used to define the command <code>ProductMotorVersionListChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// モーターバージョンを受信した時
			final int motor = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED_MOTOR_NUMBER);
			final String type = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED_TYPE);
			final String software = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED_SOFTWARE);
			final String hardware = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED_HARDWARE);
			try {
				final int ix = (motor - 1) % getMotorNums();
				final AttributeMotor _motor = ((DroneStatus)mStatus).getMotor(ix);
				if (_motor != null) {
					_motor.set(type, software, hardware);
				} else {
					Log.w(TAG, "モーターNo.が予期したのと違う:" + motor);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTGPSVERSIONCHANGED:	// (38, "Key used to define the command <code>ProductGPSVersionChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// GPSのバージョンを受信した時
			final String software = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTGPSVERSIONCHANGED_SOFTWARE);
			final String hardware = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTGPSVERSIONCHANGED_HARDWARE);

			mGPS.set(software, hardware);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED:	// (39, "Key used to define the command <code>MotorErrorStateChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// モーターのエラー状態を受信した時
			final int motorIds = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORIDS);
			final ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR)
			);

			final int n = getMotorNums();
			if (!ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_ENUM
				.ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED_MOTORERROR_NOERROR.equals(error)) {
				final int err = 1 << (error.getValue() - 1);
				for (int i = 0; i < n; i++) {
					if ((motorIds & (1 << i)) != 0) {
						getMotor(i).setError(err);
					}
				}
			} else {
				for (int i = 0; i < n; i++) {
					if ((motorIds & (1 << i)) != 0) {
						getMotor(i).clearError();
					}
				}
			}
			// FIXME UI側へ異常を通知する?
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORSOFTWAREVERSIONCHANGED:	// (40, "Key used to define the command <code>MotorSoftwareVersionChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// モーターソフトウエアバージョンを受信した時
			final String version = (String)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORSOFTWAREVERSIONCHANGED_VERSION);
			mMotorSoftwareVersion = version;
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORFLIGHTSSTATUSCHANGED:	// (41, "Key used to define the command <code>MotorFlightsStatusChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// 飛行回数・飛行時間を受信した時
			/** 飛行回数 */
			final int nbFlights = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORFLIGHTSSTATUSCHANGED_NBFLIGHTS);
			/** 最後の飛行時間[秒] */
			final int lastFlightDuration = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORFLIGHTSSTATUSCHANGED_LASTFLIGHTDURATION);
			/** 合計飛行時間[秒] */
			final int totalFlightDuration = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORFLIGHTSSTATUSCHANGED_TOTALFLIGHTDURATION);

			((DroneStatus)mStatus).setFlightDuration(nbFlights, lastFlightDuration, totalFlightDuration);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED:	// (42, "Key used to define the command <code>MotorErrorLastErrorChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// 最後に起こったモーターエラーを受信した時
			final ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED_MOTORERROR_ENUM error
				= ARCOMMANDS_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED_MOTORERROR_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED_MOTORERROR)
			);
			if (DEBUG) Log.v(TAG, "onARDrone3SettingsStateMotorErrorLastErrorChangedUpdate:" + error);
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_P7ID:	// (43, "Key used to define the command <code>P7ID</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED:	// (44, "Key used to define the command <code>PictureFormatChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 写真撮影時のフォーマットを受信した時
			final int format = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED_TYPE);
			if (DEBUG) Log.v(TAG, "onARDrone3PictureSettingsStatePictureFormatChangedUpdate:" + format);
			if (mPictureFormat != format) {
				mPictureFormat = format;
				// FIXME 未実装
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED:	// (45, "Key used to define the command <code>AutoWhiteBalanceChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// オートホワイトバランス設定を受信した時
			final int auto_white_blance = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED_TYPE);
			// 0: 自動 Auto guess of best white balance params
			// 1: 電球色 Tungsten white balance<br>
			// 2: 晴天 Daylight white balance<br>
			// 3: 曇り空 Cloudy white balance<br>
			// 4: フラシュ撮影用 White balance for a flash
			mSettings.autoWhiteBalance(auto_white_blance);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_EXPOSITIONCHANGED:	// (46, "Key used to define the command <code>ExpositionChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 露出設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_EXPOSITIONCHANGED_VALUE)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_EXPOSITIONCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_EXPOSITIONCHANGED_MAX)).floatValue();
			mSettings.setExposure(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_SATURATIONCHANGED:	// (47, "Key used to define the command <code>SaturationChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 彩度設定を受信した時
			final float current = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_SATURATIONCHANGED_VALUE)).floatValue();
			final float min = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_SATURATIONCHANGED_MIN)).floatValue();
			final float max = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_SATURATIONCHANGED_MAX)).floatValue();
			mSettings.setExposure(current, min, max);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED:	// (48, "Key used to define the command <code>TimelapseChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// タイムラプス設定を受信した時
			final boolean enabled = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED_ENABLED) != 0;
			final float interval = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED_INTERVAL)).floatValue();
			final float minInterval = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED_MININTERVAL)).floatValue();
			final float maxInterval = ((Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED_MAXINTERVAL)).floatValue();
			mSettings.setTimeLapse(enabled, interval, minInterval, maxInterval);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_VIDEOAUTORECORDCHANGED:	// (49, "Key used to define the command <code>VideoAutorecordChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
		{	// 自動録画設定を受信した時
			final boolean enabled = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_VIDEOAUTORECORDCHANGED_ENABLED) != 0;
			final int mass_storage_id = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_VIDEOAUTORECORDCHANGED_MASS_STORAGE_ID);

			if (DEBUG) Log.v(TAG, "onARDrone3PictureSettingsStateVideoAutorecordChangedUpdate:enabled=" + enabled + ",mass_storage_id=" + mass_storage_id);
			mSettings.getCamera().autoRecord(enabled, mass_storage_id);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED:	// (50, "Key used to define the command <code>VideoEnableChanged</code> of class <code>MediaStreamingState</code> in project <code>ARDrone3</code>"),
		{	// ライブビデオストリーミングの有効無効を受信した時
			final int enabled = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED_ENABLED);
			if (DEBUG) Log.v(TAG, "onARDrone3MediaStreamingStateVideoEnableChangedUpdate:enabled=" + enabled);

			// 0: Video streaming is enabled.
			// 1: Video streaming is disabled.
			// 2: Video streaming failed to start.
			mSettings.getCamera().videoStateState(enabled);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMECHANGED:	// (51, "Key used to define the command <code>HomeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// ホーム位置(GPS座標)を受信した時
			/** ホーム位置:緯度[度] */
			final double latitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMECHANGED_LATITUDE);
			/** ホーム位置:経度[度] */
			final double longitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMECHANGED_LONGITUDE);
			/** ホーム位置:高度[m] */
			final double altitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMECHANGED_ALTITUDE);
			mStatus.setHome(latitude, longitude, altitude);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED:	// (52, "Key used to define the command <code>ResetHomeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// ホーム位置(GPS座標)がリセットされた時
			/** ホーム位置:緯度[度] */
			final double latitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_LATITUDE);
			/** ホーム位置:経度[度] */
			final double longitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_LONGITUDE);
			/** ホーム位置:高度[m] */
			final double altitude = (Double)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED_ALTITUDE);
			mStatus.setHome(latitude, longitude, altitude);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED:	// (53, "Key used to define the command <code>GPSFixStateChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// GPSで自機位置を確認出来たかどうかを受信した時
			final boolean fixed = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED_FIXED) != 0;
			mGPS.setFixed(fixed);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED:	// (54, "Key used to define the command <code>GPSUpdateStateChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// GPSの状態を受信した時
			final ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM state
				= ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE)
			);
			switch (state) {
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_UPDATED:	// 1
				// 0: Drone GPS update succeed
				break;
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_INPROGRESS:	// 2
				// 1: Drone GPS update In progress
				break;
			case ARCOMMANDS_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED_STATE_FAILED:	// 3
				// 2: Drone GPS update failed
				mStatus.setPosition(AttributePosition.INVALID_VALUE, AttributePosition.INVALID_VALUE, AttributePosition.INVALID_VALUE);
				mGPS.numGpsSatellite(0);
				break;
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMETYPECHANGED:	// (55, "Key used to define the command <code>HomeTypeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// ホーム位置への戻り方を受信した時
			final ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_ENUM.getFromValue(
					(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMETYPECHANGED_TYPE)
			);
			switch (type) {
			case ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_TAKEOFF:	// (0, "The drone will try to return to the take off position")
			case ARCOMMANDS_ARDRONE3_GPSSETTINGS_HOMETYPE_TYPE_PILOT: // (1, "The drone will try to return to the pilot position")
			}
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RETURNHOMEDELAYCHANGED:	// (56, "Key used to define the command <code>ReturnHomeDelayChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
		{	// ホーム位置へ戻り始めるまでの遅延時間[秒]
			final int delay = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RETURNHOMEDELAYCHANGED_DELAY);
			if (DEBUG) Log.v(TAG, "onARDrone3GPSSettingsReturnHomeDelayUpdate:delay=" + delay);
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION:	// (57, "Key used to define the command <code>Orientation</code> of class <code>CameraState</code> in project <code>ARDrone3</code>"),
		{	// カメラの向きを受信した時
			/** Tilt camera consign for the drone [-100;100] */
			final int tilt = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_TILT);
			/** Pan camera consign for the drone [-100;100] */
			final int pan = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION_PAN);
			mSettings.getCamera().pantilt(pan, tilt);
			if (mCameraControllerListener != null) {
				try {
					mCameraControllerListener.onCameraOrientationChanged(pan, tilt);
				} catch (final Exception e) {
					// ignore
				}
			}
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_DEFAULTCAMERAORIENTATION:	// (58, "Key used to define the command <code>DefaultCameraOrientation</code> of class <code>CameraState</code> in project <code>ARDrone3</code>"),
		{	// カメラの向きの初期値を受信した時
			/** Tilt camera consign for the drone [-100;100] */
			final int tilt = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_DEFAULTCAMERAORIENTATION_TILT);
			/** Pan camera consign for the drone [-100;100] */
			final int pan = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_DEFAULTCAMERAORIENTATION_PAN);
			mSettings.getCamera().pantiltDefault(pan, tilt);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_ELECTRICFREQUENCYCHANGED:	// (59, "Key used to define the command <code>ElectricFrequencyChanged</code> of class <code>AntiflickeringState</code> in project <code>ARDrone3</code>"),
		{	// ちらつき防止設定の周波数を受信した時
			final int frequency = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_ELECTRICFREQUENCYCHANGED_FREQUENCY);

			int _frequency;
			switch (frequency) {
			case 0: // "Electric frequency of the country is 50hz"),
				_frequency = 50;
				break;
			case 1: // "Electric frequency of the country is 60hz"),
			default:
				_frequency = 60;
				break;
			}
			mSettings.getCamera().antiflickering(_frequency);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_MODECHANGED:	// (60, "Key used to define the command <code>ModeChanged</code> of class <code>AntiflickeringState</code> in project <code>ARDrone3</code>"),
		{	// ちらつき防止設定を受信した時
			final int mode = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_MODECHANGED_MODE);

			// 0, "Anti flickering based on the electric frequency previously sent"
			// 1, "Anti flickering based on a fixed frequency of 50Hz"
			// 2, "Anti flickering based on a fixed frequency of 60Hz"
			mSettings.getCamera().antiflickeringMode(mode);

			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_NUMBEROFSATELLITECHANGED:	// (61, "Key used to define the command <code>NumberOfSatelliteChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
		{	// 捕捉しているGPS衛星の数を受信した時
			final int nbSatellite = (Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_NUMBEROFSATELLITECHANGED_NUMBEROFSATELLITE);
			mGPS.numGpsSatellite(nbSatellite);
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_HOMETYPEAVAILABILITYCHANGED:	// (62, "Key used to define the command <code>HomeTypeAvailabilityChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED:	// (63, "Key used to define the command <code>HomeTypeChosenChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
		{	// ホーム位置への戻り方の選択が変更された時
			final ARCOMMANDS_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE_ENUM.getFromValue(
				(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE)
			);
			if (DEBUG) Log.v(TAG, "onARDrone3GPSStateHomeTypeChosenChangedUpdate:" + type);
			switch (type) {
			case ARCOMMANDS_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE_TAKEOFF: // (0, "The drone will try to return to the take off position"),
			case ARCOMMANDS_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE_PILOT:	// (1, "The drone will try to return to the pilot position"),
			case ARCOMMANDS_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED_TYPE_FIRST_FIX: // (2, "The drone has not enough information, it will try to return to the first GPS fix"),
			}
			// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PROSTATE_FEATURES:	// (64, "Key used to define the command <code>Features</code> of class <code>PROState</code> in project <code>ARDrone3</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG:	// (65, "Key used to define the feature <code>ARDrone3Debug</code>"),
		{	// FIXME 未実装
			break;
		}
		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG_BATTERYDEBUGSETTINGSSTATE_USEDRONE2BATTERYCHANGED:	// (66, "Key used to define the command <code>UseDrone2BatteryChanged</code> of class <code>BatteryDebugSettingsState</code> in project <code>ARDrone3Debug</code>"),
		{	// FIXME 未実装
			break;
		}
//		case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG_GPSDEBUGSTATE_NBSATELLITECHANGED:	// (67, "Key used to define the command <code>NbSatelliteChanged</code> of class <code>GPSDebugState</code> in project <code>ARDrone3Debug</code>"),
//		{	// 捕捉しているGPS衛星の数を受信した時
// 			XXX これは定義だけかも? ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_NUMBEROFSATELLITECHANGEDが代わりにある
//			break;
//		}
		default:
			break;
		}
	}

//--------------------------------------------------------------------------------
// ICameraControllerのメソッド
//--------------------------------------------------------------------------------
	private CameraControllerListener mCameraControllerListener;
	@Override
	public void setCameraControllerListener(final CameraControllerListener listener) {
		if (DEBUG) Log.v (TAG, "setCameraControllerListener:" + listener);
		mCameraControllerListener = listener;
	}

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera, 3:take jpeg fisheye image only
	 * @return
	 */
	@Override
	public boolean sendPictureFormat(final int pictureFormat) {
		if (DEBUG) Log.v (TAG, "sendPictureFormat:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM type
				= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM.getFromValue(pictureFormat);
			// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_RAW (0, "Take raw image"),
			// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG (1, "Take a 4:3 jpeg photo"),
			// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_SNAPSHOT (2, "Take a 16:9 snapshot from camera"),
			// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG_FISHEYE (3, "Take jpeg fisheye image only"),
			result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsPictureFormatSelection(type);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendPictureFormat failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		if (DEBUG) Log.v (TAG, "sendVideoRecording:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendMediaRecordVideo(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_STOP,
				(byte)mass_storage_id);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendVideoRecording failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendVideoRecording(final boolean start) {
		if (DEBUG) Log.v (TAG, "sendVideoRecording:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendMediaRecordVideoV2(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_STOP);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendVideoRecording failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendCameraOrientation(final int tilt, final int pan) {
		if (DEBUG) Log.v (TAG, "sendCameraOrientation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendCameraOrientation((byte)tilt, (byte)pan);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendCameraOrientation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendAutoWhiteBalance(final int auto_white_balance) {
		if (DEBUG) Log.v (TAG, "sendAutoWhiteBalance:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			try {
				// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_AUTO (0, "Auto guess of best white balance params"),
				// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_TUNGSTEN (1, "Tungsten white balance"),
				// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_DAYLIGHT (2, "Daylight white balance"),
				// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_CLOUDY (3, "Cloudy white balance"),
				// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_COOL_WHITE (4, "White balance for a flash"),
				if (auto_white_balance >= 0) {
					final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM awb
						= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM.getFromValue(auto_white_balance);
					result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsAutoWhiteBalanceSelection(awb);
				} else {
					result = mARDeviceController.getFeatureARDrone3Debug().sendVideoManualWhiteBalance();
					// FIXME ここでヌルポになる(getFeatureARDrone3Debugがnullを返す)
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendAutoWhiteBalance failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public int autoWhiteBalance() {
		return 0;
	}

	@Override
	public boolean sendExposure(final float exposure) {
		if (DEBUG) Log.v (TAG, "sendExposure:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsExpositionSelection(exposure);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendExposure failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendSaturation(final float saturation) {
		if (DEBUG) Log.v (TAG, "sendSaturation:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsSaturationSelection(saturation);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendSaturation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendTimelapseSelection(final boolean enabled, final float interval) {
		if (DEBUG) Log.v (TAG, "sendTimelapseSelection:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsTimelapseSelection(enabled ? (byte)1 : (byte)0, interval);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendTimelapseSelection failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id) {
		if (DEBUG) Log.v (TAG, "sendVideoAutoRecord:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPictureSettingsVideoAutorecordSelection(enabled ? (byte)1 : (byte)0, (byte)mass_storage_id);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendVideoAutoRecord failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendWobbleCancellation(final boolean enabled) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendWobbleCancellation failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s) {
		if (DEBUG) Log.v (TAG, "sendVideoSyncAnglesGyros:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendVideoSyncAnglesGyros failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public int getPan() {
		return 0;
	}

	@Override
	public int getTilt() {
		return 0;
	}

//--------------------------------------------------------------------------------
// IVideoStreamControllerのメソッド
//--------------------------------------------------------------------------------
	@Override
	public void setVideoStream(final IVideoStream video_stream) {
		if (DEBUG) Log.v(TAG, "setVideoStream:" + video_stream);
		if (video_stream instanceof IVideoStreamNew) {
			synchronized (mVideoSync) {
				mVideoStream = (IVideoStreamNew)video_stream;
			}
		}
	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return mSettings.getCamera().isVideoStreamingEnabled();
	}

	@Override
	public boolean enableVideoStreaming(final boolean enable) {
		if (DEBUG) Log.v(TAG, "enableVideoStreaming:enable=" + enable + ",isActive=" + isActive());
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable(enable ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#enableVideoStreaming failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

//--------------------------------------------------------------------------------
	@Override
	public boolean requestTakeoff() {
		if (DEBUG) Log.v (TAG, "requestTakeoff:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isGPSFixed()) {
			sendResetHome();
		}
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingTakeOff();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestTakeoff failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestLanding() {
		if (DEBUG) Log.v (TAG, "requestLanding:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingLanding();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestLanding failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestEmergencyStop() {
		if (DEBUG) Log.v (TAG, "requestEmergencyStop:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingEmergency();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestEmergencyStop failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestFlatTrim() {
		if (DEBUG) Log.v (TAG, "requestFlatTrim:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingFlatTrim();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestFlatTrim failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean startCalibration(final boolean start) {
		if (DEBUG) Log.v (TAG, "startCalibration:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureCommon().sendCalibrationMagnetoCalibration(start ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#startCalibration failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxAltitude(final float altitude) {
		if (DEBUG) Log.v (TAG, "setMaxAltitude:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxAltitude(altitude);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxAltitude failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxTilt(final float tilt) {
		if (DEBUG) Log.v (TAG, "setMaxTilt:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxTilt(tilt);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxTilt failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxVerticalSpeed(final float speed) {
		if (DEBUG) Log.v (TAG, "setMaxVerticalSpeed:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxVerticalSpeed(speed);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxVerticalSpeed failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setMaxRotationSpeed(final float speed) {
		if (DEBUG) Log.v (TAG, "setMaxRotationSpeed:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxRotationSpeed(speed);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setMaxRotationSpeed failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean canGetAttitude() {
		return true;
	}

	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		if (DEBUG) Log.v (TAG, "sendCutOutMode:");
		// これは常時ONなのかも
		return false;
	}

	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		if (DEBUG) Log.v (TAG, "sendAutoTakeOffMode:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendPilotingAutoTakeOffMode(enable ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendAutoTakeOffMode failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean setHasGuard(final boolean has_guard) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (DEBUG) Log.v (TAG, "setHasGuard:");
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendSpeedSettingsHullProtection(has_guard ? (byte)1 : (byte)0);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#setHasGuard failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	private int _timestampAndSeqNum;
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			try {
				result = mARDeviceController.getFeatureARDrone3().sendPilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, _timestampAndSeqNum++);
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendPCMD failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * 指定した方向にフリップ実行
	 @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * 	public static final int FLIP_FRONT = 1;
	 *	public static final int FLIP_BACK = 2;
	 *	public static final int FLIP_RIGHT = 3;
	 *	public static final int FLIP_LEFT = 4;
	 * @return
	 */
	@Override
	public boolean requestAnimationsFlip(final int direction) {
		if (DEBUG) Log.v (TAG, "requestAnimationsFlip:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			final ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.getFromValue(direction - 1);
			// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_FRONT (0, "Flip direction front"),
			// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_BACK (1, "Flip direction back"),
			// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_RIGHT (2, "Flip direction right"),
			// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_LEFT (3, "Flip direction left"),
			result = mARDeviceController.getFeatureARDrone3().sendAnimationsFlip(dir);
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestAnimationsFlip failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	/**
	 * 自動で指定した角度回転させる
	 * @param degree -180〜180度
	 * @return
	 */
	@Override
	public boolean requestAnimationsCap(final int degree) {
		if (DEBUG) Log.v (TAG, "requestAnimationsCap:");

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;

		if (degree != 0) {

			final AttributeFloat rotation_speed = mSettings.maxRotationSpeed();    // 回転速度[度/秒]
			final float current = rotation_speed.current();
			try {
				try {
					if (current != rotation_speed.max()) {
						// 最大回転速度に変更する
						setMaxRotationSpeed(rotation_speed.max());
						Thread.sleep(5);
					}
					final long t = (long) Math.abs(degree / rotation_speed.max() * 1000);    // 回転時間[ミリ秒]を計算
					setYaw(degree > 0 ? 100 : -100);
					Thread.sleep(t + 5);
				} catch (InterruptedException e) {
				}
			} finally {
				// 元の回転速度設定に戻す
				setYaw(0);
				setMaxRotationSpeed(current);
			}
		}
		return sentStatus;
	}

	public boolean requestAnimationsCap(final int degree, final Object sync) {
		if (DEBUG) Log.v (TAG, "requestAnimationsCap:");

		final byte d = (byte)(degree > 180 ? 180 : (degree < -180 ? -180 : degree));
		boolean sentStatus = true;

		if (degree != 0) {

			final AttributeFloat rotation_speed = mSettings.maxRotationSpeed();    // 回転速度[度/秒]
			final float current = rotation_speed.current();
			try {
				try {
					final long t = (long) Math.abs(degree / rotation_speed.max() * 1000);    // 回転時間[ミリ秒]を計算
					synchronized (sync) {
						if (current != rotation_speed.max()) {
							// 最大回転速度に変更する
							setMaxRotationSpeed(rotation_speed.max());
							sync.wait(5);
						}
						setYaw(degree > 0 ? 100 : -100);
						sync.wait(t);
					}
				} catch (InterruptedException e) {
				}
			} finally {
				// 元の回転速度設定に戻す
				setYaw(0);
				setMaxRotationSpeed(current);
			}
		}
		return sentStatus;
	}

	@Override
	public boolean requestTakePicture(final int mass_storage_id) {
		if (DEBUG) Log.v (TAG, "requestTakePicture:mass_storage_id=" + mass_storage_id);
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendMediaRecordPictureV2();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#requestTakePicture failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

	@Override
	public boolean requestTakePicture() {
		if (DEBUG) Log.v (TAG, "requestTakePicture:");
		return requestTakePicture(0);
	}

	@Override
	public boolean setHeadlightsIntensity(final int left, final int right) {
		if (DEBUG) Log.v (TAG, "setHeadlightsIntensity:");
		// これは無いみたい
		return false;
	}

	@Override
	public boolean startAnimation(final int animation) {
		if (DEBUG) Log.v (TAG, "startAnimation:");
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAnimation(final int animation) {
		if (DEBUG) Log.v (TAG, "stopAnimation:");
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAllAnimation() {
		if (DEBUG) Log.v (TAG, "stopAllAnimation:");
		// これは無いみたい?
		return false;
	}
//--------------------------------------------------------------------------------
// GPS関係
//--------------------------------------------------------------------------------
	public final AttributeGPS mGPS = new AttributeGPS();
	public boolean isGPSFixed() {
		return mGPS.fixed();
	}

	public boolean sendResetHome() {
		if (DEBUG) Log.v (TAG, "sendResetHome:");
		ARCONTROLLER_ERROR_ENUM result = ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_ERROR;
		if (isActive()) {
			result = mARDeviceController.getFeatureARDrone3().sendGPSSettingsResetHome();
		}
		if (result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK) {
			Log.e(TAG, "#sendResetHome failed:" + result);
		}
		return result != ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
	}

//--------------------------------------------------------------------------------
// WiFi関係
//--------------------------------------------------------------------------------
	/** WiFiの状態 */
	private final Map<String, WiFiStatus> mWifiStatus = new HashMap<String, WiFiStatus>();

	/**
	 * WiFiスキャンリストが変更された時
	 * @param ssid SSID of the AP
	 * @param rssi RSSI of the AP in dbm (negative value) 受信信号強度
	 * @param band The band : 2.4 GHz or 5 GHz
	 * @param channel Channel of the AP
	 */
	protected void onWifiScanListChangedUpdate(
		final String ssid, final int rssi,
		final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED_BAND_ENUM band,
		final int channel) {
		Log.d(TAG, String.format("ssid=%s,rssi=%d,band=%s,channel=%d", ssid, rssi, band.toString(), channel));
		final String key = band.toString() + Integer.toString(channel);
		WiFiStatus status = mWifiStatus.get(key);
		if (status == null) {
			status = new WiFiStatus(-66);
			mWifiStatus.put(key, status);
		}
		status.ssid = ssid;
		status.rssi = rssi;
		status.band = band.getValue();
		status.channel = channel;
	}

	/**
	 * WiFiスキャンが変化した時
	 */
	protected void onAllWifiScanChangedUpdate() {
		if (DEBUG) Log.v (TAG, "onAllWifiScanChangedUpdate:");
		// FIXME 未実装
	}

	/**
	 * WiFiチャンネルリストが変化した時
	 * @param band The band of this channel : 2.4 GHz or 5 GHz
	 * @param channel The authorized channel.
	 * @param in_or_out Bit 0 is 1 if channel is authorized outside (0 otherwise) ; Bit 1 is 1 if channel is authorized inside (0 otherwise)
	 */
	protected void onWifiAuthChannelListChangedUpdate(
		final ARCOMMANDS_ARDRONE3_NETWORKSTATE_WIFIAUTHCHANNELLISTCHANGED_BAND_ENUM band,
		final byte channel,
		final byte in_or_out) {
		Log.d(TAG, String.format("band=%s, channel=%d, in_or_out=%d", band.toString(), channel, in_or_out));
		final String key = band.toString() + Byte.toString(channel);

		// FIXME 未実装
	}

	/**
	 * WiFiチャネルの状態が変化した時
	 */
	protected void onAllWifiAuthChannelChangedUpdate() {
		if (DEBUG) Log.v (TAG, "onAllWifiAuthChannelChangedUpdate:");
		// FIXME 未実装
	}


	/** WiFiのセキュリティ設定が変化した時 */
	protected void onWifiSecurityChangedUpdate(
		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED_TYPE_ENUM type) {
		if (DEBUG) Log.v (TAG, "onWifiSecurityChangedUpdate:type=" + type);

		// FIXME 未実装
	}

	/**
	 * WiFiの選択状態が変化した時
	 * @param type The type of wifi selection settings
	 * @param band The actual  wifi band state
	 * @param channel The channel (depends of the band)
	 */
	protected void onWifiSelectionChangedUpdate(
		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_TYPE_ENUM type,
		final ARCOMMANDS_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED_BAND_ENUM band,
		final int channel) {
		if (DEBUG) Log.v (TAG, "onWifiSelectionChangedUpdate:type=" + type + ",band=" + band + ",channel=" + channel);

		// FIXME 未実装
	}
}
