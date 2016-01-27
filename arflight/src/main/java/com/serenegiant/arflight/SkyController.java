package com.serenegiant.arflight;

import android.content.Context;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.attribute.AttributeDevice;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.arflight.configs.ARNetworkConfigARDrone3;
import com.serenegiant.arflight.configs.ARNetworkConfigSkyController;

import java.sql.Date;

/**
 * Created by saki on 15/10/31.
 */
public class SkyController extends FlightController implements IVideoStreamController, IWiFiController {
	private static final boolean DEBUG = false;				// FIXME 実働時はfalseにすること
	private static final String TAG = SkyController.class.getSimpleName();

	public SkyController(final Context context, final ARDiscoveryDeviceService service) {
		super(context, service, new ARNetworkConfigSkyController());
		mInfo = new AttributeDevice();
		mSettings = new DroneSettings();
		mStatus = new DroneStatus(4);

		mSettings.setCutOffMode(true);
	}

	@Override
	public void addListener(DeviceConnectionListener mListener) {

	}

	@Override
	public void removeListener(DeviceConnectionListener mListener) {

	}

	@Override
	protected boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading) {
		return false;
	}

	@Override
	public int getState() {
		return 0;
	}

	@Override
	public void cancelStart() {

	}

	@Override
	public boolean isStarted() {
		return false;
	}

	@Override
	public boolean isConnected() {
		return false;
	}

	@Override
	public boolean sendDate(Date currentDate) {
		return false;
	}

	@Override
	public boolean sendTime(Date currentDate) {
		return false;
	}

	@Override
	public boolean sendAllSettings() {
		return false;
	}

	@Override
	public boolean sendAllStates() {
		return false;
	}

	@Override
	public boolean sendTakeoff() {
		return false;
	}

	@Override
	public boolean sendLanding() {
		return false;
	}

	@Override
	public boolean sendEmergency() {
		return false;
	}

	@Override
	public boolean sendFlatTrim() {
		return false;
	}

	@Override
	public boolean sendMaxAltitude(final float altitude) {
		return false;
	}

	@Override
	public boolean sendMaxTilt(final float tilt) {
		return false;
	}

	@Override
	public boolean sendMaxVerticalSpeed(final float speed) {
		return false;
	}

	@Override
	public boolean sendMaxRotationSpeed(final float speed) {
		return false;
	}

	@Override
	public boolean canGetAttitude() {
		return false;
	}

	@Override
	public int getMotorNums() {
		return 0;
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
	public boolean sendHasGuard(final boolean has_guard) {
		return false;
	}

	@Override
	public boolean sendAnimationsFlip(final int direction) {
		return false;
	}

	@Override
	public boolean sendAnimationsCap(final int degree) {
		return false;
	}

	@Override
	public boolean sendTakePicture(final int mass_storage_id) {
		return false;
	}

	@Override
	public boolean sendTakePicture() {
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id) {
		return false;
	}

	@Override
	public boolean sendVideoRecording(final boolean start) {
		return false;
	}

	@Override
	public void setVideoStream(final IVideoStream video_stream) {

	}

	@Override
	public boolean isVideoStreamingEnabled() {
		return false;
	}

	@Override
	public void enableVideoStreaming(final boolean enable) {

	}
}
