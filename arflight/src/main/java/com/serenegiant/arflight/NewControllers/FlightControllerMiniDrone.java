package com.serenegiant.arflight.NewControllers;

import android.content.Context;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.configs.ARNetworkConfig;

public class FlightControllerMiniDrone extends FlightController {
	public FlightControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
	}

	@Override
	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		super.onCommandReceived(deviceController, commandKey, args);

		switch (commandKey) {
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE:	// (100, "Key used to define the feature <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLATTRIMCHANGED:	// (101, "Key used to define the command <code>FlatTrimChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_FLYINGSTATECHANGED:	// (102, "Key used to define the command <code>FlyingStateChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_ALERTSTATECHANGED:	// (103, "Key used to define the command <code>AlertStateChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSTATE_AUTOTAKEOFFMODECHANGED:	// (104, "Key used to define the command <code>AutoTakeOffModeChanged</code> of class <code>PilotingState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGED:	// (105, "Key used to define the command <code>PictureStateChanged</code> of class <code>MediaRecordState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDSTATE_PICTURESTATECHANGEDV2:	// (106, "Key used to define the command <code>PictureStateChangedV2</code> of class <code>MediaRecordState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_MEDIARECORDEVENT_PICTUREEVENTCHANGED:	// (107, "Key used to define the command <code>PictureEventChanged</code> of class <code>MediaRecordEvent</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXALTITUDECHANGED:	// (108, "Key used to define the command <code>MaxAltitudeChanged</code> of class <code>PilotingSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_PILOTINGSETTINGSSTATE_MAXTILTCHANGED:	// (109, "Key used to define the command <code>MaxTiltChanged</code> of class <code>PilotingSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXVERTICALSPEEDCHANGED:	// (110, "Key used to define the command <code>MaxVerticalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXROTATIONSPEEDCHANGED:	// (111, "Key used to define the command <code>MaxRotationSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_WHEELSCHANGED:	// (112, "Key used to define the command <code>WheelsChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SPEEDSETTINGSSTATE_MAXHORIZONTALSPEEDCHANGED:	// (113, "Key used to define the command <code>MaxHorizontalSpeedChanged</code> of class <code>SpeedSettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTMOTORSVERSIONCHANGED:	// (114, "Key used to define the command <code>ProductMotorsVersionChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_PRODUCTINERTIALVERSIONCHANGED:	// (115, "Key used to define the command <code>ProductInertialVersionChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_SETTINGSSTATE_CUTOUTMODECHANGED:	// (116, "Key used to define the command <code>CutOutModeChanged</code> of class <code>SettingsState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONE_FLOODCONTROLSTATE_FLOODCONTROLCHANGED:	// (117, "Key used to define the command <code>FloodControlChanged</code> of class <code>FloodControlState</code> in project <code>MiniDrone</code>"),
		case ARCONTROLLER_DICTIONARY_KEY_MINIDRONEDEBUG:	// (118, "Key used to define the feature <code>MiniDroneDebug</code>"),
		default:
			break;
		}
	}

	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		return false;
	}

	@Override
	public boolean requestTakeoff() {
		return false;
	}

	@Override
	public boolean requestLanding() {
		return false;
	}

	@Override
	public boolean requestEmergencyStop() {
		return false;
	}

	@Override
	public boolean requestFlatTrim() {
		return false;
	}

	@Override
	public boolean startCalibration(final boolean start) {
		return false;
	}

	@Override
	public boolean setMaxAltitude(final float altitude) {
		return false;
	}

	@Override
	public boolean setMaxTilt(final float tilt) {
		return false;
	}

	@Override
	public boolean setMaxVerticalSpeed(final float speed) {
		return false;
	}

	@Override
	public boolean setMaxRotationSpeed(final float speed) {
		return false;
	}

	@Override
	public boolean canGetAttitude() {
		return false;
	}

	@Override
	public boolean sendCutOutMode(final boolean enabled) {
		return false;
	}

	@Override
	public boolean sendAutoTakeOffMode(final boolean enable) {
		return false;
	}

	@Override
	public boolean setHasGuard(final boolean has_guard) {
		return false;
	}

	@Override
	public boolean requestAnimationsFlip(final int direction) {
		return false;
	}

	@Override
	public boolean requestAnimationsCap(final int degree) {
		return false;
	}

	@Override
	public boolean requestAnimationsCap(final int degree, final Object sync) {
		return false;
	}

	@Override
	public boolean requestTakePicture(final int mass_storage_id) {
		return false;
	}

	@Override
	public boolean requestTakePicture() {
		return false;
	}

	@Override
	public boolean setHeadlightsIntensity(final int left, final int right) {
		return false;
	}

	@Override
	public boolean startAnimation(final int animation) {
		return false;
	}

	@Override
	public boolean stopAnimation(final int animation) {
		return false;
	}

	@Override
	public boolean stopAllAnimation() {
		return false;
	}
}
