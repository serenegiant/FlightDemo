package com.serenegiant.autoparrot;

import android.app.Fragment;

public class MainActivity extends AbstractMainActivity {
	// ActionBarActivityを継承するとPilotFragmentから戻る際にクラッシュする
	// Fragmentが切り替わらずに処理中にもかかわらずActivityが破棄されてしまう
	private static final boolean DEBUG = false;    // FIXME 実働時はfalseにすること
	private static String TAG = MainActivity.class.getSimpleName();

	protected Fragment createConnectionFragment() {
		return ConnectionFragment.newInstance(true);	// XXX NewAPIを使うかどうか
	}

}
