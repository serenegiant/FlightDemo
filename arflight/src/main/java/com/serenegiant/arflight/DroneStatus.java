package com.serenegiant.arflight;

import android.util.SparseArray;

import com.serenegiant.arflight.attribute.AttributeCalibration;
import com.serenegiant.arflight.attribute.AttributeFlightDuration;
import com.serenegiant.arflight.attribute.AttributeMassStorage;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.attribute.AttributePosition;
import com.serenegiant.math.Vector;

public class DroneStatus extends CommonStatus {
	public static final int STATE_FLYING_LANDED = 0x0000;	// FlyingState=0
	public static final int STATE_FLYING_TAKEOFF = 0x0100;	// FlyingState=1
	public static final int STATE_FLYING_HOVERING = 0x0200;	// FlyingState=2
	public static final int STATE_FLYING_FLYING = 0x0300;	// FlyingState=3
	public static final int STATE_FLYING_LANDING = 0x0400;	// FlyingState=4
	public static final int STATE_FLYING_EMERGENCY = 0x0500;// FlyingState=5
	public static final int STATE_FLYING_ROLLING = 0x0600;	// FlyingState=6
	public static final int STATE_FLYING_MASK = STATE_FLYING_TAKEOFF | STATE_FLYING_HOVERING | STATE_FLYING_FLYING | STATE_FLYING_LANDING | STATE_FLYING_ROLLING;

	/** 動画/静止画撮影不可 */
	public static final int MEDIA_UNAVAILABLE = -1;
	/** 動画/静止画撮影可能 */
	public static final int MEDIA_READY = 0;
	/** 動画/静止画撮影中 */
	public static final int MEDIA_BUSY = 1;
	public static final int MEDIA_SUCCESS = 2;
	public static final int MEDIA_ERROR = 9;
//	MEDIA_READY => MEDIA_BUSY => MEDIA_SUCCESS => MEDIA_READY
//	MEDIA_READY => MEDIA_BUSY => MEDIA_ERROR => MEDIA_READY

	private int mFlyingState = 0;
	private final AttributeMotor[] mMotors;
	private int mStillCaptureState = MEDIA_UNAVAILABLE;
	private int mVideoRecordingState = MEDIA_UNAVAILABLE;

	public DroneStatus(final int motor_num) {
		mMotors = new AttributeMotor[motor_num];
		for (int i = 0; i < motor_num; i++) {
			mMotors[i] = new AttributeMotor();
		}
	}

	public int getMotorNums() {
		return mMotors.length;
	}

	public AttributeMotor getMotor(final int index) {
		synchronized (mSync) {
			final int n = mMotors.length;
			if ((index >= 0) && (index < n)) {
				return mMotors[index];
			}
			return null;
		}
	}

	/** 移動速度[m/s] */
	private Vector mSpeed = new Vector();

	/**
	 * 機体の移動速度(ParrotのSDKから返ってくる値とxy順番、zの符号が違うので注意)<br>
	 * GPS座標から計算しているみたいなのでGPSを受信してないと0しか返ってこない
	 * @param x 左右方向の移動速度[m/s] (正:右)
	 * @param y 前後方向の移動速度[m/s] (正:前進)
	 * @param z 上下方向の移動速度[m/s] (正:上昇)
	 */
	public void setSpeed(final float x, final float y, final float z) {
		synchronized (mSync) {
			mSpeed.set(x, y, z);
		}
	}

	/**
	 * 機体の移動速度設定(ParrotのSDKから返ってくる値とxyの順番、zの符号が違うので注意)<br>
	 * GPS座標から計算しているみたいなのでGPSを受信してないと0しか返ってこない
 	 * @return
	 */
	public Vector speed() {
		synchronized (mSync) {
			return mSpeed;
		}
	}

	/** 機体姿勢[ラジアン] */
	private Vector mAttitude = new Vector();

	/**
	 * 機体姿勢をセット
	 * @param roll ラジアン
	 * @param pitch ラジアン
	 * @param yaw ラジアン
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw) {
		synchronized (mSync) {
			mAttitude.set(roll, pitch, yaw);
		}
	}

	/**
	 * 機体姿勢を取得(ラジアン)
	 * @return Vector(x=roll, y=pitch, z=yaw)
	 */
	public Vector attitude() {
		synchronized (mSync) {
			return mAttitude;
		}
	}

	/**
	 * 飛行回数, 飛行時間, 合計飛行時間
	 */
	private AttributeFlightDuration mAttributeFlightDuration = new AttributeFlightDuration();

	public AttributeFlightDuration setFlightDuration(final int counts, final int duration, final int total) {
		synchronized (mSync) {
			return mAttributeFlightDuration.set(counts, duration, total);
		}
	}

	/**
	 * 飛行回数を取得
	 * @return
	 */
	public int flightCounts() {
		synchronized (mSync) {
			return mAttributeFlightDuration.counts();
		}
	}

	/**
	 * 飛行時間を取得
	 * @return
	 */
	public int flightDuration() {
		synchronized (mSync) {
			return mAttributeFlightDuration.duration();
		}
	}

	/**
	 * 合計飛行時間を取得
	 * @return
	 */
	public int flightTotalDuration() {
		synchronized (mSync) {
			return mAttributeFlightDuration.total();
		}
	}

	private int mCurrentMassStorageId;
	private final SparseArray<AttributeMassStorage> mMassStorage = new SparseArray<AttributeMassStorage>();

	/**
	 * マスストレージIDをセット
	 */
	public void setMassStorage(final int mass_storage_id, final String mass_storage_name) {
		synchronized (mSync) {
			AttributeMassStorage storage = mMassStorage.get(mass_storage_id);
			if (storage == null) {
				storage = new AttributeMassStorage();
				storage.mMassStorageId = mass_storage_id;
				storage.mMassStorageName = mass_storage_name;
				mMassStorage.append(mass_storage_id, storage);
			}
			mCurrentMassStorageId = mass_storage_id;
		}
	}

	/**
	 * マスストレージの状態をセット
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 * @return
	 */
	public boolean setMassStorageInfo(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
		boolean result = false;
		synchronized (mSync) {
			final AttributeMassStorage storage = mMassStorage.get(mass_storage_id);
			if (storage != null) {
				result = (storage.size != size) || (storage.used_size != used_size)
					|| (storage.plugged != plugged) || (storage.full != full) || (storage.internal != internal);
				if (result) {
					storage.size = size;
					storage.used_size = used_size;
					storage.plugged = plugged;
					storage.full = full;
					storage.internal = internal;
				}
			} else {
				result = true;
			}
		}
		return result;
	}

	/**
	 * マスストレージIDを取得
	 * @return
	 */
	public int massStorageId() {
		synchronized (mSync) {
			return mCurrentMassStorageId;
		}
	}

	/**
	 * マスストレージ名を取得
	 * @return
	 */
	public String massStorageName() {
		return massStorageName(mCurrentMassStorageId);
	}
	/**
	 * マスストレージ名を取得
	 * @param mass_storage_id
	 * @return
	 */
	public String massStorageName(final int mass_storage_id) {
		synchronized (mSync) {
			final AttributeMassStorage storage = mMassStorage.get(mass_storage_id);
			return storage != null ? storage.mMassStorageName : null;
		}
	}


//********************************************************************************
//********************************************************************************
	/** 飛行状態をセット */
	public void setFlyingState(final int flying_sate) {
		synchronized (mStateSync) {
			if (mFlyingState != flying_sate) {
				mFlyingState = flying_sate;
			}
		}
	}

	/** 飛行状態を取得 */
	public int getFlyingState() {
		synchronized (mStateSync) {
			return mFlyingState;
		}
	}

	public boolean isFlying() {
		synchronized (mStateSync) {
			return (mAlarmState == ALARM_NON) && ((mFlyingState & STATE_FLYING_MASK) != 0);
		}
	}

	/** 静止画撮影ステータス */
	public boolean setStillCaptureState(final int state) {
		synchronized (mStateSync) {
			final boolean result = mStillCaptureState != state;
			mStillCaptureState = state;
			return result;
		}
	}

	/** 静止画撮影ステータス */
	public int getStillCaptureState() {
		synchronized (mStateSync) {
			return mStillCaptureState;
		}
	}

	/** 静止画撮影可能かどうか */
	public boolean isStillCaptureReady() {
		synchronized (mStateSync) {
			return mStillCaptureState == MEDIA_READY;
		}
	}

	/** 動画撮影ステータス */
	public boolean setVideoRecordingState(final int state) {
		synchronized (mStateSync) {
			final boolean result = mVideoRecordingState != state;
			mVideoRecordingState = state;
			return result;
		}
	}

	/** 動画撮影ステータス */
	public int getVideoRecordingState() {
		synchronized (mStateSync) {
			return mVideoRecordingState;
		}
	}

	/** 動画撮影可能かどうか */
	public boolean isVideoRecordingReady() {
		synchronized (mStateSync) {
			return mVideoRecordingState == MEDIA_READY;
		}
	}

}
