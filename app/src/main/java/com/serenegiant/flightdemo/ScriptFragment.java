package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.serenegiant.dialog.SelectFileDialogFragment;
import com.serenegiant.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScriptFragment extends BaseFragment implements SelectFileDialogFragment.OnFileSelectListener {
    private static final boolean DEBUG = true;  // FIXME 実働時はfalseにすること
    private static final String TAG = ScriptFragment.class.getSimpleName();

    public static ScriptFragment newInstance() {
        final ScriptFragment fragment = new ScriptFragment();
        return fragment;
    }

	private final List<ScriptHelper.ScriptRec> mScripts = new ArrayList<ScriptHelper.ScriptRec>();
    private SharedPreferences mPref;
	private ScriptHelper.ScriptListAdapter mScriptListAdapter;

    public ScriptFragment() {
		super();
        // デフォルトコンストラクタが必要:
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPref = getActivity().getPreferences(0);
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
        final View rootView = inflater.inflate(R.layout.fragment_script, container, false);
        final ViewPager pager = (ViewPager)rootView.findViewById(R.id.pager);
        pager.setAdapter(new ScriptPagerAdapter(inflater));
        // スクリプトの設定を読み込む
        ScriptHelper.loadScripts(mPref, mScripts);
        return rootView;
    }

    @Override
    public void onFileSelect(final File[] files) {
		if (DEBUG) Log.v(TAG, "onFileSelect:");
		final int n = files != null ? files.length : 0;
		boolean added = ScriptHelper.addScripts(files, mScripts);
		if (added && (mScriptListAdapter != null)) {
			mScriptListAdapter.notifyDataSetChanged();
		}
    }

	@Override
	public synchronized void onPause() {
		ScriptHelper.saveScripts(mPref, mScripts);
		super.onPause();
	}

    private void initScriptList(final View rootView) {
        final ListView listview = (ListView)rootView.findViewById(R.id.script_listview);
        final TextView tv = (TextView)rootView.findViewById(R.id.empty_view);
        listview.setEmptyView(tv);
		mScriptListAdapter = new ScriptHelper.ScriptListAdapter(getActivity(), R.layout.list_item_1line, mScripts);
		listview.setAdapter(mScriptListAdapter);
		ImageButton button = (ImageButton)rootView.findViewById(R.id.load_btn);
		button.setOnClickListener(mOnClickListener);
		button = (ImageButton)rootView.findViewById(R.id.help_btn);
		button.setOnClickListener(mOnClickListener);
    }

	private TextView mErrorTextView;
    private void initScriptError(final View rootView) {
		mErrorTextView = (TextView)rootView.findViewById(R.id.error_textview);
		ImageButton button = (ImageButton)rootView.findViewById(R.id.save_btn);
		button.setOnClickListener(mOnClickListener);
		button = (ImageButton)rootView.findViewById(R.id.help_btn);
		button.setOnClickListener(mOnClickListener);
    }

	private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(final View view) {
			switch (view.getId()) {
			case R.id.load_btn:
				setColorFilter((ImageView)view);
				final File root = FileUtils.getCaptureDir(getActivity(), "Documents", false);
				SelectFileDialogFragment.showDialog(ScriptFragment.this, root.getAbsolutePath(), false, "script");
				break;
			case R.id.save_btn:
				break;
			case R.id.help_btn:
				replace(HelpFragment.newInstance(HelpFragment.SCRIPT_HELP_SCRIPTS));
				break;
			}
		}
	};

    /**
     * 設定画面の各ページ用のViewを提供するためのPagerAdapterクラス
     */
    private class ScriptPagerAdapter extends PagerAdapter {
        private final LayoutInflater mInflater;
        public ScriptPagerAdapter(final LayoutInflater inflater) {
            super();
            mInflater = inflater;
        }

        @Override
        public synchronized Object instantiateItem(final ViewGroup container, final int position) {
            if (DEBUG) Log.v(TAG, "instantiateItem:position=" + position);
            View view = null;
            switch (position) {
            case 0:
                view = mInflater.inflate(R.layout.fragment_script_list, container, false);
                initScriptList(view);
                break;
            case 1:
                view = mInflater.inflate(R.layout.fragment_script_error, container, false);
                initScriptError(view);
                break;
            }
            if (view != null) {
                container.addView(view);
            }
            return view;
        }

        @Override
        public synchronized void destroyItem(final ViewGroup container, final int position, final Object object) {
            if (DEBUG) Log.v(TAG, "destroyItem:position=" + position);
            if (object instanceof View) {
                container.removeView((View)object);
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(final View view, final Object object) {
            return view.equals(object);
        }

        @Override
        public CharSequence getPageTitle(final int position) {
//          if (DEBUG) Log.v(TAG, "getPageTitle:position=" + position);
            CharSequence result = null;
/*			switch (position) {
            case 0:
                result = getString(R.string.script_list);
                break;
            case 1:
                result = getString(R.string.script_error);
                break;
            } */
            return result;
        }
    }
}
