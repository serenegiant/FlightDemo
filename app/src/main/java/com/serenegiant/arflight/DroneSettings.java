package com.serenegiant.arflight;

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

	protected final AttributeFloat mMaxAltitude = new AttributeFloat();
	protected final AttributeFloat mMaxTilt = new AttributeFloat();
	protected final AttributeFloat mMaxVerticalSpeed = new AttributeFloat();
	protected final AttributeFloat mMaxRotationSpeed = new AttributeFloat();

	public void setMaxAltitude(final float current, final float min, final float max) {
		mMaxAltitude.current = current;
		mMaxAltitude.min = min;
		mMaxAltitude.max = max;
	}
	public AttributeFloat maxAltitude() {
		return mMaxAltitude;
	}

	public void setMaxTilt(final float current, final float min, final float max) {
		mMaxTilt.current = current;
		mMaxTilt.min = min;
		mMaxTilt.max = max;
	}
	public AttributeFloat maxTilt() {
		return mMaxTilt;
	}

	public void setMaxVerticalSpeed(final float current, final float min, final float max) {
		mMaxVerticalSpeed.current = current;
		mMaxVerticalSpeed.min = min;
		mMaxVerticalSpeed.max = max;
	}
	public AttributeFloat maxVerticalSpeed() {
		return mMaxVerticalSpeed;
	}

	public void setMaxRotationSpeed(final float current, final float min, final float max) {
		mMaxRotationSpeed.current = current;
		mMaxRotationSpeed.min = min;
		mMaxRotationSpeed.max = max;
	}
	public AttributeFloat maxRotationSpeed() {
		return mMaxRotationSpeed;
	}

}
