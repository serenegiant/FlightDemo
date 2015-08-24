package com.serenegiant.arflight;

public class AttributeCamera {
	private final Object mSync = new Object();

	/** ホワイトバランス:手動 */
	public static final int WHITE_BALANCE_MANUAL = -1;
	/** ホワイトバランス: 自動 */
	public static final int WHITE_BALANCE_AUTO = 0;
	/** ホワイトバランス:電球色 */
	public static final int WHITE_BALANCE_LAMP = 1;
	/** ホワイトバランス:晴天用 */
	public static final int WHITE_BALANCE_DAYLIGHT = 2;
	/** ホワイトバランス:曇り空用 */
	public static final int WHITE_BALANCE_CLOUDY = 3;
	/** ホワイトバランス:フラッシュ撮影用 */
	public static final int WHITE_BALANCE_FLASH = 4;
	/**
	 * オートホワイトバランス設定<br>
	 * WHITE_BALANCE_MANUAL, WHITE_BALANCE_AUTO, WHITE_BALANCE_LAMP,
	 * WHITE_BALANCE_DAYLIGHT, WHITE_BALANCE_CLOUDY, WHITE_BALANCE_FLASH
	 */
	private int mAutoWhiteBalance = WHITE_BALANCE_AUTO;
	/** 視野角[度] */
	private float mFov;
	/** パンコントロール */
	private AttributeFloat mTilt = new AttributeFloat();
	/** ティルトコントロール */
	private AttributeFloat mPan = new AttributeFloat();

	public void setSettings(final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
		synchronized (mSync) {
			mFov = fov;
		}
		mTilt.set(mTilt.current(), tiltMin, tiltMax);
		mPan.set(mPan.current(), panMin, panMax);
	}

	public void set(final float tilt, final float pan) {
		mTilt.current(tilt);
		mPan.current(pan);
	}

	public float fov() {
		synchronized (mSync) {
			return mFov;
		}
	}

	public AttributeFloat tilt() {
		return mTilt;
	}

	public AttributeFloat pan() {
		return mPan;
	}

	public void autoWhiteBalance(final int auto_white_balance) {
		synchronized (mSync) {
			mAutoWhiteBalance = auto_white_balance;
		}
	}

	public int autoWhiteBalance() {
		synchronized (mSync) {
			return mAutoWhiteBalance;
		}
	}

	/** 露出設定 */
	private final AttributeFloat mExposure = new AttributeFloat();

	public void setExposure(final float current, final float min, final float max) {
		mExposure.set(current, min, max);
	}

	public AttributeFloat exposure() {
		return mExposure;
	}

	/** 彩度設定 */
	private final AttributeFloat mSaturation = new AttributeFloat();

	public void setSaturation(final float current, final float min, final float max) {
		mSaturation.set(current, min, max);
	}

	public AttributeFloat saturation() {
		return mSaturation;
	}

	/** タイムラプス設定 */
	private final AttributeTimeLapse mTimeLapse = new AttributeTimeLapse();

	public void setTimeLapse(final boolean enabled, final float current, final float min, final float max) {
		mTimeLapse.set(enabled, current, min, max);
	}

	public AttributeTimeLapse timeLapse() {
		return mTimeLapse;
	}
}