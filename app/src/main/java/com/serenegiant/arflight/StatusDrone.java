package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/22.
 */
public class StatusDrone {
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

	private final Object mSync = new Object();

	private int mFlyingState = 0;
	private int mAlarmState = ALARM_NON;
	/**
	 * 緯度[度] (500.0: 不明)
	 */
	double latitude = 500.0;
	/**
	 * 経度[度] (500.0: 不明)
	 */
	double longitude = 500.0;
	/**
	 * 高度[m]
	 */
	public double altitude;

	public void setFlyingState(final int flying_sate) {
		synchronized (mSync) {
			if (mFlyingState != flying_sate) {
				mFlyingState = flying_sate;
			}
		}
	}

	public int getFlyingState() {
		synchronized (mSync) {
			return mFlyingState;
		}
	}

	public void setAlarm(final int alarm_state) {
		synchronized (mSync) {
			if (mAlarmState != alarm_state) {
				mAlarmState = alarm_state;
			}
		}
	}

	public int getAlarm() {
		synchronized (mSync) {
			return mAlarmState;
		}
	}

	public boolean isConnected() {
		synchronized (mSync) {
			return (mAlarmState != ALARM_DISCONNECTED);
		}
	}
}
