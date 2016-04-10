package com.serenegiant.arflight.attribute;

public class AttributePosition {
	public static final double INVALID_VALUE = 500.0;

	private boolean mGPSIsValid;
	private double mGPSLatitude;	// 緯度[度], 無効値:500.0
	private double mGPSLongitude;	// 経度[度], 無効値:500.0
	private double mGPSAltitude;	// 高度[m], 無効値:500.0
	private double mAltitude;		// 対地高度[m]
	private double mHeading;		// 方位角[度]

	/**
	 * コンストラクタ
	 */
	public AttributePosition() {
		this(INVALID_VALUE, INVALID_VALUE, INVALID_VALUE, 0.0);
	}

	/**
	 * コンストラクタ(GPS座標を指定)
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public AttributePosition(final double latitude, final double longitude, final double altitude) {
		this(latitude, longitude, altitude, 0.0);
	}

	/**
	 * コンストラクタ(GPS座標を指定)
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public AttributePosition(final double latitude, final double longitude, final double altitude, final double heading) {
		mGPSLatitude = latitude;
		mGPSLongitude = longitude;
		mGPSAltitude = altitude;
		mAltitude = 0.0;
		mHeading = heading;
		mGPSIsValid = (latitude != INVALID_VALUE) && (longitude != INVALID_VALUE) && (altitude != INVALID_VALUE);
	}

	/**
	 * コピーコンストラクタ
	 * @param other
	 */
	public AttributePosition(final AttributePosition other) {
		set(other);
	}

	public boolean isGPSValid() {
		return mGPSIsValid;
	}

	public void set(final AttributePosition other) {
		mGPSLatitude = other != null ? other.mGPSLatitude : INVALID_VALUE;
		mGPSLongitude = other != null ? other.mGPSLongitude : INVALID_VALUE;;
		mGPSAltitude = other != null ? other.mAltitude : INVALID_VALUE;
		mAltitude = other != null ? other.mAltitude : 0.0;
		mHeading = other != null ? other.mHeading : 0.0;
		mGPSIsValid = other != null ? other.mGPSIsValid : false;
	}

	/**
	 * GPS座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void set(final double latitude, final double longitude, final double altitude) {
		mGPSLatitude = latitude;
		mGPSLongitude = longitude;
		mGPSAltitude = altitude;
		mGPSIsValid = (latitude != INVALID_VALUE) && (longitude != INVALID_VALUE) && (altitude != INVALID_VALUE);
	}

	/**
	 * GPS座標をセット
	 * @param latitude
	 * @param longitude
	 * @param altitude
	 */
	public void set(final double latitude, final double longitude, final double altitude, final double heading) {
		mGPSLatitude = latitude;
		mGPSLongitude = longitude;
		mGPSAltitude = altitude;
		mHeading = heading;
		mGPSIsValid = (latitude != INVALID_VALUE) && (longitude != INVALID_VALUE) && (altitude != INVALID_VALUE);
	}

	/**
	 * GPS緯度をセット
	 * @param latitude
	 */
	public void latitude(final double latitude) {
		mGPSLatitude = latitude;
		mGPSIsValid = mGPSIsValid && (latitude != INVALID_VALUE);
	}

	/**
	 * GPS緯度を取得
	 * @return
	 */
	public double latitude() {
		return mGPSLatitude ;
	}

	/**
	 * GPS経度をセット
	 * @param longitude
	 */
	public void longitude(final double longitude) {
		mGPSLongitude = longitude;
		mGPSIsValid = mGPSIsValid && (longitude != INVALID_VALUE);
	}

	/**
	 * GPS経度を取得
	 * @return
	 */
	public double longitude() {
		return mGPSLongitude;
	}

	/**
	 * 高度をセット、これでセットするのは対地高度
	 * @param altitude
	 */
	public void altitude(final double altitude) {
		mAltitude = altitude;
		mGPSIsValid = mGPSIsValid && (altitude != INVALID_VALUE);
	}

	/**
	 * GPS高度が無効値でなければGPS高度を返す。GPS高度が無効値ならば通常の高度を返す
	 * @return
	 */
	public double altitude() {
		return mGPSAltitude != INVALID_VALUE ? mGPSAltitude : mAltitude;
	}

	/**
	 * 方位角をセット
	 * @param heading
	 */
	public void heading(final double heading) {
		mHeading = heading;
	}

	/**
	 * 方位角を取得
	 * @return
	 */
	public double heading() {
		return mHeading;
	}

	/**
	 * 2点間の距離を計算(高度差は考慮してない)
	 * @param other
	 * @return 距離[m]
	 */
	public double distance(final AttributePosition other) {
		if ((other == null)
			|| (mGPSLatitude >= INVALID_VALUE) || (mGPSLongitude >= INVALID_VALUE)
			|| (other.mGPSLatitude >= INVALID_VALUE) || (other.mGPSLongitude >= INVALID_VALUE)) return 0;
		return calcDistHubeny(mGPSLatitude, mGPSLongitude, other.mGPSLatitude, other.mGPSLongitude);
	}

	// 緯度経度と距離の変換用
	private static final double BESSEL_A = 6377397.155;
	private static final double BESSEL_E2 = 0.00667436061028297;
	private static final double BESSEL_MNUM = 6334832.10663254;

	private static final double GRS80_A = 6378137.000;
	private static final double GRS80_E2 = 0.00669438002301188;
	private static final double GRS80_MNUM = 6335439.32708317;

	private static final double WGS84_A = 6378137.000;
	private static final double WGS84_E2 = 0.00669437999019758;
	private static final double WGS84_MNUM = 6335439.32729246;

	private static final int BESSEL = 0;
	private static final int GRS80 = 1;
	private static final int WGS84 = 2;

	private static final double DEGREE_TO_RADIAN = Math.PI / 180.0;

	private static double calcDistHubeny(
		final double lat1, final double lng1,
		final double lat2, final double lng2,
		final double a, final double e2, final double mnum) {

		final double my = (lat1 + lat2) / 2.0 * DEGREE_TO_RADIAN;
		final double dy = (lat1 - lat2) * DEGREE_TO_RADIAN;
		final double dx = (lng1 - lng2) * DEGREE_TO_RADIAN;

		final double sin = Math.sin(my);
		final double w = Math.sqrt(1.0 - e2 * sin * sin);
		final double m = mnum / (w * w * w);
		final double n = a / w;

		final double dym = dy * m;
		final double dxncos = dx * n * Math.cos(my);

		return Math.sqrt(dym * dym + dxncos * dxncos);
	}

	private static double calcDistHubeny(final double lat1, final double lng1, final double lat2, final double lng2) {
		return calcDistHubeny(lat1, lng1, lat2, lng2, GRS80_A, GRS80_E2, GRS80_MNUM);
	}

	private static double calcDistHubery(final double lat1, final double lng1, final double lat2, final double lng2, final int type) {
		switch (type) {
		case BESSEL:
			return calcDistHubeny(lat1, lng1, lat2, lng2, BESSEL_A, BESSEL_E2, BESSEL_MNUM);
		case WGS84:
			return calcDistHubeny(lat1, lng1, lat2, lng2, WGS84_A, WGS84_E2, WGS84_MNUM);
		default:
			return calcDistHubeny(lat1, lng1, lat2, lng2, GRS80_A, GRS80_E2, GRS80_MNUM);
		}
	}

}
