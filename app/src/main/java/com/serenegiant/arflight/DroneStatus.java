package com.serenegiant.arflight;

public class DroneStatus {
	public static final int STATE_FLYING_LANDED = 0x0000;	// FlyingState=0
	public static final int STATE_FLYING_TAKEOFF = 0x0100;	// FlyingState=1
	public static final int STATE_FLYING_HOVERING = 0x0200;	// FlyingState=2
	public static final int STATE_FLYING_FLYING = 0x0300;	// FlyingState=3
	public static final int STATE_FLYING_LANDING = 0x0400;	// FlyingState=4
	public static final int STATE_FLYING_EMERGENCY = 0x0500;// FlyingState=5
	public static final int STATE_FLYING_ROLLING = 0x0600;	// FlyingState=6

	public static final int ALARM_NON = 0;
	public static final int ALARM_USER_EMERGENCY = 1;
	public static final int ALARM_CUTOUT = 2;
	public static final int ALARM_BATTERY_CRITICAL = 3;
	public static final int ALARM_BATTERY = 4;
	public static final int ALARM_TOO_MUCH_ANGLE = 5;

	public static final int ALARM_DISCONNECTED = 100;

	private final Object mStateSync = new Object();
	private final Object mSync = new Object();

	private int mFlyingState = 0;
	private int mAlarmState = ALARM_NON;
	private int mBatteryState = -1;
	private final AttributeMotor[] mMotors;


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

	private AttributePosition mPosition = new AttributePosition();

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setPosition(final double latitude, final double longitude, final double altitude) {
		synchronized (mSync) {
			mPosition.set(latitude, longitude, altitude);
		}
	}

	/** 経度をセット */
	public void latitude(final double latitude) {
		synchronized (mSync) {
			mPosition.latitude(latitude);
		}
	}
	/** 緯度を取得[度] */
	public double latitude() {
		synchronized (mSync) {
			return mPosition.latitude();
		}
	}

	/** 経度をセット */
	public void longitude(final double longitude) {
		synchronized (mSync) {
			mPosition.longitude(longitude);
		}
	}
	/** 経度を取得[度] */
	public double longitude() {
		synchronized (mSync) {
			return mPosition.longitude();
		}
	}

	/** 高度[m]を設定  */
	public void altitude(final double altitude) {
		synchronized (mSync) {
			mPosition.altitude(altitude);
		}
	}
	/** 高度[m]を取得 */
	public double altitude() {
		synchronized (mSync) {
			return mPosition.altitude();
		}
	}

	private Vector mSpeed = new Vector();		// 移動速度[m/s]
	private Vector mAttitude = new Vector();	// 機体姿勢[度]

	/**
	 * 機体の移動速度設定(ParrotのSDKから返ってくる値とxy順番、zの符号が違うので注意)
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
	 * 機体の移動速度設定(ParrotのSDKから返ってくる値とxyの順番、zの符号が違うので注意)
 	 * @return
	 */
	public Vector speed() {
		synchronized (mSync) {
			return mSpeed;
		}
	}

	/**
	 * 機体姿勢をセット
	 * @param roll
	 * @param pitch
	 * @param yaw
	 */
	public void setAttitude(final float roll, final float pitch, final float yaw) {
		synchronized (mSync) {
			mAttitude.set(roll, pitch, yaw);
		}
	}

	/**
	 * 機体姿勢を取得
	 * @return Vector(x=roll, y=pitch, z=yaw)
	 */
	public Vector attitude() {
		synchronized (mSync) {
			return mAttitude;
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

	/** 異常状態をセット */
	public void setAlarm(final int alarm_state) {
		synchronized (mStateSync) {
			if (mAlarmState != alarm_state) {
				mAlarmState = alarm_state;
			}
		}
	}

	/** 異常状態を取得 */
	public int getAlarm() {
		synchronized (mStateSync) {
			return mAlarmState;
		}
	}

	/** バッテリー残量をセット */
	public void setBattery(final int battery_state) {
		synchronized (mStateSync) {
			mBatteryState = battery_state;
		}
	}

	/** バッテリー残量を取得 */
	public int getBattery() {
		synchronized (mStateSync) {
			return mBatteryState;
		}
	}

	/** 機体と接続しているかどうかを取得 */
	public boolean isConnected() {
		synchronized (mStateSync) {
			return (mAlarmState != ALARM_DISCONNECTED);
		}
	}
}
