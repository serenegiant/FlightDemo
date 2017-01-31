package com.serenegiant.aceparrot;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.serenegiant.widget.ColorPickerView;
import com.serenegiant.widget.RelativeRadioGroup;
import static com.serenegiant.aceparrot.AppConst.*;

public class ConfigAppFragment extends BaseFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = ConfigAppFragment.class.getSimpleName();

	public static ConfigAppFragment newInstance() {
		final ConfigAppFragment fragment = new ConfigAppFragment();
		return fragment;
	}

	private static PagerAdapterConfig[] PAGER_CONFIG_APP;
	static {
		PAGER_CONFIG_APP = new PagerAdapterConfig[3];
		PAGER_CONFIG_APP[0] = new PagerAdapterConfig(R.string.config_app_title_color, R.layout.config_app_color, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigAppFragment)parent).initColor(view);
			}
		});
		PAGER_CONFIG_APP[1] = new PagerAdapterConfig(R.string.config_app_title_others, R.layout.config_app_others, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigAppFragment)parent).initOthers(view);
			}
		});
		PAGER_CONFIG_APP[2] = new PagerAdapterConfig(R.string.config_app_title_license, R.layout.config_app_license, new PagerAdapterItemHandler() {
			@Override
			public void initialize(final BaseFragment parent, final View view) {
				((ConfigAppFragment)parent).initLicense(view);
			}
		});
	}

	private SharedPreferences mPref;
	private int mColor;
	private boolean mAutoHide;
	private boolean mOfflineVoiceRecognition;
	private boolean mScriptVoiceRecognition;

	public ConfigAppFragment() {
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
     	final View rootView = local_inflater.inflate(R.layout.fragment_config_app, container, false);
		final ConfigPagerAdapter adapter = new ConfigPagerAdapter(this, inflater, PAGER_CONFIG_APP);
		final ViewPager pager = (ViewPager)rootView.findViewById(R.id.pager);
		pager.setAdapter(adapter);
		return rootView;
	}

	@SuppressWarnings("deprecation")
	private void initColor(final View rootView) {
		final RelativeRadioGroup group = (RelativeRadioGroup)rootView.findViewById(R.id.icon_radiogroup);

		switch (mPref.getInt(KEY_ICON_TYPE, 100)) {
		case 1:		// 001
			group.check(R.id.icon_001_radiobutton);
			break;
		case 2:		// 002
			group.check(R.id.icon_002_radiobutton);
			break;
//		case 0:
		default:	// 通常
			group.check(R.id.icon_000_radiobutton);
			break;
		}
		group.setOnCheckedChangeListener(mOnRadioButtonCheckedChangeListener);
// 機体色設定
		mColor = mPref.getInt(KEY_COLOR, getResources().getColor(R.color.RED));
		final ColorPickerView picker = (ColorPickerView)rootView.findViewById(R.id.color_picker);
		picker.setColor(mColor);
		picker.showAlpha(false);
		picker.setColorPickerListener(mColorPickerListener);
	}

	private void initOthers(final View rootView) {
// アイコンを自動的に隠す設定
		mAutoHide = mPref.getBoolean(KEY_AUTO_HIDE, false);
		Switch sw = (Switch)rootView.findViewById(R.id.icon_auto_hide_switch);
		sw.setChecked(mAutoHide);
		sw.setOnCheckedChangeListener(mOnCheckedChangeListener);
// オフライン音声認識を優先するかどうか(Android>=6)
		mOfflineVoiceRecognition = mPref.getBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, false)
			&& Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
		sw = (Switch)rootView.findViewById(R.id.enable_offline_voice_recognition_switch);
		sw.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
		sw.setChecked(mOfflineVoiceRecognition);
		sw.setOnCheckedChangeListener(mOnCheckedChangeListener);
// 音声認識でのスクリプト飛行を有効にするかどうか
		mScriptVoiceRecognition = mPref.getBoolean(KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT, false);
		sw = (Switch)rootView.findViewById(R.id.enable_voice_recognition_script_switch);
		sw.setChecked(mScriptVoiceRecognition);
		sw.setOnCheckedChangeListener(mOnCheckedChangeListener);
	}

	private void initLicense(final View rootView) {
	}

	private final RelativeRadioGroup.OnCheckedChangeListener mOnRadioButtonCheckedChangeListener
		= new RelativeRadioGroup.OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(final RelativeRadioGroup group, final int checkedId) {
			switch (checkedId) {
			case R.id.icon_000_radiobutton:
				mPref.edit().putInt(KEY_ICON_TYPE, 0).apply();
				break;
			case R.id.icon_001_radiobutton:
				mPref.edit().putInt(KEY_ICON_TYPE, 1).apply();
				break;
			case R.id.icon_002_radiobutton:
				mPref.edit().putInt(KEY_ICON_TYPE, 2).apply();
				break;
			}
		}
	};

	private final ColorPickerView.ColorPickerListener mColorPickerListener
		= new ColorPickerView.ColorPickerListener() {
		@Override
		public void onColorChanged(final ColorPickerView view, final int color) {
			if (mColor != color) {
				mColor = color;
				mPref.edit().putInt(KEY_COLOR, color).apply();
				TextureHelper.clearTexture(getActivity());
			}
		}
	};

	private final CompoundButton.OnCheckedChangeListener mOnCheckedChangeListener
		= new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
			switch (buttonView.getId()) {
			case R.id.icon_auto_hide_switch:
			{
				if (mAutoHide != isChecked) {
					mAutoHide = isChecked;
					mPref.edit().putBoolean(KEY_AUTO_HIDE, isChecked).apply();
				}
				break;
			}
			case R.id.enable_offline_voice_recognition_switch:
			{
				if (mOfflineVoiceRecognition != isChecked) {
					mOfflineVoiceRecognition = isChecked;
					mPref.edit().putBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, isChecked).apply();
				}
				break;
			}
			}
		}
	};
}
