package com.serenegiant.gamepaddiag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MainFragment2 extends BaseFragment {

	public MainFragment2() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main2, container, false);
		return rootView;
	}

	@Override
	protected void updateButtons(final boolean[] downs, final long[] counts, final int[] analog_sticks) {

	}
}
