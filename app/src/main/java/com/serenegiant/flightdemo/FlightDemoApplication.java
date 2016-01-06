package com.serenegiant.flightdemo;

import android.app.Application;

import com.serenegiant.gl.FileIO;
import com.serenegiant.gl.IModelViewApplication;
import com.serenegiant.gl.MPAssetIO;
import com.serenegiant.gl.MPExtFileIO;
import com.serenegiant.gl.MPFileIO;

public class FlightDemoApplication extends Application implements IModelViewApplication {
	private FileIO mFileIO;
	private FileIO mExtFileIO;
	private FileIO mAssetIO;

	public FlightDemoApplication() {
		super();
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
