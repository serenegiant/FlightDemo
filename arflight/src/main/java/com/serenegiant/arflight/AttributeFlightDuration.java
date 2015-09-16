package com.serenegiant.arflight;

public class AttributeFlightDuration {
	private int mFlightCounts;			// 飛行回数
	private int mFlightDuration;		// 飛行時間[秒]
	private int mTotalFlightDuration;	// 合計飛行時間[秒]


	public AttributeFlightDuration set(final AttributeFlightDuration other) {
		mFlightCounts = other != null ? other.mFlightCounts : 0;
		mFlightDuration = other != null ? other.mFlightDuration : 0;
		mTotalFlightDuration = other != null ? other.mTotalFlightDuration : 0;
		return this;
	}

	public AttributeFlightDuration set(final int counts, final int duration, final int total) {
		mFlightCounts = counts;
		mFlightDuration = duration;
		mTotalFlightDuration = total;
		return this;
	}

	public void counts(final int counts) {
		mFlightCounts = counts;
	}

	public int counts() {
		return mFlightCounts;
	}

	public void duration(final int duration) {
		mFlightDuration = duration;
	}

	public int duration() {
		return mFlightDuration;
	}

	public void total(final int total) {
		mTotalFlightDuration = total;
	}

	public int total() {
		return mTotalFlightDuration;
	}
 }
