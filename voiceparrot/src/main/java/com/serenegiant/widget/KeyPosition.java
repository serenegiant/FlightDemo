package com.serenegiant.widget;
/*
 * AndroGamepad
 * library and sample to access to various gamepad with common interface
 *
 * Copyright (c) 2015-2016 saki t_saki@serenegiant.com
 *
 * File name: KeyPosition.java
 *
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
*/

public class KeyPosition {
	public final int key;
	public final int center_x;
	public final int center_y;
	public final int width;
	public final int height;

	public KeyPosition(final int key, final int center_x, final int center_y, final int width, final int height) {
		this.key = key;
		this.center_x = center_x;
		this.center_y = center_y;
		this.width = width;
		this.height = height;
	}

	public KeyPosition(final KeyPosition other) {
		if (other != null) {
			key = other.key;
			center_x = other.center_x;
			center_y = other.center_y;
			width = other.width;
			height = other.height;
		} else {
			key = -1;
			center_x = center_y = 0;
			width = height = 0;
		}
	}
}
