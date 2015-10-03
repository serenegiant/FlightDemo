package com.serenegiant.flightdemo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.serenegiant.arflight.GamePad;
import com.serenegiant.arflight.ManagerFragment;
import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.usb.DeviceFilter;
import com.serenegiant.usb.HIDGamepad;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.widget.ISideMenuView;
import com.serenegiant.widget.SideMenuFrameLayout;

import java.util.List;


public class MainActivity extends Activity /*AppCompatActivity*/ {
	// ActionBarActivityを継承するとPilotFragmentから戻る際にクラッシュする
	// Fragmentが切り替わらずに処理中にもかかわらずActivityが破棄されてしまう
	private static final boolean DEBUG = true;    // FIXME 実働時はfalseにすること
	private static String TAG = MainActivity.class.getSimpleName();

	private static boolean isLoaded = false;

	static {
		if (!isLoaded) {
			try {
				System.loadLibrary("curl");
				System.loadLibrary("arsal");
				System.loadLibrary("arsal_android");
				System.loadLibrary("arnetworkal");
				System.loadLibrary("arnetworkal_android");
				System.loadLibrary("arnetwork");
				System.loadLibrary("arnetwork_android");
				System.loadLibrary("arcommands");
				System.loadLibrary("arcommands_android");
				System.loadLibrary("arstream");
				System.loadLibrary("arstream_android");
				System.loadLibrary("json");
				System.loadLibrary("ardiscovery");
				System.loadLibrary("ardiscovery_android");
				System.loadLibrary("arcontroller");
				System.loadLibrary("arcontroller_android");
				System.loadLibrary("arutils");
				System.loadLibrary("arutils_android");
				System.loadLibrary("ardatatransfer");
				System.loadLibrary("ardatatransfer_android");
				System.loadLibrary("armedia");
				System.loadLibrary("armedia_android");
				System.loadLibrary("arupdater");
				System.loadLibrary("arupdater_android");
				System.loadLibrary("armavlink");
				System.loadLibrary("armavlink_android");

//				ARSALPrint.enableDebugPrints();	// XXX ARライブラリのデバッグメッセージを表示する時
				isLoaded = true;
			} catch (final Exception e) {
				Log.e(TAG, "Oops (LoadLibrary)", e);
			}
		}
	}

	/**
	 * サイドメニューを閉じるまでの時間
	 */
	private static final int TIMEOUT_HIDE_SIDE_MENU = 3000;
	/**
	 * サイドメニュー表示中に通常の画面表示の上に被せて表示する色
	 */
	private static final int SCRIM_COLOR = 0x3f000000;

	private static final String KEY_SCRIPTS_FIRST_TIME = "KEY_SCRIPTS_FIRST_TIME";

	// サイドメニュー
	protected DrawerLayout mDrawerLayout;
	private SideMenuFrameLayout mSideMenuFrame;
	private ActionBarDrawerToggle mDrawerToggle;
	private final Handler mUiHandler = new Handler();

	private USBMonitor mUSBMonitor;
	private HIDGamepad mGamepad;
	private TextView mGamepadTv;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
/*		final Toolbar tool_bar = (Toolbar) findViewById(R.id.sample_toolbar);
		setSupportActionBar(tool_bar); */

		NetworkChangedReceiver.enable(getApplicationContext());
		final ManagerFragment manager = ManagerFragment.getInstance(this);
		if (savedInstanceState == null) {
			final Fragment fragment = new ConnectionFragment();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}
		prepareSideMenu();
		final SharedPreferences pref = getPreferences(0);
		final boolean firstTime = pref.getBoolean(KEY_SCRIPTS_FIRST_TIME, true);
		pref.edit().putBoolean(KEY_SCRIPTS_FIRST_TIME, false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				ScriptHelper.copyScripts(MainActivity.this, firstTime);
			}
		}).start();
		// ゲームパッド用のドライバーを使う場合はUsbMonitorを生成する
		// FIXME 今はテストしやすいようにMainActivity内にあるけど本来はPilotFragment内で処理すべき
		final boolean use_usb_driver = pref.getBoolean(ConfigFragment.KEY_GAMEPAD_USE_DRIVER, false);
		if (use_usb_driver && (mUSBMonitor == null)) {
			mUSBMonitor = new USBMonitor(getApplicationContext(), mOnDeviceConnectListener);
			final List<DeviceFilter> filter = DeviceFilter.getDeviceFilters(this, R.xml.device_filter);
			mUSBMonitor.setDeviceFilter(filter.get(0));
		}
		mGamepadTv = (TextView)findViewById(R.id.debug_gamepad_testview);
		if (mGamepadTv != null) {
			mGamepadTv.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onDestroy() {
		hideProgress();
		NetworkChangedReceiver.disable(getApplicationContext());
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
		//　ActionBarActivity/AppCompatActivityはバックキーの処理がおかしくて
		// バックスタックの処理が正常にできない事に対するworkaround
		final FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			Log.i(TAG, "#onBackPressed:popBackStack");
			fm.popBackStack();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onConfigurationChanged(final Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (mDrawerToggle != null) {
			mDrawerToggle.onConfigurationChanged(newConfig);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		if (mUSBMonitor != null) {
			mUSBMonitor.register();
		}
	}

	@Override
	public void onPause() {
		if (mGamepad != null) {
			mGamepad.close();
			mGamepad = null;
		}
		if (mUSBMonitor != null) {
			mUSBMonitor.unregister();
		}
		super.onPause();
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mDrawerToggle != null) {
			mDrawerToggle.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (!isFinishing() && GamePad.processKeyEvent(event)) return true;
		return super.dispatchKeyEvent(event);
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

	/*package*/void setSideMenuView(final View side_menu_view) {
		if ((mSideMenuFrame != null) && (side_menu_view != null)) {
			mSideMenuFrame.addView(side_menu_view);
			if (side_menu_view instanceof ISideMenuView) {
				((ISideMenuView) side_menu_view).setOnSidleMenuListener(mOnSidleMenuListener);
			}
		}
	}

	/*package*/void removeSideMenuView(final View side_menu_view) {
		if ((mSideMenuFrame != null) && (side_menu_view != null)) {
			mSideMenuFrame.removeView(side_menu_view);
		}
	}

	protected void prepareSideMenu() {
		// ListViewのインスタンスを取得
		mSideMenuFrame = (SideMenuFrameLayout) findViewById(R.id.sidemenu_frame);
		if (mSideMenuFrame != null) {
			mSideMenuFrame.setOnSidleMenuListener(mOnSidleMenuListener);
			// ドローワーの設定
			mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name) {
				@Override
				public void onDrawerClosed(final View drawerView) {
					super.onDrawerClosed(drawerView);
					if (DEBUG) Log.v(TAG, "onDrawerClosed:");
					// ドローワーが閉じた時の処理
					mUiHandler.removeCallbacks(mHideSideMenuTask);
					mSideMenuFrame.reset();    // サイドメニューのアニメーションをリセット
				}

				@Override
				public void onDrawerOpened(final View drawerView) {
					super.onDrawerOpened(drawerView);
					if (DEBUG) Log.v(TAG, "onDrawerOpened:");
					// ドローワーが開いた時の処理
					mUiHandler.removeCallbacks(mHideSideMenuTask);
					mUiHandler.postDelayed(mHideSideMenuTask, TIMEOUT_HIDE_SIDE_MENU);
				}

//				@Override
//				public void onDrawerSlide(final View drawerView, final float slideOffset) {
//					super.onDrawerSlide(drawerView, slideOffset);
//					if (DEBUG) Log.v(TAG, "onDrawerSlide:" + slideOffset);
//				}

//				/**
//				 * @param newState <br>
//				 * 0: 表示済み、閉じ済みの状態 <br>
//				 * 1: ドラッグ中状態 <br>
//				 * 2: ドラッグを放した後のアニメーション中 <br>
//				 */
//				@Override
//				public void onDrawerStateChanged(final int newState) {
//					super.onDrawerStateChanged(newState);
//					if (DEBUG) Log.v(TAG, "onDrawerStateChanged	:" + newState);
//				}

/*				@Override
				public boolean onOptionsItemSelected(final MenuItem item) {
					return super.onOptionsItemSelected(item);
				} */
			};
			mDrawerLayout.setDrawerListener(mDrawerToggle);
			mDrawerLayout.setScrimColor(SCRIM_COLOR);    // サイドメニュー表示中にメインコンテンツ部に被せる色
			mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}
	}

	/**
	 * サイドメニューを一定時間後に閉じるためのRunnable
	 */
	private final Runnable mHideSideMenuTask = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mHideSideMenuTask#run");
			mSideMenuFrame.hide();
		}
	};

	/**
	 * サイドメニュー項目を表示して一定時間後に閉じるようにRunnableを投げるするためのRunnable
	 */
	private final Runnable mShowSideMenuTask = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mShowSideMenuTask#run");
			mDrawerLayout.openDrawer(Gravity.LEFT);
			mUiHandler.postDelayed(mHideSideMenuTask, TIMEOUT_HIDE_SIDE_MENU);
		}
	};

	/**
	 * SideMenuListViewからのコールバックリスナー
	 * (今のところサイドメニューの項目のスライドアウトアニメーションが全て終了した時に呼ばれる)
	 */
	private final ISideMenuView.OnSidleMenuListener mOnSidleMenuListener = new ISideMenuView.OnSidleMenuListener() {
		@Override
		public void onSideMenuShow(View view) {
			if (DEBUG) Log.v(TAG, "onSideMenuShow:");
			mUiHandler.removeCallbacks(mHideSideMenuTask);
			mUiHandler.postDelayed(mHideSideMenuTask, TIMEOUT_HIDE_SIDE_MENU);
		}

		@Override
		public void onSideMenuHide(final View view) {
			if (DEBUG) Log.v(TAG, "onSideMenuHide:");
			closeSideMenu();
		}
	};

	/**
	 * サイドメニューの内容を更新
	 */
	/*package*/void updateSideMenu() {
		if (DEBUG) Log.v(TAG, "updateSideMenu:");
		openSideMenu();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	/**
	 * サイドメニューを開く
	 */
	/*package*/void openSideMenu() {
		if (DEBUG) Log.v(TAG, "openSideMenu:");
		if (mSideMenuFrame != null) {
			mUiHandler.removeCallbacks(mShowSideMenuTask);
			mUiHandler.removeCallbacks(mHideSideMenuTask);
			if (mDrawerLayout.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED) {
				mDrawerToggle.setDrawerIndicatorEnabled(true);
				mUiHandler.post(mShowSideMenuTask);
			}
		}
	}

	/**
	 * サイドメニューを閉じる
	 */
	/*package*/void closeSideMenu() {
		if (DEBUG) Log.v(TAG, "closeSideMenu:");
		if (mSideMenuFrame != null) {
			mUiHandler.removeCallbacks(mShowSideMenuTask);
			mUiHandler.removeCallbacks(mHideSideMenuTask);
			mDrawerLayout.closeDrawers();
		}
	}

	/**
	 * サイドメニューの有効/無効を切り替える
	 * 無効から有効になった時はサイドメニューを開く
	 *
	 * @param enable
	 */
	/*package*/void setSideMenuEnable(final boolean enable) {
		if (DEBUG) Log.v(TAG, "setSideMenuEnable:" + enable);
		if (mSideMenuFrame != null) {
			mDrawerLayout.setDrawerLockMode(
				enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(enable);
			if (enable) {
				openSideMenu();
			}
			mDrawerToggle.syncState();
		}
	}

	private final USBMonitor.OnDeviceConnectListener mOnDeviceConnectListener = new USBMonitor.OnDeviceConnectListener() {
		@Override
		public void onAttach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onAttach:");
			if (mUSBMonitor != null) {
				UsbDevice _device = device;
				if ((_device == null) && (mUSBMonitor.getDeviceCount() > 0)) {
					_device = mUSBMonitor.getDeviceList().get(0);
				}
				mUSBMonitor.requestPermission(_device);
			}
		}

		@Override
		public void onConnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock, final boolean createNew) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onConnect:");
			if (mGamepadTv != null) {
				mGamepadTv.setVisibility(View.VISIBLE);
			}
			mGamepad = new HIDGamepad(mHIDGamepadCallback);
			mGamepad.open(ctrlBlock);
		}

		@Override
		public void onDisconnect(final UsbDevice device, final USBMonitor.UsbControlBlock ctrlBlock) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDisconnect:");
			if (mGamepad != null) {
				mGamepad.close();
				mGamepad = null;
			}
		}

		@Override
		public void onDettach(final UsbDevice device) {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onDettach:");
			if (mGamepad != null) {
				mGamepad.close();
				mGamepad = null;
			}
		}

		@Override
		public void onCancel() {
			if (DEBUG) Log.v(TAG, "OnDeviceConnectListener#onCancel:");
		}
	};

	private final HIDGamepad.HIDGamepadCallback mHIDGamepadCallback = new HIDGamepad.HIDGamepadCallback() {
		private final StringBuilder sb = new StringBuilder();

		@Override
		public void onRawdataChanged(final int n, final byte[] data) {
			if (mGamepadTv != null) {
				sb.setLength(0);
				final int m = n / 8 + 1;
				int ix = 0;
LOOP:			for (int j = 0; j < m; j++) {
					if (ix >= n) break LOOP;
					if (j != 0) {
						sb.append("\n");
					}
					for (int i = 0; i < 8; i++) {
						if (ix >= n) break LOOP;
						sb.append(String.format("%02x:", data[ix++]));
					}
				}
				final String text = sb.toString();
				mGamepadTv.post(new Runnable() {
					@Override
					public void run() {
						mGamepadTv.setText(text);
					}
				});
//				Log.v(TAG, text);
			}
		}
	};
}
