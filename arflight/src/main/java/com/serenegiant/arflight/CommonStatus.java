package com.serenegiant.arflight;

import com.serenegiant.arflight.attribute.AttributeCalibration;
import com.serenegiant.arflight.attribute.AttributePosition;

/**
 * Created by saki on 16/02/11.
 */
public class CommonStatus {
	public static final int ALARM_NON = 0;
	public static final int ALARM_USER_EMERGENCY = 1;
	public static final int ALARM_CUTOUT = 2;
	public static final int ALARM_BATTERY_CRITICAL = 3;
	public static final int ALARM_BATTERY = 4;
	public static final int ALARM_TOO_MUCH_ANGLE = 5;
	public static final int ALARM_DISCONNECTED = 100;		// 切断, これはアプリ内のみで有効

	protected final Object mStateSync = new Object();
	protected final Object mSync = new Object();
	protected int mAlarmState = ALARM_NON;
	protected int mBatteryState = -1;
	protected int mWiFiSignalRssi = 0;

	private AttributePosition mPosition = new AttributePosition();
	private AttributePosition mHomePosition = new AttributePosition();

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

	/**
	 * WiFi信号強度をセット
	 * @param rssi
	 */
	public void setWiFiSignal(final int rssi) {
		synchronized (mStateSync) {
			mWiFiSignalRssi = rssi;
		}
	}

	/**
	 * WiFi信号強度を取得
	 * @return
	 */
	public int getWiFiSignal() {
		synchronized (mStateSync) {
			return mWiFiSignalRssi;
		}
	}

	/** 機器と接続しているかどうかを取得 */
	public boolean isConnected() {
		synchronized (mStateSync) {
			return (mAlarmState != ALARM_DISCONNECTED);
		}
	}

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setPosition(final double latitude, final double longitude, final double altitude) {
		synchronized (mSync) {
			mPosition.set(latitude, longitude, altitude, 0.0);
		}
	}

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setPosition(final double latitude, final double longitude, final double altitude, final double heading) {
		synchronized (mSync) {
			mPosition.set(latitude, longitude, altitude, heading);
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

	/** 方位角[度]を設定 */
	public void heading(final double heading) {
		synchronized (mSync) {
			mPosition.heading(heading);
		}
	}

	/** 方位角[度]を取得 */
	public double heading() {
		synchronized (mSync) {
			return mPosition.heading();
		}
	}

	/**
	 * 座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void setHome(final double latitude, final double longitude, final double altitude) {
		synchronized (mSync) {
			mHomePosition.set(latitude, longitude, altitude);
		}
	}

	/** 経度をセット */
	public void homeLatitude(final double latitude) {
		synchronized (mSync) {
			mHomePosition.latitude(latitude);
		}
	}
	/** 緯度を取得[度] */
	public double homeLatitude() {
		synchronized (mSync) {
			return mHomePosition.latitude();
		}
	}

	/** 経度をセット */
	public void homeLongitude(final double longitude) {
		synchronized (mSync) {
			mHomePosition.longitude(longitude);
		}
	}
	/** 経度を取得[度] */
	public double homeLongitude() {
		synchronized (mSync) {
			return mHomePosition.longitude();
		}
	}

	/** 高度[m]を設定  */
	public void homeAltitude(final double altitude) {
		synchronized (mSync) {
			mHomePosition.altitude(altitude);
		}
	}
	/** 高度[m]を取得 */
	public double homeAltitude() {
		synchronized (mSync) {
			return mHomePosition.altitude();
		}
	}

	private AttributeCalibration mAttributeCalibration = new AttributeCalibration();

	/** 機器のキャリブレーション状態を設定 */
	public void updateCalibrationState(final boolean x, final boolean y, final boolean z, final boolean failed) {
		mAttributeCalibration.update(x, y, z, failed);
	}

	/** 機体のキャリブレーションが必要かどうかを設定 */
	public void needCalibration(final boolean need_calibration) {
		mAttributeCalibration.needCalibration(need_calibration);
	}

	/** 機体のキャリブレーションが必要かどうかを取得 */
	public boolean needCalibration() {
		return mAttributeCalibration.needCalibration();
	}

}
