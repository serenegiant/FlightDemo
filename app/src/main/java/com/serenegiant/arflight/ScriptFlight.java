package com.serenegiant.arflight;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.serenegiant.lang.script.ASTParse;
import com.serenegiant.lang.script.Script;
import com.serenegiant.lang.script.ScriptVisitorImpl;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.List;

public class ScriptFlight implements IAutoFlight {
	private static final boolean DEBUG = false;				// FIXME 実働時はfalseにすること
	private static final boolean DEBUG_PRESETFUNC = true;	// FIXME 実働時はfalseにすること
	private static final String TAG = "ScriptFlight";

	private final Object mSync = new Object();
	private final AutoFlightListener mAutoFlightListener;
	private final Handler mHandler;	// プライベートスレッドでの実行用

	private volatile boolean mIsPlayback;	// 再生中
	private ASTParse mASTParse;
	private ScriptVisitorImpl mVisitor;
	private final int[] cmd_values = new int[4];

	public ScriptFlight(final AutoFlightListener listener) {
		if (listener == null) {
			throw new NullPointerException("AutoFlightListenerコールバックリスナーが設定されてない");
		}
		mAutoFlightListener = listener;
		final HandlerThread thread = new HandlerThread("ScriptFlight");
		thread.start();
		mHandler = new Handler(thread.getLooper());

		ScriptVisitorImpl.resetPresetFunc();
		// プリセット関数を追加する
		// 非常停止
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("emergency", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute emergency:" + args);
					if (mAutoFlightListener.onStep(CMD_EMERGENCY, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 離陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("takeoff", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute takeoff:" + args);
					if (mAutoFlightListener.onStep(CMD_TAKEOFF, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 着陸
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("landing", "") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute landing:" + args);
					if (mAutoFlightListener.onStep(CMD_LANDING, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 移動
		// roll, pitch, gaz, yaw
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("move", "iiii") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute move:" + args);
					cmd_values[0] = args.get(0).intValue();	// roll
					cmd_values[1] = args.get(1).intValue();	// pitch
					cmd_values[2] = args.get(2).intValue();	// gaz
					cmd_values[3] = args.get(3).intValue();	// yaw
					if (mAutoFlightListener.onStep(CMD_MOVE4, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 上下移動
		// 上昇:gaz>0, 下降: gaz<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("updown", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute updown:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_UP_DOWN, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 左右移動
		// 右: roll>0,flag=1 左: roll<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("rightleft", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute rightleft:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_RIGHT_LEFT, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 前後移動
		// 前進: pitch>0,flag=1, 後退: pitch<0,flag=1
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("frontback", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute frontback:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_FORWARD_BACK, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 左右回転
		// 右回転: yaw>0, 左回転: ywa<0
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("turn", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute turn:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_TURN, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// 北磁極に対する角度を指定-360-360度 ローリングスパイダーは動かない
		// compass
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("compass", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute compass:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_COMPASS, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// flip
		// 1:前, 2:後, 3:右, 4:左
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("flip", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute flip:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_FLIP, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
		// cap
		// -180〜180度
		ScriptVisitorImpl.addPresetFunc(new ScriptVisitorImpl.FuncDef("cap", "i") {
			@Override
			protected Object execute(final List<Number> args) {
				try {
					if (DEBUG_PRESETFUNC) Log.v(TAG, "execute cap:" + args);
					cmd_values[0] = args.get(0).intValue();
					if (mAutoFlightListener.onStep(CMD_CAP, cmd_values, getCurrentTime())) {
						stop();
					}
				} catch (final Exception e) {
					Log.w(TAG, e);
				}
				return null;
			}
		});
	}

	/**
	 * 自動フライトの準備
	 * @param args
	 * @throws RuntimeException
	 */
	@Override
	public void prepare(final Object...args) throws RuntimeException {
		if (DEBUG) Log.v(TAG, "prepare:");
		final InputStream in = (args != null) && (args.length > 0) && (args[0] instanceof InputStream) ? new BufferedInputStream((InputStream)args[0]) : null;
		if (in == null) throw new IllegalArgumentException("InputStreamがセットされていない");

		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (DEBUG) Log.v(TAG, "prepare#run");
				final Script script = new Script(in);
				try {
					final ASTParse parse = script.Parse();
					synchronized (mSync) {
						mASTParse = parse;
						switch (args.length) {
//						case 0: // 0個の時はInputStreamも無いのでここには来ない
						case 1:
							mVisitor = new ScriptVisitorImpl();
							break;
						case 2:
							mVisitor = new ScriptVisitorImpl((Number) args[1]);
							break;
						case 3:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2]);
							break;
						case 4:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3]);
							break;
						case 5:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3],
								(double) args[4]);
							break;
						case 6:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3],
								(Number) args[4], (Number) args[5]);
							break;
						case 7:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3],
								(Number) args[4], (Number) args[5], (Number) args[6]);
							break;
						case 8:
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3],
								(Number) args[4], (Number) args[5], (Number) args[6], (Number) args[7]);
							break;
						default:	// 最大で引数は8個まで
							mVisitor = new ScriptVisitorImpl((Number) args[1], (Number) args[2], (Number) args[3],
								(Number) args[4], (Number) args[5], (Number) args[6], (Number) args[7], (Number) args[8]);
							break;
						}
					}
					try {
						mAutoFlightListener.onPrepared();
					} catch (final Exception e) {
						Log.w(TAG, e);
					}
				} catch (final Exception e) {
					synchronized (mSync) {
						mASTParse = null;
						mVisitor = null;
					}
					mAutoFlightListener.onError(e);
				}
				if (DEBUG) Log.v(TAG, "prepare#run:終了");
			}
		});
	}

	@Override
	public void play() throws IllegalStateException {
		if (DEBUG) Log.v(TAG, "play:");
		if (mIsPlayback) {
			throw new IllegalStateException("既に実行中");
		}
		synchronized (mSync) {
			if ((mASTParse == null) || (mVisitor == null)) {
				throw new IllegalStateException("prepareが呼ばれてない");
			}
			mIsPlayback = true;
		}
		mHandler.post(mPlaybackRunnable);
	}

	@Override
	public void stop() {
		if (DEBUG) Log.v(TAG, "stop:");
		synchronized (mSync) {
			if (mVisitor != null) {
				mVisitor.requestStop();
			}
			mIsPlayback = false;
			mSync.notifyAll();
		}
		mHandler.removeCallbacks(mPlaybackRunnable);
	}

	@Override
	public boolean isPrepared() {
		synchronized (mSync) {
			return !mIsPlayback && (mASTParse != null) && (mVisitor != null);
		}
	}

	@Override
	public boolean isPlaying() {
		return mIsPlayback;
	}

	/**
	 * 関係するリソースを破棄する
	 */
	@Override
	public void release() {
		stop();
		mHandler.getLooper().quit();
	}

	private long mStartTime;
	private long getCurrentTime() {
		return System.currentTimeMillis() - mStartTime;
	}

	/**
	 * コマンド再生スレッドの実行用Runnable
	 */
	private final Runnable mPlaybackRunnable = new Runnable() {
		@Override
		public void run() {
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run");
			try {
				mAutoFlightListener.onStart();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			mStartTime = System.currentTimeMillis();
			try {
				// スクリプト実行
				mASTParse.jjtAccept(mVisitor, null);
			} catch (final Exception e) {
				mAutoFlightListener.onError(e);
			} finally {
				stop();
			}
			synchronized (mSync) {
				mVisitor = null;
			}
			try {
				mAutoFlightListener.onStop();
			} catch (final Exception e) {
				Log.w(TAG, e);
			}
			if (DEBUG) Log.v(TAG, "mPlaybackRunnable#run:終了");
		}
	};
}
