package com.serenegiant.arflight.attribute;

public class AttributeDrone {
	private String mProductName;
	public void setProductName(final String name) {
		mProductName = name;
	}
	public String productName() {
		return mProductName;
	}
	private String mProductSoftware;
	private String mProductHardware;
	public void setProduct(final String software, final String hardware) {
		mProductSoftware = software;
		mProductHardware = hardware;
	}
	public String productSoftware() {
		return mProductSoftware;
	}
	public String productHardware() {
		return mProductHardware;
	}

	private String mSerialHigh, mSerialLow;
	public void setSerialLow(final String low) {
		mSerialLow = low;
	}
	public void setSerialHigh(final String high) {
		mSerialHigh = high;
	}
	public String getSerial() {
		return mSerialHigh + mSerialLow;
	}
}
