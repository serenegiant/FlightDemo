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
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.serenegiant.lang.script.ASTParse;
import com.serenegiant.lang.script.ParseException;
import com.serenegiant.lang.script.Script;
import com.serenegiant.arflight.R;
import com.serenegiant.utils.CRC32;
import com.serenegiant.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {
	private static final boolean DEBUG = false;  // FIXME 実働時はfalseにすること
	private static final String TAG = ScriptHelper.class.getSimpleName();

	public static class ScriptRec {
        public File path;
        public String name;
        public String token;
		public int crc;
        public boolean checked;

		@Override
		public boolean equals(Object o) {
			if (o instanceof ScriptRec) {
				final ScriptRec other = (ScriptRec)o;
				return (path == other.path) && (name == other.name) && (crc == other.crc);
			} else if (o instanceof File) {
				final File other = (File)o;
				return path == other;
			}
			return super.equals(o);
		}

		@Override
		public String toString() {
			return String.format("ScriptRec(crc=%8x,path=%s,name=%s)", crc, path != null ? path.getAbsolutePath() : "", name);
		}

		/**
		 * 同じpathが含まれているかどうかをチェック。
		 * @param list
		 * @return true: pathが同じものが含まれていた
		 */
		public boolean included(final List<?>list) {
			boolean result = false;
			final int n = list.size();
			for (int i = 0; i < n; i++) {
				final Object item = list.get(i);
				if (item instanceof ScriptRec) {
					final ScriptRec other = (ScriptRec)item;
					if (path != null && path.equals(other.path)) {
						result = true;
						break;
					}
				} else if (item instanceof File) {
					final File other = (File)item;
					if (path != null && path.equals(other)) {
						result = true;
						break;
					}
				}
			}
			return result;
		}

	}

	private static class ViewHolder {
		public TextView title;
	}

	public static class ScriptListAdapter extends ArrayAdapter<ScriptRec> {
		private final LayoutInflater mInflater;
		private final int mLayoutId;
		public ScriptListAdapter(final Context context, final int layoutId, List<ScriptRec> list) {
			super(context, layoutId, list);
			mInflater = LayoutInflater.from(context);
			mLayoutId = layoutId;
		}

		@Override
		public View getView(final int position, final View convertView, final ViewGroup parent) {
			View rootView = convertView;
			if (rootView == null) {
				rootView = mInflater.inflate(mLayoutId, parent, false);
			}
			ViewHolder holder = (ViewHolder)rootView.getTag(R.id.scriptlistadapter);
			if (holder == null) {
				holder = new ViewHolder();
				holder.title = (TextView)rootView.findViewById(R.id.title);
			}
			final ScriptRec item = getItem(position);
			if (holder.title != null) {
				holder.title.setText(item.name);
			}
			return rootView;
		}
	}

	private static String[] SCRIPTS = {
		"circle_xy.script",
		"circle_xy2.script",
		"circle_xz.script",
		"revolution_xr.script",
		"revolution_xr2.script",
		"revolution_yr.script",
		"revolution_yr2.script",
	};

	/**
	 * アセット内のスクリプトファイルをストレージにコピーする
	 * @param activity
	 * @param force
	 */
	public static void copyScripts(final Activity activity, final boolean force) {
		File root = FileUtils.getCaptureDir(activity, "Documents", 0);
		if ((root == null) || !root.canWrite()) {
			// パーミッションが無い
			root = activity.getFilesDir();
		}
		final SharedPreferences pref = activity.getPreferences(0);
		final SharedPreferences.Editor editor = pref.edit();
		try {
			final int n = SCRIPTS.length;
			final byte[] buffer = new byte[1024];
			for (int i = 0; i < n; i++) {
				final File path = new File(root, SCRIPTS[i]);
				if (force || !path.exists()) {
					try {
						final InputStream in = new BufferedInputStream(activity.getResources().getAssets().open(SCRIPTS[i]));
						try {
							final OutputStream out = new BufferedOutputStream(new FileOutputStream(path));
							try {
								for (;;) {
									final int bytes = in.read(buffer);
									if (bytes > 0) {
										out.write(buffer, 0, bytes);
									} else {
										break;
									}
								}
							} finally {
								out.close();
							}
						} finally {
							in.close();
						}
						try {
							final ScriptRec script = loadScript(path, 0);
							saveScript(editor, script, i);
						} catch (final ParseException e) {
						}
					} catch (final IOException e) {
						Log.w(TAG, e);
					}
				}
			}
		} finally {
			editor.apply();
		}
	}

	private static final String KEY_SCRIPT_NUM = "KEY_SCRIPT_NUM";
	private static final String KEY_SCRIPT_PATH = "KEY_SCRIPT_PATH";
	private static final String KEY_SCRIPT_NAME = "KEY_SCRIPT_NAME";
	private static final String KEY_SCRIPT_CRC = "KEY_SCRIPT_CRC";

	/**
	 * プレファレンスに保存されているスクリプト定義を読み込む
	 * @param pref
	 * @param scripts
	 */
	public static void loadScripts(final SharedPreferences pref, final List<ScriptRec> scripts) throws IOException {
		final int n = pref.getInt(KEY_SCRIPT_NUM, 0);
		scripts.clear();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) {
			final String path = pref.getString(KEY_SCRIPT_PATH + i, "");
			final String name = pref.getString(KEY_SCRIPT_NAME + i, "");
			final int crc = pref.getInt(KEY_SCRIPT_CRC, 0);
			try {
				final ScriptRec script = loadScript(path, crc);
				if (script != null) {
					scripts.add(script);
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
				sb.append(e.getMessage()).append("\n");
			}
		}
		if (sb.length() > 0) {
			throw new IOException(sb.toString());
		}
	}

	/**
	 * 指定したスクリプト定義をプレファレンスに書き出す
	 * @param pref
	 * @param scripts
	 */
	public static void saveScripts(final SharedPreferences pref, final List<ScriptRec> scripts) {
		final int n = scripts.size();
		final SharedPreferences.Editor editor = pref.edit();
		try {
			editor.putInt(KEY_SCRIPT_NUM, n);
			for (int i = 0; i < n; i++) {
				final ScriptRec script = scripts.get(i);
				saveScript(editor, script, i);
			}
		} finally {
			editor.apply();
		}
	}

	public static void saveScript(final SharedPreferences.Editor editor, final ScriptRec script, final int ix) {
		editor.putString(KEY_SCRIPT_PATH + ix, script.path.getAbsolutePath())
			.putString(KEY_SCRIPT_NAME + ix, script.name)
			.putInt(KEY_SCRIPT_CRC + ix, script.crc);
	}

	/**
	 * 指定したファイルからスクリプトを読み込み文法チェックして返す
	 * @param path
	 * @param crc
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static final ScriptRec loadScript(final String path, final int crc) throws IOException, ParseException {
        if (DEBUG) Log.v(TAG, "loadScript:" + path);
        if (!TextUtils.isEmpty(path)) {
            return loadScript(new File(path), crc);
        }
        return null;
    }

	/**
	 * 指定したファイルからスクリプトを読み込み文法チェックして返す
	 * @param file
	 * @param crc
	 * @return
	 * @throws FileNotFoundException
	 * @throws ParseException
	 */
	public static final ScriptRec loadScript(final File file, final int crc) throws IOException, ParseException {
        final ScriptRec result = new ScriptRec();
		if (file.exists() && file.canRead()) {
			getScriptName(file, result);
			if (crc != result.crc) {	// crcが異なる=ファイルが変更された時
				final Script script;
				script = new Script(new BufferedInputStream(new FileInputStream(file)));
				final ASTParse parse = script.Parse();
				// FIXME 仮実行
			}
			result.checked = true;
			return result;
		} else {
			throw new FileNotFoundException(file.getName() + " not found");
		}
    }

	/** スクリプトファイル内からスクリプト名定義を探すための正規表現 */
    private static final Pattern NAME_PREFIX = Pattern.compile("^#define\\s+name\\s+(\\S+)");
    /** スクリプトファイル内から音声認識用語句を探すための正規表現 */
	private static final Pattern TOKEN_PREFIX = Pattern.compile("^#define\\s+token\"\\s+(\\S+)\"");

	/**
	 * スクリプトファイル内からスクリプト名を取得しcrc32を計算して返す
	 * @param file
	 * @param rec
	 * @return
	 */
	public static final ScriptRec getScriptName(final File file, final ScriptRec rec) throws IOException {
		final ScriptRec result = rec != null ? rec : new ScriptRec();
		result.crc = 0;
		result.path = null;
		result.name = FileUtils.removeFileExtension(file.getName());
		final CRC32 crc = new CRC32();
		final LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(file)));
		String line;
		Matcher nameMatcher = null, tokenMatcher = null;
		final StringBuilder tokens = new StringBuilder();
		boolean found = false;
		do {
			line = in.readLine();	// nullが返ってきたらファイルの終端
			if (line != null) {
				crc.update(line);	// crc32を計算
				if (!found) {		// 最初に見つかったのを採用する
					if (nameMatcher == null) {
						nameMatcher = NAME_PREFIX.matcher(line);
					} else {
						nameMatcher.reset(line);
					}
					if (nameMatcher.find()) {
						result.name = nameMatcher.group(1);
						found = true;	// 見つかった
					}
				}
				if (tokenMatcher == null) {
					tokenMatcher = TOKEN_PREFIX.matcher(line);
				} else {
					tokenMatcher.reset(line);
				}
				if (tokenMatcher.find()) {
					if (tokens.length() == 0) {
						tokens.append(tokenMatcher.group(1));
					} else {
						tokens.append("|").append(tokenMatcher.group(1));
					}
				}
			}
		} while (line != null);
		result.path = file;
		result.crc = crc.getCrc();
		if (tokens.length() == 0) {
			result.token = result.name;
		} else {
			result.token = tokens.toString();
		}
        return result;
    }

	/**
	 * 指定したファイル配列からスクリプトを追加する
	 * @param files
	 * @param scrips
	 * @return
	 */
	public static boolean addScripts(final File[] files, final List<ScriptRec> scrips) throws IOException {
		boolean result = false;
		final int n = files != null ? files.length : 0;
		if (n > 0) {
			String lastError = null;
			final List<ScriptRec> temp = new ArrayList<ScriptRec>();
			try {
				for (int i = 0; i < n; i++) {
					final ScriptRec script = loadScript(files[i], 0);
					if ((script != null) && !script.included(scrips) && !script.included(temp)) {
						temp.add(script);
						result = true;
					}
				}
			} catch (final FileNotFoundException e) {
				result = false;
				lastError = e.getMessage();
			} catch (final ParseException e) {
				result = false;
				lastError = e.getMessage();
			}
			if (result) {
				scrips.addAll(temp);
			} else if (lastError != null) {
				throw new IOException(lastError);
			}
		}
		return result;
	}

	/**
	 * ログファイルにメッセージを追加
	 * @param context
	 * @param message
	 */
	public static void appendLog(final Context context, final String message) {
		try {
			final File root = FileUtils.getCaptureDir(context, "Documents", 0);
			final File log = new File(new File(root, "log"), "script" + FileUtils.getDateTimeString() + ".log");
			log.mkdirs();
			final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(log, true));
			writer.write(message);
			writer.flush();
			writer.close();
		} catch (final FileNotFoundException e) {
			Log.w(TAG, e);
		} catch (final IOException e) {
			Log.w(TAG, e);
		}
	}
}
