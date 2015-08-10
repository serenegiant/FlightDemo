package com.serenegiant.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.sql.Date;

public interface IDeviceController {
	// フリップアクションの種類
	public static final int FLIP_FRONT = 1;
	public static final int FLIP_BACK = 2;
	public static final int FLIP_RIGHT = 3;
	public static final int FLIP_LEFT = 4;

	// センサーの種類
	/** 慣性測定(ジャイロ/加速度) */
	public static final int SENSOR_IMU = 0;
	/** 高度計(気圧計) */
	public static final int SENSOR_BAROMETER = 1;
	/** 高度計(超音波) */
	public static final int SENSOR_ULTRASOUND = 2;
	/** GPS */
	public static final int SENSOR_GPS = 3;
	/** 磁気センサー(コンパス/姿勢) */
	public static final int SENSOR_MAGNETOMETER= 4;
	/** 垂直カメラ(対地速度検出) */
	public static final int SENSOR_VERTICAL_CAMERA = 5;

	public static final int STATE_MASK_CONNECTION = 0x00ff;
	public static final int STATE_MASK_FLYING = 0xff00;

	public static final int STATE_STOPPED = 0x0000;
	public static final int STATE_STARTING = 0x0001;
	public static final int STATE_STARTED = 0x0002;
	public static final int STATE_STOPPING = 0x0003;

	public static final int STATE_FLYING_LANDED = 0x0000;		// FlyingState=0
	public static final int STATE_FLYING_TAKEOFF = 0x0100;		// FlyingState=1
	public static final int STATE_FLYING_HOVERING = 0x0200;		// FlyingState=2
	public static final int STATE_FLYING_FLYING = 0x0300;		// FlyingState=3
	public static final int STATE_FLYING_LANDING = 0x0400;		// FlyingState=4
	public static final int STATE_FLYING_EMERGENCY = 0x0500;	// FlyingState=5
	public static final int STATE_FLYING_ROLLING = 0x0600;		// FlyingState=6

	public static final int ALARM_NON = 0;
	public static final int ALARM_USER_EMERGENCY = 1;
	public static final int ALARM_CUTOUT = 2;
	public static final int ALARM_BATTERY_CRITICAL = 3;
	public static final int ALARM_BATTERY = 4;
	public static final int ALARM_DISCONNECTED = 100;

	/**
	 * 機体名を取得, ローリングスパイダーだとrs_xxxxxって奴
	 * @return
	 */
	public String getName();

	/**
	 * 機体のソフトウエアバージョンを取得
	 * @return
	 */
	public String getSoftwareVersion();

	/**
	 * 機体のハードウエアバージョンを取得
	 * @return
	 */
	public String getHardwareVersion();

	/**
	 * 機体のシリアル番号を取得
	 * @return
	 */
	public String getSerial();

	/**
	 * コントローラーに関連付けられているARDiscoveryDeviceServiceを取得
	 * @return
	 */
	public ARDiscoveryDeviceService getDeviceService();

	public ARNetworkConfig getNetConfig();

	/**
	 * バッテリーの残量を取得
	 * @return バッテリー残量[%]
	 */
	public int getBattery();

	/**
	 * コールバックリスナーを追加
	 * @param mListener
	 */
	public void addListener(final DeviceConnectionListener mListener);

	/**
	 * コールバックリスナーを除去
	 * @param mListener
	 */
	public void removeListener(final DeviceConnectionListener mListener);

	/**
	 * 下位2バイトは接続ステータス, その上2バイトは飛行ステータス
	 * @return
	 */
	public int getState();
	public int getAlarm();

	public boolean start();

	/**
	 * 接続処理を中断
	 */
	public void cancelStart();
	public void stop();

	/**
	 * startしているかどうか
	 * @return
	 */
	public boolean isStarted();

	/**
	 * 接続されているかどうか
	 * @return
	 */
	public boolean isConnected();

	public boolean sendDate(Date currentDate);
	public boolean sendTime(Date currentDate);

	/**
	 * 離陸指示
	 * @return
	 */
	public boolean sendTakeoff();

	/**
	 * 着陸指示
	 * @return
	 */
	public boolean sendLanding();

	/**
	 * 非常停止指示
	 * @return
	 */
	public boolean sendEmergency();

	/**
	 * 全ての設定要求?
	 * @return
	 */
	public boolean sendAllSettings();

	/**
	 * すべての状態を要求?
	 * @return
	 */
	public boolean sendAllStates();

	/**
	 * フラットトリム実行(姿勢センサー調整)
	 * @return
	 */
	public boolean sendFlatTrim();


	/**
	 * 最大高度を設定
	 * @param altitude [m]
	 * @return
	 */
	public boolean sendMaxAltitude(final float altitude);
	public AttributeFloat getMaxAltitude();

	/**
	 * 最大傾斜設定
	 * @param tilt
	 * @return
	 */
	public boolean sendMaxTilt(final float tilt);
	public AttributeFloat getMaxTilt();

	/**
	 * 最大上昇/下降速度を設定
	 * @param speed m/s
	 * @return
	 */
	public boolean sendMaxVerticalSpeed(final float speed);
	public AttributeFloat getMaxVerticalSpeed();

		/**
		 * 最大回転速度
		 * @param speed [度/秒]
		 * @return
		 */
	public boolean sendMaxRotationSpeed(final float speed);
	public AttributeFloat getMaxRotationSpeed();

	/**
	 * モーターの個数を返す
	 * @return
	 */
	public int getMotorNums();
	public AttributeMotor getMotor(final int index);

	/**
	 * roll/pitch変更時が移動なのか機体姿勢変更なのかを指示
	 * @param flag 1:移動, 0:機体姿勢変更
	 */
	public void setFlag(final byte flag);
	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setGaz(final byte gaz);
	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する, flag=0:機体姿勢変更のみ
	 * @param roll 負:左, 正:右, -100〜+100
	 */
	public void setRoll(final byte roll);
	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する, flag=0:機体姿勢変更のみ
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setPitch(final byte pitch);
	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setYaw(final byte yaw);
	/**
	 * 北磁極に対する角度を設定・・・でもローリングスパイダーでは動かない
	 * @param heading -360〜360度
	 */
	public void setHeading(final int heading);
}
