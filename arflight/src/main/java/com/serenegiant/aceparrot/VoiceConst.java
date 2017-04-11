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

import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseIntArray;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class VoiceConst {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = VoiceConst.class.getSimpleName();

	public static final long MAX_COUNT			= 0x00000003;

	// 方向フラグ...下位8ビット
	public static final int DIR_FORWARD			= 0x00000001;
	public static final int DIR_RIGHT			= 0x00000002;
	public static final int DIR_BACKWARD		= 0x00000004;
	public static final int DIR_LEFT			= 0x00000008;
	public static final int DIR_UP				= 0x00000010;
	public static final int DIR_DOWN			= 0x00000020;
	private static final int DIR_SAME			= 0x00000080;
	// コマンド...上位24ビット
	public static final int CMD_NON				= 0x00000000;
	public static final int CMD_STOP			= 0x00000100;
	public static final int CMD_TAKEOFF			= 0x00000200;
	public static final int CMD_LANDING			= 0x00000400;
	public static final int CMD_MOVE			= 0x00000800;
	public static final int CMD_FLIP			= 0x00001000;
	public static final int CMD_TURN			= 0x00002000;
	public static final int CMD_SPIN			= 0x00004000;
	public static final int CMD_SCRIPT			= 0x00008000;
	public static final int CMD_FIRE			= 0x00010000;
	public static final int CMD_CLAW_OPEN		= 0x00020000;
	public static final int CMD_CLAW_CLOSE		= 0x00040000;
	public static final int CMD_CLAW_TOGGLE		= 0x00080000;
	public static final int CMD_ERROR			= 0x00100000;
	public static final int CMD_GREETINGS		= 0x00200000;
	public static final int CMD_COMPLEX			= 0x80000000;
	// コマンドマスク
	public static final long CMD_MASK_CLAW		= CMD_CLAW_OPEN | CMD_CLAW_CLOSE | CMD_CLAW_TOGGLE;
	public static final long CMD_MASK_MAMBO		= CMD_FIRE | CMD_MASK_CLAW;
	public static final long CMD_MASK			= 0xffffff00;
	// 回数フラグの上限値
	private static final long CMD_FORWARD_MAX	= CMD_MOVE | DIR_FORWARD | (MAX_COUNT << 32);
	private static final long CMD_RIGHT_MAX		= CMD_MOVE | DIR_RIGHT | (MAX_COUNT << 36);
	private static final long CMD_BACKWARD_MAX	= CMD_MOVE | DIR_BACKWARD | (MAX_COUNT << 40);
	private static final long CMD_LEFT_MAX		= CMD_MOVE | DIR_LEFT | (MAX_COUNT << 44);
	private static final long CMD_UP_MAX		= CMD_MOVE | DIR_UP | (MAX_COUNT << 48);
	private static final long CMD_DOWN_MAX		= CMD_MOVE | DIR_DOWN | (MAX_COUNT << 52);
	// 挨拶コマンド
	public static final long CMD_GREETINGS_HELLO	= CMD_GREETINGS | 0x00000001;
	// 複合コマンド
	public static final long CMD_COMPLEX_UP_TURN_LANDING = CMD_COMPLEX | 0x00000001;
	// エラーコマンド
	public static final long CMD_SR_ERROR_AUDIO	= CMD_ERROR | SpeechRecognizer.ERROR_AUDIO;
	public static final long CMD_SR_ERROR_CLIENT	= CMD_ERROR | SpeechRecognizer.ERROR_CLIENT;
	public static final long CMD_SR_ERROR_INSUFFICIENT_PERMISSIONS	= CMD_ERROR | SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS;
	public static final long CMD_SR_ERROR_NETWORK	= CMD_ERROR | SpeechRecognizer.ERROR_NETWORK;
	public static final long CMD_SR_ERROR_NETWORK_TIMEOUT	= CMD_ERROR | SpeechRecognizer.ERROR_NETWORK_TIMEOUT;
	public static final long CMD_SR_ERROR_NO_MATCH	= CMD_ERROR | SpeechRecognizer.ERROR_NO_MATCH;
	public static final long CMD_SR_ERROR_RECOGNIZER_BUSY	= CMD_ERROR | SpeechRecognizer.ERROR_RECOGNIZER_BUSY;
	public static final long CMD_SR_ERROR_SERVER	= CMD_ERROR | SpeechRecognizer.ERROR_SERVER;
	public static final long CMD_SR_ERROR_SPEECH_TIMEOUT	= CMD_ERROR | SpeechRecognizer.ERROR_SPEECH_TIMEOUT;

	public static final long CMD_ERROR_BATTERY_LOW_CRITICAL = CMD_ERROR | 0x00000100;
	public static final long CMD_ERROR_BATTERY_LOW = CMD_ERROR | 0x00000200;
	public static final long CMD_ERROR_MOTOR = CMD_ERROR | 0x00000400;

	private static final Random sRandom = new Random();

	public static float getRoll(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 36) & 0x03) * (((cmd & DIR_RIGHT) == DIR_RIGHT) ? 100 : 0)
			- (float)((cmd >>> 44) & 0x03) * (((cmd & DIR_LEFT) == DIR_LEFT) ? 100 : 0))  * 0.333f
			: 0.0f;
	}

	public static float getPitch(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 32) & 0x03) * (((cmd & DIR_FORWARD) == DIR_FORWARD) ? 100 : 0)
			- (float)((cmd >>> 40) & 0x03) * (((cmd & DIR_BACKWARD) == DIR_BACKWARD) ? 100 : 0)) * 0.333f
			: 0.0f;
	}

	public static float getGaz(final long cmd) {
		return ((cmd & CMD_MOVE) == CMD_MOVE) ?
			((float)((cmd >>> 48) & 0x03) * (((cmd & DIR_UP) == DIR_UP) ? 100 : 0)
			- (float)((cmd >>> 52) & 0x03) * (((cmd & DIR_DOWN) == DIR_DOWN) ? 100 : 0)) * 0.333f
			: 0.0f;
	}

	public static float getYaw(final long cmd) {
		return ((cmd & CMD_TURN) == CMD_TURN) ?
			(((cmd & DIR_RIGHT) == DIR_RIGHT) ? 100 : 0)
			- (((cmd & DIR_LEFT) == DIR_LEFT) ? 100 : 0)
			: 0.0f;
	}

	public static int getSpin(final long cmd) {
		return ((cmd & CMD_SPIN) == CMD_SPIN) ?
			(int)((cmd >>> 32) & 0x03)
				* ((((cmd & DIR_RIGHT) == DIR_RIGHT) ? 360 : 0)
					- (((cmd & DIR_LEFT) == DIR_LEFT) ? 360 : 0))
			: 0;
	}

	public static int getScript(final long cmd) {
		return ((cmd & CMD_SCRIPT) == CMD_SCRIPT) ? (int)(cmd >>> 32) : -1;
	}

	public static long findCommand(final String text) {
		long cmd = CMD_NON;

		if (DEBUG) Log.v(TAG, "findCommand:" + text);
		if (!TextUtils.isEmpty(text)) {
			cmd = findCmd(text);
			if (cmd == CMD_NON) {
				cmd = findAction(text);
				if (cmd == CMD_NON) {
					cmd = findMove(text);
					// If you un-comment following lines,
					// app will recognize script name as command.
					// But be careful use this because false recognition
					// will raise unexpected fly
					if (cmd == CMD_NON) {
						cmd = findScript(text);
					}
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
		final int len = text.length();
		final Set<String> actions = ACTION_MAP.keySet();
		for (final String action: actions) {
			final int pos = text.indexOf(action);
			if (pos >= 0) {
				final long actionCmd = ACTION_MAP.get(action);
				if ((actionCmd & CMD_MASK_MAMBO) != 0) {
					return actionCmd;
				} else if (actionCmd == CMD_SPIN) {
					int cnt = 0;
					final Set<String> dirs = DIR_MAP.keySet();
					for (final String dir: dirs) {
						final int dirPos = text.lastIndexOf(dir);
						if (dirPos >= 0) {
							final int flipDir = DIR_MAP.get(dir);
							switch (flipDir) {
							case DIR_RIGHT:
								cnt++;
								break;
							case DIR_LEFT:
								cnt--;
							}
						}
					}
					if (cnt == 0) {
						cnt = sRandom.nextInt(6) - 3;
						if (cnt == 0) {
							cnt = 1;
						}
					} else if (cnt > 3) {
						cnt = 3;
					} else if (cnt < -3) {
						cnt = -3;
					}
					if (cnt > 0) {
						// 右スピン
						return ((long)(cnt & 0x03) << 32) | actionCmd | DIR_RIGHT;
					} else {
						// 左スピン
						return ((long)(-cnt & 0x03) << 32) | actionCmd | DIR_LEFT;
					}
				} else if ((actionCmd & CMD_MASK) == CMD_COMPLEX) {
					// 複合コマンドの時はそのまま返す
					return actionCmd;
				} else {
					final Set<String> dirs = DIR_MAP.keySet();
					for (final String dir: dirs) {
						final int dirPos = text.lastIndexOf(dir);
						if (dirPos >= 0) {
							final int flipDir = DIR_MAP.get(dir);
							switch (flipDir) {
							case DIR_UP:
							case DIR_DOWN:
								if (actionCmd == CMD_FLIP) continue;
								// pass through
							case DIR_FORWARD:
							case DIR_BACKWARD:
								if ((actionCmd == CMD_TURN)
									|| (actionCmd == CMD_SPIN)) continue;
								// pass through
							case DIR_RIGHT:
							case DIR_LEFT:
								return actionCmd | flipDir;
							}
						}
					}
				}
			}
		}
		return CMD_NON;
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

	private static final String[] SCRIPTS = {
		"script",
		"すくりぷと",
		"スクリプト",
	};

	private static long findScript(@NonNull final String text) {
		long cmd = CMD_NON;

		for (final String s: SCRIPTS) {
			// script/すくりぷと/スクリプトを含むかどうかをチェック
			int ix = text.indexOf(s);
			if (ix >= 0) {
				// 含んでいる時
				try {
					// 末尾に数字があればそれをスクリプトのインデックスとする
					final String sub = text.substring(ix + s.length());
					cmd = CMD_SCRIPT | (((long) Integer.parseInt(sub)) << 32);
				} catch (final Exception e) {
					// ignore
				}
				if (cmd == CMD_NON) {
					// 末尾に数字がついていない時はスクリプトトークンを探す
					synchronized (SCRIPT_MAP) {
						final Set<String> tokens = SCRIPT_MAP.keySet();
						for (final String token: tokens) {
							if (text.contains(token)) {
								// 最初に見つかったものを返す
								cmd = CMD_SCRIPT | (SCRIPT_MAP.get(token) << 32);
								break;
							}
						}
					}
				}
			}
		}

		return cmd;
	}

	/**
	 * Mambo固有機能の有効・無効をセット
	 * @param hasClaw
	 * @param hasGun
	 */
	public static void setEnableMambo(final boolean hasClaw, final boolean hasGun) {
		for (final String key: ACTION_MAP_MAMBO.keySet()) {
			final long cmd = ACTION_MAP_MAMBO.get(key);
			if ((cmd & CMD_MASK_CLAW) != 0) {
				if (hasClaw) {
					ACTION_MAP.put(key, cmd);
				} else {
					ACTION_MAP.remove(key);
				}
			} else {
				if (hasGun) {
					ACTION_MAP.put(key, cmd);
				} else {
					ACTION_MAP.remove(key);
				}
			}
		}
	}

	private static final Map<String, Long> CMD_MAP = new LinkedHashMap<String, Long>();
	private static final Map<String, Long> ACTION_MAP = new LinkedHashMap<String, Long>();
	private static final Map<String, Long> ACTION_MAP_MAMBO = new LinkedHashMap<String, Long>();
	private static final Map<String, Integer> DIR_MAP = new LinkedHashMap<String, Integer>();
	public static final Map<String, Long> SCRIPT_MAP = new LinkedHashMap<String, Long>();
	static {
		CMD_MAP.put("stop", (long)CMD_STOP);
		CMD_MAP.put("すとっぷ", (long)CMD_STOP);
		CMD_MAP.put("ストップ", (long)CMD_STOP);
		CMD_MAP.put("止まれ", (long)CMD_STOP);
		CMD_MAP.put("止れ", (long)CMD_STOP);
		CMD_MAP.put("停止", (long)CMD_STOP);
		CMD_MAP.put("停まれ", (long)CMD_STOP);
		CMD_MAP.put("とまれ", (long)CMD_STOP);
		CMD_MAP.put("トマレ", (long)CMD_STOP);
		CMD_MAP.put("あかん", (long)CMD_STOP);
		CMD_MAP.put("ぶつかる", (long)CMD_STOP);
		CMD_MAP.put("ブツカル", (long)CMD_STOP);
		CMD_MAP.put("当たる", (long)CMD_STOP);
		CMD_MAP.put("当る", (long)CMD_STOP);
		CMD_MAP.put("あたる", (long)CMD_STOP);
		CMD_MAP.put("アタル", (long)CMD_STOP);
		CMD_MAP.put("アカン", (long)CMD_STOP);
		CMD_MAP.put("きゃ", (long)CMD_STOP);
		CMD_MAP.put("キャ", (long)CMD_STOP);
		CMD_MAP.put("わー", (long)CMD_STOP);
		CMD_MAP.put("ワー", (long)CMD_STOP);
		CMD_MAP.put("arrêtez", (long)CMD_STOP);
		CMD_MAP.put("fermata", (long)CMD_STOP);
		CMD_MAP.put("detener", (long)CMD_STOP);
		CMD_MAP.put("halt", (long)CMD_STOP);

//
		CMD_MAP.put("land", (long)CMD_LANDING);
		CMD_MAP.put("らんど", (long)CMD_LANDING);
		CMD_MAP.put("ランド", (long)CMD_LANDING);
		CMD_MAP.put("landing", (long)CMD_LANDING);
		CMD_MAP.put("らんでぃんぐ", (long)CMD_LANDING);
		CMD_MAP.put("ランディング", (long)CMD_LANDING);
		CMD_MAP.put("らんにんぐ", (long)CMD_LANDING);
		CMD_MAP.put("ランニング", (long)CMD_LANDING);
		CMD_MAP.put("着陸", (long)CMD_LANDING);
		CMD_MAP.put("ちゃくりく", (long)CMD_LANDING);
		CMD_MAP.put("チャクリク", (long)CMD_LANDING);
		CMD_MAP.put("debarquer", (long)CMD_LANDING);
		CMD_MAP.put("atterraggio", (long)CMD_LANDING);
		CMD_MAP.put("aterrizar", (long)CMD_LANDING);
		CMD_MAP.put("landung", (long)CMD_LANDING);
//
		CMD_MAP.put("take off", (long)CMD_TAKEOFF);
		CMD_MAP.put("離陸", (long)CMD_TAKEOFF);
		CMD_MAP.put("りりく", (long)CMD_TAKEOFF);
		CMD_MAP.put("リリク", (long)CMD_TAKEOFF);
		CMD_MAP.put("りりっく", (long)CMD_TAKEOFF);
		CMD_MAP.put("リリック", (long)CMD_TAKEOFF);
		CMD_MAP.put("飛べ", (long)CMD_TAKEOFF);
		CMD_MAP.put("とべ", (long)CMD_TAKEOFF);
		CMD_MAP.put("トベ", (long)CMD_TAKEOFF);
		CMD_MAP.put("飛ぶ", (long)CMD_TAKEOFF);
		CMD_MAP.put("とぶ", (long)CMD_TAKEOFF);
		CMD_MAP.put("トブ", (long)CMD_TAKEOFF);
		CMD_MAP.put("いりく", (long)CMD_TAKEOFF);
		CMD_MAP.put("イリク", (long)CMD_TAKEOFF);
		CMD_MAP.put("wake up", (long)CMD_TAKEOFF);
		CMD_MAP.put("ウエイク アップ", (long)CMD_TAKEOFF);
		CMD_MAP.put("ウェイク アップ", (long)CMD_TAKEOFF);
		CMD_MAP.put("うえいく あっぷ", (long)CMD_TAKEOFF);
		CMD_MAP.put("うぇいく あっぷ", (long)CMD_TAKEOFF);
		CMD_MAP.put("wake", (long)CMD_TAKEOFF);
		CMD_MAP.put("ウエイク", (long)CMD_TAKEOFF);
		CMD_MAP.put("ウェイク", (long)CMD_TAKEOFF);
		CMD_MAP.put("うえいく", (long)CMD_TAKEOFF);
		CMD_MAP.put("うぇいく", (long)CMD_TAKEOFF);
		CMD_MAP.put("起きろよ", (long)CMD_TAKEOFF);
		CMD_MAP.put("おきろよ", (long)CMD_TAKEOFF);
		CMD_MAP.put("起きろ", (long)CMD_TAKEOFF);
		CMD_MAP.put("おきろ", (long)CMD_TAKEOFF);
		CMD_MAP.put("décollage", (long)CMD_TAKEOFF);
		CMD_MAP.put("decollare", (long)CMD_TAKEOFF);
		CMD_MAP.put("despegue", (long)CMD_TAKEOFF);
		CMD_MAP.put("abfliegen", (long)CMD_TAKEOFF);

//--------------------------------------------------------------------------------
		ACTION_MAP.put("flip", (long)CMD_FLIP);
		ACTION_MAP.put("ふりっぷ", (long)CMD_FLIP);
		ACTION_MAP.put("フリップ", (long)CMD_FLIP);
		ACTION_MAP.put("宙返り", (long)CMD_FLIP);
		ACTION_MAP.put("ちゅうがえり", (long)CMD_FLIP);
		ACTION_MAP.put("チュウガエリ", (long)CMD_FLIP);
		ACTION_MAP.put("ちゅうかえり", (long)CMD_FLIP);
		ACTION_MAP.put("チュウカエリ", (long)CMD_FLIP);
		ACTION_MAP.put("capovolgere", (long)CMD_FLIP);
		ACTION_MAP.put("capirotazo", (long)CMD_FLIP);
		ACTION_MAP.put("purzelbaum", (long)CMD_FLIP);

		ACTION_MAP.put("turn", (long)CMD_TURN);
		ACTION_MAP.put("ターン", (long)CMD_TURN);
		ACTION_MAP.put("たーん", (long)CMD_TURN);
		ACTION_MAP.put("タン", (long)CMD_TURN);
		ACTION_MAP.put("たん", (long)CMD_TURN);
		ACTION_MAP.put("回転", (long)CMD_TURN);
		ACTION_MAP.put("開店", (long)CMD_TURN);
		ACTION_MAP.put("かいてん", (long)CMD_TURN);
		ACTION_MAP.put("カイテン", (long)CMD_TURN);
		ACTION_MAP.put("回る", (long)CMD_TURN);
		ACTION_MAP.put("まわる", (long)CMD_TURN);
		ACTION_MAP.put("マワル", (long)CMD_TURN);
		ACTION_MAP.put("回り", (long)CMD_TURN);
		ACTION_MAP.put("まわり", (long)CMD_TURN);
		ACTION_MAP.put("マワリ", (long)CMD_TURN);
		ACTION_MAP.put("tour", (long)CMD_TURN);
		ACTION_MAP.put("turno", (long)CMD_TURN);
		ACTION_MAP.put("giro", (long)CMD_TURN);
		ACTION_MAP.put("drehen", (long)CMD_TURN);

		ACTION_MAP.put("すぴん", (long)CMD_SPIN);
		ACTION_MAP.put("スピン", (long)CMD_SPIN);
		ACTION_MAP.put("すっぴん", (long)CMD_SPIN);
		ACTION_MAP.put("スッピン", (long)CMD_SPIN);
		ACTION_MAP.put("spin", (long)CMD_SPIN);

		ACTION_MAP.put("move", (long)CMD_MOVE);
		ACTION_MAP.put("むーぶ", (long)CMD_MOVE);
		ACTION_MAP.put("ムーブ", (long)CMD_MOVE);
		ACTION_MAP.put("移動", (long)CMD_MOVE);
		ACTION_MAP.put("いどう", (long)CMD_MOVE);
		ACTION_MAP.put("イドウ", (long)CMD_MOVE);
		ACTION_MAP.put("動く", (long)CMD_MOVE);
		ACTION_MAP.put("うごく", (long)CMD_MOVE);
		ACTION_MAP.put("ウゴク", (long)CMD_MOVE);
		ACTION_MAP.put("動け", (long)CMD_MOVE);
		ACTION_MAP.put("うごけ", (long)CMD_MOVE);
		ACTION_MAP.put("ウゴケ", (long)CMD_MOVE);
		ACTION_MAP.put("mouvement", (long)CMD_MOVE);
		ACTION_MAP.put("mossa", (long)CMD_MOVE);
		ACTION_MAP.put("movimiento", (long)CMD_MOVE);
		ACTION_MAP.put("bewegung", (long)CMD_MOVE);

		ACTION_MAP.put("働けよ", CMD_COMPLEX_UP_TURN_LANDING);
		ACTION_MAP.put("はたらけよ", CMD_COMPLEX_UP_TURN_LANDING);
		ACTION_MAP.put("ハタラケヨ", CMD_COMPLEX_UP_TURN_LANDING);
		ACTION_MAP.put("働け", CMD_COMPLEX_UP_TURN_LANDING);
		ACTION_MAP.put("はたらけ", CMD_COMPLEX_UP_TURN_LANDING);
		ACTION_MAP.put("ハタラケ", CMD_COMPLEX_UP_TURN_LANDING);

		ACTION_MAP.put("仕事しろ", CMD_FORWARD_MAX);
		ACTION_MAP.put("シゴトシロ", CMD_FORWARD_MAX);
		ACTION_MAP.put("しごとしろ", CMD_FORWARD_MAX);

//--------------------------------------------------------------------------------
		ACTION_MAP_MAMBO.put("開け", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ひらけ", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ヒラケ", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("開く", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ひらく", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ヒラク", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("あけ", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("アケ", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ごま", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("ゴマ", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("open", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("おーぷん", (long)CMD_CLAW_OPEN);
		ACTION_MAP_MAMBO.put("オープン", (long)CMD_CLAW_OPEN);

		ACTION_MAP_MAMBO.put("閉じろ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("とじろ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("トジロ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("close", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("くろーず", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("クローズ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("掴め", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("つかめ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("ツカメ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("catch", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("きゃっち", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("キャッチ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("grab", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("ぐらぶ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("グラブ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("chuck", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("ちゃっく", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("チャック", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("take", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("ていく", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("テイク", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("clamp", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("くらんぷ", (long)CMD_CLAW_CLOSE);
		ACTION_MAP_MAMBO.put("クランプ", (long)CMD_CLAW_CLOSE);

		ACTION_MAP_MAMBO.put("とぐる", (long)CMD_CLAW_TOGGLE);
		ACTION_MAP_MAMBO.put("トグル", (long)CMD_CLAW_TOGGLE);
		ACTION_MAP_MAMBO.put("反転", (long)CMD_CLAW_TOGGLE);
		ACTION_MAP_MAMBO.put("はんてん", (long)CMD_CLAW_TOGGLE);
		ACTION_MAP_MAMBO.put("ハンテン", (long)CMD_CLAW_TOGGLE);

		ACTION_MAP_MAMBO.put("打て", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("撃て", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("打つ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("撃つ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("うて", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ウテ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("うつ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ウツ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("発射", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("はっしゃ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ハッシャ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("fire", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ふぁいや", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ファイヤ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ふぁいあ", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ファイア", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("shoot", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("しゅーと", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("シュート", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ばーん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("バーン", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ぱーん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("パーン", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ちゅどーん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("チュドーン", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ばきゅーん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("バキューン", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ばきゅ〜ん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("バキュ〜ン", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("ばっきゅーん", (long)CMD_FIRE);
		ACTION_MAP_MAMBO.put("バッキューン", (long)CMD_FIRE);

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
		DIR_MAP.put("いっけ", DIR_FORWARD);
		DIR_MAP.put("イッケ", DIR_FORWARD);
		DIR_MAP.put("まい", DIR_FORWARD);
		DIR_MAP.put("マイ", DIR_FORWARD);
		DIR_MAP.put("my", DIR_FORWARD);
		DIR_MAP.put("devant", DIR_FORWARD);
		DIR_MAP.put("davanti", DIR_FORWARD);
		DIR_MAP.put("frente", DIR_FORWARD);
		DIR_MAP.put("vorderseite", DIR_FORWARD);

		DIR_MAP.put("後", DIR_BACKWARD);
		DIR_MAP.put("うしろ", DIR_BACKWARD);
		DIR_MAP.put("ウシロ", DIR_BACKWARD);
		DIR_MAP.put("backward", DIR_BACKWARD);
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
		DIR_MAP.put("arrière", DIR_BACKWARD);
		DIR_MAP.put("indietro", DIR_BACKWARD);
		DIR_MAP.put("espalda", DIR_BACKWARD);
		DIR_MAP.put("zurück", DIR_BACKWARD);

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
		DIR_MAP.put("droite", DIR_RIGHT);
		DIR_MAP.put("destra", DIR_RIGHT);
		DIR_MAP.put("derecho", DIR_RIGHT);
		DIR_MAP.put("recht", DIR_RIGHT);

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
		DIR_MAP.put("gauche", DIR_LEFT);
		DIR_MAP.put("sinistra", DIR_LEFT);
		DIR_MAP.put("izquierda", DIR_LEFT);
		DIR_MAP.put("links", DIR_LEFT);

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
		DIR_MAP.put("s'élever", DIR_UP);
		DIR_MAP.put("su", DIR_UP);
		DIR_MAP.put("arriba", DIR_UP);
		DIR_MAP.put("hoch", DIR_UP);

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
		DIR_MAP.put("dessous", DIR_DOWN);
		DIR_MAP.put("giu", DIR_DOWN);
		DIR_MAP.put("abajo", DIR_DOWN);
		DIR_MAP.put("unten", DIR_DOWN);

		DIR_MAP.put("々", DIR_SAME);
	}
}
