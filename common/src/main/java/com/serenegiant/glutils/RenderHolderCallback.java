package com.serenegiant.glutils;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: RenderHolderCallback.java
 *
*/

import android.view.Surface;

/**
 * RenderHolderのコールバックリスナー
 */
public interface RenderHolderCallback {
	public void onCreate(Surface surface);
	public void onDestroy();
}
