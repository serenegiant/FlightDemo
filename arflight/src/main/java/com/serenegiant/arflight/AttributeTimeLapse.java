package com.serenegiant.arflight;

public class AttributeTimeLapse {
	private boolean mEnabled;
	private float mCurrent;
	private float mMin;
	private float mMax;

	public synchronized void set(final boolean enabled, final float current, final float min, final float max) {
		mEnabled = enabled;
		mCurrent = current;
		mMin = min;
		mMax = max;
	}

	public synchronized boolean enabled() {
		return mEnabled;
	}

	public synchronized void current(final float current) {
		mCurrent = current;
	}

	public synchronized float current() {
		return mCurrent;
	}

	public synchronized float min() {
		return mMin;
	}

	public synchronized float max() {
		return mMax;
	}

	@Override
	public String toString() {
		return String.format("AttributeTimeLapse{%f/%f/%f)}", mMin, mCurrent, mMax);
	}
}
