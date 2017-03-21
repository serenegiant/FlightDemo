package com.serenegiant.aceparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                        (3-clause BSD License)
 *
 * Copyright (C) 2015-2017, saki t_saki@serenegiant.com
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 *   * Neither the names of the copyright holders nor the names of the contributors
 *     may be used to endorse or promote products derived from this software
 *     without specific prior written permission.
 *
 * This software is provided by the copyright holders and contributors "as is" and
 * any express or implied warranties, including, but not limited to, the implied
 * warranties of merchantability and fitness for a particular purpose are disclaimed.
 * In no event shall copyright holders or contributors be liable for any direct,
 * indirect, incidental, special, exemplary, or consequential damages
 * (including, but not limited to, procurement of substitute goods or services;
 * loss of use, data, or profits; or business interruption) however caused
 * and on any theory of liability, whether in contract, strict liability,
 * or tort (including negligence or otherwise) arising in any way out of
 * the use of this software, even if advised of the possibility of such damage.
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.serenegiant.arflight.R;
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
	private int mDampingRate;
	private TextView mDampingRateTv;

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
// 減衰率
		mDampingRateTv = (TextView) rootView.findViewById(R.id.damping_rate_textview);
		mDampingRate = mPref.getInt(KEY_CONFIG_VOICE_RECOGNITION_DAMPING_RATE, DEFAULT_VOICE_RECOGNITION_DAMPING_RATE);
		final SeekBar seekBar = (SeekBar) rootView.findViewById(R.id.damping_rate_seekbar);
		seekBar.setProgress(mDampingRate);
		seekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
		updateDampingRate(mDampingRate);
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
			if (checkedId == R.id.icon_000_radiobutton) {
				mPref.edit().putInt(KEY_ICON_TYPE, 0).apply();

			} else if (checkedId == R.id.icon_001_radiobutton) {
				mPref.edit().putInt(KEY_ICON_TYPE, 1).apply();

			} else if (checkedId == R.id.icon_002_radiobutton) {
				mPref.edit().putInt(KEY_ICON_TYPE, 2).apply();

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
			int i = buttonView.getId();
			if (i == R.id.icon_auto_hide_switch) {
				if (mAutoHide != isChecked) {
					mAutoHide = isChecked;
					mPref.edit().putBoolean(KEY_AUTO_HIDE, isChecked).apply();
				}
			} else if (i == R.id.enable_offline_voice_recognition_switch) {
				if (mOfflineVoiceRecognition != isChecked) {
					mOfflineVoiceRecognition = isChecked;
					mPref.edit().putBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, isChecked).apply();
				}
			} else if (i == R.id.enable_voice_recognition_script_switch) {
				if (mScriptVoiceRecognition != isChecked) {
					mScriptVoiceRecognition = isChecked;
					mPref.edit().putBoolean(KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT, isChecked).apply();
				}
			}
		}
	};

	private final SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener
		= new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
			updateDampingRate(progress);
		}

		@Override
		public void onStartTrackingTouch(final SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(final SeekBar seekBar) {
			final int progress = seekBar.getProgress();
			updateDampingRate(progress);
			if (mDampingRate != progress) {
				mDampingRate = progress;
				mPref.edit().putInt(KEY_CONFIG_VOICE_RECOGNITION_DAMPING_RATE, progress).apply();
			}
		}
	};

	private void updateDampingRate(final int progress) {
		if (mDampingRateTv != null) {
			final String txt = getString(R.string.config_title_damping_rate, progress / 100.0f);
			mDampingRateTv.setText(txt);
		}
	}
}
