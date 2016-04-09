package com.serenegiant.arflight.controllers;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.parrot.arsdk.arcontroller.ARCONTROLLER_DICTIONARY_KEY_ENUM;
import com.parrot.arsdk.arcontroller.ARControllerArgumentDictionary;
import com.parrot.arsdk.arcontroller.ARDeviceController;
import com.parrot.arsdk.ardiscovery.ARDiscoveryDeviceService;
import com.serenegiant.arflight.DeviceConnectionListener;
import com.serenegiant.arflight.FlightControllerListener;
import com.serenegiant.arflight.DataPCMD;
import com.serenegiant.arflight.DroneSettings;
import com.serenegiant.arflight.DroneStatus;
import com.serenegiant.arflight.IFlightController;
import com.serenegiant.arflight.LooperThread;
import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.arflight.configs.ARNetworkConfig;
import com.serenegiant.math.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class FlightControllerNewAPI extends DeviceControllerNewAPI implements IFlightController {
	private static final boolean DEBUG = false;	// FIXME 実働時はfalseにすること
	private static String TAG = FlightControllerNewAPI.class.getSimpleName();

	private LooperThread mFlightCMDThread;

	private final Object mDataSync = new Object();
	private final DataPCMD mDataPCMD = new DataPCMD();

	private final List<FlightControllerListener> mListeners = new ArrayList<FlightControllerListener>();

	protected DroneSettings mSettings;

	public FlightControllerNewAPI(final Context context, final ARDiscoveryDeviceService service, final ARNetworkConfig net_config) {
		super(context, service, net_config);
	}

	protected void onCommandReceived(final ARDeviceController deviceController,
		final ARCONTROLLER_DICTIONARY_KEY_ENUM commandKey,
		final ARControllerArgumentDictionary<Object> args) {

		switch (commandKey) {
		}
	}

	@Override
	protected void internal_start() {
		if (mNetConfig.hasVideo()) {
			// ビデオストリーミング用スレッドを生成&開始
			startVideoThread();
		}
		// 操縦コマンド送信スレッドを生成&開始
		startFlightCMDThread();
	}

	@Override
	protected void internal_stop() {
		requestLanding();
		// 操縦コマンド送信スレッドを終了(終了するまで戻らない)
		stopFlightCMDThread();
		// ビデオストリーミングスレッドを終了(終了するまで戻らない)
		stopVideoThread();
	}

	@Override
	protected void setCountryCode(final String code) {
		mSettings.setCountryCode(code);
	}

	@Override
	protected void setAutomaticCountry(final boolean auto) {
		mSettings.setAutomaticCountry(auto);
	}

	@Override
	public int getState() {
		synchronized (mStateSync) {
			return super.getState() + (((DroneStatus)mStatus).getFlyingState() << 8);
		}
	}

	public boolean isFlying() {
		return ((DroneStatus)mStatus).isFlying();
	}

	@Override
	public int getStillCaptureState() {
		return ((DroneStatus)mStatus).getStillCaptureState();
	}

	@Override
	public int getVideoRecordingState() {
		return ((DroneStatus)mStatus).getVideoRecordingState();
	}

	@Override
	public int getMassStorageId() {
		return ((DroneStatus)mStatus).massStorageId();
	}

	@Override
	public String getMassStorageName() {
		return ((DroneStatus)mStatus).massStorageName();
	}

	@Override
	public boolean needCalibration() {
		return mStatus.needCalibration();
	}

	/** 操縦コマンド送信スレッドを生成&開始 */
	private void startFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "startFlightCMDThread");
		if (mFlightCMDThread != null) {
			mFlightCMDThread.stopThread();
		}
        /* Create the looper thread */
		mFlightCMDThread = new FlightCMDThread((mNetConfig.getPCMDLoopIntervalsMs()));

        /* Start the looper thread. */
		mFlightCMDThread.start();
	}

	/** 操縦コマンド送信を終了(終了するまで戻らない) */
	private void stopFlightCMDThread() {
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:");
        /* Cancel the looper thread and block until it is stopped. */
		if (null != mFlightCMDThread) {
			mFlightCMDThread.stopThread();
			try {
				mFlightCMDThread.join();
				mFlightCMDThread = null;
			} catch (final InterruptedException e) {
				Log.w(TAG, e);
			}
		}
		if (DEBUG) Log.v(TAG, "stopFlightCMDThread:終了");
	}

	/** 映像ストリーミングデータ受信スレッドを開始(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void startVideoThread() {
	}

	/** 映像ストリーミングデータ受信スレッドを終了(このクラス内では何もしないので必要ならばoverrideすること) */
	protected void stopVideoThread() {
	}

	/**
	 * 最大高度設定値を返す
	 * @return
	 */
	@Override
	public AttributeFloat getMaxAltitude() {
		return mSettings.maxAltitude();
	}

	@Override
	public AttributeFloat getMaxTilt() {
		return mSettings.maxTilt();
	}

	@Override
	public AttributeFloat getMaxVerticalSpeed() {
		return mSettings.maxVerticalSpeed();
	}

	@Override
	public AttributeFloat getMaxRotationSpeed() {
		return mSettings.maxRotationSpeed();
	}

	@Override
	public Vector getAttitude(){
		return ((DroneStatus)mStatus).attitude();
	}

	public float getAltitude() {
		return (float)mStatus.altitude();
	}

	/**
	 * モーターの自動カット機能が有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isCutoffMode() {
		return mSettings.cutOffMode();
	}

	/**
	 * 自動離陸モードが有効かどうかを取得する
	 * @return
	 */
	@Override
	public boolean isAutoTakeOffModeEnabled() {
		return mSettings.autoTakeOffMode();
	}

	@Override
	public boolean hasGuard() {
		return mSettings.hasGuard();
	}

	/**
	 * モーターの個数を返す
	 * @return
	 */
	@Override
	public int getMotorNums() {
		return 4;
	}

	@Override
	public AttributeMotor getMotor(final int index) {
		return ((DroneStatus)mStatus).getMotor(index);
	}


//================================================================================
// コールバック関係
//================================================================================
	/**
	 * コールバックリスナーを設定
	 * @param listener
	 */
	@Override
	public void addListener(final DeviceConnectionListener listener) {
		super.addListener(listener);
		if (listener instanceof FlightControllerListener) {
			synchronized (mListeners) {
				mListeners.add((FlightControllerListener) listener);
				callOnFlyingStateChangedUpdate(((DroneStatus)mStatus).getFlyingState());
			}
		}
	}

	/**
	 * 指定したコールバックリスナーを取り除く
	 * @param listener
	 */
	@Override
	public void removeListener(final DeviceConnectionListener listener) {
		synchronized (mListeners) {
			if (listener instanceof FlightControllerListener) {
				mListeners.remove((FlightControllerListener)listener);
			}
		}
		super.removeListener(listener);
	}

	/**
	 * 飛行ステータス変更コールバックを呼び出す
	 * @param state
	 */
	protected void callOnFlyingStateChangedUpdate(final int state) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlyingStateChangedUpdate(state);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * フラットトリム実行完了時のコールバックを呼び出す
	 */
	protected void callOnFlatTrimChanged() {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onFlatTrimChanged();
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション状態が変更された時のコールバックを呼び出す
	 * @param need_calibration
	 */
	protected void callOnCalibrationRequiredChanged(final boolean need_calibration) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationRequiredChanged(need_calibration);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーションを開始/終了した時のコールバックを呼び出す
	 * @param is_start
	 */
	protected void callOnCalibrationStartStop(final boolean is_start) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationStartStop(is_start);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * キャリブレーション中の軸が変更された時のコールバックを呼び出す
	 * @param axis 0:x, 1:y, z:2, 3:none
	 */
	protected void callOnCalibrationAxisChanged(final int axis) {
		synchronized (mListeners) {
			for (final FlightControllerListener listener: mListeners) {
				if (listener != null) {
					try {
						listener.onCalibrationAxisChanged(axis);
					} catch (final Exception e) {
						if (DEBUG) Log.w(TAG, e);
					}
				}
			}
		}
	}

	/**
	 * 静止画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnStillCaptureStateChanged(final int state) {
		final boolean changed = ((DroneStatus)mStatus).setStillCaptureState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onStillCaptureStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 動画撮影ステータスが変化した時のコールバックを呼び出す
	 * @param state
	 */
	protected void callOnVideoRecordingStateChanged(final int state) {
		final boolean changed = ((DroneStatus)mStatus).setVideoRecordingState(state);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onVideoRecordingStateChanged(state);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

	/**
	 * 機体のストレージ状態が変化した時のコールバックを呼び出す
	 * @param mass_storage_id
	 * @param size
	 * @param used_size
	 * @param plugged
	 * @param full
	 * @param internal
	 */
	protected void callOnUpdateStorageState(final int mass_storage_id, final int size, final int used_size, final boolean plugged, final boolean full, final boolean internal) {
		final boolean changed = ((DroneStatus)mStatus).setMassStorageInfo(mass_storage_id, size, used_size, plugged, full, internal);
		if (changed) {
			synchronized (mListeners) {
				for (final FlightControllerListener listener : mListeners) {
					if (listener != null) {
						try {
							listener.onUpdateStorageState(mass_storage_id, size, used_size, plugged, full, internal);
						} catch (final Exception e) {
							if (DEBUG) Log.w(TAG, e);
						}
					}
				}
			}
		}
	}

//********************************************************************************
// 操縦関係
//********************************************************************************
	/**
	 * roll/pitch変更時が移動かどうか
	 * @param flag 1:移動
	 */
	@Override
	public void setFlag(final int flag) {
		synchronized (mDataSync) {
			mDataPCMD.flag = flag == 0 ? 0 : (flag != 0 ? 1 : 0);
		}
	}

	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇
	 */
	@Override
	public void setGaz(final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.gaz = gaz > 100 ? 100 : (gaz < -100 ? -100 : gaz);
		}
	}

	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する
	 * @param roll 負:左, 正:右
	 */
	@Override
	public void setRoll(final float roll) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
		}
	}

	/**
	 * 機体を左右に傾ける
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setRoll(final float roll, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100 ? 100 : (roll < -100 ? -100 : roll);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する
	 * @param pitch
	 */
	@Override
	public void setPitch(final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
		}
	}

	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param move, true:移動
	 */
	@Override
	public void setPitch(final float pitch, final boolean move) {
		synchronized (mDataSync) {
			mDataPCMD.pitch = pitch > 100 ? 100 : (pitch < -100 ? -100 : pitch);
			mDataPCMD.flag = move ? 1 : 0;
		}
	}

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転
	 */
	@Override
	public void setYaw(final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.yaw = yaw > 100 ? 100 : (yaw < -100 ? -100 : yaw);
		}
	}

	/**
	 * 北磁極に対する角度を設定・・・機体側で実装されてない
	 * @param heading
	 */
	@Override
	public void setHeading(final float heading) {
		synchronized (mDataSync) {
			mDataPCMD.heading = heading;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = 1;
		}
	}

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 * @param flag roll/pitchが移動を意味する時1
	 */
	@Override
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, int flag) {
		synchronized (mDataSync) {
			mDataPCMD.roll = roll > 100.0f ? 100.0f : (roll < -100.0f ? -100.0f : roll) ;
			mDataPCMD.pitch = pitch > 100.0f ? 100.0f : (pitch < -100.0f ? -100.0f : pitch) ;
			mDataPCMD.gaz = gaz > 100.0f ? 100.0f : (gaz < -100.0f ? -100.0f : gaz) ;
			mDataPCMD.yaw = yaw > 100.0f ? 100.0f : (yaw < -100.0f ? -100.0f : yaw) ;
			mDataPCMD.flag = flag;
		}
	}

	protected void getPCMD(final DataPCMD dest) {
		if (dest != null) {
			synchronized (mDataSync) {
				dest.set(mDataPCMD);
			}
		}
	}

	/**
	 * 操縦コマンドを送信
	 * @param flag flag to activate roll/pitch movement
	 * @param roll [-100,100]
	 * @param pitch [-100,100]
	 * @param yaw [-100,100]
	 * @param gaz [-100,100]
	 * @param heading [-180,180] (無効みたい)
	 * @return
	 */
	protected abstract boolean sendPCMD(final int flag, final int roll, final int pitch, final int yaw, final int gaz, final int heading);

	/**
	 * 操縦コマンド送信スレッドでのループ内の処理(sendPCMDを呼び出す)
	 * 下位クラスで定期的にコマンド送信が必要ならoverride
	 */
	protected void sendCmdInControlLoop() {
		final int flag;
		float roll, pitch, yaw, gaz, heading;
		synchronized (mDataSync) {
			flag = mDataPCMD.flag;
			roll = mDataPCMD.roll;
			pitch = mDataPCMD.pitch;
			yaw = mDataPCMD.yaw;
			gaz = mDataPCMD.gaz;
			heading = mDataPCMD.heading;
		}
		// 操縦コマンド送信
		sendPCMD(flag, (int) roll, (int) pitch, (int)yaw, (int)gaz, (int)heading);
	}

	/** 操縦コマンドを定期的に送信するためのスレッド */
	protected class FlightCMDThread extends LooperThread {
		private final long intervals_ms;
		public FlightCMDThread(final long _intervals_ms) {
			intervals_ms = _intervals_ms;
		}

		@Override
		public void onLoop() {
			final long lastTime = SystemClock.elapsedRealtime();

			final int state = FlightControllerNewAPI.super.getState();

			if (state == STATE_STARTED) {
				sendCmdInControlLoop();
			}
			// 次の送信予定時間までの休止時間を計算[ミリ秒]
			final long sleepTime = (SystemClock.elapsedRealtime() + intervals_ms) - lastTime;

			try {
				sleep(sleepTime);
			} catch (final InterruptedException e) {
				// ignore
			}
		}
	}
}
