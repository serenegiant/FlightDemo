package com.serenegiant.autoparrot;
/*
 * By downloading, copying, installing or using the software you agree to this license.
 * If you do not agree to this license, do not download, install,
 * copy or use the software.
 *
 *
 *                           License Agreement
 *                For Open Source Computer Vision Library
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
import android.util.Log;
import android.view.View;

import com.parrot.arsdk.ardiscovery.ARDISCOVERY_PRODUCT_ENUM;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.parrot.arsdk.ardiscovery.ARDiscoveryService;
import com.serenegiant.aceparrot.BaseBridgeFragment;
import com.serenegiant.aceparrot.BuildConfig;
import com.serenegiant.aceparrot.PilotFragment;
import com.serenegiant.aceparrot.R;

import java.io.IOException;

import jp.co.rediscovery.arflight.ARDeviceInfoAdapter;
import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.ManagerFragment;


/**
 * スカイコントローラーに接続してスカイコントローラーが
 * 検出している機体の一覧取得＆選択を行うためのFragment
 */
public class BridgeFragment extends BaseBridgeFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = BridgeFragment.class.getSimpleName();

	public static BridgeFragment newInstance(final ARDiscoveryDeviceService device) {
		final BridgeFragment fragment = new BridgeFragment();
		fragment.setDevice(device);
		return fragment;
	}

	public BridgeFragment() {
		super();
		// デフォルトコンストラクタが必要
		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
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
	protected void onClick(final View view) {
		if (DEBUG) Log.v(TAG, "onClick:");
		Fragment fragment = null;
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
		final int position = mDeviceListView.getCheckedItemPosition();
		final DeviceInfo info = adapter.getItem(position);
		final ARDiscoveryDeviceService device = mController.getDeviceService();
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
			final int id = view.getId();
			switch (id) {
			case R.id.pilot_button:
			case R.id.download_button:
			case R.id.gallery_button:
			case R.id.script_button:
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					switch (id) {
					case R.id.pilot_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test001", AutoPilotFragment2.MODE_TRACE);
						break;
					case R.id.download_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test002", AutoPilotFragment2.MODE_TRACE);
						break;
					case R.id.gallery_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test003", AutoPilotFragment2.MODE_TRACE);
						break;
					case R.id.script_button:
						fragment = AutoPilotFragment2.newInstance(device, info, "test004", AutoPilotFragment2.MODE_TRACKING);
						break;
					default:
						Log.w(TAG, "未知のview idが来た。なんでやねん:" + id);
						break;
					}
					break;
				default:
					Log.w(TAG, "未知の機体が来た:" + product);
					break;
				}
				break;
			default:
				Log.w(TAG, "未知のview idが来た:" + id);
				break;
			}
		} else {
			Log.w(TAG, "機体が取得できなかった:position=" + position);
		}
		if (fragment != null) {
			mIsConnectToDevice = mNeedRequestDeviceList = true;
			replace(fragment);
		}
	}

	@Override
	protected boolean onLongClick(final View view) {
		if (DEBUG) Log.v(TAG, "onClick:");
		Fragment fragment = null;
		final ManagerFragment manager = ManagerFragment.getInstance(getActivity());
		final ARDeviceInfoAdapter adapter = (ARDeviceInfoAdapter)mDeviceListView.getAdapter();
		final int position = mDeviceListView.getCheckedItemPosition();
		final DeviceInfo info = adapter.getItem(position);
		final ARDiscoveryDeviceService device = mController.getDeviceService();
		if (device != null) {
			// 製品名を取得
			final ARDISCOVERY_PRODUCT_ENUM product = ARDiscoveryService.getProductFromProductID(info.productId());
			final int id = view.getId();
			switch (id) {
			case R.id.config_show_btn:
				switch (product) {
				case ARDISCOVERY_PRODUCT_ARDRONE:	// Bebop
				case ARDISCOVERY_PRODUCT_BEBOP_2:	// Bebop2
					fragment = PilotFragment.newInstance(device, info);
					break;
				default:
					Log.w(TAG, "未知の機体が来た:" + product);
					break;
				}
				break;
			default:
				break;
			}
		} else {
			Log.w(TAG, "機体が取得できなかった:position=" + position);
		}
		if (fragment != null) {
			mIsConnectToDevice = mNeedRequestDeviceList = true;
			replace(fragment);
			return true;
		}
		return false;
	}

	@Override
	protected void setDataSource(final Context context, final MediaPlayer media_player) throws IOException {
		media_player.setDataSource(context, Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.into_the_sky));
	}

}
