package com.serenegiant.arflight;

/**
 * Created by saki on 2015/08/20.
 */
public interface IAutoFlight {
	public static final int CMD_EMERGENCY = -1;		// 非常停止

	public static final int CMD_TAKEOFF = 1;		// 離陸
	public static final int CMD_LANDING = 2;		// 着陸

	public static final int CMD_UP_DOWN = 3;		// 上昇:gaz>0, 下降: gaz<0
	public static final int CMD_RIGHT_LEFT = 4;		// 右: roll>0,flag=1 左: roll<0,flag=1
	public static final int CMD_FORWARD_BACK = 5;	// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
	public static final int CMD_TURN = 6;			// 右回転: yaw>0, 左回転: ywa<0
	public static final int CMD_COMPASS = 7;		// 北磁極に対する角度を指定-360-360度

	public static final int CMD_FLIP = 100;			// 1:前, 2:後, 3:右, 4:左
	public static final int CMD_CAP = 101;			// -180〜180度

	/**
	 * 自動フライト開始
	 */
	public void play() throws IllegalStateException;

	/**
	 * 自動フライト終了
	 */
	public void stop();

	/**
	 * 自動フライト中かどうか
	 * @return
	 */
	public boolean isPlaying();
}
