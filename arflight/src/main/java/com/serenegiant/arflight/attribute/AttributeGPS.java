package com.serenegiant.arflight.attribute;

/**
 * GPSのバージョン
 */
public class AttributeGPS extends AttributeVersion {
	public int numGpsSatellite;
	private boolean mFixed;

	public void setFixed(final boolean fixed) {
		mFixed = fixed;
	}

	public boolean fixed() {
		return mFixed;
	}
}
