package com.serenegiant.arflight.attribute;

/**
 * GPSのバージョン
 */
public class AttributeGPS extends AttributeVersion {
	private volatile int numGpsSatellite;
	private volatile boolean mFixed;

	public void setFixed(final boolean fixed) {
		mFixed = fixed;
	}

	public boolean fixed() {
		return mFixed;
	}

	public void numGpsSatellite(final int num) {
		numGpsSatellite = num;
	}

	public int numGpsSatellite() {
		return numGpsSatellite;
	}
}
