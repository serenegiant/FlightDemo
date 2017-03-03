package com.serenegiant.aceparrot;

import android.support.multidex.MultiDexApplication;

import com.serenegiant.gameengine.FileIO;
import com.serenegiant.gameengine.IGameViewApplication;
import com.serenegiant.gameengine.MPAssetIO;
import com.serenegiant.gameengine.MPExtFileIO;
import com.serenegiant.gameengine.MPFileIO;

public class FlightDemoApplication extends MultiDexApplication implements IGameViewApplication {
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
