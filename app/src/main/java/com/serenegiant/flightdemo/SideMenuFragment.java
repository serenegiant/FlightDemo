package com.serenegiant.flightdemo;

import android.app.Fragment;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.serenegiant.widget.ISideMenuView;

/**
 * Created by saki on 2015/08/19.
 */
public abstract class SideMenuFragment extends Fragment {
	private static final boolean DEBUG = true;	// FIXME 実働時はfalseにすること
	private static String TAG = SideMenuFragment.class.getSimpleName();

	/**
	 * サイドメニューを閉じるまでの時間
	 */
	private static final int TIMEOUT_HIDE_SIDE_MENU = 2500;
	/**
	 * サイドメニュー表示中に通常の画面表示の上に被せて表示する色
	 */
	private static final int SCRIM_COLOR = 0x3fff0000;

	// サイドメニュー
	protected DrawerLayout mDrawerLayout;
	private ISideMenuView mSideMenuFrame;
	private ActionBarDrawerToggle mDrawerToggle;
	private final Handler mUiHandler = new Handler();

	public SideMenuFragment() {
		super();
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
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (mDrawerToggle != null) {
			mDrawerToggle.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	protected void prepareSideMenu(final View rootView) {
		// ListViewのインスタンスを取得
		mSideMenuFrame = (ISideMenuView)rootView.findViewById(R.id.sidemenu_frame);
		if (mSideMenuFrame != null) {
			mSideMenuFrame.setOnSidleMenuListener(mOnSidleMenuListener);
			// ドローワーの設定
			mDrawerLayout = (DrawerLayout) rootView.findViewById(R.id.drawer_layout);
			mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout, R.string.app_name, R.string.app_name) {
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
	/*			@Override
				public void onDrawerSlide(final View drawerView, final float slideOffset) {
					super.onDrawerSlide(drawerView, slideOffset);
				} */
	/*			@Override
				public void onDrawerStateChanged(final int newState) {
					// 表示済み、閉じ済みの状態：0
					// ドラッグ中状態:1
					// ドラッグを放した後のアニメーション中：2
					super.onDrawerStateChanged(newState);
				} */
	/*			@Override
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
		public void onSideMenuHide(final View view) {
			if (DEBUG) Log.v(TAG, "onSideMenuHide:");
			closeSideMenu();
		}
	};

	/**
	 * サイドメニューの内容を更新
	 */
	protected void updateSideMenu() {
		if (DEBUG) Log.v(TAG, "updateSideMenu:");
		openSideMenu();
		if (mDrawerToggle != null) {
			mDrawerToggle.syncState();
		}
	}

	/**
	 * サイドメニューを開く
	 */
	protected void openSideMenu() {
		if (DEBUG) Log.v(TAG, "openSideMenu:");
		if (mSideMenuFrame != null) {
			mUiHandler.removeCallbacks(mShowSideMenuTask);
			mUiHandler.removeCallbacks(mHideSideMenuTask);
			mSideMenuFrame.reset();
			if (mDrawerLayout.getDrawerLockMode(Gravity.LEFT) == DrawerLayout.LOCK_MODE_UNLOCKED) {
				mDrawerToggle.setDrawerIndicatorEnabled(true);
				mUiHandler.post(mShowSideMenuTask);
			}
		}
	}

	/**
	 * サイドメニューを閉じる
	 */
	protected void closeSideMenu() {
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
	 * @param enable
	 */
	protected void setSideMenuEnable(final boolean enable) {
		if (DEBUG) Log.v(TAG, "setSideMenuEnable:" + enable);
		if (mSideMenuFrame != null) {
			final int mode = mDrawerLayout.getDrawerLockMode(Gravity.LEFT);
			mDrawerLayout.setDrawerLockMode(
				enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
			mDrawerToggle.setDrawerIndicatorEnabled(enable);
			if (enable && (mode != DrawerLayout.LOCK_MODE_UNLOCKED)) {
				openSideMenu();
			}
			mDrawerToggle.syncState();
		}
	}

}
