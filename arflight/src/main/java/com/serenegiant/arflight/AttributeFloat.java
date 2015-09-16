package com.serenegiant.arflight;

/**
 * 浮動小数点の設定
 */
public class AttributeFloat {
	private float mCurrent;
	private float mMin;
	private float mMax;

	public synchronized void set(final float current, final float min, final float max) {
		mCurrent = current;
		mMin = min;
		mMax = max;
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
		return String.format("AttributeFloat{%f/%f/%f)}", mMin, mCurrent, mMax);
	}
}
