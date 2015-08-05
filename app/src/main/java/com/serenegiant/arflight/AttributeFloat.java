package com.serenegiant.arflight;

/**
 * 浮動小数点の設定
 */
public class AttributeFloat {
	public float current;
	public float min;
	public float max;

	@Override
	public String toString() {
		return String.format("AttributeFloat{%f/%f/%f)}", min, current, max);
	}
}
