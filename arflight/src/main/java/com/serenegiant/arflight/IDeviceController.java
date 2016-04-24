package com.serenegiant.arflight;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.arnetwork.ARNetworkManager;
import com.parrot.arsdk.arnetworkal.ARNetworkALManager;
import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.sql.Date;

public interface IDeviceController {
	public static final int STATE_STOPPED = 0x0000;
	public static final int STATE_STARTING = 0x0001;
	public static final int STATE_STARTED = 0x0002;
	public static final int STATE_STOPPING = 0x0003;

//	ARCONTROLLER_DEVICE_STATE_ENUM.
//		ARCONTROLLER_DEVICE_STATE_STOPPED (0, "device controller is stopped"),
//		ARCONTROLLER_DEVICE_STATE_STARTING (1, "device controller is starting"),
//		ARCONTROLLER_DEVICE_STATE_RUNNING (2, "device controller is running"),
//		ARCONTROLLER_DEVICE_STATE_PAUSED (3, "device controller is paused"),
//		ARCONTROLLER_DEVICE_STATE_STOPPING (4, "device controller is stopping"),

	/** コントローラーに関連付けられているARDiscoveryDeviceServiceを取得 */
	public ARDiscoveryDeviceService getDeviceService();

	public void release();

	/**
	 * 機体名を取得, ローリングスパイダーだとrs_xxxxxって奴
	 * @return
	 */
	public String getName();

	/**
	 * 製品名を取得
	 * @return
	 */
	public String getProductName();

	public int getProductId();

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
	 * 異常コードを取得
	 * @return
	 */
	public int getAlarm();

	/**
	 * バッテリーの残量を取得
	 * @return バッテリー残量[%]
	 */
	public int getBattery();

	/**
	 * WiFi信号強度を取得
	 * @return
	 */
	public int getWiFiSignal();

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

	public boolean start();

	/**
	 * 接続処理を中断
	 */
	public void cancelStart();

	/**
	 * 切断処理
	 */
	public void stop();

	public boolean isStarted();
	/**
	 * 機体と接続しているかどうか
	 * 直接接続の時は#isStartedと同じ
	 * @return
	 */
	public boolean isConnected();

	/**
	 * 日付を送信
	 * @param currentDate
	 * @return
	 */
	public boolean sendDate(Date currentDate);

	/**
	 * 時刻を送信
	 * @param currentDate
	 * @return
	 */
	public boolean sendTime(Date currentDate);

	/**
	 * 全ての設定要求?
	 * @return
	 */
	public boolean requestAllSettings();

	/**
	 * すべての状態を要求?
	 * @return
	 */
	public boolean requestAllStates();
	/** NewAPIを使うかどうか */
	public boolean isNewAPI();
}
