package com.serenegiant.aceparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.parrot.arsdk.ARSDK;
import com.parrot.arsdk.arsal.ARSALPrint;
import com.parrot.arsdk.arsal.ARSAL_PRINT_LEVEL_ENUM;
import com.serenegiant.gamepad.Joystick;
import com.serenegiant.net.NetworkChangedReceiver;
import com.serenegiant.arflight.R;
import com.serenegiant.utils.BuildCheck;
import com.serenegiant.widget.ISideMenuView;
import com.serenegiant.widget.SideMenuFrameLayout;

import jp.co.rediscovery.arflight.ManagerFragment;

import static com.serenegiant.aceparrot.AppConst.*;

public abstract class AbstractMainActivity extends Activity implements IMainActivity {
	private static final boolean DEBUG = false;    // FIXME 実働時はfalseにすること
	private static String TAG = AbstractMainActivity.class.getSimpleName();

	static {
		ARSDK.loadSDKLibs();
//		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_DEBUG);
		ARSALPrint.setMinimumLogLevel(ARSAL_PRINT_LEVEL_ENUM.ARSAL_PRINT_ERROR);
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
	/*package*/Joystick mJoystick;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
/*		final Toolbar tool_bar = (Toolbar) findViewById(R.id.sample_toolbar);
		setSupportActionBar(tool_bar); */

		NetworkChangedReceiver.enable(getApplicationContext());
		final ManagerFragment manager = ManagerFragment.getInstance(this);
		if (savedInstanceState == null) {
			final Fragment fragment = createConnectionFragment();
			getFragmentManager().beginTransaction()
				.add(R.id.container, fragment).commit();
		}
		prepareSideMenu();
		final SharedPreferences pref = getPreferences(0);
		final boolean firstTime = pref.getBoolean(KEY_SCRIPTS_FIRST_TIME, true);
		pref.edit()
			.putBoolean(KEY_SCRIPTS_FIRST_TIME, false)
			.putBoolean(KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT, false)
			.apply();
		new Thread(new Runnable() {
			@Override
			public void run() {
				ScriptHelper.copyScripts(AbstractMainActivity.this, firstTime);
			}
		}).start();
		mJoystick = Joystick.getInstance(this);
	}

	protected abstract Fragment createConnectionFragment();

	@Override
	protected final void onStart() {
		super.onStart();
		if (BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onResume() {
		super.onResume();
		if (!BuildCheck.isAndroid7()) {
			internalOnResume();
		}
	}

	@Override
	protected final void onPause() {
		if (!BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onPause();
	}

	@Override
	protected final void onStop() {
		if (BuildCheck.isAndroid7()) {
			internalOnPause();
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		releaseJoystick();
		hideProgress();
		NetworkChangedReceiver.disable(getApplicationContext());
		super.onDestroy();
	}

	protected void internalOnResume() {
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
		if (mJoystick != null) {
			mJoystick.register();
		}
	}

	protected void internalOnPause() {
		if (mJoystick != null) {
			mJoystick.unregister();
		}
		if (isFinishing()) {
			ManagerFragment.releaseAll(this);
		}
	}

	@Override
	public boolean dispatchKeyEvent(final KeyEvent event) {
		if (mJoystick != null) {
			if (mJoystick.dispatchKeyEvent(event)) {
				return true;
			}
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean dispatchGenericMotionEvent(final MotionEvent event) {
		if (mJoystick != null) {
			mJoystick.dispatchGenericMotionEvent(event);
		}
		return super.dispatchGenericMotionEvent(event);
	}

	@Override
	public void onBackPressed() {
		//　ActionBarActivity/AppCompatActivityはバックキーの処理がおかしくて
		// バックスタックの処理が正常にできない事に対するworkaround
		final FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			if (DEBUG) Log.i(TAG, "#onBackPressed:popBackStack");
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mDrawerToggle != null) {
			mDrawerToggle.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	public void setSideMenuView(final View side_menu_view) {
		if ((mSideMenuFrame != null) && (side_menu_view != null)) {
			mSideMenuFrame.addView(side_menu_view);
			if (side_menu_view instanceof ISideMenuView) {
				((ISideMenuView) side_menu_view).setOnSidleMenuListener(mOnSidleMenuListener);
			}
		}
	}

	public void removeSideMenuView(final View side_menu_view) {
		if ((mSideMenuFrame != null) && (side_menu_view != null)) {
			mSideMenuFrame.removeView(side_menu_view);
		}
	}

	@SuppressWarnings("deprecation")
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
	public void closeSideMenu() {
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
	public void setSideMenuEnable(final boolean enable) {
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

	private ProgressDialog mProgress;

	public synchronized void showProgress(final int title_resID, final boolean cancelable,
		final DialogInterface.OnCancelListener cancel_listener) {

		if (!isFinishing()) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mProgress = ProgressDialog.show(AbstractMainActivity.this, getString(title_resID), null, true, cancelable, cancel_listener);
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

	private void releaseJoystick() {
		if (mJoystick != null) {
			mJoystick.release();
			mJoystick = null;
		}
	}

	@Override
	public Joystick getJoystick() {
		return mJoystick;
	}

}
