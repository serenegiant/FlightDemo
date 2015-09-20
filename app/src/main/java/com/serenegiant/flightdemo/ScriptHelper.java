package com.serenegiant.flightdemo;

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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScriptHelper {
	private static final boolean DEBUG = true;  // FIXME 実働時はfalseにすること
	private static final String TAG = ScriptHelper.class.getSimpleName();

	public static class ScriptRec {
        public File path;
        public String name;
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

	public static void copyScripts(final Context context, final boolean force) {
		final File root = FileUtils.getCaptureDir(context, "Documents", false);
		final int n = SCRIPTS.length;
		final byte[] buffer = new byte[1024];
		for (int i = 0; i < n; i++) {
			final File path = new File(root, SCRIPTS[i]);
			if (force || !path.exists()) {
				try {
					final InputStream in = new BufferedInputStream(context.getResources().getAssets().open(SCRIPTS[i]));
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
				} catch (final IOException e) {
					Log.w(TAG, e);
				}
			}
		}
	}

	private static final String KEY_SCRIPT_NUM = "KEY_SCRIPT_NUM";
	private static final String KEY_SCRIPT_PATH = "KEY_SCRIPT_PATH";
	private static final String KEY_SCRIPT_NAME = "KEY_SCRIPT_NAME";
	private static final String KEY_SCRIPT_CRC = "KEY_SCRIPT_CRC";

	public static void loadScripts(final SharedPreferences pref, final List<ScriptRec> scripts) {
		final int n = pref.getInt(KEY_SCRIPT_NUM, 0);
		scripts.clear();
		for (int i = 0; i < n; i++) {
			final String path = pref.getString(KEY_SCRIPT_PATH + i, "");
			final String name = pref.getString(KEY_SCRIPT_NAME + i, "");
			final int crc = pref.getInt(KEY_SCRIPT_CRC, 0);
			final ScriptRec script = loadScript(path, crc);
			if (script != null) {
				scripts.add(script);
			}
		}
	}

	public static void saveScripts(final SharedPreferences pref, final List<ScriptRec> scripts) {
		final int n = scripts.size();
		final SharedPreferences.Editor editor = pref.edit();
		try {
			editor.putInt(KEY_SCRIPT_NUM, n);
			for (int i = 0; i < n; i++) {
				final ScriptRec script = scripts.get(i);
				editor.putString(KEY_SCRIPT_PATH + i, script.path.getAbsolutePath())
					.putString(KEY_SCRIPT_NAME + i, script.name)
					.putInt(KEY_SCRIPT_CRC + i, script.crc);
			}
		} finally {
			editor.apply();
		}
	}

	public static final ScriptRec loadScript(final String path, final int crc) {
        if (DEBUG) Log.v(TAG, "loadScript:" + path);
        if (!TextUtils.isEmpty(path)) {
            return loadScript(new File(path), crc);
        }
        return null;
    }

	public static final ScriptRec loadScript(final File file, final int crc) {
        final ScriptRec result = new ScriptRec();
		if (file.exists() && file.canRead()) {
			getScriptName(file, result);
			try {
				if (crc != result.crc) {	// crcが異なる=ファイルが変更された時
					final Script script;
					script = new Script(new BufferedInputStream(new FileInputStream(file)));
					final ASTParse parse = script.Parse();
					// FIXME 仮実行
				}
				result.checked = true;
				return result;
			} catch (final FileNotFoundException e) {
				Log.w(TAG, e);
			} catch (final ParseException e) {
				Log.w(TAG, e);
			}
		}
        return null;
    }

    private static final Pattern NAME_PREFIX = Pattern.compile("^#define\\s+name\\s+(\\S+)");
	public static final ScriptRec getScriptName(final File file, final ScriptRec rec) {
		final ScriptRec result = rec != null ? rec : new ScriptRec();
		result.crc = 0;
		result.path = null;
		result.name = file.getName();
		final CRC32 crc = new CRC32();
        try {
            final LineNumberReader in = new LineNumberReader(new BufferedReader(new FileReader(file)));
            String line;
			Matcher matcher = null;
			boolean found = false;
            do {
                line = in.readLine();	// nullが返ってきたらファイルの終端
                if (line != null) {
					crc.update(line);	// crc32を計算
					if (!found) {		// 最初に見つかったのを採用する
						if (matcher == null) {
							matcher = NAME_PREFIX.matcher(line);
						} else {
							matcher.reset(line);
						}
						if (matcher.find()) {
							result.name = matcher.group(1);
							found = true;	// 見つかった
						}
					}
                }
            } while (line != null);
			result.path = file;
			result.crc = crc.getCrc();
        } catch (final FileNotFoundException e) {
            Log.w(TAG, e);
        } catch (final IOException e) {
            Log.w(TAG, e);
        }
        return result;
    }

	/**
	 * 指定したファイルリストからスクリプトを追加する
	 * @param files
	 * @param scrips
	 * @return
	 */
	public static boolean addScripts(final File[] files, final List<ScriptRec> scrips) {
		boolean result = false;
		final int n = files != null ? files.length : 0;
		for (int i = 0; i < n; i++) {
			final ScriptRec script = loadScript(files[i], 0);
			if ((script != null) && !script.included(scrips)) {
				scrips.add(script);
				result = true;
			}
		}
		return result;
	}

}
