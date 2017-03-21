package com.serenegiant.autoparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

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
