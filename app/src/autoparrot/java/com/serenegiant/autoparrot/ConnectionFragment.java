package com.serenegiant.autoparrot;
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

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.aceparrot.AbstractConnectionFragment;
import com.serenegiant.aceparrot.BaseBridgeFragment;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.aceparrot.ConfigAppFragment;
import com.serenegiant.aceparrot.R;

import java.io.IOException;

import jp.co.rediscovery.arflight.ARDeviceServiceAdapter;
import jp.co.rediscovery.arflight.ManagerFragment;

public class ConnectionFragment extends AbstractConnectionFragment {

	public static ConnectionFragment newInstance() {
		return new ConnectionFragment();
	}

	public ConnectionFragment() {
		super();
	}

	@Override
	protected int getLayoutStyle(final int type) {
		final int result;
		switch (type) {
		case 1:
			result = R.style.AppThemeAuto_001;
			break;
		case 2:
			result = R.style.AppThemeAuto_002;
			break;
//		case 0:
		default:
			result = R.style.AppThemeAuto_001;
			break;
		}
		return result;
	}

	@Override
	protected void updateButtonsOnUiThread(final boolean visible) {
		super.updateButtonsOnUiThread(visible);
		final int visibility = visible ? View.VISIBLE : View.INVISIBLE;
		mDownloadBtn.setVisibility(visibility);
		mPilotBtn.setVisibility(visibility);
		mGalleyBrn.setVisibility(visibility);
		mScriptBtn.setVisibility(visibility);
	}

	@Override
	protected void onClick(final View view, final int position) {
		Fragment fragment = null;
		final int id = view.getId();
		switch (id) {
		case R.id.pilot_button:
		case R.id.download_button:
		case R.id.gallery_button:
		case R.id.script_button:
			if (checkPermissionLocation()) {
				final ARDeviceServiceAdapter adapter = (ARDeviceServiceAdapter)mDeviceListView.getAdapter();
				final String itemValue = adapter.getItemName(position);
				final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
				final ARDiscoveryDeviceService device = manager.getDevice(itemValue);
				if (device != null) {
					// 製品名を取得
					final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(device.getProductID());
					switch (product) {
					case ARDISCOVERY_PRODUCT_ARDRONE:				// Bebop Drone product
						switch (id) {
						case R.id.pilot_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test001", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.download_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test002", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.gallery_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test003", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.script_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test004", AutoPilotFragment2.MODE_TRACKING);
							break;
						}
						break;
					case ARDISCOVERY_PRODUCT_BEBOP_2:				// Bebop drone 2.0 product
						switch (id) {
						case R.id.pilot_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test011", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.download_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test012", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.gallery_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test013", AutoPilotFragment2.MODE_TRACE);
							break;
						case R.id.script_button:
							fragment = AutoPilotFragment2.newInstance(device, null, "test014", AutoPilotFragment2.MODE_TRACKING);
							break;
						}
						break;
					case ARDISCOVERY_PRODUCT_SKYCONTROLLER:			// Sky controller product
					case ARDISCOVERY_PRODUCT_SKYCONTROLLER_2:		// Sky controller 2 product
						if (BuildConfig.USE_SKYCONTROLLER) {
							fragment = newBridgetFragment(device);
						}
						break;
					}
				}
			}
			break;
		case R.id.config_show_btn:
			fragment = ConfigAppFragment.newInstance();
			break;
		}
		if (fragment != null) {
			replace(fragment);
		}
	}

	@Override
	protected boolean onLongClick(final View view, final int position) {
		return false;
	}

	@Override
	protected BaseBridgeFragment newBridgetFragment(final ARDiscoveryDeviceService device) {
		return BridgeFragment.newInstance(device);
	}

	@Override
	protected void setDataSource(final Context context, final MediaPlayer media_player) throws IOException {
		media_player.setDataSource(context, Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.autoparrot));
	}
}
