package com.serenegiant.arflight.attribute;

public class AttributeVersion {
	protected String mSoftware;
	protected String mHardware;

	public void set(final String software, final String hardware) {
		mSoftware = software;
		mHardware = hardware;
	}

	public String software() {
		return mSoftware;
	}

	public String hardware() {
		return mHardware;
	}
}
