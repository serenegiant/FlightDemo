package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
	// ActionBarActivityを継承するとPilotFragmentから戻る際にクラッシュする
	// Fragmentが切り替わらずに処理中にもかかわらずActivityが破棄されてしまう
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
		final Toolbar tool_bar = (Toolbar)findViewById(R.id.sample_toolbar);
		this.setSupportActionBar(tool_bar);
		final ManagerFragment manager = ManagerFragment.getInstance(this);
		if (savedInstanceState == null) {
			final Fragment fragment = new ConnectionFragment();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}

	}

	@Override
	protected void onDestroy() {
		hideProgress();
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		//　ActionBarActivity/AppCompatActivityはバックキーの処理がおかしくて
		// バックスタックの処理が正常にできない事に対するworkaround
		if (getFragmentManager().getBackStackEntryCount() > 0) {
			Log.i(TAG, "#onBackPressed:popBackStack");
			getFragmentManager().popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	private ProgressDialog mProgress;
	public synchronized void showProgress(final int title_resID, final boolean cancelable,
		final DialogInterface.OnCancelListener cancel_listener) {
		if (!isFinishing()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgress = ProgressDialog.show(MainActivity.this, getString(title_resID), null, true, cancelable, cancel_listener);
				}
			});
		}
	}

	public synchronized void hideProgress() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (mProgress != null) {
					mProgress.dismiss();
					mProgress = null;
				}
			}
		});
	}
}
