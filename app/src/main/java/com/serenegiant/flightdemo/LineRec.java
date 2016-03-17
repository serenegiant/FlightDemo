package com.serenegiant.flightdemo;

import com.serenegiant.math.Vector;

/** 解析データレコード */
public class LineRec {
	public int type;
	public final Vector mLinePos = new Vector();
	public float mLineLen, mLineWidth, mAngle, mAreaRate, mCurvature;
}
