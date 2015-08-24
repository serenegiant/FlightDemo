package com.serenegiant.arflight;

public class AttributePosition {
	private static final double INVALID_VALUE = 500.0;

	private double mLatitude;	// 緯度[度], 無効値:500.0
	private double mLongitude;	// 経度[度], 無効値:500.0
	private double mAltitude;	// 高度[m]

	public AttributePosition() {
		this(500.0, 500.0, 0);
	}

	public AttributePosition(final double latitude, final double longitude, final double altitude) {
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
	}

	public AttributePosition(final AttributePosition other) {
		mLatitude = other != null ? other.mLatitude : INVALID_VALUE;
		mLongitude = other != null ? other.mLongitude : INVALID_VALUE;;
		mAltitude = other != null ? other.mAltitude : 0;
	}

	public void set(final AttributePosition other) {
		mLatitude = other != null ? other.mLatitude : INVALID_VALUE;
		mLongitude = other != null ? other.mLongitude : INVALID_VALUE;;
		mAltitude = other != null ? other.mAltitude : 0;
	}

	public void set(final double latitude, final double longitude, final double altitude) {
		mLatitude = latitude;
		mLongitude = longitude;
		mAltitude = altitude;
	}

	public void latitude(final double latitude) {
		mLatitude = latitude;
	}
	public double latitude() {
		return mLatitude;
	}

	public void longitude(final double longitude) {
		mLongitude = longitude;
	}
	public double longitude() {
		return mLongitude;
	}

	public void altitude(final double altitude) {
		mAltitude = altitude;
	}
	public double altitude() {
		return mAltitude;
	}

	/**
		 * 2点間の距離を計算(高度差は考慮してない)
		 * @param other
		 * @return 距離[m]
		 */
	public double distance(final AttributePosition other) {
		if ((other == null)
			|| (mLatitude >= INVALID_VALUE) || (mLongitude >= INVALID_VALUE)
			|| (other.mLatitude >= INVALID_VALUE) || (other.mLongitude >= INVALID_VALUE)) return 0;
		return calcDistHubeny(mLatitude, mLongitude, other.mLatitude, other.mLongitude);
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