package com.serenegiant.utils;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AssetsHelper {

	public static String loadString(final AssetManager assets, final String name) throws IOException {
		final StringBuffer sb = new StringBuffer();
		final char[] buf = new char[1024];
		final BufferedReader reader = new BufferedReader(new InputStreamReader(assets.open(name)));
		int r = reader.read(buf);
		while (r > 0) {
			sb.append(buf, 0, r);
		}
		return sb.toString();
	}
}
