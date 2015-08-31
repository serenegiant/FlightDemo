package com.serenegiant.flightdemo;

import android.app.Application;

import com.serenegiant.widget.gl.FileIO;
import com.serenegiant.widget.gl.IModelViewApplication;
import com.serenegiant.widget.gl.MPAssetIO;
import com.serenegiant.widget.gl.MPExtFileIO;
import com.serenegiant.widget.gl.MPFileIO;

public class FlightDemoApplication extends Application implements IModelViewApplication {
	private FileIO mFileIO;
	private FileIO mExtFileIO;
	private FileIO mAssetIO;

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
