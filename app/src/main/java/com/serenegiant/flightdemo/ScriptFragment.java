package com.serenegiant.flightdemo;

public class ScriptFragment extends BaseFragment {
    private static final boolean DEBUG = true;  // FIXME 実働時はfalseにすること
    private static final String TAG = ScriptFragment.class.getSimpleName();

    public static ScriptFragment newInstance() {
        final ScriptFragment fragment = new ScriptFragment();
        return fragment;
    }

    public ScriptFragment() {
        // デフォルトコンストラクタが必要:
    }
}
