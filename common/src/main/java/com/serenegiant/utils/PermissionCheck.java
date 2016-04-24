package com.serenegiant.utils;
/*
 * Androusb
 * Copyright (c) 2014-2016 saki t_saki@serenegiant.com
 * Distributed under the terms of the GNU Lesser General Public License (LGPL v3.0) License.
 * License details are in the file license.txt, distributed as part of this software.
 */

import android.Manifest.permission;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.util.Log;

import java.util.List;

public final class PermissionCheck {

	public static final void dumpPermissions(final Context context) {
    	if (context == null) return;
		try {
			final PackageManager pm = context.getPackageManager();
			final List<PermissionGroupInfo> list = pm.getAllPermissionGroups(PackageManager.GET_META_DATA);
			for (final PermissionGroupInfo info : list) {
				Log.d("PermissionCheck", info.name);
			}
		} catch (final Exception e) {
			Log.w("", e);
		}
	}

	/**
	 * パーミッションを確認
	 * @param context
	 * @param permissionName
	 * @return 指定したパーミッションがあればtrue
	 */
	@SuppressLint("NewApi")
	public static boolean hasPermission(final Context context, final String permissionName) {
    	if (context == null) return false;
		boolean result = false;
		try {
			final int check;
			if (BuildCheck.isMarshmallow()) {
				check = context.checkSelfPermission(permissionName);
			} else {
				final PackageManager pm = context.getPackageManager();
				check = pm.checkPermission(permissionName, context.getPackageName());
			}
			switch (check) {
			case PackageManager.PERMISSION_DENIED:
				break;
			case PackageManager.PERMISSION_GRANTED:
				result = true;
				break;
			}
		} catch (final Exception e) {
			Log.w("", e);
		}
    	return result;
    }

    /**
     * 録音のミッションがあるかどうかを確認
     * @param context
     * @return 録音のパーミッションがあればtrue
     */
    public static boolean hasAudio(final Context context) {
    	return hasPermission(context, permission.RECORD_AUDIO);
    }

    /**
     * ネットワークへのアクセスパーミッションがあるかどうかを確認
     * @param context
     * @return ネットワークへのアクセスパーミッションがあればtrue
     */
    public static boolean hasNetwork(final Context context) {
    	return hasPermission(context, permission.INTERNET);
    }

    /**
     * 外部ストレージへの書き込みパーミッションがあるかどうかを確認
     * @param context
     * @return 外部ストレージへの書き込みパーミッションがあればtrue
     */
    public static boolean hasWriteExternalStorage(final Context context) {
    	return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
    }

    /**
     * 外部ストレージからの読み込みパーミッションがあるかどうかを確認
     * @param context
     * @return 外部ストレージへの読み込みパーミッションがあればtrue
     */
    @SuppressLint("InlinedApi")
	public static boolean hasReadExternalStorage(final Context context) {
    	if (BuildCheck.isAndroid4())
    		return hasPermission(context, permission.READ_EXTERNAL_STORAGE);
    	else
    		return hasPermission(context, permission.WRITE_EXTERNAL_STORAGE);
    }

	/**
	 * 位置情報アクセスのパーミッションが有るかどうかを確認
	 * @param context
	 * @return
	 */
	public static boolean hasAccessLocation(final Context context) {
		return hasPermission(context, permission.ACCESS_COARSE_LOCATION)
			&& hasPermission(context, permission.ACCESS_FINE_LOCATION);
	}

	/**
	 * 低精度位置情報アクセスのパーミッションが有るかどうかを確認
	 * @param context
	 * @return
	 */
	public static boolean hasAccessCoarseLocation(final Context context) {
		return hasPermission(context, permission.ACCESS_COARSE_LOCATION);
	}

	/**
	 * 高精度位置情報アクセスのパーミッションが有るかどうかを確認
	 * @param context
	 * @return
	 */
	public static boolean hasAccessFineLocation(final Context context) {
		return hasPermission(context, permission.ACCESS_FINE_LOCATION);
	}
}
