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
	private NetworkChangedReceiver mNetworkChangedReceiver;
	private LocalBroadcastManager mLocalBroadcastManager;
	private FileIO mFileIO;
	private FileIO mExtFileIO;
	private FileIO mAssetIO;

	public FlightDemoApplication() {
		registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks);
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

	private final ActivityLifecycleCallbacks
		mActivityLifecycleCallbacks = new ActivityLifecycleCallbacks() {
		@Override
		public void onActivityCreated(final Activity activity, final Bundle savedInstanceState) {
			if (mNetworkChangedReceiver == null) {
				mNetworkChangedReceiver = NetworkChangedReceiver.registerNetworkChangedReceiver(FlightDemoApplication.this, mOnNetworkChangedListener);
				mLocalBroadcastManager = LocalBroadcastManager.getInstance(FlightDemoApplication.this);
			}
		}

		@Override
		public void onActivityStarted(final Activity activity) {
		}

		@Override
		public void onActivityResumed(final Activity activity) {
		}

		@Override
		public void onActivityPaused(final Activity activity) {
		}

		@Override
		public void onActivityStopped(final Activity activity) {
		}

		@Override
		public void onActivitySaveInstanceState(final Activity activity, final Bundle outState) {
		}

		@Override
		public void onActivityDestroyed(final Activity activity) {
			if (mNetworkChangedReceiver != null) {
				NetworkChangedReceiver.unregisterNetworkChangedReceiver(FlightDemoApplication.this, mNetworkChangedReceiver);
				mNetworkChangedReceiver = null;
				mLocalBroadcastManager = null;
			}
		}
	};


	public static final String KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING = "KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING";
	public static final String KEY_NETWORK_CHANGED_IS_CONNECTED = "KEY_NETWORK_CHANGED_IS_CONNECTED";

	private Intent mNetworkChangedIntent;
	private final NetworkChangedReceiver.OnNetworkChangedListener
		mOnNetworkChangedListener = new NetworkChangedReceiver.OnNetworkChangedListener() {
		@Override
		public void onNetworkChanged(final int isConnectedOrConnecting, final int isConnected, final int activeTypeMask) {
			if (mLocalBroadcastManager != null) {
				if (mNetworkChangedIntent == null) {
					mNetworkChangedIntent = new Intent();
				}
				mNetworkChangedIntent.putExtra(KEY_NETWORK_CHANGED_IS_CONNECTED_OR_CONNECTING, isConnectedOrConnecting);
				mNetworkChangedIntent.putExtra(KEY_NETWORK_CHANGED_IS_CONNECTED, isConnected);
				mLocalBroadcastManager.sendBroadcast(mNetworkChangedIntent);
			}
		}
	};

}
