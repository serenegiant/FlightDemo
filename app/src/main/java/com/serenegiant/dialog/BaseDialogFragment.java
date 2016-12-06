package com.serenegiant.dialog;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: BaseDialogFragment.java
 *
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
