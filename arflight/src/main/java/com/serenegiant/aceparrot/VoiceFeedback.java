package com.serenegiant.aceparrot;

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

/**
 * Created by saki on 2017/02/17.
 *
 */

public class VoiceFeedback {
	private static final boolean DEBUG = false;	// 実働時はfalseにすること
	private static final String TAG = VoiceFeedback.class.getSimpleName();

	public static final CollectionMap<Long, Integer> ID_MAP = new CollectionMap<Long, Integer>();

	private static Random sRandom = new Random();

	public static int getVoiceFeedbackId(final long cmd) {
		int result = selectFeedbackId((ArrayList<Integer>)ID_MAP.get(cmd));
		if (result == 0) {
			result = selectFeedbackId((ArrayList<Integer>)ID_MAP.get(cmd & CMD_MASK));
		}
		return result;
	}

	private static int selectFeedbackId(final ArrayList<Integer> ids) {
		if (ids != null) {
			final int n = ids != null ? ids.size() : 0;
			if (n > 0) {
				return ids.get(sRandom.nextInt(n));
			}
		}
		return 0;
	}

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
					mSoundPool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
				} else {
					final AudioAttributes.Builder attr = new AudioAttributes.Builder();
					attr.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
					attr.setLegacyStreamType(AudioManager.STREAM_MUSIC);
					attr.setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION);
					final SoundPool.Builder builder = new SoundPool.Builder();
					builder.setAudioAttributes(attr.build());
					builder.setMaxStreams(2);
					mSoundPool = builder.build();
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
		int id = VoiceFeedback.getVoiceFeedbackId(cmd);
		if (id == 0) {
			id = VoiceFeedback.getVoiceFeedbackId(CMD_ERROR);
		}
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
