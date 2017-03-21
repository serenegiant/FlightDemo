package com.serenegiant.widget;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
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
