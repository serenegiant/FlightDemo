package com.serenegiant.arflight;

import com.serenegiant.arflight.attribute.AttributeCamera;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeTimeLapse;

public class DroneSettings {
	protected String mCountryCode;
	public void setCountryCode(final String code) {
		mCountryCode = code;
	}
	public String countryCode() {
		return mCountryCode;
	}

	protected boolean mAutomaticCountry;
	public void setAutomaticCountry(final boolean auto) {
		mAutomaticCountry = auto;
	}
	public boolean automaticCountry() {
		return mAutomaticCountry;
	}

	protected boolean mCutOffMode;
	public void setCutOffMode(final boolean cutoff_mode) {
		mCutOffMode = cutoff_mode;
	}
	public boolean cutOffMode() {
		return mCutOffMode;
	}

	protected boolean mAutoTakeOffMode;
	public void setAutoTakeOffMode(final boolean auto_takeoff) {
		mAutoTakeOffMode = auto_takeoff;
	}
	public boolean autoTakeOffMode() {
		return mAutoTakeOffMode;
	}

	protected boolean mHasGuard;
	public void setHasGuard(final boolean has_guard) {
		mHasGuard = has_guard;
	}
	public boolean hasGuard() {
		return mHasGuard;
	}

	/** 室外モードか室内モードかを受信した時 */
	private boolean mOutdoorMode;

	public void outdoorMode(final boolean outdoor_mode) {
		mOutdoorMode = outdoor_mode;
	}

	public boolean outdoorMode() {
		return mOutdoorMode;
	}

	// カメラ設定
	protected final AttributeCamera mCamera = new AttributeCamera();
	public AttributeCamera getCamera() {
		return mCamera;
	}

	public void setCameraSettings(final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
		mCamera.setSettings(fov, panMax, panMin, tiltMax, tiltMin);
	}

	public AttributeFloat cameraTilt() {
		return mCamera.tilt();
	}
	public void currentCameraTilt(final float tilt) {
		mCamera.tilt().current(tilt);
	}
	public float currentCameraTilt() {
		return mCamera.tilt().current();
	}

	public AttributeFloat cameraPan() {
		return mCamera.pan();
	}
	public void currentCameraPan(final float pan) {
		mCamera.pan().current(pan);
	}
	public float currentCameraPan() {
		return mCamera.pan().current();
	}

	public void autoWhiteBalance(final int auto_white_balance) {
		mCamera.autoWhiteBalance(auto_white_balance);
	}
	public int autoWhiteBalance() {
		return mCamera.autoWhiteBalance();
	}

	public void setExposure(final float current, final float min, final float max) {
		mCamera.setExposure(current, min, max);
	}
	public AttributeFloat exposure() {
		return mCamera.exposure();
	}

	public void setSaturation(final float current, final float min, final float max) {
		mCamera.setSaturation(current, min, max);
	}
	public AttributeFloat saturation() {
		return mCamera.saturation();
	}

	public void setTimeLapse(final boolean enabled, final float current, final float min, final float max) {
		mCamera.setTimeLapse(enabled, current, min, max);
	}
	public AttributeTimeLapse timeLapse() {
		return mCamera.timeLapse();
	}

	// 操縦設定
	protected final AttributeFloat mMaxAltitude = new AttributeFloat();
	protected final AttributeFloat mMaxTilt = new AttributeFloat();
	protected final AttributeFloat mMaxVerticalSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxRotationSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxDistance = new AttributeFloat();

	public void setMaxAltitude(final float current, final float min, final float max) {
		mMaxAltitude.set(current, min, max);
	}
	public AttributeFloat maxAltitude() {
		return mMaxAltitude;
	}

	public void setMaxTilt(final float current, final float min, final float max) {
		mMaxTilt.set(current, min, max);
	}
	public AttributeFloat maxTilt() {
		return mMaxTilt;
	}

	public void setMaxVerticalSpeed(final float current, final float min, final float max) {
		mMaxVerticalSpeed.set(current, min, max);
	}
	public AttributeFloat maxVerticalSpeed() {
		return mMaxVerticalSpeed;
	}

	public void setMaxRotationSpeed(final float current, final float min, final float max) {
		mMaxRotationSpeed.set(current, min, max);
	}
	public AttributeFloat maxRotationSpeed() {
		return mMaxRotationSpeed;
	}

	public void setMaxDistance(final float current, final float min, final float max) {
		mMaxDistance.set(current, min, max);
	}
	public AttributeFloat maxDistance() {
		return mMaxDistance;
	}
}
