package com.serenegiant.flightdemo;

import com.serenegiant.math.Vector;

/** 解析データレコード */
public class LineRec {
	/** 0:直線, 1:(楕)円弧, 2:角 */
	public int type;
	/** ラインの重心座標(輪郭の重心座標) */
	public final Vector linePos = new Vector();
	/** ライン長,ライン幅, ライン角, 最小矩形面積に対する輪郭面積の比 */
	public float lineLen, lineWidth, angle, areaRate;
	/** 近似楕円の最大曲率 */
	public float curvature;
	/** 近似楕円の中心座標 */
	public final Vector ellipsePos = new Vector();
	/** 近似楕円の長軸/短軸半径 */
	public float ellipseA, ellipseB;
	/** 近似楕円の傾き */
	public float ellipseAngle;
	/** ラインの重心座標 */
	public final Vector center = new Vector();
	public long processingTimeMs;
}
