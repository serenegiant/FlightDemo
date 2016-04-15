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

	/** コントローラーに関連付けられているARDiscoveryDeviceServiceを取得 */
	public ARDiscoveryDeviceService getDeviceService();
	public ARNetworkALManager getNetALManager();
	public ARNetworkManager getNetManager();
	public ARNetworkConfig getNetConfig();

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

	public int getAlarm();
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
