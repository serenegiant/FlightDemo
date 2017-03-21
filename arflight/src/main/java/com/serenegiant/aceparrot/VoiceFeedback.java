package com.serenegiant.aceparrot;
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.util.Log;
import android.util.SparseIntArray;

import com.serenegiant.utils.CollectionMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static com.serenegiant.aceparrot.VoiceConst.*;

public class VoiceFeedback {
	private static final boolean DEBUG = false;	// 実働時はfalseにすること
	private static final String TAG = VoiceFeedback.class.getSimpleName();

	/**
	 * cmdから音声フィードバック用の音声rawリソースの対応を保持するためのCollectionMap
	 * key=cmd, value=音声RAWリソースID
	 * CollectionMapのデフォルトのコレクションはArrayList
	 */
	public static final CollectionMap<Long, Integer> ID_MAP = new CollectionMap<Long, Integer>();

	private static Random sRandom = new Random();

	/**
	 * cmdから対応する音声rawリソースIDを取得する
	 * @param cmd
	 * @return
	 */
	public static int getVoiceFeedbackId(final long cmd) {
		if (DEBUG) Log.v(TAG, String.format("getVoiceFeedbackId:cmd=%16x", cmd));
		// 回数フラグは落として取得
		int result = selectFeedbackId((ArrayList<Integer>)ID_MAP.get(cmd & 0xffffffffL));
		if (DEBUG) Log.v(TAG, "selectFeedbackId:result=" + result);
		if (result == 0) {
			// 方向フラグも落として取得を試みる
			if (DEBUG) Log.v(TAG, "selectFeedbackId retry with CMD_MASK:result=" + result);
			result = selectFeedbackId((ArrayList<Integer>)ID_MAP.get(cmd & CMD_MASK));
		}
		return result;
	}

	private static int selectFeedbackId(final ArrayList<Integer> ids) {
		if (DEBUG) Log.v(TAG, "selectFeedbackId:ids=" + ids);
		if (ids != null) {
			final int n = ids != null ? ids.size() : 0;
			if (DEBUG) Log.v(TAG, "selectFeedbackId:n=" + n);
			if (n > 0) {
				return ids.get(sRandom.nextInt(n));
			}
		}
		return 0;
	}

	/**
	 * 音声rawリソースIDとサウンドプールのIDの対応を保持するためのSparseIntArray
	 * key=音声rawリソースID, value=サウンドプールのID
	 */
	private final SparseIntArray mSoundIds = new SparseIntArray();
	private SoundPool mSoundPool;

	public VoiceFeedback() {
	}

	@Override
	protected void finalize() throws Throwable {
		release();
		super.finalize();
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	public synchronized void init(final Context context) {
		if (DEBUG) Log.v(TAG, "init:");
		if (mSoundPool == null) {
			SoundPool pool = null;
			try {
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
					pool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
				} else {
					final AudioAttributes.Builder attr = new AudioAttributes.Builder();
					attr.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
					attr.setLegacyStreamType(AudioManager.STREAM_MUSIC);
					attr.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION);
					final SoundPool.Builder builder = new SoundPool.Builder();
					builder.setAudioAttributes(attr.build());
					builder.setMaxStreams(2);
					pool = builder.build();
				}
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			mSoundPool = pool;
			if (pool != null) {
				final Collection<Integer> ids = ID_MAP.valuesAll();
				for (final int id: ids) {
					if ((id != 0) && (mSoundIds.get(id, 0) == 0)) {
						mSoundIds.put(id, mSoundPool.load(context, id, 1));
						if (DEBUG) Log.v(TAG, "init:id=" + id + ",soundId=" + mSoundIds.get(id));
					}
				}
			}
		}
	}

	public synchronized void release() {
		if (DEBUG) Log.v(TAG, "release:");
		mSoundIds.clear();
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}

	/**
	 * 音声フィーフォバックを再生時はtrueを返す
	 * @param cmd
	 * @return true 音声フィードバックを再生した
	 */
	public synchronized boolean playVoiceFeedback(final long cmd) {
		if (DEBUG) Log.v(TAG, String.format("playVoiceFeedback:cmd=%16x", cmd));
		// cmdから対応する音声rawリソースIDを取得する
		int id = getVoiceFeedbackId(cmd);
		if (id == 0) {
			id = getVoiceFeedbackId(CMD_ERROR);
		}
		// 音声rawリソースIDからサウンドプールIDを取得する
		final int soundId = mSoundIds.get(id, 0);
		if (DEBUG) Log.v(TAG, "playVoiceFeedback:id=" + id + ",soundId=" + soundId);
		if ((mSoundPool != null) && (soundId != 0)) {
			try {
				mSoundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
				if (DEBUG) Log.v(TAG, "playVoiceFeedback:play");
				return true;
			} catch (final Exception e) {
				// ignore
			}
		}
		return false;
	}

}
