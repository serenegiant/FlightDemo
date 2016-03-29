package com.serenegiant.flightdemo;

import com.serenegiant.math.Vector;

/** 解析データレコード */
public class LineRec {
	public int type;	// 0:直線, 1:(楕)円弧, 2:角
	public final Vector mLinePos = new Vector();	// ラインの中心座標(最小矩形の中心座標)
	public float mLineLen, mLineWidth, mAngle, mAreaRate;
	public float mCurvature;	// 近似楕円の最大曲率
	public final Vector mEclipsePos = new Vector();	// 近似楕円の中心座標
}
