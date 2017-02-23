package com.serenegiant.aceparrot;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
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
		if (mSoundPool == null) {
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
			final Collection<Integer> ids = VoiceFeedback.ID_MAP.valuesAll();
			for (final int id: ids) {
				if ((id != 0) && (mSoundIds.get(id, 0) == 0)) {
					mSoundIds.put(id, mSoundPool.load(context, id, 1));
				}
			}
		}
	}

	public synchronized void release() {
		mSoundIds.clear();
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}

	public synchronized void playVoiceFeedback(final long cmd) {
		int id = VoiceFeedback.getVoiceFeedbackId(cmd);
		if (id == 0) {
			id = VoiceFeedback.getVoiceFeedbackId(CMD_ERROR);
		}
		final int soundId = mSoundIds.get(id, 0);
		if ((mSoundPool != null) && (id != 0)) {
			mSoundPool.play(soundId, 1.0f, 1.0f, 0, 0, 1.0f);
		}
	}

}
