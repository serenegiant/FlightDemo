package com.serenegiant.flightdemo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Locale;

public class HelpFragment extends BaseFragment {
	private static final boolean DEBUG = false;  // FIXME 実働時はfalseにすること
	private static final String TAG = HelpFragment.class.getSimpleName();

	public static final int SCRIPT_HELP_SCRIPTS = 0;
	public static final int SCRIPT_HELP_ERROR = 1;

	private static final String KEY_SCRIPT_HELP_TYPE = "KEY_SCRIPT_HELP_TYPE";
	public static final HelpFragment newInstance(final int type) {
		final HelpFragment fragment = new HelpFragment();
		Bundle args = fragment.getArguments();
		if (args == null) {
			args = new Bundle();
		}
		fragment.mType = type;
		args.putInt(KEY_SCRIPT_HELP_TYPE, type);
		fragment.setArguments(args);
		return fragment;
	}

	private int mType;

	public HelpFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(KEY_SCRIPT_HELP_TYPE, mType);
	}

	@Override
	public synchronized void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
		final View rootView = local_inflater.inflate(R.layout.fragment_help, container, false);
		init(rootView);
		return rootView;
	}

	@Override
	protected void loadArguments(final Bundle savedInstanceState) {
		Bundle args = savedInstanceState;
		if (args == null) {
			args = getArguments();
		}
		mType = args.getInt(KEY_SCRIPT_HELP_TYPE, SCRIPT_HELP_SCRIPTS);
	}

	private void init(final View rootView) {
		final WebView webview = (WebView)rootView.findViewById(R.id.webview);
		// WebViewの設定をするためにWebSettingsオブジェクトを取得
	    final WebSettings settings = webview.getSettings();
	    // JavaScript有効・無効の設定
	    settings.setJavaScriptEnabled(false);
	    // パスワードの保存の有効・無効の設定
	    settings.setSavePassword(false);
	    // フォームデータの保存の有効・無効の設定
	    settings.setSaveFormData(false);
	    // ズーム機能の有効・無効の設定
	    settings.setSupportZoom(false);
	    // テキストエンコーディングの設定
	    settings.setDefaultTextEncodingName("utf-8");
	    // WebViewの通知リクエストの処理
		webview.setWebViewClient(mWebViewClient);
		// 背景を透過
		webview.setBackgroundColor(0x00000000);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			webview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
		}
		// アセット内のhtmlファイルを指定する
		String url = null;
		final boolean isJapanese = Locale.getDefault().equals(Locale.JAPAN);
		switch (mType) {
		case SCRIPT_HELP_SCRIPTS:
			url = isJapanese ? "file:///android_asset/help/help_jp.html" : "file:///android_asset/help/help.html";
			break;
		case SCRIPT_HELP_ERROR:
			url = isJapanese ? "file:///android_asset/help/help_jp.html" : "file:///android_asset/help/help.html";
			break;
		}
		if (DEBUG) Log.v(TAG, "url=" + url);
		if (!TextUtils.isEmpty(url)) {
			webview.loadUrl(url);
		}
	}

	private final WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
			return false;	// 未処理の時はfalseを返す・・・WebView内で処理される
//			return super.shouldOverrideUrlLoading(view, url);
		}
	};

}
