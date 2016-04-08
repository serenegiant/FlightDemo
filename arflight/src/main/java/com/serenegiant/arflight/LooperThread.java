package com.serenegiant.arflight;

import android.util.Log;

public abstract class LooperThread extends Thread {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static int thread_cnt = 0;
	private volatile boolean mIsRunning;

	public LooperThread() {
		mIsRunning = true;
	}

	@Override
	public void run() {
		onStart();

		for ( ; mIsRunning ; ) {
			onLoop();
		}

		mIsRunning = false;
		onStop();
	}

	public void stopThread() {
		mIsRunning = false;
	}

	public boolean isRunning() {
		return mIsRunning;
	}

	protected void onStart() {
		if (DEBUG) Log.v("LooperThread", "onStart:" + thread_cnt++);
	}

	protected abstract void onLoop();

	protected void onStop() {
		if (DEBUG) Log.v("LooperThread", "onStop:" + --thread_cnt);
	}
}
