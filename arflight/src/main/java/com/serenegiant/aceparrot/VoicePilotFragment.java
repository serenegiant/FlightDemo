package com.serenegiant.aceparrot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.BuildConfig;
import com.serenegiant.arflight.R;

import java.util.List;
import java.util.Map;

import jp.co.rediscovery.arflight.DeviceInfo;
import jp.co.rediscovery.arflight.DroneStatus;
import jp.co.rediscovery.arflight.IDeviceController;
import jp.co.rediscovery.arflight.IFlightController;
import jp.co.rediscovery.arflight.controllers.FlightControllerMambo;

import static com.serenegiant.aceparrot.AppConst.*;
import static com.serenegiant.aceparrot.VoiceConst.*;

/**
 * Created by saki on 2017/01/28.
 *
 */

public class VoicePilotFragment extends PilotFragment {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static final String TAG = VoicePilotFragment.class.getSimpleName();

	public static VoicePilotFragment newInstance(final ARDiscoveryDeviceService device, final DeviceInfo info) {
		if (!BuildConfig.USE_SKYCONTROLLER) throw new RuntimeException("does not support skycontroller now");
		final VoicePilotFragment fragment = new VoicePilotFragment();
		fragment.setDevice(device, info);
		return fragment;
	}

	private SpeechRecognizer mSpeechRecognizer;
	private AudioManager mAudioManager;
	private int mStreamVolume = 0;
	private boolean mOfflineVoiceRecognition;
	private boolean mScriptVoiceRecognition;
	private float mDampingRate;
	private VoiceFeedback mVoiceFeedback;

	public VoicePilotFragment() {
		super();
		// デフォルトコンストラクタが必要
	}
	@Override
	protected void internalOnResume() {
		super.internalOnResume();
		final SharedPreferences pref = getActivity().getPreferences(0);
		mOfflineVoiceRecognition = pref.getBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, false)
			&& (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
		mScriptVoiceRecognition = pref.getBoolean(KEY_CONFIG_VOICE_RECOGNITION_ENABLE_SCRIPT, false);
		mDampingRate = pref.getInt(KEY_CONFIG_VOICE_RECOGNITION_DAMPING_RATE, DEFAULT_VOICE_RECOGNITION_DAMPING_RATE) / 100.0f;
		mVoiceFeedback = new VoiceFeedback();
		mVoiceFeedback.init(getActivity());
	}

	@Override
	protected void internalOnPause() {
		if (mVoiceFeedback != null) {
			mVoiceFeedback.release();
			mVoiceFeedback = null;
		}
		stopSpeechRecognizer();
		super.internalOnPause();
	}

	@Override
	protected void onConnect(final IDeviceController controller) {
		super.onConnect(controller);
		if (mFlightController instanceof FlightControllerMambo) {
			final FlightControllerMambo mambo = (FlightControllerMambo)mFlightController;
			VoiceConst.setEnableMambo(mambo.hasClaw(), mambo.hasGun());
		}
		runOnUiThread(mStartSpeechRecognizerTask);
	}

	@Override
	protected void onDisconnect(final IDeviceController controller) {
		stopSpeechRecognizer();
		VoiceConst.setEnableMambo(false, false);
		super.onDisconnect(controller);
	}

	@Override
	protected List<String> setupScript() {
		final List<String> result = super.setupScript();

		if (mScriptVoiceRecognition) {
			synchronized (VoiceConst.SCRIPT_MAP) {
				final Map<String, Integer> map = VoiceConst.SCRIPT_MAP;
				map.clear();
				final int n = mScripts.size();
				for (int i = 0; i < n; i++) {
					final ScriptHelper.ScriptRec script = mScripts.get(i);
					// if script name contains "|", use split texts as name for voice recognition
					if (script.name.contains("|")) {
						final String[] na = script.name.split("|");
						for (final String s: na) {
							map.put(s, i);
						}
					} else {
						map.put(script.name, i);
					}
				}
			}
		}

		return result;
	}

	private int batteryAlarmCnt = -1;
	private int batteryCriticalCnt = -1;
	@Override
	protected void updateBatteryOnUIThread(final int battery) {
		super.updateBatteryOnUIThread(battery);
		if (battery > 30) {
			batteryAlarmCnt = batteryCriticalCnt = -1;
		} else if (mVoiceFeedback != null) {
		 	if ((battery < 10) && ((++batteryCriticalCnt) % 1000) == 0) {
				mVoiceFeedback.playVoiceFeedback(CMD_ERROR_BATTERY_LOW_CRITICAL);
			} else if ((battery < 30) && ((++batteryAlarmCnt) % 1000) == 0) {
				mVoiceFeedback.playVoiceFeedback(CMD_ERROR_BATTERY_LOW);
			}
		}
	}

	@Override
	protected void updateAlarmMessageOnUIThread(final int alarm) {
		super.updateAlarmMessageOnUIThread(alarm);
		switch (alarm) {
		case DroneStatus.ALARM_NON:					// No alert
			break;
		case DroneStatus.ALARM_USER_EMERGENCY:		// User emergency alert
			if (mVoiceFeedback != null) {
				stopSpeechRecognizer();
				final boolean played = mVoiceFeedback.playVoiceFeedback(CMD_ERROR_MOTOR);
				runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
			}
			break;
		case DroneStatus.ALARM_CUTOUT:				// Cut out alert
			if (mVoiceFeedback != null) {
				stopSpeechRecognizer();
				final boolean played = mVoiceFeedback.playVoiceFeedback(CMD_ERROR_MOTOR);
				runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
			}
			break;
		case DroneStatus.ALARM_BATTERY_CRITICAL:	// Critical battery alert
			if (mVoiceFeedback != null) {
				stopSpeechRecognizer();
				final boolean played = mVoiceFeedback.playVoiceFeedback(CMD_ERROR_BATTERY_LOW_CRITICAL);
				runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
			}
			batteryCriticalCnt = 1;
			break;
		case DroneStatus.ALARM_BATTERY:				// Low battery alert
			batteryAlarmCnt = 1;
			if (mVoiceFeedback != null) {
				stopSpeechRecognizer();
				final boolean played = mVoiceFeedback.playVoiceFeedback(CMD_ERROR_BATTERY_LOW);
				runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
			}
			break;
		case DroneStatus.ALARM_DISCONNECTED:		// 切断された
			break;
		default:
			Log.w(TAG, "unexpected alarm state:" + alarm);
			break;
		}
	}

	private Intent mRecognizerIntent;
	private void startSpeechRecognizer() {
		if (DEBUG) Log.v(TAG, "startSpeechRecognizer:");
		final Activity activity = getActivity();
		if ((activity == null) || activity.isFinishing()) return;
		if (!SpeechRecognizer.isRecognitionAvailable(activity)) {
			Log.w(TAG, "isRecognitionAvailable=false");
			return;
		}
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
		if (mOfflineVoiceRecognition) {
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true);
					mSpeechRecognizer.startListening(mRecognizerIntent);
					return;
				}
			} catch (final Exception e) {
				showToast(R.string.error_voice_offline, Toast.LENGTH_LONG);
			}
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			mRecognizerIntent.removeExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE);
		}
		mOfflineVoiceRecognition = false;
		final SharedPreferences pref = getActivity().getPreferences(0);
		pref.edit().putBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, false).apply();
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
		resetVolume();
	}

	private void resetVolume() {
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
			startHeartBeat();
		}

		@Override
		public void onRmsChanged(final float rmsdB) {
//			if (DEBUG) Log.v(TAG, "onRmsChanged:");
		}

		@Override
		public void onBufferReceived(final byte[] buffer) {
//			if (DEBUG) Log.v(TAG, "onBufferReceived:");
		}

		@Override
		public void onEndOfSpeech() {
			if (DEBUG) Log.v(TAG, "onEndOfSpeech:");
			stopHeartBeat();
		}

		@Override
		public void onError(final int error) {
			if (DEBUG) Log.v(TAG, "onError:");
			stopHeartBeat();
			stopMove();
			long cmd;
			switch (error) {
			case SpeechRecognizer.ERROR_AUDIO:
				// 音声データ保存失敗
				cmd = CMD_SR_ERROR_AUDIO;
				showToast(R.string.error_voice_audio, Toast.LENGTH_SHORT);
				break;
			case SpeechRecognizer.ERROR_CLIENT:
				// Android端末内のエラー(その他)
				cmd = CMD_SR_ERROR_CLIENT;
				showToast(R.string.error_voice_system, Toast.LENGTH_SHORT);
				break;
			case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
				// 権限無し
				cmd = CMD_SR_ERROR_INSUFFICIENT_PERMISSIONS;
				showToast(R.string.error_voice_no_permission, Toast.LENGTH_LONG);
				return;
			case SpeechRecognizer.ERROR_NETWORK:
				// ネットワークエラー(その他)
				cmd = CMD_SR_ERROR_NETWORK;
				showToast(R.string.error_voice_network, Toast.LENGTH_SHORT);
				break;
			case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
				// ネットワークタイムアウトエラー
				cmd = CMD_SR_ERROR_NETWORK_TIMEOUT;
				showToast(R.string.error_voice_network_timeout, Toast.LENGTH_SHORT);
				break;
			case SpeechRecognizer.ERROR_NO_MATCH:
				// 音声認識結果無し
				cmd = CMD_SR_ERROR_NO_MATCH;
				showToast(R.string.error_voice_no_command, Toast.LENGTH_LONG);
				break;
			case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
				// RecognitionServiceへ要求出せず
				// 性能が低い端末の場合に起こるらしいので、一旦破棄してから1秒後に再チャレンジ
				cmd = CMD_SR_ERROR_RECOGNIZER_BUSY;
				showToast(R.string.error_voice_unavailable, Toast.LENGTH_SHORT);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						stopSpeechRecognizer();
						runOnUiThread(mStartSpeechRecognizerTask, 1000);
					}
				});
				return;
			case SpeechRecognizer.ERROR_SERVER:
				// Server側からエラー通知
				cmd = CMD_SR_ERROR_SERVER;
				showToast(R.string.error_voice_network_server, Toast.LENGTH_SHORT);
				final SharedPreferences pref = getActivity().getPreferences(0);
				pref.edit().putBoolean(KEY_CONFIG_VOICE_RECOGNITION_PREFER_OFFLINE, false).apply();
				mOfflineVoiceRecognition = false;
				break;
			case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
				// 音声入力無し
				cmd = CMD_SR_ERROR_SPEECH_TIMEOUT;
				showToast(R.string.error_voice_no_input, Toast.LENGTH_SHORT);
				break;
			default:
				cmd = CMD_SR_ERROR_SPEECH_TIMEOUT;
				break;
			}
			resetVolume();
			final boolean played = mVoiceFeedback.playVoiceFeedback(cmd);
			runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
		}

	    @Override
	    public void onResults(final Bundle results) {
	    	// FIXME 飛行可能で無い時は無視する
			final List<String> recData = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
			final float[] conf = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES);
			long cmd = VoiceConst.CMD_NON;
			if (conf != null) {
				// confidenceがある時
				int i = 0;
				for (final String data: recData) {
					final long found = VoiceConst.findCommand(data);
					if (found != VoiceConst.CMD_NON) {
						if (conf[i] > 0.7f) {
							cmd = found;
							break;
						} else if (cmd == VoiceConst.CMD_NON) {
							cmd = found;
						}
					}
					i++;
				}
			} else {
				// confidenceが無い時
				for (final String data: recData) {
					cmd = VoiceConst.findCommand(data);
					if (cmd != VoiceConst.CMD_NON) {
						break;
					}
				}
			}
			if (cmd != VoiceConst.CMD_NON) {
				stopMove();
				removeEvent(mVoiceResetTask);
			}
			switch ((int)(cmd & VoiceConst.CMD_MASK)) {
			case VoiceConst.CMD_STOP:
				if (DEBUG) Log.v(TAG, "ボイスコントロール:stop");
				stopMove();
				setColorFilter(mEmergencyBtn);
				break;
			case VoiceConst.CMD_TAKEOFF:
				if (DEBUG) Log.v(TAG, "ボイスコントロール:離陸");
				takeOff();
				setColorFilter(mTakeOnOffBtn);
				break;
			case VoiceConst.CMD_LANDING:
				if (DEBUG) Log.v(TAG, "ボイスコントロール:着陸");
				landing();
				setColorFilter(mTakeOnOffBtn);
				break;
			case VoiceConst.CMD_FLIP:
				if (DEBUG) Log.v(TAG, "ボイスコントロール:フリップ");
				// FIXME setColorFilterは未処理
				switch ((int)(cmd & 0xff)) {
				case VoiceConst.DIR_FORWARD:
					flip(IFlightController.FLIP_FRONT);
					break;
				case VoiceConst.DIR_RIGHT:
					flip(IFlightController.FLIP_RIGHT);
					break;
				case VoiceConst.DIR_BACKWARD:
					flip(IFlightController.FLIP_BACK);
					break;
				case VoiceConst.DIR_LEFT:
					flip(IFlightController.FLIP_LEFT);
					break;
				}
				break;
			case VoiceConst.CMD_TURN:
			case VoiceConst.CMD_MOVE:
				if (DEBUG) Log.v(TAG, "ボイスコントロール:移動");
				float roll = VoiceConst.getRoll(cmd) * mGamepadSensitivity * mGamepadScaleX;
				float pitch = VoiceConst.getPitch(cmd) * mGamepadSensitivity * mGamepadScaleY;
				float gaz = VoiceConst.getGaz(cmd) * mGamepadSensitivity * mGamepadScaleZ;
				float yaw = VoiceConst.getYaw(cmd) * mGamepadSensitivity * mGamepadScaleR;
				sendMove(roll, pitch, gaz, yaw);
				if ((mDampingRate > 0.0f) && (mDampingRate < 1.0f)) {
					queueEvent(mVoiceResetTask, 300);
				}
				break;
			case VoiceConst.CMD_SCRIPT:
				try {
					startScript(VoiceConst.getScript(cmd));
				} catch (final Exception e) {
					showToast(R.string.error_voice_no_command, Toast.LENGTH_SHORT);
				}
				break;
			case VoiceConst.CMD_CLAW_OPEN:
				if ((mFlightController instanceof FlightControllerMambo)
					&& ((FlightControllerMambo) mFlightController).hasClaw()) {
					((FlightControllerMambo) mFlightController).requestClawOpen();
				}
				break;
			case VoiceConst.CMD_CLAW_CLOSE:
				Log.i(TAG, "CMD_CLAW_CLOSE");
				if ((mFlightController instanceof FlightControllerMambo)
					&& ((FlightControllerMambo) mFlightController).hasClaw()) {
					((FlightControllerMambo) mFlightController).requestClawClose();
				}
				break;
			case VoiceConst.CMD_CLAW_TOGGLE:
				Log.i(TAG, "CMD_CLAW_TOGGLE");
				if ((mFlightController instanceof FlightControllerMambo)
					&& ((FlightControllerMambo) mFlightController).hasClaw()) {
					actionToggle();
				}
				break;
			case VoiceConst.CMD_FIRE:
				Log.i(TAG, "CMD_FIRE");
				if ((mFlightController instanceof FlightControllerMambo)
					&& ((FlightControllerMambo) mFlightController).hasGun()) {
					actionToggle();
				}
				break;
			default:
				showToast(R.string.error_voice_no_command, Toast.LENGTH_SHORT);
				if (DEBUG) {
					for (final String data: recData) {
						Log.i(TAG, "onResults=" + data);
					}
				}
				break;
			}
			resetVolume();
			final boolean played = mVoiceFeedback.playVoiceFeedback(cmd);
			runOnUiThread(mStartSpeechRecognizerTask, played ? 600 : 100);
	    }

		@Override
		public void onPartialResults(final Bundle partialResults) {
			if (DEBUG) Log.v(TAG, "onPartialResults:" + partialResults);
			// これ来たこと無い(Android 7.x @Nexus6p/Nexus5x)
		}

		@Override
		public void onEvent(final int eventType, final Bundle params) {
			if (DEBUG) Log.v(TAG, "onEvent:" + params);
		}
	};

	/**
	 * 一定時間後に音声制御による移動を減衰停止させるためのRunnable
	 */
	private final Runnable mVoiceResetTask = new Runnable() {
		@Override
		public void run() {
			if (damp(mDampingRate)) {
				queueEvent(this, 100);
				return;
			}
			stopMove();
		}
	};
}
