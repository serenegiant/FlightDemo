package com.serenegiant.widget;

import android.view.View;

public interface ISideMenuView {
	/**
	 * サイドメニューの状態が変わった時のコールバックリスナー
	 */
	public interface OnSidleMenuListener {
		/**
		 * サイドメニュー項目が表示された時のコールバック
		 * @param view
		 */
		public void onSideMenuShow(View view);
		/**
		 * サイドメニュー項目がすべて非表示になった時のコールバック
		 * @param view
		 */
		public void onSideMenuHide(View view);
	}

	/**
	 * スライドアウトアニメーションを開始する
	 */
	public void hide();
	/**
	 * アニメーションをリセット
	 */
	public void reset();
	/**
	 * サイドメニューの状態が変わった時のコールバックリスナーを設定
	 * @param listener
	 */
	public void setOnSidleMenuListener(OnSidleMenuListener listener);
	/**
	 * サイドメニューの状態が変わった時のコールバックリスナーを取得
	 * @return
	 */
	public OnSidleMenuListener getOnSidleMenuListener();
}
