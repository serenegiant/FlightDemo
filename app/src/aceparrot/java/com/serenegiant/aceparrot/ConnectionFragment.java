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

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.widget.ImageButton;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;

import java.io.IOException;

public class ConnectionFragment extends AbstractConnectionFragment {

	public static ConnectionFragment newInstance() {
		return new ConnectionFragment();
	}

	private ImageButton mVoicePilotBtn, mScriptBtn;

	public ConnectionFragment() {
		super();
	}

	/**
	 * Viewを初期化
	 * @param rootView
	 */
	protected void initView(final View rootView) {
		super.initView(rootView);
		mVoicePilotBtn = (ImageButton)rootView.findViewById(R.id.voice_pilot_button);
		if (mVoicePilotBtn != null) {
			mVoicePilotBtn.setOnClickListener(mOnClickListener);
			mVoicePilotBtn.setOnLongClickListener(mOnLongClickListener);
		}

		mScriptBtn = (ImageButton)rootView.findViewById(R.id.script_button);
		if (mScriptBtn != null) {
			mScriptBtn.setOnClickListener(mOnClickListener);
			mScriptBtn.setOnLongClickListener(mOnLongClickListener);
		}
	}

	@Override
	protected void updateButtonsOnUiThread(final boolean visible) {
		super.updateButtonsOnUiThread(visible);
		final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
		if (mVoicePilotBtn != null) {
			mVoicePilotBtn.setVisibility(visibility);
		}
	}

	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		switch (view.getId()) {
		case R.id.pilot_button:
			if (checkPermissionLocation()) {
				fragment = getFragment(position, true, false);
			}
			break;
		case R.id.voice_pilot_button:
			if (checkPermissionLocation() && checkPermissionAudio()) {
				fragment = getFragment(position, true, true);
			}
			break;
		case R.id.download_button:
			if (checkPermissionWriteExternalStorage()) {
				fragment = getFragment(position, false, false);
			}
			break;
		case R.id.gallery_button:
			if (checkPermissionWriteExternalStorage()) {
				fragment = GalleyFragment.newInstance();
			}
			break;
		case R.id.script_button:
			if (checkPermissionWriteExternalStorage()) {
				fragment = ScriptFragment.newInstance();
			}
			break;
		case R.id.config_show_btn:
			fragment = ConfigAppFragment.newInstance();
			break;
		}
		replace(fragment);
	}

	protected boolean onLongClick(final View view, final int position) {
		return false;
	}

	@Override
	protected BaseBridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device) {
		return BridgeFragment.newInstance(device);
	}

	@Override
	protected void setDataSource(final Context context,
		final MediaPlayer media_player) throws IOException {

		media_player.setDataSource(context,
			Uri.parse("android.resource://" + getActivity().getPackageName()
				+ "/" + R.raw.into_the_sky));
	}
}
