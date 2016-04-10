package com.serenegiant.dialog;

/*
 * Copyright (c) 2014 saki t_saki@serenegiant.com
 *
 * File name: SelectFileDialogFragment.java
 *
*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.serenegiant.flightdemo.R;
import com.serenegiant.utils.BuildCheck;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.regex.Pattern;


//Fragmentのライフサイクル
//onAttach() > onCreate() > onCreateView() > onActivityCreated() > onStart()
//onResume() > onPause() > onBeforeStop() > onDestroyView() > onDestroy() > onDetach()

public class SelectFileDialogFragment extends BaseDialogFragment
	implements OnItemClickListener, OnClickListener {
	private static final boolean DEBUG = false;	// TODO 実働時はfalseにすること
	public static final String TAG = "SelectFileDialog";

	/**
	 * 選択したファイルの情報を取り出すためのリスナーインターフェース
	 */
	public interface OnFileSelectListener {
		// ファイルが選択されたときに呼び出される関数
		public void onFileSelect(File[] files);
	}

	protected OnFileSelectListener mOnFileSelectListener;	// 結果受取先
	final Locale mLocale = Locale.getDefault();
	private AlertDialog mDialog;							// ダイアログ
	private FileInfoArrayAdapter mAdapter;					// ファイル情報配列アダプタ
	private String mRootDir, mCurrentDir, mExts;
	private String[] mFilter = null;						// 拡張子選択フィルター文字列
	private boolean mIsMultiSelect = false;
	private Button mOkButton;
	private int mSelectNum = 0;
	private List<FileInfo> mListFileInfo = null;

	//**********************************************************************
	//
	//**********************************************************************
	public class FileInfo implements Comparable<FileInfo> {

		private final String fileName;			// 表示名
		private final File file;					// ファイルオブジェクト
		private boolean isSelected = false;	// 選択状態

		// コンストラクタ
		public FileInfo(final String fileName, final File file) {
			this.fileName = fileName;
			this.file = file;
		}

		public String getName() {
			return fileName;
		}

		public File getFile() {
			return file;
		}

		// 比較
		@Override
		public int compareTo(final FileInfo other) {
			// ディレクトリ < ファイル の順
			if ((file.isDirectory()) && (!other.getFile().isDirectory())) {
				return -1;
			}
			if ((!file.isDirectory()) && (other.getFile().isDirectory())) {
				return 1;
			}

			// ファイル同士、ディレクトリ同士の場合は、ファイル名（ディレクトリ名）の大文字小文字区別しない辞書順
			return file.getName().toLowerCase(mLocale).compareTo(
				other.getFile().getName().toLowerCase(mLocale)
			);
		}
	}

	//**********************************************************************
	//
	//**********************************************************************
	public class FileInfoArrayAdapter extends ArrayAdapter<FileInfo> {
		private final List<FileInfo> fileInfoList; // ファイル情報リスト

		// コンストラクタ
		public FileInfoArrayAdapter(final Context context, final List<FileInfo> list) {
			super(context, -1, list);

			fileInfoList = list;
		}

		// 一番上の選択されたアイテムの位置の取得
		public int getFirstSelectedItemPosition() {
			final ListIterator<FileInfo> it = fileInfoList.listIterator();
			while (it.hasNext()) {
				final FileInfo fileinfo = it.next();
				if (fileinfo.isSelected) {
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}

		// positionで指定したアイテムの次の選択されたアイテムの位置の取得
		public int getNextSelectedItemPosition(final int iPosition) {
			final ListIterator<FileInfo> it = fileInfoList.listIterator(iPosition);
			// 一つ進める
			it.next();
			while (it.hasNext()) {
				final FileInfo fileinfo = it.next();
				if (fileinfo.isSelected) {
					return it.nextIndex() - 1;
				}
			}
			return -1;
		}

		// listFileInfoの一要素の取得
		@Override
		public FileInfo getItem(final int position) {
			return fileInfoList.get(position);
		}

		// 一要素のビューの生成
		@Override
		public View getView(final int position, View convertView, final ViewGroup parent) {
			// レイアウトの生成
			if (convertView == null) {
				final Context context = getContext();
				// レイアウト
				final LinearLayout layout = new LinearLayout(context);
				layout.setPadding(10, 10, 10, 10);
				layout.setBackgroundColor(Color.WHITE);
				convertView = layout;
				// テキスト
				final TextView textview = new TextView(context);
				textview.setTag("text");
				textview.setTextColor(Color.BLACK);
				textview.setPadding(10, 10, 10, 10);
				layout.addView(textview);
			}

			// 値の指定
			final FileInfo fileinfo = fileInfoList.get(position);
			final TextView textview = (TextView)convertView.findViewWithTag("text");
			if (fileinfo.getFile().isDirectory()) {
				// ディレクトリの場合は、名前の後ろに「/」を付ける
				textview.setText( fileinfo.getName() + "/");
			} else {
				textview.setText(fileinfo.getName());
			}

			if (mIsMultiSelect) {
				// 選択行と非選択行
				if (fileinfo.isSelected) {
					// 選択行：青背景、白文字
					convertView.setBackgroundColor(Color.CYAN);
					textview.setTextColor(Color.WHITE);
				} else {
					// 非選択行：白背景、黒文字
					convertView.setBackgroundColor(Color.WHITE);
					textview.setTextColor(Color.BLACK);
				}
			}
			return convertView;
		}
	}

	//**********************************************************************
	//
	//**********************************************************************
/*	public static SelectFileDialogFragment showDialog(
		final Activity parent, final String rootDir, final boolean isMultiSelect, final String exts) {

		SelectFileDialogFragment dialog = (SelectFileDialogFragment)newInstance(-1, rootDir, isMultiSelect, exts);
	    try {
	    	dialog.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			dialog = null;
		}
		return dialog;
	} */

	public static SelectFileDialogFragment showDialog(
			final Activity parent, final String rootDir, final boolean isMultiSelect, final String exts) {

			SelectFileDialogFragment dialog = (SelectFileDialogFragment)newInstance(-1, rootDir, isMultiSelect, exts);
		    try {
		    	dialog.show(parent.getFragmentManager(), TAG);
			} catch (final IllegalStateException e) {
				dialog = null;
			}
			return dialog;
		}

	public static SelectFileDialogFragment showDialog(
		final Activity parent, final String rootDir, final boolean isMultiSelect) {

		return showDialog(parent, rootDir, isMultiSelect, null);
	}

	public static SelectFileDialogFragment showDialog(final Activity parent, final String rootDir) {
		return showDialog(parent, rootDir, false, null);
	}

	public static SelectFileDialogFragment showDialog(
		final Fragment parent, final String rootDir, final boolean isMultiSelect, final String exts) {

		SelectFileDialogFragment dialog = (SelectFileDialogFragment)newInstance(parent.getId(), rootDir, isMultiSelect, exts);
	    dialog.setTargetFragment(parent, parent.getId());
		try {
			dialog.show(parent.getFragmentManager(), TAG);
		} catch (final IllegalStateException e) {
			dialog = null;
		}
		return dialog;
	}

	public static SelectFileDialogFragment showDialog(
		final Fragment parent, final String rootDir, final boolean isMultiSelect) {

		return showDialog(parent, rootDir, isMultiSelect, null);
	}

	public static SelectFileDialogFragment showDialog(final Fragment parent, final String rootDir) {
		return showDialog(parent, rootDir, false, null);
	}

	//**********************************************************************
	//
	//**********************************************************************
	public static DialogFragment newInstance(final int requestID, final String rootDir, final boolean isMultiSelect, final String exts) {
		if (DEBUG) Log.v(TAG, "newInstance");
		final SelectFileDialogFragment frag = new SelectFileDialogFragment();
		final Bundle args = saveArgument(requestID, rootDir, null);
        args.putString("root", rootDir);
        args.putString("currentDir", rootDir);
        args.putString("exts", exts);
        args.putBoolean("isMultiSelect", isMultiSelect);
        frag.setArguments(args);
        return frag;
	}

	@Override
	public void onSaveInstanceState(final Bundle args) {
		super.onSaveInstanceState(args);
		if (DEBUG) Log.v(TAG, "onSaveInstanceState");
        args.putString("root", mRootDir);
        args.putString("currentDir", mRootDir);
        args.putString("exts", mExts);
        args.putBoolean("isMultiSelect", mIsMultiSelect);
		args.putString("currentDir", mCurrentDir);
	}

	@SuppressLint("NewApi")
	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);
		if (DEBUG) Log.v(TAG, "onAttach");
        // コールバックインターフェースを取得
    	try {
    		// 親がフラグメントの場合
    		mOnFileSelectListener = (OnFileSelectListener)getTargetFragment();
    	} catch (final NullPointerException e1) {
    	} catch (final ClassCastException e) {
    	}
        if ((mOnFileSelectListener == null) && BuildCheck.isAndroid4_2())
    	try {
    		// 親がフラグメントの場合
    		mOnFileSelectListener = (OnFileSelectListener)getParentFragment();
    	} catch (final NullPointerException e1) {
    	} catch (final ClassCastException e) {
    	}
        if (mOnFileSelectListener == null)
        try {
        	// 親がActivityの場合
        	mOnFileSelectListener = (OnFileSelectListener)activity;
        } catch (final ClassCastException e) {
    	} catch (final NullPointerException e1) {
        }
		if (mOnFileSelectListener == null) {
        	throw new ClassCastException(activity.toString() + " must implement OnFileSelectListener");
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 通常起動の場合はsavedInstanceState==null,
		// システムに破棄されたのが自動生成した時は
		// onSaveInstanceStateで保存した値が入ったBundleオブジェクトが入っている
		savedInstanceState = loadArgument(savedInstanceState);
		mRootDir = savedInstanceState.getString("root");
		mCurrentDir = savedInstanceState.getString("currentDir");
		mExts = savedInstanceState.getString("exts");
		mIsMultiSelect = savedInstanceState.getBoolean("isMultiSelect");
	}

	@Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
		if (DEBUG) Log.v(TAG, "onCreateDialog");

		setFilter(mExts);
		mSelectNum = 0;	// 選択したファイル数をクリア
		final View rootView = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_fileselect, null, false);
		final ListView listview = (ListView)rootView.findViewById(R.id.listView);
		listview.setScrollingCacheEnabled(false);
		listview.setOnItemClickListener(this);
		listview.setEmptyView(rootView.findViewById(R.id.empty_view));

		mListFileInfo = new ArrayList<FileInfo>();
		changeDir(new File(mCurrentDir));	// 初期ディレクトリを取得

		mAdapter = new FileInfoArrayAdapter(getActivity(), mListFileInfo);
		listview.setAdapter(mAdapter);

		final Builder builder = new Builder(getActivity());
		builder.setTitle(mCurrentDir);
		builder.setPositiveButton(android.R.string.cancel, null);
		if (mIsMultiSelect)
			builder.setNegativeButton(R.string.select, null);
		builder.setView(rootView);	//		builder.setView(listview);
		builder.setCancelable(false);
		mDialog = builder.create();

		// Builder#setPositiveButton() でリスナーを指定した場合、ダイアログは閉じてしまう。
		// ファイルを一つも選択していない場合は、ダイアログを閉じないようにしたいので、
		// Button#setOnClickListener() でリスナーを指定する。
		if (mIsMultiSelect) {
			mOkButton = mDialog.getButton(DialogInterface.BUTTON_POSITIVE);	// キャンセル
			mOkButton.setOnClickListener(this);
		}
		return mDialog;
	}

	// ディレクトリを変更
	private void changeDir(final File newDir) {
		mListFileInfo.clear();

		// ファイルリスト
		final File[] fileList = newDir.listFiles(getFileFilter());
		if (null != fileList) {
			for (final File fileTemp : fileList) {
//				if (fileTemp.isDirectory())
					mListFileInfo.add(new FileInfo(fileTemp.getName(), fileTemp));
			}
			Collections.sort(mListFileInfo);
		}
		// 親フォルダに戻るパスの追加
		if (!mRootDir.equals(newDir.toString())) {
			final String parent = newDir.getParent();
			if ((null != parent) && (!parent.equals(newDir.toString()))) {
				mListFileInfo.add(0, new FileInfo(
						getActivity().getResources().getString(R.string.parent_dir),
					new File(newDir.getParent())));
			}
		}
		mCurrentDir = newDir.getAbsolutePath();
		if (mDialog != null)
			mDialog.setTitle(mCurrentDir);
		if (mAdapter != null)
			mAdapter.notifyDataSetChanged();
	}

	public void setFilter(final String extString) {
		if (!TextUtils.isEmpty(extString)) {
			final Pattern p = Pattern.compile("[;\\s]+");	// セミコロンor空白で区切る
			mFilter = p.split(extString.toLowerCase(mLocale));
		}
	}

	// FileFilterオブジェクトの生成
	private FileFilter getFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(final File file) {
				if (mFilter == null) {
					return true;	// フィルタ無しのときはtrue
				}
				if (file.isDirectory()) {
					return true;	// ディレクトリのときは、true
				}
				for (final String str : mFilter) {
					if (file.getName().toLowerCase(mLocale).endsWith("." + str)) {
						return true;	// 拡張子が一致すればtrue
					}
				}
				return false;
			}
		};
	}

	// ListView内の項目をクリックしたときの処理
	@Override
	public void onItemClick(final AdapterView<?> list, final View view, final int position, final long id) {

		final FileInfo fileinfo = mAdapter.getItem(position);

		if (fileinfo.getFile().isDirectory()) {
			// ディレクトリを選択した時
			changeDir(fileinfo.getFile());
		} else {
			// ファイルを選択した時
			if (mIsMultiSelect) {	// 複数ファイル選択の時
				// ファイルをクリックしたときは、選択状態の反転と、クリック行の表示更新
				fileinfo.isSelected = !fileinfo.isSelected;
				if (fileinfo.isSelected)
					mSelectNum++;
				else
					mSelectNum--;
				list.getAdapter().getView(position, view, list);
			} else {				// 単独ファイル選択の時
				mDialog.dismiss();
				mDialog = null;
				// ファイルが選ばれた：リスナーのハンドラを呼び出す
				if (mOnFileSelectListener != null) {
					final File[] files = new File[1];
					files[0] = fileinfo.getFile();
					mOnFileSelectListener.onFileSelect(files);
				}
			}
		}
	}

	@Override
	public void onClick(final View view) {
		if (view == mOkButton) {
			// ファイルが選択されているか
			final int iPosition_first = mAdapter.getFirstSelectedItemPosition();
			int iPosition = iPosition_first;
			int iCount = 0;
			while (iPosition >= 0) {
				iPosition = mAdapter.getNextSelectedItemPosition(iPosition);
				iCount++;
			}
			if ((iCount == 0) && (mSelectNum == 0)) {
				Toast.makeText(getActivity(), R.string.not_selected, Toast.LENGTH_SHORT).show();
			} else {	// ひとつ以上選択されている
				final File[] files = new File[iCount];
				iPosition = iPosition_first;
				iCount = 0;
				while (iPosition >= 0) {
					files[iCount] = mAdapter.getItem(iPosition).getFile();
					iPosition = mAdapter.getNextSelectedItemPosition(iPosition);
					iCount++;
				}
				mDialog.dismiss();
				mDialog = null;
				// リスナーのハンドラを呼び出す
				if (mOnFileSelectListener != null) {
					mOnFileSelectListener.onFileSelect(files);
				}
			}
		}
	}

	public void setOnFileSelectListener(final OnFileSelectListener listener) {
		mOnFileSelectListener = listener;
	}

}
