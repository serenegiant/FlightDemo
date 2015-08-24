package com.serenegiant.arflight;

import com.parrot.arsdk.arsal.ARNativeData;

public class ARFrame extends ARNativeData {

	public static final int ARFRAME_DEFAULT_CAPACITY = 60000;

	private boolean mIsAvaiable;
	private boolean mIsIFrame;
	private int mMissed;

	public ARFrame() {
		super(ARFRAME_DEFAULT_CAPACITY);
		initialize();
	}

	public ARFrame(final int defaultCapacity) {
		super(defaultCapacity);
		initialize ();
	}

	public ARFrame(final long data, final int dataCapacity, final int dataSize, final boolean isIFrame, final int missed) {
		super(data, dataCapacity);
		initialize ();
		setUsedSize(dataSize);
		mMissed = missed;
		mIsIFrame = isIFrame;
	}

	private void initialize() {
		mMissed = 0;
		mIsIFrame = false;
		mIsAvaiable = true;
	}

	public int getMissed()
	{
		return mMissed;
	}

	public void setMissed(final int missed) {
		mMissed = missed;
	}

	public boolean isIFrame() {
		return mIsIFrame;
	}

	public void isIFrame(final boolean isIFrame) {
		mIsIFrame = isIFrame;
	}

	public boolean isAvailable() {
		return mIsAvaiable;
	}

	public void setAvailable (final boolean available) {
		mIsAvaiable = available;
	}

	@Override
	public String toString() {
		return String.format("ARFrame(avaiable=%b,I-Frame=%b,missed=%d,capacity=%d)", mIsAvaiable, mIsIFrame, mMissed, capacity);
	}
}
