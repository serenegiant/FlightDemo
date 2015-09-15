package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.widget.gl.FileIO;
import com.serenegiant.widget.gl.IModelViewApplication;
import com.serenegiant.widget.gl.MPAssetIO;
import com.serenegiant.widget.gl.MPExtFileIO;
import com.serenegiant.widget.gl.MPFileIO;

public class FlightDemoApplication extends Application implements IModelViewApplication {
	private FileIO mFileIO;
	private FileIO mExtFileIO;
	private FileIO mAssetIO;

	public FlightDemoApplication() {
	}


	@Override
	public FileIO getFileIO() {
		if (mFileIO == null)
			mFileIO = new MPFileIO(this);
		return mFileIO;
	}

	@Override
	public FileIO getExtFileIO() {
		if (mExtFileIO == null)
			mExtFileIO = new MPExtFileIO("com.serenegiant");
		return mExtFileIO;
	}

	@Override
	public FileIO getAssetIO() {
		if (mAssetIO == null)
			mAssetIO = new MPAssetIO(getAssets());
		return mAssetIO;
	}

}
