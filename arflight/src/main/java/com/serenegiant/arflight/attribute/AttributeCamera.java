package com.serenegiant.arflight.attribute;

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
	/** ティルトコントロール */
	private AttributeFloat mTilt = new AttributeFloat();
	private float mTiltDefault;
	/** パンコントロール */
	private AttributeFloat mPan = new AttributeFloat();
	private float mPanDefault;

	/** 自動録画設定 */
	private boolean enableAutoRecord;
	private int autoRecordMassStorageId;

	public void setSettings(final float fov, final float panMax, final float panMin, final float tiltMax, final float tiltMin) {
		synchronized (mSync) {
			mFov = fov;
			mTilt.set(mTilt.current(), tiltMin, tiltMax);
			mPan.set(mPan.current(), panMin, panMax);
		}
	}

	public float fov() {
		synchronized (mSync) {
			return mFov;
		}
	}

	public AttributeFloat tilt() {
		synchronized (mSync) {
			return mTilt;
		}
	}

	public float tiltDefault() {
		synchronized (mSync) {
			return mTiltDefault;
		}
	}

	public AttributeFloat pan() {
		synchronized (mSync) {
			return mPan;
		}
	}

	public float panDefault() {
		synchronized (mSync) {
			return mPanDefault;
		}
	}

	public void pantilt(final float pan, final float tilt) {
		synchronized (mSync) {
			mPan.current(pan);
			mTilt.current(tilt);
		}
	}

	public void pantiltDefault(final float pan, final float tilt) {
		synchronized (mSync) {
			mPanDefault = pan;
			mTiltDefault = tilt;
		}
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
		synchronized (mSync) {
			mExposure.set(current, min, max);
		}
	}

	public AttributeFloat exposure() {
		synchronized (mSync) {
			return mExposure;
		}
	}

	/** 彩度設定 */
	private final AttributeFloat mSaturation = new AttributeFloat();

	public void setSaturation(final float current, final float min, final float max) {
		synchronized (mSync) {
			mSaturation.set(current, min, max);
		}
	}

	public AttributeFloat saturation() {
		return mSaturation;
	}

	/** タイムラプス設定 */
	private final AttributeTimeLapse mTimeLapse = new AttributeTimeLapse();

	public void setTimeLapse(final boolean enabled, final float current, final float min, final float max) {
		synchronized (mSync) {
			mTimeLapse.set(enabled, current, min, max);
		}
	}

	public AttributeTimeLapse timeLapse() {
		synchronized (mSync) {
			return mTimeLapse;
		}
	}

	/**
	 * ビデオストリーミング状態<br>
	 * 0: Video streaming is enabled.<br>
	 * 1: Video streaming is disabled.<br>
	 * 2: Video streaming failed to start.<br>
	 */
	private int mVideoStreamingState;

	/**
	 * ビデオストリーミング状態
	 * @param state
	 * 0: Video streaming is enabled.
	 * 1: Video streaming is disabled.
	 * 2: Video streaming failed to start.
	 */
	public void videoStateState(final int state) {
		synchronized (mSync) {
			mVideoStreamingState = state;
		}
	}

	public int videoStateState() {
		synchronized (mSync) {
			return mVideoStreamingState;
		}
	}

	public boolean isVideoStreamingEnabled() {
		synchronized (mSync) {
			return mVideoStreamingState == 0;
		}
	}

	public void autoRecord(final boolean enable, final int mass_storage_id) {
		synchronized (mSync) {
			enableAutoRecord = enable;
			if (enable) {
				autoRecordMassStorageId = mass_storage_id;
			}
		}
	}

	public boolean isAutoRecordEnabled() {
		synchronized (mSync) {
			return enableAutoRecord;
		}
	}

	public int autoRecordMassStorageId() {
		synchronized (mSync) {
			return enableAutoRecord ? autoRecordMassStorageId : 0;
		}
	}
}