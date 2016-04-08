package com.serenegiant.arflight.NewControllers;

import android.content.Context;

import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM;
import com.parrot.arsdk.arcommands.ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DEVICE_STATE_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARCONTROLLER_ERROR_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARControllerCodec;
import com.parrot.arsdk.arcontroller.ARControllerDictionary;
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
import com.serenegiant.arflight.IWiFiController;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public class FlightControllerBebop extends FlightController implements ICameraController, IWiFiController {

	public FlightControllerBebop(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
		init();
	}

	/** 共通の初期化処理 */
	private void init() {
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);

		mSettings.setCutOffMode(true);
	}

	@Override
	protected boolean startNetwork() {
		final boolean failed = super.startNetwork();
		if (!failed) {
			mARDeviceController.addStreamListener(mStreamListener);
		}
		return failed;
	}

	@Override
	protected void stopNetwork() {
		if (mARDeviceController != null) {
			mARDeviceController.addStreamListener(mStreamListener);
		}
		super.stopNetwork();
	}

	private final ARDeviceControllerStreamListener mStreamListener = new ARDeviceControllerStreamListener() {
		@Override
		public ARCONTROLLER_ERROR_ENUM configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
			FlightControllerBebop.this.configureDecoder(deviceController, codec);
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public ARCONTROLLER_ERROR_ENUM onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
			FlightControllerBebop.this.onFrameReceived(deviceController, frame);
			return ARCONTROLLER_ERROR_ENUM.ARCONTROLLER_OK;
		}

		@Override
		public void onFrameTimeout(final ARDeviceController deviceController) {
			FlightControllerBebop.this.onFrameTimeout(deviceController);
		}
	};

	protected void configureDecoder(final ARDeviceController deviceController, final ARControllerCodec codec) {
	}

	protected void onFrameReceived(final ARDeviceController deviceController, final ARFrame frame) {
	}

	protected void onFrameTimeout(final ARDeviceController deviceController) {
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerDictionary elementDictionary) {

		super.onCommandReceived(deviceController, commandKey, elementDictionary);
		if (elementDictionary != null) {
			switch (commandKey) {
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3:	// (0, "Key used to define the feature <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGED:	// (1, "Key used to define the command <code>PictureStateChanged</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGED:	// (2, "Key used to define the command <code>VideoStateChanged</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_PICTURESTATECHANGEDV2:	// (3, "Key used to define the command <code>PictureStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDSTATE_VIDEOSTATECHANGEDV2:	// (4, "Key used to define the command <code>VideoStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED:	// (5, "Key used to define the command <code>PictureEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>ARDrone3</code>"),
			{
				// if event received is the picture notification
				final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
				if (args != null) {
					final ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM error
						= ARCOMMANDS_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR_ENUM.getFromValue(
							(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_PICTUREEVENTCHANGED_ERROR));
					// 写真を撮影した時の処理 FIXME 未実装
				}
				break;
			}
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIARECORDEVENT_VIDEOEVENTCHANGED:	// (6, "Key used to define the command <code>VideoEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLATTRIMCHANGED:	// (7, "Key used to define the command <code>FlatTrimChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED:	// (8, "Key used to define the command <code>FlyingStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			{
				// if event received is the flying state update
				final ARControllerArgumentDictionary<Object> args = elementDictionary.get(ARControllerDictionary.ARCONTROLLER_DICTIONARY_SINGLE_KEY);
				if (args != null) {
					final ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM state
						= ARCOMMANDS_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE_ENUM.getFromValue(
							(Integer)args.get(ARFeatureARDrone3.ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_FLYINGSTATECHANGED_STATE));
					// 飛行状態が変化した時の処理 FIXME 未実装
				}
				break;
			}
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALERTSTATECHANGED:	// (9, "Key used to define the command <code>AlertStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_NAVIGATEHOMESTATECHANGED:	// (10, "Key used to define the command <code>NavigateHomeStateChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_POSITIONCHANGED:	// (11, "Key used to define the command <code>PositionChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_SPEEDCHANGED:	// (12, "Key used to define the command <code>SpeedChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ATTITUDECHANGED:	// (13, "Key used to define the command <code>AttitudeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED:	// (14, "Key used to define the command <code>AutoTakeOffModeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSTATE_ALTITUDECHANGED:	// (15, "Key used to define the command <code>AltitudeChanged</code> of class <code>PilotingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGEVENT_MOVEBYEND:	// (16, "Key used to define the command <code>MoveByEnd</code> of class <code>PilotingEvent</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_WIFISCANLISTCHANGED:	// (17, "Key used to define the command <code>WifiScanListChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_ALLWIFISCANCHANGED:	// (18, "Key used to define the command <code>AllWifiScanChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSTATE_ALLWIFIAUTHCHANNELCHANGED:	// (20, "Key used to define the command <code>AllWifiAuthChannelChanged</code> of class <code>NetworkState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED:	// (21, "Key used to define the command <code>MaxAltitudeChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXTILTCHANGED:	// (22, "Key used to define the command <code>MaxTiltChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_ABSOLUTCONTROLCHANGED:	// (23, "Key used to define the command <code>AbsolutControlChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_MAXDISTANCECHANGED:	// (24, "Key used to define the command <code>MaxDistanceChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_NOFLYOVERMAXDISTANCECHANGED:	// (25, "Key used to define the command <code>NoFlyOverMaxDistanceChanged</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXHORIZONTALSPEED:	// (26, "Key used to define the command <code>AutonomousFlightMaxHorizontalSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXVERTICALSPEED:	// (27, "Key used to define the command <code>AutonomousFlightMaxVerticalSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXHORIZONTALACCELERATION:	// (28, "Key used to define the command <code>AutonomousFlightMaxHorizontalAcceleration</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXVERTICALACCELERATION:	// (29, "Key used to define the command <code>AutonomousFlightMaxVerticalAcceleration</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PILOTINGSETTINGSSTATE_AUTONOMOUSFLIGHTMAXROTATIONSPEED:	// (30, "Key used to define the command <code>AutonomousFlightMaxRotationSpeed</code> of class <code>PilotingSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED:	// (31, "Key used to define the command <code>MaxVerticalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED:	// (32, "Key used to define the command <code>MaxRotationSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_HULLPROTECTIONCHANGED:	// (33, "Key used to define the command <code>HullProtectionChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SPEEDSETTINGSSTATE_OUTDOORCHANGED:	// (34, "Key used to define the command <code>OutdoorChanged</code> of class <code>SpeedSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISELECTIONCHANGED:	// (35, "Key used to define the command <code>WifiSelectionChanged</code> of class <code>NetworkSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_NETWORKSETTINGSSTATE_WIFISECURITYCHANGED:	// (36, "Key used to define the command <code>WifiSecurityChanged</code> of class <code>NetworkSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTMOTORVERSIONLISTCHANGED:	// (37, "Key used to define the command <code>ProductMotorVersionListChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_PRODUCTGPSVERSIONCHANGED:	// (38, "Key used to define the command <code>ProductGPSVersionChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORSTATECHANGED:	// (39, "Key used to define the command <code>MotorErrorStateChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORSOFTWAREVERSIONCHANGED:	// (40, "Key used to define the command <code>MotorSoftwareVersionChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORFLIGHTSSTATUSCHANGED:	// (41, "Key used to define the command <code>MotorFlightsStatusChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_MOTORERRORLASTERRORCHANGED:	// (42, "Key used to define the command <code>MotorErrorLastErrorChanged</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_SETTINGSSTATE_P7ID:	// (43, "Key used to define the command <code>P7ID</code> of class <code>SettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_PICTUREFORMATCHANGED:	// (44, "Key used to define the command <code>PictureFormatChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_AUTOWHITEBALANCECHANGED:	// (45, "Key used to define the command <code>AutoWhiteBalanceChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_EXPOSITIONCHANGED:	// (46, "Key used to define the command <code>ExpositionChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_SATURATIONCHANGED:	// (47, "Key used to define the command <code>SaturationChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_TIMELAPSECHANGED:	// (48, "Key used to define the command <code>TimelapseChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PICTURESETTINGSSTATE_VIDEOAUTORECORDCHANGED:	// (49, "Key used to define the command <code>VideoAutorecordChanged</code> of class <code>PictureSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_MEDIASTREAMINGSTATE_VIDEOENABLECHANGED:	// (50, "Key used to define the command <code>VideoEnableChanged</code> of class <code>MediaStreamingState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMECHANGED:	// (51, "Key used to define the command <code>HomeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RESETHOMECHANGED:	// (52, "Key used to define the command <code>ResetHomeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSFIXSTATECHANGED:	// (53, "Key used to define the command <code>GPSFixStateChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_GPSUPDATESTATECHANGED:	// (54, "Key used to define the command <code>GPSUpdateStateChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_HOMETYPECHANGED:	// (55, "Key used to define the command <code>HomeTypeChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSETTINGSSTATE_RETURNHOMEDELAYCHANGED:	// (56, "Key used to define the command <code>ReturnHomeDelayChanged</code> of class <code>GPSSettingsState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_ORIENTATION:	// (57, "Key used to define the command <code>Orientation</code> of class <code>CameraState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_CAMERASTATE_DEFAULTCAMERAORIENTATION:	// (58, "Key used to define the command <code>DefaultCameraOrientation</code> of class <code>CameraState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_ELECTRICFREQUENCYCHANGED:	// (59, "Key used to define the command <code>ElectricFrequencyChanged</code> of class <code>AntiflickeringState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_ANTIFLICKERINGSTATE_MODECHANGED:	// (60, "Key used to define the command <code>ModeChanged</code> of class <code>AntiflickeringState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_NUMBEROFSATELLITECHANGED:	// (61, "Key used to define the command <code>NumberOfSatelliteChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_HOMETYPEAVAILABILITYCHANGED:	// (62, "Key used to define the command <code>HomeTypeAvailabilityChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_GPSSTATE_HOMETYPECHOSENCHANGED:	// (63, "Key used to define the command <code>HomeTypeChosenChanged</code> of class <code>GPSState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3_PROSTATE_FEATURES:	// (64, "Key used to define the command <code>Features</code> of class <code>PROState</code> in project <code>ARDrone3</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG:	// (65, "Key used to define the feature <code>ARDrone3Debug</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG_BATTERYDEBUGSETTINGSSTATE_USEDRONE2BATTERYCHANGED:	// (66, "Key used to define the command <code>UseDrone2BatteryChanged</code> of class <code>BatteryDebugSettingsState</code> in project <code>ARDrone3Debug</code>"),
			case ARCONTROLLER_DICTIONARY_KEY_ARDRONE3DEBUG_GPSDEBUGSTATE_NBSATELLITECHANGED:	// (67, "Key used to define the command <code>NbSatelliteChanged</code> of class <code>GPSDebugState</code> in project <code>ARDrone3Debug</code>"),
				break;
			default:
				break;
			}
		}
	}

//--------------------------------------------------------------------------------
// ICameraControllerのメソッド
//--------------------------------------------------------------------------------
	private CameraControllerListener mCameraControllerListener;
	@Override
	public void setCameraControllerListener(final CameraControllerListener listener) {
		mCameraControllerListener = listener;
	}

	/**
	 * 静止画撮影時の映像フォーマットを設定
	 * @param pictureFormat 0: Take raw image, 1: Take a 4:3 jpeg photo, 2: Take a 16:9 snapshot from camera, 3:take jpeg fisheye image only
	 * @return
	 */
	@Override
	public boolean sendPictureFormat(final int pictureFormat) {
		final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM type
			= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_ENUM.getFromValue(pictureFormat);
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_RAW (0, "Take raw image"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG (1, "Take a 4:3 jpeg photo"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_SNAPSHOT (2, "Take a 16:9 snapshot from camera"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_PICTUREFORMATSELECTION_TYPE_JPEG_FISHEYE (3, "Take jpeg fisheye image only"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsPictureFormatSelection(type);
		}
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordVideo(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEO_RECORD_STOP,
				(byte)mass_storage_id);
		}
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordVideoV2(
				start ? ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_START
				: ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_ENUM.ARCOMMANDS_ARDRONE3_MEDIARECORD_VIDEOV2_RECORD_STOP);
		}
		return false;
	}

	@Override
	public boolean sendCameraOrientation(final int tilt, final int pan) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendCameraOrientation((byte)tilt, (byte)pan);
		}
		return false;
	}

	@Override
	public boolean sendAutoWhiteBalance(final int auto_white_balance) {
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_AUTO (0, "Auto guess of best white balance params"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_TUNGSTEN (1, "Tungsten white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_DAYLIGHT (2, "Daylight white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_CLOUDY (3, "Cloudy white balance"),
		// ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_COOL_WHITE (4, "White balance for a flash"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			if (auto_white_balance >= 0) {
				final ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM awb
					= ARCOMMANDS_ARDRONE3_PICTURESETTINGS_AUTOWHITEBALANCESELECTION_TYPE_ENUM.getFromValue(auto_white_balance);
				mARDeviceController.getFeatureARDrone3().sendPictureSettingsAutoWhiteBalanceSelection(awb);
			} else {
				mARDeviceController.getFeatureARDrone3Debug().sendVideoManualWhiteBalance();
			}
		}
		return false;
	}

	@Override
	public int autoWhiteBalance() {
		return 0;
	}

	@Override
	public boolean sendExposure(final float exposure) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsExpositionSelection(exposure);
		}
		return false;
	}

	@Override
	public boolean sendSaturation(final float saturation) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsSaturationSelection(saturation);
		}
		return false;
	}

	@Override
	public boolean sendTimelapseSelection(final boolean enabled, final float interval) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsTimelapseSelection(enabled ? (byte)1 : (byte)0, interval);
		}
		return false;
	}

	@Override
	public boolean sendVideoAutoRecord(final boolean enabled, final int mass_storage_id) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPictureSettingsVideoAutorecordSelection(enabled ? (byte)1 : (byte)0, (byte)mass_storage_id);
		}
		return false;
	}

	@Override
	public boolean sendWobbleCancellation(final boolean enabled) {
		return false;
	}

	@Override
	public boolean sendVideoSyncAnglesGyros(final float anglesDelay_s, final float gyrosDelay_s) {
		return false;
	}

	@Override
	public int getPan() {
		return 0;
	}

	@Override
	public int getTilt() {
		return 0;
	}

	@Override
	public void setVideoStream(final IVideoStream video_stream) {

	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return false;
	}

	@Override
	public boolean enableVideoStreaming(final boolean enable) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaStreamingVideoEnable(enable ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean requestTakeoff() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingTakeOff();
		}
		return false;
	}

	@Override
	public boolean requestLanding() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingLanding();
		}
		return false;
	}

	@Override
	public boolean requestEmergencyStop() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingEmergency();
		}
		return false;
	}

	@Override
	public boolean requestFlatTrim() {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingFlatTrim();
		}
		return false;
	}

	@Override
	public boolean startCalibration(final boolean start) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureCommon().sendCalibrationMagnetoCalibration(start ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean setMaxAltitude(final float altitude) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxAltitude(altitude);
		}
		return false;
	}

	@Override
	public boolean setMaxTilt(final float tilt) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingSettingsMaxTilt(tilt);
		}
		return false;
	}

	@Override
	public boolean setMaxVerticalSpeed(final float speed) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxVerticalSpeed(speed);
		}
		return false;
	}

	@Override
	public boolean setMaxRotationSpeed(final float speed) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsMaxRotationSpeed(speed);
		}
		return false;
	}

	@Override
	public boolean canGetAttitude() {
		return true;
	}

	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		// これは常時ONなのかも
		return false;
	}

	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingAutoTakeOffMode(enable ? (byte)1 : (byte)0);
		}
		return false;
	}

	@Override
	public boolean setHasGuard(final boolean has_guard) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendSpeedSettingsHullProtection(has_guard ? (byte)1 : (byte)0);
		}
		return false;
	}

	private int _timestampAndSeqNum;
	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendPilotingPCMD((byte) flag, (byte) roll, (byte) pitch, (byte) yaw, (byte) gaz, _timestampAndSeqNum++);
		}
		return false;
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
		final ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM dir = ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_ENUM.getFromValue(direction - 1);
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_FRONT (0, "Flip direction front"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_BACK (1, "Flip direction back"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_RIGHT (2, "Flip direction right"),
		// ARCOMMANDS_ARDRONE3_ANIMATIONS_FLIP_DIRECTION_LEFT (3, "Flip direction left"),
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendAnimationsFlip(dir);
		}
		return false;
	}

	/**
	 * 自動で指定した角度回転させる
	 * @param degree -180〜180度
	 * @return
	 */
	@Override
	public boolean requestAnimationsCap(final int degree) {

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
		if ((mARDeviceController != null) && (getDeviceState().equals(ARCONTROLLER_DEVICE_STATE_ENUM.ARCONTROLLER_DEVICE_STATE_RUNNING))) {
			mARDeviceController.getFeatureARDrone3().sendMediaRecordPictureV2();
		}
		return false;
	}

	@Override
	public boolean requestTakePicture() {
		return requestTakePicture(0);
	}

	@Override
	public boolean setHeadlightsIntensity(final int left, final int right) {
		// これは無いみたい
		return false;
	}

	@Override
	public boolean startAnimation(final int animation) {
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAnimation(final int animation) {
		// これは無いみたい?
		return false;
	}

	@Override
	public boolean stopAllAnimation() {
		// これは無いみたい?
		return false;
	}
}
