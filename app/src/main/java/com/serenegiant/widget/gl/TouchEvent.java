package com.serenegiant.widget.gl;

public class TouchEvent {
	public static final int TOUCH_DOWN = 0;
	public static final int TOUCH_UP = 1;
	public static final int TOUCH_DRAGGED  = 2;
	public static final int TOUCH_DOUBLETAP = 4;
	public static final int TOUCH_LONGTAP = 4;

	public int type;
	public int x, y;
	public long eventTime, downTime;
	public int pointer;

	public TouchEvent set(final TouchEvent src) {
		if (src != null) {
			type = src.type;
			pointer = src.pointer;
			x = src.x;
			y = src.y;
			downTime = src.downTime;
			eventTime = src.eventTime;
		}
		return this;
	}
}
