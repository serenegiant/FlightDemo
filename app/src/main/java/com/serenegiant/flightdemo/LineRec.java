package com.serenegiant.flightdemo;

import com.serenegiant.math.Vector;

/** 解析データレコード */
public class LineRec {
	/** 0:直線, 1:(楕)円弧, 2:角 */
	public int type;
	/** ラインの中心座標(最小矩形の中心座標) */
	public final Vector mLinePos = new Vector();
	/** ライン長,ライン幅, ライン角, 最小矩形面積に対する輪郭面積の比 */
	public float mLineLen, mLineWidth, mAngle, mAreaRate;
	/** 近似楕円の最大曲率 */
	public float mCurvature;
	/** 近似楕円の中心座標 */
	public final Vector mEclipsePos = new Vector();
	/** ラインの重心座標 */
	public final Vector mCenter = new Vector();
}
