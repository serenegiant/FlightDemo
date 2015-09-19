package com.serenegiant.flightdemo;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.serenegiant.dialog.SelectFileDialogFragment;

import java.io.File;

public class ScriptFragment extends BaseFragment implements SelectFileDialogFragment.OnFileSelectListener {
    private static final boolean DEBUG = true;  // FIXME 実働時はfalseにすること
    private static final String TAG = ScriptFragment.class.getSimpleName();

    public static ScriptFragment newInstance() {
        final ScriptFragment fragment = new ScriptFragment();
        return fragment;
    }

    public ScriptFragment() {
        // デフォルトコンストラクタが必要:
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
        final View rootView = inflater.inflate(R.layout.fragment_script, container, false);
        final ViewPager pager = (ViewPager)rootView.findViewById(R.id.pager);
        pager.setAdapter(new ScriptPagerAdapter(inflater));
        // FIXME ここでスクリプトの設定を読み込む
        return rootView;
    }

    @Override
    public void onFileSelect(File[] files) {
    }

    private void initScriptList(final View rootView) {
        final ListView listview = (ListView)rootView.findViewById(R.id.script_listview);
        final TextView tv = (TextView)rootView.findViewById(R.id.empty_view);
        listview.setEmptyView(tv);
    }

    private void initScriptError(final View rootView) {

    }

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
            switch (position) {
            case 0:
                result = getString(R.string.script_list);
                break;
            case 1:
                result = getString(R.string.script_error);
                break;
            }
            return result;
        }
    }
}
