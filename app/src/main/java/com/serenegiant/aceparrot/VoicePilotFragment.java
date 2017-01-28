package com.serenegiant.aceparrot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

/**
 * Created by saki on 2017/01/28.
 *
 */

public class VoicePilotFragment extends PilotFragment {
	private static final boolean DEBUG = true;
	private static final String TAG = VoicePilotFragment.class.getSimpleName();

	private SpeechRecognizer mSpeechRecognizer;
	private AudioManager mAudioManager;
	private int mStreamVolume = 0;

	public VoicePilotFragment() {
		super();
		// デフォルトコンストラクタが必要
	}

	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		startSpeechRecognizer();
	}

	@Override
	protected void internalOnPause() {
		stopSpeechRecognizer();
		super.internalOnPause();
	}

	private Intent mRecognizerIntent;
	private void startSpeechRecognizer() {
		if (DEBUG) Log.v(TAG, "startSpeechRecognizer:");
		final Activity activity = getActivity();
		if ((activity == null) || activity.isFinishing()) return;
		if (!SpeechRecognizer.isRecognitionAvailable(activity)) return;
		if (mAudioManager == null) {
			mAudioManager = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
			mStreamVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
		}
		if (mSpeechRecognizer == null) {
			mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
			mSpeechRecognizer.setRecognitionListener(mRecognitionListener);
		}
		if (mRecognizerIntent == null) {
			mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity.getPackageName());
		}
		mSpeechRecognizer.startListening(mRecognizerIntent);
	}

	private void stopSpeechRecognizer() {
		if (DEBUG) Log.v(TAG, "stopSpeechRecognizer:");
		removeFromUIThread(mStartSpeechRecognizerTask);
		if (mSpeechRecognizer != null) {
			try {
				mSpeechRecognizer.cancel();
				mSpeechRecognizer.stopListening();
				mSpeechRecognizer.destroy();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			mSpeechRecognizer = null;
		}
		mRecognizerIntent = null;
		if (mAudioManager != null) {
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mStreamVolume, 0);
			mAudioManager = null;
		}
	}

	private Runnable mStartSpeechRecognizerTask
		= new Runnable() {
		@Override
		public void run() {
			startSpeechRecognizer();
		}
	};

	private final RecognitionListener mRecognitionListener
		= new RecognitionListener() {
		@Override
		public void onReadyForSpeech(final Bundle params) {
			if (DEBUG) Log.v(TAG, "onReadyForSpeech:");
		}

		@Override
		public void onBeginningOfSpeech() {
			if (DEBUG) Log.v(TAG, "onBeginningOfSpeech:");
		}

		@Override
		public void onRmsChanged(final float rmsdB) {
			if (DEBUG) Log.v(TAG, "onRmsChanged:");
		}

		@Override
		public void onBufferReceived(final byte[] buffer) {
			if (DEBUG) Log.v(TAG, "onBufferReceived:");
		}

		@Override
		public void onEndOfSpeech() {
			if (DEBUG) Log.v(TAG, "onEndOfSpeech:");
		}

		@Override
		public void onError(final int error) {
			if (DEBUG) Log.v(TAG, "onError:");
			switch (error) {
			case SpeechRecognizer.ERROR_AUDIO:
				Log.e(TAG, "音声データ保存失敗");
				break;
			case SpeechRecognizer.ERROR_CLIENT:
				Log.e(TAG, "Android端末内のエラー(その他)");
				break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				Log.e(TAG, "権限無し");
				break;
			case SpeechRecognizer.ERROR_NETWORK:
				Log.e(TAG, "ネットワークエラー(その他)");
				break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				Log.e(TAG, "ネットワークタイムアウトエラー");
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				Log.d(TAG, "音声認識結果無し");
				showToast("no match Text data", Toast.LENGTH_LONG);
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				Log.e(TAG, "RecognitionServiceへ要求出せず");
				break;
			case SpeechRecognizer.ERROR_SERVER:
				Log.e(TAG, "Server側からエラー通知");
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				Log.e(TAG, "音声入力無し");
				showToast("no input ?", Toast.LENGTH_LONG);
				break;
			default:
				break;
			}
			runOnUiThread(mStartSpeechRecognizerTask, 100);
		}

	    @Override
	    public void onResults(final Bundle results) {
			if (DEBUG) Log.v(TAG, "onResults:" + results);
	        final List<String> recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	        final float[] conf = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
	        int i = 0;
	        for (final String data: recData) {
	        	Log.v(TAG, "onResults:" + (conf != null ? conf[i] : "?") + "/" + data);
				i++;
			}
			runOnUiThread(mStartSpeechRecognizerTask, 100);
	    }

		@Override
		public void onPartialResults(final Bundle partialResults) {
			if (DEBUG) Log.v(TAG, "onPartialResults:" + partialResults);
		}

		@Override
		public void onEvent(final int eventType, final Bundle params) {
			if (DEBUG) Log.v(TAG, "onEvent:" + params);
		}
	};
}
