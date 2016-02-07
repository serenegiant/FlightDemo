package com.serenegiant.arflight;

/**
 * Created by saki on 16/02/07.
 */
public class WiFiStatus {
	public final int txPower;
	public String ssid;
	public int rssi;
	public int band;	// 0: 2.4GHz, 1: 5GHz
	public int channel;

	public double factor = 2.0;

	public WiFiStatus(final int txPower) {
		this.txPower = txPower;
	}

// RSSI(受信信号強度[dbm])とTxPower(送信強度[dbm])とd(距離[m])の関係
//	RSSI = TxPower - 20 * log10(d)
//	d = 10 ^ ((TxPower - RSSI) / 20)
//	RSSI = TxPower - 10 * factor * log10(d)
//	(TxPower - RSSI) / (10 * factor) = log10(n)
//	factor = 2.0 : 障害物のない理想空間
//	factor < 2.0 : 電波が反射しながら伝搬する空間
//	factor > 2.0 : 障害物に吸収され減衰しながら伝搬する空間
	public float distance() {
		try {
			return (float)Math.pow(10.0, (txPower - rssi) / (10 * factor));
		} catch (final Exception e) {
			return 0;
		}
	}
}
