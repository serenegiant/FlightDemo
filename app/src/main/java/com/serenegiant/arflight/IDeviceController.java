package com.serenegiant.arflight;

import java.sql.Date;

/**
 * Created by saki on 2015/08/03.
 */
public interface IDeviceController {
	public static final int FLIP_FRONT = 1;
	public static final int FLIP_BACK = 2;
	public static final int FLIP_RIGHT = 3;
	public static final int FLIP_LEFT = 4;

	/**
	 * コールバックリスナーを設定
	 * @param mListener
	 */
	public void setListener(final DeviceControllerListener mListener);

	public boolean start();
	public void stop();
	public boolean sendDate(Date currentDate);
	public boolean sendTime(Date currentDate);

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
	 * フラットトリム実行(水平方向センサー調整)
	 * @return
	 */
	public boolean sendFlatTrim();

	public boolean sendAnimationsFlip(final int direction);
	/**
	 * 自動で指定した角度回転させる
	 * @param degree -360〜360度
	 * @return
	 */
	public boolean sendAnimationsCap(final int degree);
	/**
	 * roll/pitch変更時が移動なのか機体姿勢変更なのかを指示
	 * @param flag 1:移動, 0:機体姿勢変更
	 */
	public void setFlag(final byte flag);
	/**
	 * 機体の高度を上下させる
	 * @param gaz 負:下降, 正:上昇, -100〜+100
	 */
	public void setGaz(final byte gaz);
	/**
	 * 機体を左右に傾ける。flag=1:左右に移動する, flag=0:機体姿勢変更のみ
	 * @param roll 負:左, 正:右, -100〜+100
	 */
	public void setRoll(final byte roll);
	/**
	 * 機体の機首を上げ下げする。flag=1:前後に移動する, flag=0:機体姿勢変更のみ
	 * @param pitch 負:??? 正:???, -100〜+100
	 */
	public void setPitch(final byte pitch);
	/**
	 * 機体の機首を左右に動かす=水平方向に回転する
	 * @param yaw 負:左回転, 正:右回転, -100〜+100
	 */
	public void setYaw(final byte yaw);
	/**
	 * 北磁極に対する角度を設定・・・でもローリングスパイダーでは動かない
	 * @param psi -360〜360度
	 */
	public void setPsi(final float psi);
}
