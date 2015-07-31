package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

import com.parrot.arsdk.arsal.ARSALPrint;


public class MainActivity extends Activity {
	private static String TAG = MainActivity.class.getSimpleName();

	private static boolean isLoaded = false;

	static {
		if (!isLoaded) {
			try {
				System.loadLibrary("arsal");
				System.loadLibrary("arsal_android");
				System.loadLibrary("arnetworkal");
				System.loadLibrary("arnetworkal_android");
				System.loadLibrary("arnetwork");
				System.loadLibrary("arnetwork_android");
				System.loadLibrary("arcommands");
				System.loadLibrary("arcommands_android");
				System.loadLibrary("json");
				System.loadLibrary("ardiscovery");
				System.loadLibrary("ardiscovery_android");
				System.loadLibrary("arstream");
				System.loadLibrary("arstream_android");
				System.loadLibrary("arcontroller");
				System.loadLibrary("arcontroller_android");

//				ARSALPrint.enableDebugPrints();	// XXX ARライブラリのデバッグメッセージを表示する時
				isLoaded = true;
			} catch (Exception e) {
				Log.e(TAG, "Oops (LoadLibrary)", e);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			final Fragment fragment = new ConnectionFragment();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}
/*
		try {
			final ScriptParser parser = new ScriptParser(getResources().getAssets().open("control.script"));
			parser.Parse();
//			parser.Parse().jjtAccept(new ScriptParserVisitorImpl(), null);
		} catch (IOException e) {
			Log.w(TAG, e);
		} catch (ParseException e) {
			Log.w(TAG, e);
		} catch (Exception e) {
			Log.w(TAG, e);
		} */
	}

}
