package com.serenegiant.flightdemo;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlightRecorder {
	private static final boolean DEBUG = true;
	private static final String TAG = "FlightRecorder";

	private final List<String> mRecords = new ArrayList<String>(100);

	public static final int CMD_EMERGENCY = -1;		// 非常停止

	public static final int CMD_TAKEOFF = 1;		// 離陸
	public static final int CMD_LANDING = 2;		// 着陸

	public static final int CMD_UP = 3;				// 上昇: gaz>0
	public static final int CMD_DOWN = 4;			// 下降: gaz<0
	public static final int CMD_RIGHT = 5;			// 右: yaw>0
	public static final int CMD_LEFT = 6;			// 左: yaw<0
	public static final int CMD_FORWARD = 7;		// 前進: pitch>0,flag=1
	public static final int CMD_BACK = 8;			// 後退: pitch<0,flag=1
	public static final int CMD_ROLL_RIGHT = 9;		// 右回転: roll>0,flag=1
	public static final int CMD_ROLL_LEFT = 10;		// 左回転: roll<0,flag=1

	public static final int CMD_FLIP_RIGHT = 100;	// 右フリップ
	public static final int CMD_FLIP_LEFT = 101;	// 左フリップ
	public static final int CMD_FLIP_FORWARD = 102;	// 前方フリップ
	public static final int CMD_FLIP_BACK = 103;	// 後方フリップ

	private long mStartTime;	// 記録開始時刻[ミリ秒]
	private int mCurrentPos;	// 次の読み込み位置(再生時)

	/**
	 * 指定したパスのファイルにコマンドを書き出す
	 * @param path
	 */
	public synchronized void save(final String path) {
		try {
			final BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			try {
				for (String line: mRecords) {
					writer.write(line);
					writer.newLine();
				}
				writer.flush();
			} catch (IOException e) {
				Log.w(TAG, e);
			} finally {
				writer.close();
			}
		} catch (FileNotFoundException e) {
			Log.w(TAG, e);
		} catch (IOException e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * 指定したパスのファイルからコマンドを読み込む
	 * @param path
	 */
	public synchronized void load(final String path) {
		try {
			final BufferedReader reader = new BufferedReader(new FileReader(path));
			try {
				mRecords.clear();
				for (String line = reader.readLine(); !TextUtils.isEmpty(line); line = reader.readLine()) {
					mRecords.add(line);
				}
			} catch (IOException e) {
				Log.w(TAG, e);
			} finally {
				reader.close();
			}
		} catch (FileNotFoundException e) {
			Log.w(TAG, e);
		} catch (IOException e) {
			Log.w(TAG, e);
		}
	}

	/**
	 * コマンドをクリア
	 */
	public synchronized void clear() {
		mRecords.clear();
		mStartTime = System.currentTimeMillis();
		mCurrentPos = 0;
	}

	/**
	 * 記録されているコマンドの数を取得
	 * @return
	 */
	public synchronized int size() {
		return mRecords.size();
	}

	/**
	 * 記録開始
	 */
	public synchronized void start() {
		mStartTime = System.currentTimeMillis();
	}

	/**
	 * 記録終了
	 */
	public synchronized void stop() {
		mCurrentPos = 0;
	}

	/**
	 * 指定したコマンドを記録
	 * @param cmd CMD_XXX定数
	 * @param value, -100〜100, 0は移動終了
	 */
	public synchronized void record(final int cmd, final int value) {
		final String cmd_str = String.format("%d,%d,%d", cmd, value, System.currentTimeMillis() - mStartTime);
		mRecords.add(cmd_str);
		mCurrentPos = mRecords.size() - 1;
	}

	/**
	 * 次のコマンドの位置を取得
	 * @return
	 */
	public int pos() {
		return mCurrentPos;
	}

	/**
	 * コマンドの読み込み位置を変更
	 * @param pos
	 */
	public void pos(int pos) {
		mCurrentPos = pos;
	}

	/**
	 * 指定した時刻にコマンドの読み取り位置を進める
	 * @param time
	 * @return 次の読み込むコマンドの時刻を返す
	 */
	public long seek(final long time) {
		mCurrentPos = 0;
		String[] record;
		int pos = -1;
		for (String line: mRecords) {
			try {
				pos++;
				record = line.split(",");
				final long t = Long.parseLong(record[2]);
				if (time <= t) {
					mCurrentPos = pos;
					return t;
				}
			} catch (final NumberFormatException e) {
				Log.w(TAG, e);
				break;
			} catch (final NullPointerException e) {
				Log.w(TAG, e);
				break;
			}
		}
		return 0;
	}

	/**
	 * 記録したコマンドの受け取り用クラス
	 */
	public static final class CmdRec {
		public int cmd;
		public int value;
		public long time;
	}

	/**
	 * 記録したコマンドを取得して１ステップ進める
	 * @param cmd
	 * @return コマンドを取得できればfalse, 取得できなければtrue
	 */
	public synchronized boolean step(final CmdRec cmd) {
		try {
			cmd.cmd = 0;
			if ((mCurrentPos >= 0) && (mCurrentPos < mRecords.size())) {
				final String line = mRecords.get(mCurrentPos++);
				if (!TextUtils.isEmpty(line)) {
					final String[] record = line.split(",");
					try {
						cmd.cmd = Integer.parseInt(record[0]);
						cmd.value = Integer.parseInt(record[1]);
						cmd.time = Long.parseLong(record[2]);
						return false;
					} catch (final NumberFormatException e) {
						cmd.cmd = 0;
						Log.w(TAG, e);
					}
				}
			}
		} catch (final NullPointerException e) {
			Log.w(TAG, e);
		}
		return true;
	}

}
