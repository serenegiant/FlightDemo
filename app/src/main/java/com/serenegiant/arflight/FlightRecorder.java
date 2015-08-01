package com.serenegiant.arflight;

import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FlightRecorder {
	private static final boolean DEBUG = true;
	private static final String TAG = "FlightRecorder";

	public interface PlaybackListener {
		public void onStart();

		/**
		 * 再生時のコールバック
		 * @param cmd
		 * @param value
		 * @param t
		 * @return trueを返すと終了する
		 */
		public boolean onStep(final int cmd, final int value, final long t);
		public void onStop();
	}

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

	private final Object mSync = new Object();
	private final List<String> mRecords = new ArrayList<String>(100);
	private final Object mPlaybackSync = new Object();	// PlaybackListener同期用
	private PlaybackListener mPlaybackListener;

	private volatile boolean mIsRecording;	// 記録中
	private volatile boolean mIsPlayback;	// 再生中
	private long mStartTime;	// 記録開始時刻[ミリ秒]
	private int mCurrentPos;	// 次の読み込み位置(再生時)


	public void setPlaybackListener(final PlaybackListener listener) {
		synchronized (mPlaybackSync) {
			mPlaybackListener = listener;
		}
	}

	public PlaybackListener getPlaybackListener() {
		synchronized (mPlaybackSync) {
			return mPlaybackListener;
		}
	}

	/**
	 * 指定したパスのファイルにコマンドを書き出す
	 * @param path
	 */
	public void save(final String path) {
		if (DEBUG) Log.v(TAG, "save:" + path);
		int cnt = 0;
		try {
			final List<String> copy = new ArrayList<String>();
			synchronized (mSync) {
				copy.addAll(mRecords);
			}
			final BufferedWriter writer = new BufferedWriter(new FileWriter(path));
			try {
				for (String line: copy) {
					writer.write(line);
					writer.newLine();
					if (DEBUG) Log.v(TAG, String.format("%5d)%s", cnt++, line));
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
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		if (DEBUG) Log.v(TAG, "save:finished:" + cnt);
	}

	/**
	 * 指定したパスのファイルからコマンドを読み込む
	 * @param path
	 * @throws IllegalStateException 再生中ならIllegalStateExceptionを生成する
	 */
	public void load(final String path) throws IllegalStateException {
		load(new File(path));
	}

	/**
	 * 指定したパスのファイルからコマンドを読み込む
	 * @param path
	 * @throws IllegalStateException 再生中ならIllegalStateExceptionを生成する
	 */
	public void load(final File path) throws IllegalStateException {
		if (mIsPlayback) throw new IllegalStateException("再生中だよ");
		if (DEBUG) Log.v(TAG, "load:" + path);
		int cnt = 0;
		try {
			final List<String> copy = new ArrayList<String>();
			final CmdRec cmd = new CmdRec();
			long lastTime = 0;
			final BufferedReader reader = new BufferedReader(new FileReader(path));
			try {
				for (String line = reader.readLine(); !TextUtils.isEmpty(line); line = reader.readLine()) {
					if (!parseCmd(line, cmd)) {
						copy.add(line);
						if (cmd.time > lastTime) {
							lastTime = cmd.time;
						}
					}
					if (DEBUG) Log.v(TAG, String.format("%5d)%s", cnt++, line));
				}
			} catch (IOException e) {
				Log.w(TAG, e);
			} finally {
				reader.close();
			}
			synchronized (mSync) {
				mRecords.clear();
				mRecords.addAll(copy);
				mStartTime = System.currentTimeMillis() - lastTime;
			}
		} catch (FileNotFoundException e) {
			Log.w(TAG, e);
		} catch (IOException e) {
			Log.w(TAG, e);
		} catch (Exception e) {
			Log.w(TAG, e);
		}
		if (DEBUG) Log.v(TAG, "load:finished:" + cnt);
	}

	/**
	 * コマンドをクリア
	 */
	public void clear() {
		if (DEBUG) Log.v(TAG, "clear:");
		synchronized (mSync) {
			mRecords.clear();
			mStartTime = System.currentTimeMillis();
			mCurrentPos = 0;
		}
	}

	/**
	 * 記録されているコマンドの数を取得
	 * @return
	 */
	public int size() {
		synchronized (mSync) {
			return mRecords.size();
		}
	}

	/**
	 * 記録開始
	 * @throws IllegalStateException 再生中ならIllegalStateExceptionを生成する
	 */
	public void start() throws IllegalStateException {
		if (DEBUG) Log.v(TAG, "start:");
		synchronized (mSync) {
			if (mIsPlayback) throw new IllegalStateException("再生中だよ");
			if (!mIsRecording) {
				mStartTime = System.currentTimeMillis();
				mIsRecording = true;
			}
		}
	}

	/**
	 * 記録・コマンド再生終了
	 */
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
		synchronized (mSync) {
			mIsRecording = false;
			mIsPlayback = false;
			mSync.notifyAll();
		}
	}

	/**
	 * コマンド再生開始
	 * @throws IllegalStateException 記録中ならIllegalStateExceptionを生成する
	 */
	public void play() throws IllegalStateException {
		if (DEBUG) Log.v(TAG, "play:");
		synchronized (mSync) {
			if (mIsRecording) throw new IllegalStateException("記録中だよ");
			if (!mIsPlayback) {
				mIsPlayback = true;
				new Thread(mPlaybackRunnable, "PlaybackTask").start();
			}
		}
	}

	/**
	 * 記録中かどうかを取得
	 * @return
	 */
	public boolean isRecording() {
		synchronized (mSync) {
			return mIsRecording;
		}
	}

	/**
	 * 再生中かどうかを取得
	 * @return
	 */
	public boolean isPlaying() {
		synchronized (mSync) {
			return mIsPlayback;
		}
	}

	/**
	 * 指定したコマンドを記録
	 * 記録中でなければ無視する
	 * @param cmd CMD_XXX定数
	 * @param value, -100〜100, 0は移動終了
	 */
	public void record(final int cmd, final int value) {
		synchronized (mSync) {
			if (mIsRecording) {
				final String cmd_str = String.format("%d,%d,%d", cmd, value, System.currentTimeMillis() - mStartTime);
				mRecords.add(cmd_str);
				mCurrentPos = mRecords.size() - 1;
			}
		}
	}

	/**
	 * 指定したコマンドを記録(値を指定しなくていいい場合)
	 * 記録中でなければ無視する
	 * @param cmd CMD_XXX定数
	 */
	public void record(final int cmd) {
		synchronized (mSync) {
			if (mIsRecording) {
				final String cmd_str = String.format("%d,%d,%d", cmd, 0, System.currentTimeMillis() - mStartTime);
				mRecords.add(cmd_str);
				mCurrentPos = mRecords.size() - 1;
			}
		}
	}

	/**
	 * 次のコマンドの位置を取得
	 * @return
	 */
	public int pos() {
		synchronized (mSync) {
			return mCurrentPos;
		}
	}

	/**
	 * コマンドの読み込み位置を変更
	 * @param pos
	 */
	public void pos(int pos) {
		if (DEBUG) Log.v(TAG, "pos:" + pos);
		synchronized (mSync) {
			mCurrentPos = pos;
		}
	}

	/**
	 * 指定した時刻にコマンドの読み取り位置を進める
	 * @param time
	 * @return 次の読み込むコマンドの時刻を返す
	 */
	public long seek(final long time) {
		if (DEBUG) Log.v(TAG, "seek:" + time);
		synchronized (mSync) {
			mCurrentPos = 0;
			String[] record;
			int pos = -1;
			for (String line : mRecords) {
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
	private boolean step(final CmdRec cmd) {
		try {
			cmd.cmd = 0;
			String line = null;
			synchronized (mSync) {
				if ((mCurrentPos >= 0) && (mCurrentPos < mRecords.size())) {
					line = mRecords.get(mCurrentPos++);
				}
			}
			return parseCmd(line, cmd);
		} catch (final NullPointerException e) {
			Log.w(TAG, e);
		}
		return true;
	}

	/**
	 * 文字列1レコード分を解析する
	 * @param line
	 * @param cmd
	 * @return
	 */
	private boolean parseCmd(final String line, final CmdRec cmd) {
		if (!TextUtils.isEmpty(line) && (cmd != null)) {
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
		return true;
	}

	/**
	 * コマンド再生スレッドの実行用Runnable
	 */
	private final Runnable mPlaybackRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mPlaybackSync) {
				if (mPlaybackListener != null) {
					try {
						mPlaybackListener.onStart();
					} catch (Exception e) {
						Log.w(TAG, e);
					}
				}
			}
			final CmdRec cmd = new CmdRec();
			final long start_time = System.currentTimeMillis();
			long current_time;
			for ( ; mIsPlayback; ) {
				if (!step(cmd)) {
					// 記録されているコマンドを読み込めた時
					current_time = System.currentTimeMillis() - start_time;
					if (cmd.time > current_time) {
						synchronized (mSync) {
							try {
								mSync.wait(cmd.time - current_time);
							} catch (InterruptedException e) {
							}
						}
					}
					if (!mIsPlayback) break;
					synchronized (mPlaybackSync) {
						if (mPlaybackListener != null) {
							try {
								if (mPlaybackListener.onStep(cmd.cmd, cmd.value, cmd.time)) {
									// trueが返ってきたので終了する
									break;
								}
							} catch (Exception e) {
								Log.w(TAG, e);
							}
						}
					}
				} else {
					// コマンドが無かった時
					break;
				}
			}
			mIsPlayback = false;
			synchronized (mPlaybackSync) {
				if (mPlaybackListener != null) {
					try {
						mPlaybackListener.onStop();
					} catch (Exception e) {
						Log.w(TAG, e);
					}
				}
			}
		}
	};
}
