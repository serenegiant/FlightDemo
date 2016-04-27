package com.serenegiant.arflight;

import com.serenegiant.arflight.configs.ARNetworkConfig;

import java.util.List;

public interface ISkyController extends IDeviceController {
	/** ブリッジ接続用のARNetworkConfigを新規に生成して返す */
	public ARNetworkConfig createBridgeNetConfig();
	public VideoStreamDelegater getVideoStreamDelegater();

	/**
	 * スカイコントローラーの設定をリセット
	 * onSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * SSIDもリセットされる
	 */
	public boolean resetSettings();
	/**
	 * スカイコントローラーのSSIDを設定
	 * onAccessPointSSIDChangedUpdateとonSkyControllerDeviceStateConnexionChangedUpdateが呼ばれる
	 * 次回電源投入時に有効になる
	 * @param ssid 設定するSSID 使用可能な文字数はたぶん32文字, 半角英数+α
	 * @return
	 */
	public boolean setSkyControllerSSID(final String ssid);
	/**
	 * スカイコントローラーが検出しているWiFiアクセスポイント一覧を要求
	 * 周囲に存在するWiFiの状態を確認するぐらいにしか役に立たない
	 */
	public boolean requestWifiList();
	/**
	 * スカイコントローラーが現在接続しているWiFiネットワークとの接続状態を
	 * これを呼ぶとARCommandSkyControllerWifiStateConnexionChangedListenerと
	 * ARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 */
	public boolean requestCurrentWiFi();
	/**
	 * スカイコントローラーからSSIDで指定したWiFiネットワークに接続する
	 * @param bssid
	 * @param ssid
	 * @param passphrase
	 * @return
	 */
	public boolean connectToWiFi(final String bssid, final String ssid, final String passphrase);
	/**
	 * SSIDで指定したWiFiネットワークとのスカイコントローラー上の接続設定を消去, たぶん切断される
	 * @param ssid
	 * @return
	 */
	public boolean requestForgetWiFi(final String ssid);
	/**
	 * スカイコントローラーが検出している機体一覧を要求
	 */
	public boolean requestDeviceList();

	/**
	 * スカイコントローラーが検出している機体の数を取得
	 * @return
	 */
	public int getDeviceNum();
	/**
	 * ISkyControllerを実装するクラスが保持している、スカイコントローラーが検出している機体一覧を取得
	 * コピーを返すので呼び出し以降の接続状態の変更は反映されない
	 * SDK側で#requestDeviceListが正しく動いてないようなので
	 * @return
	 */
	public List<DeviceInfo> getDeviceList();
	/**
	 * スカイコントローラーが現在接続している機体との接続状態を要求する
	 * これを呼ぶとARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * 接続している機体がなくてもARCommandSkyControllerDeviceStateConnexionChangedListenerのコールバックメソッドが呼び出される
	 * (ARCommandSkyControllerWifiStateConnexionChangedListenerは来ない)
	 */
	public boolean requestCurrentDevice();
	/**
	 * ISkyControllerを実装するクラスが保持している、スカイコントローラーが現在接続している機体を取得
	 * コピーを返すので呼び出し以降の接続状態の変更は反映されない
	 * @return null 接続されていない
	 */
	public DeviceInfo getCurrentDevice();
	/**
	 * 指定したデバイス名を持つ機体へ接続する
	 * @param deviceName
	 * @return true 接続できなかった
	 */
	public boolean connectToDevice(final String deviceName);
	public boolean connectToDevice(final DeviceInfo info);
	public void disconnectFrom();
	/**
	 * 操縦に使用する入力方法を選択
	 * ARCommandSkyControllerCoPilotingStatePilotingSourceListenerのコールバックメソッドが呼ばれる。なんでやねん
	 * @param _source 0: スカイコントローラーを使用する, 1: タブレット/スマホを使用する
	 */
	public boolean setCoPilotingSource(final int _source);
	public int getCoPilotingSource();
	/**
	 * カメラのpan/tiltをリセットする
	 * FIXME 機体のカメラなんかな? これを呼んでも何のコールバックもこない. スカイコントローラー自体のアプリ用なのかも
	 */
	public boolean resetCameraOrientation();
	/**
	 * スカイコントローラーのボタン・ジョイスティック等の一覧を要求する
	 */
	public boolean requestGamepadControls();
	/** 現在のボタン割当設定を要求 */
	public boolean requestCurrentButtonMappings();
	/** 使用可能なボタン割当設定を要求 */
	public boolean requestAvailableButtonMappings();
	/**
	 * ボタンの割当設定
	 * @param key_id 物理ボタンID
	 * @param mapping_uid ボタン機能ID
	 * @return
	 */
	public boolean setButtonMapping(final int key_id, final String mapping_uid);
	/**
	 * ボタン割り付け設定をデフォルトにリセットする
	 * @return
	 */
	public boolean resetButtonMapping();
	/** 現在のジョイスティック割当設定を要求 */
	public boolean requestCurrentAxisMappings();
	/** 使用可能なジョイスティック割当設定を要求 */
	public boolean requestAvailableAxisMappings();
	/**
	 * ジョイスティックの割当を変更する
	 * @param axis_id ジョイスティックの物理ID
	 * @param mapping_uid ジョイスティックの機能ID
	 * @return
	 */
	public boolean setAxisMapping(final int axis_id, final String mapping_uid);
	/**
	 * ジョイスティックの割当をデフォルトにリセットする
	 * なぜかonSkyControllerAxisMappingsStateCurrentAxisMappingsUpdateと
	 * onSkyControllerAxisMappingsStateAllCurrentAxisMappingsSentUpdateが2ペア分送られてくる
	 * もしかすると1回目は変更前で2回目が変更後なのかも
	 * @return
	 */
	public boolean resetAxisMapping();
	/** ジョイスティックの入力フィルター設定を要求 */
	public boolean requestCurrentAxisFilters();
	/** ジョイスティックの入力フィルターのプリセット設定を要求 */
	public boolean requestPresetAxisFilters();
	/**
	 * ジョイスティックの入力フィルター設定
	 * @param axis_id 物理ジョイスティックID
	 * @param filter_uid_or_builder フィルターID
	 * @return
	 */
	public boolean setAxisFilter(final int axis_id, final String filter_uid_or_builder);
	/**
	 * ジョイスティックの入力フィルターをデフォルトにリセットする
	 * @return
	 */
	public boolean resetAxisFilter();
	/**
	 * 磁気センサーのキャリブレーション品質更新通知の有効/無効を切り替える
	 * @param enable
	 * @return
	 */
	public boolean setMagnetoCalibrationQualityUpdates(final boolean enable);
	/**
	 * なんじゃらほい?
	 * 何のコールバックも返ってこない
	 * FIXME スカイコントローラー自体のアプリ用なのかも
	 * @return
	 */
	public boolean requestButtonEventsSettings();
	/**
	 * なんかわからんけどデバッグフラグセットするんでしょうきっと
	 * @param t1Args
	 * @return
	 */
	public boolean setDebugTest1(final byte t1Args);

	public boolean isGPSFixedSkyController();
	public int getBatterySkyController();
}
