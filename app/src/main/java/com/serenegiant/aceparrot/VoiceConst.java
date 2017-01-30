package com.serenegiant.aceparrot;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by saki on 2017/01/28.
 *
 */

public class VoiceConst {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = VoiceConst.class.getSimpleName();

	public static final long MAX_COUNT			= 0x00000003;

	public static final int DIR_FORWARD			= 0x00000001;
	public static final int DIR_RIGHT			= 0x00000002;
	public static final int DIR_BACKWARD		= 0x00000004;
	public static final int DIR_LEFT			= 0x00000008;
	public static final int DIR_UP				= 0x00000010;
	public static final int DIR_DOWN			= 0x00000020;
	private static final int DIR_SAME			= 0x10000000;

	public static final int CMD_NON				= 0x00000000;
	public static final int CMD_STOP			= 0x00000100;
	public static final int CMD_TAKEOFF			= 0x00000200;
	public static final int CMD_LANDING			= 0x00000400;
	public static final int CMD_MOVE			= 0x00000800;
	public static final int CMD_FLIP			= 0x00001000;
	public static final int CMD_TURN			= 0x00002000;
	public static final int CMD_MASK			= 0x00ffff00;

	private static final long CMD_FORWARD_MAX	= CMD_MOVE | DIR_FORWARD | (MAX_COUNT << 32);
	private static final long CMD_RIGHT_MAX		= CMD_MOVE | DIR_RIGHT | (MAX_COUNT << 36);
	private static final long CMD_BACKWARD_MAX	= CMD_MOVE | DIR_BACKWARD | (MAX_COUNT << 40);
	private static final long CMD_LEFT_MAX		= CMD_MOVE | DIR_LEFT | (MAX_COUNT << 44);
	private static final long CMD_UP_MAX		= CMD_MOVE | DIR_UP | (MAX_COUNT << 48);
	private static final long CMD_DOWN_MAX		= CMD_MOVE | DIR_DOWN | (MAX_COUNT << 52);

	public static float getRoll(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 36) & 0x03) * (((cmd & DIR_RIGHT) == DIR_RIGHT) ? 1 : 0)
			- (float)((cmd >>> 44) & 0x03) * (((cmd & DIR_LEFT) == DIR_LEFT) ? 1 : 0)) / 3.0f
			: 0.0f;
	}

	public static float getPitch(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 32) & 0x03) * (((cmd & DIR_FORWARD) == DIR_FORWARD) ? 1 : 0)
			- (float)((cmd >>> 40) & 0x03) * (((cmd & DIR_BACKWARD) == DIR_BACKWARD) ? 1 : 0)) / 3.0f
			: 0.0f;
	}

	public static float getGaz(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 48) & 0x03) * (((cmd & DIR_UP) == DIR_UP) ? 1 : 0)
			- (float)((cmd >>> 52) & 0x03) * (((cmd & DIR_DOWN) == DIR_DOWN) ? 1 : 0)) / 3.0f
			: 0.0f;
	}

	public static float getYaw(final long cmd) {
		return ((cmd & CMD_TURN) == CMD_TURN) ?
			(((cmd & DIR_RIGHT) == DIR_RIGHT) ? 1 : 0)
			- (((cmd & DIR_LEFT) == DIR_LEFT) ? 1 : 0)
			: 0.0f;
	}

	public static long findCommand(final String text) {
		long cmd = CMD_NON;

		if (!TextUtils.isEmpty(text)) {
			cmd = findCmd(text);
			if (cmd == CMD_NON) {
				cmd = findAction(text);
				if (cmd == CMD_NON) {
					cmd = findMove(text);
				}
			}
		}
		return cmd;
	}

	private static long findCmd(@NonNull final String text) {
		long cmd = CMD_NON;
		final Set<String> keys = CMD_MAP.keySet();
		for (final String key: keys) {
			if (text.contains(key)) {
				cmd = CMD_MAP.get(key);
				break;
			}
		}
		return cmd;
	}

	private static long findAction(@NonNull final String text) {
		long cmd = CMD_NON;
		final int len = text.length();
		final Set<String> actions = ACTION_MAP.keySet();
		for (final String action: actions) {
			final int pos = text.indexOf(action);
			if (pos >= 0) {
				final int actionCmd = ACTION_MAP.get(action);
				final Set<String> dirs = DIR_MAP.keySet();
				for (final String dir: dirs) {
					final int dirPos = text.lastIndexOf(dir);
					if (dirPos >= 0) {
						final int flipDir = DIR_MAP.get(dir);
						switch (flipDir) {
						case DIR_FORWARD:
						case DIR_BACKWARD:
							if (actionCmd == CMD_TURN) continue;
							// pass through
						case DIR_RIGHT:
						case DIR_LEFT:
							return actionCmd | flipDir;
						}
					}
				}
			}
		}
		return cmd;
	}

	private static long findMove(@NonNull final String text) {
	if (DEBUG) Log.v(TAG, "findMove:text=" + text);
		final SparseIntArray cmds = new SparseIntArray();

		final Set<String> dirs = DIR_MAP.keySet();
		for (final String dir: dirs) {
			final int move = DIR_MAP.get(dir);
			int sz = dir.length();
			int ix = -sz - 1;
			for ( ; ; ) {
				ix = text.indexOf(dir, ix + sz);
				if (ix < 0) break;
				// 見つかった時
				if (DEBUG) Log.v(TAG, "見つかった:" + dir + "@" + ix);
				cmds.put(ix, move);
			}
		}
		long cmd = CMD_NON;
		final int n = cmds.size();
//		String dir = null;
		int lastPos = -1, lastMove = 0, lastIx = -1;
//		final StringBuilder sb = new StringBuilder(n);
		for (int i = 0; i < n; i++) {
			int pos = cmds.keyAt(i);
			int move = cmds.valueAt(i);
			int ix = -1;
			switch (move) {
			case DIR_FORWARD:	// 0x00000001;
//				dir = "前";
				ix = 32;
				cmd &= ~CMD_BACKWARD_MAX;
				break;
			case DIR_RIGHT:		// 0x00000002;
//				dir = "右";
				ix = 36;
				cmd &= ~CMD_LEFT_MAX;
				break;
			case DIR_BACKWARD:	// 0x00000004;
//				dir = "後";
				ix = 40;
				cmd &= ~CMD_FORWARD_MAX;
				break;
			case DIR_LEFT:		// 0x00000008;
//				dir = "左";
				ix = 44;
				cmd &= ~CMD_RIGHT_MAX;
				break;
			case DIR_UP:		// 0x00000010;
//				dir = "上";
				ix = 48;
				cmd &= ~CMD_DOWN_MAX;
				break;
			case DIR_DOWN:		// 0x00000020;
//				dir = "下";
				ix = 52;
				cmd &= ~CMD_UP_MAX;
				break;
			case DIR_SAME:
				if (lastPos >= 0) {
					ix = lastIx;
					move = lastMove;
					pos = -1;
				}
				break;
			}
			if (ix > 0) {
				lastPos = pos;
				lastMove = move;
				lastIx = ix;
				final long cnt = (cmd >>> ix) & 0x03L;
				cmd = (cmd & ~(0x03L << ix)) | CMD_MOVE | move | (((cnt + 1) & 0x03L) << ix);
//				sb.append(dir);
//				if (DEBUG) Log.v(TAG, dir + ":cnd=" + cnt + "/" + Long.toHexString(((cnt + 1) & 0x03L) << ix));
			}
		}	// for
//		if (DEBUG) Log.v(TAG, "findMove:result=" + sb.toString() + "/" + Long.toHexString(cmd));

		return cmd;
	}

	private static final Map<String, Integer> CMD_MAP = new LinkedHashMap<String, Integer>();
	private static final Map<String, Integer> ACTION_MAP = new LinkedHashMap<String, Integer>();
	private static final Map<String, Integer> DIR_MAP = new HashMap<String, Integer>();
	static {
		CMD_MAP.put("stop", CMD_STOP);
		CMD_MAP.put("すとっぷ", CMD_STOP);
		CMD_MAP.put("ストップ", CMD_STOP);
		CMD_MAP.put("止まれ", CMD_STOP);
		CMD_MAP.put("止れ", CMD_STOP);
		CMD_MAP.put("停止", CMD_STOP);
		CMD_MAP.put("停まれ", CMD_STOP);
		CMD_MAP.put("とまれ", CMD_STOP);
		CMD_MAP.put("トマレ", CMD_STOP);
		CMD_MAP.put("あかん", CMD_STOP);
		CMD_MAP.put("ぶつかる", CMD_STOP);
		CMD_MAP.put("ブツカル", CMD_STOP);
		CMD_MAP.put("当たる", CMD_STOP);
		CMD_MAP.put("当る", CMD_STOP);
		CMD_MAP.put("あたる", CMD_STOP);
		CMD_MAP.put("アタル", CMD_STOP);
		CMD_MAP.put("アカン", CMD_STOP);
		CMD_MAP.put("きゃ", CMD_STOP);
		CMD_MAP.put("キャ", CMD_STOP);
		CMD_MAP.put("わー", CMD_STOP);
		CMD_MAP.put("ワー", CMD_STOP);
//
		CMD_MAP.put("land", CMD_LANDING);
		CMD_MAP.put("landing", CMD_LANDING);
		CMD_MAP.put("らんでぃんぐ", CMD_LANDING);
		CMD_MAP.put("ランディング", CMD_LANDING);
		CMD_MAP.put("らんにんぐ", CMD_LANDING);
		CMD_MAP.put("ランニング", CMD_LANDING);
		CMD_MAP.put("着陸", CMD_LANDING);
		CMD_MAP.put("ちゃくりく", CMD_LANDING);
		CMD_MAP.put("チャクリク", CMD_LANDING);
//
		CMD_MAP.put("take off", CMD_TAKEOFF);
		CMD_MAP.put("離陸", CMD_TAKEOFF);
		CMD_MAP.put("りりく", CMD_TAKEOFF);
		CMD_MAP.put("リリク", CMD_TAKEOFF);
		CMD_MAP.put("りりっく", CMD_TAKEOFF);
		CMD_MAP.put("リリック", CMD_TAKEOFF);
		CMD_MAP.put("飛べ", CMD_TAKEOFF);
		CMD_MAP.put("とべ", CMD_TAKEOFF);
		CMD_MAP.put("トベ", CMD_TAKEOFF);
		CMD_MAP.put("飛ぶ", CMD_TAKEOFF);
		CMD_MAP.put("とぶ", CMD_TAKEOFF);
		CMD_MAP.put("トブ", CMD_TAKEOFF);
		CMD_MAP.put("いりく", CMD_TAKEOFF);
		CMD_MAP.put("イリク", CMD_TAKEOFF);
//--------------------------------------------------------------------------------
		ACTION_MAP.put("flip", CMD_FLIP);
		ACTION_MAP.put("ふりっぷ", CMD_FLIP);
		ACTION_MAP.put("フリップ", CMD_FLIP);
		ACTION_MAP.put("turn", CMD_TURN);
		ACTION_MAP.put("ターン", CMD_TURN);
		ACTION_MAP.put("たーん", CMD_TURN);
//--------------------------------------------------------------------------------
		DIR_MAP.put("前", DIR_FORWARD);
		DIR_MAP.put("まえ", DIR_FORWARD);
		DIR_MAP.put("マエ", DIR_FORWARD);
		DIR_MAP.put("forward", DIR_FORWARD);
		DIR_MAP.put("ふぉわーど", DIR_FORWARD);
		DIR_MAP.put("ふぉーわーど", DIR_FORWARD);
		DIR_MAP.put("フォワード", DIR_FORWARD);
		DIR_MAP.put("フォーワード", DIR_FORWARD);
		DIR_MAP.put("front", DIR_FORWARD);
		DIR_MAP.put("ふろんと", DIR_FORWARD);
		DIR_MAP.put("フロント", DIR_FORWARD);
		DIR_MAP.put("go", DIR_FORWARD);
		DIR_MAP.put("ゴー", DIR_FORWARD);
		DIR_MAP.put("ごー", DIR_FORWARD);
		DIR_MAP.put("行け", DIR_FORWARD);
		DIR_MAP.put("いけ", DIR_FORWARD);
		DIR_MAP.put("イケ", DIR_FORWARD);
		DIR_MAP.put("まい", DIR_FORWARD);
		DIR_MAP.put("マイ", DIR_FORWARD);
		DIR_MAP.put("my", DIR_FORWARD);

		DIR_MAP.put("後", DIR_BACKWARD);
		DIR_MAP.put("うしろ", DIR_BACKWARD);
		DIR_MAP.put("ウシロ", DIR_BACKWARD);
		DIR_MAP.put("backward", DIR_FORWARD);
		DIR_MAP.put("ばっくわーど", DIR_BACKWARD);
		DIR_MAP.put("バックワード", DIR_BACKWARD);
		DIR_MAP.put("back", DIR_BACKWARD);
		DIR_MAP.put("ばっく", DIR_BACKWARD);
		DIR_MAP.put("バック", DIR_BACKWARD);
		DIR_MAP.put("りあ", DIR_BACKWARD);
		DIR_MAP.put("リア", DIR_BACKWARD);
		DIR_MAP.put("rear", DIR_BACKWARD);
		DIR_MAP.put("come", DIR_BACKWARD);
		DIR_MAP.put("カム", DIR_BACKWARD);
		DIR_MAP.put("かむ", DIR_BACKWARD);

		DIR_MAP.put("右", DIR_RIGHT);
		DIR_MAP.put("みぎ", DIR_RIGHT);
		DIR_MAP.put("ミギ", DIR_RIGHT);
		DIR_MAP.put("right", DIR_RIGHT);
		DIR_MAP.put("らいと", DIR_RIGHT);
		DIR_MAP.put("ライト", DIR_RIGHT);
		DIR_MAP.put("みみ", DIR_RIGHT);
		DIR_MAP.put("ミミ", DIR_RIGHT);
		DIR_MAP.put("耳", DIR_RIGHT);
		DIR_MAP.put("みーみー", DIR_RIGHT);
		DIR_MAP.put("ミーミー", DIR_RIGHT);
		DIR_MAP.put("三木", DIR_RIGHT);
		DIR_MAP.put("みき", DIR_RIGHT);
		DIR_MAP.put("ミキ", DIR_RIGHT);
		DIR_MAP.put("みに", DIR_RIGHT);
		DIR_MAP.put("ミニ", DIR_RIGHT);

		DIR_MAP.put("左", DIR_LEFT);
		DIR_MAP.put("ひだり", DIR_LEFT);
		DIR_MAP.put("ヒダリ", DIR_LEFT);
		DIR_MAP.put("left", DIR_LEFT);
		DIR_MAP.put("レフト", DIR_LEFT);
		DIR_MAP.put("れふと", DIR_LEFT);
		DIR_MAP.put("稲荷", DIR_LEFT);
		DIR_MAP.put("いなり", DIR_LEFT);
		DIR_MAP.put("イナリ", DIR_LEFT);
		DIR_MAP.put("いいなり", DIR_LEFT);
		DIR_MAP.put("イイナリ", DIR_LEFT);

		DIR_MAP.put("up", DIR_UP);
		DIR_MAP.put("あっぷ", DIR_UP);
		DIR_MAP.put("アップ", DIR_UP);
		DIR_MAP.put("上", DIR_UP);
		DIR_MAP.put("うえ", DIR_UP);
		DIR_MAP.put("ウエ", DIR_UP);
		DIR_MAP.put("上がれ", DIR_UP);
		DIR_MAP.put("あがれ", DIR_UP);
		DIR_MAP.put("アガレ", DIR_UP);
		DIR_MAP.put("上昇", DIR_UP);
		DIR_MAP.put("じょうしょう", DIR_UP);
		DIR_MAP.put("ジョウショウ", DIR_UP);
		DIR_MAP.put("うぇい", DIR_UP);
		DIR_MAP.put("ウェイ", DIR_UP);

		DIR_MAP.put("down", DIR_DOWN);
		DIR_MAP.put("だうん", DIR_DOWN);
		DIR_MAP.put("ダウン", DIR_DOWN);
		DIR_MAP.put("下", DIR_DOWN);
		DIR_MAP.put("した", DIR_DOWN);
		DIR_MAP.put("シタ", DIR_DOWN);
		DIR_MAP.put("下がれ", DIR_DOWN);
		DIR_MAP.put("さがれ", DIR_DOWN);
		DIR_MAP.put("サガレ", DIR_DOWN);
		DIR_MAP.put("降下", DIR_DOWN);
		DIR_MAP.put("こうか", DIR_DOWN);
		DIR_MAP.put("コウカ", DIR_DOWN);
		DIR_MAP.put("下降", DIR_DOWN);
		DIR_MAP.put("かこう", DIR_DOWN);
		DIR_MAP.put("カコウ", DIR_DOWN);

		DIR_MAP.put("々", DIR_SAME);
	}
}
