package com.serenegiant.utils;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public final class UriHelper {
//	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
//	private static final String TAG = DEBUG ? "UriHelper" : null;
	/**
	 * UriからPathへの変換処理
	 * @param cr
	 * @param uri
	 * @return String パスが見つからなければnull
	 */
	public static String getAbsolutePath(final ContentResolver cr, final Uri uri) {
		String path = null;
		try {
			final String[] columns = { MediaStore.Images.Media.DATA };
			final Cursor cursor = cr.query(uri, columns, null, null, null);
			if (cursor != null)
			try {
				if (cursor.moveToFirst())
				path = cursor.getString(0);
			} finally {
				cursor.close();
			}
		} catch (final Exception e) {
//			if (DEBUG) Log.w(TAG, e);
		}
//		Log.v("UriHandler", "getAbsolutePath:" + path);
		return path;
	}
}
