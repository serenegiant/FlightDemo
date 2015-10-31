package com.serenegiant.arflight;

import com.serenegiant.arflight.attribute.AttributeFloat;
import com.serenegiant.arflight.attribute.AttributeMotor;
import com.serenegiant.math.Vector;

public interface IFlightController extends IDeviceController {
	// フリップアクションの種類
	public static final int FLIP_FRONT = 1;
	public static final int FLIP_BACK = 2;
	public static final int FLIP_RIGHT = 3;
	public static final int FLIP_LEFT = 4;

	// アニメーション動作の種類
	public static final int ANIM_NON = -1;
	public static final int ANIM_HEADLIGHTS_FLASH = 0;
	public static final int ANIM_HEADLIGHTS_BLINK = 1;
	public static final int ANIM_HEADLIGHTS_OSCILLATION = 2;
	public static final int ANIM_SPIN = 3;
	public static final int ANIM_TAP = 4;
	public static final int ANIM_SLOW_SHAKE = 5;
	public static final int ANIM_METRONOME = 6;
	public static final int ANIM_ONDULATION = 7;
	public static final int ANIM_SPIN_JUMP = 8;
	public static final int ANIM_SPIN_TO_POSTURE = 9;
	public static final int ANIM_SPIRAL = 10;
	public static final int ANIM_SLALOM = 11;
	public static final int ANIM_BOOST = 12;

	// センサーの種類
	/** 慣性測定(ジャイロ/加速度) */
	public static final int SENSOR_IMU = 0;
	/** 高度計(気圧計) */
	public static final int SENSOR_BAROMETER = 1;
	/** 高度計(超音波) */
	public static final int SENSOR_ULTRASOUND = 2;
	/** GPS */
	public static final int SENSOR_GPS = 3;
	/** 磁気センサー(コンパス/姿勢) */
	public static final int SENSOR_MAGNETOMETER= 4;
	/** 垂直カメラ(対地速度検出) */
	public static final int SENSOR_VERTICAL_CAMERA = 5;

	public static final int STATE_MASK_CONNECTION = 0x00ff;
	public static final int STATE_MASK_FLYING = 0xff00;


	public int getAlarm();
	public boolean isFlying();
	public boolean needCalibration();
	public int getStillCaptureState();
	public int getVideoRecordingState();

	public int getMassStorageId();
	public String getMassStorageName();

	/**
	 * 離陸指示
	 * @return
	 */
	public boolean sendTakeoff();

	/**
	 * 着陸指示
	 * @return
	 */
	public boolean sendLanding();

	/**
	 * 非常停止指示
	 * @return
	 */
	public boolean sendEmergency();

	/**
	 * フラットトリム実行(姿勢センサー調整)
	 * @return
	 */
	public boolean sendFlatTrim();

	/**
	 * キャリブレーションを実行(磁気センサー調整)
	 * @param start true: 開始要求, false: 停止要求
	 * @return
	 */
	public boolean sendCalibration(final boolean start);

	/**
	 * 最大高度を設定
	 * @param altitude [m]
	 * @return
	 */
	public boolean sendMaxAltitude(final float altitude);
	public AttributeFloat getMaxAltitude();

	/**
	 * 最大傾斜設定
	 * @param tilt
	 * @return
	 */
	public boolean sendMaxTilt(final float tilt);
	public AttributeFloat getMaxTilt();

	/**
	 * 最大上昇/下降速度を設定
	 * @param speed m/s
	 * @return
	 */
	public boolean sendMaxVerticalSpeed(final float speed);
	public AttributeFloat getMaxVerticalSpeed();

	/**
	 * 最大回転速度
	 * @param speed [度/秒]
	 * @return
	 */
	public boolean sendMaxRotationSpeed(final float speed);
	public AttributeFloat getMaxRotationSpeed();

	/**
	 * 機体姿勢を取得可能かどうか
	 * @return
	 */
	public boolean canGetAttitude();
	/**
	 * 機体姿勢を取得(ラジアン)
	 * x:roll, y:pitch, z:yaw
	 * @return
	 */
	public Vector getAttitude();

	/**
	 * 高度を取得
	 * @return
	 */
	public float getAltitude();
	/**
	 * モーターの個数を返す
	 * @return
	 */
	public int getMotorNums();
	public AttributeMotor getMotor(final int index);

	public boolean isCutoffMode();
	public boolean sendCutOutMode(final boolean enabled);

	public boolean isAutoTakeOffModeEnabled();
	public boolean sendAutoTakeOffMode(final boolean enable);

	public boolean hasGuard();
	public boolean sendHasGuard(final boolean has_guard);
	/**
	 * roll/pitch変更時が移動なのか機体姿勢変更なのかを指示
	 * @param flag 1:移動, 0:機体姿勢変更
	 */
	public void setFlag(final int flag);
	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setGaz(final float gaz);
	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する, flag=0:機体姿勢変更のみ
	 * @param roll 負:左, 正:右, -100〜+100
	 */
	public void setRoll(final float roll);
	/**
	 * 機体を左右に傾ける
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param move, true:移動, false:機体姿勢変更
	 */
	public void setRoll(final float roll, boolean move);
	/**
	 * 機首を上げ下げする。flag=1:前後に移動する, flag=0:機体姿勢変更のみ
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setPitch(final float pitch);
	/**
	 * 機首を上げ下げする
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param move, true:移動, false:機体姿勢変更
	 */
	public void setPitch(final float pitch, boolean move);

	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setYaw(final float yaw);
	/**
	 * 北磁極に対する角度を設定・・・でもローリングスパイダーでは動かない
	 * @param heading -360〜360度
	 */
	public void setHeading(final float heading);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 * @param flag roll/pitchが移動を意味する時1, 機体姿勢変更のみの時は0
	 */
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw, final int flag);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setMove(final float roll, final float pitch, final float gaz, final float yaw);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setMove(final float roll, final float pitch);

	/**
	 * 移動量(傾き)をセット
	 * @param roll 負:左, 正:右, -100〜+100
	 * @param pitch 負:??? 正:???, -100〜+100
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setMove(final float roll, final float pitch, final float gaz);

	/**
	 * 指定した方向にフリップ実行
	 * @param direction = FLIP_FRONT,FLIP_BACK,FLIP_RIGHT,FLIP_LEFT
	 * @return
	 */
	public boolean sendAnimationsFlip(final int direction);

	/**
	 * 自動で指定した角度回転させる
	 * ローリングスパイダー(ミニドローン)は機体側で処理するので回転速度設定に関係なく同じ時間で処理できるが
	 * Bebopは機体側に相当する処理がなくアプリ内で角度と回転速度設定から時間を計算して送信＆待機するので処理時間が変わる。
	 * @param degree -180〜180度
	 * @return
	 */
	public boolean sendAnimationsCap(final int degree);

	/**
	 * 静止画撮影要求
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendTakePicture(final int mass_storage_id);
	/**
	 * 静止画撮影要求
	 * @return
	 */
	public boolean sendTakePicture();

	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @param mass_storage_id
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start, final int mass_storage_id);
	/**
	 * 録画開始停止指示
	 * @param start true: 録画開始, false: 録画終了
	 * @return
	 */
	public boolean sendVideoRecording(final boolean start);

	/**
	 * LEDの明るさをセット
	 * @param left [0,255], 範囲外は256の剰余を適用
	 * @param right [0,255], 範囲外は256の剰余を適用
	 * @return
	 */
	public boolean sendHeadlightsIntensity(final int left, final int right);
	/**
	 * 指定したアニメーション動作を開始。全部動くんかな?
	 * 共通のコマンドやけどJumpingSumoでしか動かないような予感。
	 * @param animation [0,12], ANIM_XXX定数
	 * @return
	 */
	public boolean sendStartAnimation(final int animation);
	/**
	 * 指定したアニメーション動作を停止。全部動くんかな?
	 * @param animation [0,12], ANIM_XXX定数
	 * @return
	 */
	public boolean sendStopAnimation(final int animation);
	/**
	 * 実行中のアニメーション動作を全て停止させる
	 * @return
	 */
	public boolean sendStopAllAnimation();
}
