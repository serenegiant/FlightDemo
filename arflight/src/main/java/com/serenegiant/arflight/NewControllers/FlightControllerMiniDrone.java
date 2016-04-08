package com.serenegiant.arflight.NewControllers;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.configs.ARNetworkConfig;

/**
 * Created by saki on 16/04/08.
 */
public class FlightControllerMiniDrone extends FlightController {
	public FlightControllerMiniDrone(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
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
