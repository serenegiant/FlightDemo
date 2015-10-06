package com.serenegiant.flightdemo;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.serenegiant.widget.RelativeRadioGroup;


public class ConfigIconFragment extends BaseFragment {

	public static ConfigIconFragment newInstance() {
		final ConfigIconFragment fragment = new ConfigIconFragment();
		return fragment;
	}

	private SharedPreferences mPref;

	public ConfigIconFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mPref = activity.getPreferences(0);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
//		if (DEBUG) Log.v(TAG, "onCreateView:");
		final LayoutInflater local_inflater = getThemedLayoutInflater(inflater);
     	final View rootView = local_inflater.inflate(R.layout.fragment_config_icon, container, false);
		final RelativeRadioGroup group = (RelativeRadioGroup)rootView.findViewById(R.id.icon_radiogroup);

		switch (mPref.getInt(ConfigFragment.KEY_ICON_TYPE, 100)) {
		case 1:		// 001
			group.check(R.id.icon_001_radiobutton);
			break;
		case 2:		// 002
			group.check(R.id.icon_002_radiobutton);
			break;
		case 3:		// 003
			group.check(R.id.icon_003_radiobutton);
			break;
		case 4:		// 004
			group.check(R.id.icon_004_radiobutton);
			break;
		case 5:		// 005
			group.check(R.id.icon_005_radiobutton);
			break;
		case 0:
		default:	// 通常
			group.check(R.id.icon_000_radiobutton);
			break;
		}
		group.setOnCheckedChangeListener(mOnRadioButtonCheckedChangeListener);
		return rootView;
	}

	private final RelativeRadioGroup.OnCheckedChangeListener mOnRadioButtonCheckedChangeListener
		= new RelativeRadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final RelativeRadioGroup group, final int checkedId) {
			switch (checkedId) {
			case R.id.icon_000_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 0).apply();
				break;
			case R.id.icon_001_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 1).apply();
				break;
			case R.id.icon_002_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 2).apply();
				break;
			case R.id.icon_003_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 3).apply();
				break;
			case R.id.icon_004_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 4).apply();
				break;
			case R.id.icon_005_radiobutton:
				mPref.edit().putInt(ConfigFragment.KEY_ICON_TYPE, 5).apply();
				break;
			}
		}
	};

}
