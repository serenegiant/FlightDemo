package com.serenegiant.dialog;
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

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

public class BaseDialogFragment extends DialogFragment {
//	private static final boolean DEBUG = false;	// TODO 実働時はfalseにすること
//	private static final String TAG = DEBUG ? "BaseDialogFragment" : null;

	protected int mRequestID;	// 呼び出し元識別用ID 2013/06/12追加
	protected String mTitle;
	protected String mMessage;

	/**
	 * 共通パラメータの保存処理を行うのでoverrideする際には必ず上位クラスのonSaveInstanceStateを呼ぶこと</br>
	 * getArgumentsで取得できるBundle(setArgumentしたBundle)にセットしてある値は自動的に引き継ぐので、
	 * 変更がなければoverrideする必要はない
	*/
	@Override
	public void onSaveInstanceState(final Bundle saveInstanceState) {
		super.onSaveInstanceState(saveInstanceState);
//		if (DEBUG) Log.v(TAG, "onSaveInstanceState");
		final Bundle args = getArguments();
		if (args != null) {
			args.putBoolean("isRestored", true);
			saveInstanceState.putAll(args);
		}
		saveInstanceState.putInt("requestID", mRequestID);
		saveInstanceState.putString("title", mTitle);
		saveInstanceState.putString("message", mMessage);
		saveInstanceState.putBoolean("isRestored", true);
	}

	/**
	 * このフラグメントへのパラメータ保持するためのBundleインスタンスへ共通パラメータをセットして返す</br>
	 * 必要であれば追加のパラメータをセットしてからsetArgumentに渡す
	 * @param requestID 呼び出し元識別用ID
	 * @param title
	 * @param message
	 * @return Bundle
	 */
	protected static Bundle saveArgument(final int requestID, final String title, final String message) {
		final Bundle args = new Bundle();
		args.putInt("requestID", requestID);
        args.putString("title", title);
        args.putString("message", message);
		return args;
	}

	/**
	 * このフラグメントへのパラメータを保持するBundleオブジェクトから共通パラメータを読み込む
	 * @param args
	 * @return
	 */
	protected Bundle loadArgument(Bundle args) {
		if (args == null)
			args = getArguments();
		if (args != null) {
			mRequestID = args.getInt("requestID");
			mTitle = args.getString("title");
			mMessage = args.getString("message");
		}
		final Dialog dialog = getDialog();
		if ((dialog != null) && (mTitle != null)) {
			dialog.setTitle(mTitle);
		}
		return args;
	}

	/**
	 * システムによりrestoreされたかどうかを返す</br>
	 * @return
	 */
	public boolean isRestored() {
		final Bundle args = getArguments();
		if (args != null) {
//			if (DEBUG) Log.v(TAG, "isRestored:args=" + args + " isRestored=" + args.getBoolean("isRestored"));
			return args.getBoolean("isRestored");
		} else
			return true;
	}

	public CharSequence getTitle() {
		if (TextUtils.isEmpty(mTitle))
			loadArgument(null);
		return mTitle;
	}

	public CharSequence getMessage() {
		if (TextUtils.isEmpty(mMessage))
			loadArgument(null);
		return mMessage;
	}


	/**
	 * containerViewIdで指定したViewgroup内に存在するフラグメントを全て削除した後指定したフラグメントを追加する</br>
	 * DialogFragment#onCreateDialogで生成したダイアログフラグメントはちゃんと表示されないみたい</br>
	 * supportLibraryの問題かもしれないけど。Fragment#onCreateViewで生成すること
	 * @param containerViewId
	 * @param fragment
	 * @return
	 */
/*	protected Fragment replace(int containerViewId, Fragment fragment, CharSequence title) {
		return replace(containerViewId, fragment, title, null);
	} */

	/**
	 * containerViewIdで指定したViewgroup内に存在するフラグメントを全て削除した後指定したフラグメントをタグ付きで追加する</br>
	 * DialogFragment#onCreateDialogで生成したダイアログフラグメントはちゃんと表示されないみたい</br>
	 * supportLibraryの問題かもしれないけど。Fragment#onCreateViewで生成すること
	 * @param containerViewId
	 * @param fragment
	 * @param tag
	 * @return
	 */
	protected Fragment replace(final int containerViewId, final Fragment fragment, final CharSequence title, final String tag) {
		if (fragment != null) {
			fragment.setTargetFragment(this, 0);
			final FragmentTransaction transaction = getFragmentManager().beginTransaction();
			transaction.setBreadCrumbTitle(title);
			transaction.replace(containerViewId, fragment, tag);
			transaction.addToBackStack(null);
			transaction.commit();
		}
		return fragment;
	}

	/**
	 * 指定したviewのtagがIntegerのインスタンスならその値を返す
	 * @param view
	 * @return
	 */
	protected static int getTagValueInt(final View view) {
		int result = -1;
		final Object obj = view.getTag();
		if (obj instanceof Integer) {
			result = (Integer)obj;
		}
		return result;
	}

}
